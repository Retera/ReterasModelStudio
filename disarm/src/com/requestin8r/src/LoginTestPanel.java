package com.requestin8r.src;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class LoginTestPanel extends JPanel {
	private final JTextField txtUsername;
	private final JPasswordField pwdPassword;

	/**
	 * Create the panel.
	 */
	public LoginTestPanel() {

		final JButton btnLogin = new JButton("Login");
		btnLogin.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
			}
		});

		txtUsername = new JTextField();
		txtUsername.setText("Username");
		txtUsername.setColumns(10);

		pwdPassword = new JPasswordField();
		pwdPassword.setText("Password");

		final JButton btnNewUser = new JButton("New User");

		final JLabel lblSimpleModelWorkshop = new JLabel("Simple Model Workshop");
		lblSimpleModelWorkshop.setFont(new Font("Tahoma", Font.PLAIN, 26));

		final JTree tree = new JTree();
		tree.setModel(new DefaultTreeModel(
			new DefaultMutableTreeNode("Operations") {
				{
					DefaultMutableTreeNode node_1;
					DefaultMutableTreeNode node_2;
					DefaultMutableTreeNode node_3;
					node_1 = new DefaultMutableTreeNode("Add");
						node_2 = new DefaultMutableTreeNode("Shape");
							node_2.add(new DefaultMutableTreeNode("Source Model"));
							node_2.add(new DefaultMutableTreeNode("What Part?"));
							node_2.add(new DefaultMutableTreeNode("Attach to..."));
							node_2.add(new DefaultMutableTreeNode("Scale"));
						node_1.add(node_2);
						node_2 = new DefaultMutableTreeNode("Player Color");
							node_2.add(new DefaultMutableTreeNode("Shape"));
							node_2.add(new DefaultMutableTreeNode("Draw on Texture"));
						node_1.add(node_2);
						node_2 = new DefaultMutableTreeNode("Glow");
							node_2.add(new DefaultMutableTreeNode("What Color"));
							node_2.add(new DefaultMutableTreeNode("What Part?"));
							node_2.add(new DefaultMutableTreeNode("Attach to..."));
						node_1.add(node_2);
						node_2 = new DefaultMutableTreeNode("Animation");
							node_2.add(new DefaultMutableTreeNode("Source Model"));
							node_2.add(new DefaultMutableTreeNode("What Part?"));
							node_2.add(new DefaultMutableTreeNode("Attach to..."));
						node_1.add(node_2);
						node_2 = new DefaultMutableTreeNode("Attachment");
							node_2.add(new DefaultMutableTreeNode("Name"));
							node_2.add(new DefaultMutableTreeNode("Attach to..."));
						node_1.add(node_2);
						node_2 = new DefaultMutableTreeNode("Special Effect");
							node_3 = new DefaultMutableTreeNode("Particle Effect");
								node_3.add(new DefaultMutableTreeNode("Fire"));
								node_3.add(new DefaultMutableTreeNode("Magic"));
							node_2.add(node_3);
							node_3 = new DefaultMutableTreeNode("Ribbon Effect");
								node_3.add(new DefaultMutableTreeNode("Texture"));
								node_3.add(new DefaultMutableTreeNode("Color"));
							node_2.add(node_3);
							node_3 = new DefaultMutableTreeNode("Sound Effect");
								node_3.add(new DefaultMutableTreeNode("What sound?"));
								node_3.add(new DefaultMutableTreeNode("Where in space?"));
							node_2.add(node_3);
						node_1.add(node_2);
					add(node_1);
					node_1 = new DefaultMutableTreeNode("Remove");
						node_2 = new DefaultMutableTreeNode("Shape");
							node_2.add(new DefaultMutableTreeNode("What Part?"));
						node_1.add(node_2);
						node_2 = new DefaultMutableTreeNode("Player Color");
							node_2.add(new DefaultMutableTreeNode("What Part?"));
						node_1.add(node_2);
						node_2 = new DefaultMutableTreeNode("Glow");
							node_2.add(new DefaultMutableTreeNode("What Part?"));
						node_1.add(node_2);
						node_2 = new DefaultMutableTreeNode("Animation");
							node_2.add(new DefaultMutableTreeNode("Which One?"));
						node_1.add(node_2);
						node_2 = new DefaultMutableTreeNode("Attachment");
							node_2.add(new DefaultMutableTreeNode("Which One?"));
						node_1.add(node_2);
						node_2 = new DefaultMutableTreeNode("Special Effect");
							node_2.add(new DefaultMutableTreeNode("Which One?"));
						node_1.add(node_2);
					add(node_1);
					node_1 = new DefaultMutableTreeNode("Change");
						node_1.add(new DefaultMutableTreeNode("Shape"));
						node_2 = new DefaultMutableTreeNode("Texture");
							node_3 = new DefaultMutableTreeNode("Recolor");
								node_3.add(new DefaultMutableTreeNode("Original Color"));
								node_3.add(new DefaultMutableTreeNode("New Color"));
							node_2.add(node_3);
							node_3 = new DefaultMutableTreeNode("Reskin");
								node_3.add(new DefaultMutableTreeNode("What Part?"));
								node_3.add(new DefaultMutableTreeNode("What Texture?"));
							node_2.add(node_3);
							node_2.add(new DefaultMutableTreeNode("Customize Texture"));
						node_1.add(node_2);
						node_2 = new DefaultMutableTreeNode("Tinting/Color");
							node_2.add(new DefaultMutableTreeNode("What Part?"));
							node_2.add(new DefaultMutableTreeNode("New Color Values"));
						node_1.add(node_2);
						node_2 = new DefaultMutableTreeNode("Animation Names");
							node_2.add(new DefaultMutableTreeNode("Which Animation?"));
							node_2.add(new DefaultMutableTreeNode("Name"));
						node_1.add(node_2);
						node_2 = new DefaultMutableTreeNode("Scale");
							node_2.add(new DefaultMutableTreeNode("Model Scale"));
						node_1.add(node_2);
						node_2 = new DefaultMutableTreeNode("Nodes");
							node_3 = new DefaultMutableTreeNode("Attachment");
								node_3.add(new DefaultMutableTreeNode("Name"));
								node_3.add(new DefaultMutableTreeNode("Position"));
							node_2.add(node_3);
							node_3 = new DefaultMutableTreeNode("Particle Emitter");
								node_3.add(new DefaultMutableTreeNode("Position"));
								node_3.add(new DefaultMutableTreeNode("Color"));
								node_3.add(new DefaultMutableTreeNode("Style"));
							node_2.add(node_3);
							node_3 = new DefaultMutableTreeNode("Sound/Splat (Model Event)");
								node_3.add(new DefaultMutableTreeNode("Position"));
								node_3.add(new DefaultMutableTreeNode("Type"));
							node_2.add(node_3);
						node_1.add(node_2);
					add(node_1);
				}
			}
		));
		final GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
						.addComponent(txtUsername)
						.addComponent(pwdPassword)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(btnLogin)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnNewUser, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
					.addGap(47)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
						.addComponent(tree, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(lblSimpleModelWorkshop, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
					.addContainerGap(219, Short.MAX_VALUE))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.TRAILING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblSimpleModelWorkshop)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addPreferredGap(ComponentPlacement.RELATED, 115, Short.MAX_VALUE)
							.addComponent(txtUsername, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(pwdPassword, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(btnLogin)
								.addComponent(btnNewUser))
							.addGap(212))
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(18)
							.addComponent(tree, GroupLayout.PREFERRED_SIZE, 357, GroupLayout.PREFERRED_SIZE)
							.addContainerGap())))
		);
		setLayout(groupLayout);

	}
}
