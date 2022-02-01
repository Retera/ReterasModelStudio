package com.hiveworkshop.rms.editor.actions.model.bitmap;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.model.material.SetLayerTextureAction;
import com.hiveworkshop.rms.editor.actions.nodes.ChangeParticleTextureAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

import java.util.ArrayList;
import java.util.List;

public class RemoveBitmapAction implements UndoAction {
	private final Bitmap bitmap;
	private final EditableModel model;
	private final ModelStructureChangeListener changeListener;
	List<UndoAction> undoActions;

	public RemoveBitmapAction(Bitmap bitmap, EditableModel model, ModelStructureChangeListener changeListener) {
		this.bitmap = bitmap;
		this.model = model;
		this.changeListener = changeListener;
		undoActions = getRemoveActions(bitmap);
	}

	@Override
	public UndoAction undo() {
		model.add(bitmap);
		for (UndoAction undoAction : undoActions){
			undoAction.undo();
		}
		if (changeListener != null) {
			changeListener.texturesChanged();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		model.remove(bitmap);
		for (UndoAction undoAction : undoActions){
			undoAction.redo();
		}
		if (changeListener != null) {
			changeListener.texturesChanged();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Removed Texture";
	}


	public List<UndoAction> getRemoveActions(Bitmap texture) {
		ArrayList<UndoAction> undoActions = new ArrayList<>();
		// remove a texture, replacing with "Textures\\white.blp" if necessary.
		Bitmap replacement = null;
		if(model.getTextures().size() > 1){
			for (int i = 0; i < model.getTextures().size(); i++){
				Bitmap b = model.getTextures().get(i);
				if (b != texture){
					replacement = b;
					break;
				}
			}
		}
		if (replacement == null){
			replacement = new Bitmap("Textures\\white.blp");
			undoActions.add(new AddBitmapAction(replacement, model, null));
		}
		for (Material material : model.getMaterials()) {
			for (Layer layer : material.getLayers()) {
				undoActions.add(new SetLayerTextureAction(texture, replacement, layer, null));
			}
		}
		for (final ParticleEmitter2 emitter : model.getParticleEmitter2s()) {
			if (emitter.getTexture().equals(texture)) {
				undoActions.add(new ChangeParticleTextureAction(emitter, replacement, changeListener));
			}
		}
		return undoActions;
	}
}
