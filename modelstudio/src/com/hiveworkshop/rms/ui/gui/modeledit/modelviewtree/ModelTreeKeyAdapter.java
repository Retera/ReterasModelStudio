package com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.function.Consumer;

public class ModelTreeKeyAdapter extends KeyAdapter {
	private boolean controlDown = false;
	private final Consumer<Boolean> setControlDown;

	public ModelTreeKeyAdapter(Consumer<Boolean> setControlDown){
		this.setControlDown = setControlDown;
	}


	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_CONTROL && !controlDown) System.out.println("controll down");
		controlDown = e.isControlDown();
		setControlDown.accept(e.isControlDown());
		super.keyPressed(e);
	}

	@Override
	public void keyReleased(KeyEvent e) {
		setControlDown.accept(e.isControlDown());
		if (e.getKeyCode() == KeyEvent.VK_CONTROL) System.out.println("controll up");
		controlDown = e.isControlDown();
		super.keyReleased(e);
	}
}
