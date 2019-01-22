package com.hiveworkshop.wc3.gui.modeledit.creator;

import com.hiveworkshop.wc3.gui.ProgramPreferences;
import com.hiveworkshop.wc3.gui.modeledit.ActiveViewportWatcher;
import com.hiveworkshop.wc3.gui.modeledit.activity.ActivityDescriptor;
import com.hiveworkshop.wc3.gui.modeledit.activity.UndoActionListener;
import com.hiveworkshop.wc3.gui.modeledit.activity.ViewportActivity;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.ModelEditorManager;
import com.hiveworkshop.wc3.mdl.v2.ModelView;

public class DrawVertexActivityDescriptor implements ActivityDescriptor {
	private final ProgramPreferences programPrefences;
	private final ActiveViewportWatcher activeViewportWatcher;

	public DrawVertexActivityDescriptor(final ProgramPreferences programPrefences,
			final ActiveViewportWatcher activeViewportWatcher) {
		this.programPrefences = programPrefences;
		this.activeViewportWatcher = activeViewportWatcher;
	}

	@Override
	public ViewportActivity createActivity(final ModelEditorManager modelEditorManager, final ModelView modelView,
			final UndoActionListener undoActionListener) {
		return new DrawVertexActivity(programPrefences, undoActionListener, modelEditorManager.getModelEditor(),
				modelView, modelEditorManager.getSelectionView(), activeViewportWatcher);
	}

}
