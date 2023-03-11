package com.hiveworkshop.rms.editor.actions.nodes;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.QuatAnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.parsers.mdlx.InterpolationType;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.TransformCalculator;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;

import java.util.List;
import java.util.TreeMap;

public class BakeAndRebindAction implements UndoAction {
	private final TreeMap<Integer, Vec3> diffTransKF = new TreeMap<>();
	private final TreeMap<Integer, Vec3> diffScaleKF = new TreeMap<>();
	private final TreeMap<Integer, Quat> diffRotKF = new TreeMap<>();

	private final IdObject objToRebind;
	private final IdObject oldParent;
	private final IdObject newParent;

	private final AnimFlag<Vec3> orgTranslationFlag;
	private final AnimFlag<Vec3> orgScalingFlag;
	private final AnimFlag<Quat> orgRotationFlag;

	private final AnimFlag<Vec3> newTranslationFlag;
	private final AnimFlag<Vec3> newScalingFlag;
	private final AnimFlag<Quat> newRotationFlag;


	public BakeAndRebindAction(IdObject objToRebind, IdObject newParent, ModelHandler modelHandler) {
		this(objToRebind, newParent, modelHandler.getModel().getAnims(), modelHandler);
	}


	public BakeAndRebindAction(IdObject objToRebind, IdObject newParent, List<Animation> anims, ModelHandler modelHandler) {
		this.objToRebind = objToRebind;
		this.oldParent = objToRebind.getParent();
		this.newParent = newParent;

		orgTranslationFlag = objToRebind.getTranslationFlag();
		orgScalingFlag = objToRebind.getScalingFlag();
		orgRotationFlag = objToRebind.getRotationFlag();

		newScalingFlag = new Vec3AnimFlag(MdlUtils.TOKEN_SCALING, InterpolationType.LINEAR, null);
		newTranslationFlag = new Vec3AnimFlag(MdlUtils.TOKEN_TRANSLATION, InterpolationType.LINEAR, null);
		newRotationFlag = new QuatAnimFlag(MdlUtils.TOKEN_ROTATION, InterpolationType.LINEAR, null);


		calculateNewTransforms(anims);

	}

	private void calculateNewTransforms(List<Animation> anims) {

		for (Animation animation : anims) {
			System.out.println("Processing animation: " + animation.getName() + " (" + animation.getLength() + ")");

			diffRotKF.clear();
			diffTransKF.clear();
			diffScaleKF.clear();

			calculateAnimValues(animation);
			addKeyframes2(animation);

		}
	}

	private void calculateAnimValues(Animation animation) {
		Mat4 tempMat = new Mat4();
		Mat4 newLocMat = new Mat4();
		Mat4 newWorldMat = new Mat4();

		Vec3 pivotPoint = objToRebind.getPivotPoint();
		TransformCalculator transformCalculator = new TransformCalculator().setSequence(animation);

		for (int i = 0; i <= animation.getLength(); i++) {
			if (i != 0 && i % 1000 == 0) {
				System.out.println("\tKF: " + i);
			}
			transformCalculator.setTrackTime(i);

			TransformCalculator.GlobTransContainer newParentTrans = transformCalculator.getGlobalTransform(newParent);
			TransformCalculator.GlobTransContainer objTrans = transformCalculator.getGlobalTransform(objToRebind);

			tempMat.set(newParentTrans.getMat4()).invert().mul(objTrans.getMat4());
			Vec3 trans = new Vec3().setAsLocationFromMat(tempMat, pivotPoint);
			diffTransKF.put(i, trans);

			Vec3 scale = new Vec3(objTrans.getScale());
			if(!objToRebind.getDontInheritScaling()){
				scale.divide(newParentTrans.getScale());
			}
			diffScaleKF.put(i, scale);

			newLocMat.fromRotationTranslationScaleOrigin(Quat.IDENTITY, trans, scale, pivotPoint);
			newWorldMat.set(newParentTrans.getMat4()).mul(newLocMat);
			tempMat.set(newWorldMat).invert().mul(objTrans.getMat4());

			Quat rot = new Quat().setFromUnnormalized(tempMat);
			diffRotKF.put(i, rot);
		}
	}

	private void addKeyframes2(Animation animation) {
		addRotationKeyframes2(animation);
		addTranslationKeyframes(animation);
		addScalingKeyframes(animation);

	}

	private void addScalingKeyframes(Animation animation) {
		Integer lastKeyScale = null;
		for(Integer i : diffScaleKF.keySet()){
			Vec3 scale = diffScaleKF.get(i);
			if(scale != null){
				boolean shouldAddScale = isShouldAddScale(diffScaleKF.higherKey(i), lastKeyScale, i, scale);
				if (shouldAddScale){
					newScalingFlag.addEntry(i, scale, animation);
					lastKeyScale = i;
				}
			}
		}
	}

	private boolean isShouldAddScale(Integer nextKey, Integer lastKey, Integer i, Vec3 scale) {
		if(lastKey == null || nextKey == null){
			return true;
		}
		Vec3 lastScale = diffScaleKF.get(lastKey);
		Vec3 nextScale = diffScaleKF.get(nextKey);
		if(lastScale == null || nextScale == null){
			return true;
		}
		if(!lastScale.equalLocs(scale) || !nextScale.equalLocs(scale)){
			float t = getTimeFactor(lastKey, i, nextKey);
			return shouldAddVec3(lastScale, scale, t, nextScale, 0.0001);
		}
		return false;
	}


	private void addTranslationKeyframes(Animation animation) {
		Integer lastKey = null;
		for(Integer i : diffTransKF.keySet()){
			Vec3 trans = diffTransKF.get(i);
			if(trans != null){
				boolean shouldAddTrans = isShouldAddTrans(lastKey, i, diffTransKF.higherKey(i), trans);
				if(shouldAddTrans){
					newTranslationFlag.addEntry(i, trans, animation);
					lastKey = i;
				}
			}
		}
	}

	private boolean isShouldAddTrans(Integer lastKey, Integer currKey, Integer nextKey, Vec3 currTrans) {
		if(lastKey == null || nextKey == null){
			return true;
		}
		Vec3 lastTrans =  diffTransKF.get(lastKey);
		Vec3 nextTrans = diffTransKF.get(nextKey);
		if(lastTrans == null || nextTrans == null){
			return true;
		}
		if(!lastTrans.equalLocs(currTrans) || !nextTrans.equalLocs(currTrans)){
			float t = getTimeFactor(lastKey, currKey, nextKey);
			return shouldAddVec3(lastTrans, currTrans, t, nextTrans, 0.0001);
		}
		return false;
	}


	Vec3 diff = new Vec3();
	private boolean shouldAddVec3(Vec3 lastValue, Vec3 currValue, float t, Vec3 nextValue, double v) {
		diff.set(lastValue).lerp(nextValue, t).sub(currValue);

		return Math.abs(diff.x) > v || Math.abs(diff.y) > v || Math.abs(diff.z) > v;
	}

	private void addRotationKeyframes2(Animation animation) {
		Integer lastKeyRot = null;
		for(Integer i : diffRotKF.keySet()){
			Quat quat = diffRotKF.get(i);
			if(quat != null){
				boolean shouldAddQuat = isShouldAddQuat2(diffRotKF.higherKey(i), lastKeyRot, i, quat);
				if(shouldAddQuat){
					newRotationFlag.addEntry(i, quat, animation);
					lastKeyRot = i;
				}
			}
		}
	}

	private boolean isShouldAddQuat2(Integer nextKey, Integer lastKey, Integer i, Quat quat) {
		if(lastKey == null || nextKey == null){
			return true;
		}
		Quat lastRot = diffRotKF.get(lastKey);
		Quat nextRot = diffRotKF.get(nextKey);
		if (lastRot == null || nextRot == null) {
			return true;
		}
		float t = getTimeFactor(lastKey, i, nextKey);
		return shouldAddQuat(lastRot, quat, t, nextRot);
	}

	private boolean shouldAddQuat(Quat lastRot, Quat quat, float t, Quat nextRot) {
		Quat interp = Quat.getSlerped(lastRot, nextRot, t);

		return     0.05f < getRotAngDiff(1, 0, 0, quat, interp)
				|| 0.05f < getRotAngDiff(0, 1, 0, quat, interp)
				|| 0.05f < getRotAngDiff(0, 0, 1, quat, interp);

	}

	Vec3 temp1 = new Vec3();
	Vec3 temp2 = new Vec3();
	private float getRotAngDiff(float x, float y, float z, Quat quat, Quat interp) {
		temp1.set(x,y,z).transform(interp);
		temp2.set(x,y,z).transform(quat);
		return (float) temp1.degAngleTo(temp2);
	}

	private float getTimeFactor(Integer lastKey, Integer currKey, Integer nextKey) {
		return (currKey - lastKey)/(float)(nextKey - lastKey);
	}


	@Override
	public UndoAction undo() {
		objToRebind.setParent(oldParent);
		if(orgTranslationFlag != null){
			objToRebind.add(orgTranslationFlag);
		} else {
			objToRebind.remove(newTranslationFlag);
		}
		if(orgScalingFlag != null){
			objToRebind.add(orgScalingFlag);
		} else {
			objToRebind.remove(newScalingFlag);
		}
		if(orgRotationFlag != null){
			objToRebind.add(orgRotationFlag);
		} else {
			objToRebind.remove(newRotationFlag);
		}

		ModelStructureChangeListener.changeListener.nodesUpdated();
		return this;
	}

	@Override
	public UndoAction redo() {
		objToRebind.setParent(newParent);
		objToRebind.add(newTranslationFlag);
		objToRebind.add(newScalingFlag);
		objToRebind.add(newRotationFlag);

		ModelStructureChangeListener.changeListener.nodesUpdated();
		return this;
	}


	@Override
	public String actionName() {
		return "Baked and changed Parent";
	}
}
