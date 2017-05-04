package com.hand.dot.util;

import org.apache.commons.lang3.text.StrBuilder;

public class StringUtil {
	public static String appendWithSeparators(String str){
        StrBuilder sb = new StrBuilder();
        String[] arr = str.split("");
        sb.appendWithSeparators(arr, " ");
        return sb.toString();
   }
}
