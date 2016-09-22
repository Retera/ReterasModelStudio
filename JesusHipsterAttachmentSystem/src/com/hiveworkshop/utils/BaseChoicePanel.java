package com.hiveworkshop.utils;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.hiveworkshop.wc3.gui.ExceptionPopup;
import com.hiveworkshop.wc3.mdl.MDL;
import com.hiveworkshop.wc3.mdx.MdxModel;
import com.hiveworkshop.wc3.mdx.MdxUtils;
import com.requestin8r.src.IconGet;

import de.wc3data.stream.BlizzardDataInputStream;

public final class BaseChoicePanel extends JPanel implements ActionListener {

	private final JButton continueButton, back;
	private final MainFrame frame;
	private final DefaultListModel<ModelChoice> modelListModel = new DefaultListModel<ModelChoice>();
	private final JList<ModelChoice> modelList;

	public BaseChoicePanel(final MainFrame frame) {
		this.frame = frame;
		final Font smallFont = new Font("Arial",Font.BOLD,16);
		final Font medFont = new Font("Arial",Font.BOLD,28);
		final Font bigFont = new Font("Arial",Font.BOLD,46);

		final JLabel title = new JLabel("Choose Base Model");
		title.setIcon(new ImageIcon(IconGet.get("BasicStruct", 64)));
		title.setFont(bigFont);
		final JLabel desc = new JLabel("Which attachment model would you like to use as a base?");
		desc.setFont(smallFont);

		add(title);
		add(desc);

		continueButton = new JButton("Continue", new ImageIcon(IconGet.get("Replay-Play", 48)));
		continueButton.setFont(medFont);
		continueButton.setEnabled(false);
		continueButton.addActionListener(this);
		final JLabel continueButtonTip = new JLabel("Use this model as the basis for your attachment.");
		continueButtonTip.setFont(smallFont);
		continueButtonTip.setEnabled(false);
		back = new JButton("Back", new ImageIcon(IconGet.get("Cancel", 24)));
		back.setFont(medFont);
		back.addActionListener(this);

		final File basesFolder = new File("bases");
		for(final File baseFile: basesFolder.listFiles()) {
			try {
				final MdxModel mdxModel = MdxUtils.loadModel(new BlizzardDataInputStream(new FileInputStream(baseFile)));
				//mdxModel.modelChunk.name;
				String modelName = baseFile.getName();
				modelName = modelName.substring(0,modelName.indexOf('.'));
				final StringBuilder finalModelName = new StringBuilder();
				for(int i = 0; i < modelName.length(); i++) {
					final char c = modelName.charAt(i);
					if( Character.isUpperCase(c) && finalModelName.length() > 0 ) {
						finalModelName.append(' ');
					}
					finalModelName.append(c);
				}
				modelListModel.addElement(new ModelChoice(finalModelName.toString(), baseFile));
			} catch (final FileNotFoundException e) {
				ExceptionPopup.display(e);
				e.printStackTrace();
			} catch (final IOException e) {
				ExceptionPopup.display(e);
				e.printStackTrace();
			}
		}
		modelList = new JList<ModelChoice>(modelListModel);
		modelList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		modelList.setFont(medFont);
		modelList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(final ListSelectionEvent e) {
				final boolean enabled = modelList.getSelectedIndex() >= 0;
				continueButton.setEnabled(enabled);
				continueButtonTip.setEnabled(enabled);
			}
		});
		add(modelList);
		add(continueButton);
		add(back);

		final GroupLayout layout = new GroupLayout(this);

		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGap(16)
				.addGroup(layout.createParallelGroup()
						.addGroup(layout.createParallelGroup()
								.addComponent(title)
								.addComponent(desc)
								.addComponent(modelList)
								.addComponent(continueButton)
								.addComponent(continueButtonTip)

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
				.addComponent(modelList)
				.addGap(32)
				.addComponent(continueButton)
				.addGap(4)
				.addComponent(continueButtonTip)
				.addGap(84)
				.addComponent(back)
				.addGap(16));

		setLayout(layout);


	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		if( e.getSource() == back ) {
			frame.jumpToPanel(frame.startPanel);
		} else if (e.getSource() == continueButton) {
			frame.jumpToPanel(new EditPanel(frame, this, MDL.read(modelList.getSelectedValue().getPath())));
		}
	}

	private static final class ModelChoice {
		private final String name;
		private final File path;
		public ModelChoice(final String name, final File path) {
			this.name = name;
			this.path = path;
		}
		public String getName() {
			return name;
		}
		public File getPath() {
			return path;
		}
		@Override
		public String toString() {
			return getName();
		}
	}
}
