package com.hiveworkshop.rms.ui.preferences;

import com.hiveworkshop.rms.ui.application.actionfunctions.ActionFunction;
import com.hiveworkshop.rms.ui.language.TextKey;

import javax.swing.*;
import java.io.Serializable;
import java.util.EnumMap;
import java.util.Map;

public class KeyBindingPrefs implements Serializable {
	private static final Map<TextKey, ActionFunction> actionFunctionMap = new EnumMap<>(TextKey.class);

	public static void addActionFunction(TextKey textKey, ActionFunction function) {
		actionFunctionMap.put(textKey, function);
	}

	public KeyStroke getKeyStroke(TextKey key) {
		return actionFunctionMap.get(key).getKeyStroke();
	}

	public void setKeyStroke(TextKey key, KeyStroke keyStroke) {
		actionFunctionMap.get(key).setKeyStroke(keyStroke);
	}

	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		for (ActionFunction action : actionFunctionMap.values()) {
			stringBuilder.append(action.getName().name()).append("=").append(action.getKeyStroke()).append("\n");

		}
		return stringBuilder.toString();
	}

	public KeyBindingPrefs parseString(String string) {
		String[] lines = string.split("\n");
		for (String line : lines) {
			String[] s = line.split("=");
//			System.out.println("parsing: \"" + line + "\"");
			if (1 < s.length) {
				try {
					TextKey textKey = TextKey.valueOf(s[0].strip());
					KeyStroke keyStroke = KeyStroke.getKeyStroke(s[1]);
					ActionFunction actionFunction = actionFunctionMap.get(textKey);
					if (actionFunction != null) {
						actionFunction.setKeyStroke(keyStroke);
					}
				} catch (Exception e) {
					System.out.println("failed to parse keybinding \"" + line + "\":");
//					System.out.println(e.getCause().getMessage());
					System.out.println(e.getMessage());
				}
			}
		}
		return this;
	}


	public ActionMap getActionMap() {
		ActionMap actionMap = new ActionMap();
		for (ActionFunction function : actionFunctionMap.values()) {
			actionMap.put(function.getName(), function.getAction());
		}
//		for (ActionMapActions action : ActionMapActions.values()) {
//			actionMap.put(action.getName(), action.getAction());
//		}
		return actionMap;
	}

	public InputMap getInputMap() {
		InputMap inputMap = new InputMap();
		for (ActionFunction function : actionFunctionMap.values()) {
			if (function.getKeyStroke() != null) {
				inputMap.put(function.getKeyStroke(), function.getName());
			}
		}
		return inputMap;
	}

	public ActionMap addActionMap(JComponent component) {
		ActionMap actionMap = component.getActionMap();
		for (ActionFunction function : actionFunctionMap.values()) {
			actionMap.put(function.getName(), function.getAction());
		}
		return actionMap;
	}

	public InputMap addInputMap(JComponent component) {
		InputMap inputMap = component.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		for (ActionFunction function : actionFunctionMap.values()) {
			if (function.getKeyStroke() != null) {
				inputMap.put(function.getKeyStroke(), function.getName());
			}
		}
		return inputMap;
	}

	public static Map<TextKey, ActionFunction> getActionFunctionMap() {
		return actionFunctionMap;
	}

	//	public void setNullToDefaults() {
//		KeyBindingPrefs2 defaultPrefs = new KeyBindingPrefs2().makeMap();
//
//		Field[] declaredFields = this.getClass().getDeclaredFields();
//		for (Field field : declaredFields) {
//			try {
//				if (field.get(this) == null) {
//					field.set(this, field.get(defaultPrefs));
//				}
//
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//		makeMap();
//		for (ActionMapActions action : ActionMapActions.values()) {
//			keyStrokeMap.computeIfAbsent(action, k -> defaultPrefs.keyStrokeMap.get(action));
//		}
//	}
}
