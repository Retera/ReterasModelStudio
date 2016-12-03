package com.hiveworkshop.modding.jass;

public class Variable {
	public static enum Type {
		LOCAL, GLOBAL;
	}
	Type type;
	boolean isArray;
	String name;
}
