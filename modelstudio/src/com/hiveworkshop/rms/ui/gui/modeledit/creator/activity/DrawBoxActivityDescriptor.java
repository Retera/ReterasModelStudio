package com.hiveworkshop.rms.ui.gui.modeledit.creator.activity;

import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ActivityDescriptor;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ModelEditorViewportActivity;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.ViewportListener;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;

public class DrawBoxActivityDescriptor implements ActivityDescriptor {
	private final ProgramPreferences programPreferences;
	private final ViewportListener viewportListener;

	public DrawBoxActivityDescriptor(final ProgramPreferences programPreferences,
	                                 final ViewportListener viewportListener) {
		this.programPreferences = programPreferences;
		this.viewportListener = viewportListener;
	}

	@Override
	public ModelEditorViewportActivity createActivity(final ModelEditorManager modelEditorManager,
	                                                  final ModelView modelView, final UndoActionListener undoActionListener) {
		return new DrawBoxActivity(programPreferences, undoActionListener, modelEditorManager.getModelEditor(),
				modelView, modelEditorManager.getSelectionView(), viewportListener, 1, 1, 1);
	}

}
