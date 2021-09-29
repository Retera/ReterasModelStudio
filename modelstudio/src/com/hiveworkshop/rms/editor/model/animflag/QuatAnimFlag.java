package com.hiveworkshop.rms.editor.model.animflag;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.GlobalSeq;
import com.hiveworkshop.rms.editor.model.TimelineContainer;
import com.hiveworkshop.rms.parsers.mdlx.InterpolationType;
import com.hiveworkshop.rms.parsers.mdlx.timeline.MdlxFloatArrayTimeline;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.util.Quat;

import java.util.TreeMap;

/**
 * A java class for MDL "motion flags," such as Alpha, Translation, Scaling, or
 * Rotation. AnimFlags are not "real" things from an MDL and are given this name
 * by me, as an invented java class to simplify the programming
 *
 * Eric Theller 11/5/2011
 */
public class QuatAnimFlag extends AnimFlag<Quat> {

	public QuatAnimFlag(String title) {
		super(title);
	}

	protected QuatAnimFlag(AnimFlag<Quat> af) {
		super(af);
	}

	public QuatAnimFlag(final MdlxFloatArrayTimeline timeline, EditableModel model) {
		super(timeline, model);

		final long[] frames = timeline.frames;
		final Object[] values = timeline.values;
		final Object[] inTans = timeline.inTans;
		final Object[] outTans = timeline.outTans;

		TreeMap<Integer, Animation> animationTreeMap = new TreeMap<>();
		model.getAnims().forEach(a -> animationTreeMap.put(a.getStart(), a));

		if (frames.length > 0) {
			boolean hasTangents = interpolationType.tangential();

			for (int i = 0, l = frames.length; i < l; i++) {
				final Object value = values[i];
				Quat valueAsObject = new Quat((float[]) value);

				Quat inTanAsObject = null;
				Quat outTanAsObject = null;

				if (hasTangents) {
					inTanAsObject = new Quat((float[]) inTans[i]);
					outTanAsObject = new Quat((float[]) outTans[i]);
				}

				if (hasGlobalSeq()) {
					addEntry((int) frames[i] - globalSeq.getStart(), valueAsObject, inTanAsObject, outTanAsObject, globalSeq);
				} else if (animationTreeMap.floorEntry((int) frames[i]) != null) {
					Sequence sequence = animationTreeMap.floorEntry((int) frames[i]).getValue();
					addEntry((int) frames[i] - sequence.getStart(), valueAsObject, inTanAsObject, outTanAsObject, sequence);
				}
			}
		}
	}

	public QuatAnimFlag(String title, InterpolationType interpolationType, GlobalSeq globalSeq) {
		super(title);
		this.interpolationType = interpolationType;
		setGlobSeq(globalSeq);
	}

	public AnimFlag<Quat> getEmptyCopy() {
		QuatAnimFlag newFlag = new QuatAnimFlag(name);
		newFlag.setSettingsFrom(this);
		return newFlag;
	}
	public AnimFlag<Quat> deepCopy(){
		return new QuatAnimFlag(this);
	}

	public Quat cloneValue(Object value) {
		if(value instanceof Quat){
			return new Quat((Quat) value);
		}
		return null;
	}

	@Override
	public MdlxFloatArrayTimeline toMdlx(final TimelineContainer container, EditableModel model) {
		final MdlxFloatArrayTimeline mdlxTimeline = new MdlxFloatArrayTimeline(4);

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
//				TreeMap<Integer, Entry<Quat>> entryTreeMap = sequenceMap.get(anim);
//				if(entryTreeMap != null){
//					for (Integer time : entryTreeMap.keySet()) {
//						if (time > anim.getLength()) {
//							break;
//						}
//						Entry<Quat> entry = entryTreeMap.get(time);
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

	protected Quat getIdentity(int typeId) {
		return (Quat) identity(typeId);
	}

	@Override
	public Quat getInterpolatedValue(Integer floorTime, Integer ceilTime, float timeFactor, Sequence anim) {
		TreeMap<Integer, Entry<Quat>> entryMap = sequenceMap.get(anim);
		Entry<Quat> entryFloor = entryMap.get(floorTime);
		Entry<Quat> entryCeil = entryMap.get(ceilTime);

		return getInterpolatedValue(entryFloor, entryCeil, timeFactor);
	}

	@Override
	public Quat getInterpolatedValue(Entry<Quat> entryFloor, Entry<Quat> entryCeil, float timeFactor) {
		Quat floorValue = entryFloor.getValue();
		Quat floorOutTan = entryFloor.getOutTan();

		Quat ceilValue = entryCeil.getValue();
		Quat ceilInTan = entryCeil.getInTan();

//		System.out.println(typeid);
		if (typeid == ROTATION) {
			return switch (interpolationType) {
				case BEZIER -> Quat.getSquad(floorValue, ceilValue, floorOutTan, ceilInTan, timeFactor);
				case DONT_INTERP -> floorValue;
				case HERMITE -> Quat.getSquad(floorValue, ceilValue, floorOutTan, ceilInTan, timeFactor);
				case LINEAR -> Quat.getSlerped(floorValue, ceilValue, timeFactor);
			};
		}
		throw new IllegalStateException();
	}

	@Override
	public float[] getTbcFactor(float bias, float tension, float continuity) {
		return getTCB(-1, bias, tension, continuity);
	}

	@Override
	public void calcNewTans(float[] factor, Entry<Quat> next, Entry<Quat> prev, Entry<Quat> cur, int animationLength) {

		Quat logNNP = new Quat(cur.value).invertQuat();
		if (next != null) {
			logNNP.mul(next.value);
		}
		calcLogQ(logNNP);


		Quat logNMN = new Quat(0, 0, 0, 1);
		if (prev != null) {
			logNMN.set(prev.value);
		}
		logNMN.mul(cur.value);
		calcLogQ(logNMN);

		if (cur.inTan == null) {
			cur.inTan = new Quat(0, 0, 0, 1);
			cur.outTan = new Quat(0, 0, 0, 1);
		}

		cur.inTan.set(logNNP).scale(factor[0]).addScaled(logNMN, factor[1]);
		cur.outTan.set(logNNP).scale(factor[2]).addScaled(logNMN, factor[3]);

		cur.outTan.sub(logNNP).scale(0.5f);
		cur.outTan.w = 0;
		calcExpQ(cur.outTan);
		cur.outTan.mulLeft(cur.value);

		cur.inTan.scale(-1).add(logNMN).scale(0.5f);
		cur.inTan.w = cur.outTan.w;
		calcExpQ(cur.inTan);
		cur.inTan.mulLeft(cur.value);

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

	private Quat calcLogQ(Quat q) {
		if (q.w > 0.99999) {
			q.w = 0.99999f;
		}
		float sinT = (float) (Math.acos(q.w) / Math.sqrt(1 - (q.w * q.w)));
		q.scale(sinT);
		q.w = 0;
		return q;
	}

	private Quat calcExpQ(Quat q) {
		float t = q.length();
		if (t < 1e-5) {
			return (Quat) q.set(1, 0, 0, 0);
		}

		q.scale((float) Math.sin(t) / t);
		q.w = (float) Math.cos(t);
		return q;
	}
}
