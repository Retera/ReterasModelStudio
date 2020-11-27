package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.parsers.mdlx.util.MdxUtils;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ModelEditorMultiManipulatorActivity;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ModelEditorViewportActivity;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.ModelEditorActionType;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.builder.model.*;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionMode;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ToolbarActionButtonType;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ToolbarButtonGroup;
import com.hiveworkshop.rms.ui.icons.RMSIcons;
import com.hiveworkshop.rms.ui.preferences.SaveProfile;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.util.Vec3;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.NoSuchElementException;

public class ToolBar {
    public static JToolBar createJToolBar(final MainPanel mainPanel) {
        mainPanel.toolbar = new JToolBar(JToolBar.HORIZONTAL);
        mainPanel.toolbar.setFloatable(false);
        mainPanel.toolbar.add(new AbstractAction("New", RMSIcons.loadToolBarImageIcon("new.png")) {
            @Override
            public void actionPerformed(final ActionEvent e) {
                try {
                    newModel(mainPanel);
                } catch (final Exception exc) {
                    exc.printStackTrace();
                    ExceptionPopup.display(exc);
                }
            }
        });
        mainPanel.toolbar.add(new AbstractAction("Open", RMSIcons.loadToolBarImageIcon("open.png")) {
            @Override
            public void actionPerformed(final ActionEvent e) {
                try {
                    onClickOpen(mainPanel);
                } catch (final Exception exc) {
                    exc.printStackTrace();
                    ExceptionPopup.display(exc);
                }
            }
        });
        mainPanel.toolbar.add(new AbstractAction("Save", RMSIcons.loadToolBarImageIcon("save.png")) {
            @Override
            public void actionPerformed(final ActionEvent e) {
                try {
                    onClickSave(mainPanel);
                } catch (final Exception exc) {
                    exc.printStackTrace();
                    ExceptionPopup.display(exc);
                }
            }
        });
        mainPanel.toolbar.addSeparator();
        mainPanel.toolbar.add(new AbstractAction("Undo", RMSIcons.loadToolBarImageIcon("undo.png")) {
            @Override
            public void actionPerformed(final ActionEvent e) {
                try {
                    mainPanel.currentModelPanel().getUndoManager().undo();
                } catch (final NoSuchElementException exc) {
                    JOptionPane.showMessageDialog(mainPanel, "Nothing to undo!");
                } catch (final Exception exc) {
                    ExceptionPopup.display(exc);
                    // exc.printStackTrace();
                }
                mainPanel.repaint();
            }
        });
        mainPanel.toolbar.add(new AbstractAction("Redo", RMSIcons.loadToolBarImageIcon("redo.png")) {
            @Override
            public void actionPerformed(final ActionEvent e) {
                try {
                    mainPanel.currentModelPanel().getUndoManager().redo();
                } catch (final NoSuchElementException exc) {
                    JOptionPane.showMessageDialog(mainPanel, "Nothing to redo!");
                } catch (final Exception exc) {
                    ExceptionPopup.display(exc);
                    // exc.printStackTrace();
                }
                mainPanel.repaint();
            }
        });
        mainPanel.toolbar.addSeparator();
        mainPanel.selectionModeGroup = new ToolbarButtonGroup<>(mainPanel.toolbar, SelectionMode.values());
        mainPanel.toolbar.addSeparator();
        mainPanel.selectionItemTypeGroup = new ToolbarButtonGroup<>(mainPanel.toolbar, SelectionItemTypes.values());
        mainPanel.toolbar.addSeparator();
        mainPanel.selectAndMoveDescriptor = new ToolbarActionButtonType(
                RMSIcons.loadToolBarImageIcon("move2.png"), "Select and Move") {
            @Override
            public ModelEditorViewportActivity createActivity(final ModelEditorManager modelEditorManager,
                                                              final ModelView modelView, final UndoActionListener undoActionListener) {
                mainPanel.actionType = ModelEditorActionType.TRANSLATION;
                return new ModelEditorMultiManipulatorActivity(
                        new MoverWidgetManipulatorBuilder(modelEditorManager.getModelEditor(),
                                modelEditorManager.getViewportSelectionHandler(), mainPanel.prefs, modelView),
                        undoActionListener, modelEditorManager.getSelectionView());
            }
        };
        mainPanel.selectAndRotateDescriptor = new ToolbarActionButtonType(
                RMSIcons.loadToolBarImageIcon("rotate.png"), "Select and Rotate") {
            @Override
            public ModelEditorViewportActivity createActivity(final ModelEditorManager modelEditorManager,
                                                              final ModelView modelView, final UndoActionListener undoActionListener) {
                mainPanel.actionType = ModelEditorActionType.ROTATION;
                return new ModelEditorMultiManipulatorActivity(
                        new RotatorWidgetManipulatorBuilder(modelEditorManager.getModelEditor(),
                                modelEditorManager.getViewportSelectionHandler(), mainPanel.prefs, modelView),
                        undoActionListener, modelEditorManager.getSelectionView());
            }
        };
        mainPanel.selectAndScaleDescriptor = new ToolbarActionButtonType(
                RMSIcons.loadToolBarImageIcon("scale.png"), "Select and Scale") {
            @Override
            public ModelEditorViewportActivity createActivity(final ModelEditorManager modelEditorManager,
                                                              final ModelView modelView, final UndoActionListener undoActionListener) {
                mainPanel.actionType = ModelEditorActionType.SCALING;
                return new ModelEditorMultiManipulatorActivity(
                        new ScaleWidgetManipulatorBuilder(modelEditorManager.getModelEditor(),
                                modelEditorManager.getViewportSelectionHandler(), mainPanel.prefs, modelView),
                        undoActionListener, modelEditorManager.getSelectionView());
            }
        };
        mainPanel.selectAndExtrudeDescriptor = new ToolbarActionButtonType(
                RMSIcons.loadToolBarImageIcon("extrude.png"), "Select and Extrude") {
            @Override
            public ModelEditorViewportActivity createActivity(final ModelEditorManager modelEditorManager,
                                                              final ModelView modelView, final UndoActionListener undoActionListener) {
                mainPanel.actionType = ModelEditorActionType.TRANSLATION;
                return new ModelEditorMultiManipulatorActivity(
                        new ExtrudeWidgetManipulatorBuilder(modelEditorManager.getModelEditor(),
                                modelEditorManager.getViewportSelectionHandler(), mainPanel.prefs, modelView),
                        undoActionListener, modelEditorManager.getSelectionView());
            }
        };
        mainPanel.selectAndExtendDescriptor = new ToolbarActionButtonType(
                RMSIcons.loadToolBarImageIcon("extend.png"), "Select and Extend") {
            @Override
            public ModelEditorViewportActivity createActivity(final ModelEditorManager modelEditorManager,
                                                              final ModelView modelView, final UndoActionListener undoActionListener) {
                mainPanel.actionType = ModelEditorActionType.TRANSLATION;
                return new ModelEditorMultiManipulatorActivity(
                        new ExtendWidgetManipulatorBuilder(modelEditorManager.getModelEditor(),
                                modelEditorManager.getViewportSelectionHandler(), mainPanel.prefs, modelView),
                        undoActionListener, modelEditorManager.getSelectionView());
            }
        };
        mainPanel.actionTypeGroup = new ToolbarButtonGroup<>(mainPanel.toolbar,
                new ToolbarActionButtonType[]{mainPanel.selectAndMoveDescriptor, mainPanel.selectAndRotateDescriptor,
                        mainPanel.selectAndScaleDescriptor, mainPanel.selectAndExtrudeDescriptor,
                        mainPanel.selectAndExtendDescriptor,});
        mainPanel.currentActivity = mainPanel.actionTypeGroup.getActiveButtonType();
        mainPanel.toolbar.addSeparator();
        mainPanel.snapButton = mainPanel.toolbar
                .add(new AbstractAction("Snap", RMSIcons.loadToolBarImageIcon("snap.png")) {
                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        try {
                            final ModelPanel currentModelPanel = mainPanel.currentModelPanel();
                            if (currentModelPanel != null) {
                                currentModelPanel.getUndoManager().pushAction(currentModelPanel.getModelEditorManager()
                                        .getModelEditor().snapSelectedVertices());
                            }
                        } catch (final NoSuchElementException exc) {
                            JOptionPane.showMessageDialog(mainPanel, "Nothing to undo!");
                        } catch (final Exception exc) {
                            ExceptionPopup.display(exc);
                        }
                    }
                });

        return mainPanel.toolbar;
    }

    static void onClickSave(MainPanel mainPanel) {
        try {
            if (mainPanel.currentMDL() != null) {
                MdxUtils.saveMdx(mainPanel.currentMDL(), mainPanel.currentMDL().getFile());
                mainPanel.profile.setPath(mainPanel.currentMDL().getFile().getParent());
                // currentMDLDisp().resetBeenSaved();
                // TODO reset been saved
            }
        } catch (final Exception exc) {
            ExceptionPopup.display(exc);
        }
        refreshController(mainPanel.geoControl, mainPanel.geoControlModelData);
    }

    static void onClickOpen(MainPanel mainPanel) {
        mainPanel.fc.setDialogTitle("Open");
        final EditableModel current = mainPanel.currentMDL();
        if ((current != null) && !current.isTemp() && (current.getFile() != null)) {
            mainPanel.fc.setCurrentDirectory(current.getFile().getParentFile());
        } else if (mainPanel.profile.getPath() != null) {
            mainPanel.fc.setCurrentDirectory(new File(mainPanel.profile.getPath()));
        }

        final int returnValue = mainPanel.fc.showOpenDialog(mainPanel);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            openFile(mainPanel, mainPanel.fc.getSelectedFile());
        }

        mainPanel.fc.setSelectedFile(null);

        // //Special thanks to the JWSFileChooserDemo from oracle's Java
        // tutorials, from which many ideas were borrowed for the following
        // FileOpenService fos = null;
        // FileContents fileContents = null;
        //
        // try
        // {
        // fos =
        // (FileOpenService)ServiceManager.lookup("javax.jnlp.FileOpenService");
        // }
        // catch (UnavailableServiceException exc )
        // {
        //
        // }
        //
        // if( fos != null )
        // {
        // try
        // {
        // fileContents = fos.openFileDialog(null, null);
        // }
        // catch (Exception exc )
        // {
        // JOptionPane.showMessageDialog(this,"Opening command failed:
        // "+exc.getLocalizedMessage());
        // }
        // }
        //
        // if( fileContents != null)
        // {
        // try
        // {
        // fileContents.getName();
        // }
        // catch (IOException exc)
        // {
        // JOptionPane.showMessageDialog(this,"Problem opening file:
        // "+exc.getLocalizedMessage());
        // }
        // }
    }

    static void newModel(MainPanel mainPanel) {
        final JPanel newModelPanel = new JPanel();
        newModelPanel.setLayout(new MigLayout());
        newModelPanel.add(new JLabel("Model Name: "), "cell 0 0");
        final JTextField newModelNameField = new JTextField("MrNew", 25);
        newModelPanel.add(newModelNameField, "cell 1 0");
        final JRadioButton createEmptyButton = new JRadioButton("Create Empty", true);
        newModelPanel.add(createEmptyButton, "cell 0 1");
        final JRadioButton createPlaneButton = new JRadioButton("Create Plane");
        newModelPanel.add(createPlaneButton, "cell 0 2");
        final JRadioButton createBoxButton = new JRadioButton("Create Box");
        newModelPanel.add(createBoxButton, "cell 0 3");
        final ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(createBoxButton);
        buttonGroup.add(createPlaneButton);
        buttonGroup.add(createEmptyButton);

        final int userDialogResult = JOptionPane.showConfirmDialog(mainPanel, newModelPanel, "New Model",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (userDialogResult == JOptionPane.OK_OPTION) {
            final EditableModel mdl = new EditableModel(newModelNameField.getText());
            if (createBoxButton.isSelected()) {
                final SpinnerNumberModel sModel = new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1);
                final JSpinner spinner = new JSpinner(sModel);
                final int userChoice = JOptionPane.showConfirmDialog(mainPanel, spinner, "Box: Choose Segments",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (userChoice != JOptionPane.OK_OPTION) {
                    return;
                }
                ModelUtils.createBox(mdl, new Vec3(64, 64, 128), new Vec3(-64, -64, 0),
                        ((Number) spinner.getValue()).intValue());
            } else if (createPlaneButton.isSelected()) {
                final SpinnerNumberModel sModel = new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1);
                final JSpinner spinner = new JSpinner(sModel);
                final int userChoice = JOptionPane.showConfirmDialog(mainPanel, spinner, "Plane: Choose Segments",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (userChoice != JOptionPane.OK_OPTION) {
                    return;
                }
                ModelUtils.createGroundPlane(mdl, new Vec3(64, 64, 0), new Vec3(-64, -64, 0),
                        ((Number) spinner.getValue()).intValue());
            }
            final ModelPanel temp = new ModelPanel(mainPanel, mdl, mainPanel.prefs, mainPanel, mainPanel.selectionItemTypeGroup,
                    mainPanel.selectionModeGroup, mainPanel.modelStructureChangeListener, mainPanel.coordDisplayListener,
                    mainPanel.viewportTransferHandler, mainPanel.activeViewportWatcher, RMSIcons.MDLIcon, false,
                    mainPanel.textureExporter);
            MenuBar.loadModel(mainPanel, true, true, temp);
        }

    }

    public static void openFile(MainPanel mainPanel, final File f) {
        mainPanel.currentFile = f;
        mainPanel.profile.setPath(mainPanel.currentFile.getParent());
        // frontArea.clearGeosets();
        // sideArea.clearGeosets();
        // botArea.clearGeosets();
        mainPanel.toolsMenu.getAccessibleContext().setAccessibleDescription(
                "Allows the user to control which parts of the model are displayed for editing.");
        mainPanel.toolsMenu.setEnabled(true);
        SaveProfile.get().addRecent(mainPanel.currentFile.getPath());
        MenuBar.updateRecent(mainPanel);
        MenuBar.loadFile(mainPanel, mainPanel.currentFile);
    }

    public static void refreshController(JScrollPane geoControl, JScrollPane geoControlModelData) {
        if (geoControl != null) {
            geoControl.repaint();
        }
        if (geoControlModelData != null) {
            geoControlModelData.repaint();
        }
    }
}
