package com.hand.dot.data.templates;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * PDFテンプレートの抽象クラス
 * @author hand-dot
 *
 */
abstract public class PdfTemplate {
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
