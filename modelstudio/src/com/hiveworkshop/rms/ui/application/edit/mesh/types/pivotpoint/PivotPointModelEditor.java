package com.hiveworkshop.rms.ui.application.edit.mesh.types.pivotpoint;

import com.hiveworkshop.rms.editor.model.EventObject;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.editor.model.visitor.IdObjectVisitor;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.AbstractModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.creator.actions.DrawBoneAction;
import com.hiveworkshop.rms.ui.gui.modeledit.cutpaste.CopiedModelData;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.nodes.DeleteNodesAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.selection.MakeNotEditableAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.selection.SetSelectionAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.tools.AutoCenterBonesAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.tools.RenameBoneAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.tools.RigAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.tools.SetParentAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.CompoundAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.DoNothingAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.EditabilityToggleHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectableComponent;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectableComponentVisitor;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.VertexSelectionHelper;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.*;

public class PivotPointModelEditor extends AbstractModelEditor<Vec3> {
	private final ProgramPreferences programPreferences;
	private final GenericSelectorVisitor genericSelectorVisitor;
	private final SelectionAtPointTester selectionAtPointTester;

	public PivotPointModelEditor(final ModelView model, final ProgramPreferences programPreferences, final SelectionManager<Vec3> selectionManager, final ModelStructureChangeListener structureChangeListener) {
		super(selectionManager, model, structureChangeListener);
		this.programPreferences = programPreferences;
		genericSelectorVisitor = new GenericSelectorVisitor();
		selectionAtPointTester = new SelectionAtPointTester();
	}

	@Override
	public UndoAction setParent(final IdObject node) {
		final Map<IdObject, IdObject> nodeToOldParent = new HashMap<>();
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

		final Map<Bone, Vec3> boneToOldPosition = new HashMap<>();
		for (final IdObject obj : selBones) {
			if (Bone.class.isAssignableFrom(obj.getClass())) {
				final Bone bone = (Bone) obj;
				final List<GeosetVertex> childVerts = new ArrayList<>();
				for (final Geoset geo : model.getModel().getGeosets()) {
					childVerts.addAll(geo.getChildrenOf(bone));
				}
				if (childVerts.size() > 0) {
					final Vec3 pivotPoint = bone.getPivotPoint();
					boneToOldPosition.put(bone, new Vec3(pivotPoint));
					pivotPoint.set(Vec3.centerOfGroup(childVerts));
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
		final Vec3 selectedVertex = selectionManager.getSelection().iterator().next();
		IdObject node = null;
		for (final IdObject bone : model.getEditableIdObjects()) {
			if (bone.getPivotPoint() == selectedVertex) {
				if (node != null) {
					throw new IllegalStateException(
							"Flagrant error. Multiple bones are bound to the same memory addresses. Save your work and restart the application.");
				}
				node = bone;
			}
		}
		if (node == null) {
			throw new IllegalStateException("Selection is not a node");
		}
		final RenameBoneAction renameBoneAction = new RenameBoneAction(node.getName(), name, node);
		renameBoneAction.redo();
		return renameBoneAction;
	}

	public static void hitTest(final List<Vec3> selectedItems, final Rectangle2D area, final Vec3 geosetVertex, final CoordinateSystem coordinateSystem, final double vertexSize) {
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
		if ((distance(x, y, minX, minY) <= (vertexSize / 2.0))
				|| (distance(x, y, maxX, maxY) <= (vertexSize / 2.0))
				|| area.contains(vertexX, vertexY)) {
			selectedItems.add(geosetVertex);
		}
	}

	@Override
	public UndoAction addTeamColor() {
		return new DoNothingAction("add team color");
	}

	@Override
	public UndoAction splitGeoset() {
		return new DoNothingAction("split geoset");
	}

	public static boolean hitTest(final Vec3 vertex, final Point2D point, final CoordinateSystem coordinateSystem, final double vertexSize) {
		final double x = coordinateSystem.convertX(vertex.getCoord(coordinateSystem.getPortFirstXYZ()));
		final double y = coordinateSystem.convertY(vertex.getCoord(coordinateSystem.getPortSecondXYZ()));
		final double px = coordinateSystem.convertX(point.getX());
		final double py = coordinateSystem.convertY(point.getY());
		return Point2D.distance(px, py, x, y) <= (vertexSize / 2.0);
	}

	@Override
	public UndoAction addSelectedBoneSuffix(final String name) {
		final Set<Vec3> selection = selectionManager.getSelection();
		final List<RenameBoneAction> actions = new ArrayList<>();
		for (final IdObject bone : model.getEditableIdObjects()) {
			if (selection.contains(bone.getPivotPoint())) {
				final RenameBoneAction renameBoneAction = new RenameBoneAction(bone.getName(), bone.getName() + name, bone);
				renameBoneAction.redo();
				actions.add(renameBoneAction);
			}
		}
		return new CompoundAction("add selected bone suffix", actions);
	}

	@Override
	public UndoAction invertSelection() {
		final List<Vec3> oldSelection = new ArrayList<>(selectionManager.getSelection());
		final Set<Vec3> invertedSelection = new HashSet<>(selectionManager.getSelection());
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
			public void popcornFxEmitter(final ParticleEmitterPopcorn popcornFxEmitter) {
				toggleSelection(invertedSelection, popcornFxEmitter.getPivotPoint());
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
				for (final Vec3 vertex : collisionShape.getVertices()) {
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
		return new SetSelectionAction<>(invertedSelection, oldSelection, selectionManager, "invert selection");
	}

	private void toggleSelection(final Set<Vec3> selection, final Vec3 position) {
		if (selection.contains(position)) {
			selection.remove(position);
		} else {
			selection.add(position);
		}
	}

	@Override
	public UndoAction selectAll() {
		final List<Vec3> oldSelection = new ArrayList<>(selectionManager.getSelection());
		final Set<Vec3> allSelection = new HashSet<>();
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
			public void popcornFxEmitter(final ParticleEmitterPopcorn popcornFxEmitter) {
				allSelection.add(popcornFxEmitter.getPivotPoint());
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
				allSelection.addAll(collisionShape.getVertices());
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
		return new SetSelectionAction<>(allSelection, oldSelection, selectionManager, "select all");
	}

	@Override
	protected List<Vec3> genericSelect(final Rectangle2D region, final CoordinateSystem coordinateSystem) {
		final List<Vec3> selectedItems = new ArrayList<>();
		final double startingClickX = region.getX();
		final double startingClickY = region.getY();
		final double endingClickX = region.getX() + region.getWidth();
		final double endingClickY = region.getY() + region.getHeight();

		final double minX = Math.min(startingClickX, endingClickX);
		final double minY = Math.min(startingClickY, endingClickY);
		final double maxX = Math.max(startingClickX, endingClickX);
		final double maxY = Math.max(startingClickY, endingClickY);
		final Rectangle2D area = new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
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

	@Override
	public void selectByVertices(final Collection<? extends Vec3> newSelection) {
		final List<Vec3> newlySelectedPivots = new ArrayList<>();
		for (final IdObject object : model.getEditableIdObjects()) {
			if (newSelection.contains(object.getPivotPoint())) {
				newlySelectedPivots.add(object.getPivotPoint());
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
					for (final Vec3 vertex : collisionShape.getVertices()) {
						if (newSelection.contains(vertex)) {
							newlySelectedPivots.add(vertex);
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
		for (final Camera camera : model.getEditableCameras()) {
			if (newSelection.contains(camera.getPosition())) {
				newlySelectedPivots.add(camera.getPosition());
			}
			if (newSelection.contains(camera.getTargetPosition())) {
				newlySelectedPivots.add(camera.getTargetPosition());
			}
		}
		selectionManager.setSelection(newlySelectedPivots);
	}

	@Override
	public UndoAction expandSelection() {
		final Set<Vec3> expandedSelection = new HashSet<>(selectionManager.getSelection());
		final Set<Vec3> oldSelection = new HashSet<>(selectionManager.getSelection());
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
				boolean selected = false;
				if (oldSelection.contains(collisionShape.getPivotPoint())) {
					selected = true;
				}
				for (final Vec3 vertex : collisionShape.getVertices()) {
					if (oldSelection.contains(vertex)) {
						selected = true;
						break;
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
		return new SetSelectionAction<>(expandedSelection, oldSelection, selectionManager, "expand selection");
	}

	public static double distance(final double vertexX, final double vertexY, final double x, final double y) {
		final double dx = x - vertexX;
		final double dy = y - vertexY;
		return Math.sqrt((dx * dx) + (dy * dy));
	}

	@Override
	protected UndoAction buildHideComponentAction(final List<? extends SelectableComponent> selectableComponents, final EditabilityToggleHandler editabilityToggleHandler, final Runnable refreshGUIRunnable) {
		final List<Vec3> previousSelection = new ArrayList<>(selectionManager.getSelection());
		final List<Vec3> possibleVerticesToTruncate = new ArrayList<>();
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
		final Runnable truncateSelectionRunnable = () -> selectionManager.removeSelection(possibleVerticesToTruncate);
		final Runnable unTruncateSelectionRunnable = () -> selectionManager.setSelection(previousSelection);
		return new MakeNotEditableAction(editabilityToggleHandler, truncateSelectionRunnable, unTruncateSelectionRunnable, refreshGUIRunnable);
	}

	@Override
	public CopiedModelData copySelection() {
		final Set<Vec3> selection = selectionManager.getSelection();
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
			if (selection.contains(camera.getTargetPosition())
					|| selection.contains(camera.getPosition())) {
				clonedCameras.add(camera);
			}
		}
		return new CopiedModelData(new ArrayList<>(), clonedNodes, clonedCameras);
	}

	@Override
	public void rawScale(final double centerX, final double centerY, final double centerZ, final double scaleX, final double scaleY, final double scaleZ) {
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
						final Vec3AnimFlag translation = (Vec3AnimFlag) object.find("Translation");
						if (translation != null) {
							for (int i = 0; i < translation.size(); i++) {
								final Vec3 scaleData = translation.getValues().get(i);
								scaleData.scale(0, 0, 0, scaleX, scaleY, scaleZ);
								if (translation.tans()) {
									final Vec3 inTanData = translation.getInTans().get(i);
									inTanData.scale(0, 0, 0, scaleX, scaleY, scaleZ);
									final Vec3 outTanData = translation.getInTans().get(i);
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
						final Vec3AnimFlag translation = (Vec3AnimFlag) object.find("Translation");
						if (translation != null) {
							for (int i = 0; i < translation.size(); i++) {
								final Vec3 scaleData = translation.getValues().get(i);
								scaleData.scale(0, 0, 0, scaleX, scaleY, scaleZ);
								if (translation.tans()) {
									final Vec3 inTanData = translation.getInTans().get(i);
									inTanData.scale(0, 0, 0, scaleX, scaleY, scaleZ);
									final Vec3 outTanData = translation.getInTans().get(i);
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
		final DeleteNodesAction deleteNodesAction = new DeleteNodesAction(selectionManager.getSelection(), deletedIdObjects, deletedCameras, structureChangeListener, model, vertexSelectionHelper);
		deleteNodesAction.redo();
		return deleteNodesAction;
	}

	@Override
	public UndoAction addVertex(final double x, final double y, final double z, final Vec3 preferredNormalFacingVector) {
		return new DoNothingAction("add vertex");
	}

	@Override
	public void rawTranslate(final double x, final double y, final double z) {
		super.rawTranslate(x, y, z);
		for (final IdObject b : model.getEditableIdObjects()) {
			if (selectionManager.getSelection().contains(b.getPivotPoint())) {
				final float[] bindPose = b.getBindPose();
				if (bindPose != null) {
					bindPose[9] += x;
					bindPose[10] += y;
					bindPose[11] += z;
				}
			}
		}
	}

	private final class SelectionAtPointTester implements IdObjectVisitor {
		private CoordinateSystem axes;
		private Point point;
		private boolean mouseOverVertex;

		private SelectionAtPointTester reset(final CoordinateSystem axes, final Point point) {
			this.axes = axes;
			this.point = point;
			mouseOverVertex = false;
			return this;
		}

		@Override
		public void ribbonEmitter(final RibbonEmitter particleEmitter) {
			handleDefaultNode(point, axes, particleEmitter);
		}

		private void handleDefaultNode(final Point point, final CoordinateSystem axes, final IdObject node) {
			if (hitTest(node.getPivotPoint(), CoordinateSystem.Util.geom(axes, point), axes, node.getClickRadius(axes) * CoordinateSystem.Util.getZoom(axes) * 2)) {
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
		public void popcornFxEmitter(final ParticleEmitterPopcorn popcornFxEmitter) {
			handleDefaultNode(point, axes, popcornFxEmitter);
		}

		@Override
		public void light(final Light light) {
			handleDefaultNode(point, axes, light);
		}

		@Override
		public void helper(final Helper node) {
			if (hitTest(node.getPivotPoint(), CoordinateSystem.Util.geom(axes, point), axes, node.getClickRadius(axes) * CoordinateSystem.Util.getZoom(axes))) {
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
			for (final Vec3 vertex : collisionShape.getVertices()) {
				if (hitTest(vertex, CoordinateSystem.Util.geom(axes, point), axes, IdObject.DEFAULT_CLICK_RADIUS)) {
					mouseOverVertex = true;
				}
			}
		}

		@Override
		public void camera(final Camera camera) {
			if (hitTest(camera.getPosition(), CoordinateSystem.Util.geom(axes, point), axes, programPreferences.getVertexSize())) {
				mouseOverVertex = true;
			}
			if (hitTest(camera.getTargetPosition(), CoordinateSystem.Util.geom(axes, point), axes, programPreferences.getVertexSize())) {
				mouseOverVertex = true;
			}
		}

		@Override
		public void bone(final Bone node) {
			if (hitTest(node.getPivotPoint(), CoordinateSystem.Util.geom(axes, point), axes, node.getClickRadius(axes) * CoordinateSystem.Util.getZoom(axes))) {
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

	@Override
	public UndoAction createFaceFromSelection(final Vec3 preferredFacingVector) {
		return new DoNothingAction("create face");
	}

	private final class GenericSelectorVisitor implements IdObjectVisitor {
		private List<Vec3> selectedItems;
		private Rectangle2D area;
		private CoordinateSystem coordinateSystem;

		private GenericSelectorVisitor reset(final List<Vec3> selectedItems, final Rectangle2D area, final CoordinateSystem coordinateSystem) {
			this.selectedItems = selectedItems;
			this.area = area;
			this.coordinateSystem = coordinateSystem;
			return this;
		}

		@Override
		public void ribbonEmitter(final RibbonEmitter particleEmitter) {
			hitTest(selectedItems, area, particleEmitter.getPivotPoint(), coordinateSystem, particleEmitter.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem) * 2);
		}

		@Override
		public void particleEmitter2(final ParticleEmitter2 particleEmitter) {
			hitTest(selectedItems, area, particleEmitter.getPivotPoint(), coordinateSystem, particleEmitter.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem) * 2);
		}

		@Override
		public void particleEmitter(final ParticleEmitter particleEmitter) {
			hitTest(selectedItems, area, particleEmitter.getPivotPoint(), coordinateSystem, particleEmitter.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem) * 2);
		}

		@Override
		public void popcornFxEmitter(final ParticleEmitterPopcorn popcornFxEmitter) {
			hitTest(selectedItems, area, popcornFxEmitter.getPivotPoint(), coordinateSystem, popcornFxEmitter.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem) * 2);
		}

		@Override
		public void light(final Light light) {
			hitTest(selectedItems, area, light.getPivotPoint(), coordinateSystem, light.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem) * 2);
		}

		@Override
		public void helper(final Helper object) {
			hitTest(selectedItems, area, object.getPivotPoint(), coordinateSystem, object.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem));
		}

		@Override
		public void eventObject(final EventObject eventObject) {
			hitTest(selectedItems, area, eventObject.getPivotPoint(), coordinateSystem, eventObject.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem) * 2);
		}

		@Override
		public void collisionShape(final CollisionShape collisionShape) {
			hitTest(selectedItems, area, collisionShape.getPivotPoint(), coordinateSystem, collisionShape.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem) * 2);
			for (final Vec3 vertex : collisionShape.getVertices()) {
				hitTest(selectedItems, area, vertex, coordinateSystem, IdObject.DEFAULT_CLICK_RADIUS);
			}
		}

		@Override
		public void camera(final Camera camera) {
			hitTest(selectedItems, area, camera.getPosition(), coordinateSystem, programPreferences.getVertexSize());
			hitTest(selectedItems, area, camera.getTargetPosition(), coordinateSystem, programPreferences.getVertexSize());
		}

		@Override
		public void bone(final Bone object) {
			hitTest(selectedItems, area, object.getPivotPoint(), coordinateSystem, object.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem));
		}

		@Override
		public void attachment(final Attachment attachment) {
			hitTest(selectedItems, area, attachment.getPivotPoint(), coordinateSystem, attachment.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem) * 2);
		}
	}

	public VertexSelectionHelper getVertexSelectionHelper() {
		return vertexSelectionHelper;
	}

	@Override
	public RigAction rig() {
		final List<Bone> selectedBones = new ArrayList<>();
		for (final IdObject object : model.getEditableIdObjects()) {
			if (selectionManager.getSelection().contains(object.getPivotPoint())) {
				if (object instanceof Bone) {
					selectedBones.add((Bone) object);
				}
			}
		}
		return new RigAction(Collections.emptyList(), selectedBones);
	}

	private static String getNumberName(final String name, final int number) {
		return name + String.format("%3s", number).replace(' ', '0');
	}

	@Override
	public UndoAction addBone(final double x, final double y, final double z) {
		final Set<String> allBoneNames = new HashSet<>();
		for (final IdObject object : model.getModel().getIdObjects()) {
			allBoneNames.add(object.getName());
		}
		int nameNumber = 1;
		while (allBoneNames.contains(getNumberName("Bone", nameNumber))) {
			nameNumber++;
		}
		final Bone bone = new Bone(getNumberName("Bone", nameNumber));
		bone.setPivotPoint(new Vec3(x, y, z));
		final DrawBoneAction drawBoneAction = new DrawBoneAction(model, structureChangeListener, bone);
		drawBoneAction.redo();
		return drawBoneAction;
	}

	@Override
	public String getSelectedHDSkinningDescription() {
		return null;
	}

	@Override
	public String getSelectedMatricesDescription() {
		return null;
	}
}
