package com.infominez.cryptofish.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infominez.cryptofish.base.BaseResponse;
import com.infominez.cryptofish.utils.AppConfiguration;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.QueryParam;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/cryptofish")
@AllArgsConstructor
@Slf4j
public class CryptofishController {

    private final AppConfiguration appConfiguration;
//0x9cc43530e4239de182ba2bc82abdc3b2b55b220d
    private static final String EXPECTED_CONTRACT_ADDRESS = "0x9Ef31ce8cca614E7aFf3c1b883740E8d2728Fe91".toLowerCase(); //TODO change it to mainnet


    private static final int MAX_RETRIES = 20;
    private static final long RETRY_DELAY_MS = 5000;

    @GetMapping("/getTokenByTxnHash")
    public BaseResponse afterPayment(@QueryParam("txnHash") String txnHash, HttpServletRequest request) throws IOException {
        log.info(" :- afterPayment() {}", txnHash);
        BaseResponse response = new BaseResponse();
        Boolean gettxreceiptstatus = null;
        Boolean getTxnDetail = null;
        BigInteger tokenTemp = null;

        String ETHEREUM_RPC_URL = "https://ethereum.publicnode.com";
//        String ETHEREUM_RPC_URL = appConfiguration.getProperty("ethereum.rpc.url");

        String jsonRpcPayload = String.format(
                "{\"jsonrpc\":\"2.0\",\"method\":\"eth_getTransactionReceipt\",\"params\":[\"%s\"],\"id\":1}",
                txnHash
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("User-Agent", "Mozilla/5.0 (compatible; CryptofishApp/1.0)");

        HttpEntity<String> requestEntity = new HttpEntity<>(jsonRpcPayload, headers);
        RestTemplate restTemplate = new RestTemplate();

        log.info("Calling Ethereum RPC at {}", ETHEREUM_RPC_URL);
        ResponseEntity<String> rpcResponse = restTemplate.exchange(ETHEREUM_RPC_URL, HttpMethod.POST, requestEntity, String.class);

        String tempResult = rpcResponse.getBody();
        JSONObject result = new JSONObject(tempResult);

        int retries = 0;
        while ((!result.has("result") || result.isNull("result")) && retries < MAX_RETRIES) {
            log.warn("Transaction receipt not ready. Retrying {}/{}", retries + 1, MAX_RETRIES);
            try {
                Thread.sleep(RETRY_DELAY_MS);
            } catch (InterruptedException e) {
                log.error("Retry sleep interrupted", e);
            }

            rpcResponse = restTemplate.exchange(ETHEREUM_RPC_URL, HttpMethod.POST, requestEntity, String.class);
            tempResult = rpcResponse.getBody();
            result = new JSONObject(tempResult);
            retries++;
        }

        if (result.has("result") && !result.isNull("result")) {
            JSONObject receipt = result.getJSONObject("result");

            if (!txnHash.equalsIgnoreCase(receipt.optString("transactionHash"))) {
                getTxnDetail = false;
                log.error("getTxnDetail -2");
                response.setStatus(302);
                response.setMessage("Unable to validate txn");
                return response;
            }

            log.info("getTxnDetail success");
            getTxnDetail = true;

            String status = receipt.optString("status");
            if (status != null) {
                switch (status) {
                    case "":
                        log.error("gettxreceiptstatus -3");
                        response.setStatus(302);
                        response.setMessage("Transaction is not completed yet. Please try after transaction settlement.");
                        return response;

                    case "0x1":
                        gettxreceiptstatus = true;
                        log.info("gettxreceiptstatus success");
                        JSONArray logs = receipt.getJSONArray("logs");
                        List<BigInteger> revealTokens = new ArrayList<>();

                        for (int i = 0; i < logs.length(); i++) {
                            try {

                                JSONObject logEntry = logs.getJSONObject(i);

                                // ✅ Check log comes from expected contract
                                String logAddress = logEntry.optString("address", "").toLowerCase();
                                if (!EXPECTED_CONTRACT_ADDRESS.equals(logAddress)) {
                                    log.warn("Skipping log from unknown contract: {}", logAddress);
                                    continue;
                                }

                                String token = logs.optJSONObject(i).optJSONArray("topics").optString(3);
                                if(token == null || token.isEmpty()) {
                                    log.info("log are empty for token in iteration  : {}", i);
                                    continue;
                                }
                                log.info("token : {}", token);
                                tokenTemp = org.web3j.utils.Numeric.toBigInt(org.web3j.utils.Numeric.hexStringToByteArray(token));
                                log.info("tokenTemp : {}", tokenTemp);

                                ObjectMapper objectMapper = new ObjectMapper();
                                JsonNode jsonNode = objectMapper.readTree(new File("/home/ubuntu/cryptofish_1655/json/" + tokenTemp + ".json"));
                                String imageUrl = jsonNode.get("image").asText();
                                String fileName = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);

                                Path sourceImagePath = Paths.get("/home/ubuntu/cryptofish_1655/image/" + fileName);
                                Path destImagePath = Paths.get("/var/www/crypto_fish/metadata2/" + fileName);
                                Files.move(sourceImagePath, destImagePath, StandardCopyOption.REPLACE_EXISTING);

                                Path sourceJsonPath = Paths.get("/home/ubuntu/cryptofish_1655/json/" + tokenTemp + ".json");
                                Path destJsonPath = Paths.get("/var/www/crypto_fish/metadata/" + tokenTemp + ".json");
                                Files.move(sourceJsonPath, destJsonPath, StandardCopyOption.REPLACE_EXISTING);

                                revealTokens.add(tokenTemp);
                            } catch (Exception e) {
                                log.error("Error in moving file of Token {} ", e);
                            }
                        }

                        response.setStatus(200);
                        response.setMessage("Success");
                        response.setResponse(revealTokens);
                        return response;

                    case "0x0":
                        log.error("gettxreceiptstatus -2");
                        response.setStatus(302);
                        response.setMessage("Transaction was not successful");
                        return response;
                }
            } else {
                log.error("gettxreceiptstatus -1");
                response.setStatus(302);
                response.setMessage("Unable to validate transaction. Create a ticket on discord");
                return response;
            }
        } else {
            log.error("getTxnDetail 0");
            response.setStatus(302);
            response.setMessage("Unable to validate transaction. Create a ticket on discord");
            return response;
        }

        if (getTxnDetail == null || !getTxnDetail) {
            return response;
        }
        return response;
    }
}








































/*
* ==========================this is previous code============================== */
//package com.infominez.cryptofish.controller;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.infominez.cryptofish.base.BaseResponse;
//import com.infominez.cryptofish.utils.AppConfiguration;
////import jakarta.servlet.http.HttpServletRequest;
//import lombok.AllArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.jsoup.Connection;
//import org.jsoup.Jsoup;
//import org.springframework.http.MediaType;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.ws.rs.QueryParam;
//import java.io.File;
//import java.io.IOException;
//import java.math.BigInteger;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.nio.file.StandardCopyOption;
//import java.util.ArrayList;
//import java.util.List;
//
//@RestController
//@RequestMapping("/cryptofish")
//@AllArgsConstructor
//@Slf4j
//public class CryptofishController {
//
//    private final AppConfiguration appConfiguration;
//
//    @GetMapping("/getTokenByTxnHash")
//    public BaseResponse afterPayment(@QueryParam("txnHash") String txnHash, HttpServletRequest request ) throws IOException{
//        log.info(" :- afterPayment()",txnHash);
//        BaseResponse response = new BaseResponse();
//        Boolean gettxreceiptstatus = null;
//        Boolean getTxnDetail = null;
//        BigInteger tokenTemp = null;
//
//
//        String apiUrl = appConfiguration.getProperty("scrap.apiUrl");
//        String apiKey = appConfiguration.getProperty("scrap.apiKey");
//
//        String apiUrlEthersScan = apiUrl + "proxy&action=eth_getTransactionReceipt&txhash="+ txnHash +"&apikey=" + apiKey;
//        Connection.Response connResponse = null;
//        connResponse = Jsoup.connect(apiUrlEthersScan).method(Connection.Method.GET).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36").header("Content-Type",MediaType.APPLICATION_JSON_VALUE).ignoreContentType(true).followRedirects(true).execute();
//        String tempResult = connResponse.body();
//        org.json.JSONObject result = new org.json.JSONObject(tempResult);
//
//        if (!result.has("result") || result.optJSONObject("result") == null || result.optJSONObject("result").isEmpty() || result.optJSONObject("result").optString("value") == null || result.optJSONObject("result").optString("hash") == null || result.optJSONObject("result").optString("status") == null) {
//            for (int i = 0; i < 20; i++) {
//                connResponse = Jsoup.connect(apiUrlEthersScan).method(Connection.Method.GET).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36").header("Content-Type",MediaType.APPLICATION_JSON_VALUE).ignoreContentType(true).followRedirects(true).execute();
//                tempResult = connResponse.body();
//                result = new org.json.JSONObject(tempResult);
//                if (result.has("result") && result.optJSONObject("result") != null && !result.optJSONObject("result").isEmpty() && result.optJSONObject("result").optString("value") != null && result.optJSONObject("result").optString("hash") != null && result.optJSONObject("result").optString("status") != null) {
//                    break;
//                }
//                try {
//                    Thread.sleep(5000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        if (result.has("result") && result.optJSONObject("result") != null && !result.optJSONObject("result").isEmpty()) {
//            if (result.optJSONObject("result").optString("value") != null && result.optJSONObject("result").optString("hash") != null) {
//                if (!txnHash.equalsIgnoreCase(result.optJSONObject("result").optString("transactionHash"))) {
//                    getTxnDetail = false;
//                    log.error("getTxnDetail -2");
//                    response.setStatus(302);
//                    response.setMessage("Unable to validate txn");
//                }
//                log.error("getTxnDetail success");
//                getTxnDetail = true;
//
//                if (result.has("result") && result.optJSONObject("result") != null) {
//                    if (result.optJSONObject("result").optString("status") != null) {
//                        switch (result.optJSONObject("result").optString("status")) {
//                            case "": {
//                                log.error("gettxreceiptstatus -3");
//                                gettxreceiptstatus = false;
//                                response.setStatus(302);
//                                response.setMessage("Transaction is not completed yet. Please try after transaction settlement.");
//                                return response;
//                            }
//                            case "0x1": {
//                                gettxreceiptstatus = true;
//                                log.info("gettxreceiptstatus success ");
//                                org.json.JSONArray logs = result.optJSONObject("result").getJSONArray("logs");
//                                List<BigInteger> revealTokens = new ArrayList<>();
//
//                                for (int i = 0; i < logs.length(); i++) {
//                                    try {
//                                        String token = logs.getJSONObject(i).getJSONArray("topics").getString(3);
//                                        log.info("token : " + token);
//                                        tokenTemp = org.web3j.utils.Numeric.toBigInt(org.web3j.utils.Numeric.hexStringToByteArray(token));
//                                        log.info("tokenTemp : " + tokenTemp);
//
//                                        // for change path of image  and json.
//                                        // change path for copy and past image and json
//
//                                        ObjectMapper objectMapper = new ObjectMapper();
//                                        // JsonNode jsonNode = objectMapper.readTree(new File("home\\source\\source-json\\" + tokenTemp+".json"));
//                                        // jsonNode = objectMapper.readTree(new File("D:\\cryptofish_items\\destination\\json\\"+ tokenTemp +".json"));
//                                        log.info(" executing before path token " + tokenTemp);
//                                        JsonNode jsonNode = objectMapper.readTree(new File("/home/imz/source/source-json/" + tokenTemp + ".json"));
//                                        log.info("executing after path token " + tokenTemp);
//                                        String imageUrl = jsonNode.get("image").asText();
//                                        log.info("imageUrl=" + imageUrl);
//                                        String fileName = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
//                                        log.info("Image Name: " + fileName);
//
//                                        //forServerPath
//                                        log.info("before  imagepath");
//                                        Path sourcePath = Paths.get("/home/imz/source/source-image/" + fileName);
//                                        Path destinationPath = Paths.get("/var/www/html/crypto-fish/Metadata2/" + fileName);
//                                        Files.move(sourcePath,destinationPath,StandardCopyOption.REPLACE_EXISTING);
//                                        log.info("after imagepath");
//                                        Path sourcePath1 = Paths.get("/home/imz/source/source-json/" + tokenTemp + ".json");
//                                        Path destinationPath1 = Paths.get("/var/www/html/crypto-fish/Metadata1/" + tokenTemp + ".json");
//                                        Files.move(sourcePath1,destinationPath1,StandardCopyOption.REPLACE_EXISTING);
//                                        revealTokens.add(tokenTemp);
//                                    } catch (Exception e) {
//                                        log.error("Error in moving file of Token {} " , e );
//                                        log.error("Error in moving file of Token = " + tokenTemp );
//                                    }
//
//                            }
//                                response.setStatus(200);
//                                response.setMessage("Success");
//                                response.setResponse(revealTokens);
//                                return response;
//
//
//                            }
//                            case "0x0": {
//                                gettxreceiptstatus = false;
//
//                                log.error("gettxreceiptstatus -2");
//                                response.setStatus(302);
//                                response.setMessage("Transaction was not successful");
//                                return response;
//                            }
//                        }
//                    } else {
//                        gettxreceiptstatus = false;
//                        log.error("gettxreceiptstatus -1");
//                        response.setStatus(302);
//                        response.setMessage("Unable to validate transaction. Create a ticket on discord");
//                        return response;
//                    }
//                } else {
//                    gettxreceiptstatus = false;
//                    log.error("gettxreceiptstatus 0");
//                    response.setStatus(302);
//                    response.setMessage("Unable to validate txn. Create a ticket on discord");
//                    return response;
//                }
//            } else {
//                getTxnDetail = false;
//                log.error("getTxnDetail -1");
//                response.setStatus(302);
//                response.setMessage("Unable to validate transaction. Create a ticket on discord");
//            }
//        } else {
//            getTxnDetail = false;
//            log.error("getTxnDetail 0");
//            response.setStatus(302);
//            response.setMessage("Unable to validate transaction. Create a ticket on discord");
//
//        }
//
//        if (getTxnDetail == null || !getTxnDetail) {
//            return response;
//        }
//        return response;
//    }
//
//
//}
