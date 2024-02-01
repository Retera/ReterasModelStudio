package com.hiveworkshop.rms.ui.application.edit.animation;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.animation.AddSequenceAction;
import com.hiveworkshop.rms.editor.actions.animation.DuplicateAnimationAction;
import com.hiveworkshop.rms.editor.actions.animation.RemoveSequenceAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.GlobalSeq;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;
import com.hiveworkshop.rms.ui.application.model.editors.TwiTextField;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.util.SearchableTwiList;
import com.hiveworkshop.rms.ui.util.TwiList;
import com.hiveworkshop.rms.util.FramePopup;
import com.hiveworkshop.rms.util.uiFactories.Button;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;

public class TimeBoundChooserPanel extends JPanel {
	private final ModelStructureChangeListener changeListener = ModelStructureChangeListener.changeListener;
	private final ModelHandler modelHandler;
	private final UndoManager undoManager;
	private final EditableModel model;
	private final JTabbedPane tabs;
	private Animation selectedAnim;
	private Collection<Animation> selectedAnims;
	private Collection<GlobalSeq> selectedGlobalSeqs;
	private GlobalSeq selectedGlobalSeq;

	public TimeBoundChooserPanel(ModelHandler modelHandler) {
		super(new BorderLayout());
		this.modelHandler = modelHandler;
		this.undoManager = modelHandler.getUndoManager();
		this.model = modelHandler.getModel();

		tabs = new JTabbedPane();
		tabs.addTab("Animation", getAnimationPanel());
		tabs.addTab("Global Sequence", getGlobSeqPanel());
		add(tabs);
	}

	private JPanel getAnimationPanel() {
		final JPanel animationPanel = new JPanel(new MigLayout("fill", "[]", "[][grow][]"));

		SearchableTwiList<Animation> animationBox2 = new SearchableTwiList<>(model.getAnims(), this::searchAnim);
		animationBox2.addSelectionListener1(o -> selectedAnim = o);
		animationBox2.addMultiSelectionListener(o -> selectedAnims = o);

		animationPanel.add(animationBox2.getSearchField(), "spanx, growx, wrap");
		JScrollPane animationScrollPane2 = animationBox2.getScrollableList();
		animationScrollPane2.setPreferredSize(new Dimension(500, 320));
		animationPanel.add(animationScrollPane2, "spanx, growx, growy, wrap");

		JPanel buttonPanel = new JPanel(new MigLayout("ins 0"));
		buttonPanel.add(Button.create("Create", e -> showAddPopup()));
		buttonPanel.add(Button.create("Duplicate", e -> duplicateAnimation(selectedAnims)));
		buttonPanel.add(Button.create("Edit", e -> showEditPopup(selectedAnim)));
		buttonPanel.add(Button.create("Delete", e -> deleteSequences(selectedAnims, "Animation")));

		animationPanel.add(buttonPanel);
		return animationPanel;
	}

	private boolean searchAnim(Animation a, String s) {
		String prefix = s.matches("[A-Z].*") ? "" : ".*";
		String r = prefix + s.toLowerCase() + ".*";
		return a.getName().toLowerCase().matches(r);
	}
	public void showAddPopup() {
		CreateAnimationPopup createAnimationPopup = new CreateAnimationPopup(modelHandler, null);
		int result = JOptionPane.showConfirmDialog(this, createAnimationPopup,
				"Create Animation", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			Animation sequence = createAnimationPopup.getNewAnimation();
			AddSequenceAction addSequenceAction = new AddSequenceAction(modelHandler.getModel(), sequence, null);
			UndoAction action = new CompoundAction(addSequenceAction.actionName(), List.of(addSequenceAction), this::updateOnChange);
			modelHandler.getUndoManager().pushAction(action.redo());
		}
	}
	public void showEditPopup(Animation selectedAnim) {
		if (selectedAnim != null && model.contains(selectedAnim)) {
			CreateAnimationPopup createAnimationPopup = new CreateAnimationPopup(modelHandler, selectedAnim);
			String title = "Edit " + selectedAnim.getName() + " (" + selectedAnim.getLength() + ")";
			int result = JOptionPane.showConfirmDialog(this, createAnimationPopup,
					title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
			if (result == JOptionPane.OK_OPTION) {
				List<UndoAction> actions = createAnimationPopup.getEditAnimActions();
				String actionName = actions.size() == 1 ? actions.get(0).actionName() : "Edit \"" + selectedAnim.getName() + "\"";
				UndoAction action = new CompoundAction(actionName, actions, this::updateOnChange);
				modelHandler.getUndoManager().pushAction(action.redo());
			}
		}
	}

	private JPanel getGlobSeqPanel() {
		final JPanel globSeqPanel = new JPanel(new MigLayout("fill"));

		TwiList<GlobalSeq> globalSeqBox = new TwiList<>(model.getGlobalSeqs());
		globalSeqBox.addSelectionListener1(o -> selectedGlobalSeq = o);
		globalSeqBox.addMultiSelectionListener(o -> selectedGlobalSeqs = o);

		JScrollPane globalSeqScrollPane = new JScrollPane(globalSeqBox);
		globalSeqScrollPane.setPreferredSize(new Dimension(500, 320));
		globSeqPanel.add(globalSeqScrollPane, "spanx, growx, growy");

		globSeqPanel.add(Button.create("Create", e -> GlobalSeqHelper.showNewGlobSeqPopup(this, "Enter Length", modelHandler)));
		globSeqPanel.add(Button.create("Delete", e -> deleteSequences(selectedGlobalSeqs, "GlobalSeq")));
		return globSeqPanel;
	}

	private void deleteSequences(Collection<? extends Sequence> sequences, String type) {
		if (sequences != null) {
			List<UndoAction> deleteActions = new ArrayList<>();
			for (Sequence sequence : sequences) {
				deleteActions.add(new RemoveSequenceAction(model, sequence, null));
			}
			String actionName = deleteActions.size() == 1 ? deleteActions.get(0).actionName() : "Delete " + deleteActions.size() + " " + type + "s";
			UndoAction undoAction = new CompoundAction(actionName, deleteActions, this::updateOnChange);
			undoManager.pushAction(undoAction.redo());
		}
	}

	private void duplicateAnimation(Collection<? extends Sequence> sequences) {
		Map<Sequence, String> newNames = getNewNames(sequences);
		if (newNames != null) {
			List<UndoAction> undoActions = new ArrayList<>();
			for (Sequence animation : newNames.keySet()) {
				undoActions.add(new DuplicateAnimationAction(model, animation, newNames.get(animation), null));
			}
			String actionName = undoActions.size() == 1 ? undoActions.get(0).actionName() : "Duplicate " + undoActions.size() + " Animations";
			CompoundAction action = new CompoundAction(actionName, undoActions, this::updateOnChange);
			undoManager.pushAction(action.redo());
		}
	}

	private Map<Sequence, String> getNewNames(Collection<? extends Sequence> sequences) {
		if (sequences != null && !sequences.isEmpty()) {
			JPanel panel = new JPanel(new MigLayout());
			Map<Sequence, String> nameMap = new LinkedHashMap<>();
			for (Sequence sequence : sequences) {
				String newName = sequence.getName() + " Copy";
				nameMap.put(sequence, newName);
				panel.add(new JLabel(sequence.getName()));
				panel.add(new TwiTextField(newName, 24, s -> nameMap.put(sequence, s)), "wrap");
			}

			String title = nameMap.size() == 1 ? "Choose new animation name" : "Choose new animation names";
			int userChoice = JOptionPane.showConfirmDialog(TimeBoundChooserPanel.this, panel,
					title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

			if (userChoice == JOptionPane.OK_OPTION) {
				return nameMap;
			}
		}
		return null;
	}

	private void updateOnChange() {
		changeListener.animationParamsChanged();
		repaint();
	}

	public static void showPopup(JComponent parent) {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			showPopup(modelPanel.getModelHandler(), parent);
		}
	}
	public static void showPopup(ModelHandler modelHandler, JComponent parent) {
		parent = parent == null ? ProgramGlobals.getMainPanel() : parent;
		TimeBoundChooserPanel tbcPanel = new TimeBoundChooserPanel(modelHandler);
		JFrame frame = FramePopup.get(tbcPanel, parent, "Quick Edit Animations");
		ProgramGlobals.linkActions(tbcPanel);
		frame.setVisible(true);

	}
}
