//package com.example.demo.controller;
//
//
//
//import com.example.demo.base.BaseResponse;
//import com.example.demo.utils.AppConfiguration;
//import jakarta.servlet.http.HttpServletRequest;
//import lombok.AllArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.jsoup.Connection;
//import org.jsoup.HttpStatusException;
//import org.jsoup.Jsoup;
//import org.springframework.http.MediaType;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import javax.ws.rs.QueryParam;
//import java.io.IOException;
//import java.math.BigDecimal;
//import java.math.BigInteger;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.nio.file.StandardCopyOption;
//import java.util.Date;
//
//@RestController
//@RequestMapping("/cryptosh")
//@AllArgsConstructor
//@Slf4j
//public class Cryptofish {
//
//    private final AppConfiguration appConfiguration;
//
//    @GetMapping("/afterPayment")
//    public BaseResponse afterPayment( @QueryParam("txnHash") String txnHash,HttpServletRequest request ){
//        log.info(" :- afterPayment()",txnHash);
//        BaseResponse response = new BaseResponse();
//        Date date = new Date();
//
//        Boolean gettxreceiptstatus = null;
//        Boolean getTxnDetail = null;
//        BigDecimal eth = null;
//
//        String apiUrl = appConfiguration.getProperty("scrap.apiUrl");
//        String apiKey = appConfiguration.getProperty("scrap.apiKey");
//        gettxreceiptstatus = getTxRecieptStatus(txnHash,response,apiUrl,apiKey);
//        if (gettxreceiptstatus == null || !gettxreceiptstatus) {
//            return response;
//        }
//        //apiUrl = apiUrl + "proxy&action=eth_getTransactionByHash&txhash="+txnHash+"&apikey=" + apiKey;
//        String apiUrl2 = "https://api.etherscan.io/api?module=proxy&action=eth_getTransactionByHash&txhash=0x5f30f7528385b53415666970c796f8aa454e6b8596d1e48dc5cf91f54ff7cc00&apikey=U5DWT2J32KCVI8FYTVEDGPESV75M7V98TM";
//
//        Connection.Response connResponse = null;
//        String tempResult = null;
//        try {
//            connResponse = Jsoup.connect(apiUrl2).method(Connection.Method.GET).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36")
//
//                    .ignoreContentType(true).followRedirects(true).execute();
//            Integer statusCode = connResponse.statusCode();
//            log.info("statusCode : {}",statusCode);
//            tempResult = connResponse.body();
//            log.info("call getTxnDetail : txnHash : {}   url : {}, Headers : {}, response : {}",txnHash,apiUrl,tempResult);
//
//            org.json.JSONObject result = new org.json.JSONObject(tempResult);
//            if (result.has("result") && result.optJSONObject("result") != null && !result.optJSONObject("result").isEmpty()) {
//                if (result.optJSONObject("result").optString("value") != null && result.optJSONObject("result").optString("hash") != null) {
//                    if (!txnHash.equalsIgnoreCase(result.optJSONObject("result").optString("hash"))) {
//                        getTxnDetail = false;
//                        log.error("getTxnDetail -2");
//                        response.setStatus(302);
//                        response.setMessage("Unable to validate txn");
//                    }
//                    log.error("getTxnDetail success");
//                    getTxnDetail = true;
//
//                    String apiUrl3 = "https://api.etherscan.io/api?module=proxy&action=eth_getTransactionReceipt&txhash=0x5f30f7528385b53415666970c796f8aa454e6b8596d1e48dc5cf91f54ff7cc00&apikey=U5DWT2J32KCVI8FYTVEDGPESV75M7V98TM";
//                    connResponse = Jsoup.connect(apiUrl3).method(Connection.Method.GET).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36").header("Content-Type",MediaType.APPLICATION_JSON_VALUE).ignoreContentType(true).followRedirects(true).execute();
//
//                    tempResult = connResponse.body();
//                    log.info("call gettxreceiptstatus : txnHash : {}   url : {}, Headers : {}, response : {}",txnHash,apiUrl,tempResult);
//                    org.json.JSONObject result3 = new org.json.JSONObject(tempResult);
//                    if (result3.has("result") && result3.optJSONObject("result") != null) {
//                        if (result3.optJSONObject("result").optString("status") != null) {
//                            switch (result3.optJSONObject("result").optString("status")) {
//                                case "": {
//                                    log.error("gettxreceiptstatus -3");
//                                    gettxreceiptstatus = false;
//                                    response.setStatus(302);
//                                    response.setMessage("Transaction is not completed yet. Please try after transaction settlement.");
//                                    return response;
//                                }
//                                case "0x1": {
//                                    gettxreceiptstatus = true;
//                                    log.info("gettxreceiptstatus success ");
//                                    org.json.JSONArray logs = result3.optJSONObject("result").getJSONArray("logs");
//                                    for (int i = 0; i < logs.length(); i++) {
//                                        String token = logs.getJSONObject(i).getJSONArray("topics").getString(3);
//                                        log.info("token : " + token);
//                                        BigInteger tokenTemp = org.web3j.utils.Numeric.toBigInt(org.web3j.utils.Numeric.hexStringToByteArray(token));
//                                        log.info("tokenTemp : " + tokenTemp);
//                                    }
////                                    Path sourcePath = Paths.get("D:\\infominezTask\\NFT\\source\\Speciesw-BGs\\" + "401.gif");
////                                    Path destinationPath = Paths.get("D:\\infominezTask\\NFT\\destination\\" + finalImageUrl);
////                                    Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
//                                    // for change path of image  and json.
//                                    Path sourcePath = Paths.get("D:\\cryptofish_items\\for_example\\source_image\\" + "jay.gif");
//                                    Path destinationPath = Paths.get("D:\\cryptofish_items\\for_example\\destination_image\\" + "jay.gif");
//                                    Files.copy(sourcePath,destinationPath,StandardCopyOption.REPLACE_EXISTING);
//
//                                    Path sourcePath1 = Paths.get("D:\\cryptofish_items\\for_example\\source_json\\" + "jay.json");
//                                    Path destinationPath1 = Paths.get("D:\\cryptofish_items\\for_example\\destination_json\\" + "jay.json");
//                                    Files.copy(sourcePath1,destinationPath1,StandardCopyOption.REPLACE_EXISTING);
//
//
//                                    response.setStatus(200);
//                                    response.setMessage("Success");
//                                    return response;
//
//
//                                }
//                                case "0x0": {
//                                    gettxreceiptstatus = false;
//
//                                    log.error("gettxreceiptstatus -2");
//                                    // aviusAnimaeTokenRepository.saveAll(aviusAnimaeTokenList);
//
//
//                                    response.setStatus(302);
//                                    response.setMessage("Transaction was not successful");
//                                    return response;
//
//
//                                }
//                            }
//                        } else {
//                            gettxreceiptstatus = false;
//
//                            log.error("gettxreceiptstatus -1");
//                            response.setStatus(302);
//                            response.setMessage("Unable to validate transaction. Create a ticket on discord");
//                            return response;
//
//
//                        }
//                    } else {
//                        gettxreceiptstatus = false;
//
//                        log.error("gettxreceiptstatus 0");
//                        response.setStatus(302);
//                        response.setMessage("Unable to validate txn. Create a ticket on discord");
//                        return response;
//
//
//                    }
//
//
//                } else {
//                    getTxnDetail = false;
//
//                    log.error("getTxnDetail -1");
//                    response.setStatus(302);
//                    response.setMessage("Unable to validate transaction. Create a ticket on discord");
//
//                }
//            } else {
//                getTxnDetail = false;
//
//                log.error("getTxnDetail 0");
//                response.setStatus(302);
//                response.setMessage("Unable to validate transaction. Create a ticket on discord");
//
//            }
//
//        } catch (HttpStatusException e) {
//            log.error("error Response : {}, :: {}",e.getStatusCode(),e.getMessage());
//            log.error("getTxnDetail 2");
//            getTxnDetail = false;
//
//            response.setStatus(302);
//            response.setMessage("Unable to validate transaction. Create a ticket on discord");
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            getTxnDetail = false;
//
//            log.error("getTxnDetail 3");
//            response.setStatus(302);
//            response.setMessage("Unable to validate transaction. Create a ticket on discord");
//
//        }
//        if (getTxnDetail == null || !getTxnDetail) {
//            return response;
//        }
//
//        return response;
//    }
//
//    private Boolean getTxRecieptStatus( String txnHash,BaseResponse response,String apiUrl,String apiKey ){
//        //  apiUrl = apiUrl + "transaction&action=gettxreceiptstatus&txhash="+txnHash+"&apikey=" + apiKey;
//        //     apiUrl = "https://api-rinkeby.etherscan.io/api?module=" + "transaction&action=gettxreceiptstatus&txhash="+txnHash+"&apikey=" + apiKey;
////        apiUrl =  "https://etherscan.io/tx/"+txnHash;
//        // apiUrl = "https://api.etherscan.io/api?module=proxy&action=eth_getTransactionByHash&txhash=" + txnHash + "&apikey=U5DWT2J32KCVI8FYTVEDGPESV75M7V98TM";
//        apiUrl = "https://api.etherscan.io/api?module=transaction&action=gettxreceiptstatus&txhash=" + txnHash + "&apikey=U5DWT2J32KCVI8FYTVEDGPESV75M7V98TM";
//        Boolean gettxreceiptstatus = null;
//        Connection.Response connResponse = null;
//        String tempResult = null;
//        try {
//            System.out.println("Url :- " + apiUrl);
//            connResponse = Jsoup.connect(apiUrl).method(Connection.Method.GET).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36").header("Content-Type",MediaType.APPLICATION_JSON_VALUE).ignoreContentType(true).followRedirects(true).execute();
//            Integer statusCode = connResponse.statusCode();
//            log.info("statusCode : {}",statusCode);
//            tempResult = connResponse.body();
//            log.info("call gettxreceiptstatus : txnHash : {}   url : {}, Headers : {}, response : {}",txnHash,apiUrl,tempResult);
//
//            org.json.JSONObject result = new org.json.JSONObject(tempResult);
//            if (result.has("status") && result.optString("status").equals("1")) {
//                if (result.has("result") && result.optJSONObject("result") != null) {
//                    if (result.optJSONObject("result").optString("status") != null) {
//                        switch (result.optJSONObject("result").optString("status")) {
//                            case "": {
//                                log.error("gettxreceiptstatus -3");
//                                gettxreceiptstatus = false;
//                                response.setStatus(302);
//                                response.setMessage("Transaction is not completed yet. Please try after transaction settlement.");
//                                return gettxreceiptstatus;
//                            }
//                            case "1": {
//                                gettxreceiptstatus = true;
//
//                                log.error("gettxreceiptstatus success ");
//                                return gettxreceiptstatus;
//
//                            }
//                            case "0": {
//                                gettxreceiptstatus = false;
//
//                                log.error("gettxreceiptstatus -2");
//                                response.setStatus(302);
//                                response.setMessage("Transaction was not successful");
//                                return gettxreceiptstatus;
//
//                            }
//                        }
//                    } else {
//                        gettxreceiptstatus = false;
//
//                        log.error("gettxreceiptstatus -1");
//                        response.setStatus(302);
//                        response.setMessage("Unable to validate transaction. Create a ticket on discord");
//                        return gettxreceiptstatus;
//
//                    }
//                } else {
//                    gettxreceiptstatus = false;
//
//                    log.error("gettxreceiptstatus 0");
//                    response.setStatus(302);
//                    response.setMessage("Unable to validate txn. Create a ticket on discord");
//                    return gettxreceiptstatus;
//
//
//                }
//            } else {
//                gettxreceiptstatus = false;
//
//                log.error("gettxreceiptstatus 1");
//                response.setStatus(302);
//                response.setMessage("Unable to validate txn. Create a ticket on discord");
//                return gettxreceiptstatus;
//
//            }
//        } catch (HttpStatusException e) {
//            log.error("error Response : {}, :: {}",e.getStatusCode(),e.getMessage());
//            log.error("gettxreceiptstatus 2");
//            gettxreceiptstatus = false;
//
//            response.setStatus(302);
//            response.setMessage("Unable to validate txn. Create a ticket on discord");
//            return gettxreceiptstatus;
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            gettxreceiptstatus = false;
//
//            log.error("gettxreceiptstatus 3");
//            response.setStatus(302);
//            response.setMessage("Unable to validate txn. Create a ticket on discord");
//            return gettxreceiptstatus;
//
//        }
//        return gettxreceiptstatus;
//
//    }
//
//
//}
