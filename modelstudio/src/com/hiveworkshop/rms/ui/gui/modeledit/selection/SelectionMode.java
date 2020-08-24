package com.hiveworkshop.rms.ui.gui.modeledit.selection;

import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ToolbarButtonType;
import com.hiveworkshop.rms.ui.icons.RMSIcons;

import javax.swing.*;

public enum SelectionMode implements ToolbarButtonType {
	SELECT("Select", RMSIcons.loadToolBarImageIcon("selectSingle.png")),
	ADD("Add Selection", RMSIcons.loadToolBarImageIcon("selectAdd.png")),
	DESELECT("Deselect", RMSIcons.loadToolBarImageIcon("selectRemove.png"));

	private final String name;
	private final ImageIcon imageIcon;

	SelectionMode(final String name, final ImageIcon imageIcon) {
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
