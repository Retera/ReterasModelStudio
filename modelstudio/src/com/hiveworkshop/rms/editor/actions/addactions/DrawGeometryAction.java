package com.hiveworkshop.rms.editor.actions.addactions;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.editor.AbstractTransformAction;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec3;

import java.util.*;

public class DrawGeometryAction extends AbstractTransformAction {
	private final ModelStructureChangeListener changeListener;
	private final UndoAction setupAction;
	private final Vec3 startPoint = new Vec3();
	private final List<GeosetVertex> vertices = new ArrayList<>();
	private final Set<Triangle> triangles = new HashSet<>();
	private final List<Vec3> orgVertPos = new ArrayList<>();
	private final List<Vec3> orgVertNorm = new ArrayList<>();
	private final Mat4 invRotMat = new Mat4();
	private final Vec3 scale = new Vec3();
	private final Vec3 normalScale = new Vec3();
	private boolean flippedNormals = false;
	private final String actionName;

	public DrawGeometryAction(String actionName,
	                          Vec3 startPoint,
	                          Mat4 rotMat,
	                          Collection<GeosetVertex> vertices,
	                          UndoAction setupAction, ModelStructureChangeListener changeListener) {
		this.changeListener = changeListener;
		this.actionName = actionName;
		this.startPoint.set(startPoint);
		this.invRotMat.set(rotMat).invert();
		for(GeosetVertex vertex : vertices){
			this.vertices.add(vertex);
			this.orgVertPos.add(new Vec3(vertex));
			this.orgVertNorm.add(new Vec3(vertex.getNormal()));
			this.triangles.addAll(vertex.getTriangles());
		}
		this.setupAction = setupAction;

	}

	public DrawGeometryAction doSetup(){
		if(setupAction != null){
			setupAction.redo();
		}
		return this;
	}

	@Override
	public UndoAction undo() {
		if(setupAction != null){
			setupAction.undo();
		}
		if(changeListener != null){
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		if(setupAction != null){
			setupAction.redo();
		}
		if(changeListener != null){
			changeListener.geosetsUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return actionName;
	}


	@Override
	public DrawGeometryAction updateTranslation(double deltaX, double deltaY, double deltaZ) {
		startPoint.translate(deltaX, deltaY, deltaZ);
		return updateTransform();
	}
	public DrawGeometryAction setTranslation(Vec3 delta) {
		startPoint.set(delta);
		return updateTransform();
	}

	@Override
	public DrawGeometryAction updateTranslation(Vec3 delta) {
		startPoint.add(delta);
		return updateTransform();
	}

	public DrawGeometryAction setScale(Vec3 scale) {
		this.scale.set(scale);
		return updateTransform();
	}


	protected DrawGeometryAction updateTransform() {
		normalScale.set(Math.copySign(1, scale.x), Math.copySign(1, scale.y), Math.copySign(1, scale.z));
		for (int i = 0; i<vertices.size(); i++) {
			vertices.get(i).set(orgVertPos.get(i)).scale(Vec3.ZERO, scale).transform(invRotMat).add(startPoint);
			vertices.get(i).getNormal().set(orgVertNorm.get(i)).multiply(normalScale).transform(invRotMat);

		}

		boolean invNormals = (Math.copySign(1, scale.x) * Math.copySign(1, scale.y) * Math.copySign(1, scale.z)) < 0;
		if(invNormals != flippedNormals){
			for(Triangle triangle : triangles){
				triangle.flip(false);
			}
		}
		flippedNormals = invNormals;
		return this;
	}
}
