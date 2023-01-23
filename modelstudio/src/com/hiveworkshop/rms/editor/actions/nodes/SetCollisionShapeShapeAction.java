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
	private final Vec3 v1_old;
	private final Vec3 v2_old;
	private Vec3 v1_new;
	private Vec3 v2_new;
	private final double oldBoundsRadius;
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
		} else if (newShape == MdlxCollisionShape.Type.PLANE) {
			toPlane();
		} else if (newShape == MdlxCollisionShape.Type.SPHERE) {
			toSphere();
		} else if (newShape == MdlxCollisionShape.Type.CYLINDER) {
			toCylinder();
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
		if (oldShape == MdlxCollisionShape.Type.PLANE) {
			float newZRad = ((v2_old.x - v1_old.x) + (v2_old.y - v1_old.x))/4f;
			v1_new = new Vec3(v1_old);
			v2_new = new Vec3(v2_old);
			v1_new.z -= newZRad;
			v2_new.z += newZRad;
			newBoundsRadius = 0;
		} else if (oldShape == MdlxCollisionShape.Type.SPHERE) {
			Vec3 temp = makeValid(new Vec3(1,1,1).scale((float) oldBoundsRadius));
			v1_new = new Vec3(v1_old).sub(temp);
			v2_new = new Vec3(v1_old).add(temp);
			newBoundsRadius = 0;
		} else if (oldShape == MdlxCollisionShape.Type.CYLINDER) {
			Vec3 temp = makeValid(new Vec3(1,1,0).scale((float) oldBoundsRadius));
			v1_new = new Vec3(v1_old).sub(temp);
			v2_new = new Vec3(v2_old).add(temp);
			newBoundsRadius = 0;
		}
	}

	private void toPlane() {
		if (oldShape == MdlxCollisionShape.Type.BOX) {
			float newZ = makeValid(getMidPoint(v1_old, v2_old)).z;
			v1_new = new Vec3(v1_old);
			v2_new = new Vec3(v2_old);
			v1_new.z = newZ;
			v2_new.z = newZ;
			newBoundsRadius = 0;
		} else if (oldShape == MdlxCollisionShape.Type.SPHERE) {
			Vec3 temp = makeValid(new Vec3(1,1,0).scale((float) oldBoundsRadius));
			v1_new = new Vec3(v1_old).sub(temp);
			v2_new = new Vec3(v1_old).add(temp);
			newBoundsRadius = 0;
		} else if (oldShape == MdlxCollisionShape.Type.CYLINDER) {
			float newZ = makeValid(getMidPoint(v1_old, v2_old)).z;
			v2_new = makeValid(new Vec3(1,1,0).scale((float) oldBoundsRadius));
			v1_new = new Vec3().sub(v2_new);
			v1_new.z = newZ;
			v2_new.z = newZ;
			newBoundsRadius = 0;
		}
	}

	private void toCylinder() {
		if (oldShape == MdlxCollisionShape.Type.BOX) {
			v1_new = makeValid(getMidPoint(v1_old, v2_old));
			v2_new = new Vec3(v1_new);
			v1_new.z = v1_old.z;
			v2_new.z = v2_old.z;
			newBoundsRadius = getAvgRad(v1_old, v2_old, true);
		} else if (oldShape == MdlxCollisionShape.Type.PLANE) {
			float newZRad = ((v2_old.x - v1_old.x) + (v2_old.y - v1_old.x))/4f;
			v1_new = makeValid(getMidPoint(v1_old, v2_old));
			v2_new = makeValid(getMidPoint(v1_old, v2_old));
			v1_new.z -= newZRad;
			v2_new.z += newZRad;
			newBoundsRadius = newZRad;
		} else if (oldShape == MdlxCollisionShape.Type.SPHERE) {
			Vec3 temp = makeValid(new Vec3(0, 0, oldBoundsRadius));
			v1_new = new Vec3(v1_old).sub(temp);
			v2_new = new Vec3(v1_old).add(temp);
			newBoundsRadius = oldBoundsRadius;
		}
	}

	private void toSphere() {
		if (oldShape == MdlxCollisionShape.Type.BOX) {
			v1_new = makeValid(getMidPoint(v1_old, v2_old));
			newBoundsRadius = getAvgRad(v1_old, v2_old, false);
		} else if (oldShape == MdlxCollisionShape.Type.PLANE) {
			float newZRad = ((v2_old.x - v1_old.x) + (v2_old.y - v1_old.x))/4f;
			v1_new = makeValid(getMidPoint(v1_old, v2_old));
			newBoundsRadius = newZRad;
		} else if (oldShape == MdlxCollisionShape.Type.CYLINDER) {
			v1_new = makeValid(getMidPoint(v1_old, v2_old));
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

	private Vec3 makeValid(Vec3 vec3){
		if (Float.isNaN(vec3.x)){
			vec3.x = 1;
		} else if (Float.isInfinite(vec3.x)) {
			vec3.x = 1000;
		}
		if (Float.isNaN(vec3.y)){
			vec3.y = 1;
		} else if (Float.isInfinite(vec3.y)) {
			vec3.y = 1000;
		}
		if (Float.isNaN(vec3.z)){
			vec3.z = 1;
		} else if (Float.isInfinite(vec3.z)) {
			vec3.z = 1000;
		}
		return vec3;
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