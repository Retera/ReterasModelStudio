package com.hiveworkshop.rms.editor.render3d;

import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;

public final class RenderNode2 {
	private final IdObject idObject;

	private boolean dontInheritScaling = false;
	boolean billboarded;
	boolean billboardedX;
	boolean billboardedY;
	boolean billboardedZ;

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

	private final Vec3 renderPivot = new Vec3(0, 0, 0);

	private final Vec3 inverseWorldLocation = new Vec3();
	private final Quat inverseWorldRotation = new Quat();
	private final Vec3 inverseWorldScale = new Vec3();

	private boolean visible;

	private final RenderModel renderModel;

	boolean dirty = false;
	boolean wasDirty = false;

	public RenderNode2(RenderModel renderModel, IdObject idObject) {
		this.renderModel = renderModel;
		this.idObject = idObject;
		renderPivot.set(idObject.getPivotPoint());
	}

	public void refreshFromEditor() {
		dontInheritScaling = idObject.getDontInheritScaling();
		billboarded = idObject.getBillboarded();
		billboardedX = idObject.getBillboardLockX();
		billboardedY = idObject.getBillboardLockY();
		billboardedZ = idObject.getBillboardLockZ();
	}

	public void recalculateTransformation() {
		if (dirty) {
//			dirty = false;
			worldScale.set(localScale);
			worldRotation.set(localRotation);
			if (idObject.getParent() != null) {
				RenderNode2 parentNode = renderModel.getRenderNode(idObject.getParent());
				Vec3 computedScaling = new Vec3();

				if (dontInheritScaling) {
					computedScaling.set(localScale).divide(parentNode.worldScale);
				} else {
					computedScaling.set(localScale);
					worldScale.multiply(parentNode.worldScale);
				}

//				localMatrix.fromRotationTranslationScaleOrigin(localRotation, localLocation, localScale, idObject.getPivotPoint());
				localMatrix.fromRotationTranslationScaleOrigin(localRotation, localLocation, computedScaling, idObject.getPivotPoint());

				worldMatrix.set(parentNode.worldMatrix).mul(localMatrix);

				worldRotation.mul(parentNode.worldRotation);
			} else {

				localMatrix.fromRotationTranslationScaleOrigin(localRotation, localLocation, localScale, idObject.getPivotPoint());
				worldMatrix.set(localMatrix);
			}

			// Inverse world rotation
			inverseWorldRotation.set(worldRotation).invertRotation();

			// Inverse world scale
			inverseWorldScale.set(1, 1, 1).divide(worldScale);

			// World location
			worldLocation.set(worldMatrix.m30, worldMatrix.m31, worldMatrix.m32);

			// Inverse world location
			inverseWorldLocation.set(worldLocation).negate();

			renderPivot.set(idObject.getPivotPoint()).transform(worldMatrix);
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
		IdObject parent = idObject.getParent();
		if (dirty || ((parent != null) && renderModel.getRenderNode(idObject.getParent()).wasDirty)) {
			dirty = true;
			wasDirty = true;
			recalculateTransformation();
		} else {
			wasDirty = false;
		}

		updateChildren();
	}

	public void updateChildren() {
		for (IdObject childNode : idObject.getChildrenNodes()) {
			if (renderModel.getRenderNode(childNode) == null) {
				throw new NullPointerException(
						"Cannot find child \"" + childNode.getName() + "\" of \"" + idObject.getName() + "\"");
			}
			renderModel.getRenderNode(childNode).update();
		}
	}

	public void resetTransformation() {
		localLocation.set(0, 0, 0);
		localRotation.set(0, 0, 0, 1);
		localScale.set(1, 1, 1);
		worldMatrix.setIdentity();

		renderPivot.set(idObject.getPivotPoint());
//		renderPivot.set(idObject.getPivotPoint()).transform(worldMatrix);

		dirty = true;
	}

//	public void setTransformation(final Vec3 location, final Quat rotation, final Vec3 scale) {
//		localLocation.set(location);
//		localRotation.set(rotation);
//		localScale.set(scale);
//
//		dirty = true;
//	}

	public void fetchTransformation(TimeEnvironmentImpl timeEnvironment) {
		setLocation(idObject.getRenderTranslation(timeEnvironment));
		setRotation(idObject.getRenderRotation(timeEnvironment));
		setScale(idObject.getRenderScale(timeEnvironment));
//		localLocation.set(idObject.getRenderTranslation(timeEnvironment));
//		localRotation.set(idObject.getRenderRotation(timeEnvironment));
//		localScale.set(idObject.getRenderScale(timeEnvironment));

		dirty = true;
	}

	public Quat setRotation(Quat rotation) {
		if(rotation == null){
			localRotation.set(0, 0, 0, 1);
		}else {
			localRotation.set(rotation);
		}
//		dirty = true;
		return localRotation;
	}

	public Vec3 setLocation(Vec3 location) {
		if(location == null){
			localLocation.set(0, 0, 0);
		}else {
			localLocation.set(location);
		}
//		dirty = true;
		return localLocation;
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

	public RenderNode2 setDirty(boolean dirty) {
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

	public RenderNode2 setVisible(boolean visible) {
		this.visible = visible;
		return this;
	}

	public Mat4 getWorldMatrix() {
		return worldMatrix;
	}

	public Mat4 getParentWorldMatrix() {
		if (idObject.getParent() != null && renderModel.getRenderNode(idObject.getParent()) != null) {
			return renderModel.getRenderNode(idObject.getParent()).getWorldMatrix();
		}
		return new Mat4().setIdentity();
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
		if (idObject.getParent() != null && renderModel.getRenderNode(idObject.getParent()) != null) {
			return renderModel.getRenderNode(idObject.getParent()).getWorldRotation();
		}
		return new Quat();
	}


	public Vec3 getWorldScale() {
		return worldScale;
	}

	public Vec3 getPivot() {
		if (renderModel.getTimeEnvironment().isLive() || ProgramGlobals.getSelectionItemType() == SelectionItemTypes.ANIMATE) {
			return renderPivot;
		}
		return idObject.getPivotPoint();
	}

	public Vec3 getRenderPivot() {
		return renderPivot;
	}

	public Vec3 getParentRenderPivot() {
		if (idObject.getParent() != null && renderModel.getRenderNode(idObject.getParent()) != null) {
			return renderModel.getRenderNode(idObject.getParent()).getRenderPivot();
		}
		return Vec3.ZERO;
	}
}