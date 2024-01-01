package com.hiveworkshop.rms.editor.model.animflag;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.TimelineContainer;
import com.hiveworkshop.rms.parsers.mdlx.InterpolationType;
import com.hiveworkshop.rms.parsers.mdlx.timeline.MdlxUInt32Timeline;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;

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
public class BitmapAnimFlag extends AnimFlag<Bitmap> {

	public BitmapAnimFlag(String title) {
		super(title);
		interpolationType = InterpolationType.DONT_INTERP;
	}

	protected BitmapAnimFlag(AnimFlag<Bitmap> af) {
		super(af);
		interpolationType = InterpolationType.DONT_INTERP;
	}

	public BitmapAnimFlag(final MdlxUInt32Timeline timeline, EditableModel model) {
		super(timeline, model);
		interpolationType = InterpolationType.DONT_INTERP;

		if (timeline.interpolationType != InterpolationType.DONT_INTERP) {
			System.out.println("(IntAnimFlag) timeline \"" + name + "\" has interpolation type: " + timeline.interpolationType.name());
		}

		final long[] frames = timeline.frames;
		final Object[] values = timeline.values;
		final Object[] inTans = timeline.inTans;
		final Object[] outTans = timeline.outTans;

		TreeMap<Integer, Animation> animationTreeMap = getAnimationTreeMap(model.getAnims());


		if (frames.length > 0) {
			List<Integer> outsideKFs = new ArrayList<>();
			boolean hasTangents = interpolationType.tangential();

			for (int i = 0, l = frames.length; i < l; i++) {
				final long[] value = (long[]) values[i];
				Bitmap valueAsObject = model.getTexture((int) value[0]);
				Bitmap inTanAsObject = null;
				Bitmap outTanAsObject = null;
//
//				valueAsObject = (int) value[0];
//
//				if (hasTangents) {
//					inTanAsObject = (int) ((long[]) inTans[i])[0];
//					outTanAsObject = (int) ((long[]) outTans[i])[0];
//				}
//
				if (hasGlobalSeq()) {
//					System.out.println("Global seq! " + globalSeq);
					addEntry((int) frames[i] - globalSeq.getStart(), valueAsObject, inTanAsObject, outTanAsObject, globalSeq);
				} else if (animationTreeMap.floorEntry((int) frames[i]) != null) {
					Sequence sequence = animationTreeMap.floorEntry((int) frames[i]).getValue();
					addEntry((int) frames[i] - sequence.getStart(), valueAsObject, inTanAsObject, outTanAsObject, sequence);
				} else {
					outsideKFs.add((int) frames[i]);
				}
			}
			System.out.println(name + " has " + outsideKFs.size() + " frames outside of animations");
		}
	}

//	private long[][] getArray(Entry<Bitmap> entry, MdlxUInt32Timeline line, EditableModel model){
////		return new int[][]{new int[]{entry.getValue()}, new int[]{entry.getInTan()}, new int[]{entry.getOutTan()}};
//		return new long[][]{new long[]{model.getTextureId(entry.getValue())}, new long[]{0}, new long[]{0}};
//	}


	public AnimFlag<Bitmap> getEmptyCopy(){
		BitmapAnimFlag newFlag = new BitmapAnimFlag(name);
		newFlag.setSettingsFrom(this);
		return newFlag;
	}
	public AnimFlag<Bitmap> deepCopy(){
		return new BitmapAnimFlag(this);
	}

	public Bitmap cloneValue(Object value) {
		if(value instanceof Bitmap){
			return (Bitmap) value;
		}
		return null;
	}

	@Override
	protected Bitmap getIdentity() {
		TreeMap<Integer, Entry<Bitmap>> map = sequenceMap.values().stream().findFirst().orElse(null);
		if(map != null){
			return map.firstEntry().getValue().getValue();
		}
//		return (Bitmap) identity(typeId);
		return null;
	}

	@Override
	public Bitmap getInterpolatedValue(Entry<Bitmap> entryFloor, Entry<Bitmap> entryCeil, float timeFactor) {
		Bitmap floorValue = entryFloor.getValue();
		Bitmap floorOutTan = entryFloor.getOutTan();

		Bitmap ceilValue = entryCeil.getValue();
		Bitmap ceilInTan = entryCeil.getInTan();

		return switch (interpolationType) {
			case DONT_INTERP, BEZIER, HERMITE, LINEAR -> floorValue;
		};
	}

	@Override
	public void setInterpType(final InterpolationType interpolationType) {
		this.interpolationType = InterpolationType.DONT_INTERP;
	}

	@Override
	public MdlxUInt32Timeline toMdlx(final TimelineContainer container, EditableModel model) {
		final MdlxUInt32Timeline mdlxTimeline = new MdlxUInt32Timeline(FlagUtils.getWar3ID(name, container));

		toMdlx3(mdlxTimeline, container, model);

		return mdlxTimeline;

//		mdlxTimeline.name = FlagUtils.getWar3ID(name, container);
//		mdlxTimeline.interpolationType = interpolationType;
//		mdlxTimeline.globalSequenceId = getGlobalSeqId(model);
//
//
//		ArrayList<Integer> tempFrames2 = new ArrayList<>();
//		ArrayList<long[]> tempValues2 = new ArrayList<>();
//		ArrayList<long[]> tempInTans2 = new ArrayList<>();
//		ArrayList<long[]> tempOutTans2 = new ArrayList<>();
//
////		for (Sequence anim : new TreeSet<>(sequenceMap.keySet())) {
//		for (Sequence anim : model.getAllSequences()) {
//			if (globalSeq == null || anim == globalSeq) {
//				TreeMap<Integer, Entry<Integer>> entryTreeMap = sequenceMap.get(anim);
//				if(entryTreeMap != null){
//					for (Integer time : entryTreeMap.keySet()) {
//						if (time > anim.getLength()) {
//							break;
//						}
//						Entry<Integer> entry = entryTreeMap.get(time);
////					tempFrames2.add(time + Math.max(anim.getStart(), tempFrames2.get(tempFrames2.size()-1) + 10));
//						tempFrames2.add(time + anim.getStart());
//						tempValues2.add(new long[] {entry.getValue()});
//						if (tans()) {
//							tempInTans2.add(new long[] {entry.getInTan()});
//							tempOutTans2.add(new long[] {entry.getOutTan()});
//						} else {
//							tempInTans2.add(new long[] {0});
//							tempOutTans2.add(new long[] {0});
//						}
//					}
//				}
//			}
//		}
//
//		int size = tempFrames2.size();
//		long[] tempFrames = new long[size];
//		long[][] tempValues = new long[size][];
//		long[][] tempInTans = new long[size][];
//		long[][] tempOutTans = new long[size][];
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
	public void calcNewTans(float[] factor, Entry<Bitmap> next, Entry<Bitmap> prev, Entry<Bitmap> cur, Integer animationLength) {
		// Calculating the derivatives in point Cur (for count cells)

//		int currPrev = cur.value;
//		if (prev != null) {
//			currPrev -= prev.value;
//		}
//		int nextCurr = -cur.value;
//		if (next != null) {
//			nextCurr += next.value;
//		}
//
//		cur.inTan = (int) (currPrev * factor[0] + nextCurr * factor[1]);
//		cur.outTan = (int) (currPrev * factor[2] + nextCurr * factor[3]);
//
//		if (next != null && prev != null && !next.time.equals(prev.time)) {
//			float timeBetweenFrames = (next.time - prev.time + animationLength) % animationLength;
//			int timeToPrevFrame = (cur.time - prev.time + animationLength) % animationLength;
//			int timeToNextFrame = (next.time - cur.time + animationLength) % animationLength;
//
//			float inAdj = 2 * timeToPrevFrame / timeBetweenFrames;
//			float outAdj = 2 * timeToNextFrame / timeBetweenFrames;
//			cur.inTan *= (int) inAdj;
//			cur.outTan *= (int) outAdj;
//		}
	}


	public BitmapAnimFlag getAsTypedOrNull(AnimFlag<?> animFlag){
		if(animFlag instanceof BitmapAnimFlag){
			return (BitmapAnimFlag) animFlag;
		}
		return null;
	}
}
