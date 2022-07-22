package com.hiveworkshop.rms.util.fileviewers;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class SklViewer extends FileViewer {

	protected ArrayList<String> getStrings(InputStream in) {
		ArrayList<String> htmlTableStrings = new ArrayList<>();
//		appendDocString(document);

		htmlTableStrings.add("\n\t<table>");


		try (BufferedReader r = new BufferedReader(new InputStreamReader(in))) {
			ArrayList<String> htmlTableCells = new ArrayList<>();
			r.lines().forEach(l -> {
				if (l.startsWith("C;X1;Y")) {
					if (0 < htmlTableCells.size()){
						htmlTableStrings.add(getHtmlTableRow(combineStrings(htmlTableCells)));
						htmlTableCells.clear();
					}
					String[] split = l.split(";K");
					htmlTableCells.add(getHtmlTableCell(split[0].split("Y")[1]));
					htmlTableCells.add(getHtmlTableCell(split[1]));
				} else if (l.matches("C;X\\d+;K.+")){
					htmlTableCells.add(getHtmlTableCell(l.split(";K")[1]));
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}

		htmlTableStrings.add("\n\t</table>");

		return htmlTableStrings;
	}

	private String getHtmlTableCell(String string){
		return "\n\t\t\t<td>" + string + "</td>";
	}

	private String getHtmlTableRow(String string){
		return "\n\t\t<tr>" + string + "\n\t\t</tr>";
	}
}
