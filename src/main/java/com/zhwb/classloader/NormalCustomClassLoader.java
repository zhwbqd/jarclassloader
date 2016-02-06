package com.zhwb.classloader;

import com.google.common.collect.Maps;
import com.google.common.io.Closeables;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.security.SecureClassLoader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * The type Custom class loader.
 *
 * @author zhangwenbin
 */
public class NormalCustomClassLoader extends SecureClassLoader {

    /**
     * The constant GLOBAL_CLASSLOADER.
     */
    public static final NormalCustomClassLoader GLOBAL_CLASSLOADER = new NormalCustomClassLoader(Thread.currentThread().getContextClassLoader());
    private static final String CLASS = ".class";
    private final Map<String, byte[]> classAsBytes;
    private final Map<String, Class> loadedClass;
    private final Map<String, byte[]> loadedResources;


    /**
     * Instantiates a new Custom class loader.
     *
     * @param parent the parent
     */
    public NormalCustomClassLoader(ClassLoader parent) {
        super(parent);
        this.loadedClass = Maps.newConcurrentMap();
        this.classAsBytes = Maps.newConcurrentMap();
        this.loadedResources = Maps.newConcurrentMap();
    }

    /**
     * 获取loader中的类信息
     *
     * @return key : className, value: classBytes
     */
    public synchronized Map<String, byte[]> getAddedResources() {
        Map<String, byte[]> maps = new HashMap<>(classAsBytes.size());
        for (Map.Entry<String, byte[]> entry : classAsBytes.entrySet()) {
            byte[] bytes = entry.getValue();
            maps.put(entry.getKey(), Arrays.copyOf(bytes, bytes.length));
        }
        return maps;
    }

    /**
     * 将流进行解析并保存
     *
     * @param jarStream the jar stream
     * @return the custom class loader
     */
    public synchronized NormalCustomClassLoader add(InputStream jarStream) {
        BufferedInputStream bis = null;
        JarInputStream jis = null;
        try {
            bis = new BufferedInputStream(jarStream);
            jis = new JarInputStream(bis);

            JarEntry jarEntry;
            while ((jarEntry = jis.getNextJarEntry()) != null) {
                String fileName = jarEntry.getName();
                if (jarEntry.isDirectory()) {
                    continue;
                }

                byte[] b = new byte[2048];
                ByteArrayOutputStream out = new ByteArrayOutputStream();

                int len;
                while ((len = jis.read(b)) > 0) {
                    out.write(b, 0, len);
                }
                if (fileName.endsWith(CLASS)) {
                    String className = fileName.substring(0, fileName.indexOf(CLASS)).replace("/", ".");
                    classAsBytes.put(className, out.toByteArray());
                } else {
                    loadedResources.put(fileName, out.toByteArray());
                }
                out.close();
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        } finally {
            Closeables.closeQuietly(bis);
            Closeables.closeQuietly(jis);
        }
        return this;
    }

    /**
     * Add jar bytes custom class loader.
     *
     * @param bytes the bytes
     * @return the custom class loader
     */
    public synchronized NormalCustomClassLoader addJarBytes(byte[] bytes) {
        add(new ByteArrayInputStream(bytes));
        return this;
    }

    /**
     * Add custom class loader.
     *
     * @param className the class name
     * @param bytes     the bytes
     * @return the custom class loader
     */
    public synchronized NormalCustomClassLoader add(String className, byte[] bytes) {
        classAsBytes.put(className, bytes);
        return this;
    }

    @Override
    protected synchronized Class<?> findClass(final String name) throws ClassNotFoundException {
        Class aClass = loadedClass.get(name);
        if (aClass != null) {
            return aClass;
        }
        byte[] classBytes = this.classAsBytes.remove(name); //拿一个删一个
        if (classBytes != null) {
            Class<?> defineClass = defineClass(name, classBytes, 0, classBytes.length);
            loadedClass.put(name, defineClass);
            return defineClass;
        }
        return super.findClass(name);
    }

    public URL getResource(String name) {
        URL resource = super.getResource(name);
        if (resource == null) {
            try {
                return new URL(null, "bytes:///" + name, new BytesHandler());
            } catch (MalformedURLException e) {
                return null;
            }
        }
        return resource;
    }

    /**
     * The type Bytes handler.
     */
    class BytesHandler extends URLStreamHandler {
        @Override
        protected URLConnection openConnection(URL u) throws IOException {
            return new ByteUrlConnection(u);
        }
    }

    /**
     * The type Byte url connection.
     */
    class ByteUrlConnection extends URLConnection {
        /**
         * Instantiates a new Byte url connection.
         *
         * @param url the url
         */
        public ByteUrlConnection(URL url) {
            super(url);
        }

        @Override
        public void connect() throws IOException {
        }

        @Override
        public InputStream getInputStream() throws IOException {
            String name = this.getURL().getPath().substring(1);
            byte[] buf = loadedResources.get(name);
            if (buf == null) {
                return null;
            }
            return new ByteArrayInputStream(buf);
        }
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        InputStream stream = super.getResourceAsStream(name);
        if (stream == null) {
            byte[] bytes = loadedResources.get(name);
            if (bytes == null) {
                return null;
            }
            return new ByteArrayInputStream(bytes);
        }
        return stream;
    }
}