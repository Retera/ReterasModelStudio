package com.hiveworkshop.mdxtinker;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

public class OkCancelPanel extends JPanel {

	private final JButton okButton, cancelButton;

	public OkCancelPanel(final JPanel contentPanel, final OkCancelListener listener) {

		final Dimension okCancelButtonSize = new Dimension(75, 20);
		okButton = new JButton("OK");
		okButton.setMinimumSize(okCancelButtonSize);
		okButton.setPreferredSize(okCancelButtonSize);
		okButton.setMaximumSize(okCancelButtonSize);
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				getTopLevelAncestor().setVisible(false);
				listener.ok();
			}
		});
		cancelButton = new JButton("Cancel");
		cancelButton.setMinimumSize(okCancelButtonSize);
		cancelButton.setPreferredSize(okCancelButtonSize);
		cancelButton.setMaximumSize(okCancelButtonSize);
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				getTopLevelAncestor().setVisible(false);
				listener.cancel();
			}
		});

		final GroupLayout layout = new GroupLayout(this);

		layout.setHorizontalGroup(layout.createSequentialGroup().addGap(32).addComponent(contentPanel).addGap(32)
				.addGroup(layout.createParallelGroup().addComponent(okButton).addComponent(cancelButton)).addGap(32));
		layout.setVerticalGroup(layout.createSequentialGroup().addGap(32).addGroup(
				layout.createParallelGroup(GroupLayout.Alignment.TRAILING).addComponent(contentPanel).addGroup(
						layout.createSequentialGroup().addComponent(okButton).addGap(4).addComponent(cancelButton)))
				.addGap(32));

		setLayout(layout);
	}

	public interface OkCancelListener {
		void ok();

		void cancel();
	}
}
