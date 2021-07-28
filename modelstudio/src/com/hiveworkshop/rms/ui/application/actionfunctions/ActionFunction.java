package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.language.TextKey;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public abstract class ActionFunction {
	protected KeyStroke keyStroke;
	protected final AbstractAction action;
	protected final TextKey name;
	protected JMenuItem menuItem;

	private static final Map<TextKey, ActionFunction> actionFunctionMap = new HashMap<>();

	public ActionFunction(TextKey name, Runnable runnable){
		this.name = name;
		this.action = getAsAction(runnable);
		actionFunctionMap.put(name, this);
		menuItem = new JMenuItem(this.action);
	}
	public ActionFunction(TextKey name, Consumer<ModelPanel> consumer){
		this.name = name;
		this.action = getAsAction(consumer);
		actionFunctionMap.put(name, this);
		menuItem = new JMenuItem(this.action);
	}
	public ActionFunction(TextKey name, AbstractAction action){
		this.name = name;
		this.action = action;
		actionFunctionMap.put(name, this);
		menuItem = new JMenuItem(this.action);
	}
	public ActionFunction(TextKey name, Runnable runnable, KeyStroke keyStroke){
		this(name, runnable);
		setKeyStroke(keyStroke);
	}
	public ActionFunction(TextKey name, Runnable runnable, String keyStroke){
		this(name, runnable);
		setKeyStroke(keyStroke);
	}

	//KeyEvent.VK_X
	public ActionFunction setMenuItemMnemonic(int keyEvent) {
		menuItem.setMnemonic(keyEvent);
		return this;
	}
	public ActionFunction setMenuItemMnemonic(char keyEvent) {
		menuItem.setMnemonic(keyEvent);
		return this;
	}

	public ActionFunction setKeyStroke(KeyStroke keyStroke) {
		this.keyStroke = keyStroke;
		menuItem.setAccelerator(this.keyStroke);
		return this;
	}
	public ActionFunction setKeyStroke(String keyStroke) {
		return setKeyStroke(KeyStroke.getKeyStroke(keyStroke));
	}
	public ActionFunction setKeyStroke(KeyEvent keyEvent) {
		return setKeyStroke(KeyStroke.getKeyStrokeForEvent(keyEvent));
	}

	public KeyStroke getKeyStroke() {
		return keyStroke;
	}

	public JMenuItem getMenuItem() {
		return menuItem;
	}

	private AbstractAction getAsAction(Runnable runnable) {
		return new AbstractAction(name.toString()) {
			@Override
			public void actionPerformed(ActionEvent e) {
				runnable.run();
			}
		};
	}
	private AbstractAction getAsAction(Consumer<ModelPanel> consumer) {
		return new AbstractAction(name.toString()) {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(ProgramGlobals.getCurrentModelPanel() != null){
					consumer.accept(ProgramGlobals.getCurrentModelPanel());
				}
			}
		};
	}
}
