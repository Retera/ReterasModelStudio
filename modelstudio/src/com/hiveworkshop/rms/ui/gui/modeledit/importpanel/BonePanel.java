package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.util.IterableListModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.CaretListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BonePanel extends JPanel {
	BoneShell selectedBone;
	JLabel title;
	JComboBox<String> importTypeBox = new JComboBox<>(BoneShell.ImportType.getDispList());

	// List for which bone to transfer motion
	IterableListModel<BoneShell> recModMotionBonesListModel = new IterableListModel<>();
	IterableListModel<BoneShell> filteredRecModMotionBonesListModel = new IterableListModel<>();
	IterableListModel<BoneShell> currentMotionBonesListModel; // to let selectImportBones read indicies from the correct list
	JList<BoneShell> importMotionIntoRecBoneList;

	List<BoneShell> bonesWithMotion = new ArrayList<>();

	JPanel cardPanel;
	CardLayout cards = new CardLayout();
	JPanel dummyPanel = new JPanel();
	IterableListModel<BoneShell> futureBones;
	IterableListModel<BoneShell> filteredFutureBones = new IterableListModel<>();
	JList<BoneShell> futureBonesList;
	JLabel parentTitle;

	boolean listenSelection = true;
	ModelHolderThing mht;

	JTextField leftSearchField;
	JTextField rightSearchField;
	JCheckBox linkBox;
	boolean listResetQued = false;


	ListSelectionListener setMotionBonesFor = this::updateList;
	BoneShellMotionListCellRenderer oneShellRenderer;


	protected BonePanel() {
		// This constructor is negative mojo
	}

	public BonePanel(ModelHolderThing mht, final BoneShellListCellRenderer renderer) {
		this.mht = mht;
		setLayout(new MigLayout("gap 0, fill", "[grow][grow]", "[][grow]"));
		oneShellRenderer = new BoneShellMotionListCellRenderer(mht.recModelManager, mht.donModelManager);

		leftSearchField = new JTextField();
		rightSearchField = new JTextField();

		leftSearchField.addCaretListener(getCaretListener(leftSearchField, rightSearchField, this::setRecModelBonesFilter));

		rightSearchField.addCaretListener(getCaretListener(rightSearchField, leftSearchField, this::setFutureBonesFilter));

		linkBox = new JCheckBox("linked search");
		linkBox.addActionListener(e -> queResetList());

		title = new JLabel("Select a Bone");
		title.setFont(new Font("Arial", Font.BOLD, 26));

		add(title, "cell 0 0, spanx, align center, wrap");

		JPanel leftPanel = new JPanel(new MigLayout("gap 0, fill", "[]", "[][grow]"));

		importTypeBox.setEditable(false);
		importTypeBox.addActionListener(e -> showImportTypeCard());
		importTypeBox.setMaximumSize(new Dimension(200, 20));
		leftPanel.add(importTypeBox, "wrap");

		JPanel importMotionIntoPanel = new JPanel(new MigLayout("gap 0, ins 0, fill", "[grow][]", "[][][grow]"));

		importMotionIntoPanel.add(new JLabel("Bones to receive motion"), "wrap");
		importMotionIntoPanel.add(leftSearchField, "grow");
		importMotionIntoPanel.add(linkBox, "wrap");

		importMotionIntoRecBoneList = new JList<>(recModMotionBonesListModel);
		currentMotionBonesListModel = recModMotionBonesListModel;
		importMotionIntoRecBoneList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		importMotionIntoRecBoneList.setCellRenderer(oneShellRenderer);
		importMotionIntoRecBoneList.addListSelectionListener(setMotionBonesFor);
		JScrollPane boneListPane = new JScrollPane(importMotionIntoRecBoneList);
		importMotionIntoPanel.add(boneListPane, "spanx, growx, growy");

		cardPanel = new JPanel(cards);
		cardPanel.add(importMotionIntoPanel, "boneList");
		cardPanel.add(dummyPanel, "blank");
		leftPanel.add(cardPanel, "growx, growy, spanx");

		JPanel rightPanel = new JPanel(new MigLayout("gap 0, fill", "[][]", "[][][grow]"));

		rightPanel.add(new JLabel("Parent:"), "align left, gap 20 0 0 0");
		parentTitle = new JLabel("Parent:      (Old Parent: {no parent})");
		rightPanel.add(parentTitle, "wrap");
		rightPanel.add(rightSearchField, "grow, spanx");

		futureBonesList = new JList<>();
		futureBonesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		futureBonesList.setCellRenderer(renderer);
		futureBonesList.addListSelectionListener(this::setParent);
		JScrollPane futureBonesListPane = new JScrollPane(futureBonesList);
		rightPanel.add(futureBonesListPane, "spanx, growx, growy");

		add(leftPanel, "cell 0 1, growx, growy");
		add(rightPanel, "cell 1 1, growx, growy");


	}

	public void setFutureBoneListModel(IterableListModel<BoneShell> model) {
		futureBonesList.setModel(model);
	}


	private void setImportIntoListModel(IterableListModel<BoneShell> model) {
		importMotionIntoRecBoneList.setModel(model);

		currentMotionBonesListModel = model;
	}

	public void setSelectedBone(BoneShell whichBone) {
		selectedBone = whichBone;
		oneShellRenderer.setSelectedBoneShell(selectedBone);
//		futureBones = mht.getFutureBoneListExtended(true);
		futureBones = mht.getFutureBoneListExtended(false);
		setTitles();
		setFutureBoneListModel(futureBones);
		updateRecModMotionBonesListModel();
		setSelectedIndex(selectedBone.getImportStatus().ordinal());
		repaint();
	}

	private void setTitles() {
		title.setText(selectedBone.getBone().getClass().getSimpleName() + " \"" + selectedBone.getName() + "\"");

		if (selectedBone.getOldParentBs() != null) {
			parentTitle.setText("Parent:      (Old Parent: " + selectedBone.getOldParentBs().getName() + ")");
		} else {
			parentTitle.setText("Parent:      (Old Parent: {no parent})");
		}
	}

	private void showImportTypeCard() {
		selectedBone.setImportStatus(importTypeBox.getSelectedIndex());

		final boolean pastListSelectionState = listenSelection;
		listenSelection = false;
		if (importTypeBox.getSelectedItem() == BoneShell.ImportType.MOTIONFROM) {
			cards.show(cardPanel, "boneList");
		} else {
			cards.show(cardPanel, "blank");
		}
		listenSelection = pastListSelectionState;
	}

	public void setSelectedIndex(final int index) {
		importTypeBox.setSelectedIndex(index);
		showImportTypeCard();
	}

	private void setParent(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting() && futureBonesList.getSelectedValue() != null) {
			if (futureBonesList.getSelectedValue() == selectedBone.getNewParentBs()) {
				selectedBone.setNewParentBs(null);
			} else {
				selectedBone.setNewParentBs(futureBonesList.getSelectedValue());
			}
			futureBonesList.setSelectedValue(null, false);
		}
	}

	private void updateRecModMotionBonesListModel() {
		recModMotionBonesListModel.clear();
		bonesWithMotion.clear();
		for (BoneShell bs : mht.recModBoneShells) {
			if (bs.getImportBoneShell() == null) {
				recModMotionBonesListModel.addElement(bs);
			} else if (bs.getImportBoneShell() == selectedBone) {
				recModMotionBonesListModel.add(0, bs);
			} else {
				bonesWithMotion.add(bs);
			}
		}
		recModMotionBonesListModel.addAll(bonesWithMotion);
	}

	private void updateList(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) {
//			System.out.println("Update list, BEFORE values: ");
			for (BoneShell bs : importMotionIntoRecBoneList.getSelectedValuesList()) {
				if (bs.getImportBoneShell() == selectedBone) {
					bs.setImportBoneShell(null);
//					System.out.println("import to null");
				} else {
					bs.setImportBoneShell(selectedBone);
//					System.out.println("import to: " + selectedBone);
				}
//				System.out.println(bs + ", importBs: " + bs.getImportBoneShell());
			}
//			System.out.println("_______________");
			importMotionIntoRecBoneList.setSelectedValue(null, false);
		}
		if (!e.getValueIsAdjusting()) {
//			System.out.println("Update list, AFTER values: ");
			for (BoneShell bs : importMotionIntoRecBoneList.getSelectedValuesList()) {
				System.out.println(bs);
			}
//			System.out.println("_______________");
		}


	}

	private void queResetList() {
		if (!linkBox.isSelected()) {
			listResetQued = true;
		}
	}

	private CaretListener getCaretListener(JTextField activeSearchField, JTextField inActiveSearchField, Consumer<String> listModelFunction) {
		return e -> {
			if (linkBox.isSelected()) {
				inActiveSearchField.setText(activeSearchField.getText());
			}
			String filterText = activeSearchField.getText();
			listModelFunction.accept(filterText);
		};
	}

	private void setFutureBonesFilter(String filterText) {
		applyFutureBonesListModel(filterText);
		if (linkBox.isSelected()) {
			applyRecModBonesFilteredListModel(filterText);
		} else if (listResetQued) {
			listResetQued = false;
			setImportIntoListModel(recModMotionBonesListModel);
			leftSearchField.setText("");
		}
	}

	private void applyFutureBonesListModel(String filterText) {
		if (!filterText.equals("")) {
			filteredFutureBones.clear();
			for (BoneShell boneShell : futureBones) {
				if (boneShell.getName().toLowerCase().contains(filterText.toLowerCase())) {
					filteredFutureBones.addElement(boneShell);
				}
			}
			setFutureBoneListModel(filteredFutureBones);
		} else {
			setFutureBoneListModel(futureBones);
		}
	}

	private void setRecModelBonesFilter(String filterText) {
		applyRecModBonesFilteredListModel(filterText);
		if (linkBox.isSelected()) {
			applyFutureBonesListModel(filterText);
		} else if (listResetQued) {
			listResetQued = false;
			setFutureBoneListModel(futureBones);
			rightSearchField.setText("");
		}
	}

	private void applyRecModBonesFilteredListModel(String filterText) {
		if (!filterText.equals("")) {
			filteredRecModMotionBonesListModel.clear();
			for (BoneShell boneShell : recModMotionBonesListModel) {
				if (boneShell.getName().toLowerCase().contains(filterText.toLowerCase())) {
					filteredRecModMotionBonesListModel.addElement(boneShell);
				}
			}
			setImportIntoListModel(filteredRecModMotionBonesListModel);
		} else {
			setImportIntoListModel(recModMotionBonesListModel);
		}
	}
}
