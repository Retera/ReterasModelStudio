package com.hiveworkshop.rms.ui.application.model.material;

import com.hiveworkshop.rms.editor.actions.model.material.AddLayerAction;
import com.hiveworkshop.rms.editor.actions.model.material.RemoveMaterialAction;
import com.hiveworkshop.rms.editor.model.Layer;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public class ComponentMaterialLayersPanel extends JPanel {
	public static final String[] REFORGED_LAYER_DEFINITIONS = {"Diffuse", "Vertex", "ORM", "Emissive", "Team Color", "Reflections"};
	private static final Color HIGHLIGHT_BUTTON_BACKGROUND_COLOR = new Color(100, 118, 135);
	private Material material;
	private final ModelHandler modelHandler;
	private final ModelStructureChangeListener changeListener;
	private final JPanel layerPanelsHolder;

	private final JCheckBox twoSided;

	public ComponentMaterialLayersPanel(ModelHandler modelHandler) {
		setLayout(new MigLayout("fill", "[][][grow]"));
		this.modelHandler = modelHandler;
		this.changeListener = ModelStructureChangeListener.changeListener;

		JPanel twoSidedBoxHolder = new JPanel(new MigLayout("fill", "[grow]"));
		add(twoSidedBoxHolder, "growx, span 3, wrap");

		twoSided = new JCheckBox("TwoSided", false);
		twoSided.addActionListener(e -> setTwoSided());
		twoSidedBoxHolder.add(twoSided);

		twoSidedBoxHolder.add(getDeleteMaterialButton(), "right");

		layerPanelsHolder = new JPanel(new MigLayout("fill", "[grow]"));
		add(layerPanelsHolder, "growx, spanx, wrap");

		add(getAddLayerButton());
	}

	private JButton getDeleteMaterialButton() {
		JButton deleteMaterialButton = new JButton("Delete");
		deleteMaterialButton.setBackground(Color.RED);
		deleteMaterialButton.setForeground(Color.WHITE);
		deleteMaterialButton.addActionListener(e -> deleteMaterial());
		return deleteMaterialButton;
	}

	private JButton getAddLayerButton() {
		JButton addLayerButton = new JButton("Add Layer");
		addLayerButton.setBackground(HIGHLIGHT_BUTTON_BACKGROUND_COLOR);
		addLayerButton.setForeground(Color.WHITE);
		addLayerButton.addActionListener(e -> addLayer());
		return addLayerButton;
	}

	public void setMaterial(Material material) {
		this.material = material;
		boolean hdShader = Material.SHADER_HD_DEFAULT_UNIT.equals(material.getShaderString());
		twoSided.setVisible(hdShader);
		if (hdShader) {
			twoSided.setSelected(material.getTwoSided());
		} else {
			twoSided.setSelected(false);
		}

		layerPanelsHolder.removeAll();
		createLayerPanels(material, modelHandler);
		revalidate();
		repaint();

	}


	private void createLayerPanels(Material material, ModelHandler modelHandler) {
		for (int i = 0; i < material.getLayers().size(); i++) {
			final Layer layer = material.getLayers().get(i);
			ComponentLayerPanel panel = new ComponentLayerPanel(layer, material, modelHandler, i);
			layerPanelsHolder.add(panel, "growx, wrap");
		}
	}


	private void addLayer() {
		AddLayerAction addLayerAction = new AddLayerAction(new Layer("None", 0), material, changeListener);
		modelHandler.getUndoManager().pushAction(addLayerAction.redo());
	}

	private void setTwoSided() {
		material.setTwoSided(twoSided.isSelected());
	}

	private void deleteMaterial() {
		if (!modelHandler.getModel().getMaterials().isEmpty()) {
			RemoveMaterialAction removeMaterialAction = new RemoveMaterialAction(material, modelHandler.getModel(), changeListener);
			modelHandler.getUndoManager().pushAction(removeMaterialAction.redo());
		}
	}

}
