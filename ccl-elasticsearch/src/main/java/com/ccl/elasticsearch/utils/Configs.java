package com.ccl.elasticsearch.utils;

import org.apache.commons.configuration.*;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by dean on 2015/8/12.
 * <p/>
 * 读取配置文件。
 * 首先读取classpath下的所有jar包里面conf文件夹下的所有属性文件；
 * 然后读取当前项目下的conf文件夹下的所有属性文件，同名的属性会覆盖；
 * 最后读取本地文件路径相对项目前一个路径下的conf文件夹下的所有属性文件，同名的属性会覆盖。
 */
public final class Configs {
    private static final String PROPERTIES_CLASSPATH_PATH = "conf/";

    private static final String PROPERTIES_FILE_POSTFIX = "cfg";

    public static CompositeConfiguration config = new CompositeConfiguration();

    static {
        try {
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            List<URL> resourceList = new ArrayList<>();
            URL resource;
            //加载用户路径下的配置文件
            String relativelyPath = System.getProperty("user.dir");
            File path = new File(relativelyPath, PROPERTIES_CLASSPATH_PATH);
            resource = path.toURI().toURL();
            resourceList.clear();
            resourceList.add(resource);
            addResourceBundles(resourceList);
            //加载当前jar包下的配置文件
            resource = contextClassLoader.getResource(PROPERTIES_CLASSPATH_PATH);
            resourceList.clear();
            resourceList.add(resource);
            addResourceBundles(resourceList);
            //加载所有jar包下的配置文件
            Enumeration<URL> resources = contextClassLoader.getResources(PROPERTIES_CLASSPATH_PATH);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                resourceList.add(url);
            }
            addResourceBundles(resourceList);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void addResourceBundles(List<URL> resources) {
        for (URL url : resources) {
            if ("jar".equals(url.getProtocol())) {
                URLClassLoader classLoader = new URLClassLoader(new URL[]{url});
                JarFile jarFile = null;
                try {
                    URLConnection conn = url.openConnection();
                    JarURLConnection jarCon = (JarURLConnection) conn;
                    jarFile = jarCon.getJarFile();
                    for (Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements(); ) {
                        JarEntry entry = entries.nextElement();
                        String entryPath = entry.getName();
                        int lastIndexOf = entryPath.lastIndexOf(".");
                        if (lastIndexOf > 0) {
                            String postfix = entryPath.substring(lastIndexOf + 1);
                            if (entryPath.startsWith(PROPERTIES_CLASSPATH_PATH) && PROPERTIES_FILE_POSTFIX.equals(postfix)) {
                                try {
                                    URL resource = classLoader.getResource(entryPath);
                                    if (null != resource)
                                        config.addConfiguration(new PropertiesConfiguration(resource), true);
                                } catch (ConfigurationException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (null != jarFile) {
                        try {
                            jarFile.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                File path = new File(url.getPath());
                if (path.exists() && path.isDirectory()) {
                    File[] files = path.listFiles();
                    if (null != files && files.length > 0) {
                        for (File file : files) {
                            String fileName = file.getName();
                            int lastIndexOf = fileName.lastIndexOf(".");
                            if (lastIndexOf > 0) {
                                String postfix = fileName.substring(lastIndexOf + 1);
                                if (PROPERTIES_FILE_POSTFIX.equals(postfix)) {
                                    try {
                                        config.addConfiguration(new PropertiesConfiguration(new File(path, fileName)), true);
                                    } catch (ConfigurationException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    public static boolean getBoolean(String key) {
        return false;
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        return false;
    }

    public static Boolean getBoolean(String key, Boolean defaultValue) {
        return null;
    }


    public static double getDouble(String key) {
        return 0;
    }

    public static double getDouble(String key, double defaultValue) {
        return 0;
    }

    public static Double getDouble(String key, Double defaultValue) {
        return null;
    }

    public static float getFloat(String key) {
        return 0;
    }

    public static float getFloat(String key, float defaultValue) {
        return 0;
    }

    public static Float getFloat(String key, Float defaultValue) {
        return null;
    }

    public static int getInt(String key) {
        return config.getInt(key);
    }

    public static int getInt(String key, int defaultValue) {
        return config.getInt(key, defaultValue);
    }

    public static long getLong(String key) {
        return 0;
    }

    public static long getLong(String key, long defaultValue) {
        return 0;
    }

    public static short getShort(String key) {
        return 0;
    }

    public static short getShort(String key, short defaultValue) {
        return 0;
    }

    public static BigDecimal getBigDecimal(String key) {
        return null;
    }

    public static BigDecimal getBigDecimal(String key, BigDecimal defaultValue) {
        return null;
    }

    public static BigInteger getBigInteger(String key) {
        return null;
    }

    public static BigInteger getBigInteger(String key, BigInteger defaultValue) {
        return null;
    }

    public static String getString(String key) {
        return config.getString(key);
    }

    public static String getString(String key, String defaultValue) {
        return config.getString(key, defaultValue);
    }

    public static String[] getStringArray(String key) {
        return config.getStringArray(key);
    }

    public static List<Object> getList(String key) {
        return null;
    }

    public static List<Object> getList(String key, List<?> defaultValue) {
        return null;
    }
}
