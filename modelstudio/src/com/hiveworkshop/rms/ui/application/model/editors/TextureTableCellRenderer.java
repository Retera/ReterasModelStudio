package com.hiveworkshop.rms.ui.application.model.editors;

import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.model.EditableModel;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class TextureTableCellRenderer extends DefaultTableCellRenderer {
	private ThumbnailProvider thumbnailProvider;
	private Font theFont = new Font("Arial", Font.PLAIN, 14);
	private final Color orgFgColor = getForeground();
	private boolean showPath = false;
	private int imageSize = 64;
	private EditableModel model;

	public TextureTableCellRenderer(final EditableModel model) {
		this(ThumbnailProvider.getThumbnailProvider(model), model);
	}
	public TextureTableCellRenderer(ThumbnailProvider provider, EditableModel model) {
		this.thumbnailProvider = provider;
		this.model = model;
	}

	@Override
	public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean iss, final boolean chf, final int row, final int column) {
		Bitmap bitmap = getBitmap(value);
		if(bitmap != null){
			final String text = getText(bitmap);
			Color fgColor = orgFgColor;

			ImageIcon myIcon = thumbnailProvider.getImageIcon(bitmap, imageSize);
			if (!thumbnailProvider.isValidImage(bitmap)) {
				fgColor = Color.gray;
			}

			super.getTableCellRendererComponent(table, text, iss, chf, row, column);
			setIcon(myIcon);
			setToolTipText(text);
			setFont(theFont);
			setForeground(fgColor);
		}

		return this;
	}

	private String getText(Bitmap bitmap) {
		String text = model == null ? "" : "# " + model.getId(bitmap) + ": ";
		if(showPath){
			text += bitmap.getRenderableTexturePath();
		} else {
			text += bitmap.getName();
		}
		return text;
	}

	private Bitmap getBitmap(Object value) {
		Bitmap bitmap = null;
		if(value instanceof Bitmap){
			bitmap = (Bitmap) value;
		}
		if(value instanceof Integer){
			bitmap = model.getTexture((Integer) value);
		}
		return bitmap;
	}


	public TextureTableCellRenderer setShowPath(boolean showPath) {
		this.showPath = showPath;
		return this;
	}

	public TextureTableCellRenderer setImageSize(int imageSize) {
		this.imageSize = imageSize;
		return this;
	}

	public TextureTableCellRenderer setTextSize(int size){
		theFont = new Font("Arial", Font.PLAIN, size);
		return this;
	}

	public TextureTableCellRenderer setModel(EditableModel model) {
		this.model = model;
		this.thumbnailProvider = ThumbnailProvider.getThumbnailProvider(model);
		return this;
	}

}
