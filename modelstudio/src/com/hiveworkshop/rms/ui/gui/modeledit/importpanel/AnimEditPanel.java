package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.util.IterableListModel;

import javax.swing.*;

public class AnimEditPanel {
	static JPanel makeAnimationPanel(ModelHolderThing mht) {
		JPanel animPanel = new JPanel();

		mht.existingAnims = new IterableListModel<>();
		for (int i = 0; i < mht.receivingModel.getAnims().size(); i++) {
			mht.existingAnims.addElement(new AnimShell(mht.receivingModel.getAnims().get(i)));
		}

		final AnimListCellRenderer animsRenderer = new AnimListCellRenderer();

		JButton importAllAnims = new JButton("Import All");
		importAllAnims.addActionListener(e -> mht.uncheckAllAnims(true));
		animPanel.add(importAllAnims);

		JButton timescaleAllAnims = new JButton("Time-scale All");
		timescaleAllAnims.addActionListener(e -> timescaleAllAnims(mht.animTabs));
		animPanel.add(timescaleAllAnims);

		JButton renameAllAnims = new JButton("Import and Rename All");
		renameAllAnims.addActionListener(e -> renameAllAnims(mht));
		animPanel.add(renameAllAnims);

		JButton uncheckAllAnims = new JButton("Leave All");
		uncheckAllAnims.addActionListener(e -> mht.uncheckAllAnims(false));
		animPanel.add(uncheckAllAnims);

		mht.clearExistingAnims = new JCheckBox("Clear pre-existing animations");

		// Build the animTabs list of AnimPanels
		for (int i = 0; i < mht.donatingModel.getAnims().size(); i++) {
			final Animation anim = mht.donatingModel.getAnim(i);
			final AnimPanel iAnimPanel = new AnimPanel(mht, anim, mht.existingAnims, animsRenderer);

			mht.animTabs.addTab(anim.getName(), ImportPanel.orangeIcon, iAnimPanel,
					"Click to modify data for this animation sequence.");
		}
		mht.animTabs.addChangeListener(mht.getDaChangeListener());

		animPanel.add(mht.clearExistingAnims);
		animPanel.add(mht.animTabs);

		final GroupLayout animLayout = new GroupLayout(animPanel);
		animLayout.setHorizontalGroup(animLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
				.addGroup(animLayout.createSequentialGroup()
						.addComponent(importAllAnims).addGap(8)
						.addComponent(renameAllAnims).addGap(8)
						.addComponent(timescaleAllAnims).addGap(8)
						.addComponent(uncheckAllAnims))
				.addComponent(mht.clearExistingAnims)
				.addComponent(mht.animTabs));
		animLayout.setVerticalGroup(animLayout.createSequentialGroup()
				.addGroup(animLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(importAllAnims)
						.addComponent(renameAllAnims)
						.addComponent(timescaleAllAnims)
						.addComponent(uncheckAllAnims))
				.addComponent(mht.clearExistingAnims).addGap(8)
				.addComponent(mht.animTabs));
		animPanel.setLayout(animLayout);

		return animPanel;
	}

	private static void renameAllAnims(ModelHolderThing mht) {
		final String newTagString = JOptionPane.showInputDialog(null,
				"Choose additional naming (i.e. swim or alternate)");
		if (newTagString != null) {
			for (int i = 0; i < mht.animTabs.getTabCount(); i++) {
				final AnimPanel aniPanel = (AnimPanel) mht.animTabs.getComponentAt(i);
				aniPanel.importTypeBox.setSelectedIndex(1);
				final String oldName = aniPanel.anim.getName();
				String baseName = oldName;
				while ((baseName.length() > 0) && baseName.contains(" ")) {
					final int lastSpaceIndex = baseName.lastIndexOf(' ');
					final String lastWord = baseName.substring(lastSpaceIndex + 1);
					boolean chunkHasInt = false;
					for (int animationId = 0; animationId < 10; animationId++) {
						if (lastWord.contains(Integer.toString(animationId))) {
							chunkHasInt = true;
						}
					}
					if (lastWord.contains("-") || chunkHasInt || lastWord.toLowerCase().contains("alternate") || (lastWord.length() <= 0)) {
						baseName = baseName.substring(0, baseName.lastIndexOf(' '));
					} else {
						break;
					}
				}
				final String afterBase = oldName.substring(Math.min(oldName.length(), baseName.length() + 1));
				final String newName = baseName + " " + newTagString + " " + afterBase;
				aniPanel.newNameEntry.setText(newName);
			}
		}
	}

	public static void timescaleAllAnims(JTabbedPane animTabs) {
		for (int i = 0; i < animTabs.getTabCount(); i++) {
			final AnimPanel aniPanel = (AnimPanel) animTabs.getComponentAt(i);
			aniPanel.importTypeBox.setSelectedIndex(2);
		}
	}
}
