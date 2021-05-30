package com.hiveworkshop.rms.editor.actions.editor;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.util.GenericScaleAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.util.Vec3;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

public class StaticMeshScaleAction implements GenericScaleAction {
	private final ModelView modelView;
	private final Vec3 center;
	private final Vec3 scale = new Vec3(1, 1, 1);
	private final Set<GeosetVertex> selectedVertices;
	private final Set<IdObject> selectedIdObjects;
	private final Set<Camera> selectedCameras;

	public StaticMeshScaleAction(ModelView modelView, Vec3 center) {
		this.modelView = modelView;
		this.center = center;
		selectedVertices = new HashSet<>(modelView.getSelectedVertices());
		selectedIdObjects = new HashSet<>(modelView.getSelectedIdObjects());
		selectedCameras = new HashSet<>(modelView.getSelectedCameras());
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
			if (object instanceof Bone) {
				translateBone((Bone) object, scale);
			} else if (object instanceof CollisionShape) {
				ExtLog extents = ((CollisionShape) object).getExtents();
				if ((extents != null) && (scale.x == scale.x) && (scale.y == scale.z)) {
					extents.setBoundsRadius(extents.getBoundsRadius() * scale.x);
				}
			}

		}
	}

	// Scales the translation animations of scaled bones (and helpers)
	// is this correct to do...?
	public void translateBone(Bone object, Vec3 scale) {
		Vec3AnimFlag translation = (Vec3AnimFlag) object.find("Translation");
		if (translation != null) {
			TreeMap<Integer, Entry<Vec3>> entryMap = translation.getEntryMap();
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
