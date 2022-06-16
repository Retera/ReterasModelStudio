package com.hiveworkshop.rms.ui.application.model.editors;

import com.jtattoo.plaf.BaseSpinnerUI;

import javax.swing.*;
import javax.swing.text.DefaultFormatter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
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

	public FloatEditorJSpinner(float value, float minValue, float stepSize) {
		this(value, minValue, stepSize, null);
	}

	public FloatEditorJSpinner(float value, float minValue, float stepSize, Consumer<Float> floatConsumer) {
		this(value, minValue, (float) Integer.MAX_VALUE, stepSize, floatConsumer);
	}
	public FloatEditorJSpinner(float value, float minValue, float maxValue, float stepSize, Consumer<Float> floatConsumer) {
		super(new SpinnerNumberModel(value, minValue, maxValue, stepSize));
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


		textField.addFocusListener(new TwiFocusListener(textField, this::runEditingStoppedListener));
		textField.addKeyListener(getSaveOnEnterKeyListener());
		for(Component component : getComponents()){
			if(component instanceof BaseSpinnerUI.SpinButton){
				((BaseSpinnerUI.SpinButton) component).addActionListener(e -> addSaveChangeTimer2());
			}
		}
	}

	public void addSaveChangeTimer2() {
		if(saveChangeTimer == null && floatConsumer != null) {
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
		if (floatConsumer != null){
			((DefaultEditor) getEditor()).getTextField().setForeground(unsavedFg);
			((DefaultEditor) getEditor()).getTextField().setBackground(unsavedBg);
			saveAtTime = System.currentTimeMillis() + 300;
		}
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
	public FloatEditorJSpinner setFloatEditingStoppedListener(Consumer<Float> floatConsumer) {
		this.floatConsumer = floatConsumer;
		return this;
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
