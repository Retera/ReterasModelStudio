package com.hiveworkshop.rms.editor.actions.addactions;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.TextureAnim;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class AddTextureAnimAction implements UndoAction {
	private final EditableModel model;
	private final TextureAnim textureAnim;
	private final ModelStructureChangeListener changeListener;

	public AddTextureAnimAction(EditableModel model, AnimFlag<?> animFlag, ModelStructureChangeListener changeListener) {
		this.model = model;
		if(animFlag == null){
			animFlag = new Vec3AnimFlag(MdlUtils.TOKEN_TRANSLATION);
		}
		textureAnim = new TextureAnim(animFlag);

		this.changeListener = changeListener;
	}

	@Override
	public UndoAction undo() {
		model.remove(textureAnim);
		if (changeListener != null) {
			changeListener.animationParamsChanged();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		model.add(textureAnim);
		if (changeListener != null) {
			changeListener.animationParamsChanged();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Add TextureAnim";
	}
}
