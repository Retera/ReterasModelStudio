package com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.animation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.util.vector.Vector3f;

import com.hiveworkshop.wc3.gui.animedit.NodeAnimationModelEditor;
import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.util.GenericScaleAction;
import com.hiveworkshop.wc3.mdl.IdObject;
import com.hiveworkshop.wc3.mdl.Vertex;

public class ScalingKeyframeAction implements GenericScaleAction {
	private final UndoAction addingTimelinesOrKeyframesAction;
	private final int trackTime;
	private final HashMap<IdObject, Vector3f> nodeToLocalScale;
	private final NodeAnimationModelEditor modelEditor;
	private final Vertex center;
	private final Integer trackGlobalSeq;

	public ScalingKeyframeAction(final UndoAction addingTimelinesOrKeyframesAction, final int trackTime,
			final Integer trackGlobalSeq, final Collection<IdObject> nodeSelection,
			final NodeAnimationModelEditor modelEditor, final double centerX, final double centerY,
			final double centerZ) {
		this.addingTimelinesOrKeyframesAction = addingTimelinesOrKeyframesAction;
		this.trackTime = trackTime;
		this.trackGlobalSeq = trackGlobalSeq;
		this.modelEditor = modelEditor;
		nodeToLocalScale = new HashMap<>();
		for (final IdObject node : nodeSelection) {
			nodeToLocalScale.put(node, new Vector3f(1, 1, 1));
		}
		center = new Vertex(centerX, centerY, centerZ);
	}

	@Override
	public void undo() {
		final Vector3f tempInverse = new Vector3f();
		for (final Map.Entry<IdObject, Vector3f> nodeAndLocalTranslation : nodeToLocalScale.entrySet()) {
			final IdObject node = nodeAndLocalTranslation.getKey();
			final Vector3f localTranslation = nodeAndLocalTranslation.getValue();
			tempInverse.x = 1 / localTranslation.x;
			tempInverse.y = 1 / localTranslation.y;
			tempInverse.z = 1 / localTranslation.z;
			node.updateLocalScalingKeyframe(trackTime, trackGlobalSeq, tempInverse);
		}
		addingTimelinesOrKeyframesAction.undo();
	}

	@Override
	public void redo() {
		addingTimelinesOrKeyframesAction.redo();
		for (final Map.Entry<IdObject, Vector3f> nodeAndLocalTranslation : nodeToLocalScale.entrySet()) {
			final IdObject node = nodeAndLocalTranslation.getKey();
			final Vector3f localTranslation = nodeAndLocalTranslation.getValue();
			node.updateLocalScalingKeyframe(trackTime, trackGlobalSeq, localTranslation);
		}
	}

	@Override
	public String actionName() {
		return "edit rotation";
	}

	@Override
	public void updateScale(final double scaleX, final double scaleY, final double scaleZ) {
		modelEditor.rawScale(center.x, center.y, center.z, scaleX, scaleY, scaleZ, nodeToLocalScale);
	}

}
