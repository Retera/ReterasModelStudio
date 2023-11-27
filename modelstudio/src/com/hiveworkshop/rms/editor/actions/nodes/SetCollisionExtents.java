package com.hiveworkshop.rms.editor.actions.nodes;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.CollisionShape;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.util.Vec3;

public class SetCollisionExtents implements UndoAction {
	private final CollisionShape collisionShape;
	private final Vec3 v1_old;
	private final Vec3 v2_old;
	private final Vec3 v1_new;
	private final Vec3 v2_new;
	private final double oldBoundsRadius;
	private final double newBoundsRadius;
	private final ModelStructureChangeListener changeListener;

	public SetCollisionExtents(CollisionShape collisionShape, double radius, Vec3 v1, Vec3 v2, ModelStructureChangeListener changeListener) {
		this.collisionShape = collisionShape;
		this.changeListener = changeListener;
		v1_old = new Vec3(collisionShape.getVertex(0));
		if(collisionShape.getVertex(1) != null){
			v2_old = new Vec3(collisionShape.getVertex(1));
		} else {
			v2_old = null;
		}
		v1_new = v1;
		v2_new = v2;
		oldBoundsRadius = collisionShape.getBoundsRadius();
		newBoundsRadius = radius;

	}

	@Override
	public SetCollisionExtents undo() {
		collisionShape.getVertex(0).set(v1_old);
		collisionShape.setVertex(1, v2_old);
		collisionShape.setBoundsRadius(oldBoundsRadius);

		if(changeListener != null){
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public SetCollisionExtents redo() {
		collisionShape.getVertex(0).set(v1_new);
		collisionShape.setVertex(1, v2_new);
		collisionShape.setBoundsRadius(newBoundsRadius);

		if(changeListener != null){
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Change Collision Extents";
	}
}