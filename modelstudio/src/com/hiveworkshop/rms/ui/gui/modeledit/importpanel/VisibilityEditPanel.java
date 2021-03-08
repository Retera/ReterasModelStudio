package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.util.IterableListModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class VisibilityEditPanel extends JPanel {

	public CardLayout visCardLayout = new CardLayout();
	public JPanel visPanelCards = new JPanel(visCardLayout);
	public MultiVisibilityPanel multiVisPanel;
	public JPanel blankPane = new JPanel();
	ModelHolderThing mht;


	public IterableListModel<VisibilityShell> recModVisSourcesOld = new IterableListModel<>();
	public IterableListModel<VisibilityShell> donModVisSourcesNew = new IterableListModel<>();

	public ArrayList<VisibilityShell> allVisShells = new ArrayList<>();

	private VisibilityPanel singleVisPanel;

	public VisibilityEditPanel(ModelHolderThing mht) {
		setLayout(new MigLayout("gap 0, fill", "", "[][grow]"));
		this.mht = mht;

		add(getTopPanel(), "spanx, align center, wrap");

		initVisibilityList(mht);
//		mht.initVisibilityList();
		mht.visibilityList();
		mht.donModVisSourcesNew = donModVisSourcesNew;
		mht.recModVisSourcesOld = recModVisSourcesOld;

		final VisShellBoxCellRenderer visRenderer = new VisShellBoxCellRenderer();
		singleVisPanel = new VisibilityPanel(mht, visRenderer, recModVisSourcesOld, donModVisSourcesNew);
		visPanelCards.add(singleVisPanel, "single");


		multiVisPanel = new MultiVisibilityPanel(mht, recModVisSourcesOld, donModVisSourcesNew, visRenderer);
		visPanelCards.add(blankPane, "blank");
		visPanelCards.add(multiVisPanel, "multiple");
		mht.visTabs.setModel(mht.futureVisComponents);
		mht.visTabs.setCellRenderer(new VisPaneListCellRenderer(mht.receivingModel));
		mht.visTabs.addListSelectionListener(e -> visTabsValueChanged(mht, e));
		mht.visTabs.setSelectedIndex(0);
		visPanelCards.setBorder(BorderFactory.createLineBorder(Color.blue.darker()));

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(mht.visTabs), visPanelCards);

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
				visCardLayout.show(visPanelCards, "multiple");

				boolean dif = false;
				boolean selectedt = selectedValuesList.get(0).isFavorOld();

				boolean difBoxOld = false;
				boolean difBoxNew = false;

				VisibilityShell tempIndexOld = selectedValuesList.get(0).getOldVisSource();
				VisibilityShell tempIndexNew = selectedValuesList.get(0).getNewVisSource();

				for (VisibilityShell vs : selectedValuesList) {
					if (selectedt != vs.isFavorOld()) {
						dif = true;
						break;
					}

					if (tempIndexOld != vs.getOldVisSource()) {
						difBoxOld = true;
					}

					if (tempIndexNew != vs.getNewVisSource()) {
						difBoxNew = true;
					}
				}
				if (!dif) {
					multiVisPanel.favorOld.setSelected(selectedt);
				}
				if (difBoxOld) {
					multiVisPanel.setMultipleOld();
				} else {
					multiVisPanel.receivingModelSourcesBox.setSelectedItem(tempIndexOld);
				}
				if (difBoxNew) {
					multiVisPanel.setMultipleNew();
				} else {
					multiVisPanel.donatingModelSourcesBox.setSelectedItem(tempIndexNew);
				}
			}
		}
	}

	public void initVisibilityList(ModelHolderThing mht) {

		final List<Named> tempList = new ArrayList<>();
		makeUniqueVisShells(mht.receivingModel, tempList);

		makeUniqueVisShells(mht.donatingModel, tempList);

		System.out.println("allVisShells:");
		for (final VisibilityShell vs : allVisShells) {
			System.out.println(vs);
		}

		System.out.println("new/old:");
		for (final VisibilitySource visSource : mht.receivingModel.getAllVis()) {
			if (visSource.getClass() != GeosetAnim.class) {
				recModVisSourcesOld.addElement(visShellFromObject(visSource));
				System.out.println(visShellFromObject(visSource));
			} else {
				recModVisSourcesOld.addElement(visShellFromObject(((GeosetAnim) visSource).getGeoset()));
				System.out.println(visShellFromObject(((GeosetAnim) visSource).getGeoset()));
			}
		}
		recModVisSourcesOld.addElement(mht.neverVisible);
		recModVisSourcesOld.addElement(mht.alwaysVisible);

		for (final VisibilitySource visSource : mht.donatingModel.getAllVis()) {
			if (visSource.getClass() != GeosetAnim.class) {
				donModVisSourcesNew.addElement(visShellFromObject(visSource));
			} else {
				donModVisSourcesNew.addElement(visShellFromObject(((GeosetAnim) visSource).getGeoset()));
			}
		}
		donModVisSourcesNew.addElement(mht.neverVisible);
		donModVisSourcesNew.addElement(mht.alwaysVisible);
	}

	public void makeUniqueVisShells(EditableModel model, List<Named> tempList) {
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
		fetchAndAddVisComp(model, tempList, model.getLights());
		fetchAndAddVisComp(model, tempList, model.getAttachments());
		fetchAndAddVisComp(model, tempList, model.getParticleEmitters());
		fetchAndAddVisComp(model, tempList, model.getParticleEmitter2s());
		fetchAndAddVisComp(model, tempList, model.getRibbonEmitters());
		fetchAndAddVisComp(model, tempList, model.getPopcornEmitters());
	}

	public void fetchAndAddVisComp(EditableModel model, List<Named> tempList, List<? extends IdObject> idObjects) {
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
