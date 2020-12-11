package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelViewManager;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComponentGeosetPanel extends JPanel implements ComponentPanel {
	DefaultListModel<Material> materialList = new DefaultListModel<>();
	//	private static final String SD = "SD";
//	private static final String HD = "HD";
	private JComboBox<String> materialChooser;
	private Geoset geoset;
	//	private final Set<ComponentGeosetMaterialPanel> materialPanels;
//	private Material material;
	private UndoActionListener undoActionListener;
	private ModelStructureChangeListener modelStructureChangeListener;
	private ComponentGeosetMaterialPanel materialPanel;
	private Map<Geoset, ComponentGeosetMaterialPanel> materialPanels;
	private boolean listenersEnabled = true;

	private boolean listenForChanges = true;

	public ComponentGeosetPanel() {
		materialPanels = new HashMap<>();
//		setLayout(new MigLayout("fill", "", "[fill][fill]"));
		setLayout(new MigLayout());
//		add(new JLabel("Material:"), "wrap, span 1");
		add(new JLabel("Material:"), "wrap");
		materialPanel = new ComponentGeosetMaterialPanel();
//		materialPanels = new HashSet<>();
//		List<String> textures = new ArrayList<>();
//		textures.add("Choose Texture");

//		materialChooser = new JComboBox<String>(getMaterials(model));
//		materialChooser.addActionListener(e -> chooseMaterial(geoset));
//		add(materialChooser, "wrap, growx");
//		add(materialPanel, "wrap, growx");
//		add(materialPanel, "span 1");
		add(materialPanel);
	}


	private String[] getMaterials(Geoset geoset) {
		materialList.addAll(geoset.getParentModel().getMaterials());

		List<String> materialNames = new ArrayList<>();
		for (Material material : geoset.getParentModel().getMaterials()) {
			materialNames.add(material.getName());
		}
//		String[] materialNames = {"ugg"};
//		return materialNames;
		return materialNames.toArray(String[]::new);
	}

	private void chooseMaterial(Geoset geoset) {
		if (listenersEnabled) {
			System.out.println(materialChooser.getSelectedIndex());
			Material material = materialList.get(materialChooser.getSelectedIndex());
			geoset.setMaterial(material);
//			final SetBitmapPathAction setBitmapPathAction = new SetBitmapPathAction(bitmap, layer.getTextureBitmap().getPath(), bitmap.getPath(), modelStructureChangeListener);
//			layer.setTexture(bitmap);
//			layer.setTextureId(materialChooser.getSelectedIndex());
//			setBitmapPathAction.redo();
//			undoActionListener.pushAction(setBitmapPathAction);
		}
	}


	public void setSelectedGeoset(final Geoset geoset, final ModelViewManager modelViewManager,
	                              final UndoActionListener undoActionListener,
	                              final ModelStructureChangeListener modelStructureChangeListener) {
		this.geoset = geoset;
		this.undoActionListener = undoActionListener;
		this.modelStructureChangeListener = modelStructureChangeListener;
		remove(materialPanel);

		materialPanels.putIfAbsent(geoset, new ComponentGeosetMaterialPanel());
		materialPanel = materialPanels.get(geoset);

		materialPanel.setMaterialChooser(geoset, modelViewManager, undoActionListener, modelStructureChangeListener);
		add(materialPanel);
		revalidate();
		repaint();

//		materialChooser = new JComboBox<String>(getMaterials(geoset));
//		materialChooser.addActionListener(e -> chooseMaterial(geoset));
//		add(materialChooser, "wrap, growx");
	}


	@Override
	public void save(final EditableModel model, final UndoActionListener undoListener,
	                 final ModelStructureChangeListener changeListener) {
	}

}
