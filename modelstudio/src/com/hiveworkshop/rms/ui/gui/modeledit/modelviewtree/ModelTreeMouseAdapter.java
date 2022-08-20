package com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree;

import javax.swing.tree.TreePath;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.function.Consumer;

public class ModelTreeMouseAdapter extends MouseAdapter {
	private final Consumer<Boolean> controlDown;
	private final ComponentThingTree tree;
	private ComponentTreeNode<?> lastNode;

	public ModelTreeMouseAdapter(Consumer<Boolean> controlDown, ComponentThingTree tree){
		this.controlDown = controlDown;
		this.tree = tree;
	}

	private void highlight(MouseEvent e){
		TreePath pathForLocation = tree.getPathForLocation(e.getX(), e.getY());
		if (pathForLocation != null && pathForLocation.getLastPathComponent() instanceof ComponentTreeNode) {
			lastNode = (ComponentTreeNode<?>) pathForLocation.getLastPathComponent();
			lastNode.highlight();

		} else {
			unHighLight();
		}
	}
	private void unHighLight(){
		if(lastNode != null){
			lastNode.unHigthlight();
			lastNode = null;
		}
	}

	@Override
	public void mouseClicked(final MouseEvent e) {
		super.mouseClicked(e);
		System.out.println("[CompTree] mouseClicked");
	}

	@Override
	public void mouseEntered(final MouseEvent e) {
		super.mouseEntered(e);
//		System.out.println("[CompTree] mouseEntered");
//		System.out.println(e);
		highlight(e);
	}

	@Override
	public void mouseExited(final MouseEvent e) {
		super.mouseExited(e);
//		System.out.println("[CompTree] mouseExited");
		unHighLight();
	}

	@Override
	public void mousePressed(final MouseEvent e) {
		super.mousePressed(e);
//		System.out.println("[CompTree] mousePressed");
		highlight(e);
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
		super.mouseReleased(e);
		highlight(e);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		super.mouseMoved(e);
		controlDown.accept(e.isControlDown());
		highlight(e);
	}
	public void mouseWheelMoved(MouseWheelEvent e){
//		System.out.println("[CompTree] mouseWheelMoved: " + e);
		super.mouseWheelMoved(e);
	}
	public void mouseDragged(MouseEvent e){
//		System.out.println("[CompTree] mouseDragged: " + e);
		super.mouseDragged(e);
		highlight(e);
	}

}
