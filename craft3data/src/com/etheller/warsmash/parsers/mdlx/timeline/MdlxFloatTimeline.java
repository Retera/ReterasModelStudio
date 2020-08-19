package com.etheller.warsmash.parsers.mdlx.timeline;

import com.etheller.warsmash.parsers.mdlx.mdl.MdlTokenInputStream;
import com.etheller.warsmash.parsers.mdlx.mdl.MdlTokenOutputStream;
import com.hiveworkshop.util.BinaryReader;
import com.hiveworkshop.util.BinaryWriter;

public final class MdlxFloatTimeline extends MdlxTimeline<float[]> {
	protected int size() {
		return 1;
	}

	protected float[] readMdxValue(final BinaryReader reader) {
		return new float[] { reader.readFloat32() };
	}

	protected float[] readMdlValue(final MdlTokenInputStream stream) {
		return new float[] { stream.readFloat() };
	}

	protected void writeMdxValue(final BinaryWriter writer, final float[] value) {
		writer.writeFloat32(value[0]);
	}

	protected void writeMdlValue(final MdlTokenOutputStream stream, final String prefix, final float[] value) {
		stream.writeKeyframe(prefix, value[0]);
	}
}
