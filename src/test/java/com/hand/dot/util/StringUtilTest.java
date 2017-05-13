package com.hand.dot.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class StringUtilTest {

	@Test
	public void appendWithSeparators() {
		assertEquals("1 2 3 4 5 6 7",StringUtil.appendWithSeparators("1234567"));
		assertEquals("1 2 3 4 5 6 7",StringUtil.appendWithSeparators("1 2 3 4 5 6 7"));
	}

	@Test
	public void removeHyphen(){
		assertEquals("1234567",StringUtil.removeHyphen("123-4567"));
		assertEquals("1234567",StringUtil.removeHyphen("1234567"));
	}

	@Test
	public void appendWithSeparatorsAndremoveHyphen(){
		assertEquals("1 2 3 4 5 6 7",StringUtil.appendWithSeparators(StringUtil.removeHyphen("123-4567")));
		assertEquals("1 2 3 4 5 6 7",StringUtil.appendWithSeparators(StringUtil.removeHyphen("1234567")));
		assertEquals("1 2 3 4 5 6 7",StringUtil.appendWithSeparators(StringUtil.removeHyphen("1 2 3 - 4 5 6 7")));
	}
}
