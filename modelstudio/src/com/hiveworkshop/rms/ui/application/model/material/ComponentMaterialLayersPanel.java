package com.hiveworkshop.rms.ui.application.model.material;

import com.hiveworkshop.rms.editor.model.Layer;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelViewManager;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ComponentMaterialLayersPanel extends JPanel {
	public static final String[] REFORGED_LAYER_DEFINITIONS = {"Diffuse", "Vertex", "ORM", "Emissive", "Team Color",
			"Reflections"};
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
		setLayout(new MigLayout("fill", "[][][grow]"));
		addLayerButton = new JButton("Add Layer");
		addLayerButton.setBackground(HIGHLIGHT_BUTTON_BACKGROUND_COLOR);
		addLayerButton.setForeground(Color.WHITE);
	}

	public void setMaterial(final Material material, final ModelViewManager modelViewManager,
	                        final UndoActionListener undoActionListener,
	                        final ModelStructureChangeListener modelStructureChangeListener) {
		System.out.println("Reloading ComponentMaterialLayersPanel");

		this.material = material;
		this.undoActionListener = undoActionListener;
		this.modelStructureChangeListener = modelStructureChangeListener;
		final boolean hdShader = Material.SHADER_HD_DEFAULT_UNIT.equals(material.getShaderString());

		if (currentlyDisplayedLayerCount != material.getLayers().size()) {
			removeAll();
			createLayerPanels(material, modelViewManager, undoActionListener, modelStructureChangeListener, hdShader);
			add(addLayerButton, "wrap");
			revalidate();
			repaint();
			currentlyDisplayedLayerCount = material.getLayers().size();
		} else {
			replaceLayerPanels(material, modelViewManager, undoActionListener, modelStructureChangeListener, hdShader);

		}

	}

	private void replaceLayerPanels(Material material, ModelViewManager modelViewManager, UndoActionListener undoActionListener, ModelStructureChangeListener modelStructureChangeListener, boolean hdShader) {
		for (int i = 0; i < material.getLayers().size(); i++) {
			final Layer layer = material.getLayers().get(i);
			final ComponentLayerPanel panel = cachedLayerPanels.get(i);
			panel.setLayer(modelViewManager.getModel(), layer,
					modelViewManager.getModel().getFormatVersion(), hdShader, undoActionListener,
					modelStructureChangeListener);
		}
	}

	private void createLayerPanels(Material material, ModelViewManager modelViewManager, UndoActionListener undoActionListener, ModelStructureChangeListener modelStructureChangeListener, boolean hdShader) {
		for (int i = 0; i < material.getLayers().size(); i++) {
			final Layer layer = material.getLayers().get(i);
			final ComponentLayerPanel panel;
			final JLabel layerLabel;
			final JButton layerDeleteButton;
			if (i < cachedLayerPanels.size()) {
				panel = cachedLayerPanels.get(i);
				layerLabel = cachedLayerLabels.get(i);
				layerDeleteButton = cachedLayerDeleteButtons.get(i);
			} else {
				panel = new ComponentLayerPanel(modelViewManager.getModel());
				layerLabel = new JLabel("Layer");
				layerDeleteButton = new JButton("Delete");
				layerDeleteButton.setBackground(Color.RED);
				layerDeleteButton.setForeground(Color.WHITE);
				cachedLayerPanels.add(panel);
				cachedLayerLabels.add(layerLabel);
				cachedLayerDeleteButtons.add(layerDeleteButton);
			}
			if (hdShader) {
				final String reforgedDefintion;
				if (i < REFORGED_LAYER_DEFINITIONS.length) {
					reforgedDefintion = REFORGED_LAYER_DEFINITIONS[i];
				} else {
					reforgedDefintion = "Unknown";
				}
				layerLabel.setText(reforgedDefintion + " Layer");
				layerLabel.setFont(layerLabel.getFont().deriveFont(Font.BOLD));
			} else {
				layerLabel.setText("Layer " + (i + 1));
				layerLabel.setFont(layerLabel.getFont().deriveFont(Font.PLAIN));
			}
			panel.setLayer(modelViewManager.getModel(), layer,
					modelViewManager.getModel().getFormatVersion(), hdShader, undoActionListener,
					modelStructureChangeListener);

			add(layerLabel);
			add(layerDeleteButton, "wrap");
			add(panel, "growx, growy, span 3, wrap");
		}
	}
}
