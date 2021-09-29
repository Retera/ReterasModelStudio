package com.hiveworkshop.rms.editor.actions.animation.animFlag;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.util.MathUtils;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;

import java.util.*;

public class SimplifyKeyframesFlagAction<T> implements UndoAction {
	private final AnimFlag<T> animFlag;
	private final Map<Sequence, Set<Entry<T>>> sequenceSetMap = new HashMap<>();

	public SimplifyKeyframesFlagAction(AnimFlag<T> animFlag, List<Sequence> sequences, float valueDiff) {
		this.animFlag = animFlag;
		findFramedToRemove(sequences, valueDiff);
	}

	public int getNumberOfEntriesToRemove(){
		int found = 0;
		for(Set<Entry<T>> entrySet: sequenceSetMap.values()){
			found += entrySet.size();
		}
		return found;
	}

	private void findFramedToRemove(List<Sequence> sequences, float valueDiff) {
		for (Sequence sequence : sequences) {
			removeTransitionalKeyframes(valueDiff, sequence);
		}
	}

	public void removeTransitionalKeyframes(float valueDiff, Sequence sequence) {
		Set<Entry<T>> entrySet = sequenceSetMap.computeIfAbsent(sequence, a -> new HashSet<>());

		TreeMap<Integer, Entry<T>> entryMap = animFlag.getEntryMap(sequence);
		if(entryMap != null && !entryMap.isEmpty()){
			TreeMap<Integer, Entry<T>> entryMapCopy = new TreeMap<>(entryMap);
			int end = entryMapCopy.lastEntry().getKey();

			for (Integer time = entryMapCopy.floorKey(end); time != null && time >= entryMapCopy.ceilingKey(0); time = entryMapCopy.lowerKey(time)) {
				int beforeTime = entryMapCopy.lowerKey(time) == null ? -1 : entryMapCopy.lowerKey(time);
				int afterTime = entryMapCopy.higherKey(time) == null ? -1 : entryMapCopy.higherKey(time);

				float time_factor = (time - beforeTime) / (float) (afterTime - beforeTime);

				Entry<T> entryBefore = entryMapCopy.get(beforeTime);
				Entry<T> entryAfter = entryMapCopy.get(afterTime);
				Entry<T> entryBetween = entryMapCopy.get(time);

				Entry<T> entryToRemove = getEntryToRemove(animFlag, valueDiff, time_factor, entryBefore, entryAfter, entryBetween);

				if (entryToRemove != null) {
					entrySet.add(entryToRemove);
					entryMapCopy.remove(time);
				}
			}
		}
	}

	private Entry<T> getEntryToRemove(AnimFlag<T> flag, float valueDiff, float time_factor, Entry<T> entryBefore, Entry<T> entryAfter, Entry<T> entryBetween) {
		if (entryBetween != null && entryBefore != null && entryAfter != null) {
			T value = flag.getInterpolatedValue(entryBefore, entryAfter, time_factor);
			Entry<T> entryAt = entryBetween.deepCopy().setValue(value);
			if (isBetween(entryBetween.getValueArr(), entryBefore.getValueArr(), entryAfter.getValueArr())
					&& withInTolerance(valueDiff, entryBetween.getValueArr(), entryAt.getValueArr())) {
				return entryBetween;
			}
//			if (entryBetween.value instanceof Float
//					&& isToRemove(valueDiff, (float) entryBetween.value, (float) entryBefore.value, (float) entryAfter.value, (float) value)
//					|| entryBetween.value instanceof Vec3
//					&& isToRemove(valueDiff, (Vec3) entryBetween.value, (Vec3) entryBefore.value, (Vec3) entryAfter.value, (Vec3) value)
//					|| entryBetween.value instanceof Quat
//					&& isToRemove(valueDiff, (Quat) entryBetween.value, (Quat) entryBefore.value, (Quat) entryAfter.value, (Quat) value)) {
//				return entryBetween;
//			}
		}

		return null;
	}

	private boolean isToRemove(float valueDiff, float between, float before, float after, float value) {
		if (MathUtils.isBetween2(before, after, between)) {
			return Math.abs(between - value) < valueDiff;
		}
		return false;
	}

	private boolean isBetween (float[] between, float[] before, float[] after) {
		for(int i = 0; i < between.length; i++){
			if(!MathUtils.isBetween2(before[i], after[i], between[i])){
				return false;
			}
		}
		return true;
	}
	private boolean withInTolerance(float valueDiff, float[] between, float[] interpolatedValue) {
		for(int i = 0; i < between.length; i++){
			if(!(Math.abs(between[i] - interpolatedValue[i]) < valueDiff)){
				return false;
			}
		}
		return true;
	}
//	private boolean isToRemove(float valueDiff, float[] between, float[] before, float[] after, float[] interpolatedValue) {
//		if (MathUtils.isBetween2(before.x, after.x, between.x)
//				&& MathUtils.isBetween2(before.y, after.y, between.y)
//				&& MathUtils.isBetween2(before.z, after.z, between.z)) {
//
//			return Math.abs(between.x - interpolatedValue.x) < valueDiff
//					|| Math.abs(between.y - interpolatedValue.y) < valueDiff
//					|| Math.abs(between.z - interpolatedValue.z) < valueDiff;
//		}
//		return false;
//	}


	private boolean isToRemove(float valueDiff, Vec3 between, Vec3 before, Vec3 after, Vec3 value) {
		if (MathUtils.isBetween2(before.x, after.x, between.x)
				&& MathUtils.isBetween2(before.y, after.y, between.y)
				&& MathUtils.isBetween2(before.z, after.z, between.z)) {

			return Math.abs(between.x - value.x) < valueDiff
					|| Math.abs(between.y - value.y) < valueDiff
					|| Math.abs(between.z - value.z) < valueDiff;
		}
		return false;
	}

	private boolean isToRemove(float valueDiff, Quat between, Quat before, Quat after, Quat value) {
		Vec3 betweenEuler = between.toEuler();
		Vec3 beforeEuler = before.toEuler();
		Vec3 afterEuler = after.toEuler();

		if (MathUtils.isBetween2(beforeEuler.x, afterEuler.x, betweenEuler.x)
				&& MathUtils.isBetween2(beforeEuler.y, afterEuler.y, betweenEuler.y)
				&& MathUtils.isBetween2(beforeEuler.z, afterEuler.z, betweenEuler.z)) {

			return Math.abs(between.x - value.x) < valueDiff
					|| Math.abs(between.y - value.y) < valueDiff
					|| Math.abs(between.z - value.z) < valueDiff;
		}
		return false;
	}

	@Override
	public UndoAction undo() {
		for (Sequence sequence : sequenceSetMap.keySet()) {
			for (Entry<?> entry : sequenceSetMap.get(sequence)) {
				animFlag.setOrAddEntryT(entry.getTime(), entry, sequence);
			}
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		for (Sequence sequence : sequenceSetMap.keySet()) {
			for (Entry<?> entry : sequenceSetMap.get(sequence)) {
				animFlag.removeKeyframe(entry.getTime(), sequence);
			}
		}
		return this;
	}

	@Override
	public String actionName() {
		return "simplify keyframes";
	}
}
