package com.hiveworkshop.rms.ui.gui.modeledit.creator;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.ui.application.ModelEditActions;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.application.edit.animation.WrongModeException;
import com.hiveworkshop.rms.ui.application.edit.animation.mdlvisripoff.TSpline;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ModelEditorChangeActivityListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.graphics2d.FaceCreationException;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.Viewport;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.ViewportListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.creator.activity.DrawBoneActivity;
import com.hiveworkshop.rms.ui.gui.modeledit.creator.activity.DrawBoxActivity;
import com.hiveworkshop.rms.ui.gui.modeledit.creator.activity.DrawPlaneActivity;
import com.hiveworkshop.rms.ui.gui.modeledit.creator.activity.DrawVertexActivity;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ModeButton2;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ModelEditorActionType3;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ToolbarButtonGroup2;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.ui.util.ModeButton;
import com.hiveworkshop.rms.util.Vec3;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

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
		animationChooserBox.addActionListener(e -> chooseAnimation());

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
		planeButton.addActionListener(e -> drawPlane(viewportListener, planeButton));
		drawPrimitivesPanel.add(planeButton);

		ModeButton boxButton = new ModeButton("Box");
		boxButton.addActionListener(e -> drawBox(viewportListener, boxButton));
		drawPrimitivesPanel.add(boxButton);

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

	private void chooseAnimation() {
		ChooseableTimeRange<?> selectedItem = (ChooseableTimeRange<?>) animationChooserBox.getSelectedItem();
		if (selectedItem != null) {
			TimeEnvironmentImpl timeEnvironment = modelHandler == null ? null : modelHandler.getEditTimeEnv();
			selectedItem.applyTo(timeEnvironment);
		}
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
		ModeButton2 vertexButton = new ModeButton2("Vertex", programPreferences.getActiveColor1(), programPreferences.getActiveColor2());
		vertexButton.addActionListener(e -> addVertex(vertexButton, viewportListener));

		ModeButton2 faceButton = new ModeButton2("Face from Selection", programPreferences.getActiveColor1(), programPreferences.getActiveColor2());
		faceButton.addActionListener(e -> createFace(viewportListener));

		ModeButton2 boneButton = new ModeButton2("Bone", programPreferences.getActiveColor1(), programPreferences.getActiveColor2());
		boneButton.addActionListener(e -> addBone(boneButton, viewportListener));

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
			UndoAction createFaceFromSelection = ModelEditActions.createFaceFromSelection(modelHandler.getModelView(), facingVector);
			modelHandler.getUndoManager().pushAction(createFaceFromSelection);
		} catch (WrongModeException exc) {
			JOptionPane.showMessageDialog(CreatorModelingPanel.this,
					"Unable to create face, wrong selection mode", "Error", JOptionPane.ERROR_MESSAGE);
		} catch (FaceCreationException exc) {
			JOptionPane.showMessageDialog(CreatorModelingPanel.this, exc.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void addBone(ModeButton2 modeButton, ViewportListener viewportListener) {
//		listener.changeActivity(new DrawBoneActivityDescriptor(viewportListener));//ToDo
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			DrawBoneActivity activity = new DrawBoneActivity(modelHandler, modelEditorManager, viewportListener);
			modeButton.setColors(programPreferences.getActiveColor1(), programPreferences.getActiveColor2());
			modelPanel.changeActivity(activity);
		}
	}

	private void addVertex(ModeButton2 modeButton, ViewportListener viewportListener) {
//		listener.changeActivity(new DrawVertexActivityDescriptor(viewportListener));//ToDo
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			DrawVertexActivity activity = new DrawVertexActivity(modelHandler, modelEditorManager, viewportListener);
			modeButton.setColors(programPreferences.getActiveColor1(), programPreferences.getActiveColor2());
			modelPanel.changeActivity(activity);
		}
	}

	private void drawBox(ViewportListener viewportListener, ModeButton modeButton) {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			DrawBoxActivity activity = new DrawBoxActivity(modelHandler, modelEditorManager, viewportListener, 1, 1, 1);
			modeButton.setColors(programPreferences.getActiveColor1(), programPreferences.getActiveColor2());
			modelPanel.changeActivity(activity);
		}
	}

	private void drawPlane(ViewportListener viewportListener, ModeButton modeButton) {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			DrawPlaneActivity activity = new DrawPlaneActivity(modelHandler, modelEditorManager, viewportListener, 1, 1, 1);
			modeButton.setColors(programPreferences.getActiveColor1(), programPreferences.getActiveColor2());
			modelPanel.changeActivity(activity);
		}
	}

	public void makeAnimationBasicsPanel(ModelEditorChangeActivityListener listener,
	                                     ToolbarButtonGroup2<ModelEditorActionType3> actionTypeGroup,
	                                     ViewportListener viewportListener, DefaultComboBoxModel<String> modeChooserBoxModel,
	                                     JPanel cardPanel) {
//		JPanel meshBasicsPanel = new JPanel(new BorderLayout());
		JPanel meshBasicsPanel = new JPanel(new MigLayout("fill"));
		cardPanel.add(meshBasicsPanel, ANIMATIONBASICS);

		JPanel editToolsPanel = new JPanel(new MigLayout("debug, fill"));
		editToolsPanel.setBorder(BorderFactory.createTitledBorder("Manipulate"));
		editToolsPanel.add(actionTypeGroup.getModeButton(ModelEditorActionType3.TRANSLATION), "wrap");
		editToolsPanel.add(actionTypeGroup.getModeButton(ModelEditorActionType3.ROTATION), "wrap");
		editToolsPanel.add(actionTypeGroup.getModeButton(ModelEditorActionType3.SCALING), "wrap");
		editToolsPanel.add(actionTypeGroup.getModeButton(ModelEditorActionType3.SQUAT), "wrap");

//		boolean temp = false;
		boolean temp = true;
		if (temp) {
			TSpline tSpline = new TSpline();
			editToolsPanel.add(tSpline, "wrap, growy");
		}

		editToolsPanel.add(new JLabel("UGG"), "wrap");
		meshBasicsPanel.add(editToolsPanel, "growx, growy");
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
