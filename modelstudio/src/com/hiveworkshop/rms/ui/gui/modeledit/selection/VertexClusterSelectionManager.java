package com.hiveworkshop.rms.ui.gui.modeledit.selection;

import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.uv.types.TVertexModelElementRenderer;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.selection.VertexClusterDefinitions;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.selection.VertexGroupBundle;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;
import java.util.List;
import java.util.*;

public final class VertexClusterSelectionManager extends SelectionManager<VertexGroupBundle> {
	private static final Color GROUP_SELECTED_COLOR = new Color(1f, 0.45f, 0.75f, 0.3f);

	private static final Color GROUP_HIGHLIGHT_COLOR = new Color(0.45f, 1f, 0.45f, 0.3f);

	private final GeosetVertexSelectionManager cachedVertexListManager;
	private final Set<Integer> resettableIntegerSet = new HashSet<>();

	private final VertexClusterDefinitions vertexClusterDefinitions;

	public VertexClusterSelectionManager(VertexClusterDefinitions vertexClusterDefinitions, ModelView modelView, SelectionItemTypes selectionMode) {
		super(modelView, selectionMode);
		this.vertexClusterDefinitions = vertexClusterDefinitions;
		cachedVertexListManager = new GeosetVertexSelectionManager(modelView, selectionMode);
		addSelectionListener(newSelection -> selectBundle(vertexClusterDefinitions, modelView));
	}

	private void selectBundle(VertexClusterDefinitions vertexClusterDefinitions, ModelView modelView) {
		Set<VertexGroupBundle> bundleSet = new HashSet<>();
		List<GeosetVertex> verticesSelected = new ArrayList<>();
		for (GeosetVertex vertex : modelView.getSelectedVertices()) {
			bundleSet.add(new VertexGroupBundle(vertex.getGeoset(), vertexClusterDefinitions.getClusterId(vertex)));
		}

		for (VertexGroupBundle bundle : bundleSet) {
			for (GeosetVertex geosetVertex : bundle.getGeoset().getVertices()) {
				if (vertexClusterDefinitions.getClusterId(geosetVertex) == bundle.getVertexGroupId()) {
					verticesSelected.add(geosetVertex);
				}
			}
		}
		modelView.setSelectedVertices(verticesSelected);
	}

	@Override
	public void setSelection(final Collection<? extends VertexGroupBundle> selectionItem) {
		Set<GeosetVertex> ugg = new HashSet<>();
		for (VertexGroupBundle bundle : selectionItem){
			ugg.addAll(bundle.getGeoset().getVertices());
		}
		modelView.setSelectedVertices(ugg);
		fireChangeListeners();
	}

	@Override
	public void addSelection(final Collection<? extends VertexGroupBundle> selectionItem) {
		Set<GeosetVertex> ugg = new HashSet<>();
		for (VertexGroupBundle bundle : selectionItem){
			ugg.addAll(bundle.getGeoset().getVertices());
		}
		modelView.addSelectedVertices(ugg);
		fireChangeListeners();
	}
	@Override
	public void removeSelection(final Collection<? extends VertexGroupBundle> selectionItem) {
		Set<GeosetVertex> ugg = new HashSet<>();
		for (VertexGroupBundle bundle : selectionItem){
			ugg.addAll(bundle.getGeoset().getVertices());
		}
		modelView.removeSelectedVertices(ugg);
		fireChangeListeners();
	}

	@Override
	public Set<VertexGroupBundle> getSelection() {
//		return modelView.getSelectedVertices();
		return new HashSet<>();
	}

	@Override
	public boolean isEmpty() {
		return modelView.getSelectedVertices().isEmpty();
	}

	@Override
	public List<VertexGroupBundle> genericSelect(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem) {
		List<VertexGroupBundle> newSelection = new ArrayList<>();
		List<GeosetVertex> geosetVerticesSelected = new ArrayList<>();

		for (Geoset geoset : modelView.getEditableGeosets()) {
			for (Triangle triangle : geoset.getTriangles()) {
				if (HitTestStuff.triHitTest(triangle, min, coordinateSystem)
						|| HitTestStuff.triHitTest(triangle, max, coordinateSystem)
						|| HitTestStuff.triHitTest(triangle, min, max, coordinateSystem)) {
					for (GeosetVertex vertex : triangle.getAll()) {
						newSelection.add(new VertexGroupBundle(geoset, vertexClusterDefinitions.getClusterId(vertex)));
					}
				}
			}
			for (GeosetVertex geosetVertex : geoset.getVertices()) {
				if (HitTestStuff.hitTest(min, max, geosetVertex, coordinateSystem, ProgramGlobals.getPrefs().getVertexSize())) {
					geosetVerticesSelected.add(geosetVertex);
				}
			}
		}
		for (GeosetVertex vertex : geosetVerticesSelected) {
			newSelection.add(new VertexGroupBundle(vertex.getGeoset(), vertexClusterDefinitions.getClusterId(vertex)));
		}
		return newSelection;
	}

	@Override
	public double getCircumscribedSphereRadius(Vec3 center) {
		return cachedVertexListManager.getCircumscribedSphereRadius(center);
	}

	@Override
	public double getCircumscribedSphereRadius(Vec2 center, int tvertexLayerId) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public Vec2 getUVCenter(int tvertexLayerId) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public Collection<? extends Vec2> getSelectedTVertices(int tvertexLayerId) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public void renderUVSelection(TVertexModelElementRenderer renderer, ModelView modelView, int tvertexLayerId) {
		throw new UnsupportedOperationException("Not yet implemented");
	}
}
