package com.hiveworkshop.wc3.gui.modeledit.componenttree.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.mdl.Bitmap;
import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.mdl.IdObject;
import com.hiveworkshop.wc3.mdl.Layer;
import com.hiveworkshop.wc3.mdl.Material;
import com.hiveworkshop.wc3.mdl.ParticleEmitter2;
import com.hiveworkshop.wc3.mdl.ShaderTextureTypeHD;

/**
 * Removes a texture and points every layer slot, animated texture id and
 * particle emitter that used it at a replacement texture instead. Without the
 * replacement the references would keep the old texture alive and it would
 * reappear on save.
 */
public final class RemoveBitmapAction implements UndoAction {
	private final EditableModel model;
	private final Bitmap bitmap;
	private final Bitmap replacement;
	private final ModelStructureChangeListener listener;
	private final List<Runnable> restores = new ArrayList<>();
	private int index = -1;
	private boolean replacementWasAdded;

	public RemoveBitmapAction(final EditableModel model, final Bitmap bitmap, final Bitmap replacement,
			final ModelStructureChangeListener listener) {
		this.model = model;
		this.bitmap = bitmap;
		this.replacement = replacement;
		this.listener = listener;
	}

	/** How many places reference the texture right now. */
	public static int countReferences(final EditableModel model, final Bitmap bitmap) {
		int count = 0;
		for (final Material material : model.getMaterials()) {
			for (final Layer layer : material.getLayers()) {
				for (final ShaderTextureTypeHD slot : ShaderTextureTypeHD.VALUES) {
					if (layer.getShaderTextures().get(slot) == bitmap) {
						count++;
					}
				}
				if (layer.getTextures() != null) {
					for (final Bitmap animated : layer.getTextures()) {
						if (animated == bitmap) {
							count++;
						}
					}
				}
			}
		}
		for (final IdObject node : model.getIdObjects()) {
			if ((node instanceof ParticleEmitter2) && (((ParticleEmitter2) node).getTexture() == bitmap)) {
				count++;
			}
		}
		return count;
	}

	@Override
	public void redo() {
		restores.clear();
		index = ComponentListUtil.removeIdentity(model.getTextures(), bitmap);
		if ((replacement != null) && (ComponentListUtil.indexOfIdentity(model.getTextures(), replacement) < 0)
				&& (countReferences(model, bitmap) > 0)) {
			model.add(replacement);
			replacementWasAdded = true;
		}
		for (final Material material : model.getMaterials()) {
			for (final Layer layer : material.getLayers()) {
				for (final ShaderTextureTypeHD slot : ShaderTextureTypeHD.VALUES) {
					if (layer.getShaderTextures().get(slot) == bitmap) {
						layer.getShaderTextures().put(slot, replacement);
						restores.add(() -> layer.getShaderTextures().put(slot, bitmap));
					}
				}
				if (layer.getTextures() != null) {
					for (int i = 0; i < layer.getTextures().size(); i++) {
						if (layer.getTextures().get(i) == bitmap) {
							final int slotIndex = i;
							layer.getTextures().set(slotIndex, replacement);
							restores.add(() -> layer.getTextures().set(slotIndex, bitmap));
						}
					}
				}
			}
		}
		for (final IdObject node : model.getIdObjects()) {
			if ((node instanceof ParticleEmitter2) && (((ParticleEmitter2) node).getTexture() == bitmap)) {
				final ParticleEmitter2 emitter = (ParticleEmitter2) node;
				emitter.setTexture(replacement);
				restores.add(() -> emitter.setTexture(bitmap));
			}
		}
		listener.texturesChanged();
	}

	@Override
	public void undo() {
		Collections.reverse(restores);
		for (final Runnable restore : restores) {
			restore.run();
		}
		restores.clear();
		if (replacementWasAdded) {
			ComponentListUtil.removeIdentity(model.getTextures(), replacement);
			replacementWasAdded = false;
		}
		ComponentListUtil.insertAt(model.getTextures(), index, bitmap);
		listener.texturesChanged();
	}

	@Override
	public String actionName() {
		return "delete texture";
	}
}
