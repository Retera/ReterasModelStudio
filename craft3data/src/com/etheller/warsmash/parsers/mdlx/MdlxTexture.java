package com.etheller.warsmash.parsers.mdlx;

import com.etheller.warsmash.parsers.mdlx.mdl.MdlTokenInputStream;
import com.etheller.warsmash.parsers.mdlx.mdl.MdlTokenOutputStream;
import com.etheller.warsmash.util.MdlUtils;
import com.hiveworkshop.util.BinaryReader;
import com.hiveworkshop.util.BinaryWriter;

public class MdlxTexture implements MdlxBlock {
	public int replaceableId = 0;
	public String path = "";
	public int flags = 0;

	public void readMdx(final BinaryReader reader, final int version) {
		this.replaceableId = reader.readInt32();
		this.path = reader.read(260);
		this.flags = reader.readInt32();
	}

	@Override
	public void writeMdx(final BinaryWriter writer, final int version) {
		writer.writeInt32(this.replaceableId);
		writer.writeWithNulls(path, 260);
		writer.writeInt32(this.flags);
	}

	@Override
	public void readMdl(final MdlTokenInputStream stream, final int version) {
		for (final String token : stream.readBlock()) {
			switch (token) {
			case MdlUtils.TOKEN_IMAGE:
				this.path = stream.read();
				break;
			case MdlUtils.TOKEN_REPLACEABLE_ID:
				this.replaceableId = stream.readInt();
				break;
			case MdlUtils.TOKEN_WRAP_WIDTH:
				this.flags |= 0x1;
				break;
			case MdlUtils.TOKEN_WRAP_HEIGHT:
				this.flags |= 0x2;
				break;
			default:
				throw new RuntimeException("Unknown token in Texture: " + token);
			}
		}
	}

	@Override
	public void writeMdl(final MdlTokenOutputStream stream, final int version) {
		stream.startBlock(MdlUtils.TOKEN_BITMAP);
		stream.writeStringAttrib(MdlUtils.TOKEN_IMAGE, this.path);

		if (this.replaceableId != 0) {
			stream.writeAttrib(MdlUtils.TOKEN_REPLACEABLE_ID, this.replaceableId);
		}

		if ((this.flags & 0x1) != 0) {
			stream.writeFlag(MdlUtils.TOKEN_WRAP_WIDTH);
		}

		if ((this.flags & 0x2) != 0) {
			stream.writeFlag(MdlUtils.TOKEN_WRAP_HEIGHT);
		}

		stream.endBlock();
	}
}
