package com.hiveworkshop.wc3.units;

public enum DataTableReforgedMode {
	HD(":hd"), SD(":sd"), OFF("::unused");

	private String keySuffix;

	private DataTableReforgedMode(String keySuffix) {
		this.keySuffix = keySuffix;
	}

	public String getKeySuffix() {
		return keySuffix;
	}
}
