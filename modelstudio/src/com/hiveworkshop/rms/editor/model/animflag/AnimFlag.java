package com.hiveworkshop.rms.editor.model.animflag;

import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.GlobalSeq;
import com.hiveworkshop.rms.editor.model.TimelineContainer;
import com.hiveworkshop.rms.parsers.mdlx.AnimationMap;
import com.hiveworkshop.rms.parsers.mdlx.InterpolationType;
import com.hiveworkshop.rms.parsers.mdlx.timeline.MdlxFloatArrayTimeline;
import com.hiveworkshop.rms.parsers.mdlx.timeline.MdlxFloatTimeline;
import com.hiveworkshop.rms.parsers.mdlx.timeline.MdlxTimeline;
import com.hiveworkshop.rms.parsers.mdlx.timeline.MdlxUInt32Timeline;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.util.Pair;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;

import java.util.*;

/**
 * A java class for MDL "motion flags," such as Alpha, Translation, Scaling, or
 * Rotation. AnimFlags are not "real" things from an MDL and are given this name
 * by me, as an invented java class to simplify the programming
 *
 * Eric Theller 11/5/2011
 */
public abstract class AnimFlag<T> {
	protected String name;
	protected InterpolationType interpolationType = InterpolationType.DONT_INTERP;
	protected GlobalSeq globalSeq;
	protected Map<Sequence, TreeMap<Integer, Entry<T>>> sequenceMap = new HashMap<>();
	protected Map<Sequence, Integer[]> timeKeysMap = new HashMap<>();

	public AnimFlag(String title) {
		name = title;
	}

	public AnimFlag(MdlxTimeline<?> timeline, EditableModel model) {
		name = AnimationMap.ID_TO_TAG.get(timeline.name).getMdlToken();

		interpolationType = timeline.interpolationType;

		if (this instanceof IntAnimFlag) {
			System.out.println("[AnimFlag] " + name + ", glob seq id: " + timeline.globalSequenceId + ", model glob seq: " + model.getGlobalSeq(timeline.globalSequenceId));
		}

		setGlobSeq(model.getGlobalSeq(timeline.globalSequenceId));
	}

	private long lastConsoleLogTime = 0;

	public static AnimFlag<?> createFromTimeline(MdlxTimeline<?> timeline, EditableModel model) {
		return switch (AnimationMap.valueOf(timeline.name.asStringValue()).getImplementation()) {
			case BITMAP_TIMELINE -> new BitmapAnimFlag((MdlxUInt32Timeline) timeline, model);
			case UINT32_TIMELINE -> new IntAnimFlag((MdlxUInt32Timeline) timeline, model);
			case FLOAT_TIMELINE -> new FloatAnimFlag((MdlxFloatTimeline) timeline, model);
			case VECTOR3_TIMELINE -> new Vec3AnimFlag((MdlxFloatArrayTimeline) timeline, model);
			case VECTOR4_TIMELINE -> new QuatAnimFlag((MdlxFloatArrayTimeline) timeline, model);
			default -> null;
		};
	}

	public abstract T cloneValue(Object value);

	protected AnimFlag(AnimFlag<T> af) {
		setSettingsFrom(af);
		for (Sequence anim : af.getAnimMap().keySet()) {
			TreeMap<Integer, Entry<T>> entryMap = af.getEntryMap(anim);
			TreeMap<Integer, Entry<T>> newEntryMap = new TreeMap<>();
			for (Integer time : entryMap.keySet()) {
				newEntryMap.put(time, entryMap.get(time).deepCopy());
//				addEntry(entryMap.get(time).deepCopy(), anim);
			}
			setEntryMap(anim, newEntryMap);
		}
//		for (Sequence anim : af.getAnimMap().keySet()) {
//			TreeMap<Integer, Entry<T>> entryMap = af.getAnimMap().get(anim);
//			for (Integer time : entryMap.keySet()) {
//				entryMap.put(time, af.getEntryMap(anim).get(time).deepCopy());
//			}
//		}
	}

	public abstract AnimFlag<T> getEmptyCopy();

	public abstract AnimFlag<T> deepCopy();

	protected void setSettingsFrom(AnimFlag<?> af) {
		name = af.name;
		globalSeq = af.globalSeq;
		interpolationType = af.interpolationType;
	}

	public boolean equals(Object o) {
		if (this == o) return true;
		if (o instanceof AnimFlag && getClass() == o.getClass()){
			AnimFlag<T> animFlag = getAsTypedOrNull((AnimFlag<?>) o);

			return name.equals(animFlag.getName())
					&& sequenceMap.equals(animFlag.sequenceMap)
					&& (Objects.equals(globalSeq, animFlag.globalSeq)
					&& interpolationType == animFlag.interpolationType);
		}
		return false;
	}

	public AnimFlag<T> setFromOther(AnimFlag<T> other) {
		sequenceMap = other.getAnimMap(); // ToDo copy entries!
		timeKeysMap.clear();
		return this;
	}

	public boolean hasSequence(Sequence sequence) {
		return sequenceMap.containsKey(sequence);
	}

	public GlobalSeq getGlobalSeq() {
		return globalSeq;
	}

	public void setGlobalSeq(GlobalSeq globalSeq) {
		this.globalSeq = globalSeq;
	}

	public void setGlobSeq(GlobalSeq globalSeq) {
		this.globalSeq = globalSeq;
	}

	public int getGlobalSeqId(EditableModel model) {
		return model.getGlobalSeqId(globalSeq);
	}


	public boolean hasGlobalSeq() {
		return this.globalSeq != null;
	}

	public InterpolationType getInterpolationType() {
		return interpolationType;
	}

	public int size() {
		int size = 0;
		for (Sequence animation : sequenceMap.keySet()) {
			if (sequenceMap.get(animation) != null) {
				size += sequenceMap.get(animation).size();
			}
		}
		return size;
	}

	public int size(Sequence anim) {
		TreeMap<Integer, Entry<T>> entryTreeMap = sequenceMap.get(anim);
		if (entryTreeMap == null || entryTreeMap.isEmpty()) {
			return 0;
		}
		return entryTreeMap.size();
	}

	public abstract MdlxTimeline<?> toMdlx(TimelineContainer container, EditableModel model);

	public <Q> MdlxTimeline<Q> toMdlx3(MdlxTimeline<Q> mdlxTimeline, TimelineContainer container, EditableModel model) {
		mdlxTimeline.name = FlagUtils.getWar3ID(name, container);
		mdlxTimeline.interpolationType = interpolationType;
		mdlxTimeline.globalSequenceId = getGlobalSeqId(model);

		Pair<ArrayList<Integer>, ArrayList<Entry<T>>> entrySavingPair = getEntrySavingPair(model);
		ArrayList<Integer> tempFrames = entrySavingPair.getFirst();
		ArrayList<Entry<T>> tempEntries = entrySavingPair.getSecond();

		int size = tempFrames.size();

		mdlxTimeline.initLists(size);

		for (int i = 0; i < size; i++) {
			Entry<T> entry = tempEntries.get(i);
			if(entry.getValue() != null){
				Q[] array = getArray(entry, mdlxTimeline, model);
//			if(i == 0){
//				System.out.println("(Q): " + (Q) entry.getValueArr() + ", org: " + entry.getValueArr());
//				System.out.println("(Q): " + ", org: " + Arrays.toString(entry.getValueArr()));
//			}
//			mdlxTimeline.add(i, tempFrames.get(i), (Q)tempEntries.get(i).getValueArr(), (Q)tempEntries.get(i).getInTanArr(), (Q)tempEntries.get(i).getOutTanArr());
//			mdlxTimeline.add(i, tempFrames.get(i), (Q) entry.getValueArr(), (Q) entry.getInTanArr(), (Q) entry.getOutTanArr());
				mdlxTimeline.add(i, tempFrames.get(i), array[0], array[1], array[2]);
			}
		}


		return mdlxTimeline;
	}

	private <Q, W> Q[] getArray(Entry<W> entry, MdlxTimeline<Q> mdlxTimeline, EditableModel model){
		if(entry.getValue() instanceof Bitmap && mdlxTimeline instanceof MdlxUInt32Timeline) {
			return (Q[]) getArray((Entry<Bitmap>) entry, (MdlxUInt32Timeline) mdlxTimeline, (Bitmap) entry.getValue(), model);
//			return new Q[][]{new int[]{(int) entry.getValue()}, new int[]{(int) entry.getInTan()}, new int[]{(int) entry.getOutTan()}};
		} else if(entry.getValue() instanceof Integer && mdlxTimeline instanceof MdlxUInt32Timeline){
			return (Q[]) getArray((Entry<Integer>)entry, (MdlxUInt32Timeline) mdlxTimeline, (int)entry.getValue());
//			return new Q[][]{new int[]{(int) entry.getValue()}, new int[]{(int) entry.getInTan()}, new int[]{(int) entry.getOutTan()}};
		} else if(entry.getValue() instanceof Float) {
			return (Q[]) getArray((Entry<Float>)entry, (MdlxFloatTimeline)mdlxTimeline, (Float)entry.getValue());
		} else if(entry.getValue() instanceof Vec3) {
			return (Q[]) getArray((Entry<Vec3>)entry, (MdlxFloatArrayTimeline)mdlxTimeline, (Vec3)entry.getValue());
		} else if(entry.getValue() instanceof Quat) {
			return (Q[]) getArray((Entry<Quat>)entry, (MdlxFloatArrayTimeline)mdlxTimeline, (Quat)entry.getValue());
//		} else if(entry.getValue() instanceof Vec3) {
		} else {
//			return (Q[]) getArray(entry, (MdlxFloatArrayTimeline)mdlxTimeline, entry.getValue());
			return null;
		}
//		return getArray(entry, mdlxTimeline);
	}

	private long[][] getArray(Entry<Bitmap> entry, MdlxUInt32Timeline line, Bitmap i, EditableModel model){
//		return new int[][]{new int[]{entry.getValue()}, new int[]{entry.getInTan()}, new int[]{entry.getOutTan()}};
		return new long[][]{new long[]{model.getTextureId(entry.getValue())}, new long[]{0}, new long[]{0}};
	}
	private long[][] getArray(Entry<Integer> entry, MdlxUInt32Timeline line, int i){
//		return new int[][]{new int[]{entry.getValue()}, new int[]{entry.getInTan()}, new int[]{entry.getOutTan()}};
		return new long[][]{new long[]{entry.getValue()}, new long[]{0}, new long[]{0}};
	}
	private float[][] getArray(Entry<java.lang.Float> entry, MdlxFloatTimeline line, float i){
//		return new float[][]{entry.getValue().toFloatArray(), entry.getInTan().toFloatArray(), entry.getOutTan().toFloatArray()};
		return new float[][]{entry.getValueArr(), entry.getInTanArr(), entry.getOutTanArr()};
	}
	private float[][] getArray(Entry<Vec3> entry, MdlxFloatArrayTimeline line, Vec3 i){
//		return new float[][]{entry.getValue().toFloatArray(), entry.getInTan().toFloatArray(), entry.getOutTan().toFloatArray()};
		return new float[][]{entry.getValueArr(), entry.getInTanArr(), entry.getOutTanArr()};
	}
	private float[][] getArray(Entry<Quat> entry, MdlxFloatArrayTimeline line, Quat i){
//		return new float[][]{entry.getValue().toFloatArray(), entry.getInTan().toFloatArray(), entry.getOutTan().toFloatArray()};
		return new float[][]{entry.getValueArr(), entry.getInTanArr(), entry.getOutTanArr()};
	}

	private Pair<ArrayList<Integer>, ArrayList<Entry<T>>> getEntrySavingPair(EditableModel model) {
		ArrayList<Integer> tempTimes = new ArrayList<>();
		ArrayList<Entry<T>> tempEntries = new ArrayList<>();
		for (Sequence anim : model.getAllSequences()) {
			if (globalSeq == null || anim == globalSeq) {
				TreeMap<Integer, Entry<T>> entryTreeMap = sequenceMap.get(anim);
				if(entryTreeMap != null){
					for (Integer time : entryTreeMap.keySet()) {
						if (time > anim.getLength()) {
							break;
						}
						tempTimes.add(time + anim.getStart());
						Entry<T> entry = entryTreeMap.get(time);
						tempEntries.add(entry);
					}
				}
			}
		}
		return new Pair<>(tempTimes, tempEntries);
	}

//	public MdlxUInt32Timeline toMdlx(final TimelineContainer container, EditableModel model) {
//		final MdlxUInt32Timeline mdlxTimeline = new MdlxUInt32Timeline();
//
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
//	}


	public void addEntry(Integer time, T value, Sequence animation) {
		Entry<T> entry = new Entry<>(time, value);
		if (tans()) {
			entry.unLinearize();
		}
		sequenceMap.computeIfAbsent(animation, k -> new TreeMap<>()).put(entry.getTime(), entry);
		if (animation instanceof GlobalSeq) {
			this.globalSeq = (GlobalSeq) animation;
		}
	}

	public void addEntry(Entry<T> entry, Sequence animation) {
		if (tans()) {
			entry.unLinearize();
		}
		sequenceMap.computeIfAbsent(animation, k -> new TreeMap<>()).put(entry.getTime(), entry);
		if (animation instanceof GlobalSeq) {
			this.globalSeq = (GlobalSeq) animation;
		}
	}

	protected void addEntry(Integer time, T value, T inTan, T outTan, Sequence animation) {
		sequenceMap.computeIfAbsent(animation, k -> new TreeMap<>()).put(time, new Entry<>(time, value, inTan, outTan));
		if (animation instanceof GlobalSeq) {
			this.globalSeq = (GlobalSeq) animation;
		}
	}

	public void setEntryMap(Sequence animation, TreeMap<Integer, Entry<T>> entryMap) {
		sequenceMap.put(animation, entryMap);
		if (animation instanceof GlobalSeq) {
			this.globalSeq = (GlobalSeq) animation;
		}
		timeKeysMap.remove(animation);
	}

	public void addEntryMap(Sequence animation, Map<Integer, Entry<T>> entryMap) {
		TreeMap<Integer, Entry<T>> entryTreeMap = sequenceMap.computeIfAbsent(animation, k -> new TreeMap<>());
		entryTreeMap.putAll(entryMap);

		if (animation instanceof GlobalSeq) {
			this.globalSeq = (GlobalSeq) animation;
		}
	}


	public TreeMap<Integer, Entry<T>> getSequenceEntryMapCopy(Sequence sequence) {
		if (hasSequence(sequence) && sequenceMap.get(sequence) != null) {
			TreeMap<Integer, Entry<T>> entryMap = sequenceMap.get(sequence);
			TreeMap<Integer, Entry<T>> entryMapCopy = new TreeMap<>();
			for (Integer time : entryMap.keySet()) {
				entryMapCopy.put(time, entryMap.get(time).deepCopy());
			}
			return entryMapCopy;
		}
		return null;
	}

	/**
	 * To set an Entry with an Entry
	 *
	 * @param time  the time of the entry to be changed
	 * @param entry the entry to replace the old entry
	 */
	public void setOrAddEntryT(Integer time, Entry<?> entry, Sequence animation) {
		if (entry.getValue() instanceof Integer && this instanceof IntAnimFlag
				|| entry.getValue() instanceof Bitmap && this instanceof BitmapAnimFlag
				|| entry.getValue() instanceof Float && this instanceof FloatAnimFlag
				|| entry.getValue() instanceof Vec3 && this instanceof Vec3AnimFlag
				|| entry.getValue() instanceof Quat && this instanceof QuatAnimFlag) {
			Entry<T> tEntry = (Entry<T>) entry.setTime(time);

			sequenceMap.computeIfAbsent(animation, k -> new TreeMap<>()).put(entry.getTime(), tEntry);

			if (animation instanceof GlobalSeq) {
				this.globalSeq = (GlobalSeq) animation;
			}
		}
	}

	public void setOrAddEntry(Integer time, Entry<T> entry, Sequence animation) {
		if (entry.getValue() instanceof Integer && this instanceof IntAnimFlag
				|| entry.getValue() instanceof Bitmap && this instanceof BitmapAnimFlag
				|| entry.getValue() instanceof Float && this instanceof FloatAnimFlag
				|| entry.getValue() instanceof Vec3 && this instanceof Vec3AnimFlag
				|| entry.getValue() instanceof Quat && this instanceof QuatAnimFlag) {
			Entry<T> tEntry = entry.setTime(time);

			sequenceMap.computeIfAbsent(animation, k -> new TreeMap<>())
					.put(entry.getTime(), tEntry);

			if (animation instanceof GlobalSeq) {
				this.globalSeq = (GlobalSeq) animation;
			}
		}
	}


	public void changeEntryAt(Integer time, Entry<T> entry, Sequence animation) {
		TreeMap<Integer, Entry<T>> entryMap = sequenceMap.get(animation);
		if (entryMap != null) {
			entryMap.remove(time);
			entryMap.put(entry.getTime(), entry);
			if (!time.equals(entry.getTime())) {
				timeKeysMap.remove(animation);
			}
			if (tans() && !entry.isTangential()) {
				entry.unLinearize();
			} else if (!tans() && entry.isTangential()) {
				entry.linearize();
			}
		}
	}


	List<T> deepCopy(List<T> source) {

		List<T> copy = new ArrayList<>();
		for (T item : source) {
			T toAdd = item;
			if (item instanceof Vec3) {
				Vec3 v = (Vec3) item;
				toAdd = (T) v;
			} else if (item instanceof Quat) {
				Quat r = (Quat) item;
				toAdd = (T) r;
			}
			copy.add(toAdd);
		}
		return copy;
	}

	public String getName() {
		return name;
	}

	public void setName(String title) {
		name = title;
	}

	public boolean tans() {
		return interpolationType.tangential();
	}

	public void linearize() {
		if (interpolationType.tangential()) {
			interpolationType = InterpolationType.LINEAR;
			for (Sequence anim : sequenceMap.keySet()) {
				TreeMap<Integer, Entry<T>> entryMap = sequenceMap.get(anim);
				for (Entry<T> entry : entryMap.values()) {
					entry.linearize();
				}
			}
		}
	}

	public void unLinearize() {
		if (!interpolationType.tangential()) {
			interpolationType = InterpolationType.BEZIER;
			for (Sequence anim : sequenceMap.keySet()) {
				TreeMap<Integer, Entry<T>> entryMap = sequenceMap.get(anim);
				for (Entry<T> entry : entryMap.values()) {
					entry.unLinearize();
				}
			}
		}
	}

	public void unLinearize2() {
		for (Sequence animation : sequenceMap.keySet()) {
			TreeMap<Integer, Entry<T>> entryTreeMap = sequenceMap.get(animation);
			int animationLength = animation.getEnd() - animation.getStart();
			AnimFlagUtils.unLiniarizeMapEntries(this, animationLength, entryTreeMap);
		}
	}

	public void setInterpolationType(InterpolationType interpolationType) {
		this.interpolationType = interpolationType;
		if (interpolationType.tangential()) {
			unLinearize();
		} else {
			linearize();
		}
	}

	public void setInterpType(InterpolationType interpolationType) {
		if (interpolationType.tangential()) {
			unLinearize2();
		} else {
			linearize();
		}
		this.interpolationType = interpolationType;
	}

	public void deleteAnim(Sequence anim) {
		sequenceMap.remove(anim);
		timeKeysMap.remove(anim);
		if (anim == globalSeq) {
			globalSeq = null;
		}
	}

	public void removeKeyframe(int trackTime, Sequence anim) {
		TreeMap<Integer, Entry<T>> entryMap = sequenceMap.get(anim);
		if (entryMap != null) {
			entryMap.remove(trackTime);
			timeKeysMap.remove(anim);
		}
	}


	public void clear() {
		sequenceMap.clear();
	}

	public Entry<T> getEntryAt(Sequence anim, int time) {
		if (sequenceMap.get(anim) != null) {
			return sequenceMap.get(anim).get(time);
		}
		return null;
	}

	public boolean hasEntryAt(Sequence anim, int time) {
		return sequenceMap.get(anim) != null && sequenceMap.get(anim).containsKey(time);
	}

	public T valueAt(Sequence anim, Integer time) {
		if (sequenceMap.get(anim) != null && sequenceMap.get(anim).containsKey(time)) {
			return sequenceMap.get(anim).get(time).getValue();
		}
		return null;
	}

	public T inTanAt(Sequence anim, Integer time) {
		if (sequenceMap.get(anim) != null && sequenceMap.get(anim).containsKey(time)) {
			return sequenceMap.get(anim).get(time).getInTan();
		}
		return null;
	}

	public T outTanAt(Sequence anim, Integer time) {
		if (sequenceMap.get(anim) != null && sequenceMap.get(anim).containsKey(time)) {
			return sequenceMap.get(anim).get(time).getOutTan();
		}
		return null;
	}

	public int getTimeFromIndex(Sequence anim, int index) {
		if (sequenceMap.get(anim) != null && 0 <= index && index < sequenceMap.get(anim).size()) {
			return getTimeKeys(anim)[index];
		}
		return -1;
	}

	public int getIndexOfTime(Sequence anim, int time) {
		TreeMap<Integer, Entry<T>> entryMap = sequenceMap.get(anim);
		if (entryMap != null) {
			return entryMap.navigableKeySet().subSet(entryMap.firstKey(), entryMap.floorKey(time)).size();
		}
		return -1;
	}

	public T getValueFromIndex(Sequence anim, int index) {
		if (anim != null) {
			TreeMap<Integer, Entry<T>> entryMap = sequenceMap.get(anim);
			if (entryMap != null && 0 <= index && index <= entryMap.size()) {
				Entry<T> entry = entryMap.get(getTimeFromIndex(anim, index));
				if (entry != null) {
					return entry.getValue();
				}
			}
			timeKeysMap.remove(anim);
		}
		return null;
	}

	public T getInTanFromIndex(Sequence anim, int index) {
		TreeMap<Integer, Entry<T>> entryMap = sequenceMap.get(anim);
		if (entryMap != null && 0 <= index && index <= entryMap.size()) {
			Entry<T> entry = entryMap.get(getTimeFromIndex(anim, index));
			if (entry != null) {
				return entry.getInTan();
			}
		}
		timeKeysMap.remove(anim);
		return null;
	}

	public T getOutTanFromIndex(Sequence anim, int index) {
		TreeMap<Integer, Entry<T>> entryMap = sequenceMap.get(anim);
		if (entryMap != null && 0 <= index && index <= entryMap.size()) {
			Entry<T> entry = entryMap.get(getTimeFromIndex(anim, index));
			if (entry != null) {
				return entry.getOutTan();
			}
		}
		timeKeysMap.remove(anim);
		return null;
	}

	/**
	 * Interpolates at a given time.
	 */
	public T interpolateAt(final TimeEnvironmentImpl animatedRenderEnvironment) {
		if (sequenceMap.isEmpty() || (animatedRenderEnvironment == null) || animatedRenderEnvironment.getCurrentSequence() == null) {
//			System.out.println("[AnimFlag] Case 1, seqMapEmpty: " + sequenceMap.isEmpty());
			return getIdentity();
		}

		Sequence currentSequence = hasGlobalSeq() ? globalSeq : animatedRenderEnvironment.getCurrentSequence();
		int time = animatedRenderEnvironment.getTrackTime(globalSeq);
		return interpolateAt(currentSequence, time);
	}

	public T interpolateAt(Sequence currentSequence, int time) {
		TreeMap<Integer, Entry<T>> entryMap = sequenceMap.get(currentSequence);
		if (entryMap == null || entryMap.isEmpty()) {
			if (this instanceof IntAnimFlag) {
				System.out.println("[AnimFlag] Case 2: no entryMap or entryMap empty");
			}
			return getIdentity();
		}

		int sequenceLength = currentSequence.getLength();

//		// no keyframes at nor after time
//		if (hasGlobalSeq() && globalSeq.getLength() >= 0 && entryMap.ceilingKey(time) == null) {
//			System.out.println("[AnimFlag] Case 3, " + globalSeq + " " + time + ", " + entryMap.ceilingKey(time));
//			return getIdentity(typeid);
//		}

		Integer lastKeyframeTime = entryMap.floorKey(sequenceLength);
		Integer firstKeyframeTime = entryMap.ceilingKey(0);

		// either:
		// - no keyframes before animationEnd
		// - no keyframes after animationStart
		// - no keyframes in animation
		// - time is outside of animation
		if (lastKeyframeTime == null
				|| firstKeyframeTime == null
				|| lastKeyframeTime < firstKeyframeTime
				|| sequenceLength < time
				|| time < 0) {
//			System.out.println("[AnimFlag] Case 4");
			return getIdentity();
		}
		// only one keyframe in the animation
		if (lastKeyframeTime.equals(firstKeyframeTime)) {
			if (this instanceof IntAnimFlag) {
//				System.out.println("[AnimFlag] Case 2: no entryMap or entryMap empty");
//			    System.out.println("[AnimFlag] OneValue");
				System.out.println("lastKeyframeTime: " + lastKeyframeTime + ", firstKeyframeTime: " + firstKeyframeTime + ", from time: " + time + " and seqLength: " + sequenceLength);
			}
			return entryMap.get(lastKeyframeTime).getValue();
		}

		Integer floorTime = entryMap.floorKey(time);
		if (floorTime == null || floorTime < 0) {
//			System.out.println("[AnimFlag] floorTime: " + floorTime + ", lKFt: " + lastKeyframeTime + ", from time: " + time);
			floorTime = lastKeyframeTime;
		}

		Integer ceilTime = entryMap.ceilingKey(time);
		if (ceilTime == null || ceilTime > sequenceLength) {
//			System.out.println("[AnimFlag] ceilTime: " + ceilTime + ", fKFt: " + firstKeyframeTime + ", from time: " + time);
			ceilTime = firstKeyframeTime;
		}

		if (floorTime.equals(ceilTime)) {
//			System.out.println("[AnimFlag] on KF");
			return entryMap.get(floorTime).getValue();
		}

		float timeFactor = getTimeFactor(time, sequenceLength, floorTime, ceilTime);

//		System.out.println("[AnimFlag] interpolating!");
		return getInterpolatedValue(floorTime, ceilTime, timeFactor, currentSequence);
	}

	protected float getTimeFactor(int time, int animationLength, Integer floorTime, Integer ceilTime) {
		int timeBetweenFrames = ceilTime - floorTime;

		// if ceilTime wrapped, add animation length
		if (timeBetweenFrames <= 0) {
			timeBetweenFrames = timeBetweenFrames + animationLength;
		}

		int timeFromKF = time - floorTime;
		// if floorTime wrapped, add animation length
		if (timeFromKF < 0) {
			timeFromKF = timeFromKF + animationLength;
		}

		return timeFromKF / (float) timeBetweenFrames;
	}

	public Entry<T> getFloorEntry(int time, Sequence anim) {
		TreeMap<Integer, Entry<T>> entryMap = sequenceMap.get(anim);
		if (entryMap != null) {
			Integer floorTime = entryMap.floorKey(time);
			if (floorTime == null || floorTime < anim.getStart()) {
				Integer key = entryMap.floorKey(anim.getEnd());
				if (key == null) {
					return null;
				}
				return entryMap.get(key);
			}
			return entryMap.get(floorTime);
		}
		return null;
	}

	public Entry<T> getCeilEntry(int time, Sequence anim) {
		TreeMap<Integer, Entry<T>> entryMap = sequenceMap.get(anim);
		if (entryMap != null) {
			Integer ceilTime = entryMap.ceilingKey(time);
			if (ceilTime == null || ceilTime > anim.getEnd()) {
				Integer key = entryMap.ceilingKey(anim.getStart());
				return key == null ? null : entryMap.get(key);
			}
			return entryMap.get(ceilTime);
		}
		return null;
	}

	public abstract T getInterpolatedValue(Integer floorTime, Integer ceilTime, float timeFactor, Sequence anim);

	public abstract T getInterpolatedValue(Entry<T> entryFloor, Entry<T> entryCeil, float timeFactor);

	protected abstract T getIdentity();

	public void slideKeyframe(int startTrackTime, int endTrackTime, Sequence anim) {
		TreeMap<Integer, Entry<T>> entryMap = sequenceMap.get(anim);
		if (entryMap == null || entryMap.isEmpty()) {
			throw new IllegalStateException("Unable to slide keyframe: no frames exist");
		}
		Entry<T> entryToSlide = getEntryAt(anim, startTrackTime);
		if (entryToSlide != null) {
			entryMap.put(endTrackTime, entryMap.remove(startTrackTime).setTime(endTrackTime));
			timeKeysMap.remove(anim);
		}
	}

	public TreeMap<Integer, Entry<T>> getEntryMap(Sequence anim) {
		return sequenceMap.get(anim);
	}

	public Map<Sequence, TreeMap<Integer, Entry<T>>> getAnimMap() {
		return sequenceMap;
	}

	public AnimFlag<T> setSequenceMap(Map<Sequence, TreeMap<Integer, Entry<T>>> otherMap) {
		sequenceMap = otherMap; // ToDo copy entries!
		return this;
	}

	private Integer[] getTimeKeys(Sequence anim) {
		TreeMap<Integer, Entry<T>> entryMap = sequenceMap.get(anim);
		if (entryMap != null) {
			if (timeKeysMap.get(anim) == null || timeKeysMap.get(anim).length != entryMap.size()) {
				timeKeysMap.put(anim, entryMap.keySet().toArray(new Integer[0]));
			}
		}
		return timeKeysMap.get(anim);
	}

//	public abstract void calcNewTans(float factor, T curValue, T nextValue, T prevValue, Entry<T> entry);

	public abstract void calcNewTans(float[] factor, Entry<T> next, Entry<T> prev, Entry<T> cur, int animationLength);

	public abstract float[] getTbcFactor(float bias, float tension, float continuity);

	public float[] getTCB(int i, float bias, float tension, float continuity) {
		float[] factor = new float[4];

		float contP = i * continuity;
		float biasP = i * bias;

		float contN = -contP;
		float biasN = -biasP;

		factor[0] = (1 - tension) * (1 + contN) * (1 + biasP) * 0.5f;
		factor[1] = (1 - tension) * (1 + contP) * (1 + biasN) * 0.5f;
		factor[2] = (1 - tension) * (1 + contP) * (1 + biasP) * 0.5f;
		factor[3] = (1 - tension) * (1 + contN) * (1 + biasN) * 0.5f;
		return factor;
	}

	public abstract AnimFlag<T> getAsTypedOrNull(AnimFlag<?> animFlag);
}
