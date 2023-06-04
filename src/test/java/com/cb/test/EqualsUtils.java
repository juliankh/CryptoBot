package com.cb.test;

import com.cb.common.util.NumberUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.cb.common.util.NumberUtils.DOUBLE_COMPARE_DELTA;
import static org.junit.Assert.*;

public class EqualsUtils {

	public static void assertMatrixNotEquals(double[][] expected, double[][] actual) {
		assertFalse(EqualsUtils.equals(expected, actual));	
	}
	
	public static void assertMatrixEquals(double[][] expected, double[][] actual) {
		assertEquals(expected.length, actual.length);
		for (int i = 0; i < expected.length; ++i) {
			assertArrayEquals(expected[i], actual[i], DOUBLE_COMPARE_DELTA);
		}	
	}
	
	public static boolean equals(double[][] matrix1, double[][] matrix2) {
		if (!NumberUtils.equals(matrix1.length, matrix2.length)) {
			return false;
		}
		for (int i = 0; i < matrix1.length; ++i) {
			double[] inner1 = matrix1[i];
			double[] inner2 = matrix2[i];
			if (!NumberUtils.equals(inner1.length, inner2.length)) {
				return false;
			}
			for (int j = 0; j < inner1.length; ++j) {
				if (!NumberUtils.equals(matrix1[i][j], matrix2[i][j])) {
					return false;
				}
			}
		}	
		return true;
	}
	
	public static boolean doubleCollectionEquals(Collection<Double> c1, Collection<Double> c2) {
		return doubleListEquals(new ArrayList<>(c1), new ArrayList<>(c2));
	}
	
	public static boolean doubleListEquals(List<Double> l1, List<Double> l2) {
		if (l1.size() != l2.size()) {
			return false;
		}
		for (int i = 0; i < l1.size(); ++i) {
			double d1 = l1.get(i);
			double d2 = l2.get(i);
			if (!NumberUtils.equals(d1, d2)) {
				return false;
			}
		}
		return true;
	}
	
	public static void assertMapOfDoublesEquals(Map<Double, Double> expected, Map<Double, Double> actual) {
		if (expected == null && actual == null) {
			return;
		} else if (expected == null || actual == null) {
			throw new AssertionError("Either expected [" + (expected == null ? "null" : "non-null") + "] or actual [" + (actual == null ? "null" : "non-null") + "] is null while the other is not");
		}
		assertCollectionOfDoublesEquals(expected.keySet(), actual.keySet());
		assertCollectionOfDoublesEquals(expected.values(), actual.values());
	}
	
	public static void assertCollectionOfDoublesEquals(Collection<Double> expected, Collection<Double> actual) {
		assertEquals(expected.size(), actual.size());
		assertCollectionOfDoublesEquals(new ArrayList<>(expected), new ArrayList<>(actual));
	}
	
	public static void assertCollectionOfDoublesEquals(List<Double> expected, List<Double> actual) {
		assertEquals(expected.size(), actual.size());
		for (int i = 0; i < expected.size(); ++i) {
			assertEquals(expected.get(i), actual.get(i), DOUBLE_COMPARE_DELTA);
		}
	}

}
