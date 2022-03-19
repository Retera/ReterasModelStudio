package com.hiveworkshop.rms.editor.actions.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.util.GenericMoveAction;
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
import com.hiveworkshop.rms.util.Vec4;

import java.util.Collection;
import java.util.HashMap;

public class TranslationKeyframeAction implements GenericMoveAction {
	private final UndoAction addingTimelinesOrKeyframesAction;
	private final int trackTime;
	private final Sequence anim;
	private final HashMap<IdObject, Vec3> nodeToLocalTranslation;
	private final RenderModel editorRenderModel;

	public TranslationKeyframeAction(UndoAction addingTimelinesOrKeyframesAction,
	                                 Collection<IdObject> nodeSelection,
	                                 RenderModel editorRenderModel) {
		this.addingTimelinesOrKeyframesAction = addingTimelinesOrKeyframesAction;
		this.editorRenderModel = editorRenderModel;
		this.trackTime = editorRenderModel.getTimeEnvironment().getEnvTrackTime();
		this.anim = editorRenderModel.getTimeEnvironment().getCurrentSequence();

		nodeToLocalTranslation = new HashMap<>();
		for (IdObject node : nodeSelection) {
			nodeToLocalTranslation.put(node, new Vec3());
		}
	}

	public TranslationKeyframeAction(UndoAction addingTimelinesOrKeyframesAction,
	                                 Collection<IdObject> nodeSelection,
	                                 RenderModel editorRenderModel, Vec3 translation) {
		this(addingTimelinesOrKeyframesAction, nodeSelection, editorRenderModel);

		Vec4 translationHeap = new Vec4();
		Vec3 translationHeap2 = new Vec3();
		for (IdObject idObject : nodeToLocalTranslation.keySet()) {
			if (nodeToLocalTranslation.get(idObject) != null) {
				setTranslationHeap(idObject, translation, translationHeap);
				translationHeap2.set(translationHeap);
				nodeToLocalTranslation.get(idObject).add(translationHeap2);
			}
		}
	}

	@Override
	public UndoAction undo() {
		Vec3 localTranslation = new Vec3();
		for (IdObject node : nodeToLocalTranslation.keySet()) {
			localTranslation.set(nodeToLocalTranslation.get(node)).scale( -1);
			updateLocalTranslationKeyframe(node, localTranslation);
		}
		addingTimelinesOrKeyframesAction.undo();
		return this;
	}

	@Override
	public UndoAction redo() {
		addingTimelinesOrKeyframesAction.redo();
		for (IdObject node : nodeToLocalTranslation.keySet()) {
			Vec3 localTranslation = nodeToLocalTranslation.get(node);
			updateLocalTranslationKeyframe(node, localTranslation);
		}
		return this;
	}

	@Override
	public String actionName() {
		return "edit translation";
	}


	public GenericMoveAction doSetup(){
		addingTimelinesOrKeyframesAction.redo();
		return this;
	}

	@Override
	public void updateTranslation(double deltaX, double deltaY, double deltaZ) {
		Vec3 delta = new Vec3(deltaX, deltaY, deltaZ);
		Vec4 translationHeap = new Vec4();
		Vec3 translationHeap2 = new Vec3();
		for (IdObject idObject : nodeToLocalTranslation.keySet()) {
			setTranslationHeap(idObject, delta, translationHeap);
			translationHeap2.set(translationHeap);

			if (nodeToLocalTranslation.get(idObject) != null) {
				nodeToLocalTranslation.get(idObject).add(translationHeap2);
			}
			updateLocalTranslationKeyframe(idObject, translationHeap2);
		}
	}
	@Override
	public GenericMoveAction updateTranslation(Vec3 delta) {
		Vec4 translationHeap = new Vec4();
		Vec3 translationHeap2 = new Vec3();
		for (IdObject idObject : nodeToLocalTranslation.keySet()) {
			setTranslationHeap(idObject, delta, translationHeap);
			translationHeap2.set(translationHeap);

			if (nodeToLocalTranslation.get(idObject) != null) {
				nodeToLocalTranslation.get(idObject).add(translationHeap2);
			}
			updateLocalTranslationKeyframe(idObject, translationHeap2);
		}
		return this;
	}

	private Vec4 setTranslationHeap(IdObject idObject, Vec3 newDelta, Vec4 translationHeap) {
		Mat4 worldMatrix = editorRenderModel.getRenderNode(idObject).getParentWorldMatrix();
		translationHeap.set(0, 0, 0, 1);
		translationHeap.transform(worldMatrix);
		translationHeap.add(newDelta);
		translationHeap.transformInverted(worldMatrix);

		return translationHeap;
	}

	public void updateLocalTranslationKeyframe(AnimatedNode animatedNode, Vec3 localTranslation) {
		Vec3AnimFlag animFlag = (Vec3AnimFlag) animatedNode.find(MdlUtils.TOKEN_TRANSLATION);
		if (animFlag == null || anim instanceof GlobalSeq && animFlag.getGlobalSeq() != anim) {
			return;
		}

		if (animFlag.hasEntryAt(anim, trackTime)) {
			Entry<Vec3> entry = animFlag.getEntryAt(anim, trackTime);
			entry.getValue().add(localTranslation);

			if (animFlag.tans()) {
				entry.getInTan().add(localTranslation);
				entry.getOutTan().add(localTranslation);
			}
		}
	}

}
