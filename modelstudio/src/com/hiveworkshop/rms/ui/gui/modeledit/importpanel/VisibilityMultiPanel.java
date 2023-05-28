package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.shells.VisibilityShell;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.VisListCellRenderer;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class VisibilityMultiPanel extends VisibilityPanel {
	ModelHolderThing mht;
	List<VisibilityShell<?>> selectedValuesList;

	public VisibilityMultiPanel(ModelHolderThing mht) {
		this.mht = mht;
		setLayout(new MigLayout("fill, gap 0", "[]", "[][][][grow][]"));
		title = new JLabel("Multiple Selected");
		title.setFont(new Font("Arial", Font.BOLD, 18));
		typeTitle = new JLabel("?");

		favorOld = new TriCheckBox("Favor component's original visibility when combining");
		favorOld.addActionListener(e -> favorOldPressed());

		add(title, "align center, wrap");
		add(new JLabel("Type:"), "align center, split 2");
		add(typeTitle, "wrap");
		add(favorOld, "left, wrap");


		visListCellRenderer = new VisListCellRenderer();
		sourceList = new SearchListPanel<>("Visibility source", this::idObjectShellNameFilter)
				.setSelectionConsumer(this::onSourceSelected)
				.setRenderer(visListCellRenderer)
				.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		add(sourceList, "growx, growy, wrap");
	}


	private void onSourceSelected(VisibilityShell<?> bs){
		if(bs != null){
			VisibilityShell<?> firstVisSource = selectedValuesList.get(0).getVisSource();
			boolean allSame = selectedValuesList.stream().allMatch(vs -> vs.getVisSource() == firstVisSource);
			for(VisibilityShell<?> vs : selectedValuesList){
				if (allSame && vs.getVisSource() == bs) {
					vs.setVisSource(null);
				} else {
					vs.setVisSource(bs);
				}
			}
		}
	}

	public void updateMultiVisPanel(List<VisibilityShell<?>> selectedValuesList){
		this.selectedValuesList = selectedValuesList;
		visListCellRenderer.setSelectedVisibilities(selectedValuesList);

		VisibilityShell<?> firstShell = selectedValuesList.get(0);
		updateSrcList(firstShell.isFromDonating());

		boolean firstIsDoFavorOld = firstShell.isFavorOld();
		if (selectedValuesList.stream().anyMatch(vs -> vs.isFavorOld() != firstIsDoFavorOld)) {
			favorOld.setIndeterminate(true);
		} else {
			this.favorOld.setSelected(firstIsDoFavorOld);
		}
	}

	private void updateSrcList(boolean fromDonating) {
		sourceList.clearAndReset();

		List<VisibilityShell<?>> othersBoneShells = fromDonating ? mht.recModVisibilityShells : mht.donModVisibilityShells;
		List<VisibilityShell<?>> selfBoneShells = fromDonating ? mht.donModVisibilityShells : mht.recModVisibilityShells;

		sourceList.addAll(othersBoneShells);
		sourceList.addAll(selfBoneShells);

		sourceList.repaint();
	}

	private void favorOldPressed() {
		for(VisibilityShell<?> vs : selectedValuesList){
			vs.setFavorOld(favorOld.isSelected());
		}
	}

}
