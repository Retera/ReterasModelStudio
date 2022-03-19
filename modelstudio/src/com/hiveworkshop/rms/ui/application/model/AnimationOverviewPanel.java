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
			panel.add(new JLabel("" + animation.getLength()));
			panel.add(new JLabel("" + animation.getRarity()));
			panel.add(new JLabel("" + animation.getMoveSpeed()));
			panel.add(new JLabel("" + animation.isNonLooping()));
		}
		JScrollPane scrollPane = new JScrollPane(panel);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		add(scrollPane, "growx, growy");
	}

//		panel.add(new TwiSpinner());
//		add(new JLabel("\u2921"));
//		add(new JLabel("\u2B6F"));
//		add(new JLabel("\u2BA4"));
//		String[] ugg = {
//				"2921",
//				"2904",
//				"2938",
//				"293E",
//				"290D",
//				"2BA2",
//				"2BA4",
//				"2B04",
//				"2B0C",
//				"2B64",
//				"2B04",
//				"2B82",
//				"2B68",
//				"2B6E",
//				"2B6F",
//				"2B5F",
//				"2B4D",
//				"233E",
//				"2315",
//				"23FF",
//				"2BBA",
//				"2B57",
//				"233E",
//				"2315",
//				"2311",
//				"2607",
//		};
//		String[] ugg2 = {
//				"\u2921",
//				"\u2904",
//				"\u2938",
//				"\u293E",
//				"\u290D",
//				"\u2BA2",
//				"\u2BA4",
//				"\u2B04",
//				"\u2B0C",
//				"\u2B64",
//				"\u2B04",
//				"\u2B82",
//				"\u2B68",
//				"\u2B6E",
//				"\u2B6F",
//				"\u2B5F",
//				"\u2B4D",
//				"\u233E",
//				"\u2315",
//				"\u23FF",
//				"\u2BBA",
//				"\u2B57",
//				"\u233E",
//				"\u2315",
//				"\u2311",
//				"\u2607",
//		};
//
////		for(String s : ugg){
////			;
////			String uc = "\u002Fu" + s;
////			add(new JLabel(s + ": " + uc));
////		}
//		for(int i = 0; i<ugg2.length; i++){
//			add(new JLabel(ugg[i] + ": " + ugg2[i]));
//		}
}
