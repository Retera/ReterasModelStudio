package com.hiveworkshop.rms.util;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.Collection;
import java.util.List;
import java.util.Vector;
import java.util.function.Consumer;
import java.util.function.Function;

public class TwiComboBox<E> extends JComboBox<E> {

	TwiComboBoxModel<E> comboBoxModel;
	private boolean allowLastToFirst = false;

	public TwiComboBox(TwiComboBoxModel<E> aModel, E prototypeValue) {
		super();
		setModel(aModel);
		comboBoxModel = aModel;
		if(prototypeValue != null){
			setPrototypeDisplayValue(prototypeValue);
		}
	}

	public TwiComboBox(TwiComboBoxModel<E> aModel) {
		this(aModel, null);
	}

	public TwiComboBox(E prototypeValue) {
		this(new TwiComboBoxModel<>(), prototypeValue);
	}

	public TwiComboBox() {
		this(new TwiComboBoxModel<>(), null);
	}

	public TwiComboBox(E[] items) {
		this(new TwiComboBoxModel<>(items), null);
	}

	public TwiComboBox(E[] items, E prototypeValue) {
		this(new TwiComboBoxModel<>(items), prototypeValue);
	}

	public TwiComboBox(Vector<E> items) {
		this(new TwiComboBoxModel<>(items), null);
	}

	public TwiComboBox(List<E> items) {
		this(new TwiComboBoxModel<>(items), null);
	}

	public TwiComboBox(List<E> items, E prototypeValue) {
		this(new TwiComboBoxModel<>(items), prototypeValue);
	}

	public TwiComboBox<E> addOnSelectItemListener(Consumer<E> consumer){
		addItemListener(e -> consumeOnSelected(consumer, e));
		return this;
	}

	private void consumeOnSelected(Consumer<E> consumer, ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			if (e.getItem() == null) {
				consumer.accept(null);
			} else {
				consumer.accept((E) e.getItem());
			}
		} else if (e.getStateChange() == ItemEvent.DESELECTED
				&& comboBoxModel != null
				&& comboBoxModel.getSelectedTyped() == null){
			consumer.accept(null);
		}
	}

	public TwiComboBox<E> setComboBoxModel(TwiComboBoxModel<E> comboBoxModel) {
		setModel(comboBoxModel);
		return this;
	}

	public void setModel(TwiComboBoxModel<E> comboBoxModel) {
		this.comboBoxModel = comboBoxModel;
		super.setModel(comboBoxModel);
	}

	public void incIndex(int steps){
		int previousSelectedIndex = Math.max(0, getSelectedIndex());

		int itemCount = getItemCount();
		int newIndex = previousSelectedIndex + steps;
		if(allowLastToFirst){
			newIndex = (itemCount + newIndex) % itemCount;
		} else {
			newIndex = MathUtils.clamp(newIndex, 0, itemCount-1);
		}

		if (newIndex != previousSelectedIndex) {
			setSelectedIndex(newIndex);
		}
	}

	public TwiComboBox<E> setAllowLastToFirst(boolean allowLastToFirst) {
		this.allowLastToFirst = allowLastToFirst;
		return this;
	}

	public boolean isAllowLastToFirst() {
		return allowLastToFirst;
	}

	public ComboBoxModel<E> getComboBoxModel() {
		return comboBoxModel;
	}


	public TwiComboBox<E> setNewModelOf(Collection<E> itemList) {
		TwiComboBoxModel<E> comboBoxModel = new TwiComboBoxModel<>();
		comboBoxModel.addAll(itemList);
		setModel(comboBoxModel);
		return this;
	}

	public TwiComboBox<E> setNewLinkedModelOf(List<E> itemList) {
		setModel(new TwiComboBoxModel<>(itemList));
		return this;
	}

	public TwiComboBox<E> selectOrFirst(E item){
		setSelectedItem(item);
		if(getSelectedItem() == null && getItemCount() > 0){
			setSelectedIndex(0);
		}
		return this;
	}

	@Override
	public void addItem(E item) {
		comboBoxModel.addElement(item);
	}
	public void addItem(E item, int i) {
		comboBoxModel.insertElementAt(item, i);
	}

	public TwiComboBox<E> add(E item) {
		comboBoxModel.addElement(item);
		return this;
	}

	public TwiComboBox<E> add(E item, int i) {
		comboBoxModel.insertElementAt(item, i);
		return this;
	}

	public TwiComboBox<E> addAll(Collection<? extends E> itemList) {
		comboBoxModel.addAll(itemList);
		return this;
	}

	public TwiComboBox<E> setStringFunctionRender(Function<Object, String> toStringFunction){
		setRenderer(new BasicComboBoxRenderer(){
			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean iss, boolean chf) {
				return super.getListCellRendererComponent(list, toStringFunction.apply(value), index, iss, chf);
			}
		});
		return this;
	}

	public E getSelected(){
		return comboBoxModel.getSelectedTyped();
	}
}
