package com.hiveworkshop.rms.ui.preferences;

import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.MetalTheme;
import java.awt.*;
import java.util.function.Function;

public enum UiElementColor {
	ACTIVE_MODE_BUTTON(MetalTheme::getFocusColor, new Color(255, 128, 32)),
	LIST_ENTRY_EDITED(MetalTheme::getFocusColor, new Color(240, 240, 80)),
	LIST_ENTRY_UNAVAILABLE(MetalTheme::getControlDarkShadow, new Color(180, 80, 80)),
	LIST_ENTRY_DISABLED(MetalTheme::getControlDisabled, new Color(120, 120, 120)),
	;

	final Function<MetalTheme, Color> colorFetcher;
	final Color internalFallbackColor;

	UiElementColor(Function<MetalTheme, Color> colorFetcher, Color fbColor) {
		this.colorFetcher = colorFetcher;
		internalFallbackColor = fbColor;
	}

	public Color getInternalColor() {
		MetalTheme currentTheme = MetalLookAndFeel.getCurrentTheme();
		if (currentTheme != null) {
			Color color = colorFetcher.apply(currentTheme);
			if (color != null) {
				return new Color(color.getRGB());
			}
		}
		return internalFallbackColor;
	}

	public String getTextKeyString() {
		return name();
	}
}
