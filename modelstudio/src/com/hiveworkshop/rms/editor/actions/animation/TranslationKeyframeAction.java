package com.hiveworkshop.rms.editor.actions.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.editor.AbstractTransformAction;
import com.hiveworkshop.rms.editor.model.AnimatedNode;
import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.CameraNode;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.render3d.RenderNode;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec3;

import java.util.Collection;
import java.util.HashMap;

public class TranslationKeyframeAction extends AbstractTransformAction {
	private final UndoAction addingTimelinesOrKeyframesAction;
	private final int trackTime;
	private final Sequence anim;
	private final HashMap<AnimatedNode, Vec3> nodeToLocalTranslation;
	private final HashMap<AnimatedNode, Entry<Vec3>> nodeToOrgTranslation;
	private final RenderModel editorRenderModel;
	private final Mat4 invRotMat = new Mat4();
	private final Mat4 rotMat = new Mat4();
	private final boolean worldSpace;
	private final Vec3 totTranslate = new Vec3();
	private final Vec3 deltaTranslate = new Vec3();

	public TranslationKeyframeAction(UndoAction addingTimelinesOrKeyframesAction,
	                                 Collection<IdObject> nodeSelection,
	                                 Collection<CameraNode> camSelection,
	                                 RenderModel editorRenderModel,
	                                 Vec3 translation,
	                                 Mat4 rotMat) {
		this(addingTimelinesOrKeyframesAction, nodeSelection, camSelection, editorRenderModel, translation, true, rotMat);
	}

	public TranslationKeyframeAction(UndoAction addingTimelinesOrKeyframesAction,
	                                 Collection<IdObject> nodeSelection,
	                                 Collection<CameraNode> camSelection,
	                                 RenderModel editorRenderModel,
	                                 Vec3 translation, boolean worldSpace,
	                                 Mat4 rotMat) {
		this.editorRenderModel = editorRenderModel;
		this.trackTime = editorRenderModel.getTimeEnvironment().getEnvTrackTime();
		this.anim = editorRenderModel.getTimeEnvironment().getCurrentSequence();
		this.addingTimelinesOrKeyframesAction = addingTimelinesOrKeyframesAction;
		this.worldSpace = worldSpace;

		nodeToLocalTranslation = new HashMap<>();
		nodeToOrgTranslation = new HashMap<>();
		for (AnimatedNode node : nodeSelection) {
			AnimFlag<Vec3> translationFlag = node.getTranslationFlag();
			if (translationFlag != null && translationFlag.hasEntryAt(anim, trackTime)) {
				nodeToOrgTranslation.put(node, translationFlag.getEntryAt(anim, trackTime).deepCopy());
			}
			nodeToLocalTranslation.put(node, new Vec3());
		}
		for (AnimatedNode node : camSelection) {
			AnimFlag<Vec3> translationFlag = node.getTranslationFlag();
			if (translationFlag != null && translationFlag.hasEntryAt(anim, trackTime)) {
				nodeToOrgTranslation.put(node, translationFlag.getEntryAt(anim, trackTime).deepCopy());
			}
			nodeToLocalTranslation.put(node, new Vec3());
		}
		this.rotMat.set(rotMat);
		this.invRotMat.set(rotMat).invert();

		if (translation != Vec3.ZERO && !translation.equalLocs(Vec3.ZERO)) {
			for (AnimatedNode idObject : nodeToLocalTranslation.keySet()) {
				if (nodeToLocalTranslation.get(idObject) != null) {
					nodeToLocalTranslation.get(idObject).add(getTranslationHeap(idObject, translation));
				}
			}
		}
	}

	@Override
	public TranslationKeyframeAction undo() {
//		Vec3 localTranslation = new Vec3();
//		for (AnimatedNode node : nodeToLocalTranslation.keySet()) {
//			localTranslation.set(nodeToLocalTranslation.get(node)).scale( -1);
//			updateLocalTranslationKeyframe(node, localTranslation);
//		}

		for (AnimatedNode node : nodeToOrgTranslation.keySet()) {
			Entry<Vec3> entry = nodeToOrgTranslation.get(node);
			AnimFlag<Vec3> translationFlag = node.getTranslationFlag();
			if (entry != null && translationFlag != null) {
				translationFlag.setOrAddEntryT(entry.getTime(), entry.deepCopy(), anim);
			}
		}

		if (addingTimelinesOrKeyframesAction != null) {
			addingTimelinesOrKeyframesAction.undo();
		}
		return this;
	}

	@Override
	public TranslationKeyframeAction redo() {
		if (addingTimelinesOrKeyframesAction != null) {
			addingTimelinesOrKeyframesAction.redo();
		}
		for (AnimatedNode node : nodeToLocalTranslation.keySet()) {
			Vec3 localTranslation = nodeToLocalTranslation.get(node);
			updateLocalTranslationKeyframe(node, localTranslation);
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Edit Translation";
	}

	public TranslationKeyframeAction doSetup() {
		if (addingTimelinesOrKeyframesAction != null) {
			addingTimelinesOrKeyframesAction.redo();
		}
		return this;
	}

	@Override
	public TranslationKeyframeAction updateTranslation(Vec3 delta) {
		deltaTranslate.set(delta);
		totTranslate.add(delta);
		for (AnimatedNode idObject : nodeToLocalTranslation.keySet()) {
			Vec3 translationHeap = getTranslationHeap(idObject, delta);

			if (nodeToLocalTranslation.get(idObject) != null) {
				nodeToLocalTranslation.get(idObject).add(translationHeap);
			}
			updateLocalTranslationKeyframe(idObject, translationHeap);
		}
		return this;
	}

	@Override
	public TranslationKeyframeAction setTranslation(Vec3 transl) {
		deltaTranslate.set(transl).sub(totTranslate);
		totTranslate.set(transl);
		for (AnimatedNode idObject : nodeToLocalTranslation.keySet()) {
			Vec3 translationHeap = getTranslationHeap(idObject, deltaTranslate);

			if (nodeToLocalTranslation.get(idObject) != null) {
				nodeToLocalTranslation.get(idObject).add(translationHeap);
			}
			updateLocalTranslationKeyframe(idObject, translationHeap);
		}
		return this;
	}

	private final Vec3 tempVec = new Vec3();
	private final Mat4 tempMat = new Mat4();
	private final Vec3 translationHeap = new Vec3();

	private Vec3 getTranslationHeap(AnimatedNode idObject, Vec3 newDelta) {
		translationHeap.set(0, 0, 0);
		RenderNode<AnimatedNode> renderNode = editorRenderModel.getRenderNode(idObject);
		if (renderNode != null) {
			tempVec.set(renderNode.getPivot())
					.transform(rotMat, 1, true)
					.add(newDelta)
					.transform(invRotMat, 1, true)
					.sub(renderNode.getPivot());

			if (worldSpace) {
				Mat4 worldMatrix = renderNode.getParentWorldMatrix();
				tempMat.set(worldMatrix).invert();
				translationHeap.add(idObject.getPivotPoint());
				translationHeap.transform(worldMatrix, 1, true);
				translationHeap.add(tempVec);
				translationHeap.transform(tempMat, 1, true);
				translationHeap.sub(idObject.getPivotPoint());
			} else {
				translationHeap.add(tempVec);
			}
		}

		return translationHeap;
	}

	private Vec3 getTranslationHeapOrg(AnimatedNode idObject, Vec3 newDelta) {
		translationHeap.set(0, 0, 0);
		RenderNode<AnimatedNode> renderNode = editorRenderModel.getRenderNode(idObject);
		if (renderNode != null) {
			tempVec.set(renderNode.getPivot())
					.transform(rotMat, 1, true)
					.add(newDelta)
					.transform(invRotMat, 1, true)
					.sub(renderNode.getPivot());

			Mat4 worldMatrix = renderNode.getParentWorldMatrix();
			tempMat.set(worldMatrix).invert();
			translationHeap.add(idObject.getPivotPoint());
			translationHeap.transform(worldMatrix, 1, true);
			translationHeap.add(tempVec);
			translationHeap.transform(tempMat, 1, true);
			translationHeap.sub(idObject.getPivotPoint());
		}

		return translationHeap;
	}

	public TranslationKeyframeAction updateLocalTranslationKeyframe(AnimatedNode animatedNode, Vec3 localTranslation) {
		Vec3AnimFlag animFlag = (Vec3AnimFlag) animatedNode.find(MdlUtils.TOKEN_TRANSLATION);
		if (animFlag != null && (anim instanceof Animation || animFlag.getGlobalSeq() == anim)) {
			if (animFlag.hasEntryAt(anim, trackTime)) {
				Entry<Vec3> entry = animFlag.getEntryAt(anim, trackTime);
				entry.getValue().add(localTranslation);

				if (animFlag.tans()) {
					entry.getInTan().add(localTranslation);
					entry.getOutTan().add(localTranslation);
				}
			}
		}

		return this;
	}

}
