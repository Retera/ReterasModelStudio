package com.hiveworkshop.rms.ui.application.model.nodepanels;

import com.hiveworkshop.rms.editor.actions.animation.AddEventTrackAction;
import com.hiveworkshop.rms.editor.actions.animation.EditEventTrackAction;
import com.hiveworkshop.rms.editor.actions.animation.RemoveEventSequenceAction;
import com.hiveworkshop.rms.editor.actions.animation.RemoveEventTrackAction;
import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.EventObject;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.application.model.editors.IntEditorJSpinner;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.SequenceComboBoxRenderer;
import com.hiveworkshop.rms.util.TwiComboBox;
import com.hiveworkshop.rms.util.sound.*;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.TreeSet;

public class ComponentEventPanel extends ComponentIdObjectPanel<EventObject> {
	private final JPanel soundsPanel;
	private final JLabel eventName;
	private final JPanel tracksPanel;
	private final EventMappings eventMappings;

	public ComponentEventPanel(ModelHandler modelHandler) {
		super(modelHandler);
		soundsPanel = new JPanel(new MigLayout(""));
		eventName = new JLabel("");
		topPanel.add(eventName, "wrap");
		topPanel.add(soundsPanel, "wrap");

		tracksPanel = new JPanel(new MigLayout());
		topPanel.add(tracksPanel, "spanx, wrap");
		eventMappings = new EventMappings(null);
//		trackPanel = new IntegerValuePanel(modelHandler, "EventTrack");
//		topPanel.add(trackPanel, "spanx, growx, wrap");
	}

	@Override
	public void updatePanels() {
		soundsPanel.removeAll();
		EventTarget event = eventMappings.getEvent(idObject.getName());
		if (event instanceof Sound) {
			soundsPanel.add(new JLabel("Name: " + event.getName()), "wrap, gapbottom 10");
			String[][] soundNameAndPaths = ((Sound) event).getFileNameAndPaths();
			for (String[] soundNameAndPath : soundNameAndPaths) {
				makeSoundButton(soundNameAndPath[1], soundNameAndPath[0]);
			}
			soundsPanel.repaint();
		} else if (event instanceof SplatMappings.Splat) {
//			soundsPanel.add(new JLabel("Name: " + event.getName()), "wrap, gapbottom 10");
			SplatMappings.Splat splat = (SplatMappings.Splat) event;
			String[][] soundNameAndPaths = splat.getFileNameAndPaths();
			for (String[] soundNameAndPath : soundNameAndPaths) {

				System.out.println("SplatPath: " + soundNameAndPath[1] + ".blp");
				BufferedImage image = BLPHandler.getImage(soundNameAndPath[1] + ".blp");
				if(image != null){
					soundsPanel.add(new JLabel(SplatImageGenerator.generateIcon2(splat)), "split 3");
					soundsPanel.add(new JLabel("File:"), "");
					soundsPanel.add(new JLabel(soundNameAndPath[1]), "wrap");

//					soundsPanel.add(new JLabel("File:"), "split 2");
//					soundsPanel.add(new JLabel(soundNameAndPath[1]), "wrap");
//					soundsPanel.add(new JLabel(SplatImageGenerator.generateIcon2(splat)), "wrap");

//					int width = image.getWidth()/splat.getColumns();
//					int height = image.getHeight()/splat.getRows();
//					int xOff = (splat.getuVLifespanEnd() % splat.getRows()) * width;
//					int yOff = ((int)(splat.getuVLifespanEnd()/splat.getRows())) * height;
//					soundsPanel.add(new JLabel(new ImageIcon(ImageCreator.getSubImage(image, xOff, yOff, width, height))), "wrap");
//					soundsPanel.add(new JLabel(new ImageIcon(ImageCreator.getMarkSubImages(image,
//							splat.getRows(), splat.getColumns(),
//							splat.getuVLifespanStart(), splat.getuVLifespanEnd(),
//							splat.getuVDecayStart(), splat.getuVDecayEnd()))), "wrap");
				} else {
					soundsPanel.add(new JLabel(new ImageIcon(BLPHandler.getBlankImage())), "wrap");
				}
			}

		} else if (event instanceof UberSplatMappings.UberSplat) {

//			soundsPanel.add(new JLabel("Name: " + event.getName()), "wrap, gapbottom 10");
			UberSplatMappings.UberSplat splat = (UberSplatMappings.UberSplat) event;
			String[][] soundNameAndPaths = splat.getFileNameAndPaths();
			for (String[] soundNameAndPath : soundNameAndPaths) {

				System.out.println("SplatPath: " + soundNameAndPath[1] + ".blp");
				BufferedImage image = BLPHandler.getImage(soundNameAndPath[1] + ".blp");
				if(image != null){

					soundsPanel.add(new JLabel(SplatImageGenerator.generateIcon2(splat)), "split 3");
					soundsPanel.add(new JLabel("File:"), "");
					soundsPanel.add(new JLabel(soundNameAndPath[1]), "wrap");

//					soundsPanel.add(new JLabel("File:"), "split 2");
//					soundsPanel.add(new JLabel(soundNameAndPath[1]), "wrap");
//					soundsPanel.add(new JLabel(SplatImageGenerator.generateIcon2(splat)), "wrap");

//					int width = image.getWidth();
//					int height = image.getHeight();
//					int xOff = 0;
//					int yOff = 0;
//					soundsPanel.add(new JLabel(new ImageIcon(ImageCreator.getSubImage(image, xOff, yOff, width, height))), "wrap");
//					soundsPanel.add(new JLabel(new ImageIcon(ImageCreator.getMarkSubImages(image,
//							1, 1,
//							0, 0,
//							0, 0))), "wrap");
				} else {
					soundsPanel.add(new JLabel(new ImageIcon(BLPHandler.getBlankImage())), "wrap");
				}
			}

		}

		eventName.setText(EventObject.getEventName(idObject.getName()));
		updateTracksPanel();
	}
	public void updatePanels1() {
		soundsPanel.removeAll();
		Sound sound = ProgramGlobals.getSoundMappings().getEvent(idObject.getName());
		if (sound != null) {
			soundsPanel.add(new JLabel("Name: " + sound.getSoundName()), "wrap, gapbottom 10");
			String[][] soundNameAndPaths = sound.getFileNameAndPaths();
			for (String[] soundNameAndPath : soundNameAndPaths) {
				makeSoundButton(soundNameAndPath[1], soundNameAndPath[0]);

//				soundsPanel.add(new JLabel(soundNameAndPath[0]));
//				soundsPanel.add(getButton("play", e -> SoundPlayer.play(soundNameAndPath[1])), "wrap");
			}
			soundsPanel.repaint();
		}
		eventName.setText(EventObject.getEventName(idObject.getName()));
		updateTracksPanel();
	}

	private void makeSoundButton(String path, String name) {
		soundsPanel.add(new JLabel(name));
		JButton button = new JButton("play");
		button.addActionListener(e -> SoundPlayer.play(path, button));
		soundsPanel.add(button, "wrap");
//		soundsPanel.add(getButton("play", e -> SoundPlayer.play(path)), "wrap");
//		System.out.println("got sound: " + path);
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
		for (Sequence sequence : idObject.getEventTrackAnimMap().keySet()){
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
		if(track != newValue){
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
}
