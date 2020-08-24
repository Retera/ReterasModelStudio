package com.hiveworkshop.rms.ui.gui.modeledit.selection;

import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ToolbarButtonType;
import com.hiveworkshop.rms.ui.icons.RMSIcons;

import javax.swing.*;

public enum SelectionItemTypes implements ToolbarButtonType {
	VERTEX("Select Vertices", RMSIcons.loadToolBarImageIcon("vertex.png")),
	FACE("Select Faces", RMSIcons.loadToolBarImageIcon("poly.png")),
	GROUP("Select Groups", RMSIcons.loadToolBarImageIcon("bundle.png")),
	ANIMATE("Select Nodes and Animate", RMSIcons.loadToolBarImageIcon("animate.png")),
	CLUSTER("Select Cluster", RMSIcons.loadToolBarImageIcon("bundle.png")),
	TPOSE("Select and T-Pose", RMSIcons.loadToolBarImageIcon("T.png"));

	private final String name;
	private final ImageIcon imageIcon;

	SelectionItemTypes(final String name, final ImageIcon imageIcon) {
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
