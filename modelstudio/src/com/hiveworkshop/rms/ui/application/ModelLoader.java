package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.editor.model.util.TempSaveModelStuff;
import com.hiveworkshop.rms.editor.model.util.TwiAiIoSys;
import com.hiveworkshop.rms.editor.model.util.TwiAiSceneParser;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.parsers.mdlx.util.MdxUtils;
import com.hiveworkshop.rms.ui.application.MenuBar1.MenuBar;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ModelEditorActionType3;
import com.hiveworkshop.rms.ui.icons.RMSIcons;
import com.hiveworkshop.rms.ui.preferences.SaveProfile;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.ui.util.ExtFilter;
import com.hiveworkshop.rms.util.Vec2;
import jassimp.AiPostProcessSteps;
import jassimp.AiProgressHandler;
import jassimp.AiScene;
import jassimp.Jassimp;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

public class ModelLoader {
	static final ImageIcon MDLIcon = RMSIcons.MDLIcon;

	public static void refreshAnimationModeState() {
		MainPanel mainPanel = ProgramGlobals.getMainPanel();
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (mainPanel.animationModeState) {
			if ((modelPanel != null) && (modelPanel.getModel() != null)) {
				if (modelPanel.getModel().getAnimsSize() > 0) {
					final Animation anim = modelPanel.getModel().getAnim(0);
					modelPanel.getModelHandler().getEditTimeEnv().setAnimation(anim);
					modelPanel.getModelHandler().getEditTimeEnv().setStaticViewMode(!mainPanel.animationModeState);
				}
				refreshAndUpdateRenderModel();
			}
			if ((mainPanel.actionTypeGroup.getActiveButtonType() == ModelEditorActionType3.EXTRUDE)
					|| (mainPanel.actionTypeGroup.getActiveButtonType() == ModelEditorActionType3.EXTEND)) {
//				mainPanel.actionTypeGroup.setToolbarButtonType(mainPanel.actionTypeGroup.getToolbarButtonTypes()[0]);
				mainPanel.actionTypeGroup.setActiveButton(ModelEditorActionType3.TRANSLATION);
			}
		}

		if (!mainPanel.animationModeState) {
			if ((modelPanel != null) && (modelPanel.getModel() != null)) {
				refreshAndUpdateRenderModel();
				modelPanel.getModelHandler().getEditTimeEnv().setAnimation(null);
				modelPanel.getModelHandler().getEditTimeEnv().setStaticViewMode(!mainPanel.animationModeState);
			}
		}
//		List<ToolbarButtonGroup<ToolbarActionButtonType>.ToolbarButtonAction> buttons = mainPanel.actionTypeGroup.getButtons();
//		List<ModeButton2> buttons = mainPanel.actionTypeGroup.getButtons();
//
//		int numberOfButtons = buttons.size();
//		for (int i = 3; i < numberOfButtons; i++) {
//			buttons.get(i).getButton().setVisible(!mainPanel.animationModeState);
//		}
		MainLayoutCreator mainLayoutCreator = mainPanel.getMainLayoutCreator();
		mainPanel.snapButton.setVisible(!mainPanel.animationModeState);
		mainLayoutCreator.getTimeSliderPanel().setDrawing(mainPanel.animationModeState);
		mainLayoutCreator.getTimeSliderPanel().setKeyframeModeActive(mainPanel.animationModeState);
		mainLayoutCreator.getTimeSliderPanel().repaint();
		mainLayoutCreator.getCreatorPanel().setAnimationModeState(mainPanel.animationModeState);
	}

	private static void refreshAndUpdateRenderModel() {
		RenderModel editorRenderModel = ProgramGlobals.getCurrentModelPanel().getEditorRenderModel();
		editorRenderModel
				.refreshFromEditor(
						ModelStructureChangeListener.IDENTITY,
						ModelStructureChangeListener.IDENTITY,
						ModelStructureChangeListener.IDENTITY,
						ProgramGlobals.getCurrentModelPanel().getPerspArea().getViewport().getParticleTextureInstance());
		editorRenderModel.updateNodes(false); // update to 0 position
	}

	public static ModelPanel newTempModelPanel(ImageIcon icon, EditableModel model) {
		MainPanel mainPanel = ProgramGlobals.getMainPanel();
		ModelHandler modelHandler = new ModelHandler(model);
		return new ModelPanel(modelHandler, ProgramGlobals.getPrefs(),
				mainPanel.selectionItemTypeGroup,
				mainPanel.selectionModeGroup,
				mainPanel.coordDisplayListener,
				mainPanel.viewportTransferHandler,
				mainPanel.viewportListener, icon, false
		);
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
		//        blankTextureModel.setTemp(true);

		Geoset newGeoset = new Geoset();
		if (version == 1000) {
			newGeoset.setLevelOfDetail(0);
		}
		Layer layer = new Layer("Blend", new Bitmap(fileName));
		layer.setUnshaded(true);
		//        layer.setTwoSided(true);
		Material material = new Material(layer);
		newGeoset.setMaterial(material);
		BufferedImage bufferedImage = material.getBufferedImage(blankTextureModel.getWrappedDataSource());
		int textureWidth = bufferedImage.getWidth();
		int textureHeight = bufferedImage.getHeight();
		float aspectRatio = textureWidth / (float) textureHeight;

		int displayWidth = (int) (aspectRatio > 1 ? 128 : 128 * aspectRatio);
		int displayHeight = (int) (aspectRatio < 1 ? 128 : 128 / aspectRatio);

		int groundOffset = aspectRatio > 1 ? (128 - displayHeight) / 2 : 0;

		Vec2 min = new Vec2(-displayWidth / 2.0, groundOffset);
		Vec2 max = new Vec2(displayWidth / 2.0, displayHeight + groundOffset);

		ModelUtils.Mesh planeMesh = ModelUtils.createPlane((byte) 0, true, 0, max, min, 1);
		newGeoset.addVerticies(planeMesh.getVertices());
		newGeoset.setTriangles(planeMesh.getTriangles());

		blankTextureModel.add(newGeoset);
		ExtLog extLog = new ExtLog(128).setDefault();
		blankTextureModel.setExtents(extLog);
		blankTextureModel.add(new Animation("Stand", 0, 1000));
		TempSaveModelStuff.doSavePreps(blankTextureModel);
		return blankTextureModel;
	}

	public static void loadModel(boolean temporary, boolean selectNewTab, ModelPanel modelPanel) {
		MainPanel mainPanel = ProgramGlobals.getMainPanel();
		if (temporary) {
			modelPanel.getModelView().getModel().setTemp(true);
		}
		JMenuItem menuItem = new JMenuItem(modelPanel.getModel().getName());
		menuItem.setIcon(modelPanel.getIcon());
		menuItem.addActionListener(e -> setCurrentModel(modelPanel));
		modelPanel.setJMenuItem(menuItem);

		modelPanel.changeActivity(mainPanel.actionTypeGroup.getActiveButtonType());

		MenuBar.addModelPanel(modelPanel);

		MainLayoutCreator mainLayoutCreator = mainPanel.getMainLayoutCreator();

		if (ProgramGlobals.getCurrentModelPanel() == modelPanel) {
			mainLayoutCreator.getViewportControllerWindowView().setComponent(modelPanel.getModelEditingTreePane());
			mainLayoutCreator.getViewportControllerWindowView().repaint();
			mainLayoutCreator.getModelDataView().setComponent(modelPanel.getComponentBrowserTreePane());
			mainLayoutCreator.getModelComponentView().setComponent(modelPanel.getComponentsPanel());
			mainLayoutCreator.getModelDataView().repaint();
		}
		if (selectNewTab) {
			modelPanel.getMenuItem().doClick();
		}
		ProgramGlobals.getModelPanels().add(modelPanel);

		if (temporary) {
			modelPanel.getModelView().getModel().setFileRef(null);
		}

		MenuBar.setToolsMenuEnabled(true);

		if (selectNewTab && ProgramGlobals.getPrefs().getQuickBrowse()) {
			for (int i = (ProgramGlobals.getModelPanels().size() - 2); i >= 0; i--) {
				ModelPanel openModelPanel = ProgramGlobals.getModelPanels().get(i);
				if (openModelPanel.getUndoManager().isRedoListEmpty() && openModelPanel.getUndoManager().isUndoListEmpty()) {
					if (openModelPanel.close()) {
						ProgramGlobals.getModelPanels().remove(openModelPanel);
						MenuBar.removeModelPanel(openModelPanel);
					}
				}
			}
		}
	}

	public static void setCurrentModel(ModelPanel modelPanel) {
		MainPanel mainPanel = ProgramGlobals.getMainPanel();
		ProgramGlobals.setCurrentModelPanel(modelPanel);
		MainLayoutCreator mainLayoutCreator = mainPanel.getMainLayoutCreator();
		if (ProgramGlobals.getCurrentModelPanel() == null) {
			JPanel jPanel = new JPanel();
			jPanel.add(new JLabel("..."));
			mainLayoutCreator.getViewportControllerWindowView().setComponent(jPanel);


			mainLayoutCreator.getFrontView().setComponent(new JPanel());
			mainLayoutCreator.getBottomView().setComponent(new JPanel());
			mainLayoutCreator.getLeftView().setComponent(new JPanel());
			mainLayoutCreator.getPerspectiveView().setComponent(new JPanel());
			mainLayoutCreator.getPreviewView().setComponent(new JPanel());
			mainLayoutCreator.getAnimationControllerView().setComponent(new JPanel());
			refreshAnimationModeState();

			mainLayoutCreator.getTimeSliderPanel().setModelHandler(null);
			mainLayoutCreator.getCreatorPanel().setModelEditorManager(null);
			mainLayoutCreator.getCreatorPanel().setCurrentModel(null);

			mainLayoutCreator.getModelDataView().setComponent(new JPanel());
			mainLayoutCreator.getModelComponentView().setComponent(new JPanel());
		} else {
			mainLayoutCreator.getViewportControllerWindowView().setComponent(modelPanel.getModelEditingTreePane());

			mainLayoutCreator.getFrontView().setComponent(modelPanel.getFrontArea());
			mainLayoutCreator.getBottomView().setComponent(modelPanel.getBotArea());
			mainLayoutCreator.getLeftView().setComponent(modelPanel.getSideArea());
			mainLayoutCreator.getPerspectiveView().setComponent(modelPanel.getPerspArea());
			mainLayoutCreator.getPreviewView().setComponent(modelPanel.getAnimationViewer());
			mainLayoutCreator.getAnimationControllerView().setComponent(modelPanel.getAnimationController());
			refreshAnimationModeState();

			mainLayoutCreator.getTimeSliderPanel().setModelHandler(ProgramGlobals.getCurrentModelPanel().getModelHandler());
			mainLayoutCreator.getCreatorPanel().setModelEditorManager(ProgramGlobals.getCurrentModelPanel().getModelEditorManager());
			mainLayoutCreator.getCreatorPanel().setCurrentModel(ProgramGlobals.getCurrentModelPanel().getModelHandler());

			mainLayoutCreator.getModelDataView().setComponent(modelPanel.getComponentBrowserTreePane());
			mainLayoutCreator.getModelComponentView().setComponent(modelPanel.getComponentsPanel());

			ProgramGlobals.getCurrentModelPanel().reloadComponentBrowser();
			ProgramGlobals.getCurrentModelPanel().reloadModelEditingTree();
		}
		mainPanel.viewportListener.viewportChanged(null);
		mainLayoutCreator.getTimeSliderPanel().revalidateKeyframeDisplay();
	}

	public static void loadFile(final File f) {
		loadFile(f, false, true, MDLIcon);
	}

	public static void loadFile(final File f, boolean temporary, final boolean selectNewTab, final ImageIcon icon) {
		System.out.println("loadFile: " + f.getName());
		System.out.println("filePath: " + f.getPath());
		ExtFilter extFilter = new ExtFilter();
		if (f.exists()) {
			final String pathLow = f.getPath().toLowerCase();
			String ext = pathLow.replaceAll(".+\\.(?=.+)", "");
			ModelPanel tempModelPanel = null;
			if (extFilter.isSupTexture(ext)) {
				final EditableModel model;
				if (ext.equals("dds")) {
					model = getImagePlaneModel(f, 1000);
				} else {
					model = getImagePlaneModel(f, 800);
				}
				model.setTemp(true);
				//            model.setFileRef(f);
				temporary = false;
				tempModelPanel = newTempModelPanel(icon, model);

			}

			if (Arrays.asList("mdx", "mdl").contains(ext)) {
				try {

					final EditableModel model = MdxUtils.loadEditable(f);
					model.setFileRef(f);

					tempModelPanel = newTempModelPanel(icon, model);

				} catch (final IOException e) {
					e.printStackTrace();
					ExceptionPopup.display(e);
					throw new RuntimeException("Reading mdx failed");
				}
			} else if (Arrays.asList("obj", "fbx").contains(ext)) {
				try {
					System.out.println("importing file \"" + f.getName() + "\" this might take a while...");
					long timeStart = System.currentTimeMillis();
					AiProgressHandler aiProgressHandler = new AiProgressHandler() {
						@Override
						public boolean update(float v) {
							//                            System.out.println("progress: " + (int)((v+1)*100) + "%  " + (System.currentTimeMillis()-timeStart) + " ms");
							return true;
						}
					};
					//                    AiClassLoaderIOSystem aiIOSystem = new AiClassLoaderIOSystem();
					TwiAiIoSys twiAiIoSys = new TwiAiIoSys();
					AiScene scene = Jassimp.importFile(f.getPath(), new HashSet<>(Collections.singletonList(AiPostProcessSteps.TRIANGULATE)), twiAiIoSys, aiProgressHandler);
					TwiAiSceneParser twiAiSceneParser = new TwiAiSceneParser(scene);
					//                    final EditableModel model = new EditableModel(scene);
					System.out.println("took " + (System.currentTimeMillis() - timeStart) + " ms to load the model");
					EditableModel model = twiAiSceneParser.getEditableModel();
					model.setFileRef(f);
					//
					tempModelPanel = newTempModelPanel(icon, model);
				} catch (final Exception e) {
					ExceptionPopup.display(e);
					e.printStackTrace();
				}
			}
			if (tempModelPanel != null) {
				loadModel(temporary, selectNewTab, tempModelPanel);
			}
		} else if (SaveProfile.get().getRecent().contains(f.getPath())) {
			int option = JOptionPane.showConfirmDialog(ProgramGlobals.getMainPanel(), "Could not find the file.\nRemove from recent?", "File not found", JOptionPane.YES_NO_OPTION);
			if (option == JOptionPane.YES_OPTION) {
				SaveProfile.get().removeFromRecent(f.getPath());
				MenuBar.updateRecent();
			}
		}
	}

	public static void revert() {
		final ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		final int oldIndex = ProgramGlobals.getModelPanels().indexOf(modelPanel);
		if (modelPanel != null) {
			if (modelPanel.close()) {
				ProgramGlobals.getModelPanels().remove(modelPanel);
				MenuBar.removeModelPanel(modelPanel);
				if (ProgramGlobals.getModelPanels().size() > 0) {
					final int newIndex = Math.min(ProgramGlobals.getModelPanels().size() - 1, oldIndex);
					setCurrentModel(ProgramGlobals.getModelPanels().get(newIndex));
				} else {
					// TODO remove from notifiers to fix leaks
					setCurrentModel(null);
				}
				final File fileToRevert = modelPanel.getModel().getFile();
				loadFile(fileToRevert);
			}
		}
	}

	public static void loadFile(File f, boolean temporary) {
		loadFile(f, temporary, true, MDLIcon);
	}
}
