package com.hiveworkshop.rms.util.fileviewers;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class TxtViewer extends FileViewer {

	protected ArrayList<String> getStrings(InputStream in) {
		ArrayList<String> htmlTableStrings = new ArrayList<>();


		try (BufferedReader r = new BufferedReader(new InputStreamReader(in))) {
			r.lines().forEach(l -> {
				htmlTableStrings.add(l + "<br>");
			});
		} catch (Exception e) {
			e.printStackTrace();
		}

		return htmlTableStrings;
	}
}
