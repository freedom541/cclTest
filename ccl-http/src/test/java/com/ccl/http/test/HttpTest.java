package com.ccl.http.test;

import org.junit.Test;

import java.io.IOException;
import java.util.*;

/**
 * Created by ccl on 17/4/13.
 */
public class HttpTest {
    @Test
    public void test(){
        String url = "https://www.baidu.com/home/pcweb/data/mancardhtml";
        List<HTTPParam> params = new ArrayList<>();
        String paramStr = "id=23&isPull=&indextype=manht&_req_seqid=0x8c2e0ffd000213ac&asyn=1&t=1492074165349&sid=1464_21099_18560_17001";
        Map<String,String> param = new HashMap<>();
        param.put("","");
        try {
            String sponse = HTTPSend.sendGet(url,getParam(paramStr));
            System.out.println(sponse);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<HTTPParam> getParam(Map<String,String> maps){
        List<HTTPParam> params = new ArrayList<>();
        if (maps != null && maps.size() > 0){
            for (Map.Entry<String,String> entry : maps.entrySet()){
                HTTPParam param = new HTTPParam();
                param.setKey(entry.getKey());
                param.setValue(entry.getValue());
                params.add(param);
            }
        }
        return params;
    }
    private List<HTTPParam> getParam(String paramStr){
        List<HTTPParam> params = new ArrayList<>();
        if (Objects.nonNull(paramStr)){
            String[] pp = paramStr.split("&");
            if (pp.length > 0){
                for (String p : pp){
                    String[] kv = p.split("=");
                    HTTPParam param = new HTTPParam();
                    param.setKey(kv[0]);
                    if (kv.length > 1)
                        param.setValue(kv[1]);
                    params.add(param);
                }
            }
        }
        return params;
    }
}
