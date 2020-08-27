package com.hiveworkshop.rms.ui.application.model.editors;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import java.beans.PropertyChangeEvent;
import java.util.Objects;

public class ComponentEditorTextField extends JTextField {

	public ComponentEditorTextField() {
		super();
		init();
	}

	public ComponentEditorTextField(final Document doc, final String text, final int columns) {
		super(doc, text, columns);
		init();
	}

	public ComponentEditorTextField(final int columns) {
		super(columns);
		init();
	}

	public ComponentEditorTextField(final String text, final int columns) {
		super(text, columns);
		init();
	}

	public ComponentEditorTextField(final String text) {
		super(text);
		init();
	}

	private void init() {
		getDocument().addDocumentListener(new UnsavedChangesDocumentListener(this));
	}

	public void reloadNewValue(final String value) {
		setText(value);
		setColorToSaved();
	}

	public void setColorToSaved() {
		setForeground(UnsavedChangesDocumentListener.SAVED_FOREGROUND_COLOR);
		setBackground(UnsavedChangesDocumentListener.SAVED_BACKGROUND_COLOR);
	}

	/**
	 * Installs a listener to receive notification when the text of any
	 * {@code JTextComponent} is changed. Internally, it installs a
	 * {@link DocumentListener} on the text component's {@link Document}, and a
	 * {@link PropertyChangeListener} on the text component to detect if the
	 * {@code Document} itself is replaced.
	 *
	 * @param text           any text component, such as a {@link JTextField} or
	 *                       {@link JTextArea}
	 * @param changeListener a listener to receieve {@link ChangeEvent}s when the
	 *                       text is changed; the source object for the events will
	 *                       be the text component
	 * @throws NullPointerException if either parameter is null
	 */
	public void addChangeListener(final ChangeListener changeListener) {
		final JTextComponent text = this;
		Objects.requireNonNull(text);
		Objects.requireNonNull(changeListener);
		final DocumentListener dl = new DocumentListener() {
			private int lastChange = 0, lastNotifiedChange = 0;

			@Override
			public void insertUpdate(final DocumentEvent e) {
				changedUpdate(e);
			}

			@Override
			public void removeUpdate(final DocumentEvent e) {
				changedUpdate(e);
			}

			@Override
			public void changedUpdate(final DocumentEvent e) {
				lastChange++;
				SwingUtilities.invokeLater(() -> {
					if (lastNotifiedChange != lastChange) {
						lastNotifiedChange = lastChange;
						changeListener.stateChanged(new ChangeEvent(text));
					}
				});
			}
		};
		text.addPropertyChangeListener("document", (final PropertyChangeEvent e) -> {
			final Document d1 = (Document) e.getOldValue();
			final Document d2 = (Document) e.getNewValue();
			if (d1 != null) {
				d1.removeDocumentListener(dl);
			}
			if (d2 != null) {
				d2.addDocumentListener(dl);
			}
			dl.changedUpdate(null);
		});
		final Document d = text.getDocument();
		if (d != null) {
			d.addDocumentListener(dl);
		}
	}

}
