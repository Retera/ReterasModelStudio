package com.hiveworkshop.rms.ui.application.viewer.perspective;

import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.viewer.ComPerspRenderEnv;
import com.hiveworkshop.rms.ui.application.viewer.ComPerspViewport;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import org.lwjgl.LWJGLException;

public class PerspectiveViewport extends ComPerspViewport {

	public PerspectiveViewport(final ModelView modelView, RenderModel renderModel, final ProgramPreferences programPreferences, ComPerspRenderEnv renderEnvironment) throws LWJGLException {
		super(modelView, renderModel, programPreferences, renderEnvironment, false);
	}
}