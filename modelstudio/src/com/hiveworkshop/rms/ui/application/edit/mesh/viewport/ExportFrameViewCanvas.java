package com.hiveworkshop.rms.ui.application.edit.mesh.viewport;

import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.viewer.twiTestRenderMaster.AnimationController2;
import com.hiveworkshop.rms.ui.application.viewer.twiTestRenderMaster.ExportFrameViewportPanel;
import com.hiveworkshop.rms.ui.application.viewer.twiTestRenderMaster.GifExportHelper;
import com.hiveworkshop.rms.ui.application.viewer.twiTestRenderMaster.ViewportCanvas;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.util.ModelDependentView;
import com.hiveworkshop.rms.util.TinyToggleButton;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class ExportFrameViewCanvas extends ModelDependentView {
	JSplitPane splitPane;
	JSplitPane splitPane2;
	JScrollPane scrollingPane;
	ExportFrameViewportPanel viewportPanel;
	ModelPanel modelPanel;
	String name;
	JPanel dudPanel;
	AnimationController2 animationController;

	TinyToggleButton renderTextures;
	TinyToggleButton wireFrame;
	TinyToggleButton showNormals;
	TinyToggleButton show3dVerts;

	GifExportHelper gifExportHelper;


	public ExportFrameViewCanvas(String s, boolean allowButtonPanel, boolean initOrtho) {
		super(s, null, new JPanel());
		this.name = s;
		dudPanel = new JPanel(new MigLayout());
		viewportPanel = new ExportFrameViewportPanel(allowButtonPanel, false);
		ViewportCanvas viewport = viewportPanel.getViewport();
		viewport.getCameraHandler().setOrtho(initOrtho);
		viewport.getCameraHandler().setCameraRotation(0, 0);

//		setUpViewAngle();

		animationController = new AnimationController2(viewport::setLevelOfDetail);

		gifExportHelper = new GifExportHelper(viewport);

		JPanel panel1 = new JPanel(new MigLayout("fill", "[grow][]"));
		panel1.add(animationController, "growx");
		panel1.add(gifExportHelper.getSettingsPanel());
		scrollingPane = new JScrollPane(panel1);
		scrollingPane.getVerticalScrollBar().setUnitIncrement(16);
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, viewportPanel, scrollingPane);
		splitPane2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, splitPane, gifExportHelper.getGifPanel());
		this.setComponent(splitPane2);
//		this.setComponent(splitPane);
		splitPane.setDividerLocation(0.8);

		// particles:       u2728 ✨, 2614 ☔, 2E19 ⸙,
		// renderTextures:  u26FE ⛾, 26BF ⚿, 25A8 ▨, 239A, ⎚
		// wireFrame:       u2B1A ⬚, 2BF4 ⯴, 26E4 ⛤, 25A6 ▦, 27B0 ➰, 27BF ➿, 2341 ⍁, 2342 ⍂
		// showNormals:     u23CA ⏊, 2BE2 ⯢, 2600 ☀, 23B7 ⎷,
		// show3dVerts:     u2BCC ⯌, 2360 ⍠, 26EC ⛬, 26DA ⛚, 26AF ⚯, 2058 ⁘,

		String ugg = ""
				+ "\u26AF"
				+ "\u2BF4"
				+ "\u2BE2"
				+ "\u2360";

		renderTextures =    getButton("\u26FE", true, b -> viewportPanel.setRenderTextures(b));
		wireFrame =         getButton("\u2342", false, b -> viewportPanel.setWireFrame(b));
		show3dVerts =       getButton("\u26DA", true, b -> viewportPanel.setShow3dVerts(b));
		showNormals =       getButton("\u23CA", false, b -> viewportPanel.setShowNormals(b));
//		renderTextures =    getButton("texture", true, b -> displayPanel.setRenderTextures(b));
//		wireFrame =         getButton("wireframe", false, b -> displayPanel.setWireFrame(b));
//		show3dVerts =       getButton("verts", true, b -> displayPanel.setShow3dVerts(b));
//		showNormals =       getButton("normals", false, b -> displayPanel.setShowNormals(b));

		getCustomTitleBarComponents().add(renderTextures);
		getCustomTitleBarComponents().add(wireFrame);
		getCustomTitleBarComponents().add(show3dVerts);
		getCustomTitleBarComponents().add(showNormals);
	}

	private void setUpViewAngle() {
		switch (name) {
			case "Front" -> viewportPanel.getViewport().getCameraHandler().setCameraRotation(0, 0);
			case "Side" -> viewportPanel.getViewport().getCameraHandler().setCameraRotation(90, 0);
			case "Top" -> viewportPanel.getViewport().getCameraHandler().setCameraRotation(0, 90);
		}
	}

	@Override
	public ExportFrameViewCanvas setModelPanel(ModelPanel modelPanel) {
		this.modelPanel = modelPanel;
		if (modelPanel == null) {
			this.setComponent(dudPanel);
			viewportPanel.setModel(null, null);
			animationController.setModel(null, null, true);
			gifExportHelper.setRenderModel(null);
		} else {
			ModelHandler modelHandler = modelPanel.getModelHandler();
			RenderModel renderModel = new RenderModel(modelHandler.getModel(), modelHandler.getModelView());
			viewportPanel.setModel(renderModel, null);
			viewportPanel.setControlsVisible(ProgramGlobals.getPrefs().showVMControls());
			viewportPanel.getViewport().getCameraHandler().setCameraRotation(0, 0);
			gifExportHelper.setRenderModel(renderModel);
//			setUpViewAngle();

			animationController.setModel(renderModel, renderModel.getTimeEnvironment().getCurrentAnimation(), true);

			reload();
//			splitPane.setDividerLocation(0.5);
			splitPane2.setDividerLocation(0.5);
		}
		System.out.println("name: " + name + ", panel: " + modelPanel);
		return this;
	}

	@Override
	public ExportFrameViewCanvas preferencesUpdated(){
		if(viewportPanel != null){
			viewportPanel.setControlsVisible(ProgramGlobals.getPrefs().showVMControls());
		}
		return this;
	}

	@Override
	public ExportFrameViewCanvas reload() {
		if (modelPanel != null) {
			animationController.reload().repaint();
			viewportPanel.reload().repaint();
		}
		return this;
	}

	Color onC = new Color(255, 255, 255);
	Color offC = new Color(100, 100, 100);
	private TinyToggleButton getButton(String text, boolean initial, Consumer<Boolean> boolConsumer){
		TinyToggleButton button = new TinyToggleButton(text, onC, offC, boolConsumer);
		button.setOn(initial);
		boolConsumer.accept(initial);
		return button;

	}

	public ViewportCanvas getPerspectiveViewport(){
		return viewportPanel.getViewport();
	}
}
//package com.hiveworkshop.rms.ui.application.edit.mesh.viewport;
//
//import com.hiveworkshop.rms.editor.render3d.RenderModel;
//import com.hiveworkshop.rms.parsers.twiImageStuff.TwiWriteGif;
//import com.hiveworkshop.rms.ui.application.FileDialog;
//import com.hiveworkshop.rms.ui.application.ProgramGlobals;
//import com.hiveworkshop.rms.ui.application.viewer.twiTestRenderMaster.AnimationController2;
//import com.hiveworkshop.rms.ui.application.viewer.twiTestRenderMaster.ExportFrameHolderThigi;
//import com.hiveworkshop.rms.ui.application.viewer.twiTestRenderMaster.ExportFrameViewportCanvas;
//import com.hiveworkshop.rms.ui.application.viewer.twiTestRenderMaster.GifExportSettings;
//import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
//import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
//import com.hiveworkshop.rms.util.ModelDependentView;
//import com.hiveworkshop.rms.util.TinyToggleButton;
//import net.miginfocom.swing.MigLayout;
//
//import javax.swing.*;
//import javax.swing.filechooser.FileNameExtensionFilter;
//import java.awt.*;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.nio.ByteBuffer;
//import java.nio.file.Files;
//import java.nio.file.StandardCopyOption;
//import java.util.Collections;
//import java.util.function.Consumer;
//
//public class ExportFrameViewCanvas extends ModelDependentView {
//	JSplitPane splitPane;
//	JSplitPane splitPane2;
//	JScrollPane scrollingPane;
//	ExportFrameHolderThigi viewportPanel;
//	ModelPanel modelPanel;
//	String name;
//	JPanel dudPanel;
//	AnimationController2 animationController;
//
//	TinyToggleButton renderTextures;
//	TinyToggleButton wireFrame;
//	TinyToggleButton showNormals;
//	TinyToggleButton show3dVerts;
//
//	GifExportSettings gifExportSettings;
//
//
//	public ExportFrameViewCanvas(String s, boolean allowButtonPanel, boolean initOrtho) {
//		super(s, null, new JPanel());
//		this.name = s;
//		dudPanel = new JPanel(new MigLayout());
//		viewportPanel = new ExportFrameHolderThigi(allowButtonPanel);
//		viewportPanel.getViewport().getCameraHandler().setOrtho(initOrtho);
//		viewportPanel.getViewport().getCameraHandler().setCameraRotation(0, 0);
////		setUpViewAngle();
//
//		animationController = new AnimationController2(viewportPanel.getViewport()::setLevelOfDetail);
//
//		gifExportSettings = new GifExportSettings(viewportPanel.getViewport()::setDoExport);
//
//		JPanel panel1 = new JPanel(new MigLayout("fill", "[grow][]"));
//		panel1.add(animationController, "growx");
//		panel1.add(gifExportSettings.getSettingsPanel());
//		scrollingPane = new JScrollPane(panel1);
//		scrollingPane.getVerticalScrollBar().setUnitIncrement(16);
//		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, viewportPanel, scrollingPane);
//		splitPane2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, splitPane, viewportPanel.getGifPanel());
//		this.setComponent(splitPane2);
////		this.setComponent(splitPane);
//		splitPane.setDividerLocation(0.8);
//
//		// particles:       u2728 ✨, 2614 ☔, 2E19 ⸙,
//		// renderTextures:  u26FE ⛾, 26BF ⚿, 25A8 ▨, 239A, ⎚
//		// wireFrame:       u2B1A ⬚, 2BF4 ⯴, 26E4 ⛤, 25A6 ▦, 27B0 ➰, 27BF ➿, 2341 ⍁, 2342 ⍂
//		// showNormals:     u23CA ⏊, 2BE2 ⯢, 2600 ☀, 23B7 ⎷,
//		// show3dVerts:     u2BCC ⯌, 2360 ⍠, 26EC ⛬, 26DA ⛚, 26AF ⚯, 2058 ⁘,
//
//		String ugg = ""
//				+ "\u26AF"
//				+ "\u2BF4"
//				+ "\u2BE2"
//				+ "\u2360";
//
//		renderTextures =    getButton("\u26FE", true, b -> viewportPanel.setRenderTextures(b));
//		wireFrame =         getButton("\u2342", false, b -> viewportPanel.setWireFrame(b));
//		show3dVerts =       getButton("\u26DA", true, b -> viewportPanel.setShow3dVerts(b));
//		showNormals =       getButton("\u23CA", false, b -> viewportPanel.setShowNormals(b));
////		renderTextures =    getButton("texture", true, b -> displayPanel.setRenderTextures(b));
////		wireFrame =         getButton("wireframe", false, b -> displayPanel.setWireFrame(b));
////		show3dVerts =       getButton("verts", true, b -> displayPanel.setShow3dVerts(b));
////		showNormals =       getButton("normals", false, b -> displayPanel.setShowNormals(b));
//
//		getCustomTitleBarComponents().add(renderTextures);
//		getCustomTitleBarComponents().add(wireFrame);
//		getCustomTitleBarComponents().add(show3dVerts);
//		getCustomTitleBarComponents().add(showNormals);
//	}
//
//	private void setUpViewAngle() {
//		switch (name) {
//			case "Front" -> viewportPanel.getViewport().getCameraHandler().setCameraRotation(0, 0);
//			case "Side" -> viewportPanel.getViewport().getCameraHandler().setCameraRotation(90, 0);
//			case "Top" -> viewportPanel.getViewport().getCameraHandler().setCameraRotation(0, 90);
//		}
//	}
//
//	@Override
//	public ExportFrameViewCanvas setModelPanel(ModelPanel modelPanel) {
//		this.modelPanel = modelPanel;
//		if (modelPanel == null) {
//			this.setComponent(dudPanel);
//			viewportPanel.setModel(null, null);
//			animationController.setModel(null, null, true);
//		} else {
//			ModelHandler modelHandler = modelPanel.getModelHandler();
//			RenderModel renderModel = new RenderModel(modelHandler.getModel(), modelHandler.getModelView());
//			viewportPanel.setModel(renderModel, null);
//			viewportPanel.setControlsVisible(ProgramGlobals.getPrefs().showVMControls());
//			viewportPanel.getViewport().getCameraHandler().setCameraRotation(0, 0);
////			setUpViewAngle();
//
//			animationController.setModel(renderModel, renderModel.getTimeEnvironment().getCurrentAnimation(), true);
//
//			reload();
////			splitPane.setDividerLocation(0.5);
//			splitPane2.setDividerLocation(0.5);
//		}
//		System.out.println("name: " + name + ", panel: " + modelPanel);
//		return this;
//	}
//
//	@Override
//	public ExportFrameViewCanvas preferencesUpdated(){
//		if(viewportPanel != null){
//			viewportPanel.setControlsVisible(ProgramGlobals.getPrefs().showVMControls());
//		}
//		return this;
//	}
//
//	@Override
//	public ExportFrameViewCanvas reload() {
//		if (modelPanel != null) {
//			animationController.reload().repaint();
//			viewportPanel.reload().repaint();
//		}
//		return this;
//	}
//
//	Color onC = new Color(255, 255, 255);
//	Color offC = new Color(100, 100, 100);
//	private TinyToggleButton getButton(String text, boolean initial, Consumer<Boolean> boolConsumer){
//		TinyToggleButton button = new TinyToggleButton(text, onC, offC, boolConsumer);
//		button.setOn(initial);
//		boolConsumer.accept(initial);
//		return button;
//
//	}
//
//	public ExportFrameViewportCanvas getPerspectiveViewport(){
//		return viewportPanel.getViewport();
//	}
//
//	JPanel gifPanel;
//	JLabel gifLabel;
//	JButton saveGif;
//	byte[] bytes;
//
//
//
//	private JPanel createGifPanel() {
//		JPanel gifPanel = new JPanel(new MigLayout("fill"));
//		gifLabel = new JLabel();
//		gifPanel.add(gifLabel, "");
//		saveGif = new JButton("Save gif");
//		saveGif.addActionListener(e -> saveGif());
//		saveGif.setEnabled(false);
//		gifPanel.add(saveGif, "top");
//		return gifPanel;
//	}
//
//	private void updateIcon(byte[] bytes){
//		this.bytes = bytes;
//		if(bytes != null){
//			gifLabel.setIcon(new ImageIcon(bytes));
//			saveGif.setEnabled(true);
//		} else {
//			gifLabel.setIcon(null);
//			saveGif.setEnabled(false);
//		}
//	}
//
//	private void saveGif(){
//		if(bytes != null){
//			saveBytes("image.gif", new FileNameExtensionFilter("GIF Image", "gif"), bytes);
//		}
//	}
//
//	private void doExport1(){
//		ByteBuffer[] buffers = new ByteBuffer[framesToExp];
//		int[] frameDelays = new int[framesToExp];
//		for(int i = 0; i<framesToExp; i++){
//			buffers[i] = byteBuffers.get(i);
//			frameDelays[i] = delays.get(i)/10;
////			frameDelays[i] = 1;
//		}
//		frameDelays[0] += settings.getDelayFirst()/10;
//		frameDelays[framesToExp-1] += settings.getDelayLast()/10;
//		byte[] asBytes = TwiWriteGif.getAsBytes(buffers, frameDelays, settings);
//		if(imageByteArrayConsumer != null){
//			imageByteArrayConsumer.accept(asBytes);
//		}
//	}
//
//	public void saveBytes(String suggestedName, FileNameExtensionFilter filter, byte[] bytes) {
//		com.hiveworkshop.rms.ui.application.FileDialog fileDialog = new com.hiveworkshop.rms.ui.application.FileDialog(this);
//		String fileName = suggestedName.replaceAll(".+[\\\\/](?=.+)", "");
//		File selectedFile = fileDialog.getSaveFile(fileName, Collections.singletonList(filter));
//		if (selectedFile != null) {
//			String expExt = fileDialog.getExtensionOrNull(fileName);
//			String saveExt = fileDialog.getExtensionOrNull(selectedFile);
//			if(expExt != null && (saveExt == null || !saveExt.equals(expExt))){
//				selectedFile = new File(selectedFile.getPath() + "." + expExt);
//			}
//			try (FileOutputStream fileOutputStream = new FileOutputStream(selectedFile)){
//				fileOutputStream.write(bytes);
//			} catch (IOException ioException) {
//				ioException.printStackTrace();
//			}
//
//		}
//	}
//	public void saveStream(String suggestedName, FileNameExtensionFilter filter, InputStream resourceAsStream) {
//		com.hiveworkshop.rms.ui.application.FileDialog fileDialog = new FileDialog(this);
//		String fileName = suggestedName.replaceAll(".+[\\\\/](?=.+)", "");
//		File selectedFile = fileDialog.getSaveFile(fileName, Collections.singletonList(filter));
//		if (selectedFile != null) {
//			String expExt = fileDialog.getExtensionOrNull(fileName);
//			String saveExt = fileDialog.getExtensionOrNull(selectedFile);
//			if(expExt != null && (saveExt == null || !saveExt.equals(expExt))){
//				selectedFile = new File(selectedFile.getPath() + "." + expExt);
//			}
//			try {
//				Files.copy(resourceAsStream, selectedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
//			} catch (IOException ioException) {
//				ioException.printStackTrace();
//			}
//		}
//	}
//}
