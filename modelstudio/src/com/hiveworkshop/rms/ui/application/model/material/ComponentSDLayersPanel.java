package com.hiveworkshop.rms.ui.application.model.material;

import com.hiveworkshop.rms.editor.actions.model.material.AddLayerAction;
import com.hiveworkshop.rms.editor.model.Layer;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.ui.application.model.ComponentPanel;
import com.hiveworkshop.rms.ui.application.model.editors.IntEditorJSpinner;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.TreeMap;

public class ComponentSDLayersPanel extends ComponentLayersPanel {
	private final TreeMap<Integer, ComponentSDLayer> sdLayerPanelTreeMap = new TreeMap<>();
	private JPanel layerPanelsHolder;
	private JLabel shaderLabel;

	public ComponentSDLayersPanel(ModelHandler modelHandler) {
		super(modelHandler);
		setLayout(new MigLayout("fill, hidemode 2", "[][][grow]", "[][][grow]"));
//		setLayout(new MigLayout("fill, hidemode 2", "[grow]", "[grow]"));

		add(getTopPanel(), "growx, spanx");
//		add(getLayersHolderPanel(), "growx, growy, span 3");
		add(getLayersHolderPanel(), "growx, growy");
	}

	protected JPanel getTopPanel() {
		JPanel topPanel = new JPanel(new MigLayout("fill, ins 0, hidemode 2", "[][][grow][grow]", "[][][grow]"));

		shaderOptionComboBox = getShaderComboBox();
		shaderLabel = new JLabel("Shader:");
		topPanel.add(shaderLabel);
		topPanel.add(shaderOptionComboBox, "growx, wrap");

		topPanel.add(new JLabel("Priority Plane:"));
		priorityPlaneSpinner = new IntEditorJSpinner(-1, -1, this::changePriorityPlane);
		topPanel.add(priorityPlaneSpinner, "growx");

		JButton duplicate_material = new JButton("Duplicate Material");
		duplicate_material.addActionListener(e -> duplicateMaterial());
		topPanel.add(duplicate_material, "right");

		topPanel.add(getDeleteButton(e -> deleteMaterial()), "right, wrap");
		return topPanel;
	}

	protected JPanel getLayersHolderPanel() {
		JPanel layersPanel = new JPanel(new MigLayout("fill, ins 0", "[][][grow]"));

		layerPanelsHolder = new JPanel(new MigLayout("fill, ins 0", "[grow]"));
		layersPanel.add(layerPanelsHolder, "growx, spanx, wrap");

		layersPanel.add(getAddLayerButton());

		return layersPanel;
	}

	@Override
	public ComponentPanel<Material> setSelectedItem(Material itemToSelect) {
		this.material = itemToSelect;
		selectedItem = itemToSelect;

		shaderLabel.setVisible(ModelUtils.isShaderStringSupported(model.getFormatVersion()));
		shaderOptionComboBox.setVisible(ModelUtils.isShaderStringSupported(model.getFormatVersion()));

		shaderOptionComboBox.setSelectedItem(material.getShaderString());
		priorityPlaneSpinner.reloadNewValue(itemToSelect.getPriorityPlane());

		layerPanelsHolder.removeAll();
		for (int i = 0; i < material.getLayers().size(); i++) {
			int finalI = i;
			ComponentSDLayer componentSDLayer = sdLayerPanelTreeMap.computeIfAbsent(i, k -> new ComponentSDLayer(modelHandler, finalI));
			componentSDLayer.setMaterial(material).setSelectedItem(material.getLayer(i));
			layerPanelsHolder.add(componentSDLayer, "growx, wrap");
		}
		return this;
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
}
