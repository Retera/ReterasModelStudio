package com.hiveworkshop.rms.editor.actions.uv;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.util.GenericRotateAction;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayList;
import java.util.Collection;

public final class StaticMeshUVRotateAction implements GenericRotateAction {
	private final ArrayList<Vec2> selectedTVerts;
	private final ArrayList<Vec2> orgTVerts;
	private final Vec3 center;
	private double radians;
	private final byte dim1;
	private final byte dim2;
	int uvLayerIndex;

	public StaticMeshUVRotateAction(Collection<GeosetVertex> selectedVertices, int uvLayerIndex, Vec2 center, byte dim1, byte dim2) {
		this(selectedVertices, uvLayerIndex, new Vec3(center.x, center.y, 0), dim1, dim2, 0);
	}

	public StaticMeshUVRotateAction(Collection<GeosetVertex> selectedVertices,
	                                int uvLayerIndex, Vec3 center,
	                                byte dim1, byte dim2,
	                                double radians) {
		selectedTVerts = new ArrayList<>();
		orgTVerts = new ArrayList<>();
		for (GeosetVertex vertex : selectedVertices) {
			if (uvLayerIndex < vertex.getTverts().size()) {
				Vec2 tVertex = vertex.getTVertex(uvLayerIndex);
				selectedTVerts.add(tVertex);
				orgTVerts.add(new Vec2(tVertex));
			}
		}
		this.uvLayerIndex = uvLayerIndex;
		this.center = center;
		this.dim1 = dim1;
		this.dim2 = dim2;
		this.radians = radians;
	}

	@Override
	public UndoAction undo() {
		for (int i = 0; i<selectedTVerts.size(); i++) {
			selectedTVerts.get(i).set(orgTVerts.get(i));
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		for (Vec2 vertex : selectedTVerts) {
			vertex.rotate(center.x, center.y, radians, dim1, dim2);
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Rotate UVs";
	}

	@Override
	public GenericRotateAction updateRotation(double radians) {
		this.radians += radians;
		for (int i = 0; i<selectedTVerts.size(); i++) {
			selectedTVerts.get(i).set(orgTVerts.get(i)).rotate(center.x, center.y, this.radians, dim1, dim2);
		}
		return this;
	}

}
