package com.argonet.practice;

import com.argonet.practice.vo.AffiliationVO;
import com.argonet.practice.vo.AuthorVO;
import com.argonet.practice.vo.MeshHeadingVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.http.HttpEntity;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

@Slf4j
public class Test {

    private static int AFFILIATION_INFO = 0;
    private static int AUTHOR_EMAIL = 0;
    private static int VALID_YN = 0;
    private static int EQUAL_CONTRIB = 0;
    private static int LAST_NAME = 0;
    private static int FORE_NAME = 0;
    private static int INITIALS = 0;
    private static int MESH_TERMS = 0;
    private static int MESH_UI = 0;
    private static int MAJOR_TOPIC_YN = 0;

    private static int REGISTRY_NUMBER = 0;
    private static int CHEMICAL_SUBSTANCE = 0;
    private static int CHEMICAL_UI = 0;
    private static int GRANT_ID = 0;
    private static int ACRONYM = 0;
    private static int AGENCY = 0;
    private static int COUNTRY = 0;
    private static int REFERENCE_PMID = 0;
    private static int CITATION = 0;

    public static void main(String[] args) throws IOException {
        Test test = new Test();
        log.info("시작");

//        test.getUpdatedGZ();

        int param = 2;
        test.getMaxLength(param);

        log.info("끝");
    }

    private void getUpdatedGZ() throws IOException {
        String url = "https://ftp.ncbi.nlm.nih.gov/pubmed/updatefiles/";
        String html = fn_crawling(url);
        html = html.split("<a href=\"README.txt\">")[1];
        String[] splitArr = html.split("<a href=\"");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(new Date());
//        date = "2024-09-22";

        for (String line : splitArr) {
            if (!line.contains("pubmed24n")) continue;
            String fileUrl = line.split("\">")[0];
            if(!fileUrl.endsWith(".gz")) continue;
            String updDt = line.split("</a>")[1];
            updDt = updDt.trim().substring(0, 10);

            if(updDt.equals(date)){
                String gzFile = downFile(fileUrl);
                String dest = "C:/pubmed/gzFiles/unzip/";
                String xmlFile = decompressGzFile(dest, gzFile);

                log.info(xmlFile);
                openXmlFile(xmlFile);

                if(xmlFile != null){
                    File f = new File(xmlFile);
                    boolean delete = f.delete();
                }
            }
        }
    }

    private String fn_crawling(String url) throws IOException {
        String html;

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);
            try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
                HttpEntity entity = response.getEntity();

                BufferedReader br;

                StringBuilder sb = new StringBuilder();
                String line;
                try {
                    br = new BufferedReader(new InputStreamReader(entity.getContent()));
                    while ((line = Objects.requireNonNull(br).readLine()) != null) {
                        sb.append(line).append("\r\n");
                    }
                } catch (IOException e) {
                    log.error("error: {}", e.getMessage());
                }

                html = sb.toString();

                EntityUtils.consume(entity);
            }
        }
        return html;
    }

    private String downFile(String pUrl) throws IOException {

        File f = new File("C:/pubmed/gzFiles/" + pUrl);
        if(f.exists()) {
            log.info("skip");
            return "C:/pubmed/gzFiles/"+pUrl;
        }

        URL url = new URL("https://ftp.ncbi.nlm.nih.gov/pubmed/updatefiles/" + pUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");
        conn.setRequestProperty("user-Agent", "Mozilla/5.0");

        int resCode = conn.getResponseCode();
        if(resCode == HttpURLConnection.HTTP_OK){
            InputStream is = conn.getInputStream();
            FileOutputStream fos = new FileOutputStream(new File("C:/pubmed/gzFiles", pUrl));

            final int BUFFER_SIZE = 4096;
            int bytesRead;
            byte[] buffer = new byte[BUFFER_SIZE];

            while((bytesRead = is.read(buffer)) != -1){
                fos.write(buffer, 0, bytesRead);
            }

            fos.close();
            is.close();
        }
        return "C:/pubmed/gzFiles/"+pUrl;
    }

    private String decompressGzFile(String dest, String file) {
        if(!file.contains(".gz"))   return null;
        String xmlFile = dest + file.substring(file.lastIndexOf("/")+1, file.lastIndexOf(".gz"));

        try {
            FileInputStream fis = new FileInputStream(file);
            GZIPInputStream gis = new GZIPInputStream(fis);
            FileOutputStream fos = new FileOutputStream(xmlFile);
            byte[] buffer = new byte[1024];
            int len;

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

    private void openXmlFile(String xmlFile){
        File file = new File(xmlFile);

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(xmlFile);

            NodeList pubmedArticles = doc.getElementsByTagName("PubmedArticle");

            for (int i = 0; i < pubmedArticles.getLength(); i++) {  // ~30000
                Node node = pubmedArticles.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element E_pubmedArticle = (Element) node;

                    Element E_article = (Element) E_pubmedArticle.getElementsByTagName("Article").item(0);
                    Element E_pubmedData = (Element) E_pubmedArticle.getElementsByTagName("PubmedData").item(0);
                    Element E_articleIdList = (Element) E_pubmedData.getElementsByTagName("ArticleIdList").item(0);
                    Element E_PMID = (Element) E_pubmedArticle.getElementsByTagName("PMID").item(0);
                    Element E_authorList = (Element) E_article.getElementsByTagName("AuthorList").item(0);
                    Element E_meshHeadingList = (Element) E_pubmedArticle.getElementsByTagName("MeshHeadingList").item(0);
                    Element E_chemicalList = (Element) E_pubmedArticle.getElementsByTagName("ChemicalList").item(0);
                    Element E_grantList = (Element) E_article.getElementsByTagName("GrantList").item(0);
                    Element E_referenceList = (Element) E_pubmedData.getElementsByTagName("ReferenceList").item(0);


                    // PUBMED_ARTICLE INSERT
                    parsingArticleInfo(E_pubmedArticle);

                    // PUBMED_ARTICLE_AUTHOR, PUBMED_ARTICLE_AFFILIATION, PUBMED_ARTICLE_MESH INSERT
                    parsingAuthorAndMesh(E_pubmedArticle);

                    // PUBMED_ARTICLE_CHEMICAL, PUBMED_ARTICLE_GRANT, PUBMED_ARTICLE_REFERENCE INSERT

                    log.info("----------------------------------------------------------------------------------------");

                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private void parsingArticleInfo(Element element){
        Element E_article = (Element) element.getElementsByTagName("Article").item(0);
        Element E_dateCompleted = (Element) element.getElementsByTagName("DateCompleted").item(0);
        Element E_dateRevised = (Element) element.getElementsByTagName("DateRevised").item(0);
        Element E_journalInfo = (Element) element.getElementsByTagName("MedlineJournalInfo").item(0);
        Element E_pubDate = (Element) E_article.getElementsByTagName("PubDate").item(0);
        Element E_pubmedData = (Element) element.getElementsByTagName("PubmedData").item(0);
        Element E_PMID = (Element) element.getElementsByTagName("PMID").item(0);
        Element E_articleIdList = (Element) E_pubmedData.getElementsByTagName("ArticleIdList").item(0);

        // DATE_COMPLETED, DATE_REVISED
        Date dateCompleted = getDateCompletedOrRevised(E_dateCompleted);
        Date dateRevised = getDateCompletedOrRevised(E_dateRevised);

        // ISSN_PRINT, ISSN_ELECTRONIC
        String[] issnArr = getIssn(E_article);
        String issnPrint = issnArr[0];
        String issnElectronic = issnArr[1];

        // MEDLINE_JOURNAL_COUNTRY, MEDLINE_JOURNAL_TA, NLM_UNIQUE_ID
        String country = E_journalInfo.getElementsByTagName("Country").item(0) != null
                ? E_journalInfo.getElementsByTagName("Country").item(0).getTextContent() : null;
        String ta = E_journalInfo.getElementsByTagName("MedlineTA").item(0) != null
                ? E_journalInfo.getElementsByTagName("MedlineTA").item(0).getTextContent() : null;
        String nlmUniqueId = E_journalInfo.getElementsByTagName("NlmUniqueID").item(0) != null
                ? E_journalInfo.getElementsByTagName("NlmUniqueID").item(0).getTextContent() : null;

        // VOLUME, ISSUE
        String volume = E_article.getElementsByTagName("Volume").item(0) != null
                ? E_article.getElementsByTagName("Volume").item(0).getTextContent() : null;
        String issue = E_article.getElementsByTagName("Issue").item(0) != null
                ? E_article.getElementsByTagName("Issue").item(0).getTextContent() : null;

        // PUB_YEAR, PUB_MONTH, PUB_DAY, MEDLINE_DATE, NORM_YM
        String[] pubDate = getPubDate(E_pubDate);
        String pubYear = pubDate[0];
        String pubMonth = pubDate[1];
        String pubDay = pubDate[2];
        String medlineDate = pubDate[3];
        String normYm = pubDate[4];

        // JOURNAL_TITLE
        String journalTitle = E_article.getElementsByTagName("Title").item(0) != null
                ? E_article.getElementsByTagName("Title").item(0).getTextContent() : null;
        // JOURNAL_TITLE_ABBR
        String isoAbbr = E_article.getElementsByTagName("ISOAbbreviation").item(0) != null
                ? E_article.getElementsByTagName("ISOAbbreviation").item(0).getTextContent() : null;
        // ARTICLE_TITLE
        String articleTitle = E_article.getElementsByTagName("ArticleTitle").item(0) != null
                ? E_article.getElementsByTagName("ArticleTitle").item(0).getTextContent() : null;
        // MEDLINE_PGN
        String medlinePgn = E_article.getElementsByTagName("MedlinePgn").item(0) != null
                ? E_article.getElementsByTagName("MedlinePgn").item(0).getTextContent() : null;
        // ABSTRACT
        String abstr = E_article.getElementsByTagName("Abstract").item(0) != null
                ? E_article.getElementsByTagName("Abstract").item(0).getTextContent() : null;
        // LANGUAGE
        String language = E_article.getElementsByTagName("Language").item(0) != null
                ? E_article.getElementsByTagName("Language").item(0).getTextContent() : null;

        // VERNACULAR_TITLE
        String vncTitle = E_article.getElementsByTagName("VernacularTitle").item(0) != null
                ? E_article.getElementsByTagName("VernacularTitle").item(0).getTextContent() : null;

        // PUBLICATION_STATUS
        String pubStatus = E_pubmedData.getElementsByTagName("PublicationStatus").item(0) != null
                ? E_pubmedData.getElementsByTagName("PublicationStatus").item(0).getTextContent() : null;

        // PMID, PMID_VERSION, PII, DOI
        int version = Integer.parseInt(E_PMID.getAttribute("Version"));
        String[] IDs = getIDs(E_articleIdList);
        String pmid = IDs[0];
        String pmcId = IDs[1];
        String pii = IDs[2];
        String doi = IDs[3];

        // REG_DATE
        Date regDate = new Date();
        // MOD_DATE
        Date modDate = new Date();

//      db insert
    }

    private Date getDateCompletedOrRevised(Element element) {
        try{
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date result = null;

            if(element != null){
                String year = element.getElementsByTagName("Year").item(0).getTextContent();
                String month = element.getElementsByTagName("Month").item(0).getTextContent();
                String day = element.getElementsByTagName("Day").item(0).getTextContent();
                result = sdf.parse(year + "-" + month + "-" + day);
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String[] getIssn(Element element) {
        NodeList issn = element.getElementsByTagName("ISSN");
        String[] issnArr = new String[2];
        for (int j = 0; j < issn.getLength(); j++) {
            NamedNodeMap attributes = issn.item(j).getAttributes();
            String issnType = attributes.getNamedItem("IssnType").getNodeValue();
            if (issnType.equals("Print"))       issnArr[0] = issn.item(j).getTextContent();
            if (issnType.equals("Electronic"))  issnArr[1] = issn.item(j).getTextContent();
        }
        return issnArr;
    }

    private String[] getPubDate(Element element){
        String pubYear = element.getElementsByTagName("Year").item(0) != null
                ? element.getElementsByTagName("Year").item(0).getTextContent() : null;
        String pubMonth = element.getElementsByTagName("Month").item(0) != null
                ? element.getElementsByTagName("Month").item(0).getTextContent() : null;
        String pubDay = element.getElementsByTagName("Day").item(0) != null
                ? element.getElementsByTagName("Day").item(0).getTextContent() : null;

        String medlineDate = element.getElementsByTagName("MedlineDate").item(0) != null
                ? element.getElementsByTagName("MedlineDate").item(0).getTextContent() : null;

        String normYm = "";
        if(pubYear != null){
            normYm = switch (pubMonth != null ? pubMonth : "") {
                case "Jan" -> pubYear + "01";
                case "Feb" -> pubYear + "02";
                case "Mar" -> pubYear + "03";
                case "Apr" -> pubYear + "04";
                case "May" -> pubYear + "05";
                case "Jun" -> pubYear + "06";
                case "Jul" -> pubYear + "07";
                case "Aug" -> pubYear + "08";
                case "Sep" -> pubYear + "09";
                case "Oct" -> pubYear + "10";
                case "Nov" -> pubYear + "11";
                case "Dec" -> pubYear + "12";
                default -> pubYear;
            };
        } else if(medlineDate != null){
            String month = medlineDate.substring(4).trim();
            month = month.substring(0, 3);
            normYm = switch (month) {
                case "Jan" -> medlineDate.substring(0, 4) + "01";
                case "Feb" -> medlineDate.substring(0, 4) + "02";
                case "Mar" -> medlineDate.substring(0, 4) + "03";
                case "Apr" -> medlineDate.substring(0, 4) + "04";
                case "May" -> medlineDate.substring(0, 4) + "05";
                case "Jun" -> medlineDate.substring(0, 4) + "06";
                case "Jul" -> medlineDate.substring(0, 4) + "07";
                case "Aug" -> medlineDate.substring(0, 4) + "08";
                case "Sep" -> medlineDate.substring(0, 4) + "09";
                case "Oct" -> medlineDate.substring(0, 4) + "10";
                case "Nov" -> medlineDate.substring(0, 4) + "11";
                case "Dec" -> medlineDate.substring(0, 4) + "12";
                default -> medlineDate.substring(0, 4);
            };

            for(char c : normYm.toCharArray()){
                if(!Character.isDigit(c)) {
                    normYm = medlineDate.substring(medlineDate.length()-4);
                    break;
                }
            }
        }
        return new String[]{pubYear, pubMonth, pubDay, medlineDate, normYm};
    }

    private static String[] getIDs(Element element) {
        NodeList articleIds = element.getElementsByTagName("ArticleId");
        String[] IDs = new String[4];
        for(int j=0; j<articleIds.getLength(); j++){
            NamedNodeMap attributes = articleIds.item(j).getAttributes();
            String idType = attributes.getNamedItem("IdType").getNodeValue();
            if(idType.equals("pubmed")) IDs[0] = articleIds.item(j).getTextContent();
            if(idType.equals("pmc"))    IDs[1] = articleIds.item(j).getTextContent();
            if(idType.equals("pii"))    IDs[2] = articleIds.item(j).getTextContent();
            if(idType.equals("doi"))    IDs[3] = articleIds.item(j).getTextContent();

        }
        return IDs;
    }

    private void parsingAuthorAndMesh(Element element){

        Element E_article = (Element) element.getElementsByTagName("Article").item(0);
        Element E_pubmedData = (Element) element.getElementsByTagName("PubmedData").item(0);
        Element E_articleIdList = (Element) E_pubmedData.getElementsByTagName("ArticleIdList").item(0);
        Element E_authorList = (Element) E_article.getElementsByTagName("AuthorList").item(0);
        Element E_meshHeadingList = (Element) element.getElementsByTagName("MeshHeadingList").item(0);

        String[] IDs = getIDs(E_articleIdList);
        String pmid = IDs[0];
        int articleId = 0;// get articleId

        // authorList
        if(E_authorList != null) {
            getAuthorList(E_authorList, articleId, pmid);
        }

        // meshKeyword
        if(E_meshHeadingList != null) {
            getMeshKeyword(E_meshHeadingList, articleId, pmid);
        }

    }

    private void getAuthorList(Element E_authorList, int articleId, String pmid){
        NodeList authorList = E_authorList.getElementsByTagName("Author");
        for(int seq=0; seq<authorList.getLength(); seq++){
            Element E_author = (Element) authorList.item(seq);
            String validYn = E_author.getAttribute("ValidYN").equalsIgnoreCase("Y") ? "Y" : "N";
            String equalCont = E_author.getAttribute("EqualContrib").equalsIgnoreCase("Y") ? "Y" : null;
            String lastName = E_author.getElementsByTagName("LastName").item(0) != null ? E_author.getElementsByTagName("LastName").item(0).getTextContent() : null;
            String foreName = E_author.getElementsByTagName("ForeName").item(0) != null ? E_author.getElementsByTagName("ForeName").item(0).getTextContent() : null;
            String initials = E_author.getElementsByTagName("Initials").item(0) != null ? E_author.getElementsByTagName("Initials").item(0).getTextContent() : null;

            String affiliation;
            String email;
            NodeList affiliationInfoList = E_author.getElementsByTagName("AffiliationInfo");
            int affiliationLength = affiliationInfoList.getLength();
            if(affiliationLength > 0){
                for(int j=0; j<affiliationLength; j++){
                    Element E_affiliation = (Element) affiliationInfoList.item(j);

                    affiliation = E_affiliation.getTextContent();

                    if(affiliation.getBytes().length > 2000) continue;

                    if(affiliation.lastIndexOf(" ") != -1){
                        email = affiliation.substring(affiliation.lastIndexOf(" ")+1);

                        String pattern = "^[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_.]?[0-9a-zA-Z])*.[a-zA-Z]{2,3}$";
                        if(!Pattern.matches(pattern, email)){
                            email = null;
                        }else{
                            affiliation = affiliation.substring(0, affiliation.lastIndexOf(" ")+1);
                        }
                    }else{
                        email = null;
                    }

                    AffiliationVO affiliationVO = new AffiliationVO(articleId, seq+1, j+1, pmid, affiliation, email);
                    log.info("affiliationVO: {}", affiliationVO);
//                    db insert
//                    int cnt = this.pubMedService.insertAffiliation(affiliationVO);
                }
            }

            AuthorVO authorVO = new AuthorVO(articleId, seq+1, pmid, validYn, equalCont, lastName, foreName, initials);

            // insert into database
            log.info("authorVO: {}", authorVO);
//            db insert
//            int cnt = this.pubMedService.insertAuthor(authorVO);
        }
    }

    private void getMeshKeyword(Element E_meshHeadingList, int articleId, String pmid){
        NodeList meshHeadingList = E_meshHeadingList.getElementsByTagName("MeshHeading");
        for(int j=0, seq=1; j<meshHeadingList.getLength(); j++){
            Element E_meshHeading = (Element) meshHeadingList.item(j);

            NodeList qualifierList = E_meshHeading.getElementsByTagName("QualifierName");
            if(qualifierList.getLength() > 0){
                for(int k=0; k<qualifierList.getLength(); k++){
                    Element descriptorName = (Element) E_meshHeading.getElementsByTagName("DescriptorName").item(0);
                    Element E_qualifier = (Element) qualifierList.item(k);

                    String meshTerms = descriptorName.getTextContent() + " / " + E_qualifier.getTextContent();
                    String meshUi = descriptorName.getAttribute("UI") + "_" + E_qualifier.getAttribute("UI");
                    String majorTopicYn = E_qualifier.getAttribute("MajorTopicYN").equals("Y") ? "Y" : "N";

                    MeshHeadingVO meshHeadingVO = new MeshHeadingVO(articleId, seq++, pmid, meshTerms, meshUi, majorTopicYn);

                    // insert into database
                    log.info("meshHeadingVO: {}", meshHeadingVO);
//                    db insert
//                    int cnt = this.pubMedService.insertMeshHeading(meshHeadingVO);
                }
            }else{
                Element descriptorName = (Element) E_meshHeading.getElementsByTagName("DescriptorName").item(0);
                String meshTerms = descriptorName.getTextContent();
                String meshUi = descriptorName.getAttribute("UI");
                String majorTopicYn = descriptorName.getAttribute("MajorTopicYN");

                MeshHeadingVO meshHeadingVO = new MeshHeadingVO(articleId, seq++, pmid, meshTerms, meshUi, majorTopicYn);

                // insert into database
                log.info("meshHeadingVO: {}", meshHeadingVO);
//                db insert
//                int cnt = this.pubMedService.insertMeshHeading(meshHeadingVO);
            }
        }
    }

//    ------------------------------------------------------------------------------------------------------------------
    private void getMaxLength(int param){
        File dir = new File("C:/pubmed/downFiles");
        File[] fileArr = dir.listFiles();
        if(fileArr != null) {
            String dest = "C://pubmed/xmlFiles/";
            for(int i=263; i<fileArr.length; i++){
                File file = fileArr[i];
//            for(File file : fileArr){
                // decompress File
                String xmlFile = decompressGzFile(dest, "C:/pubmed/downFiles/" + file.getName());

                // xmlParsing
                int[] result = parsing(xmlFile, param);

                // print Result
//                log.info("result[0]: {}\tresult[1]: {}\tresult[2]: {}\n" +
//                        "AFFILIATION_INFO: {}\tAUTHOR_EMAIL: {}\tVALID_YN: {}\tEQUAL_CONTRIB: {}\tLAST_NAME: {}\tFORE_NAME: {}\tINITIALS: {}\tMESH_TERMS: {}\tMESH_UI: {}\tMAJOR_TOPIC_YN: {}\t",
//                        result[0], result[1], result[2], AFFILIATION_INFO, AUTHOR_EMAIL, VALID_YN, EQUAL_CONTRIB, LAST_NAME, FORE_NAME, INITIALS, MESH_TERMS, MESH_UI, MAJOR_TOPIC_YN);
                String str = "REGISTRY_NUMBER: " + REGISTRY_NUMBER + "\tCHEMICAL_SUBSTANCE: " + CHEMICAL_SUBSTANCE + "\tCHEMICAL_UI: " + CHEMICAL_UI +
                             "\tGRANT_ID: " + GRANT_ID + "\tACRONYM: " + ACRONYM + "\tAGENCY: " + AGENCY + "\tCOUNTRY: " + COUNTRY +
                             "\tREFERENCE_PMID: " + REFERENCE_PMID + "\tCITATION: " + CITATION;

                try {
                    File txtFile = new File("C:/logs/dataSize.log");
                    if (!txtFile.exists()) {
                        boolean create = txtFile.createNewFile();
                    }

                    FileWriter fw = new FileWriter(txtFile, true);
                    BufferedWriter bw = new BufferedWriter(fw);
                    bw.write(str + "\r\n");
                    bw.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                // file delete
                if(xmlFile != null){
                    File f = new File(xmlFile);
                    boolean delete = f.delete();
                }
            }
        }
    }

    private int[] parsing(String xmlFile, int param) {
        int[] cnts = new int[3];

        log.info("{} 여는 중", xmlFile);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(xmlFile);

            NodeList pubmedArticles = doc.getElementsByTagName("PubmedArticle");

            for (int i = 0; i < pubmedArticles.getLength(); i++) {  // ~30000
                Node node = pubmedArticles.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element E_pubmedArticle = (Element) node;
                    Element E_article = (Element) E_pubmedArticle.getElementsByTagName("Article").item(0);
                    Element E_pubmedData = (Element) E_pubmedArticle.getElementsByTagName("PubmedData").item(0);
                    Element E_articleIdList = (Element) E_pubmedData.getElementsByTagName("ArticleIdList").item(0);
                    Element E_authorList = (Element) E_article.getElementsByTagName("AuthorList").item(0);
                    Element E_meshHeadingList = (Element) E_pubmedArticle.getElementsByTagName("MeshHeadingList").item(0);
                    Element E_chemicalList = (Element) E_pubmedArticle.getElementsByTagName("ChemicalList").item(0);
                    Element E_grantList = (Element) E_article.getElementsByTagName("GrantList").item(0);
                    Element E_referenceList = (Element) E_pubmedData.getElementsByTagName("ReferenceList").item(0);

                    String[] IDs = getIDs(E_articleIdList);
                    String pmid = IDs[0];

                    if(param == 1){
                        // authorList
                        if(E_authorList != null) {
                            getAuthorList(E_authorList, 0, pmid);
                        }

                        // meshKeyword
                        if(E_meshHeadingList != null) {
                            getMeshKeyword(E_meshHeadingList, 0, pmid);
                        }
                    }
                    else if(param == 2){
                        // chemicalList
                        if(E_chemicalList != null) {
                            cnts[0] += getChemicalList(E_chemicalList, pmid);
                        }

                        // grantList
                        if(E_grantList != null) {
                            cnts[1] += getGrantList(E_grantList, pmid);
                        }

                        // referenceList
                        if(E_referenceList != null) {
                            cnts[2] += getReferenceList(E_referenceList, pmid);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return cnts;
    }

    private int getReferenceList(Element element, String pmid) {
        NodeList referenceList = element.getElementsByTagName("Reference");
        for(int seq=0; seq<referenceList.getLength(); seq++){
            Element E_grant = (Element) referenceList.item(seq);
            Element E_citation = (Element) E_grant.getElementsByTagName("Citation").item(0);
            Element E_articleIdList = (Element) E_grant.getElementsByTagName("ArticleIdList").item(0);
            Element E_articleId = E_articleIdList != null ? (Element) (E_articleIdList).getElementsByTagName("ArticleId").item(0) : null;

            String referencePmid = E_articleId != null ? E_articleId.getTextContent() : null;
            String citation = E_citation != null ? E_citation.getTextContent() : null;

            if(referencePmid != null && referencePmid.length() > REFERENCE_PMID) REFERENCE_PMID = referencePmid.length();
            if(citation != null && citation.length() > CITATION) CITATION = citation.length();
        }
        return referenceList.getLength();
    }

    private int getGrantList(Element element, String pmid) {
        NodeList grantList = element.getElementsByTagName("Grant");
        for(int seq=0; seq<grantList.getLength(); seq++){
            Element E_grant = (Element) grantList.item(seq);

            String grantId = E_grant.getElementsByTagName("GrantID").item(0) != null ? E_grant.getElementsByTagName("GrantID").item(0).getTextContent() : null;
            String acronym = E_grant.getElementsByTagName("Acronym").item(0) != null ? E_grant.getElementsByTagName("Acronym").item(0).getTextContent() : null;
            String agency = E_grant.getElementsByTagName("Agency").item(0) != null ? E_grant.getElementsByTagName("Agency").item(0).getTextContent() : null;
            String country = E_grant.getElementsByTagName("Country").item(0) != null ? E_grant.getElementsByTagName("Country").item(0).getTextContent() : null;

            if(grantId != null && grantId.length() > GRANT_ID) GRANT_ID = grantId.length();
            if(acronym != null && acronym.length() > ACRONYM) ACRONYM = acronym.length();
            if(agency != null && agency.length() > AGENCY) AGENCY = agency.length();
            if(country != null && country.length() > COUNTRY) COUNTRY = country.length();
        }
        return grantList.getLength();
    }

    private int getChemicalList(Element element, String pmid) {
        NodeList chemicalList = element.getElementsByTagName("Chemical");
        for(int seq=0; seq<chemicalList.getLength(); seq++){
            Element E_chemical = (Element) chemicalList.item(seq);
            Element E_substance = (Element) E_chemical.getElementsByTagName("NameOfSubstance").item(0);
            Element E_registryNumber = (Element) E_chemical.getElementsByTagName("RegistryNumber").item(0);

            String registryNumber = E_registryNumber != null ? E_registryNumber.getTextContent() : null;
            String chemicalSubstance = E_substance != null ? E_substance.getTextContent() : null;
            String chemicalUi = E_substance != null ? E_substance.getAttribute("UI") : null;

            if(registryNumber != null && registryNumber.length() > REGISTRY_NUMBER) REGISTRY_NUMBER = registryNumber.length();
            if(chemicalSubstance != null && chemicalSubstance.length() > CHEMICAL_SUBSTANCE) CHEMICAL_SUBSTANCE = chemicalSubstance.length();
            if(chemicalUi != null && chemicalUi.length() > CHEMICAL_UI) CHEMICAL_UI = chemicalUi.length();
        }
        return chemicalList.getLength();
    }
}