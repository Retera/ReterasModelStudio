package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.List;

public class ModelEditActions {
    static double lastNormalMaxAngle = 90;
    static boolean useTris = false;

    public static void viewMatrices(MainPanel mainPanel) {
        final ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
        if (modelPanel != null) {
            modelPanel.viewMatrices();
        }
        mainPanel.repaint();
    }

    public static void inverseAllUVs(MainPanel mainPanel) {
        for (final Geoset geo : mainPanel.currentMDL().getGeosets()) {
            for (final GeosetVertex vertex : geo.getVertices()) {
                for (final Vec2 tvert : vertex.getTverts()) {
                    final float temp = tvert.x;
                    tvert.x = tvert.y;
                    tvert.y = temp;
                }
            }
        }
        mainPanel.repaint();
    }

    public static void flipAllUVsV(MainPanel mainPanel) {
        for (final Geoset geo : mainPanel.currentMDL().getGeosets()) {
            for (final GeosetVertex vertex : geo.getVertices()) {
                for (final Vec2 tvert : vertex.getTverts()) {
                    tvert.y = 1.0f - tvert.y;
                }
            }
        }
        mainPanel.repaint();
    }

    public static void flipAllUVsU(MainPanel mainPanel) {
        for (final Geoset geo : mainPanel.currentMDL().getGeosets()) {
            for (final GeosetVertex vertex : geo.getVertices()) {
                for (final Vec2 tvert : vertex.getTverts()) {
                    tvert.x = 1.0f - tvert.x;
                }
            }
        }
        mainPanel.repaint();
    }

    public static void insideOutNormals(MainPanel mainPanel) {
        final ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
        if (modelPanel != null) {
            modelPanel.getUndoManager().pushAction(modelPanel.getModelEditorManager().getModelEditor().flipSelectedNormals());
        }
        mainPanel.repaint();
    }

    public static void insideOut(MainPanel mainPanel) {
        final ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
        if (modelPanel != null) {
            modelPanel.getUndoManager().pushAction(modelPanel.getModelEditorManager().getModelEditor().flipSelectedFaces());
        }
        mainPanel.repaint();
    }

    public static void snapVertices(MainPanel mainPanel) {
        final ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
        if (modelPanel != null) {
            modelPanel.getUndoManager().pushAction(modelPanel.getModelEditorManager().getModelEditor().snapSelectedVertices());
        }
        mainPanel.repaint();
    }

    public static void snapNormals(MainPanel mainPanel) {
        final ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
        if (modelPanel != null) {
            modelPanel.getUndoManager().pushAction(modelPanel.getModelEditorManager().getModelEditor().snapNormals());
        }
        mainPanel.repaint();
    }

    public static void recalculateNormals(MainPanel mainPanel) {
        final ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
        if (modelPanel != null) {
            JPanel panel = new JPanel(new MigLayout());
            panel.add(new JLabel("Limiting angle"));
            JSpinner spinner = new JSpinner(new SpinnerNumberModel(lastNormalMaxAngle, -180.0, 180.0, 1));
            panel.add(spinner, "wrap");
            panel.add(new JLabel("Use triangles instead of vertices"));
            JCheckBox useTries = new JCheckBox();
            useTries.setSelected(useTris);
            panel.add(useTries);
            int option = JOptionPane.showConfirmDialog(mainPanel, panel, "Recalculate Normals", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                lastNormalMaxAngle = (double) spinner.getValue();
                useTris = useTries.isSelected();
                modelPanel.getUndoManager().pushAction(modelPanel.getModelEditorManager().getModelEditor().recalcNormals(lastNormalMaxAngle, useTris));
            }
        }
        mainPanel.repaint();
    }

    public static void recalculateExtents(MainPanel mainPanel) {
        final ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
        if (modelPanel != null) {
            final JPanel messagePanel = new JPanel(new MigLayout());
            messagePanel.add(new JLabel("This will calculate the extents of all model components. Proceed?"),
                    "wrap");
            messagePanel.add(new JLabel("(It may destroy existing extents)"), "wrap");
            final JRadioButton considerAllBtn = new JRadioButton("Consider all geosets for calculation");
            final JRadioButton considerCurrentBtn = new JRadioButton(
                    "Consider current editable geosets for calculation");
            final ButtonGroup buttonGroup = new ButtonGroup();
            buttonGroup.add(considerAllBtn);
            buttonGroup.add(considerCurrentBtn);
            considerAllBtn.setSelected(true);
            messagePanel.add(considerAllBtn, "wrap");
            messagePanel.add(considerCurrentBtn, "wrap");
            final int userChoice = JOptionPane.showConfirmDialog(mainPanel, messagePanel, "Message",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (userChoice == JOptionPane.YES_OPTION) {
                modelPanel.getUndoManager().pushAction(modelPanel.getModelEditorManager().getModelEditor()
                        .recalcExtents(considerCurrentBtn.isSelected()));
            }
        }
        mainPanel.repaint();
    }

    public static void mirrorAxis(MainPanel mainPanel, byte i, boolean mirrorFlip) {
        final ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
        if (modelPanel != null) {
            final Vec3 selectionCenter = modelPanel.getModelEditorManager().getModelEditor().getSelectionCenter();
            modelPanel.getUndoManager().pushAction(
                    modelPanel.getModelEditorManager().getModelEditor()
                            .mirror(i, mirrorFlip, selectionCenter.x, selectionCenter.y, selectionCenter.z));
        }
        mainPanel.repaint();
    }

    public static void linearizeAnimations(MainPanel mainPanel) {
        final int x = JOptionPane.showConfirmDialog(mainPanel,
                "This is an irreversible process that will lose some of your model data," +
                        "\nin exchange for making it a smaller storage size." +
                        "\n\nContinue and simplify animations?",
                "Warning: Linearize Animations", JOptionPane.OK_CANCEL_OPTION);
        if (x == JOptionPane.OK_OPTION) {
            final List<AnimFlag<?>> allAnimFlags = mainPanel.currentMDL().getAllAnimFlags();
            for (final AnimFlag<?> flag : allAnimFlags) {
                flag.linearize();
            }
        }
    }

    public static void simplifyKeyframes(MainPanel mainPanel) {
        final int x = JOptionPane.showConfirmDialog(mainPanel,
                "This is an irreversible process that will lose some of your model data," +
                        "\nin exchange for making it a smaller storage size." +
                        "\n\nContinue and simplify keyframes?",
                "Warning: Simplify Keyframes", JOptionPane.OK_CANCEL_OPTION);
        if (x == JOptionPane.OK_OPTION) {
            final EditableModel currentMDL = mainPanel.currentMDL();
            currentMDL.simplifyKeyframes();
        }
    }
}
