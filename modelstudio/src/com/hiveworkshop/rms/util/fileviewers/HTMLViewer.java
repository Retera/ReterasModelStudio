package com.hiveworkshop.rms.util.fileviewers;

import javax.swing.text.html.HTMLEditorKit;
import java.io.*;

public class HTMLViewer extends FileViewer {

	public HTMLViewer() {
		super(new HTMLEditorKit());
	}

	protected String getReadFile(File file) {
		try (FileInputStream in = new FileInputStream(file)){
			return readStream(in);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "";
	}

	protected String readStream(InputStream in) {
		StringBuilder stringBuilder = new StringBuilder();

		try (BufferedReader r = new BufferedReader(new InputStreamReader(in))) {
			r.lines().forEach(l -> stringBuilder.append(l).append("\n"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return stringBuilder.toString();
	}
}
