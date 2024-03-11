package com.hiveworkshop.rms.ui.preferences;

import java.awt.event.InputEvent;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

public class Nav3DMousePrefs {
	Map<Nav3DMouseAction, Integer> mouseActionMap = new EnumMap<>(Nav3DMouseAction.class);

	public Nav3DMousePrefs() {
		for (Nav3DMouseAction mouseAction : Nav3DMouseAction.values()) {
			mouseActionMap.put(mouseAction, mouseAction.getInternalMouseEx());
		}
	}

	public Integer getKeyStroke(Nav3DMouseAction mouseAction) {
		if (mouseActionMap.get(mouseAction) != null) {
			return mouseActionMap.get(mouseAction);
		}
		return mouseAction.getInternalMouseEx();
	}

	public Map<Nav3DMouseAction, Integer> getMouseActionMap() {
		return mouseActionMap;
	}

	public Nav3DMousePrefs setKeyStroke(Nav3DMouseAction mouseAction, Integer mouseEx) {
		mouseActionMap.put(mouseAction, mouseEx);
		return this;
	}

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("\n");
		for (Nav3DMouseAction mouseAction : Nav3DMouseAction.values()) {
			Integer mouseEx = mouseActionMap.get(mouseAction) == null ? mouseAction.getInternalMouseEx() : mouseActionMap.get(mouseAction);
//			System.out.println(mouseAction.name() + " = [" + MouseEvent.getModifiersExText(mouseEx) + "] -alt- [" + getHumanReadable(mouseEx) + "]");
			stringBuilder.append(mouseAction.name())
					.append(" = ")
					.append(getHumanReadable(mouseEx))
					.append("\n");
		}
		return stringBuilder.toString();
	}

	public Nav3DMousePrefs parseString(String string) {
		String[] lines = string.split("\n");
		for (String line : lines) {
			String[] s = line.split("=");
//			System.out.println("parsing: \"" + line + "\"");
			if (1 < s.length) {
				try {
					Nav3DMouseAction mouseAction = Nav3DMouseAction.valueOf(s[0].strip());

					int mouseEx;
					if (s[1].strip().matches("\\d+")) {
						mouseEx = parseIntString(s[1]);
					} else {
						mouseEx = fromHumanReadable(s[1]);
					}
					mouseActionMap.put(mouseAction, mouseEx);
				} catch (Exception e) {
					System.out.println("failed to parse mouseAction \"" + line + "\":");
					System.out.println(e.getMessage());
				}
			}
		}
		return this;
	}


	public Nav3DMousePrefs setFrom(Nav3DMousePrefs other) {
		for (Nav3DMouseAction mouseAction : other.mouseActionMap.keySet()) {
			mouseActionMap.put(mouseAction, other.getKeyStroke(mouseAction));
		}
		return this;
	}

	private int parseIntString(String string) {
		String s = string.replaceAll("\\D", "");
		if (s.matches("\\d+")) {
			return Integer.parseInt(s);
		}
		return 0;
	}



	private String getHumanReadable(Integer mouseEx){
		StringBuilder buf = new StringBuilder();
		if ((mouseEx & InputEvent.ALT_DOWN_MASK) != 0) {
			buf.append("ALT");
			buf.append("+");
		}
		if ((mouseEx & InputEvent.META_DOWN_MASK) != 0) {
			buf.append("META");
			buf.append("+");
		}
		if ((mouseEx & InputEvent.CTRL_DOWN_MASK) != 0) {
			buf.append("CTRL");
			buf.append("+");
		}
		if ((mouseEx & InputEvent.SHIFT_DOWN_MASK) != 0) {
			buf.append("SHIFT");
			buf.append("+");
		}
		if ((mouseEx & InputEvent.ALT_GRAPH_DOWN_MASK) != 0) {
			buf.append("ALT_GRAPH");
			buf.append("+");
		}
		if ((mouseEx & InputEvent.BUTTON1_DOWN_MASK) != 0) {
			buf.append("BUTTON1");
			buf.append("+");
		}
		if ((mouseEx & InputEvent.BUTTON2_DOWN_MASK) != 0) {
			buf.append("BUTTON2");
			buf.append("+");
		}
		if ((mouseEx & InputEvent.BUTTON3_DOWN_MASK) != 0) {
			buf.append("BUTTON3");
			buf.append("+");
		}
		return buf.deleteCharAt(buf.length()-1).toString();
	}

	private Integer fromHumanReadable(String s){
		String[] buttons = s.toUpperCase(Locale.ROOT).split("\\+");
		int mouseEx = 0;
		for (String button : buttons) {
			mouseEx |= switch (button.strip()) {
				case "ALT" -> InputEvent.ALT_DOWN_MASK;
				case "META" -> InputEvent.META_DOWN_MASK;
				case "CTRL" -> InputEvent.CTRL_DOWN_MASK;
				case "SHIFT" -> InputEvent.SHIFT_DOWN_MASK;
				case "ALT_GRAPH" -> InputEvent.ALT_GRAPH_DOWN_MASK;
				case "BUTTON1" -> InputEvent.BUTTON1_DOWN_MASK;
				case "BUTTON2" -> InputEvent.BUTTON2_DOWN_MASK;
				case "BUTTON3" -> InputEvent.BUTTON3_DOWN_MASK;
				default -> 0;
			};
		}
		return mouseEx;
	}
}
