package com.hiveworkshop.rms.ui.gui.modeledit.toolbar;

import com.hiveworkshop.rms.ui.application.MainPanel;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ActivityDescriptor;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.MultiManipulatorActivity;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ViewportActivity;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.manipulator.ModelEditorManipulatorBuilder;
import com.hiveworkshop.rms.ui.icons.RMSIcons;

import javax.swing.*;

public class ToolbarActionButtonType implements ToolbarButtonType, ActivityDescriptor {
	private final ImageIcon imageIcon;
	private final String name;
	MainPanel mainPanel;
	private String action;

	public ToolbarActionButtonType(String action, String path, String name, MainPanel mainPanel) {
		this.action = action;
		this.imageIcon = RMSIcons.loadToolBarImageIcon(path);
		this.name = name;
		this.mainPanel = mainPanel;
	}

	@Override
	public ImageIcon getImageIcon() {
		return imageIcon;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public ViewportActivity createActivity(ModelEditorManager modelEditorManager, ModelHandler modelHandler) {
//		mainPanel.actionType = getActivityType();
		return new MultiManipulatorActivity(getBuilder(modelEditorManager, modelHandler), modelHandler, modelEditorManager);
	}

	private ModelEditorActionType getActivityType() {
		return switch (action) {
			case "move", "extrude", "extend" -> ModelEditorActionType.TRANSLATION;
			case "scale" -> ModelEditorActionType.SCALING;
			case "rotate" -> ModelEditorActionType.ROTATION;

			default -> throw new IllegalStateException("Unexpected value: " + action);
		};
	}

	private ModelEditorManipulatorBuilder getBuilder(ModelEditorManager modelEditorManager, ModelHandler modelHandler) {
		return null;
//		return switch (action) {
//			case "move" -> new MoverWidgetManipulatorBuilder(modelEditorManager, modelHandler);
//			case "scale" -> new ScaleWidgetManipulatorBuilder(modelEditorManager, modelHandler);
//			case "rotate" -> new RotatorWidgetManipulatorBuilder(modelEditorManager, modelHandler);
//			case "extrude" -> new ExtrudeWidgetManipulatorBuilder(modelEditorManager, modelHandler);
//			case "extend" -> new ExtendWidgetManipulatorBuilder(modelEditorManager, modelHandler);
//
//			default -> throw new IllegalStateException("Unexpected value: " + action);
//		};
	}
}
