package com.hiveworkshop.rms.ui.application.model.material;

import com.hiveworkshop.rms.editor.actions.model.material.SetLayerAlphaAction;
import com.hiveworkshop.rms.editor.actions.model.material.SetLayerFilterModeAction;
import com.hiveworkshop.rms.editor.actions.model.material.SetLayerTextureAction;
import com.hiveworkshop.rms.editor.actions.model.material.SetLayerTextureAnimAction;
import com.hiveworkshop.rms.editor.actions.util.ConsumerAction;
import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Layer;
import com.hiveworkshop.rms.editor.model.TextureAnim;
import com.hiveworkshop.rms.editor.model.util.FilterMode;
import com.hiveworkshop.rms.editor.model.util.HD_Material_Layer;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.filesystem.sources.DataSource;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.model.ComponentPanel;
import com.hiveworkshop.rms.ui.application.model.editors.IntEditorJSpinner;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.util.ZoomableImagePreviewPanel;
import com.hiveworkshop.rms.util.TwiComboBox;
import com.hiveworkshop.rms.util.TwiTextEditor.EditorHelpers;
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
	private EditorHelpers.TextureEditor texturePanel;
	private EditorHelpers.FloatEditor alphaPanel;
	private EditorHelpers.FloatEditor emissiveGainPanel;
	private EditorHelpers.ColorEditor fresnelColorPanel;
	private EditorHelpers.FloatEditor fresnelOpacityPanel;
	private EditorHelpers.FloatEditor fresnelTeamColor;
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
		texturePanel.update(itemToSelect, itemToSelect.getTextureBitmap());
		alphaPanel.update(itemToSelect, (float) itemToSelect.getStaticAlpha());
		fresnelTeamColor.update(itemToSelect, (float) itemToSelect.getFresnelTeamColor());
		fresnelOpacityPanel.update(itemToSelect, (float) itemToSelect.getFresnelOpacity());
		fresnelColorPanel.update(itemToSelect, itemToSelect.getFresnelColor());
		emissiveGainPanel.update(itemToSelect, (float) itemToSelect.getEmissive());

//		System.out.println("setting panel visibility (is1000)");
		boolean is1000 = ModelUtils.isFresnelColorLayerSupported(modelHandler.getModel().getFormatVersion());
		fresnelColorPanel.getFlagPanel().setVisible(is1000);
		fresnelOpacityPanel.getFlagPanel().setVisible(is1000);
		fresnelTeamColor.getFlagPanel().setVisible(is1000);

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
		texturePanel = new EditorHelpers.TextureEditor(modelHandler, MdlUtils.TOKEN_TEXTURE_ID, this::setTexture);
//		System.out.println("ComponentHDLayer: creating alphaPanel");
		alphaPanel = new EditorHelpers.FloatEditor(modelHandler, MdlUtils.TOKEN_ALPHA, this::setStaticAlpha);
		;
//		System.out.println("ComponentHDLayer: creating emissiveGainPanel");
		emissiveGainPanel = new EditorHelpers.FloatEditor(modelHandler, MdlUtils.TOKEN_EMISSIVE_GAIN, this::setEmissive);

//		System.out.println("ComponentHDLayer: creating fresnelColorPanel");
		fresnelColorPanel = new EditorHelpers.ColorEditor(modelHandler, MdlUtils.TOKEN_FRESNEL_COLOR, this::setFresnelColor);
//		System.out.println("ComponentHDLayer: creating fresnelOpacityPanel");
		fresnelOpacityPanel = new EditorHelpers.FloatEditor(modelHandler, MdlUtils.TOKEN_FRESNEL_OPACITY, this::setFresnelOpacity);
//		System.out.println("ComponentHDLayer: creating fresnelTeamColor");
		fresnelTeamColor = new EditorHelpers.FloatEditor(modelHandler, MdlUtils.TOKEN_FRESNEL_TEAM_COLOR, this::setFresnelTeamColor);

//		System.out.println("ComponentHDLayer: adding panels");
		leftHandSettingsPanel.add(texturePanel.getFlagPanel(), "wrap, growx");
		leftHandSettingsPanel.add(alphaPanel.getFlagPanel(), valuePanelConstraints);
		leftHandSettingsPanel.add(emissiveGainPanel.getFlagPanel(), valuePanelConstraints);

		leftHandSettingsPanel.add(fresnelColorPanel.getFlagPanel(), valuePanelConstraints);
		leftHandSettingsPanel.add(fresnelOpacityPanel.getFlagPanel(), valuePanelConstraints);
		leftHandSettingsPanel.add(fresnelTeamColor.getFlagPanel(), valuePanelConstraints);


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
			undoManager.pushAction(new ConsumerAction<>(selectedItem::setCoordId, value, selectedItem.getCoordId(), "CoordId").redo());
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
			undoManager.pushAction(new ConsumerAction<>(selectedItem::setFresnelTeamColor, value, selectedItem.getFresnelTeamColor(), "FresnelTeamColor").redo());
		}
	}

	private void setFresnelOpacity(double value){
		if(selectedItem.getFresnelOpacity() != value) {
			undoManager.pushAction(new ConsumerAction<>(selectedItem::setFresnelOpacity, value, selectedItem.getFresnelOpacity(), "FresnelOpacity").redo());
		}
	}

	private void setFresnelColor(Vec3 color){
		if(!selectedItem.getFresnelColor().equalLocs(color)) {
			undoManager.pushAction(new ConsumerAction<>(selectedItem::setFresnelColor, color, selectedItem.getFresnelColor(), "FresnelColor").redo());
		}
	}

	private void setEmissive(double value){
		if(selectedItem.getEmissive() != value) {
			undoManager.pushAction(new ConsumerAction<>(selectedItem::setEmissive, value, selectedItem.getEmissive(), "Emissive").redo());
		}
	}
}
