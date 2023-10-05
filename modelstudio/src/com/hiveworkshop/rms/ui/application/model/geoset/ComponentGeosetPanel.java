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
import com.hiveworkshop.rms.editor.actions.util.BoolAction;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.ui.application.model.ComponentPanel;
import com.hiveworkshop.rms.ui.application.model.ComponentsPanel;
import com.hiveworkshop.rms.ui.application.model.editors.IntEditorJSpinner;
import com.hiveworkshop.rms.ui.application.model.editors.TwiTextField;
import com.hiveworkshop.rms.ui.gui.modeledit.MaterialListRenderer;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.modelcomponenttree.DisplayElementType;
import com.hiveworkshop.rms.util.TwiComboBox;
import com.hiveworkshop.rms.util.uiFactories.Button;
import com.hiveworkshop.rms.util.uiFactories.CheckBox;
import com.hiveworkshop.rms.util.uiFactories.Label;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class ComponentGeosetPanel extends ComponentPanel<Geoset> {
	private final JLabel geosetLabel = new JLabel("geoset name");
	private final JLabel trisLabel = new JLabel("0");
	private final JLabel vertLabel = new JLabel("0");
	private final JLabel uvLayersLabel = new JLabel("0");
	private JPanel lodPanel;
	private final JPanel hdNamePanel;
	private IntEditorJSpinner lodSpinner;
	private TwiTextField nameTextField;
	private final JButton toggleSdHd;
	private final JButton recalculatTangents;
	private IntEditorJSpinner selectionGroupSpinner;
	private final JCheckBox unselectableBox;
	private final JCheckBox dropShadow;
	private Geoset geoset;

	private final GeosetVisPanel visPanel;
	private final GeosetAnimPanel geosetAnimPanel;

	private TwiComboBox<Material> materialChooser;

	public ComponentGeosetPanel(ModelHandler modelHandler, ComponentsPanel componentsPanel) {
		super(modelHandler, componentsPanel);
		setLayout(new MigLayout("hidemode 1", "[][][grow]", "[]"));

		add(geosetLabel, "");
		add(getDeleteButton(e -> removeGeoset()), "spanx, right, wrap");
		hdNamePanel = getHdNamePanel();
		add(hdNamePanel, "wrap, growx, spanx");
		unselectableBox = CheckBox.create("Unselectable", this::setUnSelectable);
		add(unselectableBox, "spanx, wrap");
		dropShadow = CheckBox.create("Treat geoset as drop shadow", this::setDropShadow);
		add(dropShadow, "spanx, wrap");

		JPanel topPanel = new JPanel(new MigLayout("hidemode 1, ins 0", "[][grow][grow]", "[]"));
		add(topPanel, "wrap");
		topPanel.add(getGeosetInfoPanel(), "growx");

		JPanel materialPanelHolder = getMaterialPanelHolder();
		topPanel.add(materialPanelHolder, "wrap, growx, spanx");


		JButton editUvButton = new JButton("Edit Geoset UVs");

		toggleSdHd = Button.create("Make Geoset HD", e -> toggleSdHd());
		add(toggleSdHd, "wrap");

		recalculatTangents = Button.create("Recalculate Tangents", e -> recalculateTangents());
		add(recalculatTangents, "wrap");

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

		materialPanelHolder.add(Button.create("Clone This Material", e -> cloneMaterial()));
		materialPanelHolder.add(Button.create("View Material", e -> componentsPanel.setSelectedPanel(geoset.getMaterial(), DisplayElementType.MATERIAL)));


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

		geosetInfoPanel.add(Label.create("UVLayers: ", this::uvLayersPopup));
		geosetInfoPanel.add(uvLayersLabel, "wrap");

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
		dropShadow.setSelected(geoset.isDropShadow());
		unselectableBox.setSelected(geoset.getUnselectable());

		setToggleButtonText();
		materialChooser.setSelectedItem(geoset.getMaterial());

		trisLabel.setText("" + geoset.getTriangles().size());
		vertLabel.setText("" + geoset.getVertices().size());
		uvLayersLabel.setText("" + geoset.numUVLayers());

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
		if (newLod != geoset.getLevelOfDetail()) {
			undoManager.pushAction(new ChangeLoDAction(newLod, geoset, changeListener).redo());
		}
	}

	private void toggleSdHd() {
		if (geoset != null) {
			if (900 <= model.getFormatVersion() && geoset.hasSkin()) {
				undoManager.pushAction(new ConvertToMatricesAction(geoset, changeListener).redo());
			} else {
				undoManager.pushAction(new ConvertToSkinBonesAction(geoset, changeListener).redo());
			}
			setToggleButtonText();
		}
	}

	private void setToggleButtonText() {
		toggleSdHd.setVisible(900 <= modelHandler.getModel().getFormatVersion());
		recalculatTangents.setVisible(900 <= modelHandler.getModel().getFormatVersion());
		if (geoset.hasSkin()) {
			toggleSdHd.setText("Convert SkinWeights to Matrices");
		} else {
			toggleSdHd.setText("Convert Matrices to SkinWeights");
		}
		if (geoset.hasTangents()) {
			recalculatTangents.setText("Recalculate Tangents");
		} else {
			recalculatTangents.setText("Calculate Tangents");
		}
	}

	private void recalculateTangents() {
		if (900 <= model.getFormatVersion()) {
			undoManager.pushAction(new RecalculateTangentsAction(geoset.getVertices()).redo());
		}
	}

	private void setLoDName(String newName) {
		if (!newName.equals(geoset.getLevelOfDetailName())) {
			undoManager.pushAction(new ChangeLoDNameAction(newName, geoset, changeListener).redo());
		}
	}

	private void editUVs() {

	}

	private void setDropShadow(boolean b) {
		if (geoset.isDropShadow() != b) {
			String actionName = b ? "Set Geoset as DropDhadow" : "Set Geoset as not DropDhadow";
			undoManager.pushAction(new BoolAction(geoset::setDropShadow, b, actionName, null));
		}
	}

	private void setUnSelectable(boolean b) {
		if (geoset.getUnselectable() != b) {
			String actionName = b ? "Set Geoset Unselectable" : "Set Geoset Selectable";
			undoManager.pushAction(new BoolAction(geoset::setUnselectable, b, actionName, null));
		}
	}

	private void removeGeoset() {
		UndoAction action = new DeleteGeosetAction(model, geoset, changeListener);
		undoManager.pushAction(action.redo());
	}

	private void uvLayersPopup() {
		UVLayerPanel uvLayerPanel = new UVLayerPanel(geoset, undoManager);
		int ok_cancel = JOptionPane.showConfirmDialog(uvLayersLabel, uvLayerPanel, "UV Layers", JOptionPane.OK_CANCEL_OPTION);
		if (ok_cancel == JOptionPane.OK_OPTION) {
			uvLayerPanel.applyEdit();
		}
	}
}
