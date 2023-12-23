package com.hiveworkshop.rms.ui.preferences;

import java.awt.*;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.glColor4f;

public class EditorColorPrefs {
	Map<ColorThing, Color> colorMap = new EnumMap<>(ColorThing.class);

	public EditorColorPrefs() {
		for (ColorThing thing : ColorThing.values()) {
			colorMap.put(thing, thing.getInternalColor());
		}
	}

	public Color getColor(ColorThing thing) {
		if (colorMap.get(thing) != null) {
			return colorMap.get(thing);
		}
		return thing.getInternalColor();
	}

	public float[] getColorComponents(ColorThing thing) {
		if (colorMap.get(thing) != null) {
			return colorMap.get(thing).getComponents(null);
		}
		return thing.getInternalColor().getComponents(null);
	}

	public float[] getColorComponents(ColorThing thing, float[] components) {
		if (colorMap.get(thing) != null) {
			return colorMap.get(thing).getComponents(components);
		}
		return thing.getInternalColor().getComponents(components);
	}

	public EditorColorPrefs setColor(ColorThing thing, Color color) {
		colorMap.put(thing, color);
		return this;
	}

	public EditorColorPrefs useColor(ColorThing thing) {
		Color color = colorMap.get(thing);
		float[] components = color.getComponents(null);
		glColor4f(components[0], components[1], components[2], components[3]);
		return this;
	}

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		for (ColorThing thing : ColorThing.values()) {
			stringBuilder.append(thing.name()).append(" = ");
			Color color = colorMap.get(thing) == null ? thing.getInternalColor() : colorMap.get(thing);

			stringBuilder.append("[")
					.append(color.getRed()).append(",")
					.append(color.getGreen()).append(",")
					.append(color.getBlue()).append(",")
					.append(color.getAlpha()).append("]\n");
		}
		return stringBuilder.toString();
	}

	public EditorColorPrefs parseString(String string) {
		String[] lines = string.split("\n");
		for (String line : lines) {
			String[] s = line.split("=");
			if (1 < s.length) {
				try {
					ColorThing colorThing = ColorThing.valueOf(s[0].strip());

					int[] ints = parseIntString(s[1]);
					Color color = new Color(ints[0], ints[1], ints[2], ints[3]);
					colorMap.put(colorThing, color);
				} catch (Exception e) {
					System.out.println("failed to parse color \"" + line + "\":");
					System.out.println(e.getMessage());
				}
			}
		}
		return this;
	}

	public EditorColorPrefs parseString2(String string) {
		String[] lines = string.split("\n");
		Map<String, Color> stringColorMap = new HashMap<>();
		for (String line : lines) {
			String[] s = line.split("=");
			if (1 < s.length) {
				int[] ints = parseIntString(s[1]);
				Color color = new Color(ints[0], ints[1], ints[2], ints[3]);
				stringColorMap.put(s[0].strip(), color);
			}
		}
		for (ColorThing thing : ColorThing.values()) {
			if (stringColorMap.get(thing.name()) != null) {
				colorMap.put(thing, stringColorMap.get(thing.name()));
			}
		}

		return this;
	}

	public EditorColorPrefs setFrom(EditorColorPrefs other) {
		for (ColorThing thing : other.colorMap.keySet()) {
			colorMap.put(thing, other.getColor(thing));
		}
		return this;
	}

	private int[] parseIntString(String string) {
		String s = string.replaceAll("[^#\\da-fA-F,]", "");
		int[] ints = new int[] {255, 255, 255, 255};
		if (s.matches("#[\\da-fA-F]{6,8}")) {
			for (int i = 0; i < 4 && i < s.length() / 2; i++) {
				ints[i] = Integer.parseInt(s.substring(i * 2 + 1, i * 2 + 2), 16);
			}
		} else {
			String[] split = s.replaceAll("[^\\d,]", "").split(",");
			for (int i = 0; i < 4 && i < split.length; i++) {
				ints[i] = Integer.parseInt(split[i]);
			}
		}
		return ints;
	}
}
