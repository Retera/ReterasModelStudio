package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.AddSequenceAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlagUtils;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.parsers.mdlx.util.MdxUtils;
import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.ui.application.actionfunctions.ImportFromObjectEditor;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.browsers.model.ModelOptionPanel;
import com.hiveworkshop.rms.ui.browsers.unit.UnitOptionPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
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

		if (ProgramGlobals.getCurrentModelPanel() != null) {
			ProgramGlobals.getRootWindowUgg().getWindowHandler2().reloadThings();
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
		fetchAndAddSingleAnimation(ImportFromObjectEditor.fetchObjectModel());
	}

	public static void addAnimFromModel() {
		EditableModel animationSource = null;

		ModelOptionPanel uop = ModelOptionPanel.getModelOptionPanel(ProgramGlobals.getMainPanel());
		if (uop != null) {
			animationSource = uop.getSelectedModel();
		}
		fetchAndAddSingleAnimation(animationSource);
	}

	public static void addAnimationFromUnit() {
		String path = null;
		GameObject choice = UnitOptionPanel.getGameObject(ProgramGlobals.getMainPanel());
		if (choice != null) {
			path = choice.getField("file");
		}
		fetchAndAddSingleAnimation(path);

	}

	public static void addEmptyAnimation() {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null && modelPanel.getModelHandler() != null) {
			addEmptyAnimation(modelPanel.getModelHandler());
		}
	}

	public static void addEmptyAnimation(ModelHandler modelHandler) {
		JPanel creationPanel = new JPanel(new MigLayout());

		JPanel newAnimationPanel = new JPanel(new MigLayout());
		newAnimationPanel.add(new JLabel("Add new empty animation"), "span 2, wrap");
		newAnimationPanel.add(new JLabel("Name"));
		JTextField nameField = new JTextField();
		nameField.setText("newAnimation");
		newAnimationPanel.add(nameField, "wrap, grow");
		newAnimationPanel.add(new JLabel("Start"));
		JSpinner startSpinner = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
		newAnimationPanel.add(startSpinner, "wrap");
		newAnimationPanel.add(new JLabel("Length"));
		JSpinner lengthSpinner = new JSpinner(new SpinnerNumberModel(500, 0, Integer.MAX_VALUE, 1));
		newAnimationPanel.add(lengthSpinner, "wrap");
		creationPanel.add(newAnimationPanel, "cell 0 0");

		JTable existingAnimationTable = new JTable();
		JPanel existingAnimationsPanel = new JPanel(new MigLayout());
		JScrollPane animScrollPane = new JScrollPane(existingAnimationTable);
		animScrollPane.setPreferredSize(new Dimension(250, 300));
		existingAnimationsPanel.add(animScrollPane, "wrap, span 2");
		creationPanel.add(existingAnimationsPanel, "cell 1 0");

		List<Animation> currAnim = modelHandler.getModel().getAnims();
		List<Integer> startTimes = new ArrayList<>();
		List<Integer> lengths = new ArrayList<>();
		List<String> animationNames = new ArrayList<>();
        for (Animation a : currAnim) {
	        startTimes.add(a.getStart());
	        lengths.add(a.getLength());
	        animationNames.add(a.getName());
        }

        DefaultTableModel animationTableModel = new DefaultTableModel();
		animationTableModel.addColumn("start", startTimes.toArray());
		animationTableModel.addColumn("length", lengths.toArray());
		animationTableModel.addColumn("name", animationNames.toArray());

        existingAnimationTable.setModel(animationTableModel);

        JButton setStartAfter = new JButton("Start After");
        setStartAfter.addActionListener(e -> {
	        int length = (Integer) lengthSpinner.getValue();
	        int newStart = ((Integer) existingAnimationTable.getValueAt(existingAnimationTable.getSelectedRow(), 1)) + 1;
	        startSpinner.setValue(newStart);
	        lengthSpinner.setValue(length);
        });
        JButton setEndBefore = new JButton("End Before");
//        setEndBefore.addActionListener(e -> lengthSpinner.setValue(existingAnimationTable.getValueAt(existingAnimationTable.getSelectedRow(), 0)));
        setEndBefore.addActionListener(e -> {
	        int duration = (Integer) lengthSpinner.getValue();
	        int selectedRow = existingAnimationTable.getSelectedRow();
	        int start = ((Integer) existingAnimationTable.getValueAt(selectedRow, 0)) - 1 - duration;
	        startSpinner.setValue(start);
        });

		existingAnimationsPanel.add(setStartAfter);
		existingAnimationsPanel.add(setEndBefore);

//        optionPane.setOptions();
		int option = JOptionPane.showConfirmDialog(ProgramGlobals.getMainPanel(), creationPanel, "Create Empty Animation", JOptionPane.OK_CANCEL_OPTION);
		System.out.println("option \"" + option + "\"");
		int start = (Integer) startSpinner.getValue();
		int end = (Integer) lengthSpinner.getValue();
		if (option == 0) {
			Animation animation = new Animation(nameField.getText(), start, end);
			UndoAction action = new AddSequenceAction(modelHandler.getModel(), animation, ModelStructureChangeListener.getModelStructureChangeListener());
			modelHandler.getUndoManager().pushAction(action.redo());
		}
	}

	private static void fetchAndAddSingleAnimation(String path) {
		if(path != null){
			String filepath = ImportFileActions.convertPathToMDX(path);

			ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
			if (modelPanel != null && modelPanel.getModel() != null && filepath != null) {
				try {
					EditableModel animationSource = MdxUtils.loadEditable(GameDataFileSystem.getDefault().getFile(filepath));
					addSingleAnimation(modelPanel.getModel(), animationSource);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static void fetchAndAddSingleAnimation(EditableModel animationSource) {
		if (animationSource != null) {

			ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
			if (modelPanel != null && modelPanel.getModel() != null) {
				addSingleAnimation(modelPanel.getModel(), animationSource);
			}
		}
	}


	public static List<Animation> addAnimationsFrom(EditableModel model, EditableModel other, final List<Animation> anims) {
		// this process destroys the "other" model inside memory, so destroy
		// a copy instead
		other = TempStuffFromEditableModel.deepClone(other, "animation source file");

		List<AnimFlag<?>> othersFlags = ModelUtils.getAllAnimFlags(other);
		List<EventObject> othersEventObjs = other.getEvents();

		List<Animation> newAnimations = new ArrayList<>();

		// ------ Duplicate the time track in the other model -------------
		//
		// On this new, separate time track, we want to be able to
		// the information specific to each node about how it will
		// move if it gets translated into or onto the current model

		List<AnimFlag<?>> newImpFlags = new ArrayList<>();
		for (AnimFlag<?> af : othersFlags) {
			if (!af.hasGlobalSeq()) {
				newImpFlags.add(af.getEmptyCopy());
			} else {
				newImpFlags.add(af.deepCopy());
			}
		}
		List<EventObject> newImpEventObjs = new ArrayList<>();
		for (final Object e : othersEventObjs) {
			newImpEventObjs.add(EventObject.buildEmptyFrom((EventObject) e));
		}

		// Fill the newly created time track with the exact same data, but shifted forward
		// relative to wherever the current model's last animation starts
		for (Animation anim : anims) {
			int animTrackEnd = ModelUtils.animTrackEnd(model);
			int newStart = animTrackEnd + 300;
			int length = anim.getLength();
			Animation newAnim = anim.deepCopy();
			// clone the animation from the other model
			newAnim.setAnimStuff(newStart, length);
//			newAnim.copyToInterval(newStart, newStart + length, newAnim, othersFlags, othersEventObjs, newImpFlags, newImpEventObjs);

			for (final AnimFlag<?> af : newImpFlags) {
				if (!af.hasGlobalSeq()) {
					AnimFlag<?> source = othersFlags.get(newImpFlags.indexOf(af));
					AnimFlagUtils.copyFrom(af, source, anim, newAnim);
				}
			}
			for (final EventObject e : newImpEventObjs) {
				if (!e.hasGlobalSeq()) {
					EventObject source = othersEventObjs.get(newImpEventObjs.indexOf(e));
					e.copyFrom(source, anim, newAnim);
				}
			}

			model.add(newAnim); // add the new animation to this model
			newAnimations.add(newAnim);
		}

		// destroy the other model's animations, filling them in with the new stuff
		for (final AnimFlag<?> af : othersFlags) {
			AnimFlagUtils.setValuesTo(af, newImpFlags.get(othersFlags.indexOf(af)));
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
		final List<VisibilitySource> allVisibilitySources = ModelUtils.getAllVis(model);
		for (VisibilitySource source : allVisibilitySources) {
			AnimFlag<?> visibilityFlag = source.getVisibilityFlag();
			AnimFlag<?> copyFlag = visibilityFlag.deepCopy();
			visibilityFlag.deleteAnim(target);
			AnimFlagUtils.copyFrom(visibilityFlag, copyFlag, visibilitySource, target);
		}
	}
}
