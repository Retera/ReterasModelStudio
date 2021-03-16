package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.animation;

import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.ui.application.edit.animation.NodeAnimationModelEditor;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericScaleAction;
import com.hiveworkshop.rms.util.Vec3;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ScalingKeyframeAction implements GenericScaleAction {
	private final UndoAction addingTimelinesOrKeyframesAction;
	private final int trackTime;
	private final HashMap<IdObject, Vec3> nodeToLocalScale;
	private final NodeAnimationModelEditor modelEditor;
	private final Vec3 center;
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
			nodeToLocalScale.put(node, new Vec3());
		}
		center = new Vec3(centerX, centerY, centerZ);
	}

	public ScalingKeyframeAction(final UndoAction addingTimelinesOrKeyframesAction, final int trackTime,
	                             final Integer trackGlobalSeq, final Collection<IdObject> nodeSelection,
	                             final NodeAnimationModelEditor modelEditor, final Vec3 center) {
		this.addingTimelinesOrKeyframesAction = addingTimelinesOrKeyframesAction;
		this.trackTime = trackTime;
		this.trackGlobalSeq = trackGlobalSeq;
		this.modelEditor = modelEditor;
		nodeToLocalScale = new HashMap<>();
		for (final IdObject node : nodeSelection) {
			nodeToLocalScale.put(node, new Vec3());
		}
		this.center = new Vec3(center);
	}

	@Override
	public void undo() {
		for (final Map.Entry<IdObject, Vec3> nodeAndLocalTranslation : nodeToLocalScale.entrySet()) {
			final IdObject node = nodeAndLocalTranslation.getKey();
			final Vec3 localTranslation = nodeAndLocalTranslation.getValue();
			final Vec3 tempInverse = new Vec3(1, 1, 1).divide(localTranslation);
			node.updateLocalScalingKeyframe(trackTime, trackGlobalSeq, tempInverse);
		}
		addingTimelinesOrKeyframesAction.undo();
	}

	@Override
	public void redo() {
		addingTimelinesOrKeyframesAction.redo();
		for (final Map.Entry<IdObject, Vec3> nodeAndLocalTranslation : nodeToLocalScale.entrySet()) {
			final IdObject node = nodeAndLocalTranslation.getKey();
			final Vec3 localTranslation = nodeAndLocalTranslation.getValue();
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

	@Override
	public void updateScale(final Vec3 scale) {
		modelEditor.rawScale(center, scale, nodeToLocalScale);
	}

}
