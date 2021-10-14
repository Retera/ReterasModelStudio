package com.hiveworkshop.rms.ui.application.model.material;

import com.hiveworkshop.rms.editor.actions.model.material.SetLayerFilterModeAction;
import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Layer;
import com.hiveworkshop.rms.editor.model.animflag.FloatAnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.IntAnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.editor.model.util.FilterMode;
import com.hiveworkshop.rms.editor.model.util.HD_Material_Layer;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.filesystem.sources.DataSource;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.model.ComponentPanel;
import com.hiveworkshop.rms.ui.application.model.editors.ColorValuePanel;
import com.hiveworkshop.rms.ui.application.model.editors.FloatValuePanel;
import com.hiveworkshop.rms.ui.application.model.editors.IntEditorJSpinner;
import com.hiveworkshop.rms.ui.application.model.editors.TextureValuePanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.util.ZoomableImagePreviewPanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.image.BufferedImage;

public class ComponentHDLayer extends ComponentPanel<Layer> {
	String valuePanelConstraints = "wrap, growx, hidemode 2";

	TextureValuePanel texturePanel;
	FloatValuePanel alphaPanel;
	FloatValuePanel emissiveGainPanel;
	ColorValuePanel fresnelColorPanel;
	FloatValuePanel fresnelOpacityPanel;
	FloatValuePanel fresnelTeamColor;
	JPanel texturePreviewPanel;
	JPanel layerFlagsPanel;

	JComboBox<FilterMode> filterModeDropdown;
	JButton tVertexAnimButton;
	IntEditorJSpinner coordIdSpinner;


	public ComponentHDLayer(ModelHandler modelHandler, HD_Material_Layer ld) {
		super(modelHandler);
		setLayout(new MigLayout("fill", "[][][grow]", "[fill]"));
		Border lineBorder = BorderFactory.createLineBorder(Color.LIGHT_GRAY);
		setBorder(BorderFactory.createTitledBorder(lineBorder, ld.getLayerName() + " Layer"));

		add(getLeftHandPanel(), "spany");
		layerFlagsPanel = new JPanel(new MigLayout("ins 0, gap 0"));
		add(layerFlagsPanel, "");
		texturePreviewPanel = new JPanel(new MigLayout("gap 0, ins 0, fill", "[grow]", "[grow]"));
		add(texturePreviewPanel, "growx, top");
	}

	@Override
	public void setSelectedItem(Layer itemToSelect) {
		selectedItem = itemToSelect;
//		System.out.println("Reloading panel values");
		texturePanel.reloadNewValue(itemToSelect.getTextureId(), (IntAnimFlag) itemToSelect.find(MdlUtils.TOKEN_TEXTURE_ID), itemToSelect, MdlUtils.TOKEN_TEXTURE_ID, itemToSelect::setTextureId);
		alphaPanel.reloadNewValue((float) itemToSelect.getStaticAlpha(), (FloatAnimFlag) itemToSelect.find(MdlUtils.TOKEN_ALPHA), itemToSelect, MdlUtils.TOKEN_ALPHA, itemToSelect::setStaticAlpha);
		fresnelTeamColor.reloadNewValue((float) itemToSelect.getFresnelTeamColor(), (FloatAnimFlag) itemToSelect.find(MdlUtils.TOKEN_FRESNEL_TEAM_COLOR), itemToSelect, MdlUtils.TOKEN_FRESNEL_TEAM_COLOR, itemToSelect::setFresnelTeamColor);
		fresnelOpacityPanel.reloadNewValue((float) itemToSelect.getFresnelOpacity(), (FloatAnimFlag) itemToSelect.find(MdlUtils.TOKEN_FRESNEL_OPACITY), itemToSelect, MdlUtils.TOKEN_FRESNEL_OPACITY, itemToSelect::setFresnelOpacity);
		fresnelColorPanel.reloadNewValue(itemToSelect.getFresnelColor(), (Vec3AnimFlag) itemToSelect.find(MdlUtils.TOKEN_FRESNEL_COLOR), itemToSelect, MdlUtils.TOKEN_FRESNEL_COLOR, itemToSelect::setFresnelColor);
		emissiveGainPanel.reloadNewValue((float) itemToSelect.getEmissive(), (FloatAnimFlag) itemToSelect.find(MdlUtils.TOKEN_EMISSIVE_GAIN), itemToSelect, MdlUtils.TOKEN_EMISSIVE_GAIN, itemToSelect::setEmissive);

//		System.out.println("setting panel visibility (is1000)");
		boolean is1000 = ModelUtils.isFresnelColorLayerSupported(modelHandler.getModel().getFormatVersion());
		fresnelColorPanel.setVisible(is1000);
		fresnelOpacityPanel.setVisible(is1000);
		fresnelTeamColor.setVisible(is1000);

		layerFlagsPanel.removeAll();
//		System.out.println("re-adding Layer flagPanel");
		layerFlagsPanel.add(new LayerFlagsPanel(modelHandler, itemToSelect), "");

//		System.out.println("updating filtermode ");
		filterModeDropdown.setSelectedItem(itemToSelect.getFilterMode());
//		System.out.println("updating textureAnim ");
		tVertexAnimButton.setText(selectedItem.getTextureAnim() == null ? "None" : selectedItem.getTextureAnim().getFlagNames());

		coordIdSpinner.reloadNewValue(selectedItem.getCoordId());

//		System.out.println("updating TexturePanel");
		updateTexturePanel(itemToSelect.getTextureBitmap());
	}

	private void updateTexturePanel(Bitmap defaultTexture) {
		texturePreviewPanel.removeAll();
		if (defaultTexture != null) {
			ZoomableImagePreviewPanel imagePreviewPanel = getImagePreviewPanel(defaultTexture, model);
			texturePreviewPanel.add(imagePreviewPanel, "growx, growy");
		}
//
//		System.out.println("texture panel updated");
//		texturePreviewPanel.revalidate();
//		texturePreviewPanel.repaint();
//		System.out.println("texture panel repainted");
	}

	private ZoomableImagePreviewPanel getImagePreviewPanel(Bitmap defaultTexture, EditableModel model) {
		DataSource workingDirectory = model.getWrappedDataSource();
		ZoomableImagePreviewPanel imagePreviewPanel;
		try {
			BufferedImage texture = BLPHandler.getImage(defaultTexture, workingDirectory);
			imagePreviewPanel = new ZoomableImagePreviewPanel(texture, true);
		} catch (final Exception exc) {
			BufferedImage image = new BufferedImage(512, 512, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2 = image.createGraphics();
			g2.setColor(Color.RED);
			g2.drawString(exc.getClass().getSimpleName() + ": " + exc.getMessage(), 128, 128);
			imagePreviewPanel = new ZoomableImagePreviewPanel(image, true);
		}
		return imagePreviewPanel;
	}


	private JPanel getLeftHandPanel() {
		JPanel leftHandSettingsPanel = new JPanel(new MigLayout());

		leftHandSettingsPanel.add(getTopSettingsPanel(), "wrap, growx");

//		System.out.println("ComponentHDLayer: creating texturePanel");
		texturePanel = new TextureValuePanel(modelHandler, "Texture");
//		System.out.println("ComponentHDLayer: creating alphaPanel");
		alphaPanel = new FloatValuePanel(modelHandler, MdlUtils.TOKEN_ALPHA);
		;
//		System.out.println("ComponentHDLayer: creating emissiveGainPanel");
		emissiveGainPanel = new FloatValuePanel(modelHandler, "Emissive Gain");

//		System.out.println("ComponentHDLayer: creating fresnelColorPanel");
		fresnelColorPanel = new ColorValuePanel(modelHandler, "Fresnel Color");
//		System.out.println("ComponentHDLayer: creating fresnelOpacityPanel");
		fresnelOpacityPanel = new FloatValuePanel(modelHandler, "Fresnel Opacity");
//		System.out.println("ComponentHDLayer: creating fresnelTeamColor");
		fresnelTeamColor = new FloatValuePanel(modelHandler, "Fresnel Team Color");

//		System.out.println("ComponentHDLayer: adding panels");
		leftHandSettingsPanel.add(texturePanel, "wrap, growx");
		leftHandSettingsPanel.add(alphaPanel, valuePanelConstraints);
		leftHandSettingsPanel.add(emissiveGainPanel, valuePanelConstraints);

		leftHandSettingsPanel.add(fresnelColorPanel, valuePanelConstraints);
		leftHandSettingsPanel.add(fresnelOpacityPanel, valuePanelConstraints);
		leftHandSettingsPanel.add(fresnelTeamColor, valuePanelConstraints);


		return leftHandSettingsPanel;
	}

	private JPanel getTopSettingsPanel() {
		JPanel topSettingsPanel = new JPanel(new MigLayout("ins 0"));

		topSettingsPanel.add(new JLabel("Filter Mode:"));
		filterModeDropdown = new JComboBox<>(FilterMode.values());
		filterModeDropdown.setSelectedIndex(0);
		filterModeDropdown.addItemListener(this::filterModeDropdownListener);
		topSettingsPanel.add(filterModeDropdown, "wrap, growx");

		topSettingsPanel.add(new JLabel("TVertex Anim:"));
		tVertexAnimButton = new JButton("Choose TVertex Anim");
		topSettingsPanel.add(tVertexAnimButton, "wrap, growx");

		topSettingsPanel.add(new JLabel("CoordID:"));
		coordIdSpinner = new IntEditorJSpinner(-1, Integer.MIN_VALUE, this::setCoordId);
		topSettingsPanel.add(coordIdSpinner, "wrap, growx");
		return topSettingsPanel;
	}

	private void filterModeDropdownListener(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED && selectedItem != null) {
//			System.out.println("filterModeDropdownListener");
			FilterMode newFilterMode = (FilterMode) e.getItem();
			if (newFilterMode != selectedItem.getFilterMode()) {
				undoManager.pushAction(new SetLayerFilterModeAction(selectedItem, newFilterMode, changeListener).redo());
			}
		}
	}

	private void setCoordId(int value) {
		if (selectedItem != null) {
//			System.out.println("setCoordId");
			selectedItem.setCoordId(value);
		}
	}
}
