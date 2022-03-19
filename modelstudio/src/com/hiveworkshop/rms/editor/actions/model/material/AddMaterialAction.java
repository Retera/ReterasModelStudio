package com.hiveworkshop.rms.editor.actions.model.material;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.model.bitmap.AddBitmapAction;
import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Layer;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AddMaterialAction implements UndoAction {
	private final Material material;
	private final EditableModel model;
	private final ModelStructureChangeListener changeListener;
	private final List<UndoAction> addTextureActions = new ArrayList<>();

	public AddMaterialAction(final Material material,
	                         final EditableModel model,
	                         final ModelStructureChangeListener modelStructureChangeListener) {
		this.material = material;
		this.material.setShaderString(material.getShaderString());
//		this.material.setShaderString(material.getShaderString() + "_copy");
		this.model = model;
		this.changeListener = modelStructureChangeListener;
		Set<Bitmap> bitmaps = new HashSet<>(model.getTextures());
		for (Layer layer : material.getLayers()) {
			Bitmap bitmap = layer.getTextureBitmap();
			if (bitmap != null && !bitmaps.contains(bitmap)) {
				addTextureActions.add(new AddBitmapAction(bitmap, model, null));
			}
			if (layer.getTextures() != null && !layer.getTextures().isEmpty()) {
				for (Bitmap bitmap1 : layer.getTextures()) {
					if (bitmap1 != null && !bitmaps.contains(bitmap1)) {
						addTextureActions.add(new AddBitmapAction(bitmap1, model, null));
					}
				}
			}
		}
	}

	@Override
	public UndoAction undo() {
		model.remove(material);
		if (!addTextureActions.isEmpty()) {
			for (UndoAction action : addTextureActions) {
				action.undo();
			}
		}
		if (changeListener != null) {
			changeListener.materialsListChanged();
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		model.add(material);
		if (!addTextureActions.isEmpty()) {
			for (UndoAction action : addTextureActions) {
				action.redo();
			}
		}
		if (changeListener != null) {
			changeListener.materialsListChanged();
		}
		return this;
	}

	@Override
	public String actionName() {
		return "add Material";
	}

}
