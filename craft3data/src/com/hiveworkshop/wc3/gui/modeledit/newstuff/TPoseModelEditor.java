package com.hiveworkshop.wc3.gui.modeledit.newstuff;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.etheller.collections.HashMap;
import com.etheller.collections.ListView;
import com.etheller.collections.Map;
import com.hiveworkshop.wc3.gui.ProgramPreferences;
import com.hiveworkshop.wc3.gui.animedit.WrongModeException;
import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.cutpaste.CopiedModelData;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.nodes.DeleteNodesAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.selection.MakeNotEditableAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.tools.AutoCenterBonesAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.tools.RenameBoneAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.tools.SetParentAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.util.CompoundAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.util.DoNothingAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.listener.EditabilityToggleHandler;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectableComponent;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.wc3.gui.modeledit.selection.VertexSelectionHelper;
import com.hiveworkshop.wc3.mdl.AnimFlag;
import com.hiveworkshop.wc3.mdl.Attachment;
import com.hiveworkshop.wc3.mdl.Bone;
import com.hiveworkshop.wc3.mdl.Camera;
import com.hiveworkshop.wc3.mdl.CollisionShape;
import com.hiveworkshop.wc3.mdl.EventObject;
import com.hiveworkshop.wc3.mdl.ExtLog;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.Helper;
import com.hiveworkshop.wc3.mdl.IdObject;
import com.hiveworkshop.wc3.mdl.Light;
import com.hiveworkshop.wc3.mdl.ParticleEmitter;
import com.hiveworkshop.wc3.mdl.ParticleEmitter2;
import com.hiveworkshop.wc3.mdl.ParticleEmitterPopcorn;
import com.hiveworkshop.wc3.mdl.RibbonEmitter;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.v2.ModelView;
import com.hiveworkshop.wc3.mdl.v2.visitor.IdObjectVisitor;

public class TPoseModelEditor extends AbstractModelEditor<IdObject> {
	private final ProgramPreferences programPreferences;
	private final GenericSelectorVisitor genericSelectorVisitor;
	private final SelectionAtPointTester selectionAtPointTester;

	public TPoseModelEditor(final ModelView model, final ProgramPreferences programPreferences,
			final SelectionManager<IdObject> selectionManager,
			final ModelStructureChangeListener structureChangeListener) {
		super(selectionManager, model, structureChangeListener);
		this.programPreferences = programPreferences;
		this.genericSelectorVisitor = new GenericSelectorVisitor();
		this.selectionAtPointTester = new SelectionAtPointTester();
	}

	@Override
	public UndoAction setParent(final IdObject node) {
		final HashMap<IdObject, IdObject> nodeToOldParent = new HashMap<>();
		for (final IdObject b : model.getEditableIdObjects()) {
			if (selectionManager.getSelection().contains(b.getPivotPoint())) {
				nodeToOldParent.put(b, b.getParent());
			}
		}
		final SetParentAction setParentAction = new SetParentAction(nodeToOldParent, node, structureChangeListener);
		setParentAction.redo();
		return setParentAction;
	}

	@Override
	public UndoAction autoCenterSelectedBones() {
		final Set<IdObject> selBones = new HashSet<>();
		for (final IdObject b : model.getEditableIdObjects()) {
			if (selectionManager.getSelection().contains(b.getPivotPoint())) {
				selBones.add(b);
			}
		}

		final Map<Bone, Vertex> boneToOldPosition = new HashMap<>();
		for (final IdObject obj : selBones) {
			if (Bone.class.isAssignableFrom(obj.getClass())) {
				final Bone bone = (Bone) obj;
				final ArrayList<GeosetVertex> childVerts = new ArrayList<>();
				for (final Geoset geo : model.getModel().getGeosets()) {
					childVerts.addAll(geo.getChildrenOf(bone));
				}
				if (childVerts.size() > 0) {
					final Vertex pivotPoint = bone.getPivotPoint();
					boneToOldPosition.put(bone, new Vertex(pivotPoint));
					pivotPoint.setTo(Vertex.centerOfGroup(childVerts));
				}
			}
		}
		return new AutoCenterBonesAction(boneToOldPosition);
	}

	@Override
	public UndoAction setSelectedBoneName(final String name) {
		if (selectionManager.getSelection().size() != 1) {
			throw new IllegalStateException("Only one bone can be renamed at a time.");
		}
		final IdObject node = selectionManager.getSelection().iterator().next();
		if (node == null) {
			throw new IllegalStateException("Selection is not a node");
		}
		final RenameBoneAction renameBoneAction = new RenameBoneAction(node.getName(), name, node);
		renameBoneAction.redo();
		return renameBoneAction;
	}

	@Override
	public UndoAction addSelectedBoneSuffix(final String name) {
		final Set<IdObject> selection = selectionManager.getSelection();
		final com.etheller.collections.List<RenameBoneAction> actions = new com.etheller.collections.ArrayList<>();
		for (final IdObject bone : selection) {
			final RenameBoneAction renameBoneAction = new RenameBoneAction(bone.getName(), bone.getName() + name, bone);
			renameBoneAction.redo();
			actions.add(renameBoneAction);
		}
		return new CompoundAction("add selected bone suffix", actions);
	}

	@Override
	public UndoAction addTeamColor() {
		return new DoNothingAction("add team color");
	}

	@Override
	public UndoAction splitGeoset() {
		return new DoNothingAction("split geoset");
	}

	@Override
	public void selectByVertices(final Collection<? extends Vertex> newSelection) {
		final Set<IdObject> newlySelectedPivots = new HashSet<>();
		for (final IdObject object : model.getEditableIdObjects()) {
			if (newSelection.contains(object.getPivotPoint())) {
				newlySelectedPivots.add(object);
			}
			object.apply(new IdObjectVisitor() {
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
				public void popcornFxEmitter(final ParticleEmitterPopcorn popcornFxEmitter) {
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
					for (final Vertex vertex : collisionShape.getVertices()) {
						if (newSelection.contains(vertex)) {
							newlySelectedPivots.add(collisionShape);
						}
					}
				}

				@Override
				public void camera(final Camera camera) {

				}

				@Override
				public void bone(final Bone object) {

				}

				@Override
				public void attachment(final Attachment attachment) {

				}
			});
		}
		selectionManager.setSelection(newlySelectedPivots);
	}

	@Override
	public UndoAction expandSelection() {
		throw new WrongModeException("Not supported in T-Pose mode");
	}

	@Override
	public UndoAction invertSelection() {
		throw new WrongModeException("Not supported in T-Pose mode");
		// final ArrayList<Vertex> oldSelection = new
		// ArrayList<>(selectionManager.getSelection());
		// final Set<Vertex> invertedSelection = new
		// HashSet<>(selectionManager.getSelection());
		// final IdObjectVisitor visitor = new IdObjectVisitor() {
		// @Override
		// public void ribbonEmitter(final RibbonEmitter particleEmitter) {
		// toggleSelection(invertedSelection, particleEmitter.getPivotPoint());
		// }
		//
		// @Override
		// public void particleEmitter2(final ParticleEmitter2 particleEmitter) {
		// toggleSelection(invertedSelection, particleEmitter.getPivotPoint());
		// }
		//
		// @Override
		// public void particleEmitter(final ParticleEmitter particleEmitter) {
		// toggleSelection(invertedSelection, particleEmitter.getPivotPoint());
		// }
		//
		// @Override
		// public void light(final Light light) {
		// toggleSelection(invertedSelection, light.getPivotPoint());
		// }
		//
		// @Override
		// public void helper(final Helper object) {
		// toggleSelection(invertedSelection, object.getPivotPoint());
		// }
		//
		// @Override
		// public void eventObject(final EventObject eventObject) {
		// toggleSelection(invertedSelection, eventObject.getPivotPoint());
		// }
		//
		// @Override
		// public void collisionShape(final CollisionShape collisionShape) {
		// toggleSelection(invertedSelection, collisionShape.getPivotPoint());
		// for (final Vertex vertex : collisionShape.getVertices()) {
		// toggleSelection(invertedSelection, vertex);
		// }
		// }
		//
		// @Override
		// public void camera(final Camera camera) {
		// toggleSelection(invertedSelection, camera.getPosition());
		// toggleSelection(invertedSelection, camera.getTargetPosition());
		// }
		//
		// @Override
		// public void bone(final Bone object) {
		// toggleSelection(invertedSelection, object.getPivotPoint());
		// }
		//
		// @Override
		// public void attachment(final Attachment attachment) {
		// toggleSelection(invertedSelection, attachment.getPivotPoint());
		// }
		// };
		// for (final IdObject node : model.getEditableIdObjects()) {
		// node.apply(visitor);
		// }
		// for (final Camera object : model.getEditableCameras()) {
		// visitor.camera(object);
		// }
		// selectionManager.setSelection(invertedSelection);
		// return (new SetSelectionAction<>(invertedSelection, oldSelection,
		// selectionManager, "invert selection"));
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
		throw new WrongModeException("Not supported in T-Pose mode");
		// final ArrayList<Vertex> oldSelection = new
		// ArrayList<>(selectionManager.getSelection());
		// final Set<Vertex> allSelection = new HashSet<>();
		// final IdObjectVisitor visitor = new IdObjectVisitor() {
		// @Override
		// public void ribbonEmitter(final RibbonEmitter particleEmitter) {
		// allSelection.add(particleEmitter.getPivotPoint());
		// }
		//
		// @Override
		// public void particleEmitter2(final ParticleEmitter2 particleEmitter) {
		// allSelection.add(particleEmitter.getPivotPoint());
		// }
		//
		// @Override
		// public void particleEmitter(final ParticleEmitter particleEmitter) {
		// allSelection.add(particleEmitter.getPivotPoint());
		// }
		//
		// @Override
		// public void light(final Light light) {
		// allSelection.add(light.getPivotPoint());
		// }
		//
		// @Override
		// public void helper(final Helper object) {
		// allSelection.add(object.getPivotPoint());
		// }
		//
		// @Override
		// public void eventObject(final EventObject eventObject) {
		// allSelection.add(eventObject.getPivotPoint());
		// }
		//
		// @Override
		// public void collisionShape(final CollisionShape collisionShape) {
		// allSelection.add(collisionShape.getPivotPoint());
		// for (final Vertex vertex : collisionShape.getVertices()) {
		// allSelection.add(vertex);
		// }
		// }
		//
		// @Override
		// public void camera(final Camera camera) {
		// allSelection.add(camera.getPosition());
		// allSelection.add(camera.getTargetPosition());
		// }
		//
		// @Override
		// public void bone(final Bone object) {
		// allSelection.add(object.getPivotPoint());
		// }
		//
		// @Override
		// public void attachment(final Attachment attachment) {
		// allSelection.add(attachment.getPivotPoint());
		// }
		// };
		// for (final IdObject node : model.getEditableIdObjects()) {
		// node.apply(visitor);
		// }
		// for (final Camera object : model.getEditableCameras()) {
		// visitor.camera(object);
		// }
		// selectionManager.setSelection(allSelection);
		// return (new SetSelectionAction<>(allSelection, oldSelection,
		// selectionManager, "select all"));
	}

	@Override
	protected List<IdObject> genericSelect(final Rectangle2D region, final CoordinateSystem coordinateSystem) {
		final List<IdObject> selectedItems = new ArrayList<>();
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
		final IdObjectVisitor visitor = selectionAtPointTester.reset(axes, point);
		for (final IdObject object : model.getEditableIdObjects()) {
			object.apply(visitor);
		}
		for (final Camera camera : model.getEditableCameras()) {
			visitor.camera(camera);
		}
		return selectionAtPointTester.isMouseOverVertex();
	}

	public static void hitTest(final List<IdObject> selectedItems, final Rectangle2D area, final Vertex geosetVertex,
			final CoordinateSystem coordinateSystem, final double vertexSize, final IdObject node) {
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
		if ((distance(x, y, minX, minY) <= (vertexSize / 2.0)) || (distance(x, y, maxX, maxY) <= (vertexSize / 2.0))
				|| area.contains(vertexX, vertexY)) {
			selectedItems.add(node);
		}
	}

	public static boolean hitTest(final Vertex vertex, final Point2D point, final CoordinateSystem coordinateSystem,
			final double vertexSize) {
		final double x = coordinateSystem.convertX(vertex.getCoord(coordinateSystem.getPortFirstXYZ()));
		final double y = coordinateSystem.convertY(vertex.getCoord(coordinateSystem.getPortSecondXYZ()));
		final double px = coordinateSystem.convertX(point.getX());
		final double py = coordinateSystem.convertY(point.getY());
		return Point2D.distance(px, py, x, y) <= (vertexSize / 2.0);
	}

	public static double distance(final double vertexX, final double vertexY, final double x, final double y) {
		final double dx = x - vertexX;
		final double dy = y - vertexY;
		return Math.sqrt((dx * dx) + (dy * dy));
	}

	@Override
	protected UndoAction buildHideComponentAction(final ListView<? extends SelectableComponent> selectableComponents,
			final EditabilityToggleHandler editabilityToggleHandler, final Runnable refreshGUIRunnable) {
		final List<IdObject> previousSelection = new ArrayList<>(selectionManager.getSelection());
		final Runnable truncateSelectionRunnable = new Runnable() {
			@Override
			public void run() {
				selectionManager.removeSelection(model.getModel().getIdObjects());
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
		public void popcornFxEmitter(final ParticleEmitterPopcorn particleEmitter) {
			handleDefaultNode(point, axes, particleEmitter);
		}

		@Override
		public void light(final Light light) {
			handleDefaultNode(point, axes, light);
		}

		@Override
		public void helper(final Helper node) {
			if (hitTest(node.getPivotPoint(), CoordinateSystem.Util.geom(axes, point), axes,
					node.getClickRadius(axes) * CoordinateSystem.Util.getZoom(axes))) {
				mouseOverVertex = true;
			}
		}

		@Override
		public void eventObject(final EventObject eventObject) {
			handleDefaultNode(point, axes, eventObject);
		}

		@Override
		public void collisionShape(final CollisionShape collisionShape) {
			handleDefaultNode(point, axes, collisionShape);
			for (final Vertex vertex : collisionShape.getVertices()) {
				if (hitTest(vertex, CoordinateSystem.Util.geom(axes, point), axes, IdObject.DEFAULT_CLICK_RADIUS)) {
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
		public void bone(final Bone node) {
			if (hitTest(node.getPivotPoint(), CoordinateSystem.Util.geom(axes, point), axes,
					node.getClickRadius(axes) * CoordinateSystem.Util.getZoom(axes))) {
				mouseOverVertex = true;
			}
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
		private List<IdObject> selectedItems;
		private Rectangle2D area;
		private CoordinateSystem coordinateSystem;

		private GenericSelectorVisitor reset(final List<IdObject> selectedItems, final Rectangle2D area,
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
							* 2,
					particleEmitter);
		}

		@Override
		public void particleEmitter2(final ParticleEmitter2 particleEmitter) {
			hitTest(selectedItems, area, particleEmitter.getPivotPoint(), coordinateSystem,
					particleEmitter.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem)
							* 2,
					particleEmitter);
		}

		@Override
		public void particleEmitter(final ParticleEmitter particleEmitter) {
			hitTest(selectedItems, area, particleEmitter.getPivotPoint(), coordinateSystem,
					particleEmitter.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem)
							* 2,
					particleEmitter);
		}

		@Override
		public void popcornFxEmitter(final ParticleEmitterPopcorn particleEmitter) {
			hitTest(selectedItems, area, particleEmitter.getPivotPoint(), coordinateSystem,
					particleEmitter.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem)
							* 2,
					particleEmitter);
		}

		@Override
		public void light(final Light light) {
			hitTest(selectedItems, area, light.getPivotPoint(), coordinateSystem,
					light.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem) * 2,
					light);
		}

		@Override
		public void helper(final Helper object) {
			hitTest(selectedItems, area, object.getPivotPoint(), coordinateSystem,
					object.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem), object);
		}

		@Override
		public void eventObject(final EventObject eventObject) {
			hitTest(selectedItems, area, eventObject.getPivotPoint(), coordinateSystem,
					eventObject.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem) * 2,
					eventObject);
		}

		@Override
		public void collisionShape(final CollisionShape collisionShape) {
			hitTest(selectedItems, area, collisionShape.getPivotPoint(), coordinateSystem,
					collisionShape.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem)
							* 2,
					collisionShape);
			for (final Vertex vertex : collisionShape.getVertices()) {
				hitTest(selectedItems, area, vertex, coordinateSystem, IdObject.DEFAULT_CLICK_RADIUS, collisionShape);
			}
		}

		@Override
		public void camera(final Camera camera) {
		}

		@Override
		public void bone(final Bone object) {
			hitTest(selectedItems, area, object.getPivotPoint(), coordinateSystem,
					object.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem), object);
		}

		@Override
		public void attachment(final Attachment attachment) {
			hitTest(selectedItems, area, attachment.getPivotPoint(), coordinateSystem,
					attachment.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem) * 2,
					attachment);
		}
	}

	@Override
	public CopiedModelData copySelection() {
		final Collection<? extends Vertex> selection = selectionManager.getSelectedVertices();
		final Set<IdObject> clonedNodes = new HashSet<>();
		final Set<Camera> clonedCameras = new HashSet<>();
		for (final IdObject b : model.getEditableIdObjects()) {
			if (selection.contains(b.getPivotPoint())) {
				clonedNodes.add(b.copy());
			}
		}
		for (final IdObject obj : clonedNodes) {
			if (!clonedNodes.contains(obj.getParent())) {
				obj.setParent(null);
			}
		}
		for (final Camera camera : model.getEditableCameras()) {
			if (selection.contains(camera.getTargetPosition()) || selection.contains(camera.getPosition())) {
				clonedCameras.add(camera);
			}
		}
		return new CopiedModelData(new ArrayList<Geoset>(), clonedNodes, clonedCameras);
	}

	@Override
	public void rawScale(final double centerX, final double centerY, final double centerZ, final double scaleX,
			final double scaleY, final double scaleZ) {
		super.rawScale(centerX, centerY, centerZ, scaleX, scaleY, scaleZ);
		for (final IdObject b : model.getEditableIdObjects()) {
			if (selectionManager.getSelection().contains(b.getPivotPoint())) {
				b.apply(new IdObjectVisitor() {
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
					public void popcornFxEmitter(final ParticleEmitterPopcorn popcornFxEmitter) {
					}

					@Override
					public void light(final Light light) {
					}

					@Override
					public void helper(final Helper object) {
						final AnimFlag translation = AnimFlag.find(object.getAnimFlags(), "Translation");
						if (translation != null) {
							for (int i = 0; i < translation.size(); i++) {
								final Vertex scaleData = (Vertex) translation.getValues().get(i);
								scaleData.scale(0, 0, 0, scaleX, scaleY, scaleZ);
								if (translation.tans()) {
									final Vertex inTanData = (Vertex) translation.getInTans().get(i);
									inTanData.scale(0, 0, 0, scaleX, scaleY, scaleZ);
									final Vertex outTanData = (Vertex) translation.getInTans().get(i);
									outTanData.scale(0, 0, 0, scaleX, scaleY, scaleZ);
								}
							}
						}
					}

					@Override
					public void eventObject(final EventObject eventObject) {
					}

					@Override
					public void collisionShape(final CollisionShape collisionShape) {
						final ExtLog extents = collisionShape.getExtents();
						if ((extents != null) && (scaleX == scaleY) && (scaleY == scaleZ)) {
							extents.setBoundsRadius(extents.getBoundsRadius() * scaleX);
						}
					}

					@Override
					public void camera(final Camera camera) {
					}

					@Override
					public void bone(final Bone object) {
						final AnimFlag translation = AnimFlag.find(object.getAnimFlags(), "Translation");
						if (translation != null) {
							for (int i = 0; i < translation.size(); i++) {
								final Vertex scaleData = (Vertex) translation.getValues().get(i);
								scaleData.scale(0, 0, 0, scaleX, scaleY, scaleZ);
								if (translation.tans()) {
									final Vertex inTanData = (Vertex) translation.getInTans().get(i);
									inTanData.scale(0, 0, 0, scaleX, scaleY, scaleZ);
									final Vertex outTanData = (Vertex) translation.getInTans().get(i);
									outTanData.scale(0, 0, 0, scaleX, scaleY, scaleZ);
								}
							}
						}
					}

					@Override
					public void attachment(final Attachment attachment) {
					}
				});
			}
		}
	}

	@Override
	public UndoAction deleteSelectedComponents() {
		final List<IdObject> deletedIdObjects = new ArrayList<>();
		for (final IdObject object : model.getEditableIdObjects()) {
			if (selectionManager.getSelection().contains(object.getPivotPoint())) {
				deletedIdObjects.add(object);
			}
		}
		final List<Camera> deletedCameras = new ArrayList<>();
		for (final Camera camera : model.getEditableCameras()) {
			if (selectionManager.getSelection().contains(camera.getPosition())
					|| selectionManager.getSelection().contains(camera.getTargetPosition())) {
				deletedCameras.add(camera);
			}
		}
		final DeleteNodesAction deleteNodesAction = new DeleteNodesAction(selectionManager.getSelectedVertices(),
				deletedIdObjects, deletedCameras, structureChangeListener, model, vertexSelectionHelper);
		deleteNodesAction.redo();
		return deleteNodesAction;
	}

	@Override
	public UndoAction createFaceFromSelection(final Vertex preferredFacingVector) {
		return new DoNothingAction("create face");
	}

	@Override
	public UndoAction addVertex(final double x, final double y, final double z,
			final Vertex preferredNormalFacingVector) {
		return new DoNothingAction("add vertex");
	}

	public VertexSelectionHelper getVertexSelectionHelper() {
		return vertexSelectionHelper;
	}
}
