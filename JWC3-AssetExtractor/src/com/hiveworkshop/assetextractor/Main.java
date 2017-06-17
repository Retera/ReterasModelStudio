package com.hiveworkshop.assetextractor;

import java.nio.file.Paths;

public final class Main {
	public static void main(final String[] args) {
		new MapAssetExtractor(Paths.get(args[0])).extractObject(args[1], Paths.get(args[2]), null);
	}
}
