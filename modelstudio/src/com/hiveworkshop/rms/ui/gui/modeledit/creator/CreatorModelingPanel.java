package com.hiveworkshop.rms.ui.gui.modeledit.creator;

import com.hiveworkshop.rms.editor.model.AnimFlag;
import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.IdObject;
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
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.ActiveViewportWatcher;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.Viewport;
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
	private final ActiveViewportWatcher activeViewportWatcher;
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

	public CreatorModelingPanel(final ModelEditorChangeActivityListener listener,
			final ProgramPreferences programPreferences,
			final ToolbarButtonGroup<ToolbarActionButtonType> actionTypeGroup,
			final ActiveViewportWatcher activeViewportWatcher, final TimeEnvironmentImpl timeEnvironmentImpl) {
		this.listener = listener;
		this.programPreferences = programPreferences;
		this.actionTypeGroup = actionTypeGroup;
		this.activeViewportWatcher = activeViewportWatcher;
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
            final ChooseableTimeRange selectedItem = (ChooseableTimeRange) animationChooserBox.getSelectedItem();
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

		makeMeshBasicsPanel(listener, programPreferences, actionTypeGroup, activeViewportWatcher, modeChooserBoxModel,
				cardPanel);

		final JPanel standardPrimitivesPanel = new JPanel(new BorderLayout());
		final JPanel drawPrimitivesPanel = new JPanel(new GridLayout(16, 1));
		drawPrimitivesPanel.setBorder(BorderFactory.createTitledBorder("Draw"));
		final ModeButton planeButton = new ModeButton("Plane");
		planeButton.addActionListener(new ActionListenerImplementation(
				new DrawPlaneActivityDescriptor(programPreferences, activeViewportWatcher), programPreferences,
				listener, planeButton));
		modeButtons.add(planeButton);
		drawPrimitivesPanel.add(planeButton);
		final ModeButton boxButton = new ModeButton("Box");
		boxButton.addActionListener(new ActionListenerImplementation(
				new DrawBoxActivityDescriptor(programPreferences, activeViewportWatcher), programPreferences, listener,
				boxButton));
		modeButtons.add(boxButton);
		drawPrimitivesPanel.add(boxButton);
		final JPanel spOptionsPanel = new JPanel(new GridLayout(16, 1));
		spOptionsPanel.setBorder(BorderFactory.createTitledBorder("Options"));
		standardPrimitivesPanel.add(drawPrimitivesPanel, BorderLayout.NORTH);
		standardPrimitivesPanel.add(spOptionsPanel, BorderLayout.CENTER);

		cardPanel.add(standardPrimitivesPanel, modeChooserBoxModel.getElementAt(1));

		modeChooserBox.addActionListener(e -> cardLayout.show(cardPanel, modeChooserBox.getSelectedItem().toString()));

		makeAnimationBasicsPanel(listener, programPreferences, actionTypeGroup, activeViewportWatcher,
				modeChooserBoxModel, cardPanel);

		cardLayout.show(cardPanel, modeChooserBoxModel.getElementAt(0));
	}

	public void makeMeshBasicsPanel(final ModelEditorChangeActivityListener listener,
			final ProgramPreferences programPrefences,
			final ToolbarButtonGroup<ToolbarActionButtonType> actionTypeGroup,
			final ActiveViewportWatcher activeViewportWatcher, final DefaultComboBoxModel<String> modeChooserBoxModel,
			final JPanel cardPanel) {
		final ModeButton vertexButton = new ModeButton("Vertex");
		final ModeButton faceButton = new ModeButton("Face from Selection");
		final ModeButton boneButton = new ModeButton("Bone");
		faceButton.addActionListener(e -> {
            if (modelEditorManager == null) {
                return;
            }
            try {
                final Viewport viewport = activeViewportWatcher.getViewport();
                final Vec3 facingVector = viewport == null ? new Vec3(0, 0, 1) : viewport.getFacingVector();
                final UndoAction createFaceFromSelection = modelEditorManager
                        .getModelEditor().createFaceFromSelection(facingVector);
                undoActionListener.pushAction(createFaceFromSelection);
            } catch (final WrongModeException exc) {
                JOptionPane.showMessageDialog(CreatorModelingPanel.this,
                        "Unable to create face, wrong selection mode", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (final FaceCreationException exc) {
                JOptionPane.showMessageDialog(CreatorModelingPanel.this, exc.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
		final JPanel drawToolsPanel = new JPanel(new GridLayout(2, 1));
		drawToolsPanel.setBorder(BorderFactory.createTitledBorder("Draw"));
		drawToolsPanel.add(vertexButton);
		drawToolsPanel.add(faceButton);
		drawToolsPanel.add(boneButton);
		final JPanel meshBasicsPanel = new JPanel(new BorderLayout());
		cardPanel.add(meshBasicsPanel, modeChooserBoxModel.getElementAt(0));

		meshBasicsPanel.add(drawToolsPanel, BorderLayout.NORTH);

		final JPanel editToolsPanel = new JPanel(new GridLayout(16, 1));
		editToolsPanel.setBorder(BorderFactory.createTitledBorder("Manipulate"));

		for (final ToolbarActionButtonType type : actionTypeGroup.getToolbarButtonTypes()) {
			final String typeName = type.getName();
			final ModeButton button = new ModeButton(typeName.substring(typeName.lastIndexOf(' ') + 1));
			editToolsPanel.add(button);
			button.addActionListener(e -> listener.changeActivity(type));
			putTypeToButton(type, button);
			modeButtons.add(button);
		}

		meshBasicsPanel.add(editToolsPanel, BorderLayout.CENTER);

		vertexButton.addActionListener(e -> {
            listeningForActivityChanges = false;
            listener.changeActivity(new DrawVertexActivityDescriptor(programPrefences, activeViewportWatcher));
            resetButtons();
            vertexButton.setColors(programPrefences.getActiveColor1(), programPrefences.getActiveColor2());
            listeningForActivityChanges = true;
        });
		boneButton.addActionListener(e -> {
            listeningForActivityChanges = false;
            listener.changeActivity(new DrawBoneActivityDescriptor(programPrefences, activeViewportWatcher));
            resetButtons();
            boneButton.setColors(programPrefences.getActiveColor1(), programPrefences.getActiveColor2());
            listeningForActivityChanges = true;
        });
		modeButtons.add(vertexButton);
		modeButtons.add(boneButton);
	}

	public void makeAnimationBasicsPanel(final ModelEditorChangeActivityListener listener,
			final ProgramPreferences programPreferences,
			final ToolbarButtonGroup<ToolbarActionButtonType> actionTypeGroup,
			final ActiveViewportWatcher activeViewportWatcher, final DefaultComboBoxModel<String> modeChooserBoxModel,
			final JPanel cardPanel) {
		final JPanel meshBasicsPanel = new JPanel(new BorderLayout());
		cardPanel.add(meshBasicsPanel, ANIMATIONBASICS);
		final JPanel editToolsPanel = new JPanel(new GridLayout(16, 1));
		editToolsPanel.setBorder(BorderFactory.createTitledBorder("Manipulate"));

		int index = 0;
		for (final ToolbarActionButtonType type : actionTypeGroup.getToolbarButtonTypes()) {
			if (index < 3) {
				final String typeName = type.getName();
				final ModeButton button = new ModeButton(typeName.substring(typeName.lastIndexOf(' ') + 1));
				editToolsPanel.add(button);
				button.addActionListener(e -> listener.changeActivity(type));
				putTypeToButton(type, button);
				modeButtons.add(button);
			}
			index++;
		}
		final ActivityDescriptor selectAndSquatDescriptor = (modelEditorManager, modelView, undoActionListener) -> new ModelEditorMultiManipulatorActivity(
                new SquatToolWidgetManipulatorBuilder(modelEditorManager.getModelEditor(),
                        modelEditorManager.getViewportSelectionHandler(), programPreferences, modelView),
                undoActionListener, modelEditorManager.getSelectionView());
		final String squatTypeName = "Squat";
		final ModeButton squatButton = new ModeButton(squatTypeName);
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

	private void putTypeToButton(final ActivityDescriptor type, final ModeButton button) {
		List<ModeButton> buttons = typeToButtons.computeIfAbsent(type, k -> new ArrayList<>());
		buttons.add(button);
	}

	public void setAnimationModeState(final boolean animationModeState) {
		this.animationModeState = animationModeState;
		northCardLayout.show(northCardPanel, animationModeState ? "ANIM" : "MESH");
		if (animationModeState) {
			cardLayout.show(cardPanel, ANIMATIONBASICS);
		} else {
			cardLayout.show(cardPanel, modeChooserBox.getSelectedItem().toString());
		}
	}

	public void setModelEditorManager(final ModelEditorManager modelEditorManager) {
		this.modelEditorManager = modelEditorManager;
	}

	public void setCurrentModel(final ModelView modelView) {
		this.modelView = modelView;
		if (modelView != null) {
			reloadAnimationList();
		}
	}

	public void setChosenAnimation(final Animation animation) {
		if (animation == null) {
			animationChooserBox.setSelectedIndex(0);
		} else {
			animationChooserBox.setSelectedItem(thingToChooseableItem.get(animation));
		}
	}

	public void setChosenGlobalSeq(final Integer globalSeq) {
		if (globalSeq == null) {
			animationChooserBox.setSelectedIndex(0);
		} else {
			animationChooserBox.setSelectedItem(thingToChooseableItem.get(globalSeq));
		}
	}

	public void reloadAnimationList() {
		final ChooseableTimeRange selectedItem = (ChooseableTimeRange) animationChooserBox.getSelectedItem();
		animationChooserBoxModel.removeAllElements();
		final Object thingSelected = selectedItem == null ? null : selectedItem.getThing();
		thingToChooseableItem.clear();
		boolean sawLast = selectedItem == null;
		final ChooseableDoNothing doNothingItem = new ChooseableDoNothing("Custom Timeframe");
		animationChooserBoxModel.addElement(doNothingItem);
		thingToChooseableItem.put("Custom Timeframe", doNothingItem);
		for (final Animation animation : modelView.getModel().getAnims()) {
			final ChooseableAnimation choosableItem = new ChooseableAnimation(animation);
			thingToChooseableItem.put(animation, choosableItem);
			animationChooserBoxModel.addElement(choosableItem);
			if (animation == selectedItem) {
				sawLast = true;
			}
		}
		for (final Integer integer : modelView.getModel().getGlobalSeqs()) {
			final ChooseableGlobalSeq chooseableItem = new ChooseableGlobalSeq(integer);
			thingToChooseableItem.put(integer, chooseableItem);
			animationChooserBoxModel.addElement(chooseableItem);
		}
		if (sawLast && (selectedItem != null)) {
			animationChooserBox.setSelectedItem(thingToChooseableItem.get(thingSelected));
		}
	}

	@Override
	public void changeActivity(final ActivityDescriptor newType) {
		final List<ModeButton> modeButtons = typeToButtons.get(newType);
		if ((modeButtons != null) && !modeButtons.isEmpty()) {
			resetButtons();
			for (final ModeButton modeButton : modeButtons) {
				modeButton.setColors(programPreferences.getActiveColor1(), programPreferences.getActiveColor2());
			}
		} else {
			if (listeningForActivityChanges) {
				resetButtons();
			}
		}
	}

	public void setUndoManager(final UndoActionListener undoManager) {
		undoActionListener = undoManager;
	}

	public void resetButtons() {
		for (final ModeButton button : modeButtons) {
			button.resetColors();
		}
	}

	private interface ChooseableTimeRange {
		void applyTo(TimeEnvironmentImpl timeEnvironment);

		Object getThing();
	}

	private static final class ChooseableAnimation implements ChooseableTimeRange {
		private final Animation animation;

		public ChooseableAnimation(final Animation animation) {
			this.animation = animation;
		}

		@Override
		public void applyTo(final TimeEnvironmentImpl timeEnvironment) {
			timeEnvironment.setBounds(animation.getStart(), animation.getEnd());
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
			final int prime = 31;
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
			final ChooseableAnimation other = (ChooseableAnimation) obj;
			if (animation == null) {
				return other.animation == null;
			} else {
				return animation.equals(other.animation);
			}
		}
	}

	private static final class ChooseableDoNothing implements ChooseableTimeRange {
		private final String text;

		public ChooseableDoNothing(final String text) {
			this.text = text;
		}

		@Override
		public void applyTo(final TimeEnvironmentImpl timeEnvironment) {
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
			final int prime = 31;
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
			final ChooseableDoNothing other = (ChooseableDoNothing) obj;
			if (text == null) {
				return other.text == null;
			} else {
				return text.equals(other.text);
			}
		}
	}

	private static final class ChooseableGlobalSeq implements ChooseableTimeRange {
		private final Integer globalSeq;

		public ChooseableGlobalSeq(final Integer globalSeq) {
			this.globalSeq = globalSeq;
		}

		@Override
		public void applyTo(final TimeEnvironmentImpl timeEnvironment) {
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
			final int prime = 31;
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
			final ChooseableGlobalSeq other = (ChooseableGlobalSeq) obj;
			if (globalSeq == null) {
				return other.globalSeq == null;
			} else {
				return globalSeq.equals(other.globalSeq);
			}
		}

	}

	@Override
	public void timeChanged(final int currentTime, final Set<IdObject> objects, final List<AnimFlag> timelines) {
//		tSpline.setSelection(currentTime);
	}

}
