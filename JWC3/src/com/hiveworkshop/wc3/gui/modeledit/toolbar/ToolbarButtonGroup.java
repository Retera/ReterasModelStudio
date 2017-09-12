package com.hiveworkshop.wc3.gui.modeledit.toolbar;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JToolBar;

public final class ToolbarButtonGroup<BUTTON_TYPE extends ToolbarButtonType> {

	private final BUTTON_TYPE[] toolbarButtonTypes;
	private JButton activeButton;
	private BUTTON_TYPE activeButtonType;
	private final List<ToolbarButtonListener<BUTTON_TYPE>> listeners;

	private final List<ToolbarSelectAction> buttons;

	public ToolbarButtonGroup(final JToolBar toolBar, final BUTTON_TYPE[] toolbarButtonTypes) {
		this.toolbarButtonTypes = toolbarButtonTypes;
		listeners = new ArrayList<>();
		buttons = new ArrayList<>();
		for (final BUTTON_TYPE type : toolbarButtonTypes) {
			buttons.add(createButton(toolBar, type));
		}
	}

	public BUTTON_TYPE[] getToolbarButtonTypes() {
		return toolbarButtonTypes;
	}

	public void addToolbarButtonListener(final ToolbarButtonListener<BUTTON_TYPE> listener) {
		listeners.add(listener);
		if (activeButtonType != null) {
			listener.typeChanged(activeButtonType);
		}
	}

	public void setToolbarButtonType(final BUTTON_TYPE buttonType) {
		for (final ToolbarSelectAction action : buttons) {
			if (action.getButtonType() == buttonType) {
				setActiveButton(action.getButton(), action.getButtonType());
			}
		}
	}

	private final class ToolbarSelectAction extends AbstractAction {
		private JButton button;
		private final BUTTON_TYPE buttonType;

		public ToolbarSelectAction(final BUTTON_TYPE buttonType) {
			this.buttonType = buttonType;
		}

		public JButton getButton() {
			return button;
		}

		public BUTTON_TYPE getButtonType() {
			return buttonType;
		}

		public void setButton(final JButton button) {
			this.button = button;
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			setActiveButton(button, buttonType);
		}
	}

	private ToolbarSelectAction createButton(final JToolBar toolBar, final BUTTON_TYPE editorAction) {
		final ToolbarSelectAction toolbarEditAction = new ToolbarSelectAction(editorAction);
		final JButton button = toolBar.add(toolbarEditAction);
		button.setToolTipText(editorAction.getName());
		button.setIcon(editorAction.getImageIcon());
		button.setDisabledIcon(editorAction.getImageIcon());
		toolbarEditAction.setButton(button);
		if (activeButtonType == null) {
			setActiveButton(button, editorAction);
		}
		return toolbarEditAction;
	}

	private void setActiveButton(final JButton button, final BUTTON_TYPE type) {
		if (activeButton != null) {
			activeButton.setEnabled(true);
		}
		activeButton = button;
		activeButton.setEnabled(false);
		activeButtonType = type;
		for (final ToolbarButtonListener<BUTTON_TYPE> listener : listeners) {
			listener.typeChanged(activeButtonType);
		}
	}

	public BUTTON_TYPE getActiveButtonType() {
		return activeButtonType;
	}
}
