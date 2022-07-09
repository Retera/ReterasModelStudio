package com.hiveworkshop.rms.ui.gui.modeledit;

import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.SkinBone;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.model.editors.IntEditorJSpinner;
import com.hiveworkshop.rms.ui.application.tools.IdObjectChooserButton;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.Collections;
import java.util.Set;

public class SkinPopup extends JPanel {
    private static final int BONE_COUNT = 4;
    private Bone[] bones = new Bone[BONE_COUNT];
    private short[] weights = new short[BONE_COUNT];

    JLabel missingWeightsLabel;

    public SkinPopup(ModelView modelView) {
        this(modelView, null);
    }
    public SkinPopup(ModelView modelView, SkinBone[] skinBones) {
        setLayout(new MigLayout("", "[][][]", "[][][][]"));

        Set<Class<?>> filterClasses = Collections.singleton(Bone.class);

        for (int i = 0; i < BONE_COUNT; i++) {
            final int index = i;
            IdObjectChooserButton idObjectChooserButton = new IdObjectChooserButton(modelView.getModel(), filterClasses, this);
            idObjectChooserButton.setButtonText("Choose a Bone").setIdObjectConsumer(o -> bones[index]= (Bone) o);
            add(idObjectChooserButton, "growx");

            IntEditorJSpinner intEditorJSpinner = new IntEditorJSpinner(0, 0, 255, weight -> setWeight(index, weight));
            add(intEditorJSpinner, "wrap");
            if(skinBones != null && skinBones.length>i){
                idObjectChooserButton.setChosenIdObject(skinBones[i].getBone());
                intEditorJSpinner.reloadNewValue(skinBones[i].getWeight());
            }

        }

        missingWeightsLabel = new JLabel("( +255 )");
        add(missingWeightsLabel, "cell 2 0");
    }


    private void setWeight(int index, int weight) {
        weights[index] = (short) weight;
        int totMissingWeight = 255;
        for (int i = 0; i < BONE_COUNT; i++) {
            totMissingWeight -= weights[i];
        }
        String token = totMissingWeight >= 0 ? "+" : "";
        missingWeightsLabel.setText("( " + token + totMissingWeight + " )");
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
