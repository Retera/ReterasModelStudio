package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.util.IterableListModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionListener;

public class AnimEditPanel extends JPanel {

	ModelHolderThing mht;

	public AnimEditPanel(ModelHolderThing mht) {
		setLayout(new MigLayout("gap 0, fill", "[grow]", "[][grow]"));
		this.mht = mht;

		add(getTopPanel(), "align center, wrap");


		mht.existingAnims = new IterableListModel<>();
		for (Animation anim : mht.receivingModel.getAnims()) {
			mht.existingAnims.addElement(new AnimShell(anim));
		}

		final AnimListCellRenderer animsRenderer = new AnimListCellRenderer();

		// Build the animTabs list of AnimPanels
		for (Animation anim : mht.donatingModel.getAnims()) {
			final AnimPanel iAnimPanel = new AnimPanel(mht, anim, mht.existingAnims, animsRenderer);

			mht.animTabs.addTab(anim.getName(), ImportPanel.orangeIcon, iAnimPanel, "Click to modify data for this animation sequence.");
		}
		mht.animTabs.addChangeListener(mht.getDaChangeListener());

//		JPanel bigPanel = new JPanel(new MigLayout("gap 0, fill", "[30%:30%:30%][70%:70%:70%]", "[grow]"));
//		bigPanel.add(mht.animTabs);
//
//		add(bigPanel, "growx, growy");
		add(mht.animTabs, "growx, growy");
	}


	private JPanel getTopPanel() {
		JPanel topPanel = new JPanel(new MigLayout("gap 0"));

		JButton importAllAnims = createButton("Import All", e -> mht.uncheckAllAnims(true));
		topPanel.add(importAllAnims);

		JButton timescaleAllAnims = createButton("Time-scale All", e -> timescaleAllAnims(mht.animTabs));
		topPanel.add(timescaleAllAnims);

		JButton renameAllAnims = createButton("Import and Rename All", e -> renameAllAnims(mht));
		topPanel.add(renameAllAnims);

		JButton uncheckAllAnims = createButton("Leave All", e -> mht.uncheckAllAnims(false));
		topPanel.add(uncheckAllAnims, "wrap");


		mht.clearExistingAnims = new JCheckBox("Clear pre-existing animations");
		topPanel.add(mht.clearExistingAnims, "spanx, align center");
		return topPanel;
	}

	public JButton createButton(String text, ActionListener actionListener) {
		JButton uncheckAllAnims = new JButton(text);
		uncheckAllAnims.addActionListener(actionListener);
		return uncheckAllAnims;
	}

	private void renameAllAnims(ModelHolderThing mht) {
		final String newTagString = JOptionPane.showInputDialog(null, "Choose additional naming (i.e. swim or alternate)");

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

	public void timescaleAllAnims(JTabbedPane animTabs) {
		for (int i = 0; i < animTabs.getTabCount(); i++) {
			final AnimPanel aniPanel = (AnimPanel) animTabs.getComponentAt(i);
			aniPanel.importTypeBox.setSelectedIndex(2);
		}
	}
}
