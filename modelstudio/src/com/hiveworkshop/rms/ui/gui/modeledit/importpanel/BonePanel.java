package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.util.IterableListModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BonePanel extends JPanel {
	static final String IMPORT = "Import this bone";
	static final String MOTIONFROM = "Import motion to pre-existing:";
	static final String LEAVE = "Do not import";
	//	Bone bone;
	BoneShell selectedBone;
	JLabel title;
	String[] impOptions = {IMPORT, MOTIONFROM, LEAVE};

	JComboBox<String> importTypeBox = new JComboBox<>(impOptions);

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
	List<BoneShell> oldSelection = new ArrayList<>();
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
		setLayout(new MigLayout("gap 0, fill", "[][]", "[][grow]"));
		oneShellRenderer = new BoneShellMotionListCellRenderer(mht.recModelManager, mht.donModelManager);

		leftSearchField = new JTextField();
		rightSearchField = new JTextField();

		FocusAdapter focusAdapter = getFocusAdapter(leftSearchField, rightSearchField, this::setRecModelBonesFilter);
		leftSearchField.addFocusListener(focusAdapter);

		FocusAdapter focusAdapter1 = getFocusAdapter(rightSearchField, leftSearchField, this::setFutureBonesFilter);
		rightSearchField.addFocusListener(focusAdapter1);
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
//		importMotionIntoRecBoneList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
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

	private FocusAdapter getFocusAdapter(final JTextField mainSearchField, final JTextField secondSearchField, final Consumer<String> listModelChangerFunction) {
		return new FocusAdapter() {
			CaretListener caretListener = getCaretListener(mainSearchField, secondSearchField, listModelChangerFunction);

			@Override
			public void focusGained(FocusEvent e) {
				mainSearchField.addCaretListener(caretListener);
			}

			@Override
			public void focusLost(FocusEvent e) {
				mainSearchField.removeCaretListener(caretListener);
			}
		};
	}

	public void setFutureBoneListModel(IterableListModel<BoneShell> model) {
		futureBonesList.setModel(model);
	}

//	private void selectImportBones() {
//		importMotionIntoRecBoneList.removeListSelectionListener(setMotionBonesFor);
//		ArrayList<BoneShell> bonesToSelect = new ArrayList<>();
//		ArrayList<Integer> indeciesToSelect = new ArrayList<>();
////		for (BoneShell bs : bonesWithMotion) {
//		for (BoneShell bs : currentMotionBonesListModel) {
//			if (bs.bone.getName().equals(selectedBone.getName())
//					&& (bs.importBone == null)
//					&& (!bs.bone.getName().contains("Mesh")
//					&& !bs.bone.getName().contains("Object")
//					&& !bs.bone.getName().contains("Box")
//					|| bs.bone.getPivotPoint().equalLocs(selectedBone.getBone().getPivotPoint()))) {
//				indeciesToSelect.add(currentMotionBonesListModel.indexOf(bs));
////				importMotionIntoRecBoneList.setSelectedValue(bs, true);
////				break;
//				// System.out.println("GREAT BALLS OF FIRE");
//			}
//		}
//		importMotionIntoRecBoneList.setSelectedIndices(indeciesToSelect.stream().mapToInt(i -> i).toArray());
//
//		importMotionIntoRecBoneList.addListSelectionListener(setMotionBonesFor);
//	}

	private void setImportIntoListModel(IterableListModel<BoneShell> model) {
		importMotionIntoRecBoneList.setModel(model);

		currentMotionBonesListModel = model;
//		selectImportBones();
	}

	public void setSelectedBone(BoneShell whichBone) {
		selectedBone = whichBone;
		oneShellRenderer.setSelectedBoneShell(selectedBone);
//		futureBones = mht.getFutureBoneListExtended(true);
		futureBones = mht.getFutureBoneListExtended(false);
		setTitles();
		setFutureBoneListModel(futureBones);
		updateRecModMotionBonesListModel();
		setSelectedIndex(selectedBone.getImportStatus());
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
//		updateSelectionPicks();
		final boolean pastListSelectionState = listenSelection;
		listenSelection = false;
		if (importTypeBox.getSelectedItem() == MOTIONFROM) {
			cards.show(cardPanel, "boneList");
		} else {
			cards.show(cardPanel, "blank");
		}
		listenSelection = pastListSelectionState;
	}

	public int getSelectedIndex() {
		return importTypeBox.getSelectedIndex();
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
			// TODO add boneless BoneShell to futureBonesList to let the user select no bone + add null check on things calling bs.getParentBs().getBone();
		}
	}

//	public void updateSelectionPicks() {
//		listenSelection = false;
//		List<BoneShell> selectedValuesList = importMotionIntoRecBoneList.getSelectedValuesList();
//		updateRecModMotionBonesListModel();
//
//
//		final int[] indices = new int[selectedValuesList.size()];
//		for (int i = 0; i < indices.length; i++) {
//			indices[i] = recModMotionBonesListModel.indexOf(selectedValuesList.get(i));
//		}
//		importMotionIntoRecBoneList.setSelectedIndices(indices);
//		listenSelection = true;
//
//		List<BoneShell> newSelection;
//		if (importTypeBox.getSelectedIndex() == 1) {
//			newSelection = selectedValuesList;
//		} else {
//			newSelection = new ArrayList<>();
//		}
//
//		for (final BoneShell bs : oldSelection) {
//			bs.setImportBone(null);
//		}
//		for (BoneShell bs : newSelection) {
//			bs.setImportBone(selectedBone.getBone());
//		}
//
//		oldSelection = newSelection;
//
//		final long nanoStart = System.nanoTime();
//		futureBones = mht.getFutureBoneListExtended(false);
//		final long nanoEnd = System.nanoTime();
//		System.out.println("updating future bone list took " + (nanoEnd - nanoStart) + " ns");
//	}

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
			System.out.println("Update list, BEFORE values: ");
			for (BoneShell bs : importMotionIntoRecBoneList.getSelectedValuesList()) {
				if (bs.getImportBoneShell() == selectedBone) {
					bs.setImportBoneShell(null);
					System.out.println("import to null");
				} else {
					bs.setImportBoneShell(selectedBone);
					System.out.println("import to: " + selectedBone);
				}
				System.out.println(bs + ", importBs: " + bs.getImportBoneShell());
			}
			System.out.println("_______________");
			importMotionIntoRecBoneList.setSelectedValue(null, false);
		}
		if (!e.getValueIsAdjusting()) {
			System.out.println("Update list, AFTER values: ");
			for (BoneShell bs : importMotionIntoRecBoneList.getSelectedValuesList()) {
				System.out.println(bs);
			}
			System.out.println("_______________");
		}


	}

	private void queResetList() {
		if (!linkBox.isSelected()) {
			listResetQued = true;
		}
	}

	private CaretListener getCaretListener(JTextField activeSearchField, JTextField inActiveSearchField, Consumer<String> listModelFunction) {
		return new CaretListener() {
			long updateTime = 0;

			@Override
			public void caretUpdate(CaretEvent e) {
				if (linkBox.isSelected()) {
					inActiveSearchField.setText(activeSearchField.getText());
				}
				if (updateTime < System.currentTimeMillis()) {
					updateTime = System.currentTimeMillis() + 500;
					String filterText = activeSearchField.getText();
					listModelFunction.accept(filterText);
				}
			}
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
