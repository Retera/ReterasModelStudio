package com.hiveworkshop.wc3.gui.modeledit.selection;

import javax.swing.ImageIcon;

import com.hiveworkshop.wc3.gui.icons.RMSIcons;
import com.hiveworkshop.wc3.gui.modeledit.toolbar.ToolbarButtonType;

public enum SelectionMode implements ToolbarButtonType {
	SELECT("Select", RMSIcons.loadToolBarImageIcon("selectSingle.png")),
	ADD("Add Selection", RMSIcons.loadToolBarImageIcon("selectAdd.png")),
	DESELECT("Deselect", RMSIcons.loadToolBarImageIcon("selectRemove.png"));

	private final String name;
	private final ImageIcon imageIcon;

	private SelectionMode(final String name, final ImageIcon imageIcon) {
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
