package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.ui.gui.modeledit.renderers.BoneShellListCellRenderer;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.BoneShellMotionListCellRenderer;
import com.hiveworkshop.rms.util.IterableListModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.CaretListener;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BonePanel extends JPanel {
	protected ModelHolderThing mht;
	protected IdObjectShell<?> selectedBone;
	protected JLabel title;
	protected JComboBox<String> importTypeBox = new JComboBox<>(IdObjectShell.ImportType.getDispList());

	// List for which bone to transfer motion
	private IterableListModel<IdObjectShell<?>> recModMotionBonesListModel = new IterableListModel<>();
	private IterableListModel<IdObjectShell<?>> filteredRecModMotionBonesListModel = new IterableListModel<>();
	private IterableListModel<IdObjectShell<?>> currentMotionBonesListModel; // to let selectImportBones read indicies from the correct list
	protected JList<IdObjectShell<?>> importMotionIntoRecBoneList;

	private List<IdObjectShell<?>> bonesWithMotion = new ArrayList<>();

	protected JPanel cardPanel;
	protected CardLayout cards = new CardLayout();
	protected JPanel dummyPanel = new JPanel();
	private IterableListModel<IdObjectShell<?>> futureBones;
	private IterableListModel<IdObjectShell<?>> filteredFutureBones = new IterableListModel<>();
	private JList<IdObjectShell<?>> futureBonesList;
	private JLabel parentTitle;

	private boolean listenSelection = true;

	private JTextField leftSearchField;
	private JTextField rightSearchField;
	private JCheckBox linkBox;
	private boolean listResetQued = false;

	private BoneShellMotionListCellRenderer oneShellRenderer;
	protected BoneShellListCellRenderer renderer;


	protected BonePanel() {
		// This constructor is negative mojo
	}

	public BonePanel(ModelHolderThing mht, BoneShellListCellRenderer renderer) {
		this.mht = mht;
		setLayout(new MigLayout("gap 0, fill", "[grow][grow]", "[][grow]"));

		title = new JLabel("Select a Bone");
		title.setFont(new Font("Arial", Font.BOLD, 26));
		add(title, "cell 0 0, spanx, align center, wrap");

		this.renderer = renderer;
		oneShellRenderer = new BoneShellMotionListCellRenderer(mht.receivingModel, mht.donatingModel);

		leftSearchField = new JTextField();
		rightSearchField = new JTextField();
		leftSearchField.addCaretListener(getCaretListener(leftSearchField, rightSearchField, this::setRecModelBonesFilter));
		rightSearchField.addCaretListener(getCaretListener(rightSearchField, leftSearchField, this::setFutureBonesFilter));

		linkBox = new JCheckBox("linked search");
		linkBox.addActionListener(e -> queResetList());

		JPanel leftPanel = new JPanel(new MigLayout("gap 0, fill", "[]", "[][grow]"));

		importTypeBox.setEditable(false);
		importTypeBox.addActionListener(e -> showImportTypeCard());
		importTypeBox.setMaximumSize(new Dimension(200, 20));
		leftPanel.add(importTypeBox, "wrap");

		currentMotionBonesListModel = recModMotionBonesListModel;

		JPanel importMotionIntoPanel = new JPanel(new MigLayout("gap 0, ins 0, fill", "[grow][]", "[][][grow]"));
		importMotionIntoPanel.add(new JLabel("Bones to receive motion"), "wrap");
		importMotionIntoPanel.add(leftSearchField, "grow");
		importMotionIntoPanel.add(linkBox, "wrap");
		importMotionIntoPanel.add(getMotionIntoBoneListPane(), "spanx, growx, growy");

		cardPanel = new JPanel(cards);
		cardPanel.add(importMotionIntoPanel, "boneList");
		cardPanel.add(dummyPanel, "blank");

		leftPanel.add(cardPanel, "growx, growy, spanx");

		JPanel rightPanel = new JPanel(new MigLayout("gap 0, fill", "[][]", "[][][grow]"));

		rightPanel.add(new JLabel("Parent:"), "align left, gap 20 0 0 0");
		parentTitle = new JLabel("Parent:      (Old Parent: {no parent})");
		rightPanel.add(parentTitle, "wrap");
		rightPanel.add(rightSearchField, "grow, spanx");

		rightPanel.add(getFutureBoneListPane(renderer), "spanx, growx, growy");

		add(leftPanel, "cell 0 1, growx, growy");
		add(rightPanel, "cell 1 1, growx, growy");
	}

	private JScrollPane getMotionIntoBoneListPane() {
		importMotionIntoRecBoneList = new JList<>(recModMotionBonesListModel);
		importMotionIntoRecBoneList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		importMotionIntoRecBoneList.setCellRenderer(oneShellRenderer);
		importMotionIntoRecBoneList.addListSelectionListener(this::updateList);
		return new JScrollPane(importMotionIntoRecBoneList);
	}

	private JScrollPane getFutureBoneListPane(BoneShellListCellRenderer renderer) {
		futureBonesList = new JList<>();
		futureBonesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		futureBonesList.setCellRenderer(renderer);
		futureBonesList.addListSelectionListener(this::setParent);
		return new JScrollPane(futureBonesList);
	}

	public void setFutureBoneListModel(IterableListModel<IdObjectShell<?>> model) {
		futureBonesList.setModel(model);
	}


	private void setImportIntoListModel(IterableListModel<IdObjectShell<?>> model) {
		importMotionIntoRecBoneList.setModel(model);
		currentMotionBonesListModel = model;
	}

	public void setSelectedBone(IdObjectShell<?> boneShell) {
		selectedBone = boneShell;
		oneShellRenderer.setSelectedBoneShell(selectedBone);
		futureBones = mht.getFutureBoneHelperList();
		renderer.setSelectedBoneShell(boneShell);
		setTitles();
		setFutureBoneListModel(futureBones);
		updateRecModMotionBonesListModel();
		scrollToRevealParent(boneShell);
		setImportStatus(selectedBone.getImportStatus().ordinal());
		repaint();
	}

	private void scrollToRevealParent(IdObjectShell<?> boneShell) {
		if (boneShell.getNewParentShell() != null) {
			int i = futureBones.indexOf(boneShell.getNewParentShell());
			if (i != -1) {
				Rectangle cellBounds = futureBonesList.getCellBounds(i, i);
				if (cellBounds != null) {
					futureBonesList.scrollRectToVisible(cellBounds);
				}
			}
		}
	}

	private void setTitles() {
		title.setText(selectedBone.getIdObject().getClass().getSimpleName() + " \"" + selectedBone.getName() + "\"");

		if (selectedBone.getOldParentShell() != null) {
			parentTitle.setText("Parent:      (Old Parent: " + selectedBone.getOldParentShell().getName() + ")");
		} else {
			parentTitle.setText("Parent:      (Old Parent: {no parent})");
		}
	}

	private void showImportTypeCard() {
		selectedBone.setImportStatus(importTypeBox.getSelectedIndex());

		boolean pastListSelectionState = listenSelection;
		listenSelection = false;
		if (importTypeBox.getSelectedItem() == IdObjectShell.ImportType.MOTION_FROM) {
			cards.show(cardPanel, "boneList");
		} else {
			cards.show(cardPanel, "blank");
		}
		listenSelection = pastListSelectionState;
	}

	public void setImportStatus(final int index) {
		importTypeBox.setSelectedIndex(index);
		showImportTypeCard();
	}

	private void setParent(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting() && futureBonesList.getSelectedValue() != null) {
			if (futureBonesList.getSelectedValue() == selectedBone.getNewParentShell()) {
				selectedBone.setNewParentShell(null);
			} else {
				selectedBone.setNewParentShell(futureBonesList.getSelectedValue());
			}
			futureBonesList.setSelectedValue(null, false);
		}
	}

	private void updateRecModMotionBonesListModel() {
		recModMotionBonesListModel.clear();
		bonesWithMotion.clear();
		for (IdObjectShell<?> bs : mht.recModBoneShells) {
			if (bs.getMotionSrcShell() == null) {
				recModMotionBonesListModel.addElement(bs);
			} else if (bs.getMotionSrcShell() == selectedBone) {
				recModMotionBonesListModel.add(0, bs);
			} else {
				bonesWithMotion.add(bs);
			}
		}
		recModMotionBonesListModel.addAll(bonesWithMotion);
	}

	private void updateList(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) {
			for (IdObjectShell<?> bs : importMotionIntoRecBoneList.getSelectedValuesList()) {
				if (bs.getMotionSrcShell() == selectedBone) {
					bs.setMotionSrcShell(null);
				} else {
					bs.setMotionSrcShell(selectedBone);
				}
			}
			importMotionIntoRecBoneList.setSelectedValue(null, false);
		}
	}
//	private void updateList(ListSelectionEvent e) {
//		if (e.getValueIsAdjusting()) {
////			System.out.println("Update list, BEFORE values: ");
//			for (BoneShell bs : importMotionIntoRecBoneList.getSelectedValuesList()) {
//				if (bs.getImportBoneShell() == selectedBone) {
//					bs.setImportBoneShell(null);
////					System.out.println("import to null");
//				} else {
//					bs.setImportBoneShell(selectedBone);
////					System.out.println("import to: " + selectedBone);
//				}
////				System.out.println(bs + ", importBs: " + bs.getImportBoneShell());
//			}
////			System.out.println("_______________");
//			importMotionIntoRecBoneList.setSelectedValue(null, false);
//		}
//		if (!e.getValueIsAdjusting()) {
////			System.out.println("Update list, AFTER values: ");
//			for (BoneShell bs : importMotionIntoRecBoneList.getSelectedValuesList()) {
//				System.out.println(bs);
//			}
////			System.out.println("_______________");
//		}
//	}

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
			for (IdObjectShell<?> boneShell : futureBones) {
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
			for (IdObjectShell<?> boneShell : recModMotionBonesListModel) {
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
