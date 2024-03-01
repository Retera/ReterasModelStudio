package com.hiveworkshop.rms.util.fileviewers;

import javax.swing.text.html.HTMLEditorKit;
import java.io.*;

public class SklViewer extends FileViewer {

	private static final String TABLE_START = "\n\t<table text-align=start>";
	private static final String TABLE_END = "\n\t</table>";
	private static final String ROW_START = "\n\n\t\t<tr><span></span>";
	private static final String ROW_END = "\n\t\t</tr>";
	private static final String CELL_START = "\n\t\t\t<td>";
	private static final String CELL_END = "</td>";

	public SklViewer() {
		super(new HTMLEditorKit());
	}
	protected String getReadFile(File file) {
		try (FileInputStream in = new FileInputStream(file)) {
			return readStream(in);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	protected String readStream(InputStream in) {
		StringBuilder tableBuilder = new StringBuilder();
		tableBuilder.append(TABLE_START);
		int emptyLength = TABLE_START.length();

		try (BufferedReader r = new BufferedReader(new InputStreamReader(in))) {
			r.lines().forEach(l -> {
				if (l.startsWith("C;X1;Y")) {
					if (emptyLength < tableBuilder.length()) {
						tableBuilder.append(ROW_END);
					}
					String[] split = l.split(";K");
					tableBuilder
							.append(ROW_START)
							.append(CELL_START)
							.append(split[0].split("Y")[1]) // rowNumber
							.append(CELL_END)
							.append(CELL_START)
							.append(split[1])
							.append(CELL_END);
				} else if (l.matches("C;X\\d+;K.+")) {
					tableBuilder
							.append(CELL_START)
							.append(l.split(";K")[1])
							.append(CELL_END);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (emptyLength < tableBuilder.length()) {
			tableBuilder.append(ROW_END);
		}

		tableBuilder.append(TABLE_END);

		return tableBuilder.toString();
	}
}