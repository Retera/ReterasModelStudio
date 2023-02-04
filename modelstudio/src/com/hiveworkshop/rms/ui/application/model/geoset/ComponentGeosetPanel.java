package com.hiveworkshop.rms.ui.application.model.geoset;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.mesh.ChangeLoDAction;
import com.hiveworkshop.rms.editor.actions.mesh.ChangeLoDNameAction;
import com.hiveworkshop.rms.editor.actions.mesh.DeleteGeosetAction;
import com.hiveworkshop.rms.editor.actions.mesh.RecalculateTangentsAction;
import com.hiveworkshop.rms.editor.actions.model.material.AddMaterialAction;
import com.hiveworkshop.rms.editor.actions.model.material.ChangeMaterialAction;
import com.hiveworkshop.rms.editor.actions.tools.ConvertToMatricesAction;
import com.hiveworkshop.rms.editor.actions.tools.ConvertToSkinBonesAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.ui.application.model.ComponentPanel;
import com.hiveworkshop.rms.ui.application.model.editors.IntEditorJSpinner;
import com.hiveworkshop.rms.ui.application.model.editors.TwiTextField;
import com.hiveworkshop.rms.ui.gui.modeledit.MaterialListRenderer;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.TwiComboBox;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class ComponentGeosetPanel extends ComponentPanel<Geoset> {
	private final JLabel geosetLabel = new JLabel("geoset name");
	private final JLabel trisLabel = new JLabel("0");
	private final JLabel vertLabel = new JLabel("0");
	private JPanel lodPanel;
	private final JPanel hdNamePanel;
	private IntEditorJSpinner lodSpinner;
	private TwiTextField nameTextField;
	private final JButton toggleSdHd;
	private IntEditorJSpinner selectionGroupSpinner;
	private Geoset geoset;

	private final GeosetVisPanel visPanel;
	private final GeosetAnimPanel geosetAnimPanel;

	private TwiComboBox<Material> materialChooser;

	public ComponentGeosetPanel(ModelHandler modelHandler) {
		super(modelHandler);
		setLayout(new MigLayout("hidemode 1", "[][][grow]", "[]"));

		add(geosetLabel, "");
		add(getDeleteButton(e -> removeGeoset()), "spanx, right, wrap");
		hdNamePanel = getHdNamePanel();
		add(hdNamePanel, "wrap, growx, spanx");

		JPanel topPanel = new JPanel(new MigLayout("hidemode 1, ins 0", "[][grow][grow]", "[]"));
		add(topPanel, "wrap");
		topPanel.add(getGeosetInfoPanel(), "growx");

		JPanel materialPanelHolder = getMaterialPanelHolder();
		topPanel.add(materialPanelHolder, "wrap, growx, spanx");


		JButton editUvButton = new JButton("Edit Geoset UVs");

		toggleSdHd = new JButton("Make Geoset HD");
		toggleSdHd.addActionListener(e -> toggleSdHd());
		add(toggleSdHd, "wrap");

		visPanel = new GeosetVisPanel(modelHandler);
		add(visPanel, "wrap, spanx");

		geosetAnimPanel = new GeosetAnimPanel(modelHandler);
		add(geosetAnimPanel, "wrap, spanx");
	}

	private JPanel getMaterialPanelHolder() {
		JPanel materialPanelHolder = new JPanel(new MigLayout("hidemode 1"));
		materialPanelHolder.add(new JLabel("Material:"), "wrap");

		materialChooser = getMaterialChooser();
		materialPanelHolder.add(materialChooser, "wrap");

		JButton cloneMaterial = new JButton("Clone This Material");
		cloneMaterial.addActionListener(e -> cloneMaterial());
		materialPanelHolder.add(cloneMaterial);

		return materialPanelHolder;
	}

	private JPanel getGeosetInfoPanel() {
		JPanel geosetInfoPanel = new JPanel(new MigLayout("fill, hidemode 1", "[][][grow][grow]"));

		lodPanel = getLodPanel();
		geosetInfoPanel.add(lodPanel, "growx, spanx, wrap");

		geosetInfoPanel.add(new JLabel("Triangles: "));
		geosetInfoPanel.add(trisLabel, "wrap");

		geosetInfoPanel.add(new JLabel("Vertices: "));
		geosetInfoPanel.add(vertLabel, "wrap");

		geosetInfoPanel.add(new JLabel("SelectionGroup: "));
		selectionGroupSpinner = new IntEditorJSpinner(0, 0, 10000, this::setSelectionGroup);
		geosetInfoPanel.add(selectionGroupSpinner, "wrap");
		return geosetInfoPanel;
	}

	private TwiComboBox<Material> getMaterialChooser() {
		TwiComboBox<Material> materialComboBox = new TwiComboBox<>(modelHandler.getModel().getMaterials(), new Material());
		materialComboBox.setRenderer(new MaterialListRenderer(modelHandler.getModel()));
		materialComboBox.addOnSelectItemListener(this::changeTexture);
		return materialComboBox;
	}

	private void changeTexture(Material material) {
		if (material != null && material != geoset.getMaterial()) {
			undoManager.pushAction(new ChangeMaterialAction(geoset, material, changeListener).redo());
		}
	}

	private void cloneMaterial() {
		AddMaterialAction addMaterialAction = new AddMaterialAction(geoset.getMaterial().deepCopy(), modelHandler.getModel(), changeListener);
		undoManager.pushAction(addMaterialAction.redo());
	}

	private JPanel getHdNamePanel() {
		JPanel hdNamePanel = new JPanel(new MigLayout("fill, ins 0", "[]16[][grow][grow]"));
		hdNamePanel.add(new JLabel("Name: "));
		nameTextField = new TwiTextField(26, this::setLoDName);
		hdNamePanel.add(nameTextField, "spanx 2, wrap");
		return hdNamePanel;
	}

	private JPanel getLodPanel() {
		JPanel lodPanel = new JPanel(new MigLayout("fill, ins 0", "[]16[][grow][grow]"));
		lodPanel.add(new JLabel("LevelOfDetail: "));
		lodSpinner = new IntEditorJSpinner(0, -1, 100, this::setLoD);
		lodPanel.add(lodSpinner, "wrap");
		return lodPanel;
	}


	@Override
	public ComponentPanel<Geoset> setSelectedItem(final Geoset geoset) {
		this.geoset = geoset;
		geosetLabel.setText(geoset.getName());

		setToggleButtonText();
		materialChooser.setSelectedItem(geoset.getMaterial());

		trisLabel.setText("" + geoset.getTriangles().size());
		vertLabel.setText("" + geoset.getVertices().size());

		selectionGroupSpinner.reloadNewValue(geoset.getSelectionGroup());
		lodSpinner.reloadNewValue(geoset.getLevelOfDetail());
		nameTextField.setText(geoset.getLevelOfDetailName());

		hdNamePanel.setVisible(600 <= modelHandler.getModel().getFormatVersion());
		lodPanel.setVisible(1000 <= modelHandler.getModel().getFormatVersion());

		visPanel.setGeoset(geoset);
		geosetAnimPanel.setGeoset(geoset);

		revalidate();
		repaint();
		return this;
	}

	private void setSelectionGroup(int newGroup) {
		geoset.setSelectionGroup(newGroup);
	}


	private void setLoD(int newLod) {
		if (newLod != geoset.getLevelOfDetail()){
			undoManager.pushAction(new ChangeLoDAction(newLod, geoset, changeListener).redo());
		}
	}

	private void toggleSdHd() {
		if (geoset != null) {
			if (geoset.isHD()) {
				undoManager.pushAction(new ConvertToMatricesAction(geoset, changeListener).redo());
			} else {
				UndoAction convertToSkinBones = new ConvertToSkinBonesAction(geoset, null);
				UndoAction recalculateTangs = new RecalculateTangentsAction(geoset.getVertices());
				CompoundAction action = new CompoundAction("Make Geoset HD",
						changeListener::geosetsUpdated,
						convertToSkinBones,
						recalculateTangs);
				undoManager.pushAction(action.redo());
			}
			setToggleButtonText();
		}
	}

	private void setToggleButtonText() {
		toggleSdHd.setVisible(900 <= modelHandler.getModel().getFormatVersion());
		if (geoset.isHD()) {
			toggleSdHd.setText("Make Geoset SD");
		} else {
			toggleSdHd.setText("Make Geoset HD");
		}
	}

	private void setLoDName(String newName) {
		if (!newName.equals(geoset.getLevelOfDetailName())) {
			undoManager.pushAction(new ChangeLoDNameAction(newName, geoset, changeListener).redo());
		}
	}

	private void editUVs() {

	}

	private void removeGeoset() {
		UndoAction action = new DeleteGeosetAction(model, geoset, changeListener);
		undoManager.pushAction(action.redo());
	}
}
