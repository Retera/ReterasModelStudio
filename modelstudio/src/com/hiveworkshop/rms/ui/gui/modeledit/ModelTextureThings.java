package com.hiveworkshop.rms.ui.gui.modeledit;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.ui.application.model.editors.TextureTableCellRenderer;

public class ModelTextureThings {
	private static EditableModel model;
	private static TextureListRenderer textureListRenderer;
	private static TextureTableCellRenderer textureTableCellRenderer;

	public static void setModel(EditableModel model) {
		ModelTextureThings.model = model;
		textureListRenderer = new TextureListRenderer(model);
		textureTableCellRenderer = new TextureTableCellRenderer(model);
	}


	public static TextureListRenderer getTextureListRenderer() {
		return textureListRenderer;
	}
	public static TextureTableCellRenderer getTextureTableCellRenderer() {
		return textureTableCellRenderer;
	}
}
