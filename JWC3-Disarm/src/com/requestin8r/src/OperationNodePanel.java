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

import com.requestin8r.src.OperationType.OperationGroup;

public class OperationNodePanel extends JPanel implements ActionListener {
	JButton back;
	JButton[] buttons;
	JLabel[] buttonHints;
	JLabel title;
	MainFrame frame;
	JPanel parent;
	Project project;
	OperationNode node;
	public OperationNodePanel(MainFrame frame, JPanel parent, OperationNode node, Project project) {
		this.frame = frame;
		this.parent = parent;
		this.project = project;
		this.node = node;
		Font smallFont = new Font("Arial",Font.BOLD,16);
		Font medFont = new Font("Arial",Font.BOLD,28);
		Font bigFont = new Font("Arial",Font.BOLD,46);
		
		title = new JLabel(project.name + " > " + node.path);
		title.setIcon(new ImageIcon(node.icon));
		title.setFont(bigFont);
		JLabel desc = new JLabel(node.description);
		desc.setFont(smallFont);
//		JLabel desc2 = new JLabel("(You can mouse over each option for information about what they are.)");
//		desc2.setFont(smallFont);

		GroupLayout layout = new GroupLayout(this);
		

		
		back = new JButton("Back", new ImageIcon(IconGet.get("Cancel", 24)));
		back.setFont(medFont);
		back.addActionListener(this);

		GroupLayout.Group horizontalGroup = null;
		GroupLayout.Group[] verticalGroup = new GroupLayout.Group[3];
		GroupLayout.Group[] verticalGroupL = new GroupLayout.Group[3];
		GroupLayout.Group horizontalGroup2;
		GroupLayout.Group verticalGroup2;
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGap(16)
				.addGroup(layout.createParallelGroup()
						.addComponent(title)
						.addComponent(desc)
						.addGroup(horizontalGroup2 = layout.createSequentialGroup()
						)
						.addComponent(back)
				)
				.addGap(16));
		
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGap(16)
				.addComponent(title)
				.addGap(4)
				.addComponent(desc)
				.addGap(64)
				.addGroup(verticalGroup2 = layout.createSequentialGroup()
						)
				.addGap(84)
				.addComponent(back)
				.addGap(16));
		
		for( int i = 0; i < verticalGroup.length; i++ ) {
			verticalGroup[i] = layout.createParallelGroup();
			verticalGroupL[i] = layout.createParallelGroup();
			
			if( i != 0 ) {
				verticalGroup2.addGap(32);
			}
			
			verticalGroup2.addGroup(verticalGroup[i]);
			verticalGroup2.addGap(4);
			verticalGroup2.addGroup(verticalGroupL[i]);
		}
		
		if( node instanceof OperationGroup ) {
			OperationGroup group = (OperationGroup)node;
			buttons = new JButton[group.nodes.size()];
			buttonHints = new JLabel[group.nodes.size()];
			
			for( int i = 0; i < buttons.length; i++ ) {
				OperationNode buttonNode = group.nodes.get(i);
				buttons[i] = new JButton(buttonNode.name, new ImageIcon(buttonNode.icon.getScaledInstance(48, 48, Image.SCALE_SMOOTH)));
				buttons[i].setFont(medFont);
				buttons[i].addActionListener(this);
				buttonHints[i] = new JLabel(buttonNode.description);
				buttonHints[i].setFont(smallFont);
				GroupLayout.Group vertGroup = verticalGroup[i % 3];
				if( i % 3 == 0 ) {
					horizontalGroup = layout.createParallelGroup();
					
					if( i != 0 ) {
						horizontalGroup2.addGap(8);
//						verticalGroup2.addGap(8);
					}
					
					horizontalGroup2.addGroup(horizontalGroup);
//					verticalGroup2.addGroup(verticalGroup);
				}
				
				horizontalGroup.addComponent(buttons[i]);
				horizontalGroup.addComponent(buttonHints[i]);
				
				if( i != 0 ) {
					vertGroup.addGap(32);
				}
				
				vertGroup.addComponent(buttons[i]);
				verticalGroupL[i % 3].addComponent(buttonHints[i]);
			}
		}
		
//		geometry = new JButton("Geometry", new ImageIcon(IconGet.get("HumanArmorUpOne", 48)));
//		geometry.setFont(medFont);
//		geometry.addActionListener(this);
//		geometry.setToolTipText("Physical objects, like weapons.");
//		add(geometry);
//		JLabel geometryTip = new JLabel("Physical objects, like weapons.");
//		geometryTip.setFont(smallFont);
//		playerColor = new JButton("Player Color", new ImageIcon(IconGet.get("OrcCaptureFlag", 48)));
//		playerColor.setFont(medFont);
//		playerColor.addActionListener(this);
//		playerColor.setToolTipText("The material that changes color based on unit ownership.");
//		add(playerColor);
//		JLabel playerColorTip = new JLabel("The material that changes color based on unit ownership.");
//		playerColorTip.setFont(smallFont);
//		glow = new JButton("Glow", new ImageIcon(new ImageIcon(OperationNodePanel.class.getResource("BTNTeamGlow00.png")).getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH)));
//		glow.setFont(medFont);
//		glow.addActionListener(this);
//		glow.setToolTipText("The materials that glow with a constant shape.");
//		add(glow);
//		JLabel glowTip = new JLabel("The materials that glow with a constant shape.");
//		glowTip.setFont(smallFont);
//		animation = new JButton("Animation", new ImageIcon(IconGet.get("Bash", 48)));
//		animation.setFont(medFont);
//		animation.addActionListener(this);
//		animation.setToolTipText("Animation sequences, like walking and standing.");
//		add(animation);
//		JLabel animationTip = new JLabel("Animation sequences, like walking and standing.");
//		animationTip.setFont(smallFont);
//		attachment = new JButton("Attachment", new ImageIcon(IconGet.get("Glove", 48)));
//		attachment.setFont(medFont);
//		attachment.addActionListener(this);
//		attachment.setToolTipText("Attachment points, for combining models in-game.");
//		add(attachment);
//		JLabel attachmentTip = new JLabel("Attachment points, for combining models in-game.");
//		attachmentTip.setFont(smallFont);
//		specialEffects = new JButton("Special Effect", new ImageIcon(IconGet.get("ScatterRockets", 48)));
//		specialEffects.setFont(medFont);
//		specialEffects.addActionListener(this);
//		specialEffects.setToolTipText("Special effects such as particles, lights, or sounds.");
//		add(specialEffects);
//		JLabel specialEffectsTip = new JLabel("Special effects such as particles, lights, or sounds.");
//		specialEffectsTip.setFont(smallFont);
		
		
		
		setLayout(layout);
	}
//	public void resume() {
//		parent.getFrame().jumpToPanel(this);
//	}
	@Override
	public void actionPerformed(ActionEvent e) {
		if( e.getSource() == back ) {
			frame.jumpToPanel(parent);
		}
		else {
			int i = 0;
			for( JButton button: buttons ) {
				if( e.getSource() == button ) {
					if( node instanceof OperationGroup ) {
						OperationGroup group = (OperationGroup)node;
						frame.jumpToPanel(new OperationNodePanel(frame, this, group.nodes.get(i), project));
					}
				}
				i++;
			}
		}
	}
}
