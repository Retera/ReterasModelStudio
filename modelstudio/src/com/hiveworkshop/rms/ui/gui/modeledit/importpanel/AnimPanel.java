package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.util.IterableListModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.List;

class AnimPanel extends JPanel {
	// Animation panel for controlling which are imported

	static final String IMPORTBASIC = "Import as-is";
	static final String CHANGENAME = "Change name to:";
	static final String TIMESCALE = "Time-scale into pre-existing:";
	static final String GLOBALSEQ = "Rebuild as global sequence";
	// title
	JLabel title;
	// Import option
	JCheckBox doImport;
	// Import option
	JCheckBox inReverse;
	// The animation for this panel
	Animation anim;
	String[] animOptions = {IMPORTBASIC, CHANGENAME, TIMESCALE, GLOBALSEQ};

	JComboBox<String> importTypeBox = new JComboBox<>(animOptions);

	JPanel cardPane = new JPanel();

	JPanel blankCardImp = new JPanel();
	JPanel blankCardGS = new JPanel();

	JPanel nameCard = new JPanel();
	JTextField newNameEntry = new JTextField("", 40);

	JPanel animListCard = new JPanel();
	IterableListModel<AnimShell> recModAnims;
	IterableListModel<AnimShell> recModAnimListModel;
	JList<AnimShell> animList;
	JScrollPane animListPane;
	List<AnimShell> oldSelection = new ArrayList<>();
	boolean listenSelection = true;
	ModelHolderThing mht;

	AnimShell selectedAnim;

	final CardLayout animCardLayout = new CardLayout();

	public AnimPanel(ModelHolderThing mht, final IterableListModel<AnimShell> recModAnims, final AnimListCellRenderer renderer) {
		this.mht = mht;
		setLayout(new MigLayout("gap 0"));
		this.recModAnims = recModAnims;
		recModAnimListModel = new IterableListModel<>(recModAnims);

		title = new JLabel();
		title.setFont(new Font("Arial", Font.BOLD, 26));
		add(title, "align center, wrap");

		doImport = new JCheckBox("Import this Sequence");
		doImport.setSelected(true);
		doImport.addChangeListener(e -> CheckboxStateChanged());
		add(doImport, "left, wrap");

		inReverse = new JCheckBox("Reverse");
		inReverse.setSelected(false);
		inReverse.addChangeListener(e -> CheckboxStateChanged());
		add(inReverse, "left, wrap");

		importTypeBox.setEditable(false);
		importTypeBox.addItemListener(this::showCorrectCard);
		importTypeBox.setMaximumSize(new Dimension(200, 20));
		add(importTypeBox);

		nameCard.add(newNameEntry);

		animList = new JList<>(recModAnimListModel);
		animList.setCellRenderer(renderer);
		animList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		// Use getSelectedValuesList().toArray() to request an array of selected animations

		// Select any animation found that has the same name automatically
		// -- This iterates through the list of old animations and picks out and
		// like-named ones, so that the default selection is any animation with the same name
		// (although this should stop after the first one is picked)
		animList.addListSelectionListener(this::updateList);

		animListPane = new JScrollPane(animList);
		animListCard.add(animListPane);

		cardPane.setLayout(animCardLayout);
		cardPane.add(blankCardImp, IMPORTBASIC);
		cardPane.add(nameCard, CHANGENAME);
		cardPane.add(animListPane, TIMESCALE);
		cardPane.add(blankCardGS, GLOBALSEQ);
		// cardLayout.show(cardPane,IMPORTBASIC);
		add(cardPane, "growx, growy");
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				super.componentShown(e);
				setSelectedAnim(selectedAnim);
			}
		});
	}

	private void selectAnimInList(AnimShell anim) {
		if (anim.getImportAnimShell() != null) {
			animList.setSelectedValue(anim.getImportAnimShell(), true);
		} else {
			for (AnimShell animShell : recModAnimListModel) {
				if (animShell.getOldName().equalsIgnoreCase(anim.getOldName())) {
					animList.setSelectedValue(animShell, true);
				}
			}
		}
	}

	public void setSelectedAnim(AnimShell animShell) {
		selectedAnim = animShell;
		this.anim = animShell.getAnim();
		title.setText(animShell.getName());
		newNameEntry.setText(animShell.getName());
		selectAnimInList(animShell);
		doImport.setSelected(selectedAnim.isDoImport());
		importTypeBox.setSelectedIndex(animShell.getImportType());
	}

	public void setSelected(final boolean flag) {
		selectedAnim.setDoImport(flag);
		doImport.setSelected(selectedAnim.isDoImport());
	}

	private void CheckboxStateChanged() {
		selectedAnim.setDoImport(doImport.isSelected());
		importTypeBox.setEnabled(selectedAnim.isDoImport());
		cardPane.setEnabled(selectedAnim.isDoImport());
		animList.setEnabled(selectedAnim.isDoImport());
		newNameEntry.setEnabled(selectedAnim.isDoImport());
		updateSelectionPicks();
	}

	private void showCorrectCard(ItemEvent e) {
		// --
		// http://docs.oracle.com/javase/tutorial/uiswing/examples/layout/CardLayoutDemoProject/src/layout/CardLayoutDemo.java
		// -- http://docs.oracle.com/javase/tutorial/uiswing/layout/card.html
		// Thanks to the CardLayoutDemo.java at the above urls
		// in the JavaDocs for the example use of a CardLayout
		animCardLayout.show(cardPane, (String) e.getItem());
		System.out.println("StateChange: " + e.getStateChange() + ", selected Index: " + importTypeBox.getSelectedIndex());
		selectedAnim.setImportType(importTypeBox.getSelectedIndex());
		updateSelectionPicks();
	}

	public void updateSelectionPicks() {
		listenSelection = false;
		// IterableListModel newModel = new IterableListModel();
		List<AnimShell> selectedValuesList = animList.getSelectedValuesList();
		recModAnimListModel.clear();

		for (AnimShell as : recModAnims) {
			if ((as == null) || (as.importAnim == anim)) {
				recModAnimListModel.addElement(as);
			}
		}

		final int[] indices = new int[selectedValuesList.size()];
		for (int i = 0; i < indices.length; i++) {
			indices[i] = recModAnimListModel.indexOf(selectedValuesList.get(i));
		}

		animList.setSelectedIndices(indices);
		listenSelection = true;

		List<AnimShell> newSelection;
		if (doImport.isSelected() && (importTypeBox.getSelectedIndex() == 2)) {
			newSelection = selectedValuesList;
		} else {
			newSelection = new ArrayList<>();
		}
		for (AnimShell a : oldSelection) {
			a.setImportAnim(null);
		}
		for (AnimShell a : newSelection) {
			a.setImportAnim(anim);
		}

		oldSelection = newSelection;
	}

	private void updateList(ListSelectionEvent e) {
		if (listenSelection && e.getValueIsAdjusting()) {
			updateSelectionPicks();
		}
	}
}
