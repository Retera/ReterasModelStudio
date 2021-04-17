package com.hiveworkshop.rms.util;

import javax.swing.*;
import javax.swing.event.CaretListener;
import javax.swing.text.Document;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.function.Consumer;

public class FilteredTextField extends JTextField {

	protected String allowedCharacters = "{} ,-1234567890.eE";
	CaretListener caretListener;
	Consumer<String> focusLostFunction;
	boolean removeMultiDot = true;
	Consumer<String> caretFunction;

	public FilteredTextField() {
		this(null, null, 0);
	}

	public FilteredTextField(String text) {
		this(null, text, 0);
	}

	public FilteredTextField(String text, int columns) {
		this(null, text, columns);
	}

	public FilteredTextField(int columns) {
		this(null, null, columns);
	}

	public FilteredTextField(Document doc, String text, int columns) {
		super(doc, text, columns);
		caretListener = getCaretListener();
//		addFocusListener(this.getFocusAdapter());
		addCaretListener(caretListener);
	}


	public FilteredTextField addOnCaretEventFunction(Consumer<String> function) {
		caretFunction = function;
		return this;
	}

	public FilteredTextField setRemoveMultiDot(boolean b) {
		removeMultiDot = b;
		return this;
	}

	public FilteredTextField setAllowedCharacters(String allowedCharacters) {
		this.allowedCharacters = allowedCharacters;
		return this;
	}

	public FilteredTextField addOnFocusLostFunction(Consumer<String> function) {
		focusLostFunction = function;
		addFocusListener(getFocusAdapter());
		return this;
	}

	private FocusAdapter getFocusAdapter() {
		return new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				if (focusLostFunction != null) {
					focusLostFunction.accept(getText());
				}
			}
		};
	}

	private CaretListener getCaretListener() {
		return e -> {
			String text = getText();
			// remove non allowed characters
			if (!text.matches("[" + allowedCharacters + "]*")) {
				String newText = text.replaceAll("[^" + allowedCharacters + "]*", "");
				SwingUtilities.invokeLater(() -> {
					applyFilteredText(newText);
				});
			}
			// remove multiple dots
			if (removeMultiDot && text.matches("(.*\\.\\.+.*)")) {
				String newText = text.replaceAll("(\\.+)", ".");
				SwingUtilities.invokeLater(() -> {
					applyFilteredText(newText);
				});
			}
			if (caretFunction != null) {
				caretFunction.accept(getText());
			}
		};
	}

	private void applyFilteredText(String newText) {
		removeCaretListener(caretListener);

		int carPos = getCaretPosition();
		setText(newText);

		int newCarPos = Math.max(0, Math.min(newText.length(), carPos - 1));
		setCaretPosition(newCarPos);

		addCaretListener(caretListener);
	}
//	private void applyFilteredText(String newText) {
//		CaretListener listener = getCaretListeners()[0];
//		removeCaretListener(listener);
//
//		int carPos = getCaretPosition();
//		setText(newText);
//
//		int newCarPos = Math.max(0, Math.min(newText.length(), carPos - 1));
//		setCaretPosition(newCarPos);
//
//		addCaretListener(listener);
//	}
}
