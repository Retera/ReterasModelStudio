package com.hiveworkshop.rms.ui.application.model.material;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.ui.application.model.OverviewPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MaterialOverviewPanel extends OverviewPanel {
	private final JPanel infoPanel;

	public MaterialOverviewPanel(ModelHandler modelHandler) {
		super(modelHandler, new MigLayout("fill, ins 0", "[grow]", "[grow]"));
		infoPanel = new JPanel(new MigLayout("wrap 9", "[]10[Right]10[Right]10[Right]10[Right]20[Right]10[Right]10[Right]10[Right]", ""));

		fillInfoPanel();

		add(infoPanel, "growx, growy");
	}

	private void fillInfoPanel() {
		infoPanel.add(new JLabel("Material"));
		infoPanel.add(new JLabel("Geosets"));
		infoPanel.add(new JLabel("Ribbons"));
		infoPanel.add(new JLabel("PriorityPlane"));
		infoPanel.add(new JLabel("Layers"));
		infoPanel.add(new JLabel("TVertexAnim"));
		infoPanel.add(new JLabel("Animated Alpha"));
		infoPanel.add(new JLabel("FlipBook"));
		infoPanel.add(new JLabel("Other anim"));
		EditableModel model = modelHandler.getModel();

		Map<MaterialKey, List<Geoset>> materialToGeosets = new LinkedHashMap<>();
		Map<MaterialKey, List<RibbonEmitter>> materialToRibbons = new LinkedHashMap<>();
		model.getMaterials().forEach(m -> {
			MaterialKey key = new MaterialKey(m);
			materialToGeosets.put(key, new ArrayList<>());
			materialToRibbons.put(key, new ArrayList<>());
		});
		model.getGeosets().forEach(g -> materialToGeosets.get(new MaterialKey(g.getMaterial())).add(g));
		model.getRibbonEmitters().forEach(r -> materialToRibbons.get(new MaterialKey(r.getMaterial())).add(r));

		for (MaterialKey materialkey : materialToGeosets.keySet()) {
			Material material = materialkey.material;
			int gUsers = materialToGeosets.get(materialkey).size();
			int rUsers = materialToRibbons.get(materialkey).size();
			infoPanel.add(new JLabel("# " + model.computeMaterialID(material) + " " + material.getName()));
			infoPanel.add(new JLabel("" + gUsers));
			infoPanel.add(new JLabel("" + rUsers));
			infoPanel.add(new JLabel("" + material.getPriorityPlane()));
			infoPanel.add(new JLabel("" + material.getLayers().size()));
			infoPanel.add(new JLabel("" + (material.getLayers().stream().anyMatch(Layer::hasTexAnim) ? "yes" : "no")));
			infoPanel.add(new JLabel("" + (material.getLayers().stream().anyMatch(l -> l.getVisibilityFlag() != null) ? "yes" : "no")));
			infoPanel.add(new JLabel("" + (material.getLayers().stream().anyMatch(l -> l.getTextureSlots().stream().anyMatch(t -> t.getFlipbookTexture() != null)) ? "yes" : "no")));
			infoPanel.add(new JLabel("" + (otherAnimated(material) ? "yes" : "no")));
		}
	}

	private boolean otherAnimated(Material material) {
		for (Layer layer : material.getLayers()) {
			int animCounts = layer.getAnimFlags().size();
			animCounts -= layer.getVisibilityFlag() == null ? 0 : 1;
			if (animCounts != 0) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void update() {
		infoPanel.removeAll();
		fillInfoPanel();
		revalidate();
		repaint();
	}

	private static class MaterialKey  {
		Material material;
		MaterialKey(Material material) {
			this.material = material;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof MaterialKey) {
				return ((MaterialKey) obj).material == material;
			} else if (obj instanceof Material) {
				return obj == material;
			}
			return false;
		}

		@Override
		public int hashCode() {
			return material.hashCode();
		}
	}
}
