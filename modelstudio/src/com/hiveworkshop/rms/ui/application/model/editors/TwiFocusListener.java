package com.hiveworkshop.rms.ui.application.model.editors;

import javax.swing.*;
import javax.swing.event.CaretListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.time.LocalTime;
import java.util.Timer;
import java.util.TimerTask;

public class TwiFocusListener extends FocusAdapter {
	private Timer timer;
	private LocalTime lastEditedTime = LocalTime.now();
	private final Runnable editingStoppedListener;
	private final JTextField textField;
	private int delay = 500;
	private int lastEditedExtend = 1000;

	public TwiFocusListener(JTextField textField, Runnable editingStoppedListener){
		this.textField = textField;
		this.editingStoppedListener = editingStoppedListener;
	}

	public void startTimer() {
		timer = new Timer();
		timer.schedule(getTimerTask(), delay, delay);
	}

	public TwiFocusListener setDelay(int delay) {
		this.delay = delay;
		return this;
	}

	public boolean isTimerRunning(){
		return timer != null;
	}

	public TwiFocusListener setLastEditedExtend(int lastEditedExtend) {
		this.lastEditedExtend = lastEditedExtend;
		return this;
	}

	public void removeTimer() {
		if(timer != null){
			timer.cancel();
			timer = null;
		}
	}

	private TimerTask getTimerTask() {
		return new TimerTask() {
			@Override
			public void run() {
				if (LocalTime.now().isAfter(lastEditedTime)) {
					System.out.println("#Focus timerTask save");
					editingStoppedListener.run();
				}
			}
		};
	}

	@Override
	public void focusGained(FocusEvent e) {
		System.out.println("#Focus gained");
		textField.addCaretListener(ec -> updateEditedTime());
		startTimer();
	}

	@Override
	public void focusLost(FocusEvent e) {
		System.out.println("#Focus lost -> save");
		removeTimer();
		for (CaretListener cl : textField.getCaretListeners()) {
			textField.removeCaretListener(cl);
		}
		super.focusLost(e);
		editingStoppedListener.run();
	}

	private void updateEditedTime(){
//		System.out.println("#Focus updating time");
		lastEditedTime = LocalTime.now().plusNanos(lastEditedExtend * 1000000L);
	}
}
