package com.hiveworkshop.rms.ui.application.tools.shadereditors;

import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.ShaderManager;
import com.hiveworkshop.rms.ui.application.viewer.twiTestRenderMaster.BufferFiller;
import com.hiveworkshop.rms.util.FramePopup;
import com.hiveworkshop.rms.util.ScreenInfo;

import javax.swing.*;

public class BoneShaderEditPanel extends ShaderEditPanel {
	public BoneShaderEditPanel(ShaderManager shaderManager) {
		super(shaderManager, ShaderManager.PipelineType.BONE, "Bone.vert", "Bone.glsl", "Bone.frag");
	}

	public static void show(JComponent parent, BufferFiller bufferFiller) {
		BoneShaderEditPanel shaderEditPanel = new BoneShaderEditPanel(bufferFiller.getShaderManager());
		shaderEditPanel.setPreferredSize(ScreenInfo.getSmallWindow());
		FramePopup.show(shaderEditPanel, parent, "Node Shader Editor");

	}

}
