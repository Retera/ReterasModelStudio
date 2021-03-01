package com.hiveworkshop.rms.ui.application.edit.mesh.types.vertexcluster;

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

public final class VertexClusterSelectionManager extends AbstractSelectionManager<VertexClusterModelEditor.VertexGroupBundle> {
	private static final Color GROUP_SELECTED_COLOR = new Color(1f, 0.45f, 0.75f, 0.3f);

	private static final Color GROUP_HIGHLIGHT_COLOR = new Color(0.45f, 1f, 0.45f, 0.3f);

	private final GeosetVertexSelectionManager cachedVertexListManager;
	private final Set<Integer> resettableIntegerSet = new HashSet<>();

	private final VertexClusterDefinitions vertexClusterDefinitions;

	public VertexClusterSelectionManager(final VertexClusterDefinitions vertexClusterDefinitions) {
		this.vertexClusterDefinitions = vertexClusterDefinitions;
		cachedVertexListManager = new GeosetVertexSelectionManager();
		addSelectionListener(newSelection -> {
            final List<GeosetVertex> verticesSelected = new ArrayList<>();
            for (final VertexClusterModelEditor.VertexGroupBundle bundle : getSelection()) {
                for (final GeosetVertex geosetVertex : bundle.getGeoset().getVertices()) {
                    if (vertexClusterDefinitions.getClusterId(geosetVertex) == bundle.getVertexGroupId()) {
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
				if (containsClusters(selection, geoset, triangleVertices, 0, 1, 2)) {
					renderer.renderFace(outlineColor, fillColor, triangle.get(0), triangle.get(1), triangle.get(2));
				} else if (containsClusters(selection, geoset, triangleVertices, 0, 1, 0)) {
					renderer.renderFace(outlineColor, fillColor, triangle.get(0), triangle.get(1), triangle.get(0));
				} else if (containsClusters(selection, geoset, triangleVertices, 0, 2, 0)) {
					renderer.renderFace(outlineColor, fillColor, triangle.get(0), triangle.get(2), triangle.get(0));
				} else if (containsClusters(selection, geoset, triangleVertices, 1, 2, 1)) {
					renderer.renderFace(outlineColor, fillColor, triangle.get(1), triangle.get(2), triangle.get(1));
				}
			}
		}
	}

	private boolean containsClusters(Set<VertexClusterModelEditor.VertexGroupBundle> selection, Geoset geoset, GeosetVertex[] triangleVertices, int cluster1, int cluster2, int cluster3) {
		return containsCluster(selection, cluster1, geoset, triangleVertices)
				&& containsCluster(selection, cluster2, geoset, triangleVertices)
				&& containsCluster(selection, cluster3, geoset, triangleVertices);
	}

	private boolean containsCluster(Set<VertexClusterModelEditor.VertexGroupBundle> selection, int cluster, Geoset geoset, GeosetVertex[] triangleVertices) {
		return selection.contains(new VertexClusterModelEditor.VertexGroupBundle(geoset, vertexClusterDefinitions.getClusterId(triangleVertices[cluster])));
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
	public void renderUVSelection(final TVertexModelElementRenderer renderer, final ModelView modelView, final ProgramPreferences programPreferences, final int tvertexLayerId) {
		throw new UnsupportedOperationException("Not yet implemented");
	}
}
