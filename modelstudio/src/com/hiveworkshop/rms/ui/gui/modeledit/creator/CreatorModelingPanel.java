package com.hiveworkshop.rms.ui.gui.modeledit.creator;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.GlobalSeq;
import com.hiveworkshop.rms.ui.application.ModelEditActions;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.application.edit.animation.WrongModeException;
import com.hiveworkshop.rms.ui.application.edit.animation.mdlvisripoff.TSpline;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.graphics2d.FaceCreationException;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.ViewportPopupMenu;
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
import java.awt.event.ItemEvent;
import java.util.function.Consumer;

public class CreatorModelingPanel extends JPanel {
	private static final String ANIMATIONBASICS = "ANIMATIONBASICS";

	private ModelEditorManager modelEditorManager;
	private final ProgramPreferences programPreferences;
	//	private final DefaultComboBoxModel<ChooseableTimeRange<?>> animationChooserBoxModel;
//	private final JComboBox<ChooseableTimeRange<?>> animationChooserBox;
	private final DefaultComboBoxModel<Sequence> animationChooserBoxModel;
	private final JComboBox<Sequence> animationChooserBox;
	private final JComboBox<String> modeChooserBox;
	private final CardLayout modeCardLayout;
	private final JPanel modeCardPanel;
	private ModelHandler modelHandler;
	private final CardLayout northCardLayout;
	private final JPanel northCardPanel;
	private final ManualTransformPanel transformPanel;


	private JPopupMenu contextMenu;

	public CreatorModelingPanel() {
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
		animationChooserBox.addItemListener(this::chooseAnimation);

		northCardLayout = new CardLayout();
		northCardPanel = new JPanel(northCardLayout);
		northCardPanel.add(animationChooserBox, "ANIM");
		northCardPanel.add(modeChooserBox, "MESH");

		add(northCardPanel, BorderLayout.NORTH);
		northCardLayout.show(northCardPanel, "MESH");

		modeCardLayout = new CardLayout();
		modeCardPanel = new JPanel(modeCardLayout);

		modeCardPanel.add(getStandardPrimitivesPanel(), modeChooserBoxModel.getElementAt(0));
		modeCardPanel.add(getMeshBasicsPanel(), modeChooserBoxModel.getElementAt(1));
//		modeCardPanel.add(getMeshBasicsPanel(), modeChooserBoxModel.getElementAt(0));
//		modeCardPanel.add(getStandardPrimitivesPanel(), modeChooserBoxModel.getElementAt(1));
		modeCardPanel.add(getAnimationBasicsPanel(), ANIMATIONBASICS);

		modeChooserBox.addActionListener(e -> modeCardLayout.show(modeCardPanel, modeChooserBox.getSelectedItem().toString()));

		add(modeCardPanel, BorderLayout.CENTER);
		modeCardLayout.show(modeCardPanel, modeChooserBoxModel.getElementAt(1));

		transformPanel = new ManualTransformPanel();
//		add(transformPanel, "wrap")
//		add(transformPanel, BorderLayout.CENTER);

		JButton button = new JButton("show popup menu");
		button.addActionListener(e -> showVPPopup(button));
		add(button, BorderLayout.SOUTH);
	}

	private JPanel getStandardPrimitivesPanel() {
		JPanel drawPrimitivesPanel = new JPanel(new GridLayout(16, 1));
		drawPrimitivesPanel.setBorder(BorderFactory.createTitledBorder("Draw"));
		drawPrimitivesPanel.add(getModeButton("Plane", this::drawPlane));
		drawPrimitivesPanel.add(getModeButton("Box", this::drawBox));

		JPanel spOptionsPanel = new JPanel(new GridLayout(16, 1));
		spOptionsPanel.setBorder(BorderFactory.createTitledBorder("Options"));

		JPanel standardPrimitivesPanel = new JPanel(new BorderLayout());
		standardPrimitivesPanel.add(drawPrimitivesPanel, BorderLayout.NORTH);
		standardPrimitivesPanel.add(spOptionsPanel, BorderLayout.CENTER);

		return standardPrimitivesPanel;
	}

	private ModeButton getModeButton(String text, Consumer<ModeButton> action) {
		ModeButton modeButton = new ModeButton(text);
		modeButton.addActionListener(e -> action.accept(modeButton));
		return modeButton;
	}

	private void chooseAnimation(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
//			ChooseableTimeRange<?> selectedItem = (ChooseableTimeRange<?>) animationChooserBox.getSelectedItem();
//			if (selectedItem != null) {
//				TimeEnvironmentImpl timeEnvironment = modelHandler == null ? null : modelHandler.getEditTimeEnv();
//				selectedItem.applyTo(timeEnvironment);
//			}
			Sequence selectedItem = (Sequence) animationChooserBox.getSelectedItem();
			if (selectedItem != null && modelHandler != null) {
				modelHandler.getEditTimeEnv().setSequence(selectedItem);
			}
		}
	}

	public JPanel getMeshBasicsPanel() {

		JPanel drawToolsPanel = getDrawToolsPanel();

		JPanel editToolsPanel = new JPanel(new GridLayout(16, 1));
		editToolsPanel.setBorder(BorderFactory.createTitledBorder("Manipulate"));

		for (ModeButton2 modeButton2 : ProgramGlobals.getActionTypeGroup().getModeButtons()) {
			editToolsPanel.add(modeButton2);
		}

		JPanel meshBasicsPanel = new JPanel(new BorderLayout());
		meshBasicsPanel.add(drawToolsPanel, BorderLayout.NORTH);
		meshBasicsPanel.add(editToolsPanel, BorderLayout.CENTER);
		return meshBasicsPanel;

	}

	private JPanel getDrawToolsPanel() {
		JPanel drawToolsPanel = new JPanel(new GridLayout(2, 1));
		drawToolsPanel.setBorder(BorderFactory.createTitledBorder("Draw"));

		drawToolsPanel.add(getModeButton2("Vertex", this::addVertex));
		drawToolsPanel.add(getModeButton2("Face from Selection", this::createFace));
		drawToolsPanel.add(getModeButton2("Bone", this::addBone));
		return drawToolsPanel;
	}

	private ModeButton2 getModeButton2(String text, Consumer<ModeButton2> action) {
		ModeButton2 modeButton = new ModeButton2(text, programPreferences.getActiveColor1(), programPreferences.getActiveColor2());
		modeButton.addActionListener(e -> action.accept(modeButton));
		return modeButton;
	}

	private void createFace(ModeButton2 modeButton) {
		if (modelEditorManager == null) {
			return;
		}
		try {
//			Viewport viewport = viewportListener.getViewport();
//			Vec3 facingVector = viewport == null ? new Vec3(0, 0, 1) : viewport.getFacingVector();
			Vec3 facingVector = new Vec3(0, 0, 1);
			UndoAction createFaceFromSelection = ModelEditActions.createFaceFromSelection(modelHandler.getModelView(), facingVector);
			modelHandler.getUndoManager().pushAction(createFaceFromSelection.redo());
		} catch (WrongModeException exc) {
			JOptionPane.showMessageDialog(CreatorModelingPanel.this,
					"Unable to create face, wrong selection mode", "Error", JOptionPane.ERROR_MESSAGE);
		} catch (FaceCreationException exc) {
			JOptionPane.showMessageDialog(CreatorModelingPanel.this, exc.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void addBone(ModeButton2 modeButton) {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			DrawBoneActivity activity = new DrawBoneActivity(modelHandler, modelEditorManager);
			modeButton.setColors(programPreferences.getActiveColor1(), programPreferences.getActiveColor2());
			modelPanel.changeActivity(activity);
		}
	}

	private void addVertex(ModeButton2 modeButton) {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			DrawVertexActivity activity = new DrawVertexActivity(modelHandler, modelEditorManager);
			modeButton.setColors(programPreferences.getActiveColor1(), programPreferences.getActiveColor2());
			modelPanel.changeActivity(activity);
		}
	}

	private void drawBox(ModeButton modeButton) {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			DrawBoxActivity activity = new DrawBoxActivity(modelHandler, modelEditorManager, 1, 1, 1);
			modeButton.setColors(programPreferences.getActiveColor1(), programPreferences.getActiveColor2());
			modelPanel.changeActivity(activity);
		}
	}

	private void drawPlane(ModeButton modeButton) {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			DrawPlaneActivity activity = new DrawPlaneActivity(modelHandler, modelEditorManager, 1, 1, 1);
			modeButton.setColors(programPreferences.getActiveColor1(), programPreferences.getActiveColor2());
			modelPanel.changeActivity(activity);
		}
	}

	public JPanel getAnimationBasicsPanel() {
		JPanel animationBasicsPanel = new JPanel(new MigLayout("fill"));

		ToolbarButtonGroup2<ModelEditorActionType3> actionTypeGroup = ProgramGlobals.getActionTypeGroup();

//		JPanel editToolsPanel = new JPanel(new MigLayout("debug, fill"));
		JPanel editToolsPanel = new JPanel(new MigLayout("fill"));
		editToolsPanel.setBorder(BorderFactory.createTitledBorder("Manipulate"));
		editToolsPanel.add(actionTypeGroup.getModeButton(ModelEditorActionType3.TRANSLATION), "wrap");
		editToolsPanel.add(actionTypeGroup.getModeButton(ModelEditorActionType3.ROTATION), "wrap");
		editToolsPanel.add(actionTypeGroup.getModeButton(ModelEditorActionType3.SCALING), "wrap");
		editToolsPanel.add(actionTypeGroup.getModeButton(ModelEditorActionType3.SQUAT), "wrap");

//		boolean temp = false;
		boolean temp = true;
		if (temp) {
			TSpline tSpline = new TSpline();
			animationBasicsPanel.add(tSpline, "wrap, growy");
		}

//		editToolsPanel.add(new JLabel("UGG"), "wrap");
		animationBasicsPanel.add(editToolsPanel, "growx, growy");
		return animationBasicsPanel;
	}

	private void showVPPopup(JButton button) {
		if (contextMenu != null) {
			contextMenu.show(button, 0, 0);
		}
	}

	public void setAnimationModeState(boolean animationModeState) {
		northCardLayout.show(northCardPanel, animationModeState ? "ANIM" : "MESH");
		if (animationModeState) {
			modeCardLayout.show(modeCardPanel, ANIMATIONBASICS);
		} else {
			modeCardLayout.show(modeCardPanel, modeChooserBox.getSelectedItem().toString());
		}
	}

	public void setModelPanel(ModelPanel modelPanel) {
		if (modelPanel != null) {
			this.modelHandler = modelPanel.getModelHandler();
			this.modelEditorManager = modelPanel.getModelEditorManager();
//			contextMenu = new ViewportPopupMenu(viewportListener.getViewport(), ProgramGlobals.getMainPanel(), modelPanel.getModelHandler(), modelPanel.getModelEditorManager());
			contextMenu = new ViewportPopupMenu(null, ProgramGlobals.getMainPanel(), modelPanel.getModelHandler(), modelPanel.getModelEditorManager());

			transformPanel.setModel(modelHandler, modelEditorManager);
			reloadAnimationList();
		} else {
			this.modelHandler = null;
			this.modelEditorManager = null;
			contextMenu = null;
			transformPanel.setModel(null, null);
		}
	}

	public void reloadAnimationList() {
		Sequence selectedItem = (Sequence) animationChooserBox.getSelectedItem();
		animationChooserBoxModel.removeAllElements();

		EditableModel model = modelHandler.getModel();
		for (Animation animation : model.getAnims()) {
			animationChooserBoxModel.addElement(animation);
		}

		for (GlobalSeq globalSeq : model.getGlobalSeqs()) {
			animationChooserBoxModel.addElement(globalSeq);
		}
		if (selectedItem != null || animationChooserBoxModel.getSize() < 1) {
			animationChooserBox.setSelectedItem(selectedItem);
		} else {
			animationChooserBox.setSelectedIndex(0);
		}
	}
}
