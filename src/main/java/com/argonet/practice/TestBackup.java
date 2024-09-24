package com.argonet.practice;

import com.argonet.practice.vo.AffiliationVO;
import com.argonet.practice.vo.AuthorVO;
import com.argonet.practice.vo.MeshHeadingVO;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

@Slf4j
public class TestBackup {

    private String decompressGzFile(String dest, String file) {
        if(!file.contains(".gz"))   return null;
        String xmlFile = dest + file.substring(file.lastIndexOf("/")+1, file.lastIndexOf(".gz"));

        try {
            FileInputStream fis = new FileInputStream(file);
            GZIPInputStream gis = new GZIPInputStream(fis);
            FileOutputStream fos = new FileOutputStream(xmlFile);
            byte[] buffer = new byte[1024];
            int len;
            log.info("중복방지");

            while ((len = gis.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }

            fos.close();
            gis.close();
        } catch (Exception e) {
            log.error("error: {}", e.getMessage());
        }

        return xmlFile;
    }

    private void regExpTest() {
        String pattern = "^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*.[a-zA-Z]{2,3}$";
        String email = " larsaa@medisin.uio.no";
        if(Pattern.matches(pattern, email)) {
            log.info("O");
        }else{
            log.info("X");
        }
    }

    private void getData() {
        File dir = new File("C:/pubmed/gzFiles/unzip");
        String[] fileArr = dir.list();
        if(fileArr != null){
            for(String fileName : fileArr) {
                xmlParser("C:/pubmed/gzFiles/unzip/" + fileName);
                break;
            }
        }
    }

    private void readTxtFile() throws IOException {
        File dir = new File("C:/txtFiles");
        File[] files = dir.listFiles();
        if(files==null) return;

        for (File file : files) {
            log.info(file.getName());

            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while((line=br.readLine()) != null){
                String pmcId = line.split("PMC_ID = '")[1].split("', ")[0];
                String pmid = line.split("PMID = ")[1].split(";")[0];

                // update query
                log.info("{}\t{}", pmcId, pmid);


            }

            br.close();
            break;
        }
    }

    private void getFileSize(){
        File dir = new File("C:/txtFiles");
        File[] files = dir.listFiles();
        if(files==null) return;

        int start = 689;
        files = Arrays.copyOfRange(files, start-497, files.length, File[].class);

        Arrays.sort(files, (o1, o2) -> Long.compare(o1.length(), o2.length()) * -1);

        long totalSize = 0;
        totalSize = getSize(files, totalSize);

        long average = totalSize / files.length;
        log.info("{}\t{}", average / 1024, files.length);

    }

    private long getSize(File[] files, long totalSize) {
        for (File file : files) {
            log.info("{}\t{}B\t{}KB", file.getName(), file.length(), file.length() / 1024);
            totalSize += file.length();
        }
        return totalSize;
    }

    private void test() throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse("C:/pubmed/downFiles/unzip/pubmed24n0882.xml/pubmed24n0882.xml");
        NodeList pubmedArticles = doc.getElementsByTagName("PubmedArticle");
        for (int i = 0; i < pubmedArticles.getLength(); i++) {
            Node node = pubmedArticles.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element E_pubmedArticle = (Element) node;
                Element E_pubmedData = (Element) E_pubmedArticle.getElementsByTagName("PubmedData").item(0);
                Element E_articleIdList = (Element) E_pubmedData.getElementsByTagName("ArticleIdList").item(0);

                String[] IDs = getIDs(E_articleIdList);
                String pmid = IDs[0];
                String pmcId = IDs[1];
                String pii = IDs[2];
                String doi = IDs[3];

                if(pmid.equals("27670948")){
                    log.info("i: {}\tPMID: {}", i, pmid);
                }
            }
        }
    }

    private String[] getIDs(Element articleIdList) {
        NodeList articleIds = articleIdList.getElementsByTagName("ArticleId");
        String[] IDs = new String[4];
        for(int j=0; j<articleIds.getLength(); j++){
            NamedNodeMap attributes = articleIds.item(j).getAttributes();
            String idType = attributes.getNamedItem("IdType").getNodeValue();
            log.info("중복방지");
            if(idType.equals("pubmed")) IDs[0] = articleIds.item(j).getTextContent();
            if(idType.equals("pmc"))    IDs[1] = articleIds.item(j).getTextContent();
            if(idType.equals("pii"))    IDs[2] = articleIds.item(j).getTextContent();
            if(idType.equals("doi"))    IDs[3] = articleIds.item(j).getTextContent();

        }
        return IDs;
    }

    private String[][] setData(){
        return new String[][]{
                {"37185068", "37185069", "37185070", "37185071", "37185073", "37185072", "37185074", "37185075", "37185076", "37185077", "37185078", "37185079"},
                {"37668640", "37668641", "37668642", "37668643", "37668644", "37668645", "37668646", "37668647", "37668648", "37668649", "37668650", "37668651"}
        };
    }

    // pii 추출 -> C;/pubmed/xmlFiles/ 하위에 xml파일
    private void readXmlFile() {
        String[][] pmidArrArr = setData();
        File dir = new File("C:/pubmed/xmlFiles");
        File[] fileArr = dir.listFiles();
        if(fileArr==null) return;

        for(int idx=0; idx<fileArr.length; idx++){
            int listIdx = 0;
            String[] pmidArr = pmidArrArr[idx];
            File file = fileArr[idx];
            log.info(file.getName());

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            try{
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(file);

                NodeList pubmedArticles = doc.getElementsByTagName("PubmedArticle");
                for (int i = 0; i < pubmedArticles.getLength(); i++) {  // ~30000
                    Node node = pubmedArticles.item(i);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        Element E_pubmedArticle = (Element) node;
                        Element E_pubmedData = (Element) E_pubmedArticle.getElementsByTagName("PubmedData").item(0);
                        Element E_articleIdList = (Element) E_pubmedData.getElementsByTagName("ArticleIdList").item(0);

                        // PMID, PMID_VERSION, PII, DOI
                        String[] IDs = getIDs(E_articleIdList);     // [pmid, pmcId, pii, doi]

                        String pmid = pmidArr[listIdx];
                        if(pmid.equals(IDs[0])){
                            String result = "pmid: " + pmid + "\tsize: " + IDs[2].length() + "\tpii: " + IDs[2];
                            log.info(result);
                            listIdx++;
                            if(listIdx == pmidArr.length)   break;
                        }

                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void sleep4random(){
        double rand = Math.random() * 3 + 2;
        log.info("{}s 지연", rand);

        try {
            Thread.sleep((long) rand * 1000);
        }catch (Exception e){
            log.error(e.toString());
        }
    }

    private void xmlParser(String file) {

        log.info("{} 여는 중", file);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);

            NodeList pubmedArticles = doc.getElementsByTagName("PubmedArticle");

            for (int i = 0; i < pubmedArticles.getLength(); i++) {  // ~30000
                Node node = pubmedArticles.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element E_pubmedArticle = (Element) node;
                    Element E_PMID = (Element) E_pubmedArticle.getElementsByTagName("PMID").item(0);
                    Element E_pubmedData = (Element) E_pubmedArticle.getElementsByTagName("PubmedData").item(0);
                    Element E_articleIdList = (Element) E_pubmedData.getElementsByTagName("ArticleIdList").item(0);

                    int version = Integer.parseInt(E_PMID.getAttribute("Version"));
                    String[] IDs = getIDs(E_articleIdList);
                    String pmid = IDs[0];
                    String pmcId = IDs[1];
                    String pii = IDs[2];
                    String doi = IDs[3];

                    log.info("IDs: {} {} {} {}", pmid, pmcId, pii, doi);

                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void logFile(String str){
        try {
            File file = new File("C:/logs/testLog.log");
            if (!file.exists()) {
                boolean create = file.createNewFile();
            }

            FileWriter fw = new FileWriter(file, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(str + "\r\n");
            bw.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
