package com.hiveworkshop.rms.util;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class ActionMapBindingsUgg {

	public ActionMapBindingsUgg(){
//		KeyStroke keyStroke = KeyStroke.getKeyStrokeForEvent();
		KeyListener keyListener = new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {

			}

			@Override
			public void keyPressed(KeyEvent e) {
				e.paramString();

				KeyStroke keyStroke = KeyStroke.getKeyStrokeForEvent(e);
			}

			@Override
			public void keyReleased(KeyEvent e) {

			}
		};
	}
}
