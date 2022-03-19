package com.hiveworkshop.rms.ui.gui.modeledit.toolbar;

import com.hiveworkshop.rms.ui.icons.RMSIcons;

import javax.swing.*;

public enum ModelEditorWidgetType implements ToolbarButtonType {
	TRANSLATION("Move", RMSIcons.loadToolBarImageIcon("move2.png")),
	ROTATION("Rotate", RMSIcons.loadToolBarImageIcon("rotate.png")),
	SCALING("Scale", RMSIcons.loadToolBarImageIcon("scale.png"));

	private final String name;
	private final ImageIcon imageIcon;

	ModelEditorWidgetType(final String name, final ImageIcon imageIcon) {
		this.name = name;
		this.imageIcon = imageIcon;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public ImageIcon getImageIcon() {
		return imageIcon;
	}
}
