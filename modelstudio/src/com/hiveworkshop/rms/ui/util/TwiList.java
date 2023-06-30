package com.hiveworkshop.rms.ui.util;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.List;
import java.util.Vector;
import java.util.function.Consumer;

public class TwiList<T> extends JList<T> {
	private TwiListModel<T> listModel;

	public TwiList(TwiListModel<T> model) {
		super();
		listModel = model;
		setModel(listModel);
	}

	public TwiList(){
		this(new TwiListModel<>());
	}

	public TwiList(T[] items){
		this(new TwiListModel<>(items));
	}

	public TwiList(List<T> items){
		this(new TwiListModel<>(items));
	}

	public TwiList(Vector<T> items){
		this(new TwiListModel<>(items));
	}


	public TwiList<T> add(T t) {
		listModel.addElement(t);
		return this;
	}

	public TwiList<T> add(int i, T t) {
		listModel.add(i, t);
		return this;
	}

	public TwiList<T> addAll(Collection<T> collection) {
		listModel.addAll(collection);
		return this;
	}

	public T get(int i) {
		return listModel.getElementAt(i);
	}

	public TwiList<T> clear() {
		listModel.clear();
		return this;
	}

	public TwiList<T> remove(T t) {
		listModel.remove(t);
		return this;
	}

	public TwiList<T> removeElementAt(int i) {
		listModel.removeElementAt(i);
		return this;
	}

	public TwiList<T> moveElement(int index, int steps){
		listModel.moveElementAt(index, steps);
		return this;
	}

	public boolean isEmpty(){
		return listModel.isEmpty();
	}

	public int listSize(){
		return listModel.size();
	}

	public TwiList<T> addSelectionListener(ListSelectionListener listener) {
		addListSelectionListener(listener);
		return this;
	}

	public TwiList<T> addSelectionListener1(Consumer<T> selectionConsumer) {
		if(selectionConsumer != null){
			addListSelectionListener(e -> onSelection(e, selectionConsumer));
		}
		return this;
	}

	private void onSelection(ListSelectionEvent e, Consumer<T> selectionConsumer){
//		System.out.println("onSelection! " + e);
		if(!e.getValueIsAdjusting()){
			selectionConsumer.accept(getSelectedValue());
		}
	}

	public TwiList<T> addMultiSelectionListener(Consumer<Collection<T>> selectionConsumer) {
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

	public TwiList<T> setRenderer(ListCellRenderer<Object> cellRenderer) {
		setCellRenderer(cellRenderer);
		return this;
	}

	public JScrollPane getScrollableList(){
		return new JScrollPane(this);
	}

	public TwiList<T> setListModel(TwiListModel<T>  listModel) {
		this.listModel = listModel;
		setModel(this.listModel);
		return this;
	}

	public TwiListModel<T> getListModel() {
		return listModel;
	}

	public void scrollToSelected(){
		int selectedIndex = getSelectedIndex();
		if(selectedIndex != -1){
			ensureIndexIsVisible(selectedIndex);
		}
	}


//	Point lastPopup
	public Point getPopupLocation(MouseEvent event) {
//		System.out.println("getPopupLocation: " + event);
//		int selectedIndex = getSelectedIndex();
		int[] selectedIndices = getSelectedIndices();
		if (event == null && 0 < selectedIndices.length && 0 <= selectedIndices[0] && selectedIndices[selectedIndices.length-1] < listSize()) {
			Rectangle cellBounds = getCellBounds(selectedIndices[0], selectedIndices[selectedIndices.length-1]);
			Point location = cellBounds.getLocation();
//			return location;
//			System.out.println("bounds: " + cellBounds);
			return new Point(location.x, location.y + cellBounds.height);
		}
//		if (event == null && 0 <= selectedIndex && selectedIndex < listSize()) {
//			Rectangle cellBounds = getCellBounds(selectedIndex, selectedIndex);
//			Point location = cellBounds.getLocation();
////			return location;
//			return new Point(location.x, location.y + cellBounds.height);
//		}
		return null;
	}
}
