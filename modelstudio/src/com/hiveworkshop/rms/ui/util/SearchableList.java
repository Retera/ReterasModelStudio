package com.hiveworkshop.rms.ui.util;

import com.hiveworkshop.rms.util.IterableListModel;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class SearchableList<T> extends JList<T> {
	private IterableListModel<T> fullListModel;
	private IterableListModel<T> filteredListModel;
	private JTextField searchField;
	private BiFunction<T, String, Boolean> filterFunction;
	private Consumer<String> filterTextConsumer;

	public SearchableList(BiFunction<T, String, Boolean> filterFunction) {
		this.filterFunction = filterFunction;
		fullListModel = new IterableListModel<>();
		filteredListModel = new IterableListModel<>();
		setModel(fullListModel);
		searchField = new JTextField();
		searchField.addCaretListener(e -> applyFilter(searchField.getText()));
	}


	public SearchableList<T> add(T t) {
		fullListModel.addElement(t);
		return this;
	}

	public SearchableList<T> add(int i, T t) {
		fullListModel.add(i, t);
		return this;
	}

	public SearchableList<T> addAll(Collection<T> collection) {
		fullListModel.addAll(collection);
		return this;
	}

	public SearchableList<T> clear() {
		fullListModel.clear();
		return this;
	}

	public SearchableList<T> remove(T t) {
		fullListModel.remove(t);
		return this;
	}

	public SearchableList<T> removeAll(Collection<T> t) {
		fullListModel.removeAll(t);
		return this;
	}

	public T get(int i){
		return fullListModel.get(i);
	}

	public boolean contains(T t){
		return fullListModel.contains(t);
	}

	public boolean isEmpty(){
		return fullListModel.isEmpty();
	}

	public int listSize(){
		return fullListModel.size();
	}

	public SearchableList<T> addSelectionListener(ListSelectionListener listener) {
		addListSelectionListener(listener);
		return this;
	}

	public SearchableList<T> addSelectionListener1(Consumer<T> selectionConsumer) {
		if(selectionConsumer != null){
			addListSelectionListener(e -> onSelection(e, selectionConsumer));
		}
		return this;
	}

	private void onSelection(ListSelectionEvent e, Consumer<T> selectionConsumer){
		if(e.getValueIsAdjusting()){
			selectionConsumer.accept(getSelectedValue());
		}
	}

	public SearchableList<T> addMultiSelectionListener(Consumer<Collection<T>> selectionConsumer) {
		if(selectionConsumer != null){
			addListSelectionListener(e -> onMultiSelection(e, selectionConsumer));
		}
		return this;
	}

	private void onMultiSelection(ListSelectionEvent e, Consumer<Collection<T>> selectionConsumer){
		if(e.getValueIsAdjusting()){
			selectionConsumer.accept(getSelectedValuesList());
		}
	}

	public SearchableList<T> setRenderer(ListCellRenderer<Object> cellRenderer) {
		setCellRenderer(cellRenderer);
		return this;
	}

	public SearchableList<T> resetFilter() {
		searchField.setText("");
		setModel(fullListModel);
		return this;
	}

	public JScrollPane getScrollableList(){
		return new JScrollPane(this);
	}

	public SearchableList<T> setListModel(IterableListModel<T>  listModel) {
		fullListModel = listModel;
		setModel(fullListModel);
		return this;
	}

	public IterableListModel<T> getFullListModel() {
		return fullListModel;
	}

	public JTextField getSearchField() {
		return searchField;
	}

	public SearchableList<T> setSearch(String filterText) {
		searchField.setText(filterText);
		System.out.println("Set search text");
		return this;
	}

	public String getFilterText(){
		return searchField.getText();
	}

	public SearchableList<T> setFilterTextConsumer(Consumer<String> filterTextConsumer) {
		this.filterTextConsumer = filterTextConsumer;
		return this;
	}

	private void applyFilter(String filterText) {
		if (!filterText.equals("")) {
			filteredListModel.clear();
			for (T t : fullListModel) {
				if (filterFunction.apply(t, filterText)) {
					filteredListModel.addElement(t);
				}
			}
			setModel(filteredListModel);
		} else {
			setModel(fullListModel);
		}
		if(filterTextConsumer != null){
			filterTextConsumer.accept(filterText);
		}
	}

	public void scrollToSelected(){
		int selectedIndex = getSelectedIndex();
		if(selectedIndex != -1){
			ensureIndexIsVisible(selectedIndex);
		}
	}
}