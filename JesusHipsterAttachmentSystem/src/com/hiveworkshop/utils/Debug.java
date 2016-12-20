package com.hiveworkshop.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.hiveworkshop.wc3.gui.ExceptionPopup;
import com.hiveworkshop.wc3.mdl.MDL;
import com.hiveworkshop.wc3.mdx.MdxModel;
import com.hiveworkshop.wc3.mdx.MdxUtils;

import de.wc3data.stream.BlizzardDataInputStream;

public class Debug {
	public static void main(final String[] args) {

		final File basesFolder = new File("bases");
		final File debugFolder = new File("debug");
		debugFolder.mkdir();
		for(final File baseFile: basesFolder.listFiles()) {
			try {
				final MdxModel mdxModel = MdxUtils.loadModel(new BlizzardDataInputStream(new FileInputStream(baseFile)));
				final MDL mdlModel = new MDL(mdxModel);
				mdlModel.printTo(new File(debugFolder.getPath()+"/"+baseFile.getName().replace(".mdx", ".mdl")));
			} catch (final FileNotFoundException e) {
				ExceptionPopup.display(e);
				e.printStackTrace();
			} catch (final IOException e) {
				ExceptionPopup.display(e);
				e.printStackTrace();
			}
		}
	}
}
