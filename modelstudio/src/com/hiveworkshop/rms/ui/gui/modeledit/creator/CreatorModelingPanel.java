package com.hiveworkshop.rms.ui.gui.modeledit.creator;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.actionfunctions.CreateFace;
import com.hiveworkshop.rms.ui.application.edit.animation.mdlvisripoff.TSpline;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.ViewportPopupMenu;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.creator.activity.*;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ModeButton2;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ModelEditorActionType3;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ToolbarButtonGroup2;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.ui.util.ModeButton;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.function.Consumer;

public class CreatorModelingPanel extends JPanel {
	private static final String ANIMATIONBASICS = "ANIMATIONBASICS";

	private ModelEditorManager modelEditorManager;
	private final ProgramPreferences programPreferences;
	private ModelHandler modelHandler;


	private JPopupMenu contextMenu;

	public CreatorModelingPanel() {
		this.programPreferences = ProgramGlobals.getPrefs();
		setLayout(new MigLayout("ins 0, hidemode 2", "", "[grow][][]"));

		JButton popupMenuButton = new JButton("show popup menu");
		popupMenuButton.addActionListener(e -> showVPPopup(popupMenuButton));

		add(popupMenuButton, "wrap");
		add(getDrawToolsPanel(), "wrap, growx");
		add(getStandardPrimitivesPanel(), "wrap, growx");
	}

	private JPanel getStandardPrimitivesPanel() {
		JPanel drawPrimitivesPanel = new JPanel(new MigLayout("ins 0, gap 0, wrap 1, fill", "[grow]", ""));
		drawPrimitivesPanel.setBorder(BorderFactory.createTitledBorder("Draw"));
		drawPrimitivesPanel.add(getModeButton("Plane", this::drawPlane));
//		drawPrimitivesPanel.add(getModeButton("Face", this::drawFace));
		drawPrimitivesPanel.add(getModeButton("Box", this::drawBox));

		JPanel spOptionsPanel = new JPanel(new MigLayout("ins 0, gap 0, fill", "[grow]"));
		spOptionsPanel.setBorder(BorderFactory.createTitledBorder("Options"));

		JPanel standardPrimitivesPanel = new JPanel(new MigLayout("fill, ins 0, gap 0, wrap 1", "[grow]"));
		standardPrimitivesPanel.add(drawPrimitivesPanel, "growx");
		standardPrimitivesPanel.add(spOptionsPanel, "growx");
//		standardPrimitivesPanel.add(drawPrimitivesPanel, "");
//		standardPrimitivesPanel.add(spOptionsPanel, "");

		return standardPrimitivesPanel;
	}

	private ModeButton getModeButton(String text, Consumer<ModeButton> action) {
		ModeButton modeButton = new ModeButton(text);
		modeButton.addActionListener(e -> action.accept(modeButton));
		return modeButton;
	}

	public JPanel getMeshBasicsPanel() {
		JPanel meshBasicsPanel = new JPanel(new MigLayout("wrap 1, ins 0, fill", "[grow]", "[][][grow]"));
		meshBasicsPanel.add(getDrawToolsPanel(), "growx");
//		meshBasicsPanel.add(getEditToolsPanel(), "growx");
		return meshBasicsPanel;

	}

	private JPanel getEditToolsPanel() {
		JPanel editToolsPanel = new JPanel(new MigLayout("wrap 1, gap 0, ins 0, fill", "[grow]", ""));
		editToolsPanel.setBorder(BorderFactory.createTitledBorder("Manipulate"));

		for (ModeButton2 modeButton2 : ProgramGlobals.getActionTypeGroup().getModeButtons()) {
			editToolsPanel.add(modeButton2);
		}
		return editToolsPanel;
	}

	private JPanel getDrawToolsPanel() {
		JPanel drawToolsPanel = new JPanel(new MigLayout("wrap 1, gap 0, ins 0, fill"));
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
		CreateFace.createFace(modelHandler);
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
			DrawVertexActivity activity = new DrawVertexActivity(modelHandler, modelEditorManager, modelPanel.getEditorActionType());
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
	private void drawFace(ModeButton modeButton) {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			DrawFaceActivity activity = new DrawFaceActivity(modelHandler, modelEditorManager);
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

		animationBasicsPanel.add(editToolsPanel, "growx, growy, wrap");

		boolean temp = false;
//		boolean temp = true;
		if (temp) {
			TSpline tSpline = new TSpline();
			animationBasicsPanel.add(tSpline, "wrap, growy");
		}

//		editToolsPanel.add(new JLabel("UGG"), "wrap");
		return animationBasicsPanel;
	}

	private void showVPPopup(JButton button) {
		if (contextMenu != null) {
			contextMenu.show(button, 0, 0);
		}
	}

	public void setAnimationModeState(boolean animationModeState) {
//		modeCardPanel.show(animationModeState ? "ANIM" : "MESH");
	}

	public void setModelPanel(ModelPanel modelPanel) {
		if (modelPanel != null) {
			this.modelHandler = modelPanel.getModelHandler();
			this.modelEditorManager = modelPanel.getModelEditorManager();
//			contextMenu = new ViewportPopupMenu(viewportListener.getViewport(), ProgramGlobals.getMainPanel(), modelPanel.getModelHandler(), modelPanel.getModelEditorManager());
			contextMenu = new ViewportPopupMenu(null, ProgramGlobals.getMainPanel(), modelPanel.getModelHandler(), modelPanel.getModelEditorManager());

		} else {
			this.modelHandler = null;
			this.modelEditorManager = null;
			contextMenu = null;
		}
	}

	public void updateAnimationList() {
	}
}
