package com.hiveworkshop.rms.ui.application.edit.animation;

import com.hiveworkshop.rms.editor.model.AnimatedNode;
import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.Helper;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.editor.model.animflag.QuatAnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.render3d.RenderNode;
import com.hiveworkshop.rms.parsers.mdlx.InterpolationType;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.AbstractSelectingEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordSysUtils;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.animation.*;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.editor.StaticMeshMoveAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.CompoundAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericMoveAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericRotateAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericScaleAction;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ModelEditorActionType3;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.util.*;

public class NodeAnimationModelEditor extends AbstractSelectingEditor {
	private final RenderModel renderModel;
	private final ModelStructureChangeListener changeListener;

	public NodeAnimationModelEditor(SelectionManager selectionManager, ModelStructureChangeListener changeListener, ModelHandler modelHandler,
	                                SelectionItemTypes selectionMode) {
		super(selectionManager, modelHandler.getModelView());
		this.changeListener = changeListener;
		this.renderModel = modelHandler.getRenderModel();
	}


	public static boolean hitTest(Vec3 vertex, Vec2 point, CoordinateSystem coordinateSystem, double vertexSize, Mat4 worldMatrix) {
		Vec3 pivotHeap = Vec3.getTransformed(vertex, worldMatrix);
		pivotHeap.transform(worldMatrix);
		Vec2 vertexV2 = CoordSysUtils.convertToViewVec2(coordinateSystem, pivotHeap);
		return vertexV2.distance(point) <= (vertexSize / 2.0);
	}

	public static double distance(double vertexX, double vertexY, double x, double y) {
		double dx = x - vertexX;
		double dy = y - vertexY;
		return Math.sqrt((dx * dx) + (dy * dy));
	}

	@Override
	public boolean selectableUnderCursor(Vec2 point, CoordinateSystem axes) {
		for (IdObject object : modelView.getEditableIdObjects()) {
			Mat4 worldMatrix = renderModel.getRenderNode(object).getWorldMatrix();
			double vertexSize = object.getClickRadius(axes) * axes.getZoom() * 2;
			if (NodeAnimationModelEditor.hitTest(object.getPivotPoint(), CoordSysUtils.geomV2(axes, point), axes, vertexSize, worldMatrix)) {
				return true;
			}
		}
		return false;
	}


	private static int getTrackTime(RenderModel renderModel) {
		int trackTime = renderModel.getAnimatedRenderEnvironment().getAnimationTime();

		Integer globalSeq = renderModel.getAnimatedRenderEnvironment().getGlobalSeq();
		if (globalSeq != null) {
			trackTime = renderModel.getAnimatedRenderEnvironment().getGlobalSeqTime(globalSeq);
		}
		return trackTime;
	}

	private static AddKeyframeAction getAddKeyframeAction(AnimatedNode node,
	                                                      AnimFlag<?> timeline,
	                                                      ModelStructureChangeListener changeListener,
	                                                      int trackTime,
	                                                      Entry<?> entry) {
		if (!timeline.hasEntryAt(trackTime)) {
			if (timeline.getInterpolationType().tangential()) {
				entry.unLinearize();
			}

			changeListener.keyframeAdded(node, timeline, trackTime);
			AddKeyframeAction addKeyframeAction = new AddKeyframeAction(timeline, entry);
			addKeyframeAction.redo();
			return addKeyframeAction;
		}
		return null;
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
	public UndoAction setPosition(Vec3 center, Vec3 v) {
		Vec3 delta = Vec3.getDiff(v, center);
		StaticMeshMoveAction moveAction = new StaticMeshMoveAction(modelView, delta);
		moveAction.redo();
		return moveAction;
	}

	@Override
	public boolean editorWantsAnimation() {
		return true;
	}

	private List<UndoAction> generateKeyframes(TimeEnvironmentImpl timeEnvironmentImpl, String name, ModelEditorActionType3 actionType, Set<IdObject> selection) {
		List<UndoAction> actions = new ArrayList<>();
		for (IdObject node : selection) {
			AnimFlag<?> timeline = node.find(name, timeEnvironmentImpl.getGlobalSeq());

			if (timeline == null) {
//				if (name.equals("Rotation")) {
				if (actionType == ModelEditorActionType3.ROTATION || actionType == ModelEditorActionType3.SQUAT) {
					timeline = new QuatAnimFlag(name, InterpolationType.HERMITE, timeEnvironmentImpl.getGlobalSeq());
				} else {
					timeline = new Vec3AnimFlag(name, InterpolationType.HERMITE, timeEnvironmentImpl.getGlobalSeq());
				}
				node.add(timeline);

				AddTimelineAction addTimelineAction = new AddTimelineAction(node, timeline);
				changeListener.timelineAdded(node, timeline);
				actions.add(addTimelineAction);
			}

			int trackTime = getTrackTime(renderModel);
			RenderNode renderNode = renderModel.getRenderNode(node);

			AddKeyframeAction keyframeAction = switch (actionType) {
				case ROTATION, SQUAT -> getAddKeyframeAction(node, timeline, changeListener, trackTime, new Entry<>(trackTime, renderNode.getLocalRotation()));
				case SCALING -> getAddKeyframeAction(node, timeline, changeListener, trackTime, new Entry<>(trackTime, renderNode.getLocalScale()));
				case TRANSLATION, EXTEND, EXTRUDE -> getAddKeyframeAction(node, timeline, changeListener, trackTime, new Entry<>(trackTime, renderNode.getLocalLocation()));
			};

			if (keyframeAction != null) {
				actions.add(keyframeAction);
			}
		}
		return actions;
	}

	@Override
	public GenericMoveAction beginTranslation() {
		Set<IdObject> selection = modelView.getSelectedIdObjects();
		// TODO fix cast, meta knowledge: NodeAnimationModelEditor will only be constructed from a TimeEnvironmentImpl render environment, and never from the anim previewer impl
		TimeEnvironmentImpl timeEnvironmentImpl = renderModel.getAnimatedRenderEnvironment();

		List<UndoAction> actions = generateKeyframes(timeEnvironmentImpl, "Translation", ModelEditorActionType3.TRANSLATION, selection);

		int trackTime = renderModel.getAnimatedRenderEnvironment().getAnimationTime();
		int trackTimeToUse = timeEnvironmentImpl.getGlobalSeq() == null ? trackTime : timeEnvironmentImpl.getGlobalSeqTime(timeEnvironmentImpl.getGlobalSeq());
		return new TranslationKeyframeAction(new CompoundAction("setup", actions), trackTimeToUse, timeEnvironmentImpl.getGlobalSeq(), selection, modelView);
	}

	@Override
	public GenericScaleAction beginScaling(Vec3 center) {
		Set<IdObject> selection = modelView.getSelectedIdObjects();
		TimeEnvironmentImpl timeEnvironmentImpl = renderModel.getAnimatedRenderEnvironment();

		List<UndoAction> actions = generateKeyframes(timeEnvironmentImpl, "Scaling", ModelEditorActionType3.SCALING, selection);


		int trackTime = renderModel.getAnimatedRenderEnvironment().getAnimationTime();
		int trackTimeToUse = timeEnvironmentImpl.getGlobalSeq() == null ? trackTime : timeEnvironmentImpl.getGlobalSeqTime(timeEnvironmentImpl.getGlobalSeq());
		return new ScalingKeyframeAction(new CompoundAction("setup", actions), trackTimeToUse, timeEnvironmentImpl.getGlobalSeq(), selection, center, modelView);
	}

	@Override
	public GenericRotateAction beginRotation(Vec3 center, byte firstXYZ, byte secondXYZ) {
		Set<IdObject> selection = modelView.getSelectedIdObjects();

		TimeEnvironmentImpl timeEnvironmentImpl = renderModel.getAnimatedRenderEnvironment();

		List<UndoAction> actions = generateKeyframes(timeEnvironmentImpl, "Rotation", ModelEditorActionType3.ROTATION, selection);

		int trackTime = renderModel.getAnimatedRenderEnvironment().getAnimationTime();
		int trackTimeToUse = timeEnvironmentImpl.getGlobalSeq() == null ? trackTime : timeEnvironmentImpl.getGlobalSeqTime(timeEnvironmentImpl.getGlobalSeq());
		return new RotationKeyframeAction(new CompoundAction("setup", actions), trackTimeToUse, timeEnvironmentImpl.getGlobalSeq(), selection, modelView, center, firstXYZ, secondXYZ);
	}

	@Override
	public GenericRotateAction beginSquatTool(Vec3 center, byte firstXYZ, byte secondXYZ) {
		Set<IdObject> selection = new HashSet<>(modelView.getSelectedIdObjects());

		for (IdObject idObject : modelView.getModel().getIdObjects()) {
			if (modelView.getSelectedIdObjects().contains(idObject.getParent()) && (((idObject.getClass() == Bone.class) && (idObject.getParent().getClass() == Bone.class)) || ((idObject.getClass() == Helper.class) && (idObject.getParent().getClass() == Helper.class)))) {
				selection.add(idObject);
			}
		}

		TimeEnvironmentImpl timeEnvironmentImpl = renderModel.getAnimatedRenderEnvironment();
		List<UndoAction> actions = generateKeyframes(timeEnvironmentImpl, "Rotation", ModelEditorActionType3.SQUAT, selection);

		int trackTime = renderModel.getAnimatedRenderEnvironment().getAnimationTime();
		int trackTimeToUse = timeEnvironmentImpl.getGlobalSeq() == null ? trackTime : timeEnvironmentImpl.getGlobalSeqTime(timeEnvironmentImpl.getGlobalSeq());
		return new SquatToolKeyframeAction(new CompoundAction("setup", actions), trackTimeToUse, timeEnvironmentImpl.getGlobalSeq(), selection, modelView, center, firstXYZ, secondXYZ);
	}
}
