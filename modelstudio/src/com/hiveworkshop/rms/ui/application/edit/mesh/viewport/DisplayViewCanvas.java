package com.hiveworkshop.rms.ui.application.edit.mesh.viewport;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.viewer.twiTestRenderMaster.ViewportCanvas;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.util.ModelDependentView;
import com.hiveworkshop.rms.util.TinyToggleButton;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class DisplayViewCanvas extends ModelDependentView {
	ViewportPanel viewportPanel;
	ModelPanel modelPanel;
	String name;
	JPanel dudPanel;

	TinyToggleButton renderTextures;
	TinyToggleButton wireFrame;
	TinyToggleButton showNormals;
	TinyToggleButton show3dVerts;


	public DisplayViewCanvas(String s, boolean allowButtonPanel, boolean initOrtho) {
		super(s, null, new JPanel());
		this.name = s;
		dudPanel = new JPanel(new MigLayout());
		viewportPanel = new ViewportPanel(allowButtonPanel, true);
		viewportPanel.getViewport().getCameraHandler().setOrtho(initOrtho);
		this.setComponent(dudPanel);
		setUpViewAngle();

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
//			case "Front" -> displayPanel.setFrontView();
//			case "Side" -> displayPanel.setLeftView();
//			case "Top" -> displayPanel.setTopView();
		}
	}

	@Override
	public DisplayViewCanvas setModelPanel(ModelPanel modelPanel) {
		this.modelPanel = modelPanel;
		if (modelPanel == null) {
			this.setComponent(dudPanel);
			viewportPanel.setModel(null, null);
		} else {
			viewportPanel.setModel(modelPanel.getModelHandler().getRenderModel(), modelPanel.getViewportActivityManager());
			viewportPanel.setControlsVisible(ProgramGlobals.getPrefs().showVMControls());
			setUpViewAngle();
			this.setComponent(viewportPanel);
		}
		System.out.println("name: " + name + ", panel: " + modelPanel);
		return this;
	}

	@Override
	public DisplayViewCanvas preferencesUpdated(){
		if(viewportPanel != null){
			viewportPanel.setControlsVisible(ProgramGlobals.getPrefs().showVMControls());
		}
		return this;
	}

	@Override
	public DisplayViewCanvas reload() {
		if (modelPanel != null) {
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
		if (viewportPanel != null){
			return viewportPanel.getViewport();
		}
		return null;
	}
}
