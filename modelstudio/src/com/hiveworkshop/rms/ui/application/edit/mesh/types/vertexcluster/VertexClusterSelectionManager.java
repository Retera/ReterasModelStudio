package com.hiveworkshop.rms.ui.application.edit.mesh.types.vertexcluster;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelElementRenderer;
import com.hiveworkshop.rms.ui.application.edit.mesh.VertexClusterDefinitions;
import com.hiveworkshop.rms.ui.application.edit.mesh.selection.AbstractSelectionManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.selection.GeosetVertexSelectionManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.uv.TVertexModelElementRenderer;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionListener;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Vector2;
import com.hiveworkshop.rms.util.Vector3;

public final class VertexClusterSelectionManager extends AbstractSelectionManager<VertexClusterModelEditor.VertexGroupBundle> {
	private static final Color GROUP_SELECTED_COLOR = new Color(1f, 0.45f, 0.75f, 0.3f);

	private static final Color GROUP_HIGHLIGHT_COLOR = new Color(0.45f, 1f, 0.45f, 0.3f);

	private final GeosetVertexSelectionManager cachedVertexListManager;
	private final Set<Integer> resettableIntegerSet = new HashSet<>();

	private final VertexClusterDefinitions vertexClusterDefinitions;

	public VertexClusterSelectionManager(final VertexClusterDefinitions vertexClusterDefinitions) {
		this.vertexClusterDefinitions = vertexClusterDefinitions;
		cachedVertexListManager = new GeosetVertexSelectionManager();
		addSelectionListener(new SelectionListener() {
			@Override
			public void onSelectionChanged(final SelectionView newSelection) {
				final List<GeosetVertex> verticesSelected = new ArrayList<>();
				for (final VertexClusterModelEditor.VertexGroupBundle bundle : getSelection()) {
					for (final GeosetVertex geosetVertex : bundle.getGeoset().getVertices()) {
						if (vertexClusterDefinitions.getClusterId(geosetVertex) == bundle.getVertexGroupId()) {
							verticesSelected.add(geosetVertex);
						}
					}
				}
				cachedVertexListManager.setSelection(verticesSelected);
			}
		});
	}

	@Override
	public Vector3 getCenter() {
		return cachedVertexListManager.getCenter();
	}

	@Override
	public Collection<Triangle> getSelectedFaces() {
		return cachedVertexListManager.getSelectedFaces();
	}

	@Override
	public Collection<? extends Vector3> getSelectedVertices() {
		return cachedVertexListManager.getSelectedVertices();
	}

	@Override
	public double getCircumscribedSphereRadius(final Vector3 center) {
		return cachedVertexListManager.getCircumscribedSphereRadius(center);
	}

	@Override
	public void renderSelection(final ModelElementRenderer renderer, final CoordinateSystem coordinateSystem,
								final ModelView modelView, final ProgramPreferences programPreferences) {
		final Set<VertexClusterModelEditor.VertexGroupBundle> selection = getSelection();
		for (final Geoset geoset : modelView.getEditableGeosets()) {
			final Color outlineColor;
			final Color fillColor;
			if (geoset == modelView.getHighlightedGeoset()) {
				outlineColor = Color.YELLOW;
				fillColor = GROUP_HIGHLIGHT_COLOR;
			} else {
				outlineColor = Color.RED;
				fillColor = GROUP_SELECTED_COLOR;
			}
			for (final Triangle triangle : geoset.getTriangles()) {
				final GeosetVertex[] triangleVertices = triangle.getVerts();
				if (selection.contains(
						new VertexClusterModelEditor.VertexGroupBundle(geoset, vertexClusterDefinitions.getClusterId(triangleVertices[0])))
						&& selection.contains(new VertexClusterModelEditor.VertexGroupBundle(geoset,
								vertexClusterDefinitions.getClusterId(triangleVertices[1])))
						&& selection.contains(new VertexClusterModelEditor.VertexGroupBundle(geoset,
								vertexClusterDefinitions.getClusterId(triangleVertices[2])))) {
					renderer.renderFace(outlineColor, fillColor, triangle.get(0), triangle.get(1), triangle.get(2));
				} else if (selection.contains(
						new VertexClusterModelEditor.VertexGroupBundle(geoset, vertexClusterDefinitions.getClusterId(triangleVertices[0])))
						&& selection.contains(new VertexClusterModelEditor.VertexGroupBundle(geoset,
								vertexClusterDefinitions.getClusterId(triangleVertices[1])))) {
					renderer.renderFace(outlineColor, fillColor, triangle.get(0), triangle.get(1), triangle.get(0));
				} else if (selection.contains(
						new VertexClusterModelEditor.VertexGroupBundle(geoset, vertexClusterDefinitions.getClusterId(triangleVertices[0])))
						&& selection.contains(new VertexClusterModelEditor.VertexGroupBundle(geoset,
								vertexClusterDefinitions.getClusterId(triangleVertices[2])))) {
					renderer.renderFace(outlineColor, fillColor, triangle.get(0), triangle.get(2), triangle.get(0));
				} else if (selection.contains(
						new VertexClusterModelEditor.VertexGroupBundle(geoset, vertexClusterDefinitions.getClusterId(triangleVertices[1])))
						&& selection.contains(new VertexClusterModelEditor.VertexGroupBundle(geoset,
								vertexClusterDefinitions.getClusterId(triangleVertices[2])))) {
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

	@Override
	public Vector2 getUVCenter(final int tvertexLayerId) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public Collection<? extends Vector2> getSelectedTVertices(final int tvertexLayerId) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public double getCircumscribedSphereRadius(final Vector2 center, final int tvertexLayerId) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public void renderUVSelection(final TVertexModelElementRenderer renderer, final ModelView modelView,
                                  final ProgramPreferences programPreferences, final int tvertexLayerId) {
		throw new UnsupportedOperationException("Not yet implemented");
	}
}
