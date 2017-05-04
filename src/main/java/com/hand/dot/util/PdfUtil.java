package com.hand.dot.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PdfUtil {
	public static void join(File outputFile, List<File> fileList) {
		Document document = null;
		PdfCopy copy = null;
		try {
			document = new Document(PageSize.A4);
			copy = new PdfCopy(document, new FileOutputStream(outputFile));
			document.open();
			for (File file : fileList) {
				PdfReader reader = new PdfReader(file.getAbsolutePath());
				copy.addDocument(reader);
				reader.close();
			}
		} catch (IOException | DocumentException e) {
	        log.error(String.format("PDFUtil #join でエラーが発生しました",e));
			e.printStackTrace();
		}finally {
			if(document != null && document.isOpen()){
				document.close();
			}
		}
	}
}
