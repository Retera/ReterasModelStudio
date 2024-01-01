package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.SetKeyframeAction_T;
import com.hiveworkshop.rms.editor.actions.animation.animFlag.AddAnimFlagAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.GlobalSeq;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.editor.model.animflag.QuatAnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.application.edit.animation.TransformCalculator;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.FramePopup;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.ScreenInfo;
import com.hiveworkshop.rms.util.Vec3;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class GlobalTransformPanel extends JPanel {
	private final ModelIconHandler iconHandler = new ModelIconHandler();
	private ModelHandler modelHandler;
	private UndoManager undoManager;
	private ModelView modelView;
	private RenderModel renderModel;
	private TimeEnvironmentImpl timeEnvironment;
	private JPanel transformsPanel;
	private JTextArea infoLabel;
	private final ModelStructureChangeListener changeListener;
	private String nodeColLayout = "[][grow, left][right]";
	private final TransformCalculator transformCalculator = new TransformCalculator();

	public GlobalTransformPanel(ModelHandler modelHandler){
		super(new MigLayout("fill", "[grow]", "[grow][]"));
		setPreferredSize(ScreenInfo.getSmallWindow());
		this.modelHandler = modelHandler;
		this.renderModel = modelHandler.getRenderModel();
		this.modelView = modelHandler.getModelView();
		this.undoManager = modelHandler.getUndoManager();
		timeEnvironment = renderModel.getTimeEnvironment();
//		new TwiTextArea();


		transformsPanel = new JPanel(new MigLayout("fill", "[grow]", "[]"));
		JScrollPane scrollPane = new JScrollPane(transformsPanel);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		add(scrollPane, "growx, growy, wrap");

		infoLabel = getInfoLabel();
		transformsPanel.add(infoLabel, "growx, wrap");
		transformsPanel.add(new JLabel(""), "growy");

		JButton fetchTransforms = new JButton("Fetch Transforms");
		fetchTransforms.addActionListener(e -> fetchTransforms());
		add(fetchTransforms);
		changeListener = ModelStructureChangeListener.changeListener;
	}

	private JTextArea getInfoLabel(){
		JTextArea infoLabel = new JTextArea(
				"To prime:"
						+ "\nSelect one or more nodes in the 3D editor and press \"Fetch Transforms\". "
						+ "This will load the global transforms (location, rotation and scaling) "
						+ "of all selected nodes at the current keyframe in the current animation."
						+ "\n(These values will stay loaded until the button is pressed again or this window is closed. "
						+ "They will not change if the animation time change or an other animation is chosen)"
						+ "\n\nTo use:"
						+ "\nSelect one or more nodes in the 3D editor and press the button in this window corresponding to the transform you want to use.");
		infoLabel.setEditable(false);
		infoLabel.setOpaque(false);
		infoLabel.setLineWrap(true);
		infoLabel.setWrapStyleWord(true);
		return infoLabel;
	}
	private void fetchTransforms(){
		transformsPanel.removeAll();
		if(modelView.getSelectedIdObjects().isEmpty()){
			transformsPanel.add(infoLabel);
		} else {
			transformCalculator.setSequence(timeEnvironment.getCurrentSequence()).setTrackTime(timeEnvironment.getEnvTrackTime());
			transformsPanel.add(new JLabel(getLabelText()), "center, spanx");
//		transformsPanel.add(new JLabel("Node"), "wrap");
			for(IdObject idObject : modelView.getSelectedIdObjects()){
				transformsPanel.add(getTransformPanel(idObject), "growx, wrap");
			}

		}
		transformsPanel.add(new JLabel(""), "growy");
		revalidate();
		repaint();
	}

	private String getLabelText() {
		Sequence currentSequence = timeEnvironment.getCurrentSequence();
		if(currentSequence != null){
			String seqName;
			if(currentSequence instanceof Animation){
				 seqName = ((Animation) currentSequence).getName();
			} else {
				seqName = "GlobalSeq " + modelHandler.getModel().getGlobalSeqId((GlobalSeq) currentSequence);
			}
			int trackTime = timeEnvironment.getEnvTrackTime();
			return  "Global Transforms (LocRotScale) fetched at " + trackTime + " in " + seqName;
		}
		return "Pivot points fetched!";
	}

	private JPanel getTransformPanel(IdObject idObject){
		JPanel panel = new JPanel(new MigLayout("", nodeColLayout, ""));
		TransformCalculator.GlobTransContainer transform2 = transformCalculator.getGlobalTransform(idObject);

		panel.add(new JLabel(iconHandler.getImageIcon(idObject, modelHandler.getModel())));
		panel.add(new JLabel(idObject.getName()), "");

//		JButton useTransform = new JButton("Use Loaded Transform For Selected");
		JButton useTransform = new JButton("Apply To Selected");
		useTransform.addActionListener(e -> useTransform(transform2));
		panel.add(useTransform, "");
		return panel;
	}

	private void useTransform(TransformCalculator.GlobTransContainer transform){
		List<UndoAction> actions = new ArrayList<>();
		int trackTime = timeEnvironment.getEnvTrackTime();
		transformCalculator.setSequence(timeEnvironment.getCurrentSequence()).setTrackTime(trackTime);
		for(IdObject idObject : modelView.getSelectedIdObjects()){
			TransformCalculator.TransformContainer newTransforms = transformCalculator.getNewTransforms(idObject, transform);

			if(!newTransforms.getTrans().equalLocs(Vec3.ZERO)){
				AnimFlag<Vec3> translationFlag = idObject.getTranslationFlag();
				if(translationFlag == null) translationFlag = new Vec3AnimFlag(MdlUtils.TOKEN_TRANSLATION);
				actions.add(getUndoAction(trackTime, idObject, translationFlag, newTransforms.getTrans()));
			}

			if (!newTransforms.getRot().equals(Quat.IDENTITY)){
				AnimFlag<Quat> rotationFlag = idObject.getRotationFlag();
				if(rotationFlag == null) rotationFlag = new QuatAnimFlag(MdlUtils.TOKEN_ROTATION);
				actions.add(getUndoAction(trackTime, idObject, rotationFlag, newTransforms.getRot()));
			}
			if(!newTransforms.getScale().equalLocs(Vec3.ONE)){
				AnimFlag<Vec3> scalingFlag = idObject.getScalingFlag();
				if(scalingFlag == null) scalingFlag = new Vec3AnimFlag(MdlUtils.TOKEN_SCALING);
				actions.add(getUndoAction(trackTime, idObject, scalingFlag, newTransforms.getScale()));
			}
		}
		undoManager.pushAction(new CompoundAction("paste global transform", actions, changeListener::keyframesUpdated).redo());
	}


	private <T> UndoAction getUndoAction(int trackTime, IdObject idObject, AnimFlag<T> flag, T transform) {
		// only paste to selected nodes

		Entry<T> newEntry = new Entry<>(trackTime, transform);

		if (flag.tans()) {
			Entry<T> entryIn = flag.getFloorEntry(trackTime, timeEnvironment.getCurrentSequence());
			Entry<T> entryOut = flag.getCeilEntry(trackTime, timeEnvironment.getCurrentSequence());
			int animationLength = timeEnvironment.getCurrentSequence().getLength();
			float[] tcbFactor = flag.getTcbFactor(0.5f, 0, 0);
			flag.calcNewTans(tcbFactor, entryOut, entryIn, newEntry, animationLength);
			System.out.println("calc tans! " + entryIn + entryOut + newEntry);
		}

		if(idObject.owns(flag)){
			return new SetKeyframeAction_T<>(flag, newEntry, timeEnvironment.getCurrentSequence(), null);
		} else {
			flag.addEntry(newEntry, timeEnvironment.getCurrentSequence());
			return new AddAnimFlagAction<>(idObject, flag, null);
		}
	}


	public static void show(JComponent parent, ModelHandler modelHandler) {
		GlobalTransformPanel panel = new GlobalTransformPanel(modelHandler);
		FramePopup.show(panel, parent, "Load and use global transforms for " + modelHandler.getModel().getName());
	}
}
