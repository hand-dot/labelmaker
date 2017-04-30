package com.hand.dot.api;

import java.util.List;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.hand.dot.dto.LetterPackDto;

@RestController
public class PdfRestController {

	private final int maxPageNum = 50;

	@RequestMapping(path = "/api/v1/letterpack", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
	public List<LetterPackDto> post(@RequestBody List<LetterPackDto> letterpackList) {
		if(letterpackList != null && 0 < letterpackList.size() && letterpackList.size() <= maxPageNum){
			for(LetterPackDto letterpack:letterpackList){
				System.out.println(letterpack.toString());
			}
		}
	    return letterpackList;
	}
}
