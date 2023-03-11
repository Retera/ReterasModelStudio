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
	private final float valueDiff;

	public SimplifyKeyframesFlagAction(AnimFlag<T> animFlag, Collection<Sequence> sequences, float valueDiff, boolean allowRemovePeaks) {
		this.animFlag = animFlag;
		this.allowRemovePeaks = allowRemovePeaks;
		this.valueDiff = valueDiff;
		if (sequences != null){
			findFramesToRemove(sequences);
		} else {
			findFramesToRemove(animFlag.getAnimMap().keySet());
		}
	}

	public int getNumberOfEntriesToRemove(){
		int found = 0;
		for(Set<Entry<T>> entrySet: sequenceSetMap.values()){
			found += entrySet.size();
		}
		return found;
	}

	private void findFramesToRemove(Collection<Sequence> sequences) {
		for (Sequence sequence : sequences) {
			Set<Entry<T>> entrySet = new HashSet<>();

			TreeMap<Integer, Entry<T>> entryMap = animFlag.getEntryMap(sequence);
			if(entryMap != null && !entryMap.isEmpty()){
				TreeMap<Integer, Entry<T>> entryMapCopy = new TreeMap<>(entryMap);

				entrySet.addAll(collectEntriesToRemove2(entryMapCopy, true));

				if(allowRemovePeaks){
					entrySet.addAll(collectEntriesToRemove2(entryMapCopy, false));
				}
			}
			if(!entrySet.isEmpty()){
				sequenceSetMap.put(sequence, entrySet);
			}
		}
	}

	private Set<Entry<T>> collectEntriesToRemove2(TreeMap<Integer, Entry<T>> entryMapCopy, boolean sparePeaks) {
		Set<Entry<T>> entrySet = new HashSet<>();
		Integer lastKF = entryMapCopy.lastKey();
		Integer firstKF = entryMapCopy.firstKey();


		for (Integer time = lastKF; time != null && firstKF <= time; time = entryMapCopy.lowerKey(time)) {
			int beforeTime = entryMapCopy.lowerKey(time) == null ? -1 : entryMapCopy.lowerKey(time);
			int afterTime = entryMapCopy.higherKey(time) == null ? -1 : entryMapCopy.higherKey(time);

			float time_factor = (time - beforeTime) / (float) (afterTime - beforeTime);

			Entry<T> entryBefore = entryMapCopy.get(beforeTime);
			Entry<T> entryAfter = entryMapCopy.get(afterTime);
			Entry<T> entryBetween = entryMapCopy.get(time);
			boolean shouldRemoveEntry;
			shouldRemoveEntry = shouldRemoveEntry(animFlag, time_factor, entryBefore, entryAfter, entryBetween, sparePeaks);

			if (shouldRemoveEntry) {
				entrySet.add(entryBetween);
				entryMapCopy.remove(time);
			}
		}
		if(entryMapCopy.size() == 2){
			Entry<T> entryFirst = entryMapCopy.firstEntry().getValue();
			Entry<T> entryLast = entryMapCopy.lastEntry().getValue();
			boolean lastKeyframeIsSame = shouldRemoveEntry(animFlag, 1, entryFirst, entryFirst, entryLast, sparePeaks);
			boolean middleNoDiff = shouldRemoveEntry(animFlag, .5f, entryFirst, entryFirst, entryFirst, sparePeaks);
			if(lastKeyframeIsSame && middleNoDiff) {
				entrySet.add(entryLast);
				entryMapCopy.remove(entryLast.time);
			}
		}
		return entrySet;
	}

	private boolean shouldRemoveEntry(AnimFlag<T> flag, float time_factor,
	                                  Entry<T> entryBefore, Entry<T> entryAfter, Entry<T> entryBetween,
	                                  boolean sparePeaks) {
		if (entryBetween != null && entryBefore != null && entryAfter != null) {
			T value = flag.getInterpolatedValue(entryBefore, entryAfter, time_factor);
			Entry<T> interpolatedValue = entryBetween.deepCopy().setValue(value);

			return withInTolerance(valueDiff, entryBetween.getValueArr(), interpolatedValue.getValueArr())
					&& (!sparePeaks || isBetween(entryBetween.getValueArr(), entryBefore.getValueArr(), entryAfter.getValueArr()));
		}

		return false;
	}


	private boolean isBetween (float[] between, float[] before, float[] after) {
		for(int i = 0; i < between.length; i++){
			if(!MathUtils.isBetweenInc(before[i], after[i], between[i])){
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


	private boolean isToRemove(Vec3 between, Vec3 before, Vec3 after, Vec3 value) {
		if (MathUtils.isBetweenInc(before.x, after.x, between.x)
				&& MathUtils.isBetweenInc(before.y, after.y, between.y)
				&& MathUtils.isBetweenInc(before.z, after.z, between.z)) {

			return Math.abs(between.x - value.x) < valueDiff
					|| Math.abs(between.y - value.y) < valueDiff
					|| Math.abs(between.z - value.z) < valueDiff;
		}
		return false;
	}

	private boolean isToRemove(Quat between, Quat before, Quat after, Quat value) {
		Vec3 betweenEuler = between.toEuler();
		Vec3 beforeEuler = before.toEuler();
		Vec3 afterEuler = after.toEuler();

		if (MathUtils.isBetweenInc(beforeEuler.x, afterEuler.x, betweenEuler.x)
				&& MathUtils.isBetweenInc(beforeEuler.y, afterEuler.y, betweenEuler.y)
				&& MathUtils.isBetweenInc(beforeEuler.z, afterEuler.z, betweenEuler.z)) {

			return Math.abs(between.x - value.x) < valueDiff
					|| Math.abs(between.y - value.y) < valueDiff
					|| Math.abs(between.z - value.z) < valueDiff;
		}
		return false;
	}

	@Override
	public SimplifyKeyframesFlagAction<T> undo() {
		for (Sequence sequence : sequenceSetMap.keySet()) {
			for (Entry<?> entry : sequenceSetMap.get(sequence)) {
				animFlag.setOrAddEntryT(entry.getTime(), entry, sequence);
			}
		}
		return this;
	}

	@Override
	public SimplifyKeyframesFlagAction<T> redo() {
		for (Sequence sequence : sequenceSetMap.keySet()) {
			for (Entry<?> entry : sequenceSetMap.get(sequence)) {
				animFlag.removeKeyframe(entry.getTime(), sequence);
			}
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Simplify Keyframes";
	}
}
