package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ModelEditorMultiManipulatorActivity;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ModelEditorViewportActivity;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.ModelEditorActionType;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.builder.model.*;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionMode;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ToolbarActionButtonType;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ToolbarButtonGroup;
import com.hiveworkshop.rms.ui.icons.RMSIcons;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ToolBar {
    public static JToolBar createJToolBar(final MainPanel mainPanel) {
        JToolBar toolbar = new JToolBar(JToolBar.HORIZONTAL);
        toolbar.setFloatable(false);
        FileDialog fileDialog = new FileDialog(mainPanel);

        addToolbarIcon(toolbar, "New", "new.png", () -> MenuBarActions.newModel(mainPanel));

//        addToolbarIcon(toolbar, "Open", "open.png", () -> MenuBarActions.onClickOpen(mainPanel));
        addToolbarIcon(toolbar, "Open", "open.png", fileDialog::onClickOpen);

//        addToolbarIcon(toolbar, "Save", "save.png", () -> MenuBarActions.onClickSave(mainPanel));
        addToolbarIcon(toolbar, "Save", "save.png", fileDialog::onClickSave);

        toolbar.addSeparator();


        addToolbarIcon(toolbar, "Undo", "undo.png", mainPanel.undoAction);

        addToolbarIcon(toolbar, "Redo", "redo.png", mainPanel.redoAction);

        toolbar.addSeparator();
        mainPanel.selectionModeGroup = new ToolbarButtonGroup<>(toolbar, SelectionMode.values());

        toolbar.addSeparator();

        mainPanel.selectionItemTypeGroup = new ToolbarButtonGroup<>(toolbar, SelectionItemTypes.values());

        toolbar.addSeparator();

        mainPanel.actionTypeGroup = new ToolbarButtonGroup<>(toolbar,
                new ToolbarActionButtonType[] {
                        getMoverWid(mainPanel),
                        getRotatorWid(mainPanel),
                        getScaleWid(mainPanel),
                        getExtrudeWid(mainPanel),
                        getExtendWid(mainPanel),});
        mainPanel.currentActivity = mainPanel.actionTypeGroup.getActiveButtonType();

        toolbar.addSeparator();

        mainPanel.snapButton = addToolbarIcon(toolbar, "Snap", "snap.png", () -> ModelEditActions.snapVertices(mainPanel));

        toolbar.setMaximumSize(new Dimension(80000, 48));
        return toolbar;
    }

    private static ToolbarActionButtonType getExtendWid(MainPanel mainPanel) {
        return new ToolbarActionButtonType(
                RMSIcons.loadToolBarImageIcon("extend.png"), "Select and Extend") {
            @Override
            public ModelEditorViewportActivity createActivity(final ModelEditorManager modelEditorManager,
                                                              final ModelView modelView,
                                                              final UndoActionListener undoActionListener) {
                mainPanel.actionType = ModelEditorActionType.TRANSLATION;
                ExtendWidgetManipulatorBuilder ewmb = new ExtendWidgetManipulatorBuilder(
                        modelEditorManager.getModelEditor(), modelEditorManager.getViewportSelectionHandler(), mainPanel.prefs, modelView);
                return new ModelEditorMultiManipulatorActivity(ewmb, undoActionListener, modelEditorManager.getSelectionView());
            }
        };
    }

    private static ToolbarActionButtonType getExtrudeWid(MainPanel mainPanel) {
        return new ToolbarActionButtonType(
                RMSIcons.loadToolBarImageIcon("extrude.png"), "Select and Extrude") {
            @Override
            public ModelEditorViewportActivity createActivity(final ModelEditorManager modelEditorManager,
                                                              final ModelView modelView,
                                                              final UndoActionListener undoActionListener) {
                mainPanel.actionType = ModelEditorActionType.TRANSLATION;
                ExtrudeWidgetManipulatorBuilder ewmb = new ExtrudeWidgetManipulatorBuilder(
                        modelEditorManager.getModelEditor(), modelEditorManager.getViewportSelectionHandler(), mainPanel.prefs, modelView);
                return new ModelEditorMultiManipulatorActivity(ewmb, undoActionListener, modelEditorManager.getSelectionView());
            }
        };
    }

    private static ToolbarActionButtonType getScaleWid(MainPanel mainPanel) {
        return new ToolbarActionButtonType(
                RMSIcons.loadToolBarImageIcon("scale.png"), "Select and Scale") {
            @Override
            public ModelEditorViewportActivity createActivity(final ModelEditorManager modelEditorManager,
                                                              final ModelView modelView,
                                                              final UndoActionListener undoActionListener) {
                mainPanel.actionType = ModelEditorActionType.SCALING;
                ScaleWidgetManipulatorBuilder swmb = new ScaleWidgetManipulatorBuilder(
                        modelEditorManager.getModelEditor(), modelEditorManager.getViewportSelectionHandler(), mainPanel.prefs, modelView);
                return new ModelEditorMultiManipulatorActivity(swmb, undoActionListener, modelEditorManager.getSelectionView());
            }
        };
    }

    private static ToolbarActionButtonType getRotatorWid(MainPanel mainPanel) {
        return new ToolbarActionButtonType(
                RMSIcons.loadToolBarImageIcon("rotate.png"), "Select and Rotate") {
            @Override
            public ModelEditorViewportActivity createActivity(final ModelEditorManager modelEditorManager,
                                                              final ModelView modelView,
                                                              final UndoActionListener undoActionListener) {
                mainPanel.actionType = ModelEditorActionType.ROTATION;
                RotatorWidgetManipulatorBuilder rwmb = new RotatorWidgetManipulatorBuilder(
                        modelEditorManager.getModelEditor(), modelEditorManager.getViewportSelectionHandler(), mainPanel.prefs, modelView);
                return new ModelEditorMultiManipulatorActivity(rwmb, undoActionListener, modelEditorManager.getSelectionView());
            }
        };
    }

    private static ToolbarActionButtonType getMoverWid(MainPanel mainPanel) {
        return new ToolbarActionButtonType(
                RMSIcons.loadToolBarImageIcon("move2.png"), "Select and Move") {
            @Override
            public ModelEditorViewportActivity createActivity(final ModelEditorManager modelEditorManager,
                                                              final ModelView modelView,
                                                              final UndoActionListener undoActionListener) {
                mainPanel.actionType = ModelEditorActionType.TRANSLATION;
                MoverWidgetManipulatorBuilder mwmb = new MoverWidgetManipulatorBuilder(
                        modelEditorManager.getModelEditor(), modelEditorManager.getViewportSelectionHandler(), mainPanel.prefs, modelView);
                return new ModelEditorMultiManipulatorActivity(mwmb, undoActionListener, modelEditorManager.getSelectionView());
            }
        };
    }

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
