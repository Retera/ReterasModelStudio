package com.hiveworkshop.rms.ui.application.model.material;

import com.hiveworkshop.rms.editor.actions.model.material.*;
import com.hiveworkshop.rms.editor.actions.util.ConsumerAction;
import com.hiveworkshop.rms.editor.model.*;
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
import com.hiveworkshop.rms.util.CollapsablePanel;
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

public class ComponentLayerPanel extends ComponentPanel<Layer> {
	private final String valuePanelConstraints = "wrap, growx, hidemode 2";
	private final List<EditorHelpers.TextureEditor> texturePanels = new ArrayList<>();
	private EditorHelpers.FloatEditor alphaPanel;
	private final JPanel texturePreviewPanel;
	private final LayerFlagsPanel layerFlagsPanel;
	private TwiComboBox<FilterMode> filterModeDropdown;
	private TwiComboBox<TextureAnim> textureAnimDropdown;
	private final List<TextureAnim> textureAnims = new ArrayList<>();
	private IntEditorJSpinner coordIdSpinner;
	private JButton move_up;
	private JButton move_down;
	private Material material;
	private final JPanel leftHandSettingsPanel;
	private final JPanel topPanel;
	private EditorHelpers.FloatEditor emissiveGainPanel;
	private EditorHelpers.ColorEditor fresnelColorPanel;
	private EditorHelpers.FloatEditor fresnelOpacityPanel;
	private EditorHelpers.FloatEditor fresnelTeamColor;

	public ComponentLayerPanel(ModelHandler modelHandler, String title) {
		super(modelHandler);
		setLayout(new MigLayout("fill", "[][][grow]", "[][fill]"));
		Border lineBorder = BorderFactory.createLineBorder(Color.LIGHT_GRAY);
		setBorder(BorderFactory.createTitledBorder(lineBorder, title));

		topPanel = getTopPanel();
		add(topPanel, "growx, spanx, wrap");

		leftHandSettingsPanel = getLeftHandPanel();
		add(leftHandSettingsPanel, "spany");
		System.out.println("[ComponentLayer]: added leftHandSettingsPanel");
		layerFlagsPanel = new LayerFlagsPanel(modelHandler);
		add(layerFlagsPanel, "");
		System.out.println("[ComponentLayer]: added layerFlagsPanel");
		texturePreviewPanel = new JPanel(new MigLayout("gap 0, ins 0, fill", "[grow]", "[grow]"));
		add(texturePreviewPanel, "growx, top");
	}

	public ComponentLayerPanel setMaterial(Material material) {
		this.material = material;
		return this;
	}

	@Override
	public ComponentPanel<Layer> setSelectedItem(Layer itemToSelect) {
		selectedItem = itemToSelect;
		updateFlagPanels(itemToSelect);

		layerFlagsPanel.setLayer(itemToSelect);

		filterModeDropdown.setSelectedItem(itemToSelect.getFilterMode());
		updateTextureAnimBox(selectedItem.getTextureAnim());
		coordIdSpinner.reloadNewValue(selectedItem.getCoordId());

		updateTexturePanels(itemToSelect);

		updateTexturePreview(itemToSelect.getTexture(0));
		updateMoveButtons();
		return this;
	}

	protected JPanel getTopPanel() {
		JPanel titlePanel = new JPanel();
		titlePanel.setLayout(new MigLayout("fill", "[right]0[right]10[right]", "[]"));

		move_up = getMoveLayerButton("Move Up", true);
		move_down = getMoveLayerButton("Move Down", false);
		titlePanel.add(move_up);
		titlePanel.add(move_down);
		titlePanel.add(getDeleteButton(e -> removeLayer()));

		return titlePanel;
	}

	protected JButton getMoveLayerButton(String moveText, boolean moveUp) {
		JButton button = new JButton(moveText);
		button.addActionListener(e -> undoManager.pushAction(new ChangeLayerOrderAction(material, selectedItem, moveUp, changeListener).redo()));
		return button;
	}

	protected void updateMoveButtons() {
		List<Layer> layers = material.getLayers();
		int index = layers.indexOf(selectedItem);

		move_up.setEnabled(index > 0);
		move_down.setEnabled(index < layers.size() - 1);
		boolean canMoveLayer = isMultiLayerPermitted();
		topPanel.setVisible(canMoveLayer);
	}

	private boolean isMultiLayerPermitted() {
		int formatVersion = modelHandler.getModel().getFormatVersion();
		return formatVersion < 900
				|| 1000 < formatVersion
				|| !isHDMaterial();
	}

	private boolean isHDMaterial(){
		return material.getShaderString().equals(Material.SHADER_HD_DEFAULT_UNIT)
				|| material.getShaderString().equals(Material.SHADER_HD_CRYSTAL);
	}

	protected void updateTexturePreview(Bitmap defaultTexture) {
		texturePreviewPanel.removeAll();
		if (defaultTexture != null) {
			ZoomableImagePreviewPanel imagePreviewPanel = getImagePreviewPanel(defaultTexture, model);
			texturePreviewPanel.add(imagePreviewPanel, "growx, growy");
		}
	}

	protected JPanel getLeftHandPanel() {
		JPanel leftHandSettingsPanel = new JPanel(new MigLayout());
		JPanel topSettingsPanel = getTopSettingsPanel();

		leftHandSettingsPanel.add(topSettingsPanel, "wrap, growx, hidemode 2");
		setupFlagPanels();
		JPanel innerSettingsPanel = getInnerSettingsPanel();
		CollapsablePanel settingsPanel = new CollapsablePanel("Layer Settings", innerSettingsPanel);
		settingsPanel.setCollapsed(true);

		leftHandSettingsPanel.add(settingsPanel, "wrap, growx, hidemode 2");

		return leftHandSettingsPanel;
	}

	protected void updateTexturePanels(Layer itemToSelect) {
		for(int i = itemToSelect.getTextures().size()-1; i < texturePanels.size(); i++){
			leftHandSettingsPanel.remove(texturePanels.get(i).getFlagPanel());
		}

		for(int i = 0; i < selectedItem.getTextures().size(); i++){
			EditorHelpers.TextureEditor texturePanel = getTexturePanel(i);
			texturePanel.update(itemToSelect.getTextureSlot(i), itemToSelect.getTexture(i));
			leftHandSettingsPanel.add(texturePanel.getFlagPanel(), "wrap, growx");
		}
	}
	protected void updateFlagPanels(Layer itemToSelect){
		alphaPanel.update(itemToSelect, (float) itemToSelect.getStaticAlpha());
		fresnelTeamColor.update(itemToSelect, (float) itemToSelect.getFresnelTeamColor());
		fresnelOpacityPanel.update(itemToSelect, (float) itemToSelect.getFresnelOpacity());
		fresnelColorPanel.update(itemToSelect, itemToSelect.getFresnelColor());
		emissiveGainPanel.update(itemToSelect, (float) itemToSelect.getEmissive());

		boolean isHD = isHDMaterial();
		emissiveGainPanel.getFlagPanel().setVisible(isHD);
		boolean is1000 = ModelUtils.isFresnelColorLayerSupported(model.getFormatVersion()) && isHD;
		fresnelColorPanel.getFlagPanel().setVisible(is1000);
		fresnelOpacityPanel.getFlagPanel().setVisible(is1000);
		fresnelTeamColor.getFlagPanel().setVisible(is1000);
	}
	protected void setupFlagPanels(){
		alphaPanel = new EditorHelpers.FloatEditor(modelHandler, MdlUtils.TOKEN_ALPHA, this::setStaticAlpha);
		emissiveGainPanel = new EditorHelpers.FloatEditor(modelHandler, MdlUtils.TOKEN_EMISSIVE_GAIN, this::setEmissive);
		fresnelColorPanel = new EditorHelpers.ColorEditor(modelHandler, MdlUtils.TOKEN_FRESNEL_COLOR, this::setFresnelColor);
		fresnelOpacityPanel = new EditorHelpers.FloatEditor(modelHandler, MdlUtils.TOKEN_FRESNEL_OPACITY, this::setFresnelOpacity);
		fresnelTeamColor = new EditorHelpers.FloatEditor(modelHandler, MdlUtils.TOKEN_FRESNEL_TEAM_COLOR, this::setFresnelTeamColor);
	}

	protected JPanel getInnerSettingsPanel(){
		JPanel innerSettingsPanel = new JPanel(new MigLayout("ins 0"));
		innerSettingsPanel.add(alphaPanel.getFlagPanel(), valuePanelConstraints);
		innerSettingsPanel.add(emissiveGainPanel.getFlagPanel(), valuePanelConstraints);
		innerSettingsPanel.add(fresnelColorPanel.getFlagPanel(), valuePanelConstraints);
		innerSettingsPanel.add(fresnelOpacityPanel.getFlagPanel(), valuePanelConstraints);
		innerSettingsPanel.add(fresnelTeamColor.getFlagPanel(), valuePanelConstraints);

		return innerSettingsPanel;
	}

	protected JPanel getTopSettingsPanel() {
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

	protected void updateTextureAnimBox(TextureAnim textureAnim){
		textureAnims.clear();
		textureAnims.add(0, null);
		textureAnims.addAll(model.getTexAnims());
		textureAnimDropdown.setSelectedItem(textureAnim);
	}


	protected String getTexAnimName(Object textureAnim) {
		if (textureAnim instanceof TextureAnim) {
			int textureAnimId = model.getTextureAnimId((TextureAnim) textureAnim);
			return "[" + textureAnimId + "] " + ((TextureAnim) textureAnim).getFlagNames();
		}
		return "None";
	}

	protected void setTexture(Bitmap texture, int slot){
		if(texture != null && selectedItem.getTexture(slot) != texture) {
			undoManager.pushAction(new SetLayerTextureAction(texture, slot, selectedItem, changeListener).redo());
		}
	}

	protected void setStaticAlpha(double value){
		if(selectedItem.getStaticAlpha() != value) {
			undoManager.pushAction(new SetLayerAlphaAction(selectedItem, value, changeListener).redo());
		}
	}

	protected void setTextureAnim(TextureAnim textureAnim) {
		if(textureAnim != selectedItem.getTextureAnim()){
			undoManager.pushAction(new SetLayerTextureAnimAction(selectedItem, textureAnim, changeListener).redo());
		}
	}

	protected void changeFilterMode(FilterMode newFilterMode) {
		if (newFilterMode != null && newFilterMode != selectedItem.getFilterMode()) {
			undoManager.pushAction(new SetLayerFilterModeAction(selectedItem, newFilterMode, changeListener).redo());
		}
	}

	protected void setCoordId(int value) {
		if (selectedItem != null) {
			selectedItem.setCoordId(value);
		}
	}

	protected void setFresnelTeamColor(double value){
		if(selectedItem.getFresnelTeamColor() != value) {
			undoManager.pushAction(new ConsumerAction<>(selectedItem::setFresnelTeamColor, value, selectedItem.getFresnelTeamColor(), "FresnelTeamColor").redo());
		}
	}

	protected void setFresnelOpacity(double value){
		if(selectedItem.getFresnelOpacity() != value) {
			undoManager.pushAction(new ConsumerAction<>(selectedItem::setFresnelOpacity, value, selectedItem.getFresnelOpacity(), "FresnelOpacity").redo());
		}
	}

	protected void setFresnelColor(Vec3 color){
		if(!selectedItem.getFresnelColor().equalLocs(color)) {
			undoManager.pushAction(new ConsumerAction<>(selectedItem::setFresnelColor, color, selectedItem.getFresnelColor(), "FresnelColor").redo());
		}
	}

	protected void setEmissive(double value){
		if(selectedItem.getEmissive() != value) {
			undoManager.pushAction(new ConsumerAction<>(selectedItem::setEmissive, value, selectedItem.getEmissive(), "Emissive").redo());
		}
	}

	protected EditorHelpers.TextureEditor getTexturePanel(int slot){
		if(texturePanels.size()<=slot){
			for(int i = texturePanels.size(); i <= slot; i++) {
				int finalI = i;
				if(selectedItem.getTextures().size() == 6){
					texturePanels.add(new EditorHelpers.TextureEditor(HD_Material_Layer.values()[i].getLayerName(), modelHandler, MdlUtils.TOKEN_TEXTURE_ID, b -> setTexture(b, finalI)));
				} else {
					texturePanels.add(new EditorHelpers.TextureEditor(modelHandler, MdlUtils.TOKEN_TEXTURE_ID, b -> setTexture(b, finalI)));
				}
			}
		}
		return texturePanels.get(slot);
	}

	protected ZoomableImagePreviewPanel getImagePreviewPanel(Bitmap defaultTexture, EditableModel model) {
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

	protected void removeLayer() {
		if (material.getLayers().size() <= 1) {
			List<Geoset> geosetList = model.getGeosets();
			// Checks if this instance of the material is used.
			// This lets the user remove the material even if used clones exists
			int numUses = geosetList.stream().mapToInt(geoset -> geoset.getMaterial() == material ? 1 : 0).sum();
			if (numUses > 0) {
				JOptionPane.showMessageDialog(this, "Cannot delete material as it is being used by " + numUses + " geosets.");
			} else {
				undoManager.pushAction(new RemoveMaterialAction(material, model, changeListener).redo());
			}
		} else {
			undoManager.pushAction(new RemoveLayerAction(selectedItem, material, changeListener).redo());
		}
	}
}
