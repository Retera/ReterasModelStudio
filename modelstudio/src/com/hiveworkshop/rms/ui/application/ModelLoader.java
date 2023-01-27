package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.util.*;
import com.hiveworkshop.rms.parsers.mdlx.BinaryDecipherHelper;
import com.hiveworkshop.rms.parsers.mdlx.util.MdxUtils;
import com.hiveworkshop.rms.ui.application.actionfunctions.CloseModel;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ModelEditorActionType3;
import com.hiveworkshop.rms.ui.icons.RMSIcons;
import com.hiveworkshop.rms.ui.preferences.SaveProfile;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.ui.util.ExtFilter;
import com.hiveworkshop.rms.util.ImageUtils.ImageCreator;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.fileviewers.SklViewer;
import com.hiveworkshop.rms.util.fileviewers.TxtViewer;
import jassimp.AiPostProcessSteps;
import jassimp.AiProgressHandler;
import jassimp.AiScene;
import jassimp.Jassimp;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

public class ModelLoader {
	public static final ImageIcon MDLIcon = RMSIcons.MDLIcon;

	public static void refreshAnimationModeState() {

		if (ProgramGlobals.getCurrentModelPanel() != null) {
			ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
			modelPanel.refreshFromEditor();
		} else {
			ProgramGlobals.setEditorActionTypeButton(ModelEditorActionType3.TRANSLATION);
		}

		if ((ProgramGlobals.getSelectionItemType() == SelectionItemTypes.ANIMATE)) {
			if ((ProgramGlobals.getEditorActionType() == ModelEditorActionType3.EXTRUDE)
					|| (ProgramGlobals.getEditorActionType() == ModelEditorActionType3.EXTEND)) {
				ProgramGlobals.setEditorActionTypeButton(ModelEditorActionType3.TRANSLATION);
			}
		}

		ProgramGlobals.getRootWindowUgg().getWindowHandler2().setAnimationMode();
	}

	public static ModelPanel newTempModelPanel(ImageIcon icon, EditableModel model) {
		return new ModelPanel(new ModelHandler(model, icon));
	}


	public static EditableModel getImagePlaneModel(File file, int version) {
		String fileName = file.getName();
		System.out.println("fileName: " + fileName);
		//        File fileRef = new File(file.getPath().replaceAll("\\.[^.]+$", "") + ".mdl");
		File fileRef = new File(file.getPath());
		System.out.println("fileRef: " + fileRef + ", fileRefPath: " + fileRef.getPath());

		EditableModel blankTextureModel = new EditableModel(fileName);
		blankTextureModel.setFileRef(fileRef);
		blankTextureModel.setFormatVersion(version);
//		blankTextureModel.setTemp(true);

		Geoset newGeoset = new Geoset();
		if (version == 1000) {
			newGeoset.setLevelOfDetail(0);
		}
		Layer layer = new Layer(FilterMode.BLEND, new Bitmap(fileName));
		layer.setUnshaded(true);
//		layer.setTwoSided(true);
		Material material = new Material(layer);
		newGeoset.setMaterial(material);
		BufferedImage bufferedImage = ImageCreator.getBufferedImage(material, blankTextureModel.getWrappedDataSource());
		int textureWidth = bufferedImage.getWidth();
		int textureHeight = bufferedImage.getHeight();
		float aspectRatio = textureWidth / (float) textureHeight;

		int displayWidth = (int) (aspectRatio > 1 ? 128 : 128 * aspectRatio);
		int displayHeight = (int) (aspectRatio < 1 ? 128 : 128 / aspectRatio);

		int groundOffset = aspectRatio > 1 ? (128 - displayHeight) / 2 : 0;

		Mesh planeMesh = ModelUtils.getPlaneMesh2(new Vec3(-groundOffset, displayWidth / 2.0, 0), new Vec3(-groundOffset-displayHeight, -displayWidth / 2.0, 0), 1, 1);
		planeMesh.rotate(new Quat().setFromAxisAngle(Vec3.Y_AXIS, (float) Math.toRadians(90)));
		newGeoset.addVerticies(planeMesh.getVertices());
		planeMesh.getVertices().forEach(vertex -> vertex.setGeoset(newGeoset));
		newGeoset.addTriangles(planeMesh.getTriangles());
		planeMesh.getTriangles().forEach(triangle -> triangle.setGeoset(newGeoset));

		blankTextureModel.add(newGeoset);
		ExtLog extLog = new ExtLog(128).setDefault();
		blankTextureModel.setExtents(extLog);
		blankTextureModel.add(new Animation("Stand", 0, 1000));
		TempSaveModelStuff.doSavePreps(blankTextureModel);
		return blankTextureModel;
	}

	public static void loadModel(boolean temporary, boolean showModel, ModelPanel modelPanel) {
		if (temporary) {
			modelPanel.getModelView().getModel().setTemp(true);
		}

		if (ProgramGlobals.getCurrentModelPanel() == modelPanel) {
			ProgramGlobals.getRootWindowUgg().getWindowHandler2().showModelPanel(modelPanel);
		}
		ProgramGlobals.addModelPanel(modelPanel);

		if (showModel) {
			setCurrentModel(modelPanel);
		}

		if (temporary) {
			modelPanel.getModelView().getModel().setFileRef(null);
		}

		if (showModel && ProgramGlobals.getPrefs().getQuickBrowse()) {
			CloseModel.closeUnalteredModelsExcept(modelPanel);
		}
	}

	public static void setCurrentModel(ModelPanel modelPanel) {
		ProgramGlobals.setCurrentModelPanel(modelPanel);
//		refreshAnimationModeState();

		if (modelPanel != null) {
			modelPanel.refreshFromEditor();
		}

		ProgramGlobals.getRootWindowUgg().getWindowHandler2().setAnimationMode();

//		ProgramGlobals.getRootWindowUgg().getWindowHandler2().getViewportListener().viewportChanged(null);
		ModelStructureChangeListener.changeListener.keyframesUpdated();
	}

	public static void loadFile(File f) {
		loadFile(f, false, true, MDLIcon);
	}

	public static void loadFile(File f, boolean temporary, boolean showModel, final ImageIcon icon) {
		String filepath = f.getPath();
		System.out.println("loadFile: " + f.getName());
		System.out.println("filePath: " + filepath);
		ExtFilter extFilter = new ExtFilter();
		if (f.exists()) {
			final String pathLow = filepath.toLowerCase();
			String ext = pathLow.replaceAll(".+\\.(?=.+)", "");
			EditableModel model;
			if (extFilter.isSupTexture(ext)) {
				model = getImageModel(f, ext);
				temporary = false;

			} else if (Arrays.asList("mdx", "mdl").contains(ext)) {
				model = getMdxlModel(f);
			} else if (Arrays.asList("obj", "fbx").contains(ext)) {
				model = getAssImpModel(f);
			} else if (Arrays.asList("pkb").contains(ext)){
				BinaryDecipherHelper.load(f);
				model = null;
			} else if (Arrays.asList("slk").contains(ext)) {
				String fileName = filepath.replaceAll(".*\\\\", "");
				new SklViewer().createAndShowHTMLPanel(f, fileName);
				model = null;
			} else if (Arrays.asList("txt").contains(ext)) {
				String fileName = filepath.replaceAll(".*\\\\", "");
				new TxtViewer().createAndShowHTMLPanel(f, fileName);
				model = null;
			} else {
				model = null;
			}
//			else if (ext.equals(".skl")){
//				new TestSklViewer().createAndShowHTMLPanel(f.getPath(), "View SKL");
//			}
			if (model != null) {
//				ModelPanel tempModelPanel = newTempModelPanel(icon, model);
				ModelPanel tempModelPanel = new ModelPanel(new ModelHandler(model, icon));
				loadModel(temporary, showModel, tempModelPanel);
			}
		} else if (SaveProfile.get().getRecent().contains(filepath)) {
			int option = JOptionPane.showConfirmDialog(ProgramGlobals.getMainPanel(), "Could not find the file.\nRemove from recent?", "File not found", JOptionPane.YES_NO_OPTION);
			if (option == JOptionPane.YES_OPTION) {
				SaveProfile.get().removeFromRecent(filepath);
				ProgramGlobals.getMenuBar().updateRecent();
			}
		}
	}


	public static void loadFileNoGUI(File f) {
		// For testing purposes
		System.out.println("loadFile: " + f.getName());
		System.out.println("filePath: " + f.getPath());
		ExtFilter extFilter = new ExtFilter();
		if (f.exists()) {
			final String pathLow = f.getPath().toLowerCase();
			String ext = pathLow.replaceAll(".+\\.(?=.+)", "");
			if (extFilter.isSupTexture(ext)) {
				getImageModel(f, ext);
			} else if (Arrays.asList("mdx", "mdl").contains(ext)) {
//				BinaryDecipherHelper.load(f);
				getMdxlModel(f);
			} else if (Arrays.asList("obj", "fbx", "dae").contains(ext)) {
				getAssImpModel(f);
			} else if (Arrays.asList("pkb").contains(ext)){
				System.out.println("pkb!");
			BinaryDecipherHelper.load(f);
			}
		} else {
			System.out.println("could not find file");
		}
	}

	private static EditableModel getImageModel(File f, String ext) {
		EditableModel model;
		if (ext.equals("dds")) {
			model = getImagePlaneModel(f, 1000);
		} else {
			model = getImagePlaneModel(f, 800);
		}
		model.setTemp(true);
//		model.setFileRef(f);
		return model;
	}

	private static EditableModel getMdxlModel(File f) {
		EditableModel model;
		try {
			model = MdxUtils.loadEditable(f);
			model.setFileRef(f);
		} catch (final IOException e) {
			e.printStackTrace();
			ExceptionPopup.display(e);
			throw new RuntimeException("Reading mdx failed");
		}
		return model;
	}

	private static EditableModel getAssImpModel(File f) {
		try {
			System.out.println("importing file \"" + f.getName() + "\" this might take a while...");
			long timeStart = System.currentTimeMillis();
			AiProgressHandler aiProgressHandler = new AiProgressHandler() {
				@Override
				public boolean update(float v) {
//					System.out.println("progress: " + (int)((v+1)*100) + "%  " + (System.currentTimeMillis()-timeStart) + " ms");
					return true;
				}
			};
//			AiClassLoaderIOSystem aiIOSystem = new AiClassLoaderIOSystem();
			TwiAiIoSys twiAiIoSys = new TwiAiIoSys();


			HashSet<AiPostProcessSteps> processSteps = new HashSet<>();
			processSteps.add(AiPostProcessSteps.TRIANGULATE);
			processSteps.add(AiPostProcessSteps.REMOVE_REDUNDANT_MATERIALS);
			AiScene scene = Jassimp.importFile(f.getPath(), processSteps, twiAiIoSys, aiProgressHandler);
			TwiAiSceneParser twiAiSceneParser = new TwiAiSceneParser(scene);
			System.out.println("took " + (System.currentTimeMillis() - timeStart) + " ms to load the model");
			EditableModel model = twiAiSceneParser.getEditableModel();
			model.setFileRef(f);
			return model;
			//
		} catch (final Exception e) {
			ExceptionPopup.display(e);
			e.printStackTrace();
		}
		return null;
	}

	public static void loadFile(File f, boolean temporary) {
		loadFile(f, temporary, true, MDLIcon);
	}
}
