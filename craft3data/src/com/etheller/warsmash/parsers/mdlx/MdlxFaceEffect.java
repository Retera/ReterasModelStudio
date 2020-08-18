package com.etheller.warsmash.parsers.mdlx;

import java.io.IOException;

import com.etheller.warsmash.parsers.mdlx.mdl.MdlTokenInputStream;
import com.etheller.warsmash.parsers.mdlx.mdl.MdlTokenOutputStream;
import com.etheller.warsmash.util.ParseUtils;
import com.google.common.io.LittleEndianDataOutputStream;
import com.hiveworkshop.util.BinaryReader;

public class MdlxFaceEffect implements MdlxBlock {
	public String type = "";
	public String path = "";
	
	public void readMdx(final BinaryReader reader, final int version) throws IOException {
		type = reader.read(80);
		path = reader.read(260);
	}

	public void writeMdx(final LittleEndianDataOutputStream stream, final int version) throws IOException {
		byte[] bytes = this.type.getBytes(ParseUtils.UTF8);
		stream.write(bytes);
		for (int i = 0; i < (80 - bytes.length); i++) {
			stream.write((byte) 0);
		}
		bytes = this.path.getBytes(ParseUtils.UTF8);
		stream.write(bytes);
		for (int i = 0; i < (260 - bytes.length); i++) {
			stream.write((byte) 0);
		}
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
