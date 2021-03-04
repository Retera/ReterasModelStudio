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

	public VisibilityEditPanel(ModelHolderThing mht) {
		setLayout(new MigLayout("gap 0, fill", "", "[][grow]"));
		this.mht = mht;

		add(getTopPanel(), "spanx, align center, wrap");

		initVisibilityList(mht);
		mht.visibilityList();

		final VisShellBoxCellRenderer visRenderer = new VisShellBoxCellRenderer();
		for (final VisibilityShell vs : mht.allVisShells) {
			final VisibilityPanel vp = new VisibilityPanel(mht, vs, new DefaultComboBoxModel<>(mht.visSourcesOld.toArray()), new DefaultComboBoxModel<>(mht.visSourcesNew.toArray()), visRenderer);

			mht.allVisShellPanes.add(vp);

			visPanelCards.add(vp, vp.title.getText());
		}

		multiVisPanel = new MultiVisibilityPanel(mht, new DefaultComboBoxModel<>(mht.visSourcesOld.toArray()), new DefaultComboBoxModel<>(mht.visSourcesNew.toArray()), visRenderer);
		visPanelCards.add(blankPane, "blank");
		visPanelCards.add(multiVisPanel, "multiple");
		mht.visTabs.setModel(mht.visComponents);
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
		if (mht.visTabs.getSelectedValuesList().toArray().length < 1) {
			visCardLayout.show(visPanelCards, "blank");
		} else if (mht.visTabs.getSelectedValuesList().toArray().length == 1) {
			visCardLayout.show(visPanelCards, mht.visTabs.getSelectedValue().title.getText());
		} else if (mht.visTabs.getSelectedValuesList().toArray().length > 1) {
			visCardLayout.show(visPanelCards, "multiple");
			final Object[] selected = mht.visTabs.getSelectedValuesList().toArray();

			boolean dif = false;
			boolean set = false;
			boolean selectedt = false;

			boolean difBoxOld = false;
			boolean difBoxNew = false;
			int tempIndexOld = -99;
			int tempIndexNew = -99;

			for (int i = 0; (i < selected.length) && !dif; i++) {
				final VisibilityPanel temp = (VisibilityPanel) selected[i];
				if (!set) {
					set = true;
					selectedt = temp.favorOld.isSelected();
				} else if (selectedt != temp.favorOld.isSelected()) {
					dif = true;
				}

				if (tempIndexOld == -99) {
					tempIndexOld = temp.oldSourcesBox.getSelectedIndex();
				}
				if (tempIndexOld != temp.oldSourcesBox.getSelectedIndex()) {
					difBoxOld = true;
				}

				if (tempIndexNew == -99) {
					tempIndexNew = temp.newSourcesBox.getSelectedIndex();
				}
				if (tempIndexNew != temp.newSourcesBox.getSelectedIndex()) {
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

	public void initVisibilityList(ModelHolderThing modelHolderThing) {
		modelHolderThing.visSourcesOld = new ArrayList<>();
		modelHolderThing.visSourcesNew = new ArrayList<>();
		modelHolderThing.allVisShells = new ArrayList<>();
		EditableModel model = modelHolderThing.receivingModel;
		final List tempList = new ArrayList();
		for (final Material mat : model.getMaterials()) {
			for (final Layer lay : mat.getLayers()) {
				final VisibilityShell vs = new VisibilityShell(lay, model);
				if (!tempList.contains(lay)) {
					tempList.add(lay);
					modelHolderThing.allVisShells.add(vs);
				}
			}
		}
		for (final Geoset ga : model.getGeosets()) {
			final VisibilityShell vs = new VisibilityShell(ga, model);
			if (!tempList.contains(ga)) {
				tempList.add(ga);
				modelHolderThing.allVisShells.add(vs);
			}
		}
		for (final Object x : model.getLights()) {
			final VisibilityShell vs = new VisibilityShell((Named) x, model);
			if (!tempList.contains(x)) {
				tempList.add(x);
				modelHolderThing.allVisShells.add(vs);
			}
		}
		for (final Object x : model.getAttachments()) {
			final VisibilityShell vs = new VisibilityShell((Named) x, model);
			if (!tempList.contains(x)) {
				tempList.add(x);
				modelHolderThing.allVisShells.add(vs);
			}
		}
		for (final Object x : model.getParticleEmitters()) {
			final VisibilityShell vs = new VisibilityShell((Named) x, model);
			if (!tempList.contains(x)) {
				tempList.add(x);
				modelHolderThing.allVisShells.add(vs);
			}
		}
		for (final Object x : model.getParticleEmitter2s()) {
			final VisibilityShell vs = new VisibilityShell((Named) x, model);
			if (!tempList.contains(x)) {
				tempList.add(x);
				modelHolderThing.allVisShells.add(vs);
			}
		}
		for (final Object x : model.getRibbonEmitters()) {
			final VisibilityShell vs = new VisibilityShell((Named) x, model);
			if (!tempList.contains(x)) {
				tempList.add(x);
				modelHolderThing.allVisShells.add(vs);
			}
		}
		for (final Object x : model.getPopcornEmitters()) {
			final VisibilityShell vs = new VisibilityShell((Named) x, model);
			if (!tempList.contains(x)) {
				tempList.add(x);
				modelHolderThing.allVisShells.add(vs);
			}
		}
		model = modelHolderThing.donatingModel;
		for (final Material mat : model.getMaterials()) {
			for (final Layer x : mat.getLayers()) {
				final VisibilityShell vs = new VisibilityShell(x, model);
				if (!tempList.contains(x)) {
					tempList.add(x);
					modelHolderThing.allVisShells.add(vs);
				}
			}
		}
		for (final Geoset x : model.getGeosets()) {
			final VisibilityShell vs = new VisibilityShell(x, model);
			if (!tempList.contains(x)) {
				tempList.add(x);
				modelHolderThing.allVisShells.add(vs);
			}
		}
		for (final Object x : model.getLights()) {
			final VisibilityShell vs = new VisibilityShell((Named) x, model);
			if (!tempList.contains(x)) {
				tempList.add(x);
				modelHolderThing.allVisShells.add(vs);
			}
		}
		for (final Object x : model.getAttachments()) {
			final VisibilityShell vs = new VisibilityShell((Named) x, model);
			if (!tempList.contains(x)) {
				tempList.add(x);
				modelHolderThing.allVisShells.add(vs);
			}
		}
		for (final Object x : model.getParticleEmitters()) {
			final VisibilityShell vs = new VisibilityShell((Named) x, model);
			if (!tempList.contains(x)) {
				tempList.add(x);
				modelHolderThing.allVisShells.add(vs);
			}
		}
		for (final Object x : model.getParticleEmitter2s()) {
			final VisibilityShell vs = new VisibilityShell((Named) x, model);
			if (!tempList.contains(x)) {
				tempList.add(x);
				modelHolderThing.allVisShells.add(vs);
			}
		}
		for (final Object x : model.getRibbonEmitters()) {
			final VisibilityShell vs = new VisibilityShell((Named) x, model);
			if (!tempList.contains(x)) {
				tempList.add(x);
				modelHolderThing.allVisShells.add(vs);
			}
		}
		for (final Object x : model.getPopcornEmitters()) {
			final VisibilityShell vs = new VisibilityShell((Named) x, model);
			if (!tempList.contains(x)) {
				tempList.add(x);
				modelHolderThing.allVisShells.add(vs);
			}
		}

		System.out.println("allVisShells:");
		for (final VisibilityShell vs : modelHolderThing.allVisShells) {
			System.out.println(vs.source.getName());
		}

		System.out.println("new/old:");
		for (final Object o : modelHolderThing.receivingModel.getAllVisibilitySources()) {
			if (o.getClass() != GeosetAnim.class) {
				modelHolderThing.visSourcesOld.add(shellFromObject(modelHolderThing.allVisShells, o));
				System.out.println(shellFromObject(modelHolderThing.allVisShells, o).source.getName());
			} else {
				modelHolderThing.visSourcesOld.add(shellFromObject(modelHolderThing.allVisShells, ((GeosetAnim) o).getGeoset()));
				System.out.println(shellFromObject(modelHolderThing.allVisShells, ((GeosetAnim) o).getGeoset()).source.getName());
			}
		}
		modelHolderThing.visSourcesOld.add(VisibilityPanel.NOTVISIBLE);
		modelHolderThing.visSourcesOld.add(VisibilityPanel.VISIBLE);
		for (final Object o : modelHolderThing.donatingModel.getAllVisibilitySources()) {
			if (o.getClass() != GeosetAnim.class) {
				modelHolderThing.visSourcesNew.add(shellFromObject(modelHolderThing.allVisShells, o));
			} else {
				modelHolderThing.visSourcesNew.add(shellFromObject(modelHolderThing.allVisShells, ((GeosetAnim) o).getGeoset()));
			}
		}
		modelHolderThing.visSourcesNew.add(VisibilityPanel.NOTVISIBLE);
		modelHolderThing.visSourcesNew.add(VisibilityPanel.VISIBLE);
		modelHolderThing.visComponents = new IterableListModel<>();
	}

	public VisibilityShell shellFromObject(ArrayList<VisibilityShell> allVisShells, final Object o) {
		for (final VisibilityShell v : allVisShells) {
			if (v.source == o) {
				return v;
			}
		}
		return null;
	}

	public void allVisButton(ArrayList<VisibilityPanel> allVisShellPanes, EditableModel receivingModel, String visible) {
		for (final VisibilityPanel vPanel : allVisShellPanes) {
			if (vPanel.sourceShell.model == receivingModel) {
				vPanel.newSourcesBox.setSelectedItem(visible);
			} else {
				vPanel.oldSourcesBox.setSelectedItem(visible);
			}
		}
	}
}
