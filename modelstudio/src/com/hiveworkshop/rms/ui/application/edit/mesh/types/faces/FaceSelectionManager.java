package com.hiveworkshop.rms.ui.application.edit.mesh.types.faces;

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

import java.awt.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class FaceSelectionManager extends SelectionManager<Triangle> {
	private static final Color FACE_SELECTED_COLOR = new Color(1f, 0.45f, 0.45f, 0.3f);
	private static final Color FACE_HIGHLIGHT_COLOR = new Color(0.45f, 1f, 0.45f, 0.3f);
	private static final Color FACE_NOT_SELECTED_COLOR = new Color(0.45f, 0.45f, 1f, 0.3f);

	@Override
	public Vec3 getCenter() {
		final Set<Vec3> selectedVertices = new HashSet<>();
		for (final Triangle triangle : selection) {
			selectedVertices.addAll(Arrays.asList(triangle.getVerts()));
		}
		return Vec3.centerOfGroup(selectedVertices);
	}

	@Override
	public Set<Vec3> getSelectedVertices() {
		final Set<Vec3> vertices = new HashSet<>();
		for (final Triangle triangle : getSelection()) {
            vertices.addAll(Arrays.asList(triangle.getVerts()));
		}
		return vertices;
	}

	@Override
	public Collection<Triangle> getSelectedFaces() {
		return getSelection();
	}

	@Override
	public void renderSelection(final ModelElementRenderer renderer, final CoordinateSystem coordinateSystem,
								final ModelView modelView, final ProgramPreferences programPreferences) {
		for (final Geoset geoset : modelView.getEditableGeosets()) {
			for (final Triangle triangle : geoset.getTriangles()) {
				final Color outlineColor;
				final Color fillColor;
				if (geoset == modelView.getHighlightedGeoset()) {
					outlineColor = programPreferences.getHighlighTriangleColor();
					fillColor = FACE_HIGHLIGHT_COLOR;
				} else if (selection.contains(triangle)) {
					outlineColor = programPreferences.getSelectColor();
					fillColor = FACE_SELECTED_COLOR;
				} else {
					outlineColor = Color.BLUE;
					fillColor = FACE_NOT_SELECTED_COLOR;
					continue;
				}
//				renderer.renderFace(outlineColor, fillColor, triangle);
				renderer.renderFace(outlineColor, fillColor, triangle.get(0), triangle.get(1), triangle.get(2));
			}
		}
	}

	@Override
	public double getCircumscribedSphereRadius(final Vec3 sphereCenter) {
		double radius = 0;
		for (final Triangle item : selection) {
			for (final GeosetVertex geosetVertex : item.getVerts()) {
				final double distance = sphereCenter.distance(geosetVertex);
				if (distance >= radius) {
					radius = distance;
				}
			}
		}
		return radius;
	}

	@Override
	public double getCircumscribedSphereRadius(final Vec2 center, final int tvertexLayerId) {
		double radius = 0;
		for (final Triangle item : selection) {
			for (final GeosetVertex geosetVertex : item.getVerts()) {
				final double distance = center.distance(geosetVertex.getTVertex(tvertexLayerId));
				if (distance >= radius) {
					radius = distance;
				}
			}
		}
		return radius;
	}

	@Override
	public Vec2 getUVCenter(final int tvertexLayerId) {
		final Set<Vec2> selectedVertices = new HashSet<>();
		for (final Triangle triangle : selection) {
			for (final GeosetVertex geosetVertex : triangle.getVerts()) {
				if (tvertexLayerId < geosetVertex.getTverts().size()) {
					selectedVertices.add(geosetVertex.getTVertex(tvertexLayerId));
				}
			}
		}
		return Vec2.centerOfGroup(selectedVertices);
	}

	@Override
	public Collection<? extends Vec2> getSelectedTVertices(final int tvertexLayerId) {
		final Set<Vec2> selectedVertices = new HashSet<>();
		for (final Triangle triangle : selection) {
			for (final GeosetVertex geosetVertex : triangle.getVerts()) {
				if (tvertexLayerId < geosetVertex.getTverts().size()) {
					selectedVertices.add(geosetVertex.getTVertex(tvertexLayerId));
				}
			}
		}
		return selectedVertices;
	}

	@Override
	public void renderUVSelection(final TVertexModelElementRenderer renderer, final ModelView modelView, final ProgramPreferences programPreferences, final int tvertexLayerId) {
		for (final Geoset geoset : modelView.getEditableGeosets()) {
			for (final Triangle triangle : geoset.getTriangles()) {
				final Color outlineColor;
				final Color fillColor;
				if (geoset == modelView.getHighlightedGeoset()) {
					outlineColor = programPreferences.getHighlighTriangleColor();
					fillColor = FACE_HIGHLIGHT_COLOR;
				} else if (selection.contains(triangle)) {
					outlineColor = programPreferences.getSelectColor();
					fillColor = FACE_SELECTED_COLOR;
				} else {
					outlineColor = Color.BLUE;
					fillColor = FACE_NOT_SELECTED_COLOR;
					continue;
				}
				if ((tvertexLayerId < triangle.get(0).getTverts().size())
						&& (tvertexLayerId < triangle.get(1).getTverts().size())
						&& (tvertexLayerId < triangle.get(2).getTverts().size())) {
					renderer.renderFace(outlineColor, fillColor, triangle.get(0).getTVertex(tvertexLayerId), triangle.get(1).getTVertex(tvertexLayerId), triangle.get(2).getTVertex(tvertexLayerId));
				}
			}
		}
	}

}
