package com.hiveworkshop.rms.ui.gui.modeledit;

import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.SkinBone;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.model.editors.IntEditorJSpinner;
import com.hiveworkshop.rms.ui.application.tools.uielement.IdObjectChooserButton;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.Collections;
import java.util.Set;

public class SkinPopup extends JPanel {
    private static final int BONE_COUNT = 4;
    private Bone[] bones = new Bone[BONE_COUNT];
    private short[] weights = new short[BONE_COUNT];
    private IdObjectChooserButton[] buttons = new IdObjectChooserButton[BONE_COUNT];
    private IntEditorJSpinner[] spinners = new IntEditorJSpinner[BONE_COUNT];

    JLabel missingWeightsLabel;

    public SkinPopup(ModelView modelView) {
        this(modelView.getModel(), null);
    }
    public SkinPopup(EditableModel model, SkinBone[] skinBones) {
        setLayout(new MigLayout("", "[][][][]", "[][][][]"));

        Set<Class<?>> filterClasses = Collections.singleton(Bone.class);
        JPanel topPanel = new JPanel(new MigLayout("fill, ins 0"));
        JButton sort = new JButton("Sort");
        sort.addActionListener(e -> sortByWeight());
        topPanel.add(sort, "");
        JButton mergeSame = new JButton("Merge Same");
        mergeSame.addActionListener(e -> mergeSameBone());
        topPanel.add(mergeSame, "");
        add(topPanel, "span x, wrap");


        for (int i = 0; i < BONE_COUNT; i++) {
            final int index = i;
            buttons[i] = new IdObjectChooserButton(model, filterClasses, this);
            buttons[i].setButtonText("Choose a Bone").setIdObjectConsumer(o -> bones[index]= (Bone) o);
            add(buttons[i], "growx");

            spinners[i] = new IntEditorJSpinner(0, 0, 255, weight -> setWeight(index, weight));
            add(spinners[i], "");
            JButton remove = new JButton("remove");
            remove.addActionListener(e -> removeBone(index));
            add(remove, "skip 1, wrap");
            if(skinBones != null && skinBones.length>i){
                bones[index] = skinBones[i].getBone();
                weights[index] = skinBones[i].getWeight();
                buttons[i].setChosenIdObject(skinBones[i].getBone());
                spinners[i].reloadNewValue((int)skinBones[i].getWeight());
            }

        }
        missingWeightsLabel = new JLabel("( +255 )");
        updateMissingWeightsLabel();
        add(missingWeightsLabel, "cell 2 1");
    }


    private void setWeight(int index, int weight) {
        weights[index] = (short) (0 + weight);
        updateMissingWeightsLabel();
    }

    private void updateMissingWeightsLabel() {
        int totMissingWeight = 255;
        for (int i = 0; i < BONE_COUNT; i++) {
            totMissingWeight -= weights[i];
        }
        String token = totMissingWeight >= 0 ? "+" : "";
        missingWeightsLabel.setText("( " + token + totMissingWeight + " )");
    }

    private void removeBone(int index){
        weights[index] = (short) (0);
        bones[index] = null;
        buttons[index].setChosenIdObject(bones[index]);
        spinners[index].reloadNewValue(weights[index]);
        updateMissingWeightsLabel();
    }
    private void moveBone(int index, int adj){
        int newIndex = index + adj;
        if(0 <= newIndex && newIndex < bones.length){
            short weight1 = weights[index];
            Bone bone1 = bones[index];
            short weight2 = weights[newIndex];
            Bone bone2 = bones[newIndex];
            weights[newIndex] = weight1;
            bones[newIndex] = bone1;
            weights[index] = weight2;
            bones[index] = bone2;
            buttons[index].setChosenIdObject(bones[index]);
            spinners[index].reloadNewValue(weights[index]);

            buttons[newIndex].setChosenIdObject(bones[index]);
            spinners[newIndex].reloadNewValue(weights[index]);

        }
    }

    private void mergeSameBone(){
        for(int boneIndex = 0; boneIndex<bones.length; boneIndex++){
            Bone boneToCheck = bones[boneIndex];
            for(int i = boneIndex+1; i< bones.length; i++){
                if(bones[i] == boneToCheck){
                    weights[boneIndex] += weights[i];
                    spinners[boneIndex].reloadNewValue(weights[boneIndex]);
                    weights[i] = 0;
                    bones[i] = null;
                    buttons[i].setChosenIdObject(bones[i]);
                    spinners[i].reloadNewValue(weights[i]);
                }
            }
        }
    }
    private void sortByWeight(){
        for(int i = 0; i<bones.length; i++){
            for(int j = i+1; j< bones.length; j++){
                if(weights[i] < weights[j]){
                    short tempWeight = weights[i];
                    Bone tempBone = bones[i];

                    weights[i] = weights[j];
                    bones[i] = bones[j];

                    weights[j] = tempWeight;
                    bones[j] = tempBone;

                    buttons[i].setChosenIdObject(bones[i]);
                    spinners[i].reloadNewValue(weights[i]);
                    buttons[j].setChosenIdObject(bones[j]);
                    spinners[j].reloadNewValue(weights[j]);
                }
            }
        }
    }

    public Bone[] getBones() {
        return bones;
    }

    public short[] getSkinWeights() {
        int totMissingWeight = 255;
        for (int i = 0; i < BONE_COUNT; i++) {
            totMissingWeight -= weights[i];
        }
        weights[0] += totMissingWeight;
        return weights;
    }
}
