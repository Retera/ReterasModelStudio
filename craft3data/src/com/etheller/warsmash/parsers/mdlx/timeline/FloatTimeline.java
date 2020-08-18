package com.etheller.warsmash.parsers.mdlx.timeline;

import java.io.IOException;

import com.etheller.warsmash.parsers.mdlx.mdl.MdlTokenInputStream;
import com.etheller.warsmash.parsers.mdlx.mdl.MdlTokenOutputStream;
import com.google.common.io.LittleEndianDataOutputStream;
import com.hiveworkshop.util.BinaryReader;

public final class FloatTimeline extends Timeline<float[]> {

	@Override
	protected int size() {
		return 1;
	}

	@Override
	protected float[] readMdxValue(final BinaryReader reader) throws IOException {
		return new float[] { reader.readFloat32() };
	}

	@Override
	protected float[] readMdlValue(final MdlTokenInputStream stream) {
		return new float[] { stream.readFloat() };
	}

	@Override
	protected void writeMdxValue(final LittleEndianDataOutputStream stream, final float[] value) throws IOException {
		stream.writeFloat(value[0]);
	}

	@Override
	protected void writeMdlValue(final MdlTokenOutputStream stream, final String prefix, final float[] value) {
		stream.writeKeyframe(prefix, value[0]);
	}

}
