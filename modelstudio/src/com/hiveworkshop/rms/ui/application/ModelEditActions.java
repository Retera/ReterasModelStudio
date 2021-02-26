package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.model.AnimFlag;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.List;

public class ModelEditActions {
    static void viewMatrices(MainPanel mainPanel) {
        final ModelPanel modelPanel = mainPanel.currentModelPanel();
        if (modelPanel != null) {
            modelPanel.viewMatrices();
        }
        mainPanel.repaint();
    }

    static void inverseAllUVs(MainPanel mainPanel) {
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

    static void flipAllUVsV(MainPanel mainPanel) {
        for (final Geoset geo : mainPanel.currentMDL().getGeosets()) {
            for (final GeosetVertex vertex : geo.getVertices()) {
                for (final Vec2 tvert : vertex.getTverts()) {
                    tvert.y = 1.0f - tvert.y;
                }
            }
        }
        mainPanel.repaint();
    }

    static void flipAllUVsU(MainPanel mainPanel) {
        for (final Geoset geo : mainPanel.currentMDL().getGeosets()) {
            for (final GeosetVertex vertex : geo.getVertices()) {
                for (final Vec2 tvert : vertex.getTverts()) {
                    tvert.x = 1.0f - tvert.x;
                }
            }
        }
        mainPanel.repaint();
    }

    static void insideOutNormals(MainPanel mainPanel) {
        final ModelPanel modelPanel = mainPanel.currentModelPanel();
        if (modelPanel != null) {
            modelPanel.getUndoManager().pushAction(modelPanel.getModelEditorManager().getModelEditor().flipSelectedNormals());
        }
        mainPanel.repaint();
    }

    static void insideOut(MainPanel mainPanel) {
        final ModelPanel modelPanel = mainPanel.currentModelPanel();
        if (modelPanel != null) {
            modelPanel.getUndoManager().pushAction(modelPanel.getModelEditorManager().getModelEditor().flipSelectedFaces());
        }
        mainPanel.repaint();
    }

    static void snapVertices(MainPanel mainPanel) {
        final ModelPanel modelPanel = mainPanel.currentModelPanel();
        if (modelPanel != null) {
            modelPanel.getUndoManager().pushAction(modelPanel.getModelEditorManager().getModelEditor().snapSelectedVertices());
        }
        mainPanel.repaint();
    }

    static void snapNormals(MainPanel mainPanel) {
        final ModelPanel modelPanel = mainPanel.currentModelPanel();
        if (modelPanel != null) {
            modelPanel.getUndoManager().pushAction(modelPanel.getModelEditorManager().getModelEditor().snapNormals());
        }
        mainPanel.repaint();
    }

    static void recalculateNormals(MainPanel mainPanel) {
        final ModelPanel modelPanel = mainPanel.currentModelPanel();
        if (modelPanel != null) {
            modelPanel.getUndoManager().pushAction(modelPanel.getModelEditorManager().getModelEditor().recalcNormals());
        }
        mainPanel.repaint();
    }

    static void recalculateExtents(MainPanel mainPanel) {
        final ModelPanel modelPanel = mainPanel.currentModelPanel();
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

    static void mirrorAxis(MainPanel mainPanel, byte i) {
        final ModelPanel modelPanel = mainPanel.currentModelPanel();
        if (modelPanel != null) {
            final Vec3 selectionCenter = modelPanel.getModelEditorManager().getModelEditor().getSelectionCenter();
            modelPanel.getUndoManager().pushAction(
                    modelPanel.getModelEditorManager().getModelEditor()
                            .mirror(i, mainPanel.mirrorFlip.isSelected(), selectionCenter.x, selectionCenter.y, selectionCenter.z));
        }
        mainPanel.repaint();
    }

    static void linearizeAnimations(MainPanel mainPanel) {
        final int x = JOptionPane.showConfirmDialog(mainPanel,
                "This is an irreversible process that will lose some of your model data," +
                        "\nin exchange for making it a smaller storage size." +
                        "\n\nContinue and simplify animations?",
                "Warning: Linearize Animations", JOptionPane.OK_CANCEL_OPTION);
        if (x == JOptionPane.OK_OPTION) {
            final List<AnimFlag> allAnimFlags = mainPanel.currentMDL().getAllAnimFlags();
            for (final AnimFlag flag : allAnimFlags) {
                flag.linearize();
            }
        }
    }

    static void simplifyKeyframes(MainPanel mainPanel) {
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
