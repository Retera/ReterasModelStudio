package com.hiveworkshop.wc3.pkb.bind;

import com.hiveworkshop.wc3.pkb.vm.Instruction;

/** One compiled scope of a layer: bytecode, constants, externals and callable functions. */
public final class ProgramDescriptor {
	public byte[] bytecode = new byte[0];
	public Instruction[] instructions = new Instruction[0];
	public byte[] constantsPool = new byte[0];
	public ExternalBinding[] externals = new ExternalBinding[0];
	public FunctionBinding[] functions = new FunctionBinding[0];
	/** registerCounts[0] is unused by the harness; [1..4] size the four scope banks. */
	public final int[] registerCounts = new int[5];

	public boolean isEmpty() {
		return bytecode.length == 0;
	}
}
