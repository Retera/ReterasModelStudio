package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.editor.actions.model.material.*;
import com.hiveworkshop.rms.editor.model.Layer;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.editor.model.util.FilterMode;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.model.editors.IntEditorJSpinner;
import com.hiveworkshop.rms.ui.application.model.editors.ShaderBox;
import com.hiveworkshop.rms.ui.application.model.material.ComponentLayerPanel;
import com.hiveworkshop.rms.ui.application.model.material.MaterialFlagsPanel;
import com.hiveworkshop.rms.ui.application.tools.MaterialHDAnimEditPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.List;
import java.util.TreeMap;

public class ComponentMaterialPanel extends ComponentPanel<Material> {
	private Material material;
	private ShaderBox<String> shaderOptionComboBox;
	private IntEditorJSpinner priorityPlaneSpinner;
//	private JCheckBox twoSided;
//	private JCheckBox constantColor;
//	private JCheckBox sortPrimsFarZ;
//	private JCheckBox sortPrimsNearZ;
//	private JCheckBox fullResolution;
	private final TreeMap<Integer, ComponentLayerPanel> layerPanelTreeMap = new TreeMap<>();
	private final JPanel layersHolderPanel;
	private MaterialFlagsPanel materialFlagsPanel;
	private JLabel shaderLabel;
	private final JButton addLayerButton;
	private JButton copyMaterialAnim;

	public ComponentMaterialPanel(ModelHandler modelHandler) {
		super(modelHandler);

		setLayout(new MigLayout("fill, hidemode 2", "[][][grow]", "[][][grow]"));
		materialFlagsPanel = new MaterialFlagsPanel(modelHandler);

		add(getTopPanel(), "growx, spanx, wrap");
		layersHolderPanel = new JPanel(new MigLayout("fill, ins 0", "[grow]"));
		JPanel layersPanel = new JPanel(new MigLayout("fill, ins 0", "[][][grow]"));
		layersPanel.add(layersHolderPanel, "growx, spanx, wrap");
		addLayerButton = getButton("Add Layer", e -> addLayer());
		layersPanel.add(addLayerButton);
		add(layersPanel, "growx, growy, spanx");
	}

	@Override
	public ComponentPanel<Material> setSelectedItem(final Material material) {
		this.material = material;
		selectedItem = material;

		int formatVersion = model.getFormatVersion();
		shaderLabel.setVisible(ModelUtils.isShaderStringSupported(formatVersion));
		shaderOptionComboBox.setVisible(ModelUtils.isShaderStringSupported(formatVersion));

		shaderOptionComboBox.setSelectedItem(material.getShaderString());
		priorityPlaneSpinner.reloadNewValue(material.getPriorityPlane());

		materialFlagsPanel.setMaterial(material);
		materialFlagsPanel.setVisible(900 <= formatVersion && (material.getShaderString().equals(Material.SHADER_HD_DEFAULT_UNIT)||material.getShaderString().equals(Material.SHADER_HD_CRYSTAL)));

		copyMaterialAnim.setVisible(900 <= formatVersion && (material.getShaderString().equals(Material.SHADER_HD_DEFAULT_UNIT)||material.getShaderString().equals(Material.SHADER_HD_CRYSTAL)));

		layersHolderPanel.removeAll();
		for (int i = 0; i < material.getLayers().size(); i++) {
			int finalI = i;
			ComponentLayerPanel componentLayer = layerPanelTreeMap.computeIfAbsent(i, k -> new ComponentLayerPanel(modelHandler, "Layer " + finalI));
			componentLayer.setMaterial(material);
			componentLayer.setSelectedItem(material.getLayer(i));
			layersHolderPanel.add(componentLayer, "growx, wrap");
		}

		addLayerButton.setVisible(formatVersion < 900 || 1000 < formatVersion);

		revalidate();
		repaint();
		return this;
	}

	protected JPanel getTopPanel() {
//		JPanel topPanel = new JPanel(new MigLayout("fill, flowy, ins 0, hidemode 2", "[][][grow][grow]", "[][][grow]"));
		JPanel topPanel = new JPanel(new MigLayout("fill, ins 0, hidemode 3", "[grow][grow]", "[grow][]"));

		shaderLabel = new JLabel("Shader:");
		shaderOptionComboBox = getShaderComboBox();

		priorityPlaneSpinner = new IntEditorJSpinner(-1, -1, this::changePriorityPlane);

		JButton duplicate_material = new JButton("Duplicate Material");
		duplicate_material.addActionListener(e -> duplicateMaterial());


		copyMaterialAnim = new JButton("copy layer animations from other");
		copyMaterialAnim.addActionListener(e -> copyFromOther());


		JPanel panel1 = new JPanel(new MigLayout("ins 0, hidemode 3"));
		JPanel panel2 = new JPanel(new MigLayout("ins 0, fill, hidemode 3"));

		panel1.add(shaderLabel);
		panel1.add(shaderOptionComboBox, "growx, wrap");
		panel1.add(new JLabel("Priority Plane:"));
		panel1.add(priorityPlaneSpinner, "growx, wrap");

		panel2.add(duplicate_material, "right");
		panel2.add(getDeleteButton(e -> deleteMaterial()), "right, wrap");
		panel2.add(copyMaterialAnim, "right");


		topPanel.add(panel1);
		topPanel.add(panel2, "growx, wrap");
		topPanel.add(materialFlagsPanel, "spanx, growx");
		return topPanel;
	}

	protected ShaderBox<String> getShaderComboBox() {
		final String[] shaderOptions = {"", "Shader_SD_FixedFunction", "Shader_HD_DefaultUnit"};
		return new ShaderBox<>(shaderOptions, this::setShaderString);
	}

	protected void setShaderString(String newShader) {
		if(!material.getShaderString().equals(newShader)){
			undoManager.pushAction(new SetMaterialShaderStringAction(model, material, newShader, changeListener).redo());
		}
	}

	protected void changePriorityPlane(int newValue) {
		if (material.getPriorityPlane() != newValue) {
			undoManager.pushAction(new SetMaterialPriorityPlaneAction(material, newValue, changeListener).redo());
		}
	}

	protected void deleteMaterial() {
		if (!model.getMaterials().isEmpty()) {
			undoManager.pushAction(new RemoveMaterialAction(material, model, changeListener).redo());
		}
	}

	protected void duplicateMaterial(){
		Material newMaterial = this.material.deepCopy();
		undoManager.pushAction(new AddMaterialAction(newMaterial, model, ModelStructureChangeListener.changeListener).redo());
	}


	protected void copyFromOther() {
		MaterialHDAnimEditPanel.show(ProgramGlobals.getMainPanel(), model, material, undoManager);
		repaint();
	}
	protected void addLayer() {
		List<Layer> layers = material.getLayers();
		undoManager.pushAction(new AddLayerAction(new Layer(FilterMode.NONE, layers.get(layers.size()-1).getTextures()), material, changeListener).redo());
	}

}
