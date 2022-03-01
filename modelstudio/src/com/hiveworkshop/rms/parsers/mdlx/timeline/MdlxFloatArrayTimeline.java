package com.hiveworkshop.rms.parsers.mdlx.timeline;

import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlTokenInputStream;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlTokenOutputStream;
import com.hiveworkshop.rms.util.BinaryReader;
import com.hiveworkshop.rms.util.BinaryWriter;

public final class MdlxFloatArrayTimeline extends MdlxTimeline<float[]> {
	private final int arraySize;

	public MdlxFloatArrayTimeline(final int arraySize) {
		this.arraySize = arraySize;
	}

	@Override
	protected int valueSize() {
		return arraySize;
	}

	@Override
	protected float[] readMdxValue(final BinaryReader reader) {
		return reader.readFloat32Array(arraySize);
	}

	@Override
	protected float[] readMdlValue(final MdlTokenInputStream stream) {
		final float[] output = new float[arraySize];
		stream.readKeyframe(output);
		return output;
	}

	@Override
	protected void writeMdxValue(final BinaryWriter writer, final float[] value) {
		writer.writeFloat32Array(value);
	}

	@Override
	protected void writeMdlValue(final MdlTokenOutputStream stream, final String prefix, final float[] value) {
		stream.writeKeyframe(prefix, value);
	}

	public int getArraySize() {
		return arraySize;
	}

	@Override
	public void initLists(int size){
		frames = new long[size];
		values = new float[size][];
		if(interpolationType.tangential()){
			inTans = new float[size][];
			outTans = new float[size][];
		}
	}

	public float[][] getEntryAt(int i){
		float[][] entry = new float[3][];
		entry[0] = values[i];
		if(interpolationType.tangential()){
			entry[1] = inTans[i];
			entry[2] = outTans[i];
		} else {
			entry[1] = new float[valueSize()];
			entry[2] = new float[valueSize()];
		}
		return entry;
	}
}
