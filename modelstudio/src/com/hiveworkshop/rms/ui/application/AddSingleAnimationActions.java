package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.addactions.AddTextureAnimAction;
import com.hiveworkshop.rms.editor.actions.animation.AddSequenceAction;
import com.hiveworkshop.rms.editor.actions.animation.ReplaceSequenceTransformations;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.AddAnimFlagAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.RemoveFlagEntryMapAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.SetFlagEntryMapAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.TimelineContainer;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlagUtils;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.ui.application.actionfunctions.ImportFromObjectEditor;
import com.hiveworkshop.rms.ui.application.actionfunctions.ImportFromUnit;
import com.hiveworkshop.rms.ui.application.actionfunctions.ImportWC3Model;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.GlobalSeqHelper;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.application.tools.BoneChainMapWizard;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.util.SearchableList;
import com.hiveworkshop.rms.util.FramePopup;
import com.hiveworkshop.rms.util.Pair;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.function.Supplier;

public class AddSingleAnimationActions {

	public static void addAnimationFromFile() {
		addAnimationFrom(() -> ModelFromFile.chooseModelFile(FileDialog.OPEN_WC_MODEL, null));
	}
	public static void addAnimationFromUnit() {
		addAnimationFrom(ImportFromUnit::getFileModel);
	}

	public static void addAnimFromModel() {
		addAnimationFrom(ImportWC3Model::fetchModel);
	}

	public static void addAnimationFromObject() {
		addAnimationFrom(ImportFromObjectEditor::fetchObjectModel);
	}

	public static void addAnimationFrom(Supplier<EditableModel> sourceSupplier) {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null && sourceSupplier != null) {
			EditableModel animationSourceModel = sourceSupplier.get();
			if (animationSourceModel != null) {
				addSingleAnimation(modelPanel.getModelHandler(), animationSourceModel);
			}
			ProgramGlobals.getRootWindowUgg().getWindowHandler2().reloadThings();
		}
	}


	public static void addTextureAnim() {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null && modelPanel.getModelHandler() != null) {
			ModelHandler modelHandler = modelPanel.getModelHandler();
			UndoAction action = new AddTextureAnimAction(modelHandler.getModel(), null, ModelStructureChangeListener.getModelStructureChangeListener());
			modelHandler.getUndoManager().pushAction(action.redo());
		}
	}
	public static void addGlobalSeq() {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null && modelPanel.getModelHandler() != null) {
			ModelHandler modelHandler = modelPanel.getModelHandler();
			GlobalSeqHelper.showNewGlobSeqPopup(ProgramGlobals.getMainPanel(), "Enter Length", modelHandler);
		}
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
		List<Integer> animNumber = new ArrayList<>();
		List<Integer> lengths = new ArrayList<>();
		List<String> animationNames = new ArrayList<>();
        for (Animation a : currAnim) {
	        animNumber.add(animNumber.size());
	        lengths.add(a.getLength());
	        animationNames.add(a.getName());
        }

        DefaultTableModel animationTableModel = new DefaultTableModel();
		animationTableModel.addColumn("", animNumber.toArray());
		animationTableModel.addColumn("name", animationNames.toArray());
		animationTableModel.addColumn("length", lengths.toArray());

        existingAnimationTable.setModel(animationTableModel);

		int option = JOptionPane.showConfirmDialog(ProgramGlobals.getMainPanel(), creationPanel, "Create Empty Animation", JOptionPane.OK_CANCEL_OPTION);

		int length = (Integer) lengthSpinner.getValue();
		if (option == JOptionPane.OK_OPTION) {
			Animation animation = new Animation(nameField.getText(), 0, length);
			UndoAction action = new AddSequenceAction(modelHandler.getModel(), animation, ModelStructureChangeListener.getModelStructureChangeListener());
			modelHandler.getUndoManager().pushAction(action.redo());
		}
	}

	public static void addSingleAnimation(ModelHandler modelHandler, EditableModel animationSourceModel) {
		MainPanel mainPanel = ProgramGlobals.getMainPanel();

		EditableModel currModel = modelHandler.getModel();
		BoneChainMapWizard wizard2 = new BoneChainMapWizard(mainPanel, animationSourceModel, currModel);
		JPanel editMappingPanel = wizard2.getEditMappingPanel(-1, false, true);

		SearchableList<Animation> animationsList = new SearchableList<>(AddSingleAnimationActions::filterAnims);
		animationsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		animationsList.addAll(animationSourceModel.getAnims());
		SearchableList<Animation> visibilityList = new SearchableList<>(AddSingleAnimationActions::filterAnims);
		visibilityList.addAll(currModel.getAnims());

		JPanel animPanel = new JPanel(new MigLayout("ins 0"));
		animPanel.add(new JLabel("Choose animation(s) to be added from " + animationSourceModel.getName()), "spanx, wrap");
		animPanel.add(animationsList.getScrollableList(), "growx, wrap");

		JPanel visPanel = new JPanel(new MigLayout("ins 0"));
		visPanel.add(new JLabel("Which animation from " + currModel.getName() + "(THIS) model to copy visibility from?"), "spanx, wrap");
		visPanel.add(visibilityList.getScrollableList(), "growx, wrap");


		JPanel panel = new JPanel(new MigLayout("fill"));
		panel.add(animPanel, "growx, wrap");
		panel.add(visPanel, "growx, wrap");
		JButton doAdd = new JButton("Do Add");
		doAdd.addActionListener(e -> doAddAnimations(modelHandler, animationSourceModel.getName(), panel, animationsList.getSelectedValuesList(), visibilityList.getSelectedValue(), wizard2.fillAndGetChainMap()));
		panel.add(doAdd);

		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Choose Animation", panel);
		tabbedPane.addTab("Map Nodes", editMappingPanel);
		FramePopup.show(tabbedPane, ProgramGlobals.getMainPanel(), "Add Animation");
	}

	private static boolean filterAnims(Animation animation, String text){
		return animation.getName().toLowerCase().contains(text.toLowerCase());
	}

	private static void doAddAnimations(ModelHandler modelHandler, String animSrcName, Component parent, Collection<Animation> animations, Animation vis, Map<IdObject, IdObject> nodeMap) {
		HashMap<Sequence, Sequence> animMap = new HashMap<>();
		for (Animation animation : animations){
			animMap.put(animation, animation);
		}

		List<UndoAction> actions = new ArrayList<>();
		UndoAction importAction = getImportAction(modelHandler.getModel(), animMap, nodeMap);
		actions.add(importAction);

		for (Animation animation : animations) {
			animMap.put(animation, animation);
			actions.addAll(getSetVisibilityActions(modelHandler.getModel(), vis, animation));
		}

		modelHandler.getUndoManager().pushAction(
				new CompoundAction("Add Single Animation", actions,
						ModelStructureChangeListener.changeListener::animationParamsChanged)
						.redo());


		String visString = vis == null ? "everything visible" : vis.getName() + "'s visibility!";
		String animString;
		if (animations.size() == 1) {
			String name = animations.stream().findFirst().get().getName();
			animString = animSrcName + "'s " + name;
		} else {
			animString = animations.size() + " animations from " + animSrcName;
		}
		JOptionPane.showMessageDialog(parent, "Added " + animString + " with " + visString);
	}

	public static List<UndoAction> getSetVisibilityActions(EditableModel model, Animation visibilitySource, Animation target) {
		List<UndoAction> undoActions = new ArrayList<>();
		List<TimelineContainer> allVisibilitySources = ModelUtils.getAllVis(model);
		for (TimelineContainer source : allVisibilitySources) {
			AnimFlag<Float> visibilityFlag = source.getVisibilityFlag();
			TreeMap<Integer, Entry<Float>> entryMapCopy = visibilityFlag.getSequenceEntryMapCopy(visibilitySource);
			if (entryMapCopy != null) {
				undoActions.add(new SetFlagEntryMapAction<>(visibilityFlag, target, entryMapCopy, null));
			}
		}
		return undoActions;
	}

	private static UndoAction getImportAction(EditableModel recModel, Map<Sequence, Sequence> recToDonSequenceMap, Map<IdObject, IdObject> chainMap){
		List<UndoAction> undoActions = new ArrayList<>();
		undoActions.add(getAddSequencesAction(recModel, recToDonSequenceMap));
		undoActions.add(getImportIdObjectsAction(recToDonSequenceMap, chainMap));

		return new CompoundAction("import sub animation", undoActions, ModelStructureChangeListener.changeListener::nodesUpdated);
	}


	private static UndoAction getAddSequencesAction(EditableModel recModel, Map<Sequence, Sequence> recToDonSequenceMap) {
		List<UndoAction> undoActions = new ArrayList<>();
		for(Sequence recSeq : recToDonSequenceMap.keySet()){
			if (!recModel.contains(recSeq)) {
				undoActions.add(new AddSequenceAction(recModel, recSeq, null));
			}
		}
		return new CompoundAction("import sub animation", undoActions, null);
	}
	private static UndoAction getImportIdObjectsAction(Map<Sequence, Sequence> recToDonSequenceMap, Map<IdObject, IdObject> chainMap) {
		List<UndoAction> undoActions = new ArrayList<>();

		for (IdObject recIdObject : chainMap.keySet()) {
			IdObject donIdObject = chainMap.get(recIdObject);
			List<Pair<AnimFlag<?>, AnimFlag<?>>> flagPairs = getFlagPairs(recIdObject, donIdObject);

			for (Pair<AnimFlag<?>, AnimFlag<?>> flagPair : flagPairs) {
				AnimFlag<?> recAnimFlag = flagPair.getFirst();
				AnimFlag<?> donAnimFlag = flagPair.getSecond();

				if (recAnimFlag != null && donAnimFlag != null) {
					undoActions.addAll(getReplaceTransfActions(recToDonSequenceMap, recAnimFlag, donAnimFlag));
				} else if (recAnimFlag != null) {
					for (Sequence sequence : recToDonSequenceMap.keySet()) {
						undoActions.add(new RemoveFlagEntryMapAction<>(recAnimFlag, sequence, null));
					}
				} else {
					undoActions.add(getAddNewAnimFlagAction(recToDonSequenceMap, undoActions, recIdObject, donAnimFlag));
				}
			}
		}

		return new CompoundAction("import sub animation", undoActions, null);
	}

	private static AddAnimFlagAction<?> getAddNewAnimFlagAction(Map<Sequence, Sequence> recToDonSequenceMap, List<UndoAction> undoActions, IdObject recIdObject, AnimFlag<?> donAnimFlag) {
		AnimFlag<?> newAnimFlag = donAnimFlag.getEmptyCopy();
		for (Sequence sequence : recToDonSequenceMap.keySet()) {
			AnimFlagUtils.copyFrom(newAnimFlag, donAnimFlag, recToDonSequenceMap.get(sequence), sequence);
		}
		return new AddAnimFlagAction<>(recIdObject, newAnimFlag, null);
	}

	private static <Q> List<UndoAction> getReplaceTransfActions(Map<Sequence, Sequence> recToDonSequenceMap, AnimFlag<Q> recAnimFlag, AnimFlag<?> donAnimFlag) {
		List<UndoAction> undoActions = new ArrayList<>();
		AnimFlag<Q> donTyped = recAnimFlag.getAsTypedOrNull(donAnimFlag);
		if (donTyped != null) {
			for (Sequence recSequence : recToDonSequenceMap.keySet()) {
				Sequence donSequence = recToDonSequenceMap.get(recSequence);
				undoActions.add(new ReplaceSequenceTransformations<>(donTyped, recAnimFlag, donSequence, recSequence, null));
			}
		}
		return undoActions;
	}

	private static List<Pair<AnimFlag<?>, AnimFlag<?>>> getFlagPairs(IdObject recIdObject, IdObject donIdObject){
		List<Pair<AnimFlag<?>, AnimFlag<?>>> pairList = new ArrayList<>();
		if (recIdObject != null && donIdObject != null) {
			ArrayList<AnimFlag<?>> recAnimFlags = recIdObject.getAnimFlags();
			ArrayList<AnimFlag<?>> donAnimFlags = donIdObject.getAnimFlags();

			Set<AnimFlag<?>> matchedFlags = new HashSet<>();
			for (AnimFlag<?> recAnimFlag : recAnimFlags) {
				AnimFlag<?> donAnimFlag = donIdObject.find(recAnimFlag.getName());
				// donAnimFlag == null means the animation should be cleared
				pairList.add(new Pair<>(recAnimFlag, donAnimFlag));
				matchedFlags.add(donAnimFlag);
			}

			for (AnimFlag<?> donAnimFlag : donAnimFlags) {
				if (!matchedFlags.contains(donAnimFlag)) {
					// Add a pair if receiving IdObject doesn't have transformation of this type
					pairList.add(new Pair<>(null, donAnimFlag));
				}
			}
		}
		return pairList;
	}
	private static <Q> void addUndoActions(Map<Sequence, Sequence> recToDonSequenceMap, List<UndoAction> undoActions, AnimFlag<Q> recAnimFlag, AnimFlag<Q> donAnimFlag) {
		for (Sequence recSequence : recToDonSequenceMap.keySet()) {
			Sequence donSequence = recToDonSequenceMap.get(recSequence);
			undoActions.add(new ReplaceSequenceTransformations<>(donAnimFlag, recAnimFlag, donSequence, recSequence, null));
		}
	}
}
