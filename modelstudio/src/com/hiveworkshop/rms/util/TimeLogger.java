package com.hiveworkshop.rms.util;

import java.util.ArrayList;
import java.util.List;

public class TimeLogger {
	// For debugging/optimizations
	// To use: call start() to add the first timestamp, then use log(NAME_OF_TIMESTAMP) to add additional timestamps
	private final List<String> names = new ArrayList<>();
	private final List<Long> times = new ArrayList<>();
	private int longestName = 0;
	
	public TimeLogger start(){
		names.add("Start");
		times.add(System.currentTimeMillis());
		return this;
	}
	
	public TimeLogger log(String name){
		names.add(name);
		times.add(System.currentTimeMillis());
		longestName = Math.max(name.length(), longestName);
		return this;
	}
	
	public String getTimeString() {
		if(times.size()==names.size() && times.size()>1){
			long maxTime = times.get(times.size() - 1) - times.get(0);
			int timesSize = (int) Math.log(maxTime+1) + 2;
			StringBuilder sb = new StringBuilder();
			for (int i = 1; i< names.size(); i++){
				String name = StringPadder.padStringEnd(names.get(i) + ":", " ", longestName + 1);
				String time = StringPadder.padStringStart((times.get(i) - times.get(i-1)) + "", " ", timesSize);
				sb.append(name).append(time).append("\n");
			}
			return sb.toString().strip();
		}
		return "Nothing logged";
	}

	public String getTimeFromStartString() {
		if(times.size()==names.size() && times.size()>1){
			long maxTime = times.get(times.size() - 1) - times.get(0);
			int timesSize = (int) Math.log(maxTime) + 2;
			StringBuilder sb = new StringBuilder();
			for (int i = 1; i< names.size(); i++){
				String name = StringPadder.padStringEnd(names.get(i) + ":", " ", longestName + 1);
				String time = StringPadder.padStringStart((times.get(i) - times.get(0)) + "", " ", timesSize);
				sb.append(name).append(time).append("\n");
			}
			return sb.toString().strip();
		}
		return "Nothing logged";
	}

	public void print(){
		if(times.size()==names.size() && times.size()>1){
			String string = getTimeString();
			System.out.println(string);
		}
	}

	public void printTimeFromStart(){
		System.out.println(getTimeFromStartString());
	}

}
