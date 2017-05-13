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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hand.dot.config.PropertiesConfig;
import com.hand.dot.data.templates.PdfTemplate;
import com.hand.dot.util.FileUtil;
import com.hand.dot.util.PdfUtil;
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
	private PropertiesConfig config;

	final String pdfExtension = "pdf";
	final String pdfTemplatesDir = "/pdf-templates/";

	/**
	 * 引数の実型名の小文字のテンプレートファイルをresources/templatesから探し
	 * テンプレートのPDFファイルの入力欄を埋め、PDFを返します。
	 *
	 * @param template
	 *            PDFフォームのテンプレートデータ
	 * @param dir
	 *            ファイルを作成するディレクトリ
	 * @return 入力欄の埋まったPDFファイル
	 */
	public File fillInFieldByTemplate(PdfTemplate template, File dir) {
		// 一意となる出力ファイルパス生成
		File file = FileUtil.createRandomFilePath(dir, pdfExtension);
		try (FileOutputStream fos = new FileOutputStream(file)) {
			Class<? extends PdfTemplate> templateClass = template.getClass();
			List<String> fields = new ArrayList<>();
			for (Field field : templateClass.getDeclaredFields()) {
				field.setAccessible(true);
				fields.add(field.getName());
			}
			String pdfTemplateFileName = StringUtils.lowerCase(template.getClass().getSimpleName());
			try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
					InputStream is = PdfService.class
							.getResourceAsStream(pdfTemplatesDir + pdfTemplateFileName +"."+ pdfExtension)) {
				Stamper stamper = new Stamper(is, baos);
				for (String fieldName : fields) {
					PropertyDescriptor prop = new PropertyDescriptor(fieldName, templateClass);
					Method getter = prop.getReadMethod();
					stamper.set(fieldName, (String) getter.invoke(template, (Object[]) null));
				}
				stamper.close();
				is.close();
				baos.writeTo(fos);
				baos.close();
				fos.close();
			} catch (IOException | DocumentException e) {
				log.error(String.format("PdfService #fillInField のPDF処理でエラーが発生しました", e));
				e.printStackTrace();
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
					| IntrospectionException e) {
				log.error(String.format("PdfService #fillInField のリフレクションでエラーが発生しました"), e);
				e.printStackTrace();
			}
		} catch (IOException e) {
			log.error(String.format("PdfService #fillInField(PdfTemplate) のファイルの生成でエラーが発生しました"), e);
			e.printStackTrace();
		}
		return file;
	}

	public File fillInFieldByTemplateList(List<? extends PdfTemplate> templateList,File dir) {
		File tmpDir = FileUtil.createRandomFilePath(dir);
		tmpDir.mkdir();
		File file = FileUtil.createRandomFilePath(dir, pdfExtension);
		List<File> templateFileList = new ArrayList<>();
		for (PdfTemplate template : templateList) {
			templateFileList.add(this.fillInFieldByTemplate(template, tmpDir));
		}
		PdfUtil.join(file, templateFileList);
		try {
			FileUtils.deleteDirectory(tmpDir);
		} catch (IOException e) {
			log.error(String.format("PdfService #fillInField(List<PdfTemplate>) の一時ディレクトリ削除中にエラーが発生しました。"), e);
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
