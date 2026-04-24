package com.hiveworkshop.wc3.gui.modeledit.selection;

import javax.swing.ImageIcon;

import com.hiveworkshop.wc3.gui.icons.RMSIcons;
import com.hiveworkshop.wc3.gui.modeledit.toolbar.ToolbarButtonType;
import hiveworkshop.localizationmanager.localizationmanager;

public enum SelectionItemTypes implements ToolbarButtonType {
	VERTEX(LocalizationManager.getInstance().get("string.selectionitemtypes_select_vertices"), RMSIcons.loadToolBarImageIcon("vertex.png")),
	FACE(LocalizationManager.getInstance().get("string.selectionitemtypes_select_faces"), RMSIcons.loadToolBarImageIcon("poly.png")),
	GROUP(LocalizationManager.getInstance().get("string.selectionitemtypes_select_groups"), RMSIcons.loadToolBarImageIcon("bundle.png")),
	ANIMATE(LocalizationManager.getInstance().get("string.selectionitemtypes_select_nodes_animate"), RMSIcons.loadToolBarImageIcon("animate.png")),
	CLUSTER(LocalizationManager.getInstance().get("string.selectionitemtypes_select_cluster"), RMSIcons.loadToolBarImageIcon("bundle.png")),
	TPOSE(LocalizationManager.getInstance().get("string.selectionitemtypes_sSelect_tpose"), RMSIcons.loadToolBarImageIcon("T.png"));

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
