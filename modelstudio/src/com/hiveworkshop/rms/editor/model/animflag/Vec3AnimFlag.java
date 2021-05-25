package com.hiveworkshop.rms.editor.model.animflag;

import com.hiveworkshop.rms.editor.model.TimelineContainer;
import com.hiveworkshop.rms.parsers.mdlx.InterpolationType;
import com.hiveworkshop.rms.parsers.mdlx.timeline.MdlxFloatArrayTimeline;
import com.hiveworkshop.rms.util.Vec3;

/**
 * A java class for MDL "motion flags," such as Alpha, Translation, Scaling, or
 * Rotation. AnimFlags are not "real" things from an MDL and are given this name
 * by me, as an invented java class to simplify the programming
 *
 * Eric Theller 11/5/2011
 */
public class Vec3AnimFlag extends AnimFlag<Vec3> {

	public Vec3AnimFlag(String title) {
		super(title);
	}

	public Vec3AnimFlag(AnimFlag<Vec3> af) {
		super(af);
	}

	public Vec3AnimFlag(final MdlxFloatArrayTimeline timeline) {
		super(timeline);

		final long[] frames = timeline.frames;
		final Object[] values = timeline.values;
		final Object[] inTans = timeline.inTans;
		final Object[] outTans = timeline.outTans;

		if (frames.length > 0) {
			final boolean hasTangents = interpolationType.tangential();

			for (int i = 0, l = frames.length; i < l; i++) {
				final Object value = values[i];
				Vec3 valueAsObject = new Vec3((float[]) value);

				Vec3 inTanAsObject = null;
				Vec3 outTanAsObject = null;

				if (hasTangents) {
					inTanAsObject = new Vec3((float[]) inTans[i]);
					outTanAsObject = new Vec3((float[]) outTans[i]);
				}

				addEntry((int) frames[i], valueAsObject, inTanAsObject, outTanAsObject);
			}
		}
	}

	public Vec3AnimFlag(String title, InterpolationType interpolationType, Integer globalSeq){
		super(title);
		this.interpolationType = interpolationType;
		setGlobSeq(globalSeq);
	}

	public AnimFlag<Vec3> getEmptyCopy(){
		Vec3AnimFlag newFlag = new Vec3AnimFlag(name);
		newFlag.setSettingsFrom(this);
		return newFlag;
	}
	public AnimFlag<Vec3> deepCopy(){
		return new Vec3AnimFlag(this);
	}

	public Vec3 cloneValue(Object value) {
		if(value instanceof Vec3){
			return new Vec3((Vec3) value);
		}
		return null;
	}

	@Override
	public MdlxFloatArrayTimeline toMdlx(final TimelineContainer container) {
		final MdlxFloatArrayTimeline timeline = new MdlxFloatArrayTimeline(3);

		timeline.name = FlagUtils.getWar3ID(name, container);
		timeline.interpolationType = interpolationType;
		timeline.globalSequenceId = getGlobalSeqId();

		long[] tempFrames = new long[entryMap.size()];
		float[][] tempValues = new float[entryMap.size()][];
		float[][] tempInTans = new float[entryMap.size()][];
		float[][] tempOutTans = new float[entryMap.size()][];

		boolean hasTangents = timeline.interpolationType.tangential();

		for (int i = 0, l = entryMap.size(); i < l; i++) {
			tempFrames[i] = getTimeFromIndex(i);
			tempValues[i] = getValueFromIndex(i).toFloatArray();

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
}
