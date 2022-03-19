package com.hiveworkshop.rms.ui.application.model.editors;

import javax.swing.*;
import javax.swing.text.Document;
import java.awt.*;
import java.util.function.Consumer;

public class ComponentEditorTextField extends JTextField {
	public static final Color SAVED_FG = Color.BLACK;
	public static final Color SAVED_BG = Color.WHITE;
	public static final Color UNSAVED_FG = Color.MAGENTA.darker();
	public static final Color UNSAVED_BG = Color.LIGHT_GRAY;
	private Consumer<String> stringConsumer;

	public ComponentEditorTextField() {
		this(null, null, 0, null);
	}

	public ComponentEditorTextField(final Document doc, final String text, final int columns) {
		this(doc, text, columns, null);
	}

	public ComponentEditorTextField(final int columns) {
		this(null, null, columns, null);
	}

	public ComponentEditorTextField(final String text, final int columns) {
		this(null, text, columns, null);
	}

	public ComponentEditorTextField(final String text) {
		this(null, text, 0, null);
	}

	public ComponentEditorTextField(Consumer<String> stringConsumer) {
		this(null, null, 0, stringConsumer);
	}

	public ComponentEditorTextField(final Document doc, final String text, final int columns, Consumer<String> stringConsumer) {
		super(doc, text, columns);
		this.stringConsumer = stringConsumer;
		getDocument().addDocumentListener(new UnsavedChangesDocumentListener(this));
		this.addFocusListener(new TwiFocusListener(this, this::runEditingStoppedListener));
	}

	public ComponentEditorTextField(final int columns, Consumer<String> stringConsumer) {
		this(null, null, columns, stringConsumer);
	}

	public ComponentEditorTextField(final String text, final int columns, Consumer<String> stringConsumer) {
		this(null, text, columns, stringConsumer);
	}

	public ComponentEditorTextField(final String text, Consumer<String> stringConsumer) {
		this(null, text, 0, stringConsumer);
	}


	public ComponentEditorTextField reloadNewValue(final String value) {
		setText(value);
		setColors(SAVED_FG, SAVED_BG);
		return this;
	}

	public void setColorToSaved() {
		setForeground(SAVED_FG);
		setBackground(SAVED_BG);
	}

	private void setColors(Color fg, Color bg) {
		setForeground(fg);
		setBackground(bg);
	}

	public ComponentEditorTextField setEditingStoppedListener(Consumer<String> stringConsumer){
		this.stringConsumer = stringConsumer;
		return this;
	}

	private void runEditingStoppedListener() {
		if (stringConsumer != null) {
			stringConsumer.accept(getText());
		}
		setColors(SAVED_FG, SAVED_BG);
	}

}
