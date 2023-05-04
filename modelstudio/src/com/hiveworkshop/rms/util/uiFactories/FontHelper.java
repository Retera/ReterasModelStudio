package com.hiveworkshop.rms.util.uiFactories;

import java.awt.*;

public class FontHelper {
	public static Font get(String fontName, int style, float size){
		return new Font(fontName, style, (int) size);
	}
	public static Container set(Container container, String fontName, Integer style, Float size){
		Font currFont = container.getFont();
		int st = style == null ? currFont.getStyle() : style;
		int sz = size == null ? currFont.getSize() : size.intValue();
		container.setFont(new Font(fontName, st, sz));
		return container;
	}
	public static Container set(Container container, Integer style, Float size){
		Font currFont = container.getFont();
		int st = style == null ? currFont.getStyle() : style;
		float sz = size == null ? currFont.getSize() : size;
		container.setFont(currFont.deriveFont(st, sz));
		return container;
	}
}
