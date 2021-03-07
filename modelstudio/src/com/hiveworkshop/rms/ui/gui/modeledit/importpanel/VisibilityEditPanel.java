package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.util.IterableListModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
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
//	public ArrayList<Object> recModVisSourcesOld = new ArrayList<>();
//	public ArrayList<Object> donModVisSourcesNew = new ArrayList<>();

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

		final VisShellBoxCellRenderer visRenderer = new VisShellBoxCellRenderer();
		singleVisPanel = new VisibilityPanel(mht, visRenderer, recModVisSourcesOld, donModVisSourcesNew);
		visPanelCards.add(singleVisPanel, "single");

		for (VisibilityShell vs : allVisShells) {
			VisibilityPanel vp = new VisibilityPanel(mht, visRenderer, recModVisSourcesOld, donModVisSourcesNew);
			vp.setSource(vs);

			mht.allVisShellPanes.add(vp);

//			visPanelCards.add(vp, vp.title.getText());
		}

		multiVisPanel = new MultiVisibilityPanel(mht, recModVisSourcesOld, donModVisSourcesNew, visRenderer);
//		multiVisPanel = new MultiVisibilityPanel(mht, new DefaultComboBoxModel<>(recModVisSourcesOld.toArray()), new DefaultComboBoxModel<>(donModVisSourcesNew.toArray()), visRenderer);
		visPanelCards.add(blankPane, "blank");
		visPanelCards.add(multiVisPanel, "multiple");
		mht.visTabs.setModel(mht.futureVisComponents);
		mht.visTabs.setCellRenderer(new VisPaneListCellRenderer(mht.receivingModel));
		mht.visTabs.addListSelectionListener(e -> visTabsValueChanged(mht));
		mht.visTabs.setSelectedIndex(0);
		visPanelCards.setBorder(BorderFactory.createLineBorder(Color.blue.darker()));

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(mht.visTabs), visPanelCards);

		add(splitPane, "wrap, growx, growy, spany");
	}

	private JPanel getTopPanel() {
		JPanel topPanel = new JPanel(new MigLayout("gap 0", "", "[]8[]8[]"));

		JButton allInvisButton = createButton("All Invisible in Exotic Anims", e -> allVisButton(mht.allVisShellPanes, mht.receivingModel, VisibilityPanel.NOTVISIBLE), "Forces everything to be always invisibile in animations other than their own original animations.");
		topPanel.add(allInvisButton, "align center, wrap");

		JButton allVisButton = createButton("All Visible in Exotic Anims", e -> allVisButton(mht.allVisShellPanes, mht.receivingModel, VisibilityPanel.VISIBLE), "Forces everything to be always visibile in animations other than their own original animations.");
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

	private void visTabsValueChanged(ModelHolderThing mht) {
		List<VisibilityPanel> selectedValuesList = mht.visTabs.getSelectedValuesList();
		if (selectedValuesList.size() < 1) {
			visCardLayout.show(visPanelCards, "blank");
		} else if (selectedValuesList.size() == 1) {
//			visCardLayout.show(visPanelCards, mht.visTabs.getSelectedValue().title.getText());
			visCardLayout.show(visPanelCards, "single");
			singleVisPanel.setSource(mht.visTabs.getSelectedValue().sourceShell);
		} else {
			visCardLayout.show(visPanelCards, "multiple");

			boolean dif = false;
			boolean selectedt = selectedValuesList.get(0).favorOld.isSelected();

			boolean difBoxOld = false;
			boolean difBoxNew = false;

			int tempIndexOld = selectedValuesList.get(0).oldSourcesBox.getSelectedIndex();
			int tempIndexNew = selectedValuesList.get(0).newSourcesBox.getSelectedIndex();

			for (VisibilityPanel vp : selectedValuesList) {
				if (selectedt != vp.favorOld.isSelected()) {
					dif = true;
					break;
				}

				if (tempIndexOld != vp.oldSourcesBox.getSelectedIndex()) {
					difBoxOld = true;
				}

				if (tempIndexNew != vp.newSourcesBox.getSelectedIndex()) {
					difBoxNew = true;
				}
			}
			if (!dif) {
				multiVisPanel.favorOld.setSelected(selectedt);
			}
			if (difBoxOld) {
				multiVisPanel.setMultipleOld();
			} else {
				multiVisPanel.oldSourcesBox.setSelectedIndex(tempIndexOld);
			}
			if (difBoxNew) {
				multiVisPanel.setMultipleNew();
			} else {
				multiVisPanel.newSourcesBox.setSelectedIndex(tempIndexNew);
			}
		}
	}

	public void initVisibilityList(ModelHolderThing mht) {

		final List<Named> tempList = new ArrayList<>();
		makeUniqueVisShells(mht.receivingModel, tempList);

		makeUniqueVisShells(mht.donatingModel, tempList);

		System.out.println("allVisShells:");
		for (final VisibilityShell vs : allVisShells) {
			System.out.println(vs.source.getName());
		}

		System.out.println("new/old:");
//		for (final VisibilitySource visSource : mht.receivingModel.getAllVisibilitySources()) {
		for (final VisibilitySource visSource : mht.receivingModel.getAllVis()) {
			if (visSource.getClass() != GeosetAnim.class) {
				recModVisSourcesOld.addElement(shellFromObject(allVisShells, visSource));
				System.out.println(shellFromObject(allVisShells, visSource).source.getName());
			} else {
				recModVisSourcesOld.addElement(shellFromObject(allVisShells, ((GeosetAnim) visSource).getGeoset()));
				System.out.println(shellFromObject(allVisShells, ((GeosetAnim) visSource).getGeoset()).source.getName());
			}
		}
		VisibilityShell notVis = new VisibilityShell(false);
		VisibilityShell vis = new VisibilityShell(true);
		recModVisSourcesOld.addElement(notVis);
		recModVisSourcesOld.addElement(vis);
//		recModVisSourcesOld.add(VisibilityPanel.NOTVISIBLE);
//		recModVisSourcesOld.add(VisibilityPanel.VISIBLE);

//		for (final VisibilitySource visSource : mht.donatingModel.getAllVisibilitySources()) {
		for (final VisibilitySource visSource : mht.donatingModel.getAllVis()) {
			if (visSource.getClass() != GeosetAnim.class) {
				donModVisSourcesNew.addElement(shellFromObject(allVisShells, visSource));
			} else {
				donModVisSourcesNew.addElement(shellFromObject(allVisShells, ((GeosetAnim) visSource).getGeoset()));
			}
		}
		donModVisSourcesNew.addElement(notVis);
		donModVisSourcesNew.addElement(vis);
//		donModVisSourcesNew.add(VisibilityPanel.NOTVISIBLE);
//		donModVisSourcesNew.add(VisibilityPanel.VISIBLE);

		mht.futureVisComponents = new IterableListModel<>();
	}

	public void makeUniqueVisShells(EditableModel model, List<Named> tempList) {
		for (final Material mat : model.getMaterials()) {
			for (final Layer x : mat.getLayers()) {
				final VisibilityShell vs = new VisibilityShell(x, model);
				if (!tempList.contains(x)) {
					tempList.add(x);
					allVisShells.add(vs);
				}
			}
		}
		for (final Geoset x : model.getGeosets()) {
			final VisibilityShell vs = new VisibilityShell(x, model);
			if (!tempList.contains(x)) {
				tempList.add(x);
				allVisShells.add(vs);
			}
		}
		createAndAddIdObjectVisShell(model, tempList, model.getLights());
		createAndAddIdObjectVisShell(model, tempList, model.getAttachments());
		createAndAddIdObjectVisShell(model, tempList, model.getParticleEmitters());
		createAndAddIdObjectVisShell(model, tempList, model.getParticleEmitter2s());
		createAndAddIdObjectVisShell(model, tempList, model.getRibbonEmitters());
		createAndAddIdObjectVisShell(model, tempList, model.getPopcornEmitters());
	}

	public void createAndAddIdObjectVisShell(EditableModel model, List<Named> tempList, List<? extends IdObject> idObjects) {
		for (final IdObject x : idObjects) {
			final VisibilityShell vs = new VisibilityShell(x, model);
			if (!tempList.contains(x)) {
				tempList.add(x);
				allVisShells.add(vs);
			}
		}
	}

	public VisibilityShell shellFromObject(ArrayList<VisibilityShell> allVisShells, VisibilitySource o) {
		for (VisibilityShell v : allVisShells) {
			if (v.source == o) {
				return v;
			}
		}
		return null;
	}

	public void allVisButton(ArrayList<VisibilityPanel> allVisShellPanes, EditableModel receivingModel, String visible) {
		for (VisibilityPanel vPanel : allVisShellPanes) {
			if (vPanel.sourceShell.model == receivingModel) {
				vPanel.newSourcesBox.setSelectedItem(visible);
			} else {
				vPanel.oldSourcesBox.setSelectedItem(visible);
			}
		}
	}
}
