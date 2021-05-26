package com.hiveworkshop.rms.ui.application.edit.animation;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
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
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ModelEditorActionType3;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.util.*;

public class NodeAnimationModelEditor extends AbstractSelectingEditor<IdObject> {
	//	private final ModelView modelView;
	private final RenderModel renderModel;
	private final ModelStructureChangeListener changeListener;

	public NodeAnimationModelEditor(ModelView modelView,
	                                NodeAnimationSelectionManager selectionManager,
	                                RenderModel renderModel,
	                                ModelStructureChangeListener changeListener) {
		super(selectionManager, modelView);
//		this.modelView = modelView;
		this.changeListener = changeListener;
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
			AddKeyframeAction addKeyframeAction = new AddKeyframeAction(node, timeline, entry, changeListener);
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
	public UndoAction setPosition(Vec3 center, Vec3 v) {
		Vec3 delta = Vec3.getDiff(v, center);
		StaticMeshMoveAction moveAction = new StaticMeshMoveAction(modelView, delta);
		moveAction.redo();
		return moveAction;
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
	public Vec3 getSelectionCenter() {
		return selectionManager.getCenter();
	}

	@Override
	public boolean editorWantsAnimation() {
		return true;
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

				AddTimelineAction addTimelineAction = new AddTimelineAction(node, timeline, changeListener);
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
	public GenericScaleAction beginScaling(Vec3 center) {
		Set<IdObject> selection = modelView.getSelectedIdObjects();
		TimeEnvironmentImpl timeEnvironmentImpl = renderModel.getAnimatedRenderEnvironment();

		List<UndoAction> actions = generateKeyframes(timeEnvironmentImpl, "Scaling", ModelEditorActionType3.SCALING, selection);


		int trackTime = renderModel.getAnimatedRenderEnvironment().getAnimationTime();
		int trackTimeToUse = timeEnvironmentImpl.getGlobalSeq() == null ? trackTime : timeEnvironmentImpl.getGlobalSeqTime(timeEnvironmentImpl.getGlobalSeq());
		return new ScalingKeyframeAction(new CompoundAction("setup", actions), trackTimeToUse, timeEnvironmentImpl.getGlobalSeq(), selection, center, modelView);
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
