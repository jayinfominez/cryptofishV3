package com.infominez.cryptofish.controller;

import com.infominez.cryptofish.base.BaseResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.QueryParam;
import java.io.IOException;

@RestController
@RequestMapping("/my-secure-api")
@AllArgsConstructor
@Slf4j
public class Api {
    @GetMapping("/ ")
    public void afterPayment( @QueryParam("txnHash") String txnHash,HttpServletRequest request ) throws IOException{
        log.info("----------RUNNING----");
    }
}
