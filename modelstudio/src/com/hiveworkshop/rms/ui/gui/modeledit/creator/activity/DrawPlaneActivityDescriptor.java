package com.hiveworkshop.rms.ui.gui.modeledit.creator.activity;

import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.*;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.ActiveViewportWatcher;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.builder.model.ModelEditorManipulatorBuilder;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.Manipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;

import java.awt.*;

public class DrawPlaneActivityDescriptor implements ActivityDescriptor {
	private final ProgramPreferences programPreferences;
	private int numberOfWidthSegments;
	private final ActiveViewportWatcher activeViewportWatcher;
	private int numberOfHeightSegments;

	public DrawPlaneActivityDescriptor(final ProgramPreferences programPreferences,
			final ActiveViewportWatcher activeViewportWatcher) {
		this.programPreferences = programPreferences;
		this.activeViewportWatcher = activeViewportWatcher;
		numberOfWidthSegments = 1;
		numberOfHeightSegments = 1;
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
