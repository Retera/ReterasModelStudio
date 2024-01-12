package com.hiveworkshop.rms.ui.gui.modeledit.creator;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.actionfunctions.CreateFace;
import com.hiveworkshop.rms.ui.application.edit.animation.mdlvisripoff.TSpline;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.ViewportPopupMenu;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.creator.activity.*;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ModelEditorActionType3;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ToolbarButtonGroup2;
import com.hiveworkshop.rms.ui.util.ModeButton;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.function.Function;

public class CreatorModelingPanel extends JPanel {

	private ModelEditorManager modelEditorManager;
	private ModelHandler modelHandler;

	private JPopupMenu contextMenu;

	public CreatorModelingPanel() {
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

		drawPrimitivesPanel.add(new ModeButton("Plane").addConsumer(mb -> drawActivity(mb, at -> new DrawPlaneActivity(modelHandler, modelEditorManager, at).setNumSegs(1, 1))));
//		drawPrimitivesPanel.add(new ModeButton("Face").addConsumer(mb -> drawActivity(mb, at -> new DrawFaceActivity(modelHandler, modelEditorManager, at))));
		drawPrimitivesPanel.add(new ModeButton("Box").addConsumer(mb -> drawActivity(mb, at -> new DrawBoxActivity(modelHandler, modelEditorManager, at).setNumSegs(1, 1, 1))));

		JPanel spOptionsPanel = new JPanel(new MigLayout("ins 0, gap 0, fill", "[grow]"));
		spOptionsPanel.setBorder(BorderFactory.createTitledBorder("Options"));

		JPanel standardPrimitivesPanel = new JPanel(new MigLayout("fill, ins 0, gap 0, wrap 1", "[grow]"));
		standardPrimitivesPanel.add(drawPrimitivesPanel, "growx");
		standardPrimitivesPanel.add(spOptionsPanel, "growx");

		return standardPrimitivesPanel;
	}

	private JPanel getDrawToolsPanel() {
		JPanel drawToolsPanel = new JPanel(new MigLayout("wrap 1, gap 0, ins 0, fill"));
		drawToolsPanel.setBorder(BorderFactory.createTitledBorder("Draw"));

		drawToolsPanel.add(new ModeButton("Vertex").addConsumer(mb -> drawActivity(mb, at -> new DrawVertexActivity(modelHandler, modelEditorManager, at))));
		drawToolsPanel.add(new ModeButton("Face from Selection").addConsumer(mb -> CreateFace.createFace(modelHandler)));
		drawToolsPanel.add(new ModeButton("Bone").addConsumer(mb -> drawActivity(mb, at -> new DrawBoneActivity(modelHandler, modelEditorManager, at))));
		return drawToolsPanel;
	}


	private void drawActivity(ModeButton modeButton, Function<ModelEditorActionType3, DrawActivity> drawActivityFunction) {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			ModelEditorActionType3 actionType = modelPanel.getEditorActionType();
			DrawActivity activity = drawActivityFunction.apply(actionType).setOnActivityEnded(() -> modeButton.setActive(false));
			modeButton.setActive(true);
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
