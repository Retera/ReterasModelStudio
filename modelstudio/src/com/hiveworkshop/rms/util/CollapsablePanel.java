package com.hiveworkshop.rms.util;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CollapsablePanel extends JPanel{
//	private static final String ARROW_RIGHT = "\u25BA";
//	private static final String ARROW_DOWN = "\u25BC";
private static final String ARROW_RIGHT = "\u2BC8";
	private static final String ARROW_DOWN = "\u2BC6";

	private final JLabel collapseArrowLabel;
	private final JPanel collapsedAdditionalInfoPanel;
	private final JPanel collapsableContent;

	String title;
	JLabel titleLabel;

	public CollapsablePanel(String title, JPanel collapsableContent) {
		this(title, collapsableContent, new JPanel());
	}

	public CollapsablePanel(String title, JPanel collapsableContent, JPanel collapsedAdditionalInfoPanel) {
		this.collapsedAdditionalInfoPanel = collapsedAdditionalInfoPanel;
		this.collapsableContent = collapsableContent;
		this.setLayout(new MigLayout("fill, hidemode 1, ins 0, gap 1", "[grow]", "[][grow]"));
		this.title = title;

		titleLabel = new JLabel(title);
		collapseArrowLabel = new JLabel(ARROW_DOWN);
		JPanel titlePanel = new JPanel(new MigLayout("fill, ins 1", "[][][grow][right]", "[]"));
		setBorder(new BevelBorder(BevelBorder.RAISED));
		collapsedAdditionalInfoPanel.setVisible(false);
		titlePanel.add(collapseArrowLabel);
		titlePanel.add(titleLabel);
		titlePanel.add(collapsedAdditionalInfoPanel, "growx");
//		System.out.println("TabbedPane.highlight: " + ((javax.swing.plaf.ColorUIResource)UIManager.get("TabbedPane.highlight")).darker());
//		System.out.println("Panel.alterBackground: " + (UIManager.get("Panel.alterBackground")));
//		System.out.println("Panel.background: " + (UIManager.get("Panel.background")));
		titlePanel.setBackground(new Color(255, 255, 255, 150));

		titlePanel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				super.mouseClicked(e);
				toggleCollapsed();
			}
		});

		add(titlePanel, "growx, wrap");
		add(collapsableContent, "growx");

	}

	public CollapsablePanel toggleCollapsed() {
		collapsableContent.setVisible(!collapsableContent.isVisible());

		if (isCollapsed()) {
			collapseArrowLabel.setText(ARROW_DOWN);
			collapsedAdditionalInfoPanel.setVisible(false);
		} else {
			collapseArrowLabel.setText(ARROW_RIGHT);
			collapsedAdditionalInfoPanel.setVisible(true);
		}
		collapseArrowLabel.repaint();
		collapsedAdditionalInfoPanel.repaint();

		return this;
	}

	public CollapsablePanel setCollapsed(boolean collapsed) {
		collapsableContent.setVisible(!collapsed);

		if (isCollapsed()) {
			collapseArrowLabel.setText(ARROW_DOWN);
			collapsedAdditionalInfoPanel.setVisible(false);
		} else {
			collapseArrowLabel.setText(ARROW_RIGHT);
			collapsedAdditionalInfoPanel.setVisible(true);
		}
		collapseArrowLabel.repaint();
		collapsedAdditionalInfoPanel.repaint();

		return this;
	}

	public JPanel getCollapsableContentPanel() {
		return collapsableContent;
	}

	public boolean isCollapsed() {
		return collapsableContent.isVisible();
	}

	public CollapsablePanel setTitle(String title) {
		this.title = title;
		titleLabel.setText(title);
		repaint();
		return this;
	}
}