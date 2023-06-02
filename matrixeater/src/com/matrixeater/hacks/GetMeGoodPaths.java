package com.matrixeater.hacks;

import java.io.File;

import com.hiveworkshop.wc3.mdl.Attachment;
import com.hiveworkshop.wc3.mdl.EditableModel;

public class GetMeGoodPaths {
	private static File toFix = new File("F:\\NEEDS_ORGANIZING\\AAAlteracIsle\\Buildings");

	public static void main(final String[] args) {
		for (final File subFile : toFix.listFiles()) {
			if (subFile.getName().toLowerCase().endsWith(".mdx")) {

				try {
					final EditableModel model = EditableModel.read(subFile);
					for (final Attachment atc : model.sortedIdObjects(Attachment.class)) {
						if (atc.getPath() != null && atc.getPath().contains("NagaBirth")) {
							atc.setPath(
									"SharedModels\\" + atc.getPath().substring(atc.getPath().lastIndexOf("\\") + 1));
						}
					}
					model.saveFile(false);
				} catch (final Exception e) {
					System.err.println(subFile);
					e.printStackTrace();
				}
			}
		}
	}
}
