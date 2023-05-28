package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.shells.AnimShell;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.AnimListCellRenderer;
import com.hiveworkshop.rms.ui.util.TwiList;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class AnimMultiPanel extends AnimPanel {
	private List<AnimShell> selectedValuesList;
	protected JLabel title;
	protected JLabel subTitle;
	private String reciveText = "<html><p>Use the animation data of the following animation<br>in these animations, where applicable:";
	private String timeScaleDonInfo = "<html><p>Use the animation data of this animation in the<br>following animations, where applicable:";

	public AnimMultiPanel(ModelHolderThing mht, TwiList<AnimShell> animJList) {
		super(mht, animJList);
		setLayout(new MigLayout("gap 0, fill", "[][]", "[][][][][grow]"));
		selectedAnim = null;

		title = new JLabel("Multiple Selected");
		title.setFont(new Font("Arial", Font.BOLD, 26));
		add(title, "align center, spanx, wrap");

		subTitle = new JLabel("XX Selected");
		add(subTitle, "align center, spanx, wrap");

		add(new JLabel(""), "align center, spanx, wrap");


		animDestRenderer = new AnimListCellRenderer().setMarkDontImp(true).setDestList(true);
		animSrcRenderer = new AnimListCellRenderer();


		add(getImportCheckBox(this::setImport), "align center, split");
		add(getReverseCheckBox(this::setInReverse), "wrap");

		sourceList = new SearchListPanel<>(reciveText, this::search)
				.setRenderer(animSrcRenderer)
				.setSelectionConsumer(this::onSourceSelected)
				.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		add(sourceList, "spanx, growx, growy");
	}

	public void updateMultiAnimPanel(List<AnimShell> selectedValuesList) {
		this.selectedValuesList = selectedValuesList;
		subTitle.setText(selectedValuesList.size() + " Selected");
		animSrcRenderer.setSelectedAnims(selectedValuesList);
		animDestRenderer.setSelectedAnims(selectedValuesList);

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

//		updateRecModAnimList(firstAnimShell.isFromDonating());
		updateAnimDataSrcList(firstAnimShell.isFromDonating());

	}

	private void updateRecModAnimList(boolean fromDonating) {
		destList.clearAndReset();

		List<AnimShell> othersAnimShells = fromDonating ? mht.recModAnims : mht.donModAnims;
		List<AnimShell> selfAnimShells = fromDonating ? mht.donModAnims : mht.recModAnims;

		destList.addAll(othersAnimShells);
		destList.addAll(selfAnimShells);

		destList.repaint();
	}

	private void updateAnimDataSrcList(boolean fromDonating) {
		sourceList.clearAndReset();

		List<AnimShell> othersAnimShells = fromDonating ? mht.recModAnims : mht.donModAnims;
		List<AnimShell> selfAnimShells = fromDonating ? mht.donModAnims : mht.recModAnims;

		sourceList.addAll(othersAnimShells);
		sourceList.addAll(selfAnimShells);

		sourceList.repaint();
	}

	private void setImport(boolean doImport) {
		if(selectedValuesList != null){
			for (AnimShell animShell : selectedValuesList) {
				animShell.setDoImport(doImport);
			}
			animJList.repaint();
		}
	}
	private void setInReverse(boolean reverse) {
		if(selectedValuesList != null){
			for (AnimShell animShell : selectedValuesList) {
				animShell.setReverse(reverse);
			}
		}
	}

	private void onSourceSelected(AnimShell animShell){
		if(!selectedValuesList.isEmpty() && animShell != null) {
			AnimShell firstAnimSrc = selectedValuesList.get(0).getAnimDataSrc();
			boolean allSame = selectedValuesList.stream().allMatch(as -> as.getAnimDataSrc() == firstAnimSrc);

			for(AnimShell selectedAnim : selectedValuesList){
				if (allSame && selectedAnim.getAnimDataSrc() == animShell) {
					selectedAnim.setAnimDataSrc(null);
				} else {
					selectedAnim.setAnimDataSrc(animShell);
				}
			}
			SwingUtilities.invokeLater(animJList::repaint);
		}
	}

}
