package com.hiveworkshop.rms.ui.application.edit.mesh.types.tpose;

import com.hiveworkshop.rms.editor.model.EventObject;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.editor.model.visitor.IdObjectVisitor;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.WrongModeException;
import com.hiveworkshop.rms.ui.application.edit.mesh.AbstractModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.cutpaste.CopiedModelData;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.nodes.DeleteNodesAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.selection.MakeNotEditableAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.tools.AutoCenterBonesAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.tools.RenameBoneAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.tools.SetParentAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.CompoundAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.DoNothingAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.EditabilityToggleHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectableComponent;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.VertexSelectionHelper;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.*;

public class TPoseModelEditor extends AbstractModelEditor<IdObject> {
	private final ProgramPreferences programPreferences;
	private final GenericSelectorVisitor genericSelectorVisitor;
	private final SelectionAtPointTester selectionAtPointTester;

	public TPoseModelEditor(ModelView model,
	                        ProgramPreferences programPreferences,
	                        SelectionManager<IdObject> selectionManager,
	                        ModelStructureChangeListener structureChangeListener) {
		super(selectionManager, model, structureChangeListener);
		this.programPreferences = programPreferences;
		genericSelectorVisitor = new GenericSelectorVisitor();
		selectionAtPointTester = new SelectionAtPointTester();
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
	public UndoAction addTeamColor() {
		return new DoNothingAction("add team color");
	}

	@Override
	public UndoAction splitGeoset() {
		return new DoNothingAction("split geoset");
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
	public UndoAction expandSelection() {
		throw new WrongModeException("Not supported in T-Pose mode");
	}

	@Override
	public UndoAction setSelectedBoneName(String name) {
		if (selectionManager.getSelection().size() != 1) {
			throw new IllegalStateException("Only one bone can be renamed at a time.");
		}
		IdObject node = selectionManager.getSelection().iterator().next();
		if (node == null) {
			throw new IllegalStateException("Selection is not a node");
		}
		RenameBoneAction renameBoneAction = new RenameBoneAction(node.getName(), name, node);
		renameBoneAction.redo();
		return renameBoneAction;
	}

	@Override
	public UndoAction addSelectedBoneSuffix(String name) {
		Set<IdObject> selection = selectionManager.getSelection();
		List<RenameBoneAction> actions = new ArrayList<>();
		for (IdObject bone : selection) {
			RenameBoneAction renameBoneAction = new RenameBoneAction(bone.getName(), bone.getName() + name, bone);
			renameBoneAction.redo();
			actions.add(renameBoneAction);
		}
		return new CompoundAction("add selected bone suffix", actions);
	}

	private void toggleSelection(Set<Vec3> selection, Vec3 position) {
		if (selection.contains(position)) {
			selection.remove(position);
		} else {
			selection.add(position);
		}
	}

	@Override
	public void selectByVertices(Collection<? extends Vec3> newSelection) {
		Set<IdObject> newlySelectedPivots = new HashSet<>();
		for (IdObject object : model.getEditableIdObjects()) {
			if (newSelection.contains(object.getPivotPoint())) {
				newlySelectedPivots.add(object);
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
							newlySelectedPivots.add(collisionShape);
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
		selectionManager.setSelection(newlySelectedPivots);
	}

	@Override
	protected List<IdObject> genericSelect(Rectangle2D region, CoordinateSystem coordinateSystem) {
		List<IdObject> selectedItems = new ArrayList<>();
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
	public UndoAction invertSelection() {
		throw new WrongModeException("Not supported in T-Pose mode");
	}

	@Override
	public UndoAction selectAll() {
		throw new WrongModeException("Not supported in T-Pose mode");
	}

	@Override
	protected UndoAction buildHideComponentAction(List<? extends SelectableComponent> selectableComponents, EditabilityToggleHandler editabilityToggleHandler, Runnable refreshGUIRunnable) {
		List<IdObject> previousSelection = new ArrayList<>(selectionManager.getSelection());
		Runnable truncateSelectionRunnable = () -> selectionManager.removeSelection(model.getModel().getIdObjects());
		Runnable unTruncateSelectionRunnable = () -> selectionManager.setSelection(previousSelection);
		return new MakeNotEditableAction(editabilityToggleHandler, truncateSelectionRunnable, unTruncateSelectionRunnable, refreshGUIRunnable);
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
			if (selectionManager.getSelection().contains(camera.getPosition()) || selectionManager.getSelection().contains(camera.getTargetPosition())) {
				deletedCameras.add(camera);
			}
		}
		DeleteNodesAction deleteNodesAction = new DeleteNodesAction(selectionManager.getSelectedVertices(), deletedIdObjects, deletedCameras, structureChangeListener, model, vertexSelectionHelper);
		deleteNodesAction.redo();
		return deleteNodesAction;
	}

	@Override
	public CopiedModelData copySelection() {
		Collection<? extends Vec3> selection = selectionManager.getSelectedVertices();
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
			if (selection.contains(camera.getTargetPosition()) || selection.contains(camera.getPosition())) {
				clonedCameras.add(camera);
			}
		}
		return new CopiedModelData(new ArrayList<>(), clonedNodes, clonedCameras);
	}

	@Override
	public UndoAction addVertex(double x, double y, double z, Vec3 preferredNormalFacingVector) {
		return new DoNothingAction("add vertex");
	}

	@Override
	public UndoAction createFaceFromSelection(Vec3 preferredFacingVector) {
		return new DoNothingAction("create face");
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
		public void popcornFxEmitter(ParticleEmitterPopcorn particleEmitter) {
			handleDefaultNode(point, axes, particleEmitter);
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
		private List<IdObject> selectedItems;
		private Rectangle2D area;
		private CoordinateSystem coordinateSystem;

		private GenericSelectorVisitor reset(List<IdObject> selectedItems, Rectangle2D area, CoordinateSystem coordinateSystem) {
			this.selectedItems = selectedItems;
			this.area = area;
			this.coordinateSystem = coordinateSystem;
			return this;
		}

		@Override
		public void ribbonEmitter(RibbonEmitter object) {
			double vertexSize = object.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem) * 2;
			if (hitTest(area, object.getPivotPoint(), coordinateSystem, vertexSize)) {
				selectedItems.add(object);
			}
		}

		@Override
		public void particleEmitter2(ParticleEmitter2 object) {
			double vertexSize = object.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem) * 2;
			if (hitTest(area, object.getPivotPoint(), coordinateSystem, vertexSize)) {
				selectedItems.add(object);
			}
		}

		@Override
		public void particleEmitter(ParticleEmitter object) {
			double vertexSize = object.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem) * 2;
			if (hitTest(area, object.getPivotPoint(), coordinateSystem, vertexSize)) {
				selectedItems.add(object);
			}
		}

		@Override
		public void popcornFxEmitter(ParticleEmitterPopcorn object) {
			double vertexSize = object.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem) * 2;
			if (hitTest(area, object.getPivotPoint(), coordinateSystem, vertexSize)) {
				selectedItems.add(object);
			}
		}

		@Override
		public void light(Light object) {
			double vertexSize = object.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem) * 2;
			if (hitTest(area, object.getPivotPoint(), coordinateSystem, vertexSize)) {
				selectedItems.add(object);
			}
		}

		@Override
		public void helper(Helper object) {
			double vertexSize = object.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem);
			if (hitTest(area, object.getPivotPoint(), coordinateSystem, vertexSize)) {
				selectedItems.add(object);
			}
		}

		@Override
		public void eventObject(EventObject object) {
			double vertexSize = object.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem) * 2;
			if (hitTest(area, object.getPivotPoint(), coordinateSystem, vertexSize)) {
				selectedItems.add(object);
			}
		}

		@Override
		public void bone(Bone object) {
			double vertexSize = object.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem);
			if (hitTest(area, object.getPivotPoint(), coordinateSystem, vertexSize)) {
				selectedItems.add(object);
			}
		}

		@Override
		public void attachment(Attachment object) {
			double vertexSize = object.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem) * 2;
			if (hitTest(area, object.getPivotPoint(), coordinateSystem, vertexSize)) {
				selectedItems.add(object);
			}
		}

		@Override
		public void collisionShape(CollisionShape object) {
			double vertexSize = object.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem) * 2;
			if (hitTest(area, object.getPivotPoint(), coordinateSystem, vertexSize)) {
				selectedItems.add(object);
			}
			for (Vec3 vertex : object.getVertices()) {
				if (hitTest(area, vertex, coordinateSystem, IdObject.DEFAULT_CLICK_RADIUS)) {
					selectedItems.add(object);
				}
			}
		}

		@Override
		public void camera(Camera object) {
		}
	}

	public VertexSelectionHelper getVertexSelectionHelper() {
		return vertexSelectionHelper;
	}
}
