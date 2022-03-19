package com.hiveworkshop.rms.ui.application.model.material;

import com.hiveworkshop.rms.editor.actions.model.material.*;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.FloatAnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.IntAnimFlag;
import com.hiveworkshop.rms.editor.model.util.FilterMode;
import com.hiveworkshop.rms.filesystem.sources.DataSource;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.model.ComponentPanel;
import com.hiveworkshop.rms.ui.application.model.editors.FloatValuePanel;
import com.hiveworkshop.rms.ui.application.model.editors.IntEditorJSpinner;
import com.hiveworkshop.rms.ui.application.model.editors.TextureValuePanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.util.ZoomableImagePreviewPanel;
import com.hiveworkshop.rms.util.TwiComboBox;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class ComponentSDLayer extends ComponentPanel<Layer> {
	private TextureValuePanel texturePanel;
	private FloatValuePanel alphaPanel;
	private final JPanel texturePreviewPanel;
	private final JPanel layerFlagsPanel;
	private TwiComboBox<FilterMode> filterModeDropdown;
	private JButton tVertexAnimButton;
	private IntEditorJSpinner coordIdSpinner;
	private JButton move_up;
	private JButton move_down;
	private Material material;

	public ComponentSDLayer(ModelHandler modelHandler, int nr) {
		super(modelHandler);
		setLayout(new MigLayout("fill", "[][][grow]", "[][grow]"));
		Border lineBorder = BorderFactory.createLineBorder(Color.LIGHT_GRAY);
		setBorder(BorderFactory.createTitledBorder(lineBorder, "Layer " + nr));

		add(getTopPanel(), "growx, spanx, wrap");
		add(getLeftHandPanel(), "spany");
		layerFlagsPanel = new JPanel(new MigLayout("ins 0, gap 0"));
		add(layerFlagsPanel, "");
		texturePreviewPanel = new JPanel(new MigLayout("gap 0, ins 0, fill", "[grow]", "[grow]"));
		add(texturePreviewPanel, "growx, top");
	}

	public ComponentSDLayer setMaterial(Material material) {
		this.material = material;
		return this;
	}

	@Override
	public ComponentPanel<Layer> setSelectedItem(Layer itemToSelect) {
		selectedItem = itemToSelect;
		texturePanel.reloadNewValue(itemToSelect.getTextureId(), (IntAnimFlag) itemToSelect.find(MdlUtils.TOKEN_TEXTURE_ID), itemToSelect, MdlUtils.TOKEN_TEXTURE_ID, this::setTextureId);
		alphaPanel.reloadNewValue((float) itemToSelect.getStaticAlpha(), (FloatAnimFlag) itemToSelect.find(MdlUtils.TOKEN_ALPHA), itemToSelect, MdlUtils.TOKEN_ALPHA, this::setStaticAlpha);

		layerFlagsPanel.removeAll();
		layerFlagsPanel.add(new LayerFlagsPanel(modelHandler, itemToSelect), "");


		filterModeDropdown.setSelectedItem(itemToSelect.getFilterMode());
		tVertexAnimButton.setText(getTexAnimName());

		coordIdSpinner.reloadNewValue(selectedItem.getCoordId());

		updateTexturePanel(itemToSelect.getTextureBitmap());
		updateMoveButtons();
		return this;
	}

	private String getTexAnimName() {
		TextureAnim textureAnim = selectedItem.getTextureAnim();
		if (textureAnim != null) {
			int textureAnimId = model.getTextureAnimId(textureAnim);
			return "[" + textureAnimId + "] " + textureAnim.getFlagNames();
		}
//		String text = textureAnim == null ? "None" : textureAnim.getFlagNames();
		return "None";
	}

	private JPanel getTopPanel() {
		JPanel titlePanel = new JPanel();
		titlePanel.setLayout(new MigLayout("fill", "[right]0[right]10[right]", "[]"));

		move_up = getMoveLayerButton("Move Up", true);
		move_down = getMoveLayerButton("Move Down", false);
		titlePanel.add(move_up);
		titlePanel.add(move_down);
		titlePanel.add(getDeleteButton(e -> removeLayer()));

		return titlePanel;
	}

	private JButton getMoveLayerButton(String moveText, boolean moveUp) {
		JButton button = new JButton(moveText);
		button.addActionListener(e -> undoManager.pushAction(new ChangeLayerOrderAction(material, selectedItem, moveUp, changeListener).redo()));
		return button;
	}

	private void updateMoveButtons() {
		List<Layer> layers = material.getLayers();
		int index = layers.indexOf(selectedItem);

		move_up.setEnabled(index > 0);
		move_down.setEnabled(index < layers.size() - 1);
	}

	private void updateTexturePanel(Bitmap defaultTexture) {
		texturePreviewPanel.removeAll();
		if (defaultTexture != null) {
			ZoomableImagePreviewPanel imagePreviewPanel = getImagePreviewPanel(defaultTexture, model);
			texturePreviewPanel.add(imagePreviewPanel, "growx, growy");
		}
//		texturePreviewPanel.revalidate();
//		texturePreviewPanel.repaint();
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
		JPanel topSettingsPanel = getTopSettingsPanel();

		leftHandSettingsPanel.add(topSettingsPanel, "wrap, growx");

		texturePanel = new TextureValuePanel(modelHandler, "Texture");
		alphaPanel = new FloatValuePanel(modelHandler, MdlUtils.TOKEN_ALPHA);

		leftHandSettingsPanel.add(texturePanel, "wrap, growx");
		leftHandSettingsPanel.add(alphaPanel, "wrap, growx, hidemode 2");

		return leftHandSettingsPanel;
	}

	private JPanel getTopSettingsPanel() {
		JPanel topSettingsPanel = new JPanel(new MigLayout("ins 0"));

		topSettingsPanel.add(new JLabel("Filter Mode:"));
		filterModeDropdown = new TwiComboBox<>(FilterMode.values(), FilterMode.TRANSPARENT);
		filterModeDropdown.addOnSelectItemListener(this::changeFilterModeDrop);
		topSettingsPanel.add(filterModeDropdown, "wrap, growx");

		topSettingsPanel.add(new JLabel("TVertex Anim:"));
		tVertexAnimButton = new JButton("Choose TVertex Anim");
//		tVertexAnimButton.addActionListener();
		topSettingsPanel.add(tVertexAnimButton, "wrap, growx");

		topSettingsPanel.add(new JLabel("CoordID:"));
		coordIdSpinner = new IntEditorJSpinner(-1, Integer.MIN_VALUE, this::setCoordId);
		topSettingsPanel.add(coordIdSpinner, "wrap, growx");
		return topSettingsPanel;
	}

	private void changeFilterModeDrop(FilterMode newFilterMode) {
		if (newFilterMode != null && newFilterMode != selectedItem.getFilterMode()) {
			undoManager.pushAction(new SetLayerFilterModeAction(selectedItem, newFilterMode, changeListener).redo());
		}
	}

	private void setCoordId(int value) {
		if (selectedItem != null) {
			selectedItem.setCoordId(value);
		}
	}

	private void setTextureId(int value){
		Bitmap texture = model.getTexture(value);
		if(texture != null && selectedItem.getTextureBitmap() != texture) {
			undoManager.pushAction(new SetLayerTextureAction(texture, selectedItem, changeListener).redo());
		}
	}

	private void setStaticAlpha(double value){
		if(selectedItem.getStaticAlpha() != value) {
			undoManager.pushAction(new SetLayerAlphaAction(selectedItem, value, changeListener).redo());
		}
	}

	private void removeLayer() {
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
