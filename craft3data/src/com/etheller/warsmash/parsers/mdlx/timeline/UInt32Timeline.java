package com.etheller.warsmash.parsers.mdlx.timeline;

import java.io.IOException;

import com.etheller.warsmash.parsers.mdlx.mdl.MdlTokenInputStream;
import com.etheller.warsmash.parsers.mdlx.mdl.MdlTokenOutputStream;
import com.etheller.warsmash.util.ParseUtils;
import com.google.common.io.LittleEndianDataOutputStream;
import com.hiveworkshop.util.BinaryReader;

public final class UInt32Timeline extends Timeline<long[]> {

	@Override
	protected int size() {
		return 1;
	}

	@Override
	protected long[] readMdxValue(final BinaryReader reader) throws IOException {
		return new long[] { reader.readUInt32() };
	}

	@Override
	protected long[] readMdlValue(final MdlTokenInputStream stream) {
		return new long[] { stream.readUInt32() };
	}

	@Override
	protected void writeMdxValue(final LittleEndianDataOutputStream stream, final long[] uint32) throws IOException {
		ParseUtils.writeUInt32(stream, uint32[0]);
	}

	@Override
	protected void writeMdlValue(final MdlTokenOutputStream stream, final String prefix, final long[] uint32) {
		stream.writeKeyframe(prefix, uint32[0]);
	}

}
