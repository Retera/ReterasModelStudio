package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.animation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.ui.application.edit.animation.NodeAnimationModelEditor;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericRotateAction;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;

public class SquatToolKeyframeAction implements GenericRotateAction {
	private final UndoAction addingTimelinesOrKeyframesAction;
	private final int trackTime;
	private final HashMap<IdObject, Quat> nodeToLocalRotation;
	private final NodeAnimationModelEditor modelEditor;
	private final Vec3 center;
	private final byte dim1;
	private final byte dim2;
	private final Integer trackGlobalSeq;

	public SquatToolKeyframeAction(final UndoAction addingTimelinesOrKeyframesAction, final int trackTime,
			final Integer trackGlobalSeq, final Collection<IdObject> nodeSelection,
			final NodeAnimationModelEditor modelEditor, final double centerX, final double centerY,
			final double centerZ, final byte dim1, final byte dim2) {
		this.addingTimelinesOrKeyframesAction = addingTimelinesOrKeyframesAction;
		this.trackTime = trackTime;
		this.trackGlobalSeq = trackGlobalSeq;
		this.modelEditor = modelEditor;
		this.dim1 = dim1;
		this.dim2 = dim2;
		nodeToLocalRotation = new HashMap<>();
		for (final IdObject node : nodeSelection) {
			nodeToLocalRotation.put(node, new Quat());
		}
		center = new Vec3(centerX, centerY, centerZ);
	}

	@Override
	public void undo() {
		for (final Map.Entry<IdObject, Quat> nodeAndLocalTranslation : nodeToLocalRotation.entrySet()) {
			final IdObject node = nodeAndLocalTranslation.getKey();
			final Quat localTranslation = nodeAndLocalTranslation.getValue();
			node.updateLocalRotationKeyframeInverse(trackTime, trackGlobalSeq, localTranslation);
		}
		addingTimelinesOrKeyframesAction.undo();
	}

	@Override
	public void redo() {
		addingTimelinesOrKeyframesAction.redo();
		for (final Map.Entry<IdObject, Quat> nodeAndLocalTranslation : nodeToLocalRotation.entrySet()) {
			final IdObject node = nodeAndLocalTranslation.getKey();
			final Quat localTranslation = nodeAndLocalTranslation.getValue();
			node.updateLocalRotationKeyframe(trackTime, trackGlobalSeq, localTranslation);
		}
	}

	@Override
	public String actionName() {
		return "edit rotation w/ squat";
	}

	@Override
	public void updateRotation(final double radians) {
		modelEditor.rawSquatToolRotate2d(center.x, center.y, center.z, radians, dim1, dim2, nodeToLocalRotation);
	}

}
