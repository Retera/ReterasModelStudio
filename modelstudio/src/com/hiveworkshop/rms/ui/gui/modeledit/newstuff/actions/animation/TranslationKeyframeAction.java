package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.animation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.Vertex;
import com.hiveworkshop.rms.ui.application.edit.animation.NodeAnimationModelEditor;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericMoveAction;

public class TranslationKeyframeAction implements GenericMoveAction {
	private final UndoAction addingTimelinesOrKeyframesAction;
	private final int trackTime;
	private final HashMap<IdObject, Vertex> nodeToLocalTranslation;
	private final NodeAnimationModelEditor modelEditor;
	private final Integer trackGlobalSeq;

	public TranslationKeyframeAction(final UndoAction addingTimelinesOrKeyframesAction, final int trackTime,
			final Integer trackGlobalSeq, final Collection<IdObject> nodeSelection,
			final NodeAnimationModelEditor modelEditor) {
		this.addingTimelinesOrKeyframesAction = addingTimelinesOrKeyframesAction;
		this.trackTime = trackTime;
		this.trackGlobalSeq = trackGlobalSeq;
		this.modelEditor = modelEditor;
		nodeToLocalTranslation = new HashMap<>();
		for (final IdObject node : nodeSelection) {
			nodeToLocalTranslation.put(node, new Vertex());
		}
	}

	@Override
	public void undo() {
		for (final Map.Entry<IdObject, Vertex> nodeAndLocalTranslation : nodeToLocalTranslation.entrySet()) {
			final IdObject node = nodeAndLocalTranslation.getKey();
			final Vertex localTranslation = nodeAndLocalTranslation.getValue();
			node.updateLocalTranslationKeyframe(trackTime, trackGlobalSeq, -localTranslation.x, -localTranslation.y,
					-localTranslation.z);
		}
		addingTimelinesOrKeyframesAction.undo();
	}

	@Override
	public void redo() {
		addingTimelinesOrKeyframesAction.redo();
		for (final Map.Entry<IdObject, Vertex> nodeAndLocalTranslation : nodeToLocalTranslation.entrySet()) {
			final IdObject node = nodeAndLocalTranslation.getKey();
			final Vertex localTranslation = nodeAndLocalTranslation.getValue();
			node.updateLocalTranslationKeyframe(trackTime, trackGlobalSeq, localTranslation.x, localTranslation.y,
					localTranslation.z);
		}
	}

	@Override
	public String actionName() {
		return "edit translation";
	}

	@Override
	public void updateTranslation(final double deltaX, final double deltaY, final double deltaZ) {
		modelEditor.rawTranslate(deltaX, deltaY, deltaZ, nodeToLocalTranslation);
	}

}
