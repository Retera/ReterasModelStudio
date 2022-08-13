package com.hiveworkshop.wc3.gui.modeledit;

import java.awt.Component;
import java.awt.Font;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JList;

import com.hiveworkshop.wc3.mdl.Bitmap;
import com.hiveworkshop.wc3.mdl.EditableModel;

public class BitmapListCellRenderer extends DefaultListCellRenderer {
	private static final Bitmap NO_VALUE_BITMAP = new Bitmap("Textures\\White.blp");
	EditableModel model;
	HashMap<Bitmap, ImageIcon> map = new HashMap<Bitmap, ImageIcon>();
	Font theFont = new Font("Arial", Font.BOLD, 32);

	public BitmapListCellRenderer(final EditableModel model) {
		this.model = model;
	}

	@Override
	public Component getListCellRendererComponent(final JList list, Object value, final int index, final boolean iss,
			final boolean chf) {
		String name;
		if (value == null) {
			name = "(No value)";
			// the no value helps with the map cache
			value = NO_VALUE_BITMAP;
		}
		else {
			name = ((Bitmap) value).getName();
		}
		ImageIcon myIcon = map.get(value);
		if (myIcon == null) {
			final BufferedImage bufferedImage = ((Bitmap) value).getBufferedImage(model.getWrappedDataSource());
			if (bufferedImage != null) {
				myIcon = new ImageIcon(bufferedImage.getScaledInstance(64, 64, Image.SCALE_SMOOTH));
				map.put((Bitmap) value, myIcon);
			}
		}
		super.getListCellRendererComponent(list, name, index, iss, chf);
		setIcon(myIcon);
		setFont(theFont);

		return this;
	}
}
