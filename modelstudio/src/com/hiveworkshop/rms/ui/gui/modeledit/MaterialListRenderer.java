package com.hiveworkshop.rms.ui.gui.modeledit;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Material;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;

public class MaterialListRenderer extends DefaultListCellRenderer {
	EditableModel model;
	HashMap<Material, ImageIcon> map = new HashMap<>();
	HashMap<Material, Boolean> validImageMap = new HashMap<>();
	Font theFont = new Font("Arial", Font.PLAIN, 18);
	Color orgFgColor = getForeground();
	BufferedImage noImage;
	int imageSize = 64;

	public MaterialListRenderer(final EditableModel model) {
		this.model = model;
		noImage = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_ARGB);
		Graphics graphics = noImage.getGraphics();
		graphics.setColor(new Color(255, 255, 255, 0));
		graphics.fillRect(0, 0, imageSize, imageSize);
		graphics.dispose();
	}

	@Override
	public Component getListCellRendererComponent(final JList list, final Object value, final int index, final boolean iss, final boolean chf) {
		final String name = ((Material) value).getName();
		Color fgColor = orgFgColor;
		ImageIcon myIcon = map.get(value);

		if (myIcon == null) {
			BufferedImage bufferedImage = ((Material) value).getBufferedImage(model.getWrappedDataSource());
			if (bufferedImage == null) {
				System.out.println("could not load icon for \"" + name + "\"");
				bufferedImage = noImage;
				validImageMap.put((Material) value, false);
			} else {
				validImageMap.put((Material) value, true);
			}
			myIcon = new ImageIcon(bufferedImage.getScaledInstance(imageSize, imageSize, Image.SCALE_SMOOTH));
			map.put((Material) value, myIcon);
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
