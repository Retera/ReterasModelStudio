package com.matrixeater.hacks;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.etheller.collections.SetView;
import com.hiveworkshop.wc3.mdl.MDL;
import com.hiveworkshop.wc3.mdx.MdxUtils;
import com.hiveworkshop.wc3.mpq.MpqCodebase;

import de.wc3data.stream.BlizzardDataInputStream;

public class GetMeDatas4 {

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

		final InputStream footman = MpqCodebase.get().getResourceAsStream("Units\\Orc\\Hellscream\\Hellscream.mdx");
		try {
//			final MdxModel footmanMdx = MdxUtils.loadModel(new BlizzardDataInputStream(footman));
//			try (BlizzardDataOutputStream out = new BlizzardDataOutputStream(
//					new File("C:\\Users\\micro\\OneDrive\\Documents\\Warcraft III Beta\\Models\\SoftwareGruntX.mdx"))) {
//				footmanMdx.save(out);
//			}

			final MDL model = new MDL(MdxUtils.loadModel(new BlizzardDataInputStream(footman)));
			model.printTo(new File(
					"C:\\Users\\micro\\OneDrive\\Documents\\Warcraft III Beta\\Models\\SoftwareHellscream.mdx"));
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

}
