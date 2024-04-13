package com.hiveworkshop.rms.editor.model.animflag;

import com.hiveworkshop.rms.editor.model.*;
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
	protected InterpolationType interpolationType = InterpolationType.LINEAR;
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

	protected TreeMap<Integer, Animation> getAnimationTreeMap(ArrayList<Animation> modelAnims) {
		TreeMap<Integer, Animation> animationTreeMap = new TreeMap<>();
		for (Animation animation : modelAnims) {
			if (animation instanceof FakeAnimation) {
				animationTreeMap.putIfAbsent(animation.getStart(), animation);
			} else {
				animationTreeMap.put(animation.getStart(), animation);
			}
		}
		return animationTreeMap;
	}

	public abstract T cloneValue(Object value);

	protected AnimFlag(AnimFlag<T> af) {
		setSettingsFrom(af);
		for (Sequence sequence : af.getAnimMap().keySet()) {
			setEntryMap(sequence, af.getSequenceEntryMapCopy(sequence));
		}
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
		if (o instanceof AnimFlag && getClass() == o.getClass()) {
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
		if (sequence instanceof FakeAnimation) {
			return sequenceMap.containsKey(((FakeAnimation) sequence).getRealAnim());
		}
		return sequenceMap.containsKey(sequence);
	}

	public String getName() {
		return name;
	}

	public void setName(String title) {
		name = title;
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
		for (Sequence sequence : sequenceMap.keySet()) {
			if (sequenceMap.get(sequence) != null) {
				size += sequenceMap.get(sequence).size();
			}
		}
		return size;
	}

	public int size(Sequence sequence) {
		TreeMap<Integer, Entry<T>> entryTreeMap = getEntryTreeMap(sequence);
		if (entryTreeMap == null || entryTreeMap.isEmpty()) {
			return 0;
		}
		return entryTreeMap.size();
	}

	private Sequence getRealSequence(Sequence sequence) {
		if (sequence instanceof FakeAnimation) {
			sequence = ((FakeAnimation) sequence).getRealAnim();
		}
		return sequence;
	}

	private TreeMap<Integer, Entry<T>> getEntryTreeMap(Sequence sequence) {
		sequence = getRealSequence(sequence);
		return sequenceMap.get(sequence);
	}

	private TreeMap<Integer, Entry<T>> getOrComputeEntryMap(Sequence sequence) {
		Sequence sequence1 = getRealSequence(sequence);
		return sequenceMap.computeIfAbsent(sequence1, k -> new TreeMap<>());
	}

	public abstract MdlxTimeline<?> toMdlx(TimelineContainer container, EditableModel model);

	public <Q> MdlxTimeline<Q> toMdlx3(MdlxTimeline<Q> mdlxTimeline, TimelineContainer container, EditableModel model) {
		mdlxTimeline.name = FlagUtils.getWar3ID(name, container);
		mdlxTimeline.interpolationType = interpolationType;
		mdlxTimeline.globalSequenceId = model.getGlobalSeqId(globalSeq);

		Pair<ArrayList<Integer>, ArrayList<Entry<T>>> entrySavingPair = getEntrySavingPair(model);
		ArrayList<Integer> tempFrames = entrySavingPair.getFirst();
		ArrayList<Entry<T>> tempEntries = entrySavingPair.getSecond();

		int size = tempFrames.size();

		mdlxTimeline.initLists(size);

		for (int i = 0; i < size; i++) {
			Entry<T> entry = tempEntries.get(i);
			if (entry.getValue() != null) {
				Q[] array = getArray(entry, mdlxTimeline, model);
//			if (i == 0) {
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

	private <Q, W> Q[] getArray(Entry<W> entry, MdlxTimeline<Q> mdlxTimeline, EditableModel model) {
		if (entry.getValue() instanceof Bitmap && mdlxTimeline instanceof MdlxUInt32Timeline) {
			return (Q[]) getArray((Entry<Bitmap>) entry, (MdlxUInt32Timeline) mdlxTimeline, (Bitmap) entry.getValue(), model);
		} else if (entry.getValue() instanceof Integer && mdlxTimeline instanceof MdlxUInt32Timeline) {
			return (Q[]) getArray((Entry<Integer>)entry, (MdlxUInt32Timeline) mdlxTimeline, (int)entry.getValue());
		} else if (entry.getValue() instanceof Float) {
			return (Q[]) getArray((Entry<Float>)entry, (MdlxFloatTimeline)mdlxTimeline, (Float)entry.getValue());
		} else if (entry.getValue() instanceof Vec3) {
			return (Q[]) getArray((Entry<Vec3>)entry, (MdlxFloatArrayTimeline)mdlxTimeline, (Vec3)entry.getValue());
		} else if (entry.getValue() instanceof Quat) {
			return (Q[]) getArray((Entry<Quat>)entry, (MdlxFloatArrayTimeline)mdlxTimeline, (Quat)entry.getValue());
		} else {
			return null;
		}
	}

	private long[][] getArray(Entry<Bitmap> entry, MdlxUInt32Timeline line, Bitmap i, EditableModel model) {
		return new long[][]{new long[]{model.getTextureId(entry.getValue())}, new long[]{0}, new long[]{0}};
	}
	private long[][] getArray(Entry<Integer> entry, MdlxUInt32Timeline line, int i) {
		return new long[][]{new long[]{entry.getValue()}, new long[]{0}, new long[]{0}};
	}
	private float[][] getArray(Entry<java.lang.Float> entry, MdlxFloatTimeline line, float i) {
		return new float[][]{entry.getValueArr(), entry.getInTanArr(), entry.getOutTanArr()};
	}
	private float[][] getArray(Entry<Vec3> entry, MdlxFloatArrayTimeline line, Vec3 i) {
		return new float[][]{entry.getValueArr(), entry.getInTanArr(), entry.getOutTanArr()};
	}
	private float[][] getArray(Entry<Quat> entry, MdlxFloatArrayTimeline line, Quat i) {
		return new float[][]{entry.getValueArr(), entry.getInTanArr(), entry.getOutTanArr()};
	}

	private Pair<ArrayList<Integer>, ArrayList<Entry<T>>> getEntrySavingPair(EditableModel model) {
		ArrayList<Integer> tempTimes = new ArrayList<>();
		ArrayList<Entry<T>> tempEntries = new ArrayList<>();
		for (Sequence sequence : model.getAllSequences()) {
			if ((globalSeq == null || sequence == globalSeq) && !(sequence instanceof FakeAnimation)) {
				TreeMap<Integer, Entry<T>> entryTreeMap = sequenceMap.get(sequence);
				if (entryTreeMap != null) {
					for (Integer time : entryTreeMap.keySet()) {
						if (time > sequence.getLength()) {
							break;
						}
						tempTimes.add(time + sequence.getStart());
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
//				if (entryTreeMap != null) {
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

	public void setEntryMap(Sequence sequence, TreeMap<Integer, Entry<T>> entryMap) {
		Sequence sequence1 = getRealSequence(sequence);
		sequenceMap.put(sequence1, entryMap);
		if (sequence instanceof GlobalSeq) {
			this.globalSeq = (GlobalSeq) sequence;
		}
		timeKeysMap.remove(sequence);
	}

	public void addEntryMap(Sequence sequence, Map<Integer, Entry<T>> entryMap) {
		TreeMap<Integer, Entry<T>> entryTreeMap = getOrComputeEntryMap(sequence);
		entryTreeMap.putAll(entryMap);

		if (sequence instanceof GlobalSeq) {
			this.globalSeq = (GlobalSeq) sequence;
		}
	}

	public TreeMap<Integer, Entry<T>> getEntryMap(Sequence sequence) {
		return getEntryTreeMap(sequence);
	}

	public Map<Sequence, TreeMap<Integer, Entry<T>>> getAnimMap() {
		return sequenceMap;
	}

	public AnimFlag<T> setSequenceMap(Map<Sequence, TreeMap<Integer, Entry<T>>> otherMap) {
		sequenceMap.keySet().removeIf(sequence -> !otherMap.containsKey(sequence));
		for (Sequence sequence : otherMap.keySet()) {
			TreeMap<Integer, Entry<T>> entryTreeMap = sequenceMap.computeIfAbsent(sequence, k -> new TreeMap<>());
			entryTreeMap.clear();
			for (Entry<T> entry : otherMap.get(sequence).values()) {
				entryTreeMap.put(entry.getTime(), entry.deepCopy());
			}
		}
//		sequenceMap = otherMap; // ToDo copy entries!
		timeKeysMap.clear();
		return this;
	}

	public TreeMap<Integer, Entry<T>> getSequenceEntryMapCopy(Sequence sequence) {
		TreeMap<Integer, Entry<T>> entryMap = getEntryTreeMap(sequence);
		if (entryMap != null) {
			TreeMap<Integer, Entry<T>> entryMapCopy = new TreeMap<>();
			for (Integer time : entryMap.keySet()) {
				entryMapCopy.put(time, entryMap.get(time).deepCopy());
			}
			return entryMapCopy;
		}
		return null;
	}

	public void addEntry(Integer time, T value, Sequence sequence) {
		addEntry(new Entry<>(time, value), sequence);
	}

	public void addEntry(Entry<T> entry, Sequence sequence) {
		if (tans() && !entry.isTangential()) {
			entry.unLinearize();
		} else if (!tans() && entry.isTangential()) {
			entry.linearize();
		}
		addEntry(sequence, entry);
	}

	protected void addEntry(Integer time, T value, T inTan, T outTan, Sequence sequence) {
		addEntry(sequence, new Entry<>(time, value, inTan, outTan));
	}

	private void addEntry(Sequence sequence, Entry<T> entry) {
		TreeMap<Integer, Entry<T>> entryMap = getOrComputeEntryMap(sequence);
		entryMap.put(entry.getTime(), entry);
		if (sequence instanceof GlobalSeq) {
			this.globalSeq = (GlobalSeq) sequence;
		}
	}

	public void addEntry(Integer time, Entry<T> entry, Sequence sequence) {
		if (entry.getValue() != null) {
			addEntry(sequence, entry.setTime(time));
		}
	}

	/**
	 * To set an Entry with an Entry
	 *
	 * @param time  the time of the entry to be changed
	 * @param entry the entry to replace the old entry
	 */
	public void setOrAddEntryT(Integer time, Entry<?> entry, Sequence sequence) {
		if (entry.getValue() instanceof Integer && this instanceof IntAnimFlag
				|| entry.getValue() instanceof Bitmap && this instanceof BitmapAnimFlag
				|| entry.getValue() instanceof Float && this instanceof FloatAnimFlag
				|| entry.getValue() instanceof Vec3 && this instanceof Vec3AnimFlag
				|| entry.getValue() instanceof Quat && this instanceof QuatAnimFlag) {
			addEntry(time, (Entry<T>) entry, sequence);
		}
	}

	public void clear() {
		sequenceMap.clear();
	}

	public TreeMap<Integer, Entry<T>> deleteAnim(Sequence sequence) {
		timeKeysMap.remove(sequence);
		if (sequence == globalSeq) {
			globalSeq = null;
		}
		return sequenceMap.remove(sequence);
	}

	public Entry<T> removeKeyframe(int trackTime, Sequence sequence) {
		TreeMap<Integer, Entry<T>> entryMap = sequenceMap.get(sequence);
		if (entryMap != null) {
			timeKeysMap.remove(sequence);
			return entryMap.remove(trackTime);
		}
		return null;
	}

	public Entry<T> getEntryAt(Sequence sequence, int time) {
		TreeMap<Integer, Entry<T>> entryMap = getEntryTreeMap(sequence);
		if (entryMap != null) {
			return entryMap.get(time);
		}
		return null;
	}
	public boolean hasEntryAt(Sequence sequence, int time) {
		TreeMap<Integer, Entry<T>> entryMap = getEntryTreeMap(sequence);
		return entryMap != null && entryMap.containsKey(time);
	}

	public boolean tans() {
		return interpolationType.tangential();
	}

	public void linearize() {
		if (interpolationType.tangential()) {
			interpolationType = InterpolationType.LINEAR;
			for (Sequence sequence : sequenceMap.keySet()) {
				TreeMap<Integer, Entry<T>> entryMap = sequenceMap.get(sequence);
				for (Entry<T> entry : entryMap.values()) {
					entry.linearize();
				}
			}
		}
	}

	public void unLinearize() {
		if (!interpolationType.tangential()) {
			interpolationType = InterpolationType.BEZIER;
			for (Sequence sequence : sequenceMap.keySet()) {
				TreeMap<Integer, Entry<T>> entryMap = sequenceMap.get(sequence);
				for (Entry<T> entry : entryMap.values()) {
					entry.unLinearize();
				}
			}
		}
	}

	public void unLinearize2() {
		for (Sequence sequence : sequenceMap.keySet()) {
			TreeMap<Integer, Entry<T>> entryTreeMap = sequenceMap.get(sequence);
			int animationLength = sequence.getEnd() - sequence.getStart();
			AnimFlagUtils.unLiniarizeMapEntries(this, animationLength, entryTreeMap);
		}
	}

	public void bezToHerm() {
		if (!interpolationType.tangential()) {
			interpolationType = InterpolationType.HERMITE;
			for (Sequence sequence : sequenceMap.keySet()) {
				TreeMap<Integer, Entry<T>> entryMap = sequenceMap.get(sequence);
				for (Entry<T> entry : entryMap.values()) {
					entry.bezToHerm();
				}
			}
		}
	}
	public void hermToBez() {
		if (!interpolationType.tangential()) {
			interpolationType = InterpolationType.BEZIER;
			for (Sequence sequence : sequenceMap.keySet()) {
				TreeMap<Integer, Entry<T>> entryMap = sequenceMap.get(sequence);
				for (Entry<T> entry : entryMap.values()) {
					entry.hermToBez();
				}
			}
		}
	}

	public void setInterpType(InterpolationType interpolationType) {
		if (interpolationType != this.interpolationType) {
			if (interpolationType.tangential() && this.interpolationType.tangential()) {
				if (this.interpolationType == InterpolationType.BEZIER) {
					bezToHerm();
				} else if (this.interpolationType == InterpolationType.HERMITE) {
					hermToBez();
				}
			} else if (interpolationType.tangential()) {
//				unLinearize();
				unLinearize2();
			} else if (this.interpolationType.tangential()) {
				linearize();
			}
		}
		this.interpolationType = interpolationType;
	}

	public void quickSetInterpType(InterpolationType interpolationType) {
		this.interpolationType = interpolationType;
	}

	public T valueAt(Sequence sequence, Integer time) {
		TreeMap<Integer, Entry<T>> entryMap = getEntryTreeMap(sequence);
		if (entryMap != null && entryMap.containsKey(time)) {
			return entryMap.get(time).getValue();
		}
		return null;
	}

	public T inTanAt(Sequence sequence, Integer time) {
		TreeMap<Integer, Entry<T>> entryMap = getEntryTreeMap(sequence);
		if (entryMap != null && entryMap.containsKey(time)) {
			return entryMap.get(time).getInTan();
		}
		return null;
	}

	public T outTanAt(Sequence sequence, Integer time) {
		TreeMap<Integer, Entry<T>> entryMap = getEntryTreeMap(sequence);
		if (entryMap != null && entryMap.containsKey(time)) {
			return entryMap.get(time).getOutTan();
		}
		return null;
	}

	public int getTimeFromIndex(Sequence sequence, int index) {
		TreeMap<Integer, Entry<T>> entryMap = getEntryTreeMap(sequence);
		if (entryMap != null && 0 <= index && index < entryMap.size()) {
			return getTimeKeys(getRealSequence(sequence))[index];
		}
		return -1;
	}

	public int getIndexOfTime(Sequence sequence, int time) {
		TreeMap<Integer, Entry<T>> entryMap = getEntryTreeMap(sequence);
		if (entryMap != null) {
			return entryMap.navigableKeySet().subSet(entryMap.firstKey(), entryMap.floorKey(time)).size();
		}
		return -1;
	}

	public T getValueFromIndex(Sequence sequence, int index) {
		TreeMap<Integer, Entry<T>> entryMap = getEntryTreeMap(sequence);
		if (entryMap != null && 0 <= index && index <= entryMap.size()) {
			Entry<T> entry = entryMap.get(getTimeFromIndex(sequence, index));
			if (entry != null) {
				return entry.getValue();
			}
		}
		timeKeysMap.remove(sequence);
		return null;
	}

	public T getInTanFromIndex(Sequence sequence, int index) {
		TreeMap<Integer, Entry<T>> entryMap = getEntryTreeMap(sequence);
		if (entryMap != null && 0 <= index && index <= entryMap.size()) {
			Entry<T> entry = entryMap.get(getTimeFromIndex(sequence, index));
			if (entry != null) {
				return entry.getInTan();
			}
		}
		timeKeysMap.remove(sequence);
		return null;
	}

	public T getOutTanFromIndex(Sequence sequence, int index) {
		TreeMap<Integer, Entry<T>> entryMap = getEntryTreeMap(sequence);
		if (entryMap != null && 0 <= index && index <= entryMap.size()) {
			Entry<T> entry = entryMap.get(getTimeFromIndex(sequence, index));
			if (entry != null) {
				return entry.getOutTan();
			}
		}
		timeKeysMap.remove(sequence);
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

	public T interpolateAt(Sequence sequence, int time) {
		TreeMap<Integer, Entry<T>> entryMap = getEntryTreeMap(sequence);
		if (entryMap == null || entryMap.isEmpty()) {
			if (this instanceof IntAnimFlag) {
				System.out.println("[AnimFlag] Case 2: no entryMap or entryMap empty");
			}
			return getIdentity();
		}

		int sequenceLength = sequence.getLength();

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
		return getInterpolatedValue(floorTime, ceilTime, timeFactor, sequence);
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

	public Entry<T> getFloorEntry(int time, Sequence sequence) {
		TreeMap<Integer, Entry<T>> entryMap = getEntryTreeMap(sequence);
		if (entryMap != null) {
			Integer floorTime = entryMap.floorKey(time);
			if (floorTime == null || floorTime < 0) {
				Integer key = entryMap.floorKey(sequence.getLength());
				if (key == null) {
					return null;
				}
				return entryMap.get(key);
			}
			return entryMap.get(floorTime);
		}
		return null;
	}

	public Entry<T> getCeilEntry(int time, Sequence sequence) {
		TreeMap<Integer, Entry<T>> entryMap = getEntryTreeMap(sequence);
		if (entryMap != null) {
			Integer ceilTime = entryMap.ceilingKey(time);
			if (ceilTime == null || sequence.getLength() < ceilTime) {
				Integer key = entryMap.ceilingKey(0);
				return key == null ? null : entryMap.get(key);
			}
			return entryMap.get(ceilTime);
		}
		return null;
	}

	public T getInterpolatedValue(Integer floorTime, Integer ceilTime, float timeFactor, Sequence sequence) {
		TreeMap<Integer, Entry<T>> entryMap = getEntryTreeMap(sequence);
		Entry<T> entryFloor = entryMap.get(floorTime);
		Entry<T> entryCeil = entryMap.get(ceilTime);

		return getInterpolatedValue(entryFloor, entryCeil, timeFactor);
	}

	public abstract T getInterpolatedValue(Entry<T> entryFloor, Entry<T> entryCeil, float timeFactor);

	protected abstract T getIdentity();

	private Integer[] getTimeKeys(Sequence sequence) {
		TreeMap<Integer, Entry<T>> entryMap = getEntryTreeMap(sequence);
		if (entryMap != null) {
			if (timeKeysMap.get(sequence) == null || timeKeysMap.get(sequence).length != entryMap.size()) {
				timeKeysMap.put(sequence, entryMap.keySet().toArray(new Integer[0]));
			}
		}
		return timeKeysMap.get(sequence);
	}

//	public abstract void calcNewTans(float factor, T curValue, T nextValue, T prevValue, Entry<T> entry);

	public abstract void calcNewTans(float[] factor, Entry<T> next, Entry<T> prev, Entry<T> cur, Integer animationLength);

	public abstract float[] getTcbFactor(float tension, float continuity, float bias);

	public float[] getTCB(int negForQuat, float tension, float continuity, float bias) {
		float[] factor = new float[4];

		float contP = negForQuat * continuity;
		float biasP = negForQuat * bias;

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
