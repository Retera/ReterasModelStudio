package com.hiveworkshop.wc3.gui.modeledit.selection;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modeledit.MDLDisplay;
import com.hiveworkshop.wc3.gui.modeledit.toolbar.ToolbarButtonGroup;
import com.hiveworkshop.wc3.gui.modeledit.toolbar.ToolbarButtonListener;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.Triangle;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.util.Callback;

/**
 * Need to get this code out of MDLDisplay!
 *
 * @author Eric
 *
 */
public final class ModelSelectionManager implements SelectionManager, ToolbarButtonListener<SelectionItemTypes> {
	private List<? extends SelectionItem> selectableItems;
	private final List<SelectionItem> selection;
	private final MDLDisplay model;
	private SelectionItemTypes currentItemType;
	private final int vertexSize;
	private final Set<SelectionListener> listeners;

	private static final Color FACE_HIGHLIGHT_COLOR = new Color(1f, 0.45f, 0.45f, 0.3f);

	public ModelSelectionManager(final MDLDisplay model, final int vertexSize,
			final ToolbarButtonGroup<SelectionItemTypes> notififer) {
		this.model = model;
		this.vertexSize = vertexSize;
		selectableItems = new ArrayList<>();
		selection = new ArrayList<>();
		listeners = new HashSet<>();
		notififer.addToolbarButtonListener(this);
	}

	@Override
	public List<Triangle> getSelectedFaces() {
		final List<Triangle> faces = new ArrayList<>();
		// TODO fix this design hack
		if (currentItemType == SelectionItemTypes.VERTEX) {
			final Set<GeosetVertex> selectedVertices = new HashSet<>();
			final Set<Triangle> partiallySelectedFaces = new HashSet<>();
			for (final SelectionItem item : selection) {
				if (!(item instanceof VertexItem)) {
					throw new RuntimeException("Selection manager type failure");
				}
				final VertexItem vertexItem = (VertexItem) item;
				final Vertex vertex = vertexItem.vertexComponent.vertex;
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
		} else if (currentItemType == SelectionItemTypes.FACE) {
			for (final SelectionItem item : selection) {
				if (!(item instanceof FaceItem)) {
					throw new RuntimeException("Selection manager type failure");
				}
				final FaceItem faceItem = (FaceItem) item;
				faces.add(faceItem.triangle);
			}
		}
		return faces;
	}

	@Override
	public List<SelectionItem> getSelection() {
		return selection;
	}

	@Override
	public List<? extends SelectionItem> getSelectableItems() {
		return selectableItems;
	}

	@Override
	public void setSelection(final List<SelectionItem> selectionItem) {
		final ArrayList<SelectionItem> previousSelection = new ArrayList<>(selection);
		selection.clear();
		for (final SelectionItem item : selectionItem) {
			selection.add(item);
		}
		for (final SelectionListener listener : listeners) {
			listener.onSelectionChanged(previousSelection, selection);
		}
	}

	@Override
	public void addSelection(final List<SelectionItem> selectionItem) {
		final ArrayList<SelectionItem> previousSelection = new ArrayList<>(selection);
		for (final SelectionItem item : selectionItem) {
			selection.add(item);
		}
		for (final SelectionListener listener : listeners) {
			listener.onSelectionChanged(previousSelection, selection);
		}
	}

	@Override
	public void removeSelection(final List<SelectionItem> selectionItem) {
		final ArrayList<SelectionItem> previousSelection = new ArrayList<>(selection);
		for (final SelectionItem item : selectionItem) {
			selection.remove(item);
		}
		for (final SelectionListener listener : listeners) {
			listener.onSelectionChanged(previousSelection, selection);
		}
	}

	@Override
	public void typeChanged(final SelectionItemTypes newItemType) {
		final ArrayList<SelectionItem> previousSelection = new ArrayList<>(selection);
		final List<SelectionItem> selectableItems = new ArrayList<>();
		switch (newItemType) {
		case FACE:
			for (final Geoset geoset : model.getEditableGeosets()) {
				for (final Triangle triangle : geoset.getTriangle()) {
					selectableItems.add(new FaceItem(triangle));
				}
			}
			this.selectableItems = selectableItems;
			break;
		case GROUP:
			break;
		case VERTEX:
			for (final Geoset geoset : model.getEditableGeosets()) {
				for (final GeosetVertex vertex : geoset.getVertices()) {
					selectableItems.add(new VertexItem(vertex));
				}
			}
			this.selectableItems = selectableItems;
			break;
		}
		this.currentItemType = newItemType;
		for (final SelectionListener listener : listeners) {
			listener.onSelectionChanged(previousSelection, selection);
		}
	}

	@Override
	public void render(final Graphics2D graphics, final CoordinateSystem coordinateSystem) {
		for (final SelectionItem item : selectableItems) {
			item.render(graphics, coordinateSystem);
		}
	}

	private final class VertexItem implements SelectionItem {
		private final VertexComponent vertexComponent;

		public VertexItem(final Vertex vertex) {
			this.vertexComponent = new VertexComponent(vertex);
		}

		@Override
		public void render(final Graphics2D graphics, final CoordinateSystem coordinateSystem) {
			final double x = coordinateSystem
					.convertX(vertexComponent.getVertex().getCoord(coordinateSystem.getPortFirstXYZ()));
			final double y = coordinateSystem
					.convertY(vertexComponent.getVertex().getCoord(coordinateSystem.getPortSecondXYZ()));
			if (selection.contains(this)) {
				graphics.setColor(Color.RED);
			} else {
				graphics.setColor(model.getProgramPreferences().getVertexColor());
			}
			graphics.fillRect((int) x - vertexSize / 2, (int) y - vertexSize / 2, vertexSize, vertexSize);
		}

		@Override
		public boolean hitTest(final Point2D point, final CoordinateSystem coordinateSystem) {
			final double x = coordinateSystem
					.convertX(vertexComponent.getVertex().getCoord(coordinateSystem.getPortFirstXYZ()));
			final double y = coordinateSystem
					.convertY(vertexComponent.getVertex().getCoord(coordinateSystem.getPortSecondXYZ()));
			final double px = coordinateSystem.convertX(point.getX());
			final double py = coordinateSystem.convertY(point.getY());
			return Point2D.distance(px, py, x, y) <= vertexSize / 2.0;
		}

		@Override
		public boolean hitTest(final Rectangle2D rectangle, final CoordinateSystem coordinateSystem) {
			final double x = /* coordinateSystem.convertX */(vertexComponent.getVertex()
					.getCoord(coordinateSystem.getPortFirstXYZ()));
			final double y = /* coordinateSystem.convertY */(vertexComponent.getVertex()
					.getCoord(coordinateSystem.getPortSecondXYZ()));
			return rectangle.contains(x, y);
		}

		@Override
		public Vertex getCenter() {
			return vertexComponent.getVertex();
		}

		@Override
		public void forEachComponent(final Callback<MutableSelectionComponent> callback) {
			callback.run(vertexComponent);
		}
	}

	private static final class VertexComponent implements MutableSelectionComponent {
		private final Vertex vertex;

		public VertexComponent(final Vertex vertex) {
			this.vertex = vertex;
		}

		@Override
		public void translate(final float x, final float y, final float z) {
			vertex.x += x;
			vertex.y += y;
			vertex.z += z;
		}

		@Override
		public void scale(final float centerX, final float centerY, final float centerZ, final float x, final float y,
				final float z) {
			final double dx = vertex.x - centerX;
			final double dy = vertex.y - centerY;
			final double dz = vertex.z - centerZ;
			vertex.x = centerX + dx * x;
			vertex.y = centerY + dy * y;
			vertex.z = centerZ + dz * z;
		}

		@Override
		public void rotate(final float centerX, final float centerY, final float centerZ, final float radians,
				final CoordinateSystem coordinateSystem) {
			rotateVertex(centerX, centerY, centerZ, radians, coordinateSystem, vertex);
		}

		@Override
		public void delete() {
			throw new UnsupportedOperationException("delete is nyi");
		}

		public Vertex getVertex() {
			return vertex;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((vertex == null) ? 0 : vertex.hashCode());
			return result;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final VertexComponent other = (VertexComponent) obj;
			if (vertex == null) {
				if (other.vertex != null) {
					return false;
				}
			} else if (!vertex.equals(other.vertex)) {
				return false;
			}
			return true;
		}

		@Override
		public void rotate(final Vertex center, final Vertex perpendicular, final float radians) {
			rotateVertex(center, perpendicular, radians, vertex);
		}

	}

	private final class FaceItem implements SelectionItem {
		private final Triangle triangle;
		// single-threaded, cached
		private final int[] xpts;
		private final int[] ypts;
		private final MutableSelectionComponent[] vertexComponents;

		public FaceItem(final Triangle triangle) {
			this.triangle = triangle;
			xpts = new int[3];
			ypts = new int[3];
			vertexComponents = new MutableSelectionComponent[3];
			for (int i = 0; i < 3; i++) {
				// TODO fix to not be full-fledged vertex item, for efficiency??
				// doesn't need SelectionItem interface
				vertexComponents[i] = new VertexComponent(triangle.get(i));
			}
		}

		@Override
		public void render(final Graphics2D graphics, final CoordinateSystem coordinateSystem) {
			if (selection.contains(this)) {
				final GeosetVertex[] verts = triangle.getVerts();
				for (int i = 0; i < verts.length; i++) {
					xpts[i] = (int) coordinateSystem.convertX(verts[i].getCoord(coordinateSystem.getPortFirstXYZ()));
					ypts[i] = (int) coordinateSystem.convertY(verts[i].getCoord(coordinateSystem.getPortSecondXYZ()));
				}
				graphics.setColor(FACE_HIGHLIGHT_COLOR);
				graphics.fillPolygon(xpts, ypts, 3);
				graphics.setColor(Color.RED);
				graphics.drawPolygon(xpts, ypts, 3);
			}
		}

		@Override
		public boolean hitTest(final Point2D point, final CoordinateSystem coordinateSystem) {
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

		@Override
		public boolean hitTest(final Rectangle2D rectangle, final CoordinateSystem coordinateSystem) {
			final GeosetVertex[] verts = triangle.getVerts();
			final Path2D.Double path = new Path2D.Double();
			path.moveTo(verts[0].getCoord(coordinateSystem.getPortFirstXYZ()),
					verts[0].getCoord(coordinateSystem.getPortSecondXYZ()));
			for (int i = 1; i < verts.length; i++) {
				path.lineTo(verts[i].getCoord(coordinateSystem.getPortFirstXYZ()),
						verts[i].getCoord(coordinateSystem.getPortSecondXYZ()));
			}
			return rectangle.contains(xpts[0], ypts[0]) || rectangle.contains(xpts[1], ypts[1])
					|| rectangle.contains(xpts[2], ypts[2]) || path.intersects(rectangle);
		}

		@Override
		public Vertex getCenter() {
			return Vertex.centerOfGroup(Arrays.asList(triangle.getVerts()));
		}

		// @Override
		// public void translate(final float x, final float y, final float z) {
		// for (final Vertex vertex : triangle.getVerts()) {
		// vertex.x += x;
		// vertex.y += y;
		// vertex.z += z;
		// }
		// }
		//
		// @Override
		// public void scale(final float centerX, final float centerY, final
		// float centerZ, final float x, final float y,
		// final float z) {
		// for (final Vertex vertex : triangle.getVerts()) {
		// final double dx = vertex.x - centerX;
		// final double dy = vertex.y - centerY;
		// final double dz = vertex.z - centerZ;
		// vertex.x = centerX + dx * x;
		// vertex.y = centerY + dy * y;
		// vertex.z = centerZ + dz * z;
		// }
		// }
		//
		// @Override
		// public void rotate(final float centerX, final float centerY, final
		// float centerZ, final float radians,
		// final CoordinateSystem coordinateSystem) {
		// for (final Vertex vertex : triangle.getVerts()) {
		// rotateVertex(centerX, centerY, centerZ, radians, coordinateSystem,
		// vertex);
		// }
		// }
		//
		// @Override
		// public void delete() {
		// selection.remove(this);
		// }

		@Override
		public void forEachComponent(final Callback<MutableSelectionComponent> callback) {
			for (final MutableSelectionComponent component : vertexComponents) {
				callback.run(component);
			}
		}

	}

	@Override
	public void addSelectionListener(final SelectionListener listener) {
		listeners.add(listener);
	}

	public static void rotateVertex(final float centerX, final float centerY, final float centerZ, final float radians,
			final CoordinateSystem coordinateSystem, final Vertex vertex) {
		final double x1 = vertex.getCoord(coordinateSystem.getPortFirstXYZ());
		final double y1 = vertex.getCoord(coordinateSystem.getPortSecondXYZ());
		final double cx;// = coordinateSystem.geomX(centerX);
		switch (coordinateSystem.getPortFirstXYZ()) {
		case 0:
			cx = centerX;
			break;
		case 1:
			cx = centerY;
			break;
		default:
		case 2:
			cx = centerZ;
			break;
		}
		final double dx = x1 - cx;
		final double cy;// = coordinateSystem.geomY(centerY);
		switch (coordinateSystem.getPortSecondXYZ()) {
		case 0:
			cy = centerX;
			break;
		case 1:
			cy = centerY;
			break;
		default:
		case 2:
			cy = centerZ;
			break;
		}
		final double dy = y1 - cy;
		final double r = Math.sqrt(dx * dx + dy * dy);
		double verAng = Math.acos(dx / r);
		if (dy < 0) {
			verAng = -verAng;
		}
		// if( getDimEditable(dim1) )
		double nextDim = Math.cos(verAng + radians) * r + cx;
		if (!Double.isNaN(nextDim)) {
			vertex.setCoord(coordinateSystem.getPortFirstXYZ(), Math.cos(verAng + radians) * r + cx);
		}
		// if( getDimEditable(dim2) )
		nextDim = Math.sin(verAng + radians) * r + cy;
		if (!Double.isNaN(nextDim)) {
			vertex.setCoord(coordinateSystem.getPortSecondXYZ(), Math.sin(verAng + radians) * r + cy);
		}
	}

	public static void rotateVertex(final Vertex center, final Vertex axis, final float radians, final Vertex vertex) {
		final double centerX = center.x;
		final double centerY = center.y;
		final double centerZ = center.z;
		final double vertexX = vertex.x;
		final double vertexY = vertex.y;
		final double vertexZ = vertex.z;
		final double deltaX = vertexX - centerX;
		final double deltaY = vertexY - centerY;
		final double deltaZ = vertexZ - centerZ;
		double radiansToApply;
		final double twoPi = Math.PI * 2;
		if (radians > Math.PI) {
			radiansToApply = (radians - twoPi) % twoPi;
		} else if (radians <= -Math.PI) {
			radiansToApply = (radians + twoPi) % twoPi;
		} else {
			radiansToApply = radians;
		}
		final double cosRadians = Math.cos(radiansToApply);
		if (radiansToApply == Math.PI) {
			vertex.x = centerX - deltaX;
			vertex.y = centerY - deltaY;
			vertex.z = centerY - deltaZ;
		}
		final double resultDeltaX = vertexX * cosRadians;
		throw new UnsupportedOperationException("NYI");
	}
}
