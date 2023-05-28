package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.shells.IdObjectShell;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.BoneShellListCellRenderer;
import com.hiveworkshop.rms.ui.util.TwiList;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.List;

public class BoneMultiPanel extends BonePanel {
	private List<IdObjectShell<?>> selectedValuesList;

	public BoneMultiPanel(ModelHolderThing mht, TwiList<IdObjectShell<?>> allBoneShellJList, BoneShellListCellRenderer renderer) {
		super(mht, "Multiple Selected", allBoneShellJList, renderer);
		setLayout(new MigLayout("gap 0, fill", "[grow][grow]", "[][][grow]"));
		selectedBone = null;
		add(title, "align center, wrap");

		doImportBox = getImportCheckBox(this::setImport);
		add(doImportBox, "split, spanx");
		prioSelfBox = getPrioSelfCheckBox(this::setPrioSelf);
		add(prioSelfBox, "wrap");


		sourcesList = new SearchListPanel<>("Bone to receive motion from", this::idObjectShellNameFilter)
				.setSelectionConsumer(this::onSourceSelected)
				.setRenderer(motionReceiveRenderer)
				.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		parentList = new SearchListPanel<>("Parent:      (Old Parent: {no parent})", this::idObjectShellNameFilter)
				.setSelectionConsumer(this::onParentSelected)
				.setRenderer(renderer)
				.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);


		add(sourcesList, "cell 0 2, growx, growy");
		add(parentList, "cell 1 2, growx, growy");
	}


	public void setSelectedBones(List<IdObjectShell<?>> selectedValuesList) {
		this.selectedValuesList = selectedValuesList;
		renderer.setSelectedBones(selectedValuesList);

		motionReceiveRenderer.setSelectedBoneShell(selectedValuesList);
		motionDestRenderer.setSelectedBoneShell(selectedValuesList);
		IdObjectShell<?> firstShell = selectedValuesList.get(0);
		boolean firstImp = firstShell.getShouldImport();
		if (selectedValuesList.stream().anyMatch(bs -> bs.getShouldImport() != firstImp)) {
			doImportBox.setIndeterminate(true);
//			setMultiTypes();
		} else {
			doImportBox.setSelected(firstImp);
		}

		parentList.clearAndReset().addAll(mht.getFutureBoneHelperList());
		IdObjectShell<?> oldParentShell = firstShell.getOldParentShell();

		parentList.setText(getParentString(oldParentShell, selectedValuesList.stream().allMatch(bs -> bs.getOldParentShell() == oldParentShell)));

		updateMotionScrBones(firstShell.isFromDonating());
	}
	private String getParentString(IdObjectShell<?> oldParentShell, boolean all) {
		if (all && oldParentShell != null) {
			return "Parent:      (Old Parent: " + oldParentShell.getName() + ")";
		} else if (all) {
			return "Parent:      (Old Parent: {no parent})";
		} else {
			return "Parent:      (Old Parent: {multiple parents})";
		}
	}

	private void updateMotionScrBones(boolean fromDonating) {
		sourcesList.clearAndReset();
		List<IdObjectShell<?>> othersBoneShells = fromDonating ? mht.recModBoneShells : mht.donModBoneShells;
		List<IdObjectShell<?>> selfBoneShells = fromDonating ? mht.donModBoneShells : mht.recModBoneShells;

		sourcesList.addAll(othersBoneShells);
		sourcesList.addAll(selfBoneShells);
		sourcesList.repaint();
	}
	private void setImport(boolean doImport) {
		if(selectedValuesList != null){
			for (IdObjectShell<?> boneShell : selectedValuesList) {
				boneShell.setShouldImport(doImport);
			}
			allBoneShellJList.repaint();
			parentList.repaint();
		}
	}
	private void setPrioSelf(boolean prioSelf) {
		if(selectedValuesList != null){
			for (IdObjectShell<?> boneShell : selectedValuesList) {
				boneShell.setPrioritizeMotionFromSelf(prioSelf);
			}
		}
	}

	private void onSourceSelected(IdObjectShell<?> bs){
		IdObjectShell<?> firstSourceShell = selectedValuesList.get(0).getMotionSrcShell();
		boolean allSame = selectedValuesList.stream().allMatch(selBs -> selBs.getMotionSrcShell() == firstSourceShell);
		for (IdObjectShell<?> boneShell : selectedValuesList) {
			if (allSame && boneShell.getMotionSrcShell() == bs) {
				boneShell.setMotionSrcShell(null);
			} else {
				boneShell.setMotionSrcShell(bs);
			}
		}
	}
	private void onParentSelected(IdObjectShell<?> bs){
		IdObjectShell<?> firstParent = selectedValuesList.get(0).getNewParentShell();
		boolean allSame = selectedValuesList.stream().allMatch(selBs -> selBs.getNewParentShell() == firstParent);
		for (IdObjectShell<?> boneShell : selectedValuesList) {
			if (allSame && boneShell.getNewParentShell() == bs) {
				boneShell.setParent(null);
			} else {
				boneShell.setParent(bs);
			}
		}
	}
}
