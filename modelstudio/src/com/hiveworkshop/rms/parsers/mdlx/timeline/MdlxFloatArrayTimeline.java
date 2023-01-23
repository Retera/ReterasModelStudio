package com.hiveworkshop.rms.parsers.mdlx.timeline;

import com.hiveworkshop.rms.parsers.mdlx.AnimationMap;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlTokenInputStream;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlTokenOutputStream;
import com.hiveworkshop.rms.util.BinaryReader;
import com.hiveworkshop.rms.util.BinaryWriter;
import com.hiveworkshop.rms.util.War3ID;

public final class MdlxFloatArrayTimeline extends MdlxTimeline<float[]> {
	private final int arraySize;
	private final boolean isColor;

	public MdlxFloatArrayTimeline(final int arraySize, War3ID name) {
		super(name);
		this.arraySize = arraySize;
		isColor = name.equals(AnimationMap.KGAC.getWar3id())
				|| name.equals(AnimationMap.KFC3.getWar3id())
				|| name.equals(AnimationMap.KLAC.getWar3id())
				|| name.equals(AnimationMap.KLBC.getWar3id())
				|| name.equals(AnimationMap.KPPC.getWar3id())
				|| name.equals(AnimationMap.KRCO.getWar3id())
		;
	}

	@Override
	protected int valueSize() {
		return arraySize;
	}

	@Override
	protected float[] readMdxValue(final BinaryReader reader) {
		final float[] output = new float[arraySize];
		if(isColor){
			reader.readInvFloat32Array(output);
		} else {
			reader.readFloat32Array(output);
		}
		return output;
	}

	@Override
	protected float[] readMdlValue(final MdlTokenInputStream stream) {
		final float[] output = new float[arraySize];
		if(isColor){
			stream.readColor(output);
		} else {
			stream.readKeyframe(output);
		}
		return output;
	}

	@Override
	protected void writeMdxValue(final BinaryWriter writer, final float[] value) {
		if(isColor){
			writer.writeInvFloat32Array(value);
		} else {
			writer.writeFloat32Array(value);
		}
	}

	@Override
	protected void writeMdlValue(final MdlTokenOutputStream stream, final String prefix, final float[] value) {
		if(isColor){
			stream.writeColor(prefix, value);
		} else {
			stream.writeKeyframe(prefix, value);
		}
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
