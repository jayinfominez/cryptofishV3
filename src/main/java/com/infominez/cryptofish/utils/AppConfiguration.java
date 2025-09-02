package com.infominez.cryptofish.utils;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

@Configuration
@PropertySource("/config.properties")
public class AppConfiguration {
    @Autowired
    private Environment env;

    public String getProperty(String propertyName) {
        return env.getProperty(propertyName);
    }
}
