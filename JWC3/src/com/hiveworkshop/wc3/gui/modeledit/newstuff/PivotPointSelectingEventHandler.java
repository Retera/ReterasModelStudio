package com.hiveworkshop.wc3.gui.modeledit.newstuff;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.etheller.collections.ListView;
import com.hiveworkshop.wc3.gui.ProgramPreferences;
import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.selection.MakeNotEditableAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.selection.SetSelectionAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.listener.EditabilityToggleHandler;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectableComponent;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectableComponentVisitor;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.wc3.mdl.Attachment;
import com.hiveworkshop.wc3.mdl.Bone;
import com.hiveworkshop.wc3.mdl.Camera;
import com.hiveworkshop.wc3.mdl.CollisionShape;
import com.hiveworkshop.wc3.mdl.EventObject;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.Helper;
import com.hiveworkshop.wc3.mdl.IdObject;
import com.hiveworkshop.wc3.mdl.Light;
import com.hiveworkshop.wc3.mdl.ParticleEmitter;
import com.hiveworkshop.wc3.mdl.ParticleEmitter2;
import com.hiveworkshop.wc3.mdl.RibbonEmitter;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.v2.ModelView;
import com.hiveworkshop.wc3.mdl.v2.visitor.IdObjectVisitor;

public final class PivotPointSelectingEventHandler extends AbstractSelectingEventHandler<Vertex> {

	private final ModelView model;
	private final ProgramPreferences programPreferences;
	private final GenericSelectorVisitor genericSelectorVisitor;
	private final SelectionAtPointTester selectionAtPointTester;

	public PivotPointSelectingEventHandler(final SelectionManager<Vertex> selectionManager, final ModelView model,
			final ProgramPreferences programPreferences) {
		super(selectionManager);
		this.model = model;
		this.programPreferences = programPreferences;
		this.genericSelectorVisitor = new GenericSelectorVisitor();
		this.selectionAtPointTester = new SelectionAtPointTester();
	}

	@Override
	public UndoAction expandSelection() {
		final Set<Vertex> expandedSelection = new HashSet<>(selectionManager.getSelection());
		final Set<Vertex> oldSelection = new HashSet<>(selectionManager.getSelection());
		final IdObjectVisitor visitor = new IdObjectVisitor() {
			@Override
			public void ribbonEmitter(final RibbonEmitter particleEmitter) {

			}

			@Override
			public void particleEmitter2(final ParticleEmitter2 particleEmitter) {

			}

			@Override
			public void particleEmitter(final ParticleEmitter particleEmitter) {

			}

			@Override
			public void light(final Light light) {

			}

			@Override
			public void helper(final Helper object) {

			}

			@Override
			public void eventObject(final EventObject eventObject) {

			}

			@Override
			public void collisionShape(final CollisionShape collisionShape) {
				boolean selected = false;
				if (oldSelection.contains(collisionShape.getPivotPoint())) {
					selected = true;
				}
				for (final Vertex vertex : collisionShape.getVertices()) {
					if (oldSelection.contains(vertex)) {
						selected = true;
					}
				}
				if (selected) {
					expandedSelection.addAll(collisionShape.getVertices());
					expandedSelection.add(collisionShape.getPivotPoint());
				}
			}

			@Override
			public void camera(final Camera camera) {
				if (oldSelection.contains(camera.getTargetPosition()) || oldSelection.contains(camera.getPosition())) {
					expandedSelection.add(camera.getPosition());
					expandedSelection.add(camera.getTargetPosition());
				}
			}

			@Override
			public void bone(final Bone object) {

			}

			@Override
			public void attachment(final Attachment attachment) {

			}
		};
		for (final IdObject node : model.getEditableIdObjects()) {
			node.apply(visitor);
		}
		for (final Camera camera : model.getEditableCameras()) {
			visitor.camera(camera);
		}
		selectionManager.setSelection(expandedSelection);
		return (new SetSelectionAction<>(expandedSelection, oldSelection, selectionManager, "expand selection"));
	}

	@Override
	public UndoAction invertSelection() {
		final ArrayList<Vertex> oldSelection = new ArrayList<>(selectionManager.getSelection());
		final Set<Vertex> invertedSelection = new HashSet<>(selectionManager.getSelection());
		final IdObjectVisitor visitor = new IdObjectVisitor() {
			@Override
			public void ribbonEmitter(final RibbonEmitter particleEmitter) {
				toggleSelection(invertedSelection, particleEmitter.getPivotPoint());
			}

			@Override
			public void particleEmitter2(final ParticleEmitter2 particleEmitter) {
				toggleSelection(invertedSelection, particleEmitter.getPivotPoint());
			}

			@Override
			public void particleEmitter(final ParticleEmitter particleEmitter) {
				toggleSelection(invertedSelection, particleEmitter.getPivotPoint());
			}

			@Override
			public void light(final Light light) {
				toggleSelection(invertedSelection, light.getPivotPoint());
			}

			@Override
			public void helper(final Helper object) {
				toggleSelection(invertedSelection, object.getPivotPoint());
			}

			@Override
			public void eventObject(final EventObject eventObject) {
				toggleSelection(invertedSelection, eventObject.getPivotPoint());
			}

			@Override
			public void collisionShape(final CollisionShape collisionShape) {
				toggleSelection(invertedSelection, collisionShape.getPivotPoint());
				for (final Vertex vertex : collisionShape.getVertices()) {
					toggleSelection(invertedSelection, vertex);
				}
			}

			@Override
			public void camera(final Camera camera) {
				toggleSelection(invertedSelection, camera.getPosition());
				toggleSelection(invertedSelection, camera.getTargetPosition());
			}

			@Override
			public void bone(final Bone object) {
				toggleSelection(invertedSelection, object.getPivotPoint());
			}

			@Override
			public void attachment(final Attachment attachment) {
				toggleSelection(invertedSelection, attachment.getPivotPoint());
			}
		};
		for (final IdObject node : model.getEditableIdObjects()) {
			node.apply(visitor);
		}
		for (final Camera object : model.getEditableCameras()) {
			visitor.camera(object);
		}
		selectionManager.setSelection(invertedSelection);
		return (new SetSelectionAction<>(invertedSelection, oldSelection, selectionManager, "invert selection"));
	}

	private void toggleSelection(final Set<Vertex> selection, final Vertex position) {
		if (selection.contains(position)) {
			selection.remove(position);
		} else {
			selection.add(position);
		}
	}

	@Override
	public UndoAction selectAll() {
		final ArrayList<Vertex> oldSelection = new ArrayList<>(selectionManager.getSelection());
		final Set<Vertex> allSelection = new HashSet<>();
		final IdObjectVisitor visitor = new IdObjectVisitor() {
			@Override
			public void ribbonEmitter(final RibbonEmitter particleEmitter) {
				allSelection.add(particleEmitter.getPivotPoint());
			}

			@Override
			public void particleEmitter2(final ParticleEmitter2 particleEmitter) {
				allSelection.add(particleEmitter.getPivotPoint());
			}

			@Override
			public void particleEmitter(final ParticleEmitter particleEmitter) {
				allSelection.add(particleEmitter.getPivotPoint());
			}

			@Override
			public void light(final Light light) {
				allSelection.add(light.getPivotPoint());
			}

			@Override
			public void helper(final Helper object) {
				allSelection.add(object.getPivotPoint());
			}

			@Override
			public void eventObject(final EventObject eventObject) {
				allSelection.add(eventObject.getPivotPoint());
			}

			@Override
			public void collisionShape(final CollisionShape collisionShape) {
				allSelection.add(collisionShape.getPivotPoint());
				for (final Vertex vertex : collisionShape.getVertices()) {
					allSelection.add(vertex);
				}
			}

			@Override
			public void camera(final Camera camera) {
				allSelection.add(camera.getPosition());
				allSelection.add(camera.getTargetPosition());
			}

			@Override
			public void bone(final Bone object) {
				allSelection.add(object.getPivotPoint());
			}

			@Override
			public void attachment(final Attachment attachment) {
				allSelection.add(attachment.getPivotPoint());
			}
		};
		for (final IdObject node : model.getEditableIdObjects()) {
			node.apply(visitor);
		}
		for (final Camera object : model.getEditableCameras()) {
			visitor.camera(object);
		}
		selectionManager.setSelection(allSelection);
		return (new SetSelectionAction<>(allSelection, oldSelection, selectionManager, "select all"));
	}

	@Override
	protected List<Vertex> genericSelect(final Rectangle2D region, final CoordinateSystem coordinateSystem) {
		final List<Vertex> selectedItems = new ArrayList<>();
		final double startingClickX = region.getX();
		final double startingClickY = region.getY();
		final double endingClickX = region.getX() + region.getWidth();
		final double endingClickY = region.getY() + region.getHeight();

		final double minX = Math.min(startingClickX, endingClickX);
		final double minY = Math.min(startingClickY, endingClickY);
		final double maxX = Math.max(startingClickX, endingClickX);
		final double maxY = Math.max(startingClickY, endingClickY);
		final Rectangle2D area = new Rectangle2D.Double(minX, minY, (maxX - minX), (maxY - minY));
		final IdObjectVisitor visitor = genericSelectorVisitor.reset(selectedItems, area, coordinateSystem);
		for (final IdObject object : model.getEditableIdObjects()) {
			object.apply(visitor);
		}
		for (final Camera camera : model.getEditableCameras()) {
			visitor.camera(camera);
		}
		return selectedItems;
	}

	@Override
	public boolean canSelectAt(final Point point, final CoordinateSystem axes) {
		final boolean canSelect = false;
		final IdObjectVisitor visitor = selectionAtPointTester.reset(axes, point);
		for (final IdObject object : model.getEditableIdObjects()) {
			object.apply(visitor);
		}
		for (final Camera camera : model.getEditableCameras()) {
			visitor.camera(camera);
		}
		return canSelect;
	}

	public static void hitTest(final List<Vertex> selectedItems, final Rectangle2D area, final Vertex geosetVertex,
			final CoordinateSystem coordinateSystem, final double vertexSize) {
		final byte dim1 = coordinateSystem.getPortFirstXYZ();
		final byte dim2 = coordinateSystem.getPortSecondXYZ();
		final double minX = coordinateSystem.convertX(area.getMinX());
		final double minY = coordinateSystem.convertY(area.getMinY());
		final double maxX = coordinateSystem.convertX(area.getMaxX());
		final double maxY = coordinateSystem.convertY(area.getMaxY());
		final double vertexX = geosetVertex.getCoord(dim1);
		final double x = coordinateSystem.convertX(vertexX);
		final double vertexY = geosetVertex.getCoord(dim2);
		final double y = coordinateSystem.convertY(vertexY);
		if (distance(x, y, minX, minY) <= vertexSize / 2.0 || distance(x, y, maxX, maxY) <= vertexSize / 2.0
				|| area.contains(vertexX, vertexY)) {
			selectedItems.add(geosetVertex);
		}
	}

	public static boolean hitTest(final Vertex vertex, final Point2D point, final CoordinateSystem coordinateSystem,
			final double vertexSize) {
		final double x = coordinateSystem.convertX(vertex.getCoord(coordinateSystem.getPortFirstXYZ()));
		final double y = coordinateSystem.convertY(vertex.getCoord(coordinateSystem.getPortSecondXYZ()));
		final double px = coordinateSystem.convertX(point.getX());
		final double py = coordinateSystem.convertY(point.getY());
		return Point2D.distance(px, py, x, y) <= vertexSize / 2.0;
	}

	public static double distance(final double vertexX, final double vertexY, final double x, final double y) {
		final double dx = x - vertexX;
		final double dy = y - vertexY;
		return Math.sqrt(dx * dx + dy * dy);
	}

	@Override
	protected UndoAction buildHideComponentAction(final ListView<? extends SelectableComponent> selectableComponents,
			final EditabilityToggleHandler editabilityToggleHandler, final Runnable refreshGUIRunnable) {
		final List<Vertex> previousSelection = new ArrayList<>(selectionManager.getSelection());
		final List<Vertex> possibleVerticesToTruncate = new ArrayList<>();
		for (final SelectableComponent component : selectableComponents) {
			component.visit(new SelectableComponentVisitor() {
				@Override
				public void accept(final Camera camera) {
					possibleVerticesToTruncate.add(camera.getPosition());
					possibleVerticesToTruncate.add(camera.getTargetPosition());
				}

				@Override
				public void accept(final IdObject node) {
					possibleVerticesToTruncate.add(node.getPivotPoint());
					node.apply(new IdObjectVisitor() {
						@Override
						public void ribbonEmitter(final RibbonEmitter particleEmitter) {

						}

						@Override
						public void particleEmitter2(final ParticleEmitter2 particleEmitter) {

						}

						@Override
						public void particleEmitter(final ParticleEmitter particleEmitter) {

						}

						@Override
						public void light(final Light light) {

						}

						@Override
						public void helper(final Helper object) {

						}

						@Override
						public void eventObject(final EventObject eventObject) {

						}

						@Override
						public void collisionShape(final CollisionShape collisionShape) {
							possibleVerticesToTruncate.addAll(collisionShape.getVertices());
						}

						@Override
						public void camera(final Camera camera) {
							// do not use, visitor for IdObjects only
						}

						@Override
						public void bone(final Bone object) {

						}

						@Override
						public void attachment(final Attachment attachment) {

						}
					});
				}

				@Override
				public void accept(final Geoset geoset) {
				}
			});
		}
		final Runnable truncateSelectionRunnable = new Runnable() {
			@Override
			public void run() {
				selectionManager.removeSelection(possibleVerticesToTruncate);
			}
		};
		final Runnable unTruncateSelectionRunnable = new Runnable() {
			@Override
			public void run() {
				selectionManager.setSelection(previousSelection);
			}
		};
		return new MakeNotEditableAction(editabilityToggleHandler, truncateSelectionRunnable,
				unTruncateSelectionRunnable, refreshGUIRunnable);
	}

	private final class SelectionAtPointTester implements IdObjectVisitor {
		private CoordinateSystem axes;
		private Point point;
		private boolean mouseOverVertex;

		private SelectionAtPointTester reset(final CoordinateSystem axes, final Point point) {
			this.axes = axes;
			this.point = point;
			this.mouseOverVertex = false;
			return this;
		}

		@Override
		public void ribbonEmitter(final RibbonEmitter particleEmitter) {
			handleDefaultNode(point, axes, particleEmitter);
		}

		private void handleDefaultNode(final Point point, final CoordinateSystem axes, final IdObject node) {
			if (hitTest(node.getPivotPoint(), CoordinateSystem.Util.geom(axes, point), axes,
					node.getClickRadius(axes) * CoordinateSystem.Util.getZoom(axes) * 2)) {
				mouseOverVertex = true;
			}
		}

		@Override
		public void particleEmitter2(final ParticleEmitter2 particleEmitter) {
			handleDefaultNode(point, axes, particleEmitter);
		}

		@Override
		public void particleEmitter(final ParticleEmitter particleEmitter) {
			handleDefaultNode(point, axes, particleEmitter);
		}

		@Override
		public void light(final Light light) {
			handleDefaultNode(point, axes, light);
		}

		@Override
		public void helper(final Helper object) {
			handleDefaultNode(point, axes, object);
		}

		@Override
		public void eventObject(final EventObject eventObject) {
			handleDefaultNode(point, axes, eventObject);
		}

		@Override
		public void collisionShape(final CollisionShape collisionShape) {
			handleDefaultNode(point, axes, collisionShape);
			for (final Vertex vertex : collisionShape.getVertices()) {
				if (hitTest(vertex, CoordinateSystem.Util.geom(axes, point), axes,
						programPreferences.getVertexSize())) {
					mouseOverVertex = true;
				}
			}
		}

		@Override
		public void camera(final Camera camera) {
			if (hitTest(camera.getPosition(), CoordinateSystem.Util.geom(axes, point), axes,
					programPreferences.getVertexSize())) {
				mouseOverVertex = true;
			}
			if (hitTest(camera.getTargetPosition(), CoordinateSystem.Util.geom(axes, point), axes,
					programPreferences.getVertexSize())) {
				mouseOverVertex = true;
			}
		}

		@Override
		public void bone(final Bone object) {
			handleDefaultNode(point, axes, object);
		}

		@Override
		public void attachment(final Attachment attachment) {
			handleDefaultNode(point, axes, attachment);
		}

		public boolean isMouseOverVertex() {
			return mouseOverVertex;
		}
	}

	private final class GenericSelectorVisitor implements IdObjectVisitor {
		private List<Vertex> selectedItems;
		private Rectangle2D area;
		private CoordinateSystem coordinateSystem;

		private GenericSelectorVisitor reset(final List<Vertex> selectedItems, final Rectangle2D area,
				final CoordinateSystem coordinateSystem) {
			this.selectedItems = selectedItems;
			this.area = area;
			this.coordinateSystem = coordinateSystem;
			return this;
		}

		@Override
		public void ribbonEmitter(final RibbonEmitter particleEmitter) {
			hitTest(selectedItems, area, particleEmitter.getPivotPoint(), coordinateSystem,
					particleEmitter.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem)
							* 2);
		}

		@Override
		public void particleEmitter2(final ParticleEmitter2 particleEmitter) {
			hitTest(selectedItems, area, particleEmitter.getPivotPoint(), coordinateSystem,
					particleEmitter.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem)
							* 2);
		}

		@Override
		public void particleEmitter(final ParticleEmitter particleEmitter) {
			hitTest(selectedItems, area, particleEmitter.getPivotPoint(), coordinateSystem,
					particleEmitter.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem)
							* 2);
		}

		@Override
		public void light(final Light light) {
			hitTest(selectedItems, area, light.getPivotPoint(), coordinateSystem,
					light.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem) * 2);
		}

		@Override
		public void helper(final Helper object) {
			hitTest(selectedItems, area, object.getPivotPoint(), coordinateSystem,
					object.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem) * 2);
		}

		@Override
		public void eventObject(final EventObject eventObject) {
			hitTest(selectedItems, area, eventObject.getPivotPoint(), coordinateSystem,
					eventObject.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem) * 2);
		}

		@Override
		public void collisionShape(final CollisionShape collisionShape) {
			hitTest(selectedItems, area, collisionShape.getPivotPoint(), coordinateSystem,
					collisionShape.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem)
							* 2);
			for (final Vertex vertex : collisionShape.getVertices()) {
				hitTest(selectedItems, area, vertex, coordinateSystem, programPreferences.getVertexSize());
			}
		}

		@Override
		public void camera(final Camera camera) {
			hitTest(selectedItems, area, camera.getPosition(), coordinateSystem, programPreferences.getVertexSize());
			hitTest(selectedItems, area, camera.getTargetPosition(), coordinateSystem,
					programPreferences.getVertexSize());
		}

		@Override
		public void bone(final Bone object) {
			hitTest(selectedItems, area, object.getPivotPoint(), coordinateSystem,
					object.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem) * 2);
		}

		@Override
		public void attachment(final Attachment attachment) {
			hitTest(selectedItems, area, attachment.getPivotPoint(), coordinateSystem,
					attachment.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem) * 2);
		}
	}
}
