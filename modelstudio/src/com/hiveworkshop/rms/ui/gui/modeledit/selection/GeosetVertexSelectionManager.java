package com.hiveworkshop.rms.ui.gui.modeledit.selection;

import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.uv.types.TVertexModelElementRenderer;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.util.*;

public final class GeosetVertexSelectionManager extends SelectionManager<GeosetVertex> {

	public GeosetVertexSelectionManager(ModelView modelView, SelectionItemTypes selectionMode) {
		super(modelView, selectionMode);
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
	public List<GeosetVertex> genericSelect(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem) {
		List<GeosetVertex> selectedItems = new ArrayList<>();

		for (Geoset geoset : modelView.getEditableGeosets()) {
			for (GeosetVertex geosetVertex : geoset.getVertices()) {
				if (HitTestStuff.hitTest(min, max, geosetVertex, coordinateSystem, ProgramGlobals.getPrefs().getVertexSize()))
					selectedItems.add(geosetVertex);
			}
		}
		return selectedItems;
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
