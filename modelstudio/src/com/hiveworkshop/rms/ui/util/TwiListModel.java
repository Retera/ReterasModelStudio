package com.hiveworkshop.rms.ui.util;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class TwiListModel<E> extends AbstractListModel<E> {
	private final List<E> objects;
	private Object selectedObject;
	private E selectedTypedObject;
	private int numItemsInList;

	/**
	 * Constructs an empty TwiComboBox object.
	 */
	public TwiListModel() {
		objects = new ArrayList<>();
	}

	/**
	 * Constructs a TwiComboBox object initialized with
	 * an array of objects.
	 *
	 * @param items an array of Object objects
	 */
	public TwiListModel(final E[] items) {
		objects = new ArrayList<>(items.length);
		numItemsInList = items.length;

		int i, c;
		for (i = 0, c = items.length; i < c; i++)
			objects.add(items[i]);

		if (getSize() > 0) {
			selectedObject = getElementAt(0);
			selectedTypedObject = getElementAt(0);
		}
	}

	/**
	 * Constructs a TwiComboBox object initialized with
	 * a vector.
	 *
	 * @param v a List object ...
	 */
	public TwiListModel(List<E> v) {
		objects = v;
		numItemsInList = v.size();

		if (getSize() > 0) {
			selectedObject = getElementAt(0);
			selectedTypedObject = getElementAt(0);
		}
	}

	// implements javax.swing.ComboBoxModel

	/**
	 * Set the value of the selected item. The selected item may be null.
	 *
	 * @param anObject The combo box value or null for no selection.
	 */
	public void setSelectedItem(Object anObject) {
		if ((selectedObject != null && !selectedObject.equals(anObject)) ||
				selectedObject == null && anObject != null) {
			selectedObject = anObject;
			selectedTypedObject = (E) anObject;
			fireContentsChanged(this, -1, -1);
		}
	}

	public void setSelectedTyped(E anObject) {
		if ((selectedObject != null && !selectedObject.equals(anObject)) ||
				selectedObject == null && anObject != null) {
			selectedObject = anObject;
			selectedTypedObject = anObject;
			fireContentsChanged(this, -1, -1);
		}
	}

	// implements javax.swing.ComboBoxModel
	public Object getSelectedItem() {
		return selectedObject;
	}

	public E getSelectedTyped() {
		return selectedTypedObject;
	}

	// implements javax.swing.ListModel
	public int getSize() {
		if(objects.size() != numItemsInList){
			updateStuff();
		}
		return objects.size();
	}

	private void updateStuff(){
		int sizeDiff  = objects.size() - numItemsInList;
		if(sizeDiff<0){
			fireIntervalRemoved(this, objects.size(), numItemsInList-1);
		} else if (sizeDiff>0){
			fireIntervalAdded(this, numItemsInList, objects.size()-1);
		}
		numItemsInList = objects.size();
	}

	// implements javax.swing.ListModel
	public E getElementAt(int index) {
		if (index >= 0 && index < objects.size())
			return objects.get(index);
		else
			return null;
	}

	/**
	 * Returns the index-position of the specified object in the list.
	 *
	 * @param anObject the object to return the index of
	 * @return an int representing the index position, where 0 is
	 * the first position
	 */
	public int getIndexOf(Object anObject) {
		return objects.indexOf(anObject);
	}

	// implements javax.swing.MutableComboBoxModel
	public void addElement(E anObject) {
		objects.add(anObject);
		fireIntervalAdded(this, objects.size() - 1, objects.size() - 1);
		if (objects.size() == 1 && selectedObject == null && anObject != null) {
			setSelectedItem(anObject);
		}
	}

	public void add(E anObject) {
		objects.add(anObject);
		fireIntervalAdded(this, objects.size() - 1, objects.size() - 1);
		if (objects.size() == 1 && selectedObject == null && anObject != null) {
			setSelectedItem(anObject);
		}
	}

	// implements javax.swing.MutableComboBoxModel
	public void insertElementAt(E anObject, int index) {
		objects.add(index, anObject);
		fireIntervalAdded(this, index, index);
	}
	public void add(int index, E anObject) {
		objects.add(index, anObject);
		fireIntervalAdded(this, index, index);
	}

	// implements javax.swing.MutableComboBoxModel
	public void removeElementAt(int index) {
		if (getElementAt(index) == selectedObject) {
			if (index == 0) {
				setSelectedItem(getSize() == 1 ? null : getElementAt(index + 1));
			} else {
				setSelectedItem(getElementAt(index - 1));
			}
		}

		objects.remove(index);

		fireIntervalRemoved(this, index, index);
	}

	// implements javax.swing.MutableComboBoxModel
	public void removeElement(Object anObject) {
		int index = objects.indexOf(anObject);
		if (index != -1) {
			removeElementAt(index);
		}
	}
	public void remove(Object anObject) {
		int index = objects.indexOf(anObject);
		if (index != -1) {
			removeElementAt(index);
		}
	}

	public boolean isEmpty(){
		return objects.isEmpty();
	}

	public int size(){
		return getSize();
	}

	/**
	 * Empties the list.
	 */
	public void removeAllElements() {
		if (objects.size() > 0) {
			int firstIndex = 0;
			int lastIndex = objects.size() - 1;
			objects.clear();
			selectedObject = null;
			selectedTypedObject = null;
			fireIntervalRemoved(this, firstIndex, lastIndex);
		} else {
			selectedObject = null;
			selectedTypedObject = null;
		}
	}
	public void clear() {
		if (objects.size() > 0) {
			int firstIndex = 0;
			int lastIndex = objects.size() - 1;
			objects.clear();
			selectedObject = null;
			selectedTypedObject = null;
			fireIntervalRemoved(this, firstIndex, lastIndex);
		} else {
			selectedObject = null;
			selectedTypedObject = null;
		}
	}

	/**
	 * Adds all of the elements present in the collection.
	 *
	 * @param c the collection which contains the elements to add
	 * @throws NullPointerException if {@code c} is null
	 */
	public void addAll(Collection<? extends E> c) {
		if (c.isEmpty()) {
			return;
		}

		int startIndex = getSize();

		objects.addAll(c);
		fireIntervalAdded(this, startIndex, getSize() - 1);
	}

	/**
	 * Adds all of the elements present in the collection, starting
	 * from the specified index.
	 *
	 * @param index index at which to insert the first element from the
	 *              specified collection
	 * @param c     the collection which contains the elements to add
	 * @throws ArrayIndexOutOfBoundsException if {@code index} does not
	 *                                        fall within the range of number of elements currently held
	 * @throws NullPointerException           if {@code c} is null
	 */
	public void addAll(int index, Collection<? extends E> c) {
		if (index < 0 || index > getSize()) {
			throw new ArrayIndexOutOfBoundsException("index out of range: " +
					index);
		}

		if (c.isEmpty()) {
			return;
		}

		objects.addAll(index, c);
		fireIntervalAdded(this, index, index + c.size() - 1);
	}

	/**
	 * Adds all of the elements present.
	 *
	 * @param c one or more elements to add
	 * @throws NullPointerException if {@code c} is null
	 */
	public void addAll(E... c) {
		if (c.length == 0) {
			return;
		}

		int startIndex = getSize();

		objects.addAll(Arrays.asList(c));
//		for(E e : c){
//			objects.add(e);
//		}

		fireIntervalAdded(this, startIndex, getSize() - 1);
	}
}
