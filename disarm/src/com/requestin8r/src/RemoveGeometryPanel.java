package com.requestin8r.src;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.hiveworkshop.wc3.mdl.Attachment;

public class RemoveGeometryPanel extends JPanel implements ActionListener {
	JButton geometry, playerColor, glow, animation, attachment, specialEffects, back;
	JLabel title;
	RemovePanel parent;

	public RemoveGeometryPanel(final RemovePanel parent, final Project project) {
		this.parent = parent;
		final Font smallFont = new Font("Arial", Font.BOLD, 16);
		final Font medFont = new Font("Arial", Font.BOLD, 28);
		final Font bigFont = new Font("Arial", Font.BOLD, 46);

		title = new JLabel(project.name + " - Remove - Geometry");
		title.setIcon(new ImageIcon(IconGet.get("HumanArmorUpOne", 64)));
		title.setFont(bigFont);
		final JLabel desc = new JLabel("Choose what you would like to remove.");
		desc.setFont(smallFont);
		final JLabel desc2 = new JLabel("(You can mouse over each option for information about what they are.)");
		desc2.setFont(smallFont);

		for (final Attachment attachment : project.model.getModel().sortedIdObjects(Attachment.class)) {
			String text = attachment.getName();
			if (text.toLowerCase().endsWith(" ref")) {
				text = text.substring(0, text.length() - 4);
			}
			if (text.toLowerCase().endsWith(" ref ")) {
				text = text.substring(0, text.length() - 5);
			}
			if (text.toLowerCase().endsWith(" - ref")) {
				text = text.substring(0, text.length() - 6);
			}
			if (text.toLowerCase().endsWith(" - ref ")) {
				text = text.substring(0, text.length() - 7);
			}
			text = "\"" + text + "\"";
			if (!text.toLowerCase().startsWith("sprite") && !text.toLowerCase().startsWith("origin")
					&& !text.toLowerCase().startsWith("overhead")) {
				add(new JLabel(text));
			}
		}
		// geometry = new JButton("Geometry", new
		// ImageIcon(IconGet.get("HumanArmorUpOne", 48)));
		// geometry.setFont(medFont);
		// geometry.addActionListener(this);
		// geometry.setToolTipText("Physical objects, like weapons.");
		// add(geometry);
		// JLabel geometryTip = new JLabel("Physical objects, like weapons.");
		// geometryTip.setFont(smallFont);
		// playerColor = new JButton("Player Color", new
		// ImageIcon(IconGet.get("OrcCaptureFlag", 48)));
		// playerColor.setFont(medFont);
		// playerColor.addActionListener(this);
		// playerColor.setToolTipText("The material that changes color based on
		// unit ownership.");
		// add(playerColor);
		// JLabel playerColorTip = new JLabel("The material that changes color
		// based on unit ownership.");
		// playerColorTip.setFont(smallFont);
		// glow = new JButton("Glow", new ImageIcon(new
		// ImageIcon(RemoveGeometryPanel.class.getResource("BTNTeamGlow00.png")).getImage().getScaledInstance(48,
		// 48, Image.SCALE_SMOOTH)));
		// glow.setFont(medFont);
		// glow.addActionListener(this);
		// glow.setToolTipText("The materials that glow with a constant
		// shape.");
		// add(glow);
		// JLabel glowTip = new JLabel("The materials that glow with a constant
		// shape.");
		// glowTip.setFont(smallFont);
		// animation = new JButton("Animation", new
		// ImageIcon(IconGet.get("Bash", 48)));
		// animation.setFont(medFont);
		// animation.addActionListener(this);
		// animation.setToolTipText("Animation sequences, like walking and
		// standing.");
		// add(animation);
		// JLabel animationTip = new JLabel("Animation sequences, like walking
		// and standing.");
		// animationTip.setFont(smallFont);
		// attachment = new JButton("Attachment", new
		// ImageIcon(IconGet.get("Glove", 48)));
		// attachment.setFont(medFont);
		// attachment.addActionListener(this);
		// attachment.setToolTipText("Attachment points, for combining models
		// in-game.");
		// add(attachment);
		// JLabel attachmentTip = new JLabel("Attachment points, for combining
		// models in-game.");
		// attachmentTip.setFont(smallFont);
		// specialEffects = new JButton("Special Effect", new
		// ImageIcon(IconGet.get("ScatterRockets", 48)));
		// specialEffects.setFont(medFont);
		// specialEffects.addActionListener(this);
		// specialEffects.setToolTipText("Special effects such as particles,
		// lights, or sounds.");
		// add(specialEffects);
		// JLabel specialEffectsTip = new JLabel("Special effects such as
		// particles, lights, or sounds.");
		// specialEffectsTip.setFont(smallFont);

		back = new JButton("Back", new ImageIcon(IconGet.get("Cancel", 24)));
		back.setFont(medFont);
		back.addActionListener(this);
		add(back);

		// GroupLayout layout = new GroupLayout(this);
		//
		// layout.setHorizontalGroup(layout.createSequentialGroup()
		// .addGap(16)
		// .addGroup(layout.createParallelGroup()
		// .addComponent(title)
		// .addComponent(desc)
		// .addComponent(desc2)
		// .addGroup(layout.createSequentialGroup()
		// .addGroup(layout.createParallelGroup()
		// .addComponent(geometry)
		// .addComponent(playerColor)
		// .addComponent(glow)
		// .addComponent(animation)
		// )
		// .addGap(12)
		// .addGroup(layout.createParallelGroup()
		// .addComponent(attachment)
		// .addComponent(specialEffects)
		// )
		// )
		// .addComponent(back)
		// )
		// .addGap(16));
		//
		// layout.setVerticalGroup(layout.createSequentialGroup()
		// .addGap(16)
		// .addComponent(title)
		// .addGap(4)
		// .addComponent(desc)
		// .addGap(4)
		// .addComponent(desc2)
		// .addGap(64)
		// .addGroup(layout.createParallelGroup()
		// .addComponent(geometry)
		// .addComponent(attachment)
		// )
		// .addGap(32)
		// .addGroup(layout.createParallelGroup()
		// .addComponent(playerColor)
		// .addComponent(specialEffects)
		// )
		// .addGap(32)
		// .addGroup(layout.createParallelGroup()
		// .addComponent(glow)
		//// .addComponent(specialEffects)
		// )
		// .addGap(32)
		// .addGroup(layout.createParallelGroup()
		// .addComponent(animation)
		//// .addComponent(specialEffects)
		// )
		// .addGap(84)
		// .addComponent(back)
		// .addGap(16));
		//
		// setLayout(layout);
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		if (e.getSource() == back) {
			parent.resume();
		}
	}
}
