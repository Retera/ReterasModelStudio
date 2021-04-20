package com.hiveworkshop.rms.ui.gui.modeledit.creator;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeSliderTimeSelectionListener;
import com.hiveworkshop.rms.ui.application.edit.animation.WrongModeException;
import com.hiveworkshop.rms.ui.application.edit.animation.mdlvisripoff.TSpline;
import com.hiveworkshop.rms.ui.application.edit.animation.mdlvisripoff.TTan;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ActivityDescriptor;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ModelEditorChangeActivityListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ModelEditorMultiManipulatorActivity;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.graphics2d.FaceCreationException;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.Viewport;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.ViewportListener;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.creator.activity.DrawBoxActivityDescriptor;
import com.hiveworkshop.rms.ui.gui.modeledit.creator.activity.DrawPlaneActivityDescriptor;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.builder.model.SquatToolWidgetManipulatorBuilder;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ToolbarActionButtonType;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ToolbarButtonGroup;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.ui.util.ModeButton;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.*;

public class CreatorModelingPanel extends JPanel
		implements ModelEditorChangeActivityListener, TimeSliderTimeSelectionListener {
	private static final String ANIMATIONBASICS = "ANIMATIONBASICS";

	private final class ActionListenerImplementation implements ActionListener {
		private final ActivityDescriptor activityDescriptor;
		private final ProgramPreferences programPreferences;
		private final ModelEditorChangeActivityListener listener;
		private final ModeButton planeButton;

		private ActionListenerImplementation(final ActivityDescriptor activityDescriptor,
				final ProgramPreferences programPreferences, final ModelEditorChangeActivityListener listener,
				final ModeButton planeButton) {
			this.activityDescriptor = activityDescriptor;
			this.programPreferences = programPreferences;
			this.listener = listener;
			this.planeButton = planeButton;
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			listeningForActivityChanges = false;
			listener.changeActivity(activityDescriptor);
			resetButtons();
			planeButton.setColors(programPreferences.getActiveColor1(), programPreferences.getActiveColor2());
			listeningForActivityChanges = true;
		}
	}

	private static final String MESH_BASICS = "MB";
	private static final String STANDARD_PRIMITIVES = "SP";
	private static final String EXTENDED_PRIMITIVES = "EP";
	private static final String ANIMATION_NODES = "AN";

	private final ModelEditorChangeActivityListener listener;
	private final List<ModeButton> modeButtons = new ArrayList<>();
	private boolean listeningForActivityChanges = true;
	private boolean animationModeState;
	private ModelEditorManager modelEditorManager;
	private UndoActionListener undoActionListener;
	private final ViewportListener viewportListener;
	private final ToolbarButtonGroup<ToolbarActionButtonType> actionTypeGroup;
	private final Map<ActivityDescriptor, List<ModeButton>> typeToButtons = new HashMap<>();
	private final ProgramPreferences programPreferences;
	private final DefaultComboBoxModel<String> modeChooserBoxModel;
	private final DefaultComboBoxModel<ChooseableTimeRange> animationChooserBoxModel;
	private final JComboBox<String> modeChooserBox;
	private final JComboBox<ChooseableTimeRange> animationChooserBox;
	private final CardLayout cardLayout;
	private final JPanel cardPanel;
	private ModelView modelView;
	private final TimeEnvironmentImpl timeEnvironmentImpl;
	private final Map<Object, ChooseableTimeRange> thingToChooseableItem = new HashMap<>();
	private final CardLayout northCardLayout;
	private final JPanel northCardPanel;
	private TSpline tSpline;

	public CreatorModelingPanel(ModelEditorChangeActivityListener listener,
	                            ProgramPreferences programPreferences,
	                            ToolbarButtonGroup<ToolbarActionButtonType> actionTypeGroup,
	                            ViewportListener viewportListener,
	                            TimeEnvironmentImpl timeEnvironmentImpl) {
		this.listener = listener;
		this.programPreferences = programPreferences;
		this.actionTypeGroup = actionTypeGroup;
		this.viewportListener = viewportListener;
		this.timeEnvironmentImpl = timeEnvironmentImpl;

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
		animationChooserBox.addActionListener(e -> {
			ChooseableTimeRange selectedItem = (ChooseableTimeRange) animationChooserBox.getSelectedItem();
			if (selectedItem != null) {
				selectedItem.applyTo(timeEnvironmentImpl);
			}
		});
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
		planeButton.addActionListener(new ActionListenerImplementation(new DrawPlaneActivityDescriptor(programPreferences, viewportListener), programPreferences, listener, planeButton));
		drawPrimitivesPanel.add(planeButton);
		modeButtons.add(planeButton);

		ModeButton boxButton = new ModeButton("Box");
		boxButton.addActionListener(new ActionListenerImplementation(new DrawBoxActivityDescriptor(programPreferences, viewportListener), programPreferences, listener, boxButton));
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

	public void makeMeshBasicsPanel(ModelEditorChangeActivityListener listener,
	                                ProgramPreferences programPrefences,
	                                ToolbarButtonGroup<ToolbarActionButtonType> actionTypeGroup,
	                                ViewportListener viewportListener, DefaultComboBoxModel<String> modeChooserBoxModel,
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
				undoActionListener.pushAction(createFaceFromSelection);
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

		for (ToolbarActionButtonType type : actionTypeGroup.getToolbarButtonTypes()) {
			String typeName = type.getName();
			ModeButton button = new ModeButton(typeName.substring(typeName.lastIndexOf(' ') + 1));
			editToolsPanel.add(button);
			button.addActionListener(e -> listener.changeActivity(type));
			putTypeToButton(type, button);
			modeButtons.add(button);
		}

		meshBasicsPanel.add(editToolsPanel, BorderLayout.CENTER);

		vertexButton.addActionListener(e -> addVertex(listener, programPrefences, viewportListener, vertexButton));
		boneButton.addActionListener(e -> addBone(listener, programPrefences, viewportListener, boneButton));
		modeButtons.add(vertexButton);
		modeButtons.add(boneButton);
	}

	private void addBone(ModelEditorChangeActivityListener listener, ProgramPreferences programPrefences, ViewportListener viewportListener, ModeButton boneButton) {
		listeningForActivityChanges = false;
		listener.changeActivity(new DrawBoneActivityDescriptor(programPrefences, viewportListener));
		resetButtons();
		boneButton.setColors(programPrefences.getActiveColor1(), programPrefences.getActiveColor2());
		listeningForActivityChanges = true;
	}

	private void addVertex(ModelEditorChangeActivityListener listener, ProgramPreferences programPrefences, ViewportListener viewportListener, ModeButton vertexButton) {
		listeningForActivityChanges = false;
		listener.changeActivity(new DrawVertexActivityDescriptor(programPrefences, viewportListener));
		resetButtons();
		vertexButton.setColors(programPrefences.getActiveColor1(), programPrefences.getActiveColor2());
		listeningForActivityChanges = true;
	}

	public void makeAnimationBasicsPanel(ModelEditorChangeActivityListener listener,
	                                     ProgramPreferences programPreferences,
	                                     ToolbarButtonGroup<ToolbarActionButtonType> actionTypeGroup,
	                                     ViewportListener viewportListener, DefaultComboBoxModel<String> modeChooserBoxModel,
	                                     JPanel cardPanel) {
		JPanel meshBasicsPanel = new JPanel(new BorderLayout());
		cardPanel.add(meshBasicsPanel, ANIMATIONBASICS);
		JPanel editToolsPanel = new JPanel(new GridLayout(16, 1));
		editToolsPanel.setBorder(BorderFactory.createTitledBorder("Manipulate"));

		int index = 0;
		for (ToolbarActionButtonType type : actionTypeGroup.getToolbarButtonTypes()) {
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
		ActivityDescriptor selectAndSquatDescriptor = (modelEditorManager, modelView, undoActionListener) -> new ModelEditorMultiManipulatorActivity(new SquatToolWidgetManipulatorBuilder(modelEditorManager.getModelEditor(), modelEditorManager.getViewportSelectionHandler(), programPreferences, modelView), undoActionListener, modelEditorManager.getSelectionView());
		String squatTypeName = "Squat";
		ModeButton squatButton = new ModeButton(squatTypeName);
		editToolsPanel.add(squatButton);
		squatButton.addActionListener(e -> listener.changeActivity(selectAndSquatDescriptor));
		putTypeToButton(selectAndSquatDescriptor, squatButton);
		modeButtons.add(squatButton);

		if (false) {
			tSpline = new TSpline(new TTan());
			editToolsPanel.add(tSpline);
		}

		meshBasicsPanel.add(editToolsPanel, BorderLayout.CENTER);
	}

	private void putTypeToButton(ActivityDescriptor type, ModeButton button) {
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

	public void setCurrentModel(ModelView modelView) {
		this.modelView = modelView;
		if (modelView != null) {
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

		for (Animation animation : modelView.getModel().getAnims()) {
			ChooseableAnimation choosableItem = new ChooseableAnimation(animation);
			thingToChooseableItem.put(animation, choosableItem);
			animationChooserBoxModel.addElement(choosableItem);
			if (animation == selectedItem) {
				sawLast = true;
			}
		}

		for (Integer integer : modelView.getModel().getGlobalSeqs()) {
			ChooseableGlobalSeq chooseableItem = new ChooseableGlobalSeq(integer);
			thingToChooseableItem.put(integer, chooseableItem);
			animationChooserBoxModel.addElement(chooseableItem);
		}

		if (sawLast && (selectedItem != null)) {
			animationChooserBox.setSelectedItem(thingToChooseableItem.get(thingSelected));
		}
	}

	@Override
	public void changeActivity(ActivityDescriptor newType) {
		List<ModeButton> modeButtons = typeToButtons.get(newType);
		if ((modeButtons != null) && !modeButtons.isEmpty()) {
			resetButtons();
			for (ModeButton modeButton : modeButtons) {
				modeButton.setColors(programPreferences.getActiveColor1(), programPreferences.getActiveColor2());
			}
		} else {
			if (listeningForActivityChanges) {
				resetButtons();
			}
		}
	}

	public void setUndoManager(UndoActionListener undoManager) {
		undoActionListener = undoManager;
	}

	public void resetButtons() {
		for (ModeButton button : modeButtons) {
			button.resetColors();
		}
	}

	private interface ChooseableTimeRange {
		void applyTo(TimeEnvironmentImpl timeEnvironment);

		Object getThing();
	}

	private static final class ChooseableAnimation implements ChooseableTimeRange {
		private final Animation animation;

		public ChooseableAnimation(Animation animation) {
			this.animation = animation;
		}

		@Override
		public void applyTo(TimeEnvironmentImpl timeEnvironment) {
			timeEnvironment.setBounds(animation);
		}

		@Override
		public String toString() {
			return animation.getName();
		}

		@Override
		public Object getThing() {
			return animation;
		}

		@Override
		public int hashCode() {
			int prime = 31;
			int result = 1;
			result = (prime * result) + (animation == null ? 0 : animation.hashCode());
			return result;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			ChooseableAnimation other = (ChooseableAnimation) obj;
			if (animation == null) {
				return other.animation == null;
			} else {
				return animation.equals(other.animation);
			}
		}
	}

	private static final class ChooseableDoNothing implements ChooseableTimeRange {
		private final String text;

		public ChooseableDoNothing(String text) {
			this.text = text;
		}

		@Override
		public void applyTo(TimeEnvironmentImpl timeEnvironment) {
		}

		@Override
		public String toString() {
			return text;
		}

		@Override
		public Object getThing() {
			return text;
		}

		@Override
		public int hashCode() {
			int prime = 31;
			int result = 1;
			result = (prime * result) + (text == null ? 0 : text.hashCode());
			return result;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			ChooseableDoNothing other = (ChooseableDoNothing) obj;
			if (text == null) {
				return other.text == null;
			} else {
				return text.equals(other.text);
			}
		}
	}

	private static final class ChooseableGlobalSeq implements ChooseableTimeRange {
		private final Integer globalSeq;

		public ChooseableGlobalSeq(Integer globalSeq) {
			this.globalSeq = globalSeq;
		}

		@Override
		public void applyTo(TimeEnvironmentImpl timeEnvironment) {
			timeEnvironment.setGlobalSeq(globalSeq);
		}

		@Override
		public String toString() {
			return globalSeq.toString();
		}

		@Override
		public Object getThing() {
			return globalSeq;
		}

		@Override
		public int hashCode() {
			int prime = 31;
			int result = 1;
			result = (prime * result) + (globalSeq == null ? 0 : globalSeq.hashCode());
			return result;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			ChooseableGlobalSeq other = (ChooseableGlobalSeq) obj;
			if (globalSeq == null) {
				return other.globalSeq == null;
			} else {
				return globalSeq.equals(other.globalSeq);
			}
		}

	}

	@Override
	public void timeChanged(final int currentTime, final Set<IdObject> objects, final List<AnimFlag<?>> timelines) {
//		tSpline.setSelection(currentTime);
	}

}
