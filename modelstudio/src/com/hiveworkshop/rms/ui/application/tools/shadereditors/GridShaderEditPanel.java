package com.hiveworkshop.rms.ui.application.tools.shadereditors;

import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.ShaderManager;
import com.hiveworkshop.rms.ui.application.viewer.twiTestRenderMaster.BufferFiller;
import com.hiveworkshop.rms.util.FramePopup;
import com.hiveworkshop.rms.util.ScreenInfo;

import javax.swing.*;

public class GridShaderEditPanel extends ShaderEditPanel {

	public GridShaderEditPanel(ShaderManager shaderManager) {
		super(shaderManager, ShaderManager.PipelineType.GRID, "Grid.vert", "Grid.frag");
	}

	public static void show(JComponent parent, BufferFiller bufferFiller) {
		GridShaderEditPanel shaderEditPanel = new GridShaderEditPanel(bufferFiller.getShaderManager());
		shaderEditPanel.setPreferredSize(ScreenInfo.getSmallWindow());
		FramePopup.show(shaderEditPanel, parent, "Grid Shader Editor");

	}

}
