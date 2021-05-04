package com.hiveworkshop.rms.ui.application.edit.uv.types;

import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;
import com.hiveworkshop.rms.ui.application.edit.uv.TVertexEditorManager;
import com.hiveworkshop.rms.ui.application.edit.uv.activity.TVertexEditorMultiManipulatorActivity;
import com.hiveworkshop.rms.ui.application.edit.uv.activity.TVertexEditorViewportActivity;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.ModelEditorActionType;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.builder.uv.MoverWidgetTVertexEditorManipulatorBuilder;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.builder.uv.RotatorWidgetTVertexEditorManipulatorBuilder;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.builder.uv.ScaleWidgetTVertexEditorManipulatorBuilder;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.builder.uv.TVertexEditorManipulatorBuilder;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ToolbarButtonType;
import com.hiveworkshop.rms.ui.icons.RMSIcons;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;

import javax.swing.*;

public class TVertexToolbarActionButtonType implements ToolbarButtonType {
	private final ImageIcon imageIcon;
	private final String name;
	private final ProgramPreferences prefs;
	private final ModelEditorActionType editorActionType;

	public TVertexToolbarActionButtonType(String path, String name, ProgramPreferences prefs, ModelEditorActionType editorActionType) {
		this.imageIcon = RMSIcons.loadToolBarImageIcon(path);
		this.name = name;
		this.prefs = prefs;
		this.editorActionType = editorActionType;
	}

	@Override
	public ImageIcon getImageIcon() {
		return imageIcon;
	}

	@Override
	public String getName() {
		return name;
	}

	public TVertexEditorViewportActivity createActivity(TVertexEditorManager modelEditorManager, ModelView modelView, UndoManager undoManager) {
		System.out.println("TVertPrefs: " + prefs);
		System.out.println("TVertPrefs: " + name);
		return new TVertexEditorMultiManipulatorActivity(
				getManipulatorWidget(modelEditorManager, modelView, editorActionType),
				undoManager,
				modelEditorManager.getSelectionView());
	}
	public TVertexEditorViewportActivity createActivity(TVertexEditorManager modelEditorManager, ModelHandler modelHandler) {
		System.out.println("TVertPrefs: " + prefs);
		System.out.println("TVertPrefs: " + name);
		return new TVertexEditorMultiManipulatorActivity(
				getManipulatorWidget(modelEditorManager, modelHandler.getModelView(), editorActionType),
				modelHandler.getUndoManager(),
				modelEditorManager.getSelectionView());
	}

	private TVertexEditorManipulatorBuilder getManipulatorWidget(TVertexEditorManager modelEditorManager, ModelView modelView, ModelEditorActionType editorActionType) {
		return switch (editorActionType) {
			case SCALING -> new ScaleWidgetTVertexEditorManipulatorBuilder(modelEditorManager.getModelEditor(), modelEditorManager.getViewportSelectionHandler(), prefs, modelView);
			case ROTATION -> new RotatorWidgetTVertexEditorManipulatorBuilder(modelEditorManager.getModelEditor(), modelEditorManager.getViewportSelectionHandler(), prefs, modelView);
			case TRANSLATION -> new MoverWidgetTVertexEditorManipulatorBuilder(modelEditorManager.getModelEditor(), modelEditorManager.getViewportSelectionHandler(), prefs, modelView);

		};
	}
}
