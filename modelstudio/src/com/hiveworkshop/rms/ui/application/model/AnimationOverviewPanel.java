package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class AnimationOverviewPanel extends JPanel {

	public AnimationOverviewPanel(ModelHandler modelHandler) {
		super(new MigLayout("wrap 7", "[]10[Right]10[Right]10[Right]10[Right]10[Right]10[Right]", ""));
		add(new JLabel("Animation"));
		add(new JLabel("Start"));
		add(new JLabel("End"));
		add(new JLabel("Length"));
		add(new JLabel("Rarity"));
		add(new JLabel("MoveSpeed"));
		add(new JLabel("NonLooping"));

		for (Animation animation : modelHandler.getModel().getAnims()) {
			add(new JLabel(animation.getName()));
			add(new JLabel("" + animation.getStart()));
			add(new JLabel("" + animation.getEnd()));
			add(new JLabel("" + animation.length()));
			add(new JLabel("" + animation.getRarity()));
			add(new JLabel("" + animation.getMoveSpeed()));
			add(new JLabel("" + animation.isNonLooping()));
		}
	}
}
