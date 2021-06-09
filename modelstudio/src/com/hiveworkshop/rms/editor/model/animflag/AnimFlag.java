package com.hiveworkshop.rms.editor.model.animflag;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.TimelineContainer;
import com.hiveworkshop.rms.parsers.mdlx.AnimationMap;
import com.hiveworkshop.rms.parsers.mdlx.InterpolationType;
import com.hiveworkshop.rms.parsers.mdlx.timeline.MdlxFloatArrayTimeline;
import com.hiveworkshop.rms.parsers.mdlx.timeline.MdlxFloatTimeline;
import com.hiveworkshop.rms.parsers.mdlx.timeline.MdlxTimeline;
import com.hiveworkshop.rms.parsers.mdlx.timeline.MdlxUInt32Timeline;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeBoundProvider;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
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
	String name;
	public InterpolationType interpolationType = InterpolationType.DONT_INTERP;
	public Integer globalSeqLength;
	int globalSeqId = -1;
	public boolean hasGlobalSeq = false;
	protected TreeMap<Integer, Entry<T>> entryMap = new TreeMap<>();
	int typeid = 0;
	Integer[] timeKeys;

	public AnimFlag(String title) {
		name = title;
		generateTypeId();
	}

	public AnimFlag(MdlxTimeline<?> timeline) {
		name = AnimationMap.ID_TO_TAG.get(timeline.name).getMdlToken();
		generateTypeId();

		interpolationType = timeline.interpolationType;

		int globalSequenceId = timeline.globalSequenceId;
		if (globalSequenceId >= 0) {
			setGlobalSeqId(globalSequenceId);
			setHasGlobalSeq(true);
		}
	}

	private long lastConsoleLogTime = 0;

	public static AnimFlag<?> createFromTimeline(MdlxTimeline<?> timeline) {
		Object firstValue = timeline.values[0];
		if (firstValue instanceof float[]) {
			final int length = ((float[]) firstValue).length;
			return switch (length){
				case 1 -> new FloatAnimFlag((MdlxFloatTimeline) timeline);
				case 3 -> new Vec3AnimFlag((MdlxFloatArrayTimeline) timeline);
				case 4 -> new QuatAnimFlag((MdlxFloatArrayTimeline) timeline);
				default -> null;
			};
		} else if (firstValue instanceof long[]) {
			if (timeline.name.toString().equalsIgnoreCase("rotation")) {
				return new QuatAnimFlag((MdlxFloatArrayTimeline) timeline);
			}
			return new IntAnimFlag((MdlxUInt32Timeline) timeline);
		}
		return null;
	}

	public abstract T cloneValue(Object value);

	public AnimFlag(AnimFlag<T> af) {
		setSettingsFrom(af);
		for (Integer time : af.getEntryMap().keySet()) {
			entryMap.put(time, af.getEntryMap().get(time).deepCopy());
		}
	}

	public abstract AnimFlag<T> getEmptyCopy();

	public abstract AnimFlag<T> deepCopy();

	protected void setSettingsFrom(AnimFlag<?> af) {
		name = af.name;
		globalSeqLength = af.globalSeqLength;
		globalSeqId = af.globalSeqId;
		hasGlobalSeq = af.hasGlobalSeq;
		interpolationType = af.interpolationType;
		typeid = af.typeid;
	}

	public boolean equals(AnimFlag<T> animFlag) {
		if (animFlag == null) {
			return false;
		}
		return name.equals(animFlag.getName())
				|| entryMap.equals(animFlag.entryMap)
				|| hasGlobalSeq == animFlag.hasGlobalSeq
				|| (Objects.equals(globalSeqLength, animFlag.globalSeqLength)
				&& interpolationType == animFlag.interpolationType
				&& typeid == animFlag.typeid);
	}

	public AnimFlag<T> setFromOther(AnimFlag<T> other) {
		entryMap = other.getEntryMap(); // ToDo copy entries!
		return this;
	}

	public Integer getGlobalSeqLength() {
		return globalSeqLength;
	}

	public void setGlobalSeqLength(Integer globalSeqLength) {
		this.globalSeqLength = globalSeqLength;
	}

	public void setGlobSeq(Integer integer) {
		globalSeqLength = integer;
		hasGlobalSeq = integer != null;
	}

	public int getGlobalSeqId() {
		return globalSeqId;
	}

	public void setGlobalSeqId(int globalSeqId) {
		this.globalSeqId = globalSeqId;
	}

	public boolean hasGlobalSeq() {
		return hasGlobalSeq;
	}

	public void setHasGlobalSeq(boolean hasGlobalSeq) {
		this.hasGlobalSeq = hasGlobalSeq;
	}

	public void setInterpType(InterpolationType interpolationType) {
		if (interpolationType.tangential()) {
			unLinearize();
		} else {
			linearize();
		}
		this.interpolationType = interpolationType;
	}

	public InterpolationType getInterpolationType() {
		return interpolationType;
	}

	public int size() {
		return entryMap.size();
	}

	public abstract MdlxTimeline<?> toMdlx(TimelineContainer container);

	public void addEntry(Integer time, T value) {
		Entry<T> entry = new Entry<>(time, value);
		entryMap.put(time, entry);
		if (tans()) {
			entry.unLinearize();
		}
	}

	protected void addEntry(Integer time, T value, T inTan, T outTan) {
		entryMap.put(time, new Entry<>(time, value, inTan, outTan));
	}

//	public void addEntry(Entry<T> entry) {
//		entryMap.put(entry.getTime(), entry);
////		entryMap.put(entry.getTime(), new Entry<>(entry));
//	}
//
//	public void setEntry(Entry<T> entry) {
//		Entry<T> tEntry = entry.deepCopy();
//		entryMap.put(tEntry.time, tEntry);
//	}

	/**
	 * To set an Entry with an Entry
	 *
	 * @param time  the time of the entry to be changed
	 * @param entry the entry to replace the old entry
	 */
	public void setOrAddEntryT(Integer time, Entry<?> entry) {
		if (entry.getValue() instanceof Integer && this instanceof IntAnimFlag
				|| entry.getValue() instanceof Float && this instanceof FloatAnimFlag
				|| entry.getValue() instanceof Vec3 && this instanceof Vec3AnimFlag
				|| entry.getValue() instanceof Quat && this instanceof QuatAnimFlag) {
			Entry<T> tEntry = (Entry<T>) entry.setTime(time);
			entryMap.put(time, tEntry);
		}
	}

	public void setValuesTo(AnimFlag<?> source) {
		AnimFlag<T> tSource = getAsTypedOrNull(source);
		if (tSource != null) {
			setSettingsFrom(tSource);

			for (Integer time : tSource.getEntryMap().keySet()) {
				entryMap.put(time, tSource.getEntryMap().get(time).deepCopy());
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

	public void updateGlobalSeqRef(EditableModel mdlr) {
		if (hasGlobalSeq) {
			globalSeqLength = mdlr.getGlobalSeq(globalSeqId);
		}
	}

	public void updateGlobalSeqId(EditableModel mdlr) {
		if (hasGlobalSeq) {
			globalSeqId = mdlr.getGlobalSeqId(globalSeqLength);
		}
	}

	public void flipOver(byte axis) {
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
			for (Entry<T> entry : entryMap.values()) {
				entry.linearize();
			}
		}
	}

	public void unLinearize() {
		if (!interpolationType.tangential()) {
			interpolationType = InterpolationType.BEZIER;
			for (Entry<T> entry : entryMap.values()) {
				entry.unLinearize();
			}
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

	public void deleteAnim(Animation anim) {
		if (!hasGlobalSeq) {
			for (int time = entryMap.ceilingKey(anim.getStart()); time <= entryMap.floorKey(anim.getEnd()); time = entryMap.higherKey(time)) {
				entryMap.remove(time);
			}
		} else {
			System.out.println("KeyFrame deleting was blocked by a GlobalSequence");
		}
	}

	public void deleteTime(int time) {
		entryMap.remove(time);
	}

	public void removeKeyframe(int trackTime) {
		entryMap.remove(trackTime);
	}


	public void clear() {
		entryMap.clear();
	}

	/**
	 * Copies time track data from a certain interval into a different, new interval.
	 * The AnimFlag source of the data to copy cannot be same AnimFlag into which the
	 * data is copied, or else a ConcurrentModificationException will be thrown.
	 * Does not check that the destination interval is empty!
	 *
	 * @param source      the AnimFlag which values will be copied
	 * @param sourceStart the start time for the interval to be copied, inclusive
	 * @param sourceEnd   the end time for the interval to be copied, inclusive
	 * @param newStart    the start time for the interval in the destination AnimFlag, inclusive
	 * @param newEnd      the end time for the interval in the destination AnimFlag, inclusive
	 */
	public void copyFrom(AnimFlag<?> source, int sourceStart, int sourceEnd, int newStart, int newEnd) {
		AnimFlag<T> tSource = getAsTypedOrNull(source);
		if (tSource != null) {
			boolean linearizeEntries = !tans() && tSource.tans();
			boolean unlinearizeEntries = tans() && !tSource.tans();
//			if (tans() && !tSource.tans()) {
//				JOptionPane.showMessageDialog(null,
//						"Some animations will lose complexity due to transfer incombatibility. There will probably be no visible change.");
//				linearize();
//				// Probably makes this flag linear, but certainly makes it more like the copy source
//			}

			TreeMap<Integer, Entry<T>> scaledMap = new TreeMap<>();
			final TreeMap<Integer, Entry<T>> sourceEntryMap = tSource.getEntryMap();
			for (int time = sourceEntryMap.ceilingKey(sourceStart); time <= sourceEntryMap.floorKey(sourceEnd); time = sourceEntryMap.higherKey(time)) {
				double ratio = (double) (time - sourceStart) / (double) (sourceEnd - sourceStart);
				int newTime = (int) (newStart + (ratio * (newEnd - newStart)));
				final Entry<T> copiedEntry = sourceEntryMap.get(time).deepCopy().setTime(newTime);
				if (linearizeEntries) {
					copiedEntry.linearize();
				} else if (unlinearizeEntries){
					copiedEntry.unLinearize();
				}
				scaledMap.put(newTime, copiedEntry);
			}
			this.entryMap.putAll(scaledMap);
		}
	}

	public void copyFrom(AnimFlag<?> source) {
		AnimFlag<T> tSource = getAsTypedOrNull(source);
		if (tSource != null) {
			// ToDo give user option to either linearize animflag or unlinearize copied entries
			boolean linearizeEntries = !tans() && tSource.tans();
			boolean unlinearizeEntries = tans() && !tSource.tans();
//			if (tans() && !tSource.tans()) {
//				JOptionPane.showMessageDialog(null,
//						"Some animations will lose complexity due to transfer incombatibility. There will probably be no visible change.");
//				linearize();
//				// Probably makes this flag linear, but certainly makes it more like the copy source
//			}
			for (Integer time : tSource.getEntryMap().keySet()) {
				final Entry<T> copiedEntry = tSource.getEntryMap().get(time).deepCopy();
				if (linearizeEntries) {
					copiedEntry.linearize();
				} else if (unlinearizeEntries){
					copiedEntry.unLinearize();
				}
				entryMap.put(time, copiedEntry);
			}
		}
	}
	private AnimFlag<T> getAsTypedOrNull(AnimFlag<?> source){
		if (this instanceof IntAnimFlag && source instanceof IntAnimFlag
				|| this instanceof FloatAnimFlag && source instanceof FloatAnimFlag
				|| this instanceof Vec3AnimFlag && source instanceof Vec3AnimFlag
				|| this instanceof QuatAnimFlag && source instanceof QuatAnimFlag) {
			return  (AnimFlag<T>) source;
		}
		return null;
	}

	public void timeScale(int start, int end, int newStart, int newEnd) {
		// Timescales a part of the AnimFlag from section "start" to "end" into the new time "newStart" to "newEnd"
		TreeMap<Integer, Entry<T>> scaledMap = new TreeMap<>();
		for (int time = entryMap.ceilingKey(start); time <= entryMap.floorKey(end); time = entryMap.higherKey(time)) {
			double ratio = (double) (time - start) / (double) (end - start);
			int newTime = (int) (newStart + (ratio * (newEnd - newStart)));
			scaledMap.put(newTime, entryMap.remove(time).setTime(newTime));
		}
		entryMap.putAll(scaledMap);
	}

	public Entry<T> getEntryAt(int time) {
		return entryMap.get(time);
	}

	public boolean hasEntryAt(int time) {
		return entryMap.containsKey(time);
	}

	public T valueAt(Integer time) {
		if (entryMap.containsKey(time)) {
			return entryMap.get(time).getValue();
		}
		return null;
	}

	public T inTanAt(Integer time) {
		if (entryMap.containsKey(time)) {
			return entryMap.get(time).getInTan();
		}
		return null;
	}

	public T outTanAt(Integer time) {
		if (entryMap.containsKey(time)) {
			return entryMap.get(time).getOutTan();
		}
		return null;
	}

	public int getTimeFromIndex(int index) {
		if (0 <= index && index <= entryMap.size()) {
			return getTimeKeys()[index];
		}
		return -1;
	}

	public int getIndexOfTime(int time) {
		return entryMap.navigableKeySet().subSet(entryMap.firstKey(), entryMap.floorKey(time)).size();
	}

	public T getValueFromIndex(int index) {
		return entryMap.get(getTimeFromIndex(index)).getValue();
	}

	public T getInTanFromIndex(int index) {
		return entryMap.get(getTimeFromIndex(index)).getInTan();
	}

	public T getOutTanFromIndex(int index) {
		return entryMap.get(getTimeFromIndex(index)).getOutTan();
	}

	/**
	 * Interpolates at a given time.
	 */
	public T interpolateAt(final TimeEnvironmentImpl animatedRenderEnvironment) {
		if (entryMap.isEmpty()) {
			return getIdentity(typeid);
		}
		if ((animatedRenderEnvironment == null) || (animatedRenderEnvironment.getCurrentAnimation() == null)) {
			return entryMap.firstEntry().getValue().getValue(); // Correct?
		}
		int time;
		int animationStart;
		int animationEnd;

		if (hasGlobalSeq() && (getGlobalSeqLength() >= 0)) {
			time = animatedRenderEnvironment.getGlobalSeqTime(getGlobalSeqLength());

			// no keyframes at nor after time
			if (entryMap.ceilingKey(time) == null) {
				return getIdentity(typeid);
			}
			animationStart = 0;
			animationEnd = getGlobalSeqLength();

		} else {
			TimeBoundProvider animation = animatedRenderEnvironment.getCurrentAnimation();
			time = animatedRenderEnvironment.getAnimationTime();
			animationStart = animation.getStart();
			animationEnd = animation.getEnd();
		}

		Integer lastKeyframeTime = entryMap.floorKey(animationEnd);
		Integer firstKeyframeTime = entryMap.ceilingKey(animationStart);

		// either no keyframes before animationEnd,
		// no keyframes after animationStart,
		// no keyframes in animation
		// or time is outside of animation
		if (lastKeyframeTime == null
				|| firstKeyframeTime == null
				|| lastKeyframeTime < firstKeyframeTime
				|| animationEnd < time
				|| time < animationStart) {
			return getIdentity(typeid);
		}
		// only one keyframe in the animation
		if (lastKeyframeTime.equals(firstKeyframeTime)) {
			return entryMap.get(lastKeyframeTime).getValue();
		}

		Integer floorTime = entryMap.floorKey(time);
		if (floorTime == null || floorTime < animationStart) {
			floorTime = lastKeyframeTime;
		}
		Integer ceilTime = entryMap.ceilingKey(time);
		if (ceilTime == null || ceilTime > animationEnd) {
			ceilTime = firstKeyframeTime;
		}

		if (floorTime.equals(ceilTime)) {
			return entryMap.get(floorTime).getValue();
		}

		int timeBetweenFrames = ceilTime - floorTime;

		// if ceilTime wrapped, add animation length
		if (timeBetweenFrames < 0) {
			timeBetweenFrames = timeBetweenFrames + animationEnd - animationStart;
		}

		int timeFromKF = time - floorTime;
		// if floorTime wrapped, add animation length
		if (timeFromKF < 0) {
			timeFromKF = timeFromKF + animationEnd - animationStart;
		}

		float timeFactor = timeFromKF / (float) timeBetweenFrames;

		return getInterpolatedValue(floorTime, ceilTime, timeFactor);
	}

	public abstract T getInterpolatedValue(Integer floorTime, Integer ceilTime, float timeFactor);

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

	public void slideKeyframe(int startTrackTime, int endTrackTime) {
		if (entryMap.isEmpty()) {
			throw new IllegalStateException("Unable to slide keyframe: no frames exist");
		}
		Entry<T> entryToSlide = getEntryAt(startTrackTime);
		if (entryToSlide != null) {
			entryMap.put(endTrackTime, entryMap.remove(startTrackTime).setTime(endTrackTime));
		}
	}

	public TreeMap<Integer, Entry<T>> getEntryMap() {
		return entryMap;
	}

	public AnimFlag<T> setEntryMap(TreeMap<Integer, Entry<T>> otherMap) {
		entryMap = otherMap; // ToDo copy entries!
		return this;
	}

	private Integer[] getTimeKeys() {
		if (timeKeys == null || timeKeys.length != entryMap.size()) {
			timeKeys = entryMap.keySet().toArray(new Integer[0]);
		}
		return timeKeys;
	}
}
