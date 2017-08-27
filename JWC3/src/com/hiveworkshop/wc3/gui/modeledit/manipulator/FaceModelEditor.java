package com.hiveworkshop.wc3.gui.modeledit.manipulator;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.Triangle;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.v2.ModelView;

public class FaceModelEditor extends AbstractModelEditor<Triangle> implements ModelEditor<Triangle> {
	private static final Color FACE_HIGHLIGHT_COLOR = new Color(1f, 0.45f, 0.45f, 0.3f);
	private final ModelView model;

	public FaceModelEditor(final ModelView model) {
		this.model = model;
	}

	@Override
	public Vertex getSelectionCenter() {
		final Set<Vertex> selectedVertices = new HashSet<>();
		for (final Triangle triangle : selection) {
			for (final GeosetVertex geosetVertex : triangle.getVerts()) {
				selectedVertices.add(geosetVertex);
			}
		}
		return Vertex.centerOfGroup(selectedVertices);
	}

	@Override
	public void expandSelection() {
		for (final Triangle triangle : new ArrayList<>(selection)) {
			expandSelection(triangle);
		}
	}

	private void expandSelection(final Triangle currentTriangle) {
		selection.add(currentTriangle);
		for (final GeosetVertex geosetVertex : currentTriangle.getVerts()) {
			for (final Triangle triangle : geosetVertex.getTriangles()) {
				if (!selection.contains(triangle)) {
					expandSelection(triangle);
				}
			}
		}
	}

	@Override
	public void invertSelection() {
		for (final Geoset geoset : model.getEditableGeosets()) {
			for (final Triangle triangle : geoset.getTriangle()) {
				if (selection.contains(triangle)) {
					selection.remove(triangle);
				} else {
					selection.add(triangle);
				}
			}
		}
	}

	@Override
	public void selectAll() {
		for (final Geoset geoset : model.getEditableGeosets()) {
			for (final Triangle triangle : geoset.getTriangle()) {
				selection.add(triangle);
			}
		}
	}

	@Override
	public void translate(final float x, final float y, final float z) {
		final Set<Vertex> translatedVertices = new HashSet<>();
		for (final Triangle triangle : selection) {
			for (final Vertex vertex : triangle.getVerts()) {
				if (!translatedVertices.contains(vertex)) {
					vertex.translate(x, y, z);
					translatedVertices.add(vertex);
				}
			}
		}
	}

	@Override
	public void scale(final float centerX, final float centerY, final float centerZ, final float scaleX,
			final float scaleY, final float scaleZ) {
		final Set<Vertex> modifiedVertices = new HashSet<>();
		for (final Triangle triangle : selection) {
			for (final Vertex vertex : triangle.getVerts()) {
				if (!modifiedVertices.contains(vertex)) {
					vertex.scale(centerX, centerY, centerZ, scaleX, scaleY, scaleZ);
					modifiedVertices.add(vertex);
				}
			}
		}
	}

	@Override
	public void rotate2d(final float centerX, final float centerY, final float centerZ, final float radians,
			final byte firstXYZ, final byte secondXYZ) {
		final Set<Vertex> modifiedVertices = new HashSet<>();
		for (final Triangle triangle : selection) {
			for (final Vertex vertex : triangle.getVerts()) {
				if (!modifiedVertices.contains(vertex)) {
					vertex.rotate(centerX, centerY, centerZ, radians, firstXYZ, secondXYZ);
					modifiedVertices.add(vertex);
				}
			}
		}
	}

	@Override
	public void rotate3d(final Vertex center, final Vertex axis, final float radians) {
		final Set<Vertex> modifiedVertices = new HashSet<>();
		for (final Triangle triangle : selection) {
			for (final Vertex vertex : triangle.getVerts()) {
				if (!modifiedVertices.contains(vertex)) {
					Vertex.rotateVertex(center, axis, radians, vertex);
					modifiedVertices.add(vertex);
				}
			}
		}
	}

	@Override
	public void renderSelection(final ModelElementRenderer renderer, final CoordinateSystem coordinateSystem) {
		// TODO
		// for (final Geoset geo : model.getEditableGeosets()) {
		// final GeosetVisitor geosetRenderer = renderer.beginGeoset(null,
		// null);
		// for (final Triangle triangle : geo.getTriangle()) {
		// if (selection.contains(triangle)) {
		// final TriangleVisitor triangleRenderer =
		// geosetRenderer.beginTriangle();
		// for (final GeosetVertex geosetVertex : triangle.getVerts()) {
		// final VertexVisitor vertexRenderer =
		// triangleRenderer.vertex(geosetVertex.x, geosetVertex.y,
		// geosetVertex.z, geosetVertex.getNormal().x,
		// geosetVertex.getNormal().y,
		// geosetVertex.getNormal().z, geosetVertex.getBoneAttachments());
		// vertexRenderer.vertexFinished();
		// }
		// triangleRenderer.triangleFinished();
		// }
		// }
		// geosetRenderer.geosetFinished();
		// }
		for (final Triangle triangle : selection) {
			renderer.renderFace(Color.RED, FACE_HIGHLIGHT_COLOR, triangle.get(0), triangle.get(1), triangle.get(2));
		}
	}

	@Override
	protected List<Triangle> genericSelect(final Rectangle region, final CoordinateSystem coordinateSystem) {
		final List<Triangle> newSelection = new ArrayList<>();
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
			for (final Triangle triangle : geoset.getTriangle()) {
				if (hitTest(triangle, new Point2D.Double(area.getX(), area.getY()), coordinateSystem) || hitTest(
						triangle, new Point2D.Double(area.getX() + area.getWidth(), area.getY() + area.getHeight()),
						coordinateSystem) || hitTest(triangle, area, coordinateSystem)) {
					newSelection.add(triangle);
				}
			}
		}
		return newSelection;
	}

	public boolean hitTest(final Triangle triangle, final Point2D point, final CoordinateSystem coordinateSystem) {
		final GeosetVertex[] verts = triangle.getVerts();
		final Path2D.Double path = new Path2D.Double();
		path.moveTo(verts[0].getCoord(coordinateSystem.getPortFirstXYZ()),
				verts[0].getCoord(coordinateSystem.getPortSecondXYZ()));
		for (int i = 1; i < verts.length; i++) {
			path.lineTo(verts[i].getCoord(coordinateSystem.getPortFirstXYZ()),
					verts[i].getCoord(coordinateSystem.getPortSecondXYZ()));
			// xpts[i] = (int)
			// (verts[i].getCoord(coordinateSystem.getPortFirstXYZ()));
			// ypts[i] = (int)
			// (verts[i].getCoord(coordinateSystem.getPortSecondXYZ()));
		} // TODO fix bad performance allocation
		path.closePath();
		return path.contains(point);
	}

	public boolean hitTest(final Triangle triangle, final Rectangle2D rectangle,
			final CoordinateSystem coordinateSystem) {
		final GeosetVertex[] verts = triangle.getVerts();
		final Path2D.Double path = new Path2D.Double();
		path.moveTo(verts[0].getCoord(coordinateSystem.getPortFirstXYZ()),
				verts[0].getCoord(coordinateSystem.getPortSecondXYZ()));
		for (int i = 1; i < verts.length; i++) {
			path.lineTo(verts[i].getCoord(coordinateSystem.getPortFirstXYZ()),
					verts[i].getCoord(coordinateSystem.getPortSecondXYZ()));
		}
		return rectangle.contains(verts[0].getCoord(coordinateSystem.getPortFirstXYZ()),
				verts[0].getCoord(coordinateSystem.getPortSecondXYZ()))
				|| rectangle.contains(verts[1].getCoord(coordinateSystem.getPortFirstXYZ()),
						verts[1].getCoord(coordinateSystem.getPortSecondXYZ()))
				|| rectangle.contains(verts[2].getCoord(coordinateSystem.getPortFirstXYZ()),
						verts[2].getCoord(coordinateSystem.getPortSecondXYZ()))
				|| path.intersects(rectangle);
	}

	@Override
	public void cloneSelectedComponents() {
		// TODO Auto-generated method stub

	}

}
