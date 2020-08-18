package com.etheller.warsmash.parsers.mdlx;

import java.io.IOException;

import com.etheller.warsmash.parsers.mdlx.mdl.MdlTokenInputStream;
import com.etheller.warsmash.parsers.mdlx.mdl.MdlTokenOutputStream;
import com.etheller.warsmash.util.MdlUtils;
import com.etheller.warsmash.util.ParseUtils;
import com.google.common.io.LittleEndianDataOutputStream;
import com.hiveworkshop.util.BinaryReader;

public class MdlxAttachment extends MdlxGenericObject {
	public String path = "";
	public int attachmentId = 0;

	public MdlxAttachment() {
		super(0x800);
	}

	public void readMdx(final BinaryReader reader, final int version) throws IOException {
		final int position = reader.position();
		final long size = reader.readUInt32();

		super.readMdx(reader, version);

		this.path = reader.read(260);
		this.attachmentId = reader.readInt32();

		this.readTimelines(reader, size - (reader.position() - position));
	}

	@Override
	public void writeMdx(final LittleEndianDataOutputStream stream, final int version) throws IOException {
		ParseUtils.writeUInt32(stream, getByteLength(version));

		super.writeMdx(stream, version);

		final byte[] bytes = this.path.getBytes(ParseUtils.UTF8);
		stream.write(bytes);
		for (int i = 0; i < (260 - bytes.length); i++) {
			stream.write((byte) 0);
		}
		stream.writeInt(this.attachmentId); // Used to be Int32 in JS
		
		this.writeNonGenericAnimationChunks(stream);
	}

	@Override
	public void readMdl(final MdlTokenInputStream stream, final int version) throws IOException {
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
				throw new IOException("Unknown token in Attachment " + this.name + ": " + token);
			}
		}
	}

	@Override
	public void writeMdl(final MdlTokenOutputStream stream, final int version) throws IOException {
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
