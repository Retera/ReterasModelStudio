package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.*;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class VisibilityEditPanel extends JPanel {

	private CardLayout visCardLayout = new CardLayout();
	private JPanel visPanelCards = new JPanel(visCardLayout);
	private MultiVisibilityPanel multiVisPanel;
	private ModelHolderThing mht;


	public List<VisibilityShell> recModVisSourcesOld = new ArrayList<>();
	public List<VisibilityShell> donModVisSourcesNew = new ArrayList<>();

	public List<VisibilityShell> allVisShells = new ArrayList<>();

	private VisibilityPanel singleVisPanel;

	public VisibilityEditPanel(ModelHolderThing mht) {
		setLayout(new MigLayout("gap 0, fill", "[grow]", "[][grow]"));
		this.mht = mht;

		mht.visTabs.setModel(mht.futureVisComponents);
		mht.visTabs.setCellRenderer(new VisPaneListCellRenderer(mht.receivingModel));
		mht.visTabs.addListSelectionListener(e -> visTabsValueChanged(mht, e));

		add(getTopPanel(), "spanx, align center, wrap");

		initVisibilityList(mht);
//		mht.initVisibilityList();
		mht.visibilityList();
		mht.donModVisSourcesNew = donModVisSourcesNew;
		mht.recModVisSourcesOld = recModVisSourcesOld;


		visPanelCards.add(new JPanel(), "blank");

		final VisShellBoxCellRenderer visRenderer = new VisShellBoxCellRenderer();
		singleVisPanel = new VisibilityPanel(mht, visRenderer, recModVisSourcesOld, donModVisSourcesNew);
		visPanelCards.add(singleVisPanel, "single");


		multiVisPanel = new MultiVisibilityPanel(mht, recModVisSourcesOld, donModVisSourcesNew, visRenderer);
		visPanelCards.add(multiVisPanel, "multiple");
		visPanelCards.setBorder(BorderFactory.createLineBorder(Color.blue.darker()));

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(mht.visTabs), visPanelCards);
		splitPane.getLeftComponent().setMinimumSize(new Dimension(100, 300));

		add(splitPane, "wrap, growx, growy, spany");
	}

	private JPanel getTopPanel() {
		JPanel topPanel = new JPanel(new MigLayout("gap 0", "", "[]8[]8[]"));

		JButton allInvisButton = createButton("All Invisible in Exotic Anims", e -> allVisButton(mht.allVisShellPanes, mht.receivingModel, mht.alwaysVisible), "Forces everything to be always invisibile in animations other than their own original animations.");
		topPanel.add(allInvisButton, "align center, wrap");

		JButton allVisButton = createButton("All Visible in Exotic Anims", e -> allVisButton(mht.allVisShellPanes, mht.receivingModel, mht.neverVisible), "Forces everything to be always visibile in animations other than their own original animations.");
		topPanel.add(allVisButton, "align center, wrap");

		JButton selSimButton = createButton("Select Similar Options", e -> mht.selSimButton(), "Similar components will be selected as visibility sources in exotic animations.");
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
			List<VisibilityShell> selectedValuesList = mht.visTabs.getSelectedValuesList();
			if (selectedValuesList.size() < 1) {
				visCardLayout.show(visPanelCards, "blank");
			} else if (selectedValuesList.size() == 1) {
				visCardLayout.show(visPanelCards, "single");
				singleVisPanel.setSource(mht.visTabs.getSelectedValue());
			} else {
				multiVisPanel.updateMultiVisPanel();
				visCardLayout.show(visPanelCards, "multiple");
			}
		}
	}

	public void initVisibilityList(ModelHolderThing mht) {

		final List<Named> tempList = new ArrayList<>();

		fetchUniqueVisShells(mht.receivingModel, tempList);
		fetchUniqueVisShells(mht.donatingModel, tempList);

		for (final VisibilitySource visSource : mht.receivingModel.getAllVis()) {
			if (visSource.getClass() != GeosetAnim.class) {
				recModVisSourcesOld.add(visShellFromObject(visSource));
			} else {
				recModVisSourcesOld.add(visShellFromObject(((GeosetAnim) visSource).getGeoset()));
			}
		}
		recModVisSourcesOld.add(mht.neverVisible);
		recModVisSourcesOld.add(mht.alwaysVisible);

		for (final VisibilitySource visSource : mht.donatingModel.getAllVis()) {
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
		for (final Material mat : model.getMaterials()) {
			for (final Layer x : mat.getLayers()) {
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

	public void allVisButton(ArrayList<VisibilityShell> allVisShellPanes, EditableModel model, VisibilityShell visibilityShell) {
		for (VisibilityShell shell : allVisShellPanes) {
			if (shell.getModel() == model) {
				shell.setNewVisSource(visibilityShell);
			} else {
				shell.setOldVisSource(visibilityShell);
			}
		}
	}
}
