package com.requestin8r.src;

import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class RemovePanel extends JPanel implements ActionListener {
	JButton geometry, playerColor, glow, animation, attachment, specialEffects, back;
	JLabel title;
	WorkPanel parent;
	Project project;
	public RemovePanel(final WorkPanel parent, final Project project) {
		this.parent = parent;
		this.project = project;
		final Font smallFont = new Font("Arial",Font.BOLD,16);
		final Font medFont = new Font("Arial",Font.BOLD,28);
		final Font bigFont = new Font("Arial",Font.BOLD,46);

		title = new JLabel(project.name + " > Remove");
		title.setIcon(new ImageIcon(IconGet.get("Replay-SpeedDown", 64)));
		title.setFont(bigFont);
		final JLabel desc = new JLabel("Choose what you would like to remove.");
		desc.setFont(smallFont);
		final JLabel desc2 = new JLabel("(You can mouse over each option for information about what they are.)");
		desc2.setFont(smallFont);

		geometry = new JButton("Geometry", new ImageIcon(IconGet.get("HumanArmorUpOne", 48)));
		geometry.setFont(medFont);
		geometry.addActionListener(this);
		geometry.setToolTipText("Physical objects, like weapons.");
		add(geometry);
		final JLabel geometryTip = new JLabel("Physical objects, like weapons.");
		geometryTip.setFont(smallFont);
		playerColor = new JButton("Player Color", new ImageIcon(IconGet.get("OrcCaptureFlag", 48)));
		playerColor.setFont(medFont);
		playerColor.addActionListener(this);
		playerColor.setToolTipText("The material that changes color based on unit ownership.");
		add(playerColor);
		final JLabel playerColorTip = new JLabel("The material that changes color based on unit ownership.");
		playerColorTip.setFont(smallFont);
		glow = new JButton("Glow", new ImageIcon(new ImageIcon(RemovePanel.class.getResource("BTNTeamGlow00.png")).getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH)));
		glow.setFont(medFont);
		glow.addActionListener(this);
		glow.setToolTipText("The materials that glow with a constant shape.");
		add(glow);
		final JLabel glowTip = new JLabel("The materials that glow with a constant shape.");
		glowTip.setFont(smallFont);
		animation = new JButton("Animation", new ImageIcon(IconGet.get("Bash", 48)));
		animation.setFont(medFont);
		animation.addActionListener(this);
		animation.setToolTipText("Animation sequences, like walking and standing.");
		add(animation);
		final JLabel animationTip = new JLabel("Animation sequences, like walking and standing.");
		animationTip.setFont(smallFont);
		attachment = new JButton("Attachment", new ImageIcon(IconGet.get("Glove", 48)));
		attachment.setFont(medFont);
		attachment.addActionListener(this);
		attachment.setToolTipText("Attachment points, for combining models in-game.");
		add(attachment);
		final JLabel attachmentTip = new JLabel("Attachment points, for combining models in-game.");
		attachmentTip.setFont(smallFont);
		specialEffects = new JButton("Special Effect", new ImageIcon(IconGet.get("ScatterRockets", 48)));
		specialEffects.setFont(medFont);
		specialEffects.addActionListener(this);
		specialEffects.setToolTipText("Special effects such as particles, lights, or sounds.");
		add(specialEffects);
		final JLabel specialEffectsTip = new JLabel("Special effects such as particles, lights, or sounds.");
		specialEffectsTip.setFont(smallFont);

		back = new JButton("Back", new ImageIcon(IconGet.get("Cancel", 24)));
		back.setFont(medFont);
		back.addActionListener(this);


		final GroupLayout layout = new GroupLayout(this);

		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGap(16)
				.addGroup(layout.createParallelGroup()
						.addComponent(title)
						.addComponent(desc)
						.addComponent(desc2)
						.addGroup(layout.createSequentialGroup()
								.addGroup(layout.createParallelGroup()
										.addComponent(geometry)
										.addComponent(playerColor)
										.addComponent(glow)
										.addComponent(animation)
								)
								.addGap(12)
								.addGroup(layout.createParallelGroup()
										.addComponent(attachment)
										.addComponent(specialEffects)
								)
						)
						.addComponent(back)
				)
				.addGap(16));

		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGap(16)
				.addComponent(title)
				.addGap(4)
				.addComponent(desc)
				.addGap(4)
				.addComponent(desc2)
				.addGap(64)
				.addGroup(layout.createParallelGroup()
						.addComponent(geometry)
						.addComponent(attachment)
						)
				.addGap(32)
				.addGroup(layout.createParallelGroup()
						.addComponent(playerColor)
						.addComponent(specialEffects)
						)
				.addGap(32)
				.addGroup(layout.createParallelGroup()
						.addComponent(glow)
//						.addComponent(specialEffects)
						)
				.addGap(32)
				.addGroup(layout.createParallelGroup()
						.addComponent(animation)
//						.addComponent(specialEffects)
						)
				.addGap(84)
				.addComponent(back)
				.addGap(16));

		setLayout(layout);
	}
	public void resume() {
		parent.getFrame().jumpToPanel(this);
	}
	@Override
	public void actionPerformed(final ActionEvent e) {
		if( e.getSource() == back ) {
			parent.resume();
		}
		else if( e.getSource() == geometry ) {
			parent.getFrame().jumpToPanel(new RemoveGeometryPanel(this, project));
		}
	}
}
