package com.etheller.warsmash.parsers.mdlx.timeline;

import com.etheller.warsmash.parsers.mdlx.mdl.MdlTokenInputStream;
import com.etheller.warsmash.parsers.mdlx.mdl.MdlTokenOutputStream;
import com.hiveworkshop.util.BinaryReader;
import com.hiveworkshop.util.BinaryWriter;

public final class MdlxFloatArrayTimeline extends MdlxTimeline<float[]> {
	private final int arraySize;

	public MdlxFloatArrayTimeline(final int arraySize) {
		this.arraySize = arraySize;
	}

	protected int size() {
		return this.arraySize;
	}

	protected float[] readMdxValue(final BinaryReader reader) {
		return reader.readFloat32Array(this.arraySize);
	}

	protected float[] readMdlValue(final MdlTokenInputStream stream) {
		final float[] output = new float[this.arraySize];
		stream.readKeyframe(output);
		return output;
	}

	protected void writeMdxValue(final BinaryWriter writer, final float[] value) {
		writer.writeFloat32Array(value);
	}

	protected void writeMdlValue(final MdlTokenOutputStream stream, final String prefix, final float[] value) {
		stream.writeKeyframe(prefix, value);
	}

	public int getArraySize() {
		return this.arraySize;
	}
}
