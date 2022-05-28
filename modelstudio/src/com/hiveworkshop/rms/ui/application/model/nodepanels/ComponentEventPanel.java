package com.hiveworkshop.rms.ui.application.model.nodepanels;

import com.hiveworkshop.rms.editor.actions.animation.AddEventTrackAction;
import com.hiveworkshop.rms.editor.actions.animation.EditEventTrackAction;
import com.hiveworkshop.rms.editor.actions.animation.RemoveEventSequenceAction;
import com.hiveworkshop.rms.editor.actions.animation.RemoveEventTrackAction;
import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.EventObject;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.application.model.editors.IntEditorJSpinner;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.SequenceComboBoxRenderer;
import com.hiveworkshop.rms.util.TwiComboBox;
import com.hiveworkshop.rms.util.sound.Sound;
import com.hiveworkshop.rms.util.sound.SoundPlayer;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.TreeSet;
import java.util.function.Consumer;

public class ComponentEventPanel extends ComponentIdObjectPanel<EventObject> {
	private final JPanel soundsPanel;
	private final JLabel eventName;
	private final JPanel tracksPanel;

	public ComponentEventPanel(ModelHandler modelHandler) {
		super(modelHandler);
		soundsPanel = new JPanel(new MigLayout());
		eventName = new JLabel("");
		topPanel.add(eventName, "wrap");
		topPanel.add(soundsPanel, "wrap");

		tracksPanel = new JPanel(new MigLayout());
		topPanel.add(tracksPanel, "spanx, wrap");
//		trackPanel = new IntegerValuePanel(modelHandler, "EventTrack");
//		topPanel.add(trackPanel, "spanx, growx, wrap");
	}

	@Override
	public void updatePanels() {
		soundsPanel.removeAll();
		Sound sound = ProgramGlobals.getSoundMappings().getSound(idObject.getName());
		if (sound != null) {
			soundsPanel.add(new JLabel("Name: " + sound.getSoundName()), "wrap, gapbottom 10");
			String[] soundPaths = sound.getFilePaths();
			String[] soundNames = sound.getFileNames();
//			System.out.println("got paths");
			for (int i = 0; i < soundPaths.length; i++) {
				makeSoundButton(soundPaths[i], soundNames[i]);
			}
		}
		eventName.setText(EventObject.getEventName(idObject.getName()));
		updateTracksPanel();
//		trackPanel.reloadNewValue(0, idObject.);
	}

	private void makeSoundButton(String path, String name) {
		soundsPanel.add(new JLabel(name));
		soundsPanel.add(getButton("play", e -> SoundPlayer.play(path)), "wrap");
		soundsPanel.repaint();
		System.out.println("got sound: " + path);
	}

	private void updateTracksPanel() {
		tracksPanel.removeAll();
//		tracksPanel.add(new JLabel("Animation"));
//		tracksPanel.add(new JLabel("Event Start Time"), "wrap");
		for (Sequence sequence : new TreeSet<>(idObject.getEventTrackAnimMap().keySet())) {
			TreeSet<Integer> eventTrack = idObject.getEventTrack(sequence);
			if (eventTrack != null) {
				JPanel sequencePanel = getSequencePanel(sequence, eventTrack);
				tracksPanel.add(sequencePanel, "wrap");
			}
		}
		JButton addEventActionButton = new JButton("Add Sequence");
		addEventActionButton.addActionListener(e -> addEventAction());
		tracksPanel.add(addEventActionButton, "wrap");
	}

	private JPanel getSequencePanel(Sequence sequence, TreeSet<Integer> eventTrack) {
		JPanel sequenceTrackPanel = new JPanel(new MigLayout("ins 0"));
		for (int track : eventTrack) {
			Consumer<Integer> integerConsumer = (i) -> editingStoppedListener(sequence, track, i);
			IntEditorJSpinner trackSpinner = new IntEditorJSpinner(track, Integer.MIN_VALUE, integerConsumer);
			sequenceTrackPanel.add(trackSpinner, "");
			sequenceTrackPanel.add(getXButton(e -> removeTrack(sequence, track)), "wrap");
		}
		int newTrackTime = eventTrack.last() == null ? 0 : eventTrack.last() + 1;
		sequenceTrackPanel.add(getButton("Add", e -> addTrack(sequence, newTrackTime)), "wrap");

		JPanel sequencePanel = new JPanel(new MigLayout("ins 0"));
		sequencePanel.setBorder(BorderFactory.createTitledBorder("" + sequence));
//		sequencePanel.add(new JLabel("" + sequence));
		sequencePanel.add(getDeleteButton(e -> removeSequence(sequence)), "wrap");
		sequencePanel.add(sequenceTrackPanel);

		return sequencePanel;
	}

	private void addEventAction() {
		JPanel panel = new JPanel(new MigLayout());
		TwiComboBox<Sequence> animationBox = new TwiComboBox<>(new Animation("Stand and work for me", 0, 1));
		animationBox.addAll(model.getAnims());
		animationBox.addAll(model.getGlobalSeqs());

		animationBox.setRenderer(new SequenceComboBoxRenderer(modelHandler));
		panel.add(animationBox);
		int opt = JOptionPane.showConfirmDialog(this, panel, "Add Event Track", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (opt == JOptionPane.OK_OPTION && animationBox.getSelectedItem() != null) {
			undoManager.pushAction(new AddEventTrackAction(idObject, (Sequence) animationBox.getSelectedItem(), 0, changeListener).redo());
		}
	}

	private void editingStoppedListener(Sequence sequence, int track, int newValue) {
		editTrack(sequence, track, newValue);
	}

	private void editTrack(Sequence sequence, int track, int newValue) {
		undoManager.pushAction(new EditEventTrackAction(idObject, sequence, track, newValue, changeListener).redo());
	}

	private void addTrack(Sequence sequence, int track) {
		undoManager.pushAction(new AddEventTrackAction(idObject, sequence, track, changeListener).redo());
	}

	private void addSequence(Sequence sequence) {
		undoManager.pushAction(new AddEventTrackAction(idObject, sequence, 0, changeListener).redo());
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
}
