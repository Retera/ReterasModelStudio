package com.hiveworkshop.rms.ui.preferences;

import java.util.EnumMap;
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
		for (Nav3DMouseAction mouseAction : Nav3DMouseAction.values()) {
			stringBuilder.append(mouseAction.name()).append(" = ");
			Integer mouseEx = mouseActionMap.get(mouseAction) == null ? mouseAction.getInternalMouseEx() : mouseActionMap.get(mouseAction);

			stringBuilder.append(mouseEx).append("\n");
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

//					Integer mouseEx = Integer.parseInt(s[1].strip());
//					mouseActionMap.put(mouseAction, mouseEx);
					int mouseEx = parseIntString(s[1]);
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
}
