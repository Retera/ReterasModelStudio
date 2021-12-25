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

public class FloatEditorJSpinner extends JSpinner {
	private static final Color SAVED_FG = Color.BLACK;
	private static final Color SAVED_BG = Color.WHITE;
	private static final Color UNSAVED_FG = Color.MAGENTA.darker();
	private static final Color UNSAVED_BG = Color.LIGHT_GRAY;
	private Consumer<Float> floatConsumer;
	private long saveAtTime = 0;
	private Timer saveChangeTimer;

	public FloatEditorJSpinner(float value, Consumer<Float> floatConsumer) {
		this(value, 0f, 1.0f, floatConsumer);
	}

	public FloatEditorJSpinner(float value, float minValue, Consumer<Float> floatConsumer) {
		this(value, minValue, 1.0f, floatConsumer);
	}

	public FloatEditorJSpinner(float value, float minValue, float stepSize, Consumer<Float> floatConsumer) {
		super(new SpinnerNumberModel(value, minValue, (float) Integer.MAX_VALUE, stepSize));
		this.floatConsumer = floatConsumer;
		init();
	}

	private void init() {
		addChangeListener(e -> {
			setColors(UNSAVED_FG, UNSAVED_BG);
		});
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

	private void setColors(Color unsavedFg, Color unsavedBg) {
		((DefaultEditor) getEditor()).getTextField().setForeground(unsavedFg);
		((DefaultEditor) getEditor()).getTextField().setBackground(unsavedBg);
		saveAtTime = System.currentTimeMillis() + 300;
	}

	public FloatEditorJSpinner reloadNewValue(final Object value) {
		setValue(value);
		setColors(SAVED_FG, SAVED_BG);
		return this;
	}

	/**
	 * Uses a FocusListener to execute the runnable on focus lost
	 * or if no caret action was detected in the last 5 minutes
	 */
	public FloatEditorJSpinner addFloatEditingStoppedListener(Consumer<Float> floatConsumer) {
		this.floatConsumer = floatConsumer;
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
		if (floatConsumer != null) {
			floatConsumer.accept(getFloatValue());
		}
		setColors(SAVED_FG, SAVED_BG);
	}


	public float getFloatValue() {
		if (getValue().getClass().equals(Float.class)) {
			return (float) getValue();
		}
		return (float) ((double) getValue());
	}

}
