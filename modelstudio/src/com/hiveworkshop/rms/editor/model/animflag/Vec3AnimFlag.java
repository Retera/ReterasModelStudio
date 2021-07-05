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
		final MdlxFloatArrayTimeline mdlxTimeline = new MdlxFloatArrayTimeline(3);

		mdlxTimeline.name = FlagUtils.getWar3ID(name, container);
		mdlxTimeline.interpolationType = interpolationType;
		mdlxTimeline.globalSequenceId = getGlobalSeqId();

		long[] tempFrames = new long[entryMap.size()];
		float[][] tempValues = new float[entryMap.size()][];
		float[][] tempInTans = new float[entryMap.size()][];
		float[][] tempOutTans = new float[entryMap.size()][];

		boolean hasTangents = mdlxTimeline.interpolationType.tangential();

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

		mdlxTimeline.frames = tempFrames;
		mdlxTimeline.values = tempValues;
		mdlxTimeline.inTans = tempInTans;
		mdlxTimeline.outTans = tempOutTans;

		return mdlxTimeline;
	}

	@Override
	public Vec3 getIdentity(int typeid) {
		return (Vec3) identity(typeid);
	}

	public Vec3 getInterpolatedValue(Integer floorTime, Integer ceilTime, float timeFactor) {
		Entry<Vec3> entryFloor = entryMap.get(floorTime);
		Entry<Vec3> entryCeil = entryMap.get(ceilTime);

		return getInterpolatedValue(entryFloor, entryCeil, timeFactor);
	}

	@Override
	public Vec3 getInterpolatedValue(Entry<Vec3> entryFloor, Entry<Vec3> entryCeil, float timeFactor) {
		Vec3 floorValue = entryFloor.getValue();
		Vec3 floorOutTan = entryFloor.getOutTan();

		Vec3 ceilValue = entryCeil.getValue();
		Vec3 ceilInTan = entryCeil.getInTan();

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

	@Override
	public float[] getTbcFactor(float bias, float tension, float continuity) {
		return getTCB(-1, bias, tension, continuity);
	}

	@Override
	public void calcNewTans(float factor[], Entry<Vec3> next, Entry<Vec3> prev, Entry<Vec3> cur, int animationLength) {
		// Calculating the derivatives in point Cur (for count cells)
		if (cur.inTan == null) {
			cur.inTan = new Vec3(0, 0, 0);
			cur.outTan = new Vec3(0, 0, 0);
		}

		Vec3 currPrev = new Vec3(cur.value);
		Vec3 nextCurr = new Vec3(0, 0, 0).sub(cur.value);
		if (prev != null) {
			currPrev.sub(prev.value);
		}
		if (next != null) {
			nextCurr.add(next.value);
		}

		cur.inTan.set(currPrev).scale(factor[0]).addScaled(nextCurr, factor[1]);
		cur.outTan.set(currPrev).scale(factor[2]).addScaled(nextCurr, factor[3]);

		if (next != null && prev != null && !next.time.equals(prev.time)) {
			float timeBetweenFrames = (next.time - prev.time + animationLength) % animationLength;
			int timeToPrevFrame = (cur.time - prev.time + animationLength) % animationLength;
			int timeToNextFrame = (next.time - cur.time + animationLength) % animationLength;

			float inAdj = 2 * timeToPrevFrame / timeBetweenFrames;
			float outAdj = 2 * timeToNextFrame / timeBetweenFrames;
			cur.inTan.scale(inAdj);
			cur.outTan.scale(outAdj);
		}

	}
}
