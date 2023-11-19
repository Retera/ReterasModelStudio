package com.hiveworkshop.rms.editor.model.animflag;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.GlobalSeq;
import com.hiveworkshop.rms.editor.model.TimelineContainer;
import com.hiveworkshop.rms.parsers.mdlx.InterpolationType;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.parsers.mdlx.timeline.MdlxFloatArrayTimeline;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.util.Vec3;

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
public class Vec3AnimFlag extends AnimFlag<Vec3> {

	public Vec3AnimFlag(String title) {
		super(title);
	}

	protected Vec3AnimFlag(AnimFlag<Vec3> af) {
		super(af);
	}

	public Vec3AnimFlag(String title, InterpolationType interpolationType, GlobalSeq globalSeq) {
		super(title);
		this.interpolationType = interpolationType;
		setGlobSeq(globalSeq);
	}

	public Vec3AnimFlag(final MdlxFloatArrayTimeline timeline, EditableModel model) {
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
				Vec3 valueAsObject = new Vec3((float[]) value);

				Vec3 inTanAsObject = null;
				Vec3 outTanAsObject = null;

				if (hasTangents) {
					inTanAsObject = new Vec3((float[]) inTans[i]);
					outTanAsObject = new Vec3((float[]) outTans[i]);
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

	public AnimFlag<Vec3> getEmptyCopy() {
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
	public MdlxFloatArrayTimeline toMdlx(final TimelineContainer container, EditableModel model) {
		final MdlxFloatArrayTimeline mdlxTimeline = new MdlxFloatArrayTimeline(3, FlagUtils.getWar3ID(name, container));

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
////		for (Sequence anim : new TreeSet<>(sequenceMap.keySet())) {
//		for (Sequence anim : model.getAllSequences()) {
//			if (globalSeq == null || anim == globalSeq) {
//				TreeMap<Integer, Entry<Vec3>> entryTreeMap = sequenceMap.get(anim);
//				if(entryTreeMap != null){
//					for (Integer time : entryTreeMap.keySet()) {
//						if (time > anim.getLength()) {
//							break;
//						}
//						Entry<Vec3> entry = entryTreeMap.get(time);
////					tempFrames2.add(time + Math.max(anim.getStart(), tempFrames2.get(tempFrames2.size()-1) + 10));
//						tempFrames2.add(time + anim.getStart());
//						tempValues2.add(entry.getValue().toFloatArray());
//						if (tans()) {
//							tempInTans2.add(entry.getInTan().toFloatArray());
//							tempOutTans2.add(entry.getOutTan().toFloatArray());
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
//	@Override
//	public MdlxFloatArrayTimeline toMdlx(final TimelineContainer container, EditableModel model) {
//		final MdlxFloatArrayTimeline mdlxTimeline = new MdlxFloatArrayTimeline(3);
//
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
////		for (Sequence anim : new TreeSet<>(sequenceMap.keySet())) {
//		for (Sequence anim : model.getAllSequences()) {
//			if (globalSeq == null || anim == globalSeq) {
//				TreeMap<Integer, Entry<Vec3>> entryTreeMap = sequenceMap.get(anim);
//				if(entryTreeMap != null){
//					for (Integer time : entryTreeMap.keySet()) {
//						if (time > anim.getLength()) {
//							break;
//						}
//						Entry<Vec3> entry = entryTreeMap.get(time);
////					tempFrames2.add(time + Math.max(anim.getStart(), tempFrames2.get(tempFrames2.size()-1) + 10));
//						tempFrames2.add(time + anim.getStart());
//						tempValues2.add(entry.getValue().toFloatArray());
//						if (tans()) {
//							tempInTans2.add(entry.getInTan().toFloatArray());
//							tempOutTans2.add(entry.getOutTan().toFloatArray());
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
//	}


	@Override
	protected Vec3 getIdentity() {
		if(name.equals(MdlUtils.TOKEN_TRANSLATION)){
			return Vec3.ZERO;
		} else {
			return Vec3.ONE;
		}
	}

	@Override
	public Vec3 getInterpolatedValue(Entry<Vec3> entryFloor, Entry<Vec3> entryCeil, float timeFactor) {
		Vec3 floorValue = entryFloor.getValue();
		Vec3 floorOutTan = entryFloor.getOutTan();

		Vec3 ceilValue = entryCeil.getValue();
		Vec3 ceilInTan = entryCeil.getInTan();

		return switch (interpolationType) {
			case BEZIER -> Vec3.getBezier(floorValue, floorOutTan, ceilInTan, ceilValue, timeFactor);
			case DONT_INTERP -> floorValue;
			case HERMITE -> Vec3.getHermite(floorValue, floorOutTan, ceilInTan, ceilValue, timeFactor);
			case LINEAR -> Vec3.getLerped(floorValue, ceilValue, timeFactor);
		};
	}

	@Override
	public float[] getTbcFactor(float bias, float tension, float continuity) {
		return getTCB(1, bias, tension, continuity);
	}

	@Override
	public void calcNewTans(float[] factor, Entry<Vec3> next, Entry<Vec3> prev, Entry<Vec3> cur, int animationLength) {
		// Calculating the derivatives in point Cur (for count cells)
		if (cur.inTan == null) {
			cur.inTan = new Vec3(0, 0, 0);
			cur.outTan = new Vec3(0, 0, 0);
		}

		Vec3 currPrev = new Vec3(cur.value);
		Vec3 nextCurr = new Vec3(0, 0, 0).sub(cur.value);
		if (prev == null) {
			currPrev.sub(cur.value);
		} else {
			currPrev.sub(prev.value);
		}
		if (next == null) {
			nextCurr.add(cur.value);
		} else {
			nextCurr.add(next.value);
		}
//		if (prev != null) {
//			currPrev.sub(prev.value);
//		}
//		if (next != null) {
//			nextCurr.add(next.value);
//		}

		cur.inTan.set(currPrev).scale(factor[0]).addScaled(nextCurr, factor[1]);
		cur.outTan.set(currPrev).scale(factor[2]).addScaled(nextCurr, factor[3]);
//		System.out.println("currPrev: " + currPrev);
//		System.out.println("nextCurr: " + nextCurr);
//		System.out.println("factor: " + Arrays.toString(factor));

		if (next != null && prev != null && !next.time.equals(prev.time)) {
			float timeBetweenFrames = (next.time - prev.time + animationLength) % animationLength;
			int timeToPrevFrame = (cur.time - prev.time + animationLength) % animationLength;
			int timeToNextFrame = (next.time - cur.time + animationLength) % animationLength;

//			System.out.println("timeBetweenFrames: " + timeBetweenFrames);

			float inAdj = 2 * timeToPrevFrame / timeBetweenFrames;
			float outAdj = 2 * timeToNextFrame / timeBetweenFrames;
			cur.inTan.scale(inAdj);
			cur.outTan.scale(outAdj);
		}

	}

	public Vec3AnimFlag getAsTypedOrNull(AnimFlag<?> animFlag){
		if(animFlag instanceof Vec3AnimFlag){
			return (Vec3AnimFlag) animFlag;
		}
		return null;
	}
}
