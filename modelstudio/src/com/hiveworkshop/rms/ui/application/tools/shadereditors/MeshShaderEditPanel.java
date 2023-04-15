package com.hiveworkshop.rms.ui.application.tools.shadereditors;

import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.ShaderManager;
import com.hiveworkshop.rms.ui.application.viewer.twiTestRenderMaster.BufferFiller;
import com.hiveworkshop.rms.util.FramePopup;
import com.hiveworkshop.rms.util.ScreenInfo;

import javax.swing.*;

public class MeshShaderEditPanel extends ShaderEditPanel {

	public MeshShaderEditPanel(ShaderManager shaderManager) {
		super(shaderManager, ShaderManager.PipelineType.MESH, "HDDiffuseVertColor.vert", "HDDiffuseVertColor.frag");
	}

	public static void show(JComponent parent, BufferFiller bufferFiller) {
		MeshShaderEditPanel shaderEditPanel = new MeshShaderEditPanel(bufferFiller.getShaderManager());
		shaderEditPanel.setPreferredSize(ScreenInfo.getSmallWindow());
		FramePopup.show(shaderEditPanel, parent, "Shader Editor");

	}
}
