package com.hiveworkshop.rms.ui.application.edit.animation;

import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.Named;
import com.hiveworkshop.rms.editor.model.TimelineContainer;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;

public class TimeLinePopup extends JPopupMenu {
	private final KeyframeHandler keyframeHandler;
	private TimeEnvironmentImpl timeEnvironment;
	private final ArrayList<JMenu> subMenus = new ArrayList<>();
	private int submenuStartIndex = 0;
	private int menuItems = 0;
	private int subMenuItemsToShow = 20;

	private final JMenuItem upMenuItem = new JMenuItem("\u25B2");
	private final JMenuItem downMenuItem = new JMenuItem("\u25BC");


	public TimeLinePopup(KeyframeHandler keyframeHandler){
		this.keyframeHandler = keyframeHandler;
		downMenuItem.addItemListener(e -> doScroll(true, upMenuItem, downMenuItem));
		upMenuItem.addItemListener(e -> doScroll(false, upMenuItem, downMenuItem));
		upMenuItem.addMouseMotionListener(getMouseAdapter(false));
		downMenuItem.addMouseMotionListener(getMouseAdapter(true));
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

	public TimeLinePopup setTimeEnvironment(TimeEnvironmentImpl timeEnvironment) {
		this.timeEnvironment = timeEnvironment;
		return this;
	}

	public void fillAndShow(Integer time, Component invoker, int x, int y, boolean objectMenus){
		removeAll();

		JMenuItem timeIndicator = new JMenuItem("" + time + " (" + keyframeHandler.getSelectionToUse().size() + " Nodes)");
		timeIndicator.setEnabled(false);
		add(timeIndicator);
		addSeparator();
		add(getMenuItem("Delete All", e -> keyframeHandler.deleteKeyframes("delete keyframe", time, keyframeHandler.getKeyFrame(time).getObjects())));
		addSeparator();
		if(objectMenus){
			add(getMenuItem("Cut", e -> keyframeHandler.cutItem(time)));
		}
		add(getMenuItem("Copy", e -> keyframeHandler.copyKeyframes(time)));
		add(getMenuItem("Copy Frame (whole model)", e -> keyframeHandler.copyAllKeyframes(time)));
		add(getMenuItem("Paste", e -> keyframeHandler.pasteToAllSelected(time)));

		addSeparator();
		JMenuItem nodeIndicator = new JMenuItem("");
		add(nodeIndicator);
		addSeparator();
		int nodes = 0;
		nodeIndicator.setEnabled(false);
		menuItems = getComponentCount();
		subMenus.clear();
		if(objectMenus){
			for (TimelineContainer object : keyframeHandler.getKeyFrame(time).getObjects()) {
				if(!object.getAnimFlags().isEmpty()){
					for (AnimFlag<?> flag : object.getAnimFlags()) {
						if (flag.hasEntryAt(timeEnvironment.getCurrentSequence(), time)) {
							String name;
							if(object instanceof Named){
								name = ((Named) object).getName();
							} else {
								name = object.getClass().getSimpleName();
							}
							JMenu subMenu = new JMenu(name + ": " + flag.getName());

							subMenu.add(getMenuItem("Delete", e -> keyframeHandler.deleteKeyframe(flag, time)));
							subMenu.addSeparator();
							subMenu.add(getMenuItem("Cut", e -> keyframeHandler.cutSpecificItem(time, object, flag)));
							subMenu.add(getMenuItem("Copy", e -> keyframeHandler.copyKeyframes(object, flag, time)));
							subMenu.add(getMenuItem("Paste", e -> keyframeHandler.pasteToSpecificTimeline(time, flag)));

							subMenus.add(subMenu);
							nodes++;
						}
					}
//					JMenu objectMenu = getObjectMenu(time, object);
//					if(0 < objectMenu.getItemCount()){
//						subMenus.add(objectMenu);
//						nodes++;
//					}
				}
			}
//			addSeparator();
		}
		addSubmenus();

		nodeIndicator.setText(nodes + " Nodes with keyframes");
		show(invoker, x, y);
	}

	private void addSubmenus(){
		submenuStartIndex = 0;
		if(subMenus.size()>subMenuItemsToShow){
			add(upMenuItem);
			upMenuItem.setEnabled(false);
		}
		for(int i = 0; i<subMenuItemsToShow && i<subMenus.size(); i++){
			add(subMenus.get(i));
		}
		if(subMenus.size()>subMenuItemsToShow){
			add(downMenuItem);
		}
	}

	private void doScroll(boolean down, JMenuItem upMenuItem, JMenuItem downMenuItem){
		if(upMenuItem.isEnabled() && !down || downMenuItem.isEnabled() && down){
			if(down && 1 < subMenus.size() - (submenuStartIndex + subMenuItemsToShow)){
				remove(subMenus.get(submenuStartIndex));
				submenuStartIndex++;
				add(subMenus.get(submenuStartIndex + subMenuItemsToShow), getComponentCount()-1);
			} else if (!down && 0<submenuStartIndex){
				remove(subMenus.get(submenuStartIndex+subMenuItemsToShow));
				submenuStartIndex--;
				add(subMenus.get(submenuStartIndex), menuItems+1);
			}
			upMenuItem.setEnabled(0 < submenuStartIndex);
			downMenuItem.setEnabled(1 < subMenus.size() - (submenuStartIndex + subMenuItemsToShow));
			revalidate();
			repaint();
		}
	}


	private JMenu getObjectMenu(Integer time, IdObject object) {
		JMenu objectMenu = new JMenu(object.getName());
		objectMenu.add(getMenuItem("Delete", e -> keyframeHandler.deleteKeyframes("delete keyframe", time, Collections.singleton(object))));
		objectMenu.add(getMenuItem("Copy Transforms", e -> keyframeHandler.copyObjectKeyframe(time, object)));
		objectMenu.addSeparator();
		for (AnimFlag<?> flag : object.getAnimFlags()) {
			if (flag.hasEntryAt(timeEnvironment.getCurrentSequence(), time)) {
				JMenu subMenu = new JMenu(flag.getName());

				subMenu.add(getMenuItem("Delete", e -> keyframeHandler.deleteKeyframe(flag, time)));
				subMenu.addSeparator();
				subMenu.add(getMenuItem("Cut", e -> keyframeHandler.cutSpecificItem(time, object, flag)));
				subMenu.add(getMenuItem("Copy", e -> keyframeHandler.copyKeyframes(object, flag, time)));
				subMenu.add(getMenuItem("Paste", e -> keyframeHandler.pasteToSpecificTimeline(time, flag)));

				objectMenu.add(subMenu);
			}
		}
		return objectMenu;
	}


	private JMenuItem getMenuItem(String text, ActionListener actionListener) {
		JMenuItem menuItem = new JMenuItem(text);
		menuItem.addActionListener(actionListener);
		return menuItem;
	}
}
