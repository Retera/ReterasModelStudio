package com.hiveworkshop.rms.ui.application.model.material;

import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.editor.model.util.HD_Material_Layer;
import com.hiveworkshop.rms.ui.application.model.ComponentPanel;
import com.hiveworkshop.rms.ui.application.model.editors.IntEditorJSpinner;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.TreeMap;

public class ComponentHDLayersPanel extends ComponentLayersPanel {
	private final TreeMap<HD_Material_Layer, ComponentHDLayer> hdLayerPanelTreeMap = new TreeMap<>();

	public ComponentHDLayersPanel(ModelHandler modelHandler) {
		super(modelHandler);
		setLayout(new MigLayout("fill, hidemode 2", "[][][grow]", "[][][grow]"));
//		setLayout(new MigLayout("fill, hidemode 2", "[grow]", "[grow]"));

		add(getTopPanel(), "growx, spanx");
//		add(getTwoSidedBoxHolder(), "growx, span 3, wrap");
//		add(getLayersHolderPanel(), "growx, growy, span 3");
//		add(getLayersHolderPanel(), "growx, growy");
		add(getLayersHolderPanel(), "growx, growy, spanx");
	}

	protected JPanel getTopPanel() {
		JPanel topPanel = new JPanel(new MigLayout("fill, ins 0, hidemode 2", "[][][grow]", "[][][grow]"));

		JLabel shaderLabel = new JLabel("Shader:");
		topPanel.add(shaderLabel);
		shaderOptionComboBox = getShaderComboBox();
		topPanel.add(shaderOptionComboBox, "growx");
		topPanel.add(getDeleteButton(e -> deleteMaterial()), "right, wrap");

		topPanel.add(new JLabel("Priority Plane:"));
		priorityPlaneSpinner = new IntEditorJSpinner(-1, -1, this::changePriorityPlane);
		topPanel.add(priorityPlaneSpinner, "growx, wrap");

		twoSided = new JCheckBox("TwoSided", false);
		twoSided.addActionListener(e -> setTwoSided());
		topPanel.add(twoSided);

		return topPanel;
	}

	private JPanel getTwoSidedBoxHolder() {
		JPanel twoSidedBoxHolder = new JPanel(new MigLayout("fill", "[grow]"));

		twoSided = new JCheckBox("TwoSided", false);
		twoSided.addActionListener(e -> setTwoSided());
		twoSidedBoxHolder.add(twoSided);
		twoSidedBoxHolder.add(getDeleteButton(e -> deleteMaterial()), "right");
		return twoSidedBoxHolder;
	}

	protected JPanel getLayersHolderPanel() {
		JPanel layerPanelsHolder = new JPanel(new MigLayout("fill, ins 0", "[grow]"));

		for (HD_Material_Layer ld : HD_Material_Layer.values()) {
//			System.out.println("ComponentHDLayersPanel: creating " +ld + " panel");
			ComponentHDLayer componentHDLayer = hdLayerPanelTreeMap.computeIfAbsent(ld, k -> new ComponentHDLayer(modelHandler, ld));
			layerPanelsHolder.add(componentHDLayer, "growx, wrap");
		}

		return layerPanelsHolder;
	}


	@Override
	public ComponentPanel<Material> setSelectedItem(final Material material) {
		this.material = material;
		selectedItem = material;
//		System.out.println("setting Material");

//		System.out.println("setting Shader Option");
		shaderOptionComboBox.setSelectedItem(material.getShaderString());
//		System.out.println("setting PriorityPlane");
		priorityPlaneSpinner.reloadNewValue(material.getPriorityPlane());


//		System.out.println("setting TwoSided");
		twoSided.setSelected(material.getTwoSided());

		for (HD_Material_Layer ld : hdLayerPanelTreeMap.keySet()) {
//			System.out.println("updating " + ld);
			hdLayerPanelTreeMap.get(ld).setSelectedItem(material.getLayer(ld.ordinal()));
		}
		return this;
	}
}
