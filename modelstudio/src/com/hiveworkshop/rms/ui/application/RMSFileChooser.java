package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.ui.preferences.SaveProfileNew;
import com.hiveworkshop.rms.util.uiFactories.Button;
import com.jtattoo.plaf.BaseFileChooserUI;
import net.miginfocom.swing.MigLayout;

import javax.accessibility.AccessibleContext;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import javax.swing.plaf.FileChooserUI;
import java.awt.*;
import java.io.File;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

public class RMSFileChooser extends JFileChooser {
	private final JToolBar favToolBar;
	private final JPanel hideblePanel1;
	private final JPanel hideblePanel2;
//	private Set<File> favorites;
	private final SaveProfileNew saveProfile;
	private final Deque<File> recentBack;
	private final Deque<File> recentFwrd;
	{
		recentBack = new ArrayDeque<>();
		recentFwrd = new ArrayDeque<>();
	}

//	private void printContainer(Container container, String prefix) {
//		for (int i = 0; i< container.getComponentCount(); i++) {
//			Component inComp = container.getComponent(i);
//			System.out.println(prefix + i + ": "  + inComp);
//			if (inComp instanceof Container) {
//				printContainer((Container) inComp, "\t" + prefix + i + ".");
//			}
//		}
//	}

	public RMSFileChooser() {
		this(null);
	}
	public RMSFileChooser(SaveProfileNew saveProfile) {
		this.saveProfile = saveProfile;
//		if (saveProfile != null) {
//			favorites = saveProfile.getFavorites();
//		} else {
//			favorites = new TreeSet<>();
//		}

		JPanel outerPanel = new JPanel(new MigLayout("ins 0, gap 0, fill, hidemode 2", "[fill]", "[][grow, fill]"));
		hideblePanel1 = new JPanel(new MigLayout("ins 0, gap 0, fill, hidemode 2", "[fill]", "[][grow, fill]"));
		hideblePanel2 = new JPanel(new MigLayout("ins 0, gap 0, fill, hidemode 2", "[fill]", "[][grow, fill]"));
		hideblePanel2.setVisible(false);
		JPanel twiPanel = new JPanel(new MigLayout("ins 0, gap 0"));
		JScrollPane scrollPane = new JScrollPane(twiPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		scrollPane.setPreferredSize(new Dimension(160, 250));

		hideblePanel1.add(getContractToolbar(), "split, growx");
		hideblePanel1.add(getBf_toolBar(), "right, wrap");
		hideblePanel1.add(scrollPane, "growy");

		favToolBar = getFavToolBar();
		remakeFavList();
		twiPanel.add(favToolBar, "wrap");

		twiPanel.add(getDirToolBar());

		JToolBar expandToolbar = getExpandToolbar();
		hideblePanel2.add(expandToolbar, "right, wrap");
		JPanel fillPanel = new JPanel(new MigLayout());
		fillPanel.setPreferredSize(new Dimension(5, 250));
		hideblePanel2.add(fillPanel);

		outerPanel.add(hideblePanel1);
		outerPanel.add(hideblePanel2);
		add(outerPanel, BorderLayout.WEST);
	}

	private JToolBar getFavToolBar() {
		JToolBar favToolBar = new JToolBar(JToolBar.VERTICAL);
		favToolBar.setFloatable(false);
		return favToolBar;
	}

	private JToolBar getDirToolBar() {
		JToolBar dirToolBar = new JToolBar(JToolBar.VERTICAL);
		dirToolBar.setFloatable(false);

		File[] chooserShortcutPanelFiles = getFileSystemView().getChooserShortcutPanelFiles();
		System.out.println("files: " + Arrays.toString(chooserShortcutPanelFiles));
		for (File file : chooserShortcutPanelFiles) {
			Icon systemIcon = FileSystemView.getFileSystemView().getSystemIcon(file);
			String displayName = FileSystemView.getFileSystemView().getSystemDisplayName(file);
			dirToolBar.add(Button.create(displayName, systemIcon, e -> {setCurrentDirectory(file); repaint();}));
		}
		return dirToolBar;
	}

	private JToolBar getBf_toolBar() {
		JButton backButton = Button.create("\ud83e\udc70", e -> back());
		backButton.setToolTipText("Back");
		JButton frwdButton = Button.create("\ud83e\udc72", e -> forward());
		frwdButton.setToolTipText("Forward");
		JButton favorite = Button.create("\u2605", e -> favorite());
		favorite.setToolTipText("Add Shortcut");


		JToolBar bf_toolBar = new JToolBar(JToolBar.HORIZONTAL);
		bf_toolBar.setFloatable(false);
		bf_toolBar.add(backButton);
		bf_toolBar.add(frwdButton);
		bf_toolBar.add(favorite);
		return bf_toolBar;
	}
	private JToolBar getExpandToolbar() {
		JToolBar expandToolbar = new JToolBar(JToolBar.VERTICAL);
		JButton showButton = Button.create("\u25b6", e -> showFavs());
		showButton.setToolTipText("Show");

		JButton backButton = Button.create("\ud83e\udc70", e -> back());
		backButton.setToolTipText("Back");
		JButton frwdButton = Button.create("\ud83e\udc72", e -> forward());
		frwdButton.setToolTipText("Forward");
		JButton favorite = Button.create("\u2605", e -> favorite());
		favorite.setToolTipText("Add Shortcut");

		expandToolbar.setFloatable(false);
		expandToolbar.add(showButton);
		expandToolbar.addSeparator();
		expandToolbar.add(backButton);
		expandToolbar.add(frwdButton);
		expandToolbar.add(favorite);
		return expandToolbar;
	}
	private JToolBar getContractToolbar() {
		JButton hideButton = Button.create("\u25c0", e -> hideFavs());
		hideButton.setToolTipText("Hide");

		JToolBar expandToolbar = new JToolBar(JToolBar.HORIZONTAL);
		expandToolbar.setFloatable(false);
		expandToolbar.add(hideButton);
		return expandToolbar;
	}

	private void showFavs() {
		hideblePanel1.setVisible(true);
		hideblePanel2.setVisible(false);
		revalidate();
	}
	private void hideFavs() {
		hideblePanel1.setVisible(false);
		hideblePanel2.setVisible(true);
		revalidate();
	}

	private void back() {
		if (!recentBack.isEmpty()) {
			setCurrentDirectory(recentBack.peek());
		}
	}
	private void forward() {
		if (!recentFwrd.isEmpty()) {
			setCurrentDirectory(recentFwrd.peek());
		}
	}
	private void favorite() {
		if (saveProfile != null) {
			saveProfile.addFavorite(getCurrentDirectory());
		}
		remakeFavList();
		revalidate();
	}

	private void remakeFavList() {
		if (saveProfile != null) {
			favToolBar.removeAll();
			for (File file : saveProfile.getFavorites().getFiles()) {
				Icon systemIcon = FileSystemView.getFileSystemView().getSystemIcon(file);
				String displayName = FileSystemView.getFileSystemView().getSystemDisplayName(file);
				JButton favButton = Button.create(displayName, systemIcon, e -> setCurrentDirectory(file));
				JPopupMenu popup = new JPopupMenu();
				popup.add(new JMenuItem("Remove favorite")).addActionListener(e -> removeFav(file));
				favButton.setComponentPopupMenu(popup);
				favButton.setToolTipText(file.getPath());
				favToolBar.add(favButton);
			}
		}
	}

	private void removeFav(File file) {
		if (saveProfile != null) {
			saveProfile.removeFromFavorite(file);
		}
		remakeFavList();
		revalidate();
	}
	public void setCurrentDirectory(File dir) {
		if (recentBack != null) {
			if (recentBack.peek() == dir) {
				recentFwrd.push(getCurrentDirectory());
				recentBack.pop();
			} else if (recentFwrd.peek() == dir) {
				recentFwrd.pop();
				recentBack.push(getCurrentDirectory());
			} else {
				recentFwrd.clear();
				recentBack.push(getCurrentDirectory());
			}
		}
		super.setCurrentDirectory(dir);
	}

	protected JDialog createDialog(Component parent) throws HeadlessException {
		FileChooserUI ui = getUI();
		System.out.println("FileChooserUI: " + ui);
		if (ui instanceof BaseFileChooserUI bfcUi) {
			fixBottomPanel(bfcUi);
		}
		String title = ui.getDialogTitle(this);
		putClientProperty(AccessibleContext.ACCESSIBLE_DESCRIPTION_PROPERTY, title);

		JDialog dialog;
		Window window = getWindowForComponent(parent);
		if (window instanceof Frame) {
			dialog = new JDialog((Frame)window, title, true);
		} else {
			dialog = new JDialog((Dialog)window, title, true);
		}
		dialog.setComponentOrientation(this.getComponentOrientation());

		Container contentPane = dialog.getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(this, BorderLayout.CENTER);

		if (JDialog.isDefaultLookAndFeelDecorated()) {
			boolean supportsWindowDecorations = UIManager.getLookAndFeel().getSupportsWindowDecorations();
			if (supportsWindowDecorations) {
				dialog.getRootPane().setWindowDecorationStyle(JRootPane.FILE_CHOOSER_DIALOG);
			}
		}

//		printContainer(dialog, "Comp ");

		dialog.pack();
		dialog.setLocationRelativeTo(parent);

		return dialog;
	}

	private void fixBottomPanel(BaseFileChooserUI bUi) {
		JButton defaultButton = bUi.getDefaultButton(this);
		if (defaultButton != null && defaultButton.getParent() instanceof JPanel buttonPanel) {
			buttonPanel.setLayout(new MigLayout("wrap 1, ins 0, fill", "[grow, fill]"));
			if (buttonPanel.getParent() instanceof JPanel bottomPanel) {
				bottomPanel.setLayout(new MigLayout("wrap 2, fill", "[grow, fill][]", "[][]"));
				bottomPanel.setPreferredSize(new Dimension(700, 55));
				for (int i = 0; i < bottomPanel.getComponentCount(); i++) {
					if (bottomPanel.getComponent(i) instanceof Box.Filler) {
						bottomPanel.remove(bottomPanel.getComponent(i));
						i--;
					}
				}
				bottomPanel.add(buttonPanel, "spany", 1);
			}
		}
	}


	static Window getWindowForComponent(Component parentComponent) throws HeadlessException {
		if (parentComponent == null)
			return JOptionPane.getRootFrame();
		if (parentComponent instanceof Frame || parentComponent instanceof Dialog)
			return (Window)parentComponent;
		return getWindowForComponent(parentComponent.getParent());
	}

	@Override
	public void approveSelection() {
		File selectedFile = this.getSelectedFile();
		String ext = getExtension(selectedFile);
		if (!selectedFile.getName().endsWith(ext)) {
			selectedFile = new File(selectedFile.getPath() + ext);
			this.setSelectedFile(selectedFile);
		}
		System.out.println("filechooser this: " + this);
		System.out.println("dialog type: " + this.getDialogType());
		if (selectedFile.exists() && this.getDialogType() == JFileChooser.SAVE_DIALOG) {
			int confirmOverwriteFile = JOptionPane.showConfirmDialog(
					getParent(),
					"File \"" + selectedFile.getName() + "\" already exists. Overwrite anyway?",
					"Export File",
					JOptionPane.OK_CANCEL_OPTION);
			if (confirmOverwriteFile == JOptionPane.OK_OPTION) {
				//selectedFile.delete();
			} else {
				return;
			}
		}
		super.approveSelection();
	}

	private String getExtension(File modelFile) {
		final String name = modelFile.getName();
		if (name.lastIndexOf('.') != -1) {
			return name.substring(name.lastIndexOf('.'));
		} else if (getFileFilter() instanceof FileNameExtensionFilter filter) {
			String[] extensions = filter.getExtensions();
			if (0 < extensions.length) {
				return "." + extensions[0];
			}
		}
		return "";
	}

	private void setFilter(List<FileNameExtensionFilter> filters) {
		resetChoosableFileFilters();
		for (FileNameExtensionFilter filter : filters) {
			addChoosableFileFilter(filter);
		}
	}

	private static void setUpLookAndFeel() {
//		try {
//			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel");
//		} catch (final Exception exc) {
//			try {
//				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//			} catch (final ClassNotFoundException | UnsupportedLookAndFeelException | IllegalAccessException | InstantiationException e) {
//				e.printStackTrace();
//			}
//		}
		try {
			LookAndFeel lookAndFeel = UIManager.createLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (final ClassNotFoundException | UnsupportedLookAndFeelException | IllegalAccessException | InstantiationException e) {
			e.printStackTrace();
		}
	}
}
