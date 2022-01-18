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
		this(value, minValue, Integer.MAX_VALUE, intConsumer);
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


		textField.addFocusListener(new TwiFocusListener(textField, this::runEditingStoppedListener));
		textField.addKeyListener(getSaveOnEnterKeyListener());
		for(Component component : getComponents()){
			if(component instanceof BaseSpinnerUI.SpinButton){
				((BaseSpinnerUI.SpinButton) component).addActionListener(e -> addSaveChangeTimer2());
			}
		}
	}

	private void setColors(Color fg, Color bg) {
		((DefaultEditor) getEditor()).getTextField().setForeground(fg);
		((DefaultEditor) getEditor()).getTextField().setBackground(bg);
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
