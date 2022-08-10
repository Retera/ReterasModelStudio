package com.hiveworkshop.rms.ui.browsers.model;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.parsers.mdlx.util.MdxUtils;
import com.hiveworkshop.rms.ui.application.ImportFileActions;
import com.hiveworkshop.rms.ui.application.MainFrame;
import com.hiveworkshop.rms.ui.application.ModelLoader;
import com.hiveworkshop.rms.ui.application.model.editors.TwiFocusListener;
import com.hiveworkshop.rms.ui.application.viewer.AnimationViewer;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.TwiComboBoxModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.List;

public class ModelOptionPanel extends JPanel {
	private final List<ModelGroup> groups;

	public static void dropCache() {
		ModelGroupsHolder.dropCache();
	}

	private final Model nullModel = new Model(null, "", "");

	private final JComboBox<ModelGroup> groupBox;
	private final JComboBox<Model> modelBox;
	private final JTextField filePathField;
	private final List<TwiComboBoxModel<Model>> groupModels = new ArrayList<>();

	private final AnimationViewer viewer;
	private final EditableModel blank = new EditableModel();

	private String currentFilePath;
	private Model currentModel;
	private EditableModel toLoad;

	public ModelOptionPanel() {

		groups = ModelGroupsHolder.getGroups();
		TwiComboBoxModel<ModelGroup> groupsModel = new TwiComboBoxModel<>(groups);
		for (ModelGroup group : groups) {
			TwiComboBoxModel<Model> groupModel = new TwiComboBoxModel<>(group.getModels());
			groupModels.add(groupModel);
		}

		groupBox = new JComboBox<>(groupsModel);
		groupBox.addItemListener(this::groupBoxListener);
		groupBox.setMaximumRowCount(11);
		groupBox.setMaximumSize(new Dimension(200, 25));

		modelBox = new JComboBox<>(groupModels.get(0));
		modelBox.addItemListener(this::modelBoxListener);
		modelBox.setMaximumRowCount(20);
		modelBox.setMaximumSize(new Dimension(1000, 25));

		filePathField = new JTextField(18);
		filePathField.setMaximumSize(new Dimension(1000, 25));
		filePathField.addFocusListener(new TwiFocusListener(filePathField, this::updateModel));

		viewer = new AnimationViewer(new ProgramPreferences(), false);
		viewer.setTitle("No model loaded");
		groupBox.setSelectedIndex(0);
		modelBox.setSelectedIndex(0);
		setFilePathFromModelBox();

		setLayout(new MigLayout("fill", "[][grow]", "[grow]"));
		JPanel rightPanel = new JPanel(new MigLayout("fill", "[]", "[][][][grow]"));
		rightPanel.add(groupBox, "wrap, growx");
		rightPanel.add(modelBox, "wrap, growx");
		rightPanel.add(filePathField, "wrap, growx");

		add(viewer, "growx");
		add(rightPanel, "growx, growy");
	}

	private void updateModel(){
		String filepath = filePathField.getText();
		if(!filepath.equals(currentFilePath)){
			if(selectBoxItemsFromPath(filepath) == null){
				nullModel.setFilepath(filepath);
				TwiComboBoxModel<Model> groupModel = groupModels.get(groupBox.getSelectedIndex());
				groupModel.setSelectedItem(nullModel);
			}
			currentFilePath = filepath;
			showModel(filepath);
		}
	}

	private void groupBoxListener(ItemEvent e) {
		if(e.getStateChange() == ItemEvent.SELECTED) {
			TwiComboBoxModel<Model> groupModel = groupModels.get(groupBox.getSelectedIndex());
			modelBox.setModel(groupModel);
			modelBox.setSelectedIndex(0);
			setFilePathFromModelBox();
		}
	}

	private void modelBoxListener(ItemEvent e) {
		if(e.getStateChange() == ItemEvent.SELECTED){
			setFilePathFromModelBox();
		}
	}

	private void setFilePathFromModelBox() {
		Model model = (Model) modelBox.getSelectedItem();
		currentModel =  model == null ? nullModel : model;
		String filepath = model == null ? "" : model.getFilepath();
		currentFilePath = filepath;
		if (!filePathField.getText().equals(filepath)) {
			filePathField.setText(filepath);
		}
		showModel(filepath);
	}

	public String getSelection() {
		return currentModel.getFilepath();
	}

	public EditableModel getSelectedModel() {
		return toLoad;
	}

	public String getCachedIconPath() {
		return currentModel.getCachedIcon();
	}

	public ModelOptionPanel setSelection(String path) {
		if (path != null) {
			if (selectBoxItemsFromPath(path) == null) {
				nullModel.setFilepath(path);
				modelBox.getModel().setSelectedItem(nullModel);
			}
			currentFilePath = path;
			filePathField.setText(path);
		} else {
			currentFilePath = "";
			filePathField.setText("");
		}
		return this;
	}

	private Model selectBoxItemsFromPath(String path) {
		for (ModelGroup group : groups) {
			for (Model model : group.getModels()) {
				if (model.getFilepath().equals(path)) {
					groupBox.setSelectedItem(group);
					modelBox.setSelectedItem(model);
					return model;
				}
			}
		}
		return null;
	}

	private void showModel(String filepath) {
		try {
			filepath = ImportFileActions.convertPathToMDX(filepath);
			EditableModel editableModel = MdxUtils.loadEditable(filepath, null);
			setModel(editableModel);
			if (editableModel == null) {
				System.err.println("failed to load file: \"" + filepath + "\"");
			}

			if (modelBox.getParent() !=null){
				modelBox.setMaximumSize(modelBox.getParent().getSize());
				filePathField.setMaximumSize(modelBox.getParent().getSize());
			}
		} catch (final Exception exc) {
			exc.printStackTrace();
			setModel(null);
			// bad model!
		}
	}

	private void setModel(EditableModel o) {
		toLoad = o;
		if(o == null){
			viewer.setModel(blank);
			viewer.setTitle("No model loaded");
		} else {
			viewer.setModel(o);
			viewer.setTitle(o.getName());
		}
	}

	public static ModelOptionPanel getModelOptionPanel(Component component) {
		ModelOptionPanel uop = new ModelOptionPanel();
		int x = JOptionPane.showConfirmDialog(component, uop, "Choose Model", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);
		if (x == JOptionPane.OK_OPTION && isValidFilepath(uop.getSelection())) {
			return uop;
		} else {
			return null;
		}
	}

	public ImageIcon getIconForSelected(){
		String iconPath = getCachedIconPath();
		if(iconPath != null && iconPath.length() > 0){
			Image scaledInstance = BLPHandler.getGameTex(iconPath).getScaledInstance(16, 16, Image.SCALE_FAST);
			return new ImageIcon(scaledInstance);
		} else {
			return ModelLoader.MDLIcon;
		}
	}

	public static boolean isValidFilepath(String filepath) {
		try {
			//check model by converting its path
			ImportFileActions.convertPathToMDX(filepath);
		} catch (final Exception exc) {
			exc.printStackTrace();
			JOptionPane.showMessageDialog(MainFrame.frame,
					"The chosen model could not be used.",
					"Program Error",
					JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
}
