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

	private void toBox() {
		switch (oldShape) {
			case BOX -> toBox(v1_old, v2_old, new Vec3(0,0,0));
			case SPHERE -> toBox(v1_old, v1_old, new Vec3(1,1,1).scale((float) oldBoundsRadius));
			case PLANE -> toBox(v1_old, v2_old, new Vec3(0,0,1).scale((float) getAvgRad(v1_old, v2_old, true)));
			case CYLINDER -> toBox(v1_old, v2_old, new Vec3(1,1,0).scale((float) oldBoundsRadius));
		}
	}
	private void toBox(Vec3 v1, Vec3 v2, Vec3 temp) {
		makeValid(temp);
		v1_new = new Vec3(v1).sub(temp);
		v2_new = new Vec3(v2).add(temp);
		newBoundsRadius = 0;
	}

	private void toPlane() {
		switch (oldShape) {
			case BOX -> toPlane(v1_old, v2_old);
			case SPHERE -> toPlane(v1_old, v1_old);
			case PLANE -> toPlane(v1_old, v2_old);
			case CYLINDER -> toPlane(v1_old, v2_old);
		}
	}
	private void toPlane(Vec3 v1, Vec3 v2) {
		v1_new = new Vec3(v1.x, v1.y, (v1.z + v2.z)/2f).addScaled(Vec3.XY_AXIS, (float) -oldBoundsRadius);
		v2_new = new Vec3(v2.x, v2.y, (v1.z + v2.z)/2f).addScaled(Vec3.XY_AXIS, (float) oldBoundsRadius);
		newBoundsRadius = 0;
	}
	private void toCylinder() {
		switch (oldShape) {
			case BOX -> toCylinder(getAvgRad(v1_old, v2_old, true), (v2_old.z - v1_old.z)/2.0);
			case SPHERE -> toCylinder(oldBoundsRadius, null);
			case PLANE -> toCylinder(getAvgRad(v1_old, v2_old, true), null);
			case CYLINDER -> toCylinder(oldBoundsRadius, (v2_old.z - v1_old.z)/2.0);
		}
	}
	private void toCylinder(double newBRad, Double newZRad) {
		Vec3 temp = makeValid(new Vec3(0, 0, newZRad == null ? newBRad : newZRad));
		v1_new = makeValid(getMidPoint(v1_old, v2_old)).sub(temp);
		v2_new = makeValid(getMidPoint(v1_old, v2_old)).add(temp);
		newBoundsRadius = newBRad;
	}
	private void toSphere() {
		switch (oldShape) {
			case BOX -> toSphere(getAvgRad(v1_old, v2_old, false));
			case SPHERE -> toSphere(oldBoundsRadius);
			case PLANE -> toSphere(getAvgRad(v1_old, v2_old, true));
			case CYLINDER -> toSphere(oldBoundsRadius);
		}
	}
	private void toSphere(double newRad) {
		v1_new = makeValid(getMidPoint(v1_old, v2_old));
		newBoundsRadius = newRad;
	}

	private Vec3 getMidPoint(Vec3 v1, Vec3 v2) {
		if (v2 == null) {
			return new Vec3(v1);
		} else {
			return new Vec3(v1).add(v2).scale(0.5f);
		}
	}

	private double getAvgRad(Vec3 v1, Vec3 v2, boolean xy){
		if (xy) {
			return ((v2.x - v1.x) + (v2.y - v1.y)) / 4.0;
		} else {
			return ((v2.x - v1.x) + (v2.y - v1.y) + (v2.z - v1.z)) / 6.0;
		}
	}

	private Vec3 makeValid(Vec3 vec3){
		vec3.x = getValidFloat(vec3.x);
		vec3.y = getValidFloat(vec3.y);
		vec3.z = getValidFloat(vec3.z);
		return vec3;
	}

	private float getValidFloat(float v) {
		if (Float.isNaN(v)){
			return 1;
		} else if (Float.isInfinite(v)) {
			return 1000;
		} else {
			return v;
		}
	}

	@Override
	public SetCollisionShapeShapeAction undo() {
		collisionShape.setType(oldShape);
		collisionShape.setVertex(0, v1_old);
		collisionShape.setVertex(1, v2_old);
		collisionShape.setBoundsRadius(oldBoundsRadius);

		if(changeListener != null){
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public SetCollisionShapeShapeAction redo() {
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
		return "Change Shape From " + oldShape + " To " + newShape;
	}
}