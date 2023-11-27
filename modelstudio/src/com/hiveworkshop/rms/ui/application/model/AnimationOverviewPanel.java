package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class AnimationOverviewPanel extends OverviewPanel {
	private final JPanel animInfoTable;
	public AnimationOverviewPanel(ModelHandler modelHandler) {
		super(modelHandler, new MigLayout("fill, ins 0", "[grow]", "[grow]"));
		animInfoTable = new JPanel(new MigLayout("wrap 7", "[]10[Right]10[Right]10[Right]10[Right]10[Right]10[Right]", ""));
		fillAnimTable();
		JScrollPane scrollPane = new JScrollPane(animInfoTable);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		add(scrollPane, "growx, growy");
	}

	@Override
	public void update() {
		animInfoTable.removeAll();
		fillAnimTable();
		revalidate();
		repaint();
	}

	private void fillAnimTable(){
		animInfoTable.add(new JLabel("Animation"));
		animInfoTable.add(new JLabel("Start"));
		animInfoTable.add(new JLabel("End"));
		animInfoTable.add(new JLabel("Length"));
		animInfoTable.add(new JLabel("Rarity"));
		animInfoTable.add(new JLabel("MoveSpeed"));
		animInfoTable.add(new JLabel("NonLooping"));

		for (Animation animation : modelHandler.getModel().getAnims()) {
			animInfoTable.add(new JLabel("" + animation));
			animInfoTable.add(new JLabel("" + animation.getStart()));
			animInfoTable.add(new JLabel("" + animation.getEnd()));
			animInfoTable.add(new JLabel("" + animation.getLength()));
			animInfoTable.add(new JLabel("" + animation.getRarity()));
			animInfoTable.add(new JLabel("" + animation.getMoveSpeed()));
			animInfoTable.add(new JLabel("" + animation.isNonLooping()));
		}
	}
}
