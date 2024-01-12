package com.hiveworkshop.rms.ui.gui.modeledit.toolbar;

import com.hiveworkshop.rms.ui.util.ModeButton;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import java.awt.*;
import java.util.function.Consumer;

public class ToolbarButton2 <T extends ToolbarButtonType> {
	private final JButton toolbarButton;
	private final ModeButton modeButton;
	private final Border defaultBorder = BorderFactory.createLineBorder(new Color(0,0,0,0), 2);


	public ToolbarButton2(T buttonType, Consumer<T> typeConsumer) {
		toolbarButton = new JButton(buttonType.getImageIcon());
		toolbarButton.setHideActionText(true);
		toolbarButton.setHorizontalTextPosition(JButton.CENTER);
		toolbarButton.setVerticalTextPosition(JButton.BOTTOM);
		toolbarButton.setToolTipText(buttonType.getName());
//		toolbarButton.setToolTipText(buttonType.getName());
//		toolbarButton.setIcon(buttonType.getImageIcon());
		toolbarButton.setDisabledIcon(buttonType.getImageIcon());
		toolbarButton.addActionListener(e -> typeConsumer.accept(buttonType));
		toolbarButton.setBorder(defaultBorder);

//		defaultBorder = toolbarButton.getBorder();

		modeButton = new ModeButton(buttonType.getName());
		modeButton.setToolTipText(buttonType.getName());
		modeButton.addActionListener(e -> typeConsumer.accept(buttonType));
	}

	public void setActive(boolean active) {
		if (active) {
			toolbarButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		} else {
			toolbarButton.setBorder(defaultBorder);
		}
		modeButton.setActive(active);
	}

	public JButton getToolbarButton() {
		return toolbarButton;
	}

	public ModeButton getModeButton() {
		return modeButton;
	}
}
