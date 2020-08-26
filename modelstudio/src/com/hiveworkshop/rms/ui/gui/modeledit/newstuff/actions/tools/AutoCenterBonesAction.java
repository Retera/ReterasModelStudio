package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.tools;

import java.util.HashMap;
import java.util.Map;

import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.util.Vector3;

public final class AutoCenterBonesAction implements UndoAction {
	private final Map<Bone, Vector3> boneToOldPosition;
	private final Map<Bone, Vector3> boneToNewPosition;

	public AutoCenterBonesAction(final Map<Bone, Vector3> boneToOldPosition) {
		this.boneToOldPosition = boneToOldPosition;
		boneToNewPosition = new HashMap<>();
		for (final Bone bone : boneToOldPosition.keySet()) {
			boneToNewPosition.put(bone, new Vector3(bone.getPivotPoint()));
		}
	}

	@Override
	public void undo() {
		for (final Map.Entry<Bone, Vector3> entry : boneToOldPosition.entrySet()) {
			final Bone bone = entry.getKey();
			bone.getPivotPoint().set(entry.getValue());
		}
	}

	@Override
	public void redo() {
		for (final Map.Entry<Bone, Vector3> entry : boneToNewPosition.entrySet()) {
			final Bone bone = entry.getKey();
			bone.getPivotPoint().set(entry.getValue());
		}
	}

	@Override
	public String actionName() {
		return "auto-center bones";
	}

}
