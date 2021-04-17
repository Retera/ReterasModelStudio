package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.util.IterableListModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.List;

class AnimPanel extends JPanel {

	JLabel title;
	JCheckBox inReverse;

	JComboBox<String> importTypeBox = new JComboBox<>(AnimShell.ImportType.getDispList());

	JPanel cardPane = new JPanel();

	JPanel blankCardImp = new JPanel();
	JPanel blankCardGS = new JPanel();

	JPanel nameCard = new JPanel();
	JTextField newNameEntry = new JTextField("", 40);

	JPanel animListCard = new JPanel();
	IterableListModel<AnimShell> recModAnims;
	IterableListModel<AnimShell> recModAnimListModel;
	JList<AnimShell> recModAnimJList;
	JScrollPane animListPane;
	ModelHolderThing mht;

	AnimShell selectedAnim;
	AnimListCellRenderer animRenderer;

	final CardLayout animCardLayout = new CardLayout();

	public AnimPanel() {
	}

	public AnimPanel(ModelHolderThing mht, final IterableListModel<AnimShell> recModAnims, final AnimListCellRenderer renderer) {
		this.mht = mht;
		setLayout(new MigLayout("gap 0"));
		this.recModAnims = recModAnims;
		recModAnimListModel = new IterableListModel<>(recModAnims);

		title = new JLabel("Select an Animation");
		title.setFont(new Font("Arial", Font.BOLD, 26));
		add(title, "align center, spanx, wrap");

		inReverse = new JCheckBox("Reverse");
		inReverse.setSelected(false);
		inReverse.addActionListener(e -> setInReverse());
		inReverse.setEnabled(false);
		add(inReverse, "left, wrap");

		importTypeBox.setEditable(false);
		importTypeBox.addItemListener(this::showCorrectCard);
		importTypeBox.setMaximumSize(new Dimension(200, 20));
		importTypeBox.setEnabled(false);
		add(importTypeBox, "wrap");

		nameCard.add(newNameEntry);
		animRenderer = renderer;
		recModAnimJList = new JList<>(recModAnimListModel);
		recModAnimJList.setCellRenderer(animRenderer);
		recModAnimJList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		recModAnimJList.addListSelectionListener(this::updateList);

		animListPane = new JScrollPane(recModAnimJList);
		animListCard.add(animListPane);

		cardPane.setLayout(animCardLayout);
		cardPane.add(blankCardImp, AnimShell.ImportType.DONTIMPORT.getDispText());
		cardPane.add(blankCardImp, AnimShell.ImportType.IMPORTBASIC.getDispText());
		cardPane.add(nameCard, AnimShell.ImportType.CHANGENAME.getDispText());
		cardPane.add(animListPane, AnimShell.ImportType.TIMESCALE.getDispText());
		cardPane.add(blankCardGS, AnimShell.ImportType.GLOBALSEQ.getDispText());
		add(cardPane, "growx, growy");
	}

	public void setSelectedAnim(AnimShell animShell) {
		selectedAnim = animShell;
		animRenderer.setSelectedAnim(selectedAnim);
		updateRecModAnimList();
		title.setText(animShell.getName());
		newNameEntry.setText(animShell.getName());
		importTypeBox.setEnabled(true);
		importTypeBox.setSelectedIndex(animShell.getImportType().ordinal());
	}

	private void setInReverse() {
		selectedAnim.setReverse(inReverse.isSelected());
	}

	private void showCorrectCard(ItemEvent e) {
		animCardLayout.show(cardPane, (String) e.getItem());
		System.out.println("StateChange: " + e.getStateChange() + ", selected Index: " + importTypeBox.getSelectedIndex());
		selectedAnim.setImportType(importTypeBox.getSelectedIndex());
		inReverse.setEnabled(selectedAnim.getImportType() != AnimShell.ImportType.DONTIMPORT);
		updateRecModAnimList();
	}

	private void updateRecModAnimList() {
		recModAnimListModel.clear();
		List<AnimShell> usedAnims = new ArrayList<>();

		for (AnimShell as : recModAnims) {
			if (as.getImportAnimShell() == null) {
				recModAnimListModel.addElement(as);
			} else if (as.getImportAnimShell() == selectedAnim) {
				recModAnimListModel.add(0, as);
			} else {
				usedAnims.add(as);
			}
		}
		recModAnimListModel.addAll(usedAnims);
	}

	private void updateList(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) {
			for (AnimShell animShell : recModAnimJList.getSelectedValuesList()) {
				if (animShell.getImportAnimShell() == selectedAnim) {
					animShell.setImportAnimShell(null);
				} else {
					animShell.setImportAnimShell(selectedAnim);
				}
			}
			recModAnimJList.setSelectedValue(null, false);
		}
	}
}
