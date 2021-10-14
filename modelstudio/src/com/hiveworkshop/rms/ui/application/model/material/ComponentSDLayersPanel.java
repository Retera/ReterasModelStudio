package com.hiveworkshop.rms.ui.application.model.material;

import com.hiveworkshop.rms.editor.actions.model.material.AddLayerAction;
import com.hiveworkshop.rms.editor.actions.model.material.RemoveMaterialAction;
import com.hiveworkshop.rms.editor.actions.model.material.SetMaterialPriorityPlaneAction;
import com.hiveworkshop.rms.editor.model.Layer;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.ui.application.model.ComponentPanel;
import com.hiveworkshop.rms.ui.application.model.editors.IntEditorJSpinner;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.TreeMap;

public class ComponentSDLayersPanel extends ComponentPanel<Material> {
	private static final Color HIGHLIGHT_BUTTON_BACKGROUND_COLOR = new Color(100, 118, 135);
	private Material material;
	private final TreeMap<Integer, ComponentSDLayer> sdLayerPanelTreeMap = new TreeMap<>();

	private IntEditorJSpinner priorityPlaneSpinner;

	private JPanel layerPanelsHolder;

	public ComponentSDLayersPanel(ModelHandler modelHandler) {
		super(modelHandler);
		setLayout(new MigLayout("fill, hidemode 2", "[][][grow]", "[][][grow]"));

		add(getTopPanel(), "growx, spanx");
		add(getLayersHolderPanel(), "growx, growy, span 3");
	}

	private JPanel getTopPanel() {
		JPanel topPanel = new JPanel(new MigLayout("fill, ins 0, hidemode 2", "[][][grow]", "[][][grow]"));

		topPanel.add(new JLabel("Priority Plane:"));
		priorityPlaneSpinner = new IntEditorJSpinner(-1, -1, this::changePriorityPlane);
		topPanel.add(priorityPlaneSpinner, "growx");
		topPanel.add(getDeleteButton(e -> deleteMaterial()), "right, wrap");
		return topPanel;
	}

	private JPanel getLayersHolderPanel() {
		JPanel layersPanel = new JPanel(new MigLayout("fill", "[][][grow]"));

		layerPanelsHolder = new JPanel(new MigLayout("fill", "[grow]"));
		layersPanel.add(layerPanelsHolder, "growx, spanx, wrap");

		layersPanel.add(getAddLayerButton());

		return layersPanel;
	}

	@Override
	public void setSelectedItem(Material itemToSelect) {
		this.material = itemToSelect;
		selectedItem = itemToSelect;

		priorityPlaneSpinner.reloadNewValue(itemToSelect.getPriorityPlane());

		layerPanelsHolder.removeAll();
		for (int i = 0; i < material.getLayers().size(); i++) {
			int finalI = i;
			ComponentSDLayer componentSDLayer = sdLayerPanelTreeMap.computeIfAbsent(i, k -> new ComponentSDLayer(modelHandler, finalI));
			componentSDLayer.setMaterial(material).setSelectedItem(material.getLayer(i));
			layerPanelsHolder.add(componentSDLayer, "growx, wrap");
		}
	}


	private void changePriorityPlane(int newValue) {
//		System.out.println("changePriorityPlane");
		undoManager.pushAction(new SetMaterialPriorityPlaneAction(material, newValue, changeListener).redo());
	}

	private JButton getAddLayerButton() {
		JButton addLayerButton = new JButton("Add Layer");
		addLayerButton.setBackground(HIGHLIGHT_BUTTON_BACKGROUND_COLOR);
		addLayerButton.setForeground(Color.WHITE);
		addLayerButton.addActionListener(e -> addLayer());
		return addLayerButton;
	}

	private void addLayer() {
		undoManager.pushAction(new AddLayerAction(new Layer(0), material, changeListener).redo());
	}

	private void deleteMaterial() {
		if (!model.getMaterials().isEmpty()) {
			undoManager.pushAction(new RemoveMaterialAction(material, model, changeListener).redo());
		}
	}
}
