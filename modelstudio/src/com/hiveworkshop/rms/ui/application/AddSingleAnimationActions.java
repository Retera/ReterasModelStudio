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
import com.hiveworkshop.rms.editor.model.VisibilitySource;
import com.hiveworkshop.rms.editor.model.animflag.*;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.parsers.mdlx.util.MdxUtils;
import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.ui.application.actionfunctions.ImportFromObjectEditor;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.GlobalSeqHelper;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.application.tools.BoneChainMapWizard;
import com.hiveworkshop.rms.ui.browsers.model.ModelOptionPanel;
import com.hiveworkshop.rms.ui.browsers.unit.UnitOptionPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.util.SearchableList;
import com.hiveworkshop.rms.util.FramePopup;
import com.hiveworkshop.rms.util.Pair;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.*;

public class AddSingleAnimationActions {

	public static void addAnimationFromFile() {
		FileDialog fileDialog = new FileDialog();

		EditableModel animationSourceModel = fileDialog.chooseModelFile(FileDialog.OPEN_WC_MODEL);
		if (animationSourceModel != null) {
			ModelHandler modelHandler = new ModelHandler(fileDialog.getModel());
			addSingleAnimation(modelHandler, animationSourceModel);
		}

		if (ProgramGlobals.getCurrentModelPanel() != null) {
			ProgramGlobals.getRootWindowUgg().getWindowHandler2().reloadThings();
		}
	}

	public static void addSingleAnimation(ModelHandler modelHandler, EditableModel animationSourceModel) {
		MainPanel mainPanel = ProgramGlobals.getMainPanel();

		EditableModel currModel = modelHandler.getModel();
		BoneChainMapWizard wizard2 = new BoneChainMapWizard(mainPanel, animationSourceModel, currModel);
		JPanel editMappingPanel = wizard2.getEditMappingPanel(-1, false, true);

		SearchableList<Animation> animationsList = new SearchableList<>(AddSingleAnimationActions::filterAnims);
		animationsList.addAll(animationSourceModel.getAnims());
		SearchableList<Animation> visibilityList = new SearchableList<>(AddSingleAnimationActions::filterAnims);
		visibilityList.addAll(currModel.getAnims());

		JPanel animPanel = new JPanel(new MigLayout("ins 0"));
		animPanel.add(new JLabel("Choose animation to be added from " + animationSourceModel.getName()), "spanx, wrap");
		animPanel.add(animationsList.getScrollableList(), "growx, wrap");

		JPanel visPanel = new JPanel(new MigLayout("ins 0"));
		visPanel.add(new JLabel("Which animation from " + currModel.getName() + "(THIS) model to copy visibility from?"), "spanx, wrap");
		visPanel.add(visibilityList.getScrollableList(), "growx, wrap");


		JPanel panel = new JPanel(new MigLayout("fill"));
		panel.add(animPanel, "growx, wrap");
		panel.add(visPanel, "growx, wrap");
		JButton doAdd = new JButton("Do Add");
		doAdd.addActionListener(e -> doAddAnimation(modelHandler, animationSourceModel.getName(), panel, animationsList.getSelectedValue(), visibilityList.getSelectedValue(), wizard2.fillAndGetChainMap()));
		panel.add(doAdd);

		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Choose Animation", panel);
		tabbedPane.addTab("Map Nodes", editMappingPanel);
		FramePopup.show(tabbedPane, ProgramGlobals.getMainPanel(), "Add Animation");
	}

	private static boolean filterAnims(Animation animation, String text){
		return animation.getName().toLowerCase().contains(text.toLowerCase());
	}

	private static void doAddAnimation(ModelHandler modelHandler, String animSrcName, Component parent, Animation animation, Animation vis, Map<IdObject, IdObject> nodeMap) {
		HashMap<Sequence, Sequence> animMap = new HashMap<>();
		animMap.put(animation, animation);

		UndoAction importAction = getImportAction(modelHandler.getModel(), animMap, nodeMap);
		UndoAction setVisibilityAction = getSetVisibilityAction(modelHandler.getModel(), vis, animation);

		modelHandler.getUndoManager().pushAction(
				new CompoundAction("Add Single Animation", ModelStructureChangeListener.changeListener::animationParamsChanged,
						importAction,
						setVisibilityAction)
						.redo());


		JOptionPane.showMessageDialog(parent, "Added " + animSrcName + "'s " + animation.getName()
				+ " with " + vis.getName() + "'s visibility  OK!");
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
//			UndoAction action = new AddSequenceAction(modelHandler.getModel(), new GlobalSeq(1000 + modelHandler.getModel().getGlobalSeqs().size()), ModelStructureChangeListener.getModelStructureChangeListener());
//			modelHandler.getUndoManager().pushAction(action.redo());
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
//		newAnimationPanel.add(new JLabel("Start"));
//		JSpinner startSpinner = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
//		newAnimationPanel.add(startSpinner, "wrap");
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
//		List<Integer> startTimes = new ArrayList<>();
		List<Integer> animNumber = new ArrayList<>();
		List<Integer> lengths = new ArrayList<>();
		List<String> animationNames = new ArrayList<>();
        for (Animation a : currAnim) {
//	        startTimes.add(a.getStart());
	        animNumber.add(animNumber.size());
	        lengths.add(a.getLength());
	        animationNames.add(a.getName());
        }

        DefaultTableModel animationTableModel = new DefaultTableModel();
//		animationTableModel.addColumn("start", startTimes.toArray());
		animationTableModel.addColumn("", animNumber.toArray());
		animationTableModel.addColumn("name", animationNames.toArray());
		animationTableModel.addColumn("length", lengths.toArray());

        existingAnimationTable.setModel(animationTableModel);

//        JButton setStartAfter = new JButton("Start After");
//        setStartAfter.addActionListener(e -> {
//	        int length = (Integer) lengthSpinner.getValue();
//	        int newStart = ((Integer) existingAnimationTable.getValueAt(existingAnimationTable.getSelectedRow(), 1)) + 1;
//	        startSpinner.setValue(newStart);
//	        lengthSpinner.setValue(length);
//        });
//        JButton setEndBefore = new JButton("End Before");
////        setEndBefore.addActionListener(e -> lengthSpinner.setValue(existingAnimationTable.getValueAt(existingAnimationTable.getSelectedRow(), 0)));
//        setEndBefore.addActionListener(e -> {
//	        int duration = (Integer) lengthSpinner.getValue();
//	        int selectedRow = existingAnimationTable.getSelectedRow();
//	        int start = ((Integer) existingAnimationTable.getValueAt(selectedRow, 0)) - 1 - duration;
//	        startSpinner.setValue(start);
//        });
//
//		existingAnimationsPanel.add(setStartAfter);
//		existingAnimationsPanel.add(setEndBefore);

//        optionPane.setOptions();
		int option = JOptionPane.showConfirmDialog(ProgramGlobals.getMainPanel(), creationPanel, "Create Empty Animation", JOptionPane.OK_CANCEL_OPTION);
		System.out.println("option \"" + option + "\"");
//		int start = (Integer) startSpinner.getValue();
		int length = (Integer) lengthSpinner.getValue();
		if (option == 0) {
//			Animation animation = new Animation(nameField.getText(), start, length);
			Animation animation = new Animation(nameField.getText(), 0, length);
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
					addSingleAnimation(modelPanel.getModelHandler(), animationSource);
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
				addSingleAnimation(modelPanel.getModelHandler(), animationSource);
			}
		}
	}

	public static UndoAction getSetVisibilityAction(EditableModel model, Animation visibilitySource, Animation target) {
		List<UndoAction> undoActions = new ArrayList<>();
		List<VisibilitySource> allVisibilitySources = ModelUtils.getAllVis(model);
		for (VisibilitySource source : allVisibilitySources) {
			AnimFlag<Float> visibilityFlag = source.getVisibilityFlag();
			TreeMap<Integer, Entry<Float>> entryMapCopy = visibilityFlag.getSequenceEntryMapCopy(visibilitySource);
			if(entryMapCopy != null){
				undoActions.add(new SetFlagEntryMapAction<>(visibilityFlag, target, entryMapCopy, null));
			}
		}
		return new CompoundAction("Copy Visibility", undoActions, null);
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
			if(!recModel.contains(recSeq)){
				undoActions.add(new AddSequenceAction(recModel, recSeq, null));
			}
		}
		return new CompoundAction("import sub animation", undoActions, null);
	}
	private static UndoAction getImportIdObjectsAction(Map<Sequence, Sequence> recToDonSequenceMap, Map<IdObject, IdObject> chainMap) {
		List<UndoAction> undoActions = new ArrayList<>();

		System.out.println(chainMap.size());

		for (IdObject recIdObject : chainMap.keySet()) {
			IdObject donIdObject = chainMap.get(recIdObject);
			List<Pair<AnimFlag<?>, AnimFlag<?>>> flagPairs = getFlagPairs(recIdObject, donIdObject);

			for (Pair<AnimFlag<?>, AnimFlag<?>> flagPair : flagPairs) {
				AnimFlag<?> recAnimFlag = flagPair.getFirst();
				AnimFlag<?> donAnimFlag = flagPair.getSecond();

				if (recAnimFlag instanceof IntAnimFlag && donAnimFlag instanceof IntAnimFlag){
					addUndoActions(recToDonSequenceMap, undoActions, (IntAnimFlag) recAnimFlag, (IntAnimFlag) donAnimFlag);
				} else if (recAnimFlag instanceof BitmapAnimFlag && donAnimFlag instanceof BitmapAnimFlag){
					addUndoActions(recToDonSequenceMap, undoActions, (BitmapAnimFlag) recAnimFlag, (BitmapAnimFlag) donAnimFlag);
				} else if (recAnimFlag instanceof FloatAnimFlag && donAnimFlag instanceof FloatAnimFlag){
					addUndoActions(recToDonSequenceMap, undoActions, (FloatAnimFlag) recAnimFlag, (FloatAnimFlag) donAnimFlag);
				} else if (recAnimFlag instanceof Vec3AnimFlag && donAnimFlag instanceof Vec3AnimFlag){
					addUndoActions(recToDonSequenceMap, undoActions, (Vec3AnimFlag) recAnimFlag, (Vec3AnimFlag) donAnimFlag);
				} else if (recAnimFlag instanceof QuatAnimFlag && donAnimFlag instanceof QuatAnimFlag){
					addUndoActions(recToDonSequenceMap, undoActions, (QuatAnimFlag) recAnimFlag, (QuatAnimFlag) donAnimFlag);
				} else if (recAnimFlag == null){
					AnimFlag<?> newAnimFlag = donAnimFlag.getEmptyCopy();
					for (Sequence sequence : recToDonSequenceMap.keySet()) {
						AnimFlagUtils.copyFrom(newAnimFlag, donAnimFlag, recToDonSequenceMap.get(sequence), sequence);
					}
					undoActions.add(new AddAnimFlagAction<>(recIdObject, newAnimFlag, null));
				} else if (donAnimFlag == null){
					for (Sequence sequence : recToDonSequenceMap.keySet()) {
						undoActions.add(new RemoveFlagEntryMapAction<>(recAnimFlag, sequence, null));
					}
				}
			}
		}

		System.out.println("Done Importing! :O");
		return new CompoundAction("import sub animation", undoActions, null);
	}


	private static List<Pair<AnimFlag<?>, AnimFlag<?>>> getFlagPairs(IdObject recIdObject, IdObject donIdObject){
		List<Pair<AnimFlag<?>, AnimFlag<?>>> pairList = new ArrayList<>();
		if(recIdObject != null && donIdObject != null){
			ArrayList<AnimFlag<?>> recAnimFlags = recIdObject.getAnimFlags();
			ArrayList<AnimFlag<?>> donAnimFlags = donIdObject.getAnimFlags();

			Set<AnimFlag<?>> matchedFlags = new HashSet<>();
			for (AnimFlag<?> recAnimFlag : recAnimFlags){
				AnimFlag<?> donAnimFlag = donIdObject.find(recAnimFlag.getName());
				pairList.add(new Pair<>(recAnimFlag, donAnimFlag));
				matchedFlags.add(donAnimFlag);
			}

			for (AnimFlag<?> donAnimFlag : donAnimFlags){
				if(!matchedFlags.contains(donAnimFlag)){
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
