package com.imudges.web.CloudAddressBook.Util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 读取配置文件类
 * */

public class ConfigReader {
    public String read(String param){
        Properties properties = new Properties();
        InputStream in = this.getClass().getResourceAsStream("/config.properties");

        try {
            //装载配置文件内的key-value对
            properties.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties.getProperty(param,"null");
    }

    public int readInt(String param){
        Properties properties = new Properties();
        InputStream in = this.getClass().getResourceAsStream("/config.properties");

        try {
            //装载配置文件内的key-value对
            properties.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            return Integer.parseInt(properties.getProperty(param,"null"));
        } catch (Exception e){
            return -1;
        }
    }
}
