package com.hiveworkshop.rms.ui.gui.modeledit;

import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.BoneShell;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.ParentToggleRenderer;
import com.hiveworkshop.rms.util.IterableListModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public class SkinPopup extends JPanel {
    private static final int BONE_COUNT = 4;
    private Bone[] bones = new Bone[BONE_COUNT];
    private JButton[] boneButtons = new JButton[BONE_COUNT];
    private JSpinner[] weightSpinners = new JSpinner[BONE_COUNT];

    public SkinPopup(ModelView modelView) {
        setLayout(new MigLayout());

        for (int i = 0; i < BONE_COUNT; i++) {
            final int index = i;
            JButton boneButton = new JButton("null");
            add(boneButton, "growx");
            boneButton.addActionListener(e -> showSkinPopup(modelView, index, boneButton));

            JSpinner boneWeightSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 255, 1));
            add(boneWeightSpinner, "wrap");

            boneButtons[i] = boneButton;
            weightSpinners[i] = boneWeightSpinner;
        }
    }

    private void showSkinPopup(ModelView modelView, int index, JButton boneButton) {
        JPanel panel = new JPanel(new BorderLayout());

        IterableListModel<BoneShell> boneShellListModel = new IterableListModel<>();
        for (Bone bone : modelView.getModel().getBones()) {
            BoneShell boneShell = new BoneShell(bone);
            boneShellListModel.addElement(boneShell);
        }

        JCheckBox showParents = new JCheckBox("Show Parents");
        panel.add(showParents, BorderLayout.NORTH);

        JList<BoneShell> bonesJList = new JList<>(boneShellListModel);

        bonesJList.setCellRenderer(new ParentToggleRenderer(showParents, modelView, null));
        panel.add(new JScrollPane(bonesJList), BorderLayout.CENTER);

        BoneShell selectedValue = bonesJList.getSelectedValue();

        bones[index] = selectedValue.getBone();
        boneButton.setText(selectedValue.getBone().getName());

        JOptionPane.showMessageDialog(this, panel);
    }

    public Bone[] getBones() {
        return bones;
    }

    public short[] getSkinWeights() {
        short[] weights = new short[BONE_COUNT];
        for (int i = 0; i < BONE_COUNT; i++) {
            weights[i] = ((Number) weightSpinners[i].getValue()).shortValue();
        }
        return weights;
    }
}
