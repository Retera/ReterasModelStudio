package com.hiveworkshop.rms.ui.application.model.material;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.FloatAnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.IntAnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelViewManager;
import com.hiveworkshop.rms.filesystem.sources.DataSource;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.parsers.mdlx.MdlxLayer.FilterMode;
import com.hiveworkshop.rms.ui.application.actions.model.material.ChangeLayerStaticTextureAction;
import com.hiveworkshop.rms.ui.application.actions.model.material.RemoveLayerAction;
import com.hiveworkshop.rms.ui.application.actions.model.material.RemoveMaterialAction;
import com.hiveworkshop.rms.ui.application.actions.model.material.SetLayerFilterModeAction;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;
import com.hiveworkshop.rms.ui.application.model.editors.*;
import com.hiveworkshop.rms.ui.util.ZoomableImagePreviewPanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class ComponentLayerPanel extends JPanel {
	public static final String[] REFORGED_LAYER_DEFINITIONS = {"Diffuse", "Vertex", "ORM", "Emissive", "Team Color",
			"Reflections"};
	private JComboBox<FilterMode> filterModeDropdown;
	private JComboBox<String> textureChooser;
	private final LayerFlagsPanel layerFlagsPanel;
	private final JPanel texturePreviewPanel;
	private JButton tVertexAnimButton;
	private ComponentEditorJSpinner coordIdSpinner;
	private TextureValuePanel texturePanel;
	private FloatValuePanel alphaPanel;
	private FloatValuePanel emissiveGainPanel;
	private JPanel titlePanel;
	private Layer layer;
	private Material material;
	private ModelViewManager modelViewManager;

	private FloatValuePanel fresnelOpacityPanel;
	private FloatValuePanel fresnelTeamColor;
	private ColorValuePanel fresnelColorPanel;

	private UndoActionListener undoActionListener;
	private ModelStructureChangeListener modelStructureChangeListener;
	private boolean listenersEnabled = true;
	DefaultListModel<Bitmap> bitmapListModel;

	public ComponentLayerPanel(Material material, ModelViewManager modelViewManager, int i, boolean hdShader, UndoActionListener undoActionListener, ModelStructureChangeListener modelStructureChangeListener) {
		setLayout(new MigLayout("fill", "[][][grow]", "[][fill][fill]"));
		setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
//		setOpaque(true);
//		setBackground(Color.cyan);

		this.modelViewManager = modelViewManager;
		this.undoActionListener = undoActionListener;
		this.modelStructureChangeListener = modelStructureChangeListener;
		this.material = material;
		titlePanel = new JPanel();
		titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.X_AXIS));
		add(titlePanel, "growx, span 3, wrap");

		final JLabel layerLabel = new JLabel("Layer");
		titlePanel.add(layerLabel);
		final JButton layerDeleteButton = getDeleteButton(layer);

		if (hdShader) {
			final String reforgedDefinition;
			if (i < REFORGED_LAYER_DEFINITIONS.length) {
				reforgedDefinition = REFORGED_LAYER_DEFINITIONS[i];
			} else {
				titlePanel.add(Box.createHorizontalGlue());
				titlePanel.add(layerDeleteButton);
				reforgedDefinition = "Unknown";
			}
			layerLabel.setText(reforgedDefinition + " Layer");
			layerLabel.setFont(layerLabel.getFont().deriveFont(Font.BOLD));
		} else {
			titlePanel.add(Box.createHorizontalGlue());
			titlePanel.add(layerDeleteButton);
			layerLabel.setText("Layer " + (i + 1));
			layerLabel.setFont(layerLabel.getFont().deriveFont(Font.PLAIN));
		}

		final JPanel leftHandSettingsPanel = new JPanel(new MigLayout());
		fillLeftHandPanel(modelViewManager.getModel(), leftHandSettingsPanel);
		add(leftHandSettingsPanel);


		layerFlagsPanel = new LayerFlagsPanel();
		layerFlagsPanel.setBorder(BorderFactory.createTitledBorder("Flags"));
		add(layerFlagsPanel);

		texturePreviewPanel = new JPanel();
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
		textureChooser.addActionListener(e -> chooseTexture());
		leftHandSettingsPanel.add(textureChooser, "wrap, growx");

		leftHandSettingsPanel.add(new JLabel("TVertex Anim:"));
		tVertexAnimButton = new JButton("Choose TVertex Anim");
		leftHandSettingsPanel.add(tVertexAnimButton, "wrap, growx");

		leftHandSettingsPanel.add(new JLabel("CoordID:"));
		coordIdSpinner = new ComponentEditorJSpinner(new SpinnerNumberModel(0, Integer.MIN_VALUE, Integer.MAX_VALUE, 1));
		coordIdSpinner.addEditingStoppedListener(this::setCoordId);
		leftHandSettingsPanel.add(coordIdSpinner, "wrap, growx");

		texturePanel = new TextureValuePanel("Texture", undoActionListener, modelStructureChangeListener, model);
		texturePanel.setKeyframeHelper(new TimelineKeyNamer(model));
		leftHandSettingsPanel.add(texturePanel, "wrap, span 2, growx");

		alphaPanel = new FloatValuePanel("Alpha", undoActionListener, modelStructureChangeListener);
		alphaPanel.setKeyframeHelper(new TimelineKeyNamer(model));
		leftHandSettingsPanel.add(alphaPanel, "wrap, span 2, growx, hidemode 2");

		emissiveGainPanel = new FloatValuePanel("Emissive Gain", undoActionListener, modelStructureChangeListener);
		emissiveGainPanel.setKeyframeHelper(new TimelineKeyNamer(model));
		leftHandSettingsPanel.add(emissiveGainPanel, "wrap, span 2, growx, hidemode 2");

		fresnelColorPanel = new ColorValuePanel("Fresnel Color", undoActionListener, modelStructureChangeListener);
		fresnelColorPanel.setKeyframeHelper(new TimelineKeyNamer(model));
		leftHandSettingsPanel.add(fresnelColorPanel, "wrap, span 2, growx, hidemode 2");

		fresnelOpacityPanel = new FloatValuePanel("Fresnel Opacity", undoActionListener, modelStructureChangeListener);
		fresnelOpacityPanel.setKeyframeHelper(new TimelineKeyNamer(model));
		leftHandSettingsPanel.add(fresnelOpacityPanel, "wrap, span 2, growx, hidemode 2");

		fresnelTeamColor = new FloatValuePanel("Fresnel Team Color", undoActionListener, modelStructureChangeListener);
		fresnelTeamColor.setKeyframeHelper(new TimelineKeyNamer(model));
		leftHandSettingsPanel.add(fresnelTeamColor, "wrap, span 2, growx, hidemode 2");
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

	private void chooseTexture() {
		if (listenersEnabled) {
			Bitmap bitmap = bitmapListModel.get(textureChooser.getSelectedIndex());

			ChangeLayerStaticTextureAction changeLayerStaticTextureAction = new ChangeLayerStaticTextureAction(bitmap, layer, modelStructureChangeListener);
			changeLayerStaticTextureAction.redo();
			undoActionListener.pushAction(changeLayerStaticTextureAction);
		}
	}

	public void setLayer(final EditableModel model, final Layer layer, final int formatVersion,
	                     final boolean hdShader, final UndoActionListener undoActionListener) {
		listenersEnabled = false;
		this.layer = layer;
//		this.undoActionListener = undoActionListener;
//		this.modelStructureChangeListener = modelStructureChangeListener;

		layerFlagsPanel.setLayer(layer);
		filterModeDropdown.setSelectedItem(layer.getFilterMode());

		loadBitmapPreview(layer.getTextureBitmap(), model);

		textureChooser.setModel(new DefaultComboBoxModel<>(getTextures(model)));
		textureChooser.setSelectedIndex(layer.getTextureId());

		coordIdSpinner.reloadNewValue(layer.getCoordId());
		tVertexAnimButton.setText(layer.getTextureAnim() == null ? "None" : layer.getTextureAnim().getFlagNames());

		texturePanel.reloadNewValue(layer.getTextureId(), (IntAnimFlag) layer.find("TextureID"), layer, "TextureID", layer::setTextureId);

		alphaPanel.reloadNewValue((float) layer.getStaticAlpha(), (FloatAnimFlag) layer.find("Alpha"), layer, "Alpha", layer::setStaticAlpha);


		emissiveGainPanel.setVisible(ModelUtils.isEmissiveLayerSupported(formatVersion) && hdShader);
		emissiveGainPanel.reloadNewValue((float) layer.getEmissive(), (FloatAnimFlag) layer.find("EmissiveGain"), layer, "EmissiveGain", layer::setEmissive);

		final boolean fresnelColorLayerSupported = ModelUtils.isFresnelColorLayerSupported(formatVersion) && hdShader;

		fresnelColorPanel.setVisible(fresnelColorLayerSupported);

		fresnelColorPanel.reloadNewValue(layer.getFresnelColor(), (Vec3AnimFlag) layer.find("FresnelColor"), layer, "FresnelColor", layer::setFresnelColor);

		fresnelOpacityPanel.setVisible(fresnelColorLayerSupported);
		fresnelOpacityPanel.reloadNewValue((float) layer.getFresnelOpacity(), (FloatAnimFlag) layer.find("FresnelOpacity"), layer, "FresnelOpacity", layer::setFresnelOpacity);

		fresnelTeamColor.setVisible(fresnelColorLayerSupported);
		fresnelTeamColor.reloadNewValue((float) layer.getFresnelTeamColor(), (FloatAnimFlag) layer.find("FresnelTeamColor"), layer, "FresnelTeamColor", layer::setFresnelTeamColor);

		listenersEnabled = true;
	}

	private JButton getDeleteButton(Layer layer) {
		final JButton layerDeleteButton;
		layerDeleteButton = new JButton("Delete");
		layerDeleteButton.setBackground(Color.RED);
		layerDeleteButton.setForeground(Color.WHITE);
		layerDeleteButton.addActionListener(e -> removeLayer(layer));
		return layerDeleteButton;
	}

	private void setCoordId() {
		layer.setCoordId(coordIdSpinner.getIntValue());
		coordIdSpinner.reloadNewValue(coordIdSpinner.getIntValue());
	}

	private void removeLayer(Layer layer) {
		if (material.getLayers().size() <= 1) {
			List<Geoset> geosetList = modelViewManager.getModel().getGeosets();
			int numUses = 0;
			for (Geoset geoset : geosetList) {
				if (geoset.getMaterial() == material) {
					// Checks if this instance of the material is used.
					// This lets the user remove the material even if used clones exists
					numUses++;
				}
			}

			if (numUses > 0) {
				JOptionPane.showMessageDialog(this, "Cannot delete material as it is being used by " + numUses + " geosets.");
			} else {
				removeMaterial();
			}
		} else {
			RemoveLayerAction removeLayerAction = new RemoveLayerAction(layer, material, modelStructureChangeListener);
			undoActionListener.pushAction(removeLayerAction);
			removeLayerAction.redo();
		}
	}

	private void removeMaterial() {
		RemoveMaterialAction removeMaterialAction = new RemoveMaterialAction(material, modelViewManager, modelStructureChangeListener);
		undoActionListener.pushAction(removeMaterialAction);
		removeMaterialAction.redo();
	}
}
