package com.hiveworkshop.wc3.gui.modeledit.selection;

import javax.swing.ImageIcon;

import com.hiveworkshop.wc3.gui.modeledit.toolbar.ToolbarButtonType;
import com.hiveworkshop.wc3.gui.modeledit.viewport.ViewportIconUtils;

public enum TVertexSelectionItemTypes implements ToolbarButtonType {
	VERTEX("Select Vertices", ViewportIconUtils.loadImageIcon("icons/actions/vertex.png")),
	FACE("Select Faces", ViewportIconUtils.loadImageIcon("icons/actions/poly.png"));

	private final String name;
	private final ImageIcon imageIcon;

	private TVertexSelectionItemTypes(final String name, final ImageIcon imageIcon) {
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
