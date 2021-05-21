package com.hiveworkshop.rms.ui.application.edit.mesh.types.geosetvertex;

import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelElementRenderer;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.uv.types.TVertexModelElementRenderer;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class GeosetVertexSelectionManager extends SelectionManager<GeosetVertex> {

	public GeosetVertexSelectionManager(ModelView modelView) {
		super(modelView);
	}

	@Override
	public Vec3 getCenter() {
		return Vec3.centerOfGroup(modelView.getSelectedVertices());
	}

	@Override
	public Set<GeosetVertex> getSelection() {
		return modelView.getSelectedVertices();
	}

	@Override
	public void setSelection(final Collection<? extends GeosetVertex> selectionItem) {
		modelView.setSelectedVertices((Collection<GeosetVertex>) selectionItem);
		fireChangeListeners();
	}

	@Override
	public void addSelection(final Collection<? extends GeosetVertex> selectionItem) {
		modelView.addSelectedVertices((Collection<GeosetVertex>) selectionItem);
		fireChangeListeners();
	}

	@Override
	public void removeSelection(final Collection<? extends GeosetVertex> selectionItem) {
		modelView.removeSelectedVertices((Collection<GeosetVertex>) selectionItem);
		fireChangeListeners();
	}

	@Override
	public boolean isEmpty() {
		return modelView.getSelectedVertices().isEmpty();
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
	public void renderSelection(ModelElementRenderer renderer,
	                            CoordinateSystem coordinateSystem,
	                            ModelView model) {
//		for (Geoset geo : model.getEditableGeosets()) {
//			List<GeosetVertex> vertices = geo.getVertices();
//			for (GeosetVertex geosetVertex : vertices) {
//				if (model.getHighlightedGeoset() == geo) {
//					renderer.renderVertex(ProgramGlobals.getPrefs().getHighlighVertexColor(), geosetVertex);
//				} else if (modelView.isSelected(geosetVertex)) {
//					renderer.renderVertex(ProgramGlobals.getPrefs().getSelectColor(), geosetVertex);
//				} else {
//					renderer.renderVertex(ProgramGlobals.getPrefs().getVertexColor(), geosetVertex);
//				}
//			}
//		}
	}

	@Override
	public double getCircumscribedSphereRadius(Vec3 sphereCenter) {
		double radius = 0;
		for (Vec3 item : modelView.getSelectedVertices()) {
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
		for (GeosetVertex item : modelView.getSelectedVertices()) {
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
		for (GeosetVertex vertex : modelView.getSelectedVertices()) {
			if (tvertexLayerId < vertex.getTverts().size()) {
				selectedTVertices.add(vertex.getTVertex(tvertexLayerId));
			}
		}
		return selectedTVertices;
	}

	@Override
	public void renderUVSelection(TVertexModelElementRenderer renderer, ModelView modelView, int tvertexLayerId) {
		for (Geoset geo : modelView.getEditableGeosets()) {
			List<GeosetVertex> vertices = geo.getVertices();
			for (GeosetVertex geosetVertex : vertices) {
				if (tvertexLayerId >= geosetVertex.getTverts().size()) {
					continue;
				}
				if (modelView.getHighlightedGeoset() == geo) {
					renderer.renderVertex(ProgramGlobals.getPrefs().getHighlighVertexColor(), geosetVertex.getTVertex(tvertexLayerId));
				} else if (modelView.isSelected(geosetVertex)) {
					renderer.renderVertex(ProgramGlobals.getPrefs().getSelectColor(), geosetVertex.getTVertex(tvertexLayerId));
				} else {
					renderer.renderVertex(ProgramGlobals.getPrefs().getVertexColor(), geosetVertex.getTVertex(tvertexLayerId));
				}
			}
		}
	}
}
