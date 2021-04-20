package com.hiveworkshop.rms.ui.application.edit.mesh.types.vertexgroup;

import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelElementRenderer;
import com.hiveworkshop.rms.ui.application.edit.mesh.types.geosetvertex.GeosetVertexSelectionManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.uv.types.TVertexModelElementRenderer;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;
import java.util.List;
import java.util.*;

public final class VertexGroupSelectionManager extends SelectionManager<VertexGroupBundle> {
	private static final Color GROUP_SELECTED_COLOR = new Color(1f, 0.75f, 0.45f, 0.3f);

	private static final Color GROUP_HIGHLIGHT_COLOR = new Color(0.45f, 1f, 0.45f, 0.3f);

	private final GeosetVertexSelectionManager cachedVertexListManager;
	private final Set<Integer> resettableIntegerSet = new HashSet<>();

	public VertexGroupSelectionManager() {
		cachedVertexListManager = new GeosetVertexSelectionManager();
		addSelectionListener(newSelection -> {
			List<GeosetVertex> verticesSelected = new ArrayList<>();
			for (VertexGroupBundle bundle : getSelection()) {
				for (GeosetVertex geosetVertex : bundle.getGeoset().getVertices()) {
					if (geosetVertex.getVertexGroup() == bundle.getVertexGroupId()) {
						verticesSelected.add(geosetVertex);
					}
				}
			}
			cachedVertexListManager.setSelection(verticesSelected);
		});
	}

	@Override
	public Vec3 getCenter() {
		return cachedVertexListManager.getCenter();
	}

	@Override
	public Collection<? extends Vec3> getSelectedVertices() {
		return cachedVertexListManager.getSelectedVertices();
	}

	@Override
	public Collection<Triangle> getSelectedFaces() {
		return cachedVertexListManager.getSelectedFaces();
	}

	@Override
	public void renderSelection(ModelElementRenderer renderer, CoordinateSystem coordinateSystem, ModelView modelView, ProgramPreferences programPreferences) {
		Set<VertexGroupBundle> selection = getSelection();
		for (Geoset geoset : modelView.getEditableGeosets()) {
			Color outlineColor;
			Color fillColor;
			if (geoset == modelView.getHighlightedGeoset()) {
				outlineColor = Color.YELLOW;
				fillColor = GROUP_HIGHLIGHT_COLOR;
			} else {
				outlineColor = Color.ORANGE;
				fillColor = GROUP_SELECTED_COLOR;
			}
			for (Triangle triangle : geoset.getTriangles()) {
				GeosetVertex[] triangleVertices = triangle.getVerts();

				if (renderIf(renderer, selection, geoset, outlineColor, fillColor, triangle, triangleVertices, 0, 1, 2))
					continue;
				if (renderIf(renderer, selection, geoset, outlineColor, fillColor, triangle, triangleVertices, 0, 1, 0))
					continue;
				if (renderIf(renderer, selection, geoset, outlineColor, fillColor, triangle, triangleVertices, 0, 2, 0))
					continue;
				if (renderIf(renderer, selection, geoset, outlineColor, fillColor, triangle, triangleVertices, 1, 2, 1))
					continue;
			}
			// for (GeosetVertex geosetVertex : geoset.getVertices()) {
			// if (selection.contains(new VertexGroupBundle(geoset,
			// geosetVertex.getVertexGroup()))) {
			// renderer.renderVertex(outlineColor, geosetVertex);
			// }
			// }
		}
	}

	public boolean renderIf(ModelElementRenderer renderer, Set<VertexGroupBundle> selection, Geoset geoset, Color outlineColor, Color fillColor, Triangle triangle, GeosetVertex[] triangleVertices, int i, int i2, int i3) {
		if (selectionContainsVertexGroup(selection, i, geoset, triangleVertices)
				&& selectionContainsVertexGroup(selection, i2, geoset, triangleVertices)
				&& selectionContainsVertexGroup(selection, i3, geoset, triangleVertices)) {

			renderer.renderFace(outlineColor, fillColor, triangle.get(i), triangle.get(i2), triangle.get(i3));
			return true;
		}
		return false;
	}

	private boolean selectionContainsVertexGroup(Set<VertexGroupBundle> selection, int i, Geoset geoset, GeosetVertex[] triangleVertices) {
		return selection.contains(new VertexGroupBundle(geoset, triangleVertices[i].getVertexGroup()));
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
	public void renderUVSelection(TVertexModelElementRenderer renderer, ModelView modelView,
	                              ProgramPreferences programPreferences, int tvertexLayerId) {
		throw new UnsupportedOperationException("Not yet implemented");
	}
}
