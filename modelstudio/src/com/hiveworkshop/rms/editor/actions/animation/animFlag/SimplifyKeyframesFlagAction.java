package com.hiveworkshop.rms.editor.actions.animation.animFlag;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Bitmap;
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

	float largestDist = 0;
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

				entrySet.addAll(collectEntriesToRemove2(entryMapCopy, valueDiff, true));

				if(allowRemovePeaks){
					entrySet.addAll(collectEntriesToRemove2(entryMapCopy, valueDiff, false));
				}
			}
			if(!entrySet.isEmpty()){
				sequenceSetMap.put(sequence, entrySet);
			}
		}
	}

	private Set<Entry<T>> collectEntriesToRemove2(TreeMap<Integer, Entry<T>> entryMapCopy, float valueDiff, boolean sparePeaks) {
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
			shouldRemoveEntry = shouldRemoveEntry(animFlag, time_factor, entryBefore, entryAfter, entryBetween, valueDiff, sparePeaks);

			if (shouldRemoveEntry) {
				entrySet.add(entryBetween);
				entryMapCopy.remove(time);
			}
		}
		if(entryMapCopy.size() == 2){
			Entry<T> entryFirst = entryMapCopy.firstEntry().getValue();
			Entry<T> entryLast = entryMapCopy.lastEntry().getValue();
			boolean lastKeyframeIsSame = shouldRemoveEntry(animFlag, 1, entryFirst, entryFirst, entryLast, valueDiff, sparePeaks);
			boolean middleNoDiff = shouldRemoveEntry(animFlag, .5f, entryFirst, entryFirst, entryFirst, valueDiff, sparePeaks);
			if(lastKeyframeIsSame && middleNoDiff) {
				entrySet.add(entryLast);
				entryMapCopy.remove(entryLast.time);
			}
		}
		return entrySet;
	}

	private boolean shouldRemoveEntry1(AnimFlag<T> flag, float time_factor,
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
		if(between.length == 4){
			for (int i = 0; i < between.length; i++) {
				if(!MathUtils.isBetweenInc(before[i], after[i], between[i])){
					return false;
				}
			}
		} else {
			for (int i = 0; i < between.length; i++) {
				if(!MathUtils.isBetweenInc(before[i], after[i], between[i])){
					return false;
				}
			}
		}
		return true;
	}
	private boolean isBetween2 (float[] between, float[] before, float[] after) {
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

	private boolean shouldRemoveEntry(AnimFlag<T> flag, float time_factor,
	                                  Entry<T> entryBefore, Entry<T> entryAfter, Entry<T> entryBetween,
	                                  float valueDiff, boolean sparePeaks) {
		if (entryBetween != null && entryBefore != null && entryAfter != null) {
			T orgValue = entryBetween.getValue();
			T interpValue = flag.getInterpolatedValue(entryBefore, entryAfter, time_factor);
			T befValue = entryBefore.getValue();
			T aftValue = entryAfter.getValue();
			if (orgValue instanceof Integer){
				return checkInt((Integer) orgValue, (Integer) interpValue, (Integer) befValue, (Integer) aftValue, valueDiff, sparePeaks);
			} else if (orgValue instanceof Bitmap){
				return checkBitmap((Bitmap) orgValue, (Bitmap) interpValue, (Bitmap) befValue, (Bitmap) aftValue, valueDiff, sparePeaks);
			} else if (orgValue instanceof Float){
				return checkFloat((Float) orgValue, (Float) interpValue, (Float) befValue, (Float) aftValue, valueDiff, sparePeaks);
			} else if (orgValue instanceof Vec3){
				return checkVec3((Vec3) orgValue, (Vec3) interpValue, (Vec3) befValue, (Vec3) aftValue, valueDiff, sparePeaks);
			} else if (orgValue instanceof Quat){
				return checkQuat((Quat) orgValue, (Quat) interpValue, (Quat) befValue, (Quat) aftValue, valueDiff, sparePeaks);
			}
		}

		return false;
	}

	private boolean checkInt(Integer vOrg, Integer vInterp, Integer vBef, Integer vAft, float valueDiff, boolean sparePeaks) {
		return Math.abs(vOrg - vInterp) < valueDiff && (!sparePeaks || MathUtils.isBetweenInc(vBef, vAft, vOrg));
	}

	private boolean checkBitmap(Bitmap vOrg, Bitmap vInterp, Bitmap vBef, Bitmap vAft, float valueDiff, boolean sparePeaks) {
		return Objects.equals(vOrg, vInterp);
	}

	private boolean checkFloat(Float vOrg, Float vInterp, Float vBef, Float vAft, float valueDiff, boolean sparePeaks) {
		return Math.abs(vOrg - vInterp) < valueDiff && (!sparePeaks || MathUtils.isBetweenInc(vBef, vAft, vOrg));
	}

	private boolean checkVec3_2(Vec3 vOrg, Vec3 vInterp, Vec3 vBef, Vec3 vAft, float valueDiff, boolean sparePeaks) {
		return (!sparePeaks
				|| MathUtils.isBetweenInc(vBef.x, vAft.x, vOrg.x)
				&& MathUtils.isBetweenInc(vBef.y, vAft.y, vOrg.y)
				&& MathUtils.isBetweenInc(vBef.z, vAft.z, vOrg.z))
				&& Math.abs(vOrg.x - vInterp.x) < valueDiff
				&& Math.abs(vOrg.y - vInterp.y) < valueDiff
				&& Math.abs(vOrg.z - vInterp.z) < valueDiff;
	}
	private boolean checkVec3(Vec3 vOrg, Vec3 vInterp, Vec3 vBef, Vec3 vAft, float valueDiff, boolean sparePeaks) {
		return (!sparePeaks
				|| MathUtils.isBetweenInc(vBef.x, vAft.x, vOrg.x)
				&& MathUtils.isBetweenInc(vBef.y, vAft.y, vOrg.y)
				&& MathUtils.isBetweenInc(vBef.z, vAft.z, vOrg.z))
				&& vOrg.distance(vInterp) < valueDiff;
	}


	Vec3 gauge1 = new Vec3();
	Vec3 gauge2 = new Vec3();
	private boolean checkQuat(Quat v1, Quat v2) {
		Vec3 compAxis = getCompAxis(v1, v2);
		gauge1.set(compAxis).transform(v1);
		gauge2.set(compAxis).transform(v2);

//		return Math.abs(v1 - v2) < valueDiff;
		return gauge1.distance(gauge2) < valueDiff;
	}

	Vec3 gaugeOrg = new Vec3();
	Vec3 gaugeInterp = new Vec3();
	Vec3 gaugeOrgX = new Vec3();
	Vec3 gaugeInterpX = new Vec3();
	Vec3 gaugeOrgY = new Vec3();
	Vec3 gaugeInterpY = new Vec3();
	Vec3 gaugeBef = new Vec3();
	Vec3 gaugeAft = new Vec3();
	private boolean checkQuat_1(Quat vOrg, Quat vInterp, Quat vBef, Quat vAft, float valueDiff, boolean sparePeaks) {
		Vec3 compAxis = getCompAxis(vOrg, vInterp);
		gaugeOrg.set(compAxis).transform(vOrg);
		gaugeInterp.set(compAxis).transform(vInterp);
		gaugeBef.set(compAxis).transform(vBef);
		gaugeAft.set(compAxis).transform(vAft);

		boolean doRemove = gaugeOrg.distance(gaugeInterp) < valueDiff
				&& (!sparePeaks
				|| MathUtils.isBetweenInc(gaugeBef.x, gaugeAft.x, gaugeOrg.x)
				&& MathUtils.isBetweenInc(gaugeBef.y, gaugeAft.y, gaugeOrg.y)
				&& MathUtils.isBetweenInc(gaugeBef.z, gaugeAft.z, gaugeOrg.z));
		if(doRemove){
			largestDist = Math.max(largestDist, ensurePosW(vOrg).distance(ensurePosW(vInterp)));
		}
		return doRemove;
	}

	Vec3 gaugeBefX = new Vec3();
	Vec3 gaugeAftX = new Vec3();
	Vec3 gaugeBefY = new Vec3();
	Vec3 gaugeAftY = new Vec3();
//	private boolean checkQuat_f2(Quat vOrg, Quat vInterp, Quat vBef, Quat vAft, boolean sparePeaks) {
	private boolean checkQuat(Quat vOrg, Quat vInterp, Quat vBef, Quat vAft, float valueDiff, boolean sparePeaks) {
//		ensurePosW(tempOrg.set(vOrg));
//		ensurePosW(tempInterp.set(vInterp));
//		ensurePosW(tempBef.set(vBef));
//		ensurePosW(tempAft.set(vAft));
		if(valueDiff < ensurePosW(vOrg).distance(ensurePosW(vInterp))){
			return false;
		}
		if(sparePeaks){
			gaugeOrgX.set(Vec3.X_AXIS).transform(vOrg);
			gaugeBefX.set(Vec3.X_AXIS).transform(vBef);
			gaugeAftX.set(Vec3.X_AXIS).transform(vAft);
			if (       !MathUtils.isBetweenInc(gaugeBefX.x, gaugeAftX.x, gaugeOrgX.x)
					|| !MathUtils.isBetweenInc(gaugeBefX.y, gaugeAftX.y, gaugeOrgX.y)
					|| !MathUtils.isBetweenInc(gaugeBefX.z, gaugeAftX.z, gaugeOrgX.z)) {
				return false;
			}
			gaugeOrgY.set(Vec3.Y_AXIS).transform(vOrg);
			gaugeBefY.set(Vec3.Y_AXIS).transform(vOrg);
			gaugeAftY.set(Vec3.Y_AXIS).transform(vOrg);

			if (       !MathUtils.isBetweenInc(gaugeBefY.x, gaugeAftY.x, gaugeOrgY.x)
					|| !MathUtils.isBetweenInc(gaugeBefY.y, gaugeAftY.y, gaugeOrgY.y)
					|| !MathUtils.isBetweenInc(gaugeBefY.z, gaugeAftY.z, gaugeOrgY.z)) {
				return false;
			}
		} else {

			gaugeOrgX.set(Vec3.X_AXIS).transform(vOrg);
			gaugeOrgY.set(Vec3.Y_AXIS).transform(vOrg);
		}

//		boolean proceed = (!sparePeaks
//				|| MathUtils.isBetweenInc(tempBef.x, tempAft.x, tempOrg.x)
//				&& MathUtils.isBetweenInc(tempBef.y, tempAft.y, tempOrg.y)
//				&& MathUtils.isBetweenInc(tempBef.z, tempAft.z, tempOrg.z)
//				&& MathUtils.isBetweenInc(tempBef.w, tempAft.w, tempOrg.w));

//		gaugeOrgX.set(Vec3.X_AXIS).transform(vOrg);
//		gaugeOrgY.set(Vec3.Y_AXIS).transform(vOrg);
		gaugeInterpX.set(Vec3.X_AXIS).transform(vInterp);
		gaugeInterpY.set(Vec3.Y_AXIS).transform(vInterp);

		boolean doRemove = gaugeOrgX.distance(gaugeInterpX) < valueDiff && gaugeOrgY.distance(gaugeInterpY) < valueDiff;

		if(doRemove){
			largestDist = Math.max(largestDist, ensurePosW(vOrg).distance(ensurePosW(vInterp)));
		}
		return doRemove;
	}
	private boolean checkQuat_fastIsh(Quat vOrg, Quat vInterp, Quat vBef, Quat vAft, float valueDiff, boolean sparePeaks) {
//	private boolean checkQuat(Quat vOrg, Quat vInterp, Quat vBef, Quat vAft, boolean sparePeaks) {
		ensurePosW(tempOrg.set(vOrg));
		ensurePosW(tempInterp.set(vInterp));
		ensurePosW(tempBef.set(vBef));
		ensurePosW(tempAft.set(vAft));

		boolean proceed = (!sparePeaks
				|| MathUtils.isBetweenInc(tempBef.x, tempAft.x, tempOrg.x)
				&& MathUtils.isBetweenInc(tempBef.y, tempAft.y, tempOrg.y)
				&& MathUtils.isBetweenInc(tempBef.z, tempAft.z, tempOrg.z)
				&& MathUtils.isBetweenInc(tempBef.w, tempAft.w, tempOrg.w));

		if(proceed){

//			Vec3 compAxis = getCompAxis(vOrg, vInterp);
			gaugeOrgX.set(Vec3.X_AXIS).transform(vOrg);
			gaugeInterpX.set(Vec3.X_AXIS).transform(vInterp);
			gaugeOrgY.set(Vec3.Y_AXIS).transform(vOrg);
			gaugeInterpY.set(Vec3.Y_AXIS).transform(vInterp);

			boolean doRemove = gaugeOrgX.distance(gaugeInterpX) < valueDiff && gaugeOrgY.distance(gaugeInterpY) < valueDiff;

			if(doRemove){
				largestDist = Math.max(largestDist, ensurePosW(vOrg).distance(ensurePosW(vInterp)));
			}
			return doRemove;
		}
		return false;
	}
	private boolean checkQuat__11(Quat vOrg, Quat vInterp, Quat vBef, Quat vAft, float valueDiff, boolean sparePeaks) {
		ensurePosW(tempOrg.set(vOrg));
		ensurePosW(tempInterp.set(vInterp));
		ensurePosW(tempBef.set(vBef));
		ensurePosW(tempAft.set(vAft));

		boolean proceed = (!sparePeaks
				|| MathUtils.isBetweenInc(tempBef.x, tempAft.x, tempOrg.x)
				&& MathUtils.isBetweenInc(tempBef.y, tempAft.y, tempOrg.y)
				&& MathUtils.isBetweenInc(tempBef.z, tempAft.z, tempOrg.z)
				&& MathUtils.isBetweenInc(tempBef.w, tempAft.w, tempOrg.w));

		if(proceed){

			Vec3 compAxis = getCompAxis(vOrg, vInterp);
			gaugeOrg.set(compAxis).transform(vOrg);
//			if(sparePeaks){
//				gaugeBef.set(compAxis).transform(vBef);
//				gaugeAft.set(compAxis).transform(vAft);
//				if (!MathUtils.isBetweenInc(gaugeBef.x, gaugeAft.x, gaugeOrg.x)
//						|| !MathUtils.isBetweenInc(gaugeBef.y, gaugeAft.y, gaugeOrg.y)
//						|| !MathUtils.isBetweenInc(gaugeBef.z, gaugeAft.z, gaugeOrg.z)) {
//					return false;
//				}
//			}

			gaugeInterp.set(compAxis).transform(vInterp);

			return gaugeOrg.distance(gaugeInterp) < valueDiff;
		}
		return false;
	}

	Quat tempOrg = new Quat();
	Quat tempInterp = new Quat();
	Quat tempBef = new Quat();
	Quat tempAft = new Quat();
	private boolean checkQuat_2(Quat vOrg, Quat vInterp, Quat vBef, Quat vAft, float valueDiff, boolean sparePeaks) {

//		Vec3 compAxis = getCompAxis(vOrg, vInterp);
		ensurePosW(tempOrg.set(vOrg));
		ensurePosW(tempInterp.set(vInterp));
		ensurePosW(tempBef.set(vBef));
		ensurePosW(tempAft.set(vAft));

		return (!sparePeaks
				|| MathUtils.isBetweenInc(tempBef.x, tempAft.x, tempOrg.x)
				&& MathUtils.isBetweenInc(tempBef.y, tempAft.y, tempOrg.y)
				&& MathUtils.isBetweenInc(tempBef.z, tempAft.z, tempOrg.z)
				&& MathUtils.isBetweenInc(tempBef.w, tempAft.w, tempOrg.w))
				&& Math.abs(tempOrg.x - tempInterp.x) < valueDiff
				&& Math.abs(tempOrg.y - tempInterp.y) < valueDiff
				&& Math.abs(tempOrg.z - tempInterp.z) < valueDiff
				&& Math.abs(tempOrg.w - tempInterp.w) < valueDiff;
	}


	private Quat ensurePosW(Quat quat){
		if(quat.w < 0){
			quat.x = -quat.x;
			quat.y = -quat.y;
			quat.z = -quat.z;
			quat.w = -quat.w;
		}
		return quat;
	}


	Vec3 tempAxis = new Vec3();
	private Vec3 getCompAxis(Quat quat) {
		tempAxis.setAsAxis(quat);
		if(Math.abs(tempAxis.dot(Vec3.X_AXIS)) < Math.abs(tempAxis.dot(Vec3.Z_AXIS))){
			return tempAxis.cross(Vec3.X_AXIS);
		} else {
			return tempAxis.cross(Vec3.Z_AXIS);
		}
	}

	Vec3 tempAxis1 = new Vec3();
	Vec3 tempAxis2 = new Vec3();
	private Vec3 getCompAxis(Quat q1, Quat q2) {
		tempAxis1.setAsAxis(q1);
		tempAxis2.setAsAxis(q2);
		if (Math.abs(tempAxis1.dot(tempAxis2)) < .9) {
			return tempAxis1.cross(tempAxis2).normalize();
		} else if (Math.abs(tempAxis1.dot(Vec3.X_AXIS)) < .9 && Math.abs(tempAxis2.dot(Vec3.X_AXIS)) < .9) {
			return tempAxis1.cross(Vec3.X_AXIS).normalize();
		} else if (Math.abs(tempAxis1.dot(Vec3.Z_AXIS)) < .9 && Math.abs(tempAxis2.dot(Vec3.Z_AXIS)) < .9) {
			return tempAxis1.cross(Vec3.Z_AXIS).normalize();
		} else {
			return tempAxis1.cross(Vec3.Y_AXIS).normalize();
		}

//		float dotX1 = Math.abs(tempAxis1.dot(Vec3.X_AXIS));
//		float dotY1 = Math.abs(tempAxis1.dot(Vec3.Y_AXIS));
//		float dotZ1 = Math.abs(tempAxis1.dot(Vec3.Z_AXIS));
//
//		float dotX2 = Math.abs(tempAxis2.dot(Vec3.X_AXIS));
//		float dotY2 = Math.abs(tempAxis2.dot(Vec3.Y_AXIS));
//		float dotZ2 = Math.abs(tempAxis2.dot(Vec3.Z_AXIS));
//
//
//		if(dotX2 < dotZ2){
//			return tempAxis2.cross(Vec3.X_AXIS);
//		} else {
//			return tempAxis2.cross(Vec3.Z_AXIS);
//		}
	}
}
