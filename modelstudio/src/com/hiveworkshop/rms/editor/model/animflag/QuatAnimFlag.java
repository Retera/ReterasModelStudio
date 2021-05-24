package com.hiveworkshop.rms.editor.model.animflag;

import com.hiveworkshop.rms.editor.model.TimelineContainer;
import com.hiveworkshop.rms.parsers.mdlx.InterpolationType;
import com.hiveworkshop.rms.parsers.mdlx.timeline.MdlxFloatArrayTimeline;
import com.hiveworkshop.rms.util.Quat;

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
public class QuatAnimFlag extends AnimFlag<Quat> {


//	public QuatAnimFlag(MdlxTimeline<Float[]> timeline) {
//		super(timeline);
//	}

	public QuatAnimFlag(final MdlxFloatArrayTimeline timeline) {
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
				Quat valueAsObject = null;
				Quat inTanAsObject = null;
				Quat outTanAsObject = null;

				if (isFloat) {
					final float[] valueAsArray = (float[]) value;

					if (vectorSize == 4) {
						valueAsObject = new Quat(valueAsArray);

						if (hasTangents) {
							inTanAsObject = new Quat((float[]) inTans[i]);
							outTanAsObject = new Quat((float[]) outTans[i]);
						}
					}
				}

				addEntry((int) frames[i], valueAsObject, inTanAsObject, outTanAsObject);
			}
		}
	}

	public QuatAnimFlag(String title, List<Integer> times, List<Quat> values) {
		super(title, times, values);
	}

	public QuatAnimFlag(String title) {
		super(title);
	}

	public QuatAnimFlag(AnimFlag<Quat> af) {
		super(af);
	}

	public QuatAnimFlag(QuatAnimFlag af) {
		super(af);
	}

	public static QuatAnimFlag createEmpty2018(final String title, final InterpolationType interpolationType, final Integer globalSeq) {
		final QuatAnimFlag flag = new QuatAnimFlag(title);
//		flag.name = title;
		flag.interpolationType = interpolationType;
		flag.generateTypeId();
		flag.setGlobSeq(globalSeq);
		return flag;
	}

	public static Quat cloneValue(final Quat value) {
		if (value == null) {
			return null;
		}
		return new Quat(value);
	}

	public void setValuesTo(QuatAnimFlag af) {
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
		final MdlxFloatArrayTimeline timeline = new MdlxFloatArrayTimeline(4);

		if (!entryMap.isEmpty()) {
			setVectorSize(entryMap.firstEntry().getValue().getValue());
		}

		timeline.name = FlagUtils.getWar3ID(name, container);
		timeline.interpolationType = interpolationType;
		timeline.globalSequenceId = getGlobalSeqId();

		final long[] tempFrames = new long[entryMap.size()];
		final float[][] tempValues = new float[entryMap.size()][];
		final float[][] tempInTans = new float[entryMap.size()][];
		final float[][] tempOutTans = new float[entryMap.size()][];

		final boolean hasTangents = timeline.interpolationType.tangential();

		for (int i = 0, l = entryMap.size(); i < l; i++) {
			final Quat value = getValueFromIndex(i);

			tempFrames[i] = getTimeFromIndex(i);

			tempValues[i] = value.toFloatArray();

			if (hasTangents) {
				tempInTans[i] = getInTanFromIndex(i).toFloatArray();
				tempOutTans[i] = getOutTanFromIndex(i).toFloatArray();
			} else {
				tempInTans[i] = (new Quat()).toFloatArray();
				tempOutTans[i] = (new Quat()).toFloatArray();
			}
		}

		timeline.frames = tempFrames;
		timeline.values = tempValues;
		timeline.inTans = tempInTans;
		timeline.outTans = tempOutTans;

		return timeline;
	}

	protected Quat getIdentity(int typeId) {
		return (Quat) identity(typeId);
	}

	@Override
	protected Quat getInterpolatedValue(Integer floorTime, Integer ceilTime, float timeFactor) {
		Quat floorValue = entryMap.get(floorTime).getValue();
		Quat floorOutTan = entryMap.get(floorTime).getOutTan();

		Quat ceilValue = entryMap.get(ceilTime).getValue();
		Quat ceilInTan = entryMap.get(ceilTime).getInTan();

//		System.out.println(typeid);
		switch (typeid) {
//			case TRANSLATION, SCALING, COLOR -> {
			case ROTATION -> {
				return switch (interpolationType) {
					case BEZIER -> Quat.getSquad(floorValue, ceilValue, floorOutTan, ceilInTan, timeFactor);
					case DONT_INTERP -> floorValue;
					case HERMITE -> Quat.getSquad(floorValue, ceilValue, floorOutTan, ceilInTan, timeFactor);
					case LINEAR -> Quat.getSlerped(floorValue, ceilValue, timeFactor);
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
	public void copyFrom(final QuatAnimFlag source, final int sourceStart, final int sourceEnd, final int newStart, final int newEnd) {
		// Timescales a part of the AnimFlag from the source into the new time "newStart" to "newEnd"
		if (tans() && !source.tans()) {
			JOptionPane.showMessageDialog(null,
					"Some animations will lose complexity due to transfer incombatibility. There will probably be no visible change.");
			linearize();
			// Probably makes this flag linear, but certainly makes it more like the copy source
		}

		TreeMap<Integer, Entry<Quat>> scaledMap = new TreeMap<>();
		for (int time = source.getEntryMap().ceilingKey(sourceStart); time <= source.getEntryMap().floorKey(sourceEnd); time = source.getEntryMap().higherKey(time)) {
			double ratio = (double) (time - sourceStart) / (double) (sourceEnd - sourceStart);
			int newTime = (int) (newStart + (ratio * (newEnd - newStart)));
			scaledMap.put(newTime, new Entry<>(source.getEntryMap().get(time)).setTime(newTime));
		}
		entryMap.putAll(scaledMap);
	}

	public void copyFrom(final QuatAnimFlag source) {
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
