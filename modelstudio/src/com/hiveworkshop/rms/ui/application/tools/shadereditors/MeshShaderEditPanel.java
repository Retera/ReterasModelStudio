package com.hiveworkshop.rms.ui.application.tools.shadereditors;

import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.ShaderManager;
import com.hiveworkshop.rms.ui.application.viewer.twiTestRenderMaster.BufferFiller;
import com.hiveworkshop.rms.util.FramePopup;
import com.hiveworkshop.rms.util.ScreenInfo;

import javax.swing.*;

public class MeshShaderEditPanel extends ShaderEditPanel {

	boolean isHD;

	public MeshShaderEditPanel(ShaderManager shaderManager, boolean isHD) {
		super(shaderManager, getShaderStrings(isHD));
		this.isHD = isHD;
	}

	protected void createCustomShader(){
		shaderManager.createCustomShader(shaderTrackers[0].getCurrShader(), shaderTrackers[1].getCurrShader(), isHD);
	}
	protected void removeCustomShader(){
		shaderManager.removeCustomShader();
	}

	private static String[] getShaderStrings(boolean isHD){
		if(isHD){
			return new String[]{"HDDiffuseVertColor.vert", "HDDiffuseVertColor.frag"};
		} else {
			return new String[]{"simpleDiffuse.vert", "simpleDiffuse.frag"};
		}
	}

	public static void show(JComponent parent, BufferFiller bufferFiller) {
		MeshShaderEditPanel shaderEditPanel = new MeshShaderEditPanel(bufferFiller.getShaderManager(), bufferFiller.isHD());
//		shaderEditPanel.setSize(1600, 900);
		shaderEditPanel.setPreferredSize(ScreenInfo.getSmallWindow());
		FramePopup.show(shaderEditPanel, parent, "Shader Editor");

	}

}
