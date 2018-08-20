package com.hiveworkshop.wc3.gui.modeledit.selection;

import javax.swing.ImageIcon;

import com.hiveworkshop.wc3.gui.modeledit.toolbar.ToolbarButtonType;
import com.hiveworkshop.wc3.gui.modeledit.viewport.IconUtils;

public enum SelectionItemTypes implements ToolbarButtonType {
	VERTEX("Select Vertices", IconUtils.loadImageIcon("icons/actions/vertex.png")),
	FACE("Select Faces", IconUtils.loadImageIcon("icons/actions/poly.png")),
	GROUP("Select Groups", IconUtils.loadImageIcon("icons/actions/bundle.png")),
	ANIMATE("Select Nodes and Animate", IconUtils.loadImageIcon("icons/actions/animate.png")),
	CLUSTER("Select Cluster", IconUtils.loadImageIcon("icons/actions/bundle.png")),
	TPOSE("Select and T-Pose", IconUtils.loadImageIcon("icons/actions/T.png"));

	private final String name;
	private final ImageIcon imageIcon;

	private SelectionItemTypes(final String name, final ImageIcon imageIcon) {
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
