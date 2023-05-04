package com.hiveworkshop.rms.ui.util;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class SearchableTwiList<T> extends JList<T> {
	private TwiListModel<T> fullListModel;
	private List<T> allObjects;
	private List<T> filteredObjects = new ArrayList<>();
	private TwiListModel<T> filteredListModel;
	private JTextField searchField;
	private BiFunction<T, String, Boolean> filterFunction;
	private Consumer<String> filterTextConsumer;

	public SearchableTwiList(BiFunction<T, String, Boolean> filterFunction){
		this(new ArrayList<>(), filterFunction);
	}
	public SearchableTwiList(List<T> items, BiFunction<T, String, Boolean> filterFunction){
		super();
		this.filterFunction = filterFunction;

		allObjects = items;
		fullListModel = new TwiListModel<>(items);
		setModel(fullListModel);

		filteredListModel = new TwiListModel<>(filteredObjects);

		searchField = new JTextField();
		searchField.addCaretListener(e -> applyFilter(searchField.getText()));
	}


	public SearchableTwiList<T> add(T t) {
		fullListModel.addElement(t);
		return this;
	}

	public SearchableTwiList<T> add(int i, T t) {
		fullListModel.add(i, t);
		return this;
	}

	public SearchableTwiList<T> addAll(Collection<T> collection) {
		fullListModel.addAll(collection);
		return this;
	}

	public SearchableTwiList<T> clear() {
		fullListModel.clear();
		return this;
	}

	public SearchableTwiList<T> remove(T t) {
		fullListModel.remove(t);
		return this;
	}

	public SearchableTwiList<T> removeAll(Collection<T> t) {
		fullListModel.removeAll(t);
		return this;
	}

	public T get(int i){
		return fullListModel.getElementAt(i);
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

	public SearchableTwiList<T> addSelectionListener(ListSelectionListener listener) {
		addListSelectionListener(listener);
		return this;
	}

	public SearchableTwiList<T> addSelectionListener1(Consumer<T> selectionConsumer) {
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

	public SearchableTwiList<T> addMultiSelectionListener(Consumer<Collection<T>> selectionConsumer) {
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

	public SearchableTwiList<T> setRenderer(ListCellRenderer<Object> cellRenderer) {
		setCellRenderer(cellRenderer);
		return this;
	}

	public SearchableTwiList<T> resetFilter() {
		searchField.setText("");
		setModel(fullListModel);
		return this;
	}

	public JScrollPane getScrollableList(){
		return new JScrollPane(this);
	}

	public SearchableTwiList<T> setList(List<T> list) {
		allObjects = list;
		fullListModel = new TwiListModel<>(list);
		setModel(fullListModel);
		return this;
	}

	public TwiListModel<T> getFullListModel() {
		return fullListModel;
	}

	public JTextField getSearchField() {
		return searchField;
	}

	public SearchableTwiList<T> setSearch(String filterText) {
		searchField.setText(filterText);
		System.out.println("Set search text");
		return this;
	}

	public String getFilterText(){
		return searchField.getText();
	}

	public SearchableTwiList<T> setFilterTextConsumer(Consumer<String> filterTextConsumer) {
		this.filterTextConsumer = filterTextConsumer;
		return this;
	}

	private void applyFilter(String filterText) {
		if (!filterText.equals("")) {
			System.out.println("search: " + filterText);
			filteredObjects.clear();
			for (T t : allObjects) {
				if (filterFunction.apply(t, filterText)) {
					filteredObjects.add(t);
				}
			}
			System.out.println("search: " + filterText);
			setModel(filteredListModel);
			repaint();
//			filteredListModel.clear();
//			for (T t : allObjects) {
//				if (filterFunction.apply(t, filterText)) {
//					filteredListModel.addElement(t);
//				}
//			}
//			setModel(filteredListModel);
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
