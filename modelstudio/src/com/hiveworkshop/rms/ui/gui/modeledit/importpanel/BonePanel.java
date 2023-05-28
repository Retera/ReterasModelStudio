package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.shells.IdObjectShell;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.BoneShellListCellRenderer;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.BoneShellMotionListCellRenderer;
import com.hiveworkshop.rms.ui.util.TwiList;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

public class BonePanel extends JPanel {
	protected ModelHolderThing mht;
	protected IdObjectShell<?> selectedBone;
	protected JLabel title;
	protected TriCheckBox doImportBox;
	protected TriCheckBox prioSelfBox;

	protected BoneShellMotionListCellRenderer motionReceiveRenderer;
	protected BoneShellMotionListCellRenderer motionDestRenderer;
	protected BoneShellListCellRenderer renderer;

	protected SearchListPanel<IdObjectShell<?>> destsList;
	protected SearchListPanel<IdObjectShell<?>> sourcesList;
	protected SearchListPanel<IdObjectShell<?>> parentList;
	protected final TwiList<IdObjectShell<?>> allBoneShellJList;


	protected BonePanel(ModelHolderThing mht, String titleString, TwiList<IdObjectShell<?>> allBoneShellJList, BoneShellListCellRenderer renderer) {
		this.mht = mht;
		this.renderer = renderer;
		this.allBoneShellJList = allBoneShellJList;

		title = new JLabel(titleString);
		title.setFont(new Font("Arial", Font.BOLD, 26));

		motionReceiveRenderer = new BoneShellMotionListCellRenderer(mht.receivingModel, mht.donatingModel);
		motionDestRenderer = new BoneShellMotionListCellRenderer(mht.receivingModel, mht.donatingModel).setDestList(true);

		// This constructor is negative mojo
	}

	public BonePanel(ModelHolderThing mht, TwiList<IdObjectShell<?>> allBoneShellJList, BoneShellListCellRenderer renderer) {
		this(mht, "Select a Bone", allBoneShellJList, renderer);
		setLayout(new MigLayout("gap 0, fill", "[grow][grow]", "[][][grow]"));
		add(title, "cell 0 0, spanx, align center, wrap");

		doImportBox = getImportCheckBox(this::setImport);
		add(doImportBox, "split, spanx");
		prioSelfBox = getPrioSelfCheckBox(this::setPrioSelf);
		add(prioSelfBox, "wrap");


//		destsList = new SearchListPanel<>("Bones to receive motion", this::idObjectShellNameFilter)
//				.setSelectionConsumer(this::onDestSelected)
//				.setRenderer(motionDestRenderer)
//				.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

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

	protected TriCheckBox getImportCheckBox(Consumer<Boolean> boolConsumer) {
		TriCheckBox doImportBox = new TriCheckBox("Import");
		doImportBox.setSelected(true);
		doImportBox.addActionListener(e -> boolConsumer.accept(doImportBox.isSelected()));
		return doImportBox;
	}
	private void setImport(boolean doImport) {
		selectedBone.setShouldImport(doImport);
		allBoneShellJList.repaint();
		parentList.repaint();
	}
	protected TriCheckBox getPrioSelfCheckBox(Consumer<Boolean> boolConsumer) {
		TriCheckBox doPrioSelf = new TriCheckBox("Prioritize Motion from Self");
		doPrioSelf.setSelected(false);
		doPrioSelf.addActionListener(e -> boolConsumer.accept(doPrioSelf.isSelected()));
		return doPrioSelf;
	}
	private void setPrioSelf(boolean prioSelf) {
		selectedBone.setPrioritizeMotionFromSelf(prioSelf);
	}


	public void setSelectedBone(IdObjectShell<?> boneShell) {
		selectedBone = boneShell;
		motionReceiveRenderer.setSelectedBoneShell(selectedBone);
//		motionDestRenderer.setSelectedBoneShell(selectedBone.getMotionDestShells());
		motionDestRenderer.setSelectedBoneShell(selectedBone);
		renderer.setSelectedBoneShell(boneShell);
		title.setText(getTitleText());

		parentList.clearAndReset().addAll(mht.getFutureBoneHelperList());
		parentList.setText(getParentString(boneShell.getOldParentShell()));
		parentList.scrollToReveal(boneShell.getNewParentShell());

		doImportBox.setSelected(boneShell.getShouldImport());
//		updateMotionDestBones(boneShell.isFromDonating());
		updateMotionScrBones(boneShell.isFromDonating());
		repaint();
	}


	private String getTitleText() {
		return selectedBone.getIdObject().getClass().getSimpleName() + " \"" + selectedBone.getName() + "\"";
	}

	private String getParentString(IdObjectShell<?> oldParentShell) {
		if (oldParentShell != null) {
			return "Parent:      (Old Parent: " + oldParentShell.getName() + ")";
		} else {
			return "Parent:      (Old Parent: {no parent})";
		}
	}

	private void updateMotionDestBones(boolean fromDonating) {
		destsList.clearAndReset();

		List<IdObjectShell<?>> othersBoneShells = fromDonating ? mht.recModBoneShells : mht.donModBoneShells;
		List<IdObjectShell<?>> selfBoneShells = fromDonating ? mht.donModBoneShells : mht.recModBoneShells;

		destsList.addAll(othersBoneShells);
		destsList.addAll(selfBoneShells);
		destsList.remove(selectedBone);

		if(!selectedBone.getMotionDestShells().isEmpty()){
			destsList.scrollToReveal(selectedBone.getMotionDestShells().get(0));
		}
		destsList.repaint();
	}

	private void updateMotionScrBones(boolean fromDonating) {
		sourcesList.clearAndReset();

		List<IdObjectShell<?>> othersBoneShells = fromDonating ? mht.recModBoneShells : mht.donModBoneShells;
		List<IdObjectShell<?>> selfBoneShells = fromDonating ? mht.donModBoneShells : mht.recModBoneShells;

		sourcesList.addAll(othersBoneShells);
		sourcesList.addAll(selfBoneShells);
		sourcesList.remove(selectedBone);

		if(selectedBone.getMotionSrcShell() != null){
			sourcesList.scrollToReveal(selectedBone.getMotionSrcShell());
		}

		sourcesList.repaint();
	}


	private void onDestSelected(IdObjectShell<?> bs){
		if(selectedBone != null && bs != null){
			if (bs.getMotionSrcShell() == selectedBone) {
				bs.setMotionSrcShell(null);
			} else {
				bs.setMotionSrcShell(selectedBone);
			}
		}
	}

	private void onSourceSelected(IdObjectShell<?> bs){
		if(selectedBone != null && bs != null){
			if (selectedBone.getMotionSrcShell() == bs) {
				selectedBone.setMotionSrcShell(null);
			} else {
				selectedBone.setMotionSrcShell(bs);
			}
		}
	}
	private void onParentSelected(IdObjectShell<?> bs){
		if(selectedBone != null && bs != null){
			if (selectedBone.getNewParentShell() == bs) {
				selectedBone.setParent(null);
			} else {
				selectedBone.setParent(bs);
			}
		}
	}

	protected boolean idObjectShellNameFilter(IdObjectShell<?> boneShell, String filterText) {
//		return boneShell.getName().toLowerCase().contains(filterText.toLowerCase());
		return boneShell.getName().matches("(?i).*" + filterText + ".*");
	}
}
