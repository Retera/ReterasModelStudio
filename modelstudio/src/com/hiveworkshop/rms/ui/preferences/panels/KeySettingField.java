package com.hiveworkshop.rms.ui.preferences.panels;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class KeySettingField extends JTextField {
	private final KeyStroke keyStroke;
	private KeyEvent event;

	public KeySettingField(KeyStroke keyStroke) {
		super(24);
		this.keyStroke = keyStroke;
		if (keyStroke != null) {
			setText(keyStroke.toString());
		}
		setEditable(false);
		addKeyListener(getKeyAdapter());
	}

	private KeyAdapter getKeyAdapter() {
		return new KeyAdapter() {
			KeyEvent lastPressedEvent;

			@Override
			public void keyPressed(KeyEvent e) {
				lastPressedEvent = e;
				if (event == null) {
					setText(KeyStroke.getKeyStrokeForEvent(e).toString());
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (event == null) {
					event = lastPressedEvent;
				}
			}
		};
	}


	public KeyEvent getEvent() {
		return event;
	}
	public void onEdit() {
		event = null;
		setText("");
		requestFocus();
	}
	public void onRemove() {
		event = null;
		setText("");
	}
	public void onReset() {
		event = null;
		if (keyStroke != null) {
			setText(keyStroke.toString());
		} else {
			setText("");
		}
	}

	public KeyStroke getNewKeyStroke() {
		if(event != null){
			return KeyStroke.getKeyStrokeForEvent(event);
		} else {
			return KeyStroke.getKeyStroke("null");
		}
	}
}
