package com.hiveworkshop.rms.ui.gui.modeledit.selection;

import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ToolbarButtonType;
import com.hiveworkshop.rms.ui.icons.RMSIcons;

import javax.swing.*;

public enum TVertexSelectionItemTypes implements ToolbarButtonType {
	VERTEX("Select Vertices", RMSIcons.loadToolBarImageIcon("vertex.png")),
	FACE("Select Faces", RMSIcons.loadToolBarImageIcon("poly.png"));

	private final String name;
	private final ImageIcon imageIcon;

	TVertexSelectionItemTypes(final String name, final ImageIcon imageIcon) {
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
