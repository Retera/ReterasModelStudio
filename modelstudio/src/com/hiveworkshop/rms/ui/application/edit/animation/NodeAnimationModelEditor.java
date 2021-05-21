package com.hiveworkshop.rms.ui.application.edit.animation;

import com.hiveworkshop.rms.editor.model.*;
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
import com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree.CheckableDisplayElement;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.animation.*;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.editor.StaticMeshMoveAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.selection.MakeNotEditableAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.CompoundAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericMoveAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericRotateAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericScaleAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.EditabilityToggleHandler;
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

	public static void hitTest(List<IdObject> selectedItems, Vec2 min, Vec2 max, Vec3 geosetVertex, CoordinateSystem coordinateSystem, double vertexSize, IdObject object, RenderModel renderModel) {
		RenderNode renderNode = renderModel.getRenderNode(object);
		Vec3 pivotHeap = Vec3.getTransformed(geosetVertex, renderNode.getWorldMatrix());

		byte dim1 = coordinateSystem.getPortFirstXYZ();
		byte dim2 = coordinateSystem.getPortSecondXYZ();

		Vec2 minView = new Vec2(min).minimize(max);
		Vec2 maxView = new Vec2(max).maximize(min);

		Vec2 vertexV2 = pivotHeap.getProjected(dim1, dim2);

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

	public static double distance(double vertexX, double vertexY, double x, double y) {
		double dx = x - vertexX;
		double dy = y - vertexY;
		return Math.sqrt((dx * dx) + (dy * dy));
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
	public UndoAction rotate(Vec3 center, Vec3 rotate) {

		GenericRotateAction rotationX = beginRotation(center, (byte) 2, (byte) 1);
		rotationX.updateRotation(rotate.x);
		GenericRotateAction rotationY = beginRotation(center, (byte) 0, (byte) 2);
		rotationY.updateRotation(rotate.y);
		GenericRotateAction rotationZ = beginRotation(center, (byte) 1, (byte) 0);
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

		generateKeyframes(selection, actions, timeEnvironmentImpl, "Translation", (node, translationTimeline) -> createTranslationKeyframe(node, renderModel, (Vec3AnimFlag) translationTimeline, structureChangeListener));

		int trackTime = renderModel.getAnimatedRenderEnvironment().getAnimationTime();
		int trackTimeToUse = timeEnvironmentImpl.getGlobalSeq() == null ? trackTime : timeEnvironmentImpl.getGlobalSeqTime(timeEnvironmentImpl.getGlobalSeq());
		return new TranslationKeyframeAction(new CompoundAction("setup", actions), trackTimeToUse, timeEnvironmentImpl.getGlobalSeq(), selection, modelView);
	}

	public static AddKeyframeAction createTranslationKeyframe(AnimatedNode animatedNode,
	                                                          RenderModel renderModel,
	                                                          Vec3AnimFlag translationFlag,
	                                                          ModelStructureChangeListener changeListener) {
		// TODO global seqs, needs separate check on AnimRendEnv, and also we must make AnimFlag.find seek on globalSeqId
		int trackTime = getTrackTime(renderModel);
		int floorIndex = translationFlag.floorIndex(trackTime);
		RenderNode renderNode = renderModel.getRenderNode(animatedNode);

		if ((floorIndex != -1) && (translationFlag.getTimes().size() > 0) && (translationFlag.getTimes().get(floorIndex).equals(trackTime))) {
			return null;
		} else {
			Vec3 localLocation = renderNode.getLocalLocation();
			return getAddKeyframeAction(animatedNode, translationFlag, changeListener, trackTime, floorIndex, localLocation);
		}
	}

	private static int getTrackTime(RenderModel renderModel) {
		int animationTime = renderModel.getAnimatedRenderEnvironment().getAnimationTime();
		int trackTime = animationTime;

		Integer globalSeq = renderModel.getAnimatedRenderEnvironment().getGlobalSeq();
		if (globalSeq != null) {
			trackTime = renderModel.getAnimatedRenderEnvironment().getGlobalSeqTime(globalSeq);
		}
		return trackTime;
	}

	@Override
	public UndoAction translate(Vec3 v) {
		GenericMoveAction genericMoveAction = beginTranslation();
		genericMoveAction.updateTranslation(v);
		genericMoveAction.redo();
		return genericMoveAction;
	}
	@Override
	public UndoAction scale(Vec3 center, Vec3 scale) {
		GenericScaleAction genericScaleAction = beginScaling(center);
		genericScaleAction.updateScale(scale);
		genericScaleAction.redo();
		return genericScaleAction;
	}

	@Override
	public UndoAction setPosition(Vec3 center, Vec3 v) {
		Vec3 delta = Vec3.getDiff(v, center);
		StaticMeshMoveAction moveAction = new StaticMeshMoveAction(modelView, delta);
		moveAction.redo();
		return moveAction;
	}

	@Override
	public GenericRotateAction beginRotation(Vec3 center, byte firstXYZ, byte secondXYZ) {
		Set<IdObject> selection = modelView.getSelectedIdObjects();
		List<UndoAction> actions = new ArrayList<>();

		TimeEnvironmentImpl timeEnvironmentImpl = renderModel.getAnimatedRenderEnvironment();

		generateKeyframes(selection, actions, timeEnvironmentImpl, "Rotation", (node, translationTimeline) -> createRotationKeyframe(node, renderModel, (QuatAnimFlag) translationTimeline, structureChangeListener));

		int trackTime = renderModel.getAnimatedRenderEnvironment().getAnimationTime();
		int trackTimeToUse = timeEnvironmentImpl.getGlobalSeq() == null ? trackTime : timeEnvironmentImpl.getGlobalSeqTime(timeEnvironmentImpl.getGlobalSeq());
		return new RotationKeyframeAction(new CompoundAction("setup", actions), trackTimeToUse, timeEnvironmentImpl.getGlobalSeq(), selection, modelView, center, firstXYZ, secondXYZ);
	}

	public static AddKeyframeAction createRotationKeyframe(AnimatedNode animatedNode,
	                                                       RenderModel renderModel,
	                                                       QuatAnimFlag rotationTimeline,
	                                                       ModelStructureChangeListener changeListener) {
		// TODO global seqs, needs separate check on AnimRendEnv, and also we must make AnimFlag.find seek on globalSeqId
		int trackTime = getTrackTime(renderModel);
		int floorIndex = rotationTimeline.floorIndex(trackTime);
		RenderNode renderNode = renderModel.getRenderNode(animatedNode);

		if ((floorIndex != -1) && (rotationTimeline.getTimes().size() > 0) && (rotationTimeline.getTimes().get(floorIndex).equals(trackTime))) {
			return null;
		} else {
			Quat localRotation = renderNode.getLocalRotation();
			return getAddKeyframeAction(animatedNode, rotationTimeline, changeListener, trackTime, floorIndex, localRotation);
		}
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
	public GenericScaleAction beginScaling(Vec3 center) {
		Set<IdObject> selection = modelView.getSelectedIdObjects();
		List<UndoAction> actions = new ArrayList<>();
		TimeEnvironmentImpl timeEnvironmentImpl = renderModel.getAnimatedRenderEnvironment();

		generateKeyframes(selection, actions, timeEnvironmentImpl, "Scaling", (node, translationTimeline) -> createScalingKeyframe(node, renderModel, (Vec3AnimFlag) translationTimeline, structureChangeListener));


		int trackTime = renderModel.getAnimatedRenderEnvironment().getAnimationTime();
		int trackTimeToUse = timeEnvironmentImpl.getGlobalSeq() == null ? trackTime : timeEnvironmentImpl.getGlobalSeqTime(timeEnvironmentImpl.getGlobalSeq());
		return new ScalingKeyframeAction(new CompoundAction("setup", actions), trackTimeToUse, timeEnvironmentImpl.getGlobalSeq(), selection, center, modelView);
	}

	public static AddKeyframeAction createScalingKeyframe(AnimatedNode animatedNode,
	                                                      RenderModel renderModel,
	                                                      Vec3AnimFlag scalingTimeline,
	                                                      ModelStructureChangeListener changeListener) {
		// TODO global seqs, needs separate check on AnimRendEnv, and also we must make AnimFlag.find seek on globalSeqId
		int trackTime = getTrackTime(renderModel);
		int floorIndex = scalingTimeline.floorIndex(trackTime);
		RenderNode renderNode = renderModel.getRenderNode(animatedNode);

		if ((floorIndex != -1) && (scalingTimeline.getTimes().size() > 0) && (scalingTimeline.getTimes().get(floorIndex).equals(trackTime))) {
			return null;
		} else {
			Vec3 localScale = renderNode.getLocalScale();
			return getAddKeyframeAction(animatedNode, scalingTimeline, changeListener, trackTime, floorIndex, localScale);
		}
	}

	@Override
	public GenericRotateAction beginSquatTool(Vec3 center, byte firstXYZ, byte secondXYZ) {
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

			AddKeyframeAction keyframeAction = createRotationKeyframe(node, renderModel, translationTimeline, structureChangeListener);
			if (keyframeAction != null) {
				actions.add(keyframeAction);
			}
		}

		int trackTime = renderModel.getAnimatedRenderEnvironment().getAnimationTime();
		int trackTimeToUse = timeEnvironmentImpl.getGlobalSeq() == null ? trackTime : timeEnvironmentImpl.getGlobalSeqTime(timeEnvironmentImpl.getGlobalSeq());
		return new SquatToolKeyframeAction(new CompoundAction("setup", actions), trackTimeToUse, timeEnvironmentImpl.getGlobalSeq(), selection, modelView, center, firstXYZ, secondXYZ);
	}


	private static AddKeyframeAction getAddKeyframeAction(AnimatedNode animatedNode,
	                                                      Vec3AnimFlag timeline,
	                                                      ModelStructureChangeListener changeListener,
	                                                      int trackTime, int floorIndex, Vec3 vec3) {
		int insertIndex = floorIndex + 1;
		timeline.getTimes().add(insertIndex, trackTime);

		Vec3 keyframeValue = new Vec3(vec3);
		timeline.getValues().add(insertIndex, keyframeValue);
//		if (timeline.tans()) {
		if (timeline.interpolationType.tangential()) {
			Vec3 inTan = new Vec3(vec3);
			timeline.getInTans().add(insertIndex, inTan);

			Vec3 outTan = new Vec3(vec3);
			timeline.getOutTans().add(insertIndex, outTan);

			changeListener.keyframeAdded(animatedNode, timeline, trackTime);
			return new AddKeyframeAction(animatedNode, timeline, trackTime, keyframeValue, inTan, outTan, changeListener);
		} else {
			changeListener.keyframeAdded(animatedNode, timeline, trackTime);
			return new AddKeyframeAction(animatedNode, timeline, trackTime, keyframeValue, changeListener);
		}
	}

	private static AddKeyframeAction getAddKeyframeAction(AnimatedNode animatedNode,
	                                                      QuatAnimFlag rotationTimeline,
	                                                      ModelStructureChangeListener changeListener,
	                                                      int trackTime,
	                                                      int floorIndex,
	                                                      Quat localRotation) {
		int insertIndex = floorIndex + 1;
		rotationTimeline.getTimes().add(insertIndex, trackTime);

		Quat keyframeValue = new Quat(localRotation);
		rotationTimeline.getValues().add(insertIndex, keyframeValue);
		if (rotationTimeline.interpolationType.tangential()) {
			Quat inTan = new Quat(localRotation);
			rotationTimeline.getInTans().add(insertIndex, inTan);

			Quat outTan = new Quat(localRotation);
			rotationTimeline.getOutTans().add(insertIndex, outTan);

			changeListener.keyframeAdded(animatedNode, rotationTimeline, trackTime);
			return new AddKeyframeAction(animatedNode, rotationTimeline, trackTime, keyframeValue, inTan, outTan, changeListener);
		} else {
			changeListener.keyframeAdded(animatedNode, rotationTimeline, trackTime);
			return new AddKeyframeAction(animatedNode, rotationTimeline, trackTime, keyframeValue, changeListener);
		}
	}
}
