package com.hiveworkshop.rms.ui.application.edit.mesh.types.geosetvertex;

import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelElementRenderer;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.uv.types.TVertexModelElementRenderer;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class GeosetVertexSelectionManager extends SelectionManager<GeosetVertex> {

	@Override
	public Vec3 getCenter() {
		return Vec3.centerOfGroup(selection);
	}

	@Override
	public Collection<? extends Vec3> getSelectedVertices() {
		return getSelection();
	}

	@Override
	public Set<Triangle> getSelectedFaces() {
		Set<Triangle> faces = new HashSet<>();
		Set<GeosetVertex> selectedVertices = new HashSet<>();
		Set<Triangle> partiallySelectedFaces = new HashSet<>();
		for (GeosetVertex vertex : getSelection()) {
			partiallySelectedFaces.addAll(vertex.getTriangles());
			selectedVertices.add(vertex);
		}
		for (Triangle face : partiallySelectedFaces) {
			boolean whollySelected = true;
			for (GeosetVertex gv : face.getVerts()) {
				if (!selectedVertices.contains(gv)) {
					whollySelected = false;
					break;
				}
			}
			if (whollySelected) {
				faces.add(face);
			}
		}
		return faces;
	}

	@Override
	public void renderSelection(ModelElementRenderer renderer, CoordinateSystem coordinateSystem,
	                            ModelView model, ProgramPreferences programPreferences) {
		for (Geoset geo : model.getEditableGeosets()) {
			List<GeosetVertex> vertices = geo.getVertices();
			for (GeosetVertex geosetVertex : vertices) {
				if (model.getHighlightedGeoset() == geo) {
					renderer.renderVertex(programPreferences.getHighlighVertexColor(), geosetVertex);
				} else if (selection.contains(geosetVertex)) {
					renderer.renderVertex(programPreferences.getSelectColor(), geosetVertex);
				} else {
					renderer.renderVertex(programPreferences.getVertexColor(), geosetVertex);
				}
			}
		}
	}

	@Override
	public double getCircumscribedSphereRadius(Vec3 sphereCenter) {
		double radius = 0;
		for (Vec3 item : selection) {
			double distance = sphereCenter.distance(item);
			if (distance >= radius) {
				radius = distance;
			}
		}
		return radius;
	}

	@Override
	public double getCircumscribedSphereRadius(Vec2 center, int tvertexLayerId) {
		double radius = 0;
		for (GeosetVertex item : selection) {
			if (tvertexLayerId < item.getTverts().size()) {
				double distance = center.distance(item.getTVertex(tvertexLayerId));
				if (distance >= radius) {
					radius = distance;
				}
			}
		}
		return radius;
	}

	@Override
	public Vec2 getUVCenter(int tvertexLayerId) {
		return Vec2.centerOfGroup(getSelectedTVertices(tvertexLayerId));
	}

	@Override
	public Collection<? extends Vec2> getSelectedTVertices(int tvertexLayerId) {
		Set<Vec2> selectedTVertices = new HashSet<>();
		for (GeosetVertex vertex : selection) {
			if (tvertexLayerId < vertex.getTverts().size()) {
				selectedTVertices.add(vertex.getTVertex(tvertexLayerId));
			}
		}
		return selectedTVertices;
	}

	@Override
	public void renderUVSelection(TVertexModelElementRenderer renderer, ModelView modelView,
	                              ProgramPreferences programPreferences, int tvertexLayerId) {
		for (Geoset geo : modelView.getEditableGeosets()) {
			List<GeosetVertex> vertices = geo.getVertices();
			for (GeosetVertex geosetVertex : vertices) {
				if (tvertexLayerId >= geosetVertex.getTverts().size()) {
					continue;
				}
				if (modelView.getHighlightedGeoset() == geo) {
					renderer.renderVertex(programPreferences.getHighlighVertexColor(), geosetVertex.getTVertex(tvertexLayerId));
				} else if (selection.contains(geosetVertex)) {
					renderer.renderVertex(programPreferences.getSelectColor(), geosetVertex.getTVertex(tvertexLayerId));
				} else {
					renderer.renderVertex(programPreferences.getVertexColor(), geosetVertex.getTVertex(tvertexLayerId));
				}
			}
		}
	}
}
