package com.hiveworkshop.wc3.gui.modeledit.toolbar;

import javax.swing.ImageIcon;

public class ToolbarActionButtonType implements ToolbarButtonType {
	private final ImageIcon imageIcon;
	private final String name;

	public ToolbarActionButtonType(final ImageIcon imageIcon, final String name) {
		this.imageIcon = imageIcon;
		this.name = name;
	}

	@Override
	public ImageIcon getImageIcon() {
		return imageIcon;
	}

	@Override
	public String getName() {
		return name;
	}

}
