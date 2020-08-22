package com.hiveworkshop.wc3.gui.modeledit.components.material;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SpinnerNumberModel;

import com.etheller.warsmash.parsers.mdlx.MdlxLayer.FilterMode;
import com.hiveworkshop.wc3.gui.BLPHandler;
import com.hiveworkshop.wc3.gui.datachooser.DataSource;
import com.hiveworkshop.wc3.gui.modeledit.actions.componenttree.material.SetLayerFilterModeAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.activity.UndoActionListener;
import com.hiveworkshop.wc3.gui.modeledit.components.editors.ColorValuePanel;
import com.hiveworkshop.wc3.gui.modeledit.components.editors.ComponentEditorJSpinner;
import com.hiveworkshop.wc3.gui.modeledit.components.editors.FloatValuePanel;
import com.hiveworkshop.wc3.mdl.Layer;
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
		this.layerFlagsPanel = new LayerFlagsPanel();
		this.layerFlagsPanel.setBorder(BorderFactory.createTitledBorder("Flags"));
		add(leftHandSettingsPanel);
		add(this.layerFlagsPanel);
		leftHandSettingsPanel.setLayout(new MigLayout());
		leftHandSettingsPanel.add(new JLabel("Filter Mode:"));
		this.filterModeDropdown = new JComboBox<FilterMode>(FilterMode.values());
		this.filterModeDropdown.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (ComponentLayerPanel.this.listenersEnabled) {
					final SetLayerFilterModeAction setLayerFilterModeAction = new SetLayerFilterModeAction(
							ComponentLayerPanel.this.layer, ComponentLayerPanel.this.layer.getFilterMode(),
							((FilterMode) ComponentLayerPanel.this.filterModeDropdown.getSelectedItem()),
							ComponentLayerPanel.this.modelStructureChangeListener);
					setLayerFilterModeAction.redo();
					ComponentLayerPanel.this.undoActionListener.pushAction(setLayerFilterModeAction);
				}
			}
		});
		leftHandSettingsPanel.add(this.filterModeDropdown, "wrap, growx");
		leftHandSettingsPanel.add(new JLabel("Texture:"));
		this.textureButton = new JButton("Choose Texture");
		this.textureButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {

			}
		});
		leftHandSettingsPanel.add(this.textureButton, "wrap, growx");
		this.coordIdSpinner = new ComponentEditorJSpinner(
				new SpinnerNumberModel(0, Integer.MIN_VALUE, Integer.MAX_VALUE, 1));
		leftHandSettingsPanel.add(new JLabel("TVertex Anim:"));
		this.tVertexAnimButton = new JButton("Choose TVertex Anim");
		leftHandSettingsPanel.add(this.tVertexAnimButton, "wrap, growx");
		leftHandSettingsPanel.add(new JLabel("CoordID:"));
		leftHandSettingsPanel.add(this.coordIdSpinner, "wrap, growx");

		this.alphaPanel = new FloatValuePanel("Alpha");
		leftHandSettingsPanel.add(this.alphaPanel, "wrap, span 2");
		this.emissiveGainPanel = new FloatValuePanel("Emissive Gain");
		leftHandSettingsPanel.add(this.emissiveGainPanel, "wrap, span 2, hidemode 2");
		this.fresnelColorPanel = new ColorValuePanel("Fresnel Color");
		leftHandSettingsPanel.add(this.fresnelColorPanel, "wrap, span 2, hidemode 2");
		this.fresnelOpacityPanel = new FloatValuePanel("Fresnel Opacity");
		leftHandSettingsPanel.add(this.fresnelOpacityPanel, "wrap, span 2, hidemode 2");
		this.fresnelTeamColor = new FloatValuePanel("Fresnel Team Color");
		leftHandSettingsPanel.add(this.fresnelTeamColor, "wrap, span 2, hidemode 2");

	}

	public void setLayer(final DataSource workingDirectory, final Layer layer, final int formatVersion,
			final boolean hdShader, final UndoActionListener undoActionListener,
			final ModelStructureChangeListener modelStructureChangeListener) {
		this.listenersEnabled = false;
		this.layer = layer;
		this.undoActionListener = undoActionListener;
		this.modelStructureChangeListener = modelStructureChangeListener;
		this.layerFlagsPanel.setLayer(layer);
		this.filterModeDropdown.setSelectedItem(layer.getFilterMode());
		this.textureButton.setIcon(new ImageIcon(
				IconUtils.worldEditStyleIcon(BLPHandler.getImage(layer.getTextureBitmap(), workingDirectory))));
		this.coordIdSpinner.reloadNewValue(layer.getCoordId());
		this.tVertexAnimButton.setText(layer.getTextureAnim() == null ? "None" : layer.getTextureAnim().toString());
		this.alphaPanel.reloadNewValue((float) layer.getStaticAlpha(), layer.find("Alpha"));
		this.emissiveGainPanel.setVisible(ModelUtils.isEmissiveLayerSupported(formatVersion) && hdShader);
		this.emissiveGainPanel.reloadNewValue((float) layer.getEmissive(), layer.find("EmissiveGain"));
		final boolean fresnelColorLayerSupported = ModelUtils.isFresnelColorLayerSupported(formatVersion) && hdShader;
		this.fresnelColorPanel.setVisible(fresnelColorLayerSupported);
		this.fresnelColorPanel.reloadNewValue(layer.getFresnelColor(), layer.find("FresnelColor"));
		this.fresnelOpacityPanel.setVisible(fresnelColorLayerSupported);
		this.fresnelOpacityPanel.reloadNewValue((float) layer.getFresnelOpacity(), layer.find("FresnelOpacity"));
		this.fresnelTeamColor.setVisible(fresnelColorLayerSupported);
		this.fresnelTeamColor.reloadNewValue((float) layer.getFresnelTeamColor(), layer.find("FresnelTeamColor"));
		this.listenersEnabled = true;
	}
}
