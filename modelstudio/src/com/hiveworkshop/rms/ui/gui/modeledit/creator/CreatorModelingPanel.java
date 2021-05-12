package com.hiveworkshop.rms.ui.gui.modeledit.creator;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.application.edit.animation.WrongModeException;
import com.hiveworkshop.rms.ui.application.edit.animation.mdlvisripoff.TSpline;
import com.hiveworkshop.rms.ui.application.edit.animation.mdlvisripoff.TTan;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ActivityDescriptor;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ModelEditorChangeActivityListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ModelEditorMultiManipulatorActivity;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.graphics2d.FaceCreationException;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.Viewport;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.ViewportListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.builder.model.SquatToolWidgetManipulatorBuilder;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ModeButton2;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ModelEditorActionType3;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ToolbarButtonGroup2;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.ui.util.ModeButton;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreatorModelingPanel extends JPanel implements ModelEditorChangeActivityListener {
	private static final String ANIMATIONBASICS = "ANIMATIONBASICS";

	private static final String MESH_BASICS = "MB";
	private static final String STANDARD_PRIMITIVES = "SP";
	private static final String EXTENDED_PRIMITIVES = "EP";
	private static final String ANIMATION_NODES = "AN";

	private final ModelEditorChangeActivityListener listener;
	private final List<ModeButton> modeButtons = new ArrayList<>();
	private boolean listeningForActivityChanges = true;
	private boolean animationModeState;
	private ModelEditorManager modelEditorManager;
	private UndoManager undoManager;
	private final ViewportListener viewportListener;
	private final ToolbarButtonGroup2<ModelEditorActionType3> actionTypeGroup;
	private final Map<ModelEditorActionType3, List<ModeButton>> typeToButtons = new HashMap<>();
	private final ProgramPreferences programPreferences;
	private final DefaultComboBoxModel<String> modeChooserBoxModel;
	private final DefaultComboBoxModel<ChooseableTimeRange> animationChooserBoxModel;
	private final JComboBox<String> modeChooserBox;
	private final JComboBox<ChooseableTimeRange> animationChooserBox;
	private final CardLayout cardLayout;
	private final JPanel cardPanel;
	private ModelHandler modelHandler;
	private final Map<Object, ChooseableTimeRange> thingToChooseableItem = new HashMap<>();
	private final CardLayout northCardLayout;
	private final JPanel northCardPanel;
	private TSpline tSpline;

	public CreatorModelingPanel(ModelEditorChangeActivityListener listener,
	                            ProgramPreferences programPreferences,
	                            ToolbarButtonGroup2<ModelEditorActionType3> actionTypeGroup,
	                            ViewportListener viewportListener,
	                            TimeEnvironmentImpl timeEnvironmentImpl) {
		this.listener = listener;
		this.programPreferences = programPreferences;
		this.actionTypeGroup = actionTypeGroup;
		this.viewportListener = viewportListener;

		setLayout(new BorderLayout());

		animationChooserBoxModel = new DefaultComboBoxModel<>();
		modeChooserBoxModel = new DefaultComboBoxModel<>();
		modeChooserBoxModel.addElement("Mesh Basics");
		modeChooserBoxModel.addElement("Standard Primitives");
		modeChooserBoxModel.addElement("Extended Primitives");
		modeChooserBoxModel.addElement("Animation Nodes");
		modeChooserBox = new JComboBox<>(modeChooserBoxModel);
		animationChooserBox = new JComboBox<>(animationChooserBoxModel);
		animationChooserBox.setVisible(false);
		ActionListener actionListener = e -> {
			ChooseableTimeRange selectedItem = (ChooseableTimeRange) animationChooserBox.getSelectedItem();
			if (selectedItem != null) {
				selectedItem.applyTo(getTimeEnv());
			}
		};
		animationChooserBox.addActionListener(actionListener);
		northCardLayout = new CardLayout();
		northCardPanel = new JPanel(northCardLayout);
		add(northCardPanel, BorderLayout.NORTH);
		northCardPanel.add(animationChooserBox, "ANIM");
		northCardPanel.add(modeChooserBox, "MESH");
		northCardLayout.show(northCardPanel, "MESH");

		cardLayout = new CardLayout();
		cardPanel = new JPanel(cardLayout);
		add(cardPanel, BorderLayout.CENTER);

		makeMeshBasicsPanel(listener, programPreferences, actionTypeGroup, viewportListener, modeChooserBoxModel, cardPanel);

		JPanel standardPrimitivesPanel = new JPanel(new BorderLayout());
		JPanel drawPrimitivesPanel = new JPanel(new GridLayout(16, 1));
		drawPrimitivesPanel.setBorder(BorderFactory.createTitledBorder("Draw"));

		ModeButton planeButton = new ModeButton("Plane");
//		planeButton.addActionListener(new ActionListenerImplementation(new DrawPlaneActivityDescriptor(viewportListener), programPreferences, listener, planeButton));//todo
		drawPrimitivesPanel.add(planeButton);
		modeButtons.add(planeButton);

		ModeButton boxButton = new ModeButton("Box");
//		boxButton.addActionListener(new ActionListenerImplementation(new DrawBoxActivityDescriptor(viewportListener), programPreferences, listener, boxButton));//todo
		drawPrimitivesPanel.add(boxButton);
		modeButtons.add(boxButton);

		JPanel spOptionsPanel = new JPanel(new GridLayout(16, 1));
		spOptionsPanel.setBorder(BorderFactory.createTitledBorder("Options"));
		standardPrimitivesPanel.add(drawPrimitivesPanel, BorderLayout.NORTH);
		standardPrimitivesPanel.add(spOptionsPanel, BorderLayout.CENTER);

		cardPanel.add(standardPrimitivesPanel, modeChooserBoxModel.getElementAt(1));

		modeChooserBox.addActionListener(e -> cardLayout.show(cardPanel, modeChooserBox.getSelectedItem().toString()));

		makeAnimationBasicsPanel(listener, programPreferences, actionTypeGroup, viewportListener, modeChooserBoxModel, cardPanel);

		cardLayout.show(cardPanel, modeChooserBoxModel.getElementAt(0));
	}

	private TimeEnvironmentImpl getTimeEnv(){
		if(modelHandler != null){
			return modelHandler.getEditTimeEnv();
		}
		return null;
	}

	public void makeMeshBasicsPanel(ModelEditorChangeActivityListener listener,
	                                ProgramPreferences programPrefences,
	                                ToolbarButtonGroup2<ModelEditorActionType3> actionTypeGroup,
	                                ViewportListener viewportListener,
	                                DefaultComboBoxModel<String> modeChooserBoxModel,
	                                JPanel cardPanel) {
		ModeButton vertexButton = new ModeButton("Vertex");
		ModeButton faceButton = new ModeButton("Face from Selection");
		ModeButton boneButton = new ModeButton("Bone");
		faceButton.addActionListener(e -> {
			if (modelEditorManager == null) {
				return;
			}
			try {
				Viewport viewport = viewportListener.getViewport();
				Vec3 facingVector = viewport == null ? new Vec3(0, 0, 1) : viewport.getFacingVector();
				UndoAction createFaceFromSelection = modelEditorManager.getModelEditor().createFaceFromSelection(facingVector);
				undoManager.pushAction(createFaceFromSelection);
			} catch (WrongModeException exc) {
				JOptionPane.showMessageDialog(CreatorModelingPanel.this,
						"Unable to create face, wrong selection mode", "Error", JOptionPane.ERROR_MESSAGE);
			} catch (FaceCreationException exc) {
				JOptionPane.showMessageDialog(CreatorModelingPanel.this, exc.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			}
		});
		JPanel drawToolsPanel = new JPanel(new GridLayout(2, 1));
		drawToolsPanel.setBorder(BorderFactory.createTitledBorder("Draw"));
		drawToolsPanel.add(vertexButton);
		drawToolsPanel.add(faceButton);
		drawToolsPanel.add(boneButton);

		JPanel meshBasicsPanel = new JPanel(new BorderLayout());
		meshBasicsPanel.add(drawToolsPanel, BorderLayout.NORTH);

		cardPanel.add(meshBasicsPanel, modeChooserBoxModel.getElementAt(0));

		JPanel editToolsPanel = new JPanel(new GridLayout(16, 1));
		editToolsPanel.setBorder(BorderFactory.createTitledBorder("Manipulate"));

//		for (ModelEditorActionType3 type : actionTypeGroup.getToolbarButtonTypes()) {
//			String typeName = type.getName();
//			ModeButton button = new ModeButton(typeName.substring(typeName.lastIndexOf(' ') + 1));
//			editToolsPanel.add(button);
//			button.addActionListener(e -> listener.changeActivity(type));
//			putTypeToButton(type, button);
//			modeButtons.add(button);
//		}
		for (ModeButton2 modeButton2 : actionTypeGroup.getModeButtons()) {
			editToolsPanel.add(modeButton2);
//			button.addActionListener(e -> listener.changeActivity(type));
//			putTypeToButton(type, button);
//			modeButtons.add(button);
		}

		meshBasicsPanel.add(editToolsPanel, BorderLayout.CENTER);

		vertexButton.addActionListener(e -> addVertex(listener, programPrefences, viewportListener, vertexButton));
		boneButton.addActionListener(e -> addBone(listener, programPrefences, viewportListener, boneButton));
		modeButtons.add(vertexButton);
		modeButtons.add(boneButton);
	}

	private void addBone(ModelEditorChangeActivityListener listener, ProgramPreferences programPrefences, ViewportListener viewportListener, ModeButton boneButton) {
		listeningForActivityChanges = false;
//		listener.changeActivity(new DrawBoneActivityDescriptor(viewportListener));//ToDo
//		resetButtons();
		boneButton.setColors(programPrefences.getActiveColor1(), programPrefences.getActiveColor2());
		listeningForActivityChanges = true;
	}

	private void addVertex(ModelEditorChangeActivityListener listener, ProgramPreferences programPrefences, ViewportListener viewportListener, ModeButton vertexButton) {
		listeningForActivityChanges = false;
//		listener.changeActivity(new DrawVertexActivityDescriptor(viewportListener));//ToDo
//		resetButtons();
		vertexButton.setColors(programPrefences.getActiveColor1(), programPrefences.getActiveColor2());
		listeningForActivityChanges = true;
	}

	public void makeAnimationBasicsPanel(ModelEditorChangeActivityListener listener,
	                                     ProgramPreferences programPreferences,
	                                     ToolbarButtonGroup2<ModelEditorActionType3> actionTypeGroup,
	                                     ViewportListener viewportListener, DefaultComboBoxModel<String> modeChooserBoxModel,
	                                     JPanel cardPanel) {
		JPanel meshBasicsPanel = new JPanel(new BorderLayout());
		cardPanel.add(meshBasicsPanel, ANIMATIONBASICS);
		JPanel editToolsPanel = new JPanel(new GridLayout(16, 1));
		editToolsPanel.setBorder(BorderFactory.createTitledBorder("Manipulate"));

		int index = 0;
		for (ModelEditorActionType3 type : actionTypeGroup.getToolbarButtonTypes()) {
			if (index < 3) {
				String typeName = type.getName();
				ModeButton button = new ModeButton(typeName.substring(typeName.lastIndexOf(' ') + 1));
				editToolsPanel.add(button);
				button.addActionListener(e -> listener.changeActivity(type));
				putTypeToButton(type, button);
				modeButtons.add(button);
			}
			index++;
		}
		ActivityDescriptor selectAndSquatDescriptor = (modelEditorManager, modelHandler) -> new ModelEditorMultiManipulatorActivity(
				new SquatToolWidgetManipulatorBuilder(modelEditorManager.getModelEditor(),
						modelEditorManager.getViewportSelectionHandler(), programPreferences, modelHandler.getModelView()),
				modelHandler.getUndoManager(), modelEditorManager.getSelectionView());
		String squatTypeName = "Squat";
		ModeButton squatButton = new ModeButton(squatTypeName);
		editToolsPanel.add(squatButton);
//		squatButton.addActionListener(e -> listener.changeActivity(selectAndSquatDescriptor));
//		putTypeToButton(selectAndSquatDescriptor, squatButton); //ToDo
		modeButtons.add(squatButton);

		if (false) {
			tSpline = new TSpline(new TTan());
			editToolsPanel.add(tSpline);
		}

		meshBasicsPanel.add(editToolsPanel, BorderLayout.CENTER);
	}

	private void putTypeToButton(ModelEditorActionType3 type, ModeButton button) {
		List<ModeButton> buttons = typeToButtons.computeIfAbsent(type, k -> new ArrayList<>());
		buttons.add(button);
	}

	public void setAnimationModeState(boolean animationModeState) {
		this.animationModeState = animationModeState;
		northCardLayout.show(northCardPanel, animationModeState ? "ANIM" : "MESH");
		if (animationModeState) {
			cardLayout.show(cardPanel, ANIMATIONBASICS);
		} else {
			cardLayout.show(cardPanel, modeChooserBox.getSelectedItem().toString());
		}
	}

	public void setModelEditorManager(ModelEditorManager modelEditorManager) {
		this.modelEditorManager = modelEditorManager;
	}

	public void setCurrentModel(ModelHandler modelHandler) {
		this.modelHandler = modelHandler;
		if (modelHandler != null) {
			reloadAnimationList();
		}
	}

	public void setChosenAnimation(Animation animation) {
		if (animation == null && animationChooserBox.getItemCount() > 0) {
			animationChooserBox.setSelectedIndex(0);
		} else if (animation != null) {
			animationChooserBox.setSelectedItem(thingToChooseableItem.get(animation));
		}
	}

	public void setChosenGlobalSeq(Integer globalSeq) {
		if (globalSeq == null) {
			animationChooserBox.setSelectedIndex(0);
		} else {
			animationChooserBox.setSelectedItem(thingToChooseableItem.get(globalSeq));
		}
	}

	public void reloadAnimationList() {
		ChooseableTimeRange selectedItem = (ChooseableTimeRange) animationChooserBox.getSelectedItem();
		animationChooserBoxModel.removeAllElements();
		Object thingSelected = selectedItem == null ? null : selectedItem.getThing();
		thingToChooseableItem.clear();
		boolean sawLast = selectedItem == null;
		ChooseableDoNothing doNothingItem = new ChooseableDoNothing("Custom Timeframe");
		animationChooserBoxModel.addElement(doNothingItem);
		thingToChooseableItem.put("Custom Timeframe", doNothingItem);

		for (Animation animation : modelHandler.getModel().getAnims()) {
			ChooseableAnimation choosableItem = new ChooseableAnimation(animation);
			thingToChooseableItem.put(animation, choosableItem);
			animationChooserBoxModel.addElement(choosableItem);
			if (animation == selectedItem) {
				sawLast = true;
			}
		}

		for (Integer integer : modelHandler.getModel().getGlobalSeqs()) {
			ChooseableGlobalSeq chooseableItem = new ChooseableGlobalSeq(integer);
			thingToChooseableItem.put(integer, chooseableItem);
			animationChooserBoxModel.addElement(chooseableItem);
		}

		if (sawLast && (selectedItem != null)) {
			animationChooserBox.setSelectedItem(thingToChooseableItem.get(thingSelected));
		}
	}

	@Override
	public void changeActivity(ModelEditorActionType3 newType) {
//		List<ModeButton> modeButtons = typeToButtons.get(newType);
//		if ((modeButtons != null) && !modeButtons.isEmpty()) {
////			resetButtons();
////			for (ModeButton modeButton : modeButtons) {
////				modeButton.setColors(programPreferences.getActiveColor1(), programPreferences.getActiveColor2());
////			}
//		} else {
//			if (listeningForActivityChanges) {
//				resetButtons();
//			}
//		}
	}

	public void setUndoManager(UndoManager undoManager) {
		this.undoManager = undoManager;
	}

	public void resetButtons() {
		for (ModeButton button : modeButtons) {
			button.resetColors();
		}
	}

	public interface ChooseableTimeRange {
		void applyTo(TimeEnvironmentImpl timeEnvironment);

		Object getThing();
	}

//	public void uggugg(){
//		new ActionListenerImplementation(new DrawBoxActivityDescriptor(viewportListener), programPreferences, listener, boxButton);
//	}



	private final class ActionListenerImplementation implements ActionListener {
		private final ModelEditorActionType3 activityDescriptor;
		private final ProgramPreferences programPreferences;
		private final ModelEditorChangeActivityListener listener;
		private final ModeButton modeButton;

		private ActionListenerImplementation(ModelEditorActionType3 activityDescriptor,
		                                     ProgramPreferences programPreferences,
		                                     ModelEditorChangeActivityListener listener,
		                                     ModeButton modeButton) {
			this.activityDescriptor = activityDescriptor;
			this.programPreferences = programPreferences;
			this.listener = listener;
			this.modeButton = modeButton;
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			listeningForActivityChanges = false;
			listener.changeActivity(activityDescriptor);
//			resetButtons();
//			modeButton.setColors(programPreferences.getActiveColor1(), programPreferences.getActiveColor2());
			listeningForActivityChanges = true;
		}
	}

}
