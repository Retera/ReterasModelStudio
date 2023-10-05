package com.hiveworkshop.rms.ui.gui.modeledit;

import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.ui.application.model.editors.ThumbnailProvider;

import javax.swing.*;
import java.awt.*;

public class TextureListRenderer extends DefaultListCellRenderer {
	private ThumbnailProvider thumbnailProvider;
	private Font theFont = new Font(Font.SANS_SERIF, Font.PLAIN, 18);
	private final Color orgFgColor = getForeground();
	private boolean showPath = false;
	private int imageSize = 64;
	private EditableModel model;

	public TextureListRenderer(final EditableModel model) {
		this(ThumbnailProvider.getThumbnailProvider(model), model);
	}
	public TextureListRenderer(ThumbnailProvider provider, EditableModel model) {
		this.thumbnailProvider = provider;
		this.model = model;
	}


	@Override
	public Component getListCellRendererComponent(final JList list, final Object value, final int index,
	                                              final boolean iss, final boolean chf) {
		if (value instanceof Bitmap) {
			Bitmap bitmap = (Bitmap) value;
			String text = getText(bitmap);
			String toolTipText = getToolTipText(bitmap);
			Color fgColor = orgFgColor;
			ImageIcon myIcon = thumbnailProvider.getImageIcon(bitmap, imageSize);
			if (!thumbnailProvider.isValidImage(bitmap)) {
				fgColor = Color.gray;
			}

			super.getListCellRendererComponent(list, text, index, iss, chf);
			setIcon(myIcon);
			setToolTipText(toolTipText);
			setFont(theFont);
			setForeground(fgColor);
		}

		return this;
	}

	private String getText(Bitmap bitmap) {
		String text = model == null ? "" : "# " + model.getId(bitmap) + ": ";
		if (model != null && 900 < model.getFormatVersion() && bitmap.getPath().toLowerCase().endsWith(".blp")) {
			text += "\u1d2e ";
		} else if (model != null && model.getFormatVersion() < 1000 && bitmap.getPath().toLowerCase().endsWith(".dds")) {
			text += "\u1d30 ";
		}

		if (showPath) {
			text += bitmap.getRenderableTexturePath();
		} else {
			text += bitmap.getName();
		}
		return text;
	}

	private String getToolTipText(Bitmap bitmap) {
		String text = "<html>";
		text += bitmap.getRenderableTexturePath();
		text += "<br>";
		if (!"".equals(bitmap.getExtension())) {
			text += bitmap.getExtension();
		} else {
			text += "replaceableId: " + bitmap.getReplaceableId();
		}
		text += "</html>";

		return text;
	}

	public TextureListRenderer setShowPath(boolean showPath) {
		this.showPath = showPath;
		return this;
	}

	public TextureListRenderer setImageSize(int imageSize) {
		this.imageSize = imageSize;
		return this;
	}

	public TextureListRenderer setTextSize(int size) {
		theFont = new Font("Arial", Font.PLAIN, size);
		return this;
	}

	public TextureListRenderer setModel(EditableModel model) {
		this.model = model;
		this.thumbnailProvider = ThumbnailProvider.getThumbnailProvider(model);
		return this;
	}
}
