package com.hiveworkshop.rms.ui.gui.modeledit.components.editors;

import java.awt.Color;

import javax.swing.JComponent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

final class UnsavedChangesDocumentListener implements DocumentListener {
	public static final Color SAVED_FOREGROUND_COLOR = Color.BLACK;
	public static final Color UNSAVED_FOREGROUND_COLOR = Color.MAGENTA.darker();
	public static final Color SAVED_BACKGROUND_COLOR = Color.WHITE;
	public static final Color UNSAVED_BACKGROUND_COLOR = Color.LIGHT_GRAY;
	private final JComponent component;

	public UnsavedChangesDocumentListener(final JComponent component) {
		this.component = component;
	}

	@Override
	public void insertUpdate(final DocumentEvent e) {
		component.setForeground(UNSAVED_FOREGROUND_COLOR);
		component.setBackground(UNSAVED_BACKGROUND_COLOR);
	}

	@Override
	public void removeUpdate(final DocumentEvent e) {
		component.setForeground(UNSAVED_FOREGROUND_COLOR);
		component.setBackground(UNSAVED_BACKGROUND_COLOR);
	}

	@Override
	public void changedUpdate(final DocumentEvent e) {
		component.setForeground(UNSAVED_FOREGROUND_COLOR);
		component.setBackground(UNSAVED_BACKGROUND_COLOR);
	}

}