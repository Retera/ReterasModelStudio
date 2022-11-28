package com.hiveworkshop.rms.editor.actions.editor;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.util.GenericScaleAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;

public class StaticMeshScaleAction implements GenericScaleAction {
	private final Vec3 center;
	private final Vec3 scale;
	private final ArrayList<GeosetVertex> selectedVertices;
	private final ArrayList<IdObject> selectedIdObjects;
	private final ArrayList<CameraNode> selectedCameraNodes;

	private final ArrayList<Vec3> opgPosVertices;
	private final ArrayList<Vec3> opgPosIdObjects;
	private final ArrayList<Vec3> opgPosCameraNodes;

	public StaticMeshScaleAction(ModelView modelView, Vec3 center) {
		this(modelView, center, new Vec3(1, 1, 1));
	}

	public StaticMeshScaleAction(ModelView modelView, Vec3 center, Vec3 scale) {
		this(modelView.getSelectedVertices(),
				modelView.getSelectedIdObjects(),
				modelView.getSelectedCameraNodes(),
				center, scale);
	}

	public StaticMeshScaleAction(Collection<GeosetVertex> selectedVertices,
	                             Collection<IdObject> selectedIdObjects,
	                             Collection<CameraNode> selectedCameraNodes, Vec3 center) {
		this(selectedVertices, selectedIdObjects, selectedCameraNodes, center, new Vec3(1, 1, 1));
	}

	public StaticMeshScaleAction(Collection<GeosetVertex> selectedVertices,
	                             Collection<IdObject> selectedIdObjects,
	                             Collection<CameraNode> selectedCameraNodes,
	                             Vec3 center,
	                             Vec3 scale) {
		this.center = center;
		this.scale = scale;
		this.selectedVertices = new ArrayList<>(selectedVertices);
		this.selectedIdObjects = new ArrayList<>(selectedIdObjects);
		this.selectedCameraNodes = new ArrayList<>(selectedCameraNodes);

		this.opgPosVertices = new ArrayList<>();
		this.selectedVertices.forEach(v -> opgPosVertices.add(new Vec3(v)));
		this.opgPosIdObjects = new ArrayList<>();
		this.selectedIdObjects.forEach(o -> opgPosIdObjects.add(new Vec3(o.getPivotPoint())));
		this.opgPosCameraNodes = new ArrayList<>();
		this.selectedCameraNodes.forEach(c -> opgPosCameraNodes.add(new Vec3(c.getPosition())));
	}

	@Override
	public UndoAction undo() {
		Vec3 revScale = new Vec3(1, 1, 1).divide(0 < scale.length() ? scale : new Vec3(0.1, 0.1, 0.1).scale(0.0000001f));
		rawScale(center, revScale);

		for (int i = 0; i<selectedVertices.size(); i++) {
			selectedVertices.get(i).set(opgPosVertices.get(i));
		}

		for (int i = 0; i<selectedIdObjects.size(); i++) {
			selectedIdObjects.get(i).setPivotPoint(opgPosIdObjects.get(i));
		}

		for (int i = 0; i<selectedCameraNodes.size(); i++) {
			selectedCameraNodes.get(i).setPosition(opgPosCameraNodes.get(i));
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		rawScale(center, scale);
		return this;
	}

	@Override
	public String actionName() {
		return "scale";
	}

	@Override
	public GenericScaleAction updateScale(Vec3 scale) {
		this.scale.multiply(scale);
		rawScale(center, scale);
		return this;
	}

	public void rawScale(Vec3 center, Vec3 scale) {
		double avgScale = (scale.x + scale.y + scale.z) / 3;
		for (Vec3 vertex : selectedVertices) {
			vertex.scale(center, scale);
		}
		for (IdObject object : selectedIdObjects) {
			object.getPivotPoint().scale(center, scale);
			translateNode(object, scale);
//			if (object instanceof Bone) {
//				translateBone(object, scale);
//			} else
			if (object instanceof CollisionShape) {
				CollisionShape shape = (CollisionShape) object;
				if ((scale.x == scale.z) && (scale.y == scale.z)) {
					shape.setBoundsRadius(shape.getBoundsRadius() * scale.x);
				}
			}
			if(object instanceof ParticleEmitter2){
				((ParticleEmitter2) object).setLatitude(((ParticleEmitter2) object).getLatitude()*scale.z);
				((ParticleEmitter2) object).setWidth(((ParticleEmitter2) object).getWidth()*scale.y);
				((ParticleEmitter2) object).setLength(((ParticleEmitter2) object).getLength()*scale.x);
				((ParticleEmitter2) object).getParticleScaling().multiply(scale);
				((ParticleEmitter2) object).setSpeed(((ParticleEmitter2) object).getSpeed() * avgScale);
				((ParticleEmitter2) object).setGravity(((ParticleEmitter2) object).getGravity() * avgScale);
			}
			if(object instanceof ParticleEmitter){
				((ParticleEmitter) object).setLatitude(((ParticleEmitter) object).getLatitude()*scale.z);
				((ParticleEmitter) object).setLongitude(((ParticleEmitter) object).getLongitude()*scale.y);
				((ParticleEmitter) object).setInitVelocity(((ParticleEmitter) object).getInitVelocity()*avgScale);

			}
		}
		for (CameraNode cameraNode : selectedCameraNodes) {
			cameraNode.getPosition().scale(center, scale);
			translateNode(cameraNode, scale);
		}
	}
	public void translateNode(AnimatedNode node, Vec3 scale) {
		Vec3AnimFlag translation = (Vec3AnimFlag) node.find(MdlUtils.TOKEN_TRANSLATION);
		if (translation != null) {
			for (TreeMap<Integer, Entry<Vec3>> entryMap : translation.getAnimMap().values()) {
				if (entryMap != null) {
					for (Entry<Vec3> entry : entryMap.values()) {
						entry.getValue().multiply(scale);
						if (translation.tans()) {
							entry.getInTan().multiply(scale);
							entry.getOutTan().multiply(scale);
						}
					}
				}
			}
		}
	}



	private void scaleExtent(ExtLog extents, Vec3 center, Vec3 scale, double avgScale) {
		if (extents == null) {
			return;
		}
		if (extents.getMaximumExtent() != null) {
			extents.getMaximumExtent().scale(center, scale);
		}
		if (extents.getMinimumExtent() != null) {
			extents.getMinimumExtent().scale(center, scale);
		}
		if(extents.hasBoundsRadius()){
			extents.setBoundsRadius(extents.getBoundsRadius() * avgScale);
		}
	}

}
