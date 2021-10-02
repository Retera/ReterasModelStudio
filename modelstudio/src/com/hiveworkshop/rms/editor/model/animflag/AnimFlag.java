package com.hiveworkshop.rms.editor.model.animflag;

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
	// Types of AnimFlags:
	public static final int OTHER_TYPE = 0; // Use for titles like "Intensity", "AmbIntensity", and other extraneous things
	public static final int ALPHA = 0;
	public static final int SCALING = 1;
	public static final int ROTATION = 2;
	public static final int TRANSLATION = 3;
	public static final int COLOR = 4;
	public static final int TEXTUREID = 5;

	public static final Quat ROTATE_IDENTITY = new Quat(0, 0, 0, 1);
	public static final Vec3 SCALE_IDENTITY = new Vec3(1, 1, 1);
	public static final Vec3 TRANSLATE_IDENTITY = new Vec3(0, 0, 0);
	protected String name;
	protected InterpolationType interpolationType = InterpolationType.DONT_INTERP;
	protected GlobalSeq globalSeq;
	protected Map<Sequence, TreeMap<Integer, Entry<T>>> sequenceMap = new HashMap<>();
	protected int typeid = 0;
	protected Map<Sequence, Integer[]> timeKeysMap = new HashMap<>();

	public AnimFlag(String title) {
		name = title;
		generateTypeId();
	}

	public AnimFlag(MdlxTimeline<?> timeline, EditableModel model) {
		name = AnimationMap.ID_TO_TAG.get(timeline.name).getMdlToken();
		generateTypeId();

		interpolationType = timeline.interpolationType;

		if (this instanceof IntAnimFlag) {
			System.out.println(name + ", glob seq id: " + timeline.globalSequenceId + ", model glob seq: " + model.getGlobalSeq(timeline.globalSequenceId));
		}

		setGlobSeq(model.getGlobalSeq(timeline.globalSequenceId));
	}

	private long lastConsoleLogTime = 0;

	public static AnimFlag<?> createFromTimeline(MdlxTimeline<?> timeline, EditableModel model) {
		return switch (AnimationMap.valueOf(timeline.name.asStringValue()).getImplementation()) {
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
		typeid = af.typeid;
	}

	public boolean equals(AnimFlag<T> animFlag) {
		if (animFlag == null) {
			return false;
		}
		return name.equals(animFlag.getName())
				|| sequenceMap.equals(animFlag.sequenceMap)
				|| (Objects.equals(globalSeq, animFlag.globalSeq)
				&& interpolationType == animFlag.interpolationType
				&& typeid == animFlag.typeid);
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
			Q[] array = getArray(entry, mdlxTimeline);
			if(i == 0){
				System.out.println("(Q): " + (Q) entry.getValueArr() + ", org: " + entry.getValueArr());
				System.out.println("(Q): " + ", org: " + Arrays.toString(entry.getValueArr()));
			}
//			mdlxTimeline.add(i, tempFrames.get(i), (Q)tempEntries.get(i).getValueArr(), (Q)tempEntries.get(i).getInTanArr(), (Q)tempEntries.get(i).getOutTanArr());
//			mdlxTimeline.add(i, tempFrames.get(i), (Q) entry.getValueArr(), (Q) entry.getInTanArr(), (Q) entry.getOutTanArr());
			mdlxTimeline.add(i, tempFrames.get(i), array[0], array[1], array[2]);
		}


		return mdlxTimeline;
	}

	private <Q, W> Q[] getArray(Entry<W> entry, MdlxTimeline<Q> mdlxTimeline){
		if(entry.getValue() instanceof Integer && mdlxTimeline instanceof MdlxUInt32Timeline){
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

	public void setValuesTo(AnimFlag<?> source) {
		//todo check it this should clear existing
		AnimFlag<T> tSource = getAsTypedOrNull(source);
		if (tSource != null) {
			setSettingsFrom(tSource);

			for (Sequence anim : tSource.getAnimMap().keySet()) {
				TreeMap<Integer, Entry<T>> entryMap = tSource.getAnimMap().get(anim);
				entryMap.replaceAll((t, v) -> tSource.getEntryMap(anim).get(t).deepCopy());
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

	public int getTypeId() {
		return typeid;
	}

	public void generateTypeId() {
		typeid = switch (name) {
			case "Scaling" -> SCALING;
			case "Rotation" -> ROTATION;
			case "Translation" -> TRANSLATION;
			case "Color" -> COLOR;
			case "TextureID" ->TEXTUREID; // aflg.title.equals("Visibility") || -- 100.088% visible in UndeadCampaign3D OutTans! Golook!
			default -> ALPHA;
		};
	}

	public void flipOver(byte axis) {
		for (Sequence anim : sequenceMap.keySet()) {
			TreeMap<Integer, Entry<T>> entryMap = sequenceMap.get(anim);
			Collection<Entry<T>> entries = entryMap.values();
			if (typeid == ROTATION && this instanceof QuatAnimFlag) {
				// Rotation
				for (Entry<T> entry : entries) {
					flipQuat(axis, (Quat) entry.getValue());
					flipQuat(axis, (Quat) entry.getInTan());
					flipQuat(axis, (Quat) entry.getOutTan());
				}
			} else if (typeid == TRANSLATION && this instanceof Vec3AnimFlag) {
				// Translation
				for (Entry<T> entry : entries) {
					flipVec3(axis, (Vec3) entry.getValue());
					flipVec3(axis, (Vec3) entry.getInTan());
					flipVec3(axis, (Vec3) entry.getOutTan());
				}
			}
		}
	}

	public void flipVec3(byte axis, Vec3 value) {
		value.setCoord(axis, -value.getCoord(axis));
	}

	private void flipQuat(byte axis, Quat quat) {
		Vec3 euler = quat.toEuler();
		switch (axis) {
			case 0 -> {
				euler.x = -euler.x;
				euler.y = -euler.y;
			}
			case 1 -> {
				euler.x = -euler.x;
				euler.z = -euler.z;
			}
			case 2 -> {
				euler.y = -euler.y;
				euler.z = -euler.z;
			}
		}
		quat.set(euler);
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
			unLiniarizeMapEntries(animationLength, entryTreeMap);
		}
	}

	private void unLiniarizeMapEntries(int animationLength, TreeMap<Integer, Entry<T>> entryTreeMap) {
		entryTreeMap.forEach((t, e) -> e.unLinearize());
		for (Integer time : entryTreeMap.keySet()) {
			Integer prevTime = entryTreeMap.lowerKey(time) == null ? entryTreeMap.lastKey() : entryTreeMap.lowerKey(time);
			Integer nextTime = entryTreeMap.higherKey(time) == null ? entryTreeMap.firstKey() : entryTreeMap.higherKey(time);

			Entry<T> prevValue = entryTreeMap.get(prevTime);
			Entry<T> nextValue = entryTreeMap.get(nextTime);

			float[] factor = getTbcFactor(0, 0.5f, 0);
			calcNewTans(factor, nextValue, prevValue, entryTreeMap.get(time), animationLength);
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
		}
	}


	public void clear() {
		sequenceMap.clear();
	}

	/**
	 * Copies time track data from a certain interval into a different, new interval.
	 * The AnimFlag source of the data to copy cannot be same AnimFlag into which the
	 * data is copied, or else a ConcurrentModificationException will be thrown.
	 * Does not check that the destination interval is empty!
	 *
	 * @param source     the AnimFlag from which values will be copied
	 * @param sourceAnim the Animation from which to copy
	 * @param newAnim    the Animation to receive Entries
	 * @param offset     the offset from the start of the receiving animation at which to start adding keyframes
	 */
	public void copyFrom(AnimFlag<?> source, Sequence sourceAnim, Sequence newAnim, int offset) {

		AnimFlag<T> tSource = getAsTypedOrNull(source);
		if (tSource != null && tSource.getEntryMap(sourceAnim) != null) {
			boolean sourceHasTans = tSource.tans();

			TreeMap<Integer, Entry<T>> sequenceEntryMapCopy = tSource.getSequenceEntryMapCopy(sourceAnim);
			if (sequenceEntryMapCopy != null) {

				if (sourceAnim.getLength() + offset != newAnim.getLength()) {
					double ratio = (newAnim.getLength() - offset) / ((double) sourceAnim.getLength());
					scaleMapEntries(ratio, sequenceEntryMapCopy);
				}

				if (offset != 0) {
					TreeMap<Integer, Entry<T>> seqMovedMap = new TreeMap<>();
					sequenceEntryMapCopy.forEach((t, e) -> seqMovedMap.put(t + offset, e.setTime(t + offset)));
					sequenceEntryMapCopy = seqMovedMap;
				}

				if (!tans() && sourceHasTans) {
					sequenceEntryMapCopy.forEach((t, e) -> e.linearize());
				} else if (tans() && !sourceHasTans) {
					unLiniarizeMapEntries(newAnim.getLength(), sequenceEntryMapCopy);
				}

				TreeMap<Integer, Entry<T>> entryMap = sequenceMap.computeIfAbsent(newAnim, k -> new TreeMap<>());
				entryMap.putAll(sequenceEntryMapCopy);
			}
		}
	}

	// Does clear existing values
	public void copyFrom(AnimFlag<?> source, Sequence sourceAnim, Sequence newAnim) {
		AnimFlag<T> tSource = getAsTypedOrNull(source);
		if (tSource != null && tSource.getEntryMap(sourceAnim) != null) {
			boolean sourceHasTans = tSource.tans();

			TreeMap<Integer, Entry<T>> sequenceEntryMapCopy = tSource.getSequenceEntryMapCopy(sourceAnim);
			if (sequenceEntryMapCopy != null) {

				if (sourceAnim.getLength() != newAnim.getLength()) {
					double ratio = ((double) newAnim.getLength()) / ((double) sourceAnim.getLength());
					scaleMapEntries(ratio, sequenceEntryMapCopy);
				}

				if (!tans() && sourceHasTans) {
					sequenceEntryMapCopy.forEach((t, e) -> e.linearize());
				} else if (tans() && !sourceHasTans) {
					unLiniarizeMapEntries(newAnim.getLength(), sequenceEntryMapCopy);
				}
				sequenceMap.put(newAnim, sequenceEntryMapCopy);
			}
		}
	}

	public void copyFrom(AnimFlag<?> source) {
		AnimFlag<T> tSource = getAsTypedOrNull(source);
		if (tSource != null) {
			// ToDo give user option to either linearize animflag or unlinearize copied entries
			boolean linearizeEntries = !tans() && tSource.tans();
			boolean unlinearizeEntries = tans() && !tSource.tans();
			for (Sequence anim : tSource.getAnimMap().keySet()) {
				TreeMap<Integer, Entry<T>> sourceEntryMap = tSource.getAnimMap().get(anim);
				TreeMap<Integer, Entry<T>> entryMap = sequenceMap.computeIfAbsent(anim, k -> new TreeMap<>());
				for (Integer time : sourceEntryMap.keySet()) {
					final Entry<T> copiedEntry = sourceEntryMap.get(time).deepCopy();
					if (linearizeEntries) {
						copiedEntry.linearize();
					} else if (unlinearizeEntries) {
						copiedEntry.unLinearize();
					}
					entryMap.put(time, copiedEntry);
				}
			}
		}
	}

	private AnimFlag<T> getAsTypedOrNull(AnimFlag<?> source) {
		if (this instanceof IntAnimFlag && source instanceof IntAnimFlag
				|| this instanceof FloatAnimFlag && source instanceof FloatAnimFlag
				|| this instanceof Vec3AnimFlag && source instanceof Vec3AnimFlag
				|| this instanceof QuatAnimFlag && source instanceof QuatAnimFlag) {
			return (AnimFlag<T>) source;
		}
		return null;
	}

	public void timeScale2(Sequence anim, int newLength, int offsetFromStart) {
		// Timescales a part of the AnimFlag from section "start" to "end" into the new time "newStart" to "newEnd"
		TreeMap<Integer, Entry<T>> entryMap = sequenceMap.get(anim);
		if (entryMap != null) {
			TreeMap<Integer, Entry<T>> scaledMap = new TreeMap<>();
			int animLength = Math.max(0, anim.getLength());
			double ratio = (double) (newLength) / (double) animLength;
			Integer lastKF = entryMap.floorKey(animLength);
			if (lastKF != null) {
				for (Integer time = entryMap.ceilingKey(0); time != null && time <= lastKF; time = entryMap.higherKey(time)) {
					int newTime = (int) (offsetFromStart + (time * ratio));
					scaledMap.put(newTime, entryMap.remove(time).setTime(newTime));
				}
			}
			entryMap.putAll(scaledMap);
		}
	}

	public void timeScale3(Sequence anim, double ratio) {
		// Timescales a part of the AnimFlag from section "start" to "end" into the new time "newStart" to "newEnd"
		TreeMap<Integer, Entry<T>> entryMap = sequenceMap.get(anim);
		if (entryMap != null) {
			scaleMapEntries(ratio, entryMap);
		}
	}

	private void scaleMapEntries(double ratio, TreeMap<Integer, Entry<T>> entryMap) {
		TreeMap<Integer, Entry<T>> scaledMap = new TreeMap<>();
		for (Integer time : entryMap.keySet()) {
			int newTime = (int) (time * ratio);
			scaledMap.put(newTime, entryMap.get(time).setTime(newTime));
		}
		entryMap.clear();
		entryMap.putAll(scaledMap);
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
//			System.out.println("Identity 1, seqMapEmpty: " + sequenceMap.isEmpty());
			return getIdentity(typeid);
		}

		Sequence currentSequence = hasGlobalSeq() ? globalSeq : animatedRenderEnvironment.getCurrentSequence();
		int time = animatedRenderEnvironment.getTrackTime(globalSeq);
		return interpolateAt(currentSequence, time);
	}

	public T interpolateAt(Sequence currentSequence, int time) {
		TreeMap<Integer, Entry<T>> entryMap = sequenceMap.get(currentSequence);
		if (entryMap == null || entryMap.isEmpty()) {
			if (this instanceof IntAnimFlag) {
				System.out.println("Identity 2: no entryMap or entryMap empty");
			}
			return getIdentity(typeid);
		}

		int sequenceLength = currentSequence.getLength();

//		// no keyframes at nor after time
//		if (hasGlobalSeq() && globalSeq.getLength() >= 0 && entryMap.ceilingKey(time) == null) {
//			System.out.println("Identity 3, " + globalSeq + " " + time + ", " + entryMap.ceilingKey(time));
//			return getIdentity(typeid);
//		}

		Integer lastKeyframeTime = entryMap.floorKey(sequenceLength);
		Integer firstKeyframeTime = entryMap.ceilingKey(0);

		// either no keyframes before animationEnd,
		// no keyframes after animationStart,
		// no keyframes in animation
		// or time is outside of animation
		if (lastKeyframeTime == null
				|| firstKeyframeTime == null
				|| lastKeyframeTime < firstKeyframeTime
				|| sequenceLength < time
				|| time < 0) {
//			System.out.println("Identity 4");
			return getIdentity(typeid);
		}
		// only one keyframe in the animation
		if (lastKeyframeTime.equals(firstKeyframeTime)) {
			if (this instanceof IntAnimFlag) {
//				System.out.println("Identity 2: no entryMap or entryMap empty");
//			System.out.println("OneValue");
				System.out.println("lastKeyframeTime: " + lastKeyframeTime + ", firstKeyframeTime: " + firstKeyframeTime + ", from time: " + time + " and seqLength: " + sequenceLength);
			}
			return entryMap.get(lastKeyframeTime).getValue();
		}

		Integer floorTime = entryMap.floorKey(time);
		if (floorTime == null || floorTime < 0) {
//			System.out.println("floorTime: " + floorTime + ", lKFt: " + lastKeyframeTime + ", from time: " + time);
			floorTime = lastKeyframeTime;
		}

		Integer ceilTime = entryMap.ceilingKey(time);
		if (ceilTime == null || ceilTime > sequenceLength) {
//			System.out.println("ceilTime: " + ceilTime + ", fKFt: " + firstKeyframeTime + ", from time: " + time);
			ceilTime = firstKeyframeTime;
		}

		if (floorTime.equals(ceilTime)) {
//			System.out.println("on KF");
			return entryMap.get(floorTime).getValue();
		}

		float timeFactor = getTimeFactor(time, sequenceLength, floorTime, ceilTime);

//		System.out.println("interpolating!");
		return getInterpolatedValue(floorTime, ceilTime, timeFactor, currentSequence);
	}

	protected float getTimeFactor(int time, int animationLength, Integer floorTime, Integer ceilTime) {
		int timeBetweenFrames = ceilTime - floorTime;

		// if ceilTime wrapped, add animation length
		if (timeBetweenFrames < 0) {
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

	protected abstract T getIdentity(int typeId);

	protected Object identity(int typeId) {
		return switch (typeId) {
			case ALPHA -> 1.f;
			case TRANSLATION -> TRANSLATE_IDENTITY;
			case SCALING, COLOR -> SCALE_IDENTITY;
			case ROTATION -> ROTATE_IDENTITY;
			case TEXTUREID -> {
				long currentTime = System.currentTimeMillis();
				if (lastConsoleLogTime < currentTime) {
					System.err.println("Texture identity used in renderer... TODO make this function more intelligent.");
					lastConsoleLogTime = currentTime + 1000;
				}
				yield 0;
			}
			default -> throw new IllegalStateException("Unexpected value: " + typeId);
		};
	}

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
}
