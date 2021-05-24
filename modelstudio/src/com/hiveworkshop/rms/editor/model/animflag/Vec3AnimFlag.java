package com.hiveworkshop.rms.editor.model.animflag;

import com.hiveworkshop.rms.editor.model.TimelineContainer;
import com.hiveworkshop.rms.parsers.mdlx.InterpolationType;
import com.hiveworkshop.rms.parsers.mdlx.timeline.MdlxFloatArrayTimeline;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import java.util.List;
import java.util.TreeMap;

/**
 * A java class for MDL "motion flags," such as Alpha, Translation, Scaling, or
 * Rotation. AnimFlags are not "real" things from an MDL and are given this name
 * by me, as an invented java class to simplify the programming
 *
 * Eric Theller 11/5/2011
 */
public class Vec3AnimFlag extends AnimFlag<Vec3> {


//	public Vec3AnimFlag(MdlxTimeline<Float[]> timeline) {
//		super(timeline);
//	}

	public Vec3AnimFlag(final MdlxFloatArrayTimeline timeline) {
		super(timeline);

		final long[] frames = timeline.frames;
		final Object[] values = timeline.values;
		final Object[] inTans = timeline.inTans;
		final Object[] outTans = timeline.outTans;

		if (frames.length > 0) {
			setVectorSize(values[0]);
			final boolean hasTangents = interpolationType.tangential();


			for (int i = 0, l = frames.length; i < l; i++) {
				final Object value = values[i];
				Vec3 valueAsObject = null;
				Vec3 inTanAsObject = null;
				Vec3 outTanAsObject = null;

				if (isFloat) {
					final float[] valueAsArray = (float[]) value;

					if (vectorSize == 3) {
						valueAsObject = new Vec3(valueAsArray);

						if (hasTangents) {
							inTanAsObject = new Vec3((float[]) inTans[i]);
							outTanAsObject = new Vec3((float[]) outTans[i]);
						}
					}
				}

				addEntry((int) frames[i], valueAsObject, inTanAsObject, outTanAsObject);
			}
		}
	}

	public Vec3AnimFlag(String title, List<Integer> times, List<Vec3> values) {
		super(title, times, values);
	}

	public Vec3AnimFlag(String title) {
		super(title);
	}

	public Vec3AnimFlag(AnimFlag<Vec3> af) {
		super(af);
	}

	public Vec3AnimFlag(Vec3AnimFlag af) {
		super(af);
	}

	public static Vec3AnimFlag createEmpty2018(final String title, final InterpolationType interpolationType, final Integer globalSeq) {
		final Vec3AnimFlag flag = new Vec3AnimFlag(title);
//		flag.name = title;
		flag.interpolationType = interpolationType;
		flag.generateTypeId();
		flag.setGlobSeq(globalSeq);
		return flag;
	}

	public static Vec3 cloneValue(final Vec3 value) {
		if (value == null) {
			return null;
		}
		return new Vec3(value);
	}

	public void setValuesTo(Vec3AnimFlag af) {
		name = af.name;
		globalSeqLength = af.globalSeqLength;
		globalSeqId = af.globalSeqId;
		hasGlobalSeq = af.hasGlobalSeq;
		interpolationType = af.interpolationType;
		typeid = af.typeid;

		for (Integer time : af.getEntryMap().keySet()) {
			entryMap.put(time, new Entry<>(af.getEntryMap().get(time)));
		}
	}

	@Override
//	public MdlxTimeline<Float[]> toMdlx(TimelineContainer container) {
//		return null;
//	}

	public MdlxFloatArrayTimeline toMdlx(final TimelineContainer container) {
		final MdlxFloatArrayTimeline timeline = new MdlxFloatArrayTimeline(3);

		if (!entryMap.isEmpty()) {
			setVectorSize(entryMap.firstEntry().getValue().getValue());
		}

		timeline.name = FlagUtils.getWar3ID(name, container);
		timeline.interpolationType = interpolationType;
		timeline.globalSequenceId = getGlobalSeqId();

		long[] tempFrames = new long[entryMap.size()];
		float[][] tempValues = new float[entryMap.size()][];
		float[][] tempInTans = new float[entryMap.size()][];
		float[][] tempOutTans = new float[entryMap.size()][];

		boolean hasTangents = timeline.interpolationType.tangential();

		for (int i = 0, l = entryMap.size(); i < l; i++) {
			Vec3 value = getValueFromIndex(i);

			tempFrames[i] = getTimeFromIndex(i);

			tempValues[i] = value.toFloatArray();

			if (hasTangents) {
				tempInTans[i] = getInTanFromIndex(i).toFloatArray();
				tempOutTans[i] = getOutTanFromIndex(i).toFloatArray();
			} else {
				tempInTans[i] = (new Vec3()).toFloatArray();
				tempOutTans[i] = (new Vec3()).toFloatArray();
			}
		}

		timeline.frames = tempFrames;
		timeline.values = tempValues;
		timeline.inTans = tempInTans;
		timeline.outTans = tempOutTans;

		return timeline;
	}

	@Override
	public Vec3 getIdentity(int typeid) {
		return (Vec3) identity(typeid);
	}

	protected Vec3 getInterpolatedValue(Integer floorTime, Integer ceilTime, float timeFactor) {
		Vec3 floorValue = entryMap.get(floorTime).getValue();
		Vec3 floorOutTan = entryMap.get(floorTime).getOutTan();

		Vec3 ceilValue = entryMap.get(ceilTime).getValue();
		Vec3 ceilInTan = entryMap.get(ceilTime).getInTan();

		switch (typeid) {
			case TRANSLATION, SCALING, COLOR -> {
				return switch (interpolationType) {
					case BEZIER -> Vec3.getBezier(floorValue, floorOutTan, ceilInTan, ceilValue, timeFactor);
					case DONT_INTERP -> floorValue;
					case HERMITE -> Vec3.getHermite(floorValue, floorOutTan, ceilInTan, ceilValue, timeFactor);
					case LINEAR -> Vec3.getLerped(floorValue, ceilValue, timeFactor);
				};
			}
		}
		throw new IllegalStateException();
	}

	/**
	 * Copies time track data from a certain interval into a different, new interval.
	 * The AnimFlag source of the data to copy cannot be same AnimFlag into which the
	 * data is copied, or else a ConcurrentModificationException will be thrown.
	 */
	public void copyFrom(final Vec3AnimFlag source, final int sourceStart, final int sourceEnd, final int newStart, final int newEnd) {
		// Timescales a part of the AnimFlag from the source into the new time "newStart" to "newEnd"
		if (tans() && !source.tans()) {
			JOptionPane.showMessageDialog(null,
					"Some animations will lose complexity due to transfer incombatibility. There will probably be no visible change.");
			linearize();
			// Probably makes this flag linear, but certainly makes it more like the copy source
		}

		TreeMap<Integer, Entry<Vec3>> scaledMap = new TreeMap<>();
		for (int time = source.getEntryMap().ceilingKey(sourceStart); time <= source.getEntryMap().floorKey(sourceEnd); time = source.getEntryMap().higherKey(time)) {
			double ratio = (double) (time - sourceStart) / (double) (sourceEnd - sourceStart);
			int newTime = (int) (newStart + (ratio * (newEnd - newStart)));
			scaledMap.put(newTime, new Entry<>(source.getEntryMap().get(time)).setTime(newTime));
		}
		entryMap.putAll(scaledMap);
	}

	public void copyFrom(final Vec3AnimFlag source) {
		// ToDo fix interpolation types
		if (tans() && !source.tans()) {
			JOptionPane.showMessageDialog(null,
					"Some animations will lose complexity due to transfer incombatibility. There will probably be no visible change.");
			linearize();
			// Probably makes this flag linear, but certainly makes it more like the copy source
		}
		for (Integer time : source.getEntryMap().keySet()) {
			entryMap.put(time, new Entry<>(source.getEntryMap().get(time)));
		}
	}
}
