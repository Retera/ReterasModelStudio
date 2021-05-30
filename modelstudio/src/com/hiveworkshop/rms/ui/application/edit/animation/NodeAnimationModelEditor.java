package com.hiveworkshop.rms.ui.application.edit.animation;

import com.hiveworkshop.rms.editor.model.EventObject;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.QuatAnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.editor.model.visitor.IdObjectVisitor;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.render3d.RenderNode;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.parsers.mdlx.InterpolationType;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.AbstractSelectingEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.cutpaste.CopiedModelData;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.ModelEditorActionType;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.animation.*;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.editor.StaticMeshMoveAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.selection.MakeNotEditableAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.selection.SetSelectionAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.tools.CloneAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.tools.RigAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.CompoundAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericMoveAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericRotateAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericScaleAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.ClonedNodeNamePicker;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.EditabilityToggleHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectableComponent;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectableComponentVisitor;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.*;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.*;
import java.util.function.BiFunction;

public class NodeAnimationModelEditor extends AbstractSelectingEditor<IdObject> {
	private final ProgramPreferences programPreferences;
	private final GenericSelectorVisitor genericSelectorVisitor;
	private final SelectionAtPointTester selectionAtPointTester;
	private final ModelView model;
	private final RenderModel renderModel;
	private final ModelStructureChangeListener structureChangeListener;

	public NodeAnimationModelEditor(ModelView model, ProgramPreferences programPreferences, SelectionManager<IdObject> selectionManager, RenderModel renderModel, ModelStructureChangeListener structureChangeListener) {
		super(selectionManager);
		this.model = model;
		this.programPreferences = programPreferences;
		this.structureChangeListener = structureChangeListener;
		genericSelectorVisitor = new GenericSelectorVisitor();
		selectionAtPointTester = new SelectionAtPointTester();
		this.renderModel = renderModel;
	}

	@Override
	public UndoAction autoCenterSelectedBones() {
		throw new WrongModeException("Unable to autocenter bones in Animation Editor");
	}

	public static void hitTest(List<IdObject> selectedItems, Rectangle2D area, Vec3 geosetVertex, CoordinateSystem coordinateSystem, double vertexSize, IdObject object, RenderModel renderModel) {
		RenderNode renderNode = renderModel.getRenderNode(object);
		Vec4 pivotHeap = new Vec4(geosetVertex, 1);
		pivotHeap.transform(renderNode.getWorldMatrix());
		byte dim1 = coordinateSystem.getPortFirstXYZ();
		byte dim2 = coordinateSystem.getPortSecondXYZ();
		double minX = coordinateSystem.viewX(area.getMinX());
		double minY = coordinateSystem.viewY(area.getMinY());
		double maxX = coordinateSystem.viewX(area.getMaxX());
		double maxY = coordinateSystem.viewY(area.getMaxY());
		double vertexX = pivotHeap.getCoord(dim1);
		double x = coordinateSystem.viewX(vertexX);
		double vertexY = pivotHeap.getCoord(dim2);
		double y = coordinateSystem.viewY(vertexY);
		if ((distance(x, y, minX, minY) <= (vertexSize / 2.0)) || (distance(x, y, maxX, maxY) <= (vertexSize / 2.0)) || area.contains(vertexX, vertexY)) {
			selectedItems.add(object);
		}
	}

	public static boolean hitTest(Vec3 vertex, Point2D point, CoordinateSystem coordinateSystem, double vertexSize, Mat4 worldMatrix) {
		Vec4 pivotHeap = new Vec4(vertex, 1);
		pivotHeap.transform(worldMatrix);
		double x = coordinateSystem.viewX(pivotHeap.getCoord(coordinateSystem.getPortFirstXYZ()));
		double y = coordinateSystem.viewY(pivotHeap.getCoord(coordinateSystem.getPortSecondXYZ()));
		double px = coordinateSystem.viewX(point.getX());
		double py = coordinateSystem.viewY(point.getY());
		return Point2D.distance(px, py, x, y) <= (vertexSize / 2.0);
	}

	@Override
	public UndoAction addTeamColor() {
		throw new WrongModeException("Unable to add team color in Animation Editor");
	}

	public static double distance(double vertexX, double vertexY, double x, double y) {
		double dx = x - vertexX;
		double dy = y - vertexY;
		return Math.sqrt((dx * dx) + (dy * dy));
	}

	@Override
	public UndoAction expandSelection() {
		throw new WrongModeException("Unable to expand selection in Node Animation Editor");
	}

	@Override
	public UndoAction setSelectedBoneName(String name) {
		throw new WrongModeException("Unable to change bone names in Animation Editor");
	}

	@Override
	public UndoAction addSelectedBoneSuffix(String name) {
		throw new WrongModeException("Unable to change bone names in Animation Editor");
	}

	@Override
	public void selectByVertices(Collection<? extends Vec3> newSelection) {
		Set<IdObject> newlySelectedObjects = new HashSet<>();
		for (IdObject object : model.getEditableIdObjects()) {
			if (newSelection.contains(object.getPivotPoint())) {
				newlySelectedObjects.add(object);
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
							newlySelectedObjects.add(collisionShape);
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
		// TODO cameras in a second CameraAnimationEditor
		selectionManager.setSelection(newlySelectedObjects);
	}

	@Override
	public UndoAction invertSelection() {
		List<IdObject> oldSelection = new ArrayList<>(selectionManager.getSelection());
		Set<IdObject> invertedSelection = new HashSet<>(selectionManager.getSelection());
		for (IdObject node : model.getEditableIdObjects()) {
			toggleSelection(invertedSelection, node);
		}
		selectionManager.setSelection(invertedSelection);
		return new SetSelectionAction<>(invertedSelection, oldSelection, selectionManager, "invert selection");
	}

	private void toggleSelection(Set<IdObject> selection, IdObject position) {
		if (selection.contains(position)) {
			selection.remove(position);
		} else {
			selection.add(position);
		}
	}

	@Override
	public UndoAction selectAll() {
		List<IdObject> oldSelection = new ArrayList<>(selectionManager.getSelection());
		Set<IdObject> allSelection = new HashSet<>(model.getEditableIdObjects());
		selectionManager.setSelection(allSelection);
		return new SetSelectionAction<>(allSelection, oldSelection, selectionManager, "select all");
	}

	@Override
	protected List<IdObject> genericSelect(Rectangle2D region, CoordinateSystem coordinateSystem) {
		List<IdObject> selectedItems = new ArrayList<>();
		double startingClickX = region.getX();
		double startingClickY = region.getY();
		double endingClickX = region.getX() + region.getWidth();
		double endingClickY = region.getY() + region.getHeight();

		double minX = Math.min(startingClickX, endingClickX);
		double minY = Math.min(startingClickY, endingClickY);
		double maxX = Math.max(startingClickX, endingClickX);
		double maxY = Math.max(startingClickY, endingClickY);
		Rectangle2D area = new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
		IdObjectVisitor visitor = genericSelectorVisitor.reset(selectedItems, area, coordinateSystem);
		for (IdObject object : model.getEditableIdObjects()) {
			object.apply(visitor);
		}
		return selectedItems;
	}

	@Override
	public boolean canSelectAt(Point point, CoordinateSystem axes) {
		IdObjectVisitor visitor = selectionAtPointTester.reset(axes, point);
		for (IdObject object : model.getEditableIdObjects()) {
			object.apply(visitor);
		}
		return selectionAtPointTester.isMouseOverVertex();
	}

	@Override
	protected UndoAction buildHideComponentAction(List<? extends SelectableComponent> selectableComponents, EditabilityToggleHandler editabilityToggleHandler, Runnable refreshGUIRunnable) {
		List<IdObject> previousSelection = new ArrayList<>(selectionManager.getSelection());
		List<IdObject> possibleVerticesToTruncate = new ArrayList<>();

		for (SelectableComponent component : selectableComponents) {
			component.visit(new SelectableComponentVisitor() {
				@Override
				public void accept(Camera camera) {
				}

				@Override
				public void accept(IdObject node) {
					possibleVerticesToTruncate.add(node);
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
	public UndoAction mirror(byte dim, boolean flipModel, double centerX, double centerY, double centerZ) {
		throw new WrongModeException("Mirror has not yet been coded in Animation Editor");
	}

	@Override
	public UndoAction addVertex(double x, double y, double z, Vec3 preferredNormalFacingVector) {
		throw new WrongModeException("Unable to add vertices in Animation Editor");
	}

	@Override
	public CopiedModelData copySelection() {
		throw new WrongModeException("Unable to copy selection in animation editor");
	}

	@Override
	public UndoAction deleteSelectedComponents() {
		throw new WrongModeException("Unable to delete selection in animation editor");
	}

	@Override
	public UndoAction setMatrix(Collection<Bone> bones) {
		throw new WrongModeException("Unable to set Matrix in Animation Editor");
	}

	@Override
	public UndoAction setHDSkinning(Bone[] bones, short[] skinWeights) {
		throw new WrongModeException("Unable to set HD skinning bones in Animation Editor");
	}

	@Override
	public UndoAction snapNormals() {
		throw new WrongModeException("Unable to modify normals in Animation Editor");
	}

	@Override
	public UndoAction recalcNormals(double maxAngle, boolean useTries) {
		throw new WrongModeException("Unable to modify normals in Animation Editor");
	}

	@Override
	public UndoAction recalcExtents(boolean onlyIncludeEditableGeosets) {
		throw new WrongModeException("Unable to modify extents in Animation Editor");
	}

	@Override
	public GenericMoveAction addPlane(Vec2 p1, Vec2 p2, byte dim1, byte dim2, Vec3 facingVector,
	                                  int numberOfWidthSegments, int numberOfHeightSegments) {
		throw new WrongModeException("Unable to add plane in Animation Editor");

	}

	@Override
	public GenericMoveAction addBox(Vec2 p1, Vec2 p2, byte dim1, byte dim2, Vec3 facingVector,
	                                int numberOfLengthSegments, int numberOfWidthSegments, int numberOfHeightSegments) {
		throw new WrongModeException("Unable to add box in Animation Editor");
	}

	@Override
	public UndoAction flipSelectedFaces() {
		throw new WrongModeException("Unable to flip faces in Animation Editor");
	}

	@Override
	public UndoAction flipSelectedNormals() {
		throw new WrongModeException("Unable to flip normals in Animation Editor");
	}

	@Override
	public UndoAction snapSelectedVertices() {
		throw new WrongModeException("Unable to snap vertices in Animation Editor");
	}

	@Override
	public UndoAction snapSelectedNormals() {
		throw new WrongModeException("Unable to modify normals in Animation Editor");
	}

	@Override
	public UndoAction beginExtrudingSelection() {
		throw new WrongModeException("Unable to extrude in Animation Editor");
	}

	@Override
	public UndoAction beginExtendingSelection() {
		throw new WrongModeException("Unable to extrude in Animation Editor");
	}

	@Override
	public CloneAction cloneSelectedComponents(ClonedNodeNamePicker clonedNodeNamePicker) {
		throw new WrongModeException("Unable to clone components in Animation Editor");
	}

	@Override
	public void rawScale(double centerX, double centerY, double centerZ, double scaleX, double scaleY, double scaleZ) {
		throw new UnsupportedOperationException("Unable to scale directly in animation mode, use other system");
	}

	@Override
	public void rawScale(Vec3 center, Vec3 scale) {
		throw new UnsupportedOperationException("Unable to scale directly in animation mode, use other system");
	}

	public void rawScale(double centerX, double centerY, double centerZ, double scaleX, double scaleY, double scaleZ, Map<IdObject, Vec3> nodeToLocalScale) {
		for (IdObject idObject : selectionManager.getSelection()) {
			idObject.updateScalingKeyframe(renderModel, scaleX, scaleY, scaleZ, nodeToLocalScale.get(idObject));
		}
	}

	public void rawScale(Vec3 center, Vec3 scale, Map<IdObject, Vec3> nodeToLocalScale) {
		for (IdObject idObject : selectionManager.getSelection()) {
			idObject.updateScalingKeyframe(renderModel, scale, nodeToLocalScale.get(idObject));
		}
	}

	@Override
	public void rawRotate2d(double centerX, double centerY, double centerZ, double radians, byte firstXYZ, byte secondXYZ) {
		throw new UnsupportedOperationException("Unable to rotate directly in animation mode, use other system");
	}

	public void rawRotate2d(double centerX, double centerY, double centerZ, double radians, byte firstXYZ, byte secondXYZ, Map<IdObject, Quat> nodeToLocalRotation) {
		for (IdObject idObject : selectionManager.getSelection()) {
			idObject.updateRotationKeyframe(renderModel, centerX, centerY, centerZ, radians, firstXYZ, secondXYZ, nodeToLocalRotation.get(idObject));
		}
	}

	@Override
	public void rawTranslate(double x, double y, double z) {
		// throw new UnsupportedOperationException("Unable to translate directly in animation mode, use other system");
		for (IdObject idObject : selectionManager.getSelection()) {
			idObject.updateTranslationKeyframe(renderModel, x, y, z, new Vec3());
		}
	}

	public void rawTranslate(double x, double y, double z, Map<IdObject, Vec3> nodeToLocalTranslation) {
		for (IdObject idObject : selectionManager.getSelection()) {
			idObject.updateTranslationKeyframe(renderModel, x, y, z, nodeToLocalTranslation.get(idObject));
		}
	}

	public void rawSquatToolRotate2d(double centerX, double centerY, double centerZ, double radians, byte firstXYZ, byte secondXYZ, Map<IdObject, Quat> nodeToLocalRotation) {
		for (IdObject idObject : selectionManager.getSelection()) {
			idObject.updateRotationKeyframe(renderModel, centerX, centerY, centerZ, radians, firstXYZ, secondXYZ, nodeToLocalRotation.get(idObject));
		}
		for (IdObject idObject : model.getModel().getIdObjects()) {
			if (selectionManager.getSelection().contains(idObject.getParent()) && (((idObject.getClass() == Bone.class) && (idObject.getParent().getClass() == Bone.class)) || ((idObject.getClass() == Helper.class) && (idObject.getParent().getClass() == Helper.class)))) {
				idObject.updateRotationKeyframe(renderModel, centerX, centerY, centerZ, -radians, firstXYZ, secondXYZ, nodeToLocalRotation.get(idObject));
			}
		}
	}

	@Override
	public UndoAction rotate(Vec3 center, double rotateX, double rotateY, double rotateZ) {

		GenericRotateAction rotationX = beginRotation(center.x, center.y, center.z, (byte) 2, (byte) 1);
		rotationX.updateRotation(rotateX);
		GenericRotateAction rotationY = beginRotation(center.x, center.y, center.z, (byte) 0, (byte) 2);
		rotationY.updateRotation(rotateY);
		GenericRotateAction rotationZ = beginRotation(center.x, center.y, center.z, (byte) 1, (byte) 0);
		rotationZ.updateRotation(rotateZ);
		CompoundAction compoundAction = new CompoundAction("rotate", Arrays.asList(rotationX, rotationY, rotationZ));
		compoundAction.redo();
		return compoundAction;
	}

	@Override
	public UndoAction rotate(Vec3 center, Vec3 rotate) {

		GenericRotateAction rotationX = beginRotation(center.x, center.y, center.z, (byte) 2, (byte) 1);
		rotationX.updateRotation(rotate.x);
		GenericRotateAction rotationY = beginRotation(center.x, center.y, center.z, (byte) 0, (byte) 2);
		rotationY.updateRotation(rotate.y);
		GenericRotateAction rotationZ = beginRotation(center.x, center.y, center.z, (byte) 1, (byte) 0);
		rotationZ.updateRotation(rotate.z);
		CompoundAction compoundAction = new CompoundAction("rotate", Arrays.asList(rotationX, rotationY, rotationZ));
		compoundAction.redo();
		return compoundAction;
	}

	@Override
	public GenericMoveAction beginTranslation() {
		Set<IdObject> selection = selectionManager.getSelection();
		List<UndoAction> actions = new ArrayList<>();
		// TODO fix cast, meta knowledge: NodeAnimationModelEditor will only be constructed from a TimeEnvironmentImpl render environment, and never from the anim previewer impl
		TimeEnvironmentImpl timeEnvironmentImpl = (TimeEnvironmentImpl) renderModel.getAnimatedRenderEnvironment();

		generateKeyframes(selection, actions, timeEnvironmentImpl, "Translation", (node, translationTimeline) -> node.createTranslationKeyframe(renderModel, (Vec3AnimFlag) translationTimeline, structureChangeListener));

		int trackTime = renderModel.getAnimatedRenderEnvironment().getAnimationTime() + renderModel.getAnimatedRenderEnvironment().getCurrentAnimation().getStart();
		int trackTimeToUse = timeEnvironmentImpl.getGlobalSeq() == null ? trackTime : timeEnvironmentImpl.getGlobalSeqTime(timeEnvironmentImpl.getGlobalSeq());
		return new TranslationKeyframeAction(new CompoundAction("setup", actions), trackTimeToUse, timeEnvironmentImpl.getGlobalSeq(), selection, this);
	}

	@Override
	public void rawRotate3d(Vec3 center, Vec3 axis, double radians) {
		throw new UnsupportedOperationException("NYI");
	}

	@Override
	public UndoAction translate(double x, double y, double z) {
		Vec3 delta = new Vec3(x, y, z);
		StaticMeshMoveAction moveAction = new StaticMeshMoveAction(this, delta);
		moveAction.redo();
		return moveAction;
	}

	@Override
	public UndoAction translate(Vec3 v) {
		Vec3 delta = new Vec3(v);
		StaticMeshMoveAction moveAction = new StaticMeshMoveAction(this, delta);
		moveAction.redo();
		return moveAction;
	}

	@Override
	public UndoAction setPosition(Vec3 center, double x, double y, double z) {
		Vec3 delta = new Vec3(x - center.x, y - center.y, z - center.z);
		StaticMeshMoveAction moveAction = new StaticMeshMoveAction(this, delta);
		moveAction.redo();
		return moveAction;
	}

	@Override
	public UndoAction setPosition(Vec3 center, Vec3 v) {
		Vec3 delta = Vec3.getDiff(v, center);
		StaticMeshMoveAction moveAction = new StaticMeshMoveAction(this, delta);
		moveAction.redo();
		return moveAction;
	}

	@Override
	public GenericRotateAction beginRotation(double centerX, double centerY, double centerZ, byte firstXYZ, byte secondXYZ) {
		Set<IdObject> selection = selectionManager.getSelection();
		List<UndoAction> actions = new ArrayList<>();

		TimeEnvironmentImpl timeEnvironmentImpl = (TimeEnvironmentImpl) renderModel.getAnimatedRenderEnvironment();

		generateKeyframes(selection, actions, timeEnvironmentImpl, "Rotation", (node, translationTimeline) -> node.createRotationKeyframe(renderModel, (QuatAnimFlag) translationTimeline, structureChangeListener));

		int trackTime = renderModel.getAnimatedRenderEnvironment().getAnimationTime() + renderModel.getAnimatedRenderEnvironment().getCurrentAnimation().getStart();
		int trackTimeToUse = timeEnvironmentImpl.getGlobalSeq() == null ? trackTime : timeEnvironmentImpl.getGlobalSeqTime(timeEnvironmentImpl.getGlobalSeq());
		return new RotationKeyframeAction(new CompoundAction("setup", actions), trackTimeToUse, timeEnvironmentImpl.getGlobalSeq(), selection, this, centerX, centerY, centerZ, firstXYZ, secondXYZ);
	}

	@Override
	public Vec3 getSelectionCenter() {
		return selectionManager.getCenter();
	}

	@Override
	public boolean editorWantsAnimation() {
		return true;
	}

	private void generateKeyframes(Set<IdObject> selection, List<UndoAction> actions, TimeEnvironmentImpl timeEnvironmentImpl, String name, BiFunction<IdObject, AnimFlag<?>, AddKeyframeAction> keyframeFunction) {
		for (IdObject node : selection) {
			AnimFlag<?> transformationTimeline = node.find(name, timeEnvironmentImpl.getGlobalSeq());

			if (transformationTimeline == null) {
				if (name.equals("Rotation")) {
					transformationTimeline = QuatAnimFlag.createEmpty2018(name, InterpolationType.HERMITE, timeEnvironmentImpl.getGlobalSeq());
				} else {
					transformationTimeline = Vec3AnimFlag.createEmpty2018(name, InterpolationType.HERMITE, timeEnvironmentImpl.getGlobalSeq());
				}
//				transformationTimeline = AnimFlag.createEmpty2018(name, InterpolationType.HERMITE, timeEnvironmentImpl.getGlobalSeq());
				node.add(transformationTimeline);

				AddTimelineAction addTimelineAction = new AddTimelineAction(node, transformationTimeline, structureChangeListener);
				structureChangeListener.timelineAdded(node, transformationTimeline);
				actions.add(addTimelineAction);
			}
//			AddKeyframeAction keyframeAction = node.createRotationKeyframe(renderModel, transformationTimeline, structureChangeListener);
			AddKeyframeAction keyframeAction = keyframeFunction.apply(node, transformationTimeline);
			if (keyframeAction != null) {
				actions.add(keyframeAction);
			}
		}
	}

	@Override
	public GenericScaleAction beginScaling(double centerX, double centerY, double centerZ) {
		Set<IdObject> selection = selectionManager.getSelection();
		List<UndoAction> actions = new ArrayList<>();
		TimeEnvironmentImpl timeEnvironmentImpl = (TimeEnvironmentImpl) renderModel.getAnimatedRenderEnvironment();

		generateKeyframes(selection, actions, timeEnvironmentImpl, "Scaling", (node, translationTimeline) -> node.createScalingKeyframe(renderModel, (Vec3AnimFlag) translationTimeline, structureChangeListener));


		int trackTime = renderModel.getAnimatedRenderEnvironment().getAnimationTime() + renderModel.getAnimatedRenderEnvironment().getCurrentAnimation().getStart();
		int trackTimeToUse = timeEnvironmentImpl.getGlobalSeq() == null ? trackTime : timeEnvironmentImpl.getGlobalSeqTime(timeEnvironmentImpl.getGlobalSeq());
		return new ScalingKeyframeAction(new CompoundAction("setup", actions), trackTimeToUse, timeEnvironmentImpl.getGlobalSeq(), selection, this, centerX, centerY, centerZ);
	}

	@Override
	public GenericScaleAction beginScaling(Vec3 center) {
		Set<IdObject> selection = selectionManager.getSelection();
		List<UndoAction> actions = new ArrayList<>();
		TimeEnvironmentImpl timeEnvironmentImpl = (TimeEnvironmentImpl) renderModel.getAnimatedRenderEnvironment();

		generateKeyframes(selection, actions, timeEnvironmentImpl, "Scaling", (node, translationTimeline) -> node.createScalingKeyframe(renderModel, (Vec3AnimFlag) translationTimeline, structureChangeListener));


		int trackTime = renderModel.getAnimatedRenderEnvironment().getAnimationTime() + renderModel.getAnimatedRenderEnvironment().getCurrentAnimation().getStart();
		int trackTimeToUse = timeEnvironmentImpl.getGlobalSeq() == null ? trackTime : timeEnvironmentImpl.getGlobalSeqTime(timeEnvironmentImpl.getGlobalSeq());
		return new ScalingKeyframeAction(new CompoundAction("setup", actions), trackTimeToUse, timeEnvironmentImpl.getGlobalSeq(), selection, this, center);
	}

	@Override
	public UndoAction createKeyframe(ModelEditorActionType actionType) {
		String keyframeMdlTypeName = switch (actionType) {
			case ROTATION -> "Rotation";
			case SCALING -> "Scaling";
			case TRANSLATION -> "Translation";
		};

		Set<IdObject> selection = selectionManager.getSelection();
		List<UndoAction> actions = new ArrayList<>();

		TimeEnvironmentImpl timeEnvironmentImpl = (TimeEnvironmentImpl) renderModel.getAnimatedRenderEnvironment();
		for (IdObject node : selection) {
			AnimFlag<?> transformationTimeline = node.find(keyframeMdlTypeName, timeEnvironmentImpl.getGlobalSeq());

			if (transformationTimeline == null) {
				if (keyframeMdlTypeName.equals("Rotation")) {
					transformationTimeline = QuatAnimFlag.createEmpty2018(keyframeMdlTypeName, InterpolationType.HERMITE, timeEnvironmentImpl.getGlobalSeq());
				} else {
					transformationTimeline = Vec3AnimFlag.createEmpty2018(keyframeMdlTypeName, InterpolationType.HERMITE, timeEnvironmentImpl.getGlobalSeq());
				}
				node.add(transformationTimeline);

				AddTimelineAction addTimelineAction = new AddTimelineAction(node, transformationTimeline, structureChangeListener);
				structureChangeListener.timelineAdded(node, transformationTimeline);

				actions.add(addTimelineAction);
			}
			AddKeyframeAction keyframeAction = switch (actionType) {
				case ROTATION -> node.createRotationKeyframe(renderModel, (QuatAnimFlag) transformationTimeline, structureChangeListener);
				case SCALING -> node.createScalingKeyframe(renderModel, (Vec3AnimFlag) transformationTimeline, structureChangeListener);
				case TRANSLATION -> node.createTranslationKeyframe(renderModel, (Vec3AnimFlag) transformationTimeline, structureChangeListener);
			};
			if (keyframeAction != null) {
				actions.add(keyframeAction);
			}
		}

		return new CompoundAction("create keyframe", actions);

	}

	@Override
	public GenericRotateAction beginSquatTool(double centerX, double centerY, double centerZ, byte firstXYZ, byte secondXYZ) {
		Set<IdObject> selection = new HashSet<>(selectionManager.getSelection());

		for (IdObject idObject : model.getModel().getIdObjects()) {
			if (selectionManager.getSelection().contains(idObject.getParent()) && (((idObject.getClass() == Bone.class) && (idObject.getParent().getClass() == Bone.class)) || ((idObject.getClass() == Helper.class) && (idObject.getParent().getClass() == Helper.class)))) {
				selection.add(idObject);
			}
		}
		List<UndoAction> actions = new ArrayList<>();
		TimeEnvironmentImpl timeEnvironmentImpl = (TimeEnvironmentImpl) renderModel.getAnimatedRenderEnvironment();

		for (IdObject node : selection) {
			QuatAnimFlag translationTimeline = (QuatAnimFlag) node.find("Rotation", timeEnvironmentImpl.getGlobalSeq());
			if (translationTimeline == null) {
				translationTimeline = QuatAnimFlag.createEmpty2018("Rotation", InterpolationType.HERMITE, timeEnvironmentImpl.getGlobalSeq());
				node.add(translationTimeline);

				AddTimelineAction addTimelineAction = new AddTimelineAction(node, translationTimeline, structureChangeListener);
				structureChangeListener.timelineAdded(node, translationTimeline);
				actions.add(addTimelineAction);
			}

			AddKeyframeAction keyframeAction = node.createRotationKeyframe(renderModel, translationTimeline, structureChangeListener);
			if (keyframeAction != null) {
				actions.add(keyframeAction);
			}
		}

		int trackTime = renderModel.getAnimatedRenderEnvironment().getAnimationTime() + renderModel.getAnimatedRenderEnvironment().getCurrentAnimation().getStart();
		int trackTimeToUse = timeEnvironmentImpl.getGlobalSeq() == null ? trackTime : timeEnvironmentImpl.getGlobalSeqTime(timeEnvironmentImpl.getGlobalSeq());
		return new SquatToolKeyframeAction(new CompoundAction("setup", actions), trackTimeToUse, timeEnvironmentImpl.getGlobalSeq(), selection, this, centerX, centerY, centerZ, firstXYZ, secondXYZ);
	}

	@Override
	public UndoAction createFaceFromSelection(Vec3 preferredFacingVector) {
		throw new WrongModeException("Unable to create face in animation editor");
	}

	@Override
	public String getSelectedMatricesDescription() {
		throw new WrongModeException("Unable to investigate mesh in Animation Editor");
	}

	@Override
	public String getSelectedHDSkinningDescription() {
		throw new WrongModeException("Unable to investigate mesh in Animation Editor");
	}

	@Override
	public UndoAction setParent(IdObject node) {
		throw new WrongModeException("Can't set parent in Animation Editor");
	}

	@Override
	public UndoAction splitGeoset() {
		throw new WrongModeException("Unable to split geoset in animation editor");
	}

	@Override
	public UndoAction addBone(double x, double y, double z) {
		throw new WrongModeException("Unable to add bone in Animation Editor");
	}

	private final class SelectionAtPointTester implements IdObjectVisitor {
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
			Mat4 worldMatrix = renderModel.getRenderNode(node).getWorldMatrix();
			if (hitTest(node.getPivotPoint(), CoordinateSystem.Util.geom(axes, point), axes, node.getClickRadius(axes) * CoordinateSystem.Util.getZoom(axes) * 2, worldMatrix)) {
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
			Mat4 worldMatrix = renderModel.getRenderNode(node).getWorldMatrix();
			if (hitTest(node.getPivotPoint(), CoordinateSystem.Util.geom(axes, point), axes, node.getClickRadius(axes) * CoordinateSystem.Util.getZoom(axes), worldMatrix)) {
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
		}

		@Override
		public void camera(Camera camera) {
			System.err.println("CAMERA processed in NodeAnimationModelEditor!!!");
			// if (hitTest(camera.getPosition(), CoordinateSystem.Util.geom(axes, point),
			// axes,
			// programPreferences.getVertexSize(), worldMatrix)) {
			// mouseOverVertex = true;
			// }
			// if (hitTest(camera.getTargetPosition(), CoordinateSystem.Util.geom(axes,
			// point), axes,
			// programPreferences.getVertexSize(), worldMatrix)) {
			// mouseOverVertex = true;
			// }
		}

		@Override
		public void bone(Bone node) {
			Mat4 worldMatrix = renderModel.getRenderNode(node).getWorldMatrix();
			if (hitTest(node.getPivotPoint(), CoordinateSystem.Util.geom(axes, point), axes, node.getClickRadius(axes) * CoordinateSystem.Util.getZoom(axes), worldMatrix)) {
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

	@Override
	public RigAction rig() {
		throw new WrongModeException("Unable to rig in Animation Editor");
	}

	private final class GenericSelectorVisitor implements IdObjectVisitor {
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
		public void ribbonEmitter(RibbonEmitter particleEmitter) {
			hitTest(selectedItems, area, particleEmitter.getPivotPoint(), coordinateSystem, particleEmitter.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem) * 2, particleEmitter, renderModel);
		}

		@Override
		public void particleEmitter2(ParticleEmitter2 particleEmitter) {
			hitTest(selectedItems, area, particleEmitter.getPivotPoint(), coordinateSystem, particleEmitter.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem) * 2, particleEmitter, renderModel);
		}

		@Override
		public void particleEmitter(ParticleEmitter particleEmitter) {
			hitTest(selectedItems, area, particleEmitter.getPivotPoint(), coordinateSystem, particleEmitter.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem) * 2, particleEmitter, renderModel);
		}

		@Override
		public void popcornFxEmitter(ParticleEmitterPopcorn popcornFxEmitter) {
			hitTest(selectedItems, area, popcornFxEmitter.getPivotPoint(), coordinateSystem, popcornFxEmitter.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem) * 2, popcornFxEmitter, renderModel);
		}

		@Override
		public void light(Light light) {
			hitTest(selectedItems, area, light.getPivotPoint(), coordinateSystem, light.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem) * 2, light, renderModel);
		}

		@Override
		public void helper(Helper object) {
			hitTest(selectedItems, area, object.getPivotPoint(), coordinateSystem, object.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem), object, renderModel);
		}

		@Override
		public void eventObject(EventObject eventObject) {
			hitTest(selectedItems, area, eventObject.getPivotPoint(), coordinateSystem, eventObject.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem) * 2, eventObject, renderModel);
		}

		@Override
		public void collisionShape(CollisionShape collisionShape) {
			hitTest(selectedItems, area, collisionShape.getPivotPoint(), coordinateSystem, collisionShape.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem) * 2, collisionShape, renderModel);
		}

		@Override
		public void camera(Camera camera) {
			System.err.println("Attempted to process camera with Node Animation Editor generic selector!!!");
		}

		@Override
		public void bone(Bone object) {
			hitTest(selectedItems, area, object.getPivotPoint(), coordinateSystem, object.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem), object, renderModel);
		}

		@Override
		public void attachment(Attachment attachment) {
			hitTest(selectedItems, area, attachment.getPivotPoint(), coordinateSystem, attachment.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem) * 2, attachment, renderModel);
		}

	}

}
