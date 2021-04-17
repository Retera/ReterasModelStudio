package com.hiveworkshop.rms.ui.application.model.editors;

import javax.swing.*;
import javax.swing.event.CaretListener;
import javax.swing.text.DefaultFormatter;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.LocalTime;
import java.util.Timer;
import java.util.TimerTask;

public class ComponentEditorJSpinner extends JSpinner {

	public ComponentEditorJSpinner() {
		super();
		init();
	}

	public ComponentEditorJSpinner(final SpinnerModel model) {
		super(model);
		init();
	}

	private void init() {
		addChangeListener(e -> {
			((DefaultEditor) getEditor()).getTextField().setForeground(UnsavedChangesDocumentListener.UNSAVED_FOREGROUND_COLOR);
			((DefaultEditor) getEditor()).getTextField().setBackground(UnsavedChangesDocumentListener.UNSAVED_BACKGROUND_COLOR);
		});
		final JFormattedTextField textField = ((JSpinner.DefaultEditor) getEditor()).getTextField();
		final DefaultFormatter formatter = (DefaultFormatter) textField.getFormatter();
		formatter.setCommitsOnValidEdit(true);
//		textField.getDocument().addDocumentListener(new UnsavedChangesDocumentListener(textField));
	}

	public void reloadNewValue(final Object value) {
		setValue(value);
		((JSpinner.DefaultEditor) getEditor()).getTextField().setForeground(UnsavedChangesDocumentListener.SAVED_FOREGROUND_COLOR);
		((JSpinner.DefaultEditor) getEditor()).getTextField().setBackground(UnsavedChangesDocumentListener.SAVED_BACKGROUND_COLOR);
	}

	/**
	 * Uses a FocusListener to execute the runnable on focus lost
	 * or if no caret action was detected in the last 5 minutes
	 */
	public void addEditingStoppedListener(final Runnable runnable) {
		final JFormattedTextField textField = ((JSpinner.DefaultEditor) getEditor()).getTextField();

		textField.addFocusListener(new FocusAdapter() {
			public java.util.Timer timer;
			LocalTime lastEditedTime = LocalTime.now();
			final CaretListener caretListener = e -> lastEditedTime = LocalTime.now();
			TimerTask timerTask;

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
				textField.addCaretListener(caretListener);
				addTimer();
			}

			@Override
			public void focusLost(FocusEvent e) {
				removeTimer();
				for (CaretListener cl : textField.getCaretListeners()) {
					textField.removeCaretListener(cl);
				}
				super.focusLost(e);
				runnable.run();
			}
		});
	}

	public void addActionListener(final Runnable runnable) {
		final JFormattedTextField textField = ((JSpinner.DefaultEditor) getEditor()).getTextField();
		textField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(final KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					runnable.run();
				}
			}
		});
	}

	public float getFloatValue() {
		if (getValue().getClass().equals(Float.class)) {
			return (float) getValue();
		}
		return (float) ((double) getValue());
	}

	public double getDoubleValue() {
		return (double) getValue();
	}

	public int getIntValue() {
		return (Integer) getValue();
	}

}
