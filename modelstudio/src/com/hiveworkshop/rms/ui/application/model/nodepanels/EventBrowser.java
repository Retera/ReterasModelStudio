package com.hiveworkshop.rms.ui.application.model.nodepanels;

import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.SearchListPanel;
import com.hiveworkshop.rms.util.ScreenInfo;
import com.hiveworkshop.rms.util.sound.EventMappings;
import com.hiveworkshop.rms.util.sound.EventTarget;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class EventBrowser extends JPanel {
	private final EventMappings eventMappings;
	private final EventPreviewPanel eventPreviewPanel;
	private final SearchListPanel<EventTarget> eventListPanel;
	private final TwiSearchFilter filter = new TwiSearchFilter();
	private EventTarget selectedTarget = null;

	public EventBrowser() {
		this(new EventMappings(null));
	}

	public EventBrowser(EventMappings eventMappings) {
		super(new MigLayout("fill", "[50%][50%]"));
		setPreferredSize(ScreenInfo.getSmallWindow());
		this.eventMappings = eventMappings;
		eventListPanel = getEventListPanel();
		eventPreviewPanel = new EventPreviewPanel();

		add(eventListPanel, "growx, growy");
		JScrollPane scrollPane = new JScrollPane(eventPreviewPanel);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		add(scrollPane, "growx, growy");
	}


	private SearchListPanel<EventTarget> getEventListPanel() {
		List<EventTarget> items = getSortedTargets();
		SearchListPanel<EventTarget> eventListPanel = new SearchListPanel<>("", items, this::filterEvents);
		eventListPanel.setRenderer(getRenderer());
		eventListPanel.setSelectionConsumer(this::onSelect);
		return eventListPanel;
	}

	private List<EventTarget> getSortedTargets() {
		TreeMap<String, EventTarget> tagToEvent = new TreeMap<>();
		for (EventTarget e : eventMappings.getSoundMappings().getEvents()) {
			tagToEvent.put(EventTarget.getFullTag(e), e);
		}
		for (EventTarget e : eventMappings.getSpawnMappings().getEvents()) {
			tagToEvent.put(EventTarget.getFullTag(e), e);
		}
		for (EventTarget e : eventMappings.getSplatMappings().getEvents()) {
			tagToEvent.put(EventTarget.getFullTag(e), e);
		}
		for (EventTarget e : eventMappings.getUberSplatMappings().getEvents()) {
			tagToEvent.put(EventTarget.getFullTag(e), e);
		}

		return new ArrayList<>(tagToEvent.values());
	}

	public String getSelectedTargetFullTag() {
		return EventTarget.getFullTag(selectedTarget);
	}

	public EventTarget getSelectedTarget() {
		return selectedTarget;
	}

	public EventBrowser setSelectedTarget(EventTarget selectedTarget) {
//		System.out.println("selectint target: " + selectedTarget);
		eventListPanel.select(selectedTarget);
		eventListPanel.scrollToReveal(selectedTarget);
		onSelect(selectedTarget);
		return this;
	}


	private void onSelect(EventTarget target) {
//		System.out.println("selected: " + target + " (" + target.getClass().getSimpleName() + ")");
		eventPreviewPanel.setEvent(target);
		selectedTarget = target;
	}

	private DefaultListCellRenderer getRenderer() {
		return new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(final JList list, final Object value, final int index, final boolean iss, final boolean chf) {
				String name = "";
				if (value instanceof EventTarget eventTarget) {
					name = EventTarget.getFullTag(eventTarget) + " " + eventTarget.getName();
				}
//				if (value instanceof Sound sound) {
//					name = "SNDx" + sound;
//				} else if (value instanceof SplatMappings.Splat splat) {
//					if (splat.getName().contains("Footprint") || splat.getName().contains("FootPrint")) {
//						name = "FPTx" + splat;
//					} else {
//						name = "SPLx" + splat;
//					}
//				} else if (value instanceof UberSplatMappings.UberSplat splat) {
//					name = "UBRx" + splat;
//				} else if (value instanceof SpawnMappings.Spawn spawn) {
//					name = "SPNx" + spawn;
//				}
				return super.getListCellRendererComponent(list, name, index, iss, chf);
			}
		};
	}

	private boolean filterEvents(EventTarget et, String text) {
		return filter.anyMatch(text, et.getName(), EventTarget.getFullTag(et)) || filter.anyMatch(text, et.getFilePaths());
	}

}
