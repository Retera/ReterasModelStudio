package com.hiveworkshop.wc3.gui.modeledit.components.material;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpinnerNumberModel;

import com.hiveworkshop.wc3.gui.BLPHandler;
import com.hiveworkshop.wc3.gui.datachooser.DataSource;
import com.hiveworkshop.wc3.gui.modeledit.actions.componenttree.material.SetLayerFilterModeAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.activity.UndoActionListener;
import com.hiveworkshop.wc3.gui.modeledit.components.editors.ColorValuePanel;
import com.hiveworkshop.wc3.gui.modeledit.components.editors.ComponentEditorJSpinner;
import com.hiveworkshop.wc3.gui.modeledit.components.editors.FloatValuePanel;
import com.hiveworkshop.wc3.mdl.Layer;
import com.hiveworkshop.wc3.mdl.Layer.FilterMode;
import com.hiveworkshop.wc3.util.IconUtils;
import com.hiveworkshop.wc3.util.ModelUtils;

import net.miginfocom.swing.MigLayout;

public class ComponentLayerPanel extends JPanel {
	private final JComboBox<FilterMode> filterModeDropdown;
	private final JButton textureButton;
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
							layer.getFilterMode(), ((FilterMode) filterModeDropdown.getSelectedItem()),
							modelStructureChangeListener);
					setLayerFilterModeAction.redo();
					undoActionListener.pushAction(setLayerFilterModeAction);
				}
			}
		});
		leftHandSettingsPanel.add(filterModeDropdown, "wrap, growx");
		leftHandSettingsPanel.add(new JLabel("Texture:"));
		textureButton = new JButton("Choose Texture");
		textureButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {

			}
		});
		leftHandSettingsPanel.add(textureButton, "wrap, growx");
		coordIdSpinner = new ComponentEditorJSpinner(
				new SpinnerNumberModel(0, Integer.MIN_VALUE, Integer.MAX_VALUE, 1));
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

	public void setLayer(final DataSource workingDirectory, final Layer layer, final int formatVersion,
			final boolean hdShader, final UndoActionListener undoActionListener,
			final ModelStructureChangeListener modelStructureChangeListener) {
		listenersEnabled = false;
		this.layer = layer;
		this.undoActionListener = undoActionListener;
		this.modelStructureChangeListener = modelStructureChangeListener;
		layerFlagsPanel.setLayer(layer);
		filterModeDropdown.setSelectedItem(layer.getFilterMode());
		if (layer.getTextureBitmap() != null) {
			final BufferedImage image = BLPHandler.getImage(layer.getTextureBitmap(), workingDirectory);
			if (image != null) {
				textureButton.setIcon(new ImageIcon(IconUtils.worldEditStyleIcon(image)));
			}
		}
		textureButton.setText(layer.getName());
		coordIdSpinner.reloadNewValue(layer.getCoordId());
		tVertexAnimButton.setText(layer.getTextureAnim() == null ? "None" : layer.getTextureAnim().toString());
		alphaPanel.reloadNewValue((float) layer.getStaticAlpha(), layer.getFlag("Alpha"));
		emissiveGainPanel.setVisible(ModelUtils.isEmissiveLayerSupported(formatVersion) && hdShader);
		emissiveGainPanel.reloadNewValue((float) layer.getEmissive(), layer.getFlag("EmissiveGain"));
		final boolean fresnelColorLayerSupported = ModelUtils.isFresnelColorLayerSupported(formatVersion) && hdShader;
		fresnelColorPanel.setVisible(fresnelColorLayerSupported);
		fresnelColorPanel.reloadNewValue(layer.getFresnelColor(), layer.getFlag("FresnelColor"));
		fresnelOpacityPanel.setVisible(fresnelColorLayerSupported);
		fresnelOpacityPanel.reloadNewValue((float) layer.getFresnelOpacity(), layer.getFlag("FresnelOpacity"));
		fresnelTeamColor.setVisible(fresnelColorLayerSupported);
		fresnelTeamColor.reloadNewValue((float) layer.getFresnelTeamColor(), layer.getFlag("FresnelTeamColor"));
		listenersEnabled = true;
	}
}
