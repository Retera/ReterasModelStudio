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
		setCameraRot(0, 0);

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
			case "Front" -> setCameraRot(0, 0);
			case "Side" -> setCameraRot(90, 0);
			case "Top" -> setCameraRot(0, -90);
		}
	}
	public void setCameraRot(float right, float up) {
		// Degrees
		viewportPanel.getViewport().getCameraHandler().setCameraRotation(right, up, 0);
	}

	@Override
	public ExportFrameViewCanvas setModelPanel(ModelPanel modelPanel) {
		this.modelPanel = modelPanel;
		if (modelPanel == null) {
			this.setComponent(dudPanel);
			viewportPanel.setModel(null, null, true);
			animationController.setModel(null, null, true);
			gifExportHelper.setRenderModel(null);
		} else {
			ModelHandler modelHandler = modelPanel.getModelHandler();
			RenderModel renderModel = new RenderModel(modelHandler.getModel(), modelHandler.getModelView());
			viewportPanel.setModel(renderModel, null, true);
			viewportPanel.setControlsVisible(ProgramGlobals.getPrefs().showVMControls());
			viewportPanel.getViewport().getCameraHandler().setCameraRotation(0, 0, 0);
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
	public ExportFrameViewCanvas preferencesUpdated() {
		if (viewportPanel != null) {
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
	private TinyToggleButton getButton(String text, boolean initial, Consumer<Boolean> boolConsumer) {
		TinyToggleButton button = new TinyToggleButton(text, onC, offC, boolConsumer);
		button.setOn(initial);
		boolConsumer.accept(initial);
		return button;

	}

	public ViewportCanvas getPerspectiveViewport() {
		return viewportPanel.getViewport();
	}
}
