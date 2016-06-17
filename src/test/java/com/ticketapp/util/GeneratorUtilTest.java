package com.ticketapp.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.ticketapp.util.GeneratorUtil;

public class GeneratorUtilTest {

	@Test(expected = IllegalArgumentException.class)
	public void testShouldThrowIAEException() {
		GeneratorUtil.getString(-1);
	}

	@Test
	public void testShouldReturnSingleCharStringFirst() {
		assertEquals("B", GeneratorUtil.getString(1));
	}

	@Test
	public void testShouldReturnSingleCharString() {
		assertEquals("P", GeneratorUtil.getString(15));
	}

	@Test
	public void testShouldReturnSingleCharStringBVA() {
		assertEquals("Z", GeneratorUtil.getString(25));
	}

	@Test
	public void testShouldReturnDoubleCharStringFirst() {
		assertEquals("BA", GeneratorUtil.getString(26));
	}

	@Test
	public void testShouldReturnDoubleCharString() {
		assertEquals("DG", GeneratorUtil.getString(84));
	}

	@Test
	public void testShouldReturnDoubleCharStringBVA() {
		assertEquals("ZZ", GeneratorUtil.getString(675));
	}

	@Test
	public void testShouldReturnTrippleCharStringFirst() {
		assertEquals("BAA", GeneratorUtil.getString(676));
	}

	@Test
	public void testShouldReturnTrippleCharString() {
		assertEquals("GKF", GeneratorUtil.getString(4321));
	}
}
