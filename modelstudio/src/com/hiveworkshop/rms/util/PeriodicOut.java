package com.hiveworkshop.rms.util;

public class PeriodicOut {
	private long lastOut = 0;
	private int period;

	public PeriodicOut(int period){
		this.period = period;
	}

//	public void print(String... out){
//		if(lastOut < System.currentTimeMillis()){
//			lastOut = System.currentTimeMillis() + period;
//			StringBuilder sb = new StringBuilder();
//			for(String s : out){
//				sb.append(s);
//			}
//			System.out.println(sb);
//		}
//	}
	public void print(String out){
		if(lastOut < System.currentTimeMillis()){
			lastOut = System.currentTimeMillis() + period;
			System.out.println(out);
		}
	}
	public void print(String split, String... out){
		if(lastOut < System.currentTimeMillis()){
			lastOut = System.currentTimeMillis() + period;
			StringBuilder sb = new StringBuilder();
			for(String s : out){
				sb.append(s).append(split);
			}
			System.out.println(sb);
		}
	}
}
