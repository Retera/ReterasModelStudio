package com.etheller.warsmash.parsers.mdlx;

import com.etheller.warsmash.parsers.mdlx.mdl.MdlTokenInputStream;
import com.etheller.warsmash.parsers.mdlx.mdl.MdlTokenOutputStream;
import com.etheller.warsmash.util.MdlUtils;

public class MdlxHelper extends MdlxGenericObject {
	public MdlxHelper() {
		super(0x0); // NOTE: ghostwolf JS didn't pass the 0x1 flag????
		// ANOTHER NOTE: setting the 0x1 flag causes other fan programs to spam error
		// messages
	}

	@Override
	public void readMdl(final MdlTokenInputStream stream, final int version) {
		for (final String token : readMdlGeneric(stream)) {
			throw new RuntimeException("Unknown token in Helper: " + token);
		}
	}

	@Override
	public void writeMdl(final MdlTokenOutputStream stream, final int version) {
		stream.startObjectBlock(MdlUtils.TOKEN_HELPER, this.name);
		writeGenericHeader(stream);
		writeGenericTimelines(stream);
		stream.endBlock();
	}
}
