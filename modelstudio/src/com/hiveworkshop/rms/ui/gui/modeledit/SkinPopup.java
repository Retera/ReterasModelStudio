package com.hiveworkshop.rms.ui.gui.modeledit;

import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.BoneShell;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.BoneShellListCellRenderer;
import com.hiveworkshop.rms.util.IterableListModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class SkinPopup extends JPanel {
    private static final int BONE_COUNT = 4;
    private Bone[] bones = new Bone[BONE_COUNT];
    private JButton[] boneButtons = new JButton[BONE_COUNT];
    private JSpinner[] weightSpinners = new JSpinner[BONE_COUNT];

    IterableListModel<BoneShell> filteredBones = new IterableListModel<>();
    IterableListModel<BoneShell> boneList;
    JList<BoneShell> bonesJList;
    JTextField boneSearch;

    JLabel missingWeightsLabel;

    public SkinPopup(ModelView modelView) {
        setLayout(new MigLayout("", "[][][]", "[][][][]"));

        JPanel boneChooserPanel = boneChooserPanel(modelView);

        for (int i = 0; i < BONE_COUNT; i++) {
            final int index = i;
            JButton boneButton = new JButton("Choose a Bone");
            add(boneButton, "growx");
            boneButton.addActionListener(e -> boneChooserPopup(index, boneButton, boneChooserPanel));

            JSpinner boneWeightSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 255, 1));
            boneWeightSpinner.addChangeListener(e -> updateWeightLabel());
            add(boneWeightSpinner, "wrap");

            boneButtons[i] = boneButton;
            weightSpinners[i] = boneWeightSpinner;
        }
        missingWeightsLabel = new JLabel("( +255 )");
        add(missingWeightsLabel, "cell 2 0");
    }

    private void boneChooserPopup(int index, JButton boneButton, JPanel panel) {


//        JOptionPane.showOptionDialog(this, panel, "Choose Bone", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, new String[] {"Ok", "Cancle"}, 1);
        int option = JOptionPane.showConfirmDialog(this, panel, "Choose Bone", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
        if (option == JOptionPane.OK_OPTION) {
            onBoneChosen(index, boneButton);
        }
//        JOptionPane.showMessageDialog(this, panel);
    }

    private JPanel boneChooserPanel(ModelView modelView) {
        JPanel panel = new JPanel(new MigLayout("fill, gap 0", "[grow]", "[][][grow]"));

        BoneShellListCellRenderer renderer = new BoneShellListCellRenderer(modelView, null).setShowClass(false);
        JCheckBox showParents = new JCheckBox("Show Parents");
        showParents.addActionListener(e -> showParents(renderer, showParents, panel));
        panel.add(showParents, "wrap");

        boneSearch = new JTextField();
        boneSearch.addCaretListener(e -> filterBones());
        panel.add(boneSearch, "growx, wrap");

        boneList = new IterableListModel<>();
        for (Bone bone : modelView.getModel().getBones()) {
            BoneShell boneShell = new BoneShell(bone);
            boneList.addElement(boneShell);
        }

        bonesJList = new JList<>(boneList);
        bonesJList.setCellRenderer(renderer);

        panel.add(new JScrollPane(bonesJList), "growx, growy, wrap");
        return panel;
    }

    private void showParents(BoneShellListCellRenderer renderer, JCheckBox checkBox, JPanel panel) {
        renderer.setShowParent(checkBox.isSelected());
        panel.repaint();
    }

    private void onBoneChosen(int index, JButton boneButton) {
        BoneShell selectedValue = bonesJList.getSelectedValue();

        bones[index] = selectedValue.getBone();
        boneButton.setText(selectedValue.getBone().getName());
    }

    public Bone[] getBones() {
        return bones;
    }

    public short[] getSkinWeights() {
        short[] weights = new short[BONE_COUNT];
        int totMissingWeight = 255;
        for (int i = 0; i < BONE_COUNT; i++) {
            weights[i] = ((Number) weightSpinners[i].getValue()).shortValue();
            totMissingWeight -= weights[i];
        }
        weights[0] += totMissingWeight;
        return weights;
    }

    private void updateWeightLabel() {
        int totMissingWeight = 255;
        for (int i = 0; i < BONE_COUNT; i++) {
            totMissingWeight -= ((Number) weightSpinners[i].getValue()).intValue();
        }
        String token = totMissingWeight >= 0 ? "+" : "";
        missingWeightsLabel.setText("( " + token + totMissingWeight + " )");
    }

    private void filterBones() {
        String filterText = boneSearch.getText();
        if (!filterText.equals("")) {
            filteredBones.clear();
            for (BoneShell boneShell : boneList) {
                if (boneShell.getName().toLowerCase().contains(filterText.toLowerCase())) {
                    filteredBones.addElement(boneShell);
                }
            }
            bonesJList.setModel(filteredBones);
        } else {
            bonesJList.setModel(boneList);
        }
    }
}
