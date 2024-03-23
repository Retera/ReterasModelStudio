package com.hiveworkshop.rms.ui.util;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.preferences.UiElementColor;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class ModeButton extends JButton {
	Color activeColor;
	Color currBG;

	public ModeButton(String s, Color activeColor) {
		super(s);
		this.activeColor = new Color(activeColor.getRGB());
		currBG = getBackground();
	}

	public ModeButton(String s) {
		super(s);
		this.activeColor = ProgramGlobals.getPrefs().getUiElementColorPrefs().getColor(UiElementColor.ACTIVE_MODE_BUTTON);
//		System.out.println("active color: " + activeColor);
		currBG = getBackground();
	}

	public ModeButton addListener(Runnable runnable) {
		addActionListener(e -> runnable.run());
		return this;
	}

	public ModeButton addConsumer(Consumer<ModeButton> consumer) {
		addActionListener(e -> consumer.accept(this));
		return this;
	}

	public void setActive(boolean active) {
		if (active) {
			setBackground(activeColor);
		} else {
			setBackground(currBG);
		}
		repaint();
	}
}
