package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.Animation;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.ActionListener;

public class AnimEditPanel extends JPanel {

	ModelHolderThing mht;
	AnimPanel singleAnimPanel;

	public AnimEditPanel(ModelHolderThing mht) {
		setLayout(new MigLayout("gap 0, fill", "[grow]", "[][grow]"));
		this.mht = mht;

		add(getTopPanel(), "align center, wrap");

		final AnimListCellRenderer animsRenderer = new AnimListCellRenderer();

		for (Animation anim : mht.receivingModel.getAnims()) {
			AnimShell animShell = new AnimShell(anim);
			mht.recModAnims.addElement(animShell);
		}

		// Build the animTabs list of AnimPanels
		singleAnimPanel = new AnimPanel(mht, mht.recModAnims, animsRenderer);
		for (Animation anim : mht.donatingModel.getAnims()) {
			AnimShell animShell = new AnimShell(anim);
			mht.donModAnims.addElement(animShell);
			mht.animTabList.addElement(animShell);
			final AnimPanel iAnimPanel = new AnimPanel(mht, mht.recModAnims, animsRenderer);
			iAnimPanel.setSelectedAnim(animShell);

			mht.animTabs.addTab(anim.getName(), ImportPanel.orangeIcon, iAnimPanel, "Click to modify data for this animation sequence.");
		}


		JScrollPane animStrollPane = new JScrollPane(mht.animJList);
		animStrollPane.setMinimumSize(new Dimension(150, 200));
		mht.animJList.setCellRenderer(animsRenderer);
		mht.animJList.addListSelectionListener(e -> changeAnim(mht, e));

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, animStrollPane, singleAnimPanel);
		add(splitPane, "growx, growy");

//		JPanel bigPanel = new JPanel(new MigLayout("gap 0, fill", "[30%:30%:30%][70%:70%:70%]", "[grow]"));
//		bigPanel.add(mht.animTabs);
//
//		add(bigPanel, "growx, growy");
//		add(mht.animTabs, "growx, growy");
	}

	private void changeAnim(ModelHolderThing mht, ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) {
			singleAnimPanel.setSelectedAnim(mht.animJList.getSelectedValue());
		}
	}


	private JPanel getTopPanel() {
		JPanel topPanel = new JPanel(new MigLayout("gap 0"));

		JButton importAllAnims = createButton("Import All", e -> mht.setImportTypeForAllAnims(AnimShell.ImportType.IMPORTBASIC));
		topPanel.add(importAllAnims);

		JButton timescaleAllAnims = createButton("Time-scale All", e -> mht.setImportTypeForAllAnims(AnimShell.ImportType.TIMESCALE));
		topPanel.add(timescaleAllAnims);

		JButton renameAllAnims = createButton("Import and Rename All", e -> renameAllAnims(mht));
		topPanel.add(renameAllAnims);

		JButton uncheckAllAnims = createButton("Leave All", e -> mht.setImportTypeForAllAnims(AnimShell.ImportType.DONTIMPORT));
		topPanel.add(uncheckAllAnims, "wrap");


		topPanel.add(mht.clearRecModAnims, "spanx, align center");
		return topPanel;
	}

	public JButton createButton(String text, ActionListener actionListener) {
		JButton button = new JButton(text);
		button.addActionListener(actionListener);
		return button;
	}

	private void renameAllAnims(ModelHolderThing mht) {
		final String newTagString = JOptionPane.showInputDialog(this, "Choose additional naming (i.e. swim or alternate)");

		if (newTagString != null) {
			for (AnimShell animShell : mht.animTabList) {
				animShell.setImportType(AnimShell.ImportType.CHANGENAME);
				final String oldName = animShell.getOldName();
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
				animShell.setName(newName);
			}
		}
	}
}
