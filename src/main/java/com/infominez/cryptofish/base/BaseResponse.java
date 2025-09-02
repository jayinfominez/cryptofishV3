package com.infominez.cryptofish.base;

import lombok.Data;
import lombok.ToString;

@ToString
@Data
public class BaseResponse {
    private Integer status;
    private String message;
    private Object response;


    public BaseResponse setInternalServerError(){
        this.status = 500;
        this.message = "Internal Server Error";
        this.response = null;
        return this;
    }

    public BaseResponse setUnauthorized(){
        this.status = 403;
        this.message = "Unauthorised Access";
        this.response = null;
        return this;
    }
}
