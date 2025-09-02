package com.infominez.cryptofish.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infominez.cryptofish.base.BaseResponse;
import com.infominez.cryptofish.utils.AppConfiguration;
//import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.QueryParam;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/cryptofish")
@AllArgsConstructor
@Slf4j
public class CryptofishController {

    private final AppConfiguration appConfiguration;

    @GetMapping("/getTokenByTxnHash")
    public BaseResponse afterPayment(@QueryParam("txnHash") String txnHash, HttpServletRequest request ) throws IOException{
        log.info(" :- afterPayment()",txnHash);
        BaseResponse response = new BaseResponse();
        Boolean gettxreceiptstatus = null;
        Boolean getTxnDetail = null;
        BigInteger tokenTemp = null;


        String apiUrl = appConfiguration.getProperty("scrap.apiUrl");
        String apiKey = appConfiguration.getProperty("scrap.apiKey");

        String apiUrlEthersScan = apiUrl + "proxy&action=eth_getTransactionReceipt&txhash="+ txnHash +"&apikey=" + apiKey;
        Connection.Response connResponse = null;
        connResponse = Jsoup.connect(apiUrlEthersScan).method(Connection.Method.GET).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36").header("Content-Type",MediaType.APPLICATION_JSON_VALUE).ignoreContentType(true).followRedirects(true).execute();
        String tempResult = connResponse.body();
        org.json.JSONObject result = new org.json.JSONObject(tempResult);

        if (!result.has("result") || result.optJSONObject("result") == null || result.optJSONObject("result").isEmpty() || result.optJSONObject("result").optString("value") == null || result.optJSONObject("result").optString("hash") == null || result.optJSONObject("result").optString("status") == null) {
            for (int i = 0; i < 20; i++) {
                connResponse = Jsoup.connect(apiUrlEthersScan).method(Connection.Method.GET).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36").header("Content-Type",MediaType.APPLICATION_JSON_VALUE).ignoreContentType(true).followRedirects(true).execute();
                tempResult = connResponse.body();
                result = new org.json.JSONObject(tempResult);
                if (result.has("result") && result.optJSONObject("result") != null && !result.optJSONObject("result").isEmpty() && result.optJSONObject("result").optString("value") != null && result.optJSONObject("result").optString("hash") != null && result.optJSONObject("result").optString("status") != null) {
                    break;
                }
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        if (result.has("result") && result.optJSONObject("result") != null && !result.optJSONObject("result").isEmpty()) {
            if (result.optJSONObject("result").optString("value") != null && result.optJSONObject("result").optString("hash") != null) {
                if (!txnHash.equalsIgnoreCase(result.optJSONObject("result").optString("transactionHash"))) {
                    getTxnDetail = false;
                    log.error("getTxnDetail -2");
                    response.setStatus(302);
                    response.setMessage("Unable to validate txn");
                }
                log.error("getTxnDetail success");
                getTxnDetail = true;

                if (result.has("result") && result.optJSONObject("result") != null) {
                    if (result.optJSONObject("result").optString("status") != null) {
                        switch (result.optJSONObject("result").optString("status")) {
                            case "": {
                                log.error("gettxreceiptstatus -3");
                                gettxreceiptstatus = false;
                                response.setStatus(302);
                                response.setMessage("Transaction is not completed yet. Please try after transaction settlement.");
                                return response;
                            }
                            case "0x1": {
                                gettxreceiptstatus = true;
                                log.info("gettxreceiptstatus success ");
                                org.json.JSONArray logs = result.optJSONObject("result").getJSONArray("logs");
                                List<BigInteger> revealTokens = new ArrayList<>();

                                for (int i = 0; i < logs.length(); i++) {
                                    try {
                                        String token = logs.getJSONObject(i).getJSONArray("topics").getString(3);
                                        log.info("token : " + token);
                                        tokenTemp = org.web3j.utils.Numeric.toBigInt(org.web3j.utils.Numeric.hexStringToByteArray(token));
                                        log.info("tokenTemp : " + tokenTemp);

                                        // for change path of image  and json.
                                        // change path for copy and past image and json

                                        ObjectMapper objectMapper = new ObjectMapper();
                                        // JsonNode jsonNode = objectMapper.readTree(new File("home\\source\\source-json\\" + tokenTemp+".json"));
                                        // jsonNode = objectMapper.readTree(new File("D:\\cryptofish_items\\destination\\json\\"+ tokenTemp +".json"));
                                        log.info(" executing before path token " + tokenTemp);
                                        JsonNode jsonNode = objectMapper.readTree(new File("/home/imz/source/source-json/" + tokenTemp + ".json"));
                                        log.info("executing after path token " + tokenTemp);
                                        String imageUrl = jsonNode.get("image").asText();
                                        log.info("imageUrl=" + imageUrl);
                                        String fileName = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
                                        log.info("Image Name: " + fileName);

                                        //forServerPath
                                        log.info("before  imagepath");
                                        Path sourcePath = Paths.get("/home/imz/source/source-image/" + fileName);
                                        Path destinationPath = Paths.get("/var/www/html/crypto-fish/Metadata2/" + fileName);
                                        Files.move(sourcePath,destinationPath,StandardCopyOption.REPLACE_EXISTING);
                                        log.info("after imagepath");
                                        Path sourcePath1 = Paths.get("/home/imz/source/source-json/" + tokenTemp + ".json");
                                        Path destinationPath1 = Paths.get("/var/www/html/crypto-fish/Metadata1/" + tokenTemp + ".json");
                                        Files.move(sourcePath1,destinationPath1,StandardCopyOption.REPLACE_EXISTING);
                                        revealTokens.add(tokenTemp);
                                    } catch (Exception e) {
                                        log.error("Error in moving file of Token {} " , e );
                                        log.error("Error in moving file of Token = " + tokenTemp );
                                    }

                            }
                                response.setStatus(200);
                                response.setMessage("Success");
                                response.setResponse(revealTokens);
                                return response;


                            }
                            case "0x0": {
                                gettxreceiptstatus = false;

                                log.error("gettxreceiptstatus -2");
                                response.setStatus(302);
                                response.setMessage("Transaction was not successful");
                                return response;
                            }
                        }
                    } else {
                        gettxreceiptstatus = false;
                        log.error("gettxreceiptstatus -1");
                        response.setStatus(302);
                        response.setMessage("Unable to validate transaction. Create a ticket on discord");
                        return response;
                    }
                } else {
                    gettxreceiptstatus = false;
                    log.error("gettxreceiptstatus 0");
                    response.setStatus(302);
                    response.setMessage("Unable to validate txn. Create a ticket on discord");
                    return response;
                }
            } else {
                getTxnDetail = false;
                log.error("getTxnDetail -1");
                response.setStatus(302);
                response.setMessage("Unable to validate transaction. Create a ticket on discord");
            }
        } else {
            getTxnDetail = false;
            log.error("getTxnDetail 0");
            response.setStatus(302);
            response.setMessage("Unable to validate transaction. Create a ticket on discord");

        }

        if (getTxnDetail == null || !getTxnDetail) {
            return response;
        }
        return response;
    }


}
