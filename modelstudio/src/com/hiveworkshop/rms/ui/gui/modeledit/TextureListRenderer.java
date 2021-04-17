package com.hiveworkshop.rms.ui.gui.modeledit;

import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;

public class TextureListRenderer extends DefaultListCellRenderer {
	EditableModel model;
	HashMap<Bitmap, ImageIcon> map = new HashMap<>();
	Font theFont = new Font("Arial", Font.BOLD, 32);

	public TextureListRenderer(final EditableModel model) {
		this.model = model;
	}

	@Override
	public Component getListCellRendererComponent(final JList list, final Object value, final int index,
	                                              final boolean iss, final boolean chf) {
		final String name = ((Bitmap) value).getName();
		ImageIcon myIcon = map.get(value);
		if (myIcon == null) {
			BufferedImage bufferedImage = BLPHandler.getImage(((Bitmap) value), model.getWrappedDataSource());
			myIcon = new ImageIcon(bufferedImage.getScaledInstance(64, 64, Image.SCALE_SMOOTH));
			map.put((Bitmap) value, myIcon);
		}
		super.getListCellRendererComponent(list, name, index, iss, chf);
		setIcon(myIcon);
		setFont(theFont);

		return this;
	}
}
