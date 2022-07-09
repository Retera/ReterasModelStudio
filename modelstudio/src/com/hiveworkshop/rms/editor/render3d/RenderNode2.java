package com.hiveworkshop.rms.editor.render3d;

import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;

public final class RenderNode2 extends RenderNode<IdObject> {
	private boolean dontInheritScaling = false;
	boolean billboarded;
	boolean billboardedX;
	boolean billboardedY;
	boolean billboardedZ;

	public RenderNode2(RenderModel renderModel, IdObject animatedNode) {
		super(renderModel, animatedNode);
		renderPivot.set(animatedNode.getPivotPoint());
	}

	public void refreshFromEditor() {
		dontInheritScaling = animatedNode.getDontInheritScaling();
		billboarded = animatedNode.getBillboarded();
		billboardedX = animatedNode.getBillboardLockX();
		billboardedY = animatedNode.getBillboardLockY();
		billboardedZ = animatedNode.getBillboardLockZ();
	}

	public void recalculateTransformation() {
		if (dirty) {
//			dirty = false;
			worldScale.set(localScale);
			worldRotation.set(localRotation);
			RenderNode2 parentNode = renderModel.getRenderNode(animatedNode.getParent());
			Vec3 computedScaling = new Vec3();

			if (dontInheritScaling) {
				computedScaling.set(localScale).divide(parentNode.worldScale);
			} else {
				computedScaling.set(localScale);
				worldScale.multiply(parentNode.worldScale);
			}

			localMatrix.fromRotationTranslationScaleOrigin(localRotation, localLocation, computedScaling, animatedNode.getPivotPoint());

			worldMatrix.set(parentNode.worldMatrix).mul(localMatrix);

			worldRotation.mul(parentNode.worldRotation);

			// Inverse world rotation
			inverseWorldRotation.set(worldRotation).invertRotation();

			// Inverse world scale
			inverseWorldScale.set(1, 1, 1).divide(worldScale);

			// World location
			worldLocation.set(worldMatrix.m30, worldMatrix.m31, worldMatrix.m32);

			// Inverse world location
			inverseWorldLocation.set(worldLocation).negate();

			renderPivot.set(animatedNode.getPivotPoint()).transform(worldMatrix);
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
		if (dirty || (renderModel.getRenderNode(animatedNode.getParent()).wasDirty)) {
			dirty = true;
			wasDirty = true;
			recalculateTransformation();
		} else {
			wasDirty = false;
		}

		updateChildren();
	}

	public void updateChildren() {
		for (IdObject childNode : animatedNode.getChildrenNodes()) {
			if (renderModel.getRenderNode(childNode) == null) {
				throw new NullPointerException(
						"Cannot find child \"" + childNode.getName() + "\" of \"" + animatedNode.getName() + "\"");
			}
			renderModel.getRenderNode(childNode).update();
		}
	}

	public void resetTransformation() {
		localLocation.set(0, 0, 0);
		localRotation.set(0, 0, 0, 1);
		localScale.set(1, 1, 1);
		worldMatrix.setIdentity();

		renderPivot.set(animatedNode.getPivotPoint());
//		renderPivot.set(idObject.getPivotPoint()).transform(worldMatrix);

		dirty = true;
	}

	public void fetchTransformation(TimeEnvironmentImpl timeEnvironment) {
		setLocation(animatedNode.getRenderTranslation(timeEnvironment));
		setRotation(animatedNode.getRenderRotation(timeEnvironment));
		setScale(animatedNode.getRenderScale(timeEnvironment));

		dirty = true;
	}

	public Mat4 getParentWorldMatrix() {
		return renderModel.getRenderNode(animatedNode.getParent()).getWorldMatrix();
	}

	public boolean hasParent() {
		return animatedNode.getParent() != null && renderModel.getRenderNode(animatedNode.getParent()) != null;
	}

	/**
	 * Supposedly returns final matrix based on bind pose, but don't actually use
	 * this yet, I'm not even sure it's computed correctly. Graphically, based on my
	 * tests, it looked like maybe we do not need it.
	 */


	public Quat getParentWorldRotation() {
		return renderModel.getRenderNode(animatedNode.getParent()).getWorldRotation();
	}


	public Vec3 getPivot() {
		if (renderModel.getTimeEnvironment().isLive() || ProgramGlobals.getSelectionItemType() == SelectionItemTypes.ANIMATE) {
			return renderPivot;
		}
		return animatedNode.getPivotPoint();
	}

	public Vec3 getParentPivot() {
		if (hasParent()) {
			return renderModel.getRenderNode(animatedNode.getParent()).getPivot();
		}
//		return Vec3.ZERO;
		return null;
	}

	public Vec3 getRenderPivot() {
		return renderPivot;
	}

	public Vec3 getParentRenderPivot() {
		if (hasParent()) {
			return renderModel.getRenderNode(animatedNode.getParent()).getRenderPivot();
		}
		return null;
	}

	public Vec3 getParentWorldScale() {
		if (hasParent()) {
			return renderModel.getRenderNode(animatedNode.getParent()).getWorldScale();
		}
		return Vec3.ONE;
	}
}