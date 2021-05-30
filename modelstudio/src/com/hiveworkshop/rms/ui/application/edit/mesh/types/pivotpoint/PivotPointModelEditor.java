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
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.DoNothingMoveActionAdapter;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericMoveAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.EditabilityToggleHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectableComponent;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectableComponentVisitor;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.VertexSelectionHelper;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.*;

public class PivotPointModelEditor extends AbstractModelEditor<Vec3> {
	private final ProgramPreferences programPreferences;
	private final GenericSelectorVisitor genericSelectorVisitor;
	private final SelectionAtPointTester selectionAtPointTester;

	public PivotPointModelEditor(ModelView model, ProgramPreferences programPreferences, SelectionManager<Vec3> selectionManager, ModelStructureChangeListener structureChangeListener) {
		super(selectionManager, model, structureChangeListener);
		this.programPreferences = programPreferences;
		genericSelectorVisitor = new GenericSelectorVisitor();
		selectionAtPointTester = new SelectionAtPointTester();
	}

	private static String getNumberName(String name, int number) {
		return name + String.format("%3s", number).replace(' ', '0');
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
	public GenericMoveAction addPlane(Vec2 p1, Vec2 p2, byte dim1, byte dim2, Vec3 facingVector,
	                                  int numberOfWidthSegments, int numberOfHeightSegments) {
		return new DoNothingMoveActionAdapter(new DoNothingAction("Add Plane"));
	}

	@Override
	public GenericMoveAction addBox(Vec2 p1, Vec2 p2, byte dim1, byte dim2, Vec3 facingVector, int numberOfLengthSegments, int numberOfWidthSegments, int numberOfHeightSegments) {
		return new DoNothingMoveActionAdapter(new DoNothingAction("Add Box"));
	}

	@Override
	public UndoAction setParent(IdObject node) {
		Map<IdObject, IdObject> nodeToOldParent = new HashMap<>();
		for (IdObject b : model.getEditableIdObjects()) {
			if (selectionManager.getSelection().contains(b.getPivotPoint())) {
				nodeToOldParent.put(b, b.getParent());
			}
		}
		SetParentAction setParentAction = new SetParentAction(nodeToOldParent, node, structureChangeListener);
		setParentAction.redo();
		return setParentAction;
	}

	@Override
	public UndoAction autoCenterSelectedBones() {
		Set<IdObject> selBones = new HashSet<>();
		for (IdObject b : model.getEditableIdObjects()) {
			if (selectionManager.getSelection().contains(b.getPivotPoint())) {
				selBones.add(b);
			}
		}

		Map<Geoset, Map<Bone, List<GeosetVertex>>> geosetBoneMaps = new HashMap<>();
		for (Geoset geo : model.getModel().getGeosets()) {
			geosetBoneMaps.put(geo, geo.getBoneMap());
		}

		Map<Bone, Vec3> boneToOldPosition = new HashMap<>();
		for (IdObject obj : selBones) {
			if (Bone.class.isAssignableFrom(obj.getClass())) {
				Bone bone = (Bone) obj;
				List<GeosetVertex> childVerts = new ArrayList<>();
				for (Geoset geo : model.getModel().getGeosets()) {
					List<GeosetVertex> vertices = geosetBoneMaps.get(geo).get(bone);
					if (vertices != null) {
						childVerts.addAll(vertices);
					}
				}
				if (childVerts.size() > 0) {
					Vec3 pivotPoint = bone.getPivotPoint();
					boneToOldPosition.put(bone, new Vec3(pivotPoint));
					pivotPoint.set(Vec3.centerOfGroup(childVerts));
				}
			}
		}
		return new AutoCenterBonesAction(boneToOldPosition);
	}

	@Override
	public UndoAction setSelectedBoneName(String name) {
		if (selectionManager.getSelection().size() != 1) {
			throw new IllegalStateException("Only one bone can be renamed at a time.");
		}
		Vec3 selectedVertex = selectionManager.getSelection().iterator().next();
		IdObject node = null;
		for (IdObject bone : model.getEditableIdObjects()) {
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
		RenameBoneAction renameBoneAction = new RenameBoneAction(node.getName(), name, node);
		renameBoneAction.redo();
		return renameBoneAction;
	}

	@Override
	public UndoAction addSelectedBoneSuffix(String name) {
		Set<Vec3> selection = selectionManager.getSelection();
		List<RenameBoneAction> actions = new ArrayList<>();
		for (IdObject bone : model.getEditableIdObjects()) {
			if (selection.contains(bone.getPivotPoint())) {
				RenameBoneAction renameBoneAction = new RenameBoneAction(bone.getName(), bone.getName() + name, bone);
				renameBoneAction.redo();
				actions.add(renameBoneAction);
			}
		}
		return new CompoundAction("add selected bone suffix", actions);
	}

	@Override
	public UndoAction invertSelection() {
		List<Vec3> oldSelection = new ArrayList<>(selectionManager.getSelection());
		Set<Vec3> invertedSelection = new HashSet<>(selectionManager.getSelection());
		IdObjectVisitor visitor = new IdObjectVisitor() {
			@Override
			public void ribbonEmitter(RibbonEmitter particleEmitter) {
				toggleSelection(invertedSelection, particleEmitter.getPivotPoint());
			}

			@Override
			public void particleEmitter2(ParticleEmitter2 particleEmitter) {
				toggleSelection(invertedSelection, particleEmitter.getPivotPoint());
			}

			@Override
			public void particleEmitter(ParticleEmitter particleEmitter) {
				toggleSelection(invertedSelection, particleEmitter.getPivotPoint());
			}

			@Override
			public void popcornFxEmitter(ParticleEmitterPopcorn popcornFxEmitter) {
				toggleSelection(invertedSelection, popcornFxEmitter.getPivotPoint());
			}

			@Override
			public void light(Light light) {
				toggleSelection(invertedSelection, light.getPivotPoint());
			}

			@Override
			public void helper(Helper object) {
				toggleSelection(invertedSelection, object.getPivotPoint());
			}

			@Override
			public void eventObject(EventObject eventObject) {
				toggleSelection(invertedSelection, eventObject.getPivotPoint());
			}

			@Override
			public void collisionShape(CollisionShape collisionShape) {
				toggleSelection(invertedSelection, collisionShape.getPivotPoint());
				for (Vec3 vertex : collisionShape.getVertices()) {
					toggleSelection(invertedSelection, vertex);
				}
			}

			@Override
			public void camera(Camera camera) {
				toggleSelection(invertedSelection, camera.getPosition());
				toggleSelection(invertedSelection, camera.getTargetPosition());
			}

			@Override
			public void bone(Bone object) {
				toggleSelection(invertedSelection, object.getPivotPoint());
			}

			@Override
			public void attachment(Attachment attachment) {
				toggleSelection(invertedSelection, attachment.getPivotPoint());
			}
		};
		for (IdObject node : model.getEditableIdObjects()) {
			node.apply(visitor);
		}
		for (Camera object : model.getEditableCameras()) {
			visitor.camera(object);
		}
		selectionManager.setSelection(invertedSelection);
		return new SetSelectionAction<>(invertedSelection, oldSelection, selectionManager, "invert selection");
	}

	private void toggleSelection(Set<Vec3> selection, Vec3 position) {
		if (selection.contains(position)) {
			selection.remove(position);
		} else {
			selection.add(position);
		}
	}

	@Override
	public UndoAction selectAll() {
		List<Vec3> oldSelection = new ArrayList<>(selectionManager.getSelection());
		Set<Vec3> allSelection = new HashSet<>();
		IdObjectVisitor visitor = new IdObjectVisitor() {
			@Override
			public void ribbonEmitter(RibbonEmitter particleEmitter) {
				allSelection.add(particleEmitter.getPivotPoint());
			}

			@Override
			public void particleEmitter2(ParticleEmitter2 particleEmitter) {
				allSelection.add(particleEmitter.getPivotPoint());
			}

			@Override
			public void particleEmitter(ParticleEmitter particleEmitter) {
				allSelection.add(particleEmitter.getPivotPoint());
			}

			@Override
			public void popcornFxEmitter(ParticleEmitterPopcorn popcornFxEmitter) {
				allSelection.add(popcornFxEmitter.getPivotPoint());
			}

			@Override
			public void light(Light light) {
				allSelection.add(light.getPivotPoint());
			}

			@Override
			public void helper(Helper object) {
				allSelection.add(object.getPivotPoint());
			}

			@Override
			public void eventObject(EventObject eventObject) {
				allSelection.add(eventObject.getPivotPoint());
			}

			@Override
			public void collisionShape(CollisionShape collisionShape) {
				allSelection.add(collisionShape.getPivotPoint());
				allSelection.addAll(collisionShape.getVertices());
			}

			@Override
			public void camera(Camera camera) {
				allSelection.add(camera.getPosition());
				allSelection.add(camera.getTargetPosition());
			}

			@Override
			public void bone(Bone object) {
				allSelection.add(object.getPivotPoint());
			}

			@Override
			public void attachment(Attachment attachment) {
				allSelection.add(attachment.getPivotPoint());
			}
		};
		for (IdObject node : model.getEditableIdObjects()) {
			node.apply(visitor);
		}
		for (Camera object : model.getEditableCameras()) {
			visitor.camera(object);
		}
		selectionManager.setSelection(allSelection);
		return new SetSelectionAction<>(allSelection, oldSelection, selectionManager, "select all");
	}

	@Override
	protected List<Vec3> genericSelect(Rectangle2D region, CoordinateSystem coordinateSystem) {
		List<Vec3> selectedItems = new ArrayList<>();
		Rectangle2D area = getArea(region);

		IdObjectVisitor visitor = genericSelectorVisitor.reset(selectedItems, area, coordinateSystem);
		for (IdObject object : model.getEditableIdObjects()) {
			object.apply(visitor);
		}
		for (Camera camera : model.getEditableCameras()) {
			visitor.camera(camera);
		}
		return selectedItems;
	}

	@Override
	public boolean canSelectAt(Point point, CoordinateSystem axes) {
		IdObjectVisitor visitor = selectionAtPointTester.reset(axes, point);
		for (IdObject object : model.getEditableIdObjects()) {
			object.apply(visitor);
		}
		for (Camera camera : model.getEditableCameras()) {
			visitor.camera(camera);
		}
		return selectionAtPointTester.isMouseOverVertex();
	}

	@Override
	public void selectByVertices(Collection<? extends Vec3> newSelection) {
		List<Vec3> newlySelectedPivots = new ArrayList<>();
		for (IdObject object : model.getEditableIdObjects()) {
			if (newSelection.contains(object.getPivotPoint())) {
				newlySelectedPivots.add(object.getPivotPoint());
			}
			object.apply(new IdObjectVisitor() {
				@Override
				public void ribbonEmitter(RibbonEmitter particleEmitter) {
				}

				@Override
				public void particleEmitter2(ParticleEmitter2 particleEmitter) {
				}

				@Override
				public void particleEmitter(ParticleEmitter particleEmitter) {
				}

				@Override
				public void popcornFxEmitter(ParticleEmitterPopcorn popcornFxEmitter) {
				}

				@Override
				public void light(Light light) {
				}

				@Override
				public void helper(Helper object) {
				}

				@Override
				public void eventObject(EventObject eventObject) {
				}

				@Override
				public void collisionShape(CollisionShape collisionShape) {
					for (Vec3 vertex : collisionShape.getVertices()) {
						if (newSelection.contains(vertex)) {
							newlySelectedPivots.add(vertex);
						}
					}
				}

				@Override
				public void camera(Camera camera) {
				}

				@Override
				public void bone(Bone object) {
				}

				@Override
				public void attachment(Attachment attachment) {
				}
			});
		}
		for (Camera camera : model.getEditableCameras()) {
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
		Set<Vec3> expandedSelection = new HashSet<>(selectionManager.getSelection());
		Set<Vec3> oldSelection = new HashSet<>(selectionManager.getSelection());
		IdObjectVisitor visitor = new IdObjectVisitor() {
			@Override
			public void ribbonEmitter(RibbonEmitter particleEmitter) {
			}

			@Override
			public void particleEmitter2(ParticleEmitter2 particleEmitter) {
			}

			@Override
			public void particleEmitter(ParticleEmitter particleEmitter) {
			}

			@Override
			public void popcornFxEmitter(ParticleEmitterPopcorn popcornFxEmitter) {
			}

			@Override
			public void light(Light light) {
			}

			@Override
			public void helper(Helper object) {
			}

			@Override
			public void eventObject(EventObject eventObject) {
			}

			@Override
			public void collisionShape(CollisionShape collisionShape) {
				boolean selected = false;
				if (oldSelection.contains(collisionShape.getPivotPoint())) {
					selected = true;
				}
				for (Vec3 vertex : collisionShape.getVertices()) {
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
			public void camera(Camera camera) {
				if (oldSelection.contains(camera.getTargetPosition()) || oldSelection.contains(camera.getPosition())) {
					expandedSelection.add(camera.getPosition());
					expandedSelection.add(camera.getTargetPosition());
				}
			}

			@Override
			public void bone(Bone object) {

			}

			@Override
			public void attachment(Attachment attachment) {

			}
		};
		for (IdObject node : model.getEditableIdObjects()) {
			node.apply(visitor);
		}
		for (Camera camera : model.getEditableCameras()) {
			visitor.camera(camera);
		}
		selectionManager.setSelection(expandedSelection);
		return new SetSelectionAction<>(expandedSelection, oldSelection, selectionManager, "expand selection");
	}

	@Override
	protected UndoAction buildHideComponentAction(List<? extends SelectableComponent> selectableComponents, EditabilityToggleHandler editabilityToggleHandler, Runnable refreshGUIRunnable) {
		List<Vec3> previousSelection = new ArrayList<>(selectionManager.getSelection());
		List<Vec3> possibleVerticesToTruncate = new ArrayList<>();
		for (SelectableComponent component : selectableComponents) {
			component.visit(new SelectableComponentVisitor() {
				@Override
				public void accept(Camera camera) {
					possibleVerticesToTruncate.add(camera.getPosition());
					possibleVerticesToTruncate.add(camera.getTargetPosition());
				}

				@Override
				public void accept(IdObject node) {
					possibleVerticesToTruncate.add(node.getPivotPoint());
					node.apply(new IdObjectVisitor() {
						@Override
						public void ribbonEmitter(RibbonEmitter particleEmitter) {
						}

						@Override
						public void particleEmitter2(ParticleEmitter2 particleEmitter) {
						}

						@Override
						public void particleEmitter(ParticleEmitter particleEmitter) {
						}

						@Override
						public void popcornFxEmitter(ParticleEmitterPopcorn popcornFxEmitter) {
						}

						@Override
						public void light(Light light) {
						}

						@Override
						public void helper(Helper object) {
						}

						@Override
						public void eventObject(EventObject eventObject) {
						}

						@Override
						public void collisionShape(CollisionShape collisionShape) {
							possibleVerticesToTruncate.addAll(collisionShape.getVertices());
						}

						@Override
						public void camera(Camera camera) {
							// do not use, visitor for IdObjects only
						}

						@Override
						public void bone(Bone object) {
						}

						@Override
						public void attachment(Attachment attachment) {
						}
					});
				}

				@Override
				public void accept(Geoset geoset) {
				}
			});
		}
		Runnable truncateSelectionRunnable = () -> selectionManager.removeSelection(possibleVerticesToTruncate);
		Runnable unTruncateSelectionRunnable = () -> selectionManager.setSelection(previousSelection);
		return new MakeNotEditableAction(editabilityToggleHandler, truncateSelectionRunnable, unTruncateSelectionRunnable, refreshGUIRunnable);
	}

	@Override
	public CopiedModelData copySelection() {
		Set<Vec3> selection = selectionManager.getSelection();
		Set<IdObject> clonedNodes = new HashSet<>();
		Set<Camera> clonedCameras = new HashSet<>();
		for (IdObject b : model.getEditableIdObjects()) {
			if (selection.contains(b.getPivotPoint())) {
				clonedNodes.add(b.copy());
			}
		}
		for (IdObject obj : clonedNodes) {
			if (!clonedNodes.contains(obj.getParent())) {
				obj.setParent(null);
			}
		}
		for (Camera camera : model.getEditableCameras()) {
			if (selection.contains(camera.getTargetPosition())
					|| selection.contains(camera.getPosition())) {
				clonedCameras.add(camera);
			}
		}
		return new CopiedModelData(new ArrayList<>(), clonedNodes, clonedCameras);
	}

	@Override
	public void rawScale(double centerX, double centerY, double centerZ, double scaleX, double scaleY, double scaleZ) {
		super.rawScale(centerX, centerY, centerZ, scaleX, scaleY, scaleZ);
		for (IdObject b : model.getEditableIdObjects()) {
			if (selectionManager.getSelection().contains(b.getPivotPoint())) {
				b.apply(new IdObjectVisitor() {
					@Override
					public void ribbonEmitter(RibbonEmitter particleEmitter) {
					}

					@Override
					public void particleEmitter2(ParticleEmitter2 particleEmitter) {
					}

					@Override
					public void particleEmitter(ParticleEmitter particleEmitter) {
					}

					@Override
					public void popcornFxEmitter(ParticleEmitterPopcorn popcornFxEmitter) {
					}

					@Override
					public void light(Light light) {
					}

					@Override
					public void helper(Helper object) {
						Vec3AnimFlag translation = (Vec3AnimFlag) object.find("Translation");
						if (translation != null) {
							for (int i = 0; i < translation.size(); i++) {
								Vec3 scaleData = translation.getValues().get(i);
								scaleData.scale(0, 0, 0, scaleX, scaleY, scaleZ);
								if (translation.tans()) {
									Vec3 inTanData = translation.getInTans().get(i);
									inTanData.scale(0, 0, 0, scaleX, scaleY, scaleZ);
									Vec3 outTanData = translation.getInTans().get(i);
									outTanData.scale(0, 0, 0, scaleX, scaleY, scaleZ);
								}
							}
						}
					}

					@Override
					public void eventObject(EventObject eventObject) {
					}

					@Override
					public void collisionShape(CollisionShape collisionShape) {
						ExtLog extents = collisionShape.getExtents();
						if ((extents != null) && (scaleX == scaleY) && (scaleY == scaleZ)) {
							extents.setBoundsRadius(extents.getBoundsRadius() * scaleX);
						}
					}

					@Override
					public void camera(Camera camera) {
					}

					@Override
					public void bone(Bone object) {
						Vec3AnimFlag translation = (Vec3AnimFlag) object.find("Translation");
						if (translation != null) {
							for (int i = 0; i < translation.size(); i++) {
								Vec3 scaleData = translation.getValues().get(i);
								scaleData.scale(0, 0, 0, scaleX, scaleY, scaleZ);
								if (translation.tans()) {
									Vec3 inTanData = translation.getInTans().get(i);
									inTanData.scale(0, 0, 0, scaleX, scaleY, scaleZ);
									Vec3 outTanData = translation.getInTans().get(i);
									outTanData.scale(0, 0, 0, scaleX, scaleY, scaleZ);
								}
							}
						}
					}

					@Override
					public void attachment(Attachment attachment) {
					}
				});
			}
		}
	}

	@Override
	public UndoAction deleteSelectedComponents() {
		List<IdObject> deletedIdObjects = new ArrayList<>();
		for (IdObject object : model.getEditableIdObjects()) {
			if (selectionManager.getSelection().contains(object.getPivotPoint())) {
				deletedIdObjects.add(object);
			}
		}
		List<Camera> deletedCameras = new ArrayList<>();
		for (Camera camera : model.getEditableCameras()) {
			if (selectionManager.getSelection().contains(camera.getPosition())
					|| selectionManager.getSelection().contains(camera.getTargetPosition())) {
				deletedCameras.add(camera);
			}
		}
		DeleteNodesAction deleteNodesAction = new DeleteNodesAction(selectionManager.getSelection(), deletedIdObjects, deletedCameras, structureChangeListener, model, vertexSelectionHelper);
		deleteNodesAction.redo();
		return deleteNodesAction;
	}

	@Override
	public UndoAction addVertex(double x, double y, double z, Vec3 preferredNormalFacingVector) {
		return new DoNothingAction("add vertex");
	}

	@Override
	public void rawTranslate(double x, double y, double z) {
		super.rawTranslate(x, y, z);
		for (IdObject b : model.getEditableIdObjects()) {
			if (selectionManager.getSelection().contains(b.getPivotPoint())) {
				float[] bindPose = b.getBindPose();
				if (bindPose != null) {
					bindPose[9] += x;
					bindPose[10] += y;
					bindPose[11] += z;
				}
			}
		}
	}

	@Override
	public UndoAction createFaceFromSelection(Vec3 preferredFacingVector) {
		return new DoNothingAction("create face");
	}

	@Override
	public RigAction rig() {
		System.out.println("pivot Rig, sel verts: " + selectionManager.getSelectedVertices().size());
		List<Bone> selectedBones = new ArrayList<>();
		for (IdObject object : model.getEditableIdObjects()) {
			if (selectionManager.getSelection().contains(object.getPivotPoint())) {
				if (object instanceof Bone) {
					selectedBones.add((Bone) object);
				}
			}
		}
		return new RigAction(Collections.emptyList(), selectedBones);
	}

	public VertexSelectionHelper getVertexSelectionHelper() {
		return vertexSelectionHelper;
	}

	@Override
	public UndoAction addBone(double x, double y, double z) {
		Set<String> allBoneNames = new HashSet<>();
		for (IdObject object : model.getModel().getIdObjects()) {
			allBoneNames.add(object.getName());
		}
		int nameNumber = 1;
		while (allBoneNames.contains(getNumberName("Bone", nameNumber))) {
			nameNumber++;
		}
		Bone bone = new Bone(getNumberName("Bone", nameNumber));
		bone.setPivotPoint(new Vec3(x, y, z));
		DrawBoneAction drawBoneAction = new DrawBoneAction(model, structureChangeListener, bone);
		drawBoneAction.redo();
		return drawBoneAction;
	}

	private class SelectionAtPointTester implements IdObjectVisitor {
		private CoordinateSystem axes;
		private Point point;
		private boolean mouseOverVertex;

		private SelectionAtPointTester reset(CoordinateSystem axes, Point point) {
			this.axes = axes;
			this.point = point;
			mouseOverVertex = false;
			return this;
		}

		@Override
		public void ribbonEmitter(RibbonEmitter particleEmitter) {
			handleDefaultNode(point, axes, particleEmitter);
		}

		private void handleDefaultNode(Point point, CoordinateSystem axes, IdObject node) {
			if (hitTest(node.getPivotPoint(), CoordinateSystem.Util.geom(axes, point), axes, node.getClickRadius(axes) * CoordinateSystem.Util.getZoom(axes) * 2)) {
				mouseOverVertex = true;
			}
		}

		@Override
		public void particleEmitter2(ParticleEmitter2 particleEmitter) {
			handleDefaultNode(point, axes, particleEmitter);
		}

		@Override
		public void particleEmitter(ParticleEmitter particleEmitter) {
			handleDefaultNode(point, axes, particleEmitter);
		}

		@Override
		public void popcornFxEmitter(ParticleEmitterPopcorn popcornFxEmitter) {
			handleDefaultNode(point, axes, popcornFxEmitter);
		}

		@Override
		public void light(Light light) {
			handleDefaultNode(point, axes, light);
		}

		@Override
		public void helper(Helper node) {
			if (hitTest(node.getPivotPoint(), CoordinateSystem.Util.geom(axes, point), axes, node.getClickRadius(axes) * CoordinateSystem.Util.getZoom(axes))) {
				mouseOverVertex = true;
			}
		}

		@Override
		public void eventObject(EventObject eventObject) {
			handleDefaultNode(point, axes, eventObject);
		}

		@Override
		public void collisionShape(CollisionShape collisionShape) {
			handleDefaultNode(point, axes, collisionShape);
			for (Vec3 vertex : collisionShape.getVertices()) {
				if (hitTest(vertex, CoordinateSystem.Util.geom(axes, point), axes, IdObject.DEFAULT_CLICK_RADIUS)) {
					mouseOverVertex = true;
				}
			}
		}

		@Override
		public void camera(Camera camera) {
			if (hitTest(camera.getPosition(), CoordinateSystem.Util.geom(axes, point), axes, programPreferences.getVertexSize())) {
				mouseOverVertex = true;
			}
			if (hitTest(camera.getTargetPosition(), CoordinateSystem.Util.geom(axes, point), axes, programPreferences.getVertexSize())) {
				mouseOverVertex = true;
			}
		}

		@Override
		public void bone(Bone node) {
			if (hitTest(node.getPivotPoint(), CoordinateSystem.Util.geom(axes, point), axes, node.getClickRadius(axes) * CoordinateSystem.Util.getZoom(axes))) {
				mouseOverVertex = true;
			}
		}

		@Override
		public void attachment(Attachment attachment) {
			handleDefaultNode(point, axes, attachment);
		}

		public boolean isMouseOverVertex() {
			return mouseOverVertex;
		}
	}

	private class GenericSelectorVisitor implements IdObjectVisitor {
		private List<Vec3> selectedItems;
		private Rectangle2D area;
		private CoordinateSystem coordinateSystem;

		private GenericSelectorVisitor reset(List<Vec3> selectedItems, Rectangle2D area, CoordinateSystem coordinateSystem) {
			this.selectedItems = selectedItems;
			this.area = area;
			this.coordinateSystem = coordinateSystem;
			return this;
		}

		@Override
		public void ribbonEmitter(RibbonEmitter object) {
			double vertexSize = object.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem) * 2;
			if (hitTest(area, object.getPivotPoint(), coordinateSystem, vertexSize)) {
				selectedItems.add(object.getPivotPoint());
			}
		}

		@Override
		public void particleEmitter2(ParticleEmitter2 object) {
			double vertexSize = object.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem) * 2;
			if (hitTest(area, object.getPivotPoint(), coordinateSystem, vertexSize)) {
				selectedItems.add(object.getPivotPoint());
			}
		}

		@Override
		public void particleEmitter(ParticleEmitter object) {
			double vertexSize = object.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem) * 2;
			if (hitTest(area, object.getPivotPoint(), coordinateSystem, vertexSize)) {
				selectedItems.add(object.getPivotPoint());
			}
		}

		@Override
		public void popcornFxEmitter(ParticleEmitterPopcorn object) {
			double vertexSize = object.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem) * 2;
			if (hitTest(area, object.getPivotPoint(), coordinateSystem, vertexSize)) {
				selectedItems.add(object.getPivotPoint());
			}
		}

		@Override
		public void light(Light object) {
			double vertexSize = object.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem) * 2;
			if (hitTest(area, object.getPivotPoint(), coordinateSystem, vertexSize)) {
				selectedItems.add(object.getPivotPoint());
			}
		}

		@Override
		public void helper(Helper object) {
			double vertexSize = object.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem);
			if (hitTest(area, object.getPivotPoint(), coordinateSystem, vertexSize)) {
				selectedItems.add(object.getPivotPoint());
			}
		}

		@Override
		public void eventObject(EventObject object) {
			double vertexSize = object.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem) * 2;
			if (hitTest(area, object.getPivotPoint(), coordinateSystem, vertexSize)) {
				selectedItems.add(object.getPivotPoint());
			}
		}

		@Override
		public void bone(Bone object) {
			double vertexSize = object.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem);
			if (hitTest(area, object.getPivotPoint(), coordinateSystem, vertexSize)) {
				selectedItems.add(object.getPivotPoint());
			}
		}

		@Override
		public void attachment(Attachment object) {
			double vertexSize = object.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem) * 2;
			if (hitTest(area, object.getPivotPoint(), coordinateSystem, vertexSize)) {
				selectedItems.add(object.getPivotPoint());
			}
		}

		@Override
		public void collisionShape(CollisionShape object) {
			double vertexSize = object.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem) * 2;
			if (hitTest(area, object.getPivotPoint(), coordinateSystem, vertexSize)) {
				selectedItems.add(object.getPivotPoint());
			}

			for (Vec3 vertex : object.getVertices()) {
				if (hitTest(area, vertex, coordinateSystem, IdObject.DEFAULT_CLICK_RADIUS)) {
					selectedItems.add(vertex);
				}
			}
		}

		@Override
		public void camera(Camera object) {
			int vertexSize = programPreferences.getVertexSize();
			if (hitTest(area, object.getPosition(), coordinateSystem, vertexSize)) {
				selectedItems.add(object.getPosition());
			}
			if (hitTest(area, object.getTargetPosition(), coordinateSystem, vertexSize)) {
				selectedItems.add(object.getTargetPosition());
			}
		}
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
