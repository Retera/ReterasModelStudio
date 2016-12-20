package com.hiveworkshop.wc3.gui.modeledit.selection;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modeledit.MDLDisplay;
import com.hiveworkshop.wc3.gui.modeledit.toolbar.ToolbarButtonGroup;
import com.hiveworkshop.wc3.gui.modeledit.toolbar.ToolbarButtonListener;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.Vertex;

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
	private final List<SelectionListener> listeners;

	public ModelSelectionManager(final MDLDisplay model, final int vertexSize,
			final ToolbarButtonGroup<SelectionItemTypes> notififer) {
		this.model = model;
		this.vertexSize = vertexSize;
		selectableItems = new ArrayList<>();
		selection = new ArrayList<>();
		notififer.addToolbarButtonListener(this);
		listeners = new ArrayList<>();
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
		selection.clear();
		for (final SelectionItem item : selectionItem) {
			selection.add(item);
		}
	}

	@Override
	public void addSelection(final List<SelectionItem> selectionItem) {
		for (final SelectionItem item : selectionItem) {
			selection.add(item);
		}
	}

	@Override
	public void removeSelection(final List<SelectionItem> selectionItem) {
		for (final SelectionItem item : selectionItem) {
			selection.remove(item);
		}
	}

	@Override
	public void typeChanged(final SelectionItemTypes newItemType) {
		switch (newItemType) {
		case FACE:
			break;
		case GROUP:
			break;
		case VERTEX:
			final List<SelectionItem> selectableItems = new ArrayList<>();
			for (final Geoset geoset : model.getEditableGeosets()) {
				for (final GeosetVertex vertex : geoset.getVertices()) {
					selectableItems.add(new VertexItem(vertex));
				}
			}
			this.selectableItems = selectableItems;
			break;
		}
		this.currentItemType = newItemType;
	}

	@Override
	public void render(final Graphics2D graphics, final CoordinateSystem coordinateSystem) {
		for (final SelectionItem item : selectableItems) {
			item.render(graphics, coordinateSystem);
		}
	}

	private final class VertexItem implements SelectionItem {
		private final Vertex vertex;

		public VertexItem(final Vertex vertex) {
			this.vertex = vertex;
		}

		@Override
		public void render(final Graphics2D graphics, final CoordinateSystem coordinateSystem) {
			final double x = coordinateSystem.convertX(vertex.getCoord(coordinateSystem.getPortFirstXYZ()));
			final double y = coordinateSystem.convertY(vertex.getCoord(coordinateSystem.getPortSecondXYZ()));
			if (selection.contains(this)) {
				graphics.setColor(Color.RED);
			} else {
				graphics.setColor(model.getProgramPreferences().getVertexColor());
			}
			graphics.fillRect((int) x - vertexSize / 2, (int) y - vertexSize / 2, vertexSize, vertexSize);
		}

		@Override
		public boolean hitTest(final Point2D point, final CoordinateSystem coordinateSystem) {
			final double x = coordinateSystem.convertX(vertex.getCoord(coordinateSystem.getPortFirstXYZ()));
			final double y = coordinateSystem.convertY(vertex.getCoord(coordinateSystem.getPortSecondXYZ()));
			return point.distance(x, y) <= vertexSize / 2.0;
		}

		@Override
		public boolean hitTest(final Rectangle rectangle, final CoordinateSystem coordinateSystem) {
			final double x = coordinateSystem.convertX(vertex.getCoord(coordinateSystem.getPortFirstXYZ()));
			final double y = coordinateSystem.convertY(vertex.getCoord(coordinateSystem.getPortSecondXYZ()));
			return rectangle.contains(x, y);
		}

		@Override
		public void delete() {
			selection.remove(this);
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

		@Override
		public Vertex getCenter() {
			return vertex;
		}

	}

	@Override
	public void addSelectionListener(final SelectionListener listener) {
		listeners.add(listener);
	}
}
