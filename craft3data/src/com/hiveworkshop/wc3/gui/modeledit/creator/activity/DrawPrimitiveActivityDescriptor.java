package com.hiveworkshop.wc3.gui.modeledit.creator.activity;

import com.hiveworkshop.wc3.gui.ProgramPreferences;
import com.hiveworkshop.wc3.gui.modeledit.ActiveViewportWatcher;
import com.hiveworkshop.wc3.gui.modeledit.activity.ActivityDescriptor;
import com.hiveworkshop.wc3.gui.modeledit.activity.ModelEditorViewportActivity;
import com.hiveworkshop.wc3.gui.modeledit.activity.UndoActionListener;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.ModelEditorManager;
import com.hiveworkshop.wc3.mdl.v2.ModelView;
import com.hiveworkshop.wc3.util.PrimitiveMeshes.PrimitiveOptions;
import com.hiveworkshop.wc3.util.PrimitiveMeshes.PrimitiveShape;

public final class DrawPrimitiveActivityDescriptor implements ActivityDescriptor {
	private final ProgramPreferences programPreferences;
	private final ActiveViewportWatcher activeViewportWatcher;
	private final PrimitiveShape shape;
	private final PrimitiveOptions options;

	public DrawPrimitiveActivityDescriptor(final ProgramPreferences programPreferences,
			final ActiveViewportWatcher activeViewportWatcher, final PrimitiveShape shape,
			final PrimitiveOptions options) {
		this.programPreferences = programPreferences;
		this.activeViewportWatcher = activeViewportWatcher;
		this.shape = shape;
		this.options = options;
	}

	@Override
	public ModelEditorViewportActivity createActivity(final ModelEditorManager modelEditorManager,
			final ModelView modelView, final UndoActionListener undoActionListener) {
		return new DrawPrimitiveActivity(programPreferences, undoActionListener, modelEditorManager.getModelEditor(),
				modelView, modelEditorManager.getSelectionView(), activeViewportWatcher, shape, options);
	}
}
