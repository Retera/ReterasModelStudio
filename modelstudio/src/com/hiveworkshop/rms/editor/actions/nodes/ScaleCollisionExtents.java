package com.hiveworkshop.rms.editor.actions.nodes;

import com.hiveworkshop.rms.editor.actions.editor.AbstractTransformAction;
import com.hiveworkshop.rms.editor.model.CollisionShape;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class ScaleCollisionExtents extends AbstractTransformAction {
	private final CollisionShape collisionShape;
	private final CollisionShape old;
	private final List<Vec3> oldVertices;
	private final double oldBoundsRadius;
	private final Vec3 oldPivot;
	private final Vec3 center;
	private final Vec3 scale;
	private final Mat4 invRotMat = new Mat4();
	private final Mat4 rotMat = new Mat4();
	private final boolean scaleTranslation;
	private final ModelStructureChangeListener changeListener;

	public ScaleCollisionExtents(CollisionShape collisionShape, Vec3 center, Mat4 rotMat, Vec3 scale, boolean scaleTranslation, ModelStructureChangeListener changeListener) {
		this.collisionShape = collisionShape;
		this.old = collisionShape.copy();
		this.changeListener = changeListener;
		this.center = center;
		this.scale = scale;
		this.rotMat.set(rotMat);
		this.invRotMat.set(rotMat).invert();
		this.scaleTranslation = scaleTranslation;

		this.oldPivot = new Vec3(collisionShape.getPivotPoint());

		oldVertices = new ArrayList<>();
		for (Vec3 vert : collisionShape.getVertices()) {
			oldVertices.add(new Vec3(vert));
		}
		oldBoundsRadius = collisionShape.getBoundsRadius();

	}

	@Override
	public ScaleCollisionExtents undo() {
		for (int i = 0; i < oldVertices.size(); i++) {
			Vec3 vert = oldVertices.get(i);
			collisionShape.getVertices().get(i).set(vert);
		}
		collisionShape.setBoundsRadius(oldBoundsRadius);

		if(changeListener != null){
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public ScaleCollisionExtents redo() {
		resetScale();

		if(changeListener != null){
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public ScaleCollisionExtents updateScale(Vec3 scale) {
		this.scale.multiply(scale);
		resetScale();
		doScale(center, this.scale);
		return this;
	}
	@Override
	public ScaleCollisionExtents setScale(Vec3 scale) {
		this.scale.set(scale);
		resetScale();
		doScale(center, scale);
		return this;
	}

	private void resetScale(){
		for (int i = 0; i < oldVertices.size(); i++) {
			Vec3 vert = oldVertices.get(i);
			collisionShape.getVertices().get(i).set(vert);
		}
		collisionShape.setBoundsRadius(oldBoundsRadius);
	}

	private void doScale(Vec3 center, Vec3 scale){

		if (scaleTranslation) {
			resetAnimatedTranslations();
			collisionShape.getPivotPoint().set(oldPivot)
					.sub(center)
					.transform(rotMat, 1, true)
					.multiply(scale)
					.transform(invRotMat, 1, true)
					.add(center);

			// todo fix the scaling of animated translations
			scaleAnimatedTranslations(scale);
		}

		List<Vec3> vertices = collisionShape.getVertices();
		for(Vec3 vertex : vertices) {
			vertex
					.add(collisionShape.getPivotPoint())
					.sub(center)
					.transform(rotMat, 1, true)
					.multiply(scale)
					.transform(invRotMat, 1, true)
					.add(center)
					.sub(collisionShape.getPivotPoint());
		}
		if ((scale.x == scale.z) && (scale.y == scale.z)) {
			collisionShape.setBoundsRadius(collisionShape.getBoundsRadius() * scale.x);
		}
	}

	public void resetAnimatedTranslations() {
		AnimFlag<?> animFlagT = collisionShape.find(MdlUtils.TOKEN_TRANSLATION);
		AnimFlag<?> orgAnimFlagT = old.find(MdlUtils.TOKEN_TRANSLATION);
		if (animFlagT instanceof Vec3AnimFlag translation && orgAnimFlagT instanceof Vec3AnimFlag opgTranslation) {
			translation.setSequenceMap(opgTranslation.getAnimMap());
		}
	}

	public void scaleAnimatedTranslations(Vec3 totScale) {
		AnimFlag<?> animFlagT = collisionShape.find(MdlUtils.TOKEN_TRANSLATION);
		if (animFlagT instanceof Vec3AnimFlag translation) {
			for (TreeMap<Integer, Entry<Vec3>> entryMap : translation.getAnimMap().values()) {
				if (entryMap != null) {
					for (Entry<Vec3> entry : entryMap.values()) {
						entry.getValue().multiply(totScale);
						if (translation.tans()) {
							entry.getInTan().multiply(totScale);
							entry.getOutTan().multiply(totScale);
						}
					}
				}
			}
		}
	}

	@Override
	public String actionName() {
		return "Change Collision Extents";
	}
}