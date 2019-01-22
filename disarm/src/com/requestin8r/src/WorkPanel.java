package com.requestin8r.src;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.hiveworkshop.wc3.gui.modeledit.PerspDisplayPanel;
import com.hiveworkshop.wc3.mdl.MDL;
import com.hiveworkshop.wc3.mdl.v2.ModelViewManager;

public class WorkPanel extends JPanel implements ActionListener {
	MainFrame frame;

	final MDL blank = new MDL();
	final ModelViewManager blankDisp = new ModelViewManager(blank);

	PerspDisplayPanel viewer;

	JButton add, remove, change, back;
	JLabel title;
	Project project;

	public WorkPanel(final Project project, final MainFrame frame) {
		this.frame = frame;
		this.project = project;
		final Font smallFont = new Font("Arial", Font.BOLD, 16);
		final Font medFont = new Font("Arial", Font.BOLD, 28);
		final Font bigFont = new Font("Arial", Font.BOLD, 46);

		title = new JLabel(project.name);
		title.setIcon(new ImageIcon(project.icon));
		title.setFont(bigFont);
		final JLabel desc = new JLabel("Choose an operation to perform on the model.");
		desc.setFont(smallFont);

		viewer = new PerspDisplayPanel("", project.model, null);

		add = new JButton("Add", new ImageIcon(IconGet.get("StatUp", 48)));
		add.setFont(medFont);
		add.addActionListener(this);
		final JLabel addTip = new JLabel("Add something to the model.");
		addTip.setFont(smallFont);
		remove = new JButton("Remove", new ImageIcon(IconGet.get("Replay-SpeedDown", 48)));
		remove.setFont(medFont);
		remove.addActionListener(this);
		final JLabel removeTip = new JLabel("Remove something from the model.");
		removeTip.setFont(smallFont);
		change = new JButton("Change", new ImageIcon(IconGet.get("Replay-Loop", 48)));
		change.setFont(medFont);
		change.addActionListener(this);
		final JLabel changeTip = new JLabel("Change something that's already in the model.");
		changeTip.setFont(smallFont);
		back = new JButton("Back", new ImageIcon(IconGet.get("Cancel", 24)));
		back.setFont(medFont);
		back.addActionListener(this);
		final JLabel backTip = new JLabel("Takes you back to the welcome screen. You might lose unsaved changes.");
		backTip.setFont(smallFont);

		final GroupLayout layout = new GroupLayout(this);

		layout.setHorizontalGroup(layout.createSequentialGroup().addGap(16)
				.addGroup(layout.createParallelGroup().addComponent(title).addComponent(desc)
						.addGroup(layout.createSequentialGroup()
								.addGroup(layout.createParallelGroup().addComponent(add).addComponent(addTip)
										.addComponent(remove).addComponent(removeTip).addComponent(change)
										.addComponent(changeTip))
								.addGap(12).addComponent(viewer))
						.addComponent(back).addComponent(backTip))
				.addGap(16));

		layout.setVerticalGroup(layout.createSequentialGroup().addGap(16).addComponent(title).addGap(4)
				.addComponent(desc).addGap(64)
				.addGroup(layout.createParallelGroup()
						.addGroup(layout.createSequentialGroup().addComponent(add).addGap(4).addComponent(addTip)
								.addGap(32).addComponent(remove).addGap(4).addComponent(removeTip).addGap(32)
								.addComponent(change).addGap(4).addComponent(changeTip))
						.addComponent(viewer))
				.addGap(84).addComponent(back).addGap(4).addComponent(backTip).addGap(16));

		setLayout(layout);
	}

	public void resume() {
		frame.setContentPane(this);
		frame.revalidate();
		frame.pack();
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		if (e.getSource() == remove) {
			final RemovePanel removePanel = new RemovePanel(this, project);
			frame.setContentPane(removePanel);
			frame.revalidate();
			frame.pack();
			// frame.setEnabled(false);
			// JFrame frame2 = new JFrame();
			// frame2.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			// frame2.setContentPane(new RemovePanel());
			// frame2.setVisible(true);
		} else if (e.getSource() == add) {
			final OperationNodePanel newPanel = new OperationNodePanel(frame, this, OperationType.add, project);
			frame.jumpToPanel(newPanel);
			// frame.setEnabled(false);
			// JFrame frame2 = new JFrame();
			// frame2.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			// frame2.setContentPane(new RemovePanel());
			// frame2.setVisible(true);
		} else if (e.getSource() == back) {
			final int x = JOptionPane.showConfirmDialog(frame,
					"Are you sure you wish to leave, losing any unsaved progress on " + title.getText() + "?",
					"Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (x == JOptionPane.YES_OPTION) {
				frame.setContentPane(frame.mainPanel);
				frame.revalidate();
				frame.pack();
			}
		}
	}

	public MainFrame getFrame() {
		return frame;
	}
}
