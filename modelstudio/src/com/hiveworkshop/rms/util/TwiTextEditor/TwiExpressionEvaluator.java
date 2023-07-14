package com.hiveworkshop.rms.util.TwiTextEditor;

public class TwiExpressionEvaluator {
	String addOp = "\\+";
	String mulOp = "\\*";
	String divOp = "/";
	String modOP = "%";
	String expOp = "\\^";

//	String[] orderedOps = {addOp, mulOp, divOp, modOP, expOp, nonOp};
	String[] orderedOps = {addOp, mulOp, divOp, modOP, expOp};

	private Double eval2(String pCont) {
		pCont = pCont.replaceAll("[()]+", "");
		String[] addSplit = pCont.split("((?=" + addOp +")|(?<=" + addOp +"))");

		StringBuilder addSb = new StringBuilder();
		for (String addIdem : addSplit) {
			if(addIdem.matches(addOp)){
				addSb.append(addIdem);
			} else {
				StringBuilder mulSb = new StringBuilder();
				String[] mulSplit = addIdem.split("((?=" + mulOp + ")|(?<=" + mulOp + "))");
				for (String mulItem : mulSplit) {
					if (mulItem.matches(mulOp)) {
						mulSb.append(mulItem);
					} else {
						StringBuilder divSb = new StringBuilder();
						String[] divSplit = mulItem.split("((?="+ divOp +")|(?<="+ divOp +"))");
						for (String divItem : divSplit) {
							if (divItem.matches(divOp)) {
								divSb.append(divItem);
							} else {
								String[] modSplit = divItem.split("((?=" + modOP +")|(?<=" + modOP +"))");
								StringBuilder modSb = new StringBuilder();
								for (String modItem : modSplit) {
									if (modItem.matches(modOP)) {
										modSb.append(modItem);
									} else {
//										modSb.append(evalExp(modItem).toString());
										modSb.append(evalForOp(modItem, expOp).toString());
									}
								}
//								divSb.append(evalMod(modSb.toString()));
								divSb.append(evalForOp(modSb.toString(), modOP));
							}
						}
//						mulSb.append(evalDiv(divSb.toString()));
						mulSb.append(evalForOp(divSb.toString(), divOp));
					}
				}
//				addSb.append(evalMul(mulSb.toString()));
				addSb.append(evalForOp(mulSb.toString(), mulOp));
			}
		}


//		return evalAdd(addSb.toString());
		return evalForOp(addSb.toString(), addOp);
	}

	public double evaluate(String expression){
		String paretisC = "\\([\\d.\\-+/*%^]+\\)";
		String innerP_LA = "(?=" + paretisC + ")";
		expression = expression.replaceAll(" ", "")
				.replaceAll("(?<=[\\d)])-(?=[\\d(.])", "+-1*")
				.replaceAll("(?<=[\\d)])\\(", "*(");
		System.out.println("expression: " + expression);

		String innerP_LB = "(?<=" + paretisC + ")";
		String e2 = expression;
		for(int i = 0; i < expression.length(); i++) {
			String[] split5 = e2.split("(" + innerP_LA + "|" + innerP_LB + ")");
			StringBuilder sb = new StringBuilder();
			for (String s : split5) {
				if (s.matches(paretisC)) {
					sb.append(evalGeneric(s.replaceAll("[()]+", ""), 0));
				} else {
					sb.append(s);
				}
			}
			System.out.println("  eval " + i + ": " + sb);
			e2 = sb.toString();
			if(e2.matches("-?\\d*(\\.\\d+)?([eE][-+]?\\d+)?")){
				break;
			} else if(e2.matches("[^()]+")){
				e2 = evalGeneric(e2, 0).toString();
			}
		}
		System.out.println("= " + e2);
		return Double.parseDouble(e2);
	}



	private Double evalGeneric(String item, int opI) {
		String[] split = item.split("((?=" + orderedOps[opI] + ")|(?<=" + orderedOps[opI] + "))");
		StringBuilder sb = new StringBuilder();
		for (String s : split) {
			if (s.matches(orderedOps[opI])) {
				sb.append(s);
			} else if (opI + 1 < orderedOps.length) {
				sb.append(evalGeneric(s, opI + 1));
			} else {
				sb.append(s);
			}
		}
		return evalForOp(sb.toString(), orderedOps[opI]);
	}


	private Double evalForOp(String s, String op) {
		String[] split = s.split(op);
		double val = Double.parseDouble(split[split.length-1]);
		for (int l = split.length-2; 0 <= l; l--){
			val = eval(op, Double.valueOf(split[l]), val);

		}
		return val;
	}

	private double eval(String operand, Double v1, Double v2) {
		return switch (operand) {
			case "*", "\\*" -> v1 * v2;
			case "/", "\\/" -> v1 / v2;
			case "+", "\\+" -> v1 + v2;
			case "-", "\\-" -> v1 - v2;
			case "%", "\\%" -> v1 % v2;
			case "^", "\\^" -> Math.pow(v1, v2);
			default -> {
				System.out.println("operand: \"" + operand + "\"");
				throw new IllegalStateException("Unexpected value: " + operand);
			}
		};
	}

	public static void main(String[] args) {
		String expression1 = "(40 + 4(35 - 4)*(20-1)/3 -(((22)*(2*(22))+3)+4/5)) + 2^2";
		String expression2 = "-2 * (17 - 1.5 / 2)";
		String expression3 = "(17 - 1.5 / 2) * -2";
		String expression4 = "-2(17 - 1.5 / 2)";
		String expression5 = "4%2";
		String expression6 = "(12 (10 / 2 (10 / 2)) (-2)) (10)";
		String expression7 = "(12 (10 / 2 (10 / 2)) (-2)) (10)^2";
		TwiExpressionEvaluator twiExpressionEvaluator = new TwiExpressionEvaluator();
		twiExpressionEvaluator.evaluate(expression1);
		twiExpressionEvaluator.evaluate(expression2);
		twiExpressionEvaluator.evaluate(expression3);
		twiExpressionEvaluator.evaluate(expression4);
		twiExpressionEvaluator.evaluate(expression5);
		twiExpressionEvaluator.evaluate(expression6);
		twiExpressionEvaluator.evaluate(expression7);
	}
}
