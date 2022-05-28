package com.hiveworkshop.rms.ui.application.model.material;

import com.hiveworkshop.rms.editor.actions.model.material.ChangeLayerOrderAction;
import com.hiveworkshop.rms.editor.actions.model.material.RemoveLayerAction;
import com.hiveworkshop.rms.editor.actions.model.material.RemoveMaterialAction;
import com.hiveworkshop.rms.editor.actions.model.material.SetLayerFilterModeAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.FloatAnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.IntAnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.editor.model.util.FilterMode;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.filesystem.sources.DataSource;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;
import com.hiveworkshop.rms.ui.application.model.editors.ColorValuePanel;
import com.hiveworkshop.rms.ui.application.model.editors.FloatValuePanel;
import com.hiveworkshop.rms.ui.application.model.editors.IntEditorJSpinner;
import com.hiveworkshop.rms.ui.application.model.editors.TextureValuePanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.util.ZoomableImagePreviewPanel;
import com.hiveworkshop.rms.util.TwiComboBox;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.image.BufferedImage;
import java.util.List;

public class ComponentLayerPanel extends JPanel {
	public static final String[] REFORGED_LAYER_DEFINITIONS
			= {"Diffuse", "Vertex", "ORM", "Emissive", "Team Color", "Reflections"};

	private final Layer layer;
	private final Material material;

//	private ComponentEditorJSpinner coordIdSpinner;

	private final UndoManager undoManager;
	private final ModelHandler modelHandler;
	private final ModelStructureChangeListener changeListener;

	public ComponentLayerPanel(Layer layer, Material material, ModelHandler modelHandler, int i) {
		setLayout(new MigLayout("fill", "[][][grow]", "[][fill]"));
		setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		this.layer = layer;

		this.modelHandler = modelHandler;
		this.undoManager = modelHandler.getUndoManager();
		this.changeListener = ModelStructureChangeListener.changeListener;
		this.material = material;

		add(getTitlePanel(material, i), "growx, spanx, wrap");
		add(getLeftHandPanel(), "spany");
		add(new LayerFlagsPanel(modelHandler, layer), "");
		add(getBitmapPreview(layer.getTextureBitmap(), modelHandler.getModel()), "growx, top");
	}

	private JPanel getTitlePanel(Material material, int i) {
		JPanel titlePanel = new JPanel();
		titlePanel.setLayout(new MigLayout("fill", "[left, grow][right]0[right]10[right]", "[]"));

		JLabel layerLabel = new JLabel("Layer");
		titlePanel.add(layerLabel);
		JButton layerDeleteButton = getDeleteButton(this.layer);

		if (Material.SHADER_HD_DEFAULT_UNIT.equals(material.getShaderString())) {
			final String reforgedDefinition;
			if (i < REFORGED_LAYER_DEFINITIONS.length) {
				reforgedDefinition = REFORGED_LAYER_DEFINITIONS[i];
			} else {
				titlePanel.add(layerDeleteButton, "spanx, right");
				reforgedDefinition = "Unknown";
			}
			layerLabel.setText(reforgedDefinition + " Layer");
			layerLabel.setFont(layerLabel.getFont().deriveFont(Font.BOLD));
		} else {
			titlePanel.add(getMoveLayerButton("Move Up", true));
			titlePanel.add(getMoveLayerButton("Move Down", false));
			titlePanel.add(layerDeleteButton);
			layerLabel.setText("Layer " + (i + 1));
			layerLabel.setFont(layerLabel.getFont().deriveFont(Font.PLAIN));
		}
		return titlePanel;
	}

	private JButton getMoveLayerButton(String moveText, boolean moveUp) {
		JButton button = new JButton(moveText);
		List<Layer> layers = material.getLayers();
		int index = layers.indexOf(layer);
		button.addActionListener(e -> {
			modelHandler.getUndoManager().pushAction(new ChangeLayerOrderAction(material, layer, moveUp, ModelStructureChangeListener.changeListener).redo());
		});
		if (index <= 0 && moveUp || index >= layers.size() - 1 && !moveUp) {
			button.setEnabled(false);
		}
		return button;
	}

	private JPanel getBitmapPreview(Bitmap defaultTexture, EditableModel model) {
		JPanel texturePreviewPanel = new JPanel();
		texturePreviewPanel.setLayout(new MigLayout("gap 0, ins 0, fill", "[grow]", "[grow]"));

		if (defaultTexture != null) {
			DataSource workingDirectory = model.getWrappedDataSource();
			try {
				BufferedImage texture = BLPHandler.getImage(defaultTexture, workingDirectory);
				texturePreviewPanel.add(new ZoomableImagePreviewPanel(texture, true), "growx, growy");
			} catch (final Exception exc) {
				BufferedImage image = new BufferedImage(512, 512, BufferedImage.TYPE_INT_ARGB);
				Graphics2D g2 = image.createGraphics();
				g2.setColor(Color.RED);
				g2.drawString(exc.getClass().getSimpleName() + ": " + exc.getMessage(), 128, 128);
				texturePreviewPanel.add(new ZoomableImagePreviewPanel(image, true), "growx, growy");
			}
			texturePreviewPanel.revalidate();
			texturePreviewPanel.repaint();
		}
		return texturePreviewPanel;
	}

	private JPanel getLeftHandPanel() {
		JPanel leftHandSettingsPanel = new JPanel(new MigLayout());
		JPanel topSettingsPanel = getTopSettingsPanel();

		leftHandSettingsPanel.add(topSettingsPanel, "wrap, growx");

		TextureValuePanel texturePanel = new TextureValuePanel(modelHandler, "Texture");
		texturePanel.reloadNewValue(layer.getTextureId(), (IntAnimFlag) layer.find(MdlUtils.TOKEN_TEXTURE_ID), layer, MdlUtils.TOKEN_TEXTURE_ID, layer::setTextureId);
		leftHandSettingsPanel.add(texturePanel, "wrap, growx");

		FloatValuePanel alphaPanel = new FloatValuePanel(modelHandler, MdlUtils.TOKEN_ALPHA);
		alphaPanel.reloadNewValue((float) layer.getStaticAlpha(), (FloatAnimFlag) layer.find(MdlUtils.TOKEN_ALPHA), layer, MdlUtils.TOKEN_ALPHA, layer::setStaticAlpha);
		leftHandSettingsPanel.add(alphaPanel, "wrap, growx, hidemode 2");

		boolean hdShader = Material.SHADER_HD_DEFAULT_UNIT.equals(material.getShaderString());
		if (hdShader && ModelUtils.isEmissiveLayerSupported(modelHandler.getModel().getFormatVersion())) {
			addHD900Panels(leftHandSettingsPanel);
		}
		if (hdShader && ModelUtils.isFresnelColorLayerSupported(modelHandler.getModel().getFormatVersion())) {
			addHD1000Panels(leftHandSettingsPanel);
		}

		return leftHandSettingsPanel;
	}

	private JPanel getTopSettingsPanel() {
		JPanel topSettingsPanel = new JPanel(new MigLayout("ins 0"));

		topSettingsPanel.add(new JLabel("Filter Mode:"));
		TwiComboBox<FilterMode> filterModeDropdown = new TwiComboBox<>(FilterMode.values(), FilterMode.TRANSPARENT);
		filterModeDropdown.setSelectedItem(layer.getFilterMode());
		filterModeDropdown.addOnSelectItemListener(this::setFilterMode);
		topSettingsPanel.add(filterModeDropdown, "wrap, growx");

		topSettingsPanel.add(new JLabel("TVertex Anim:"));
		JButton tVertexAnimButton = new JButton("Choose TVertex Anim");
		tVertexAnimButton.setText(layer.getTextureAnim() == null ? "None" : layer.getTextureAnim().getFlagNames());
		topSettingsPanel.add(tVertexAnimButton, "wrap, growx");

		topSettingsPanel.add(new JLabel("CoordID:"));
		IntEditorJSpinner coordIdSpinner = new IntEditorJSpinner(layer.getCoordId(), Integer.MIN_VALUE, this::setCoordId);
		topSettingsPanel.add(coordIdSpinner, "wrap, growx");
		return topSettingsPanel;
	}

	private void addHD1000Panels(JPanel leftHandSettingsPanel) {

		ColorValuePanel fresnelColorPanel = new ColorValuePanel(modelHandler, "Fresnel Color");
		fresnelColorPanel.reloadNewValue(layer.getFresnelColor(), (Vec3AnimFlag) layer.find(MdlUtils.TOKEN_FRESNEL_COLOR), layer, MdlUtils.TOKEN_FRESNEL_COLOR, layer::setFresnelColor);
		leftHandSettingsPanel.add(fresnelColorPanel, "wrap, growx, hidemode 2");

		FloatValuePanel fresnelOpacityPanel = new FloatValuePanel(modelHandler, "Fresnel Opacity");
		fresnelOpacityPanel.reloadNewValue((float) layer.getFresnelOpacity(), (FloatAnimFlag) layer.find(MdlUtils.TOKEN_FRESNEL_OPACITY), layer, MdlUtils.TOKEN_FRESNEL_OPACITY, layer::setFresnelOpacity);
		leftHandSettingsPanel.add(fresnelOpacityPanel, "wrap, growx, hidemode 2");

		FloatValuePanel fresnelTeamColor = new FloatValuePanel(modelHandler, "Fresnel Team Color");
		fresnelTeamColor.reloadNewValue((float) layer.getFresnelTeamColor(), (FloatAnimFlag) layer.find(MdlUtils.TOKEN_FRESNEL_TEAM_COLOR), layer, MdlUtils.TOKEN_FRESNEL_TEAM_COLOR, layer::setFresnelTeamColor);
		leftHandSettingsPanel.add(fresnelTeamColor, "wrap, growx, hidemode 2");
	}

	private void addHD900Panels(JPanel leftHandSettingsPanel) {
		FloatValuePanel emissiveGainPanel = new FloatValuePanel(modelHandler, "Emissive Gain");
		emissiveGainPanel.reloadNewValue((float) layer.getEmissive(), (FloatAnimFlag) layer.find(MdlUtils.TOKEN_EMISSIVE_GAIN), layer, MdlUtils.TOKEN_EMISSIVE_GAIN, layer::setEmissive);
		leftHandSettingsPanel.add(emissiveGainPanel, "wrap, growx, hidemode 2");
	}

	private void filterModeDropdownListener(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			FilterMode selectedItem = (FilterMode) e.getItem();
			undoManager.pushAction(new SetLayerFilterModeAction(layer, selectedItem, changeListener).redo());
		}
	}
	private void setFilterMode(FilterMode filterMode) {
		if (filterMode != layer.getFilterMode()) {
			undoManager.pushAction(new SetLayerFilterModeAction(layer, filterMode, changeListener).redo());
		}
	}

	private JButton getDeleteButton(Layer layer) {
		JButton layerDeleteButton;
		layerDeleteButton = new JButton("Delete");
		layerDeleteButton.setBackground(Color.RED);
		layerDeleteButton.setForeground(Color.WHITE);
		layerDeleteButton.addActionListener(e -> removeLayer(layer));
		return layerDeleteButton;
	}

	private void setCoordId(int value) {
		layer.setCoordId(value);
//		coordIdSpinner.reloadNewValue(coordIdSpinner.getIntValue());
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
				undoManager.pushAction(new RemoveMaterialAction(material, modelHandler.getModel(), changeListener).redo());
			}
		} else {
			undoManager.pushAction(new RemoveLayerAction(layer, material, changeListener).redo());
		}
	}
}
