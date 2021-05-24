package com.hiveworkshop.rms.editor.model.animflag;

import com.hiveworkshop.rms.editor.model.TimelineContainer;
import com.hiveworkshop.rms.parsers.mdlx.timeline.MdlxFloatTimeline;
import com.hiveworkshop.rms.util.MathUtils;

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
public class FloatAnimFlag extends AnimFlag<Float> {

	public FloatAnimFlag(final MdlxFloatTimeline timeline) {
		super(timeline);

//		System.out.println(Arrays.deepToString(timeline.values));

		final long[] frames = timeline.frames;
		final Object[] values = timeline.values;
		final Object[] inTans = timeline.inTans;
		final Object[] outTans = timeline.outTans;

		if (frames.length > 0) {
			setVectorSize(values[0]);
			final boolean hasTangents = interpolationType.tangential();

			for (int i = 0, l = frames.length; i < l; i++) {
				final Object value = values[i];
				float valueAsObject = 0f;
				Float inTanAsObject = null;
				Float outTanAsObject = null;

				if (isFloat) {
					final float[] valueAsArray = (float[]) value;

					if (vectorSize == 1) {
						valueAsObject = valueAsArray[0];

						if (hasTangents) {
							inTanAsObject = ((float[]) inTans[i])[0];
							outTanAsObject = ((float[]) outTans[i])[0];
						}
					}
				}

				addEntry((int) frames[i], valueAsObject, inTanAsObject, outTanAsObject);
			}
		}
	}

	public FloatAnimFlag(String title, List<Integer> times, List<Float> values) {
		super(title, times, values);
	}

	public FloatAnimFlag(String title) {
		super(title);
	}

	public FloatAnimFlag(AnimFlag<Float> af) {
		super(af);
	}

	public FloatAnimFlag(FloatAnimFlag af) {
		super(af);
	}

	private static FloatAnimFlag getMostVis(FloatAnimFlag flag1, FloatAnimFlag flag2, FloatAnimFlag mostVisible) {
		if (mostVisible == null) {
			mostVisible = flag1;
		} else if (mostVisible == flag2) {
			return null;
		}
		return mostVisible;
	}

	public void setValuesTo(final FloatAnimFlag af) {
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
	protected Float getIdentity(int typeId) {
		if ((typeId == ROTATION) && !entryMap.isEmpty() && entryMap.firstEntry().getValue().getValue() != null) {
			return (float) identity(ALPHA); // magic Camera rotation!
		}
		return (float) identity(typeId);
	}

	@Override
	protected Float getInterpolatedValue(Integer floorTime, Integer ceilTime, float timeFactor) {
		Float floorValue = entryMap.get(floorTime).getValue();
		Float floorOutTan = entryMap.get(floorTime).getOutTan();

		Float ceilValue = entryMap.get(ceilTime).getValue();
		Float ceilInTan = entryMap.get(ceilTime).getInTan();

		switch (typeid) {
//			case TRANSLATION, SCALING, COLOR -> {
			case ALPHA -> {
				return switch (interpolationType) {
					case BEZIER -> MathUtils.bezier(floorValue, floorOutTan, ceilInTan, ceilValue, timeFactor);
					case DONT_INTERP -> floorValue;
					case HERMITE -> MathUtils.hermite(floorValue, floorOutTan, ceilInTan, ceilValue, timeFactor);
					case LINEAR -> MathUtils.lerp(floorValue, ceilValue, timeFactor);
				};
			}
		}
		throw new IllegalStateException();
	}

	@Override
//	public MdlxTimeline<Float[]> toMdlx(TimelineContainer container) {
//		return null;
//	}

	public MdlxFloatTimeline toMdlx(final TimelineContainer container) {
		final MdlxFloatTimeline timeline = new MdlxFloatTimeline();
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

		final boolean hasTangents = timeline.interpolationType.tangential();

		for (int i = 0, l = entryMap.size(); i < l; i++) {
			Float value = getValueFromIndex(i);

			tempFrames[i] = getTimeFromIndex(i);

			if (isFloat) {
				if (vectorSize == 1) {
					tempValues[i] = new float[] {value};

					if (hasTangents) {
						tempInTans[i] = new float[] {getInTanFromIndex(i)};
						tempOutTans[i] = new float[] {getOutTanFromIndex(i)};
					} else {
						tempInTans[i] = new float[] {0};
						tempOutTans[i] = new float[] {0};
					}
				}
			}
		}

		timeline.frames = tempFrames;
		timeline.values = tempValues;
		timeline.inTans = tempInTans;
		timeline.outTans = tempOutTans;

		return timeline;
	}

	//ToDo should probably stay in use...
	public FloatAnimFlag getMostVisible(final FloatAnimFlag partner) {
//		if (partner != null) {
//			if ((typeid == 0) && (partner.typeid == 0)) {
//				final List<Integer> selfTimes = new ArrayList<>(times);
//				final List<Integer> partnerTimes = new ArrayList<>(partner.times);
//				final List<Float> selfValues = new ArrayList<>(values);
//				final List<Float> partnerValues = new ArrayList<>(partner.values);
//
//				FloatAnimFlag mostVisible = null;
//				mostVisible = getMostVissibleAnimFlag(selfTimes, partnerTimes, selfValues, partnerValues, mostVisible, partner, this);
//				if (mostVisible == null) return null;
//
//				mostVisible = getMostVissibleAnimFlag(partnerTimes, selfTimes, partnerValues, selfValues, mostVisible, this, partner);
//				if (mostVisible == null) return null;
//
//				// partner has priority!
//				return mostVisible;
//			} else {
//				JOptionPane.showMessageDialog(null,
//						"Error: Program attempted to compare visibility with non-visibility animation component.\nThis... probably means something is horribly wrong. Save your work, if you can.");
//			}
//		}
		return null;
	}

	private FloatAnimFlag getMostVissibleAnimFlag(List<Integer> atimes, List<Integer> btimes, List<Float> avalues, List<Float> bvalues, FloatAnimFlag mostVisible, FloatAnimFlag flag1, FloatAnimFlag flag2) {
		for (int i = atimes.size() - 1; i >= 0; i--)
		// count down from top, meaning that removing the current value causes no harm
		{
			final Integer currentTime = atimes.get(i);
			final Float currentVal = avalues.get(i);

			if (btimes.contains(currentTime)) {
				final Float partVal = bvalues.get(btimes.indexOf(currentTime));
				if (partVal > currentVal) {
					mostVisible = getMostVis(flag1, flag2, mostVisible);
					if (mostVisible == null) return null;
				} else if (partVal < currentVal) {
					mostVisible = getMostVis(flag2, flag1, mostVisible);
					if (mostVisible == null) return null;
				}
			} else if (currentVal < 1) {
				mostVisible = getMostVis(flag1, flag2, mostVisible);
				if (mostVisible == null) return null;
			}
		}
		return mostVisible;
	}

	/**
	 * Copies time track data from a certain interval into a different, new interval.
	 * The AnimFlag source of the data to copy cannot be same AnimFlag into which the
	 * data is copied, or else a ConcurrentModificationException will be thrown.
	 */
	public void copyFrom(final FloatAnimFlag source, final int sourceStart, final int sourceEnd, final int newStart, final int newEnd) {
		// Timescales a part of the AnimFlag from the source into the new time "newStart" to "newEnd"
		if (tans() && !source.tans()) {
			JOptionPane.showMessageDialog(null,
					"Some animations will lose complexity due to transfer incombatibility. There will probably be no visible change.");
			linearize();
			// Probably makes this flag linear, but certainly makes it more like the copy source
		}

		TreeMap<Integer, Entry<Float>> scaledMap = new TreeMap<>();
		for (int time = source.getEntryMap().ceilingKey(sourceStart); time <= source.getEntryMap().floorKey(sourceEnd); time = source.getEntryMap().higherKey(time)) {
			double ratio = (double) (time - sourceStart) / (double) (sourceEnd - sourceStart);
			int newTime = (int) (newStart + (ratio * (newEnd - newStart)));
			scaledMap.put(newTime, new Entry<>(source.getEntryMap().get(time)).setTime(newTime));
		}
	}

	public void copyFrom(final FloatAnimFlag source) {
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
