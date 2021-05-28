package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.editor;

import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericRotateAction;
import com.hiveworkshop.rms.util.Vec3;

import java.util.HashSet;
import java.util.Set;

public final class StaticMeshRotateAction implements GenericRotateAction {
	private final Vec3 center;
	private double radians;
	private final byte dim1;
	private final byte dim2;
	private final Set<GeosetVertex> selectedVertices;
	private final Set<IdObject> selectedIdObjects;
	private final Set<Camera> selectedCameras;

	public StaticMeshRotateAction(ModelView modelView, Vec3 center, byte dim1, byte dim2) {
		this.center = center;
		radians = 0;
		this.dim1 = dim1;
		this.dim2 = dim2;
		selectedVertices = new HashSet<>(modelView.getSelectedVertices());
		selectedIdObjects = new HashSet<>(modelView.getSelectedIdObjects());
		selectedCameras = new HashSet<>(modelView.getSelectedCameras());
	}

	@Override
	public UndoAction undo() {
		rotate(-radians);
		return this;
	}

	@Override
	public UndoAction redo() {
		rotate(radians);
		return this;
	}

	@Override
	public String actionName() {
		return "rotate";
	}

	@Override
	public GenericRotateAction updateRotation(double radians) {
		this.radians += radians;
		rotate(radians);
		return this;
	}

	public void rotate(double v) {
		for (GeosetVertex vertex : selectedVertices) {
			vertex.rotate(center, v, dim1, dim2);
		}
		for (IdObject b : selectedIdObjects) {
			b.getPivotPoint().rotate(center, v, dim1, dim2);
			float[] bindPose = b.getBindPose();
			if (bindPose != null) {
				bindPose[9] = b.getPivotPoint().x;
				bindPose[10] = b.getPivotPoint().y;
				bindPose[11] = b.getPivotPoint().z;
			}
		}

		for (Camera camera : selectedCameras) {
			camera.getPosition().rotate(center, v, dim1, dim2);
		}
	}
}
