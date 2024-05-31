package com.hiveworkshop.rms.ui.application.model.nodepanels;

import com.hiveworkshop.rms.editor.actions.animation.AddEventTrackAction;
import com.hiveworkshop.rms.editor.actions.animation.EditEventTrackAction;
import com.hiveworkshop.rms.editor.actions.animation.RemoveEventSequenceAction;
import com.hiveworkshop.rms.editor.actions.animation.RemoveEventTrackAction;
import com.hiveworkshop.rms.editor.actions.nodes.NameChangeAction;
import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.EventObject;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.application.model.ComponentsPanel;
import com.hiveworkshop.rms.ui.application.model.editors.IntEditorJSpinner;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.SequenceComboBoxRenderer;
import com.hiveworkshop.rms.util.TwiComboBox;
import com.hiveworkshop.rms.util.sound.EventMappings;
import com.hiveworkshop.rms.util.sound.EventTarget;
import com.hiveworkshop.rms.util.uiFactories.Button;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.TreeSet;

public class ComponentEventPanel extends ComponentIdObjectPanel<EventObject> {
	private final EventPreviewPanel eventPreviewPanel;
	private final JLabel eventName;
	private final JPanel tracksPanel;
	private final EventMappings eventMappings;

	public ComponentEventPanel(ModelHandler modelHandler, ComponentsPanel componentsPanel) {
		super(modelHandler, componentsPanel);
		eventPreviewPanel = new EventPreviewPanel();
		eventName = new JLabel("");
		topPanel.add(Button.create("Search Event", e -> searchEvent()), "wrap");
		topPanel.add(eventName, "wrap");
		topPanel.add(eventPreviewPanel, "wrap");

		tracksPanel = new JPanel(new MigLayout());
		topPanel.add(tracksPanel, "spanx, wrap");
		eventMappings = new EventMappings(null);
	}

	@Override
	public void updatePanels() {
		EventTarget event = eventMappings.getEvent(idObject.getName());
		eventPreviewPanel.setEvent(event);

		eventName.setText(EventObject.getEventName(idObject.getName()));
		updateTracksPanel();
	}

	private void updateTracksPanel() {
		tracksPanel.removeAll();
		for (Sequence sequence : new TreeSet<>(idObject.getEventTrackAnimMap().keySet())) {
			TreeSet<Integer> eventTrack = idObject.getEventTrack(sequence);
			if (eventTrack != null) {
				JPanel sequencePanel = getSequencePanel(sequence, eventTrack);
				tracksPanel.add(sequencePanel, "wrap");
			}
		}
		JButton addEventActionButton = new JButton("Add Sequence");
		addEventActionButton.addActionListener(e -> addSequence(getSequence()));
		tracksPanel.add(addEventActionButton, "wrap");
	}

	private JPanel getSequencePanel(Sequence sequence, TreeSet<Integer> eventTrack) {
		JPanel sequenceTrackPanel = new JPanel(new MigLayout("ins 0"));
		for (int track : eventTrack) {
			IntEditorJSpinner trackSpinner = new IntEditorJSpinner(track, Integer.MIN_VALUE, (i) -> editTrack(sequence, track, i));
			sequenceTrackPanel.add(trackSpinner, "");
			sequenceTrackPanel.add(getXButton(e -> removeTrack(sequence, track)), "wrap");
		}
		int newTrackTime = eventTrack.last() == null ? 0 : eventTrack.last() + 1;
		sequenceTrackPanel.add(getButton("Add", e -> addTrack(sequence, newTrackTime)), "wrap");

		JPanel sequencePanel = new JPanel(new MigLayout("ins 0"));
		sequencePanel.setBorder(BorderFactory.createTitledBorder("" + sequence));
		sequencePanel.add(getDeleteButton(e -> removeSequence(sequence)), "wrap");
		sequencePanel.add(sequenceTrackPanel);

		return sequencePanel;
	}
	private void addSequence(Sequence newSequence) {
		if (newSequence != null) {
			undoManager.pushAction(new AddEventTrackAction(idObject, newSequence, 0, changeListener).redo());
		}
	}
	private Sequence getSequence() {
		JPanel panel = new JPanel(new MigLayout());
		TwiComboBox<Sequence> animationBox = new TwiComboBox<>(new Animation("Stand and work for me", 0, 1));
		animationBox.addAll(model.getAnims());
		animationBox.addAll(model.getGlobalSeqs());
		animationBox.removeItem(idObject.getGlobalSeq());
		for (Sequence sequence : idObject.getEventTrackAnimMap().keySet()) {
			animationBox.removeItem(sequence);
		}
		animationBox.selectOrFirst(null);

		animationBox.setRenderer(new SequenceComboBoxRenderer(modelHandler));
		panel.add(animationBox);
		int opt = JOptionPane.showConfirmDialog(this, panel, "Add Event Track", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (opt == JOptionPane.OK_OPTION) {
			return animationBox.getSelected();
		}
		return null;
	}

	private void editTrack(Sequence sequence, int track, int newValue) {
		if (track != newValue) {
			undoManager.pushAction(new EditEventTrackAction(idObject, sequence, track, newValue, changeListener).redo());
		}
	}

	private void addTrack(Sequence sequence, int track) {
		undoManager.pushAction(new AddEventTrackAction(idObject, sequence, track, changeListener).redo());
	}

	private void removeTrack(Sequence sequence, int track) {
		if (idObject.getEventTrack(sequence).size() <= 1) {
			removeSequence(sequence);
		} else {
			undoManager.pushAction(new RemoveEventTrackAction(idObject, sequence, track, changeListener).redo());
		}
	}

	private void removeSequence(Sequence sequence) {
		undoManager.pushAction(new RemoveEventSequenceAction(idObject, sequence, changeListener).redo());
	}

	private void searchEvent() {
		EventBrowser eventBrowser = new EventBrowser(eventMappings);
		EventTarget event = eventMappings.getEvent(idObject.getName());
		SwingUtilities.invokeLater(() -> eventBrowser.setSelectedTarget(event));
		int confirmDialog = JOptionPane.showConfirmDialog(this, eventBrowser, "Search Event", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (confirmDialog == JOptionPane.OK_OPTION) {
			String newName = eventBrowser.getSelectedTargetFullTag();
			if (!newName.equals("") && !newName.equals(idObject.getName())) {
				System.out.println("setting new name to: " + newName);
				undoManager.pushAction(new NameChangeAction(idObject, newName, changeListener).redo());
			}
		}
	}

}
