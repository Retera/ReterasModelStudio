package com.hiveworkshop.wc3.gui.modeledit.components.editors;

import java.awt.Color;

import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Tints an editor field while it holds a value that has not been applied yet.
 * The colours follow the look and feel so dark themes keep readable text.
 */
final class UnsavedChangesDocumentListener implements DocumentListener {
	private final JComponent component;

	public UnsavedChangesDocumentListener(final JComponent component) {
		this.component = component;
	}

	private static Color uiColor(final String key, final Color fallback) {
		final Color color = UIManager.getColor(key);
		return color == null ? fallback : color;
	}

	private static boolean isDark(final Color color) {
		return ((0.299 * color.getRed()) + (0.587 * color.getGreen()) + (0.114 * color.getBlue())) < 128;
	}

	public static Color savedForegroundColor() {
		return uiColor("TextField.foreground", Color.BLACK);
	}

	public static Color savedBackgroundColor() {
		return uiColor("TextField.background", Color.WHITE);
	}

	/** Magenta, brightened on dark themes so it stays legible. */
	public static Color unsavedForegroundColor() {
		return isDark(savedBackgroundColor()) ? new Color(255, 140, 255) : Color.MAGENTA.darker();
	}

	/** The field background nudged toward the text colour. */
	public static Color unsavedBackgroundColor() {
		final Color bg = savedBackgroundColor();
		final Color fg = savedForegroundColor();
		return new Color((bg.getRed() * 3 + fg.getRed()) / 4, (bg.getGreen() * 3 + fg.getGreen()) / 4,
				(bg.getBlue() * 3 + fg.getBlue()) / 4);
	}

	public static void markSaved(final JComponent component) {
		component.setForeground(savedForegroundColor());
		component.setBackground(savedBackgroundColor());
	}

	public static void markUnsaved(final JComponent component) {
		component.setForeground(unsavedForegroundColor());
		component.setBackground(unsavedBackgroundColor());
	}

	@Override
	public void insertUpdate(final DocumentEvent e) {
		markUnsaved(component);
	}

	@Override
	public void removeUpdate(final DocumentEvent e) {
		markUnsaved(component);
	}

	@Override
	public void changedUpdate(final DocumentEvent e) {
		markUnsaved(component);
	}

}
