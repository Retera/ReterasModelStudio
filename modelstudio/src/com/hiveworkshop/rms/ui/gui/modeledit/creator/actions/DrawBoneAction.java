package com.hiveworkshop.rms.ui.gui.modeledit.creator.actions;

import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;

import java.util.ArrayList;
import java.util.List;

public class DrawBoneAction implements UndoAction {
	private final ModelView modelView;
	private final Bone bone;
	private final ModelStructureChangeListener modelStructureChangeListener;
	private final List<IdObject> boneAsList;

	public DrawBoneAction(final ModelView modelView, final ModelStructureChangeListener modelStructureChangeListener,
			final Bone bone) {
		this.modelView = modelView;
		this.bone = bone;
		this.modelStructureChangeListener = modelStructureChangeListener;
		boneAsList = new ArrayList<>();
		boneAsList.add(bone);
	}

	@Override
	public UndoAction undo() {
		modelView.getModel().remove(bone);
		modelStructureChangeListener.nodesUpdated();
		return this;
	}

	@Override
	public UndoAction redo() {
		modelView.getModel().add(bone);
		modelStructureChangeListener.nodesUpdated();
		return this;
	}

	@Override
	public String actionName() {
		return "add " + bone.getName();
	}

}
