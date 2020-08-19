package com.etheller.warsmash.parsers.mdlx;

import com.etheller.warsmash.parsers.mdlx.mdl.MdlTokenInputStream;
import com.etheller.warsmash.parsers.mdlx.mdl.MdlTokenOutputStream;
import com.hiveworkshop.util.BinaryReader;
import com.hiveworkshop.util.BinaryWriter;

public class MdlxFaceEffect implements MdlxBlock {
	public String type = "";
	public String path = "";
	
	public void readMdx(final BinaryReader reader, final int version) {
		type = reader.read(80);
		path = reader.read(260);
	}

	public void writeMdx(final BinaryWriter writer, final int version) {
		writer.writeWithNulls(type, 80);
		writer.writeWithNulls(path, 260);
	}

	public void readMdl(final MdlTokenInputStream stream, final int version) {
		this.type = stream.read();
	
		for (final String token : stream.readBlock()) {
			if (token.equals("Path")) {
				this.path = stream.read();
			} else {
				throw new IllegalStateException("Unknown token in MdlxFaceEffect: " + token);
			}
		}
	}

	public void writeMdl(final MdlTokenOutputStream stream, final int version) {
		stream.startObjectBlock("FaceFX", type);

		stream.writeStringAttrib("Path", path);

		stream.endBlock();
	}
}
