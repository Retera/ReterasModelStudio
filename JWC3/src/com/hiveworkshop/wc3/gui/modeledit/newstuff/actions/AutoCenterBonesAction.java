package com.hiveworkshop.wc3.gui.modeledit.newstuff.actions;

import com.etheller.collections.HashMap;
import com.etheller.collections.Map;
import com.etheller.collections.MapView;
import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.mdl.Bone;
import com.hiveworkshop.wc3.mdl.Vertex;

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
		for (final MapView.Entry<Bone, Vertex> entry : boneToOldPosition) {
			final Bone bone = entry.getKey();
			bone.getPivotPoint().setTo(entry.getValue());
		}
	}

	@Override
	public void redo() {
		for (final MapView.Entry<Bone, Vertex> entry : boneToNewPosition) {
			final Bone bone = entry.getKey();
			bone.getPivotPoint().setTo(entry.getValue());
		}
	}

	@Override
	public String actionName() {
		return "auto-center bones";
	}

}
