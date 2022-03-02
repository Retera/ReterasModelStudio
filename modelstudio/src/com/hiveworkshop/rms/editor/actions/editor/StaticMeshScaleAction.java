package com.hiveworkshop.rms.editor.actions.editor;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.util.GenericScaleAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.util.Vec3;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

public class StaticMeshScaleAction implements GenericScaleAction {
	private final Vec3 center;
	private final Vec3 scale;
	private final Set<GeosetVertex> selectedVertices;
	private final Set<IdObject> selectedIdObjects;
	private final Set<CameraNode> selectedCameraNodes;

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
		this.selectedVertices = new HashSet<>(selectedVertices);
		this.selectedIdObjects = new HashSet<>(selectedIdObjects);
		this.selectedCameraNodes = new HashSet<>(selectedCameraNodes);
	}

	@Override
	public UndoAction undo() {
		Vec3 revScale = new Vec3(1, 1, 1).divide(scale);
		rawScale(center, revScale);
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
				ExtLog extents = ((CollisionShape) object).getExtents();
				if ((extents != null) && (scale.x == scale.x) && (scale.y == scale.z)) {
					extents.setBoundsRadius(extents.getBoundsRadius() * scale.x);
				}
			}
			if(object instanceof ParticleEmitter2){
				((ParticleEmitter2) object).setLatitude(((ParticleEmitter2) object).getLatitude()*scale.z);
				((ParticleEmitter2) object).setWidth(((ParticleEmitter2) object).getWidth()*scale.y);
				((ParticleEmitter2) object).setLength(((ParticleEmitter2) object).getLength()*scale.x);
			}
			if(object instanceof ParticleEmitter){
				((ParticleEmitter) object).setLatitude(((ParticleEmitter) object).getLatitude()*scale.z);
				((ParticleEmitter) object).setLongitude(((ParticleEmitter) object).getLongitude()*scale.y);
			}
		}
		for (CameraNode cameraNode : selectedCameraNodes) {
			cameraNode.getPosition().scale(center, scale);
			translateNode(cameraNode, scale);
		}
	}

	// Scales the translation animations of scaled bones (and helpers)
	// is this correct to do...?
	public void translateBone(IdObject object, Vec3 scale) {
		Vec3AnimFlag translation = (Vec3AnimFlag) object.find(MdlUtils.TOKEN_TRANSLATION);
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

}
