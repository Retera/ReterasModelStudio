package com.hiveworkshop.rms.editor.actions.animation.animFlag;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;

import java.util.*;

public class RemoveNoChangeKeyframesFlagAction<T> implements UndoAction {
	private final AnimFlag<T> animFlag;
	private final Set<Sequence> sequencesToClear = new HashSet<>();
	private final float valueDiff;
	private final T noChangeValue;
	private final List<RemoveFlagEntryMapAction<?>> removeFlagEntryMapActions = new ArrayList<>();

	public RemoveNoChangeKeyframesFlagAction(AnimFlag<T> animFlag, Collection<Sequence> sequences, float valueDiff, T noChangeValue) {
		this.animFlag = animFlag;
		this.valueDiff = valueDiff;
		this.noChangeValue = noChangeValue;

		if (sequences != null){
			findFramesToRemove(sequences);
		} else {
			findFramesToRemove(animFlag.getAnimMap().keySet());
		}

		for (Sequence sequence : sequencesToClear) {
			if (animFlag.hasSequence(sequence)) {
				removeFlagEntryMapActions.add(new RemoveFlagEntryMapAction<>(animFlag, sequence, null));
			}
		}
	}

	public int getNumberOfSequencesToClear(){
		return sequencesToClear.size();
	}

	private void findFramesToRemove(Collection<Sequence> sequences) {
		for (Sequence sequence : sequences) {
			TreeMap<Integer, Entry<T>> entryMap = animFlag.getEntryMap(sequence);
			if(entryMap == null || noChangeInSequence(entryMap)){
				sequencesToClear.add(sequence);
			}
		}
	}

	private boolean noChangeInSequence(TreeMap<Integer, Entry<T>> entryMapCopy) {
		boolean ignoreTans = entryMapCopy.size() < 2;

		for (Integer time : entryMapCopy.keySet()) {
			Entry<T> entryBetween = entryMapCopy.get(time);
			if (!shouldRemoveEntry(entryBetween, ignoreTans)) {
				return false;
			}
		}
		return true;
	}


	@Override
	public RemoveNoChangeKeyframesFlagAction<T> undo() {
		for (RemoveFlagEntryMapAction<?> action : removeFlagEntryMapActions) {
			action.undo();
		}
		return this;
	}

	@Override
	public RemoveNoChangeKeyframesFlagAction<T> redo() {
		for (RemoveFlagEntryMapAction<?> action : removeFlagEntryMapActions) {
			action.redo();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Simplify Keyframes";
	}

	private boolean shouldRemoveEntry(Entry<T> entry, boolean ignoreTans) {
		if (entry != null) {
			T value = entry.getValue();
			T inTan = entry.getInTan();
			T outTan = entry.getOutTan();
			if (value instanceof Integer){
				return checkInt((Integer) value, (Integer) noChangeValue) && (ignoreTans || checkInt((Integer) inTan, (Integer) noChangeValue) && checkInt((Integer) outTan, (Integer) noChangeValue));
			} else if (value instanceof Bitmap){
				return checkBitmap((Bitmap) value, (Bitmap) noChangeValue) && (ignoreTans || checkBitmap((Bitmap) inTan, (Bitmap) noChangeValue) && checkBitmap((Bitmap) outTan, (Bitmap) noChangeValue));
			} else if (value instanceof Float){
				return checkFloat((Float) value, (Float) noChangeValue) && (ignoreTans || checkFloat((Float) inTan, (Float) noChangeValue) && checkFloat((Float) outTan, (Float) noChangeValue));
			} else if (value instanceof Vec3){
				return checkVec3((Vec3) value, (Vec3) noChangeValue) && (ignoreTans || checkVec3((Vec3) inTan, (Vec3) noChangeValue) && checkVec3((Vec3) outTan, (Vec3) noChangeValue));
			} else if (value instanceof Quat){
				return checkQuat((Quat) value, (Quat) noChangeValue) && (ignoreTans || checkQuat((Quat) inTan, (Quat) noChangeValue) && checkQuat((Quat) outTan, (Quat) noChangeValue));
			}
		}

		return false;
	}

	private boolean checkObj(Object v1, Object v2) {
		return false;
	}
	private boolean checkBitmap(Bitmap vOrg, Bitmap vNoChange) {
		return Objects.equals(vOrg, vNoChange);
	}

	private boolean checkInt(Integer vOrg, Integer vNoChange) {
		return vOrg == null || Math.abs(vOrg - vNoChange) < valueDiff;
	}

	private boolean checkFloat(Float vOrg, Float vNoChange) {
		return vOrg == null || Math.abs(vOrg - vNoChange) < valueDiff;
	}

	private boolean checkVec3(Vec3 vOrg, Vec3 vNoChange) {
		return vOrg == null || vOrg.distance(vNoChange) < valueDiff;
	}

	Vec3 gaugeOrgX = new Vec3();
	Vec3 gaugeInterpX = new Vec3();
	Vec3 gaugeOrgY = new Vec3();
	Vec3 gaugeInterpY = new Vec3();
	private boolean checkQuat(Quat vOrg, Quat vNoChange) {
		if(vOrg == null){
			return true;
		}
		if(valueDiff < ensurePosW(vOrg).distance(ensurePosW(vNoChange))){
			return false;
		}

		gaugeInterpX.set(Vec3.X_AXIS).transform(vNoChange);
		gaugeInterpY.set(Vec3.Y_AXIS).transform(vNoChange);

		return gaugeOrgX.distance(gaugeInterpX) < valueDiff && gaugeOrgY.distance(gaugeInterpY) < valueDiff;
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
