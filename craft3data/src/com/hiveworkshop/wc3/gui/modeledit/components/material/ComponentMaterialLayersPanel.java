package com.hiveworkshop.wc3.gui.modeledit.components.material;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.activity.UndoActionListener;
import com.hiveworkshop.wc3.mdl.Layer;
import com.hiveworkshop.wc3.mdl.Material;
import com.hiveworkshop.wc3.mdl.v2.ModelViewManager;

import net.miginfocom.swing.MigLayout;

public class ComponentMaterialLayersPanel extends JPanel {
	public static final String[] REFORGED_LAYER_DEFINITIONS = { "Diffuse", "Normal", "ORM", "Emissive", "Team Color",
			"Reflections" };
	private static final Color HIGHLIGHT_BUTTON_BACKGROUND_COLOR = new Color(100, 118, 135);
	private Material material;
	private UndoActionListener undoActionListener;
	private ModelStructureChangeListener modelStructureChangeListener;
	private final JButton addLayerButton;
	private final List<ComponentLayerPanel> cachedLayerPanels = new ArrayList<>();
	private final List<JLabel> cachedLayerLabels = new ArrayList<>();
	private final List<JButton> cachedLayerDeleteButtons = new ArrayList<>();
	private int currentlyDisplayedLayerCount = 0;

	public ComponentMaterialLayersPanel() {
		setLayout(new MigLayout());
		addLayerButton = new JButton("Add Layer");
		addLayerButton.setBackground(HIGHLIGHT_BUTTON_BACKGROUND_COLOR);
		addLayerButton.setForeground(Color.WHITE);
	}

	public void setMaterial(final Material material, final ModelViewManager modelViewManager,
			final UndoActionListener undoActionListener,
			final ModelStructureChangeListener modelStructureChangeListener) {
		this.material = material;
		this.undoActionListener = undoActionListener;
		this.modelStructureChangeListener = modelStructureChangeListener;
		final boolean hdShader = false;// Material.SHADER_HD_DEFAULT_UNIT.equals(material.getShaderString());

		if (currentlyDisplayedLayerCount != material.getLayers().size()) {
			removeAll();
			for (int i = 0; i < material.getLayers().size(); i++) {
				final Layer layer = material.getLayers().get(i);
				ComponentLayerPanel panel;
				JLabel layerLabel;
				JButton layerDeleteButton;
				if (i < cachedLayerPanels.size()) {
					panel = cachedLayerPanels.get(i);
					layerLabel = cachedLayerLabels.get(i);
					layerDeleteButton = cachedLayerDeleteButtons.get(i);
				}
				else {
					panel = new ComponentLayerPanel();
					layerLabel = new JLabel("Layer");
					layerDeleteButton = new JButton("Delete");
					layerDeleteButton.setBackground(Color.RED);
					layerDeleteButton.setForeground(Color.WHITE);
					cachedLayerPanels.add(panel);
					cachedLayerLabels.add(layerLabel);
					cachedLayerDeleteButtons.add(layerDeleteButton);
				}
				if (hdShader) {
					String reforgedDefintion;
					if (i < REFORGED_LAYER_DEFINITIONS.length) {
						reforgedDefintion = REFORGED_LAYER_DEFINITIONS[i];
					}
					else {
						reforgedDefintion = "Unknown";
					}
					layerLabel.setText(reforgedDefintion + " Layer");
					layerLabel.setFont(layerLabel.getFont().deriveFont(Font.BOLD));
				}
				else {
					layerLabel.setText("Layer " + (i + 1));
					layerLabel.setFont(layerLabel.getFont().deriveFont(Font.PLAIN));
				}
				panel.setLayer(modelViewManager.getModel().getWrappedDataSource(), layer,
						modelViewManager.getModel().getFormatVersion(), undoActionListener,
						modelStructureChangeListener, modelViewManager);
				add(layerLabel);
				add(layerDeleteButton, "wrap");
				add(panel, "growx, growy, span 2, wrap");
			}
			add(addLayerButton, "wrap");
			revalidate();
			repaint();
			currentlyDisplayedLayerCount = material.getLayers().size();
		}
		else {
			for (int i = 0; i < material.getLayers().size(); i++) {
				final Layer layer = material.getLayers().get(i);
				final ComponentLayerPanel panel = cachedLayerPanels.get(i);
				panel.setLayer(modelViewManager.getModel().getWrappedDataSource(), layer,
						modelViewManager.getModel().getFormatVersion(), undoActionListener,
						modelStructureChangeListener, modelViewManager);
			}

		}

	}
}
