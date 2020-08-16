package com.matrixeater.hacks;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.etheller.collections.SetView;
import com.hiveworkshop.wc3.mdx.MdxModel;
import com.hiveworkshop.wc3.mdx.MdxUtils;
import com.hiveworkshop.wc3.mpq.MpqCodebase;

import de.wc3data.stream.BlizzardDataInputStream;
import de.wc3data.stream.BlizzardDataOutputStream;

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
			final InputStream footman = new BlizzardDataInputStream(new FileInputStream(new File(
					"C:\\Users\\micro\\OneDrive\\Documents\\Warcraft III Beta\\Models\\Footman_SD_Reforged.mdx")));
			final MdxModel footmanMdx = MdxUtils.loadModel(new BlizzardDataInputStream(footman));
			try (BlizzardDataOutputStream out = new BlizzardDataOutputStream(new File(
					"C:\\Users\\micro\\OneDrive\\Documents\\Warcraft III Beta\\Models\\SoftwareStandardFootman.mdx"))) {
				footmanMdx.save(out);
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

}
