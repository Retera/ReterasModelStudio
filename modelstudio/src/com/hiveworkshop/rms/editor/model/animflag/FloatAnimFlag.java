package com.hiveworkshop.rms.editor.model.animflag;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.GlobalSeq;
import com.hiveworkshop.rms.editor.model.TimelineContainer;
import com.hiveworkshop.rms.parsers.mdlx.InterpolationType;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.parsers.mdlx.timeline.MdlxFloatTimeline;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.util.MathUtils;

import java.util.ArrayList;
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

	public FloatAnimFlag(String title) {
		super(title);
	}

	protected FloatAnimFlag(AnimFlag<Float> af) {
		super(af);
	}

	public FloatAnimFlag(String title, InterpolationType interpolationType, GlobalSeq globalSeq) {
		super(title);
		this.interpolationType = interpolationType;
		setGlobSeq(globalSeq);
	}

	public FloatAnimFlag(final MdlxFloatTimeline timeline, EditableModel model) {
		super(timeline, model);

		final long[] frames = timeline.frames;
		final Object[] values = timeline.values;
		final Object[] inTans = timeline.inTans;
		final Object[] outTans = timeline.outTans;

		TreeMap<Integer, Animation> animationTreeMap = getAnimationTreeMap(model.getAnims());

		if (frames.length > 0) {
			List<Integer> outsideKFs = new ArrayList<>();
			final boolean hasTangents = interpolationType.tangential();

			for (int i = 0, l = frames.length; i < l; i++) {
				final Object value = values[i];
				float valueAsObject = ((float[]) value)[0];

				Float inTanAsObject = null;
				Float outTanAsObject = null;

				if (hasTangents) {
					inTanAsObject = ((float[]) inTans[i])[0];
					outTanAsObject = ((float[]) outTans[i])[0];
				}

				if (hasGlobalSeq()) {
					addEntry((int) frames[i] - globalSeq.getStart(), valueAsObject, inTanAsObject, outTanAsObject, globalSeq);
				} else if (animationTreeMap.floorEntry((int) frames[i]) != null) {
					Sequence sequence = animationTreeMap.floorEntry((int) frames[i]).getValue();
					addEntry((int) frames[i] - sequence.getStart(), valueAsObject, inTanAsObject, outTanAsObject, sequence);
				} else {
					outsideKFs.add((int) frames[i]);
				}
			}
//			System.out.println(name + " has " + outsideKFs.size() + " frames outside of animations");
		}
	}

	public AnimFlag<Float> getEmptyCopy(){
		FloatAnimFlag newFlag = new FloatAnimFlag(name);
		newFlag.setSettingsFrom(this);
		return newFlag;
	}
	public AnimFlag<Float> deepCopy(){
		return new FloatAnimFlag(this);
	}

	public Float cloneValue(Object value) {
		if(value instanceof Float){
			return (Float) value;
		}
		return null;
	}

	@Override
	protected Float getIdentity() {
		if(name.equals(MdlUtils.TOKEN_ALPHA) || name.equals(MdlUtils.TOKEN_VISIBILITY)){
			return 1f;
		} else {
			return 0f;
		}
	}

	@Override
	public Float getInterpolatedValue(Entry<Float> entryFloor, Entry<Float> entryCeil, float timeFactor) {
		Float floorValue = entryFloor.getValue();
		Float floorOutTan = entryFloor.getOutTan();

		Float ceilValue = entryCeil.getValue();
		Float ceilInTan = entryCeil.getInTan();

		return switch (interpolationType) {
			case BEZIER -> MathUtils.bezier(floorValue, floorOutTan, ceilInTan, ceilValue, timeFactor);
			case DONT_INTERP -> floorValue;
			case HERMITE -> MathUtils.hermite(floorValue, floorOutTan, ceilInTan, ceilValue, timeFactor);
			case LINEAR -> MathUtils.lerp(floorValue, ceilValue, timeFactor);
		};
	}

	@Override
	public MdlxFloatTimeline toMdlx(final TimelineContainer container, EditableModel model) {
		final MdlxFloatTimeline mdlxTimeline = new MdlxFloatTimeline(FlagUtils.getWar3ID(name, container));

		toMdlx3(mdlxTimeline, container, model);

		return mdlxTimeline;

//		mdlxTimeline.name = FlagUtils.getWar3ID(name, container);
//		mdlxTimeline.interpolationType = interpolationType;
//		mdlxTimeline.globalSequenceId = getGlobalSeqId(model);
//
//
//		ArrayList<Integer> tempFrames2 = new ArrayList<>();
//		ArrayList<float[]> tempValues2 = new ArrayList<>();
//		ArrayList<float[]> tempInTans2 = new ArrayList<>();
//		ArrayList<float[]> tempOutTans2 = new ArrayList<>();
//
////		for (Sequence anim : sequenceMap.keySet()) {
////		for (Sequence anim : new TreeSet<>(sequenceMap.keySet())) {
//		for (Sequence anim : model.getAllSequences()) {
//			System.out.println(anim);
//			if (globalSeq == null || anim == globalSeq) {
//				TreeMap<Integer, Entry<Float>> entryTreeMap = sequenceMap.get(anim);
//				if(entryTreeMap != null){
//					for (Integer time : entryTreeMap.keySet()) {
//						if (time > anim.getLength()) {
//							break;
//						}
//						Entry<Float> entry = entryTreeMap.get(time);
////					tempFrames2.add(time + Math.max(anim.getStart(), tempFrames2.get(tempFrames2.size()-1) + 10));
//						tempFrames2.add(time + anim.getStart());
//						tempValues2.add(new float[] {entry.getValue()});
//						if (tans()) {
//							tempInTans2.add(new float[] {entry.getInTan()});
//							tempOutTans2.add(new float[] {entry.getOutTan()});
//						} else {
//							tempInTans2.add(new float[] {0});
//							tempOutTans2.add(new float[] {0});
//						}
//					}
//				}
//			}
//		}
//
//		int size = tempFrames2.size();
//		long[] tempFrames = new long[size];
//		float[][] tempValues = new float[size][];
//		float[][] tempInTans = new float[size][];
//		float[][] tempOutTans = new float[size][];
//
//		for (int i = 0; i < size; i++) {
//			tempFrames[i] = tempFrames2.get(i);
//			tempValues[i] = tempValues2.get(i);
//			tempInTans[i] = tempInTans2.get(i);
//			tempOutTans[i] = tempOutTans2.get(i);
//		}
//
//		mdlxTimeline.frames = tempFrames;
//		mdlxTimeline.values = tempValues;
//		mdlxTimeline.inTans = tempInTans;
//		mdlxTimeline.outTans = tempOutTans;
//
//		return mdlxTimeline;
	}

	@Override
	public float[] getTcbFactor(float tension, float continuity, float bias) {
		return getTCB(1, tension, continuity, bias);
	}

	@Override
	public void calcNewTans(float[] factor, Entry<Float> next, Entry<Float> prev, Entry<Float> cur, Integer animationLength) {
		// Calculating the derivatives in point Cur (for count cells)

//		float currPrev = cur.value - prev.value;
//		float nextCurr = next.value - cur.value;

		float currPrev = cur.value;
		if (prev != null) {
			currPrev -= prev.value;
		}
		float nextCurr = -cur.value;
		if (next != null) {
			nextCurr += next.value;
		}

		cur.inTan = currPrev * factor[0] + nextCurr * factor[1];
		cur.outTan = currPrev * factor[2] + nextCurr * factor[3];

		if (animationLength != null && next != null && prev != null && !next.time.equals(prev.time)) {
			float timeBetweenFrames = (next.time - prev.time + animationLength) % animationLength;
			int timeToPrevFrame = (cur.time - prev.time + animationLength) % animationLength;
			int timeToNextFrame = (next.time - cur.time + animationLength) % animationLength;

			float inAdj = 2 * timeToPrevFrame / timeBetweenFrames;
			float outAdj = 2 * timeToNextFrame / timeBetweenFrames;
			cur.inTan *= inAdj;
			cur.outTan *= outAdj;
		}
	}

	public FloatAnimFlag getAsTypedOrNull(AnimFlag<?> animFlag){
		if(animFlag instanceof FloatAnimFlag){
			return (FloatAnimFlag) animFlag;
		}
		return null;
	}
}
