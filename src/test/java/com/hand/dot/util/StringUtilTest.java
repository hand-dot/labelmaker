package com.hand.dot.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class StringUtilTest {

	@Test
	public void appendWithSeparators() {
		assertEquals("1 2 3 4 5 6 7",StringUtil.appendWithSeparators("1234567"));
	}

}
