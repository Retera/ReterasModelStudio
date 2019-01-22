package com.hiveworkshop.wc3.gui.modeledit.toolbar;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

public final class ToolbarButtonGroup<BUTTON_TYPE extends ToolbarButtonType> {

	private final BUTTON_TYPE[] toolbarButtonTypes;
	private JButton activeButton;
	private BUTTON_TYPE activeButtonType;
	private final List<ToolbarButtonListener<BUTTON_TYPE>> listeners;

	private final List<ToolbarButtonAction> buttons;
	private Border activeButtonDefaultBorder;

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

	public void maybeSetButtonType(final Object possibleButtonType) {
		boolean foundMatch = false;
		for (final ToolbarButtonAction action : buttons) {
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

	public void setToolbarButtonType(final BUTTON_TYPE buttonType) {
		for (final ToolbarButtonAction action : buttons) {
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

	private ToolbarButtonAction createButton(final JToolBar toolBar, final BUTTON_TYPE editorAction) {
		final ToolbarButtonAction toolbarEditAction = new ToolbarButtonAction(editorAction);
		final JButton button = toolBar.add(toolbarEditAction);
		button.setToolTipText(editorAction.getName());
		button.setIcon(editorAction.getImageIcon());
		button.setDisabledIcon(editorAction.getImageIcon());
		toolbarEditAction.setButton(button);
		if (activeButtonType == null) {
			setActiveButton(button, editorAction, toolbarEditAction.defaultBorder);
		}
		return toolbarEditAction;
	}

	private void setActiveButton(final JButton button, final BUTTON_TYPE type, final Border defaultBorder) {
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
			for (final ToolbarButtonListener<BUTTON_TYPE> listener : listeners) {
				listener.typeChanged(activeButtonType);
			}
		} else {
			for (final ToolbarButtonListener<BUTTON_TYPE> listener : listeners) {
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
