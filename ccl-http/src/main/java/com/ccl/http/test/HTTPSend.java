package com.ccl.http.test;

/**
 * Created by ccl on 17/4/13.
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.List;

/**
 * Created by XdaTk on 2014/12/21.
 * <p/>
 * HTTP请求工具类
 */
public class HTTPSend {
    /**
     * 发送get请求
     *
     * @param url  请求地址
     * @param list 请求参数
     *
     * @return 请求结果
     *
     * @throws IOException
     */
    public static String sendGet(String url, List<HTTPParam> list) throws IOException {
        StringBuffer buffer = new StringBuffer(); //用来拼接参数
        StringBuffer result = new StringBuffer(); //用来接受返回值
        URL httpUrl = null; //HTTP URL类 用这个类来创建连接
        URLConnection connection = null; //创建的http连接
        BufferedReader bufferedReader = null; //接受连接受的参数
        //如果存在参数，我们才需要拼接参数 类似于 localhost/index.html?a=a&b=b
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                buffer.append(list.get(i).getKey()).append("=").append(URLEncoder.encode(list.get(i).getValue(), "utf-8"));
                //如果不是最后一个参数，不需要添加&
                if ((i + 1) < list.size()) {
                    buffer.append("&");
                }
            }
            url = url + "?" + buffer.toString();
        }
        //创建URL
        httpUrl = new URL(url);
        //建立连接
        connection = httpUrl.openConnection();
        connection.setRequestProperty("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        connection.setRequestProperty("connection", "keep-alive");
        connection.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:34.0) Gecko/20100101 Firefox/34.0");
        //connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        //connection.setRequestProperty("Cookie", "BDUSS=jREaTVNRmJMcFEtMVg3SmE3TURSRnFWSHBvNDE3b2xnamdKREFmdzM3Y3VXZjlZSVFBQUFBJCQAAAAAAAAAAAEAAABC9NMMbGlhbmc1NDE3NzEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC7M11guzNdYc; BAIDUID=4D2FAFE867DB247790DFD5FA4F6CE443:FG=1; PSTM=1492054007; BIDUPSID=4D9ACFF56535F5ED640B02ADFD614104; pgv_pvi=7972793344; pgv_si=s3383683072; BDRCVFR[r3VqGGrxDQ3]=mk3SLVN4HKm; H_PS_645EC=6238DVMnILCtEE3pVQzjWs0uxWK6zDNMOgGiFUvxTLRhtqF0ubqh8LvJxqweH8WCP83r; BDRCVFR[feWj1Vr5u3D]=I67x6TjHwwYf0; BD_CK_SAM=1; PSINO=5; BDORZ=B490B5EBF6F3CD402E515D22BCDA1598; BD_HOME=1; H_PS_PSSID=1464_21099_18560_17001; BD_UPN=123253; sugstore=1; __bsi=16365157246005867217_00_0_I_R_75_0303_C02F_N_I_I_0");
        connection.connect();
        //接受连接返回参数
        bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            result.append(line);
        }
        bufferedReader.close();
        return result.toString();
    }

    /**
     * 发送Post请求
     *
     * @param url  请求地址
     * @param list 请求参数
     *
     * @return 请求结果
     *
     * @throws IOException
     */
    public static String sendPost(String url, List<HTTPParam> list) throws IOException {
        StringBuffer buffer = new StringBuffer(); //用来拼接参数
        StringBuffer result = new StringBuffer(); //用来接受返回值
        URL httpUrl = null; //HTTP URL类 用这个类来创建连接
        URLConnection connection = null; //创建的http连接
        PrintWriter printWriter = null;
        BufferedReader bufferedReader; //接受连接受的参数
        //创建URL
        httpUrl = new URL(url);
        //建立连接
        connection = httpUrl.openConnection();
        connection.setRequestProperty("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        connection.setRequestProperty("connection", "keep-alive");
        connection.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:34.0) Gecko/20100101 Firefox/34.0");
        //connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        //connection.setRequestProperty("Cookie", "BDUSS=jREaTVNRmJMcFEtMVg3SmE3TURSRnFWSHBvNDE3b2xnamdKREFmdzM3Y3VXZjlZSVFBQUFBJCQAAAAAAAAAAAEAAABC9NMMbGlhbmc1NDE3NzEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAC7M11guzNdYc; BAIDUID=4D2FAFE867DB247790DFD5FA4F6CE443:FG=1; PSTM=1492054007; BIDUPSID=4D9ACFF56535F5ED640B02ADFD614104; pgv_pvi=7972793344; pgv_si=s3383683072; BDRCVFR[r3VqGGrxDQ3]=mk3SLVN4HKm; H_PS_645EC=6238DVMnILCtEE3pVQzjWs0uxWK6zDNMOgGiFUvxTLRhtqF0ubqh8LvJxqweH8WCP83r; BDRCVFR[feWj1Vr5u3D]=I67x6TjHwwYf0; BD_CK_SAM=1; PSINO=5; BDORZ=B490B5EBF6F3CD402E515D22BCDA1598; BD_HOME=1; H_PS_PSSID=1464_21099_18560_17001; BD_UPN=123253; sugstore=1; __bsi=16365157246005867217_00_0_I_R_75_0303_C02F_N_I_I_0");

        connection.setDoOutput(true);
        connection.setDoInput(true);
        printWriter = new PrintWriter(connection.getOutputStream());
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                buffer.append(list.get(i).getKey()).append("=").append(URLEncoder.encode(list.get(i).getValue(), "utf-8"));
                //如果不是最后一个参数，不需要添加&
                if ((i + 1) < list.size()) {
                    buffer.append("&");
                }
            }
        }
        printWriter.print(buffer.toString());
        printWriter.flush();
        connection.connect();
        //接受连接返回参数
        bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            result.append(line);
        }
        bufferedReader.close();
        return result.toString();
    }
}
