package com.hiveworkshop.rms.ui.gui.modeledit.creator;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.GlobalSeq;
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
import java.util.function.BiConsumer;

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
	private final ViewportListener viewportListener;


	private JPopupMenu contextMenu;

	public CreatorModelingPanel(ViewportListener viewportListener) {
		this.programPreferences = ProgramGlobals.getPrefs();
		this.viewportListener = viewportListener;
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

		modeCardPanel.add(getMeshBasicsPanel(viewportListener), modeChooserBoxModel.getElementAt(0));
		modeCardPanel.add(getStandardPrimitivesPanel(viewportListener), modeChooserBoxModel.getElementAt(1));
		modeCardPanel.add(getAnimationBasicsPanel(), ANIMATIONBASICS);

		modeChooserBox.addActionListener(e -> modeCardLayout.show(modeCardPanel, modeChooserBox.getSelectedItem().toString()));

		add(modeCardPanel, BorderLayout.CENTER);
		modeCardLayout.show(modeCardPanel, modeChooserBoxModel.getElementAt(0));

		JButton button = new JButton("show popup menu");
		button.addActionListener(e -> showVPPopup(button));
		add(button, BorderLayout.SOUTH);
	}

	private JPanel getStandardPrimitivesPanel(ViewportListener viewportListener) {
		JPanel drawPrimitivesPanel = new JPanel(new GridLayout(16, 1));
		drawPrimitivesPanel.setBorder(BorderFactory.createTitledBorder("Draw"));

		drawPrimitivesPanel.add(getModeButton(viewportListener, "Plane", this::drawPlane));

//		ModeButton boxButton = new ModeButton("Box");
//		boxButton.addActionListener(e -> drawBox(boxButton, viewportListener));
//		drawPrimitivesPanel.add(boxButton);
		drawPrimitivesPanel.add(getModeButton(viewportListener, "Box", this::drawBox));

		JPanel spOptionsPanel = new JPanel(new GridLayout(16, 1));
		spOptionsPanel.setBorder(BorderFactory.createTitledBorder("Options"));

		JPanel standardPrimitivesPanel = new JPanel(new BorderLayout());
		standardPrimitivesPanel.add(drawPrimitivesPanel, BorderLayout.NORTH);
		standardPrimitivesPanel.add(spOptionsPanel, BorderLayout.CENTER);
		return standardPrimitivesPanel;
	}

	private ModeButton getModeButton(ViewportListener viewportListener, String text, BiConsumer<ModeButton, ViewportListener> action) {
		ModeButton modeButton = new ModeButton(text);
		modeButton.addActionListener(e -> action.accept(modeButton, viewportListener));
		return modeButton;
	}

	private void chooseAnimation(ItemEvent e) {
		if(e.getStateChange() == ItemEvent.SELECTED){
			ChooseableTimeRange<?> selectedItem = (ChooseableTimeRange<?>) animationChooserBox.getSelectedItem();
			if (selectedItem != null) {
				TimeEnvironmentImpl timeEnvironment = modelHandler == null ? null : modelHandler.getEditTimeEnv();
				selectedItem.applyTo(timeEnvironment);
			}
		}
	}

	public JPanel getMeshBasicsPanel(ViewportListener viewportListener) {
		JPanel meshBasicsPanel = new JPanel(new BorderLayout());

		JPanel drawToolsPanel = getDrawToolsPanel(viewportListener);
		meshBasicsPanel.add(drawToolsPanel, BorderLayout.NORTH);


		JPanel editToolsPanel = new JPanel(new GridLayout(16, 1));
		editToolsPanel.setBorder(BorderFactory.createTitledBorder("Manipulate"));


		for (ModeButton2 modeButton2 : ProgramGlobals.getActionTypeGroup().getModeButtons()) {
			editToolsPanel.add(modeButton2);
		}

		meshBasicsPanel.add(editToolsPanel, BorderLayout.CENTER);
		return meshBasicsPanel;

	}

	private JPanel getDrawToolsPanel(ViewportListener viewportListener) {
		JPanel drawToolsPanel = new JPanel(new GridLayout(2, 1));
		drawToolsPanel.setBorder(BorderFactory.createTitledBorder("Draw"));

		drawToolsPanel.add(getModeButton2(viewportListener, "Vertex", this::addVertex));
		drawToolsPanel.add(getModeButton2(viewportListener, "Face from Selection", this::createFace));
		drawToolsPanel.add(getModeButton2(viewportListener, "Bone", this::addBone));
		return drawToolsPanel;
	}

	private ModeButton2 getModeButton2(ViewportListener viewportListener, String text, BiConsumer<ModeButton2, ViewportListener> action) {
		ModeButton2 modeButton = new ModeButton2(text, programPreferences.getActiveColor1(), programPreferences.getActiveColor2());
		modeButton.addActionListener(e -> action.accept(modeButton, viewportListener));
		return modeButton;
	}

	private void createFace(ModeButton2 modeButton, ViewportListener viewportListener) {
		if (modelEditorManager == null) {
			return;
		}
		try {
			Viewport viewport = viewportListener.getViewport();
			Vec3 facingVector = viewport == null ? new Vec3(0, 0, 1) : viewport.getFacingVector();
			UndoAction createFaceFromSelection = ModelEditActions.createFaceFromSelection(modelHandler.getModelView(), facingVector);
			modelHandler.getUndoManager().pushAction(createFaceFromSelection.redo());
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

	private void drawBox(ModeButton modeButton, ViewportListener viewportListener) {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			DrawBoxActivity activity = new DrawBoxActivity(modelHandler, modelEditorManager, viewportListener, 1, 1, 1);
			modeButton.setColors(programPreferences.getActiveColor1(), programPreferences.getActiveColor2());
			modelPanel.changeActivity(activity);
		}
	}

	private void drawPlane(ModeButton modeButton, ViewportListener viewportListener) {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			DrawPlaneActivity activity = new DrawPlaneActivity(modelHandler, modelEditorManager, viewportListener, 1, 1, 1);
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
			editToolsPanel.add(tSpline, "wrap, growy");
		}

		editToolsPanel.add(new JLabel("UGG"), "wrap");
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

	public void setModelEditorManager(ModelEditorManager modelEditorManager) {
		this.modelEditorManager = modelEditorManager;
	}

	public void setCurrentModel(ModelHandler modelHandler) {
		this.modelHandler = modelHandler;
		if (modelHandler != null) {
			reloadAnimationList();
		}
	}
	public void setModelPanel(ModelPanel modelPanel) {
		if (modelPanel != null) {
			this.modelHandler = modelPanel.getModelHandler();
			this.modelEditorManager = modelPanel.getModelEditorManager();
			contextMenu = new ViewportPopupMenu(viewportListener.getViewport(), ProgramGlobals.getMainPanel(), modelPanel.getModelHandler(), modelPanel.getModelEditorManager());
			reloadAnimationList();
		} else {
			this.modelHandler = null;
			this.modelEditorManager = null;
			contextMenu = null;
		}
	}

	public void reloadAnimationList() {
		ChooseableTimeRange<?> selectedItem = (ChooseableTimeRange<?>) animationChooserBox.getSelectedItem();
		animationChooserBoxModel.removeAllElements();

//		ChooseableDoNothing doNothingItem = new ChooseableDoNothing("Custom Timeframe");
//		animationChooserBoxModel.addElement(doNothingItem);

//		ChooseableTimeRange<?> thingSelected = selectedItem == null ? doNothingItem : selectedItem;


		EditableModel model = modelHandler.getModel();
		for (Animation animation : model.getAnims()) {
			ChooseableAnimation choosableItem = new ChooseableAnimation(animation);
			animationChooserBoxModel.addElement(choosableItem);
		}

		for (GlobalSeq integer : model.getGlobalSeqs()) {
			ChooseableGlobalSeq chooseableItem = new ChooseableGlobalSeq(integer);
			animationChooserBoxModel.addElement(chooseableItem);
		}
//		animationChooserBox.setSelectedItem(thingSelected);
		if(selectedItem != null || animationChooserBoxModel.getSize() < 1){
			animationChooserBox.setSelectedItem(selectedItem);
		} else {
			animationChooserBox.setSelectedIndex(0);
		}
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
