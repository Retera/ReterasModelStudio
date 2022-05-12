package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.util.Mesh;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.ui.application.MainPanel;
import com.hiveworkshop.rms.ui.application.ModelLoader;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.icons.RMSIcons;
import com.hiveworkshop.rms.ui.language.TextKey;
import com.hiveworkshop.rms.util.SmartButtonGroup;
import com.hiveworkshop.rms.util.Vec3;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class CreateNewModel extends ActionFunction{
	public CreateNewModel(){
		super(TextKey.NEW, () -> newModel(), "control N");
	}

	public static void newModel() {
		JPanel newModelPanel = new JPanel(new MigLayout("fill, ins 0"));
		newModelPanel.add(new JLabel("Model Name: "), "");
		JTextField newModelNameField = new JTextField("MrNew", 25);
		newModelPanel.add(newModelNameField, "wrap");

		JPanel optionPanel = new JPanel(new MigLayout("ins 0","",""));
		optionPanel.add(new JLabel("Segments"));
		JSpinner segmentSpinner = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
		optionPanel.add(segmentSpinner);
		optionPanel.setVisible(false);


		SmartButtonGroup typeGroup = new SmartButtonGroup();
		for(type t : type.values()){
			typeGroup.addJRadioButton("Create " + t.getName(), e -> optionPanel.setVisible(t.isShowOptions()));
		}
		typeGroup.setSelectedIndex(0);

		newModelPanel.add(typeGroup.getButtonPanel(), "wrap");
		newModelPanel.add(optionPanel, "spanx");

		MainPanel mainPanel = ProgramGlobals.getMainPanel();

		int userDialogResult = JOptionPane.showConfirmDialog(mainPanel, newModelPanel, "New Model", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (userDialogResult == JOptionPane.OK_OPTION) {
			int segments = ((Number) segmentSpinner.getValue()).intValue();

			type selected = type.values()[typeGroup.getSelectedIndex()];
			Mesh mesh = getMesh(selected, new Vec3(64, 64, 0), new Vec3(-64, -64, 128), segments);
			createModel(newModelNameField, mesh, selected.getName());
		}
	}

	private static Mesh getMesh(type t, Vec3 max, Vec3 min, int segments){
		return switch (t) {
			case EMPTY -> null;
			case PLANE -> ModelUtils.createPlane((byte) 2, true, 0, min.getProjected((byte) 0, (byte) 1), max.getProjected((byte) 0, (byte) 1), segments);
			case BOX -> ModelUtils.getBoxMesh(min, max, segments, segments, segments);
		};
	}

	private static void createModel(JTextField newModelNameField, Mesh mesh, String name) {
		EditableModel model = new EditableModel(newModelNameField.getText());
		Bone bone = new Bone("Root");
		model.add(bone);

		if(mesh != null){
			Geoset geoset = getGeoset(bone, mesh);
			geoset.setName(name);
			geoset.setMaterial(ModelUtils.getWhiteMaterial(model));

			addGeoset(geoset, model);
		}
		model.setExtents(new ExtLog(128).setDefault());

		ModelPanel temp = new ModelPanel(new ModelHandler(model, RMSIcons.MDLIcon));
		ModelLoader.loadModel(true, true, temp);
	}

	private static Geoset getGeoset(Bone bone, Mesh mesh) {
		Geoset geoset = new Geoset();

		for (GeosetVertex vertex : mesh.getVertices()) {
			vertex.setGeoset(geoset);
			vertex.addBoneAttachment(bone);
			geoset.add(vertex);
		}
		for (Triangle triangle : mesh.getTriangles()) {
			triangle.setGeoset(geoset);
			geoset.add(triangle);
		}
		return geoset;
	}

	private static void addGeoset(Geoset geoset, EditableModel model) {
		model.add(geoset);
		Material material = geoset.getMaterial();
		if (!model.contains(material)) {
			model.add(material);
			for (Layer layer : material.getLayers()){
				Bitmap bitmap = layer.getTextureBitmap();
				if(model.contains(bitmap)){
					model.add(bitmap);
				}
			}
		}
	}

	enum type {
		EMPTY("Empty", false),
		PLANE("Plane", true),
		BOX("Box", true),
		;
		final String name;
		final boolean showOptions;
		type(String name, boolean showOptions){
			this.name = name;
			this.showOptions = showOptions;
		}

		public String getName() {
			return name;
		}

		public boolean isShowOptions() {
			return showOptions;
		}
	}
}
