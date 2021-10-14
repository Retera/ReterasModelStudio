package com.hiveworkshop.rms.ui.application.model.material;

import com.hiveworkshop.rms.editor.actions.model.material.*;
import com.hiveworkshop.rms.editor.model.Layer;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.editor.model.util.FilterMode;
import com.hiveworkshop.rms.editor.model.util.HD_Material_Layer;
import com.hiveworkshop.rms.ui.application.model.ComponentPanel;
import com.hiveworkshop.rms.ui.application.model.editors.IntEditorJSpinner;
import com.hiveworkshop.rms.ui.application.model.editors.ShaderBox;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.TreeMap;

public class ComponentHDLayersPanel extends ComponentPanel<Material> {
	public static final String[] REFORGED_LAYER_DEFINITIONS = {"Diffuse", "Vertex", "ORM", "Emissive", "Team Color", "Reflections"};
	private static final Color HIGHLIGHT_BUTTON_BACKGROUND_COLOR = new Color(100, 118, 135);
	private static final String SD = "SD";
	private static final String HD = "HD";
	private Material material;

	private final TreeMap<HD_Material_Layer, ComponentHDLayer> hdLayerPanelTreeMap = new TreeMap<>();

	private JComboBox<String> shaderOptionComboBox;
	private JLabel shaderLabel;
	private IntEditorJSpinner priorityPlaneSpinner;

	private JCheckBox twoSided;

	public ComponentHDLayersPanel(ModelHandler modelHandler) {
		super(modelHandler);
		setLayout(new MigLayout("fill, hidemode 2", "[][][grow]", "[][][grow]"));

		add(getTopPanel(), "growx, spanx");
//		add(getTwoSidedBoxHolder(), "growx, span 3, wrap");
		add(getLayersHolderPanel(), "growx, growy, span 3");
	}

	private JPanel getTopPanel() {
		JPanel topPanel = new JPanel(new MigLayout("fill, ins 0, hidemode 2", "[][][grow]", "[][][grow]"));

		shaderLabel = new JLabel("Shader:");
		shaderOptionComboBox = getShaderComboBox();
		topPanel.add(shaderLabel);
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

	private JPanel getLayersHolderPanel() {
		JPanel layersPanel = new JPanel(new MigLayout("fill", "[][][grow]"));

		JPanel layerPanelsHolder = new JPanel(new MigLayout("fill", "[grow]"));
		layersPanel.add(layerPanelsHolder, "growx, spanx, wrap");

		for (HD_Material_Layer ld : HD_Material_Layer.values()) {
//			System.out.println("ComponentHDLayersPanel: creating " +ld + " panel");
			ComponentHDLayer componentHDLayer = hdLayerPanelTreeMap.computeIfAbsent(ld, k -> new ComponentHDLayer(modelHandler, ld));
			layerPanelsHolder.add(componentHDLayer, "growx, wrap");
		}

		return layerPanelsHolder;
	}


	@Override
	public void setSelectedItem(final Material material) {
		this.material = material;
		selectedItem = material;
		System.out.println("setting Material");

		System.out.println("setting Shader Option");
		shaderOptionComboBox.setSelectedItem(material.getShaderString());
		System.out.println("setting PriorityPlane");
		priorityPlaneSpinner.reloadNewValue(material.getPriorityPlane());


		System.out.println("setting TwoSided");
		twoSided.setSelected(material.getTwoSided());

		for (HD_Material_Layer ld : hdLayerPanelTreeMap.keySet()) {
			System.out.println("updating " + ld);
			hdLayerPanelTreeMap.get(ld).setSelectedItem(material.getLayer(ld.ordinal()));
		}
	}

	private JComboBox<String> getShaderComboBox() {
		final String[] shaderOptions = {"", "Shader_SD_FixedFunction", "Shader_HD_DefaultUnit"};
		return new ShaderBox<>(shaderOptions, this::setShaderString);
	}

	private void changePriorityPlane(int newValue) {
		System.out.println("changePriorityPlane");
		undoManager.pushAction(new SetMaterialPriorityPlaneAction(material, newValue, changeListener).redo());
	}

	private void setShaderString(String newShader) {
		System.out.println("setShaderString");
		undoManager.pushAction(new SetMaterialShaderStringAction(material, newShader, changeListener).redo());
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

	private void setTwoSided() {
		System.out.println("setTwoSided");
		material.setTwoSided(twoSided.isSelected());
	}

	private void deleteMaterial() {
		if (!model.getMaterials().isEmpty()) {
			undoManager.pushAction(new RemoveMaterialAction(material, model, changeListener).redo());
		}
	}

//	private JPanel getLayerPanel(Layer layer, HD_Material_Layer ld){
//		JPanel layerPanel = new JPanel(new MigLayout("fill", "[][][grow]", "[][fill]"));
//		Border lineBorder = BorderFactory.createLineBorder(Color.LIGHT_GRAY);
//		layerPanel.setBorder(BorderFactory.createTitledBorder(lineBorder, ld.getLayerName() + " Layer"));
//
//		layerPanel.add(getLeftHandPanel(layer), "spany");
//		layerPanel.add(new LayerFlagsPanel(modelHandler, layer), "");
//		layerPanel.add(getBitmapPreview(layer.getTextureBitmap(), modelHandler.getModel()), "growx, top");
//
//		return layerPanel;
//	}


//	private JPanel getBitmapPreview(Bitmap defaultTexture, EditableModel model) {
//		JPanel texturePreviewPanel = new JPanel();
//		texturePreviewPanel.setLayout(new MigLayout("gap 0, ins 0, fill", "[grow]", "[grow]"));
//
//		if (defaultTexture != null) {
//			DataSource workingDirectory = model.getWrappedDataSource();
//			try {
//				BufferedImage texture = BLPHandler.getImage(defaultTexture, workingDirectory);
//				texturePreviewPanel.add(new ZoomableImagePreviewPanel(texture, true), "growx, growy");
//			} catch (final Exception exc) {
//				BufferedImage image = new BufferedImage(512, 512, BufferedImage.TYPE_INT_ARGB);
//				Graphics2D g2 = image.createGraphics();
//				g2.setColor(Color.RED);
//				g2.drawString(exc.getClass().getSimpleName() + ": " + exc.getMessage(), 128, 128);
//				texturePreviewPanel.add(new ZoomableImagePreviewPanel(image, true), "growx, growy");
//			}
//			texturePreviewPanel.revalidate();
//			texturePreviewPanel.repaint();
//		}
//		return texturePreviewPanel;
//	}

//	private JPanel getLeftHandPanel(Layer layer) {
//		JPanel leftHandSettingsPanel = new JPanel(new MigLayout());
//		JPanel topSettingsPanel = getTopSettingsPanel(layer);
//
//		leftHandSettingsPanel.add(topSettingsPanel, "wrap, growx");
//
//		TextureValuePanel texturePanel = getTexturePanel(layer);
//		leftHandSettingsPanel.add(texturePanel, "wrap, growx");
//
//		FloatValuePanel alphaPanel = getAlphaPanel(layer);
//		leftHandSettingsPanel.add(alphaPanel, "wrap, growx, hidemode 2");
//
//		leftHandSettingsPanel.add(getEmisivePanel(layer), "wrap, growx, hidemode 2");
//
//		if (ModelUtils.isFresnelColorLayerSupported(modelHandler.getModel().getFormatVersion())) {
//			leftHandSettingsPanel.add(getFresnelColorPanel(layer), "wrap, growx, hidemode 2");
//			leftHandSettingsPanel.add(getFresnelOpacityPanel(layer), "wrap, growx, hidemode 2");
//			leftHandSettingsPanel.add(getFresnelTeamColor(layer), "wrap, growx, hidemode 2");
//		}
//
//		return leftHandSettingsPanel;
//	}

//	private TextureValuePanel getTexturePanel(Layer layer) {
//		TextureValuePanel texturePanel = new TextureValuePanel(modelHandler, "Texture");
//		texturePanel.reloadNewValue(layer.getTextureId(), (IntAnimFlag) layer.find(MdlUtils.TOKEN_TEXTURE_ID), layer, MdlUtils.TOKEN_TEXTURE_ID, layer::setTextureId);
//		return texturePanel;
//	}
//
//	private FloatValuePanel getAlphaPanel(Layer layer) {
//		FloatValuePanel alphaPanel = new FloatValuePanel(modelHandler, MdlUtils.TOKEN_ALPHA);
//		alphaPanel.reloadNewValue((float) layer.getStaticAlpha(), (FloatAnimFlag) layer.find(MdlUtils.TOKEN_ALPHA), layer, MdlUtils.TOKEN_ALPHA, layer::setStaticAlpha);
//		return alphaPanel;
//	}
//
//	private FloatValuePanel getFresnelTeamColor(Layer layer) {
//		FloatValuePanel fresnelTeamColor = new FloatValuePanel(modelHandler, "Fresnel Team Color");
//		fresnelTeamColor.reloadNewValue((float) layer.getFresnelTeamColor(), (FloatAnimFlag) layer.find(MdlUtils.TOKEN_FRESNEL_TEAM_COLOR), layer, MdlUtils.TOKEN_FRESNEL_TEAM_COLOR, layer::setFresnelTeamColor);
//		return fresnelTeamColor;
//	}
//
//	private FloatValuePanel getFresnelOpacityPanel(Layer layer) {
//		FloatValuePanel fresnelOpacityPanel = new FloatValuePanel(modelHandler, "Fresnel Opacity");
//		fresnelOpacityPanel.reloadNewValue((float) layer.getFresnelOpacity(), (FloatAnimFlag) layer.find(MdlUtils.TOKEN_FRESNEL_OPACITY), layer, MdlUtils.TOKEN_FRESNEL_OPACITY, layer::setFresnelOpacity);
//		return fresnelOpacityPanel;
//	}
//
//	private ColorValuePanel getFresnelColorPanel(Layer layer) {
//		ColorValuePanel fresnelColorPanel = new ColorValuePanel(modelHandler, "Fresnel Color");
//		fresnelColorPanel.reloadNewValue(layer.getFresnelColor(), (Vec3AnimFlag) layer.find(MdlUtils.TOKEN_FRESNEL_COLOR), layer, MdlUtils.TOKEN_FRESNEL_COLOR, layer::setFresnelColor);
//		return fresnelColorPanel;
//	}
//
//	private FloatValuePanel getEmisivePanel(Layer layer) {
//		FloatValuePanel emissiveGainPanel = new FloatValuePanel(modelHandler, "Emissive Gain");
//		emissiveGainPanel.reloadNewValue((float) layer.getEmissive(), (FloatAnimFlag) layer.find(MdlUtils.TOKEN_EMISSIVE_GAIN), layer, MdlUtils.TOKEN_EMISSIVE_GAIN, layer::setEmissive);
//		return emissiveGainPanel;
//	}
//
//	private JPanel getTopSettingsPanel(Layer layer) {
//		JPanel topSettingsPanel = new JPanel(new MigLayout("ins 0"));
//
//		topSettingsPanel.add(new JLabel("Filter Mode:"));
//		JComboBox<MdlxLayer.FilterMode> filterModeDropdown = new JComboBox<>(MdlxLayer.FilterMode.values());
//		filterModeDropdown.setSelectedItem(layer.getFilterMode());
//		filterModeDropdown.addItemListener(e -> filterModeDropdownListener(e, layer));
//		topSettingsPanel.add(filterModeDropdown, "wrap, growx");
//
//		topSettingsPanel.add(new JLabel("TVertex Anim:"));
//		JButton tVertexAnimButton = new JButton("Choose TVertex Anim");
//		tVertexAnimButton.setText(layer.getTextureAnim() == null ? "None" : layer.getTextureAnim().getFlagNames());
//		topSettingsPanel.add(tVertexAnimButton, "wrap, growx");
//
//		topSettingsPanel.add(new JLabel("CoordID:"));
//		IntEditorJSpinner coordIdSpinner = new IntEditorJSpinner(layer.getCoordId(), Integer.MIN_VALUE, (i) -> setCoordId(layer, i));
//		coordIdSpinner.addIntEditingStoppedListener((i) -> setCoordId(layer, i));
//		topSettingsPanel.add(coordIdSpinner, "wrap, growx");
//		return topSettingsPanel;
//	}

	private void filterModeDropdownListener(ItemEvent e, Layer layer) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			FilterMode selectedItem = (FilterMode) e.getItem();
			undoManager.pushAction(new SetLayerFilterModeAction(layer, selectedItem, changeListener).redo());
		}
	}

	private void setCoordId(Layer layer, int value) {
		layer.setCoordId(value);
	}
}
