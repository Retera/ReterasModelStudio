package com.hiveworkshop.wc3.jworldedit;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import com.hiveworkshop.wc3.units.DataTable;
import com.hiveworkshop.wc3.units.Element;

public class GoodSLKEdit extends JPanel {
	private DataTable dataTable;
	private final JFileChooser jfc = new JFileChooser();
	private final JTable table;
	private final DefaultTableModel defaultTableModel;

	public GoodSLKEdit() {
		jfc.setFileFilter(new FileNameExtensionFilter("SLK Files of Warcraft", "slk"));
		setLayout(new BorderLayout());
		defaultTableModel = new DefaultTableModel();
		table = new JTable(defaultTableModel);
		add(new JScrollPane(table), BorderLayout.CENTER);
	}

	public static void main(final String[] args) {
		final JFrame frame = new JFrame("Good SLK Edit");
		final GoodSLKEdit goodSLKEdit = new GoodSLKEdit();
		frame.setJMenuBar(goodSLKEdit.createJMenuBar());
		frame.setContentPane(goodSLKEdit);
		frame.pack();
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	public JMenuBar createJMenuBar() {
		final JMenuBar menuBar = new JMenuBar();

		final JMenu fileMenu = new JMenu("File");
		final JMenuItem openMenuItem = new JMenuItem("Open");

		openMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				final int openDialog = jfc.showOpenDialog(GoodSLKEdit.this);
				if (openDialog == JFileChooser.APPROVE_OPTION) {
					final File file = jfc.getSelectedFile();
					if (file != null) {
						dataTable = new DataTable();
						dataTable.readSLK(file);
						final Set<String> keySet = dataTable.keySet();
						final String firstItem = keySet.iterator().next();
						final Element firstElement = dataTable.get(firstItem);
						final Set<String> titlekeys = firstElement.keySet();
						final List<String> titles = new ArrayList<>();
						titles.add("ID");
						titles.addAll(titlekeys);
						final String[] headers = titles.toArray(new String[titles.size()]);
						final Object[][] values = new Object[keySet.size()][headers.length];
						int index = 0;
						for (final String element : keySet) {
							values[index][0] = element;
							for (int i = 1; i < headers.length; i++) {
								values[index][i] = dataTable.get(element).getField(headers[i]);
							}
							index++;
						}
						defaultTableModel.setDataVector(values, headers);
					}
				}
			}
		});

		fileMenu.add(openMenuItem);
		menuBar.add(fileMenu);

		return menuBar;
	}
}
