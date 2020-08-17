package com.matrixeater.hacks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.etheller.collections.SetView;
import com.etheller.warsmash.parsers.mdlx.MdlxModel;

import com.hiveworkshop.wc3.mdx.MdxUtils;

import com.hiveworkshop.wc3.mpq.MpqCodebase;

public class GetMeDatas2 {

	public static void main(final String[] args) {
		final SetView<String> mergedListfile = MpqCodebase.get().getMergedListfile();
//		for (final String file : mergedListfile) {
//			System.out.println(file);
//		}
//		final InputStream slk = MpqCodebase.get().getResourceAsStream("Units\\UnitData.slk");
//		try (BufferedReader reader = new BufferedReader(new InputStreamReader(slk))) {
//			String line;
//
//			while ((line = reader.readLine()) != null) {
//				System.out.println(line);
//			}
//		} catch (final IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		final BufferedImage gameTex = BLPHandler.get()
//				.getGameTex("ReplaceableTextures\\CommandButtons\\BTNReplay-Play.blp");
//		if (gameTex == null) {
//			throw new IllegalStateException("game tex is null");
//		}
//		JOptionPane.showMessageDialog(null, new JLabel(new ImageIcon(gameTex)));

		try {
			final InputStream footman = new FileInputStream(new File(
					"C:\\Users\\micro\\OneDrive\\Documents\\Warcraft III Beta\\Models\\Footman_SD_Reforged.mdx"));
			final MdlxModel footmanMdx = MdxUtils.loadModel(footman);
			try (OutputStream out = new FileOutputStream(new File(
					"C:\\Users\\micro\\OneDrive\\Documents\\Warcraft III Beta\\Models\\SoftwareStandardFootman.mdx"))) {
				footmanMdx.saveMdx(out);
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

}
