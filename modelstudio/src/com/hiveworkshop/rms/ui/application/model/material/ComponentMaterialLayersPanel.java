package com.hiveworkshop.rms.ui.application.model.material;

import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.Layer;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelViewManager;
import com.hiveworkshop.rms.ui.application.actions.model.material.AddLayerAction;
import com.hiveworkshop.rms.ui.application.actions.model.material.RemoveLayerAction;
import com.hiveworkshop.rms.ui.application.actions.model.material.RemoveMaterialAction;
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
	private ModelViewManager modelViewManager;
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
		addLayerButton.addActionListener(e -> addLayer());
	}

	public void setMaterial(final Material material, final ModelViewManager modelViewManager,
	                        final UndoActionListener undoActionListener,
	                        final ModelStructureChangeListener modelStructureChangeListener) {
		System.out.println("Reloading ComponentMaterialLayersPanel");

		this.material = material;
		this.modelViewManager = modelViewManager;
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
				layerDeleteButton.addActionListener(e -> removeLayer(layer));
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

	private void addLayer() {
		AddLayerAction addLayerAction = new AddLayerAction(new Layer("None", 0), material, modelStructureChangeListener);
		undoActionListener.pushAction(addLayerAction);
		addLayerAction.redo();
//		setMaterial(material, modelViewManager, undoActionListener, modelStructureChangeListener);
	}

	private void removeLayer(Layer layer) {
		boolean doRemove = true;
		if (material.getLayers().size() <= 1) {
			doRemove = false;
			List<Geoset> geosetList = modelViewManager.getModel().getGeosets();
			int numUses = 0;
			for (Geoset geoset : geosetList) {
//				if (geoset.getMaterial().equals(material)) {
				if (geoset.getMaterial() == material) {

					System.out.println("is super same? " + (geoset.getMaterial() == material));
					numUses++;
				}
			}
//			List<Material> materials = modelViewManager.getModel().getMaterials();
			if (numUses > 0) {
//				JOptionPane.showMessageDialog(this, "Removing this layer is not possible since it would\nremove a material used by " + numUses + " geosets.");
				JOptionPane.showMessageDialog(this, "Cannot delete material as it is being used by " + numUses + " geosets.");
				//TODO check if it's really being used, maybe by checking ComponentGeosetMaterialPanel#MaterialChooser#index
			} else {
//				int removeLayer = JOptionPane.showConfirmDialog(this, "Removing this layer will remove the material.\nDo you want to remove the material?", "Remove material", JOptionPane.YES_NO_OPTION);
//				if (removeLayer == 0){
//				doRemove = true;
				removeMaterial();
//				}
			}
		}
		if (doRemove) {
			RemoveLayerAction removeLayerAction = new RemoveLayerAction(layer, material, modelStructureChangeListener);
			undoActionListener.pushAction(removeLayerAction);
			removeLayerAction.redo();
//			setMaterial(material, modelViewManager, undoActionListener, modelStructureChangeListener);
		}
	}

	private void removeMaterial() {
		RemoveMaterialAction removeMaterialAction = new RemoveMaterialAction(material, modelViewManager, modelStructureChangeListener);
		undoActionListener.pushAction(removeMaterialAction);
		removeMaterialAction.redo();
//		this.getParent().remove(this);
	}
}
