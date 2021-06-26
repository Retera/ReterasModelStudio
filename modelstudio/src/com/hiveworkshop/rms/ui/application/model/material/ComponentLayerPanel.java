package com.hiveworkshop.rms.ui.application.model.material;

import com.hiveworkshop.rms.editor.actions.model.material.ChangeLayerStaticTextureAction;
import com.hiveworkshop.rms.editor.actions.model.material.RemoveLayerAction;
import com.hiveworkshop.rms.editor.actions.model.material.RemoveMaterialAction;
import com.hiveworkshop.rms.editor.actions.model.material.SetLayerFilterModeAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.FloatAnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.IntAnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.filesystem.sources.DataSource;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.parsers.mdlx.MdlxLayer.FilterMode;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;
import com.hiveworkshop.rms.ui.application.model.editors.ColorValuePanel;
import com.hiveworkshop.rms.ui.application.model.editors.ComponentEditorJSpinner;
import com.hiveworkshop.rms.ui.application.model.editors.FloatValuePanel;
import com.hiveworkshop.rms.ui.application.model.editors.TextureValuePanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.util.ZoomableImagePreviewPanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class ComponentLayerPanel extends JPanel {
	public static final String[] REFORGED_LAYER_DEFINITIONS
			= {"Diffuse", "Vertex", "ORM", "Emissive", "Team Color", "Reflections"};

	private final Layer layer;
	private final Material material;

	private JComboBox<FilterMode> filterModeDropdown;
	private ComponentEditorJSpinner coordIdSpinner;
	private JComboBox<String> textureChooser;
	private JButton tVertexAnimButton;
	private JPanel titlePanel;

	private final LayerFlagsPanel layerFlagsPanel;
	private final JPanel texturePreviewPanel;
	private TextureValuePanel texturePanel;
	private FloatValuePanel alphaPanel;
	private FloatValuePanel emissiveGainPanel;
	private FloatValuePanel fresnelOpacityPanel;
	private FloatValuePanel fresnelTeamColor;
	private ColorValuePanel fresnelColorPanel;

	private final UndoManager undoManager;
	private final ModelHandler modelHandler;
	private final ModelStructureChangeListener changeListener;
	private boolean listenersEnabled = true;
	DefaultListModel<Bitmap> bitmapListModel;

	public ComponentLayerPanel(Layer layer, Material material, ModelHandler modelHandler, int i, ModelStructureChangeListener changeListener) {
		setLayout(new MigLayout("fill", "[][][grow]", "[][fill][fill]"));
		setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		this.layer = layer;
//		setOpaque(true);
//		setBackground(Color.cyan);

		this.modelHandler = modelHandler;
		this.undoManager = modelHandler.getUndoManager();
		this.changeListener = changeListener;
		this.material = material;
		titlePanel = new JPanel();
		titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.X_AXIS));
		add(titlePanel, "growx, span 3, wrap");

		JLabel layerLabel = new JLabel("Layer");
		titlePanel.add(layerLabel);
		JButton layerDeleteButton = getDeleteButton(this.layer);

		if (Material.SHADER_HD_DEFAULT_UNIT.equals(material.getShaderString())) {
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
		fillLeftHandPanel(leftHandSettingsPanel);
		add(leftHandSettingsPanel);


		layerFlagsPanel = new LayerFlagsPanel();
		layerFlagsPanel.setBorder(BorderFactory.createTitledBorder("Flags"));
		add(layerFlagsPanel);

		texturePreviewPanel = new JPanel();
		texturePreviewPanel.setLayout(new BorderLayout());
		add(texturePreviewPanel, "growx");
		setLayer();
	}

	private void loadBitmapPreview(final Bitmap defaultTexture, EditableModel model) {
		if (defaultTexture != null) {
			DataSource workingDirectory = model.getWrappedDataSource();
			texturePreviewPanel.removeAll();
			try {
				BufferedImage texture = BLPHandler.getImage(defaultTexture, workingDirectory);
				texturePreviewPanel.add(new ZoomableImagePreviewPanel(texture));
			} catch (final Exception exc) {
				BufferedImage image = new BufferedImage(512, 512, BufferedImage.TYPE_INT_ARGB);
				Graphics2D g2 = image.createGraphics();
				g2.setColor(Color.RED);
				g2.drawString(exc.getClass().getSimpleName() + ": " + exc.getMessage(), 128, 128);
				texturePreviewPanel.add(new ZoomableImagePreviewPanel(image));
			}
//			texturePreviewPanel.setSize(64,64);
			texturePreviewPanel.revalidate();
			texturePreviewPanel.repaint();
		}
	}

	private void fillLeftHandPanel(JPanel leftHandSettingsPanel) {

		leftHandSettingsPanel.add(new JLabel("Filter Mode:"));
		filterModeDropdown = new JComboBox<>(FilterMode.values());
		filterModeDropdown.addActionListener(e -> filterModeDropdownListener());
		leftHandSettingsPanel.add(filterModeDropdown, "wrap, growx");

		leftHandSettingsPanel.add(new JLabel("Texture:"));
		textureChooser = new JComboBox<String>(getTextures());
		textureChooser.addActionListener(e -> chooseTexture());
		leftHandSettingsPanel.add(textureChooser, "wrap, growx");

		leftHandSettingsPanel.add(new JLabel("TVertex Anim:"));
		tVertexAnimButton = new JButton("Choose TVertex Anim");
		leftHandSettingsPanel.add(tVertexAnimButton, "wrap, growx");

		leftHandSettingsPanel.add(new JLabel("CoordID:"));
		coordIdSpinner = new ComponentEditorJSpinner(new SpinnerNumberModel(0, Integer.MIN_VALUE, Integer.MAX_VALUE, 1));
		coordIdSpinner.addEditingStoppedListener(this::setCoordId);
		leftHandSettingsPanel.add(coordIdSpinner, "wrap, growx");

		texturePanel = new TextureValuePanel(modelHandler, "Texture", undoManager, changeListener);
		leftHandSettingsPanel.add(texturePanel, "wrap, span 2, growx");

		alphaPanel = new FloatValuePanel(modelHandler, MdlUtils.TOKEN_ALPHA, undoManager, changeListener);
		leftHandSettingsPanel.add(alphaPanel, "wrap, span 2, growx, hidemode 2");

		emissiveGainPanel = new FloatValuePanel(modelHandler, "Emissive Gain", undoManager, changeListener);
		leftHandSettingsPanel.add(emissiveGainPanel, "wrap, span 2, growx, hidemode 2");

		fresnelColorPanel = new ColorValuePanel(modelHandler, "Fresnel Color", undoManager, changeListener);
		leftHandSettingsPanel.add(fresnelColorPanel, "wrap, span 2, growx, hidemode 2");

		fresnelOpacityPanel = new FloatValuePanel(modelHandler, "Fresnel Opacity", undoManager, changeListener);
		leftHandSettingsPanel.add(fresnelOpacityPanel, "wrap, span 2, growx, hidemode 2");

		fresnelTeamColor = new FloatValuePanel(modelHandler, "Fresnel Team Color", undoManager, changeListener);
		leftHandSettingsPanel.add(fresnelTeamColor, "wrap, span 2, growx, hidemode 2");
	}

	private String[] getTextures() {

		bitmapListModel = new DefaultListModel<>();
		List<String> bitmapNames = new ArrayList<>();

		for (final Bitmap bitmap : modelHandler.getModel().getTextures()) {
			bitmapNames.add(bitmap.getName());
			bitmapListModel.addElement(bitmap);
		}

		return bitmapNames.toArray(new String[0]);
	}

	private void filterModeDropdownListener() {
		if (listenersEnabled) {
			FilterMode selectedItem = (FilterMode) filterModeDropdown.getSelectedItem();
			undoManager.pushAction(new SetLayerFilterModeAction(layer, selectedItem, changeListener).redo());
		}
	}

	private void chooseTexture() {
		if (listenersEnabled) {
			Bitmap bitmap = bitmapListModel.get(textureChooser.getSelectedIndex());

			undoManager.pushAction(new ChangeLayerStaticTextureAction(bitmap, layer, changeListener).redo());
		}
	}

	public void setLayer() {
		int formatVersion = modelHandler.getModel().getFormatVersion();
		listenersEnabled = false;

		layerFlagsPanel.setLayer(layer);
		filterModeDropdown.setSelectedItem(layer.getFilterMode());

		loadBitmapPreview(layer.getTextureBitmap(), modelHandler.getModel());

		textureChooser.setModel(new DefaultComboBoxModel<>(getTextures()));
		textureChooser.setSelectedIndex(layer.getTextureId());

		coordIdSpinner.reloadNewValue(layer.getCoordId());
		tVertexAnimButton.setText(layer.getTextureAnim() == null ? "None" : layer.getTextureAnim().getFlagNames());

		texturePanel.reloadNewValue(layer.getTextureId(), (IntAnimFlag) layer.find(MdlUtils.TOKEN_TEXTURE_ID), layer, MdlUtils.TOKEN_TEXTURE_ID, layer::setTextureId);

		alphaPanel.reloadNewValue((float) layer.getStaticAlpha(), (FloatAnimFlag) layer.find(MdlUtils.TOKEN_ALPHA), layer, MdlUtils.TOKEN_ALPHA, layer::setStaticAlpha);

		boolean hdShader = Material.SHADER_HD_DEFAULT_UNIT.equals(material.getShaderString());
		emissiveGainPanel.setVisible(ModelUtils.isEmissiveLayerSupported(formatVersion) && hdShader);
		emissiveGainPanel.reloadNewValue((float) layer.getEmissive(), (FloatAnimFlag) layer.find(MdlUtils.TOKEN_EMISSIVE_GAIN), layer, MdlUtils.TOKEN_EMISSIVE_GAIN, layer::setEmissive);

		boolean fresnelColorLayerSupported = ModelUtils.isFresnelColorLayerSupported(formatVersion) && hdShader;

		fresnelColorPanel.setVisible(fresnelColorLayerSupported);
		fresnelColorPanel.reloadNewValue(layer.getFresnelColor(), (Vec3AnimFlag) layer.find(MdlUtils.TOKEN_FRESNEL_COLOR), layer, MdlUtils.TOKEN_FRESNEL_COLOR, layer::setFresnelColor);

		fresnelOpacityPanel.setVisible(fresnelColorLayerSupported);
		fresnelOpacityPanel.reloadNewValue((float) layer.getFresnelOpacity(), (FloatAnimFlag) layer.find(MdlUtils.TOKEN_FRESNEL_OPACITY), layer, MdlUtils.TOKEN_FRESNEL_OPACITY, layer::setFresnelOpacity);

		fresnelTeamColor.setVisible(fresnelColorLayerSupported);
		fresnelTeamColor.reloadNewValue((float) layer.getFresnelTeamColor(), (FloatAnimFlag) layer.find(MdlUtils.TOKEN_FRESNEL_TEAM_COLOR), layer, MdlUtils.TOKEN_FRESNEL_TEAM_COLOR, layer::setFresnelTeamColor);

		listenersEnabled = true;
	}

	private JButton getDeleteButton(Layer layer) {
		JButton layerDeleteButton;
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
			List<Geoset> geosetList = modelHandler.getModel().getGeosets();
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
			undoManager.pushAction(new RemoveLayerAction(layer, material, changeListener).redo());
		}
	}

	private void removeMaterial() {
		RemoveMaterialAction removeMaterialAction = new RemoveMaterialAction(material, modelHandler.getModel(), changeListener);
		undoManager.pushAction(removeMaterialAction.redo());
	}
}
