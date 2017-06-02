package com.hiveworkshop.assetripper;

import java.nio.file.Paths;

public final class Main {
	public static void main(final String[] args) {
		new MapAssetRipper(Paths.get(args[0])).ripObject(args[1], Paths.get(args[2]), null);
	}
}
