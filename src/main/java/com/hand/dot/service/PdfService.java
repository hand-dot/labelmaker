package com.hand.dot.service;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hand.dot.config.Config;
import com.hand.dot.data.templates.PdfTemplate;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PdfService {

    @Autowired
    private Config config;

	final String pdfExtension = ".pdf";
	final String pdfTemplatesDir = "/pdf-templates/";

	/**
	 * 引数の型のテンプレートファイルをresources/templatesから探し
	 * テンプレートのPDFファイルの入力欄を埋め、PDFを返します。
	 * @return
	 */
	public File fillInField(PdfTemplate template){
		File workDir = new File(config.getOutputWorkDir());
		//ワークディレクトリがない場合は作成する
        if (!workDir.exists()) {
        	workDir.mkdir();
        }
        // 一意となる出力ファイルパス生成
		File file = new File(config.getOutputWorkDir() + "/" + UUID.randomUUID().toString() + pdfExtension);
		try (FileOutputStream fos = new FileOutputStream(file)){
			Class<? extends PdfTemplate> templateClass = template.getClass();
			List<String> fields = new ArrayList<>();
			for (Field field : templateClass.getDeclaredFields()) {
				field.setAccessible(true);
				fields.add(field.getName());
			}
			String pdfTemplateFileName = StringUtils.lowerCase(template.getClass().getSimpleName());

			try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
			InputStream is = PdfService.class.getResourceAsStream(pdfTemplatesDir + pdfTemplateFileName + pdfExtension)){
				Stamper stamper = new Stamper(is,baos);
				for(String fieldName: fields){
					PropertyDescriptor prop = new PropertyDescriptor(fieldName, templateClass);
					Method getter = prop.getReadMethod();
					stamper.set(fieldName, (String) getter.invoke(template, (Object[]) null));
				}
				stamper.close();
				baos.writeTo(fos);
			} catch (IOException | DocumentException e) {
		        log.debug(String.format("PDF処理で発生したエラー"));
				e.printStackTrace();
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | IntrospectionException e) {
		        log.debug(String.format("リフレクションで発生したエラー"));
				e.printStackTrace();
			}
		} catch (IOException e) {
	        log.debug(String.format("ファイルの生成で発生したエラー"));
			e.printStackTrace();
		}
		return file;
	}

	class Stamper {
        private final PdfReader reader;
	    private final PdfStamper stamper;
	    private final AcroFields fields;

	    public Stamper(InputStream inputPath, OutputStream outputPath) throws IOException, DocumentException {
	    	reader = new PdfReader(inputPath);
	        stamper = new PdfStamper(reader, outputPath);
	        fields = stamper.getAcroFields();
	    }

	    public void set(String key, String value) throws IOException, DocumentException {
	        fields.setField(key, value);
	    }

	    public synchronized void close() throws IOException, DocumentException {
	    	BaseFont font = BaseFont.createFont("KozMinPro-Regular", "UniJIS-UCS2-H", BaseFont.NOT_EMBEDDED);
	    	fields.addSubstitutionFont(font);
	    	stamper.setFormFlattening(true);
	        stamper.setFreeTextFlattening(true);
	        stamper.close();
	        reader.close();
	    }
	}

}
