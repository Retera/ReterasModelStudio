package com.hiveworkshop.rms.ui.application.model.material;

import com.hiveworkshop.rms.editor.model.Layer;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelViewManager;
import com.hiveworkshop.rms.ui.application.actions.model.material.AddLayerAction;
import com.hiveworkshop.rms.ui.application.actions.model.material.RemoveMaterialAction;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class ComponentMaterialLayersPanel extends JPanel {
	public static final String[] REFORGED_LAYER_DEFINITIONS = {"Diffuse", "Vertex", "ORM", "Emissive", "Team Color",
			"Reflections"};
	private static final Color HIGHLIGHT_BUTTON_BACKGROUND_COLOR = new Color(100, 118, 135);
	private Material material;
	private UndoActionListener undoActionListener;
	private ModelViewManager modelViewManager;
	private ModelStructureChangeListener modelStructureChangeListener;
	private final JPanel layerPanelsHolder;
	private final Map<String, ComponentLayerPanel> layerPanelMap;

	private final JCheckBox twoSided;

	public ComponentMaterialLayersPanel() {
		setLayout(new MigLayout("fill", "[][][grow]"));

		JPanel twoSidedBoxHolder = new JPanel(new MigLayout("fill", "[grow]"));
		add(twoSidedBoxHolder, "growx, span 3, wrap");

		twoSided = new JCheckBox("TwoSided", false);
		twoSided.addActionListener(e -> setTwoSided());
		twoSidedBoxHolder.add(twoSided);

		JButton deleteMaterialButton = new JButton("Delete");
		deleteMaterialButton.setBackground(Color.RED);
		deleteMaterialButton.setForeground(Color.WHITE);
		deleteMaterialButton.addActionListener(e -> deleteMaterial());
		twoSidedBoxHolder.add(deleteMaterialButton, "right");

//		add(twoSided, "wrap");
		layerPanelMap = new HashMap<>();
		layerPanelsHolder = new JPanel(new MigLayout("fill", "[grow]"));
//		layerPanelsHolder.setOpaque(true);
//		layerPanelsHolder.setBackground(Color.magenta);
		add(layerPanelsHolder, "growx, span 3, wrap");
		JButton addLayerButton = getAddLayerButton();
		add(addLayerButton);
	}

	private JButton getAddLayerButton() {
		final JButton addLayerButton;
		addLayerButton = new JButton("Add Layer");
		addLayerButton.setBackground(HIGHLIGHT_BUTTON_BACKGROUND_COLOR);
		addLayerButton.setForeground(Color.WHITE);
		addLayerButton.addActionListener(e -> addLayer());
		return addLayerButton;
	}

	public void setMaterial(final Material material, final ModelViewManager modelViewManager,
	                        final UndoActionListener undoActionListener,
	                        final ModelStructureChangeListener modelStructureChangeListener) {
		this.material = material;
		this.undoActionListener = undoActionListener;
		this.modelViewManager = modelViewManager;
		this.modelStructureChangeListener = modelStructureChangeListener;
		final boolean hdShader = Material.SHADER_HD_DEFAULT_UNIT.equals(material.getShaderString());
		twoSided.setVisible(hdShader);
		if (hdShader) {
			twoSided.setSelected(material.getTwoSided());
		} else {
			twoSided.setSelected(false);
		}

		layerPanelsHolder.removeAll();
		createLayerPanels(material, modelViewManager, undoActionListener, modelStructureChangeListener, hdShader);
		revalidate();
		repaint();

	}


	private void createLayerPanels(Material material, ModelViewManager modelViewManager, UndoActionListener undoActionListener, ModelStructureChangeListener modelStructureChangeListener, boolean hdShader) {
		for (int i = 0; i < material.getLayers().size(); i++) {
			final Layer layer = material.getLayers().get(i);
			ComponentLayerPanel panel;

			String keyString = material.toString() + layer.toString();
			if (layerPanelMap.containsKey(keyString)) {
				panel = layerPanelMap.get(keyString);
			} else {
				panel = new ComponentLayerPanel(material, modelViewManager, i, hdShader, undoActionListener, modelStructureChangeListener);
				layerPanelMap.put(keyString, panel);
			}
			panel.setLayer(modelViewManager.getModel(), layer, modelViewManager.getModel().getFormatVersion(), hdShader, undoActionListener);
			layerPanelsHolder.add(panel, "growx, wrap");
		}
	}


	private void addLayer() {
		AddLayerAction addLayerAction = new AddLayerAction(new Layer("None", 0), material, modelStructureChangeListener);
		undoActionListener.pushAction(addLayerAction);
		addLayerAction.redo();
	}

	private void setTwoSided() {
		material.setTwoSided(twoSided.isSelected());
	}

	private void deleteMaterial() {
		if (!modelViewManager.getModel().getMaterials().isEmpty()) {
			RemoveMaterialAction removeMaterialAction = new RemoveMaterialAction(material, modelViewManager, modelStructureChangeListener);
			undoActionListener.pushAction(removeMaterialAction);
			removeMaterialAction.redo();
		}
	}

}
