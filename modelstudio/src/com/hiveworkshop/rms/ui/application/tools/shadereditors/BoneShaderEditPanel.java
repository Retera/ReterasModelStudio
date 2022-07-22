package com.hiveworkshop.rms.ui.application.tools.shadereditors;

import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.ShaderManager;
import com.hiveworkshop.rms.ui.application.viewer.twiTestRenderMaster.BufferFiller;
import com.hiveworkshop.rms.util.FramePopup;
import com.hiveworkshop.rms.util.ScreenInfo;

import javax.swing.*;

public class BoneShaderEditPanel extends ShaderEditPanel {
	public BoneShaderEditPanel(ShaderManager shaderManager) {
		super(shaderManager, "Bone.vert", "Bone.glsl", "Bone.frag");
	}

	protected void createCustomShader(){
		shaderManager.createCustomBoneShader(shaderTrackers[0].getCurrShader(), shaderTrackers[2].getCurrShader(), shaderTrackers[1].getCurrShader());
	}
	protected void removeCustomShader(){
		shaderManager.removeCustomBoneShader();
	}

	public static void show(JComponent parent, BufferFiller bufferFiller) {
		BoneShaderEditPanel shaderEditPanel = new BoneShaderEditPanel(bufferFiller.getShaderManager());
//		shaderEditPanel.setSize(1600, 900);
		shaderEditPanel.setPreferredSize(ScreenInfo.getSmallWindow());
		FramePopup.show(shaderEditPanel, parent, "Node Shader Editor");

	}

}
