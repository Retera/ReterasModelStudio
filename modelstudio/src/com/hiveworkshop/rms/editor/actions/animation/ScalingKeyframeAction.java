package com.hiveworkshop.rms.editor.actions.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.editor.AbstractTransformAction;
import com.hiveworkshop.rms.editor.model.AnimatedNode;
import com.hiveworkshop.rms.editor.model.GlobalSeq;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class ScalingKeyframeAction extends AbstractTransformAction {
	private final UndoAction addingTimelinesOrKeyframesAction;
	private final int trackTime;
	private final List<IdObject> nodeList;
	private final Vec3 scale;
	private final Sequence anim;
	private final HashMap<AnimatedNode, Entry<Vec3>> nodeToOrgScale;
	private final RenderModel editorRenderModel;
	private final Mat4 invRotMat = new Mat4();
	private final Mat4 rotMat = new Mat4();

	public ScalingKeyframeAction(UndoAction addingTimelinesOrKeyframesAction,
	                             Collection<IdObject> nodeSelection,
	                             Vec3 center, Vec3 scale, RenderModel editorRenderModel, Mat4 rotMat) {
		this.addingTimelinesOrKeyframesAction = addingTimelinesOrKeyframesAction;
		this.trackTime = editorRenderModel.getTimeEnvironment().getEnvTrackTime();
		this.anim = editorRenderModel.getTimeEnvironment().getCurrentSequence();
		nodeList = new ArrayList<>(nodeSelection);
		this.editorRenderModel = editorRenderModel;

		this.scale = new Vec3(scale);
		this.rotMat.set(rotMat);
		this.invRotMat.set(rotMat).invert();
		nodeToOrgScale = new HashMap<>();
		for (AnimatedNode node : nodeSelection) {
			Vec3AnimFlag animFlag = (Vec3AnimFlag) node.find(MdlUtils.TOKEN_SCALING);
			if (animFlag != null && (anim instanceof GlobalSeq || animFlag.getGlobalSeq() != anim)) {
				nodeToOrgScale.put(node, animFlag.getEntryAt(anim, trackTime).deepCopy());
			}
		}
	}

	@Override
	public ScalingKeyframeAction undo() {
//		Vec3 tempInverse = new Vec3(1, 1, 1).divide(scale);
//		for (IdObject node : nodeList) {
//			updateScalingKeyframe(node, trackTime, tempInverse);
//		}
		for (AnimatedNode node : nodeToOrgScale.keySet()) {
			Entry<Vec3> orgScale = nodeToOrgScale.get(node);
			if(orgScale != null){
				Vec3AnimFlag animFlag = (Vec3AnimFlag) node.find(MdlUtils.TOKEN_SCALING);
				animFlag.getEntryAt(anim, orgScale.getTime()).setValues(orgScale);
			}
		}
		if(addingTimelinesOrKeyframesAction != null){
			addingTimelinesOrKeyframesAction.undo();
		}
		return this;
	}

	@Override
	public ScalingKeyframeAction redo() {
		if(addingTimelinesOrKeyframesAction != null){
			addingTimelinesOrKeyframesAction.redo();
		}
		for (IdObject node : nodeList) {
			updateScalingKeyframe(node, trackTime, scale);
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Edit Scaling";
	}

	public ScalingKeyframeAction doSetup(){
		if(addingTimelinesOrKeyframesAction != null){
			addingTimelinesOrKeyframesAction.redo();
		}
		return this;
	}

	@Override
	public ScalingKeyframeAction updateScale(Vec3 scale) {
		this.scale.multiply(scale);
		for (IdObject node : nodeList) {
			updateScalingKeyframe(node, trackTime, scale);
		}
		return this;
	}
	@Override
	public ScalingKeyframeAction setScale(Vec3 scale) {
		scaleDiff.set(scale).divide(this.scale);
		this.scale.set(scale);
		for (IdObject node : nodeList) {
			updateScalingKeyframe(node, trackTime, scaleDiff);
		}
		return this;
	}

	Vec3 scaleDiff = new Vec3();
	public void updateScalingKeyframe(AnimatedNode animatedNode, int trackTime, Vec3 scale) {
		Vec3AnimFlag animFlag = (Vec3AnimFlag) animatedNode.find(MdlUtils.TOKEN_SCALING);
		if (animFlag == null || anim instanceof GlobalSeq && animFlag.getGlobalSeq() != anim) {
			return;
		}

		if (animFlag.hasEntryAt(anim, trackTime)) {
			Entry<Vec3> entry = animFlag.getEntryAt(anim, trackTime);
			entry.getValue().multiply(scale);

			if (animFlag.tans()) {
				entry.getInTan().multiply(scale);
				entry.getOutTan().multiply(scale);
			}
		}
	}


//	// I had some idea of being able to move nodes apart by scaling or something...
//	private Vec3 center;
//	private RenderModel editorRenderModel;
//	private HashMap<IdObject, Vec3> nodeToLocalScale;
//	private HashMap<IdObject, Vec3> nodeToLocalTranslation;
//	private boolean individualOrigens = true;
//	public ScalingKeyframeAction(UndoAction addingTimelinesOrKeyframesAction,
//	                             Collection<IdObject> nodeSelection,
//	                             Vec3 center, Vec3 scale, RenderModel editorRenderModel, boolean individualOrigens) {
//		this.addingTimelinesOrKeyframesAction = addingTimelinesOrKeyframesAction;
//		this.editorRenderModel = editorRenderModel;
//		this.individualOrigens = individualOrigens;
//		this.trackTime = editorRenderModel.getTimeEnvironment().getEnvTrackTime();
//		this.anim = editorRenderModel.getTimeEnvironment().getCurrentSequence();
//		nodeList = new ArrayList<>(nodeSelection);
//		nodeToLocalScale = new HashMap<>();
//		nodeToLocalTranslation = new HashMap<>();
//
//		Vec4 translationHeap = new Vec4();
//		Vec3 translationHeap2 = new Vec3();
//		Vec3 delta = new Vec3();
//		Vec4 delta2 = new Vec4();
//		for (IdObject idObject : nodeSelection) {
//
//			if(!individualOrigens){
//				nodeToLocalTranslation.put(idObject, new Vec3());
//				delta.set(editorRenderModel.getRenderNode(idObject).getRenderPivot());
//				delta.scale(center, scale).sub(editorRenderModel.getRenderNode(idObject).getRenderPivot());
//				setTranslationHeap(idObject, delta, translationHeap);
//				translationHeap2.set(translationHeap);
//				nodeToLocalTranslation.get(idObject).scale(center, scale);
//			}
////			delta2.set(scale, 1);
////			setScalingHeap(idObject, delta2, translationHeap);
////			delta.set(translationHeap);
////			nodeToLocalScale.put(idObject, new Vec3(1,1,1).multiply(delta));
//			nodeToLocalScale.put(idObject, new Vec3(1,1,1).multiply(scale));
//		}
//		this.center = new Vec3(center);
//		this.scale = new Vec3(scale);
//	}
//
//
//	public ScalingKeyframeAction undo2() {
//		Vec3 tempInverse = new Vec3(1, 1, 1).divide(scale);
//		for (IdObject node : nodeToLocalScale.keySet()) {
//			tempInverse.set(1, 1, 1).divide(nodeToLocalScale.get(node));
//			updateScalingKeyframe(node, trackTime, tempInverse);
//		}
//		Vec3 localTranslation = new Vec3();
//		for (IdObject node : nodeToLocalTranslation.keySet()) {
//			localTranslation.set(nodeToLocalTranslation.get(node)).scale( -1);
//			updateTranslationKeyframe(node, localTranslation);
//		}
//		addingTimelinesOrKeyframesAction.undo();
//		return this;
//	}
//
//
//	public ScalingKeyframeAction redo2() {
//		addingTimelinesOrKeyframesAction.redo();
//		for (IdObject node : nodeToLocalScale.keySet()) {
//			Vec3 localScale = nodeToLocalScale.get(node);
//			updateScalingKeyframe(node, trackTime, localScale);
//		}
//		for (IdObject node : nodeToLocalTranslation.keySet()) {
//			Vec3 localTranslation = nodeToLocalTranslation.get(node);
//			updateTranslationKeyframe(node, localTranslation);
//		}
//		return this;
//	}
//
//
//	public ScalingKeyframeAction updateScale2(Vec3 scale) {
//		Vec4 translationHeap = new Vec4();
//		Vec3 translationHeap2 = new Vec3();
//		Vec3 delta = new Vec3();
//		Vec4 delta2 = new Vec4();
//		for (IdObject idObject : nodeToLocalScale.keySet()) {
//			if(nodeToLocalTranslation.get(idObject) != null) {
//				delta.set(editorRenderModel.getRenderNode(idObject).getRenderPivot());
//				delta.scale(center, scale).sub(editorRenderModel.getRenderNode(idObject).getRenderPivot());
//				setTranslationHeap(idObject, delta, translationHeap);
//				translationHeap2.set(translationHeap);
//				updateTranslationKeyframe(idObject, translationHeap2);
//				nodeToLocalTranslation.get(idObject).scale(center, scale);
//			}
////			delta2.set(scale, 1);
////			setScalingHeap(idObject, delta2, translationHeap);
////			delta.set(delta2);
//			if(nodeToLocalScale.get(idObject) != null) {
//				nodeToLocalScale.get(idObject).multiply(scale);
////				nodeToLocalScale.get(idObject).multiply(delta);
//			}
//
////			updateScalingKeyframe(idObject, trackTime, scale);
//			updateScalingKeyframe(idObject, trackTime, delta);
//		}
//		return this;
//	}
//
//	public void updateTranslationKeyframe(AnimatedNode animatedNode, Vec3 translationHeap) {
//		// TODO global seqs, needs separate check on AnimRendEnv, and also we must  make AnimFlag.find seek on globalSeqId
//		Vec3AnimFlag animFlag = (Vec3AnimFlag) animatedNode.find(MdlUtils.TOKEN_TRANSLATION);
//		if (animFlag == null || anim instanceof GlobalSeq && animFlag.getGlobalSeq() != anim) {
//			return;
//		}
//
//		if (animFlag.hasEntryAt(anim, trackTime)) {
//			Entry<Vec3> entry = animFlag.getEntryAt(anim, trackTime);
//			entry.getValue().add(translationHeap);
//
//			if (animFlag.tans()) {
//				entry.getInTan().add(translationHeap);
//				entry.getOutTan().add(translationHeap);
//			}
//		}
//	}
//
//	private Vec4 setTranslationHeap(IdObject idObject, Vec3 newDelta, Vec4 translationHeap) {
//		Mat4 worldMatrix = editorRenderModel.getRenderNode(idObject).getParentWorldMatrix();
//		translationHeap.set(0, 0, 0, 1);
//		translationHeap.transform(worldMatrix);
//		translationHeap.add(newDelta);
//		translationHeap.transformInverted(worldMatrix);
//
//		return translationHeap;
//	}
//
//	private Vec4 setScalingHeap(IdObject idObject, Vec4 newScale, Vec4 scalingHeap) {
//		Mat4 worldMatrix = editorRenderModel.getRenderNode(idObject).getParentWorldMatrix();
//		scalingHeap.set(1, 1, 1, 1);
//		scalingHeap.transform(worldMatrix);
//		scalingHeap.multiply(newScale);
//		scalingHeap.transformInverted(worldMatrix);
//
//		return scalingHeap;
//	}
}
