package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.AddEventTrackAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.AddFlagEntryAction;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.RemoveFlagEntryAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.EventObject;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.AddSingleAnimationActions;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.actionfunctions.ActionFunction;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.SequenceComboBoxRenderer;
import com.hiveworkshop.rms.ui.language.TextKey;
import com.hiveworkshop.rms.util.FramePopup;
import com.hiveworkshop.rms.util.TwiComboBox;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class KeyframeCopyPanel extends JPanel {
	private final ModelHandler modelHandler;
	private final String RRW = "\u23EE"; // ⏮
	private final String FFW = "\u23ED"; // ⏭
	private JSpinner donTimeSpinner;
	private JSpinner donTimeEndSpinner;
	private JSpinner recTimeSpinner;

	private Animation donAnim;
	private Animation recAnim;

	boolean onlySelectedNodes = false;

	/**
	 * Create the panel.
	 */
	public KeyframeCopyPanel(final ModelHandler modelHandler) {
		this.modelHandler = modelHandler;
		setLayout(new MigLayout("fill", "[grow][grow]"));

		JTextArea info = new JTextArea("Copies all keyframes from chosen interval in source animation into destination animation starting at specified frame.");
		info.setEditable(false);
		info.setOpaque(false);
		info.setLineWrap(true);
		info.setWrapStyleWord(true);
		add(info, "spanx, growx, wrap");
//		JTextArea warning = new JTextArea("WARNING: Make sure that the copied interval fits within the destination animation.");
		JTextArea warning = new JTextArea("Note: The destination interval might exceed the animation length. These keyframes will be pruned from the saved model.");
		warning.setEditable(false);
		warning.setOpaque(false);
		warning.setLineWrap(true);
		warning.setWrapStyleWord(true);
		add(warning, "spanx, growx, wrap");

		JButton addAnimButton = new JButton("add new animation");
		addAnimButton.addActionListener(e -> AddSingleAnimationActions.addEmptyAnimation(this.modelHandler));
		add(addAnimButton, "skip, right, wrap");

		JCheckBox onlySelected = new JCheckBox("Only selected nodes");
		onlySelected.addActionListener(e -> onlySelectedNodes = onlySelected.isSelected());
		add(onlySelected, "wrap");

		JPanel donAnimPanel = getDonAnimPanel();
		add(donAnimPanel, "growx, aligny top");

//		donAnimPanel.add(new JButton("\u23E9"));

		JPanel recAnimPanel = getRecAnimPanel();
		add(recAnimPanel, "growx, wrap, aligny top");

		JButton copyButton = getButton(e -> doCopy(), "Copy Keyframe");
		add(copyButton, "spanx, align center, wrap");
	}

	public static void showPanel() {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			KeyframeCopyPanel copyPanel = new KeyframeCopyPanel(modelPanel.getModelHandler());
			FramePopup.show(copyPanel, ProgramGlobals.getMainPanel(), "Copy Keyframes");
		}
	}

	private JPanel getRecAnimPanel() {
		JPanel recAnimPanel = new JPanel(new MigLayout("fill, gap 0"));
		recAnimPanel.add(new JLabel("To:"), "wrap");

		List<Animation> animations = modelHandler.getModel().getAnims();
		recAnim = animations.get(0);
		recTimeSpinner = new JSpinner(getAnimModel(recAnim));
		TwiComboBox<Animation> recAnimBox = getAnimChooser(animations, this::recAnimChosen);

		recAnimPanel.add(recAnimBox, "growx, gapbottom 5, wrap");
		recAnimPanel.add(recTimeSpinner, "wrap");

		recAnimPanel.add(getSpinnerPanel("Start", recTimeSpinner, () -> 0, () -> recAnim.getLength()), "growx, wrap");
		return recAnimPanel;
	}

	private JPanel getDonAnimPanel() {
		JPanel donAnimPanel = new JPanel(new MigLayout("fill, gap 0"));
		donAnimPanel.add(new JLabel("From:"), "wrap");

		List<Animation> animations = modelHandler.getModel().getAnims();
		donAnim = animations.get(0);
		donTimeSpinner = new JSpinner(getAnimModel(donAnim));
		donTimeEndSpinner = new JSpinner(getAnimModel(donAnim));
		TwiComboBox<Animation> donAnimBox = getAnimChooser(animations, this::donAnimChoosen);

		donAnimPanel.add(donAnimBox, "growx, gapbottom 5, wrap");
		donAnimPanel.add(getSpinnerPanel("Start", donTimeSpinner, () -> 0, () -> donAnim.getLength()), "growx, wrap");
		donAnimPanel.add(getSpinnerPanel("End", donTimeEndSpinner, () -> 0, () -> donAnim.getLength()), "growx, wrap");
		return donAnimPanel;
	}

	private JPanel getSpinnerPanel(String title, JSpinner spinner,
	                               Supplier<Integer> startSupplier, Supplier<Integer> endSupplier) {
		JPanel panel = new JPanel(new MigLayout("fill, gap 0, ins 0", "[5%:10%:10%]10[grow][][]"));
		panel.add(new JLabel(title));
		panel.add(spinner, "growx");
		panel.add(getButton(e -> spinner.setValue(startSupplier.get()), RRW));
		panel.add(getButton(e -> spinner.setValue(endSupplier.get()), FFW));
		return panel;
	}

	private JButton getButton(ActionListener actionListener, String s) {
		JButton button = new JButton(s);
		button.addActionListener(actionListener);
		return button;
	}

	private TwiComboBox<Animation> getAnimChooser(List<Animation> animations, Consumer<Animation> donAnimChoosen) {
		TwiComboBox<Animation> donAnimBox = new TwiComboBox<>(animations, new Animation("Stand and Work for me", 0, 1));
		donAnimBox.setRenderer(new SequenceComboBoxRenderer(modelHandler));
		donAnimBox.addOnSelectItemListener(donAnimChoosen);
		return donAnimBox;
	}


	private void recAnimChosen(Animation anim) {
		if(anim != null){
			recAnim = anim;
			recTimeSpinner.setModel(getAnimModel(recAnim));
		}
	}

	private void donAnimChoosen(Animation anim) {
		if(donAnim != null){
			donAnim = anim;
			donTimeSpinner.setModel(getAnimModel(donAnim));
			donTimeEndSpinner.setModel(getAnimModel(donAnim));
		}
	}

	private SpinnerNumberModel getAnimModel(Animation animation) {
		return new SpinnerNumberModel(0, 0, animation.getLength(), 1);
	}

	private void doCopy() {
		Integer donStart = (Integer) donTimeSpinner.getValue();
		Integer recStart = (Integer) recTimeSpinner.getValue();
		int times = (Integer) donTimeEndSpinner.getValue() - donStart + 1;
		if(onlySelectedNodes){
			copyKeyframeSelected(donAnim, donStart, recAnim, recStart, times);
		} else {
			copyKeyframe(donAnim, donStart, recAnim, recStart, times);
		}
	}

	private void copyKeyframe(Animation donAnimation, int donKeyframe, Animation recAnimation, int recKeyframe, int times) {
		EditableModel model = modelHandler.getModel();

		List<IdObject> idObjects = model.getIdObjects();
		List<Geoset> geosets = model.getGeosets();
		List<Camera> cameras = model.getCameras();
		List<EventObject> events = model.getEvents();

		copyKeyframes(donAnimation, donKeyframe, recAnimation, recKeyframe, times, model, idObjects, geosets, cameras, events);
	}

	private void copyKeyframeSelected(Animation donAnimation, int donKeyframe, Animation recAnimation, int recKeyframe, int times) {
		EditableModel model = modelHandler.getModel();
		ModelView modelView = modelHandler.getModelView();

		Set<IdObject> idObjects = modelView.getSelectedIdObjects();
		Set<Geoset> geosets = modelView.getEditableGeosets();
		Set<Camera> cameras = modelView.getSelectedCameras();
		List<EventObject> eventObjects = idObjects.stream().filter(o -> o instanceof EventObject).map(o -> (EventObject)o).collect(Collectors.toList());

		copyKeyframes(donAnimation, donKeyframe, recAnimation, recKeyframe, times, model, idObjects, geosets, cameras, eventObjects);
	}


	private void copyKeyframes(Animation donAnimation, int donKeyframe,
	                           Animation recAnimation, int recKeyframe, int times,
	                           EditableModel model,
	                           Collection<IdObject> idObjects,
	                           Collection<Geoset> geosets,
	                           Collection<Camera> cameras,
	                           Collection<EventObject> events) {
		List<UndoAction> undoActions = new ArrayList<>();

		for (IdObject idObject : idObjects) {
			ArrayList<AnimFlag<?>> animFlags = idObject.getAnimFlags();
			undoActions.addAll(getSetKeyframesAction(donAnimation, donKeyframe, recAnimation, recKeyframe, times, animFlags));
		}
		for(Material material : model.getMaterials()){
			for(Layer layer : material.getLayers()){
				ArrayList<AnimFlag<?>> animFlags = layer.getAnimFlags();
				undoActions.addAll(getSetKeyframesAction(donAnimation, donKeyframe, recAnimation, recKeyframe, times, animFlags));
			}
		}

		for(Geoset geoset : geosets){
			ArrayList<AnimFlag<?>> animFlags = geoset.getAnimFlags();
			undoActions.addAll(getSetKeyframesAction(donAnimation, donKeyframe, recAnimation, recKeyframe, times, animFlags));
		}
		for (TextureAnim textureAnim : model.getTexAnims()){
			ArrayList<AnimFlag<?>> animFlags = textureAnim.getAnimFlags();
			undoActions.addAll(getSetKeyframesAction(donAnimation, donKeyframe, recAnimation, recKeyframe, times, animFlags));
		}
		for (Camera camera : cameras){
			ArrayList<AnimFlag<?>> animFlags1 = camera.getSourceNode().getAnimFlags();
			undoActions.addAll(getSetKeyframesAction(donAnimation, donKeyframe, recAnimation, recKeyframe, times, animFlags1));
			ArrayList<AnimFlag<?>> animFlags2 = camera.getTargetNode().getAnimFlags();
			undoActions.addAll(getSetKeyframesAction(donAnimation, donKeyframe, recAnimation, recKeyframe, times, animFlags2));
		}
		for (EventObject eventObject : events){
			TreeSet<Integer> eventTrack = eventObject.getEventTrack(donAnimation);
			if(eventTrack != null){
				ArrayList<Integer> newTracks = new ArrayList<>();
				for(Integer time : eventTrack){
					int newTime = time - donKeyframe + recKeyframe;
					if(0 <= newTime && newTime < donKeyframe + times -1){
						newTracks.add(newTime);
					}
				}
				if(!newTracks.isEmpty()){
					undoActions.add(new AddEventTrackAction(eventObject, recAnimation, newTracks, ModelStructureChangeListener.changeListener));
				}
			}
		}

		if(!undoActions.isEmpty()){
			modelHandler.getUndoManager().pushAction(new CompoundAction("Copy keyframes", undoActions, ModelStructureChangeListener.changeListener::keyframesUpdated).redo());
		}
	}

	private List<UndoAction> getSetKeyframesAction(Animation donAnimation, int donKeyframe, Animation recAnimation, int recKeyframe, int times, ArrayList<AnimFlag<?>> animFlags) {
		List<UndoAction> undoActions = new ArrayList<>();
		for (AnimFlag<?> animFlag : animFlags) {
			undoActions.addAll(getAnimFlagKeyframeActions(donAnimation, donKeyframe, recAnimation, recKeyframe, times, animFlag));
		}
		return undoActions;
	}

	private <T> List<UndoAction> getAnimFlagKeyframeActions(Animation donAnimation, int donKeyframe, Animation recAnimation, int recKeyframe, int times, AnimFlag<T> animFlag) {
		List<UndoAction> undoActions = new ArrayList<>();
		List<Integer> removeTimes = new ArrayList<>();
		List<Entry<T>> addEntries = new ArrayList<>();
		int donAdj = 1;
		if(times < 0){
			donAdj = -1;
		}
		for (int j = 0; j < Math.abs(times); j++) {
			if(animFlag.hasEntryAt(recAnimation, recKeyframe + j)){
				removeTimes.add(recKeyframe + j);
			}
			if(animFlag.hasEntryAt(donAnimation, donKeyframe + j*donAdj)){
				addEntries.add(animFlag.getEntryAt(donAnim, donKeyframe + j*donAdj).deepCopy().setTime(recKeyframe + j));
			}
		}
		if(!removeTimes.isEmpty()){
			undoActions.add(new RemoveFlagEntryAction<>(animFlag, removeTimes, recAnimation, null));
		}
		if(!addEntries.isEmpty()){
			undoActions.add(new AddFlagEntryAction<>(animFlag, addEntries, recAnimation, null));
		}
		return undoActions;
	}



	private static class CopyKeyframes extends ActionFunction {
		CopyKeyframes(){
			super(TextKey.COPY_KFS_BETWEEN_ANIMS, () -> KeyframeCopyPanel.showPanel());
			setMenuItemMnemonic(KeyEvent.VK_K);
		}
	}

	public static JMenuItem getMenuItem(){
		return new CopyKeyframes().getMenuItem();
	}
}
