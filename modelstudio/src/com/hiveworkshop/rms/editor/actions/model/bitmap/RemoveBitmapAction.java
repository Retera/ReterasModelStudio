package com.hiveworkshop.rms.editor.actions.model.bitmap;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.SetFlagEntryAction;
import com.hiveworkshop.rms.editor.actions.nodes.ChangeParticleTextureAction;
import com.hiveworkshop.rms.editor.actions.util.ConsumerAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.BitmapAnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;

import java.util.ArrayList;
import java.util.List;

public class RemoveBitmapAction implements UndoAction {
	private final Bitmap bitmapToRemove;
	private final Bitmap replacement;
	private final int index;
	private final EditableModel model;
	private final ModelStructureChangeListener changeListener;
	private final List<UndoAction> undoActions;
	boolean animChanges = false;

	public RemoveBitmapAction(Bitmap bitmapToRemove, EditableModel model, ModelStructureChangeListener changeListener) {
		this(bitmapToRemove, null, model, changeListener);
	}

	public RemoveBitmapAction(Bitmap bitmapToRemove, Bitmap replacement, EditableModel model, ModelStructureChangeListener changeListener) {
		this.bitmapToRemove = bitmapToRemove;
		if(replacement != null) {
			this.replacement = replacement;
		} else {
			this.replacement = getReplacementBitmap(bitmapToRemove);
		}
		this.index = model.getId(bitmapToRemove);
		this.model = model;
		this.changeListener = changeListener;
		undoActions = getRemoveActions(bitmapToRemove);
	}

	@Override
	public UndoAction undo() {
		model.add(bitmapToRemove, index);
		for (UndoAction undoAction : undoActions){
			undoAction.undo();
		}
		if (changeListener != null) {
			changeListener.texturesChanged();
			if(animChanges){
				changeListener.animationParamsChanged();
			}
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		model.remove(bitmapToRemove);
		for (UndoAction undoAction : undoActions){
			undoAction.redo();
		}
		if (changeListener != null) {
			changeListener.texturesChanged();
			if(animChanges){
				changeListener.animationParamsChanged();
			}
		}
		return this;
	}

	@Override
	public String actionName() {
		return "Removed Texture";
	}

	public List<UndoAction> getRemoveActions(Bitmap bitmap) {
		ArrayList<UndoAction> undoActions = new ArrayList<>();
		// remove a texture, replacing with "Textures\\white.blp" if necessary.
		if (!model.contains(this.replacement)) {
			undoActions.add(new AddBitmapAction(replacement, model, null));
		}

		List<Layer.Texture> affectedLayerSlots = new ArrayList<>();
		List<Entry<Bitmap>> tempEntries = new ArrayList<>();
		for (Material material : model.getMaterials()) {
			for (Layer layer : material.getLayers()) {
				for (Layer.Texture texSlot : layer.getTextureSlots()) {
					if (texSlot.getTexture() == bitmap) {
						affectedLayerSlots.add(texSlot);
					}
					if (texSlot.getFlipbookTexture() != null) {
						BitmapAnimFlag flipbookTexture = texSlot.getFlipbookTexture();
						for (Sequence sequence : flipbookTexture.getAnimMap().keySet()) {
							for (Entry<Bitmap> entry : flipbookTexture.getEntryMap(sequence).values()) {
								if (entry.getValue() == bitmap || entry.getInTan() == bitmap || entry.getOutTan() == bitmap) {
									tempEntries.add(entry);
								}
							}
							if (!tempEntries.isEmpty()) {
								List<Entry<Bitmap>> newEntries = new ArrayList<>();
								for (Entry<Bitmap> entry : tempEntries) {
									Entry<Bitmap> copy = entry.deepCopy();
									if (copy.getValue() == bitmap) {
										copy.setValue(replacement);
									}
									if (copy.getInTan() == bitmap) {
										copy.setInTan(replacement);
									}
									if (copy.getOutTan() == bitmap) {
										copy.setOutTan(replacement);
									}
									newEntries.add(copy);
								}
								undoActions.add(new SetFlagEntryAction<>(flipbookTexture, newEntries, sequence, null));
								tempEntries.clear();
								animChanges = true;
							}
						}
					}
				}
			}
		}
		undoActions.add(new ConsumerAction<>(tex -> affectedLayerSlots.forEach(ts -> ts.setTexture(tex)), replacement, bitmap, ""));

		for (final ParticleEmitter2 emitter : model.getParticleEmitter2s()) {
			if (emitter.getTexture().equals(bitmap)) {
				undoActions.add(new ChangeParticleTextureAction(emitter, replacement, changeListener));
			}
		}
		return undoActions;
	}

	private Bitmap getReplacementBitmap(Bitmap texture) {
		for(Bitmap bitmap : model.getTextures()){
			if(bitmap != texture){
				return bitmap;
			}
		}
		return new Bitmap("Textures\\white.blp");
	}
}
