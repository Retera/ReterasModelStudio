package com.hiveworkshop.rms.ui.gui.modeledit.toolbar;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import java.awt.*;
import java.util.function.Consumer;

public class ToolbarButton2 <T extends ToolbarButtonType> {
	private final JButton toolbarButton;
	private final ModeButton2 modeButton2;
	private final Border defaultBorder = BorderFactory.createLineBorder(new Color(0,0,0,0), 2);


	public ToolbarButton2(T buttonType, Consumer<T> typeConsumer){
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

		modeButton2 = new ModeButton2(buttonType.getName(), ProgramGlobals.getPrefs().getActiveColor1(), ProgramGlobals.getPrefs().getActiveColor2());
		modeButton2.setToolTipText(buttonType.getName());
		modeButton2.addActionListener(e -> typeConsumer.accept(buttonType));
	}

	public void setActive(boolean active){
		if(active){
			toolbarButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
//			toolbarButton.setBorder(defaultBorder);
		} else {
			toolbarButton.setBorder(defaultBorder);
//			toolbarButton.setBorder(null);
//			toolbarButton.setBorder(BorderFactory.createLineBorder(new Color(0,0,0,0)));
		}
		modeButton2.setActive(active);
	}

	public JButton getToolbarButton(){
		return toolbarButton;
	}

	public ModeButton2 getModeButton2(){
		return modeButton2;
	}
}
