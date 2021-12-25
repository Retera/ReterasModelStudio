package com.hiveworkshop.rms.ui.application.model.editors;

import com.jtattoo.plaf.BaseSpinnerUI;

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
	private static final Color SAVED_FG = Color.BLACK;
	private static final Color SAVED_BG = Color.WHITE;
	private static final Color UNSAVED_FG = Color.MAGENTA.darker();
	private static final Color UNSAVED_BG = Color.LIGHT_GRAY;
	private Consumer<Integer> intConsumer;
	private long saveAtTime = 0;
	private Timer saveChangeTimer;

	public IntEditorJSpinner(int value, Consumer<Integer> intConsumer) {
		this(value, 0, intConsumer);
	}

	public IntEditorJSpinner(int value, int minValue, Consumer<Integer> intConsumer) {
		super(new SpinnerNumberModel(value, minValue, Integer.MAX_VALUE, 1));
		this.intConsumer = intConsumer;
		init();
	}

	public IntEditorJSpinner(int value, int minValue, int maxValue, Consumer<Integer> intConsumer) {
		super(new SpinnerNumberModel(value, minValue, maxValue, 1));
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
		for(Component component : getComponents()){
			if(component instanceof BaseSpinnerUI.SpinButton){
				((BaseSpinnerUI.SpinButton) component).addActionListener(e -> addSaveChangeTimer2());
			}
		}
	}

	private void setColors(Color unsavedFg, Color unsavedBg) {
		((DefaultEditor) getEditor()).getTextField().setForeground(unsavedFg);
		((DefaultEditor) getEditor()).getTextField().setBackground(unsavedBg);
		saveAtTime = System.currentTimeMillis() + 300;

	}

	public void addSaveChangeTimer2() {
		if(saveChangeTimer == null) {
			saveChangeTimer = new Timer();
			TimerTask saveChangeTimerTask;
			saveChangeTimerTask = new TimerTask() {
				@Override
				public void run() {
					saveChange();
				}
			};
			saveChangeTimer.schedule(saveChangeTimerTask, 100, 100);
		}
	}

	private void saveChange() {
		if (saveAtTime < System.currentTimeMillis()) {
			runEditingStoppedListener();
			saveChangeTimer.cancel();
			saveChangeTimer = null;
		}
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
						if (LocalTime.now().isAfter(lastEditedTime.plusSeconds(1))) {
							runEditingStoppedListener();
						}
					}
				};
				timer = new Timer();
				timer.schedule(timerTask, 500, 500);
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
				runEditingStoppedListener();
			}
		};
	}

	private KeyAdapter getSaveOnEnterKeyListener() {
		return new KeyAdapter() {
			@Override
			public void keyReleased(final KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					runEditingStoppedListener();
				}
			}
		};
	}

	private void runEditingStoppedListener() {
		if (intConsumer != null) {
			intConsumer.accept(getIntValue());
		}
		setColors(SAVED_FG, SAVED_BG);
	}

	public int getIntValue() {
		return (Integer) getValue();
	}

}
