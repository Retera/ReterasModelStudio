package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.editor.model.util.TwiAiIoSys;
import com.hiveworkshop.rms.editor.model.util.TwiAiSceneParser;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.parsers.mdlx.util.MdxUtils;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ToolbarActionButtonType;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ToolbarButtonGroup;
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
import java.util.List;

public class ModelLoader {
	static final ImageIcon MDLIcon = RMSIcons.MDLIcon;

	public static void refreshAnimationModeState(MainPanel mainPanel) {
		ModelPanel modelPanel = mainPanel.currentModelPanel();
		if (mainPanel.animationModeState) {
			if ((modelPanel != null) && (modelPanel.getModel() != null)) {
				if (modelPanel.getModel().getAnimsSize() > 0) {
					final Animation anim = modelPanel.getModel().getAnim(0);
					modelPanel.getModelHandler().getEditTimeEnv().setAnimation(anim);
					modelPanel.getModelHandler().getEditTimeEnv().setStaticViewMode(!mainPanel.animationModeState);
				}
				refreshAndUpdateModelPanel(mainPanel);
				mainPanel.timeSliderPanel.setNodeSelectionManager(modelPanel.getModelEditorManager().getNodeAnimationSelectionManager());
			}
			if ((mainPanel.actionTypeGroup.getActiveButtonType() == mainPanel.actionTypeGroup.getToolbarButtonTypes()[3])
					|| (mainPanel.actionTypeGroup.getActiveButtonType() == mainPanel.actionTypeGroup.getToolbarButtonTypes()[4])) {
				mainPanel.actionTypeGroup.setToolbarButtonType(mainPanel.actionTypeGroup.getToolbarButtonTypes()[0]);
			}
		}

		if (!mainPanel.animationModeState) {
			if ((modelPanel != null) && (modelPanel.getModel() != null)) {
				refreshAndUpdateModelPanel(mainPanel);
				modelPanel.getModelHandler().getEditTimeEnv().setAnimation(null);
				modelPanel.getModelHandler().getEditTimeEnv().setStaticViewMode(!mainPanel.animationModeState);
			}
		}
		List<ToolbarButtonGroup<ToolbarActionButtonType>.ToolbarButtonAction> buttons = mainPanel.actionTypeGroup.getButtons();

		int numberOfButtons = buttons.size();
		for (int i = 3; i < numberOfButtons; i++) {
			buttons.get(i).getButton().setVisible(!mainPanel.animationModeState);
		}
		mainPanel.snapButton.setVisible(!mainPanel.animationModeState);
		mainPanel.timeSliderPanel.setDrawing(mainPanel.animationModeState);
		mainPanel.timeSliderPanel.setKeyframeModeActive(mainPanel.animationModeState);

		mainPanel.timeSliderPanel.repaint();
		mainPanel.creatorPanel.setAnimationModeState(mainPanel.animationModeState);
	}

	private static void refreshAndUpdateModelPanel(MainPanel mainPanel) {
		RenderModel editorRenderModel = mainPanel.currentModelPanel().getEditorRenderModel();
		editorRenderModel
				.refreshFromEditor(
//						mainPanel.animatedRenderEnvironment,
						ModelStructureChangeListener.IDENTITY,
						ModelStructureChangeListener.IDENTITY,
						ModelStructureChangeListener.IDENTITY,
						mainPanel.currentModelPanel().getPerspArea().getViewport().getParticleTextureInstance());
		editorRenderModel.updateNodes(false); // update to 0 position
	}

	public static ModelPanel newTempModelPanel(MainPanel mainPanel, ImageIcon icon, EditableModel model) {
		ModelPanel temp;
		ModelHandler modelHandler = new ModelHandler(model, mainPanel.getUndoHandler());
		temp = new ModelPanel(mainPanel, modelHandler, mainPanel.prefs,
				mainPanel.selectionItemTypeGroup,
				mainPanel.selectionModeGroup,
				mainPanel.modelStructureChangeListener,
				mainPanel.coordDisplayListener,
				mainPanel.viewportTransferHandler,
				mainPanel.viewportListener, icon, false
		);
		return temp;
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
		blankTextureModel.doSavePreps();
		return blankTextureModel;
	}

	public static void loadModel(MainPanel mainPanel, boolean temporary, boolean selectNewTab, ModelPanel modelPanel) {
		if (temporary) {
			modelPanel.getModelViewManager().getModel().setTemp(true);
		}
		JMenuItem menuItem = new JMenuItem(modelPanel.getModel().getName());
		menuItem.setIcon(modelPanel.getIcon());

		MenuBar.windowMenu.add(menuItem);
		menuItem.addActionListener(e -> setCurrentModel(mainPanel, modelPanel));
		modelPanel.setJMenuItem(menuItem);
		modelPanel.getModelViewManager().addStateListener(new RepaintingModelStateListener(mainPanel));
		modelPanel.changeActivity(mainPanel.currentActivity);

//		if (mainPanel.mEditingTP == null) {
		if (mainPanel.currentModelPanel == modelPanel) {
//			mainPanel.geoControl = new JScrollPane(modelPanel.getModelViewManagingTree());
			mainPanel.viewportControllerWindowView.setComponent(modelPanel.getModelEditingTreePane());
			mainPanel.viewportControllerWindowView.repaint();
//			mainPanel.geoControlModelData = new JScrollPane(modelPanel.getModelComponentBrowserTree());
			mainPanel.modelDataView.setComponent(modelPanel.getComponentBrowserTreePane());
			mainPanel.modelComponentView.setComponent(modelPanel.getComponentsPanel());
			mainPanel.modelDataView.repaint();
		}
		if (selectNewTab) {
			modelPanel.getMenuItem().doClick();
		}
		mainPanel.modelPanels.add(modelPanel);

		if (temporary) {
			modelPanel.getModelViewManager().getModel().setFileRef(null);
		}

		MenuBar.toolsMenu.setEnabled(true);

		if (selectNewTab && mainPanel.prefs.getQuickBrowse()) {
			for (int i = (mainPanel.modelPanels.size() - 2); i >= 0; i--) {
				ModelPanel openModelPanel = mainPanel.modelPanels.get(i);
				if (openModelPanel.getUndoManager().isRedoListEmpty() && openModelPanel.getUndoManager().isUndoListEmpty()) {
					if (openModelPanel.close()) {
						mainPanel.modelPanels.remove(openModelPanel);
						MenuBar.windowMenu.remove(openModelPanel.getMenuItem());
					}
				}
			}
		}
	}

	public static void setCurrentModel(MainPanel mainPanel, ModelPanel modelPanel) {
		mainPanel.currentModelPanel = modelPanel;
		if (mainPanel.currentModelPanel == null) {
			JPanel jPanel = new JPanel();
			jPanel.add(new JLabel("..."));
			mainPanel.viewportControllerWindowView.setComponent(jPanel);

			mainPanel.frontView.setComponent(new JPanel());
			mainPanel.bottomView.setComponent(new JPanel());
			mainPanel.leftView.setComponent(new JPanel());
			mainPanel.perspectiveView.setComponent(new JPanel());
			mainPanel.previewView.setComponent(new JPanel());
			mainPanel.animationControllerView.setComponent(new JPanel());
			refreshAnimationModeState(mainPanel);

			mainPanel.timeSliderPanel.setModelHandler(null);
			mainPanel.creatorPanel.setModelEditorManager(null);
			mainPanel.creatorPanel.setCurrentModel(null);

			mainPanel.modelDataView.setComponent(new JPanel());
			mainPanel.modelComponentView.setComponent(new JPanel());
		} else {
			mainPanel.viewportControllerWindowView.setComponent(modelPanel.getModelEditingTreePane());

			mainPanel.frontView.setComponent(modelPanel.getFrontArea());
			mainPanel.bottomView.setComponent(modelPanel.getBotArea());
			mainPanel.leftView.setComponent(modelPanel.getSideArea());
			mainPanel.perspectiveView.setComponent(modelPanel.getPerspArea());
			mainPanel.previewView.setComponent(modelPanel.getAnimationViewer());
			mainPanel.animationControllerView.setComponent(modelPanel.getAnimationController());
			refreshAnimationModeState(mainPanel);

			mainPanel.timeSliderPanel.setModelHandler(mainPanel.currentModelPanel.getModelHandler());
			mainPanel.creatorPanel.setModelEditorManager(mainPanel.currentModelPanel.getModelEditorManager());
			mainPanel.creatorPanel.setCurrentModel(mainPanel.currentModelPanel.getModelHandler());

			mainPanel.modelDataView.setComponent(modelPanel.getComponentBrowserTreePane());
			mainPanel.modelComponentView.setComponent(modelPanel.getComponentsPanel());

			mainPanel.currentModelPanel.reloadComponentBrowser();
			mainPanel.currentModelPanel.reloadModelEditingTree();
		}
		mainPanel.viewportListener.viewportChanged(null);
		mainPanel.timeSliderPanel.revalidateKeyframeDisplay();
	}

	public static void loadFile(MainPanel mainPanel, final File f) {
		loadFile(mainPanel, f, false, true, MDLIcon);
	}

	public static void loadFile(MainPanel mainPanel, final File f, boolean temporary, final boolean selectNewTab, final ImageIcon icon) {
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
				tempModelPanel = newTempModelPanel(mainPanel, icon, model);

			}

			if (Arrays.asList("mdx", "mdl").contains(ext)) {
				try {

					final EditableModel model = MdxUtils.loadEditable(f);
					model.setFileRef(f);

					tempModelPanel = newTempModelPanel(mainPanel, icon, model);

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
					tempModelPanel = newTempModelPanel(mainPanel, icon, model);
				} catch (final Exception e) {
					ExceptionPopup.display(e);
					e.printStackTrace();
				}
			}
			if (tempModelPanel != null) {
				loadModel(mainPanel, temporary, selectNewTab, tempModelPanel);
			}
		} else if (SaveProfile.get().getRecent().contains(f.getPath())) {
			int option = JOptionPane.showConfirmDialog(mainPanel, "Could not find the file.\nRemove from recent?", "File not found", JOptionPane.YES_NO_OPTION);
			if (option == JOptionPane.YES_OPTION) {
				SaveProfile.get().removeFromRecent(f.getPath());
				MenuBar.updateRecent();
			}
		}
	}

	static void revert(MainPanel mainPanel) {
		final ModelPanel modelPanel = mainPanel.currentModelPanel();
		final int oldIndex = mainPanel.modelPanels.indexOf(modelPanel);
		if (modelPanel != null) {
			if (modelPanel.close()) {
				mainPanel.modelPanels.remove(modelPanel);
				MenuBar.windowMenu.remove(modelPanel.getMenuItem());
				if (mainPanel.modelPanels.size() > 0) {
					final int newIndex = Math.min(mainPanel.modelPanels.size() - 1, oldIndex);
					setCurrentModel(mainPanel, mainPanel.modelPanels.get(newIndex));
				} else {
					// TODO remove from notifiers to fix leaks
					setCurrentModel(mainPanel, null);
				}
				final File fileToRevert = modelPanel.getModel().getFile();
				loadFile(mainPanel, fileToRevert);
			}
		}
	}

	public static void loadFile(MainPanel mainPanel, File f, boolean temporary) {
		loadFile(mainPanel, f, temporary, true, MDLIcon);
	}
}
