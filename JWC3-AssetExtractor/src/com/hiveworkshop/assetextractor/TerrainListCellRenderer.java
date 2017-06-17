package com.hiveworkshop.assetextractor;

import java.awt.Component;
import java.awt.image.BufferedImage;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JList;

import com.hiveworkshop.wc3.gui.BLPHandler;

public class TerrainListCellRenderer extends DefaultListCellRenderer {
	@Override
	public Component getListCellRendererComponent(final JList<?> list, final Object value, final int index,
			final boolean isSelected, final boolean cellHasFocus) {
		final Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		final BufferedImage gameTex = BLPHandler.get().getGameTex(((Terrain) value).getTexturePath());
		final BufferedImage subimage = gameTex.getSubimage(0, 0, 64, 64);
		this.setIcon(new ImageIcon(subimage));
		return this;
	}
}
