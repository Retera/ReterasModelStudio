package com.hiveworkshop.wc3.gui.modeledit.creator;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.hiveworkshop.wc3.gui.ProgramPreferences;
import com.hiveworkshop.wc3.gui.animedit.TimeEnvironmentImpl;
import com.hiveworkshop.wc3.gui.animedit.WrongModeException;
import com.hiveworkshop.wc3.gui.modeledit.ActiveViewportWatcher;
import com.hiveworkshop.wc3.gui.modeledit.FaceCreationException;
import com.hiveworkshop.wc3.gui.modeledit.ModeButton;
import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.Viewport;
import com.hiveworkshop.wc3.gui.modeledit.activity.ActivityDescriptor;
import com.hiveworkshop.wc3.gui.modeledit.activity.ModelEditorChangeActivityListener;
import com.hiveworkshop.wc3.gui.modeledit.activity.ModelEditorMultiManipulatorActivity;
import com.hiveworkshop.wc3.gui.modeledit.activity.ModelEditorViewportActivity;
import com.hiveworkshop.wc3.gui.modeledit.activity.UndoActionListener;
import com.hiveworkshop.wc3.gui.modeledit.creator.activity.DrawBoxActivityDescriptor;
import com.hiveworkshop.wc3.gui.modeledit.creator.activity.DrawPlaneActivityDescriptor;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.ModelEditorManager;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.builder.model.SquatToolWidgetManipulatorBuilder;
import com.hiveworkshop.wc3.gui.modeledit.toolbar.ToolbarActionButtonType;
import com.hiveworkshop.wc3.gui.modeledit.toolbar.ToolbarButtonGroup;
import com.hiveworkshop.wc3.mdl.Animation;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.v2.ModelView;

public class CreatorModelingPanel extends JPanel implements ModelEditorChangeActivityListener {
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
	private final Map<ActivityDescriptor, ModeButton> typeToButton = new HashMap<>();
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
		animationChooserBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final ChooseableTimeRange selectedItem = (ChooseableTimeRange) animationChooserBox.getSelectedItem();
				if (selectedItem != null) {
					selectedItem.applyTo(timeEnvironmentImpl);
				}
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

		modeChooserBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				cardLayout.show(cardPanel, modeChooserBox.getSelectedItem().toString());
			}
		});

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
		faceButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (CreatorModelingPanel.this.modelEditorManager == null) {
					return;
				}
				try {
					final Viewport viewport = activeViewportWatcher.getViewport();
					final Vertex facingVector = viewport == null ? new Vertex(0, 0, 1) : viewport.getFacingVector();
					final UndoAction createFaceFromSelection = CreatorModelingPanel.this.modelEditorManager
							.getModelEditor().createFaceFromSelection(facingVector);
					undoActionListener.pushAction(createFaceFromSelection);
				} catch (final WrongModeException exc) {
					JOptionPane.showMessageDialog(CreatorModelingPanel.this,
							"Unable to create face, wrong selection mode", "Error", JOptionPane.ERROR_MESSAGE);
				} catch (final FaceCreationException exc) {
					JOptionPane.showMessageDialog(CreatorModelingPanel.this, exc.getMessage(), "Error",
							JOptionPane.ERROR_MESSAGE);
				}
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
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					listener.changeActivity(type);
				}
			});
			typeToButton.put(type, button);
			modeButtons.add(button);
		}

		meshBasicsPanel.add(editToolsPanel, BorderLayout.CENTER);

		vertexButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				listeningForActivityChanges = false;
				listener.changeActivity(new DrawVertexActivityDescriptor(programPrefences, activeViewportWatcher));
				resetButtons();
				vertexButton.setColors(programPrefences.getActiveColor1(), programPrefences.getActiveColor2());
				listeningForActivityChanges = true;
			}
		});
		boneButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				listeningForActivityChanges = false;
				listener.changeActivity(new DrawBoneActivityDescriptor(programPrefences, activeViewportWatcher));
				resetButtons();
				vertexButton.setColors(programPrefences.getActiveColor1(), programPrefences.getActiveColor2());
				listeningForActivityChanges = true;
			}
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
				button.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(final ActionEvent e) {
						listener.changeActivity(type);
					}
				});
				typeToButton.put(type, button);
				modeButtons.add(button);
			}
			index++;
		}
		final ActivityDescriptor selectAndSquatDescriptor = new ActivityDescriptor() {
			@Override
			public ModelEditorViewportActivity createActivity(final ModelEditorManager modelEditorManager,
					final ModelView modelView, final UndoActionListener undoActionListener) {
				return new ModelEditorMultiManipulatorActivity(
						new SquatToolWidgetManipulatorBuilder(modelEditorManager.getModelEditor(),
								modelEditorManager.getViewportSelectionHandler(), programPreferences, modelView),
						undoActionListener, modelEditorManager.getSelectionView());
			}
		};
		final String squatTypeName = "Squat";
		final ModeButton squatButton = new ModeButton(squatTypeName);
		editToolsPanel.add(squatButton);
		squatButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				listener.changeActivity(selectAndSquatDescriptor);
			}
		});
		typeToButton.put(selectAndSquatDescriptor, squatButton);
		modeButtons.add(squatButton);

		meshBasicsPanel.add(editToolsPanel, BorderLayout.CENTER);
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
		if (sawLast && selectedItem != null) {
			animationChooserBox.setSelectedItem(thingToChooseableItem.get(thingSelected));
		}
	}

	@Override
	public void changeActivity(final ActivityDescriptor newType) {
		final ModeButton modeButton = typeToButton.get(newType);
		if (modeButton != null) {
			resetButtons();
			modeButton.setColors(programPreferences.getActiveColor1(), programPreferences.getActiveColor2());
		} else {
			if (listeningForActivityChanges) {
				resetButtons();
			}
		}
	}

	public void setUndoManager(final UndoActionListener undoManager) {
		this.undoActionListener = undoManager;
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
			result = prime * result + (animation == null ? 0 : animation.hashCode());
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
				if (other.animation != null) {
					return false;
				}
			} else if (!animation.equals(other.animation)) {
				return false;
			}
			return true;
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
			result = prime * result + (text == null ? 0 : text.hashCode());
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
				if (other.text != null) {
					return false;
				}
			} else if (!text.equals(other.text)) {
				return false;
			}
			return true;
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
			result = prime * result + (globalSeq == null ? 0 : globalSeq.hashCode());
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
				if (other.globalSeq != null) {
					return false;
				}
			} else if (!globalSeq.equals(other.globalSeq)) {
				return false;
			}
			return true;
		}

	}
}
