package com.hiveworkshop.wc3.gui.modeledit.newstuff;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hiveworkshop.wc3.gui.ProgramPreferences;
import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.VertexClusterModelEditor.VertexGroupBundle;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionListener;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.Triangle;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.v2.ModelView;

public final class VertexClusterSelectionManager extends AbstractSelectionManager<VertexGroupBundle> {
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
				for (final VertexGroupBundle bundle : getSelection()) {
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
	public Vertex getCenter() {
		return cachedVertexListManager.getCenter();
	}

	@Override
	public Collection<Triangle> getSelectedFaces() {
		return cachedVertexListManager.getSelectedFaces();
	}

	@Override
	public Collection<? extends Vertex> getSelectedVertices() {
		return cachedVertexListManager.getSelectedVertices();
	}

	@Override
	public double getCircumscribedSphereRadius(final Vertex center) {
		return cachedVertexListManager.getCircumscribedSphereRadius(center);
	}

	@Override
	public void renderSelection(final ModelElementRenderer renderer, final CoordinateSystem coordinateSystem,
			final ModelView modelView, final ProgramPreferences programPreferences) {
		final Set<VertexGroupBundle> selection = getSelection();
		for (final Geoset geoset : modelView.getEditableGeosets()) {
			Color outlineColor;
			Color fillColor;
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
						new VertexGroupBundle(geoset, vertexClusterDefinitions.getClusterId(triangleVertices[0])))
						&& selection.contains(new VertexGroupBundle(geoset,
								vertexClusterDefinitions.getClusterId(triangleVertices[1])))
						&& selection.contains(new VertexGroupBundle(geoset,
								vertexClusterDefinitions.getClusterId(triangleVertices[2])))) {
					renderer.renderFace(outlineColor, fillColor, triangle.get(0), triangle.get(1), triangle.get(2));
				} else if (selection.contains(
						new VertexGroupBundle(geoset, vertexClusterDefinitions.getClusterId(triangleVertices[0])))
						&& selection.contains(new VertexGroupBundle(geoset,
								vertexClusterDefinitions.getClusterId(triangleVertices[1])))) {
					renderer.renderFace(outlineColor, fillColor, triangle.get(0), triangle.get(1), triangle.get(0));
				} else if (selection.contains(
						new VertexGroupBundle(geoset, vertexClusterDefinitions.getClusterId(triangleVertices[0])))
						&& selection.contains(new VertexGroupBundle(geoset,
								vertexClusterDefinitions.getClusterId(triangleVertices[2])))) {
					renderer.renderFace(outlineColor, fillColor, triangle.get(0), triangle.get(2), triangle.get(0));
				} else if (selection.contains(
						new VertexGroupBundle(geoset, vertexClusterDefinitions.getClusterId(triangleVertices[1])))
						&& selection.contains(new VertexGroupBundle(geoset,
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
}
