package com.example.demo;

import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.net.ssl.*;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

@Service
public class MainService {
    private static final String KEY = "B772FAD1D21D71B8C2ED9F7B7BDFC013";
    private static final String apiURL = "https://krdict.korean.go.kr/api/search";

    public class NetworkUtil {

        public static void disableSslVerification() {
            try {
                // ============================================
                // trust manager 생성(인증서 체크 전부 안함)
                TrustManager[] trustAllCerts =new TrustManager[] {new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType){ }
                    public void checkServerTrusted(X509Certificate[] certs, String authType){ }
                }};

                // trust manager 설치
                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

                // ============================================
                // host name verifier 생성(호스트 네임 체크안함)
                HostnameVerifier allHostsValid = (hostname, session) -> true;

                // host name verifier 설치
                HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                e.printStackTrace();
            }
        }
    }


    public ResponseEntity searchMain(String q) {
        NetworkUtil.disableSslVerification();

        RequestEntity requestEntity = new RequestEntity(KEY, q);
        ResponseEntity responseEntity = null;

        try {
            URL url = new URL(apiURL + requestEntity.getParameter());
            StringBuffer response = getResponse(url);

            responseEntity = responseParsing(response.toString());
//			System.out.println("responseEntity = " + responseEntity);

        } catch (Exception e) {
            System.out.println(e);
        } finally {
            return responseEntity;
        }
    }

    private static StringBuffer getResponse(URL url) throws IOException {

        // refer : https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/net/HttpURLConnection.html
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setUseCaches(false);
        con.setDoInput(true);
        con.setDoOutput(true);
        con.setReadTimeout(30000);
        con.setRequestMethod("GET");
        con.connect();

        int responseCode = con.getResponseCode();
        BufferedReader br;
        if (responseCode == 200) {
            br = new BufferedReader(new InputStreamReader(con.getInputStream()));
        } else {
            br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
        }

        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = br.readLine()) != null) {
            response.append(inputLine);
        }
        br.close();

        return response;
    }

    private static ResponseEntity responseParsing(String response) {

        ResponseEntity responseEntity = new ResponseEntity();
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

        try {
            // process XML securely, avoid attacks like XML External Entities (XXE) -> Optional
            documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(new InputSource(new StringReader(response)));

            // DocumentElement normalize -> Optional
            document.getDocumentElement().normalize();

            // get item tag
            NodeList nodeList = document.getElementsByTagName("item");

            for (int idx = 0; idx < nodeList.getLength(); idx++) {

                Node node = nodeList.item(idx);

                if (node.getNodeType() == Node.ELEMENT_NODE) {

                    Element element = (Element) node;
                    String word = element.getElementsByTagName("word").item(0).getTextContent();
                    String pos = element.getElementsByTagName("pos").item(0).getTextContent();

                    List<SenseDTO> senseList = getSenseList(document);
                    responseEntity.setWord(word);
                    responseEntity.setPos(pos);
                    responseEntity.setSenseList(senseList);
                }
            }
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }

        return responseEntity;
    }

    private static List<SenseDTO> getSenseList(Document document) {

        List<SenseDTO> result = new ArrayList<>();
        // get sense tag
        NodeList list = document.getElementsByTagName("sense");

        for (int idx = 0; idx < list.getLength(); idx++) {

            Node node = list.item(idx);

            Element element = (Element) node;
            String definition = element.getElementsByTagName("definition").item(0).getTextContent();
            String trans_word = element.getElementsByTagName("trans_word").item(0).getTextContent();
            String trans_dfn = element.getElementsByTagName("trans_dfn").item(0).getTextContent();

            result.add(new SenseDTO(definition, trans_word, trans_dfn));
        }

        return result;
    }
}
