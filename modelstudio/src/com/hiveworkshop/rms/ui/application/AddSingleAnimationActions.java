package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.parsers.mdlx.util.MdxUtils;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.util.UnitFields;
import com.hiveworkshop.rms.ui.browsers.model.ModelOptionPane;
import com.hiveworkshop.rms.ui.browsers.unit.UnitOptionPane;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AddSingleAnimationActions {

	public static void addAnimationFromFile() {
		FileDialog fileDialog = new FileDialog();

		EditableModel animationSourceModel = fileDialog.chooseModelFile(FileDialog.OPEN_WC_MODEL);
		if (animationSourceModel != null) {
			addSingleAnimation(fileDialog.getModel(), animationSourceModel);
		}

//        MenuBarActions.refreshController(mainPanel.currentModelPanel.getGeoControl(), mainPanel.currentModelPanel.getGeoControlModelData());
		if (ProgramGlobals.getCurrentModelPanel() != null) {
			ProgramGlobals.getCurrentModelPanel().repaintModelTrees();
		}
	}

	public static void addSingleAnimation(EditableModel current, EditableModel animationSourceModel) {
		Animation choice = null;
		MainPanel mainPanel = ProgramGlobals.getMainPanel();
		choice = (Animation) JOptionPane.showInputDialog(mainPanel, "Choose an animation!", "Add Animation",
				JOptionPane.QUESTION_MESSAGE, null, animationSourceModel.getAnims().toArray(),
				animationSourceModel.getAnims().get(0));
		if (choice == null) {
			JOptionPane.showMessageDialog(mainPanel, "Bad choice. No animation added.");
			return;
		}
		Animation visibilitySource = (Animation) JOptionPane.showInputDialog(mainPanel,
				"Which animation from THIS model to copy visiblity from?", "Add Animation",
				JOptionPane.QUESTION_MESSAGE, null, current.getAnims().toArray(), current.getAnims().get(0));
		if (visibilitySource == null) {
			JOptionPane.showMessageDialog(mainPanel, "No visibility will be copied.");
		}
		List<Animation> animationsAdded = addAnimationsFrom(current, animationSourceModel, Collections.singletonList(choice));
		for (Animation anim : animationsAdded) {
			copyVisibility(current, visibilitySource, anim);
		}
		JOptionPane.showMessageDialog(mainPanel, "Added " + animationSourceModel.getName() + "'s " + choice.getName()
				+ " with " + visibilitySource.getName() + "'s visibility  OK!");
		ModelStructureChangeListener.changeListener.animationParamsChanged();
	}

	public static void addAnimationFromObject() {
		MutableObjectData.MutableGameObject fetchResult = ImportFileActions.fetchObject();
		if (fetchResult != null) {
			String path = fetchResult.getFieldAsString(UnitFields.MODEL_FILE, 0);
			fetchAndAddSingleAnimation(path);
		}
	}

	public static void addAnimFromModel() {
		fetchAndAddSingleAnimation(ModelOptionPane.fetchModel1(ProgramGlobals.getMainPanel()));
	}

	public static void addAnimationFromUnit() {
		fetchAndAddSingleAnimation(UnitOptionPane.fetchUnit1(ProgramGlobals.getMainPanel()));

	}

	public static void addEmptyAnimation() {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null && modelPanel.getModel() != null) {
			addEmptyAnimation(modelPanel.getModel());
		}
	}

	public static void addEmptyAnimation(EditableModel current) {
		JPanel creationPanel = new JPanel(new MigLayout());

		JPanel newAnimationPanel = new JPanel(new MigLayout());
		newAnimationPanel.add(new JLabel("Add new empty animation"), "span 2, wrap");
		newAnimationPanel.add(new JLabel("Name"));
		JTextField nameField = new JTextField();
		nameField.setText("newAnimation");
//        nameField.setText("");
		newAnimationPanel.add(nameField, "wrap, grow");
		newAnimationPanel.add(new JLabel("Start"));
        JSpinner startSpinner = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
        newAnimationPanel.add(startSpinner, "wrap");
        newAnimationPanel.add(new JLabel("End"));
        JSpinner endSpinner = new JSpinner(new SpinnerNumberModel(1, 0, Integer.MAX_VALUE, 1));
        newAnimationPanel.add(endSpinner, "wrap");
        creationPanel.add(newAnimationPanel, "cell 0 0");

        JTable existingAnimationTable = new JTable();
        JPanel existingAnimationsPanel = new JPanel(new MigLayout());
        JScrollPane animScrollPane = new JScrollPane(existingAnimationTable);
        animScrollPane.setPreferredSize(new Dimension(250, 300));
        existingAnimationsPanel.add(animScrollPane, "wrap, span 2");
        creationPanel.add(existingAnimationsPanel, "cell 1 0");

        List<Animation> currAnim = current.getAnims();
        List<Integer> startTimes = new ArrayList<>();
        List<Integer> endTimes = new ArrayList<>();
        List<String> animationNames = new ArrayList<>();
        for (Animation a : currAnim) {
            startTimes.add(a.getStart());
            endTimes.add(a.getEnd());
            animationNames.add(a.getName());
        }

        DefaultTableModel animationTableModel = new DefaultTableModel();
        animationTableModel.addColumn("start", startTimes.toArray());
        animationTableModel.addColumn("end", endTimes.toArray());
        animationTableModel.addColumn("name", animationNames.toArray());

        existingAnimationTable.setModel(animationTableModel);

        JButton setStartAfter = new JButton("Start After");
        setStartAfter.addActionListener(e -> {
            int end = (Integer) endSpinner.getValue();
            int duration = end - (Integer) startSpinner.getValue();
            int newStart = ((Integer) existingAnimationTable.getValueAt(existingAnimationTable.getSelectedRow(), 1)) + 1;
            if (newStart > end) {
                end = newStart + duration;
            }
            startSpinner.setValue(newStart);
            endSpinner.setValue(end);
        });
        JButton setEndBefore = new JButton("End Before");
//        setEndBefore.addActionListener(e -> endSpinner.setValue(existingAnimationTable.getValueAt(existingAnimationTable.getSelectedRow(), 0)));
        setEndBefore.addActionListener(e -> {
            int start = (Integer) startSpinner.getValue();
            int duration = (Integer) endSpinner.getValue() - start;
            int newEnd = ((Integer) existingAnimationTable.getValueAt(existingAnimationTable.getSelectedRow(), 0)) - 1;
            if (newEnd < start) {
                start = newEnd - duration;
            }
            startSpinner.setValue(start);
            endSpinner.setValue(newEnd);
        });

        existingAnimationsPanel.add(setStartAfter);
        existingAnimationsPanel.add(setEndBefore);

//        optionPane.setOptions();
		int option = JOptionPane.showConfirmDialog(ProgramGlobals.getMainPanel(), creationPanel, "Create Empty Animation", JOptionPane.OK_CANCEL_OPTION);
        System.out.println("option \"" + option + "\"");
        int start = (Integer) startSpinner.getValue();
        int end = (Integer) endSpinner.getValue();
        if (option == 0 && start < end) {
	        Animation animation = new Animation(nameField.getText(), start, end);
	        current.addAnimation(animation);
	        ModelStructureChangeListener.changeListener.animationParamsChanged();
        } else if (option == 0 && start >= end) {
//            JPanel newEndPanel = new JPanel();
//            JSpinner newEndSpinner = new JSpinner(new SpinnerNumberModel(start + 1,start+1,Integer.MAX_VALUE, 1));
//            newEndPanel.add(newEndSpinner);
	        JOptionPane.showConfirmDialog(ProgramGlobals.getMainPanel(), "End needs to be after start", "Choose valid end time", JOptionPane.DEFAULT_OPTION);
        }

    }

	private static void fetchAndAddSingleAnimation(String path) {
		if(path != null){
			String filepath = ImportFileActions.convertPathToMDX(path);

			ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
			if (modelPanel != null && modelPanel.getModel() != null && filepath != null) {
				EditableModel animationSource;
				try {
					animationSource = MdxUtils.loadEditable(GameDataFileSystem.getDefault().getFile(filepath));
					addSingleAnimation(modelPanel.getModel(), animationSource);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
    }


	public static List<Animation> addAnimationsFrom(EditableModel model, EditableModel other, final List<Animation> anims) {
		// this process destroys the "other" model inside memory, so destroy
		// a copy instead
		other = TempStuffFromEditableModel.deepClone(other, "animation source file");

		final List<AnimFlag<?>> flags = model.getAllAnimFlags();
//		final List<EventObject> eventObjs = (List<EventObject>) sortedIdObjects(EventObject.class);
		final List<EventObject> eventObjs = model.getEvents();

		final List<AnimFlag<?>> othersFlags = other.getAllAnimFlags();
//		final List<EventObject> othersEventObjs = (List<EventObject>) other.sortedIdObjects(EventObject.class);
		final List<EventObject> othersEventObjs = other.getEvents();

		final List<Animation> newAnimations = new ArrayList<>();

		// ------ Duplicate the time track in the other model -------------
		//
		// On this new, separate time track, we want to be able to
		// the information specific to each node about how it will
		// move if it gets translated into or onto the current model

		final List<AnimFlag<?>> newImpFlags = new ArrayList<>();
		for (final AnimFlag<?> af : othersFlags) {
			if (!af.hasGlobalSeq) {
				newImpFlags.add(af.getEmptyCopy());
			} else {
				newImpFlags.add(af.deepCopy());
			}
		}
		final List<EventObject> newImpEventObjs = new ArrayList<>();
		for (final Object e : othersEventObjs) {
			newImpEventObjs.add(EventObject.buildEmptyFrom((EventObject) e));
		}

		// Fill the newly created time track with the exact same data, but shifted forward
		// relative to wherever the current model's last animation starts
		for (final Animation anim : anims) {
			final int animTrackEnd = model.animTrackEnd();
			final int newStart = animTrackEnd + 300;
			final int newEnd = newStart + anim.length();
			final Animation newAnim = new Animation(anim);
			// clone the animation from the other model
			newAnim.copyToInterval(newStart, newEnd, othersFlags, othersEventObjs, newImpFlags, newImpEventObjs);
			newAnim.setInterval(newStart, newEnd);
			model.add(newAnim); // add the new animation to this model
			newAnimations.add(newAnim);
		}

		// destroy the other model's animations, filling them in with the new stuff
		for (final AnimFlag<?> af : othersFlags) {
			af.setValuesTo(newImpFlags.get(othersFlags.indexOf(af)));
		}
		for (final Object e : othersEventObjs) {
			((EventObject) e).setValuesTo(newImpEventObjs.get(othersEventObjs.indexOf(e)));
		}

		// Now, map the bones in the other model onto the bones in the current model
		final List<Bone> leftBehind = new ArrayList<>();
		// the bones that don't find matches in current model
//		for (final IdObject object : other.idObjects) {
		for (final IdObject object : other.getBones()) {
			if (object instanceof Bone) {
				// the bone from the other model
				final Bone bone = (Bone) object;
				// the object in this model of similar name
				final Object localObject = model.getObject(bone.getName());
				if ((localObject instanceof Bone)) {
					final Bone localBone = (Bone) localObject;
					localBone.copyMotionFrom(bone);
					// if it's a match, take the data
				} else {
					leftBehind.add(bone);
				}
			}
		}
		for (final Bone bone : leftBehind) {
			if (bone.animates()) {
				model.add(bone);
			}
		}

		return newAnimations;
		// i think we're done????
	}

	public static void copyVisibility(EditableModel model, Animation visibilitySource, Animation target) {
//		final List<VisibilitySource> allVisibilitySources = getAllVisibilitySources();
		final List<VisibilitySource> allVisibilitySources = model.getAllVis();
		for (VisibilitySource source : allVisibilitySources) {
			AnimFlag<?> visibilityFlag = source.getVisibilityFlag();
			AnimFlag<?> copyFlag = visibilityFlag.deepCopy();
			visibilityFlag.deleteAnim(target);
			visibilityFlag.copyFrom(copyFlag, visibilitySource.getStart(), visibilitySource.getEnd(), target.getStart(), target.getEnd());
		}
	}
}
