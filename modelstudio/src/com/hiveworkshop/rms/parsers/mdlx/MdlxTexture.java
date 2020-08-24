package com.hiveworkshop.rms.parsers.mdlx;

import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlTokenInputStream;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlTokenOutputStream;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.util.BinaryReader;
import com.hiveworkshop.rms.util.BinaryWriter;

public class MdlxTexture implements MdlxBlock {
	public int replaceableId = 0;
	public String path = "";
	public int flags = 0;

	@Override
	public void readMdx(final BinaryReader reader, final int version) {
		replaceableId = reader.readInt32();
		path = reader.read(260);
		flags = reader.readInt32();
	}

	@Override
	public void writeMdx(final BinaryWriter writer, final int version) {
		writer.writeInt32(replaceableId);
		writer.writeWithNulls(path, 260);
		writer.writeInt32(flags);
	}

	@Override
	public void readMdl(final MdlTokenInputStream stream, final int version) {
		for (final String token : stream.readBlock()) {
			switch (token) {
				case MdlUtils.TOKEN_IMAGE:
					path = stream.read();
					break;
				case MdlUtils.TOKEN_REPLACEABLE_ID:
					replaceableId = stream.readInt();
					break;
				case MdlUtils.TOKEN_WRAP_WIDTH:
					flags |= 0x1;
					break;
				case MdlUtils.TOKEN_WRAP_HEIGHT:
					flags |= 0x2;
					break;
				default:
					throw new RuntimeException("Unknown token in Texture: " + token);
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

		if ((flags & 0x1) != 0) {
			stream.writeFlag(MdlUtils.TOKEN_WRAP_WIDTH);
		}

		if ((flags & 0x2) != 0) {
			stream.writeFlag(MdlUtils.TOKEN_WRAP_HEIGHT);
		}

		stream.endBlock();
	}
}
