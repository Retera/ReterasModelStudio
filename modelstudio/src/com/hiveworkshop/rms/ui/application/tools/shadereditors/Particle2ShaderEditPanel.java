package com.hiveworkshop.rms.ui.application.tools.shadereditors;

import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.ShaderManager;
import com.hiveworkshop.rms.ui.application.viewer.twiTestRenderMaster.BufferFiller;
import com.hiveworkshop.rms.util.FramePopup;
import com.hiveworkshop.rms.util.ScreenInfo;

import javax.swing.*;

public class Particle2ShaderEditPanel extends ShaderEditPanel {
	public Particle2ShaderEditPanel(ShaderManager shaderManager) {
		super(shaderManager, ShaderManager.PipelineType.PARTICLE2, "Particle.vert", "Particle.glsl", "Particle.frag");
	}

	public static void show(JComponent parent, BufferFiller bufferFiller) {
		Particle2ShaderEditPanel shaderEditPanel = new Particle2ShaderEditPanel(bufferFiller.getShaderManager());
		shaderEditPanel.setPreferredSize(ScreenInfo.getSmallWindow());
		FramePopup.show(shaderEditPanel, parent, "Particle Shader Editor");

	}

}
