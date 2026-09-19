package com.hiveworkshop.wc3.gui.modeledit.components.cards;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;

import com.hiveworkshop.wc3.gui.modeledit.components.ExtLogEditor;
import com.hiveworkshop.wc3.mdl.ExtLog;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.GeosetAnim;
import com.hiveworkshop.wc3.mdl.Material;

import net.miginfocom.swing.MigLayout;

public class ComponentGeosetCard extends ComponentCard<Geoset> {
	private static final String UNSELECTABLE = "Unselectable";
	private final ExtLogEditor extentsEditor;
	private final JButton openGeosetAnim;

	public ComponentGeosetCard() {
		addInfo("Geoset", geoset -> geoset.getUIName(model()));
		addInfo("Size", geoset -> geoset.getVertices().size() + " vertices, " + geoset.getTriangles().size()
				+ " triangles, " + geoset.getMatrix().size() + " matrices");
		addTextField("LoD Name", Geoset::getLevelOfDetailName, Geoset::setLevelOfDetailName);
		addIntSpinner("Level of Detail", 0, Integer.MAX_VALUE, Geoset::getLevelOfDetail, Geoset::setLevelOfDetail);
		addComboBox("Material", this::materialOptions, this::describeMaterial, Geoset::getMaterial,
				Geoset::setMaterial);
		addIntSpinner("Selection Group", 0, Integer.MAX_VALUE, Geoset::getSelectionGroup, Geoset::setSelectionGroup);
		addFlagCheckBox(UNSELECTABLE, Geoset::getFlags);

		beginSection("Extents");
		extentsEditor = new ExtLogEditor();
		extentsEditor.addActionListener(() -> {
			final Geoset target = item;
			extentsEditor.commitEdits();
			apply("Geoset Extents", target.getExtents(), extentsEditor.getExtLog(), target::setExtents);
		});
		addWide(extentsEditor);
		final JButton recalculate = addButton("Recalculate from vertices", () -> {
			final Geoset target = item;
			apply("Geoset Extents", target.getExtents(), target.calculateExtent(), target::setExtents);
		});
		addWide(recalculate, "align right");
		endSection();

		beginSection("Geoset Animation");
		final JPanel row = new JPanel(new MigLayout("insets 0", "[grow][]", ""));
		openGeosetAnim = new JButton("Open Geoset Animation");
		openGeosetAnim.addActionListener(e -> {
			final GeosetAnim anim = geosetAnimOf(item);
			if (anim != null) {
				navigationListener.openInModelTab(anim);
			}
		});
		row.add(openGeosetAnim);
		addWide(row);
		endSection();
	}

	@Override
	protected void onReload() {
		extentsEditor.setExtLog(item.getExtents() == null ? new ExtLog(ExtLog.NO_BOUNDS_RADIUS) : item.getExtents());
		final GeosetAnim anim = geosetAnimOf(item);
		openGeosetAnim.setEnabled(anim != null);
		openGeosetAnim.setText(anim != null ? "Open Geoset Animation" : "No geoset animation");
	}

	private GeosetAnim geosetAnimOf(final Geoset geoset) {
		for (final GeosetAnim anim : model().getGeosetAnims()) {
			if (anim.getGeoset() == geoset) {
				return anim;
			}
		}
		return geoset.getGeosetAnim();
	}

	private List<Material> materialOptions() {
		return new ArrayList<>(model().getMaterials());
	}

	private String describeMaterial(final Material material) {
		return "Material " + model().getMaterials().indexOf(material) + ": " + material.getName();
	}
}
