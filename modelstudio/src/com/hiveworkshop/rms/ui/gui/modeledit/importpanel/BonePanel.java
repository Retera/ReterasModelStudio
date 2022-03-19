package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.ui.gui.modeledit.renderers.BoneShellListCellRenderer;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.BoneShellMotionListCellRenderer;
import com.hiveworkshop.rms.ui.util.SearchableList;
import com.hiveworkshop.rms.util.IterableListModel;
import com.hiveworkshop.rms.util.TwiComboBox;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BonePanel extends JPanel {
	protected ModelHolderThing mht;
	protected IdObjectShell<?> selectedBone;
	protected JLabel title;
	protected TwiComboBox<IdObjectShell.ImportType> importTypeBox;

	protected SearchableList<IdObjectShell<?>> motionDestList;
	protected SearchableList<IdObjectShell<?>> motionSrcList;
	protected SearchableList<IdObjectShell<?>> parentList;

	private List<IdObjectShell<?>> bonesWithMotion = new ArrayList<>();

	protected JPanel cardPanel;
	protected CardLayout cards = new CardLayout();
	protected JPanel dummyPanel = new JPanel();
	private IterableListModel<IdObjectShell<?>> futureBones;
	private JLabel parentTitle;

	private boolean listenSelection = true;

	protected BoneShellMotionListCellRenderer motionReceiveRenderer;
	protected BoneShellListCellRenderer renderer;


	protected BonePanel(ModelHolderThing mht, String titleString, BoneShellListCellRenderer renderer) {
		this.mht = mht;
		this.renderer = renderer;

		title = new JLabel(titleString);
		title.setFont(new Font("Arial", Font.BOLD, 26));

		motionReceiveRenderer = new BoneShellMotionListCellRenderer(mht.receivingModel, mht.donatingModel);
		// This constructor is negative mojo
	}

	public BonePanel(ModelHolderThing mht, BoneShellListCellRenderer renderer) {
		this(mht, "Select a Bone", renderer);
		setLayout(new MigLayout("gap 0, fill", "[grow][grow]", "[][grow]"));
		add(title, "cell 0 0, spanx, align center, wrap");

		JPanel leftPanel = new JPanel(new MigLayout("gap 0, fill", "[]", "[][grow]"));

		importTypeBox = getImportTypeComboBox();
		leftPanel.add(importTypeBox, "wrap");

		JPanel importMotionIntoPanel = getImportMotionIntoPanel();
		JPanel receiveMotionFromPanel = getReciveMotionFromPanel();

		cardPanel = new JPanel(cards);
		cardPanel.add(importMotionIntoPanel, IdObjectShell.ImportType.MOTION_FROM.name());
		cardPanel.add(receiveMotionFromPanel, IdObjectShell.ImportType.RECEIVE_MOTION.name());
		cardPanel.add(dummyPanel, "blank");

		leftPanel.add(cardPanel, "growx, growy, spanx");

		JPanel rightPanel = getRightPanel(renderer);

		add(leftPanel, "cell 0 1, growx, growy");
		add(rightPanel, "cell 1 1, growx, growy");
	}

	private JPanel getRightPanel(BoneShellListCellRenderer renderer) {
		JPanel rightPanel = new JPanel(new MigLayout("gap 0, fill", "[][]", "[][][grow]"));

		rightPanel.add(new JLabel("Parent:"), "align left, gap 20 0 0 0");
		parentTitle = new JLabel("Parent:      (Old Parent: {no parent})");
		rightPanel.add(parentTitle, "wrap");
		parentList = new SearchableList<>(this::idObjectShellNameFilter)
				.addSelectionListener(this::setParent)
				.setRenderer(renderer);
		parentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		rightPanel.add(parentList.getSearchField(), "grow, spanx");

		rightPanel.add(parentList.getScrollableList(), "spanx, growx, growy");
		return rightPanel;
	}

	protected TwiComboBox<IdObjectShell.ImportType> getImportTypeComboBox() {
		TwiComboBox<IdObjectShell.ImportType> importTypeBox = new TwiComboBox<>(IdObjectShell.ImportType.values(), IdObjectShell.ImportType.RECEIVE_MOTION);
		importTypeBox.setStringFunctionRender(i -> i instanceof IdObjectShell.ImportType ? ((IdObjectShell.ImportType)i).getDispText() : "Multiple Selected");
		importTypeBox.addOnSelectItemListener(this::showImportTypeCard);
		importTypeBox.setEditable(false);
		importTypeBox.setMaximumSize(new Dimension(200, 20));
		return importTypeBox;
	}

	protected JPanel getImportMotionIntoPanel() {
		JPanel importMotionIntoPanel = new JPanel(new MigLayout("gap 0, ins 0, fill", "[grow][]", "[][][grow]"));
		importMotionIntoPanel.add(new JLabel("Bones to receive motion"), "wrap");
		motionDestList = new SearchableList<>(this::idObjectShellNameFilter)
				.addSelectionListener(this::motionDestChosen)
				.setRenderer(motionReceiveRenderer);
		motionDestList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		importMotionIntoPanel.add(motionDestList.getSearchField(), "grow, wrap");
		importMotionIntoPanel.add(motionDestList.getScrollableList(), "spanx, growx, growy");
		return importMotionIntoPanel;
	}

	protected JPanel getReciveMotionFromPanel() {
		JPanel importMotionIntoPanel = new JPanel(new MigLayout("gap 0, ins 0, fill", "[grow][]", "[][][grow]"));
		importMotionIntoPanel.add(new JLabel("Bone to receive motion from"), "wrap");
		motionSrcList = new SearchableList<>(this::idObjectShellNameFilter)
				.addSelectionListener(this::motionSrcChosen)
				.setRenderer(motionReceiveRenderer);
		motionSrcList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		importMotionIntoPanel.add(motionSrcList.getSearchField(), "grow, wrap");
		importMotionIntoPanel.add(motionSrcList.getScrollableList(), "spanx, growx, growy");
		return importMotionIntoPanel;
	}


	public void setSelectedBone(IdObjectShell<?> boneShell) {
		selectedBone = boneShell;
		motionReceiveRenderer.setSelectedBoneShell(selectedBone);
		futureBones = mht.getFutureBoneHelperList();
		renderer.setSelectedBoneShell(boneShell);
		setTitles();
		parentList.setListModel(futureBones);
		updateRecModMotionBonesListModel();
		updateDonModMotionScrBonesListModel();
		scrollToRevealParent(boneShell);
		setImportStatus(selectedBone.getImportStatus());
		repaint();
	}

	private void scrollToRevealParent(IdObjectShell<?> boneShell) {
		if (boneShell.getNewParentShell() != null) {
			int i = parentList.getFullListModel().indexOf(boneShell.getNewParentShell());
			if (i != -1) {
				Rectangle cellBounds = parentList.getCellBounds(i, i);
				if (cellBounds != null) {
					parentList.scrollRectToVisible(cellBounds);
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

	protected void showImportTypeCard(IdObjectShell.ImportType type) {
		if(type != null) {
			selectedBone.setImportStatus(type);

			boolean pastListSelectionState = listenSelection;
			listenSelection = false;
			if (type == IdObjectShell.ImportType.MOTION_FROM) {
				cards.show(cardPanel, IdObjectShell.ImportType.MOTION_FROM.name());
			}else if (type == IdObjectShell.ImportType.RECEIVE_MOTION) {
				cards.show(cardPanel, IdObjectShell.ImportType.RECEIVE_MOTION.name());
			} else {
				cards.show(cardPanel, "blank");
			}
			listenSelection = pastListSelectionState;
		}
	}

	public void setImportStatus(IdObjectShell.ImportType type) {
		importTypeBox.setSelectedItem(type);
		showImportTypeCard(type);
	}

	private void setParent(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
			IdObjectShell<?> selectedValue = parentList.getSelectedValue();
			if(selectedValue != null){
				if (selectedValue == selectedBone.getNewParentShell()) {
					selectedBone.setNewParentShell(null);
				} else {
					selectedBone.setNewParentShell(selectedValue);
				}
				parentList.setSelectedValue(null, false);
			}
		}
	}

	private void updateRecModMotionBonesListModel() {
		motionDestList.clear().resetFilter();
		bonesWithMotion.clear();
		for (IdObjectShell<?> bs : mht.recModBoneShells) {
			if (bs.getMotionSrcShell() == null) {
				motionDestList.add(bs);
			} else if (bs.getMotionSrcShell() == selectedBone) {
				motionDestList.add(0, bs);
			} else {
				bonesWithMotion.add(bs);
			}
		}
		motionDestList.addAll(bonesWithMotion);
	}

	private void updateDonModMotionScrBonesListModel() {
		motionSrcList.clear().resetFilter();
		bonesWithMotion.clear();
		for (IdObjectShell<?> bs : mht.donModBoneShells) {
			if (selectedBone.getMotionSrcShell() == bs) {
				motionSrcList.add(0, bs);
			} else if (bs.getImportStatus() == IdObjectShell.ImportType.MOTION_FROM) {
				motionSrcList.add(bs);
			} else {
				bonesWithMotion.add(bs);
			}
		}
		motionSrcList.addAll(bonesWithMotion);
	}

	protected void motionDestChosen(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) {
			for (IdObjectShell<?> bs : motionDestList.getSelectedValuesList()) {
				if (bs.getMotionSrcShell() == selectedBone) {
					bs.setMotionSrcShell(null);
				} else {
					bs.setMotionSrcShell(selectedBone);
				}
			}
			motionDestList.setSelectedValue(null, false);
		}
	}

	protected void motionSrcChosen(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) {
			IdObjectShell<?> bs = motionSrcList.getSelectedValue();
			if (bs != null) {
				bs.addMotionDest(selectedBone);
			}
			motionSrcList.setSelectedValue(null, false);
		}
	}

	protected boolean idObjectShellNameFilter(IdObjectShell<?> boneShell, String filterText) {
		return boneShell.getName().toLowerCase().contains(filterText.toLowerCase());
	}
}
