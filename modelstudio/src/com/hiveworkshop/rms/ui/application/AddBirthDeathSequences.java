package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.parsers.mdlx.InterpolationType;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class AddBirthDeathSequences {
    static void riseFallBirthActionRes(MainPanel mainPanel) {
        final ModelView disp = mainPanel.currentModelPanel().getModelViewManager();
        final EditableModel model = disp.getModel();
        final Animation lastAnim = model.getAnim(model.getAnimsSize() - 1);

        final Animation oldBirth = model.findAnimByName("birth");
        final Animation oldDeath = model.findAnimByName("death");

        Animation birth = new Animation("Birth", lastAnim.getEnd() + 300, lastAnim.getEnd() + 2300);
        Animation death = new Animation("Death", birth.getEnd() + 300, birth.getEnd() + 2300);
        final Animation stand = model.findAnimByName("stand");

        final int confirmed = JOptionPane.showConfirmDialog(mainPanel,
                "This will permanently alter model. Are you sure?", "Confirmation",
                JOptionPane.OK_CANCEL_OPTION);
        if (confirmed != JOptionPane.OK_OPTION) {
            return;
        }

        boolean wipeoutOldBirth = false;
        if (oldBirth != null) {
            final String[] choices = {"Ignore", "Delete", "Overwrite"};
            final Object x = JOptionPane.showInputDialog(mainPanel,
                    "Existing birth detected. What should be done with it?", "Question",
                    JOptionPane.PLAIN_MESSAGE, null, choices, choices[0]);
            if (x == choices[1]) {
                wipeoutOldBirth = true;
            } else if (x == choices[2]) {
                birth = oldBirth;
            } else {
                return;
            }
        }
        boolean wipeoutOldDeath = false;
        if (oldDeath != null) {
            final String[] choices = {"Ignore", "Delete", "Overwrite"};
            final Object x = JOptionPane.showInputDialog(mainPanel,
                    "Existing death detected. What should be done with it?", "Question",
                    JOptionPane.PLAIN_MESSAGE, null, choices, choices[0]);
            if (x == choices[1]) {
                wipeoutOldDeath = true;
            } else if (x == choices[2]) {
                death = oldDeath;
            } else {
                return;
            }
        }
        if (wipeoutOldBirth) {
            model.remove(oldBirth);
        }
        if (wipeoutOldDeath) {
            model.remove(oldDeath);
        }

        final List<IdObject> roots = new ArrayList<>();
        for (final IdObject obj : model.getIdObjects()) {
            if (obj.getParent() == null) {
                roots.add(obj);
            }
        }
        for (final AnimFlag af : model.getAllAnimFlags()) {
            af.deleteAnim(birth);
            af.deleteAnim(death);
        }
        for (final IdObject obj : roots) {
            if (obj instanceof Bone) {
                final Bone b = (Bone) obj;
                AnimFlag trans = null;
                boolean globalSeq = false;
                for (final AnimFlag af : b.getAnimFlags()) {
                    if (af.getTypeId() == AnimFlag.TRANSLATION) {
                        if (af.hasGlobalSeq()) {
                            globalSeq = true;
                        } else {
                            trans = af;
                        }
                    }
                }
                if (globalSeq) {
                    continue;
                }
                if (trans == null) {
                    final List<Integer> times = new ArrayList<>();
                    final List<Integer> values = new ArrayList<>();
                    trans = new AnimFlag("Translation", times, values);
                    trans.setInterpType(InterpolationType.LINEAR);
                    b.getAnimFlags().add(trans);
                }
                trans.addEntry(birth.getStart(), new Vec3(0, 0, -300));
                trans.addEntry(birth.getEnd(), new Vec3(0, 0, 0));
                trans.addEntry(death.getStart(), new Vec3(0, 0, 0));
                trans.addEntry(death.getEnd(), new Vec3(0, 0, -300));
            }
        }

        // visibility
        for (final VisibilitySource source : model.getAllVisibilitySources()) {
            final AnimFlag dummy = new AnimFlag("dummy");
            final AnimFlag af = source.getVisibilityFlag();
            dummy.copyFrom(af);
            af.deleteAnim(birth);
            af.deleteAnim(death);
            af.copyFrom(dummy, stand.getStart(), stand.getEnd(), birth.getStart(), birth.getEnd());
            af.copyFrom(dummy, stand.getStart(), stand.getEnd(), death.getStart(), death.getEnd());
            af.setEntry(death.getEnd(), 0);
        }

        if (!birth.isNonLooping()) {
            birth.setNonLooping(true);
        }
        if (!death.isNonLooping()) {
            death.setNonLooping(true);
        }

        if (!model.contains(birth)) {
            model.add(birth);
        }
        if (!model.contains(death)) {
            model.add(death);
        }

        JOptionPane.showMessageDialog(mainPanel, "Done!");
    }
}
