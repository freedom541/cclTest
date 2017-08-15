package com.ccl.jersey.codegen;

import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by ccl on 2015/8/6.
 * <p>
 * 国际化支持。默认资源文件放在类根路径下的i18n目录下，资源文件命名为messages
 */
public final class Messages {
    private static Map<Locale, AggregateBundle> resourceBundles;

    private static final String PROPERTIES_PATH = "i18n/";

    private static final String PROPERTIES_FILE_NAME = "messages";
    private static final String PROPERTIES_FILE_POSTFIX = "properties";

    static {
        resourceBundles = new HashMap<>();
        try {
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> resources = contextClassLoader.getResources(PROPERTIES_PATH);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                addURL(url);
            }

            URL url = contextClassLoader.getResource(PROPERTIES_PATH);
            addURL(url);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void addURL(URL url) {
        if ("jar".equals(url.getProtocol())) {
            JarFile jarFile = null;
            try {
                URLConnection conn = url.openConnection();
                JarURLConnection jarCon = (JarURLConnection) conn;
                jarFile = jarCon.getJarFile();
                for (Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements(); ) {
                    JarEntry entry = entries.nextElement();
                    String file = entry.getName();
                    int lastIndexOf = file.lastIndexOf(".");
                    if (lastIndexOf > 0) {
                        String postfix = file.substring(lastIndexOf + 1);
                        if (file.startsWith(PROPERTIES_PATH) && PROPERTIES_FILE_POSTFIX.equals(postfix)) {
                            file = file.substring(5, file.length() - 11);
                            if (file.length() > 8) {
                                String lang = file.substring(9);
                                Locale locale = getLocale(lang);
                                addResource(url, locale);
                            } else {
                                addResource(url, Locale.ROOT);
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
            File path = new File(url.getFile());
            String[] list = path.list();
            if (null != list) {
                for (String file : list) {
                    file = file.substring(0, file.length() - 11);
                    if (file.length() > 8) {
                        String lang = file.substring(9);
                        Locale locale = getLocale(lang);
                        addResource(url, locale);
                    } else {
                        addResource(url, Locale.ROOT);
                    }
                }
            }
        }
    }

    private static void addResource(URL url, Locale locale) {
        AggregateBundle aggregateBundle = resourceBundles.get(locale);
        if (null == aggregateBundle) {
            aggregateBundle = new AggregateBundle();
            resourceBundles.put(locale, aggregateBundle);
        }
        URLClassLoader classLoader = new URLClassLoader(new URL[]{url});
        ResourceBundle resourceBundle;
        try {
            resourceBundle = ResourceBundle.getBundle(PROPERTIES_FILE_NAME, locale, classLoader);
        } catch (MissingResourceException e) {
            resourceBundle = ResourceBundle.getBundle(PROPERTIES_FILE_NAME, Locale.ROOT, classLoader);
        }
        aggregateBundle.addBundle(resourceBundle);
    }

    public static void addResource(Map<String, Object> data, Locale locale) {
        AggregateBundle aggregateBundle = resourceBundles.get(locale);
        if (null == aggregateBundle) {
            aggregateBundle = new AggregateBundle();
            resourceBundles.put(locale, aggregateBundle);
        }
        aggregateBundle.addData(data);
    }

    public static Locale getLocale(String lang) {
        if (StringUtils.isNotBlank(lang)) {
            String[] split = lang.split("_");
            if (split.length > 1) {
                return new Locale(split[0], split[1]);
            } else {
                return new Locale(split[0]);
            }
        }
        return Locale.getDefault();
    }

    public static String getMessage(String key, Object... args) {
        String lang = RequestHeaderUtils.getLang();
        Locale locale = getLocale(lang);
        return getMessage(key, locale, args);
    }

    public static String getMessage(String key, Locale locale, Object... args) {
        try {
            AggregateBundle resourceBundle = resourceBundles.get(locale);
            if (null == resourceBundle) {
                resourceBundle = resourceBundles.get(Locale.ROOT);
            }
            if (null == resourceBundle) {
                return null;
            }
            String string = resourceBundle.getString(key);
            return MessageFormat.format(string, args);
        } catch (Exception e) {
            e.printStackTrace();
            return key;
        }
    }


    /**
     * A {@link ResourceBundle} whose content is aggregated from multiple source bundles.
     * <p>
     * This class is package-private for the sake of testability.
     */
    public static class AggregateBundle extends ResourceBundle {
        private Map<String, Object> contents = new HashMap<>();

        /**
         * Creates a new AggregateBundle.
         */
        public AggregateBundle() {
        }

        @Override
        public Enumeration<String> getKeys() {
            return new IteratorEnumeration<>(contents.keySet().iterator());
        }

        @Override
        protected Object handleGetObject(String key) {
            return contents.get(key);
        }

        public void addBundle(ResourceBundle bundle) {
            Enumeration<String> keys = bundle.getKeys();
            while (keys.hasMoreElements()) {
                String oneKey = keys.nextElement();
                contents.put(oneKey, bundle.getObject(oneKey));
            }
        }

        public void addData(Map<String, Object> data) {
            contents.putAll(data);
        }


    }

    private static class IteratorEnumeration<T> implements Enumeration<T> {

        private Iterator<T> source;

        /**
         * Creates a new IterationEnumeration.
         *
         * @param source The source iterator. Must not be null.
         */
        public IteratorEnumeration(Iterator<T> source) {

            if (source == null) {
                throw new IllegalArgumentException("Source must not be null");
            }

            this.source = source;
        }

        public boolean hasMoreElements() {
            return source.hasNext();
        }

        public T nextElement() {
            return source.next();
        }
    }
}
