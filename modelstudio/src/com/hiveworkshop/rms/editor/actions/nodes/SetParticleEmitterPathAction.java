package com.hiveworkshop.rms.editor.actions.nodes;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.ParticleEmitter;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

public class SetParticleEmitterPathAction implements UndoAction {
	private final ParticleEmitter particleEmitter;
	private final String prevPath;
	private final String newPath;
	private final ModelStructureChangeListener changeListener;

	public SetParticleEmitterPathAction(ParticleEmitter particleEmitter, String newPath, ModelStructureChangeListener changeListener) {
		this.particleEmitter = particleEmitter;
		this.prevPath = particleEmitter.getPath();
		this.newPath = newPath;
		this.changeListener = changeListener;
	}

	@Override
	public UndoAction undo() {
		particleEmitter.setPath(prevPath);
		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		particleEmitter.setPath(newPath);
		if (changeListener != null) {
			changeListener.nodesUpdated();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "change texture Path";
	}
}
