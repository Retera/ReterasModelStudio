package com.hiveworkshop.rms.ui.gui.modeledit.creator;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.ui.application.ModelEditActions;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.application.edit.animation.WrongModeException;
import com.hiveworkshop.rms.ui.application.edit.animation.mdlvisripoff.TSpline;
import com.hiveworkshop.rms.ui.application.edit.animation.mdlvisripoff.TTan;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ModelEditorChangeActivityListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.graphics2d.FaceCreationException;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.Viewport;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.ViewportListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ModeButton2;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ModelEditorActionType3;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ToolbarButtonGroup2;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.ui.util.ModeButton;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class CreatorModelingPanel extends JPanel implements ModelEditorChangeActivityListener {
	private static final String ANIMATIONBASICS = "ANIMATIONBASICS";

	private ModelEditorManager modelEditorManager;
	private final ProgramPreferences programPreferences;
	private final DefaultComboBoxModel<ChooseableTimeRange<?>> animationChooserBoxModel;
	private final JComboBox<ChooseableTimeRange<?>> animationChooserBox;
	private final JComboBox<String> modeChooserBox;
	private final CardLayout modeCardLayout;
	private final JPanel modeCardPanel;
	private ModelHandler modelHandler;
	private final CardLayout northCardLayout;
	private final JPanel northCardPanel;

	public CreatorModelingPanel(ModelEditorChangeActivityListener listener,
	                            ToolbarButtonGroup2<ModelEditorActionType3> actionTypeGroup,
	                            ViewportListener viewportListener) {
		this.programPreferences = ProgramGlobals.getPrefs();

		setLayout(new BorderLayout());

		DefaultComboBoxModel<String> modeChooserBoxModel = new DefaultComboBoxModel<>();
		modeChooserBoxModel.addElement("Mesh Basics");
		modeChooserBoxModel.addElement("Standard Primitives");
		modeChooserBoxModel.addElement("Extended Primitives");
		modeChooserBoxModel.addElement("Animation Nodes");
		modeChooserBox = new JComboBox<>(modeChooserBoxModel);

		animationChooserBoxModel = new DefaultComboBoxModel<>();
		animationChooserBox = new JComboBox<>(animationChooserBoxModel);
		animationChooserBox.setVisible(false);
		ActionListener actionListener = e -> {
			ChooseableTimeRange<?> selectedItem = (ChooseableTimeRange<?>) animationChooserBox.getSelectedItem();
			if (selectedItem != null) {
				TimeEnvironmentImpl timeEnvironment = modelHandler == null ? null : modelHandler.getEditTimeEnv();
				selectedItem.applyTo(timeEnvironment);
			}
		};
		animationChooserBox.addActionListener(actionListener);

		northCardLayout = new CardLayout();
		northCardPanel = new JPanel(northCardLayout);
		add(northCardPanel, BorderLayout.NORTH);
		northCardPanel.add(animationChooserBox, "ANIM");
		northCardPanel.add(modeChooserBox, "MESH");
		northCardLayout.show(northCardPanel, "MESH");

		modeCardLayout = new CardLayout();
		modeCardPanel = new JPanel(modeCardLayout);
		add(modeCardPanel, BorderLayout.CENTER);

		makeMeshBasicsPanel(actionTypeGroup, viewportListener, modeChooserBoxModel, modeCardPanel);

		JPanel drawPrimitivesPanel = new JPanel(new GridLayout(16, 1));
		drawPrimitivesPanel.setBorder(BorderFactory.createTitledBorder("Draw"));

		ModeButton planeButton = new ModeButton("Plane");
//		planeButton.addActionListener(new ActionListenerImplementation(new DrawPlaneActivityDescriptor(viewportListener), programPreferences, listener, planeButton));//todo
		drawPrimitivesPanel.add(planeButton);
//		modeButtons.add(planeButton);

		ModeButton boxButton = new ModeButton("Box");
//		boxButton.addActionListener(new ActionListenerImplementation(new DrawBoxActivityDescriptor(viewportListener), programPreferences, listener, boxButton));//todo
		drawPrimitivesPanel.add(boxButton);
//		modeButtons.add(boxButton);

		JPanel spOptionsPanel = new JPanel(new GridLayout(16, 1));
		spOptionsPanel.setBorder(BorderFactory.createTitledBorder("Options"));

		JPanel standardPrimitivesPanel = new JPanel(new BorderLayout());
		standardPrimitivesPanel.add(drawPrimitivesPanel, BorderLayout.NORTH);
		standardPrimitivesPanel.add(spOptionsPanel, BorderLayout.CENTER);

		modeCardPanel.add(standardPrimitivesPanel, modeChooserBoxModel.getElementAt(1));

		modeChooserBox.addActionListener(e -> modeCardLayout.show(modeCardPanel, modeChooserBox.getSelectedItem().toString()));

		makeAnimationBasicsPanel(listener, actionTypeGroup, viewportListener, modeChooserBoxModel, modeCardPanel);

		modeCardLayout.show(modeCardPanel, modeChooserBoxModel.getElementAt(0));
	}

	public void makeMeshBasicsPanel(ToolbarButtonGroup2<ModelEditorActionType3> actionTypeGroup,
	                                ViewportListener viewportListener,
	                                DefaultComboBoxModel<String> modeChooserBoxModel,
	                                JPanel cardPanel) {
		JPanel meshBasicsPanel = new JPanel(new BorderLayout());

		JPanel drawToolsPanel = getDrawToolsPanel(viewportListener);
		meshBasicsPanel.add(drawToolsPanel, BorderLayout.NORTH);


		JPanel editToolsPanel = new JPanel(new GridLayout(16, 1));
		editToolsPanel.setBorder(BorderFactory.createTitledBorder("Manipulate"));


		for (ModeButton2 modeButton2 : actionTypeGroup.getModeButtons()) {
			editToolsPanel.add(modeButton2);
		}

		meshBasicsPanel.add(editToolsPanel, BorderLayout.CENTER);

		cardPanel.add(meshBasicsPanel, modeChooserBoxModel.getElementAt(0));

	}

	private JPanel getDrawToolsPanel(ViewportListener viewportListener) {
		ModeButton2 vertexButton = new ModeButton2("Vertex",programPreferences.getActiveColor1(), programPreferences.getActiveColor2());
		vertexButton.addActionListener(e -> addVertex(vertexButton));

		ModeButton2 faceButton = new ModeButton2("Face from Selection",programPreferences.getActiveColor1(), programPreferences.getActiveColor2());
		faceButton.addActionListener(e -> createFace(viewportListener));

		ModeButton2 boneButton = new ModeButton2("Bone",programPreferences.getActiveColor1(), programPreferences.getActiveColor2());
		boneButton.addActionListener(e -> addBone(boneButton));

		JPanel drawToolsPanel = new JPanel(new GridLayout(2, 1));
		drawToolsPanel.setBorder(BorderFactory.createTitledBorder("Draw"));

		drawToolsPanel.add(vertexButton);
		drawToolsPanel.add(faceButton);
		drawToolsPanel.add(boneButton);
		return drawToolsPanel;
	}

	private void createFace(ViewportListener viewportListener) {
		if (modelEditorManager == null) {
			return;
		}
		try {
			Viewport viewport = viewportListener.getViewport();
			Vec3 facingVector = viewport == null ? new Vec3(0, 0, 1) : viewport.getFacingVector();
//			UndoAction createFaceFromSelection = modelEditorManager.getModelEditor().createFaceFromSelection(facingVector);
			UndoAction createFaceFromSelection = ModelEditActions.createFaceFromSelection(modelHandler.getModelView(), facingVector);
			modelHandler.getUndoManager().pushAction(createFaceFromSelection);
		} catch (WrongModeException exc) {
			JOptionPane.showMessageDialog(CreatorModelingPanel.this,
					"Unable to create face, wrong selection mode", "Error", JOptionPane.ERROR_MESSAGE);
		} catch (FaceCreationException exc) {
			JOptionPane.showMessageDialog(CreatorModelingPanel.this, exc.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void addBone(ModeButton2 boneButton) {
//	private void addBone(ModelEditorChangeActivityListener listener, ViewportListener viewportListener, ModeButton2 boneButton) {
//		listener.changeActivity(new DrawBoneActivityDescriptor(viewportListener));//ToDo
		boneButton.setColors(programPreferences.getActiveColor1(), programPreferences.getActiveColor2());
	}

	private void addVertex(ModeButton2 vertexButton) {
//	private void addVertex(ModelEditorChangeActivityListener listener, ViewportListener viewportListener, ModeButton2 vertexButton) {
//		listener.changeActivity(new DrawVertexActivityDescriptor(viewportListener));//ToDo
		vertexButton.setColors(programPreferences.getActiveColor1(), programPreferences.getActiveColor2());
	}

	public void makeAnimationBasicsPanel(ModelEditorChangeActivityListener listener,
	                                     ToolbarButtonGroup2<ModelEditorActionType3> actionTypeGroup,
	                                     ViewportListener viewportListener, DefaultComboBoxModel<String> modeChooserBoxModel,
	                                     JPanel cardPanel) {
		JPanel meshBasicsPanel = new JPanel(new BorderLayout());
		cardPanel.add(meshBasicsPanel, ANIMATIONBASICS);
		JPanel editToolsPanel = new JPanel(new GridLayout(16, 1));
		editToolsPanel.setBorder(BorderFactory.createTitledBorder("Manipulate"));
		editToolsPanel.add(actionTypeGroup.getModeButton(ModelEditorActionType3.TRANSLATION));
		editToolsPanel.add(actionTypeGroup.getModeButton(ModelEditorActionType3.ROTATION));
		editToolsPanel.add(actionTypeGroup.getModeButton(ModelEditorActionType3.SCALING));
		editToolsPanel.add(actionTypeGroup.getModeButton(ModelEditorActionType3.SQUAT));

//		int index = 0;
//		for (ModelEditorActionType3 type : actionTypeGroup.getToolbarButtonTypes()) {
//			if (index < 3) {
//				String typeName = type.getName();
//				ModeButton button = new ModeButton(typeName.substring(typeName.lastIndexOf(' ') + 1));
//				editToolsPanel.add(button);
////				button.addActionListener(e -> listener.changeActivity(type));
////				putTypeToButton(type, button);
////				modeButtons.add(button);
//			}
//			index++;
//		}
//		ActivityDescriptor selectAndSquatDescriptor = (modelEditorManager, modelHandler) -> new ModelEditorMultiManipulatorActivity(
//				new SquatToolWidgetManipulatorBuilder(modelEditorManager.getModelEditor(),
//						modelEditorManager.getViewportSelectionHandler(), programPreferences, modelHandler.getModelView()),
//				modelHandler.getUndoManager(), modelEditorManager.getSelectionView());
//		String squatTypeName = "Squat";
//		ModeButton squatButton = new ModeButton(squatTypeName);
//		editToolsPanel.add(squatButton);
////		squatButton.addActionListener(e -> listener.changeActivity(selectAndSquatDescriptor));
////		putTypeToButton(selectAndSquatDescriptor, squatButton); //ToDo
//		modeButtons.add(squatButton);

		if (false) {
			TSpline tSpline = new TSpline(new TTan());
			editToolsPanel.add(tSpline);
		}

		meshBasicsPanel.add(editToolsPanel, BorderLayout.CENTER);
	}

	public void setAnimationModeState(boolean animationModeState) {
		northCardLayout.show(northCardPanel, animationModeState ? "ANIM" : "MESH");
		if (animationModeState) {
			modeCardLayout.show(modeCardPanel, ANIMATIONBASICS);
		} else {
			modeCardLayout.show(modeCardPanel, modeChooserBox.getSelectedItem().toString());
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

	public void reloadAnimationList() {
		ChooseableTimeRange<?> selectedItem = (ChooseableTimeRange<?>) animationChooserBox.getSelectedItem();
		animationChooserBoxModel.removeAllElements();

		ChooseableDoNothing doNothingItem = new ChooseableDoNothing("Custom Timeframe");
		animationChooserBoxModel.addElement(doNothingItem);

		ChooseableTimeRange<?> thingSelected = selectedItem == null ? doNothingItem : selectedItem;


		for (Animation animation : modelHandler.getModel().getAnims()) {
			ChooseableAnimation choosableItem = new ChooseableAnimation(animation);
			animationChooserBoxModel.addElement(choosableItem);
		}

		for (Integer integer : modelHandler.getModel().getGlobalSeqs()) {
			ChooseableGlobalSeq chooseableItem = new ChooseableGlobalSeq(integer);
			animationChooserBoxModel.addElement(chooseableItem);
		}
		animationChooserBox.setSelectedItem(thingSelected);
	}

	@Override
//	public void changeActivity(ModelEditorActionType3 newType) {
////		List<ModeButton> modeButtons = typeToButtons.get(newType);
////		if ((modeButtons != null) && !modeButtons.isEmpty()) {
//////			resetButtons();
//////			for (ModeButton modeButton : modeButtons) {
//////				modeButton.setColors(programPreferences.getActiveColor1(), programPreferences.getActiveColor2());
//////			}
////		} else {
////			if (listeningForActivityChanges) {
////				resetButtons();
////			}
////		}
//	}
	public void changeActivity(ModelEditorActionType3 newType) {
//        currentActivity = newType;
		for (ModelPanel modelPanel : ProgramGlobals.getModelPanels()) {
			modelPanel.changeActivity(newType);
		}
//        creatorPanel.changeActivity(newType);
	}


	public interface ChooseableTimeRange<T> {
		void applyTo(TimeEnvironmentImpl timeEnvironment);

		T getThing();
	}

}
