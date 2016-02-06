package com.zhwb.classloader;

import com.google.common.collect.Maps;
import com.google.common.io.Closeables;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.SecureClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * The type Custom class loader.
 *
 * @author zhangwenbin
 */
public class CustomClassLoaderV1 extends SecureClassLoader {

    /**
     * The constant GLOBAL_CLASSLOADER.
     */
    public static final CustomClassLoaderV1 GLOBAL_CLASSLOADER = new CustomClassLoaderV1(Thread.currentThread().getContextClassLoader());
    private static final String CLASS = ".class";

    /**
     * Instantiates a new Custom class loader.
     *
     * @param parent the parent
     */
    public CustomClassLoaderV1(ClassLoader parent) {
        super(parent);
    }

    /**
     * 将流进行解析并保存
     *
     * @param jarStream the jar stream
     * @return the custom class loader
     */
    public synchronized CustomClassLoaderV1 loadJarStream(InputStream jarStream) {
        BufferedInputStream bis = null;
        JarInputStream jis = null;
        Map<String, byte[]> classAsBytes = Maps.newHashMap();
        Map<String, byte[]> loadedResources = Maps.newHashMap();
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
        return createInnerLoader(classAsBytes, loadedResources);
    }

    private InnerLoader createInnerLoader(final Map<String, byte[]> classAsBytes, final Map<String, byte[]> loadedResources) {
        return AccessController.doPrivileged(new PrivilegedAction<InnerLoader>() {
            public InnerLoader run() {
                return new InnerLoader(CustomClassLoaderV1.this, classAsBytes, loadedResources);
            }
        });
    }

    /**
     * Add jar bytes custom class loader.
     *
     * @param bytes the bytes
     * @return the custom class loader
     */
    public synchronized CustomClassLoaderV1 loadJarBytes(byte[] bytes) {
        return loadJarStream(new ByteArrayInputStream(bytes));
    }

    /**
     * Add class and its bytes into classloader
     *
     * @param className the class name
     * @param bytes     the bytes
     * @return the custom class loader
     */
    public synchronized CustomClassLoaderV1 loadSingleClassBytes(String className, byte[] bytes) {
        Map<String, byte[]> classAsBytes = new HashMap<>(1);
        classAsBytes.put(className, bytes);
        return createInnerLoader(classAsBytes, null);
    }

    public static class InnerLoader extends CustomClassLoaderV1 {
        private final CustomClassLoaderV1 delegate;
        private Map<String, byte[]> classAsBytes;
        private Map<String, byte[]> loadedResources;

        public InnerLoader(CustomClassLoaderV1 delegate, Map<String, byte[]> classAsBytes, Map<String, byte[]> loadedResources) {
            super(delegate);
            this.delegate = delegate;
            this.classAsBytes = classAsBytes;
            this.loadedResources = loadedResources;
        }

        @Override
        protected synchronized Class<?> findClass(String name) throws ClassNotFoundException {
            byte[] classBytes = classAsBytes.get(name);
            if (classBytes != null) {
                Class<?> defineClass = defineClass(name, classBytes, 0, classBytes.length);
                if (defineClass != null) {
                    return defineClass;
                }
            }
            return delegate.findClass(name);
        }

        @Override
        public InputStream getResourceAsStream(String name) {
            InputStream stream = super.getResourceAsStream(name);
            if (stream == null) {
                byte[] bytes = this.loadedResources.get(name);
                if (bytes == null) {
                    return null;
                }
                return new ByteArrayInputStream(bytes);
            }
            return stream;
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
    }

}