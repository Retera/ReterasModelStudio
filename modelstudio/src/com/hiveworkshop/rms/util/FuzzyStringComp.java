package com.hiveworkshop.rms.util;

import java.util.ArrayList;
import java.util.Collections;

// from http://www.catalysoft.com/articles/StrikeAMatch.html
// by Simon White
public class FuzzyStringComp {

	/** @return an array of adjacent letter pairs contained in the input string */
	private static String[] letterPairs(String str) {

		int numPairs = str.length()-1;
		String[] pairs = new String[numPairs];

		for (int i=0; i<numPairs; i++) {
			pairs[i] = str.substring(i,i+2);

		}

		return pairs;

	}
	/** @return an ArrayList of 2-character Strings. */
	private static ArrayList<String> wordLetterPairs2(String str) {

		ArrayList<String> allPairs = new ArrayList<>();
		// Tokenize the string and put the tokens/words into an array
		String[] words = str.split("\\s");

		// For each word
		for (String word : words) {
			for (int i=0; i<word.length()-1; i++) {
				// Find the pairs of characters
				allPairs.add(word.substring(i,i+2));
			}
		}

		return allPairs;

	}
	/** @return an ArrayList of 2-character Strings. */

	private static ArrayList<String> wordLetterPairs(String str) {
		ArrayList<String> allPairs = new ArrayList<>();
		// Tokenize the string and put the tokens/words into an array
		String[] words = str.split("\\s");

		// For each word
		for (String word : words) {
			String[] pairsInWord = letterPairs(word);
			// Find the pairs of characters
			Collections.addAll(allPairs, pairsInWord);
		}

		return allPairs;

	}

	/** @return lexical similarity value in the range [0,1] */
	public static double compareStrings(String str1, String str2) {
		ArrayList<String> pairs1 = wordLetterPairs2(str1.toUpperCase());
		ArrayList<String> pairs2 = wordLetterPairs2(str2.toUpperCase());

		int intersection = 0;

		for (String pair1 : pairs1) {
			for (int j = 0; j < pairs2.size(); j++) {
				String pair2 = pairs2.get(j);
				if (pair1.equals(pair2)) {
					intersection++;
					pairs2.remove(j);
					break;
				}
			}
		}

		int union = pairs1.size() + pairs2.size();
		return (2.0*intersection)/union;
	}

	public static Integer compareI(String str1, String str2) {
		ArrayList<String> pairs1 = wordLetterPairs2(str1.toUpperCase());
		ArrayList<String> pairs2 = wordLetterPairs2(str2.toUpperCase());

		int intersection = 0;

		for (String pair1 : pairs1) {
			for (int j = 0; j < pairs2.size(); j++) {
				String pair2 = pairs2.get(j);
				if (pair1.equals(pair2)) {
					intersection++;
					pairs2.remove(j);
					break;
				}
			}
		}

		double union = pairs1.size() + pairs2.size();
		return (int)((1.0 - (2.0*intersection)/union) * 100.0);
	}

}
