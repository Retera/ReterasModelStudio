package com.hiveworkshop.rms.editor.model.animflag;

import com.hiveworkshop.rms.editor.model.TimelineContainer;
import com.hiveworkshop.rms.parsers.mdlx.InterpolationType;
import com.hiveworkshop.rms.parsers.mdlx.timeline.MdlxUInt32Timeline;

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
public class IntAnimFlag extends AnimFlag<Integer> {

	public IntAnimFlag(final MdlxUInt32Timeline timeline) {
		super(timeline);

		final long[] frames = timeline.frames;
		final Object[] values = timeline.values;
		final Object[] inTans = timeline.inTans;
		final Object[] outTans = timeline.outTans;

		if (frames.length > 0) {
			setVectorSize(values[0]);
			final boolean hasTangents = interpolationType.tangential();

			for (int i = 0, l = frames.length; i < l; i++) {
				final long[] value = (long[]) values[i];
				int valueAsObject = 0;
				Integer inTanAsObject = null;
				Integer outTanAsObject = null;

				if (!isFloat) {
					valueAsObject = (int) value[0];

					if (hasTangents) {
						inTanAsObject = (int) ((long[]) inTans[i])[0];
						outTanAsObject = (int) ((long[]) outTans[i])[0];
					}
				}

				addEntry((int) frames[i], valueAsObject, inTanAsObject, outTanAsObject);
			}
		}
	}

	public IntAnimFlag(String title, List<Integer> times, List<Integer> values) {
		super(title, times, values);
	}

	public IntAnimFlag(String title) {
		super(title);
	}

	public IntAnimFlag(AnimFlag<Integer> af) {
		super(af);
	}

	public IntAnimFlag(IntAnimFlag af) {
		super(af);
	}

	public void setValuesTo(IntAnimFlag af) {
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
	protected Integer getIdentity(int typeId) {
		return (int) identity(typeId);
	}

	@Override
	protected Integer getInterpolatedValue(Integer floorTime, Integer ceilTime, float timeFactor) {
		Integer floorValue = entryMap.get(floorTime).getValue();
		Integer floorOutTan = entryMap.get(floorTime).getOutTan();

		Integer ceilValue = entryMap.get(ceilTime).getValue();
		Integer ceilInTan = entryMap.get(ceilTime).getInTan();

		switch (typeid) {
//			case TRANSLATION, SCALING, COLOR -> {
			case TEXTUREID -> {
				return switch (interpolationType) {
//					case BEZIER -> int.getBezier(floorValue, floorOutTan, ceilInTan, ceilValue, timeFactor);
//					case DONT_INTERP -> floorValue;
//					case HERMITE -> int.getHermite(floorValue, floorOutTan, ceilInTan, ceilValue, timeFactor);
//					case LINEAR -> int.getLerped(floorValue, ceilValue, timeFactor);
					case DONT_INTERP, BEZIER, HERMITE, LINEAR -> floorValue;
				};
			}
		}
		throw new IllegalStateException();
	}
	@Override
	public void setInterpType(final InterpolationType interpolationType) {
		this.interpolationType = InterpolationType.DONT_INTERP;
	}

	@Override
	public MdlxUInt32Timeline toMdlx(final TimelineContainer container) {
		final MdlxUInt32Timeline timeline = new MdlxUInt32Timeline();

		timeline.name = FlagUtils.getWar3ID(name, container);
		timeline.interpolationType = interpolationType;
		timeline.globalSequenceId = getGlobalSeqId();

		long[] tempFrames = new long[entryMap.size()];
		long[][] tempValues = new long[entryMap.size()][];
		long[][] tempInTans = new long[entryMap.size()][];
		long[][] tempOutTans = new long[entryMap.size()][];

		boolean hasTangents = timeline.interpolationType.tangential();

		for (int i = 0, l = entryMap.size(); i < l; i++) {
			Integer value = getValueFromIndex(i);

			tempFrames[i] = getTimeFromIndex(i);


			tempValues[i] = (new long[] {value.longValue()});

			if (hasTangents) {
				tempInTans[i] = new long[] {getInTanFromIndex(i).longValue()};
				tempOutTans[i] = new long[] {getOutTanFromIndex(i).longValue()};
			} else {
				tempInTans[i] = new long[] {0};
				tempOutTans[i] = new long[] {0};
			}
		}

		timeline.frames = tempFrames;
		timeline.values = tempValues;
		timeline.inTans = tempInTans;
		timeline.outTans = tempOutTans;

		return timeline;
	}

	/**
	 * Copies time track data from a certain interval into a different, new interval.
	 * The AnimFlag source of the data to copy cannot be same AnimFlag into which the
	 * data is copied, or else a ConcurrentModificationException will be thrown.
	 */
	public void copyFrom(final IntAnimFlag source, final int sourceStart, final int sourceEnd, final int newStart, final int newEnd) {
		if (tans() && !source.tans()) {
			JOptionPane.showMessageDialog(null,
					"Some animations will lose complexity due to transfer incombatibility. There will probably be no visible change.");
			linearize();
			// Probably makes this flag linear, but certainly makes it more like the copy source
		}

		TreeMap<Integer, Entry<Integer>> scaledMap = new TreeMap<>();
		for (int time = source.getEntryMap().ceilingKey(sourceStart); time <= source.getEntryMap().floorKey(sourceEnd); time = source.getEntryMap().higherKey(time)) {
			double ratio = (double) (time - sourceStart) / (double) (sourceEnd - sourceStart);
			int newTime = (int) (newStart + (ratio * (newEnd - newStart)));
			scaledMap.put(newTime, new Entry<>(source.getEntryMap().get(time)).setTime(newTime));
		}
	}

	public void copyFrom(final IntAnimFlag source) {
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
