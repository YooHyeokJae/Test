package com.argonet.practice.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.util.*;

@Slf4j
@Controller
public class MainController {

    @GetMapping("/home")
    public String home() {
        return "home";
    }

    @PostMapping("/fileProc1")
    @ResponseBody
    public List<Map<String, Object>> fileProc1(@RequestBody MultipartFile file) throws Exception {
//        log.info("file: {}", file);
        return excelWriter(file);
    }

    private static List<Map<String, Object>> excelWriter(MultipartFile file) throws Exception {
        List<Map<String, Object>> result = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream("C:/Users/yhj/Documents/" + file.getOriginalFilename())) {
            Workbook workbook = new HSSFWorkbook(fis);
            Sheet sheet = workbook.getSheetAt(0);

            for(int rowIdx=10; rowIdx<=sheet.getLastRowNum(); rowIdx++) {
                Row row = sheet.getRow(rowIdx);
                if(row != null){
                    Map<String, Object> data = new HashMap<>();
                    for(int colIdx=0; colIdx<=row.getLastCellNum(); colIdx++) {
                        Cell cell = row.getCell(colIdx);
//                        log.info("{}열 {}행: {}", rowIdx, colIdx, cell);

                        switch (colIdx) {
                            case 0:  data.put("subject", cell.getStringCellValue()); break;
                            case 1:  data.put("docName", cell.getStringCellValue()); break;
                            case 2:  data.put("monAm", cell.getStringCellValue()); break;
                            case 3:  data.put("monPm", cell.getStringCellValue()); break;
                            case 4:  data.put("tueAm", cell.getStringCellValue()); break;
                            case 5:  data.put("tuePm", cell.getStringCellValue()); break;
                            case 6:  data.put("wedAm", cell.getStringCellValue()); break;
                            case 7:  data.put("wedPm", cell.getStringCellValue()); break;
                            case 8:  data.put("thuAm", cell.getStringCellValue()); break;
                            case 9:  data.put("thePm", cell.getStringCellValue()); break;
                            case 10: data.put("friAm", cell.getStringCellValue()); break;
                            case 11: data.put("friPm", cell.getStringCellValue()); break;
                            case 12: data.put("satAm", cell.getStringCellValue()); break;
                            case 13: data.put("satPm", cell.getStringCellValue()); break;
                            case 14: data.put("field", cell.getStringCellValue()); break;
                            default:
                        }
                    }
                    result.add(data);
                }
            }
        }

        return result;
    }


}
