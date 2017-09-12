package com.hiveworkshop.wc3.gui.modeledit.newstuff;

import java.awt.Color;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.hiveworkshop.wc3.gui.ProgramPreferences;
import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modeledit.viewport.NodeIconPalette;
import com.hiveworkshop.wc3.mdl.Camera;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.IdObject;
import com.hiveworkshop.wc3.mdl.Triangle;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.v2.ModelView;

public final class VertexSelectionManager extends AbstractSelectionManager<Vertex> {

	@Override
	public Set<Triangle> getSelectedFaces() {
		final Set<Triangle> faces = new HashSet<>();
		final Set<GeosetVertex> selectedVertices = new HashSet<>();
		final Set<Triangle> partiallySelectedFaces = new HashSet<>();
		for (final Vertex vertex : getSelection()) {
			if (vertex instanceof GeosetVertex) {
				final GeosetVertex gv = (GeosetVertex) vertex;
				partiallySelectedFaces.addAll(gv.getTriangles());
				selectedVertices.add(gv);
			}
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
			for (final Triangle triangle : geo.getTriangles()) {
				for (final GeosetVertex geosetVertex : triangle.getVerts()) {
					if (selection.contains(geosetVertex)) {
						renderer.renderVertex(programPreferences.getSelectColor(), geosetVertex);
					} else {
						renderer.renderVertex(programPreferences.getVertexColor(), geosetVertex);
					}
				}
			}
		}
		for (final IdObject object : model.getEditableIdObjects()) {
			if (selection.contains(object.getPivotPoint())) {
				renderer.renderIdObject(object, NodeIconPalette.SELECTED, programPreferences.getSelectColor(),
						programPreferences.getPivotPointsSelectedColor());
			}
		}
		for (final Camera camera : model.getEditableCameras()) {
			renderer.renderCamera(
					selection.contains(camera.getPosition()) ? Color.GREEN.darker() : Color.ORANGE.darker(),
					camera.getPosition(),
					selection.contains(camera.getTargetPosition()) ? Color.GREEN.darker() : Color.ORANGE.darker(),
					camera.getTargetPosition());
		}
	}

	@Override
	public Collection<Vertex> getSelectedVertices() {
		return getSelection();
	}
}
