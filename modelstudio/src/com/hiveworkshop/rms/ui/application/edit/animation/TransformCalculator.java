package com.hiveworkshop.rms.ui.application.edit.animation;

import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;

public class TransformCalculator {
	private final TimeEnvironmentImpl timeEnvironment = new TimeEnvironmentImpl();
	private final Mat4 tempMat = new Mat4();
	private final Mat4 locMat = new Mat4();
	private final Mat4 parentMat = new Mat4();
	private final Mat4 worldMat = new Mat4();
	private final Vec3 temp = new Vec3();
	private final Vec3 transl = new Vec3();
	private final Vec3 scale = new Vec3();
	private final Vec3 locScale = new Vec3();
	private final Vec3 worldScale = new Vec3();
	private final Vec3 parentScale = new Vec3();
	private final Vec3 pivot = new Vec3();
	private final Quat rot = new Quat();

	public TransformCalculator(){
	}

	public TransformCalculator setSequence(Sequence sequence){
		timeEnvironment.setSequence(sequence);
		return this;
	}
	public TransformCalculator setTrackTime(int trackTime){
		timeEnvironment.setAnimationTime(trackTime);
		return this;
	}


	private Mat4 computeWorldMat(IdObject idObject){
		if(idObject == null){
			worldScale.set(Vec3.ZERO);
			return worldMat.fromRotationTranslationScale(Quat.IDENTITY, Vec3.ZERO, Vec3.ONE);
		}
		parentMat.set(computeWorldMat(idObject.getParent()));

		Mat4 localMat = getLocalMatrix(idObject);
		worldScale.multiply(locScale);
		return worldMat.set(parentMat).mul(localMat);
	}

	private Mat4 getLocalMatrix(IdObject idObject){

		Vec3 pivot = idObject.getPivotPoint();

		Vec3 localTrans = getLocalTrans(idObject);

		Quat localRotation = getLocalRot(idObject);

		Vec3 computedScaling = getLocalScale(idObject);
		locScale.set(computedScaling);

//		if (idObject.getParent() != null){
//			Vec3 parentScale = getParentScale(idObject);
//			if (idObject.getDontInheritScaling()){
//				computedScaling.divide(parentScale);
//			} else {
//				worldScale.multiply()
//			}
//		}
		if (idObject.getDontInheritScaling() && idObject.getParent() != null){
			Vec3 parentScale = getParentScale(idObject);
			computedScaling.divide(parentScale);
			worldScale.set(Vec3.ZERO);
		}

		return locMat.fromRotationTranslationScaleOrigin(localRotation, localTrans, computedScaling, pivot);
	}

	private Vec3 computeWorldScale(IdObject idObject){
		if(idObject == null){
			return new Vec3(Vec3.ONE);
		}

		Vec3 localScale = idObject.getRenderScale(timeEnvironment);
		if(localScale == null) localScale = new Vec3(1,1,1);
		if (idObject.getDontInheritScaling()){
			return localScale;
		}
		Vec3 parentScale = computeWorldScale(idObject.getParent());
		if(parentScale == null) parentScale = new Vec3(1,1,1);

		return localScale.multiply(parentScale);
	}



	public GlobTransContainer getGlobalTransform2(IdObject idObject, Sequence sequence, int trackTime){
		TimeEnvironmentImpl timeEnvironment = new TimeEnvironmentImpl();
		timeEnvironment.setSequence(sequence);
		timeEnvironment.setAnimationTime(trackTime);
		Mat4 worldMat = computeWorldMat(idObject);
		Vec3 worldScale = computeWorldScale(idObject);
		return new GlobTransContainer(idObject.getPivotPoint(), worldScale, worldMat);
	}

	public GlobTransContainer getGlobalTransform(IdObject idObject){
		Mat4 worldMat = computeWorldMat(idObject);
		Vec3 worldScale = computeWorldScale(idObject);
//		return new GlobTransContainer(idObject.getPivotPoint(), worldScale, worldMat);
		if(idObject == null){
			return new GlobTransContainer().setPivot(Vec3.ZERO).setScale(worldScale).setMat4(worldMat);
		}
		return new GlobTransContainer().setPivot(idObject.getPivotPoint()).setScale(worldScale).setMat4(worldMat);
	}




	public TransformContainer getNewTransforms(IdObject idObject, Vec3 targetWorldScale, Mat4 targetWorldMatrix, Vec3 targetPivot){
		Vec3 parWorldScale = computeWorldScale(idObject.getParent());
		Mat4 parentWorldMatrix = computeWorldMat(idObject.getParent());

		return calcLocRotScale(idObject, parWorldScale, parentWorldMatrix, targetWorldScale, targetWorldMatrix, targetPivot);
	}

	public TransformContainer getNewTransforms(IdObject idObject, GlobTransContainer targetTransforms){
		Vec3 parWorldScale = computeWorldScale(idObject.getParent());
		Mat4 parentWorldMatrix = computeWorldMat(idObject.getParent());

		return calcLocRotScale(idObject, parWorldScale, parentWorldMatrix, targetTransforms.getScale(), targetTransforms.getMat4(), targetTransforms.getPivot());
	}

	public TransformContainer getNewTransforms(IdObject idObject, GlobTransContainer targetTransforms, Sequence sequence, int trackTime){
		TimeEnvironmentImpl timeEnvironment = new TimeEnvironmentImpl();
		timeEnvironment.setSequence(sequence);
		timeEnvironment.setAnimationTime(trackTime);

		Vec3 parWorldScale = computeWorldScale(idObject.getParent());
		Mat4 parentWorldMatrix = computeWorldMat(idObject.getParent());

		return calcLocRotScale(idObject, parWorldScale, parentWorldMatrix, targetTransforms.getScale(), targetTransforms.getMat4(), targetTransforms.pivot);
	}

	private TransformContainer calcLocRotScale(IdObject idObject, Vec3 parWorldScale, Mat4 parentWorldMatrix, Vec3 targetWorldScale, Mat4 targetWorldMatrix, Vec3 targetPivot){
		Mat4 tempMat = new Mat4();
		Mat4 newLocMat = new Mat4();
		Mat4 newWorldMat = new Mat4();

		tempMat.set(parentWorldMatrix).invert().mul(targetWorldMatrix);
		Vec3 trans = new Vec3().setAsLocationFromMat(tempMat, targetPivot).add(targetPivot).sub(idObject.getPivotPoint());

		Vec3 scale = new Vec3(targetWorldScale);
		if(!idObject.getDontInheritScaling()){
			scale.divide(parWorldScale);
		}

		newLocMat.fromRotationTranslationScaleOrigin(Quat.IDENTITY, trans, scale, idObject.getPivotPoint());
		newWorldMat.set(parentWorldMatrix).mul(newLocMat);
		tempMat.set(newWorldMat).invert().mul(targetWorldMatrix);

		Quat rot = new Quat().setFromUnnormalized(tempMat);
//		Quat rot = new Quat().setIdentity();
		return new TransformContainer(trans, scale, rot);
	}

	private Vec3 getParentScale(IdObject idObject) {
		Vec3 scale = idObject.getParent().getRenderScale(timeEnvironment);
		if(scale == null) {
			return parentScale.set(Vec3.ONE);
		} else {
			return parentScale.set(scale);
		}
	}

	private Vec3 getLocalTrans(IdObject idObject){
		Vec3 localLocation = idObject.getRenderTranslation(timeEnvironment);
		if (localLocation == null) {
			return transl.set(Vec3.ZERO);
		} else {
			return transl.set(localLocation);
		}
	}
	private Vec3 getLocalScale(IdObject idObject){
		Vec3 localScale = idObject.getRenderScale(timeEnvironment);
		if (localScale == null) {
			return scale.set(Vec3.ONE);
		} else {
			return scale.set(localScale);
		}
	}
	private Quat getLocalRot(IdObject idObject){
		Quat localRot = idObject.getRenderRotation(timeEnvironment);
		if (localRot == null) {
			return rot.setIdentity();
		} else {
			return rot.set(localRot);
		}
	}

	public static class TransformContainer {
		private final Vec3 pivot;
		private final Vec3 trans;
		private final Vec3 scale;
		private final Quat rot;

		TransformContainer(Vec3 trans, Vec3 scale, Quat rot) {
			this.pivot = new Vec3();
			this.trans = trans;
			this.scale = scale;
			this.rot = rot;
		}
		TransformContainer(Vec3 pivot, Vec3 trans, Vec3 scale, Quat rot) {
			this.pivot = pivot;
			this.trans = trans;
			this.scale = scale;
			this.rot = rot;
		}

		public Vec3 getPivot() {
			return pivot;
		}

		public Vec3 getTrans() {
			return trans;
		}

		public Vec3 getScale() {
			return scale;
		}

		public Quat getRot() {
			return rot;
		}
	}

	public static class GlobTransContainer {
		private final Vec3 scale;
		private final Vec3 pivot;
		private final Mat4 mat4;

		GlobTransContainer() {
			this.pivot = new Vec3();
			this.scale = new Vec3(1,1,1);
			this.mat4 = new Mat4().setIdentity();
		}
		GlobTransContainer(Vec3 pivot, Vec3 scale, Mat4 mat4) {
			this.pivot = pivot;
			this.scale = scale;
			this.mat4 = mat4;
		}

		public Vec3 getScale() {
			return scale;
		}

		public Mat4 getMat4() {
			return mat4;
		}

		public Vec3 getPivot() {
			return pivot;
		}

		public GlobTransContainer setScale(Vec3 s) {
			scale.set(s);
			return this;
		}

		public GlobTransContainer setPivot(Vec3 p) {
			pivot.set(p);
			return this;
		}

		public GlobTransContainer setMat4(Mat4 m) {
			mat4.set(m);
			return this;
		}
	}
}
