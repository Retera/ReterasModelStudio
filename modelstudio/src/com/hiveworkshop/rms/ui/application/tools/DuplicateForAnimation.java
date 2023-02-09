package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.addactions.AddGeosetAction;
import com.hiveworkshop.rms.editor.actions.animation.AddTimelineAction;
import com.hiveworkshop.rms.editor.actions.nodes.AddNodeAction;
import com.hiveworkshop.rms.editor.actions.nodes.BakeAndRebindAction;
import com.hiveworkshop.rms.editor.actions.selection.SetSelectionUggAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.editor.model.animflag.FloatAnimFlag;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;
import com.hiveworkshop.rms.ui.application.tools.uielement.GeosetChooser;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionBundle;
import com.hiveworkshop.rms.util.FramePopup;
import com.hiveworkshop.rms.util.ScreenInfo;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.*;

public class DuplicateForAnimation extends JPanel {
	ModelHandler modelHandler;
	UndoManager undoManager;
	ModelStructureChangeListener changeListener = ModelStructureChangeListener.changeListener;
	GeosetChooser geosetChooser;
	Geoset geoset;
	List<Animation> animationsToSplit = new ArrayList<>();
	ModelIconHandler iconHandler = new ModelIconHandler();


	public DuplicateForAnimation(ModelHandler modelHandler){
		super(new MigLayout("fill", "[][]", "[grow][]"));
		setPreferredSize(ScreenInfo.getSuitableSize(550, 440, 1.2));
		this.modelHandler = modelHandler;
		this.undoManager = modelHandler.getUndoManager();
		geosetChooser = new GeosetChooser(modelHandler.getModel());

		add(getInfoLabel("Creates a copy of the chosen geoset and the bones bound to it. "
				+ "The copy will have the inverted visibility of the original geoset. The topmost "
				+ "bone-copies will have their parents removed."), "spanx, growx, wrap");

		JButton chooseGeoset = new JButton("Choose Geoset");
		chooseGeoset.setIcon(iconHandler.getImageIcon(modelHandler.getModel()));
		chooseGeoset.addActionListener(e -> chooseGeoset(chooseGeoset));
		add(chooseGeoset, "");

		add(new JLabel("Animations to split"), "wrap");
		add(getInfoLabel("Choose which animations to use the created copy in. The original geoset will be hidden in these animations."), "growx");

		JScrollPane scrollPane = new JScrollPane(getAnimChoosingPanel());
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		add(scrollPane, "growx, growy, wrap");

		JButton doStuff = new JButton("Create Copy");
		doStuff.addActionListener(e -> doStuff());
		add(doStuff);
	}

	private JPanel getAnimChoosingPanel(){
		JPanel panel = new JPanel(new MigLayout("gap 0"));
		for(Animation animation : modelHandler.getModel().getAnims()){
			JCheckBox comp = new JCheckBox(animation.getName());
			comp.addActionListener(e -> chooseAnim(animation, comp.isSelected()));
			panel.add(comp, "wrap");
		}
		return panel;
	}

	private JTextArea getInfoLabel(String text){
		JTextArea infoLabel = new JTextArea(text);
		infoLabel.setEditable(false);
		infoLabel.setOpaque(false);
		infoLabel.setLineWrap(true);
		infoLabel.setWrapStyleWord(true);
		return infoLabel;
	}

	private void chooseGeoset(JButton button){
		geoset = geosetChooser.chooseGeoset(geoset, this);
		if(geoset == null){
			button.setText("Choose Geoset");
		} else {
			button.setText(geoset.getName());
		}
		button.setIcon(iconHandler.getImageIcon(geoset, modelHandler.getModel()));
	}

	private void chooseAnim(Animation animation, boolean selected){
		if(selected){
			animationsToSplit.add(animation);
		} else {
			animationsToSplit.remove(animation);
		}
	}

	private void doStuff(){
		if(geoset != null){
			duplicateGeoWNodes(geoset, animationsToSplit);
		}
	}

	private void duplicateGeoWNodes(Geoset geoset, List<Animation> animationsToSplit){
		List<UndoAction> undoActions = new ArrayList<>();
		Set<IdObject> idObjects = new HashSet<>(geoset.getBoneMap().keySet());

		Map<IdObject, IdObject> oldToNewObjMap = new HashMap<>();
		for (IdObject idObject : idObjects) {
			IdObject copy = idObject.copy();
			copy.setName(getCopyName(copy.getName()));
			oldToNewObjMap.put(idObject, copy);
		}

		Set<IdObject> toRebindIdObjects = new HashSet<>();
		for (IdObject idObject : oldToNewObjMap.values()) {
			IdObject newParent = oldToNewObjMap.get(idObject.getParent());
			if(newParent != null) {
				idObject.setParent(newParent);
			} else {
				toRebindIdObjects.add(idObject);
			}
		}

		for(IdObject idObject : oldToNewObjMap.values()){
			undoActions.add(new AddNodeAction(modelHandler.getModel(), idObject, null));
		}

		for(IdObject idObject : toRebindIdObjects) {
			undoActions.add(new BakeAndRebindAction(idObject, null, animationsToSplit, modelHandler));
		}

		Geoset newGeoset = getNewGeoset(geoset, oldToNewObjMap);
		AnimFlag<Float> visFlagForOrg = getVisFlagForOrg(animationsToSplit, newGeoset);

		undoActions.add(new AddGeosetAction(newGeoset, modelHandler.getModel(), null));
		undoActions.add(new AddTimelineAction<>(geoset, visFlagForOrg));

		SelectionBundle selectionBundle = new SelectionBundle(oldToNewObjMap.values(), newGeoset.getVertices());
		undoActions.add(new SetSelectionUggAction(selectionBundle, modelHandler.getModelView(), null));

		undoManager.pushAction(new CompoundAction("Duplicate for animation", undoActions, changeListener::geosetsUpdated).redo());
	}

	private AnimFlag<Float> getVisFlagForOrg(List<Animation> animationsToSplit, Geoset geoset) {
		if(geoset.getVisibilityFlag() == null){
			geoset.setVisibilityFlag(new FloatAnimFlag(geoset.visFlagName()));
		}
		AnimFlag<Float> visFlagForOrg = geoset.getVisibilityFlag().deepCopy();
		invertVis(animationsToSplit, visFlagForOrg);

		return visFlagForOrg;
	}

	private Geoset getNewGeoset(Geoset geoset, Map<IdObject, IdObject> oldToNewObjMap) {
		Geoset newGeoset = geoset.deepCopy();

		for (GeosetVertex newVert : newGeoset.getVertices()) {
			newVert.replaceBones(oldToNewObjMap, false);
		}

		if(newGeoset.getVisibilityFlag() == null){
			newGeoset.setVisibilityFlag(new FloatAnimFlag(newGeoset.visFlagName()));
		}

		List<Sequence> animationsToKeep = new ArrayList<>(modelHandler.getModel().getAnims());
		animationsToKeep.removeAll(animationsToSplit);
		invertVis(animationsToKeep, newGeoset.getVisibilityFlag());

		return newGeoset;
	}

	private String getCopyName(String copyName) {
		EditableModel model = modelHandler.getModel();
		String name = copyName + " copy";
		if (model.getObject(name) != null) {
			for (int i = 2; i < 100; i++) {
				if (model.getObject(name + i) == null) {
					return name + i;
				}
			}
		}
		return name;
	}

	private void invertVis(List<? extends Sequence> allSequences, AnimFlag<Float> animFlag) {
		for (Sequence sequence : allSequences){
			if(animFlag.getEntryAt(sequence, 0) == null){
				if(animFlag.tans()){
					animFlag.addEntry(new Entry<>(0, 1.0f, 1.0f, 1.0f), sequence);
				} else {
					animFlag.addEntry(new Entry<>(0, 1.0f), sequence);
				}
			}
		}
		for(TreeMap<Integer, Entry<Float>> entryMap : animFlag.getAnimMap().values()){
			for(Entry<Float> entry : entryMap.values()){
				entry.setValue(Math.min(1f, Math.max(0f, 1f-entry.getValue())));
			}
		}
	}

	public static void show(JComponent parent, ModelHandler modelHandler) {
		DuplicateForAnimation panel = new DuplicateForAnimation(modelHandler);
		FramePopup.show(panel, parent, "Duplicate For Animation");
	}

}
