package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.Animation;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;

public class AnimEditPanel extends JPanel {

	ModelHolderThing mht;
	AnimPanel singleAnimPanel;
	AnimListCellRenderer animRenderer;
	CardLayout animCardLayout = new CardLayout();
	JPanel animPanelCards = new JPanel(animCardLayout);
	MultiAnimPanel multiAnimPanel;

	public AnimEditPanel(ModelHolderThing mht) {
		setLayout(new MigLayout("gap 0, fill", "[grow]", "[][grow]"));
		this.mht = mht;

		add(getTopPanel(), "align center, wrap");

		animRenderer = new AnimListCellRenderer();

		for (Animation anim : mht.receivingModel.getAnims()) {
			AnimShell animShell = new AnimShell(anim);
			mht.recModAnims.addElement(animShell);
			mht.animTabList.addElement(animShell);
		}

		animPanelCards.add(new JPanel(), "blank");
		// Build the animTabs list of AnimPanels
		singleAnimPanel = new AnimPanel(mht, mht.recModAnims, animRenderer);
		animPanelCards.add(singleAnimPanel, "single");

		multiAnimPanel = new MultiAnimPanel(mht);
		animPanelCards.add(multiAnimPanel, "multiple");

		for (Animation anim : mht.donatingModel.getAnims()) {
			AnimShell animShell = new AnimShell(anim);
			mht.donModAnims.addElement(animShell);
			mht.animTabList.addElement(animShell);
		}

		JScrollPane animStrollPane = new JScrollPane(mht.animJList);
		animStrollPane.setMinimumSize(new Dimension(150, 200));
		mht.animJList.setCellRenderer(animRenderer);
		animRenderer.setSelectedAnim(null);
		mht.animJList.addListSelectionListener(e -> changeAnim(mht, e));
		mht.animJList.setSelectedValue(null, false);

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, animStrollPane, animPanelCards);
		add(splitPane, "growx, growy");
	}

	private void changeAnim(ModelHolderThing mht, ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) {
			List<AnimShell> selectedValuesList = mht.animJList.getSelectedValuesList();

			singleAnimPanel.setSelectedAnim(mht.animJList.getSelectedValue());
			if (selectedValuesList.size() < 1) {
				animRenderer.setSelectedAnim(null);
				animCardLayout.show(animPanelCards, "blank");
			} else if (selectedValuesList.size() == 1) {
				animRenderer.setSelectedAnim(mht.animJList.getSelectedValue());
				singleAnimPanel.setSelectedAnim(mht.animJList.getSelectedValue());
				animCardLayout.show(animPanelCards, "single");
			} else {
				animRenderer.setSelectedAnim(null);
//				multiAnimPanel.updateMultiBonePanel();
				animCardLayout.show(animPanelCards, "multiple");
			}
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
