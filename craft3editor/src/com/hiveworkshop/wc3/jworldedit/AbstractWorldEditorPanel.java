package com.hiveworkshop.wc3.jworldedit;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import com.hiveworkshop.wc3.gui.BLPHandler;
import com.hiveworkshop.wc3.resources.WEString;
import com.hiveworkshop.wc3.units.DataTable;

public abstract class AbstractWorldEditorPanel extends JPanel {

	public AbstractWorldEditorPanel() {
		super();
	}

	public AbstractWorldEditorPanel(final boolean isDoubleBuffered) {
		super(isDoubleBuffered);
	}

	public AbstractWorldEditorPanel(final LayoutManager layout, final boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
	}

	public AbstractWorldEditorPanel(final LayoutManager layout) {
		super(layout);
	}

	public static final class ToolbarButtonAction extends AbstractAction {
		private ToolbarButtonAction(final String name, final Icon icon) {
			super(name, icon);
		}

		@Override
		public void actionPerformed(final ActionEvent e) {

		}
	}

	public static JButton makeButton(final DataTable worldEditorData, final JToolBar toolBar, final String actionName,
			final String iconKey, final String tooltipKey) {
		return makeButton(worldEditorData, toolBar, actionName, getIcon(worldEditorData, iconKey), tooltipKey);
	}

	public static JButton makeButton(final DataTable worldEditorData, final JToolBar toolBar, final String actionName,
			final ImageIcon icon, final String tooltipKey) {
		final JButton button = toolBar.add(new ToolbarButtonAction(actionName, icon));
		button.setToolTipText(WEString.getString(tooltipKey).replace("&", ""));
		button.setPreferredSize(new Dimension(24, 24));
		button.setMargin(new Insets(1, 1, 1, 1));
		button.setFocusable(false);
		return button;
	}

	public static ImageIcon getIcon(final DataTable worldEditorData, final String iconName) {
		String iconTexturePath = worldEditorData.get("WorldEditArt").getField(iconName);
		if (!iconTexturePath.toString().endsWith(".blp")) {
			iconTexturePath += ".blp";
		}
		return new ImageIcon(BLPHandler.get().getGameTex(iconTexturePath));
	}
}
