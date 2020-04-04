package com.hiveworkshop.wc3.gui.modeledit.components;

import java.awt.CardLayout;

import javax.swing.JPanel;

public class ComponentsPanel extends JPanel {
	private static final String HEADER = "HEADER";
	private static final String COMMENT = "COMMENT";
	private static final String ANIMATION = "ANIMATION";
	private final CardLayout cardLayout;

	public ComponentsPanel() {
		cardLayout = new CardLayout();
		setLayout(cardLayout);
		add(new ComponentHeaderPanel(), HEADER);
		add(new ComponentCommentPanel(), COMMENT);
		add(new ComponentAnimationPanel(), ANIMATION);
//		add(comp)
		cardLayout.show(this, HEADER);
	}
}
