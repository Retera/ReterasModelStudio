package com.hiveworkshop.rms.ui.application.viewer.twiTestRenderMaster;

import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ViewportActivityManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.ViewportPanel;
import org.lwjgl.LWJGLException;

import java.awt.*;

public class ExportFrameViewportPanel extends ViewportPanel {

	public ExportFrameViewportPanel(boolean allowButtonPanel, boolean showNodes) {
		super(allowButtonPanel, showNodes);
	}


	protected ViewportCanvas getViewport(boolean potrait) throws LWJGLException {
		final ViewportCanvas viewport = new ExportFrameViewportCanvas(ProgramGlobals.getPrefs(), potrait);
		viewport.setMinimumSize(new Dimension(200, 200));
		return viewport;
	}

	public ExportFrameViewportPanel setModel(RenderModel renderModel, ViewportActivityManager activityListener, boolean loadDefaultCamera) {
		this.activityListener = activityListener;
		if (renderModel != null) {
			viewport.setModel(renderModel, loadDefaultCamera);
			viewport.getMouseAdapter().setActivityManager(activityListener);
		} else {
			viewport.setModel(null, loadDefaultCamera);
		}
		return this;
	}
}
