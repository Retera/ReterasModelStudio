package com.hiveworkshop.rms.ui.gui.modeledit;

import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.ParentToggleRenderer;
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
        for(int i= 0; i < BONE_COUNT; i++) {
            final int index = i;
            JButton boneButton = new JButton("null");
            add(boneButton, "growx");
            boneButton.addActionListener(e -> {
                DefaultListModel<BoneShell> boneShellDefaultListModel = new DefaultListModel<>();
                for (Bone bone : modelView.getModel().getBones()) {
                    boneShellDefaultListModel.addElement(new BoneShell(bone));
                }
                JList<BoneShell> bones = new JList<>(boneShellDefaultListModel);
                JCheckBox showParents = new JCheckBox("Show Parents");
                bones.setCellRenderer(new ParentToggleRenderer(showParents, modelView, null));
                JPanel panel = new JPanel(new BorderLayout());
                panel.add(showParents, BorderLayout.NORTH);
                panel.add(new JScrollPane(bones), BorderLayout.CENTER);
                JOptionPane.showMessageDialog(SkinPopup.this, panel);
                BoneShell selectedValue = bones.getSelectedValue();
                SkinPopup.this.bones[index] = selectedValue.bone;
                boneButton.setText(selectedValue.bone.getName());
            });
            JSpinner boneWeightSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 255, 1));
            add(boneWeightSpinner, "wrap");
            boneButtons[i] = boneButton;
            weightSpinners[i] = boneWeightSpinner;
        }
    }

    public Bone[] getBones() {
        return bones;
    }

    public short[] getSkinWeights() {
        short[] weights = new short[BONE_COUNT];
        for(int i = 0; i < BONE_COUNT; i++) {
            weights[i] = ((Number)weightSpinners[i].getValue()).shortValue();
        }
        return weights;
    }
}
