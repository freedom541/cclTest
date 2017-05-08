package com.ccl.common.lang;

import org.apache.commons.lang.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by ccl on 17/4/25.
 * <p/>
 * 读取配置文件。
 * 首先读取classpath下的所有jar包里面conf文件夹下的所有属性文件；
 * 然后读取当前项目下的conf文件夹下的所有属性文件，同名的属性会覆盖；
 * 最后读取本地文件路径相对项目前一个路径下的conf文件夹下的所有属性文件，同名的属性会覆盖。
 */
public final class Configs {
    private static final String PROPERTIES_CLASSPATH_PATH = "conf/";

    private static final String PROPERTIES_FILE_POSTFIX = "yml";

    public static Properties config = new Properties();

    static {
        load();
    }

    public static void load() {
        try {
            config.clear();

            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            List<URL> resourceList = new ArrayList<>();
            URL resource;
            //加载所有jar包下的配置文件
            Enumeration<URL> resources = contextClassLoader.getResources(PROPERTIES_CLASSPATH_PATH);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                resourceList.add(url);
            }
            addResourceBundles(resourceList);

            //加载当前jar包下的配置文件
            resource = contextClassLoader.getResource(PROPERTIES_CLASSPATH_PATH);
            resourceList.clear();
            resourceList.add(resource);
            addResourceBundles(resourceList);

            //加载用户路径下的配置文件
            String relativelyPath = System.getProperty("user.dir");
            File path = new File(relativelyPath, PROPERTIES_CLASSPATH_PATH);
            resource = path.toURI().toURL();
            resourceList.clear();
            resourceList.add(resource);
            addResourceBundles(resourceList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void dumpFile(String fileName, String fileContent) {
        String relativelyPath = System.getProperty("user.dir");
        File path = new File(relativelyPath, PROPERTIES_CLASSPATH_PATH);
        OutputStreamWriter outputStreamWriter = null;
        try {
            if (!path.exists()) {
                path.mkdirs();
            }
            File file = new File(path, fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            outputStreamWriter = new OutputStreamWriter(new FileOutputStream(file), "utf-8");
            outputStreamWriter.write(fileContent);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != outputStreamWriter) {
                try {
                    outputStreamWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void dumpFile(String fileName) {
        String relativelyPath = System.getProperty("user.dir");
        File path = new File(relativelyPath, PROPERTIES_CLASSPATH_PATH);
        OutputStreamWriter outputStreamWriter = null;
        try {
            if (!path.exists()) {
                path.mkdirs();
            }
            File file = new File(path, fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            Yaml yaml=new Yaml();
            yaml.dump(config,new FileWriter(file));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != outputStreamWriter) {
                try {
                    outputStreamWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void addResourceBundles(List<URL> resources) {
        for (URL url : resources) {
            if (null == url) {
                continue;
            }
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
                                InputStream resource = classLoader.getResourceAsStream(entryPath);
                                if (null != resource) {
                                    Yaml yaml = new Yaml();
                                    Map<String, Object> hashMap = asMap(yaml.load(resource));
                                    Map<String, Object> result = new LinkedHashMap<String, Object>();
                                    buildFlattenedMap(result, hashMap, null);
                                    config.putAll(result);
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
                                    Yaml yaml = new Yaml();
                                    try {
                                        Map<String, Object> hashMap = asMap(yaml.load(new FileInputStream(file)));
                                        Map<String, Object> result = new LinkedHashMap<String, Object>();
                                        buildFlattenedMap(result, hashMap, null);
                                        config.putAll(result);
                                    } catch (FileNotFoundException e) {
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

    private static Map<String, Object> asMap(Object object) {
        // YAML can have numbers as keys
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        if (!(object instanceof Map)) {
            // A document can be a text literal
            result.put("document", object);
            return result;
        }

        Map<Object, Object> map = (Map<Object, Object>) object;
        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Map) {
                value = asMap(value);
            }
            Object key = entry.getKey();
            if (key instanceof CharSequence) {
                result.put(key.toString(), value);
            } else {
                // It has to be a map key in this case
                result.put("[" + key.toString() + "]", value);
            }
        }
        return result;
    }

    private static void buildFlattenedMap(Map<String, Object> result, Map<String, Object> source, String path) {
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String key = entry.getKey();
            if (StringUtils.isNotBlank(path)) {
                if (key.startsWith("[")) {
                    key = path + key;
                } else {
                    key = path + "." + key;
                }
            }
            Object value = entry.getValue();
            if (value instanceof String) {
                result.put(key, value);
            } else if (value instanceof Map) {
                // Need a compound key
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) value;
                result.put(key, value);
                buildFlattenedMap(result, map, key);
            } else if (value instanceof Collection) {
                result.put(key, value);
                // Need a compound key
                @SuppressWarnings("unchecked")
                Collection<Object> collection = (Collection<Object>) value;
                int count = 0;
                for (Object object : collection) {
                    buildFlattenedMap(result,
                            Collections.singletonMap("[" + (count++) + "]", object), key);
                }
            } else {
                result.put(key, value == null ? "" : value);
            }
        }
    }

    public static Boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public static Boolean getBoolean(String key, Boolean defaultValue) {
        Boolean value = (Boolean) getObject(key);
        if (null == value) {
            return defaultValue;
        }
        return value;
    }

    public static Integer getInt(String key) {
        return getInt(key, null);
    }

    public static Integer getInt(String key, Integer defaultValue) {
        Integer value = (Integer) getObject(key);
        if (null == value) {
            return defaultValue;
        }
        return value;
    }


    public static String getString(String key) {
        return getString(key, null);
    }

    public static String getString(String key, String defaultValue) {
        String value = (String) getObject(key);
        if (null == value) {
            return defaultValue;
        }
        return value;
    }

    public static Object getObject(String key) {
        return config.get(key);
    }

    public static Properties getStringValueConfig() {
        Properties properties = new Properties();
        Set<Object> keySet = config.keySet();
        for (Object key : keySet) {
            properties.put(key, String.valueOf(config.get(key)));
        }
        return properties;
    }

    public static void main(String[] args) {
        Properties pp = Configs.getStringValueConfig();
        System.out.println(pp);
    }
}

