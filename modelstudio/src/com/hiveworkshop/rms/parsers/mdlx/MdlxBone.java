package com.hiveworkshop.rms.parsers.mdlx;

import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlTokenInputStream;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlTokenOutputStream;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.util.BinaryReader;
import com.hiveworkshop.rms.util.BinaryWriter;

public class MdlxBone extends MdlxGenericObject {
	public int geosetId = -1;
	public int geosetAnimationId = -1;

	public MdlxBone() {
		super(0x100);
	}

	@Override
	public void readMdx(final BinaryReader reader, final int version) {
		super.readMdx(reader, version);

		geosetId = reader.readInt32();
		geosetAnimationId = reader.readInt32();
	}

	@Override
	public void writeMdx(final BinaryWriter writer, final int version) {
		super.writeMdx(writer, version);

		writer.writeInt32(geosetId);
		writer.writeInt32(geosetAnimationId);
	}

	@Override
	public void readMdl(final MdlTokenInputStream stream, final int version) {
		for (String token : super.readMdlGeneric(stream)) {
			if (MdlUtils.TOKEN_GEOSETID.equals(token)) {
				token = stream.read();

				if (MdlUtils.TOKEN_MULTIPLE.equals(token)) {
					geosetId = -1;
				}
				else {
					geosetId = Integer.parseInt(token);
				}
			}
			else if (MdlUtils.TOKEN_GEOSETANIMID.equals(token)) {
				token = stream.read();

				if (MdlUtils.TOKEN_NONE.equals(token)) {
					geosetAnimationId = -1;
				}
				else {
					geosetAnimationId = Integer.parseInt(token);
				}
			}
			else {
				ExceptionPopup.addStringToShow("Line " + stream.getLineNumber() + ": Unknown token in Bone " + name + ": " + token);
			}
		}
	}

	@Override
	public void writeMdl(final MdlTokenOutputStream stream, final int version) {
		stream.startObjectBlock(MdlUtils.TOKEN_BONE, name);
		writeGenericHeader(stream);

		if (geosetId == -1) {
			stream.writeAttrib(MdlUtils.TOKEN_GEOSETID, MdlUtils.TOKEN_MULTIPLE);
		}
		else {
			stream.writeAttrib(MdlUtils.TOKEN_GEOSETID, geosetId);
		}
		if (geosetAnimationId == -1) {
			stream.writeAttrib(MdlUtils.TOKEN_GEOSETANIMID, MdlUtils.TOKEN_NONE);
		}
		else {
			stream.writeAttrib(MdlUtils.TOKEN_GEOSETANIMID, geosetAnimationId);
		}

		writeGenericTimelines(stream);
		stream.endBlock();
	}

	@Override
	public long getByteLength(final int version) {
		return 8 + super.getByteLength(version);
	}
}
