package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.model.CloneEditableModel;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.parsers.mdlx.util.MdxUtils;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.util.TwiComboBox;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ModelFromFile {

	public static EditableModel chooseModelFile(int operationType, Component parent) {
		FileDialog fileDialog = new FileDialog(parent);
		File file = fileDialog.openFile(operationType);

		if (file != null) {
			String ext = fileDialog.getExtension(file).toLowerCase();
			if (fileDialog.isSavableModelExt(ext)) {
				try {
					EditableModel model = MdxUtils.loadEditable(file);
					model.setFileRef(file);
					return model;
				} catch (IOException ioException) {
					ioException.printStackTrace();
				}
			} else if (fileDialog.isSavableTextureExt(ext)) {
				int version = ext.equals("dds") ? 1000 : 800;
				return ModelLoader.getImagePlaneModel(file, version);
			}
		}
		return null;
	}



	public static EditableModel getWorkspaceModel(String title, String text, Component parent) {
		ModelPanel modelPanel = getModelPanel(title, text, null, parent);

		if (modelPanel != null) {
			return modelPanel.getModel();
		}
		return null;
	}
	public static EditableModel getWorkspaceModelCopy(String title, String text, Component parent) {
		ModelPanel modelPanel = getModelPanel(title, text, null, parent);

		if (modelPanel != null) {
			EditableModel model = modelPanel.getModel();
			return CloneEditableModel.deepClone(model, model.getHeaderName());
		}
		return null;
	}
	public static EditableModel getWorkspaceModel2(Component parent) {
		List<EditableModel> optionNames = ProgramGlobals.getModelPanels().stream()
				.map(ModelPanel::getModel)
				.collect(Collectors.toList());

		EditableModel choice = (EditableModel) JOptionPane.showInputDialog(parent,
				"Choose a workspace item to import data from:", "Import from Workspace",
				JOptionPane.PLAIN_MESSAGE, null, optionNames.toArray(), optionNames.get(0));
		if (choice != null) {
			return CloneEditableModel.deepClone(choice, choice.getHeaderName());
		}
		return null;
	}

	public static ModelPanel getModelPanel(String title, String text, ModelPanel toSelect, Component parent) {
		JPanel panel = new JPanel(new MigLayout());
		panel.add(new JLabel(text), "wrap");

		Map<Integer, ModelPanel> models = new HashMap<>();
		List<ModelPanel> modelPanels = ProgramGlobals.getModelPanels();
		for (ModelPanel m : modelPanels) {
			models.put(models.size(), m);
		}

		TwiComboBox<ModelPanel> modelsBox = getModelComboBox(toSelect);

		panel.add(modelsBox);

		int option = JOptionPane.showConfirmDialog(parent, panel, title, JOptionPane.OK_CANCEL_OPTION);

		if (option == JOptionPane.OK_OPTION) {
			return modelsBox.getSelected();
		}
		return null;
	}

	private static TwiComboBox<ModelPanel> getModelComboBox(ModelPanel toSelect) {
		ModelPanel prototype = new ModelPanel(new ModelHandler(new EditableModel("Prototype Prototype")));
		TwiComboBox<ModelPanel> modelsBox = new TwiComboBox<>(ProgramGlobals.getModelPanels(), prototype);
		modelsBox.setStringFunctionRender(o -> {
			if(o instanceof ModelPanel){
				return ((ModelPanel) o).getModel().getName() + (o == ProgramGlobals.getCurrentModelPanel() ? " (current)" : "");
			}
			return "None";
		});
		modelsBox.selectOrFirst(toSelect);

		return modelsBox;
	}
	private static TwiComboBox<String> getModelComboBox1(Map<Integer, ModelPanel> models) {
		String[] names = new String[models.size()];
		int currentModelPanel = 0;
		for (Integer i : models.keySet()) {
			ModelPanel m = models.get(i);
			names[i] = m.getModel().getName();
			if(m == ProgramGlobals.getCurrentModelPanel()){
				currentModelPanel = i;
				names[i] += " (current)";
			}
		}

		TwiComboBox<String> modelsBox = new TwiComboBox<>(names, "Prototype Prototype");
		if(currentModelPanel < names.length){
			modelsBox.setSelectedIndex(currentModelPanel);
		}
		return modelsBox;
	}
}
