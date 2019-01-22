package com.hiveworkshop.wc3.gui.modeledit.toolbar;

import javax.swing.ImageIcon;

import com.hiveworkshop.wc3.gui.modeledit.activity.ActivityDescriptor;

public abstract class ToolbarActionButtonType implements ToolbarButtonType, ActivityDescriptor {
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
