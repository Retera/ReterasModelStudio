package com.hiveworkshop.rms.ui.preferences;

import javax.swing.*;
import java.util.EnumMap;
import java.util.Map;

public class CameraControlPrefs {
	Map<CameraShortCut, KeyStroke> shortCutMap = new EnumMap<>(CameraShortCut.class);

	public CameraControlPrefs() {
		for (CameraShortCut shortCut : CameraShortCut.values()) {
			shortCutMap.put(shortCut, shortCut.getInternalKeyStroke());
		}
	}

	public KeyStroke getKeyStroke(CameraShortCut shortCut) {
		if (shortCutMap.get(shortCut) != null) {
			return shortCutMap.get(shortCut);
		}
		return shortCut.getInternalKeyStroke();
	}

	public Map<CameraShortCut, KeyStroke> getShortCutMap() {
		return shortCutMap;
	}

	public CameraControlPrefs setKeyStroke(CameraShortCut shortCut, KeyStroke keyStroke) {
		shortCutMap.put(shortCut, keyStroke);
		return this;
	}

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		for (CameraShortCut shortCut : CameraShortCut.values()) {
			stringBuilder.append(shortCut.name()).append(" = ");
			KeyStroke keyStroke = shortCutMap.get(shortCut) == null ? shortCut.getInternalKeyStroke() : shortCutMap.get(shortCut);

			stringBuilder.append(keyStroke).append("\n");
		}
//		System.out.println("CAMERA SHORTCUTS:\n" + stringBuilder);
		return stringBuilder.toString();
	}

	public CameraControlPrefs parseString(String string) {
		String[] lines = string.split("\n");
		for (String line : lines) {
			String[] s = line.split("=");
			if (s.length > 1) {
				try {
					CameraShortCut shortCut = CameraShortCut.valueOf(s[0].strip());

					KeyStroke keyStroke = KeyStroke.getKeyStroke(s[1]);
//					int keyCode = parseIntString(s[1]);
					shortCutMap.put(shortCut, keyStroke);
				} catch (Exception e) {
					System.out.println("failed to parse keybinding \"" + line + "\":");
					System.out.println(e.getMessage());
				}
			}
		}
		return this;
	}


	public CameraControlPrefs setFrom(CameraControlPrefs other) {
		for (CameraShortCut shortCut : other.shortCutMap.keySet()) {
			shortCutMap.put(shortCut, other.getKeyStroke(shortCut));
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
