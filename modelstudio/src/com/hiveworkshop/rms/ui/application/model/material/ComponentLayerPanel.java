package com.hiveworkshop.rms.ui.application.model.material;

import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Layer;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.filesystem.sources.DataSource;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.parsers.mdlx.MdlxLayer.FilterMode;
import com.hiveworkshop.rms.ui.application.actions.model.bitmap.SetBitmapPathAction;
import com.hiveworkshop.rms.ui.application.actions.model.material.SetLayerFilterModeAction;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;
import com.hiveworkshop.rms.ui.application.model.editors.ColorValuePanel;
import com.hiveworkshop.rms.ui.application.model.editors.ComponentEditorJSpinner;
import com.hiveworkshop.rms.ui.application.model.editors.FloatValuePanel;
import com.hiveworkshop.rms.ui.util.ZoomableImagePreviewPanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class ComponentLayerPanel extends JPanel {
	private JComboBox<FilterMode> filterModeDropdown;
	private JComboBox<String> textureChooser;
	private LayerFlagsPanel layerFlagsPanel;
	private JPanel texturePreviewPanel;
	private JButton tVertexAnimButton;
	private ComponentEditorJSpinner coordIdSpinner;
	private FloatValuePanel alphaPanel;
	private FloatValuePanel emissiveGainPanel;
	private Layer layer;
	private FloatValuePanel fresnelOpacityPanel;
	private FloatValuePanel fresnelTeamColor;
	private ColorValuePanel fresnelColorPanel;
	private UndoActionListener undoActionListener;
	private ModelStructureChangeListener modelStructureChangeListener;
	private boolean listenersEnabled = true;
	DefaultListModel<Bitmap> bitmapListModel;

	public ComponentLayerPanel(EditableModel model) {
		setLayout(new MigLayout("fill", "[][][grow]", "[fill][fill]"));

		final JPanel leftHandSettingsPanel = new JPanel();
		leftHandSettingsPanel.setLayout(new MigLayout());
		fillLeftHandPanel(model, leftHandSettingsPanel);
//		leftHandSettingsPanel.setOpaque(true);
//		leftHandSettingsPanel.setBackground(Color.cyan);
		add(leftHandSettingsPanel);


		layerFlagsPanel = new LayerFlagsPanel();
		layerFlagsPanel.setBorder(BorderFactory.createTitledBorder("Flags"));
//		layerFlagsPanel.setOpaque(true);
//		layerFlagsPanel.setBackground(Color.yellow);
		add(layerFlagsPanel);

		texturePreviewPanel = new JPanel();
//		texturePreviewPanel.setLayout(new MigLayout("fill"));
//		texturePreviewPanel.setOpaque(true);
//		texturePreviewPanel.setBackground(Color.cyan);
//		texturePreviewPanel.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		texturePreviewPanel.setLayout(new BorderLayout());
		add(texturePreviewPanel, "growx");
	}

	private void loadBitmapPreview(final Bitmap defaultTexture, EditableModel model) {
		if (defaultTexture != null) {
			final DataSource workingDirectory = model.getWrappedDataSource();
			texturePreviewPanel.removeAll();
			try {
				final BufferedImage texture = BLPHandler.getImage(defaultTexture, workingDirectory);
				texturePreviewPanel.add(new ZoomableImagePreviewPanel(texture));
			} catch (final Exception exc) {
				final BufferedImage image = new BufferedImage(512, 512, BufferedImage.TYPE_INT_ARGB);
				final Graphics2D g2 = image.createGraphics();
				g2.setColor(Color.RED);
				g2.drawString(exc.getClass().getSimpleName() + ": " + exc.getMessage(), 128, 128);
				texturePreviewPanel.add(new ZoomableImagePreviewPanel(image));
			}
//			texturePreviewPanel.setSize(64,64);
			texturePreviewPanel.revalidate();
			texturePreviewPanel.repaint();
		}
	}

	private void fillLeftHandPanel(EditableModel model, JPanel leftHandSettingsPanel) {
		leftHandSettingsPanel.add(new JLabel("Filter Mode:"));
		filterModeDropdown = new JComboBox<>(FilterMode.values());
		filterModeDropdown.addActionListener(e -> filterModeDropdownListener());
		leftHandSettingsPanel.add(filterModeDropdown, "wrap, growx");

		leftHandSettingsPanel.add(new JLabel("Texture:"));
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
			Bitmap bitmap = bitmapListModel.get(textureChooser.getSelectedIndex());

			final SetBitmapPathAction setBitmapPathAction = new SetBitmapPathAction(bitmap, layer.getTextureBitmap().getPath(), bitmap.getPath(), modelStructureChangeListener);

			layer.setTexture(bitmap);
			layer.setTextureId(textureChooser.getSelectedIndex());
			setBitmapPathAction.redo();
			undoActionListener.pushAction(setBitmapPathAction);
		}
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

		loadBitmapPreview(layer.getTextureBitmap(), model);

		textureChooser.setModel(new DefaultComboBoxModel<>(getTextures(model)));
		textureChooser.setSelectedIndex(layer.getTextureId());

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
