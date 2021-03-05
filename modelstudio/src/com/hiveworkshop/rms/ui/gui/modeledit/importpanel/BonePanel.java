package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.util.IterableListModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ListSelectionEvent;
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
	IterableListModel<BoneShell> recModOrgBonesListModel;
	IterableListModel<BoneShell> filteredRecModOrgBonesListModel = new IterableListModel<>();
	JList<BoneShell> boneList;
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


	protected BonePanel() {
		// This constructor is negative mojo
	}

	public BonePanel(ModelHolderThing mht, final BoneShellListCellRenderer renderer) {
		this.mht = mht;
		setLayout(new MigLayout("gap 0, fill", "[][]", "[][grow]"));
		recModOrgBonesListModel = new IterableListModel<>(mht.recModOrgBones);
		leftSearchField = new JTextField();
		rightSearchField = new JTextField();
		leftSearchField.addFocusListener(new FocusAdapter() {
			CaretListener caretListener = getCaretListener(leftSearchField, rightSearchField, (string) -> setRecModelBonesFilter(string));

			@Override
			public void focusGained(FocusEvent e) {
				leftSearchField.addCaretListener(caretListener);
			}

			@Override
			public void focusLost(FocusEvent e) {
				leftSearchField.removeCaretListener(caretListener);
			}
		});
		rightSearchField.addFocusListener(new FocusAdapter() {
			CaretListener caretListener = getCaretListener(rightSearchField, leftSearchField, (string) -> setFutureBonesFilter(string));

			@Override
			public void focusGained(FocusEvent e) {
				rightSearchField.addCaretListener(caretListener);
			}

			@Override
			public void focusLost(FocusEvent e) {
				rightSearchField.removeCaretListener(caretListener);
			}
		});
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

		boneList = new JList<>();
		boneList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		boneList.setCellRenderer(renderer);
		boneList.addListSelectionListener(this::updateList);
		JScrollPane boneListPane = new JScrollPane(boneList);
		JPanel importIntoPanel = new JPanel(new MigLayout("gap 0, fill", "[grow][]", "[][grow]"));
		importIntoPanel.add(leftSearchField, "grow");
		importIntoPanel.add(linkBox, "wrap");
		importIntoPanel.add(boneListPane, "spanx");

		cardPanel = new JPanel(cards);
		cardPanel.add(importIntoPanel, "boneList");
		cardPanel.add(dummyPanel, "blank");
		cards.show(cardPanel, "blank");
		leftPanel.add(cardPanel, "growx, growy, spanx");

		JPanel rightPanel = new JPanel(new MigLayout("gap 0, fill", "[][]", "[][][grow]"));

		rightPanel.add(new JLabel("Parent:"), "align left, gap 20 0 0 0");
		parentTitle = new JLabel("Parent:      (Old Parent: {no parent})");
		rightPanel.add(parentTitle, "wrap");
		rightPanel.add(rightSearchField, "grow, spanx");

		futureBonesList = new JList<>();
		futureBonesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		futureBonesList.setCellRenderer(renderer);
		futureBonesList.addListSelectionListener(e -> setParent());
		JScrollPane futureBonesListPane = new JScrollPane(futureBonesList);
		futureBonesListPane.setBackground(Color.orange);
		futureBonesListPane.setOpaque(true);
		rightPanel.add(futureBonesListPane, "spanx, growx, growy");

		add(leftPanel, "cell 0 1, growx, growy");
		add(rightPanel, "cell 1 1, growx, growy");


	}
//
//	public BonePanel(ModelHolderThing mht, final BoneShellListCellRenderer renderer) {
//		this.mht = mht;
//		setLayout(new MigLayout("gap 0, fill", "[][]", "[][]"));
//		recModOrgBonesListModel = new IterableListModel<>(mht.recModOrgBones);
//
//
//		title = new JLabel("Select a Bone");
//		title.setFont(new Font("Arial", Font.BOLD, 26));
//
//		add(title, "cell 0 0, spanx, align center, wrap");
//
//		parentTitle = new JLabel("Parent:      (Old Parent: {no parent})");
//		add(parentTitle, "cell 2 1");
//
//		importTypeBox.setEditable(false);
//		importTypeBox.addActionListener(e -> showImportTypeCard());
//		importTypeBox.setMaximumSize(new Dimension(200, 20));
//		add(importTypeBox, "cell 0 1");
//
//		boneList = new JList<>();
//		boneList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
//		boneList.setCellRenderer(renderer);
//		boneList.addListSelectionListener(this::updateList);
//		JScrollPane boneListPane = new JScrollPane(boneList);
//
//		futureBonesList = new JList<>();
//		futureBonesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//		futureBonesList.setCellRenderer(renderer);
//		futureBonesList.addListSelectionListener(e -> setParent());
//		JScrollPane futureBonesListPane = new JScrollPane(futureBonesList);
//		futureBonesListPane.setBackground(Color.orange);
//		futureBonesListPane.setOpaque(true);
//		add(futureBonesListPane, "cell 2 2, growy");
//
//		cardPanel = new JPanel(cards);
//		cardPanel.add(boneListPane, "boneList");
//		cardPanel.add(dummyPanel, "blank");
//		cards.show(cardPanel, "blank");
//		add(cardPanel, "cell 1 2, growy");
//	}

	public void setFutureBoneListModel(IterableListModel<BoneShell> model) {
		futureBonesList.setModel(model);
	}

	private void selectImportBone() {
		for (BoneShell bs : recModOrgBonesListModel) {
			if (bs.bone.getName().equals(selectedBone.getName())
					&& (bs.importBone == null)
					&& (!bs.bone.getName().contains("Mesh") && !bs.bone.getName().contains("Object") && !bs.bone.getName().contains("Box") || bs.bone.getPivotPoint().equalLocs(selectedBone.getBone().getPivotPoint()))) {
				boneList.setSelectedValue(bs, true);
				bs.setImportBoneShell(selectedBone);
				break;
				// System.out.println("GREAT BALLS OF FIRE");
			}
		}
	}

	private void setImportIntoListModel(IterableListModel<BoneShell> model) {
		boneList.setModel(model);
	}

	//	private void setBone(Bone whichBone) {
//		bone = whichBone;
//	}
	public void setSelectedBone(BoneShell whichBone) {
		selectedBone = whichBone;
		initList();
		setTitles();
		setImportIntoListModel(recModOrgBonesListModel);
		selectImportBone();
		futureBones = mht.getFutureBoneListExtended(true);
		setFutureBoneListModel(futureBones);
		setParent(selectedBone.getParentBs());
		setSelectedIndex(selectedBone.getImportStatus());
	}

	private void setTitles() {
		title.setText(selectedBone.getBone().getClass().getSimpleName() + " \"" + selectedBone.getName() + "\"");
		if (selectedBone.getParentBs() != null) {
			parentTitle.setText("Parent:      (Old Parent: " + selectedBone.getParentBs().getName() + ")");
		} else {
			parentTitle.setText("Parent:      (Old Parent: {no parent})");
		}
	}

	private void showImportTypeCard() {
		selectedBone.setImportStatus(importTypeBox.getSelectedIndex());
		updateSelectionPicks();
		final boolean pastListSelectionState = listenSelection;
		listenSelection = false;
		if (importTypeBox.getSelectedItem() == MOTIONFROM) {
			cards.show(cardPanel, "boneList");
		} else {
			cards.show(cardPanel, "blank");
		}
		listenSelection = pastListSelectionState;
	}

	public void initList() {
		futureBones = mht.getFutureBoneListExtended(false);
		for (BoneShell bs : futureBones) {
			if (bs == selectedBone.getParentBs()) {
				futureBonesList.setSelectedValue(bs, true);
			}
		}
	}

	public int getSelectedIndex() {
		return importTypeBox.getSelectedIndex();
	}

	public void setSelectedIndex(final int index) {
		importTypeBox.setSelectedIndex(index);
	}

	public void setSelectedValue(final String value) {
		importTypeBox.setSelectedItem(value);
	}

	public void setParent(final BoneShell pick) {
		futureBonesList.setSelectedValue(pick, true);
	}

	private void setParent() {
		selectedBone.setParentBs(futureBonesList.getSelectedValue());
	}

	public void updateSelectionPicks() {
		listenSelection = false;
		List<BoneShell> selectedValuesList = boneList.getSelectedValuesList();
		recModOrgBonesListModel.clear();
		for (BoneShell bs : mht.recModOrgBones) {
			if ((bs.importBone == null) || (bs.importBone == selectedBone.getBone())) {
				recModOrgBonesListModel.addElement(bs);
			}
		}


		final int[] indices = new int[selectedValuesList.size()];
		for (int i = 0; i < indices.length; i++) {
			indices[i] = recModOrgBonesListModel.indexOf(selectedValuesList.get(i));
		}
		boneList.setSelectedIndices(indices);
		listenSelection = true;

		List<BoneShell> newSelection;
		if (importTypeBox.getSelectedIndex() == 1) {
			newSelection = selectedValuesList;
		} else {
			newSelection = new ArrayList<>();
		}

		for (final BoneShell bs : oldSelection) {
			bs.setImportBone(null);
		}
		for (BoneShell bs : newSelection) {
			bs.setImportBone(selectedBone.getBone());
		}

		oldSelection = newSelection;

		final long nanoStart = System.nanoTime();
		futureBones = mht.getFutureBoneListExtended(false);
		final long nanoEnd = System.nanoTime();
		System.out.println("updating future bone list took " + (nanoEnd - nanoStart) + " ns");
	}

	private void updateList(ListSelectionEvent e) {
		if (listenSelection && e.getValueIsAdjusting()) {
			updateSelectionPicks();
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
			setImportIntoListModel(recModOrgBonesListModel);
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
			filteredRecModOrgBonesListModel.clear();
			for (BoneShell boneShell : recModOrgBonesListModel) {
				if (boneShell.getName().toLowerCase().contains(filterText.toLowerCase())) {
					filteredRecModOrgBonesListModel.addElement(boneShell);
				}
			}
			setImportIntoListModel(filteredRecModOrgBonesListModel);
		} else {
			setImportIntoListModel(recModOrgBonesListModel);
		}
	}
}
