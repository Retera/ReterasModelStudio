package com.hiveworkshop.rms.ui.application.model.material;

import com.hiveworkshop.rms.editor.actions.model.material.SetLayerAlphaAction;
import com.hiveworkshop.rms.editor.actions.model.material.SetLayerFilterModeAction;
import com.hiveworkshop.rms.editor.actions.model.material.SetLayerTextureAction;
import com.hiveworkshop.rms.editor.actions.model.material.SetLayerTextureAnimAction;
import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Layer;
import com.hiveworkshop.rms.editor.model.TextureAnim;
import com.hiveworkshop.rms.editor.model.animflag.BitmapAnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.FloatAnimFlag;
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
import com.hiveworkshop.rms.ui.application.model.editors.TextureValuePanel2;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.util.ZoomableImagePreviewPanel;
import com.hiveworkshop.rms.util.TwiComboBox;
import com.hiveworkshop.rms.util.Vec3;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class ComponentHDLayer extends ComponentPanel<Layer> {
	private String valuePanelConstraints = "wrap, growx, hidemode 2";
	private TextureValuePanel2 texturePanel;
	private FloatValuePanel alphaPanel;
	private FloatValuePanel emissiveGainPanel;
	private ColorValuePanel fresnelColorPanel;
	private FloatValuePanel fresnelOpacityPanel;
	private FloatValuePanel fresnelTeamColor;
	private final JPanel texturePreviewPanel;
	private final JPanel layerFlagsPanel;
	private TwiComboBox<FilterMode> filterModeDropdown;
	private TwiComboBox<TextureAnim> textureAnimDropdown;
	private List<TextureAnim> textureAnims = new ArrayList<>();
	private IntEditorJSpinner coordIdSpinner;


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
	public ComponentPanel<Layer> setSelectedItem(Layer itemToSelect) {
		selectedItem = itemToSelect;
//		System.out.println("Reloading panel values");
		texturePanel.reloadNewValue(itemToSelect.getTextureBitmap(), (BitmapAnimFlag) itemToSelect.find(MdlUtils.TOKEN_TEXTURE_ID), itemToSelect, MdlUtils.TOKEN_TEXTURE_ID, this::setTexture);
		alphaPanel.reloadNewValue((float) itemToSelect.getStaticAlpha(), (FloatAnimFlag) itemToSelect.find(MdlUtils.TOKEN_ALPHA), itemToSelect, MdlUtils.TOKEN_ALPHA, this::setStaticAlpha);
		fresnelTeamColor.reloadNewValue((float) itemToSelect.getFresnelTeamColor(), (FloatAnimFlag) itemToSelect.find(MdlUtils.TOKEN_FRESNEL_TEAM_COLOR), itemToSelect, MdlUtils.TOKEN_FRESNEL_TEAM_COLOR, this::setFresnelTeamColor);
		fresnelOpacityPanel.reloadNewValue((float) itemToSelect.getFresnelOpacity(), (FloatAnimFlag) itemToSelect.find(MdlUtils.TOKEN_FRESNEL_OPACITY), itemToSelect, MdlUtils.TOKEN_FRESNEL_OPACITY, this::setFresnelOpacity);
		fresnelColorPanel.reloadNewValue(itemToSelect.getFresnelColor(), (Vec3AnimFlag) itemToSelect.find(MdlUtils.TOKEN_FRESNEL_COLOR), itemToSelect, MdlUtils.TOKEN_FRESNEL_COLOR, this::setFresnelColor);
		emissiveGainPanel.reloadNewValue((float) itemToSelect.getEmissive(), (FloatAnimFlag) itemToSelect.find(MdlUtils.TOKEN_EMISSIVE_GAIN), itemToSelect, MdlUtils.TOKEN_EMISSIVE_GAIN, this::setEmissive);

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
		updateTextureAnimBox(selectedItem.getTextureAnim());

		coordIdSpinner.reloadNewValue(selectedItem.getCoordId());

//		System.out.println("updating TexturePanel");
		updateTexturePanel(itemToSelect.getTextureBitmap());
		return this;
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
		texturePanel = new TextureValuePanel2(modelHandler, "Texture");
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
		filterModeDropdown = new TwiComboBox<>(FilterMode.values(), FilterMode.TRANSPARENT);
		filterModeDropdown.addOnSelectItemListener(this::changeFilterMode);
		topSettingsPanel.add(filterModeDropdown, "wrap, growx");

		topSettingsPanel.add(new JLabel("TVertex Anim:"));
		textureAnimDropdown = new TwiComboBox<>(textureAnims);
		textureAnimDropdown.setStringFunctionRender(this::getTexAnimName);
		textureAnimDropdown.addOnSelectItemListener(this::setTextureAnim);
		topSettingsPanel.add(textureAnimDropdown, "wrap, growx");

		topSettingsPanel.add(new JLabel("CoordID:"));
		coordIdSpinner = new IntEditorJSpinner(-1, Integer.MIN_VALUE, this::setCoordId);
		topSettingsPanel.add(coordIdSpinner, "wrap, growx");
		return topSettingsPanel;
	}

	private void updateTextureAnimBox(TextureAnim textureAnim){
		textureAnims.clear();
		textureAnims.add(0, null);
		textureAnims.addAll(model.getTexAnims());
		textureAnimDropdown.setSelectedItem(textureAnim);
	}


	private String getTexAnimName(Object textureAnim) {
		if (textureAnim instanceof TextureAnim) {
			int textureAnimId = model.getTextureAnimId((TextureAnim) textureAnim);
			return "[" + textureAnimId + "] " + ((TextureAnim) textureAnim).getFlagNames();
		}
		return "None";
	}

	private void setTextureAnim(TextureAnim textureAnim) {
		if(textureAnim != selectedItem.getTextureAnim()){
			System.out.println("notTehSame: " + textureAnim);
			undoManager.pushAction(new SetLayerTextureAnimAction(selectedItem, textureAnim, changeListener).redo());
		}
	}

	private void changeFilterMode(FilterMode newFilterMode) {
		if (newFilterMode != null && newFilterMode != selectedItem.getFilterMode()) {
			undoManager.pushAction(new SetLayerFilterModeAction(selectedItem, newFilterMode, changeListener).redo());
		}
	}


	private void setCoordId(int value) {
		if (selectedItem != null) {
//			System.out.println("setCoordId");
			selectedItem.setCoordId(value);
		}
	}

	private void setTextureId(int value){
		Bitmap texture = model.getTexture(value);
		if(texture != null && selectedItem.getTextureBitmap() != texture) {
			undoManager.pushAction(new SetLayerTextureAction(texture, selectedItem, changeListener).redo());
		}
	}

	private void setTexture(Bitmap texture){
		if(texture != null && selectedItem.getTextureBitmap() != texture) {
			undoManager.pushAction(new SetLayerTextureAction(texture, selectedItem, changeListener).redo());
		}
	}

	private void setStaticAlpha(double value){
		if(selectedItem.getStaticAlpha() != value) {
			undoManager.pushAction(new SetLayerAlphaAction(selectedItem, value, changeListener).redo());
		}
	}

	private void setFresnelTeamColor(double value){
		if(selectedItem.getFresnelTeamColor() != value) {
//			undoManager.pushAction(new SetLayerFilterModeAction().redo());
			selectedItem.setFresnelTeamColor(value);
		}
	}

	private void setFresnelOpacity(double value){
		if(selectedItem.getFresnelOpacity() != value) {
//			undoManager.pushAction(new SetLayerFilterModeAction().redo());
			selectedItem.setFresnelOpacity(value);
		}
	}

	private void setFresnelColor(Vec3 color){
		if(!selectedItem.getFresnelColor().equalLocs(color)) {
//			undoManager.pushAction(new SetLayerFilterModeAction().redo());
			selectedItem.setFresnelColor(color);
		}
	}

	private void setEmissive(double value){
		if(selectedItem.getEmissive() != value) {
//			undoManager.pushAction(new SetLayerFilterModeAction().redo());
			selectedItem.setEmissive(value);
		}
	}
}
