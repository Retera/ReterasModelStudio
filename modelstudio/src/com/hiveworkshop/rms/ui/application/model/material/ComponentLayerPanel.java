package com.hiveworkshop.rms.ui.application.model.material;

import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Layer;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.parsers.mdlx.MdlxLayer.FilterMode;
import com.hiveworkshop.rms.ui.application.actions.model.bitmap.SetBitmapPathAction;
import com.hiveworkshop.rms.ui.application.actions.model.material.SetLayerFilterModeAction;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;
import com.hiveworkshop.rms.ui.application.model.editors.ColorValuePanel;
import com.hiveworkshop.rms.ui.application.model.editors.ComponentEditorJSpinner;
import com.hiveworkshop.rms.ui.application.model.editors.FloatValuePanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class ComponentLayerPanel extends JPanel {
	private final JComboBox<FilterMode> filterModeDropdown;
	private final JComboBox<String> textureChooser;
	private final LayerFlagsPanel layerFlagsPanel;
	private final JButton tVertexAnimButton;
	private final ComponentEditorJSpinner coordIdSpinner;
	private final FloatValuePanel alphaPanel;
	private final FloatValuePanel emissiveGainPanel;
	private Layer layer;
	private final FloatValuePanel fresnelOpacityPanel;
	private final FloatValuePanel fresnelTeamColor;
	private final ColorValuePanel fresnelColorPanel;
	private UndoActionListener undoActionListener;
	private ModelStructureChangeListener modelStructureChangeListener;
	private boolean listenersEnabled = true;
	DefaultListModel<Bitmap> bitmapListModel;

	public ComponentLayerPanel(EditableModel model) {
		setLayout(new MigLayout("fill", "", "[fill][fill]"));
		final JPanel leftHandSettingsPanel = new JPanel();

		layerFlagsPanel = new LayerFlagsPanel();
		layerFlagsPanel.setBorder(BorderFactory.createTitledBorder("Flags"));
		add(leftHandSettingsPanel);
		add(layerFlagsPanel);

		leftHandSettingsPanel.setLayout(new MigLayout());

		leftHandSettingsPanel.add(new JLabel("Filter Mode:"));
		filterModeDropdown = new JComboBox<>(FilterMode.values());
		filterModeDropdown.addActionListener(e -> filterModeDropdownListener());
		leftHandSettingsPanel.add(filterModeDropdown, "wrap, growx");

		leftHandSettingsPanel.add(new JLabel("Texture:"));
//		List<String> textures = new ArrayList<>();
//		textures.add("Choose Texture");
		textureChooser = new JComboBox<String>(getTextures(model));
		textureChooser.addActionListener(e -> chooseTexture(model));
		leftHandSettingsPanel.add(textureChooser, "wrap, growx");

		coordIdSpinner = new ComponentEditorJSpinner(new SpinnerNumberModel(0, Integer.MIN_VALUE, Integer.MAX_VALUE, 1));
		leftHandSettingsPanel.add(new JLabel("TVertex Anim:"));

		tVertexAnimButton = new JButton("Choose TVertex Anim");
		leftHandSettingsPanel.add(tVertexAnimButton, "wrap, growx");

		leftHandSettingsPanel.add(new JLabel("CoordID:"));
		leftHandSettingsPanel.add(coordIdSpinner, "wrap, growx");

		alphaPanel = new FloatValuePanel("Alpha");
		leftHandSettingsPanel.add(alphaPanel, "wrap, span 2");

		emissiveGainPanel = new FloatValuePanel("Emissive Gain");
		leftHandSettingsPanel.add(emissiveGainPanel, "wrap, span 2, hidemode 2");

		fresnelColorPanel = new ColorValuePanel("Fresnel Color");
		leftHandSettingsPanel.add(fresnelColorPanel, "wrap, span 2, hidemode 2");

		fresnelOpacityPanel = new FloatValuePanel("Fresnel Opacity");
		leftHandSettingsPanel.add(fresnelOpacityPanel, "wrap, span 2, hidemode 2");

		fresnelTeamColor = new FloatValuePanel("Fresnel Team Color");
		leftHandSettingsPanel.add(fresnelTeamColor, "wrap, span 2, hidemode 2");

	}

	private String[] getTextures(EditableModel model) {

		bitmapListModel = new DefaultListModel<>();
		List<String> bitmapNames = new ArrayList<>();

		for (final Bitmap bitmap : model.getTextures()) {
			System.out.println(bitmap.getName());
			System.out.println(bitmap.getReplaceableId());
			String path = bitmap.getPath();
			System.out.println(path);
			bitmapNames.add(bitmap.getName());
			bitmapListModel.addElement(bitmap);
		}

		return bitmapNames.toArray(new String[0]);
	}

	private void filterModeDropdownListener() {
		if (listenersEnabled) {
			final SetLayerFilterModeAction setLayerFilterModeAction = new SetLayerFilterModeAction(layer, layer.getFilterMode(), ((FilterMode) filterModeDropdown.getSelectedItem()), modelStructureChangeListener);
			setLayerFilterModeAction.redo();
			undoActionListener.pushAction(setLayerFilterModeAction);
		}
	}

	private void chooseTexture(EditableModel model) {
		if (listenersEnabled) {
			System.out.println(textureChooser.getSelectedIndex());
			Bitmap bitmap = bitmapListModel.get(textureChooser.getSelectedIndex());
//			model.add(bitmap);
			final SetBitmapPathAction setBitmapPathAction = new SetBitmapPathAction(bitmap, layer.getTextureBitmap().getPath(), bitmap.getPath(), modelStructureChangeListener);
			layer.setTexture(bitmap);
			layer.setTextureId(textureChooser.getSelectedIndex());
			setBitmapPathAction.redo();
			undoActionListener.pushAction(setBitmapPathAction);
		}

		// Custom textures can fail to load.
//		if (layer.firstTexture() != null) {
//			final BufferedImage image = BLPHandler.getImage(layer.firstTexture(), workingDirectory);
//			if (image != null) {
//				textureButton.setIcon(new ImageIcon(IconUtils.worldEditStyleIcon(image)));
//			}
//			textureButton.setText(layer.getName());
//		}
		System.out.println("choose Texture");
	}

	public void setLayer(final EditableModel model, final Layer layer, final int formatVersion,
	                     final boolean hdShader, final UndoActionListener undoActionListener,
	                     final ModelStructureChangeListener modelStructureChangeListener) {
		listenersEnabled = false;
		this.layer = layer;
		this.undoActionListener = undoActionListener;
		this.modelStructureChangeListener = modelStructureChangeListener;
		layerFlagsPanel.setLayer(layer);
		filterModeDropdown.setSelectedItem(layer.getFilterMode());

		textureChooser.setModel(new DefaultComboBoxModel<>(getTextures(model)));
		System.out.println(layer.getTextureId() + " of " + textureChooser.getItemCount());
		textureChooser.setSelectedIndex(layer.getTextureId());

		// Custom textures can fail to load.
//		if (layer.firstTexture() != null) {
//			final BufferedImage image = BLPHandler.getImage(layer.firstTexture(), workingDirectory);
//			if (image != null) {
//				textureButton.setIcon(new ImageIcon(IconUtils.worldEditStyleIcon(image)));
//			}
//			textureButton.setText(layer.getName());
//		}

		coordIdSpinner.reloadNewValue(layer.getCoordId());
		tVertexAnimButton.setText(layer.getTextureAnim() == null ? "None" : layer.getTextureAnim().toString());
		alphaPanel.reloadNewValue((float) layer.getStaticAlpha(), layer.find("Alpha"));

		emissiveGainPanel.setVisible(ModelUtils.isEmissiveLayerSupported(formatVersion) && hdShader);
		emissiveGainPanel.reloadNewValue((float) layer.getEmissive(), layer.find("EmissiveGain"));

		final boolean fresnelColorLayerSupported = ModelUtils.isFresnelColorLayerSupported(formatVersion) && hdShader;

		fresnelColorPanel.setVisible(fresnelColorLayerSupported);
		fresnelColorPanel.reloadNewValue(layer.getFresnelColor(), layer.find("FresnelColor"));

		fresnelOpacityPanel.setVisible(fresnelColorLayerSupported);
		fresnelOpacityPanel.reloadNewValue((float) layer.getFresnelOpacity(), layer.find("FresnelOpacity"));

		fresnelTeamColor.setVisible(fresnelColorLayerSupported);
		fresnelTeamColor.reloadNewValue((float) layer.getFresnelTeamColor(), layer.find("FresnelTeamColor"));

		listenersEnabled = true;
	}
}
