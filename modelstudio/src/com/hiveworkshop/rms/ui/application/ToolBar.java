package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.MultiManipulatorActivity;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ViewportActivity;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.manipulator.ModelEditorManipulatorBuilder;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ModelEditorActionType3;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.SelectionMode;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ToolbarButtonGroup2;
import com.hiveworkshop.rms.ui.icons.RMSIcons;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ToolBar {
    public static JToolBar createJToolBar(MainPanel mainPanel) {
        JToolBar toolbar = new JToolBar(JToolBar.HORIZONTAL);
        toolbar.setFloatable(false);
        FileDialog fileDialog = new FileDialog(mainPanel);

        addToolbarIcon(toolbar, "New", "new.png", () -> MenuBarActions.newModel(mainPanel));

//        addToolbarIcon(toolbar, "Open", "open.png", () -> MenuBarActions.onClickOpen(mainPanel));
        addToolbarIcon(toolbar, "Open", "open.png", fileDialog::onClickOpen);

//        addToolbarIcon(toolbar, "Save", "save.png", () -> MenuBarActions.onClickSave(mainPanel));
        addToolbarIcon(toolbar, "Save", "save.png", fileDialog::onClickSave);

        toolbar.addSeparator();


        addToolbarIcon(toolbar, "Undo", "undo.png", mainPanel.getUndoHandler().getUndoAction());
        addToolbarIcon(toolbar, "Redo", "redo.png", mainPanel.getUndoHandler().getRedoAction());

        toolbar.addSeparator();
        mainPanel.selectionModeGroup = new ToolbarButtonGroup2<>(toolbar, SelectionMode.values());
        mainPanel.selectionModeGroup.setActiveButton(SelectionMode.SELECT);

        toolbar.addSeparator();

        mainPanel.selectionItemTypeGroup = new ToolbarButtonGroup2<>(toolbar, SelectionItemTypes.values());
        mainPanel.selectionItemTypeGroup.setActiveButton(SelectionItemTypes.VERTEX);

        toolbar.addSeparator();

        mainPanel.actionTypeGroup = new ToolbarButtonGroup2<>(toolbar, ModelEditorActionType3.values());
        mainPanel.actionTypeGroup.setActiveButton(ModelEditorActionType3.TRANSLATION);
//        mainPanel.actionTypeGroup = new ToolbarButtonGroup<>(toolbar,
//                new ToolbarActionButtonType[] {
//                        new ToolbarActionButtonType("move", "move2.png", "Select and Move", mainPanel),
//                        new ToolbarActionButtonType("rotate", "rotate.png", "Select and Rotate", mainPanel),
//                        new ToolbarActionButtonType("scale", "scale.png", "Select and Scale", mainPanel),
//                        new ToolbarActionButtonType("extrude", "extrude.png", "Select and Extrude", mainPanel),
//                        new ToolbarActionButtonType("extend", "extend.png", "Select and Extend", mainPanel),
//                });

//        mainPanel.currentActivity = mainPanel.actionTypeGroup.getActiveButtonType();

	    toolbar.addSeparator();

	    mainPanel.snapButton = addToolbarIcon(toolbar, "Snap", "snap.png", () -> ModelEditActions.snapVertices(mainPanel));

	    toolbar.setMaximumSize(new Dimension(80000, 48));
	    return toolbar;
    }

	public static ViewportActivity createActivity(ModelEditorManager modelEditorManager, ModelHandler modelHandler, MainPanel mainPanel, ModelEditorActionType3 action) {
//        mainPanel.actionType = getActivityType(action);
        return new MultiManipulatorActivity(new ModelEditorManipulatorBuilder(modelEditorManager, modelHandler, action), modelHandler, modelEditorManager);
    }

//    private static ModelEditorActionType2 getActivityType(ModelEditorActionType3 action) {
//        return switch (action) {
//            case TRANSLATION, EXTRUDE, EXTEND -> ModelEditorActionType2.TRANSLATION;
//            case SCALING -> ModelEditorActionType2.SCALING;
//            case ROTATION -> ModelEditorActionType2.ROTATION;
//        };
//    }

//    private static ModelEditorManipulatorBuilder getBuilder(ModelEditorManager modelEditorManager, ModelHandler modelHandler, ModelEditorActionType3 action) {
//        return switch (action) {
//            case TRANSLATION -> new MoverWidgetManipulatorBuilder(modelEditorManager, modelHandler);
//            case ROTATION -> new RotatorWidgetManipulatorBuilder(modelEditorManager, modelHandler);
//            case SCALING -> new ScaleWidgetManipulatorBuilder(modelEditorManager, modelHandler);
//            case EXTRUDE -> new ExtrudeWidgetManipulatorBuilder(modelEditorManager, modelHandler);
//            case EXTEND -> new ExtendWidgetManipulatorBuilder(modelEditorManager, modelHandler);
//        };
//    }

    static JButton addToolbarIcon(JToolBar toolbar, String hooverText, String icon, AbstractAction action) {
        JButton button = new JButton(RMSIcons.loadToolBarImageIcon(icon));
        button.setToolTipText(hooverText);
        button.addActionListener(action);
        toolbar.add(button);
        return button;
    }

    static JButton addToolbarIcon(JToolBar toolbar, String hooverText, String icon, Runnable function) {
        AbstractAction action = new AbstractAction(hooverText) {
            @Override
            public void actionPerformed(final ActionEvent e) {
                try {
                    function.run();
                } catch (final Exception exc) {
                    exc.printStackTrace();
                    ExceptionPopup.display(exc);
                }
            }
        };

        JButton button = new JButton(RMSIcons.loadToolBarImageIcon(icon));
        button.setToolTipText(hooverText);
        button.addActionListener(action);
        toolbar.add(button);
        return button;
    }

}
