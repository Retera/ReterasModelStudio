package com.hiveworkshop.wc3.gui.modeledit.creator.activity;

import java.awt.Cursor;
import java.awt.Graphics2D;

import com.hiveworkshop.wc3.gui.ProgramPreferences;
import com.hiveworkshop.wc3.gui.modeledit.ActiveViewportWatcher;
import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modeledit.activity.ActivityDescriptor;
import com.hiveworkshop.wc3.gui.modeledit.activity.ButtonType;
import com.hiveworkshop.wc3.gui.modeledit.activity.ModelEditorMultiManipulatorActivity;
import com.hiveworkshop.wc3.gui.modeledit.activity.ModelEditorViewportActivity;
import com.hiveworkshop.wc3.gui.modeledit.activity.UndoActionListener;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.ModelEditor;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.ModelEditorManager;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.builder.model.ModelEditorManipulatorBuilder;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.manipulator.Manipulator;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.wc3.mdl.RenderModel;
import com.hiveworkshop.wc3.mdl.v2.ModelView;

public class DrawPlaneActivityDescriptor implements ActivityDescriptor {
	private final ProgramPreferences programPreferences;
	private int numberOfWidthSegments;
	private final ActiveViewportWatcher activeViewportWatcher;
	private int numberOfHeightSegments;

	public DrawPlaneActivityDescriptor(final ProgramPreferences programPreferences,
			final ActiveViewportWatcher activeViewportWatcher) {
		this.programPreferences = programPreferences;
		this.activeViewportWatcher = activeViewportWatcher;
		this.numberOfWidthSegments = 1;
		this.numberOfHeightSegments = 1;
	}

	public void setNumberOfHeightSegments(final int numberOfHeightSegments) {
		this.numberOfHeightSegments = numberOfHeightSegments;
	}

	public void setNumberOfWidthSegments(final int numberOfWidthSegments) {
		this.numberOfWidthSegments = numberOfWidthSegments;
	}

	@Override
	public ModelEditorViewportActivity createActivity(final ModelEditorManager modelEditorManager,
			final ModelView modelView, final UndoActionListener undoActionListener) {
		return new ModelEditorMultiManipulatorActivity(new ModelEditorManipulatorBuilder() {

			private ModelEditor newModelEditor;

			@Override
			public void modelEditorChanged(final ModelEditor newModelEditor) {
				this.newModelEditor = newModelEditor;

			}

			@Override
			public void renderStatic(final Graphics2D graphics, final CoordinateSystem coordinateSystem,
					final SelectionView selectionView) {

			}

			@Override
			public void render(final Graphics2D graphics, final CoordinateSystem coordinateSystem,
					final SelectionView selectionView, final RenderModel renderModel) {

			}

			@Override
			public Cursor getCursorAt(final int x, final int y, final CoordinateSystem coordinateSystem,
					final SelectionView selectionView) {
				return null;
			}

			@Override
			public Manipulator buildActivityListener(final int x, final int y, final ButtonType clickedButton,
					final CoordinateSystem coordinateSystem, final SelectionView selectionView) {
				return new DrawPlaneManipulator(newModelEditor, programPreferences, coordinateSystem,
						numberOfWidthSegments, numberOfHeightSegments,
						activeViewportWatcher.getViewport().getFacingVector());// TODO null vp?
			}
		}, undoActionListener, modelEditorManager.getSelectionView());
	}

}
