package com.hiveworkshop.wc3.jworldedit.wipdesign;

import java.awt.Color;

import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

public class TechshaperPanel extends JPanel {
	private final JButton newButton;
	private final JButton drawRequirementLine;
	private final JButton drawUpgradeLine;
	private final TechtreePanel techtreePanel;

	public TechshaperPanel() {
		setBackground(new Color(7,7,7));
		newButton = new JButton(new ImageIcon(TechshaperPanel.class.getResource("res/new.png")));
		add( newButton );
		drawRequirementLine = new JButton(new ImageIcon(TechshaperPanel.class.getResource("res/drawOrange.png")));
		add( drawRequirementLine );
		drawUpgradeLine = new JButton(new ImageIcon(TechshaperPanel.class.getResource("res/drawBlue.png")));
		add( drawUpgradeLine );
		techtreePanel = new TechtreePanel(13, 13);
		add(techtreePanel);
		final GroupLayout layout = new GroupLayout(this);
		layout.setHorizontalGroup(layout.createParallelGroup()
				.addGroup(layout.createSequentialGroup()
						.addComponent(newButton)
						.addGap(430)
						.addComponent(drawRequirementLine)
						.addComponent(drawUpgradeLine)
						)
				.addComponent(techtreePanel)
				);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup()
						.addComponent(newButton)
						.addComponent(drawRequirementLine)
						.addComponent(drawUpgradeLine)
						)
				.addComponent(techtreePanel)
				);
		setLayout(layout);
	}
}
