package com.hiveworkshop.rms.editor.model.animflag;

import com.hiveworkshop.rms.editor.model.TimelineContainer;
import com.hiveworkshop.rms.parsers.mdlx.AnimationMap;
import com.hiveworkshop.rms.parsers.mdlx.InterpolationType;
import com.hiveworkshop.rms.parsers.mdlx.timeline.MdlxUInt32Timeline;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeBoundProvider;
import com.hiveworkshop.rms.ui.application.viewer.AnimatedRenderEnvironment;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

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
		name = AnimationMap.ID_TO_TAG.get(timeline.name).getMdlToken();
		generateTypeId();

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

	@Override
	public void setValuesTo2(final AnimFlag<Integer> af) {
		this.setValuesTo(af);
	}

	public void setValuesTo(IntAnimFlag af) {
		name = af.name;
		globalSeq = af.globalSeq;
		globalSeqId = af.globalSeqId;
		hasGlobalSeq = af.hasGlobalSeq;
		interpolationType = af.interpolationType;
		typeid = af.typeid;
		times = new ArrayList<>(af.times);
		values = deepCopy(af.values);
		inTans = deepCopy(af.inTans);
		outTans = deepCopy(af.outTans);
	}


	@Override
	public Integer interpolateAt(final AnimatedRenderEnvironment animatedRenderEnvironment) {
//		System.out.println(name + ", interpolateAt");
		if ((animatedRenderEnvironment == null) || (animatedRenderEnvironment.getCurrentAnimation() == null)) {
//			System.out.println("~~ animatedRenderEnvironment == null");
			if (values.size() > 0) {
				return values.get(0);
			}
			return (Integer) identity(typeid);
		}
		int localTypeId = typeid;
//		System.out.println("typeId 1: " + typeid);

		if (times.isEmpty()) {
//			System.out.println(name + ", ~~ no times");
			return (Integer) identity(localTypeId);
		}
		// TODO ghostwolf says to stop using binary search, because linear walking is faster for the small MDL case
		final int time;
		int ceilIndex;
		final int floorIndex;
		int floorValue;
		Integer floorIndexTime;
		Integer ceilIndexTime;
		if (hasGlobalSeq() && (getGlobalSeq() >= 0)) {
			time = animatedRenderEnvironment.getGlobalSeqTime(getGlobalSeq());
			final int floorAnimStartIndex = Math.max(0, floorIndex(1));
			floorIndex = Math.max(0, floorIndex(time));
			ceilIndex = Math.max(floorIndex, ceilIndex(time));

			floorValue = values.get(floorIndex);
			floorIndexTime = times.get(floorIndex);
			ceilIndexTime = times.get(ceilIndex);

			if (ceilIndexTime < 0) {
				return (int) identity(localTypeId);
			} else if (floorIndexTime > getGlobalSeq() && values.size() > 0) {
				// out of range global sequences end up just using the higher value keyframe
				return values.get(floorIndex);
			} else if (floorIndexTime > getGlobalSeq() || (floorIndexTime < 0) && (ceilIndexTime > getGlobalSeq()) || floorIndexTime < 0) {
				return (int) identity(localTypeId);
			} else if (ceilIndexTime > getGlobalSeq() && floorIndex == floorAnimStartIndex) {
				return values.get(floorIndex);
			}
		} else {
//			System.out.println(name + ", ~~ no global seq");
			final TimeBoundProvider animation = animatedRenderEnvironment.getCurrentAnimation();
			int animationStart = animation.getStart();
			time = animationStart + animatedRenderEnvironment.getAnimationTime();
			final int floorAnimStartIndex = Math.max(0, floorIndex(animationStart + 1));
			int animationEnd = animation.getEnd();
			final int floorAnimEndIndex = Math.max(0, floorIndex(animationEnd));

			floorIndex = floorIndex(time);
			ceilIndex = Math.max(floorIndex, ceilIndex(time));
			ceilIndexTime = times.get(ceilIndex);

			final int lookupFloorIndex = Math.max(0, floorIndex);
			floorValue = values.get(lookupFloorIndex);
			floorIndexTime = times.get(lookupFloorIndex);

			if (ceilIndexTime < animationStart
					|| floorIndexTime > animationEnd
					|| (floorIndexTime < animationStart && ceilIndexTime > animationEnd)) {
//				System.out.println(name + ", ~~~~ identity(localTypeId)1 " + localTypeId + " id: " + identity(localTypeId));
				return (Integer) identity(localTypeId);
			}
			if ((floorIndex == -1) || (floorIndexTime < animationStart)) {
				floorValue = values.get(floorAnimEndIndex);
			} else if ((ceilIndexTime > animationEnd || (ceilIndexTime < time && times.get(floorAnimEndIndex) < time))
					&& times.get(floorAnimStartIndex) == animationStart) {
				if (times.get(floorAnimStartIndex) == animationStart) {
					ceilIndex = ceilIndex(animationStart);
				}
			}
		}
		if (floorIndex == ceilIndex) {
			return floorValue;
		}

		// Integer
		if (localTypeId == TEXTUREID) {
			return switch (interpolationType) {
				// dont use linear on these, does that even make any sense?
				// dont use hermite on these, does that even make any sense?
				// dont use bezier on these, does that even make any sense?
				case DONT_INTERP, BEZIER, HERMITE, LINEAR -> floorValue;
			};
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

		timeline.name = getWar3ID(container);
		timeline.interpolationType = interpolationType;
		timeline.globalSequenceId = getGlobalSeqId();

		final List<Integer> times = getTimes();
		final List<Integer> values = getValues();
		final List<Integer> inTans = getInTans();
		final List<Integer> outTans = getOutTans();

		final long[] tempFrames = new long[times.size()];
		final long[][] tempValues = new long[times.size()][];
		final long[][] tempInTans = new long[times.size()][];
		final long[][] tempOutTans = new long[times.size()][];

		final boolean hasTangents = timeline.interpolationType.tangential();

		for (int i = 0, l = times.size(); i < l; i++) {
			final Integer value = values.get(i);

			tempFrames[i] = times.get(i).longValue();


			tempValues[i] = (new long[] {value.longValue()});

			if (hasTangents) {
				tempInTans[i] = new long[] {inTans.get(i).longValue()};
				tempOutTans[i] = new long[] {outTans.get(i).longValue()};
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
		// Timescales a part of the AnimFlag from the source into the new time "newStart" to "newEnd"
		boolean tans = source.tans();
		if (tans && interpolationType == InterpolationType.LINEAR) {
			final int x = JOptionPane.showConfirmDialog(null,
					"ERROR! A source was found to have Linear and Nonlinear motion simultaneously. Does the following have non-zero data? " + source.inTans,
					"Help This Program!", JOptionPane.YES_NO_OPTION);
			if (x == JOptionPane.NO_OPTION) {
				tans = false;
			}
		}
		for (final Integer time : source.times) {
			final int index = source.times.indexOf(time);
			if ((time >= sourceStart) && (time <= sourceEnd)) {
				// If this "time" is a part of the anim being rescaled
				final double ratio = (double) (time - sourceStart) / (double) (sourceEnd - sourceStart);
				times.add((int) (newStart + (ratio * (newEnd - newStart))));
				values.add(source.values.get(index));
				if (tans) {
					inTans.add(source.inTans.get(index));
					outTans.add(source.outTans.get(index));
				}
			}
		}
		sort();
	}

	public void copyFrom(final IntAnimFlag source) {
		times.addAll(source.times);
		values.addAll(source.values);
		if (source.tans() && tans()) {
			inTans.addAll(source.inTans);
			outTans.addAll(source.outTans);
		} else if (tans()) {
			JOptionPane.showMessageDialog(null,
					"Some animations will lose complexity due to transfer incombatibility. There will probably be no visible change.");
			inTans.clear();
			outTans.clear();
			interpolationType = InterpolationType.LINEAR;
			// Probably makes this flag linear, but certainly makes it more like the copy source
		}
	}
}
