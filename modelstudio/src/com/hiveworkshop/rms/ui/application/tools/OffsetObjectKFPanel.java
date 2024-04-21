package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.OffsetSequenceAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.EventObject;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.Pair;
import com.hiveworkshop.rms.util.SmartNumberSlider;
import com.hiveworkshop.rms.util.uiFactories.Button;
import com.hiveworkshop.rms.util.uiFactories.CheckBox;
import com.hiveworkshop.rms.util.uiFactories.FontHelper;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.List;
import java.util.*;

public class OffsetObjectKFPanel extends JPanel{
	private final static Color bg = new Color(128, 128, 128, 50);
	private final static Color bg2 = new Color(128, 128, 128, 0);
	static Map<String, Boolean> transformTypeMap = new LinkedHashMap<>();
	static {
		transformTypeMap.put("Translation", true);
		transformTypeMap.put("Scaling", true);
		transformTypeMap.put("Rotation", true);
		transformTypeMap.put("Other", true);
	}
	static Map<Class<? extends IdObject>, Boolean> objTypeMap = new LinkedHashMap<>();
	static {
		objTypeMap.put(Bone.class, true);
		objTypeMap.put(Light.class, true);
		objTypeMap.put(Helper.class, true);
		objTypeMap.put(ParticleEmitter.class, true);
		objTypeMap.put(ParticleEmitter2.class, true);
		objTypeMap.put(ParticleEmitterPopcorn.class, true);
		objTypeMap.put(RibbonEmitter.class, true);
		objTypeMap.put(EventObject.class, true);
		objTypeMap.put(CollisionShape.class, true);
		objTypeMap.put(Attachment.class, true);
//			objTypeMap.put(Geoset.class, true);
//			doMap.put(TextureAnim.class, true);
//			doMap.put(Material.class, true);
	}
	private static boolean onlySBool = true;
	private static boolean wrapKfs = true;

	private final Map<Sequence, Integer> sequenceOffsetMap = new HashMap<>();
	private final ModelHandler modelHandler;
	private final UndoManager undoManager;
	public OffsetObjectKFPanel(ModelHandler modelHandler) {
		super(new MigLayout("ins 0, fill", "[grow]", "[grow]"));
		this.modelHandler = modelHandler;
		undoManager = modelHandler.getUndoManager();
		EditableModel model = modelHandler.getModel();

		JPanel panelInner = new JPanel(new MigLayout("ins 0, fill"));
		panelInner.setBorder(new TitledBorder(""));
		panelInner.add(CheckBox.create("Only selected nodes", onlySBool, b -> onlySBool = b), "");

		JCheckBox wrap_keyframes = CheckBox.create("Wrap Keyframes", wrapKfs, b -> wrapKfs = b);
		CheckBox.setTooltip(wrap_keyframes, "Add keyframes moved past the start/end of the animation to the other side of the animation");
		panelInner.add(wrap_keyframes, "wrap");
		JPanel panel = new JPanel(new MigLayout("fill, ins 2"));
		panel.add(panelInner, "growx");

		JPanel objTypePanel = new JPanel(new MigLayout("fill, ins 0, wrap 3", "[grow][grow]"));
		objTypePanel.setBorder(new TitledBorder("Node types to affect"));
		for (Class<? extends IdObject> nodeClass : objTypeMap.keySet()) {
			objTypePanel.add(CheckBox.create(nodeClass.getSimpleName(), objTypeMap.get(nodeClass), b -> objTypeMap.put(nodeClass, b)), "");
		}
		add(objTypePanel, "growx, spanx, wrap");


		JPanel typePanel = new JPanel(new MigLayout("fill, ins 0"));
		typePanel.setBorder(BorderFactory.createTitledBorder("Transforms to affect"));
		add(typePanel, "growx, spanx, wrap");

		for (String animType : transformTypeMap.keySet()) {
			typePanel.add(CheckBox.create(animType, transformTypeMap.get(animType), b -> transformTypeMap.put(animType, b)), "");
		}

		JPanel animationsPanel = new JPanel(new MigLayout("ins 0, gap 0, fill"));
		boolean isOddRow = false;
		for (Animation anim : model.getAnims()) {
			sequenceOffsetMap.put(anim, 0);
			JPanel animaRowPanel = getSequenceRowPanel(model, isOddRow, anim);
			animationsPanel.add(animaRowPanel, "growx, wrap");
			isOddRow = !isOddRow;
		}
		for (GlobalSeq globalSeq : model.getGlobalSeqs()) {
			sequenceOffsetMap.put(globalSeq, 0);
			JPanel animaRowPanel = getSequenceRowPanel(model, isOddRow, globalSeq);
			animationsPanel.add(animaRowPanel, "growx, wrap");
			isOddRow = !isOddRow;
		}

		JScrollPane scrollPane = new JScrollPane(animationsPanel);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		add(scrollPane, "spanx 2, growx, growy, wrap");

		add(panel, "right, growx");
		add(FontHelper.set(Button.create(" >>  Apply  << ", e -> applyAction()), Font.BOLD, 16f), "center, wrap");
	}

	private JPanel getSequenceRowPanel(EditableModel model, boolean isOddRow, Sequence sequence) {
		SmartNumberSlider numberSlider = new SmartNumberSlider("" + sequence.getLength(), 0, -sequence.getLength(), sequence.getLength(), v -> sequenceOffsetMap.put(sequence, v), false, false);
		JPanel animaRowPanel = new JPanel(new MigLayout("ins 2, fill", "[grow, growprio 150, sg sgName][grow, sg sgSlider, right][sg sg25, right][sg sg25, right][sg sgReset, right]"));
		if (isOddRow) {
			animaRowPanel.setBackground(bg);
			numberSlider.setBackground(bg2);
			numberSlider.setOpaque(false);
			animaRowPanel.setOpaque(true);
		}
		animaRowPanel.add(new JLabel(getSeqNumbName(model, sequence)), "growx");
		animaRowPanel.add(numberSlider, "growx");
		animaRowPanel.add(Button.create("-25%", e -> numberSlider.setValue(sequenceOffsetMap.get(sequence)-(int)((sequence.getLength()*.25)))), "");
		animaRowPanel.add(Button.create("+25%", e -> numberSlider.setValue(sequenceOffsetMap.get(sequence)+(int)((sequence.getLength()*.25)))), "");
		animaRowPanel.add(Button.create("Reset", e -> numberSlider.setValue(0)), "");
		return animaRowPanel;
	}

	private String getSeqNumbName(EditableModel model, Sequence sequence) {
		if (sequence instanceof Animation) {
			return "(" + model.getId(sequence) + ") " + sequence;
		} else if (sequence instanceof GlobalSeq) {
			return "(" + model.getId(sequence) + ") " + sequence;
		}
		return "" + sequence;
	}

	private void applyAction() {
		List<UndoAction> actions = getOffsetActions(getSequenceToOffsetAnimFlagsMap());
		if (!actions.isEmpty()) {
			CompoundAction offsetKeyframes = new CompoundAction("OffsetKeyframes", actions, ModelStructureChangeListener.changeListener::nodesUpdated);
			undoManager.pushAction(offsetKeyframes.redo());
		}
	}

	private List<UndoAction> getOffsetActions(Map<Sequence, Pair<Integer, List<AnimFlag<?>>>> sequenceOffsetMap) {
		List<UndoAction> actions = new ArrayList<>();
		for (Sequence sequence : sequenceOffsetMap.keySet()) {
			Pair<Integer, List<AnimFlag<?>>> offsetToFlags = sequenceOffsetMap.get(sequence);
			Integer offset = offsetToFlags.getFirst();
			List<AnimFlag<?>> animFlags = offsetToFlags.getSecond();
			if (!animFlags.isEmpty()) {
				actions.add(new OffsetSequenceAction(animFlags, sequence, offset, wrapKfs, null));
			}
		}
		return actions;
	}

	private Map<Sequence, Pair<Integer, List<AnimFlag<?>>>> getSequenceToOffsetAnimFlagsMap() {
		Set<AnimFlag<?>> flags = new HashSet<>();
		Set<IdObject> nodes = new HashSet<>();
		if (onlySBool) {
			ModelView modelView = modelHandler.getModelView();
			modelView.getSelectedIdObjects().stream().filter(n -> objTypeMap.get(n.getClass())).forEach(nodes::add);
		} else {
			EditableModel model = modelHandler.getModel();
			model.getIdObjects().stream().filter(n -> objTypeMap.containsKey(n.getClass()) && objTypeMap.get(n.getClass())).forEach(nodes::add);
		}
		for (IdObject node : nodes) {
			for (AnimFlag<?> animFlag : node.getAnimFlags()) {
				if (transformTypeMap.get(animFlag.getName())) {
					flags.add(animFlag);
				}
			}
		}

		Map<Sequence, Pair<Integer, List<AnimFlag<?>>>> sequenceOffsetMap = new HashMap<>();
		for (Sequence sequence : this.sequenceOffsetMap.keySet()) {
			Integer offset = this.sequenceOffsetMap.get(sequence);
			if (offset != 0) {
				List<AnimFlag<?>> animFlags = flags.stream().filter(flag -> flag.hasSequence(sequence) && 0 < flag.size(sequence)).toList();
				if (!animFlags.isEmpty()) {
					sequenceOffsetMap.put(sequence, new Pair<>(offset, animFlags));
				}
			}
		}
		return sequenceOffsetMap;
	}
}
