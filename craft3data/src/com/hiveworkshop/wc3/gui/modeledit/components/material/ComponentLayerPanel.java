package com.hiveworkshop.wc3.gui.modeledit.components.material;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpinnerNumberModel;

import com.hiveworkshop.wc3.gui.BLPHandler;
import com.hiveworkshop.wc3.gui.datachooser.DataSource;
import com.hiveworkshop.wc3.gui.modeledit.BitmapListCellRenderer;
import com.hiveworkshop.wc3.gui.modeledit.TextureAnimListCellRenderer;
import com.hiveworkshop.wc3.gui.modeledit.actions.componenttree.material.SetLayerBitmapAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.componenttree.material.SetLayerCoordIdAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.componenttree.material.SetLayerFilterModeAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.componenttree.material.SetLayerShaderAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.componenttree.material.SetLayerTextureAnimAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.activity.UndoActionListener;
import com.hiveworkshop.wc3.gui.modeledit.components.editors.ColorValuePanel;
import com.hiveworkshop.wc3.gui.modeledit.components.editors.ComponentEditorJSpinner;
import com.hiveworkshop.wc3.gui.modeledit.components.editors.ComponentEditorTextField;
import com.hiveworkshop.wc3.gui.modeledit.components.editors.FloatValuePanel;
import com.hiveworkshop.wc3.mdl.Bitmap;
import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.mdl.Layer;
import com.hiveworkshop.wc3.mdl.Layer.FilterMode;
import com.hiveworkshop.wc3.mdl.LayerShader;
import com.hiveworkshop.wc3.mdl.ShaderTextureTypeHD;
import com.hiveworkshop.wc3.mdl.TextureAnim;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.v2.ModelViewManager;
import com.hiveworkshop.wc3.util.Callback;
import com.hiveworkshop.wc3.util.IconUtils;
import com.hiveworkshop.wc3.util.ModelUtils;

import net.miginfocom.swing.MigLayout;

public class ComponentLayerPanel extends JPanel {
	private final JComboBox<FilterMode> filterModeDropdown;
	private final JComboBox<LayerShader> shaderOptionComboBox;
	private ComponentEditorTextField comboBoxEditor;
	private final EnumMap<ShaderTextureTypeHD, JButton> textureTypeToButton;
	private final List<JLabel> nonDiffuseTextureLabels = new ArrayList<>();
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
	private ModelViewManager modelViewManager;

	public ComponentLayerPanel() {
		setLayout(new MigLayout("fill", "", "[fill][fill]"));
		final JPanel leftHandSettingsPanel = new JPanel();
		layerFlagsPanel = new LayerFlagsPanel();
		layerFlagsPanel.setBorder(BorderFactory.createTitledBorder("Flags"));
		add(leftHandSettingsPanel);
		add(layerFlagsPanel);
		leftHandSettingsPanel.setLayout(new MigLayout());
		leftHandSettingsPanel.add(new JLabel("Filter Mode:"));
		filterModeDropdown = new JComboBox<Layer.FilterMode>(Layer.FilterMode.values());
		filterModeDropdown.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (listenersEnabled) {
					final SetLayerFilterModeAction setLayerFilterModeAction = new SetLayerFilterModeAction(layer,
							layer.getFilterMode(), (FilterMode) filterModeDropdown.getSelectedItem(),
							modelStructureChangeListener);
					setLayerFilterModeAction.redo();
					undoActionListener.pushAction(setLayerFilterModeAction);
				}
			}
		});
		leftHandSettingsPanel.add(filterModeDropdown, "wrap, growx");

		shaderOptionComboBox = new JComboBox<LayerShader>(LayerShader.values());
		shaderOptionComboBox.setEditable(false);

		shaderOptionComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (listenersEnabled) {
					final SetLayerShaderAction setMaterialShaderStringAction = new SetLayerShaderAction(layer,
							layer.getLayerShader(), (LayerShader) shaderOptionComboBox.getSelectedItem(),
							modelStructureChangeListener);
					setMaterialShaderStringAction.redo();
					undoActionListener.pushAction(setMaterialShaderStringAction);
				}
			}
		});
		leftHandSettingsPanel.add(new JLabel("Shader:"));
		leftHandSettingsPanel.add(shaderOptionComboBox, "wrap, growx, span 2");

		textureTypeToButton = new EnumMap<>(ShaderTextureTypeHD.class);
		for (final ShaderTextureTypeHD shaderTextureTypeHD : ShaderTextureTypeHD.VALUES) {
			final JLabel shaderTextureLabel = new JLabel(shaderTextureTypeHD.name() + " Texture:");
			if (shaderTextureTypeHD != ShaderTextureTypeHD.Diffuse) {
				nonDiffuseTextureLabels.add(shaderTextureLabel);
			}
			leftHandSettingsPanel.add(shaderTextureLabel);
			final JButton textureButton = new JButton("Choose Texture");
			textureButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					final EditableModel model = modelViewManager.getModel();
					final ArrayList<Bitmap> textures = model.getTextures();
					final DefaultListModel<Bitmap> bitmapListModel = new DefaultListModel<>();
					bitmapListModel.addElement(null);
					for (final Bitmap tex : textures) {
						bitmapListModel.addElement(tex);
					}
					final JList<Bitmap> bitmapList = new JList<>(bitmapListModel);
					bitmapList.setCellRenderer(new BitmapListCellRenderer(model));
					if (JOptionPane.showConfirmDialog(ComponentLayerPanel.this.getRootPane(),
							new JScrollPane(bitmapList), "Choose BItmap",
							JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
						final Bitmap selectedValue = bitmapList.getSelectedValue();
						final SetLayerBitmapAction setLayerBitmapAction = new SetLayerBitmapAction(layer,
								shaderTextureTypeHD, layer.getShaderTextures().get(shaderTextureTypeHD), selectedValue,
								modelStructureChangeListener);
						setLayerBitmapAction.redo();
						undoActionListener.pushAction(setLayerBitmapAction);
					}
				}
			});
			leftHandSettingsPanel.add(textureButton, "wrap, growx");
			textureTypeToButton.put(shaderTextureTypeHD, textureButton);
		}
		coordIdSpinner = new ComponentEditorJSpinner(
				new SpinnerNumberModel(0, Integer.MIN_VALUE, Integer.MAX_VALUE, 1));
		coordIdSpinner.addActionListener(new Runnable() {
			@Override
			public void run() {
				final SetLayerCoordIdAction setLayerCoordIdAction = new SetLayerCoordIdAction(layer, layer.getCoordId(),
						((Number) coordIdSpinner.getValue()).intValue(), modelStructureChangeListener);
				setLayerCoordIdAction.redo();
				undoActionListener.pushAction(setLayerCoordIdAction);
			}
		});
		leftHandSettingsPanel.add(new JLabel("TVertex Anim:"));
		tVertexAnimButton = new JButton("Choose TVertex Anim");
		tVertexAnimButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final EditableModel model = modelViewManager.getModel();
				final ArrayList<TextureAnim> textureAnims = model.getTexAnims();
				final DefaultListModel<TextureAnim> textureAnimsListModel = new DefaultListModel<>();
				textureAnimsListModel.addElement(null);
				for (final TextureAnim texAnim : textureAnims) {
					textureAnimsListModel.addElement(texAnim);
				}
				final JList<TextureAnim> textureAnimList = new JList<>(textureAnimsListModel);
				textureAnimList.setCellRenderer(new TextureAnimListCellRenderer(model));
				if (JOptionPane.showConfirmDialog(ComponentLayerPanel.this.getRootPane(),
						new JScrollPane(textureAnimList), "Choose TextureAnim",
						JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
					final TextureAnim selectedValue = textureAnimList.getSelectedValue();
					final SetLayerTextureAnimAction setLayerTextureAnimAction = new SetLayerTextureAnimAction(layer,
							layer.getTextureAnim(), selectedValue, modelStructureChangeListener);
					setLayerTextureAnimAction.redo();
					undoActionListener.pushAction(setLayerTextureAnimAction);
				}
			}
		});
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

	public void setLayer(final DataSource workingDirectory, final Layer layer, final int formatVersion,
			final UndoActionListener undoActionListener,
			final ModelStructureChangeListener modelStructureChangeListener, final ModelViewManager modelViewManager) {
		this.modelViewManager = modelViewManager;
		final boolean hdShader = layer.getLayerShader() == LayerShader.HD;
		listenersEnabled = false;
		this.layer = layer;
		this.undoActionListener = undoActionListener;
		this.modelStructureChangeListener = modelStructureChangeListener;
		layerFlagsPanel.setLayer(layer);
		filterModeDropdown.setSelectedItem(layer.getFilterMode());
		for (final ShaderTextureTypeHD shaderTextureTypeHD : ShaderTextureTypeHD.VALUES) {
			final JButton textureButton = textureTypeToButton.get(shaderTextureTypeHD);

			final Bitmap textureBitmap = layer.getShaderTextures().get(shaderTextureTypeHD);
			boolean hasIcon = false;
			if (textureBitmap != null) {
				final BufferedImage image = BLPHandler.getImage(textureBitmap, workingDirectory);
				if (image != null) {
					textureButton.setIcon(new ImageIcon(IconUtils.worldEditStyleIcon(image)));
					hasIcon = true;
				}
			}
			if (!hasIcon) {
				textureButton.setIcon(null);
			}
			textureButton.setText(layer.getTextureName(textureBitmap, "None"));
			if (shaderTextureTypeHD != ShaderTextureTypeHD.Diffuse) {
				textureButton.setVisible(hdShader);
			}
		}
		for (final JLabel label : nonDiffuseTextureLabels) {
			label.setVisible(hdShader);
		}
		coordIdSpinner.reloadNewValue(layer.getCoordId());
		tVertexAnimButton.setText(layer.getTextureAnim() == null ? "None"
				: "TextureAnim " + modelViewManager.getModel().getTextureAnimId(layer.getTextureAnim()));
		alphaPanel.reloadNewValue((float) layer.getStaticAlpha(), new Callback<Float>() {
			@Override
			public void run(final Float value) {
				layer.setStaticAlpha(value);
				modelStructureChangeListener.texturesChanged();
			}
		}, layer.getFlag("Alpha"), undoActionListener, modelStructureChangeListener);
		emissiveGainPanel.setVisible(ModelUtils.isEmissiveLayerSupported(formatVersion) && hdShader);
		emissiveGainPanel.reloadNewValue((float) layer.getEmissive(), new Callback<Float>() {
			@Override
			public void run(final Float value) {
				layer.setEmissive(value);
				modelStructureChangeListener.texturesChanged();
			}
		}, layer.getFlag("EmissiveGain"), undoActionListener, modelStructureChangeListener);
		final boolean fresnelColorLayerSupported = ModelUtils.isFresnelColorLayerSupported(formatVersion) && hdShader;
		fresnelColorPanel.setVisible(fresnelColorLayerSupported);
		fresnelColorPanel.reloadNewValue(layer.getFresnelColor(), new Callback<Vertex>() {
			@Override
			public void run(final Vertex value) {
				layer.setFresnelColor(value);
				modelStructureChangeListener.texturesChanged();
			}
		}, layer.getFlag("FresnelColor"), undoActionListener, modelStructureChangeListener);
		fresnelOpacityPanel.setVisible(fresnelColorLayerSupported);
		fresnelOpacityPanel.reloadNewValue((float) layer.getFresnelOpacity(), new Callback<Float>() {
			@Override
			public void run(final Float value) {
				layer.setFresnelOpacity(value);
				modelStructureChangeListener.texturesChanged();
			}
		}, layer.getFlag("FresnelOpacity"), undoActionListener, modelStructureChangeListener);
		fresnelTeamColor.setVisible(fresnelColorLayerSupported);
		fresnelTeamColor.reloadNewValue((float) layer.getFresnelTeamColor(), new Callback<Float>() {
			@Override
			public void run(final Float value) {
				layer.setFresnelTeamColor(value);
				modelStructureChangeListener.texturesChanged();
			}
		}, layer.getFlag("FresnelTeamColor"), undoActionListener, modelStructureChangeListener);
		shaderOptionComboBox.setSelectedIndex(layer.getLayerShader().ordinal());
		listenersEnabled = true;
	}
}
