package com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Quaternion;

import com.etheller.collections.HashMap;
import com.etheller.collections.Map;
import com.etheller.collections.MapView;
import com.etheller.collections.TreeMap;
import com.hiveworkshop.wc3.gui.animedit.TimeEnvironmentImpl;
import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.gui.modelviewer.ViewerCamera;
import com.hiveworkshop.wc3.mdl.AnimFlag;
import com.hiveworkshop.wc3.mdl.Animation;
import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.mdl.IdObject;
import com.hiveworkshop.wc3.mdl.QuaternionRotation;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.render3d.RenderModel;
import com.hiveworkshop.wc3.mdl.render3d.RenderNode;
import com.hiveworkshop.wc3.mdl.render3d.RenderResourceAllocator;
import com.hiveworkshop.wc3.mdl.v2.ModelViewManager;
import com.hiveworkshop.wc3.util.MathUtils;

public final class ReLinkRFBoneAction implements UndoAction {
	private EditableModel model;
	private TimeEnvironmentImpl timeEnvironment;
	private RenderModel utilityRenderModel;
	private final Map<IdObject, IdObject> nodeToOldParent;
	private final IdObject newParent;
	private final ModelStructureChangeListener changeListener;
	private final List<IdObject> nodes;
	private List<TreeSet<Integer>> allKFSets;
	private Map<IdObject, Map<Integer, Matrix4f>> nodeToFrameToMatrix;

	public ReLinkRFBoneAction(EditableModel model, final Map<IdObject, IdObject> nodeToOldParent,
			final IdObject newParent, final ModelStructureChangeListener changeListener) {
		this.model = model;
		utilityRenderModel = new RenderModel(model, new ModelViewManager(model));
		timeEnvironment = new TimeEnvironmentImpl();
		utilityRenderModel.refreshFromEditor(timeEnvironment, new ViewerCamera(), RenderResourceAllocator.DO_NOTHING);
		this.nodeToOldParent = nodeToOldParent;
		this.newParent = newParent;
		this.changeListener = changeListener;
		nodes = new ArrayList<>();
		for (final MapView.Entry<IdObject, IdObject> entry : nodeToOldParent) {
			nodes.add(entry.getKey());
		}
		allKFSets = new ArrayList<>();
		List<AnimFlag> allFlags = this.model.getAllAnimFlags();
		for (Animation anim : model.getAnims()) {
			TreeSet<Integer> allKFSet = new TreeSet<>();
			for (AnimFlag flag : allFlags) {
				for (Integer time : flag.getTimes()) {
					if ((time >= anim.getStart()) || (time <= anim.getEnd())) {
						allKFSet.add(time);
					}
				}
			}
			allKFSets.add(allKFSet);
		}
		nodeToFrameToMatrix = new HashMap<IdObject, Map<Integer, Matrix4f>>();
	}

	@Override
	public void undo() {
		nodeToFrameToMatrix.clear();
		for (TreeSet<Integer> allKFSet : allKFSets) {
			timeEnvironment.setBounds(allKFSet.first(), allKFSet.last());
			for (Integer time : allKFSet) {
				// Animate the model to every possible keyframe time
				timeEnvironment.setCurrentTime(time);
				utilityRenderModel.updateNodes(true, false);

				// At the given time, extract the difference between the node offset from old
				// parent and node offset from new parent
				for (final MapView.Entry<IdObject, IdObject> entry : nodeToOldParent) {
					setParentRewriteAnimations(entry.getKey(), entry.getValue());
				}
			}
		}
		for (MapView.Entry<IdObject, Map<Integer, Matrix4f>> nodeAndFrameToMatrix : nodeToFrameToMatrix) {
			IdObject node = nodeAndFrameToMatrix.getKey();
			Map<Integer, Matrix4f> frameToMatrix = nodeAndFrameToMatrix.getValue();
			AnimFlag translationFlag = AnimFlag.find(node.getAnimFlags(), "Translation");
			AnimFlag rotationFlag = AnimFlag.find(node.getAnimFlags(), "Rotation");
			for (MapView.Entry<Integer, Matrix4f> frameAndMatrix : frameToMatrix) {
				writeKeyFrame(frameAndMatrix.getKey(), translationFlag, rotationFlag, frameAndMatrix.getValue(),
						node.getPivotPoint());
			}
		}
		for (final MapView.Entry<IdObject, IdObject> entry : nodeToOldParent) {
			entry.getKey().setParent(entry.getValue());
		}
		changeListener.nodesRemoved(nodes);
		changeListener.nodesAdded(nodes);
	}

	@Override
	public void redo() {
		nodeToFrameToMatrix.clear();
		for (TreeSet<Integer> allKFSet : allKFSets) {
			timeEnvironment.setBounds(allKFSet.first(), allKFSet.last());
			for (Integer time : allKFSet) {
				// Animate the model to every possible keyframe time
				timeEnvironment.setCurrentTime(time);
				utilityRenderModel.updateNodes(true, false);

				// At the given time, extract the difference between the node offset from old
				// parent and node offset from new parent
				for (final MapView.Entry<IdObject, IdObject> entry : nodeToOldParent) {
					setParentRewriteAnimations(entry.getKey(), newParent);
				}
			}
		}
		for (MapView.Entry<IdObject, Map<Integer, Matrix4f>> nodeAndFrameToMatrix : nodeToFrameToMatrix) {
			IdObject node = nodeAndFrameToMatrix.getKey();
			Map<Integer, Matrix4f> frameToMatrix = nodeAndFrameToMatrix.getValue();
			AnimFlag translationFlag = AnimFlag.find(node.getAnimFlags(), "Translation");
			AnimFlag rotationFlag = AnimFlag.find(node.getAnimFlags(), "Rotation");
			for (MapView.Entry<Integer, Matrix4f> frameAndMatrix : frameToMatrix) {
				writeKeyFrame(frameAndMatrix.getKey(), translationFlag, rotationFlag, frameAndMatrix.getValue(),
						node.getPivotPoint());
			}
		}
		for (final MapView.Entry<IdObject, IdObject> entry : nodeToOldParent) {
			entry.getKey().setParent(newParent);
		}
		changeListener.nodesRemoved(nodes);
		changeListener.nodesAdded(nodes);
	}

	@Override
	public String actionName() {
		return "re-link bone probably for RF";
	}

	Quaternion tempQuatDelta = new Quaternion();
	Quaternion tempQuat = new Quaternion();
	Matrix4f tempMat = new Matrix4f();

	private void setParentRewriteAnimations(IdObject node, IdObject newParent) {
		IdObject previousParent = node.getParent();
		Vertex previousParentPivot = previousParent == null ? Vertex.ORIGIN : previousParent.getPivotPoint();
		Vertex newParentPivot = newParent == null ? Vertex.ORIGIN : newParent.getPivotPoint();
		RenderNode renderNode = utilityRenderModel.getRenderNode(node);
		RenderNode previousParentRenderNode = utilityRenderModel.getRenderNode(previousParent);
		RenderNode newParentRenderNode = utilityRenderModel.getRenderNode(newParent);

		Matrix4f tempMatDst = new Matrix4f();
		Matrix4f worldMatrix = renderNode.getWorldMatrix();
		Matrix4f newParentWorldMatrix = newParentRenderNode.getWorldMatrix();
		Matrix4f.invert(newParentWorldMatrix, tempMat);
		Matrix4f.mul(tempMat, worldMatrix, tempMatDst);

		int animationTime = timeEnvironment.getAnimationTime();

		Map<Integer, Matrix4f> frameToMatrix = nodeToFrameToMatrix.get(node);
		if (frameToMatrix == null) {
			frameToMatrix = new TreeMap<>();
			nodeToFrameToMatrix.put(node, frameToMatrix);
		}
		frameToMatrix.put(animationTime, tempMatDst);
	}

	private void writeKeyFrame(int animationTime, AnimFlag translationFlag, AnimFlag rotationFlag, Matrix4f tempMatDst,
			Vertex pivot) {
		boolean missTranslation = true;
		boolean missRotation = true;
		for (int keyframeIndex = 0; keyframeIndex < translationFlag.getTimes().size(); keyframeIndex++) {
			Integer time = translationFlag.getTimes().get(keyframeIndex);
			if (time == animationTime) {
				// the current translation keyframe to rewrite.
				Vertex value = (Vertex) translationFlag.getValues().get(keyframeIndex);
				MathUtils.extractTranslation(tempMatDst, value, pivot);
				missTranslation = false;
			}
		}

		for (int keyframeIndex = 0; keyframeIndex < rotationFlag.getTimes().size(); keyframeIndex++) {
			Integer time = rotationFlag.getTimes().get(keyframeIndex);
			if (time == animationTime) {
				// the current translation keyframe to rewrite.
				QuaternionRotation value = (QuaternionRotation) rotationFlag.getValues().get(keyframeIndex);
//				tempQuat.setFromMatrix(tempMatDst);
				MathUtils.toQuat(tempMatDst, tempQuat);
				value.a = tempQuat.x;
				value.b = tempQuat.y;
				value.c = tempQuat.z;
				value.d = tempQuat.w;
				missRotation = false;
			}
		}
		if (missTranslation && !missRotation) {
			Vertex translationValue = new Vertex(0, 0, 0);
			MathUtils.extractTranslation(tempMatDst, translationValue, pivot);
			if (translationFlag.tans()) {
				translationFlag.addKeyframe(animationTime, translationValue, new Vertex(translationValue),
						new Vertex(translationValue));
			}
			else {
				translationFlag.addKeyframe(animationTime, translationValue);
			}
		}
		if (missRotation && !missTranslation) {
//			tempQuat.setFromMatrix(tempMatDst);
			MathUtils.toQuat(tempMatDst, tempQuat);
			QuaternionRotation value = new QuaternionRotation(tempQuat.x, tempQuat.y, tempQuat.z, tempQuat.w);
			if (rotationFlag.tans()) {
				rotationFlag.addKeyframe(animationTime, value, new QuaternionRotation(value),
						new QuaternionRotation(value));
			}
			else {
				rotationFlag.addKeyframe(animationTime, value);
			}
		}
	}
}
