package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.ui.gui.modeledit.BoneShell;
import com.hiveworkshop.rms.util.IterableListModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BonePanel extends JPanel {
	static final String IMPORT = "Import this bone";
	static final String MOTIONFROM = "Import motion to pre-existing:";
	static final String LEAVE = "Do not import";
	Bone bone;
	JLabel title;
	String[] impOptions = {IMPORT, MOTIONFROM, LEAVE};

	JComboBox<String> importTypeBox = new JComboBox<>(impOptions);

	// List for which bone to transfer motion
	IterableListModel<BoneShell> existingBones;
	IterableListModel<BoneShell> listModel;
	JList<BoneShell> boneList;
	JScrollPane boneListPane;
	JPanel cardPanel;
	CardLayout cards = new CardLayout();
	JPanel dummyPanel = new JPanel();
	IterableListModel<BoneShell> futureBones;
	JList<BoneShell> futureBonesList;
	JScrollPane futureBonesListPane;
	JLabel parentTitle;
	List<BoneShell> oldSelection = new ArrayList<>();
	boolean listenSelection = true;
	ModelHolderThing mht;

	protected BonePanel() {
		// This constructor is negative mojo
	}

	public BonePanel(ModelHolderThing mht, final Bone whichBone, final IterableListModel<BoneShell> existingBonesList, final BoneShellListCellRenderer renderer) {
		this.mht = mht;
		setLayout(new MigLayout("gap 0"));
		bone = whichBone;
		existingBones = existingBonesList;
		listModel = new IterableListModel<>(existingBonesList);

		title = new JLabel(bone.getClass().getSimpleName() + " \"" + bone.getName() + "\"");
		title.setFont(new Font("Arial", Font.BOLD, 26));

		add(title, "cell 0 0, spanx, align center, wrap");
		if (bone.getParent() != null) {
			parentTitle = new JLabel("Parent:      (Old Parent: " + bone.getParent().getName() + ")");
		} else {
			parentTitle = new JLabel("Parent:      (Old Parent: {no parent})");
		}
		add(parentTitle, "cell 2 1");

		importTypeBox.setEditable(false);
		importTypeBox.addActionListener(e -> ShowCorrectCard());
		importTypeBox.setMaximumSize(new Dimension(200, 20));
		add(importTypeBox, "cell 0 1");

		boneList = new JList<>(listModel);
		boneList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		boneList.setCellRenderer(renderer);
		boneList.addListSelectionListener(this::updateList);
		boneListPane = new JScrollPane(boneList);

		for (BoneShell bs : listModel) {
			if (bs.bone.getName().equals(bone.getName())
					&& (bs.importBone == null)
					&& (!bs.bone.getName().contains("Mesh") && !bs.bone.getName().contains("Object") && !bs.bone.getName().contains("Box") || bs.bone.getPivotPoint().equalLocs(bone.getPivotPoint()))) {
				boneList.setSelectedValue(bs, true);
				bs.setImportBone(bone);
				break;
				// System.out.println("GREAT BALLS OF FIRE");
			}
		}

		futureBones = mht.getFutureBoneListExtended(true);
		futureBonesList = new JList<>(futureBones);
		futureBonesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		futureBonesList.setCellRenderer(renderer);
		futureBonesListPane = new JScrollPane(futureBonesList);
		add(futureBonesListPane, "cell 2 2, growy");

		cardPanel = new JPanel(cards);
		cardPanel.add(boneListPane, "boneList");
		cardPanel.add(dummyPanel, "blank");
		cards.show(cardPanel, "blank");
		add(cardPanel, "cell 1 2, growy");
	}

	private void ShowCorrectCard() {
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
			if (bs.bone == bone.getParent()) {
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

	public void updateSelectionPicks() {
		listenSelection = false;
		List<BoneShell> selectedValuesList = boneList.getSelectedValuesList();
		listModel.clear();
		for (BoneShell bs : existingBones) {
			if ((bs.importBone == null) || (bs.importBone == bone)) {
				listModel.addElement(bs);
			}
		}


		final int[] indices = new int[selectedValuesList.size()];
		for (int i = 0; i < indices.length; i++) {
			indices[i] = listModel.indexOf(selectedValuesList.get(i));
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
			bs.setImportBone(bone);
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
}
