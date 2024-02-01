package com.hiveworkshop.rms.ui.application.model.material;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.ui.application.model.OverviewPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.HashSet;
import java.util.Set;

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

		Set<Geoset> geosets = new HashSet<>(model.getGeosets());
		Set<RibbonEmitter> ribbons = new HashSet<>(model.getRibbonEmitters());

		for (Material material : model.getMaterials()) {
			infoPanel.add(new JLabel("# " + model.computeMaterialID(material) + " " + material.getName()));
			infoPanel.add(new JLabel("" + countGeosets(material, geosets)));
			infoPanel.add(new JLabel("" + countRibbons(material, ribbons)));
			infoPanel.add(new JLabel("" + material.getPriorityPlane()));
			infoPanel.add(new JLabel("" + material.getLayers().size()));
			infoPanel.add(new JLabel("" + (material.getLayers().stream().anyMatch(Layer::hasTexAnim) ? "yes" : "no")));
			infoPanel.add(new JLabel("" + (material.getLayers().stream().anyMatch(l -> l.getVisibilityFlag() != null) ? "yes" : "no")));
			infoPanel.add(new JLabel("" + (material.getLayers().stream().anyMatch(l -> l.getTextureSlots().stream().anyMatch(t -> t.getFlipbookTexture() != null)) ? "yes" : "no")));
			infoPanel.add(new JLabel("" + (otherAnimated(material) ? "yes" : "no")));
		}
		if (!geosets.isEmpty()) {
			System.err.println("The model's material list was missing " + geosets.size() + " material(s) used by geosets");
		}
		if (!ribbons.isEmpty()) {
			System.err.println("The model's material list was missing " + ribbons.size() + " material(s) used by ribbon emitters");
		}
	}

	private int countGeosets(Material material, Set<Geoset> geosets){
		int count = geosets.size();
		geosets.removeIf(g -> g.getMaterial() == material);
		return count - geosets.size();
	}
	private int countRibbons(Material material, Set<RibbonEmitter> ribbons) {
		int count = ribbons.size();
		ribbons.removeIf(r -> r.getMaterial() == material);
		return count - ribbons.size();
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
