package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.selection;

import java.util.List;

import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelViewManager;

public final class HideGeosetsAction implements UndoAction {
	private final List<Geoset> geosets;
	private final ModelViewManager modelViewManager;
	private final Runnable refreshGUIRunnable;

	public HideGeosetsAction(final List<Geoset> geosets, final ModelViewManager modelViewManager,
			final Runnable refreshGUIRunnable) {
		this.geosets = geosets;
		this.modelViewManager = modelViewManager;
		this.refreshGUIRunnable = refreshGUIRunnable;
	}

	@Override
	public void undo() {
		for (final Geoset geoset : geosets) {
			modelViewManager.makeGeosetEditable(geoset);
		}
		refreshGUIRunnable.run();
	}

	@Override
	public void redo() {
		for (final Geoset geoset : geosets) {
			modelViewManager.makeGeosetNotEditable(geoset);
		}
		refreshGUIRunnable.run();
	}

	@Override
	public String actionName() {
		return "hide geosets";
	}

}
