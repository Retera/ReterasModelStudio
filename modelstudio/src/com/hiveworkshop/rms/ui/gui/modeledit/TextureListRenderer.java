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
	HashMap<Bitmap, Boolean> validImageMap = new HashMap<>();
	Font theFont = new Font("Arial", Font.PLAIN, 18);
	Color orgFgColor = getForeground();
	BufferedImage noImage;
	int imageSize = 64;

	public TextureListRenderer(final EditableModel model) {
		this.model = model;
		noImage = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_ARGB);
		Graphics graphics = noImage.getGraphics();
		graphics.setColor(new Color(255, 255, 255, 0));
		graphics.fillRect(0, 0, imageSize, imageSize);
		graphics.dispose();
	}

	@Override
	public Component getListCellRendererComponent(final JList list, final Object value, final int index,
	                                              final boolean iss, final boolean chf) {
		final String name = ((Bitmap) value).getName();
		Color fgColor = orgFgColor;
		ImageIcon myIcon = map.get(value);
		if (myIcon == null) {
			BufferedImage bufferedImage = BLPHandler.getImage(((Bitmap) value), model.getWrappedDataSource());
			if (bufferedImage == null) {
				System.out.println("could not load icon for \"" + name + "\"");
				bufferedImage = noImage;
				validImageMap.put((Bitmap) value, false);
			} else {
				validImageMap.put((Bitmap) value, true);
			}
			myIcon = new ImageIcon(bufferedImage.getScaledInstance(imageSize, imageSize, Image.SCALE_SMOOTH));
			map.put((Bitmap) value, myIcon);
		}
		if (!validImageMap.get(value)) {
			fgColor = Color.gray;
		}
		super.getListCellRendererComponent(list, name, index, iss, chf);
		setIcon(myIcon);
		setFont(theFont);
		setForeground(fgColor);

		return this;
	}
}
