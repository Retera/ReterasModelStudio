package com.etheller.warsmash.parsers.mdlx;

import java.io.IOException;

import com.etheller.warsmash.parsers.mdlx.mdl.MdlTokenInputStream;
import com.etheller.warsmash.parsers.mdlx.mdl.MdlTokenOutputStream;
import com.etheller.warsmash.util.MdlUtils;
import com.google.common.io.LittleEndianDataOutputStream;
import com.hiveworkshop.util.BinaryReader;

public class MdlxBone extends MdlxGenericObject {
	public int geosetId = -1;
	public int geosetAnimationId = -1;

	public MdlxBone() {
		super(0x100);
	}

	public void readMdx(final BinaryReader reader, final int version) throws IOException {
		super.readMdx(reader, version);

		this.geosetId = reader.readInt32();
		this.geosetAnimationId = reader.readInt32();
	}

	@Override
	public void writeMdx(final LittleEndianDataOutputStream stream, final int version) throws IOException {
		super.writeMdx(stream, version);
		stream.writeInt(this.geosetId);
		stream.writeInt(this.geosetAnimationId);
	}

	@Override
	public void readMdl(final MdlTokenInputStream stream, final int version) {
		for (String token : super.readMdlGeneric(stream)) {
			if (MdlUtils.TOKEN_GEOSETID.equals(token)) {
				token = stream.read();

				if (MdlUtils.TOKEN_MULTIPLE.equals(token)) {
					this.geosetId = -1;
				}
				else {
					this.geosetId = Integer.parseInt(token);
				}
			}
			else if (MdlUtils.TOKEN_GEOSETANIMID.equals(token)) {
				token = stream.read();

				if (MdlUtils.TOKEN_NONE.equals(token)) {
					this.geosetAnimationId = -1;
				}
				else {
					this.geosetAnimationId = Integer.parseInt(token);
				}
			}
			else {
				throw new RuntimeException("Unknown token in Bone " + this.name + ": " + token);
			}
		}
	}

	@Override
	public void writeMdl(final MdlTokenOutputStream stream, final int version) throws IOException {
		stream.startObjectBlock(MdlUtils.TOKEN_BONE, this.name);
		this.writeGenericHeader(stream);

		if (this.geosetId == -1) {
			stream.writeAttrib(MdlUtils.TOKEN_GEOSETID, MdlUtils.TOKEN_MULTIPLE);
		}
		else {
			stream.writeAttrib(MdlUtils.TOKEN_GEOSETID, this.geosetId);
		}
		if (this.geosetAnimationId == -1) {
			stream.writeAttrib(MdlUtils.TOKEN_GEOSETANIMID, MdlUtils.TOKEN_NONE);
		}
		else {
			stream.writeAttrib(MdlUtils.TOKEN_GEOSETANIMID, this.geosetAnimationId);
		}

		this.writeGenericTimelines(stream);
		stream.endBlock();
	}

	@Override
	public long getByteLength(final int version) {
		return 8 + super.getByteLength(version);
	}
}
