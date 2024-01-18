package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.shells.AnimShell;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.AnimListCellRenderer;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.ListStatus;
import com.hiveworkshop.rms.ui.util.TwiList;

import javax.swing.*;
import java.util.List;
import java.util.function.Consumer;

public abstract class AnimPanel extends JPanel {

	protected JLabel animInfo;
	protected TriCheckBox inReverse;
	protected TriCheckBox doImportBox;

	protected ModelHolderThing mht;

	protected List<AnimShell> selectedValuesList;
	protected TwiList<AnimShell> animJList;

	protected SearchListPanel<AnimShell> sourceList;
	protected AnimListCellRenderer animSrcRenderer;


	public AnimPanel(ModelHolderThing mht, TwiList<AnimShell> animJList) {
		this.mht = mht;
		this.animJList = animJList;
	}

	public void updateAnimPanel(List<AnimShell> selectedValuesList) {
		this.selectedValuesList = selectedValuesList;

		animInfo.setText(getInfoText());

		AnimShell firstAnimShell = selectedValuesList.get(0);

		boolean firstImp = firstAnimShell.isDoImport();
		if (selectedValuesList.stream().anyMatch(as -> as.isDoImport() != firstImp)) {
			doImportBox.setIndeterminate(true);
		} else {
			doImportBox.setSelected(firstImp);
		}

		boolean firstRev = firstAnimShell.isReverse();
		if (selectedValuesList.stream().anyMatch(as -> as.isReverse() != firstRev)) {
			inReverse.setIndeterminate(true);
		} else {
			inReverse.setSelected(firstRev);
		}

		updateAnimDataSrcList(firstAnimShell.isFromDonating());
	}

	protected abstract String getInfoText();



	private void updateAnimDataSrcList(boolean fromDonating) {
		sourceList.clearAndReset();

		List<AnimShell> othersAnimShells = fromDonating ? mht.recModAnims : mht.donModAnims;
		List<AnimShell> selfAnimShells = fromDonating ? mht.donModAnims : mht.recModAnims;

		sourceList.addAll(othersAnimShells);
		sourceList.addAll(selfAnimShells);

		if(selectedValuesList.size() == 1){
			sourceList.remove(selectedValuesList.get(0));

			if(selectedValuesList.get(0).getAnimDataSrc() != null){
				sourceList.scrollToReveal(selectedValuesList.get(0).getAnimDataSrc());
			}
		}

		sourceList.repaint();
	}

	protected JCheckBox getReverseCheckBox(Consumer<Boolean> boolConsumer) {
		inReverse = new TriCheckBox("Reverse");
		inReverse.setSelected(false);
		inReverse.addActionListener(e -> boolConsumer.accept(inReverse.isSelected()));
		return inReverse;
	}
	protected JCheckBox getImportCheckBox(Consumer<Boolean> boolConsumer) {
		doImportBox = new TriCheckBox("Import");
		doImportBox.setSelected(true);
		doImportBox.addActionListener(e -> boolConsumer.accept(doImportBox.isSelected()));
		return doImportBox;
	}

	protected boolean search(AnimShell animShell, String text){
		return animShell.getDisplayName().matches("(?i).*" + text + ".*");
	}

	protected void onSourceSelected(AnimShell animShell){
		if (animShell != null) {
			boolean allHasAnimAsSrc = selectedValuesList.stream().allMatch(as -> as.getAnimDataSrc() == animShell);
			AnimShell animToSet = allHasAnimAsSrc ? null : animShell;

			for (AnimShell selectedAnim : selectedValuesList) {
				if (selectedAnim != null) {
					selectedAnim.setAnimDataSrc(animToSet);
				}
			}
			SwingUtilities.invokeLater(animJList::repaint);
		}
	}

	protected void setImport(boolean doImport) {
		if(selectedValuesList != null){
			for (AnimShell animShell : selectedValuesList) {
				animShell.setDoImport(doImport);
			}
			animJList.repaint();
		}
	}
	protected void setInReverse(boolean reverse) {
		if(selectedValuesList != null){
			for (AnimShell animShell : selectedValuesList) {
				animShell.setReverse(reverse);
			}
			animJList.repaint();
		}
	}
	protected boolean notAllSelected(Object animShell, boolean isSel) {
		return selectedValuesList.stream().allMatch(o -> o != null && o.getAnimDataSrc() == animShell);
	}
	protected boolean isSrcSelected(Object animShell, boolean isSel) {
		return selectedValuesList.stream().anyMatch(o -> o != null && o.getAnimDataSrc() == animShell);
	}
	protected ListStatus getSrcStatus(Object animShell) {
		return ListStatus.FREE;
	}
}
