package com.hiveworkshop.wc3.gui.modeledit.selection;

import javax.swing.ImageIcon;

import com.hiveworkshop.wc3.gui.modeledit.toolbar.ToolbarButtonType;
import com.hiveworkshop.wc3.gui.modeledit.viewport.ViewportIconUtils;

public enum SelectionMode implements ToolbarButtonType {
	SELECT("Select", ViewportIconUtils.loadImageIcon("icons/actions/selectSingle.png")),
	ADD("Add Selection", ViewportIconUtils.loadImageIcon("icons/actions/selectAdd.png")),
	DESELECT("Deselect", ViewportIconUtils.loadImageIcon("icons/actions/selectRemove.png"));

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
