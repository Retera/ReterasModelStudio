package com.hiveworkshop.wc3.gui.modeledit.creator.actions;

import java.util.ArrayList;
import java.util.List;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.mdl.Bone;
import com.hiveworkshop.wc3.mdl.IdObject;
import com.hiveworkshop.wc3.mdl.v2.ModelView;

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
	public void undo() {
		modelView.getModel().remove(bone);
		modelStructureChangeListener.nodesRemoved(boneAsList);
	}

	@Override
	public void redo() {
		modelView.getModel().add(bone);
		modelStructureChangeListener.nodesAdded(boneAsList);
	}

	@Override
	public String actionName() {
		return "add " + bone.getName();
	}

}
