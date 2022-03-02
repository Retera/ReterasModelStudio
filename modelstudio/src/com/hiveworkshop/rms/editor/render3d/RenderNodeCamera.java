package com.hiveworkshop.rms.editor.render3d;

import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.FloatAnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.IntAnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.QuatAnimFlag;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;

public final class RenderNodeCamera {
	private final Camera camera;

	private boolean dontInheritScaling = false;
	boolean billboarded;
	boolean billboardedX;
	boolean billboardedY;
	boolean billboardedZ;

	private final Vec3 targetLocalLocation = new Vec3(0, 0, 0);
	private final Vec3 localLocation = new Vec3(0, 0, 0);
	private final Vec3 localTranslation = new Vec3(0, 0, 0);
	private final Quat localRotation = new Quat(0, 0, 0, 1);
	private final Vec3 localScale = new Vec3(1, 1, 1);
	private final Mat4 localMatrix = new Mat4();

	private final Vec3 worldLocation = new Vec3();
//	private final Vec3 worldTranslation = new Vec3(0, 0, 0);
	private final Quat worldRotation = new Quat();
	private final Vec3 worldScale = new Vec3(1, 1, 1);
	private final Mat4 worldMatrix = new Mat4();

	private final Vec3 cameraUp = new Vec3(0, 0, 1);
	private final Vec3 renderPivot = new Vec3(0, 0, 0);
	private final Vec3 renderTarget = new Vec3(0, 0, 0);

	private final Vec3 inverseWorldLocation = new Vec3();
	private final Quat inverseWorldRotation = new Quat();
	private final Vec3 inverseWorldScale = new Vec3();

	private boolean visible;

	private final RenderModel renderModel;

	boolean dirty = false;
	boolean wasDirty = false;

	public RenderNodeCamera(RenderModel renderModel, Camera camera) {
		this.renderModel = renderModel;
		this.camera = camera;
		renderPivot.set(camera.getPosition());
	}

	public void refreshFromEditor() {
	}

	public void recalculateTransformation() {
		if (dirty) {
			localMatrix.fromRotationTranslationScaleOrigin(Quat.IDENTITY, targetLocalLocation, localScale, camera.getTargetPosition());
			worldMatrix.setIdentity().mul(localMatrix);
			renderTarget.set(camera.getTargetPosition()).transform(worldMatrix);

//			dirty = false;
			worldScale.set(localScale);
			worldRotation.set(localRotation);
//			worldRotation.setIdentity();
			Vec3 computedScaling = new Vec3();


			computedScaling.set(localScale);

			localMatrix.fromRotationTranslationScaleOrigin(localRotation, localLocation, localScale, camera.getPosition());
//			localMatrix.fromRotationTranslationScaleOrigin(Quat.IDENTITY, localLocation, localScale, camera.getPosition());

			worldMatrix.setIdentity().mul(localMatrix);

			worldRotation.setIdentity();

			// Inverse world rotation
			inverseWorldRotation.set(worldRotation).invertRotation();

			// Inverse world scale
			inverseWorldScale.set(1, 1, 1).divide(worldScale);

			// World location
			worldLocation.set(worldMatrix.m30, worldMatrix.m31, worldMatrix.m32);

			// Inverse world location
			inverseWorldLocation.set(worldLocation).negate();

			renderPivot.set(camera.getPosition()).transform(worldMatrix);

			cameraUp.set(0,0,1).transform(localRotation);
//			if(!worldLocation.equalLocs(renderPivot)){
//				Vec3 diff = new Vec3(worldLocation).sub(renderPivot);
//				System.out.println("WL: " + worldLocation + " != RP: " + renderPivot + " (diff: " + diff + ", piv: " + idObject.getPivotPoint() + ")");
//			}
//			if(!worldRotation.equals(localRotation)){
//				Quat diff = new Quat(worldRotation);
//				diff.sub(localRotation);
//				System.out.println("WL: " + worldRotation + " != RP: " + localRotation + " (diff: " + diff + ", piv: " + idObject.getPivotPoint() + ")");
//			}
		}
	}

	public void update() {
		if (dirty) {
			dirty = true;
			wasDirty = true;
			recalculateTransformation();
		} else {
			wasDirty = false;
		}

		updateChildren();
	}

	public void updateChildren() {
//		for (IdObject childNode : camera.getChildrenNodes()) {
//			if (renderModel.getRenderNode(childNode) == null) {
//				throw new NullPointerException(
//						"Cannot find child \"" + childNode.getName() + "\" of \"" + camera.getName() + "\"");
//			}
//			renderModel.getRenderNode(childNode).update();
//		}
	}

	public void resetTransformation() {
		targetLocalLocation.set(0,0,0);
		localLocation.set(0, 0, 0);
		localRotation.set(0, 0, 0, 1);
		localScale.set(1, 1, 1);
		worldMatrix.setIdentity();

		renderPivot.set(camera.getPosition());
		renderTarget.set(camera.getTargetPosition());
		cameraUp.set(0,0,1);
//		renderPivot.set(idObject.getPivotPoint()).transform(worldMatrix);

		dirty = true;
	}

	public Vec3 getCameraUp(){
		return cameraUp;
	}
//	public void setTransformation(final Vec3 location, final Quat rotation, final Vec3 scale) {
//		localLocation.set(location);
//		localRotation.set(rotation);
//		localScale.set(scale);
//
//		dirty = true;
//	}

	public double getFoV(){
		return camera.getFieldOfView();
	}

	public double getFarClip(){
		return camera.getFarClip();
	}

	public double getNearClip(){
		return camera.getNearClip();
	}

	public void fetchTransformation(TimeEnvironmentImpl timeEnvironment) {
		setLocation(camera.getSourceNode().getRenderTranslation(timeEnvironment));
		setTargetLocation(camera.getTargetNode().getRenderTranslation(timeEnvironment));
//		setRotation(camera.getSourceNode().getRenderRotation(timeEnvironment));

//		setRotation(null);
		setRenderRotation(timeEnvironment);

		dirty = true;
	}

	Vec3 axisHeap = new Vec3();
	private void setRenderRotation(TimeEnvironmentImpl timeEnvironment) {
		AnimFlag<?> rotationFlag = camera.getSourceNode().find(MdlUtils.TOKEN_ROTATION);
		if (rotationFlag instanceof IntAnimFlag) {
			int angle = (Integer) rotationFlag.interpolateAt(timeEnvironment);
			axisHeap.set(camera.getTargetPosition()).add(targetLocalLocation).sub(camera.getPosition()).sub(localLocation);
			localRotation.setFromAxisAngle(axisHeap, angle);
		} else if (rotationFlag instanceof FloatAnimFlag) {
			Float angle = (Float) rotationFlag.interpolateAt(timeEnvironment);
			axisHeap.set(camera.getTargetPosition()).add(targetLocalLocation).sub(camera.getPosition()).sub(localLocation);
			localRotation.setFromAxisAngle(axisHeap, angle);
		} else if (rotationFlag instanceof QuatAnimFlag) {
			setRotation(((QuatAnimFlag)rotationFlag).interpolateAt(timeEnvironment));
		} else {
			axisHeap.set(camera.getTargetPosition()).add(targetLocalLocation).sub(camera.getPosition()).sub(localLocation);
			localRotation.setFromAxisAngle(axisHeap, 0);
//			setRotation(null);
		}
	}

	public Quat setRotation(Quat rotation) {
		if(rotation == null){
			localRotation.set(0, 0, 0, 1);
		} else {
//			localRotation.set(0, 0, 0, 1);
			localRotation.set(rotation);
		}
//		dirty = true;
		return localRotation;
	}

	public Vec3 setLocation(Vec3 location) {
		if(location == null){
			localLocation.set(0, 0, 0);
		}else {
//			localLocation.set(0, 0, 0);
			localLocation.set(location);
		}
//		dirty = true;
		return localLocation;
	}
	public Vec3 setTargetLocation(Vec3 location) {
		if(location == null){
			targetLocalLocation.set(0, 0, 0);
		}else {
//			targetLocalLocation.set(0, 0, 0);
			targetLocalLocation.set(location);
		}
//		dirty = true;
		return targetLocalLocation;
	}
	public Vec3 setTranslation(Vec3 translation) {
		if(translation == null){
			localTranslation.set(0, 0, 0);
		}else {
			localTranslation.set(translation);
		}
//		dirty = true;
		return localTranslation;
	}

	public Vec3 setScale(Vec3 scale) {
		if(scale == null){
			localScale.set(1, 1, 1);
		}else {
			localScale.set(scale);
		}
//		dirty = true;
		return localScale;
	}

	public RenderNodeCamera setDirty(boolean dirty) {
		this.dirty = dirty;
		return this;
	}

	//
//	public void setRotation(Quat rotation) {
//		localRotation.set(rotation);
//		dirty = true;
//	}
//
//	public void setLocation(Vec3 location) {
//		localLocation.set(location);
//		dirty = true;
//	}
//
//	public void setScale(Vec3 scale) {
//		localScale.set(scale);
//		dirty = true;
//	}

	public RenderNodeCamera setVisible(boolean visible) {
		this.visible = visible;
		return this;
	}

	public Mat4 getWorldMatrix() {
		return worldMatrix;
	}

	public Mat4 getParentWorldMatrix() {
		return renderModel.getRootPosition().getWorldMatrix();
	}

	public boolean hasParent() {
		return false;
	}

	/**
	 * Supposedly returns final matrix based on bind pose, but don't actually use
	 * this yet, I'm not even sure it's computed correctly. Graphically, based on my
	 * tests, it looked like maybe we do not need it.
	 */

	public Quat getInverseWorldRotation() {
		return inverseWorldRotation;
	}

	public Vec3 getInverseWorldLocation() {
		return inverseWorldLocation;
	}

	public Vec3 getInverseWorldScale() {
		return inverseWorldScale;
	}

	public Vec3 getWorldLocation() {
		return worldLocation;
	}

	public Vec3 getLocalLocation() {
		return localLocation;
	}

	public Vec3 getLocalScale() {
		return localScale;
	}

	public Mat4 getLocalMatrix() {
		return localMatrix;
	}

	public Quat getLocalRotation() {
		return localRotation;
	}

	public Quat getWorldRotation() {
		return worldRotation;
	}

	public Quat getParentWorldRotation() {
		return renderModel.getRootPosition().getWorldRotation();
	}


	public Vec3 getWorldScale() {
		return worldScale;
	}

	public Vec3 getPivot() {
		if (renderModel.getTimeEnvironment().isLive() || ProgramGlobals.getSelectionItemType() == SelectionItemTypes.ANIMATE) {
			return renderPivot;
		}
		return camera.getPosition();
	}
	public Vec3 getTarget() {
		if (renderModel.getTimeEnvironment().isLive() || ProgramGlobals.getSelectionItemType() == SelectionItemTypes.ANIMATE) {
			return renderTarget;
		}
		return camera.getTargetPosition();
	}

	public Vec3 getRenderPivot() {
		return renderPivot;
	}

	public Vec3 getParentRenderPivot() {
		if (hasParent()) {
			return renderModel.getRootPosition().getRenderPivot();
		}
		return Vec3.ZERO;
	}
}