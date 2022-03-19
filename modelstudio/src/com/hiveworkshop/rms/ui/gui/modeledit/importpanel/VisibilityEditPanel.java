package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.VisPaneListCellRenderer;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.VisShellBoxCellRenderer;
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
	private final MultiVisibilityPanel multiVisPanel;
	private final ModelHolderThing mht;


	public List<VisibilityShell> recModVisSourcesOld = new ArrayList<>();
	public List<VisibilityShell> donModVisSourcesNew = new ArrayList<>();

	public List<VisibilityShell> allVisShells = new ArrayList<>();

	private final VisibilityPanel singleVisPanel;

	public VisibilityEditPanel(ModelHolderThing mht) {
		setLayout(new MigLayout("gap 0, fill", "[grow]", "[][grow]"));
		this.mht = mht;

		mht.visibilityShellJList.setModel(mht.futureVisComponents);
		mht.visibilityShellJList.setCellRenderer(new VisPaneListCellRenderer());
		mht.visibilityShellJList.addListSelectionListener(e -> visTabsValueChanged(mht, e));

		add(getTopPanel(), "spanx, align center, wrap");

		initVisibilityList(mht);
		mht.visibilityList();
		mht.donModVisSourcesNew = donModVisSourcesNew;
		mht.recModVisSourcesOld = recModVisSourcesOld;

		VisShellBoxCellRenderer visRenderer = new VisShellBoxCellRenderer();
		singleVisPanel = new VisibilityPanel(mht, visRenderer, recModVisSourcesOld, donModVisSourcesNew);
		multiVisPanel = new MultiVisibilityPanel(mht, recModVisSourcesOld, donModVisSourcesNew, visRenderer);

		panelCards.add(new JPanel(), "blank");
		panelCards.add(singleVisPanel, "single");
		panelCards.add(multiVisPanel, "multiple");
		panelCards.setBorder(BorderFactory.createLineBorder(Color.blue.darker()));

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(mht.visibilityShellJList), panelCards);
		splitPane.getLeftComponent().setMinimumSize(new Dimension(100, 300));
		add(splitPane, "wrap, growx, growy, spany");
	}

	private JPanel getTopPanel() {
		JPanel topPanel = new JPanel(new MigLayout("gap 0", "", "[]8[]8[]"));

//		JButton allInvisButton = createButton("All Invisible in Exotic Anims", e -> allVisButton(mht.allVisShells, mht.receivingModel, mht.alwaysVisible), "Forces everything to be always invisibile in animations other than their own original animations.");
		JButton allInvisButton = createButton("All Invisible in Exotic Anims", e -> allVisButton(mht.allVisShells, mht.neverVisible), "Forces everything to be always invisibile in animations other than their own original animations.");
		topPanel.add(allInvisButton, "align center, wrap");

//		JButton allVisButton = createButton("All Visible in Exotic Anims", e -> allVisButton(mht.allVisShells, mht.receivingModel, mht.neverVisible), "Forces everything to be always visibile in animations other than their own original animations.");
		JButton allVisButton = createButton("All Visible in Exotic Anims", e -> allVisButton(mht.allVisShells, mht.alwaysVisible), "Forces everything to be always visibile in animations other than their own original animations.");
		topPanel.add(allVisButton, "align center, wrap");

		JButton selSimButton = createButton("Select Similar Options", e -> mht.selectSimilarVisSources(), "Similar components will be selected as visibility sources in exotic animations.");
		topPanel.add(selSimButton, "align center, wrap");
		return topPanel;
	}

	public JButton createButton(String text, ActionListener actionListener, String toolTipText) {
		JButton selSimButton = new JButton(text);
		selSimButton.addActionListener(actionListener);
		selSimButton.setToolTipText(toolTipText);
		return selSimButton;
	}

	private void visTabsValueChanged(ModelHolderThing mht, ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) {
			List<VisibilityShell> selectedValuesList = mht.visibilityShellJList.getSelectedValuesList();
			if (selectedValuesList.size() < 1) {
				cardLayout.show(panelCards, "blank");
			} else if (selectedValuesList.size() == 1) {
				cardLayout.show(panelCards, "single");
				singleVisPanel.setSource(mht.visibilityShellJList.getSelectedValue());
			} else {
				multiVisPanel.updateMultiVisPanel(selectedValuesList);
				cardLayout.show(panelCards, "multiple");
			}
		}
	}

	public void initVisibilityList(ModelHolderThing mht) {

		List<Named> tempList = new ArrayList<>();

		fetchUniqueVisShells(mht.receivingModel, tempList);
		fetchUniqueVisShells(mht.donatingModel, tempList);

		for (VisibilitySource visSource : ModelUtils.getAllVis(mht.receivingModel)) {
			if (visSource.getClass() != GeosetAnim.class) {
				recModVisSourcesOld.add(visShellFromObject(visSource));
			} else {
				recModVisSourcesOld.add(visShellFromObject(((GeosetAnim) visSource).getGeoset()));
			}
		}
		recModVisSourcesOld.add(mht.neverVisible);
		recModVisSourcesOld.add(mht.alwaysVisible);

		for (VisibilitySource visSource : ModelUtils.getAllVis(mht.donatingModel)) {
			if (visSource.getClass() != GeosetAnim.class) {
				donModVisSourcesNew.add(visShellFromObject(visSource));
			} else {
				donModVisSourcesNew.add(visShellFromObject(((GeosetAnim) visSource).getGeoset()));
			}
		}
		donModVisSourcesNew.add(mht.neverVisible);
		donModVisSourcesNew.add(mht.alwaysVisible);
	}

	public void fetchUniqueVisShells(EditableModel model, List<Named> tempList) {
		for (Material mat : model.getMaterials()) {
			for (Layer x : mat.getLayers()) {
				VisibilityShell vs = visShellFromObject(x);
				if (!tempList.contains(x)) {
					tempList.add(x);
					allVisShells.add(vs);
				}
			}
		}
		for (final Geoset x : model.getGeosets()) {
			VisibilityShell vs = visShellFromObject(x);
			if (!tempList.contains(x)) {
				tempList.add(x);
				allVisShells.add(vs);
			}
		}
		fetchAndAddVisComp(tempList, model.getLights());
		fetchAndAddVisComp(tempList, model.getAttachments());
		fetchAndAddVisComp(tempList, model.getParticleEmitters());
		fetchAndAddVisComp(tempList, model.getParticleEmitter2s());
		fetchAndAddVisComp(tempList, model.getRibbonEmitters());
		fetchAndAddVisComp(tempList, model.getPopcornEmitters());
	}

	public void fetchAndAddVisComp(List<Named> tempList, List<? extends IdObject> idObjects) {
		for (final IdObject x : idObjects) {
			VisibilityShell vs = visShellFromObject(x);
			if (!tempList.contains(x)) {
				tempList.add(x);
				allVisShells.add(vs);
			}
		}
	}

	public VisibilityShell visShellFromObject(VisibilitySource vs) {
		return mht.allVisShellBiMap.get(vs);
	}

	// ToDo Fix this to check through all animShells
	//  this might be more broken than
	public void allVisButton(ArrayList<VisibilityShell> allVisShellPanes, VisibilityShell visibilityShell) {
		for (VisibilityShell shell : allVisShellPanes) {
			if (shell.isFromDonating()) {
				shell.setRecModAnimsVisSource(visibilityShell);
			} else {
				shell.setDonModAnimsVisSource(visibilityShell);
			}
		}
	}
}
