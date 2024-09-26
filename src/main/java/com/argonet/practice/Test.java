package com.argonet.practice;

import lombok.extern.slf4j.Slf4j;

import java.io.*;

@Slf4j
public class Test {
    public static void main(String[] args) {
        Test test = new Test();

    }

    /**
     * byte 단위로 substring
     * ex - String origin = "a한글b"
     *      String result = subStringByBytes(str, 3, 4);
     * @param str 원본 문자열
     * @param offset byte 크기
     * @param bytes 시작 위치
     * @return 부분 문자열
     */
    private String subStringByBytes(String str, int offset, int bytes) {
        int begin = -1;
        int end = -1;
        for(int i=0, len=0; i<str.length(); i++){
            String ch = str.substring(i, i+1);
            len += ch.getBytes().length;
            if(begin == -1 && len == offset)       begin = i;
            else if(begin == -1 && len > offset)   begin = i+1;

            if(end == -1 && len == offset + bytes)       end = i+1;
            else if(end == -1 && len > offset + bytes)   end = i;
        }
        return str.substring(begin, end);
    }


    /**
     * 로그 파일 생성
     * ex - String file = "test.log";
     *      String str = "~~";
     *      wrtieTxt(file, str);
     * @param file 로그파일 명
     * @param str 기록할 문자열
     */
    private void writeTxt(String file, String str){
        if(!file.startsWith("C:/") || !file.startsWith("c:/")) file = "C:/logs/"+file;
        try {
            File txtFile = new File(file);
            if (!txtFile.exists()) {
                boolean create = txtFile.createNewFile();
                log.info("{}", create);
            }

            FileWriter fw = new FileWriter(txtFile, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(str + "\r\n");
            bw.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}