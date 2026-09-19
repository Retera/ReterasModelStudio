package com.hiveworkshop.wc3.gui.modeledit.components.cards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import com.hiveworkshop.wc3.mdl.EventObject;

import net.miginfocom.swing.MigLayout;

public class ComponentEventObjectCard extends ComponentNodeCard<EventObject> {
	private DefaultListModel<Integer> trackModel;
	private JList<Integer> trackList;

	@Override
	protected void addTypeRows() {
		beginSection("Event Object");
		addComboBox("Global Sequence", this::globalSequenceOptions, this::describeGlobalSequence,
				event -> event.isHasGlobalSeq() ? event.getGlobalSeq() : null, (event, globalSeq) -> {
					event.setGlobalSeq(globalSeq);
					event.setHasGlobalSeq(globalSeq != null);
				});
		trackModel = new DefaultListModel<>();
		trackList = new JList<>(trackModel);
		trackList.setVisibleRowCount(6);
		final JSpinner timeSpinner = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
		final JButton addKey = addButton("Add", () -> {
			final EventObject target = item;
			final List<Integer> oldTrack = new ArrayList<>(target.getEventTrack());
			final List<Integer> newTrack = new ArrayList<>(oldTrack);
			final Integer time = ((Number) timeSpinner.getValue()).intValue();
			if (!newTrack.contains(time)) {
				newTrack.add(time);
				Collections.sort(newTrack);
			}
			apply("Event keys", oldTrack, newTrack, track -> target.setEventTrack(new ArrayList<>(track)));
		});
		final JButton removeKey = addButton("Remove Selected", () -> {
			final EventObject target = item;
			final List<Integer> oldTrack = new ArrayList<>(target.getEventTrack());
			final List<Integer> newTrack = new ArrayList<>(oldTrack);
			newTrack.removeAll(trackList.getSelectedValuesList());
			apply("Event keys", oldTrack, newTrack, track -> target.setEventTrack(new ArrayList<>(track)));
		});
		final JPanel buttons = new JPanel(new MigLayout("insets 0", "[grow,fill][][]", ""));
		buttons.add(timeSpinner);
		buttons.add(addKey);
		buttons.add(removeKey);
		addRow("Event times", new JScrollPane(trackList), "growx, hmin 80");
		addRow(null, buttons, "growx");
		endSection();
	}

	@Override
	protected void onReload() {
		super.onReload();
		trackModel.clear();
		final List<Integer> times = new ArrayList<>(item.getEventTrack());
		Collections.sort(times);
		for (final Integer time : times) {
			trackModel.addElement(time);
		}
	}

	private List<Integer> globalSequenceOptions() {
		final List<Integer> options = new ArrayList<>();
		options.add(null);
		options.addAll(model().getGlobalSeqs());
		return options;
	}

	private String describeGlobalSequence(final Integer globalSeq) {
		return "GlobalSeq " + model().getGlobalSeqId(globalSeq) + " (" + globalSeq + " ms)";
	}
}
