package com.hiveworkshop.rms.ui.browsers.jworldedit;

import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.parsers.slk.DataTable;

import javax.swing.*;
import java.awt.*;

public abstract class AbstractWorldEditorPanel extends JPanel {

	public AbstractWorldEditorPanel() {
		super();
	}

	public AbstractWorldEditorPanel(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
	}

	public AbstractWorldEditorPanel(LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
	}

	public AbstractWorldEditorPanel(LayoutManager layout) {
		super(layout);
	}

	public static JButton makeButton(DataTable worldEditorData, JToolBar toolBar, String actionName, String iconKey, String tooltipKey) {
		return makeButton(worldEditorData, toolBar, actionName, getIcon(worldEditorData, iconKey), tooltipKey);
	}

	public static JButton makeButton(DataTable worldEditorData, JToolBar toolBar, String actionName, ImageIcon icon, String tooltipKey) {
		JButton button = toolBar.add(new ToolbarButtonAction(actionName, icon));
		button.setToolTipText(WEString.getString(tooltipKey).replace("&", ""));
		button.setPreferredSize(new Dimension(24, 24));
		button.setMargin(new Insets(1, 1, 1, 1));
		button.setFocusable(false);
		return button;
	}

	public static ImageIcon getIcon(DataTable worldEditorData, String iconName) {
		String iconTexturePath = worldEditorData.get("WorldEditArt").getField(iconName);
		if (!iconTexturePath.endsWith(".blp")) {
			iconTexturePath += ".blp";
		}
		return new ImageIcon(BLPHandler.getGameTex(iconTexturePath));
	}
}
