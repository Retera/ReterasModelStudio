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
import com.hiveworkshop.rms.ui.application.tools.uielement.IdObjectChooserButton;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionBundle;
import com.hiveworkshop.rms.util.FramePopup;
import com.hiveworkshop.rms.util.ScreenInfo;
import com.hiveworkshop.rms.util.uiFactories.CheckBox;
import com.hiveworkshop.rms.util.uiFactories.Label;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.*;

public class DuplicateForAnimation extends JPanel {
	private final ModelHandler modelHandler;
	private final UndoManager undoManager;
	private final ModelStructureChangeListener changeListener = ModelStructureChangeListener.changeListener;
	private final List<Animation> animationsToSplit = new ArrayList<>();
	private final ModelIconHandler iconHandler = new ModelIconHandler();
	private final TreeSet<Geoset> geosets;
	private IdObject newTopParent = null;


	public DuplicateForAnimation(ModelHandler modelHandler) {
		super(new MigLayout("fill", "[][]", "[][][][]"));
		setPreferredSize(ScreenInfo.getSuitableSize(550, 440, 1.2));
		this.modelHandler = modelHandler;
		this.undoManager = modelHandler.getUndoManager();
		geosets = new TreeSet<>(Comparator.comparingInt(modelHandler.getModel()::getGeosetId));

		add(getInfoLabel("Creates a copies of the chosen geosets and the bones bound to it. "
				+ "The original geosets will be invisible, "
				+ "and the copies will use the original visibility, in the chosen animations. "
				+"The topmost bone-copies will have their parents changed and their transforms recalculated."),
		    "spanx, growx, wrap");

//		add(getInfoLabel("Creates a copy of the chosen geoset and the bones bound to it. "
//				+ "The copy will have the inverted visibility of the original geoset. The topmost "
//				+ "bone-copies will have their parents removed."), "spanx, growx, wrap");

		add(getGeosetChoosingPanel(), "growx, growy");

//		add(getInfoLabel("Choose which animations to use the created copy in. The original geoset will be hidden in these animations."), "growx");

		add(getAnimChoosingPanel(), "growx, growy, wrap");

		JButton doStuff = new JButton("Create Copy");
		doStuff.addActionListener(e -> doStuff());
		add(doStuff);
	}

	private JPanel getAnimChoosingPanel() {
		JPanel innerPanel = new JPanel(new MigLayout("fill, gap 0"));
		for (Animation animation : modelHandler.getModel().getAnims()) {
			JCheckBox comp = CheckBox.create(animation.getName(), false, b -> chooseAnim(animation, b));
			innerPanel.add(comp, "growx, wrap");
		}

		JScrollPane scrollPane = new JScrollPane(innerPanel);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);

		JPanel panel = new JPanel(new MigLayout("fill, gap 0"));
		panel.add(new JLabel("Animations to split"), "wrap");
		panel.add(scrollPane, "growx, growy, wrap");


		panel.add(Label.create("Parent:", "Node to bind copied bone-chains to"), "spanx, split 2");
		IdObjectChooserButton idObjectChooserButton = new IdObjectChooserButton(modelHandler.getModel(), false, this)
				.setButtonText("Choose Parent For Copies");
		idObjectChooserButton.setIdObjectConsumer(node -> newTopParent = node);
		panel.add(idObjectChooserButton);
		return panel;
	}
	private JPanel getGeosetChoosingPanel() {
		JPanel innerPanel = new JPanel(new MigLayout("fill, gap 0"));
		EditableModel model = modelHandler.getModel();
		for (Geoset geoset : model.getGeosets()) {
			JCheckBox comp = CheckBox.create(null, null, false, b -> setImp(geoset, b));
			innerPanel.add(comp, "");
			JLabel label = Label.create(geoset.getName(), iconHandler.getImageIcon(geoset, model), () -> {setImp(geoset, !comp.isSelected());comp.setSelected(!comp.isSelected());});
			innerPanel.add(label, "growx, wrap");
		}
		JScrollPane scrollPane = new JScrollPane(innerPanel);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);

		JPanel panel = new JPanel(new MigLayout("fill, gap 0"));
		panel.add(new JLabel("Geosets to copy"), "wrap");
		panel.add(scrollPane, "growx, growy");
		return panel;
	}

	private void setImp(Geoset geoset, boolean doImp) {
//		System.out.println("imp " + geoset.getName() + ": " + doImp);
		if (doImp) {
			geosets.add(geoset);
		} else {
			geosets.remove(geoset);
		}
	}
	private JTextArea getInfoLabel(String text) {
		JTextArea infoLabel = new JTextArea(text);
		infoLabel.setEditable(false);
		infoLabel.setOpaque(false);
		infoLabel.setLineWrap(true);
		infoLabel.setWrapStyleWord(true);
		return infoLabel;
	}

	private void chooseAnim(Animation animation, boolean selected) {
		if (selected) {
			animationsToSplit.add(animation);
		} else {
			animationsToSplit.remove(animation);
		}
	}

	private void doStuff() {
		if (!geosets.isEmpty()) {
			duplicateGeoWNodes(geosets, animationsToSplit);
		}
	}
	private void duplicateGeoWNodes(Collection<Geoset> geosets, List<Animation> animationsToSplit) {
		List<UndoAction> undoActions = new ArrayList<>();
		Set<IdObject> tempIdObjects = new HashSet<>();
		for (Geoset geoset : geosets) {
			tempIdObjects.addAll(geoset.getBoneMap().keySet());
		}
		Set<IdObject> idObjects = getSortedUsedNodes(tempIdObjects);

		Map<IdObject, IdObject> oldToNewObjMap = new HashMap<>();
		for (IdObject idObject : idObjects) {
			IdObject copy = idObject.copy();
			copy.setName(getCopyName(copy.getName()));
			oldToNewObjMap.put(idObject, copy);
		}

		Set<IdObject> toRebindIdObjects = new HashSet<>();
		for (IdObject newNode : oldToNewObjMap.values()) {
			IdObject newParent = oldToNewObjMap.get(newNode.getParent());
			if (newParent != null) {
				newNode.setParent(newParent);
			} else {
				toRebindIdObjects.add(newNode);
			}
		}

		for (IdObject newNode : oldToNewObjMap.values()) {
			undoActions.add(new AddNodeAction(modelHandler.getModel(), newNode, null));
		}

		for (IdObject idObject : toRebindIdObjects) {
			undoActions.add(new BakeAndRebindAction(idObject, newTopParent, animationsToSplit, modelHandler));
		}

		List<GeosetVertex> vertsToSelect = new ArrayList<>();
		for (Geoset geoset : geosets) {
			Geoset newGeoset = getNewGeoset(geoset, oldToNewObjMap);
			vertsToSelect.addAll(newGeoset.getVertices());
			AnimFlag<Float> visFlagForOrg = getVisFlagForOrg(animationsToSplit, geoset);

			undoActions.add(new AddGeosetAction(newGeoset, modelHandler.getModel(), null));
			undoActions.add(new AddTimelineAction<>(geoset, visFlagForOrg));
		}

		SelectionBundle selectionBundle = new SelectionBundle(oldToNewObjMap.values(), vertsToSelect);
		undoActions.add(new SetSelectionUggAction(selectionBundle, modelHandler.getModelView(), null));

		undoManager.pushAction(new CompoundAction("Duplicate for animation", undoActions, changeListener::geosetsUpdated).redo());
	}

	private Set<IdObject> getSortedUsedNodes(Set<IdObject> usedNodes) {
		Set<IdObject> expandedUsedNodes = new LinkedHashSet<>();

		List<IdObject> tempNodeChain = new ArrayList<>();
		for (IdObject newNode : usedNodes) {
			if (!expandedUsedNodes.contains(newNode)) {
				IdObject tempParent = newNode.getParent();
				expandedUsedNodes.add(newNode);
				while (tempParent != null) {
					tempNodeChain.add(tempParent);
					if (usedNodes.contains(tempParent)) {
						expandedUsedNodes.addAll(tempNodeChain);
						tempNodeChain.clear();
					}
					tempParent = tempParent.getParent();
				}
			}
		}

		LinkedHashSet<IdObject> sortedNodes = new LinkedHashSet<>();
		modelHandler.getModel().getIdObjects().stream().filter(expandedUsedNodes::contains).forEach(sortedNodes::add);
		return sortedNodes;
	}

	private AnimFlag<Float> getVisFlagForOrg(List<Animation> animationsToSplit, Geoset geoset) {
		AnimFlag<Float> visFlag = geoset.getVisibilityFlag();
		if (visFlag == null) {
			visFlag = new FloatAnimFlag(geoset.visFlagName());
		}
		AnimFlag<Float> visFlagForOrg = visFlag.deepCopy();
		splitVis(new HashSet<>(animationsToSplit), false, visFlagForOrg);

		return visFlagForOrg;
	}

	private Geoset getNewGeoset(Geoset geoset, Map<IdObject, IdObject> oldToNewObjMap) {
		Geoset newGeoset = geoset.deepCopy();

		for (GeosetVertex newVert : newGeoset.getVertices()) {
			newVert.replaceBones(oldToNewObjMap, false);
		}

		if (newGeoset.getVisibilityFlag() == null) {
			newGeoset.setVisibilityFlag(new FloatAnimFlag(newGeoset.visFlagName()));
		}

		splitVis(new HashSet<>(animationsToSplit), true, newGeoset.getVisibilityFlag());

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

	private void splitVis(Set<Sequence> animationsToSplit, boolean keepProvided, AnimFlag<Float> animFlag) {
		Entry<Float> invisEntry = animFlag.tans() ? new Entry<>(0, 0.0f, 0.0f, 0.0f) : new Entry<>(0, 0.0f);
		for (Sequence sequence : animFlag.getAnimMap().keySet()) {
			TreeMap<Integer, Entry<Float>> entryMap = animFlag.getEntryMap(sequence);
			if (animationsToSplit.contains(sequence) && !keepProvided || !animationsToSplit.contains(sequence) && keepProvided) {
				entryMap.clear();
				entryMap.put(0, invisEntry.deepCopy());
				entryMap.put(sequence.getLength(), invisEntry.deepCopy().setTime(sequence.getLength()));
			}
		}
	}

	public static void show(JComponent parent, ModelHandler modelHandler) {
		DuplicateForAnimation panel = new DuplicateForAnimation(modelHandler);
		FramePopup.show(panel, parent, "Duplicate For Animation");
	}

}
