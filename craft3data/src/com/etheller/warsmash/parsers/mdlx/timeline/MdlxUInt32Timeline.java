package com.etheller.warsmash.parsers.mdlx.timeline;

import com.etheller.warsmash.parsers.mdlx.mdl.MdlTokenInputStream;
import com.etheller.warsmash.parsers.mdlx.mdl.MdlTokenOutputStream;
import com.hiveworkshop.util.BinaryReader;
import com.hiveworkshop.util.BinaryWriter;

public final class MdlxUInt32Timeline extends MdlxTimeline<long[]> {
	protected int size() {
		return 1;
	}

	protected long[] readMdxValue(final BinaryReader reader) {
		return new long[] { reader.readUInt32() };
	}

	protected long[] readMdlValue(final MdlTokenInputStream stream) {
		return new long[] { stream.readUInt32() };
	}

	protected void writeMdxValue(final BinaryWriter writer, final long[] uint32) {
		writer.writeUInt32(uint32[0]);
	}

	protected void writeMdlValue(final MdlTokenOutputStream stream, final String prefix, final long[] uint32) {
		stream.writeKeyframe(prefix, uint32[0]);
	}

}
