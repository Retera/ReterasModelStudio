package com.hiveworkshop.rms.ui.application.edit.animation;

import com.hiveworkshop.rms.editor.model.EventObject;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
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
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;

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

	public NodeAnimationModelEditor(final ModelView model, final ProgramPreferences programPreferences, final SelectionManager<IdObject> selectionManager, final RenderModel renderModel, final ModelStructureChangeListener structureChangeListener) {
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

	@Override
	public UndoAction setSelectedBoneName(final String name) {
		throw new WrongModeException("Unable to change bone names in Animation Editor");
	}

	@Override
	public UndoAction addSelectedBoneSuffix(final String name) {
		throw new WrongModeException("Unable to change bone names in Animation Editor");
	}

	@Override
	public UndoAction addTeamColor() {
		throw new WrongModeException("Unable to add team color in Animation Editor");
	}

	@Override
	public void selectByVertices(final Collection<? extends Vec3> newSelection) {
		final Set<IdObject> newlySelectedObjects = new HashSet<>();
		for (final IdObject object : model.getEditableIdObjects()) {
			if (newSelection.contains(object.getPivotPoint())) {
				newlySelectedObjects.add(object);
			}
			object.apply(new IdObjectVisitor() {
				@Override
				public void ribbonEmitter(final RibbonEmitter particleEmitter) {}

				@Override
				public void particleEmitter2(final ParticleEmitter2 particleEmitter) {}

				@Override
				public void particleEmitter(final ParticleEmitter particleEmitter) {}

				@Override
				public void popcornFxEmitter(final ParticleEmitterPopcorn popcornFxEmitter) {}

				@Override
				public void light(final Light light) {}

				@Override
				public void helper(final Helper object) {}

				@Override
				public void eventObject(final EventObject eventObject) {}

				@Override
				public void collisionShape(final CollisionShape collisionShape) {
					for (final Vec3 vertex : collisionShape.getVertices()) {
						if (newSelection.contains(vertex)) {
							newlySelectedObjects.add(collisionShape);
						}
					}
				}

				@Override
				public void camera(final Camera camera) {}

				@Override
				public void bone(final Bone object) {}

				@Override
				public void attachment(final Attachment attachment) {}
			});
		}
		// TODO cameras in a second CameraAnimationEditor
		selectionManager.setSelection(newlySelectedObjects);
	}

	@Override
	public UndoAction expandSelection() {
		throw new WrongModeException("Unable to expand selection in Node Animation Editor");
	}

	@Override
	public UndoAction invertSelection() {
		final List<IdObject> oldSelection = new ArrayList<>(selectionManager.getSelection());
		final Set<IdObject> invertedSelection = new HashSet<>(selectionManager.getSelection());
		for (final IdObject node : model.getEditableIdObjects()) {
			toggleSelection(invertedSelection, node);
		}
		selectionManager.setSelection(invertedSelection);
		return new SetSelectionAction<>(invertedSelection, oldSelection, selectionManager, "invert selection");
	}

	private void toggleSelection(final Set<IdObject> selection, final IdObject position) {
		if (selection.contains(position)) {
			selection.remove(position);
		} else {
			selection.add(position);
		}
	}

	@Override
	public UndoAction selectAll() {
		final List<IdObject> oldSelection = new ArrayList<>(selectionManager.getSelection());
		final Set<IdObject> allSelection = new HashSet<>(model.getEditableIdObjects());
		selectionManager.setSelection(allSelection);
		return new SetSelectionAction<>(allSelection, oldSelection, selectionManager, "select all");
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
		final Rectangle2D area = new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
		final IdObjectVisitor visitor = genericSelectorVisitor.reset(selectedItems, area, coordinateSystem);
		for (final IdObject object : model.getEditableIdObjects()) {
			object.apply(visitor);
		}
		return selectedItems;
	}

	@Override
	public boolean canSelectAt(final Point point, final CoordinateSystem axes) {
		final IdObjectVisitor visitor = selectionAtPointTester.reset(axes, point);
		for (final IdObject object : model.getEditableIdObjects()) {
			object.apply(visitor);
		}
		return selectionAtPointTester.isMouseOverVertex();
	}

	private static final Vec4 pivotHeap = new Vec4();

	public static void hitTest(final List<IdObject> selectedItems, final Rectangle2D area, final Vec3 geosetVertex, final CoordinateSystem coordinateSystem, final double vertexSize, final IdObject object, final RenderModel renderModel) {
		final RenderNode renderNode = renderModel.getRenderNode(object);
		Vec4 pivotHeap = new Vec4(geosetVertex, 1);
		pivotHeap.transform(renderNode.getWorldMatrix());
		final byte dim1 = coordinateSystem.getPortFirstXYZ();
		final byte dim2 = coordinateSystem.getPortSecondXYZ();
		final double minX = coordinateSystem.convertX(area.getMinX());
		final double minY = coordinateSystem.convertY(area.getMinY());
		final double maxX = coordinateSystem.convertX(area.getMaxX());
		final double maxY = coordinateSystem.convertY(area.getMaxY());
		final double vertexX = pivotHeap.getCoord(dim1);
		final double x = coordinateSystem.convertX(vertexX);
		final double vertexY = pivotHeap.getCoord(dim2);
		final double y = coordinateSystem.convertY(vertexY);
		if ((distance(x, y, minX, minY) <= (vertexSize / 2.0)) || (distance(x, y, maxX, maxY) <= (vertexSize / 2.0)) || area.contains(vertexX, vertexY)) {
			selectedItems.add(object);
		}
	}

	public static boolean hitTest(final Vec3 vertex, final Point2D point, final CoordinateSystem coordinateSystem, final double vertexSize, final Mat4 worldMatrix) {
		Vec4 pivotHeap = new Vec4(vertex, 1);
		pivotHeap.transform(worldMatrix);
		final double x = coordinateSystem.convertX(pivotHeap.getCoord(coordinateSystem.getPortFirstXYZ()));
		final double y = coordinateSystem.convertY(pivotHeap.getCoord(coordinateSystem.getPortSecondXYZ()));
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
	protected UndoAction buildHideComponentAction(final List<? extends SelectableComponent> selectableComponents, final EditabilityToggleHandler editabilityToggleHandler, final Runnable refreshGUIRunnable) {
		final List<IdObject> previousSelection = new ArrayList<>(selectionManager.getSelection());
		final List<IdObject> possibleVerticesToTruncate = new ArrayList<>();
		for (final SelectableComponent component : selectableComponents) {
			component.visit(new SelectableComponentVisitor() {
				@Override
				public void accept(final Camera camera) {
				}

				@Override
				public void accept(final IdObject node) {
					possibleVerticesToTruncate.add(node);
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
	public UndoAction mirror(final byte dim, final boolean flipModel, final double centerX, final double centerY, final double centerZ) {
		throw new WrongModeException("Mirror has not yet been coded in Animation Editor");
	}

	@Override
	public UndoAction addVertex(final double x, final double y, final double z, final Vec3 preferredNormalFacingVector) {
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
	public UndoAction setMatrix(final Collection<Bone> bones) {
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
	public UndoAction recalcNormals() {
		throw new WrongModeException("Unable to modify normals in Animation Editor");
	}

	@Override
	public UndoAction recalcExtents(final boolean onlyIncludeEditableGeosets) {
		throw new WrongModeException("Unable to modify extents in Animation Editor");
	}

	@Override
	public GenericMoveAction addPlane(final double x, final double y, final double x2, final double y2, final byte dim1, final byte dim2, final Vec3 facingVector, final int numberOfWidthSegments, final int numberOfHeightSegments) {
		throw new WrongModeException("Unable to add plane in Animation Editor");
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
	public CloneAction cloneSelectedComponents(final ClonedNodeNamePicker clonedNodeNamePicker) {
		throw new WrongModeException("Unable to clone components in Animation Editor");
	}

	@Override
	public void rawScale(final double centerX, final double centerY, final double centerZ, final double scaleX, final double scaleY, final double scaleZ) {
		throw new UnsupportedOperationException("Unable to scale directly in animation mode, use other system");
	}

	public void rawScale(final double centerX, final double centerY, final double centerZ, final double scaleX, final double scaleY, final double scaleZ, final Map<IdObject, Vec3> nodeToLocalScale) {
		for (final IdObject idObject : selectionManager.getSelection()) {
			idObject.updateScalingKeyframe(renderModel, scaleX, scaleY, scaleZ, nodeToLocalScale.get(idObject));
		}
	}

	@Override
	public void rawRotate2d(final double centerX, final double centerY, final double centerZ, final double radians, final byte firstXYZ, final byte secondXYZ) {
		throw new UnsupportedOperationException("Unable to rotate directly in animation mode, use other system");
	}

	public void rawRotate2d(final double centerX, final double centerY, final double centerZ, final double radians, final byte firstXYZ, final byte secondXYZ, final Map<IdObject, Quat> nodeToLocalRotation) {
		for (final IdObject idObject : selectionManager.getSelection()) {
			idObject.updateRotationKeyframe(renderModel, centerX, centerY, centerZ, radians, firstXYZ, secondXYZ, nodeToLocalRotation.get(idObject));
		}
	}

	@Override
	public void rawTranslate(final double x, final double y, final double z) {
		// throw new UnsupportedOperationException("Unable to translate directly in animation mode, use other system");
		for (final IdObject idObject : selectionManager.getSelection()) {
			idObject.updateTranslationKeyframe(renderModel, x, y, z, new Vec3());
		}
	}

	public void rawTranslate(final double x, final double y, final double z,
	                         final Map<IdObject, Vec3> nodeToLocalTranslation) {
		for (final IdObject idObject : selectionManager.getSelection()) {
			idObject.updateTranslationKeyframe(renderModel, x, y, z, nodeToLocalTranslation.get(idObject));
		}
	}

	public void rawSquatToolRotate2d(final double centerX, final double centerY, final double centerZ, final double radians, final byte firstXYZ, final byte secondXYZ, final Map<IdObject, Quat> nodeToLocalRotation) {
		for (final IdObject idObject : selectionManager.getSelection()) {
			idObject.updateRotationKeyframe(renderModel, centerX, centerY, centerZ, radians, firstXYZ, secondXYZ, nodeToLocalRotation.get(idObject));
		}
		for (final IdObject idObject : model.getModel().getIdObjects()) {
			if (selectionManager.getSelection().contains(idObject.getParent()) && (((idObject.getClass() == Bone.class) && (idObject.getParent().getClass() == Bone.class)) || ((idObject.getClass() == Helper.class) && (idObject.getParent().getClass() == Helper.class)))) {
				idObject.updateRotationKeyframe(renderModel, centerX, centerY, centerZ, -radians, firstXYZ, secondXYZ, nodeToLocalRotation.get(idObject));
			}
		}
	}

	@Override
	public UndoAction rotate(final Vec3 center, final double rotateX, final double rotateY, final double rotateZ) {

		final GenericRotateAction rotationX = beginRotation(center.x, center.y, center.z, (byte) 2, (byte) 1);
		rotationX.updateRotation(rotateX);
		final GenericRotateAction rotationY = beginRotation(center.x, center.y, center.z, (byte) 0, (byte) 2);
		rotationY.updateRotation(rotateY);
		final GenericRotateAction rotationZ = beginRotation(center.x, center.y, center.z, (byte) 1, (byte) 0);
		rotationZ.updateRotation(rotateZ);
		final CompoundAction compoundAction = new CompoundAction("rotate", Arrays.asList(rotationX, rotationY, rotationZ));
		compoundAction.redo();
		return compoundAction;
	}

	@Override
	public GenericMoveAction beginTranslation() {
		final Set<IdObject> selection = selectionManager.getSelection();
		final List<UndoAction> actions = new ArrayList<>();
		// TODO fix cast, meta knowledge: NodeAnimationModelEditor will only be constructed from a TimeEnvironmentImpl render environment, and never from the anim previewer impl
		final TimeEnvironmentImpl timeEnvironmentImpl = (TimeEnvironmentImpl) renderModel.getAnimatedRenderEnvironment();

		generateKeyframes(selection, actions, timeEnvironmentImpl, "Translation", (node, translationTimeline) -> node.createTranslationKeyframe(renderModel, translationTimeline, structureChangeListener));

		final int trackTime = renderModel.getAnimatedRenderEnvironment().getAnimationTime() + renderModel.getAnimatedRenderEnvironment().getCurrentAnimation().getStart();
		final int trackTimeToUse = timeEnvironmentImpl.getGlobalSeq() == null ? trackTime : timeEnvironmentImpl.getGlobalSeqTime(timeEnvironmentImpl.getGlobalSeq());
		return new TranslationKeyframeAction(new CompoundAction("setup", actions), trackTimeToUse, timeEnvironmentImpl.getGlobalSeq(), selection, this);
	}

	@Override
	public void rawRotate3d(final Vec3 center, final Vec3 axis, final double radians) {
		throw new UnsupportedOperationException("NYI");
	}

	@Override
	public UndoAction translate(final double x, final double y, final double z) {
		final Vec3 delta = new Vec3(x, y, z);
		final StaticMeshMoveAction moveAction = new StaticMeshMoveAction(this, delta);
		moveAction.redo();
		return moveAction;
	}

	@Override
	public UndoAction setPosition(final Vec3 center, final double x, final double y, final double z) {
		final Vec3 delta = new Vec3(x - center.x, y - center.y, z - center.z);
		final StaticMeshMoveAction moveAction = new StaticMeshMoveAction(this, delta);
		moveAction.redo();
		return moveAction;
	}

	@Override
	public GenericRotateAction beginRotation(final double centerX, final double centerY, final double centerZ, final byte firstXYZ, final byte secondXYZ) {
		final Set<IdObject> selection = selectionManager.getSelection();
		final List<UndoAction> actions = new ArrayList<>();

		final TimeEnvironmentImpl timeEnvironmentImpl = (TimeEnvironmentImpl) renderModel.getAnimatedRenderEnvironment();

		generateKeyframes(selection, actions, timeEnvironmentImpl, "Rotation", (node, translationTimeline) -> node.createRotationKeyframe(renderModel, translationTimeline, structureChangeListener));

		final int trackTime = renderModel.getAnimatedRenderEnvironment().getAnimationTime() + renderModel.getAnimatedRenderEnvironment().getCurrentAnimation().getStart();
		final int trackTimeToUse = timeEnvironmentImpl.getGlobalSeq() == null ? trackTime : timeEnvironmentImpl.getGlobalSeqTime(timeEnvironmentImpl.getGlobalSeq());
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

	private void generateKeyframes(Set<IdObject> selection, List<UndoAction> actions, TimeEnvironmentImpl timeEnvironmentImpl, String name, BiFunction<IdObject, AnimFlag, AddKeyframeAction> keyframeFunction) {
		for (final IdObject node : selection) {
			AnimFlag translationTimeline = node.find(name, timeEnvironmentImpl.getGlobalSeq());

			if (translationTimeline == null) {
				translationTimeline = AnimFlag.createEmpty2018(name, InterpolationType.HERMITE, timeEnvironmentImpl.getGlobalSeq());
				node.add(translationTimeline);

				final AddTimelineAction addTimelineAction = new AddTimelineAction(node, translationTimeline, structureChangeListener);
				structureChangeListener.timelineAdded(node, translationTimeline);
				actions.add(addTimelineAction);
			}
//			final AddKeyframeAction keyframeAction = node.createRotationKeyframe(renderModel, translationTimeline, structureChangeListener);
			final AddKeyframeAction keyframeAction = keyframeFunction.apply(node, translationTimeline);
			if (keyframeAction != null) {
				actions.add(keyframeAction);
			}
		}
	}

	@Override
	public GenericScaleAction beginScaling(final double centerX, final double centerY, final double centerZ) {
		final Set<IdObject> selection = selectionManager.getSelection();
		final List<UndoAction> actions = new ArrayList<>();
		final TimeEnvironmentImpl timeEnvironmentImpl = (TimeEnvironmentImpl) renderModel.getAnimatedRenderEnvironment();

		generateKeyframes(selection, actions, timeEnvironmentImpl, "Scaling", (node, translationTimeline) -> node.createScalingKeyframe(renderModel, translationTimeline, structureChangeListener));


		final int trackTime = renderModel.getAnimatedRenderEnvironment().getAnimationTime() + renderModel.getAnimatedRenderEnvironment().getCurrentAnimation().getStart();
		final int trackTimeToUse = timeEnvironmentImpl.getGlobalSeq() == null ? trackTime : timeEnvironmentImpl.getGlobalSeqTime(timeEnvironmentImpl.getGlobalSeq());
		return new ScalingKeyframeAction(new CompoundAction("setup", actions), trackTimeToUse, timeEnvironmentImpl.getGlobalSeq(), selection, this, centerX, centerY, centerZ);
	}

	@Override
	public UndoAction createKeyframe(final ModelEditorActionType actionType) {
		final String keyframeMdlTypeName = switch (actionType) {
			case ROTATION -> "Rotation";
			case SCALING -> "Scaling";
			case TRANSLATION -> "Translation";
		};

		final Set<IdObject> selection = selectionManager.getSelection();
		final List<UndoAction> actions = new ArrayList<>();

		final TimeEnvironmentImpl timeEnvironmentImpl = (TimeEnvironmentImpl) renderModel.getAnimatedRenderEnvironment();
		for (final IdObject node : selection) {
			AnimFlag translationTimeline = node.find(keyframeMdlTypeName, timeEnvironmentImpl.getGlobalSeq());

			if (translationTimeline == null) {
				translationTimeline = AnimFlag.createEmpty2018(keyframeMdlTypeName, InterpolationType.HERMITE, timeEnvironmentImpl.getGlobalSeq());
				node.add(translationTimeline);

				final AddTimelineAction addTimelineAction = new AddTimelineAction(node, translationTimeline, structureChangeListener);
				structureChangeListener.timelineAdded(node, translationTimeline);

				actions.add(addTimelineAction);
			}
			final AddKeyframeAction keyframeAction = switch (actionType) {
				case ROTATION -> node.createRotationKeyframe(renderModel, translationTimeline, structureChangeListener);
				case SCALING -> node.createScalingKeyframe(renderModel, translationTimeline, structureChangeListener);
				case TRANSLATION -> node.createTranslationKeyframe(renderModel, translationTimeline, structureChangeListener);
			};
			if (keyframeAction != null) {
				actions.add(keyframeAction);
			}
		}

		return new CompoundAction("create keyframe", actions);

	}

	@Override
	public GenericMoveAction addBox(final double x, final double y, final double x2, final double y2, final byte dim1, final byte dim2, final Vec3 facingVector, final int numberOfLengthSegments, final int numberOfWidthSegments, final int numberOfHeightSegments) {
		throw new WrongModeException("Unable to create box in animation editor");
	}

	@Override
	public GenericRotateAction beginSquatTool(final double centerX, final double centerY, final double centerZ, final byte firstXYZ, final byte secondXYZ) {
		final Set<IdObject> selection = new HashSet<>(selectionManager.getSelection());

		for (final IdObject idObject : model.getModel().getIdObjects()) {
			if (selectionManager.getSelection().contains(idObject.getParent()) && (((idObject.getClass() == Bone.class) && (idObject.getParent().getClass() == Bone.class)) || ((idObject.getClass() == Helper.class) && (idObject.getParent().getClass() == Helper.class)))) {
				selection.add(idObject);
			}
		}
		final List<UndoAction> actions = new ArrayList<>();
		final TimeEnvironmentImpl timeEnvironmentImpl = (TimeEnvironmentImpl) renderModel.getAnimatedRenderEnvironment();

		for (final IdObject node : selection) {
			AnimFlag translationTimeline = node.find("Rotation", timeEnvironmentImpl.getGlobalSeq());
			if (translationTimeline == null) {
				translationTimeline = AnimFlag.createEmpty2018("Rotation", InterpolationType.HERMITE, timeEnvironmentImpl.getGlobalSeq());
				node.add(translationTimeline);

				final AddTimelineAction addTimelineAction = new AddTimelineAction(node, translationTimeline, structureChangeListener);
				structureChangeListener.timelineAdded(node, translationTimeline);
				actions.add(addTimelineAction);
			}

			final AddKeyframeAction keyframeAction = node.createRotationKeyframe(renderModel, translationTimeline, structureChangeListener);
			if (keyframeAction != null) {
				actions.add(keyframeAction);
			}
		}

		final int trackTime = renderModel.getAnimatedRenderEnvironment().getAnimationTime() + renderModel.getAnimatedRenderEnvironment().getCurrentAnimation().getStart();
		final int trackTimeToUse = timeEnvironmentImpl.getGlobalSeq() == null ? trackTime : timeEnvironmentImpl.getGlobalSeqTime(timeEnvironmentImpl.getGlobalSeq());
		return new SquatToolKeyframeAction(new CompoundAction("setup", actions), trackTimeToUse, timeEnvironmentImpl.getGlobalSeq(), selection, this, centerX, centerY, centerZ, firstXYZ, secondXYZ);
	}

	@Override
	public UndoAction createFaceFromSelection(final Vec3 preferredFacingVector) {
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
			final Mat4 worldMatrix = renderModel.getRenderNode(node).getWorldMatrix();
			if (hitTest(node.getPivotPoint(), CoordinateSystem.Util.geom(axes, point), axes, node.getClickRadius(axes) * CoordinateSystem.Util.getZoom(axes) * 2, worldMatrix)) {
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
			final Mat4 worldMatrix = renderModel.getRenderNode(node).getWorldMatrix();
			if (hitTest(node.getPivotPoint(), CoordinateSystem.Util.geom(axes, point), axes, node.getClickRadius(axes) * CoordinateSystem.Util.getZoom(axes), worldMatrix)) {
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
		}

		@Override
		public void camera(final Camera camera) {
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
		public void bone(final Bone node) {
			final Mat4 worldMatrix = renderModel.getRenderNode(node).getWorldMatrix();
			if (hitTest(node.getPivotPoint(), CoordinateSystem.Util.geom(axes, point), axes, node.getClickRadius(axes) * CoordinateSystem.Util.getZoom(axes), worldMatrix)) {
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
	public UndoAction splitGeoset() {
		throw new WrongModeException("Unable to split geoset in animation editor");
	}

	private final class GenericSelectorVisitor implements IdObjectVisitor {
		private List<IdObject> selectedItems;
		private Rectangle2D area;
		private CoordinateSystem coordinateSystem;

		private GenericSelectorVisitor reset(final List<IdObject> selectedItems, final Rectangle2D area, final CoordinateSystem coordinateSystem) {
			this.selectedItems = selectedItems;
			this.area = area;
			this.coordinateSystem = coordinateSystem;
			return this;
		}

		@Override
		public void ribbonEmitter(final RibbonEmitter particleEmitter) {
			hitTest(selectedItems, area, particleEmitter.getPivotPoint(), coordinateSystem, particleEmitter.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem) * 2, particleEmitter, renderModel);
		}

		@Override
		public void particleEmitter2(final ParticleEmitter2 particleEmitter) {
			hitTest(selectedItems, area, particleEmitter.getPivotPoint(), coordinateSystem, particleEmitter.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem) * 2, particleEmitter, renderModel);
		}

		@Override
		public void particleEmitter(final ParticleEmitter particleEmitter) {
			hitTest(selectedItems, area, particleEmitter.getPivotPoint(), coordinateSystem, particleEmitter.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem) * 2, particleEmitter, renderModel);
		}

		@Override
		public void popcornFxEmitter(final ParticleEmitterPopcorn popcornFxEmitter) {
			hitTest(selectedItems, area, popcornFxEmitter.getPivotPoint(), coordinateSystem, popcornFxEmitter.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem) * 2, popcornFxEmitter, renderModel);
		}

		@Override
		public void light(final Light light) {
			hitTest(selectedItems, area, light.getPivotPoint(), coordinateSystem, light.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem) * 2, light, renderModel);
		}

		@Override
		public void helper(final Helper object) {
			hitTest(selectedItems, area, object.getPivotPoint(), coordinateSystem, object.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem), object, renderModel);
		}

		@Override
		public void eventObject(final EventObject eventObject) {
			hitTest(selectedItems, area, eventObject.getPivotPoint(), coordinateSystem, eventObject.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem) * 2, eventObject, renderModel);
		}

		@Override
		public void collisionShape(final CollisionShape collisionShape) {
			hitTest(selectedItems, area, collisionShape.getPivotPoint(), coordinateSystem, collisionShape.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem) * 2, collisionShape, renderModel);
		}

		@Override
		public void camera(final Camera camera) {
			System.err.println("Attempted to process camera with Node Animation Editor generic selector!!!");
		}

		@Override
		public void bone(final Bone object) {
			hitTest(selectedItems, area, object.getPivotPoint(), coordinateSystem, object.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem), object, renderModel);
		}

		@Override
		public void attachment(final Attachment attachment) {
			hitTest(selectedItems, area, attachment.getPivotPoint(), coordinateSystem, attachment.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem) * 2, attachment, renderModel);
		}

	}

	@Override
	public UndoAction setParent(final IdObject node) {
		throw new WrongModeException("Can't set parent in Animation Editor");
	}

	@Override
	public RigAction rig() {
		throw new WrongModeException("Unable to rig in Animation Editor");
	}

	@Override
	public UndoAction addBone(final double x, final double y, final double z) {
		throw new WrongModeException("Unable to add bone in Animation Editor");
	}

}
