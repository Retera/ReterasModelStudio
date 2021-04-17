package com.hiveworkshop.rms.ui.gui.modeledit.creator.activity;

import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ActivityDescriptor;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ModelEditorViewportActivity;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.ViewportListener;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;

public class DrawPlaneActivityDescriptor implements ActivityDescriptor {
	private final ProgramPreferences programPreferences;
	private int numberOfWidthSegments;
	private final ViewportListener viewportListener;
	private int numberOfHeightSegments;

	public DrawPlaneActivityDescriptor(final ProgramPreferences programPreferences,
	                                   final ViewportListener viewportListener) {
		this.programPreferences = programPreferences;
		this.viewportListener = viewportListener;
		numberOfWidthSegments = 1;
		numberOfHeightSegments = 1;
	}

	@Override
	public ModelEditorViewportActivity createActivity(final ModelEditorManager modelEditorManager,
	                                                  final ModelView modelView, final UndoActionListener undoActionListener) {
		return new DrawPlaneActivity(programPreferences, undoActionListener, modelEditorManager.getModelEditor(),
				modelView, modelEditorManager.getSelectionView(), viewportListener, 1, 1, 1);
	}

}
