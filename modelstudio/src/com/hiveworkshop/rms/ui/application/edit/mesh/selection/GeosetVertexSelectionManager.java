package com.hiveworkshop.rms.ui.application.edit.mesh.selection;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.TVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelElementRenderer;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.uv.TVertexModelElementRenderer;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Vertex;

public final class GeosetVertexSelectionManager extends AbstractSelectionManager<GeosetVertex> {

	@Override
	public Set<Triangle> getSelectedFaces() {
		final Set<Triangle> faces = new HashSet<>();
		final Set<GeosetVertex> selectedVertices = new HashSet<>();
		final Set<Triangle> partiallySelectedFaces = new HashSet<>();
		for (final GeosetVertex vertex : getSelection()) {
			partiallySelectedFaces.addAll(vertex.getTriangles());
			selectedVertices.add(vertex);
		}
		for (final Triangle face : partiallySelectedFaces) {
			boolean whollySelected = true;
			for (final GeosetVertex gv : face.getVerts()) {
				if (!selectedVertices.contains(gv)) {
					whollySelected = false;
				}
			}
			if (whollySelected) {
				faces.add(face);
			}
		}
		return faces;
	}

	@Override
	public Vertex getCenter() {
		return Vertex.centerOfGroup(selection);
	}

	@Override
	public double getCircumscribedSphereRadius(final Vertex sphereCenter) {
		double radius = 0;
		for (final Vertex item : selection) {
			final double distance = sphereCenter.distance(item);
			if (distance >= radius) {
				radius = distance;
			}
		}
		return radius;
	}

	@Override
	public void renderSelection(final ModelElementRenderer renderer, final CoordinateSystem coordinateSystem,
								final ModelView model, final ProgramPreferences programPreferences) {
		for (final Geoset geo : model.getEditableGeosets()) {
			final List<GeosetVertex> vertices = geo.getVertices();
			for (final GeosetVertex geosetVertex : vertices) {
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
	public Collection<? extends Vertex> getSelectedVertices() {
		return getSelection();
	}

	@Override
	public TVertex getUVCenter(final int tvertexLayerId) {
		return TVertex.centerOfGroup(getSelectedTVertices(tvertexLayerId));
	}

	@Override
	public Collection<? extends TVertex> getSelectedTVertices(final int tvertexLayerId) {
		final Set<TVertex> selectedTVertices = new HashSet<>();
		for (final GeosetVertex vertex : selection) {
			if (tvertexLayerId < vertex.getTverts().size()) {
				selectedTVertices.add(vertex.getTVertex(tvertexLayerId));
			}
		}
		return selectedTVertices;
	}

	@Override
	public double getCircumscribedSphereRadius(final TVertex center, final int tvertexLayerId) {
		double radius = 0;
		for (final GeosetVertex item : selection) {
			if (tvertexLayerId < item.getTverts().size()) {
				final double distance = center.distance(item.getTVertex(tvertexLayerId));
				if (distance >= radius) {
					radius = distance;
				}
			}
		}
		return radius;
	}

	@Override
	public void renderUVSelection(final TVertexModelElementRenderer renderer, final ModelView modelView,
                                  final ProgramPreferences programPreferences, final int tvertexLayerId) {
		for (final Geoset geo : modelView.getEditableGeosets()) {
			final List<GeosetVertex> vertices = geo.getVertices();
			for (final GeosetVertex geosetVertex : vertices) {
				if (tvertexLayerId >= geosetVertex.getTverts().size()) {
					continue;
				}
				if (modelView.getHighlightedGeoset() == geo) {
					renderer.renderVertex(programPreferences.getHighlighVertexColor(),
							geosetVertex.getTVertex(tvertexLayerId));
				} else if (selection.contains(geosetVertex)) {
					renderer.renderVertex(programPreferences.getSelectColor(), geosetVertex.getTVertex(tvertexLayerId));
				} else {
					renderer.renderVertex(programPreferences.getVertexColor(), geosetVertex.getTVertex(tvertexLayerId));
				}
			}
		}
	}
}
