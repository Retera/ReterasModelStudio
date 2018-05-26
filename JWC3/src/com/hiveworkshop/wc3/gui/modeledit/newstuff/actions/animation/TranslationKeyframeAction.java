package com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.animation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.util.vector.Vector3f;

import com.hiveworkshop.wc3.gui.animedit.NodeAnimationModelEditor;
import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.util.GenericMoveAction;
import com.hiveworkshop.wc3.mdl.IdObject;

public class TranslationKeyframeAction implements GenericMoveAction {
	private final UndoAction addingTimelinesOrKeyframesAction;
	private final int trackTime;
	private final HashMap<IdObject, Vector3f> nodeToLocalTranslation;
	private final NodeAnimationModelEditor modelEditor;

	public TranslationKeyframeAction(final UndoAction addingTimelinesOrKeyframesAction, final int trackTime,
			final Collection<IdObject> nodeSelection, final NodeAnimationModelEditor modelEditor) {
		this.addingTimelinesOrKeyframesAction = addingTimelinesOrKeyframesAction;
		this.trackTime = trackTime;
		this.modelEditor = modelEditor;
		nodeToLocalTranslation = new HashMap<>();
		for (final IdObject node : nodeSelection) {
			nodeToLocalTranslation.put(node, new Vector3f());
		}
	}

	@Override
	public void undo() {
		for (final Map.Entry<IdObject, Vector3f> nodeAndLocalTranslation : nodeToLocalTranslation.entrySet()) {
			final IdObject node = nodeAndLocalTranslation.getKey();
			final Vector3f localTranslation = nodeAndLocalTranslation.getValue();
			node.updateLocalTranslationKeyframe(trackTime, -localTranslation.x, -localTranslation.y,
					-localTranslation.z);
		}
		addingTimelinesOrKeyframesAction.undo();
	}

	@Override
	public void redo() {
		addingTimelinesOrKeyframesAction.redo();
		for (final Map.Entry<IdObject, Vector3f> nodeAndLocalTranslation : nodeToLocalTranslation.entrySet()) {
			final IdObject node = nodeAndLocalTranslation.getKey();
			final Vector3f localTranslation = nodeAndLocalTranslation.getValue();
			node.updateLocalTranslationKeyframe(trackTime, localTranslation.x, localTranslation.y, localTranslation.z);
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
