package com.hiveworkshop.wc3.gui.modeledit.selection;

import javax.swing.ImageIcon;

import com.hiveworkshop.wc3.gui.modeledit.toolbar.ToolbarButtonType;
import com.hiveworkshop.wc3.gui.modeledit.viewport.IconUtils;

public enum SelectionMode implements ToolbarButtonType {
	SELECT("Select", IconUtils.loadImageIcon("icons/actions/selectSingle.png")),
	ADD("Add Selection", IconUtils.loadImageIcon("icons/actions/selectAdd.png")),
	DESELECT("Deselect", IconUtils.loadImageIcon("icons/actions/selectRemove.png"));

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
