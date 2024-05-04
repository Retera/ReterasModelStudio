package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.ui.util.SearchableTwiList;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class SearchListPanel<T> extends JPanel {
	private final SearchableTwiList<T> searchList;
	private final List<T> itemList;
	private Consumer<T> selectionConsumer;
	private final JLabel label;

	public SearchListPanel(String title, BiFunction<T, String, Boolean> filterFunction) {
		this(title, new ArrayList<>(), filterFunction);
	}

	public SearchListPanel(String title, List<T> itemList, BiFunction<T, String, Boolean> filterFunction) {
		super(new MigLayout("gap 0, ins 0, fill", "[grow][]", "[][][grow]"));
		label = new JLabel(title);
		this.itemList = itemList;
		searchList = new SearchableTwiList<>(this.itemList, filterFunction);
		searchList.addSelectionListener(this::onSelectionEvent);
		add(label, "wrap");
		add(searchList.getSearchField(), "grow, wrap");
		add(searchList.getScrollableList(), "spanx, growx, growy");
	}

	public SearchListPanel<T> clearAndReset() {
		searchList.resetFilter();
		itemList.clear();
		return this;
	}

	public SearchListPanel<T> clear() {
		itemList.clear();
		return this;
	}
	public SearchListPanel<T> add(T item) {
		itemList.add(item);
		return this;
	}
	public SearchListPanel<T> add(int i, T item) {
		itemList.add(i, item);
		return this;
	}
	public SearchListPanel<T> addAll(Collection<T> items) {
		itemList.addAll(items);
		return this;
	}
	public SearchListPanel<T> remove(T item) {
		itemList.remove(item);
		return this;
	}
	public SearchListPanel<T> resetSearch() {
		searchList.resetFilter();
		return this;
	}
	public SearchListPanel<T> setRenderer(ListCellRenderer<Object> renderer) {
		searchList.setRenderer(renderer);
		return this;
	}
	public SearchListPanel<T> setSelectionMode(int selectionMode) {
		searchList.setSelectionMode(selectionMode);
		return this;
	}
	public SearchListPanel<T> addSelectionListener(ListSelectionListener listener) {
		searchList.addSelectionListener(listener);
		return this;
	}
	public SearchListPanel<T> setSelectionConsumer(Consumer<T> selectionConsumer) {
		this.selectionConsumer = selectionConsumer;
//		searchList.addSelectionListener1(selectionConsumer);
//		if (this.selectionConsumer != null) {
//			searchList.addSelectionListener(this::onSelectionEvent);
//		}
		return this;
	}
	public SearchListPanel<T> scrollToReveal(T item) {
		if (item != null) {
			int i = itemList.indexOf(item);
			if (i != -1) {
				Rectangle cellBounds = searchList.getCellBounds(i, i);
				if (cellBounds != null) {
					searchList.scrollRectToVisible(cellBounds);
				}
			}
		}
		return this;
	}
//	public SearchListPanel<T> setMultiSelectionConsumer(Consumer<Collection<T>> selectionConsumer) {
////		this.selectionConsumer = selectionConsumer;
//		searchList.addMultiSelectionListener(selectionConsumer);
////		if (this.selectionConsumer != null) {
////			searchList.addSelectionListener(this::onSelectionEvent);
////		}
//		return this;
//	}

	public SearchListPanel<T> setText(String text) {
		label.setText(text);
		return this;
	}

	protected void onSelectionEvent(ListSelectionEvent e) {
		if (selectionConsumer != null && e.getValueIsAdjusting()) {
			for (T t : searchList.getSelectedValuesList()) {
				selectionConsumer.accept(t);
			}
			searchList.setSelectedValue(null, false);
		}
	}
}
