package com.hiveworkshop.rms.ui.gui.modeledit;

import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.ui.application.model.editors.TextureTableCellRenderer;
import com.hiveworkshop.rms.util.IterableListModel;

public class ModelTextureThings {
	private static EditableModel model;
	private static TextureListRenderer textureListRenderer;
	private static TextureTableCellRenderer textureTableCellRenderer;
	private static IterableListModel<Bitmap> bitmapListModel = new IterableListModel<>();

	public static void setModel(EditableModel model) {
		ModelTextureThings.model = model;
		textureListRenderer = new TextureListRenderer(model);
		textureTableCellRenderer = new TextureTableCellRenderer(model);
	}

	public static void updateBitmapList() {
		bitmapListModel.clear();
		bitmapListModel.addAll(model.getTextures());
	}

	public static TextureListRenderer getTextureListRenderer() {
		return textureListRenderer;
	}
	public static TextureTableCellRenderer getTextureTableCellRenderer() {
		return textureTableCellRenderer;
	}

	public static IterableListModel<Bitmap> getBitmapListModel() {
		return bitmapListModel;
	}

}
