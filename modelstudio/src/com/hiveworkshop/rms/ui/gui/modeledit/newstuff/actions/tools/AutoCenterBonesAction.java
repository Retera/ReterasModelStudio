package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.tools;

import java.util.HashMap;
import java.util.Map;

import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.util.Vertex;

public final class AutoCenterBonesAction implements UndoAction {
	private final Map<Bone, Vertex> boneToOldPosition;
	private final Map<Bone, Vertex> boneToNewPosition;

	public AutoCenterBonesAction(final Map<Bone, Vertex> boneToOldPosition) {
		this.boneToOldPosition = boneToOldPosition;
		boneToNewPosition = new HashMap<>();
		for (final Bone bone : boneToOldPosition.keySet()) {
			boneToNewPosition.put(bone, new Vertex(bone.getPivotPoint()));
		}
	}

	@Override
	public void undo() {
		for (final Map.Entry<Bone, Vertex> entry : boneToOldPosition.entrySet()) {
			final Bone bone = entry.getKey();
			bone.getPivotPoint().set(entry.getValue());
		}
	}

	@Override
	public void redo() {
		for (final Map.Entry<Bone, Vertex> entry : boneToNewPosition.entrySet()) {
			final Bone bone = entry.getKey();
			bone.getPivotPoint().set(entry.getValue());
		}
	}

	@Override
	public String actionName() {
		return "auto-center bones";
	}

}
