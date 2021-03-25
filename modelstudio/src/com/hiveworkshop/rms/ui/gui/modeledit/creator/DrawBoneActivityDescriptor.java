package com.hiveworkshop.rms.ui.gui.modeledit.creator;

import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ActivityDescriptor;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ModelEditorViewportActivity;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.ViewportListener;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;

public class DrawBoneActivityDescriptor implements ActivityDescriptor {
	private final ProgramPreferences programPrefences;
	private final ViewportListener viewportListener;

	public DrawBoneActivityDescriptor(final ProgramPreferences programPrefences,
	                                  final ViewportListener viewportListener) {
		this.programPrefences = programPrefences;
		this.viewportListener = viewportListener;
	}

	@Override
	public ModelEditorViewportActivity createActivity(final ModelEditorManager modelEditorManager,
	                                                  final ModelView modelView, final UndoActionListener undoActionListener) {
		return new DrawBoneActivity(programPrefences, undoActionListener, modelEditorManager.getModelEditor(),
				modelView, modelEditorManager.getSelectionView(), viewportListener);
	}

}
