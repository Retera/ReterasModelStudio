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
	private final boolean allowRemovePeaks;

	public SimplifyKeyframesFlagAction(AnimFlag<T> animFlag, List<Sequence> sequences, float valueDiff, boolean allowRemovePeaks) {
		this.animFlag = animFlag;
		this.allowRemovePeaks = allowRemovePeaks;
		findFramesToRemove(sequences, valueDiff);

	}

	public int getNumberOfEntriesToRemove(){
		int found = 0;
		for(Set<Entry<T>> entrySet: sequenceSetMap.values()){
			found += entrySet.size();
		}
		return found;
	}

	private void findFramesToRemove(List<Sequence> sequences, float valueDiff) {
		for (Sequence sequence : sequences) {
			Set<Entry<T>> entrySet = sequenceSetMap.computeIfAbsent(sequence, a -> new HashSet<>());

			TreeMap<Integer, Entry<T>> entryMap = animFlag.getEntryMap(sequence);
			if(entryMap != null && !entryMap.isEmpty()){
				TreeMap<Integer, Entry<T>> entryMapCopy = new TreeMap<>(entryMap);
				int end = entryMapCopy.lastEntry().getKey();

				collectEntriesSparePeaks(valueDiff, entrySet, entryMapCopy, end, true);

				if(allowRemovePeaks){
					collectEntriesSparePeaks(valueDiff, entrySet, entryMapCopy, end, false);
				}
			}
		}
	}

	private void collectEntriesSparePeaks(float valueDiff, Set<Entry<T>> entrySet, TreeMap<Integer, Entry<T>> entryMapCopy, int end, boolean sparePeaks) {
		for (Integer time = entryMapCopy.floorKey(end); time != null && time >= entryMapCopy.ceilingKey(0); time = entryMapCopy.lowerKey(time)) {
			int beforeTime = entryMapCopy.lowerKey(time) == null ? -1 : entryMapCopy.lowerKey(time);
			int afterTime = entryMapCopy.higherKey(time) == null ? -1 : entryMapCopy.higherKey(time);

			float time_factor = (time - beforeTime) / (float) (afterTime - beforeTime);

			Entry<T> entryBefore = entryMapCopy.get(beforeTime);
			Entry<T> entryAfter = entryMapCopy.get(afterTime);
			Entry<T> entryBetween = entryMapCopy.get(time);
			boolean shouldRemoveEntry;
			if(sparePeaks){
				shouldRemoveEntry = shouldRemoveEntry(animFlag, valueDiff, time_factor, entryBefore, entryAfter, entryBetween);
			} else {
				shouldRemoveEntry = shouldRemoveEntry2(animFlag, valueDiff, time_factor, entryBefore, entryAfter, entryBetween);
			}

			if (shouldRemoveEntry) {
				entrySet.add(entryBetween);
				entryMapCopy.remove(time);
			}
		}
	}

	private boolean shouldRemoveEntry(AnimFlag<T> flag, float valueDiff, float time_factor, Entry<T> entryBefore, Entry<T> entryAfter, Entry<T> entryBetween) {
		if (entryBetween != null && entryBefore != null && entryAfter != null) {
			T value = flag.getInterpolatedValue(entryBefore, entryAfter, time_factor);
			Entry<T> interpolatedValue = entryBetween.deepCopy().setValue(value);

			return isBetween(entryBetween.getValueArr(), entryBefore.getValueArr(), entryAfter.getValueArr())
					&& withInTolerance(valueDiff, entryBetween.getValueArr(), interpolatedValue.getValueArr());
		}

		return false;
	}

	private boolean shouldRemoveEntry2(AnimFlag<T> flag, float valueDiff, float time_factor, Entry<T> entryBefore, Entry<T> entryAfter, Entry<T> entryBetween) {
		if (entryBetween != null && entryBefore != null && entryAfter != null) {
			T value = flag.getInterpolatedValue(entryBefore, entryAfter, time_factor);
			Entry<T> interpolatedValue = entryBetween.deepCopy().setValue(value);

			return withInTolerance(valueDiff, entryBetween.getValueArr(), interpolatedValue.getValueArr());
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
