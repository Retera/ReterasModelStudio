package com.hiveworkshop.rms.ui.application.model.editors;

import javax.swing.*;
import javax.swing.event.CaretListener;
import javax.swing.text.DefaultFormatter;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.LocalTime;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

public class IntEditorJSpinner extends JSpinner {
	public static final Color SAVED_FG = Color.BLACK;
	public static final Color SAVED_BG = Color.WHITE;
	public static final Color UNSAVED_FG = Color.MAGENTA.darker();
	public static final Color UNSAVED_BG = Color.LIGHT_GRAY;
	Consumer<Integer> intConsumer;

	public IntEditorJSpinner(int value, Consumer<Integer> intConsumer) {
		this(value, 0, intConsumer);
	}

	public IntEditorJSpinner(int value, int minValue, Consumer<Integer> intConsumer) {
		super(new SpinnerNumberModel(value, minValue, Integer.MAX_VALUE, 1));
		this.intConsumer = intConsumer;
		init();
	}

	private void init() {
		addChangeListener(e -> setColors(UNSAVED_FG, UNSAVED_BG));
		final JFormattedTextField textField = ((DefaultEditor) getEditor()).getTextField();
		final DefaultFormatter formatter = (DefaultFormatter) textField.getFormatter();
		formatter.setCommitsOnValidEdit(true);


		textField.addFocusListener(getFocusAdapter(textField));
		textField.addKeyListener(getSaveOnEnterKeyListener());
	}

	private void setColors(Color unsavedFg, Color unsavedBg) {
		((DefaultEditor) getEditor()).getTextField().setForeground(unsavedFg);
		((DefaultEditor) getEditor()).getTextField().setBackground(unsavedBg);
	}

	public IntEditorJSpinner reloadNewValue(final Object value) {
		setValue(value);
		setColors(SAVED_FG, SAVED_BG);
		return this;
	}

	/**
	 * Uses a FocusListener to execute the runnable on focus lost
	 * or if no caret action was detected in the last 5 minutes
	 */
	public IntEditorJSpinner addIntEditingStoppedListener(Consumer<Integer> intConsumer) {
		this.intConsumer = intConsumer;
		return this;
	}

	private FocusAdapter getFocusAdapter(JFormattedTextField textField) {
		return new FocusAdapter() {
			public Timer timer;
			LocalTime lastEditedTime = LocalTime.now();
			final CaretListener caretListener = e -> lastEditedTime = LocalTime.now();
			TimerTask timerTask;

			public void addTimer() {
				timerTask = new TimerTask() {
					@Override
					public void run() {
						if (LocalTime.now().isAfter(lastEditedTime.plusSeconds(300))) {
							runEditingStopedListener();
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
				runEditingStopedListener();
			}
		};
	}

	private KeyAdapter getSaveOnEnterKeyListener() {
		return new KeyAdapter() {
			@Override
			public void keyReleased(final KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					runEditingStopedListener();
				}
			}
		};
	}

	private void runEditingStopedListener() {
		if (intConsumer != null) {
			intConsumer.accept(getIntValue());
		}
		setColors(SAVED_FG, SAVED_BG);
	}

	public int getIntValue() {
		return (Integer) getValue();
	}

}
