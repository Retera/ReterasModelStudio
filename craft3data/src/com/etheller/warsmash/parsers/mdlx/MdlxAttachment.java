package com.etheller.warsmash.parsers.mdlx;

import com.etheller.warsmash.parsers.mdlx.mdl.MdlTokenInputStream;
import com.etheller.warsmash.parsers.mdlx.mdl.MdlTokenOutputStream;
import com.etheller.warsmash.util.MdlUtils;
import com.hiveworkshop.util.BinaryReader;
import com.hiveworkshop.util.BinaryWriter;

public class MdlxAttachment extends MdlxGenericObject {
	public String path = "";
	public int attachmentId = 0;

	public MdlxAttachment() {
		super(0x800);
	}

	@Override
	public void readMdx(final BinaryReader reader, final int version) {
		final int position = reader.position();
		final long size = reader.readUInt32();

		super.readMdx(reader, version);

		this.path = reader.read(260);
		this.attachmentId = reader.readInt32();

		this.readTimelines(reader, size - (reader.position() - position));
	}

	@Override
	public void writeMdx(final BinaryWriter writer, final int version) {
		writer.writeUInt32(getByteLength(version));

		super.writeMdx(writer, version);

		writer.writeWithNulls(path, 260);
		writer.writeInt32(this.attachmentId);
		
		this.writeNonGenericAnimationChunks(writer);
	}

	@Override
	public void readMdl(final MdlTokenInputStream stream, final int version) {
		for (final String token : super.readMdlGeneric(stream)) {
			if (MdlUtils.TOKEN_ATTACHMENT_ID.equals(token)) {
				this.attachmentId = stream.readInt();
			}
			else if (MdlUtils.TOKEN_PATH.equals(token)) {
				this.path = stream.read();
			}
			else if (MdlUtils.TOKEN_VISIBILITY.equals(token)) {
				this.readTimeline(stream, AnimationMap.KATV);
			}
			else {
				throw new RuntimeException("Unknown token in Attachment " + this.name + ": " + token);
			}
		}
	}

	@Override
	public void writeMdl(final MdlTokenOutputStream stream, final int version) {
		stream.startObjectBlock(MdlUtils.TOKEN_ATTACHMENT, this.name);
		this.writeGenericHeader(stream);

		// flowtsohg asks in his JS:
		// Is this needed? MDX supplies it, but MdlxConv does not use it.
		// Retera:
		// I tried to preserve it when it was shown, but omit it when it was omitted
		// for MDL in Matrix Eater. Matrix Eater's MDL -> MDX is generating them
		// and discarding what was read from the MDL. The Matrix Eater is notably
		// buggy from a cursory read through, and would always omit AttachmentID 0
		// in MDL output.
		stream.writeAttrib(MdlUtils.TOKEN_ATTACHMENT_ID, this.attachmentId);

		if ((this.path != null) && (this.path.length() > 0)) {
			stream.writeStringAttrib(MdlUtils.TOKEN_PATH, this.path);
		}

		this.writeTimeline(stream, AnimationMap.KATV);

		this.writeGenericTimelines(stream);
		stream.endBlock();
	}

	@Override
	public long getByteLength(final int version) {
		return 268 + super.getByteLength(version);
	}
}
