package com.hiveworkshop.wc3.gui.modeledit.creator.activity;

import com.hiveworkshop.wc3.gui.ProgramPreferences;
import com.hiveworkshop.wc3.gui.modeledit.ActiveViewportWatcher;
import com.hiveworkshop.wc3.gui.modeledit.activity.ActivityDescriptor;
import com.hiveworkshop.wc3.gui.modeledit.activity.UndoActionListener;
import com.hiveworkshop.wc3.gui.modeledit.activity.ViewportActivity;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.ModelEditorManager;
import com.hiveworkshop.wc3.mdl.v2.ModelView;

public class DrawBoxActivityDescriptor implements ActivityDescriptor {
	private final ProgramPreferences programPreferences;
	private final ActiveViewportWatcher activeViewportWatcher;

	public DrawBoxActivityDescriptor(final ProgramPreferences programPreferences,
			final ActiveViewportWatcher activeViewportWatcher) {
		this.programPreferences = programPreferences;
		this.activeViewportWatcher = activeViewportWatcher;
	}

	@Override
	public ViewportActivity createActivity(final ModelEditorManager modelEditorManager, final ModelView modelView,
			final UndoActionListener undoActionListener) {
		return new DrawBoxActivity(programPreferences, undoActionListener, modelEditorManager.getModelEditor(),
				modelView, modelEditorManager.getSelectionView(), activeViewportWatcher, 1, 1, 1);
	}

}
