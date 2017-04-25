/*
 * ******************************************************
 * Copyright VMware, Inc. 2010-2012.  All Rights Reserved.
 * ******************************************************
 *
 * DISCLAIMER. THIS PROGRAM IS PROVIDED TO YOU "AS IS" WITHOUT
 * WARRANTIES OR CONDITIONS # OF ANY KIND, WHETHER ORAL OR WRITTEN,
 * EXPRESS OR IMPLIED. THE AUTHOR SPECIFICALLY # DISCLAIMS ANY IMPLIED
 * WARRANTIES OR CONDITIONS OF MERCHANTABILITY, SATISFACTORY # QUALITY,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.vmware.connection;

import com.vmware.vim25.*;
import org.joda.time.DateTime;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * This simple object shows how to set up a vSphere connection as it was done in vSphere 4.x and is provided
 * as a reference for anyone working with older vSphere servers that do not support modern SSO features.
 * It is intended as a utility class for use by Samples that will need to connect before they can do anything useful.
 * This is a light weight POJO that should be very easy to reuse later.
 * <p/>
 * Samples that need a connection open before they can do anything useful extend ConnectedVimServiceBase so that the
 * code in those samples can focus on demonstrating the feature at hand. The logic of most samples will not be
 * changed by the use of the BasicConnection or the SsoConnection.
 * </p>
 *
 * @see ConnectedVimServiceBase
 */
public class BasicConnection implements Connection {
    private VimService vimService;
    private VimPortType vimPort;
    private ServiceContent serviceContent;
    private UserSession userSession;
    private ManagedObjectReference svcInstRef;

    private URL url;
    private String username;
    private String password = ""; // default password is empty since on rare occasion passwords are not set
    @SuppressWarnings("rawtypes")
	private Map headers;

    public void setUrl(String url) {
        try {
            this.url = new URL(url);
        } catch (MalformedURLException e) {
            throw new ConnectionMalformedUrlException("malformed URL argument: '" + url + "'", e);
        }
    }

    public String getUrl() {
        return url.toString();
    }

    public String getHost() {
        return url.getHost();
    }

    public Integer getPort() {
        int port = url.getPort();
        return port;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return this.password;
    }

    public VimService getVimService() {
        return vimService;
    }

    public VimPortType getVimPort() {
        return vimPort;
    }

    public ServiceContent getServiceContent() {
        return serviceContent;
    }

    public UserSession getUserSession() {
        return userSession;
    }

    public String getServiceInstanceName() {
        return "ServiceInstance"; // Theoretically this could change but it never does in these samples.
    }

    @SuppressWarnings("rawtypes")
	public Map getHeaders() {
        return headers;
    }

    public ManagedObjectReference getServiceInstanceReference() {
        if (svcInstRef == null) {
            ManagedObjectReference ref = new ManagedObjectReference();
            ref.setType(this.getServiceInstanceName());
            ref.setValue(this.getServiceInstanceName());
            svcInstRef = ref;
        }
        return svcInstRef;
    }

    public Connection connect() {
        if (!isConnected()) {
            try {
                _connect();
            } catch (Exception e) {
                Throwable cause = (e.getCause() != null)?e.getCause():e;
                throw new BasicConnectionException(
                        "failed to connect: " + e.getMessage() + " : " + cause.getMessage(),
                        cause);
            }
        }
        return this;
    }

    private static final String SVC_INST_NAME = "ServiceInstance";
    private static final ManagedObjectReference SVC_INST_REF = new ManagedObjectReference();

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


    @SuppressWarnings("rawtypes")
	private void _connect() throws Exception {

        HostnameVerifier hv = new HostnameVerifier()
        {
            @Override
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
        Map<String, Object> ctxt =
                ((BindingProvider) vimPort).getRequestContext();

        ctxt.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url.toString());
        ctxt.put(BindingProvider.SESSION_MAINTAIN_PROPERTY, true);

        serviceContent = vimPort.retrieveServiceContent(this.getServiceInstanceReference());
        headers =
                (Map) ((BindingProvider) vimPort).getResponseContext().get(
                        MessageContext.HTTP_RESPONSE_HEADERS);

        userSession = vimPort.login(
                serviceContent.getSessionManager(),
                username,
                password,
                null);
        XMLGregorianCalendar calendar = userSession.getLastActiveTime();
        DateTime time = new DateTime();
        calendar.setYear(time.getYear());
        calendar.setMonth(time.getMonthOfYear());
        calendar.setDay(time.getDayOfMonth());
        calendar.setTime(time.getHourOfDay(),time.getMinuteOfHour(),time.getSecondOfMinute());
        userSession.setLastActiveTime(calendar);

        System.out.println(serviceContent.getAbout().getFullName());
        System.out.println("Server type is " + serviceContent.getAbout().getApiType());
        String acquireCloneTicket = vimPort.acquireCloneTicket(serviceContent.getSessionManager());
        System.out.println("acquireCloneTicket: " + acquireCloneTicket);
        String[] str =acquireCloneTicket.split("--tp-");
        System.out.println(str[1]);
        System.out.println(str[1].replaceAll("-",":"));

    }

    public boolean isConnected() {
        if (userSession == null) {
            return false;
        }
        long startTime = userSession.getLastActiveTime().toGregorianCalendar().getTime().getTime();
        startTime = startTime + 30 * 60 * 1000;
        // 30 minutes in milliseconds = 30 minutes * 60 seconds * 1000 milliseconds
        return new DateTime().getMillis() < startTime;
    }

    public Connection disconnect() {
        if (this.isConnected()) {
            try {
                vimPort.logout(serviceContent.getSessionManager());
            } catch (Exception e) {
                Throwable cause = e.getCause();
                throw new BasicConnectionException(
                        "failed to disconnect properly: " + e.getMessage() + " : " + cause.getMessage(),
                        cause
                );
            } finally {
                // A connection is very memory intensive, I'm helping the garbage collector here
                userSession = null;
                serviceContent = null;
                vimPort = null;
                vimService = null;
            }
        }
        return this;
    }

    @Override
    public URL getURL() {
        return this.url;
    }

    private class BasicConnectionException extends ConnectionException {
        private static final long serialVersionUID = 1L;
        public BasicConnectionException(String s, Throwable t) {
            super(s, t);
        }
    }
}
