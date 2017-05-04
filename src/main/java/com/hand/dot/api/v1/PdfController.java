package com.hand.dot.api.v1;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.hand.dot.config.Config;
import com.hand.dot.data.templates.LetterPack;
import com.hand.dot.service.PdfService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class PdfController {

	@Autowired
	private PdfService pdfService;
	@Autowired
	private Config config;

	private final int maxPageNum = 50;

	@RequestMapping(value = "/api/v1/letterpack", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> post(@RequestBody List<LetterPack> letterpackList) throws IOException {
		ResponseEntity<?> res;

		try {
			if (letterpackList != null && 0 < letterpackList.size() && letterpackList.size() <= maxPageNum) {
				File file = pdfService.fillInFieldByTemplateList(letterpackList, new File(config.getOutputWorkDir()));
				Resource resource = new UrlResource(file.toPath().toUri());
				res = ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
						.header(HttpHeaders.CONTENT_DISPOSITION,
								"attachment; filename=\"" + resource.getFilename() + "\"")
						.body(resource);
			} else {
				res = ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON)
						.body("一度に作成できるPDFは最大" + config.getMaxPageNum() + "ページです");
			}
		} catch (Throwable e) {
			log.error(String.format("APIの呼び出しからのPDF作成に失敗しました。", e));
			throw e;
		}
		if(res == null){
			res = ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON)
					.body("サーバエラーが発生しました。申し訳ございませんが、しばらくたってからもう一度お試しください。");
		}
		return res;
	}
}
