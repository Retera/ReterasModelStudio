package com.matrixeater.hacks;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import com.hiveworkshop.json.JSONArray;
import com.hiveworkshop.json.JSONObject;
import com.hiveworkshop.json.JSONTokener;
import com.hiveworkshop.wc3.mpq.MpqCodebase;

public class FtlopMaker2 {

	public static void main(final String[] args) {
//		JSONArray x = new JSONArray(source)
		try (InputStream stream = MpqCodebase.get().getResourceAsStream("filealiases.json")) {
//			stream.mark(4);
//			if ('\ufeff' != stream.read()) {
//				stream.reset(); // not the BOM marker
//			}
			final JSONArray jsonObject = new JSONArray(new JSONTokener(stream));
			for (int i = 0; i < jsonObject.length(); i++) {
				final JSONObject alias = jsonObject.getJSONObject(i);
				final String src = alias.getString("src");
				final String dest = alias.getString("dest");
//						fileAliases.put(src.toLowerCase(Locale.US).replace('/', '\\'),
//								dest.toLowerCase(Locale.US).replace('/', '\\'));
				final Path resolve = Paths.get("E:\\Games\\FtlopModLocalFiles\\").resolve(src);
				Files.createDirectories(resolve.getParent());
				System.out.println("copying " + dest + " -> " + src);
				try {
					Files.copy(Paths.get("E:\\Games\\FtlopModLocalFiles\\").resolve(dest), resolve,
							StandardCopyOption.REPLACE_EXISTING);
				} catch (final Exception exc) {
					exc.printStackTrace();
				}
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

}
