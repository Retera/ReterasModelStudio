package com.hiveworkshop.rms.ui.gui.modeledit.toolbar;

import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ActivityDescriptor;

import javax.swing.*;

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
