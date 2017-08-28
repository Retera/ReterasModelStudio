package com.hiveworkshop.wc3.gui.modeledit.manipulator.activity;

import com.hiveworkshop.wc3.gui.modeledit.manipulator.ModelEditor;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.wc3.gui.modeledit.useractions.UndoManager;
import com.hiveworkshop.wc3.mdl.Vertex;

public final class BetterScaleYActivityListener extends AbstractBetterScaleActivityListener {
	private final Vertex resettableScaleFactors;

	public BetterScaleYActivityListener(final ModelEditor modelEditor, final UndoManager undoManager,
			final SelectionView selectionView) {
		super(modelEditor, undoManager, selectionView);
		this.resettableScaleFactors = new Vertex(0, 0, 0);
	}

	@Override
	protected final void scaleWithFactor(final ModelEditor modelEditor, final Vertex center, final double scaleFactor,
			final byte dim1, final byte dim2) {
		resettableScaleFactors.x = 1;
		resettableScaleFactors.y = 1;
		resettableScaleFactors.z = 1;
		resettableScaleFactors.setCoord(dim2, scaleFactor);
		modelEditor.scale(center.x, center.y, center.z, scaleFactor, scaleFactor, scaleFactor);
	}

}
