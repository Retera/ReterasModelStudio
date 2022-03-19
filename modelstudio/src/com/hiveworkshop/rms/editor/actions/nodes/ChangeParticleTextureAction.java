package com.hiveworkshop.rms.editor.actions.nodes;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.model.ParticleEmitter2;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class ChangeParticleTextureAction implements UndoAction {
	private final ParticleEmitter2 emitter;
	private final Bitmap oldBitmap;
	private final Bitmap bitmap;
	private final ModelStructureChangeListener changeListener;

	public ChangeParticleTextureAction(ParticleEmitter2 emitter, Bitmap bitmap,
	                                   ModelStructureChangeListener changeListener) {
		this.emitter = emitter;
		this.oldBitmap = emitter.getTexture();
		this.bitmap = bitmap;
		this.changeListener = changeListener;
	}

	@Override
	public UndoAction undo() {
		this.emitter.setTexture(oldBitmap);
		if (changeListener != null) {
			changeListener.nodeHierarchyChanged();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		this.emitter.setTexture(bitmap);
		if (changeListener != null) {
			changeListener.nodeHierarchyChanged();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Changed texture of " + emitter.getName();
	}
}
