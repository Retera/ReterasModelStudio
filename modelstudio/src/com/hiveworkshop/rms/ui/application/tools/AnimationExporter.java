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
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AnimationExporter extends JPanel {
	private final FileDialog fileDialog;
	private String modelName;
	private String folder;
	private boolean removeGeometry = true;
	private String extention = "mdx";

	public AnimationExporter(EditableModel model) {
		super(new MigLayout("ins 0, fill", "[][grow][]", "[][grow][]"));
		fileDialog = new FileDialog(this);
		EditableModel tempModel = TempOpenModelStuff.createEditableModel(TempSaveModelStuff.toMdlx(model));

		add(CheckBox.create("Remove Geometry", true, b -> removeGeometry = b), "spanx, wrap");

		SearchableList<Animation> animationsList = new SearchableList<>(AnimationExporter::filterAnims);
		animationsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		animationsList.addAll(tempModel.getAnims());

		JPanel animPanel = new JPanel(new MigLayout("ins 0, fill"));
		JLabel infoLabel = new JLabel("Choose animation(s) to export from " + model.getName());
		infoLabel.setToolTipText("Animations will be exported as individual files!");
		animPanel.add(infoLabel, "spanx, wrap");
		animPanel.add(animationsList.getScrollableList(), "growx, wrap");
		add(animPanel, "spanx, growx, growy, wrap");

		String name = model.getFile() != null ? model.getFile().getName() : model.getName();
		modelName = name.split("\\.\\w+$")[0];
		TwiTextField fileField = new TwiTextField(modelName, 24, s -> modelName = s);
		folder = model.getWorkingDirectory() != null ? model.getWorkingDirectory().getPath() : FileDialog.getPath();
		TwiTextField folderField = new TwiTextField(folder, 24, s -> folder = s);
		JButton folderButton = Button.create("Choose", e -> folderField.setTextAndRun(fileDialog.chooseDir(FileDialog.OPEN_FILE).getPath()));

		add(new JLabel("File prefix"), "");
		add(fileField, "wrap");
		add(new JLabel("Folder"), "");
		add(folderField, "");
		add(folderButton, "wrap");

		SmartButtonGroup smartButtonGroup = new SmartButtonGroup();
		smartButtonGroup.addJRadioButton(".mdl", e -> extention = "mdl");
		smartButtonGroup.addJRadioButton(".mdx", e -> extention = "mdx");
		smartButtonGroup.setSelectedIndex(1);
		add(smartButtonGroup, "");


		add(Button.create("Export", e -> exportAnimations(
				tempModel, animationsList.getSelectedValuesList(), removeGeometry,
				modelName, extention, folder, this)), "align center");

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

	private static boolean filterAnims(Animation animation, String text) {
		return animation.getName().toLowerCase().contains(text.toLowerCase());
	}


	public static JMenuItem getMenuItem2() {
		JMenuItem menuItem = new JMenuItem("Export Animations");
		menuItem.addActionListener(e -> showWindow());
//		menuItem.addActionListener(e -> doStuff());
		return menuItem;
	}
	public static void doStuff() {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			exportAnimations_simple(modelPanel.getModel(), true, ProgramGlobals.getMainPanel());
		}
	}

	private static void exportAnimations_simple(EditableModel model, boolean removeGeometry, JComponent parent) {
		FileDialog fileDialog = new FileDialog(parent);
		String fileName;
		String ext;
		if (model.getFile() != null) {
			String[] fileParts = model.getFile().getName().split(".(?=\\w+$)");
			fileName = fileParts[0];
			ext = fileDialog.getExtension(model.getFile());
		} else {
			fileName = model.getName();
			ext = "mdx";
		}
//		String ext = fileParts[1];
		File location = fileDialog.chooseDir(FileDialog.OPEN_FILE);
		if (location != null) {
			EditableModel tempModel = TempOpenModelStuff.createEditableModel(TempSaveModelStuff.toMdlx(model));
			List<Animation> anims = new ArrayList<>(tempModel.getAnims());
			exportAnimations(tempModel, anims, removeGeometry, fileName, ext, location.getPath(), parent);
		}
	}

	private static void exportAnimations(EditableModel tempModel, List<Animation> anims, boolean removeGeometry,
	                                     String fileName, String ext, String location, Component parent) {
		if (removeGeometry) {
			tempModel.getGeosets().clear();
		}
		int exportedAnims = 0;
		int maxAnims = anims.size();
		tempModel.getGlobalSeqs().clear();
		Set<String> usedAnimNames = new HashSet<>();
		for (Animation animation : anims) {
			tempModel.getAnims().clear();
			String nameToUse = getNameToUse(maxAnims, animation.getName(), usedAnimNames);
			tempModel.add(animation);
			File modelFile = new File(location, fileName + "_" + nameToUse + "." + ext);
			try {
				if (ext.equals("mdl")) {
					MdxUtils.saveMdl(tempModel, modelFile);
				} else {
					MdxUtils.saveMdx(tempModel, modelFile);
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			exportedAnims++;
		}
		TwiPopup.quickDismissPopup(parent, "Exported " + exportedAnims + " animations", "Exported Animations");
	}

	private static String getNameToUse(int maxAnims, String name, Set<String> usedAnimNames) {
		String nameToUse = name;
		for (int i = 1; i < maxAnims && usedAnimNames.contains(nameToUse); i++) {
			if (i < 10) {
				nameToUse = name + "0" + i;
			} else {
				nameToUse = name + i;
			}
		}
		usedAnimNames.add(nameToUse);
		return nameToUse;
	}
}
