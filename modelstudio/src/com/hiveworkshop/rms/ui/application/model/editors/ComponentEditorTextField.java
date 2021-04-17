package com.hiveworkshop.rms.ui.application.model.editors;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeEvent;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

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
	 * Uses a FocusListener to execute the runnable on focus lost
	 * or if no caret action was detected in the last 5 minutes
	 */
	public void addEditingStoppedListener(final Runnable runnable) {
		final JTextComponent text = this;
		this.addFocusListener(new FocusAdapter() {
			public Timer timer;
			LocalTime lastEditedTime = LocalTime.now();
			TimerTask timerTask;
			CaretListener caretListener = e -> lastEditedTime = LocalTime.now();

			public void addTimer() {
				timerTask = new TimerTask() {
					@Override
					public void run() {
						if (LocalTime.now().isAfter(lastEditedTime.plusSeconds(300))) {
							runnable.run();
						}
					}
				};
				timer = new Timer();
				timer.schedule(timerTask, 2000, 2000);
			}

			public void removeTimer() {
				timer.cancel();
			}

			@Override
			public void focusGained(FocusEvent e) {
				text.addCaretListener(caretListener);
				addTimer();
			}

			@Override
			public void focusLost(FocusEvent e) {
				removeTimer();
				for (CaretListener cl : text.getCaretListeners()) {
					text.removeCaretListener(cl);
				}
				super.focusLost(e);
				runnable.run();
			}
		});
	}

	/**
	 * Installs a listener to receive notification when the text of any
	 * {@code JTextComponent} is changed. Internally, it installs a
	 * {@link DocumentListener} on the text component's {@link Document}, and a
	 * {@link ChangeListener} on the text component to detect if the
	 * {@code Document} itself is replaced.
	 *
	 * text           any text component, such as a {@link JTextField} or
	 * {@link JTextArea}
	 *
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
