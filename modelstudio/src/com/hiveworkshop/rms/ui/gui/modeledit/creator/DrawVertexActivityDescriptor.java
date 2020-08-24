package com.hiveworkshop.rms.ui.gui.modeledit.creator;

import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ActivityDescriptor;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ModelEditorViewportActivity;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.ActiveViewportWatcher;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;

public class DrawVertexActivityDescriptor implements ActivityDescriptor {
	private final ProgramPreferences programPrefences;
	private final ActiveViewportWatcher activeViewportWatcher;

	public DrawVertexActivityDescriptor(final ProgramPreferences programPrefences,
			final ActiveViewportWatcher activeViewportWatcher) {
		this.programPrefences = programPrefences;
		this.activeViewportWatcher = activeViewportWatcher;
	}

	@Override
	public ModelEditorViewportActivity createActivity(final ModelEditorManager modelEditorManager,
                                                      final ModelView modelView, final UndoActionListener undoActionListener) {
		return new DrawVertexActivity(programPrefences, undoActionListener, modelEditorManager.getModelEditor(),
				modelView, modelEditorManager.getSelectionView(), activeViewportWatcher);
	}

}
