package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.shells.VisibilityShell;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.VisPaneListCellRenderer;
import com.hiveworkshop.rms.ui.util.TwiList;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class VisibilityEditPanel extends JPanel {

	private final CardLayout cardLayout = new CardLayout();
	private final JPanel panelCards = new JPanel(cardLayout);
	private final VisibilityMultiPanel multiVisPanel;
	private final ModelHolderThing mht;

	private final VisibilityPanel singleVisPanel;

	private final List<VisibilityShell<?>> visibilityShells = new ArrayList<>();
	private final TwiList<VisibilityShell<?>> visibilityShellJList = new TwiList<>(visibilityShells);

	public VisibilityEditPanel(ModelHolderThing mht) {
		setLayout(new MigLayout("gap 0, fill", "[grow]", "[][grow]"));
		this.mht = mht;

		visibilityShells.addAll(mht.allVisShells);
		visibilityShellJList.setCellRenderer(new VisPaneListCellRenderer());
		visibilityShellJList.addListSelectionListener(e -> visTabsValueChanged(mht, e));

		add(getTopPanel(), "spanx, align center, wrap");

//		VisShellBoxCellRenderer visRenderer = new VisShellBoxCellRenderer();
		singleVisPanel = new VisibilityPanel(mht);
		multiVisPanel = new VisibilityMultiPanel(mht);

		panelCards.add(new JPanel(), "blank");
		panelCards.add(singleVisPanel, "single");
		panelCards.add(multiVisPanel, "multiple");
		panelCards.setBorder(BorderFactory.createLineBorder(Color.blue.darker()));

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(visibilityShellJList), panelCards);
		splitPane.getLeftComponent().setMinimumSize(new Dimension(100, 300));
		add(splitPane, "wrap, growx, growy, spany");
	}

	private JPanel getTopPanel() {
		JPanel topPanel = new JPanel(new MigLayout("gap 0", "[][]", "[]"));
		topPanel.add(getSetVisPanel(false));
		topPanel.add(getSetVisPanel(true));
		return topPanel;
	}

	private JPanel getSetVisPanel(boolean donMod){
		JPanel panel = new JPanel(new MigLayout("gap 0, ins 0"));

		String modelName = donMod ? mht.donatingModel.getName() : mht.receivingModel.getName();
		panel.setBorder(BorderFactory.createTitledBorder(modelName));

		List<VisibilityShell<?>> allVisShells = donMod ? mht.donModVisibilityShells : mht.recModVisibilityShells;
		VisibilityShell<?> neverVisible = donMod ? mht.recNeverVis : mht.donNeverVis;
		VisibilityShell<?> alwaysVisible = donMod ? mht.recAlwaysVis : mht.donAlwaysVis;

		String otherModName = donMod ? "[#]" : "[&]";

		JButton allInvisButton = createButton("All Invisible in " + otherModName, e -> allAsVisSource(allVisShells, neverVisible), "Forces everything to be always invisibile in animations other than their own original animations.");
		JButton allVisButton = createButton("All Visible in " + otherModName, e -> allAsVisSource(allVisShells, alwaysVisible), "Forces everything to be always visibile in animations other than their own original animations.");
		JButton selSimButton = createButton("Select Similar", e -> selectSimilarVisSources(donMod), "Similar components will be selected as visibility sources in exotic animations.");

		panel.add(allInvisButton);
		panel.add(allVisButton);
		panel.add(selSimButton);

		return panel;
	}

	public JButton createButton(String text, ActionListener actionListener, String toolTipText) {
		JButton selSimButton = new JButton(text);
		selSimButton.addActionListener(actionListener);
		selSimButton.setToolTipText(toolTipText);
		return selSimButton;
	}

	private void visTabsValueChanged(ModelHolderThing mht, ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) {
			List<VisibilityShell<?>> selectedValuesList = visibilityShellJList.getSelectedValuesList();
			if (selectedValuesList.size() < 1) {
				cardLayout.show(panelCards, "blank");
			} else if (selectedValuesList.size() == 1) {
				cardLayout.show(panelCards, "single");
				singleVisPanel.setSource(visibilityShellJList.getSelectedValue());
			} else {
				multiVisPanel.updateMultiVisPanel(selectedValuesList);
				cardLayout.show(panelCards, "multiple");
			}
		}
	}

	public void allAsVisSource(List<VisibilityShell<?>> allVisShellPanes, VisibilityShell<?> visibilityShell) {
		for (VisibilityShell<?> shell : allVisShellPanes) {
			shell.setVisSource(visibilityShell);
		}
		repaint();
	}


	public void selectSimilarVisSources(boolean donMod) {
		// this should maybe look for best match and check source type...

		List<VisibilityShell<?>> visShells = donMod ? mht.donModVisibilityShells : mht.recModVisibilityShells;
		List<VisibilityShell<?>> otherVisSources = donMod ? mht.recModVisibilityShells : mht.donModVisibilityShells;
		for (final VisibilityShell<?> visibilityShell : visShells) {
			for (VisibilityShell<?> vs : otherVisSources) {
				if (visibilityShell.getNameSource().getName().equals(vs.getNameSource().getName())) {
					visibilityShell.setVisSource(vs);
				}
			}
		}
		repaint();
	}

}
