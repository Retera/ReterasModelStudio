package com.hiveworkshop.rms.ui.application.edit.animation;

import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.CollisionShape;
import com.hiveworkshop.rms.editor.model.Helper;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.QuatAnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.render3d.RenderNode;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.parsers.mdlx.InterpolationType;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.AbstractSelectingEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordSysUtils;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.cutpaste.CopiedModelData;
import com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree.CheckableDisplayElement;
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
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ModelEditorActionType3;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.util.*;
import java.util.function.BiFunction;

public class NodeAnimationModelEditor extends AbstractSelectingEditor<IdObject> {
	//	private final ModelView modelView;
	private final RenderModel renderModel;
	private final ModelStructureChangeListener structureChangeListener;

	public NodeAnimationModelEditor(ModelView modelView,
	                                NodeAnimationSelectionManager selectionManager,
	                                RenderModel renderModel,
	                                ModelStructureChangeListener structureChangeListener) {
		super(selectionManager, modelView);
//		this.modelView = modelView;
		this.structureChangeListener = structureChangeListener;
		this.renderModel = renderModel;
	}

	@Override
	public UndoAction autoCenterSelectedBones() {
		throw new WrongModeException("Unable to autocenter bones in Animation Editor");
	}

	public static void hitTest(List<IdObject> selectedItems, Vec2 min, Vec2 max, Vec3 geosetVertex, CoordinateSystem coordinateSystem, double vertexSize, IdObject object, RenderModel renderModel) {
		RenderNode renderNode = renderModel.getRenderNode(object);
		Vec3 pivotHeap = Vec3.getTransformed(geosetVertex, renderNode.getWorldMatrix());

		byte dim1 = coordinateSystem.getPortFirstXYZ();
		byte dim2 = coordinateSystem.getPortSecondXYZ();

		Vec2 minView = new Vec2(min).minimize(max);
		Vec2 maxView = new Vec2(max).maximize(min);

//		double minX = coordinateSystem.viewX(area.getMinX());
//		double minY = coordinateSystem.viewY(area.getMinY());
//		double maxX = coordinateSystem.viewX(area.getMaxX());
//		double maxY = coordinateSystem.viewY(area.getMaxY());
		Vec2 vertexV2 = pivotHeap.getProjected(dim1, dim2);
//		double vertexX = pivotHeap.getCoord(dim1);
//		double x = coordinateSystem.viewX(vertexX);
//		double vertexY = pivotHeap.getCoord(dim2);
//		double y = coordinateSystem.viewY(vertexY);
		vertexV2.distance(max);
		if ((vertexV2.distance(min) <= (vertexSize / 2.0)) || (vertexV2.distance(max) <= (vertexSize / 2.0)) || within(vertexV2, min, max)) {
			selectedItems.add(object);
		}
	}

	private static boolean within(Vec2 point, Vec2 min, Vec2 max){
		boolean xIn = max.x >= point.x && point.x >= min.x;
		boolean yIn = max.y >= point.y && point.y >= min.y;
		return xIn && yIn;
	}

	public static boolean hitTest(Vec3 vertex, Vec2 point, CoordinateSystem coordinateSystem, double vertexSize, Mat4 worldMatrix) {
		Vec3 pivotHeap = Vec3.getTransformed(vertex, worldMatrix);
		pivotHeap.transform(worldMatrix);
		Vec2 vertexV2 = CoordSysUtils.convertToViewVec2(coordinateSystem, pivotHeap);
//		double x = coordinateSystem.viewX(pivotHeap.getCoord(coordinateSystem.getPortFirstXYZ()));
//		double y = coordinateSystem.viewY(pivotHeap.getCoord(coordinateSystem.getPortSecondXYZ()));
//		double px = coordinateSystem.viewX(point.getX());
//		double py = coordinateSystem.viewY(point.getY());
		return vertexV2.distance(point) <= (vertexSize / 2.0);
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
		for (IdObject object : modelView.getEditableIdObjects()) {
			if (newSelection.contains(object.getPivotPoint())) {
				newlySelectedObjects.add(object);
			}
			if (object instanceof CollisionShape) {
				for (Vec3 vertex : ((CollisionShape) object).getVertices()) {
					if (newSelection.contains(vertex)) {
						newlySelectedObjects.add(object);
					}
				}
			}
		}
		// TODO cameras in a second CameraAnimationEditor
		selectionManager.setSelection(newlySelectedObjects);
	}

	@Override
	public UndoAction invertSelection() {
		List<IdObject> oldSelection = new ArrayList<>(modelView.getSelectedIdObjects());
		modelView.invertIdObjSelection();
		Set<IdObject> invertedSelection = new HashSet<>(modelView.getSelectedIdObjects());
//		for (IdObject node : modelView.getEditableIdObjects()) {
//			toggleSelection(invertedSelection, node);
//		}
//		selectionManager.setSelection(invertedSelection);
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
		List<IdObject> oldSelection = new ArrayList<>(modelView.getSelectedIdObjects());
		modelView.selectAllIdObjs();
		Set<IdObject> allSelection = new HashSet<>(modelView.getSelectedIdObjects());
//		selectionManager.setSelection(allSelection);
		return new SetSelectionAction<>(allSelection, oldSelection, selectionManager, "select all");
	}

	@Override
	protected List<IdObject> genericSelect(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem) {
		List<IdObject> selectedItems = new ArrayList<>();
//		double startingClickX = region.getX();
//		double startingClickY = region.getY();
//		double endingClickX = region.getX() + region.getWidth();
//		double endingClickY = region.getY() + region.getHeight();

//		double minX = Math.min(startingClickX, endingClickX);
//		double minY = Math.min(startingClickY, endingClickY);
//		double maxX = Math.max(startingClickX, endingClickX);
//		double maxY = Math.max(startingClickY, endingClickY);
//		Rectangle2D area = new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);

		for (IdObject object : modelView.getEditableIdObjects()) {
			double vertexSize = object.getClickRadius(coordinateSystem) * coordinateSystem.getZoom() * 2;
			hitTest(selectedItems, min, max, object.getPivotPoint(), coordinateSystem, vertexSize, object, renderModel);
		}
		return selectedItems;
	}

	@Override
	public boolean canSelectAt(Vec2 point, CoordinateSystem axes) {
		for (IdObject object : modelView.getEditableIdObjects()) {
			Mat4 worldMatrix = renderModel.getRenderNode(object).getWorldMatrix();
			double vertexSize = object.getClickRadius(axes) * axes.getZoom() * 2;
			if (NodeAnimationModelEditor.hitTest(object.getPivotPoint(), CoordSysUtils.geomV2(axes, point), axes, vertexSize, worldMatrix)) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected UndoAction buildHideComponentAction(List<? extends CheckableDisplayElement<?>> selectableComponents, EditabilityToggleHandler editabilityToggleHandler, Runnable refreshGUIRunnable) {
		List<IdObject> previousSelection = new ArrayList<>(modelView.getSelectedIdObjects());
		List<IdObject> possibleVerticesToTruncate = new ArrayList<>();

		for (CheckableDisplayElement<?> component : selectableComponents) {
			Object item = component.getItem();
			if (item instanceof IdObject) {
				possibleVerticesToTruncate.add((IdObject) item);
			}
		}
		Runnable truncateSelectionRunnable = () -> modelView.removeSelectedIdObjects(possibleVerticesToTruncate);

		Runnable unTruncateSelectionRunnable = () -> modelView.setSelectedIdObjects(previousSelection);
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
		for (IdObject idObject : modelView.getSelectedIdObjects()) {
			idObject.updateScalingKeyframe(renderModel, scaleX, scaleY, scaleZ, nodeToLocalScale.get(idObject));
		}
	}

	public void rawScale(Vec3 center, Vec3 scale, Map<IdObject, Vec3> nodeToLocalScale) {
		for (IdObject idObject : modelView.getSelectedIdObjects()) {
			idObject.updateScalingKeyframe(renderModel, scale, nodeToLocalScale.get(idObject));
		}
	}

	@Override
	public void rawRotate2d(double centerX, double centerY, double centerZ, double radians, byte firstXYZ, byte secondXYZ) {
		throw new UnsupportedOperationException("Unable to rotate directly in animation mode, use other system");
	}

	public void rawRotate2d(double centerX, double centerY, double centerZ, double radians, byte firstXYZ, byte secondXYZ, Map<IdObject, Quat> nodeToLocalRotation) {
		for (IdObject idObject : modelView.getSelectedIdObjects()) {
			idObject.updateRotationKeyframe(renderModel, centerX, centerY, centerZ, radians, firstXYZ, secondXYZ, nodeToLocalRotation.get(idObject));
		}
	}

	@Override
	public void rawTranslate(double x, double y, double z) {
		// throw new UnsupportedOperationException("Unable to translate directly in animation mode, use other system");
		for (IdObject idObject : modelView.getSelectedIdObjects()) {
			idObject.updateTranslationKeyframe(renderModel, x, y, z, new Vec3());
		}
	}

	public void rawTranslate(double x, double y, double z, Map<IdObject, Vec3> nodeToLocalTranslation) {
		for (IdObject idObject : modelView.getSelectedIdObjects()) {
			idObject.updateTranslationKeyframe(renderModel, x, y, z, nodeToLocalTranslation.get(idObject));
		}
	}

	public void rawSquatToolRotate2d(double centerX, double centerY, double centerZ, double radians, byte firstXYZ, byte secondXYZ, Map<IdObject, Quat> nodeToLocalRotation) {
		for (IdObject idObject : modelView.getSelectedIdObjects()) {
			idObject.updateRotationKeyframe(renderModel, centerX, centerY, centerZ, radians, firstXYZ, secondXYZ, nodeToLocalRotation.get(idObject));
		}
		for (IdObject idObject : modelView.getModel().getIdObjects()) {
			if (modelView.getSelectedIdObjects().contains(idObject.getParent()) && (((idObject.getClass() == Bone.class) && (idObject.getParent().getClass() == Bone.class)) || ((idObject.getClass() == Helper.class) && (idObject.getParent().getClass() == Helper.class)))) {
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
		Set<IdObject> selection = modelView.getSelectedIdObjects();
		List<UndoAction> actions = new ArrayList<>();
		// TODO fix cast, meta knowledge: NodeAnimationModelEditor will only be constructed from a TimeEnvironmentImpl render environment, and never from the anim previewer impl
		TimeEnvironmentImpl timeEnvironmentImpl = renderModel.getAnimatedRenderEnvironment();

		generateKeyframes(selection, actions, timeEnvironmentImpl, "Translation", (node, translationTimeline) -> node.createTranslationKeyframe(renderModel, (Vec3AnimFlag) translationTimeline, structureChangeListener));

		int trackTime = renderModel.getAnimatedRenderEnvironment().getAnimationTime();
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
		Set<IdObject> selection = modelView.getSelectedIdObjects();
		List<UndoAction> actions = new ArrayList<>();

		TimeEnvironmentImpl timeEnvironmentImpl = renderModel.getAnimatedRenderEnvironment();

		generateKeyframes(selection, actions, timeEnvironmentImpl, "Rotation", (node, translationTimeline) -> node.createRotationKeyframe(renderModel, (QuatAnimFlag) translationTimeline, structureChangeListener));

		int trackTime = renderModel.getAnimatedRenderEnvironment().getAnimationTime();
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
		Set<IdObject> selection = modelView.getSelectedIdObjects();
		List<UndoAction> actions = new ArrayList<>();
		TimeEnvironmentImpl timeEnvironmentImpl = renderModel.getAnimatedRenderEnvironment();

		generateKeyframes(selection, actions, timeEnvironmentImpl, "Scaling", (node, translationTimeline) -> node.createScalingKeyframe(renderModel, (Vec3AnimFlag) translationTimeline, structureChangeListener));


		int trackTime = renderModel.getAnimatedRenderEnvironment().getAnimationTime();
		int trackTimeToUse = timeEnvironmentImpl.getGlobalSeq() == null ? trackTime : timeEnvironmentImpl.getGlobalSeqTime(timeEnvironmentImpl.getGlobalSeq());
		return new ScalingKeyframeAction(new CompoundAction("setup", actions), trackTimeToUse, timeEnvironmentImpl.getGlobalSeq(), selection, this, centerX, centerY, centerZ);
	}

	@Override
	public GenericScaleAction beginScaling(Vec3 center) {
		Set<IdObject> selection = modelView.getSelectedIdObjects();
		List<UndoAction> actions = new ArrayList<>();
		TimeEnvironmentImpl timeEnvironmentImpl = renderModel.getAnimatedRenderEnvironment();

		generateKeyframes(selection, actions, timeEnvironmentImpl, "Scaling", (node, translationTimeline) -> node.createScalingKeyframe(renderModel, (Vec3AnimFlag) translationTimeline, structureChangeListener));


		int trackTime = renderModel.getAnimatedRenderEnvironment().getAnimationTime();
		int trackTimeToUse = timeEnvironmentImpl.getGlobalSeq() == null ? trackTime : timeEnvironmentImpl.getGlobalSeqTime(timeEnvironmentImpl.getGlobalSeq());
		return new ScalingKeyframeAction(new CompoundAction("setup", actions), trackTimeToUse, timeEnvironmentImpl.getGlobalSeq(), selection, this, center);
	}

	@Override
	public UndoAction createKeyframe(ModelEditorActionType3 actionType) {
		String keyframeMdlTypeName = switch (actionType) {
			case ROTATION -> "Rotation";
			case SCALING -> "Scaling";
			case TRANSLATION, EXTEND, EXTRUDE -> "Translation";
		};

		Set<IdObject> selection = modelView.getSelectedIdObjects();
		List<UndoAction> actions = new ArrayList<>();

		TimeEnvironmentImpl timeEnvironmentImpl = renderModel.getAnimatedRenderEnvironment();
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
				case TRANSLATION, EXTEND, EXTRUDE -> node.createTranslationKeyframe(renderModel, (Vec3AnimFlag) transformationTimeline, structureChangeListener);
			};
			if (keyframeAction != null) {
				actions.add(keyframeAction);
			}
		}

		return new CompoundAction("create keyframe", actions);

	}

	@Override
	public GenericRotateAction beginSquatTool(double centerX, double centerY, double centerZ, byte firstXYZ, byte secondXYZ) {
		Set<IdObject> selection = new HashSet<>(modelView.getSelectedIdObjects());

		for (IdObject idObject : modelView.getModel().getIdObjects()) {
			if (modelView.getSelectedIdObjects().contains(idObject.getParent()) && (((idObject.getClass() == Bone.class) && (idObject.getParent().getClass() == Bone.class)) || ((idObject.getClass() == Helper.class) && (idObject.getParent().getClass() == Helper.class)))) {
				selection.add(idObject);
			}
		}
		List<UndoAction> actions = new ArrayList<>();
		TimeEnvironmentImpl timeEnvironmentImpl = renderModel.getAnimatedRenderEnvironment();

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

		int trackTime = renderModel.getAnimatedRenderEnvironment().getAnimationTime();
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

	@Override
	public RigAction rig() {
		throw new WrongModeException("Unable to rig in Animation Editor");
	}

}
