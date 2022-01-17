package com.hiveworkshop.rms.ui.application.model.editors;

import javax.swing.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.function.Consumer;

public class TwiTextField extends JTextField {
	private Consumer<String> textConsumer;
	private String text = "";

	public TwiTextField(int columns, Consumer<String> textChangedAction) {
		this("", columns, textChangedAction);
	}

	public TwiTextField(String text, int columns, Consumer<String> textChangedAction) {
		super(text, columns);
		textConsumer = textChangedAction;
		this.addFocusListener(getFocusAdapter());
		this.addKeyListener(getKeyAdapter());
	}

	@Override
	public void setText(String text) {
		super.setText(text);
		this.text = text;
	}

	public TwiTextField setTextConsumer(Consumer<String> textConsumer) {
		this.textConsumer = textConsumer;
		return this;
	}

	private FocusAdapter getFocusAdapter() {
		return new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				applyNewText();
			}
		};
	}

	private KeyAdapter getKeyAdapter() {
		return new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					applyNewText();
				} else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					setText(text);
				}
			}
		};
	}

	private void applyNewText() {
		String newText = getText();
		text = newText;
		if (textConsumer != null) {
			textConsumer.accept(newText);
		}
	}
}
