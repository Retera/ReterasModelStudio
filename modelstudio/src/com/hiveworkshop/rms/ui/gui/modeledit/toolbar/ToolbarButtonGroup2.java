package com.hiveworkshop.rms.ui.gui.modeledit.toolbar;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.function.Consumer;

public class ToolbarButtonGroup2<T extends ToolbarButtonType> {

	private final T[] toolbarButtonTypes;
	private TreeMap<T, ToolbarButton2<T>> buttonMap = new TreeMap<>();
	private ToolbarButton2<T> activeButton;
	private T activeButtonType;
	private final List<Consumer<T>> listeners2 = new ArrayList<>();

	public ToolbarButtonGroup2(JToolBar toolBar, T[] toolbarButtonTypes) {
		this.toolbarButtonTypes = toolbarButtonTypes;
		for (T type : toolbarButtonTypes) {
//			buttonMap.put(type, new ToolbarButton2(type, this::setActiveButton));
			buttonMap.put(type, new ToolbarButton2<>(type, (t) -> setActiveButton(type)));
		}
		toolBar.addSeparator();
		for (ToolbarButton2<?> button2 : buttonMap.values()) {
			toolBar.add(button2.getToolbarButton());
		}
	}
	public ToolbarButtonGroup2(T[] toolbarButtonTypes) {
		this.toolbarButtonTypes = toolbarButtonTypes;
		for (T type : toolbarButtonTypes) {
			buttonMap.put(type, new ToolbarButton2<>(type, (t) -> setActiveButton(type)));
		}
	}

	public void setActiveButton(T type){
		activeButtonType = type;
		if(activeButton != null){
			activeButton.setActive(false);
		}
		activeButton = buttonMap.get(type);
		if (activeButton != null){
			activeButton.setActive(true);
		}
		for(Consumer<T> listener : listeners2){
			listener.accept(type);
		}
	}

	public T[] getToolbarButtonTypes() {
		return toolbarButtonTypes;
	}

	public void addToolbarButtonListener(Consumer<T> listener) {
		listeners2.add(listener);
	}
	public void removeToolbarButtonListener(Consumer<T> listener) {
		listeners2.remove(listener);
	}

	public T getActiveButtonType() {
		return activeButtonType;
	}

	public List<JButton> getToolbarButtons() {
		List<JButton> toolbarButtons = new ArrayList<>();
		for (ToolbarButton2<T> button2 : buttonMap.values()) {
			toolbarButtons.add(button2.getToolbarButton());
		}
		return toolbarButtons;
	}

	public List<ModeButton2> getModeButtons() {
		List<ModeButton2> modeButton2s = new ArrayList<>();
		for (ToolbarButton2<T> button2 : buttonMap.values()) {
			modeButton2s.add(button2.getModeButton2());
		}
		return modeButton2s;
	}

	public JButton getToolbarButton(T type) {
		return buttonMap.get(type).getToolbarButton();
	}

	public ModeButton2 getModeButton(T type){
		return buttonMap.get(type).getModeButton2();
	}
}
