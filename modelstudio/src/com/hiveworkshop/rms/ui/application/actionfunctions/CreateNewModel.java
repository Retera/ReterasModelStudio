package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.editor.actions.mesh.RecalculateTangentsAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.util.Mesh;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.ui.application.MainPanel;
import com.hiveworkshop.rms.ui.application.ModelLoader;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.model.editors.IntEditorJSpinner;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.icons.RMSIcons;
import com.hiveworkshop.rms.ui.language.TextKey;
import com.hiveworkshop.rms.util.SmartButtonGroup;
import com.hiveworkshop.rms.util.Vec3;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class CreateNewModel extends ActionFunction {
	public CreateNewModel() {
		super(TextKey.NEW, CreateNewModel::newModel, "control N");
	}

	public static void newModel() {
		JPanel newModelPanel = new JPanel(new MigLayout("fill, ins 0"));
		newModelPanel.add(new JLabel("Model Name: "), "");
		JTextField newModelNameField = new JTextField("MrNew", 25);
		newModelPanel.add(newModelNameField, "wrap");

		newModelPanel.add(new JLabel("Format Version: "), "");
		IntEditorJSpinner formatVersionSpinner = new IntEditorJSpinner(800, 0, null);
		newModelPanel.add(formatVersionSpinner, "wrap");


		JPanel optionPanel = new JPanel(new MigLayout("ins 0","",""));
		optionPanel.add(new JLabel("Segments"));
		JSpinner segmentSpinner = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
		optionPanel.add(segmentSpinner);
		optionPanel.setVisible(false);


		SmartButtonGroup typeGroup = new SmartButtonGroup();
		for (MeshType t : MeshType.values()) {
			typeGroup.addJRadioButton("Create " + t.getName(), e -> optionPanel.setVisible(t.isShowOptions()));
		}
		typeGroup.setSelectedIndex(0);

		newModelPanel.add(typeGroup.getButtonPanel(), "wrap");
		newModelPanel.add(optionPanel, "spanx");

		MainPanel mainPanel = ProgramGlobals.getMainPanel();

		int userDialogResult = JOptionPane.showConfirmDialog(mainPanel, newModelPanel, "New Model", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (userDialogResult == JOptionPane.OK_OPTION) {
			int segments = ((Number) segmentSpinner.getValue()).intValue();

			MeshType selected = MeshType.values()[typeGroup.getSelectedIndex()];
			Mesh mesh = getMesh(selected, new Vec3(64, 64, 0), new Vec3(-64, -64, 128), segments);
			createModel(newModelNameField.getText(), formatVersionSpinner.getIntValue(), mesh, selected.getName());
		}
	}

	private static Mesh getMesh(MeshType t, Vec3 max, Vec3 min, int segments) {
		return switch (t) {
			case EMPTY -> null;
			case PLANE -> ModelUtils.getPlaneMesh2(min, max, segments, segments);
			case BOX -> ModelUtils.getBoxMesh2(min, max, segments, segments, segments);
		};
	}


	private static void createModel(String name, int formatVersion, Mesh mesh, String geosetName) {
		EditableModel model = new EditableModel(name);
		model.setFormatVersion(formatVersion);
		model.add(new Animation("Stand", 100, 1100));
		Bone bone = new Bone("Root");
		model.add(bone);

		if (mesh != null) {
			Bitmap texture = new Bitmap("Textures\\BTNtempW.blp");
			model.add(texture);
			Material material = new Material(new Layer(texture));
			model.add(material);

			Geoset geoset = getGeoset(bone, formatVersion, mesh);
			geoset.setName(geosetName);
			geoset.setMaterial(material);

			model.add(geoset);
		}
		model.setExtents(new ExtLog(128).setDefault());

		ModelPanel temp = new ModelPanel(new ModelHandler(model, RMSIcons.MDLIcon));
		ModelLoader.loadModel(true, true, temp);
	}

	private static Geoset getGeoset(Bone bone, int formatVersion, Mesh mesh) {
		Geoset geoset = new Geoset();

		for (GeosetVertex vertex : mesh.getVertices()) {
			vertex.setGeoset(geoset);
			if (formatVersion < 900) {
				vertex.addBoneAttachment(bone);
			} else {
				vertex.initSkinBones();
				vertex.setSkinBone(bone, (short) 255, 0);
			}
			geoset.add(vertex);
		}
		for (Triangle triangle : mesh.getTriangles()) {
			triangle.setGeoset(geoset);
			geoset.add(triangle);
		}
		if (900 <= formatVersion) {
			new RecalculateTangentsAction(mesh.getVertices()).redo();
		}
		return geoset;
	}

	enum MeshType {
		EMPTY("Empty", false),
		PLANE("Plane", true),
		BOX("Box", true),
		;
		final String name;
		final boolean showOptions;

		MeshType(String name, boolean showOptions) {
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
