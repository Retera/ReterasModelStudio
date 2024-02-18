package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.util.*;
import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.parsers.mdlx.BinaryDecipherHelper;
import com.hiveworkshop.rms.parsers.mdlx.util.MdxUtils;
import com.hiveworkshop.rms.ui.application.actionfunctions.CloseModel;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.icons.RMSIcons;
import com.hiveworkshop.rms.ui.preferences.SaveProfile;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.ui.util.ExtFilter;
import com.hiveworkshop.rms.ui.util.ModelLoadingInfo;
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
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class ModelLoader {
	public static final ImageIcon MDLIcon = RMSIcons.MDLIcon;

	public static EditableModel getImagePlaneModel(File file, int version) {
		String fileName = file.getName();
		System.out.println("fileName: \"" + fileName+ "\"");
		//        File fileRef = new File(file.getPath().replaceAll("\\.[^.]+$", "") + ".mdl");
		File fileRef = new File(file.getPath());
		System.out.println("fileRef: \"" + fileRef + "\", fileRefPath: \"" + fileRef.getPath() + "\"");

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

	public static void loadModel(EditableModel model) {
		ModelPanel modelPanel = new ModelPanel(new ModelHandler(model));
		ModelLoader.loadModel(true, true, modelPanel);
	}

	public static void loadModel(boolean temporary, boolean showModel, ModelPanel modelPanel) {
		if (temporary) {
			modelPanel.getModelHandler().getModel().setTemp(true);
			modelPanel.getModelHandler().getModel().setFileRef(null);
		}
		loadModel(showModel, modelPanel);
	}

	public static void loadModel(boolean showModel, ModelPanel modelPanel) {
		if (ProgramGlobals.getCurrentModelPanel() == modelPanel) {
			ProgramGlobals.getRootWindowUgg().getWindowHandler2().showModelPanel(modelPanel);
		}
		ProgramGlobals.addModelPanel(modelPanel);

		if (showModel) {
			setCurrentModel(modelPanel);
		}

		if (showModel && ProgramGlobals.getPrefs().getQuickBrowse()) {
			CloseModel.closeUnalteredModelsExcept(modelPanel);
		}
	}

	public static void setCurrentModel(ModelPanel modelPanel) {
		ProgramGlobals.setCurrentModelPanel(modelPanel);

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
		if (f.exists()) {
			loadAndShowModelFromFile(f, temporary, showModel, icon);
		} else if (SaveProfile.get().getRecent().contains(filepath)) {
			int option = JOptionPane.showConfirmDialog(ProgramGlobals.getMainPanel(), "Could not find\n\"" + filepath + "\"\nRemove from recent?", "File not found", JOptionPane.YES_NO_OPTION);
			if (option == JOptionPane.YES_OPTION) {
				SaveProfile.get().removeFromRecent(filepath);
				ProgramGlobals.getMenuBar().updateRecent();
			}
		}
	}

	private static void loadAndShowModelFromFile(File f, boolean temporary, boolean showModel, ImageIcon icon) {
		final ModelLoadingInfo loadingInfo = new ModelLoadingInfo(f, true);
		loadingInfo.start();
		SwingWorker<EditableModel, String> loaderThing = new SwingWorker<>() {
			@Override
			protected EditableModel doInBackground() {
				return getEditableModel(f, temporary, loadingInfo::setMessage);
			}

			@Override
			protected void done() {
				try {
					EditableModel model = get();
					if (model != null) {
						loadingInfo.setMessage("Creating ModelPanel");
						ModelPanel tempModelPanel = new ModelPanel(new ModelHandler(model, icon));
						loadingInfo.setMessage("Loading in Model");
						loadModel(showModel, tempModelPanel);
					}
				} catch (InterruptedException | ExecutionException e) {
					Throwable cause = e.getCause();
					if (cause instanceof RuntimeException) {
						throw (RuntimeException) cause;
					} else {
						throw new RuntimeException("Failed to load \"" + f + "\"", e);
					}
				} finally {
					loadingInfo.stop();
				}
			}
		};

		loaderThing.execute();
	}

	private static EditableModel getEditableModel(File f, boolean temporary, Consumer<String> stringConsumer) {
		ExtFilter extFilter = new ExtFilter();
		String filepath = f.getPath();
		final String pathLow = filepath.toLowerCase();
		String ext = pathLow.replaceAll(".+\\.(?=.+)", "");
		EditableModel model;
		if (extFilter.isSupTexture(ext)) {
			stringConsumer.accept("Loading image");
			model = getImageModel(f, ext);
			temporary = false;

		} else if (Arrays.asList("mdx", "mdl").contains(ext)) {
			model = getMdxlModel(f, stringConsumer);
		} else if (Arrays.asList("obj", "fbx").contains(ext)) {
			model = getAssImpModel(f, stringConsumer);
		} else if (Arrays.asList("pkb").contains(ext)) {
			BinaryDecipherHelper.load(f);
			model = null;
		} else if (Arrays.asList("slk").contains(ext)) {
			stringConsumer.accept("Loading skl");
			String fileName = filepath.replaceAll(".*\\\\", "");
			new SklViewer().createAndShowHTMLPanel(f, fileName);
			model = null;
		} else if (Arrays.asList("txt", "fdf", "json", "ini").contains(ext)) {
			String fileName = filepath.replaceAll(".*\\\\", "");
			new TxtViewer().createAndShowHTMLPanel(f, fileName);
			model = null;
		} else {
			model = null;
		}

		if (model != null) {
			if (temporary) {
				model.setTemp(true);
				model.setFileRef(null);
			}
		}
		return model;
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
				getMdxlModel(f, s -> {});
			} else if (Arrays.asList("obj", "fbx", "dae").contains(ext)) {
				getAssImpModel(f, s -> {});
			} else if (Arrays.asList("pkb").contains(ext)) {
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
		model.setFileRef(null);
		return model;
	}

	private static EditableModel getMdxlModel(File f, Consumer<String> stringConsumer) {
		try {
			EditableModel model = MdxUtils.loadEditable(f, stringConsumer);
			model.setFileRef(f);
			return model;
		} catch (final IOException e) {
			e.printStackTrace();
			ExceptionPopup.display(e);
			throw new RuntimeException("Reading mdx failed");
		}
	}

	private static EditableModel getAssImpModel(File f, Consumer<String> stringConsumer) {
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
			TwiAiSceneParser twiAiSceneParser = new TwiAiSceneParser(scene, stringConsumer);
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

	public static void loadFile(String path, boolean temporary) {
		loadFile(GameDataFileSystem.getDefault().getFile(path), temporary);
	}
	public static void loadFile(File f, boolean temporary) {
		loadFile(f, temporary, true, MDLIcon);
	}
}
