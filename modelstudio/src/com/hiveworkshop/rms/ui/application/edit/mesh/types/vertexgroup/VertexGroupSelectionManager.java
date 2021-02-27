package com.hiveworkshop.rms.ui.application.edit.mesh.types.vertexgroup;

import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelElementRenderer;
import com.hiveworkshop.rms.ui.application.edit.mesh.selection.AbstractSelectionManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.types.geosetvertex.GeosetVertexSelectionManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.uv.types.TVertexModelElementRenderer;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;
import java.util.List;
import java.util.*;

public final class VertexGroupSelectionManager extends AbstractSelectionManager<VertexGroupModelEditor.VertexGroupBundle> {
	private static final Color GROUP_SELECTED_COLOR = new Color(1f, 0.75f, 0.45f, 0.3f);

	private static final Color GROUP_HIGHLIGHT_COLOR = new Color(0.45f, 1f, 0.45f, 0.3f);

	private final GeosetVertexSelectionManager cachedVertexListManager;
	private final Set<Integer> resettableIntegerSet = new HashSet<>();

	public VertexGroupSelectionManager() {
		cachedVertexListManager = new GeosetVertexSelectionManager();
		addSelectionListener(newSelection -> {
            final List<GeosetVertex> verticesSelected = new ArrayList<>();
            for (final VertexGroupModelEditor.VertexGroupBundle bundle : getSelection()) {
                for (final GeosetVertex geosetVertex : bundle.getGeoset().getVertices()) {
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
	public Collection<Triangle> getSelectedFaces() {
		return cachedVertexListManager.getSelectedFaces();
	}

	@Override
	public Collection<? extends Vec3> getSelectedVertices() {
		return cachedVertexListManager.getSelectedVertices();
	}

	@Override
	public double getCircumscribedSphereRadius(final Vec3 center) {
		return cachedVertexListManager.getCircumscribedSphereRadius(center);
	}

	@Override
	public void renderSelection(final ModelElementRenderer renderer, final CoordinateSystem coordinateSystem, final ModelView modelView, final ProgramPreferences programPreferences) {
		final Set<VertexGroupModelEditor.VertexGroupBundle> selection = getSelection();
		for (final Geoset geoset : modelView.getEditableGeosets()) {
			final Color outlineColor;
			final Color fillColor;
			if (geoset == modelView.getHighlightedGeoset()) {
				outlineColor = Color.YELLOW;
				fillColor = GROUP_HIGHLIGHT_COLOR;
			} else {
				outlineColor = Color.ORANGE;
				fillColor = GROUP_SELECTED_COLOR;
			}
			for (final Triangle triangle : geoset.getTriangles()) {
				final GeosetVertex[] triangleVertices = triangle.getVerts();
				if (selectionContainsVertexGroup(selection, 0, geoset, triangleVertices)
						&& selectionContainsVertexGroup(selection, 1, geoset, triangleVertices)
						&& selectionContainsVertexGroup(selection, 2, geoset, triangleVertices)) {
					renderer.renderFace(outlineColor, fillColor, triangle.get(0), triangle.get(1), triangle.get(2));
				} else if (selectionContainsVertexGroup(selection, 0, geoset, triangleVertices)
						&& selectionContainsVertexGroup(selection, 1, geoset, triangleVertices)) {
					renderer.renderFace(outlineColor, fillColor, triangle.get(0), triangle.get(1), triangle.get(0));
				} else if (selectionContainsVertexGroup(selection, 0, geoset, triangleVertices)
						&& selectionContainsVertexGroup(selection, 2, geoset, triangleVertices)) {
					renderer.renderFace(outlineColor, fillColor, triangle.get(0), triangle.get(2), triangle.get(0));
				} else if (selectionContainsVertexGroup(selection, 1, geoset, triangleVertices)
						&& selectionContainsVertexGroup(selection, 2, geoset, triangleVertices)) {
					renderer.renderFace(outlineColor, fillColor, triangle.get(1), triangle.get(2), triangle.get(1));
				}
			}
			// for (final GeosetVertex geosetVertex : geoset.getVertices()) {
			// if (selection.contains(new VertexGroupBundle(geoset,
			// geosetVertex.getVertexGroup()))) {
			// renderer.renderVertex(outlineColor, geosetVertex);
			// }
			// }
		}
	}

	private boolean selectionContainsVertexGroup(Set<VertexGroupModelEditor.VertexGroupBundle> selection, int i, Geoset geoset, GeosetVertex[] triangleVertices) {
		return selection.contains(new VertexGroupModelEditor.VertexGroupBundle(geoset, triangleVertices[i].getVertexGroup()));
	}

	@Override
	public Vec2 getUVCenter(final int tvertexLayerId) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public Collection<? extends Vec2> getSelectedTVertices(final int tvertexLayerId) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public double getCircumscribedSphereRadius(final Vec2 center, final int tvertexLayerId) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public void renderUVSelection(final TVertexModelElementRenderer renderer, final ModelView modelView,
                                  final ProgramPreferences programPreferences, final int tvertexLayerId) {
		throw new UnsupportedOperationException("Not yet implemented");
	}
}
