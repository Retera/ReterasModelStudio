package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.ui.gui.modeledit.renderers.VisListCellRenderer;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class VisibilityPanel extends JPanel {
	protected SearchListPanel<VisibilityShell<?>> sourceList;
	protected TriCheckBox favorOld;
	protected VisibilityShell<?> selectedVisShell;
	protected ModelHolderThing mht;
	protected VisListCellRenderer visListCellRenderer;

	protected JLabel title;
	protected JLabel typeTitle;

	protected VisibilityPanel() {
		// for use in multi pane
	}

	public VisibilityPanel(ModelHolderThing mht) {
		this.mht = mht;
		setLayout(new MigLayout("fill, gap 0", "[]", "[][][][grow][]"));
		title = new JLabel("Select a source");
		title.setFont(new Font("Arial", Font.BOLD, 18));
		title.setMaximumSize(new Dimension(500, 500));
		add(title, "align center, wrap");

		add(new JLabel("Type:"), "align center, split 2");
		typeTitle = new JLabel("Object type for VisShell");
		add(typeTitle, "wrap");

		favorOld = new TriCheckBox("Favor component's original visibility when combining");
		favorOld.addActionListener(e -> setFavorOld());
		add(favorOld, "left, wrap");

		visListCellRenderer = new VisListCellRenderer();


		sourceList = new SearchListPanel<>("Visibility Source", this::idObjectShellNameFilter)
				.setSelectionConsumer(this::onSourceSelected)
				.setRenderer(visListCellRenderer)
				.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		add(sourceList, "growx, growy, wrap");
	}

	public void setSource(VisibilityShell<?> sourceShell) {
		this.selectedVisShell = sourceShell;
		typeTitle.setText(sourceShell.getSource().getClass().getSimpleName());

		updateSrcList(sourceShell.isFromDonating());
		visListCellRenderer.setSelectedVis(sourceShell);

		title.setText(sourceShell.toString());
		favorOld.setSelected(selectedVisShell.isFavorOld());
	}

	private void setFavorOld(){
		selectedVisShell.setFavorOld(favorOld.isSelected());
	}

	private void updateSrcList(boolean fromDonating) {
		sourceList.clearAndReset();

		List<VisibilityShell<?>> othersBoneShells = fromDonating ? mht.recModVisibilityShells : mht.donModVisibilityShells;
		List<VisibilityShell<?>> selfBoneShells = fromDonating ? mht.donModVisibilityShells : mht.recModVisibilityShells;

		sourceList.addAll(othersBoneShells);
		sourceList.addAll(selfBoneShells);
		sourceList.remove(selectedVisShell);

		if(selectedVisShell.getVisSource() != null){
			sourceList.scrollToReveal(selectedVisShell.getVisSource());
		}

		sourceList.repaint();
	}


	private void onSourceSelected(VisibilityShell<?> bs){
		if(selectedVisShell != null && bs != null){
			if (selectedVisShell.getVisSource() == bs) {
				selectedVisShell.setVisSource(null);
			} else {
				selectedVisShell.setVisSource(bs);
			}
		}
	}
	protected boolean idObjectShellNameFilter(VisibilityShell<?> boneShell, String filterText) {
//		return boneShell.getName().toLowerCase().contains(filterText.toLowerCase());
		return boneShell.toString().matches("(?i).*" + filterText + ".*");
	}
}
