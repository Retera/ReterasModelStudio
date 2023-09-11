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
//		infoPanel = new JPanel(new MigLayout("wrap 6", "[]10[Right]10[Right]10[Right]20[Left]10[Right]", ""));
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
//		infoPanel.add(new JLabel("Animated"));
		infoPanel.add(new JLabel("TVertexAnim"));
		infoPanel.add(new JLabel("Animated Alpha"));
		infoPanel.add(new JLabel("FlipBook"));
		infoPanel.add(new JLabel("Other anim"));
//		infoPanel.add(new JLabel("Material"));
//		infoPanel.add(new JLabel("GeosetAnim"));
//		int verts = 0;
//		int tris = 0;
		EditableModel model = modelHandler.getModel();

		Map<Material, List<Geoset>> materialToGeosets = new LinkedHashMap<>();
		model.getMaterials().forEach(m -> materialToGeosets.put(m, new ArrayList<>()));
		model.getGeosets().forEach(g -> materialToGeosets.get(g.getMaterial()).add(g));

		Map<Material, List<RibbonEmitter>> materialToRibbons = new LinkedHashMap<>();
		model.getMaterials().forEach(m -> materialToRibbons.put(m, new ArrayList<>()));
		model.getRibbonEmitters().forEach(r -> materialToRibbons.get(r.getMaterial()).add(r));

		for (Material material : materialToGeosets.keySet()) {
			int gUsers = materialToGeosets.get(material).size();
			int rUsers = materialToRibbons.get(material).size();
			infoPanel.add(new JLabel("# " + model.computeMaterialID(material) + " " + material.getName()));
			infoPanel.add(new JLabel("" + gUsers));
			infoPanel.add(new JLabel("" + rUsers));
			infoPanel.add(new JLabel("" + material.getPriorityPlane()));
			infoPanel.add(new JLabel("" + material.getLayers().size()));
			infoPanel.add(new JLabel("" + (material.getLayers().stream().anyMatch(Layer::hasTexAnim) ? "yes" : "no")));
			infoPanel.add(new JLabel("" + (material.getLayers().stream().anyMatch(l -> l.getVisibilityFlag() != null) ? "yes" : "no")));
			infoPanel.add(new JLabel("" + (material.getLayers().stream().anyMatch(l -> l.getTextureSlots().stream().anyMatch(t -> t.getFlipbookTexture() != null)) ? "yes" : "no")));
//			infoPanel.add(new JLabel("" + (material.getLayers().stream().anyMatch(l -> !l.getAnimFlags().isEmpty()) ? "yes" : "no")));
			infoPanel.add(new JLabel("" + (otherAnimated(material) ? "yes" : "no")));

//			infoPanel.add(new JLabel("# " + model.computeMaterialID(material) + " " + material.getName()));
//			infoPanel.add(new JLabel("" + (geoset.hasAnim() ? "yes" : "no")));
		}

//		infoPanel.add(new JLabel("Total"), "gapy 10");
//		infoPanel.add(new JLabel("" + verts));
//		infoPanel.add(new JLabel("" + tris));
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
}
