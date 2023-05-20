package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.util.ModelFactory.TempOpenModelStuff;
import com.hiveworkshop.rms.editor.model.util.TempSaveModelStuff;
import com.hiveworkshop.rms.parsers.mdlx.util.MdxUtils;
import com.hiveworkshop.rms.ui.application.FileDialog;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.model.editors.TwiTextField;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.icons.RMSIcons;
import com.hiveworkshop.rms.ui.util.SearchableList;
import com.hiveworkshop.rms.ui.util.TwiPopup;
import com.hiveworkshop.rms.util.SmartButtonGroup;
import com.hiveworkshop.rms.util.uiFactories.Button;
import com.hiveworkshop.rms.util.uiFactories.CheckBox;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AnimationExporter extends JPanel {
	String modelName;
	String folder;
	boolean removeGeometry = true;
	String extention = "mdx";
	FileDialog fileDialog;

	public AnimationExporter(EditableModel model){
//		super(new MigLayout("ins 0, fill, wrap 2", "[sgx anim][sgx anim]", "[][grow][]"));
		super(new MigLayout("ins 0, fill, wrap 2", "[][]", "[][grow][]"));
		fileDialog = new FileDialog(this);
		EditableModel tempModel = TempOpenModelStuff.createEditableModel(TempSaveModelStuff.toMdlx(model));

		add(CheckBox.create("Remove Geometry", true, b -> removeGeometry = b), "wrap");

		SearchableList<Animation> animationsList = new SearchableList<>(AnimationExporter::filterAnims);
		animationsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		animationsList.addAll(tempModel.getAnims());

		JPanel animPanel = new JPanel(new MigLayout("ins 0"));
		animPanel.add(new JLabel("Choose animation(s) to export from " + model.getName()), "spanx, wrap");
		animPanel.add(animationsList.getScrollableList(), "growx, wrap");
		add(animPanel, "spanx, growx, growy, wrap");

		String name = model.getFile() != null ? model.getFile().getName() : model.getName();
		modelName = name.split("\\.\\w+$")[0];
		TwiTextField fileField = new TwiTextField(modelName, 24, s -> modelName = s);
//		folder = model.getWorkingDirectory() != null ? model.getWorkingDirectory().getPath() : fileDialog.getFileChooser().getCurrentDirectory().getPath();
		folder = model.getWorkingDirectory() != null ? model.getWorkingDirectory().getPath() : FileDialog.getPath();
		TwiTextField folderField = new TwiTextField(folder, 24, s -> folder = s);
		JButton folderButton = Button.create("Choose", e -> folderField.setTextAndRun(fileDialog.chooseDir(FileDialog.OPEN_FILE).getPath()));

		add(fileField, "wrap");
		add(folderField, "");
		add(folderButton, "wrap");

		SmartButtonGroup smartButtonGroup = new SmartButtonGroup();
		smartButtonGroup.addJRadioButton(".mdl", e -> extention = "mdl");
		smartButtonGroup.addJRadioButton(".mdx", e -> extention = "mdx");
		smartButtonGroup.setSelectedIndex(1);
		add(smartButtonGroup, "wrap");


		add(Button.create("Export", e -> exportAnimations(tempModel, animationsList.getSelectedValuesList(), removeGeometry, modelName, extention, folder)));

	}



	public static void showWindow() {
		JFrame parentFrame = new JFrame("Animation Exporter");
		parentFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		AnimationExporter contentPane = new AnimationExporter(ProgramGlobals.getCurrentModelPanel().getModel());
		parentFrame.setContentPane(contentPane);
		parentFrame.setIconImage(RMSIcons.AnimIcon.getImage());
		parentFrame.pack();
		parentFrame.setLocationRelativeTo(null);
		parentFrame.setVisible(true);
	}

//	private static class AnimTransfer extends ActionFunction {
//		AnimTransfer(){
//			super(TextKey.IMPORT_ANIM, AnimationExporter::showWindow);
//			setMenuItemMnemonic(KeyEvent.VK_I);
//		}
//	}
//
//	public static JMenuItem getMenuItem(){
//		return new AnimTransfer().getMenuItem();
//	}
	private static boolean filterAnims(Animation animation, String text){
		return animation.getName().toLowerCase().contains(text.toLowerCase());
	}


	public static JMenuItem getMenuItem2(){
		JMenuItem menuItem = new JMenuItem("Export Animations");
//		menuItem.addActionListener(e -> showWindow());
		menuItem.addActionListener(e -> doStuff());
		return menuItem;
	}
	public static void doStuff(){
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if(modelPanel != null){
			exportAnimations(modelPanel.getModel(), true, ProgramGlobals.getMainPanel());
		}
	}

	private static void exportAnimations(EditableModel model, boolean removeGeometry, JComponent parent){
		FileDialog fileDialog = new FileDialog(parent);
		String fileName;
		String ext;
		if(model.getFile() != null){
			String[] fileParts = model.getFile().getName().split(".(?=\\w+$)");
			fileName = fileParts[0];
			ext = fileDialog.getExtension(model.getFile());
		} else {
			fileName = model.getName();
			ext = "mdx";
		}
//		String ext = fileParts[1];
		File location = fileDialog.chooseDir(FileDialog.OPEN_FILE);
		if(location != null){
			exportAnimations(model, removeGeometry, fileName, ext, location);
		}
	}

	private static void exportAnimations(EditableModel model, boolean removeGeometry, String fileName, String ext, File location) {
		EditableModel tempModel = TempOpenModelStuff.createEditableModel(TempSaveModelStuff.toMdlx(model));
		if(removeGeometry){
			tempModel.getGeosets().clear();
		}
		List<Animation> anims = new ArrayList<>(tempModel.getAnims());
		int maxAnims = anims.size();
		tempModel.getGlobalSeqs().clear();
		Set<String> usedAnimNames = new HashSet<>();
		for(Animation animation : anims){
			tempModel.getAnims().clear();
			String nameToUse = animation.getName();
			for (int i = 1; i<maxAnims && usedAnimNames.contains(nameToUse); i++){
				if(i<10){
					nameToUse = animation.getName() + "0" + i;
				} else {
					nameToUse = animation.getName() + i;
				}
			}
			usedAnimNames.add(nameToUse);
			tempModel.add(animation);
			try {
				File file = new File(location, fileName + "_" + nameToUse + "." + ext);
				if (ext.equals("mdl")) {
					MdxUtils.saveMdl(model, file);
				} else {
					MdxUtils.saveMdx(model, file);
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}


	private void exportAnimations(EditableModel tempModel, List<Animation> anims, boolean removeGeometry, String fileName, String ext, String location) {
		if(removeGeometry){
			tempModel.getGeosets().clear();
		}
		int exportedAnims = 0;
		int maxAnims = anims.size();
		tempModel.getGlobalSeqs().clear();
		for (Animation animation : anims) {
			tempModel.getAnims().clear();
			String nameToUse = getNameToUse(maxAnims, animation.getName());
			tempModel.add(animation);
			try {
				File file = new File(location, fileName + "_" + nameToUse + "." + ext);
				if (ext.equals("mdl")) {
					MdxUtils.saveMdl(tempModel, file);
				} else {
					MdxUtils.saveMdx(tempModel, file);
				}
				exportedAnims++;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		TwiPopup.quickDismissPopup(this, "Exported " + exportedAnims + " animations", "Exported Animations");
	}

	Set<String> usedAnimNames = new HashSet<>();
	private String getNameToUse(int maxAnims, String name) {
		String nameToUse = name;
		for (int i = 1; i< maxAnims && usedAnimNames.contains(nameToUse); i++){
			if(i<10){
				nameToUse = name + "0" + i;
			} else {
				nameToUse = name + i;
			}
		}
		usedAnimNames.add(nameToUse);
		return nameToUse;
	}
}
