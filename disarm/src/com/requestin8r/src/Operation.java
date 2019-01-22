package com.requestin8r.src;

import java.util.Map;

import com.requestin8r.src.OperationType.Field;

public class Operation {
	OperationType type;
	Map<Field<?>, Object> stats;
	Map<Field<?>, String> comments;
}
