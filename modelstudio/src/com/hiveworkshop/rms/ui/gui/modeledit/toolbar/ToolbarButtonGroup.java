package com.hiveworkshop.rms.ui.gui.modeledit.toolbar;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public final class ToolbarButtonGroup<BUTTON_TYPE extends ToolbarButtonType> {

	private final BUTTON_TYPE[] toolbarButtonTypes;
	private JButton activeButton;
	private BUTTON_TYPE activeButtonType;
	private final List<ToolbarButtonListener<BUTTON_TYPE>> listeners;

	private final List<ToolbarButtonAction> buttons;
	private Border activeButtonDefaultBorder;

	public ToolbarButtonGroup(JToolBar toolBar, BUTTON_TYPE[] toolbarButtonTypes) {
		this.toolbarButtonTypes = toolbarButtonTypes;
		listeners = new ArrayList<>();
		buttons = new ArrayList<>();
		for (BUTTON_TYPE type : toolbarButtonTypes) {
			buttons.add(createButton(toolBar, type));
		}
	}

	public BUTTON_TYPE[] getToolbarButtonTypes() {
		return toolbarButtonTypes;
	}

	public void addToolbarButtonListener(ToolbarButtonListener<BUTTON_TYPE> listener) {
		listeners.add(listener);
		if (activeButtonType != null) {
			listener.typeChanged(activeButtonType);
		}
	}

	public void maybeSetButtonType(Object possibleButtonType) {
		boolean foundMatch = false;
		for (ToolbarButtonAction action : buttons) {
			if (action.getButtonType() == possibleButtonType) {
				setActiveButton(action.getButton(), action.getButtonType(), action.defaultBorder);
				foundMatch = true;
				break;
			}
		}
		if (!foundMatch) {
			clear();
		}
	}

	public void setToolbarButtonType(BUTTON_TYPE buttonType) {
		for (ToolbarButtonAction action : buttons) {
			if (action.getButtonType() == buttonType) {
				setActiveButton(action.getButton(), action.getButtonType(), action.defaultBorder);
			}
		}
	}

	public void clear() {
		setActiveButton(null, null, null);
	}

	public final class ToolbarButtonAction extends AbstractAction {
		private JButton button;
		private Border defaultBorder;
		private final BUTTON_TYPE buttonType;

		public ToolbarButtonAction(final BUTTON_TYPE buttonType) {
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
			defaultBorder = button.getBorder();
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			setActiveButton(button, buttonType, defaultBorder);
		}
	}

	private ToolbarButtonAction createButton(JToolBar toolBar, BUTTON_TYPE editorAction) {
		ToolbarButtonAction toolbarEditAction = new ToolbarButtonAction(editorAction);
		JButton button = toolBar.add(toolbarEditAction);
		button.setToolTipText(editorAction.getName());
		button.setIcon(editorAction.getImageIcon());
		button.setDisabledIcon(editorAction.getImageIcon());
		toolbarEditAction.setButton(button);
		if (activeButtonType == null) {
			setActiveButton(button, editorAction, toolbarEditAction.defaultBorder);
		}
		return toolbarEditAction;
	}

	private void setActiveButton(JButton button, BUTTON_TYPE type, Border defaultBorder) {
		if (activeButton == button) {
			return;
		}
		if (activeButton != null) {
			// activeButton.setEnabled(true);
			activeButton.setBorder(activeButtonDefaultBorder);
		}
		activeButton = button;
		if (button != null) {
			// activeButton.setEnabled(false);
			activeButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
			activeButtonType = type;
			this.activeButtonDefaultBorder = defaultBorder;
			for (ToolbarButtonListener<BUTTON_TYPE> listener : listeners) {
				listener.typeChanged(activeButtonType);
			}
		} else {
			for (ToolbarButtonListener<BUTTON_TYPE> listener : listeners) {
				listener.typeChanged(null);
			}
		}
	}

	public BUTTON_TYPE getActiveButtonType() {
		return activeButtonType;
	}

	public List<ToolbarButtonAction> getButtons() {
		return buttons;
	}
}
