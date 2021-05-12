package com.hiveworkshop.rms.ui.gui.modeledit.toolbar;

import com.hiveworkshop.rms.ui.icons.RMSIcons;

import javax.swing.*;

public enum ModelEditorActionType3 implements ToolbarButtonType {
	TRANSLATION("Move", RMSIcons.loadToolBarImageIcon("move2.png")),
	ROTATION("Rotate", RMSIcons.loadToolBarImageIcon("rotate.png")),
	SCALING("Scale", RMSIcons.loadToolBarImageIcon("scale.png")),
	EXTRUDE("Extrude", RMSIcons.loadToolBarImageIcon("extrude.png")),
	EXTEND("Extend", RMSIcons.loadToolBarImageIcon("extend.png"));

	private final String name;
	private final ImageIcon imageIcon;

	ModelEditorActionType3(final String name, final ImageIcon imageIcon) {
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
