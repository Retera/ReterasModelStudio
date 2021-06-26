package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class AnimationOverviewPanel extends JPanel {

	public AnimationOverviewPanel(ModelHandler modelHandler) {
		super(new MigLayout("fill, ins 0", "[grow]", "[grow]"));
		JPanel panel = new JPanel(new MigLayout("wrap 7", "[]10[Right]10[Right]10[Right]10[Right]10[Right]10[Right]", ""));

		panel.add(new JLabel("Animation"));
		panel.add(new JLabel("Start"));
		panel.add(new JLabel("End"));
		panel.add(new JLabel("Length"));
		panel.add(new JLabel("Rarity"));
		panel.add(new JLabel("MoveSpeed"));
		panel.add(new JLabel("NonLooping"));

		for (Animation animation : modelHandler.getModel().getAnims()) {
			panel.add(new JLabel(animation.getName()));
			panel.add(new JLabel("" + animation.getStart()));
			panel.add(new JLabel("" + animation.getEnd()));
			panel.add(new JLabel("" + animation.length()));
			panel.add(new JLabel("" + animation.getRarity()));
			panel.add(new JLabel("" + animation.getMoveSpeed()));
			panel.add(new JLabel("" + animation.isNonLooping()));
		}
		JScrollPane scrollPane = new JScrollPane(panel);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		add(scrollPane, "growx, growy");
	}
}
