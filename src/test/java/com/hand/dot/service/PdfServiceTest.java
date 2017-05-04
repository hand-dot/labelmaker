package com.hand.dot.service;

import java.io.File;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.hand.dot.data.templates.LetterPack;

@SpringBootTest
@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration
public class PdfServiceTest {

    @Autowired
    private PdfService pdfService;

	@Test
	public void fillInField() {
		LetterPack letterPack = new LetterPack();
		letterPack.setToPost("1234567");
		letterPack.setToAddres("東京都東京1-2-3-4 東京マンション123号");
		letterPack.setToName("東京都 出得太");
		letterPack.setToTel("123-3456-7890");
		letterPack.setFromPost("7654321");
		letterPack.setFromAddres("大阪府大阪1-2-3-4 大阪マンション123号");
		letterPack.setFromName("大阪府 出得太");
		letterPack.setFromTel("098-7654-4321");
		letterPack.setItemName("テストアイテム");
		File file = pdfService.fillInField(letterPack);
		System.out.println(file.getAbsolutePath());
	}

}
