package com.hiveworkshop.rms.ui.application.edit.animation;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class ScrollableJSubMenu {
	private final ArrayList<JMenuItem> subMenus = new ArrayList<>();
	private int submenuStartIndex = 0;
	private int subMenuItemsToShow = 20;

	private final JMenuItem upMenuItem = new JMenuItem("\u25B2");
	private final JMenuItem downMenuItem = new JMenuItem("\u25BC");
	private final JPopupMenu parentMenu;

	public ScrollableJSubMenu(JPopupMenu parentMenu, int subMenuItemsToShow) {
		this.parentMenu = parentMenu;
		this.subMenuItemsToShow = subMenuItemsToShow;
		downMenuItem.addItemListener(e -> doScroll(true, upMenuItem, downMenuItem));
		upMenuItem.addItemListener(e -> doScroll(false, upMenuItem, downMenuItem));
		upMenuItem.addMouseMotionListener(getMouseAdapter(false));
		downMenuItem.addMouseMotionListener(getMouseAdapter(true));
	}

	public ScrollableJSubMenu addArrowsToParent(){
		parentMenu.add(upMenuItem).setVisible(false);
		parentMenu.add(downMenuItem).setVisible(false);
		return this;
	}

	private MouseAdapter getMouseAdapter(boolean down) {
		return new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				super.mouseEntered(e);
				doScroll(down, upMenuItem, downMenuItem);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				super.mouseExited(e);
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				super.mouseDragged(e);
				doScroll(down, upMenuItem, downMenuItem);
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				super.mouseMoved(e);
				doScroll(down, upMenuItem, downMenuItem);
			}
		};
	}

	private void addSubmenusToParent(){
		submenuStartIndex = 0;
		if(subMenuItemsToShow < subMenus.size()){
			parentMenu.add(upMenuItem);
			upMenuItem.setEnabled(false);
		}
		int componentIndex = parentMenu.getComponentIndex(upMenuItem);
		for(int i = 0; i<subMenuItemsToShow && i<subMenus.size(); i++){
			parentMenu.add(subMenus.get(i), componentIndex + i)
					.setVisible(subMenus.size() - submenuStartIndex < subMenuItemsToShow);
		}
		if(subMenuItemsToShow < subMenus.size()){
			parentMenu.add(downMenuItem);
		}
	}

	public ScrollableJSubMenu clear(){
		subMenus.clear();
		upMenuItem.setEnabled(false);
		downMenuItem.setEnabled(true);
		submenuStartIndex = 0;
		return this;
	}

	public ScrollableJSubMenu removeAll(){
		for (JMenuItem item : subMenus) {
			parentMenu.remove(item);
		}
		clear();
		return this;
	}

	public JMenuItem add(String text, ActionListener actionListener) {
		JMenuItem menuItem = new JMenuItem(text);
		menuItem.addActionListener(actionListener);
		subMenus.add(menuItem);
		int componentIndex = parentMenu.getComponentIndex(upMenuItem);
		int submenuEndIndex = componentIndex + subMenus.size();
		parentMenu.add(menuItem, submenuEndIndex);
		menuItem.setVisible(subMenus.size() - submenuStartIndex < subMenuItemsToShow);

		upMenuItem.setVisible(subMenuItemsToShow < subMenus.size());
		downMenuItem.setVisible(subMenuItemsToShow < subMenus.size());
		return menuItem;
	}

	public JMenuItem add(JMenuItem menuItem) {
		subMenus.add(menuItem);
		int componentIndex = parentMenu.getComponentIndex(upMenuItem);
		int submenuEndIndex = componentIndex + subMenus.size();
		parentMenu.add(menuItem, submenuEndIndex);
		menuItem.setVisible(subMenus.size() - submenuStartIndex < subMenuItemsToShow);

		upMenuItem.setVisible(subMenuItemsToShow < subMenus.size());
		downMenuItem.setVisible(subMenuItemsToShow < subMenus.size());
		return menuItem;
	}

	private void doScroll(boolean down, JMenuItem upMenuItem, JMenuItem downMenuItem){
		if(upMenuItem.isEnabled() && !down || downMenuItem.isEnabled() && down){
			if(down && 1 < subMenus.size() - (submenuStartIndex + subMenuItemsToShow)){
				subMenus.get(submenuStartIndex).setVisible(false);
				submenuStartIndex++;
				subMenus.get(submenuStartIndex + subMenuItemsToShow).setVisible(true);
			} else if (!down && 0 < submenuStartIndex){
				subMenus.get(submenuStartIndex+subMenuItemsToShow).setVisible(false);
				submenuStartIndex--;
				subMenus.get(submenuStartIndex).setVisible(true);
			}
			upMenuItem.setEnabled(0 < submenuStartIndex);
			downMenuItem.setEnabled(1 < subMenus.size() - (submenuStartIndex + subMenuItemsToShow));
			parentMenu.revalidate();
			parentMenu.repaint();
		}
	}
}
