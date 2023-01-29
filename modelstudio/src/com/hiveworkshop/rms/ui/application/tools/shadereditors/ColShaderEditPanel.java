package com.hiveworkshop.rms.ui.application.tools.shadereditors;

import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.ShaderManager;
import com.hiveworkshop.rms.ui.application.viewer.twiTestRenderMaster.BufferFiller;
import com.hiveworkshop.rms.util.FramePopup;
import com.hiveworkshop.rms.util.ScreenInfo;

import javax.swing.*;

public class ColShaderEditPanel extends ShaderEditPanel {
	public ColShaderEditPanel(ShaderManager shaderManager) {
		super(shaderManager, "ShapeOutline.vert", "ShapeOutline.glsl", "ShapeOutline.frag");
	}

	protected void createCustomShader(){
		shaderManager.createCustomColShader(shaderTrackers[0].getCurrShader(), shaderTrackers[2].getCurrShader(), shaderTrackers[1].getCurrShader());
	}
	protected void removeCustomShader(){
		shaderManager.removeCustomColShader();
	}

	public static void show(JComponent parent, BufferFiller bufferFiller) {
		ColShaderEditPanel shaderEditPanel = new ColShaderEditPanel(bufferFiller.getShaderManager());
//		shaderEditPanel.setSize(1600, 900);
		shaderEditPanel.setPreferredSize(ScreenInfo.getSmallWindow());
		FramePopup.show(shaderEditPanel, parent, "Collision Shader Editor");

	}

}
