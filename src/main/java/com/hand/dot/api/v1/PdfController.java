package com.hand.dot.api.v1;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.hand.dot.data.templates.LetterPack;

@RestController
public class PdfController {

	private final int maxPageNum = 50;

	@RequestMapping(value = "/api/v1/letterpack", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public List<LetterPack> post(@RequestBody List<LetterPack> letterpackList) {
		if(letterpackList != null && 0 < letterpackList.size() && letterpackList.size() <= maxPageNum){
			for(LetterPack letterpack:letterpackList){
				System.out.println(letterpack.toString());
			}
		}
	    return letterpackList;
	}
}
