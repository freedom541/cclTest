package com.vmware.test.netCode;

/**
 * Created by ccl on 17/2/17.
 */
import com.vmware.vim25.*;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.SOAPFaultException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Di
 * @功能描述 服务器的连接、认证、断开
 * @date 创建时间：2016年12月8日 下午3:37:18
 * @version 1.0
 */
public class MoniterWsInterface
{
    private static String url = "https://10.200.6.92:443/sdk/vimService";
    private static String userName = "administrator@vsphere.local";
    private static String password = "Wb1234==";

    private static final ManagedObjectReference SVC_INST_REF = new ManagedObjectReference();
    public static VimService vimService;
    public static VimPortType vimPort;

    public static ServiceContent serviceContent;
    private static final String SVC_INST_NAME = "ServiceInstance";
    private static Boolean isConnected = false;
    public static ManagedObjectReference perfManager;
    public static ManagedObjectReference propCollectorRef;

    private static class TrustAllTrustManager implements javax.net.ssl.TrustManager, javax.net.ssl.X509TrustManager
    {
        public java.security.cert.X509Certificate[] getAcceptedIssuers()
        {
            return null;
        }

        public boolean isServerTrusted(java.security.cert.X509Certificate[] certs)
        {
            return true;
        }

        public boolean isClientTrusted(java.security.cert.X509Certificate[] certs)
        {
            return true;
        }

        public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) throws java.security.cert.CertificateException
        {
            return;
        }

        public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) throws java.security.cert.CertificateException
        {
            return;
        }
    }

    private static void trustAllHttpsCertificates() throws Exception
    {
        javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
        javax.net.ssl.TrustManager tm = new TrustAllTrustManager();
        trustAllCerts[0] = tm;
        javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext.getInstance("SSL");
        javax.net.ssl.SSLSessionContext sslsc = sc.getServerSessionContext();
        sslsc.setSessionTimeout(0);
        sc.init(null, trustAllCerts, null);
        javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
    }

    /**
     * @功能描述 连接认证
     */
    public static void connect() throws Exception
    {
        HostnameVerifier hv = new HostnameVerifier()
        {
            public boolean verify(String urlHostName, SSLSession session)
            {
                return true;
            }
        };
        trustAllHttpsCertificates();

        HttpsURLConnection.setDefaultHostnameVerifier(hv);

        SVC_INST_REF.setType(SVC_INST_NAME);
        SVC_INST_REF.setValue(SVC_INST_NAME);

        vimService = new VimService();
        vimPort = vimService.getVimPort();
        Map<String, Object> ctxt = ((BindingProvider) vimPort).getRequestContext();

        ctxt.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url);
        ctxt.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);

        serviceContent = vimPort.retrieveServiceContent(SVC_INST_REF);
        vimPort.login(serviceContent.getSessionManager(), userName, password, null);
        isConnected = true;

        perfManager = serviceContent.getPerfManager();
        propCollectorRef = serviceContent.getPropertyCollector();

        System.out.println(serviceContent.getAbout().getFullName());
        System.out.println("Server type is " + serviceContent.getAbout().getApiType());
    }

    /**
     * @功能描述 断开连接
     * @return
     * @throws Exception
     */
    public static void disconnect() throws Exception
    {
        if (isConnected)
        {
            vimPort.logout(serviceContent.getSessionManager());
        }
        isConnected = false;
    }

    /**
     * @功能描述 打印错误信息
     * @param
     * @param sfe
     */
    public static void printSoapFaultException(SOAPFaultException sfe)
    {
        System.out.println("Soap fault: ");
        if (sfe.getFault().hasDetail())
        {
            System.out.println(sfe.getFault().getDetail().getFirstChild().getLocalName());
        }
        if (sfe.getFault().getFaultString() != null)
        {
            System.out.println("Message: " + sfe.getFault().getFaultString());
        }
    }

    /**
     * @功能描述 根据属性检索要查询的对象信息
     * @param listpfs 属性过滤器集合
     * @throws Exception
     */
    public static List<ObjectContent> retrievePropertiesAllObjects(List<PropertyFilterSpec> listpfs) throws Exception
    {
        RetrieveOptions propObjectRetrieveOpts = new RetrieveOptions();
        List<ObjectContent> listobjcontent = new ArrayList<ObjectContent>();
        try
        {
            RetrieveResult rslts = vimPort.retrievePropertiesEx(propCollectorRef, listpfs, propObjectRetrieveOpts);
            if (rslts != null && rslts.getObjects() != null && !rslts.getObjects().isEmpty())
            {
                listobjcontent.addAll(rslts.getObjects());
            }
            String token = null;
            if (rslts != null && rslts.getToken() != null)
            {
                token = rslts.getToken();
            }
            while (token != null && !token.isEmpty())
            {
                rslts = vimPort.continueRetrievePropertiesEx(propCollectorRef, token);
                token = null;
                if (rslts != null)
                {
                    token = rslts.getToken();
                    if (rslts.getObjects() != null && !rslts.getObjects().isEmpty())
                    {
                        listobjcontent.addAll(rslts.getObjects());
                    }
                }
            }
        }
        catch (SOAPFaultException sfe)
        {
            printSoapFaultException(sfe);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return listobjcontent;
    }

    /**
     * @ main测试方法
     * */
    public static void main(String[] args)
    {
        new MoniterWsInterface();
        try
        {
            connect();
        }
        catch (SOAPFaultException sfe)
        {
            printSoapFaultException(sfe);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                disconnect();
            }
            catch (SOAPFaultException sfe)
            {
                printSoapFaultException(sfe);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}