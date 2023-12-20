package com.hiveworkshop.rms.parsers.mdlx;

import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlTokenInputStream;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlTokenOutputStream;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.util.BinaryReader;
import com.hiveworkshop.rms.util.BinaryWriter;

public class MdlxTexture implements MdlxBlock {
	public int replaceableId = 0;
	public String path = "";
	public int wrapFlag = 0;

	@Override
	public void readMdx(final BinaryReader reader, final int version) {
		replaceableId = reader.readInt32();
		path = reader.read(260);
		wrapFlag = reader.readInt32();
	}

	@Override
	public void writeMdx(final BinaryWriter writer, final int version) {
		writer.writeInt32(replaceableId);
		writer.writeWithNulls(path, 260);
		writer.writeInt32(wrapFlag);
	}

	@Override
	public void readMdl(final MdlTokenInputStream stream, final int version) {
		for (final String token : stream.readBlock()) {
			switch (token) {
				case MdlUtils.TOKEN_IMAGE -> path = stream.read();
				case MdlUtils.TOKEN_REPLACEABLE_ID -> replaceableId = stream.readInt();
				case MdlUtils.TOKEN_WRAP_WIDTH ->  wrapFlag |= 0x1;
				case MdlUtils.TOKEN_WRAP_HEIGHT -> wrapFlag |= 0x2;
				default -> ExceptionPopup.addStringToShow("Line " + stream.getLineNumber() + ": Unknown token in Texture: " + token);
			}
		}
	}

	@Override
	public void writeMdl(final MdlTokenOutputStream stream, final int version) {
		stream.startBlock(MdlUtils.TOKEN_BITMAP);
		stream.writeStringAttrib(MdlUtils.TOKEN_IMAGE, path);

		if (replaceableId != 0) {
			stream.writeAttrib(MdlUtils.TOKEN_REPLACEABLE_ID, replaceableId);
		}

		if ((wrapFlag & 0x1) != 0) {
			stream.writeFlag(MdlUtils.TOKEN_WRAP_WIDTH);
		}
		if ((wrapFlag & 0x2) != 0) {
			stream.writeFlag(MdlUtils.TOKEN_WRAP_HEIGHT);
		}

		stream.endBlock();
	}

	@Override
	public long getByteLength(final int version) {
		return 268;
	}
}
