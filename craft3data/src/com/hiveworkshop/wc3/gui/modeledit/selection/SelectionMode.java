package com.hiveworkshop.wc3.gui.modeledit.selection;

import javax.swing.ImageIcon;

import com.hiveworkshop.wc3.gui.icons.RMSIcons;
import com.hiveworkshop.wc3.gui.modeledit.toolbar.ToolbarButtonType;
import hiveworkshop.localizationmanager.localizationmanager;

public enum SelectionMode implements ToolbarButtonType {
	SELECT(LocalizationManager.getInstance().get("string.selectionmode_select"), RMSIcons.loadToolBarImageIcon("selectSingle.png")),
	ADD(LocalizationManager.getInstance().get("string.selectionmode_add_selection"), RMSIcons.loadToolBarImageIcon("selectAdd.png")),
	DESELECT(LocalizationManager.getInstance().get("string.selectionmode_deselect"), RMSIcons.loadToolBarImageIcon("selectRemove.png"));

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
