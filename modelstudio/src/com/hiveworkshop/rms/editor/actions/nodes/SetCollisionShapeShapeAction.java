package com.hiveworkshop.rms.editor.actions.nodes;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.CollisionShape;
import com.hiveworkshop.rms.parsers.mdlx.MdlxCollisionShape;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.util.Vec3;

public class SetCollisionShapeShapeAction implements UndoAction {
	private final MdlxCollisionShape.Type oldShape;
	private final MdlxCollisionShape.Type newShape;
	private final CollisionShape collisionShape;
	private Vec3 v1_old;
	private Vec3 v2_old;
	private Vec3 v1_new;
	private Vec3 v2_new;
	private double oldBoundsRadius;
	private double newBoundsRadius;
	private final ModelStructureChangeListener changeListener;

	public SetCollisionShapeShapeAction(MdlxCollisionShape.Type newShape, CollisionShape collisionShape, ModelStructureChangeListener changeListener) {
		this.oldShape = collisionShape.getType();
		this.newShape = newShape;
		this.collisionShape = collisionShape;
		this.changeListener = changeListener;
		v1_old = new Vec3(collisionShape.getVertex(0));
		if(collisionShape.getVertex(1) != null){
			v2_old = new Vec3(collisionShape.getVertex(1));
		} else {
			v2_old = null;
		}
		oldBoundsRadius = collisionShape.getBoundsRadius();


		if (newShape == MdlxCollisionShape.Type.BOX) {
			toBox();
		} else if (newShape == MdlxCollisionShape.Type.CYLINDER) {
			toCylinder();
		} else if (newShape == MdlxCollisionShape.Type.SPHERE) {
			toSphere();
		}

	}

	@Override
	public UndoAction undo() {
		collisionShape.setType(oldShape);
		collisionShape.setVertex(0, v1_old);
		collisionShape.setVertex(1, v2_old);
		collisionShape.setBoundsRadius(oldBoundsRadius);

		if(changeListener != null){
			changeListener.nodesUpdated();
		}
		return this;
	}

	private void toBox() {
		if (oldShape == MdlxCollisionShape.Type.CYLINDER) {
			Vec3 temp = new Vec3(1,1,0).scale((float) oldBoundsRadius);
			v1_new = new Vec3(v1_old).sub(temp);
			v2_new = new Vec3(v2_old).add(temp);
			newBoundsRadius = -99;
		} else if (oldShape == MdlxCollisionShape.Type.SPHERE) {
			Vec3 temp = new Vec3(1,1,1).scale((float) oldBoundsRadius);
			v1_new = new Vec3(v1_old).sub(temp);
			v2_new = new Vec3(v1_old).add(temp);
			newBoundsRadius = -99;
		}
	}

	private void toCylinder() {
		if (oldShape == MdlxCollisionShape.Type.BOX) {
			v1_new = getMidPoint(v1_old, v2_old);
			v2_new = new Vec3(v1_new);
			v1_new.z = v1_old.z;
			v2_new.z = v2_old.z;
			newBoundsRadius = getAvgRad(v1_old, v2_old, true);
		} else if (oldShape == MdlxCollisionShape.Type.SPHERE) {
			Vec3 temp = new Vec3(0, 0, oldBoundsRadius);
			v1_new = new Vec3(v1_old).sub(temp);
			v2_new = new Vec3(v1_old).add(temp);
			newBoundsRadius = oldBoundsRadius;
		}
	}

	private void toSphere() {
		if (oldShape == MdlxCollisionShape.Type.BOX) {
			v1_new = getMidPoint(v1_old, v2_old);
			newBoundsRadius = getAvgRad(v1_old, v2_old, false);
		} else if (oldShape == MdlxCollisionShape.Type.CYLINDER) {
			v1_new = getMidPoint(v1_old, v2_old);
			newBoundsRadius = oldBoundsRadius;
		}
	}

	private Vec3 getMidPoint(Vec3 v1, Vec3 v2) {
		return new Vec3(v1).add(v2).scale(0.5f);
	}

	private double getAvgRad(Vec3 v1, Vec3 v2, boolean xy){
		if (xy) {
			return ((v2.x - v1.x) + (v2.y - v1.y))/4;
		} else {
			return ((v2.x - v1.x) + (v2.y - v1.y) + (v2.z - v1.z))/6;
		}
	}

	@Override
	public UndoAction redo() {
		collisionShape.setType(newShape);
		collisionShape.setVertex(0, v1_new);
		collisionShape.setVertex(1, v2_new);
		collisionShape.setBoundsRadius(newBoundsRadius);

		if(changeListener != null){
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "change shape from " + oldShape + " to " + newShape;
	}
}