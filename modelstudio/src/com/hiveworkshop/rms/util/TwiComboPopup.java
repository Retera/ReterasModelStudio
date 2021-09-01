package com.hiveworkshop.rms.util;

import javax.accessibility.AccessibleContext;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.ComboPopup;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;

public class TwiComboPopup<T> extends JPopupMenu implements ComboPopup {

	protected JList<T> list;

	final ListModel<T> EmptyListModel = new IterableListModel<>();

	private static Border LIST_BORDER = new LineBorder(Color.BLACK, 1);
	protected JComboBox<T> comboBox;
	protected JScrollPane scroller;

	private Handler handler;
	protected MouseMotionListener mouseMotionListener;
	protected MouseListener mouseListener;
	protected KeyListener keyListener;
	protected ListSelectionListener listSelectionListener;

	// Listeners that are attached to the list
	protected MouseListener listMouseListener;

	protected MouseMotionListener listMouseMotionListener;

	// Added to the combo box for bound properties
	protected PropertyChangeListener propertyChangeListener;

	// Added to the combo box model
	protected ListDataListener listDataListener;
	protected ItemListener itemListener;
	private MouseWheelListener scrollerMouseWheelListener;


	protected Timer autoscrollTimer;
	protected boolean hasEntered = false;
	protected boolean isAutoScrolling = false;
	protected int scrollDirection = SCROLL_UP;
	protected static final int SCROLL_UP = 0;
	protected static final int SCROLL_DOWN = 1;


	//========================================
	// begin ComboPopup method implementations
	//

	/**
	 * Implementation of ComboPopup.show().
	 */
	@SuppressWarnings("deprecation")
	public void show() {
		comboBox.firePopupMenuWillBecomeVisible();
		setListSelection(comboBox.getSelectedIndex());
		Point location = getPopupLocation();
		show(comboBox, location.x, location.y);
	}


	/**
	 * Implementation of ComboPopup.hide().
	 */
	@SuppressWarnings("deprecation")
	public void hide() {
		MenuSelectionManager manager = MenuSelectionManager.defaultManager();
		MenuElement[] selection = manager.getSelectedPath();
		for (int i = 0; i < selection.length; i++) {
			if (selection[i] == this) {
				manager.clearSelectedPath();
				break;
			}
		}
		if (selection.length > 0) {
			comboBox.repaint();
		}
	}

	/**
	 * Implementation of ComboPopup.getList().
	 */
	public JList<Object> getList() {
		return (JList<Object>) list;
	}

	/**
	 * Implementation of ComboPopup.getMouseListener().
	 *
	 * @return a <code>MouseListener</code> or null
	 * @see ComboPopup#getMouseListener
	 */
	public MouseListener getMouseListener() {
		if (mouseListener == null) {
			mouseListener = createMouseListener();
		}
		return mouseListener;
	}

	/**
	 * Implementation of ComboPopup.getMouseMotionListener().
	 *
	 * @return a <code>MouseMotionListener</code> or null
	 * @see ComboPopup#getMouseMotionListener
	 */
	public MouseMotionListener getMouseMotionListener() {
		if (mouseMotionListener == null) {
			mouseMotionListener = createMouseMotionListener();
		}
		return mouseMotionListener;
	}

	/**
	 * Implementation of ComboPopup.getKeyListener().
	 *
	 * @return a <code>KeyListener</code> or null
	 * @see ComboPopup#getKeyListener
	 */
	public KeyListener getKeyListener() {
		if (keyListener == null) {
			keyListener = createKeyListener();
		}
		return keyListener;
	}

	/**
	 * Called when the UI is uninstalling.  Since this popup isn't in the component
	 * tree, it won't get it's uninstallUI() called.  It removes the listeners that
	 * were added in addComboBoxListeners().
	 */
	public void uninstallingUI() {
		if (propertyChangeListener != null) {
			comboBox.removePropertyChangeListener(propertyChangeListener);
		}
		if (itemListener != null) {
			comboBox.removeItemListener(itemListener);
		}
		uninstallComboBoxModelListeners(comboBox.getModel());
		uninstallListListeners();
		uninstallScrollerListeners();
		// We do this, otherwise the listener the ui installs on
		// the model (the combobox model in this case) will keep a
		// reference to the list, causing the list (and us) to never get gced.
		list.setModel((ListModel<T>) EmptyListModel);
	}

	//
	// end ComboPopup method implementations
	//======================================

	/**
	 * Removes the listeners from the combo box model
	 *
	 * @param model The combo box model to install listeners
	 * @see #installComboBoxModelListeners
	 */
	protected void uninstallComboBoxModelListeners(ComboBoxModel<?> model) {
		if (model != null && listDataListener != null) {
			model.removeListDataListener(listDataListener);
		}
	}


	//===================================================================
	// begin Initialization routines

	/**
	 * Constructs a new instance of {@code BasicComboPopup}.
	 *
	 * @param combo an instance of {@code JComboBox}
	 */
	public TwiComboPopup(JComboBox<T> combo) {
		super();
		setName("ComboPopup.popup");
		comboBox = combo;

//		System.out.println("TwiComboPopup: Settin LW");
		setLightWeightPopupEnabled(comboBox.isLightWeightPopupEnabled());

		// UI construction of the popup.
//		System.out.println("TwiComboPopup: createList()");
		list = createList();

		list.setName("ComboBox.list");
//		System.out.println("TwiComboPopup: configureList()");
		configureList();
//		System.out.println("TwiComboPopup: createScroller()");
		scroller = createScroller();
		scroller.setName("ComboBox.scrollPane");
//		System.out.println("TwiComboPopup: configureScroller()");
		configureScroller();
//		System.out.println("TwiComboPopup: configurePopup()");
		configurePopup();

//		System.out.println("TwiComboPopup: installComboBoxListeners()");
		installComboBoxListeners();
	}

	public TwiComboPopup(JComboBox<T> combo, T prototypeValue) {
		super();
		setName("ComboPopup.popup");
		comboBox = combo;

//		System.out.println("TwiComboPopup: Settin LW");
		setLightWeightPopupEnabled(comboBox.isLightWeightPopupEnabled());

		// UI construction of the popup.
//		System.out.println("TwiComboPopup: createList()");
		list = createList();
		list.setPrototypeCellValue(prototypeValue);
		list.setName("ComboBox.list");
//		System.out.println("TwiComboPopup: configureList()");
		configureList();
//		System.out.println("TwiComboPopup: createScroller()");
		scroller = createScroller();
		scroller.setName("ComboBox.scrollPane");
//		System.out.println("TwiComboPopup: configureScroller()");
		configureScroller();
//		System.out.println("TwiComboPopup: configurePopup()");
		configurePopup();

//		System.out.println("TwiComboPopup: installComboBoxListeners()");
		installComboBoxListeners();
	}

	// Overriden PopupMenuListener notification methods to inform combo box
	// PopupMenuListeners.

	protected void firePopupMenuWillBecomeVisible() {
		if (scrollerMouseWheelListener != null) {
			comboBox.addMouseWheelListener(scrollerMouseWheelListener);
		}
		super.firePopupMenuWillBecomeVisible();
		// comboBox.firePopupMenuWillBecomeVisible() is called from TwiComboPopup.show() method
		// to let the user change the popup menu from the PopupMenuListener.popupMenuWillBecomeVisible()
	}

	protected void firePopupMenuWillBecomeInvisible() {
		if (scrollerMouseWheelListener != null) {
			comboBox.removeMouseWheelListener(scrollerMouseWheelListener);
		}
		super.firePopupMenuWillBecomeInvisible();
		comboBox.firePopupMenuWillBecomeInvisible();
	}

	protected void firePopupMenuCanceled() {
		if (scrollerMouseWheelListener != null) {
			comboBox.removeMouseWheelListener(scrollerMouseWheelListener);
		}
		super.firePopupMenuCanceled();
		comboBox.firePopupMenuCanceled();
	}

	/**
	 * Creates a listener
	 * that will watch for mouse-press and release events on the combo box.
	 *
	 * <strong>Warning:</strong>
	 * When overriding this method, make sure to maintain the existing
	 * behavior.
	 *
	 * @return a <code>MouseListener</code> which will be added to
	 * the combo box or null
	 */
	protected MouseListener createMouseListener() {
		return getHandler();
	}

	/**
	 * Creates the mouse motion listener which will be added to the combo
	 * box.
	 *
	 * <strong>Warning:</strong>
	 * When overriding this method, make sure to maintain the existing
	 * behavior.
	 *
	 * @return a <code>MouseMotionListener</code> which will be added to
	 * the combo box or null
	 */
	protected MouseMotionListener createMouseMotionListener() {
		return getHandler();
	}

	/**
	 * Creates the key listener that will be added to the combo box. If
	 * this method returns null then it will not be added to the combo box.
	 *
	 * @return a <code>KeyListener</code> or null
	 */
	protected KeyListener createKeyListener() {
		return null;
	}

	/**
	 * Creates a list selection listener that watches for selection changes in
	 * the popup's list.  If this method returns null then it will not
	 * be added to the popup list.
	 *
	 * @return an instance of a <code>ListSelectionListener</code> or null
	 */
	protected ListSelectionListener createListSelectionListener() {
		return null;
	}

	/**
	 * Creates a list data listener which will be added to the
	 * <code>ComboBoxModel</code>. If this method returns null then
	 * it will not be added to the combo box model.
	 *
	 * @return an instance of a <code>ListDataListener</code> or null
	 */
	protected ListDataListener createListDataListener() {
		return null;
	}

	/**
	 * Creates a mouse listener that watches for mouse events in
	 * the popup's list. If this method returns null then it will
	 * not be added to the combo box.
	 *
	 * @return an instance of a <code>MouseListener</code> or null
	 */
	protected MouseListener createListMouseListener() {
		return getHandler();
	}

	/**
	 * Creates a mouse motion listener that watches for mouse motion
	 * events in the popup's list. If this method returns null then it will
	 * not be added to the combo box.
	 *
	 * @return an instance of a <code>MouseMotionListener</code> or null
	 */
	protected MouseMotionListener createListMouseMotionListener() {
		return getHandler();
	}

	/**
	 * Creates a <code>PropertyChangeListener</code> which will be added to
	 * the combo box. If this method returns null then it will not
	 * be added to the combo box.
	 *
	 * @return an instance of a <code>PropertyChangeListener</code> or null
	 */
	protected PropertyChangeListener createPropertyChangeListener() {
		return getHandler();
	}

	/**
	 * Creates an <code>ItemListener</code> which will be added to the
	 * combo box. If this method returns null then it will not
	 * be added to the combo box.
	 * <p>
	 * Subclasses may override this method to return instances of their own
	 * ItemEvent handlers.
	 *
	 * @return an instance of an <code>ItemListener</code> or null
	 */
	protected ItemListener createItemListener() {
		return getHandler();
	}

	private Handler getHandler() {
		if (handler == null) {
			handler = new Handler();
		}
		return handler;
	}

	/**
	 * Creates the JList used in the popup to display
	 * the items in the combo box model. This method is called when the UI class
	 * is created.
	 *
	 * @return a <code>JList</code> used to display the combo box items
	 */
	protected JList<T> createList() {
		return new JList<T>(comboBox.getModel()) {
			public void processMouseEvent(MouseEvent e) {
				if (e.isPopupTrigger()) {
					// Fix for 4234053. Filter out the Control Key from the list.
					// ie., don't allow CTRL key deselection.
					Toolkit toolkit = Toolkit.getDefaultToolkit();
					MouseEvent newEvent = new MouseEvent(
							(Component) e.getSource(), e.getID(), e.getWhen(),
							e.getModifiersEx() ^ toolkit.getMenuShortcutKeyMaskEx(),
							e.getX(), e.getY(),
							e.getXOnScreen(), e.getYOnScreen(),
							e.getClickCount(),
							e.isPopupTrigger(),
							MouseEvent.NOBUTTON);
					e = newEvent;
				}
				super.processMouseEvent(e);
			}
		};
	}

	/**
	 * Configures the list which is used to hold the combo box items in the
	 * popup. This method is called when the UI class
	 * is created.
	 *
	 * @see #createList
	 */
	protected void configureList() {
		list.setFont(comboBox.getFont());
		list.setForeground(comboBox.getForeground());
		list.setBackground(comboBox.getBackground());
//		System.out.println("TwiComboPopup#configureList: setColor");
		list.setSelectionForeground(UIManager.getColor("ComboBox.selectionForeground"));
		list.setSelectionBackground(UIManager.getColor("ComboBox.selectionBackground"));
		list.setBorder(null);
//		System.out.println("TwiComboPopup#configureList: setCellRenderer");
		list.setCellRenderer(comboBox.getRenderer());
		list.setFocusable(false);
//		System.out.println("TwiComboPopup#configureList: setSelectionMode");
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//		System.out.println("TwiComboPopup#configureList: setListSelection");
		setListSelection(comboBox.getSelectedIndex());

//		System.out.println("TwiComboPopup#configureList: installListListeners()");
		installListListeners();
	}

	/**
	 * Adds the listeners to the list control.
	 */
	protected void installListListeners() {
		if ((listMouseListener = createListMouseListener()) != null) {
			list.addMouseListener(listMouseListener);
		}
		if ((listMouseMotionListener = createListMouseMotionListener()) != null) {
			list.addMouseMotionListener(listMouseMotionListener);
		}
		if ((listSelectionListener = createListSelectionListener()) != null) {
			list.addListSelectionListener(listSelectionListener);
		}
	}

	void uninstallListListeners() {
		if (listMouseListener != null) {
			list.removeMouseListener(listMouseListener);
			listMouseListener = null;
		}
		if (listMouseMotionListener != null) {
			list.removeMouseMotionListener(listMouseMotionListener);
			listMouseMotionListener = null;
		}
		if (listSelectionListener != null) {
			list.removeListSelectionListener(listSelectionListener);
			listSelectionListener = null;
		}
		handler = null;
	}

	/**
	 * Creates the scroll pane which houses the scrollable list.
	 *
	 * @return the scroll pane which houses the scrollable list
	 */
	protected JScrollPane createScroller() {
		JScrollPane sp = new JScrollPane(list,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		sp.setHorizontalScrollBar(null);
		return sp;
	}

	/**
	 * Configures the scrollable portion which holds the list within
	 * the combo box popup. This method is called when the UI class
	 * is created.
	 */
	protected void configureScroller() {
		scroller.setFocusable(false);
		scroller.getVerticalScrollBar().setFocusable(false);
		scroller.setBorder(null);
		installScrollerListeners();
	}

	/**
	 * Configures the popup portion of the combo box. This method is called
	 * when the UI class is created.
	 */
	protected void configurePopup() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorderPainted(true);
		setBorder(LIST_BORDER);
		setOpaque(false);
		add(scroller);
		setDoubleBuffered(true);
		setFocusable(false);
	}

	private void installScrollerListeners() {
		scrollerMouseWheelListener = getHandler();
		if (scrollerMouseWheelListener != null) {
			scroller.addMouseWheelListener(scrollerMouseWheelListener);
		}
	}

	private void uninstallScrollerListeners() {
		if (scrollerMouseWheelListener != null) {
			scroller.removeMouseWheelListener(scrollerMouseWheelListener);
			scrollerMouseWheelListener = null;
		}
	}

	/**
	 * This method adds the necessary listeners to the JComboBox.
	 */
	protected void installComboBoxListeners() {
		if ((propertyChangeListener = createPropertyChangeListener()) != null) {
			comboBox.addPropertyChangeListener(propertyChangeListener);
		}
		if ((itemListener = createItemListener()) != null) {
			comboBox.addItemListener(itemListener);
		}
		installComboBoxModelListeners(comboBox.getModel());
	}

	/**
	 * Installs the listeners on the combo box model. Any listeners installed
	 * on the combo box model should be removed in
	 * <code>uninstallComboBoxModelListeners</code>.
	 *
	 * @param model The combo box model to install listeners
	 * @see #uninstallComboBoxModelListeners
	 */
	protected void installComboBoxModelListeners(ComboBoxModel<?> model) {
		if (model != null && (listDataListener = createListDataListener()) != null) {
			model.addListDataListener(listDataListener);
		}
	}

	//
	// end Initialization routines
	//=================================================================


	//===================================================================
	// begin Event Listenters
	//


	private class AutoScrollActionHandler implements ActionListener {
		private int direction;

		AutoScrollActionHandler(int direction) {
			this.direction = direction;
		}

		public void actionPerformed(ActionEvent e) {
			if (direction == SCROLL_UP) {
				autoScrollUp();
			} else {
				autoScrollDown();
			}
		}
	}


	private class Handler implements ItemListener, MouseListener,
			MouseMotionListener, MouseWheelListener,
			PropertyChangeListener, Serializable {
		//
		// MouseListener
		// NOTE: this is added to both the JList and JComboBox
		//
		public void mouseClicked(MouseEvent e) {
		}

		public void mousePressed(MouseEvent e) {
			if (e.getSource() == list) {
				return;
			}
			if (!SwingUtilities.isLeftMouseButton(e) || !comboBox.isEnabled())
				return;

			if (comboBox.isEditable()) {
				Component comp = comboBox.getEditor().getEditorComponent();
				if ((!(comp instanceof JComponent)) || ((JComponent) comp).isRequestFocusEnabled()) {
					comp.requestFocus();
				}
			} else if (comboBox.isRequestFocusEnabled()) {
				comboBox.requestFocus();
			}
			togglePopup();
		}

		public void mouseReleased(MouseEvent e) {
			if (e.getSource() == list) {
				if (list.getModel().getSize() > 0) {
					// JList mouse listener
					if (comboBox.getSelectedIndex() == list.getSelectedIndex()) {
						comboBox.getEditor().setItem(list.getSelectedValue());
					}
					comboBox.setSelectedIndex(list.getSelectedIndex());
				}
				comboBox.setPopupVisible(false);
				// workaround for cancelling an edited item (bug 4530953)
				if (comboBox.isEditable() && comboBox.getEditor() != null) {
					comboBox.configureEditor(comboBox.getEditor(),
							comboBox.getSelectedItem());
				}
				return;
			}
			// JComboBox mouse listener
			Component source = (Component) e.getSource();
			Dimension size = source.getSize();
			Rectangle bounds = new Rectangle(0, 0, size.width, size.height);
			if (!bounds.contains(e.getPoint())) {
				MouseEvent newEvent = convertMouseEvent(e);
				Point location = newEvent.getPoint();
				Rectangle r = new Rectangle();
				list.computeVisibleRect(r);
				if (r.contains(location)) {
					if (comboBox.getSelectedIndex() == list.getSelectedIndex()) {
						comboBox.getEditor().setItem(list.getSelectedValue());
					}
					comboBox.setSelectedIndex(list.getSelectedIndex());
				}
				comboBox.setPopupVisible(false);
			}
			hasEntered = false;
			stopAutoScrolling();
		}

		public void mouseEntered(MouseEvent e) {
		}

		public void mouseExited(MouseEvent e) {
		}

		//
		// MouseMotionListener:
		// NOTE: this is added to both the List and ComboBox
		//
		public void mouseMoved(MouseEvent anEvent) {
			if (anEvent.getSource() == list) {
				Point location = anEvent.getPoint();
				Rectangle r = new Rectangle();
				list.computeVisibleRect(r);
				if (r.contains(location)) {
					updateListBoxSelectionForEvent(anEvent, false);
				}
			}
		}

		public void mouseDragged(MouseEvent e) {
			if (e.getSource() == list) {
				return;
			}
			if (isVisible()) {
				MouseEvent newEvent = convertMouseEvent(e);
				Rectangle r = new Rectangle();
				list.computeVisibleRect(r);

				if (newEvent.getPoint().y >= r.y && newEvent.getPoint().y <= r.y + r.height - 1) {
					hasEntered = true;
					if (isAutoScrolling) {
						stopAutoScrolling();
					}
					Point location = newEvent.getPoint();
					if (r.contains(location)) {
						updateListBoxSelectionForEvent(newEvent, false);
					}
				} else {
					if (hasEntered) {
						int directionToScroll = newEvent.getPoint().y < r.y ? SCROLL_UP : SCROLL_DOWN;
						if (isAutoScrolling && scrollDirection != directionToScroll) {
							stopAutoScrolling();
							startAutoScrolling(directionToScroll);
						} else if (!isAutoScrolling) {
							startAutoScrolling(directionToScroll);
						}
					} else {
						if (e.getPoint().y < 0) {
							hasEntered = true;
							startAutoScrolling(SCROLL_UP);
						}
					}
				}
			}
		}

		//
		// PropertyChangeListener
		//
		public void propertyChange(PropertyChangeEvent e) {
			@SuppressWarnings("unchecked")
			JComboBox<T> comboBox = (JComboBox) e.getSource();
			String propertyName = e.getPropertyName();

			if (propertyName == "model") {
				@SuppressWarnings("unchecked")
				ComboBoxModel<T> oldModel = (ComboBoxModel) e.getOldValue();
				@SuppressWarnings("unchecked")
				ComboBoxModel<T> newModel = (ComboBoxModel) e.getNewValue();
				uninstallComboBoxModelListeners(oldModel);
				installComboBoxModelListeners(newModel);

				list.setModel(newModel);

				if (isVisible()) {
					hide();
				}
			} else if (propertyName == "renderer") {
				list.setCellRenderer(comboBox.getRenderer());
				if (isVisible()) {
					hide();
				}
			} else if (propertyName == "componentOrientation") {
				// Pass along the new component orientation
				// to the list and the scroller

				ComponentOrientation o = (ComponentOrientation) e.getNewValue();

				JList<?> list = getList();
				if (list != null && list.getComponentOrientation() != o) {
					list.setComponentOrientation(o);
				}

				if (scroller != null && scroller.getComponentOrientation() != o) {
					scroller.setComponentOrientation(o);
				}

				if (o != getComponentOrientation()) {
					setComponentOrientation(o);
				}
			} else if (propertyName == "lightWeightPopupEnabled") {
				setLightWeightPopupEnabled(comboBox.isLightWeightPopupEnabled());
			}
		}

		//
		// ItemListener
		//
		public void itemStateChanged(ItemEvent e) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				@SuppressWarnings("unchecked")
				JComboBox<Object> comboBox = (JComboBox) e.getSource();
				setListSelection(comboBox.getSelectedIndex());
			} else {
				setListSelection(-1);
			}
		}

		//
		// MouseWheelListener
		//
		public void mouseWheelMoved(MouseWheelEvent e) {
			e.consume();
		}
	}

	//
	// end Event Listeners
	//=================================================================


	/**
	 * Overridden to unconditionally return false.
	 */
	@SuppressWarnings("deprecation")
	public boolean isFocusTraversable() {
		return false;
	}

	//===================================================================
	// begin Autoscroll methods
	//

	/**
	 * This protected method is implementation specific and should be private.
	 * do not call or override.
	 *
	 * @param direction the direction of scrolling
	 */
	protected void startAutoScrolling(int direction) {
		// XXX - should be a private method within InvocationMouseMotionHandler
		// if possible.
		if (isAutoScrolling) {
			autoscrollTimer.stop();
		}

		isAutoScrolling = true;

		if (direction == SCROLL_UP) {
			scrollDirection = SCROLL_UP;
			Point convertedPoint = SwingUtilities.convertPoint(scroller, new Point(1, 1), list);
			int top = list.locationToIndex(convertedPoint);
			list.setSelectedIndex(top);

			autoscrollTimer = new Timer(100, new TwiComboPopup.AutoScrollActionHandler(SCROLL_UP));
		} else if (direction == SCROLL_DOWN) {
			scrollDirection = SCROLL_DOWN;
			Dimension size = scroller.getSize();
			Point convertedPoint = SwingUtilities.convertPoint(scroller, new Point(1, (size.height - 1) - 2), list);
			int bottom = list.locationToIndex(convertedPoint);
			list.setSelectedIndex(bottom);

			autoscrollTimer = new Timer(100, new TwiComboPopup.AutoScrollActionHandler(SCROLL_DOWN));
		}
		autoscrollTimer.start();
	}

	protected void stopAutoScrolling() {
		isAutoScrolling = false;

		if (autoscrollTimer != null) {
			autoscrollTimer.stop();
			autoscrollTimer = null;
		}
	}

	protected void autoScrollUp() {
		int index = list.getSelectedIndex();
		if (index > 0) {
			list.setSelectedIndex(index - 1);
			list.ensureIndexIsVisible(index - 1);
		}
	}

	protected void autoScrollDown() {
		int index = list.getSelectedIndex();
		int lastItem = list.getModel().getSize() - 1;
		if (index < lastItem) {
			list.setSelectedIndex(index + 1);
			list.ensureIndexIsVisible(index + 1);
		}
	}

	//
	// end Autoscroll methods
	//=================================================================


	//===================================================================
	// begin Utility methods
	//

	/**
	 * Gets the AccessibleContext associated with this TwiComboPopup.
	 * The AccessibleContext will have its parent set to the ComboBox.
	 *
	 * @return an AccessibleContext for the TwiComboPopup
	 * @since 1.5
	 */
	public AccessibleContext getAccessibleContext() {
		AccessibleContext context = super.getAccessibleContext();
		context.setAccessibleParent(comboBox);
		return context;
	}


	/**
	 * This is a utility method that helps event handlers figure out where to
	 * send the focus when the popup is brought up.  The standard implementation
	 * delegates the focus to the editor (if the combo box is editable) or to
	 * the JComboBox if it is not editable.
	 *
	 * @param e a mouse event
	 */
	protected void delegateFocus(MouseEvent e) {
		if (comboBox.isEditable()) {
			Component comp = comboBox.getEditor().getEditorComponent();
			if ((!(comp instanceof JComponent)) || ((JComponent) comp).isRequestFocusEnabled()) {
				comp.requestFocus();
			}
		} else if (comboBox.isRequestFocusEnabled()) {
			comboBox.requestFocus();
		}
	}

	/**
	 * Makes the popup visible if it is hidden and makes it hidden if it is
	 * visible.
	 */
	protected void togglePopup() {
		if (isVisible()) {
			hide();
		} else {
			show();
		}
	}

	/**
	 * Sets the list selection index to the selectedIndex. This
	 * method is used to synchronize the list selection with the
	 * combo box selection.
	 *
	 * @param selectedIndex the index to set the list
	 */
	private void setListSelection(int selectedIndex) {

		if (selectedIndex == -1) {
//			System.out.println("TwiComboPopup#setListSelection: clearSelection");
			list.clearSelection();
		} else {
//			System.out.println("TwiComboPopup#setListSelection: setSelectedIndex");
			list.setSelectedIndex(selectedIndex);
//			System.out.println("TwiComboPopup#setListSelection: ensureIndexIsVisible");
			list.ensureIndexIsVisible(selectedIndex);
		}
	}

	/**
	 * Converts mouse event.
	 *
	 * @param e a mouse event
	 * @return converted mouse event
	 */
	protected MouseEvent convertMouseEvent(MouseEvent e) {
		Point convertedPoint = SwingUtilities.convertPoint((Component) e.getSource(),
				e.getPoint(), list);
		@SuppressWarnings("deprecation")
		MouseEvent newEvent = new MouseEvent((Component) e.getSource(),
				e.getID(),
				e.getWhen(),
				e.getModifiers(),
				convertedPoint.x,
				convertedPoint.y,
				e.getXOnScreen(),
				e.getYOnScreen(),
				e.getClickCount(),
				e.isPopupTrigger(),
				MouseEvent.NOBUTTON);
		return newEvent;
	}


	/**
	 * Retrieves the height of the popup based on the current
	 * ListCellRenderer and the maximum row count.
	 *
	 * @param maxRowCount the row count
	 * @return the height of the popup
	 */
	protected int getPopupHeightForRowCount(int maxRowCount) {
		// Set the cached value of the minimum row count
		int minRowCount = Math.min(maxRowCount, comboBox.getItemCount());
		int height = 0;
		ListCellRenderer<? super T> renderer = list.getCellRenderer();
		T value = null;

		for (int i = 0; i < minRowCount; ++i) {
			value = list.getModel().getElementAt(i);
			Component c = renderer.getListCellRendererComponent(list, value, i, false, false);
			height += c.getPreferredSize().height;
		}

		if (height == 0) {
			height = comboBox.getHeight();
		}

		Border border = scroller.getViewportBorder();
		if (border != null) {
			Insets insets = border.getBorderInsets(null);
			height += insets.top + insets.bottom;
		}

		border = scroller.getBorder();
		if (border != null) {
			Insets insets = border.getBorderInsets(null);
			height += insets.top + insets.bottom;
		}

		return height;
	}

	/**
	 * Calculate the placement and size of the popup portion of the combo box based
	 * on the combo box location and the enclosing screen bounds. If
	 * no transformations are required, then the returned rectangle will
	 * have the same values as the parameters.
	 *
	 * @param px starting x location
	 * @param py starting y location
	 * @param pw starting width
	 * @param ph starting height
	 * @return a rectangle which represents the placement and size of the popup
	 */
	protected Rectangle computePopupBounds(int px, int py, int pw, int ph) {
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Rectangle screenBounds;

		// Calculate the desktop dimensions relative to the combo box.
		GraphicsConfiguration gc = comboBox.getGraphicsConfiguration();
		Point p = new Point();
		SwingUtilities.convertPointFromScreen(p, comboBox);
		if (gc != null) {
			Insets screenInsets = toolkit.getScreenInsets(gc);
			screenBounds = gc.getBounds();
			screenBounds.width -= (screenInsets.left + screenInsets.right);
			screenBounds.height -= (screenInsets.top + screenInsets.bottom);
			screenBounds.x += (p.x + screenInsets.left);
			screenBounds.y += (p.y + screenInsets.top);
		} else {
			screenBounds = new Rectangle(p, toolkit.getScreenSize());
		}
		int borderHeight = 0;
		Border popupBorder = getBorder();
		if (popupBorder != null) {
			Insets borderInsets = popupBorder.getBorderInsets(this);
			borderHeight = borderInsets.top + borderInsets.bottom;
			screenBounds.width -= (borderInsets.left + borderInsets.right);
			screenBounds.height -= borderHeight;
		}
		Rectangle rect = new Rectangle(px, py, pw, ph);
		if (py + ph > screenBounds.y + screenBounds.height) {
			if (ph <= -screenBounds.y - borderHeight) {
				// popup goes above
				rect.y = -ph - borderHeight;
			} else {
				// a full screen height popup
				rect.y = screenBounds.y + Math.max(0, (screenBounds.height - ph) / 2);
				rect.height = Math.min(screenBounds.height, ph);
			}
		}
		return rect;
	}

	/**
	 * Calculates the upper left location of the Popup.
	 */
	private Point getPopupLocation() {
		Dimension popupSize = comboBox.getSize();
		Insets insets = getInsets();

		// reduce the width of the scrollpane by the insets so that the popup
		// is the same width as the combo box.
		popupSize.setSize(popupSize.width - (insets.right + insets.left), getPopupHeightForRowCount(comboBox.getMaximumRowCount()));
		Rectangle popupBounds = computePopupBounds(0, comboBox.getBounds().height, popupSize.width, popupSize.height);
		Dimension scrollSize = popupBounds.getSize();
		Point popupLocation = popupBounds.getLocation();

		scroller.setMaximumSize(scrollSize);
		scroller.setPreferredSize(scrollSize);
		scroller.setMinimumSize(scrollSize);

		list.revalidate();

		return popupLocation;
	}

	/**
	 * A utility method used by the event listeners.  Given a mouse event, it changes
	 * the list selection to the list item below the mouse.
	 *
	 * @param anEvent      a mouse event
	 * @param shouldScroll if {@code true} list should be scrolled.
	 */
	protected void updateListBoxSelectionForEvent(MouseEvent anEvent, boolean shouldScroll) {
		// XXX - only seems to be called from this class. shouldScroll flag is
		// never true
		Point location = anEvent.getPoint();
		if (list == null)
			return;
		int index = list.locationToIndex(location);
		if (index == -1) {
			if (location.y < 0)
				index = 0;
			else
				index = comboBox.getModel().getSize() - 1;
		}
		if (list.getSelectedIndex() != index) {
			list.setSelectedIndex(index);
			if (shouldScroll)
				list.ensureIndexIsVisible(index);
		}
	}
}
