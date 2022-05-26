package com.hiveworkshop.rms.parsers.mdlx;

import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlTokenInputStream;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlTokenOutputStream;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.util.BinaryReader;
import com.hiveworkshop.rms.util.BinaryWriter;

public class MdlxFaceEffect implements MdlxBlock {
	public String type = "";
	public String path = "";
	
	@Override
	public void readMdx(final BinaryReader reader, final int version) {
		type = reader.read(80);
		path = reader.read(260);
	}

	@Override
	public void writeMdx(final BinaryWriter writer, final int version) {
		writer.writeWithNulls(type, 80);
		writer.writeWithNulls(path, 260);
	}

	@Override
	public void readMdl(final MdlTokenInputStream stream, final int version) {
		type = stream.read();
	
		for (final String token : stream.readBlock()) {
			if (token.equals(MdlUtils.TOKEN_PATH)) {
				path = stream.read();
			} else {
				ExceptionPopup.addStringToShow("Line " + stream.getLineNumber() + ": Unknown token in MdlxFaceEffect: " + token);
			}
		}
	}

	@Override
	public void writeMdl(final MdlTokenOutputStream stream, final int version) {
		stream.startObjectBlock(MdlUtils.TOKEN_FACE_FX, type);

		stream.writeStringAttrib(MdlUtils.TOKEN_PATH, path);

		stream.endBlock();
	}
}
