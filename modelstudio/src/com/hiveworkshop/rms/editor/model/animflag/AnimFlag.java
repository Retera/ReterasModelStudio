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
import com.hiveworkshop.rms.util.Vec4;

import javax.swing.*;
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
	// 0 Alpha
	public static final int ALPHA = 0;
	// 1 Scaling
	public static final int SCALING = 1;
	// 2 Rotation
	public static final int ROTATION = 2;
	// 3 Translation
	public static final int TRANSLATION = 3;
	// 4 Color
	public static final int COLOR = 4;
	// 5 TextureID
	public static final int TEXTUREID = 5;

	/**
	 * Use for titles like "Intensity", "AmbIntensity", and other extraneous things
	 * not included in the options above.
	 */
	public static final int OTHER_TYPE = 0;
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
	int vectorSize = 1;
	boolean isFloat = true;
	Integer[] timeKeys;

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

	// end special constructors
	public AnimFlag(String title, List<Integer> times, List<T> values) {
		name = title;
		for (int i = 0; i < times.size(); i++) {
			entryMap.put(times.get(i), new Entry<>(times.get(i), values.get(i)));
		}
		generateTypeId();
	}

//	public static AnimFlag<?> createFromName(AnimFlag<?> af) {
//		if(af instanceof IntAnimFlag){
//			return new IntAnimFlag((IntAnimFlag) af);
//		}else if(af instanceof FloatAnimFlag){
//			return new FloatAnimFlag((FloatAnimFlag) af);
//		}else if(af instanceof Vec3AnimFlag){
//			return new Vec3AnimFlag((Vec3AnimFlag) af);
//		}else if(af instanceof QuatAnimFlag){
//			return new QuatAnimFlag((QuatAnimFlag) af);
//		}
//		else return null;
//	}


//	public static AnimFlag createEmpty2018(String title, InterpolationType interpolationType, Integer globalSeq) {
//		AnimFlag flag = new AnimFlag();
//		flag.name = title;
//		flag.interpolationType = interpolationType;
//		flag.generateTypeId();
//		flag.setGlobSeq(globalSeq);
//		return flag;
//	}

	public AnimFlag(String title) {
		name = title;
		generateTypeId();
	}

	public static AnimFlag<?> createFromTimeline(MdlxTimeline<?> timeline) {
		Object firstValue = timeline.values[0];
		if (firstValue instanceof float[]) {
			if (((float[]) firstValue).length == 1) {
				return new FloatAnimFlag((MdlxFloatTimeline) timeline);
			} else if (((float[]) firstValue).length == 3) {
				return new Vec3AnimFlag((MdlxFloatArrayTimeline) timeline);
			} else if (((float[]) firstValue).length == 4) {
				return new QuatAnimFlag((MdlxFloatArrayTimeline) timeline);
			}
		} else if (firstValue instanceof long[]) {
			if (timeline.name.toString().equalsIgnoreCase("rotation")) {
				return new QuatAnimFlag((MdlxFloatArrayTimeline) timeline);
			}
			return new IntAnimFlag((MdlxUInt32Timeline) timeline);
//			return new IntAnimFlag((MdlxTimeline<long[]>) timeline);
		}
		return null;
	}

	public static AnimFlag<?> createFromAnimFlag(AnimFlag<?> af) {
		if (af instanceof IntAnimFlag) {
			return new IntAnimFlag((IntAnimFlag) af);
		} else if (af instanceof FloatAnimFlag) {
			return new FloatAnimFlag((FloatAnimFlag) af);
		} else if (af instanceof Vec3AnimFlag) {
			return new Vec3AnimFlag((Vec3AnimFlag) af);
		} else if (af instanceof QuatAnimFlag) {
			return new QuatAnimFlag((QuatAnimFlag) af);
		} else return null;
	}

	public static Object cloneValue(Object value) {
		if (value == null) {
			return null;
		}

		if (value instanceof Integer || value instanceof Float) {
			return value;
		} else if (value instanceof Vec3) {
			return new Vec3((Vec3) value);
		} else if (value instanceof Quat) {
			return new Quat((Quat) value);
		} else {
			throw new IllegalStateException(value.getClass().getName());
		}
	}

	public static Object cloneValueAsEmpty(Object value) {
		if (value == null) {
			return null;
		}

		if (value instanceof Integer) {
			return 0;
		} else if (value instanceof Float) {
			return 0f;
		} else if (value instanceof Vec3) {
			return new Vec3();
		} else if (value instanceof Quat) {
			return new Quat();
		} else {
			throw new IllegalStateException(value.getClass().getName());
		}
	}

	public static AnimFlag<?> buildEmptyFrom(AnimFlag<?> af) {
		if (af instanceof IntAnimFlag) {
			return new IntAnimFlag((IntAnimFlag) af);
		} else if (af instanceof FloatAnimFlag) {
			return new FloatAnimFlag((FloatAnimFlag) af);
		} else if (af instanceof Vec3AnimFlag) {
			return new Vec3AnimFlag((Vec3AnimFlag) af);
		} else if (af instanceof QuatAnimFlag) {
			return new QuatAnimFlag((QuatAnimFlag) af);
		} else return null;
	}

	public AnimFlag(AnimFlag<T> af) {
		name = af.name;
		globalSeqLength = af.globalSeqLength;
		globalSeqId = af.globalSeqId;
		hasGlobalSeq = af.hasGlobalSeq;
		interpolationType = af.interpolationType;
		typeid = af.typeid;
		for (Integer time : af.getEntryMap().keySet()) {
			entryMap.put(time, new Entry<>(af.getEntryMap().get(time)));
		}
		vectorSize = af.vectorSize;
		isFloat = af.isFloat;
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

	protected void setVectorSize(Object value) {
		if (value instanceof float[]) {
			vectorSize = ((float[]) value).length;
		} else if (value instanceof Vec3) {
			vectorSize = 3;
		} else if (value instanceof Vec4) {
			vectorSize = 4;
		} else if (value.getClass().getName().equals("java.lang.Float") || value.getClass().getName().equals("java.lang.Integer")) {
			vectorSize = 1;
		} else {
			isFloat = false;
			vectorSize = ((long[]) value).length;
		}
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

	public void addEntry(Integer time, T value) {
		entryMap.put(time, new Entry<>(time, value));
	}

	public void addEntry(Integer time, T value, T inTan, T outTan) {
		entryMap.put(time, new Entry<>(time, value, inTan, outTan));
	}

	public void addEntry(Entry<T> entry) {
		entryMap.put(entry.getTime(), new Entry<>(entry));
	}

//	public abstract T cloneValue(T value);

	/**
	 * To set an Entry with an Entry
	 *
	 * @param time  the time of the entry to be changed
	 * @param entry the entry to replace the old entry
	 */
	public void setEntryT(Integer time, Entry<?> entry) {
		if (entry.value instanceof Integer && this instanceof IntAnimFlag) {
			setEntry(time, (Entry<T>) entry);
		} else if (entry.value instanceof Float && this instanceof FloatAnimFlag) {
			setEntry(time, (Entry<T>) entry);
		} else if (entry.value instanceof Vec3 && this instanceof Vec3AnimFlag) {
			setEntry(time, (Entry<T>) entry);
		} else if (entry.value instanceof Quat && this instanceof QuatAnimFlag) {
			setEntry(time, (Entry<T>) entry);
		}
	}

	public void setEntry(Integer time, Entry<T> entry) {


		Entry<T> tEntry = new Entry<>(entry);
		entryMap.put(time, tEntry.setTime(time)); // Todo only replace existing entries?

//		if(entryMap.containsKey(time)){
//			entryMap.put(time, tEntry.setTime(time));
//		}
	}

	public void setOrAddEntryT(Integer time, Entry<?> entry) {
		Entry<T> tEntry = new Entry<>((Entry<T>) entry);
		entryMap.put(time, tEntry.setTime(time));

		if (entry.getValue() instanceof Integer && this instanceof IntAnimFlag) {
			setOrAddEntry(time, (Entry<T>) entry);
		} else if (entry.getValue() instanceof Float && this instanceof FloatAnimFlag) {
			setOrAddEntry(time, (Entry<T>) entry);
		} else if (entry.getValue() instanceof Vec3 && this instanceof Vec3AnimFlag) {
			setOrAddEntry(time, (Entry<T>) entry);
		} else if (entry.getValue() instanceof Quat && this instanceof QuatAnimFlag) {
			setOrAddEntry(time, (Entry<T>) entry);
		}
	}

	public void setOrAddEntry(Integer time, Entry<T> entry) {
		System.out.println("vec3 set entry");
		entryMap.put(time, entry);
	}

	public void setEntry(Entry<T> entry) {
		Entry<T> tEntry = new Entry<>(entry);
		entryMap.put(entry.time, tEntry.setTime(entry.time));

		setEntry(entry.time, entry);
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

	public void setValuesTo(AnimFlag<?> af) {
		if (af instanceof IntAnimFlag && this instanceof IntAnimFlag) {
			((IntAnimFlag) this).setValuesTo((IntAnimFlag) af);
		} else if (af instanceof FloatAnimFlag && this instanceof FloatAnimFlag) {
			((FloatAnimFlag) this).setValuesTo((FloatAnimFlag) af);
		} else if (af instanceof Vec3AnimFlag && this instanceof Vec3AnimFlag) {
			((Vec3AnimFlag) this).setValuesTo((Vec3AnimFlag) af);
		} else if (af instanceof QuatAnimFlag && this instanceof QuatAnimFlag) {
			((QuatAnimFlag) this).setValuesTo((QuatAnimFlag) af);
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
			case "Scaling" -> 1;
			case "Rotation" -> 2;
			case "Translation" -> 3;
			case "TextureID" -> 5; // aflg.title.equals("Visibility") || -- 100.088% visible in UndeadCampaign3D OutTans! Golook!
			case "Color" -> 4;
			default -> 0;
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

	// ToDo fix this!
	public AnimFlag<T> getMostVisible(AnimFlag<T> partner) {
		if (partner != null) {
			if ((typeid == ALPHA) && (partner.typeid == ALPHA)) {
//				List<Integer> atimes = new ArrayList<>(times);
//				List<Integer> btimes = new ArrayList<>(partner.times);
//				List<T> avalues = new ArrayList<>(values);
//				List<T> bvalues = new ArrayList<>(partner.values);
//
//				AnimFlag<T> mostVisible = null;
//				mostVisible = getMostVissibleAnimFlag(atimes, btimes, avalues, bvalues, mostVisible, partner, this);
//				if (mostVisible == null) return null;
//
//				mostVisible = getMostVissibleAnimFlag(btimes, atimes, bvalues, avalues, mostVisible, this, partner);
//				if (mostVisible == null) return null;
//
//				// partner has priority!
//				return Objects.requireNonNullElse(mostVisible, partner);
			} else {
				JOptionPane.showMessageDialog(null,
						"Error: Program attempted to compare visibility with non-visibility animation component." +
								"\nThis... probably means something is horribly wrong. Save your work, if you can.");
			}
		}
		return null;
	}

	// ToDo should probably be protected abstract..?
	private AnimFlag<T> getMostVissibleAnimFlag(List<Integer> atimes, List<Integer> btimes, List<T> avalues, List<T> bvalues, AnimFlag<T> mostVisible, AnimFlag<T> flag1, AnimFlag<T> flag2) {
		for (int i = atimes.size() - 1; i >= 0; i--)
		// count down from top, meaning that removing the current value causes no harm
		{
		}
		return mostVisible;
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
		if (this instanceof IntAnimFlag && source instanceof IntAnimFlag) {
			((IntAnimFlag) this).copyFrom((IntAnimFlag) source, sourceStart, sourceEnd, newStart, newEnd);
		}
		if (this instanceof FloatAnimFlag && source instanceof FloatAnimFlag) {
			((FloatAnimFlag) this).copyFrom((FloatAnimFlag) source, sourceStart, sourceEnd, newStart, newEnd);
		}
		if (this instanceof Vec3AnimFlag && source instanceof Vec3AnimFlag) {
			((Vec3AnimFlag) this).copyFrom((Vec3AnimFlag) source, sourceStart, sourceEnd, newStart, newEnd);
		}
		if (this instanceof QuatAnimFlag && source instanceof QuatAnimFlag) {
			((QuatAnimFlag) this).copyFrom((QuatAnimFlag) source, sourceStart, sourceEnd, newStart, newEnd);
		}
	}

	public void copyFrom(AnimFlag<?> source) {
		if (this instanceof IntAnimFlag && source instanceof IntAnimFlag) {
			((IntAnimFlag) this).copyFrom((IntAnimFlag) source);
		}
		if (this instanceof FloatAnimFlag && source instanceof FloatAnimFlag) {
			((FloatAnimFlag) this).copyFrom((FloatAnimFlag) source);
		}
		if (this instanceof Vec3AnimFlag && source instanceof Vec3AnimFlag) {
			((Vec3AnimFlag) this).copyFrom((Vec3AnimFlag) source);
		}
		if (this instanceof QuatAnimFlag && source instanceof QuatAnimFlag) {
			((QuatAnimFlag) this).copyFrom((QuatAnimFlag) source);
		}
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

	protected abstract T getInterpolatedValue(Integer floorTime, Integer ceilTime, float timeFactor);

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

	/**
	 * Interpolates at a given time. The lack of generics on this function is
	 * abysmal, but currently this is how the codebase is.
	 */
	public T interpolateAt(final TimeEnvironmentImpl animatedRenderEnvironment) {
		if (entryMap.isEmpty()) {
			return getIdentity(typeid);
		}
		if ((animatedRenderEnvironment == null) || (animatedRenderEnvironment.getCurrentAnimation() == null)) {
			return entryMap.firstEntry().getValue().getValue(); // Correct?
		}
//		TimeBoundProvider animation = animatedRenderEnvironment.getCurrentAnimation();
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
//			if (this instanceof QuatAnimFlag && hasEntryAt(467) && (time == 466 || time == 467 || time == 468)){
//				System.out.println("floorTime: " + floorTime);
//				System.out.println("ceilTime: " + ceilTime);
//				System.out.println(entryMap.get(floorTime).getValue());
//			}
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

//		if (this instanceof QuatAnimFlag && hasEntryAt(467) && (time == 466 || time == 467 || time == 468)){
//			System.out.println("timeFactor: " + timeFactor);
//			System.out.println("floorTime: " + floorTime);
//			System.out.println("ceilTime: " + ceilTime);
//			System.out.println("floorValue: " + valueAt(floorTime));
//			System.out.println("ceilValue: " + valueAt(ceilTime));
//			System.out.println("floorOutTan: " + outTanAt(floorTime));
//			System.out.println("ceilInTan: " + inTanAt(ceilTime));
//			System.out.println(getInterpolatedValue(floorTime, ceilTime, timeFactor));
//		}
		return getInterpolatedValue(floorTime, ceilTime, timeFactor);
	}

	public void removeKeyframe(int trackTime) {
		entryMap.remove(trackTime);
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

	public void addKeyframe(Entry<T> entry) {
		addEntry(entry);
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
