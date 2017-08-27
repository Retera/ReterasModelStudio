package com.hiveworkshop.wc3.gui.modeledit.manipulator;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import com.hiveworkshop.wc3.gui.ProgramPreferences;
import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modeledit.viewport.NodeIconPalette;
import com.hiveworkshop.wc3.mdl.Bone;
import com.hiveworkshop.wc3.mdl.Camera;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.IdObject;
import com.hiveworkshop.wc3.mdl.Triangle;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.v2.ModelView;

public class VertexModelEditor extends AbstractModelEditor<Vertex> implements ModelEditor<Vertex> {
	private final ModelView model;
	private final ProgramPreferences programPreferences;

	public VertexModelEditor(final ModelView model, final ProgramPreferences programPreferences) {
		this.model = model;
		this.programPreferences = programPreferences;
	}

	@Override
	public Vertex getSelectionCenter() {
		return Vertex.centerOfGroup(selection);
	}

	@Override
	public void expandSelection() {
		final ArrayList<Vertex> oldSelection = new ArrayList<>(selection);
		for (final Vertex v : oldSelection) {
			if (v instanceof GeosetVertex) {
				final GeosetVertex gv = (GeosetVertex) v;
				expandSelection(gv);
			}
		}
	}

	private void expandSelection(final GeosetVertex currentVertex) {
		selection.add(currentVertex);
		for (final Triangle tri : currentVertex.getTriangles()) {
			for (final GeosetVertex other : tri.getVerts()) {
				if (!selection.contains(other)) {
					expandSelection(other);
				}
			}
		}
	}

	@Override
	public void invertSelection() {
		for (final Geoset geo : model.getEditableGeosets()) {
			for (final GeosetVertex geosetVertex : geo.getVertices()) {
				toggleSelection(geosetVertex);
			}
		}
		for (final IdObject object : model.getEditableIdObjects()) {
			toggleSelection(object.getPivotPoint());
		}
		for (final Camera object : model.getEditableCameras()) {
			toggleSelection(object.getPosition());
			toggleSelection(object.getTargetPosition());
		}
	}

	private void toggleSelection(final Vertex position) {
		if (selection.contains(position)) {
			selection.remove(position);
		} else {
			selection.add(position);
		}
	}

	@Override
	public void selectAll() {
		for (final Geoset geo : model.getEditableGeosets()) {
			for (final GeosetVertex geosetVertex : geo.getVertices()) {
				selection.add(geosetVertex);
			}
		}
		for (final IdObject object : model.getEditableIdObjects()) {
			selection.add(object.getPivotPoint());
		}
		for (final Camera object : model.getEditableCameras()) {
			selection.add(object.getPosition());
			selection.add(object.getTargetPosition());
		}
	}

	@Override
	public void translate(final float x, final float y, final float z) {
		for (final Vertex vertex : selection) {
			vertex.translate(x, y, z);
		}
	}

	@Override
	public void scale(final float centerX, final float centerY, final float centerZ, final float scaleX,
			final float scaleY, final float scaleZ) {
		for (final Vertex vertex : selection) {
			vertex.scale(centerX, centerY, centerZ, scaleX, scaleY, scaleZ);
		}
	}

	@Override
	public void rotate2d(final float centerX, final float centerY, final float centerZ, final float radians,
			final byte firstXYZ, final byte secondXYZ) {
		for (final Vertex vertex : selection) {
			vertex.rotate(centerX, centerY, centerZ, radians, firstXYZ, secondXYZ);
		}
	}

	@Override
	public void rotate3d(final Vertex center, final Vertex axis, final float radians) {
		for (final Vertex vertex : selection) {
			Vertex.rotateVertex(center, axis, radians, vertex);
		}
	}

	@Override
	public void renderSelection(final ModelElementRenderer renderer, final CoordinateSystem coordinateSystem) {
		// for (final Geoset geo : model.getEditableGeosets()) {
		// final GeosetVisitor geosetRenderer = renderer.beginGeoset(null,
		// null);
		// for (final Triangle triangle : geo.getTriangle()) {
		// final TriangleVisitor triangleRenderer =
		// geosetRenderer.beginTriangle();
		// for (final GeosetVertex geosetVertex : triangle.getVerts()) {
		// if (selection.contains(geosetVertex)) {
		// final VertexVisitor vertexRenderer =
		// triangleRenderer.vertex(geosetVertex.x, geosetVertex.y,
		// geosetVertex.z, geosetVertex.getNormal().x,
		// geosetVertex.getNormal().y,
		// geosetVertex.getNormal().z, geosetVertex.getBoneAttachments());
		// vertexRenderer.vertexFinished();
		// }
		// }
		// triangleRenderer.triangleFinished();
		// }
		// geosetRenderer.geosetFinished();
		// }
		// for (final IdObject object : model.getEditableIdObjects()) {
		// if (selection.contains(object.getPivotPoint())) {
		// object.apply(renderer);
		// }
		// }
		// for (final Camera camera : model.getEditableCameras()) {
		// if (selection.contains(camera.getPosition())) {
		// renderer.camera(camera);
		// }
		// if (selection.contains(camera.getTargetPosition())) {
		// renderer.camera(camera);
		// }
		// }
		for (final Geoset geo : model.getEditableGeosets()) {
			for (final Triangle triangle : geo.getTriangle()) {
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
				renderer.renderIdObject(object, NodeIconPalette.SELECTED);
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
	public void cloneSelectedComponents() {
		final List<Vertex> source = new ArrayList<>(selection);

		final ArrayList<GeosetVertex> vertCopies = new ArrayList<>();
		final ArrayList<Triangle> selTris = new ArrayList<>();
		final ArrayList<IdObject> selBones = new ArrayList<>();
		final ArrayList<IdObject> newBones = new ArrayList<>();
		for (int i = 0; i < source.size(); i++) {
			final Vertex vert = source.get(i);
			if (vert.getClass() == GeosetVertex.class) {
				final GeosetVertex gv = (GeosetVertex) vert;
				vertCopies.add(new GeosetVertex(gv));
			} else {
				vertCopies.add(null);
			}
		}
		for (final IdObject b : model.getEditableIdObjects()) {
			if (source.contains(b.getPivotPoint()) && !selBones.contains(b)) {
				selBones.add(b);
				newBones.add(b.copy());
			}
		}
		final ArrayList<Triangle> newTriangles = new ArrayList<>();
		for (int k = 0; k < source.size(); k++) {
			final Vertex vert = source.get(k);
			if (vert.getClass() == GeosetVertex.class) {
				final GeosetVertex gv = (GeosetVertex) vert;
				final ArrayList<Triangle> gvTriangles = new ArrayList<>();// gv.getTriangles());
				for (final Triangle tri : gv.getGeoset().getTriangle()) {
					if (tri.contains(gv)) {
						boolean good = true;
						for (final Vertex vTemp : tri.getAll()) {
							if (!source.contains(vTemp)) {
								good = false;
								break;
							}
						}
						if (good) {
							gvTriangles.add(tri);
							if (!selTris.contains(tri)) {
								selTris.add(tri);
							}
						}
					}
				}
			}
		}
		for (final Triangle tri : selTris) {
			final GeosetVertex a = vertCopies.get(source.indexOf(tri.get(0)));
			final GeosetVertex b = vertCopies.get(source.indexOf(tri.get(1)));
			final GeosetVertex c = vertCopies.get(source.indexOf(tri.get(2)));
			newTriangles.add(new Triangle(a, b, c, a.getGeoset()));
		}
		for (final GeosetVertex gv : vertCopies) {
			if (gv != null) {
				model.getModel().add(gv);
			}
		}
		for (final Triangle tri : newTriangles) {
			if (tri != null) {
				model.getModel().add(tri);
			}
			tri.forceVertsUpdate();
		}
		for (final IdObject b : newBones) {
			if (b != null) {
				model.getModel().add(b);
			}
		}
		selection.clear();
		for (final Vertex ver : vertCopies) {
			if (ver != null) {
				selection.add(ver);
				if (ver.getClass() == GeosetVertex.class) {
					final GeosetVertex gv = (GeosetVertex) ver;
					for (int i = 0; i < gv.getBones().size(); i++) {
						final Bone b = gv.getBones().get(i);
						if (selBones.contains(b)) {
							gv.getBones().set(i, (Bone) newBones.get(selBones.indexOf(b)));
						}
					}
				}
			}
		}
		for (final IdObject b : newBones) {
			selection.add(b.getPivotPoint());
			if (selBones.contains(b.getParent())) {
				b.setParent(newBones.get(selBones.indexOf(b.getParent())));
			}
		}
	}

	@Override
	protected List<Vertex> genericSelect(final Rectangle region, final CoordinateSystem coordinateSystem) {
		final List<Vertex> selectedItems = new ArrayList<>();
		final double startingClickX = coordinateSystem.geomX(region.getX());
		final double startingClickY = coordinateSystem.geomY(region.getY());
		final double endingClickX = coordinateSystem.geomX(region.getX() + region.getWidth());
		final double endingClickY = coordinateSystem.geomY(region.getY() + region.getHeight());

		final double minX = Math.min(startingClickX, endingClickX);
		final double minY = Math.min(startingClickY, endingClickY);
		final double maxX = Math.max(startingClickX, endingClickX);
		final double maxY = Math.max(startingClickY, endingClickY);
		final Rectangle2D area = new Rectangle2D.Double(minX, minY, (maxX - minX), (maxY - minY));
		for (final Geoset geoset : model.getEditableGeosets()) {
			for (final GeosetVertex geosetVertex : geoset.getVertices()) {
				hitTest(coordinateSystem, selectedItems, area, geosetVertex);
			}
		}
		for (final IdObject object : model.getEditableIdObjects()) {
			hitTest(coordinateSystem, selectedItems, area, object.getPivotPoint());
		}
		return selectedItems;
	}

	private static void hitTest(final CoordinateSystem coordinateSystem, final List<Vertex> selectedItems,
			final Rectangle2D area, final Vertex geosetVertex) {
		final double vertexX = geosetVertex.getCoord(coordinateSystem.getPortFirstXYZ());
		final double vertexY = geosetVertex.getCoord(coordinateSystem.getPortSecondXYZ());
		if (distance(vertexX, vertexY, area.getX(), area.getY()) <= 1.5f
				|| distance(vertexX, vertexY, area.getX() + area.getWidth(), area.getY() + area.getHeight()) <= 1.5f
				|| area.contains(vertexX, vertexY)) {
			selectedItems.add(geosetVertex);
		}
	}

	private static double distance(final double vertexX, final double vertexY, final double x, final double y) {
		final double dx = x - vertexX;
		final double dy = y - vertexY;
		return Math.sqrt(dx * dx + dy * dy);
	}

}
