/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.dependent;

import com.github.dr.rwserver.data.global.Data;
import com.github.dr.rwserver.struct.Seq;
import com.github.dr.rwserver.util.file.FileUtil;
import com.github.dr.rwserver.util.log.Log;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarFile;

import static com.github.dr.rwserver.net.HttpRequestOkHttp.downUrl;
import static com.github.dr.rwserver.util.IsUtil.notIsBlank;

/**
 * @author Dr
 */
public class LibraryManager {

	private static Instrumentation inst = null;
	private static final Seq<File> loadEnd = new Seq<>();
    private final Seq<File> load = new Seq<>();

    private final String URL;
    private final String PATH;
    private final Seq<ImportData> dependencies = new Seq<>();


    /** JRE将在启动main()之前调用方法 */
    public static void agentmain(final String a, final Instrumentation inst) {
        LibraryManager.inst = inst;
    }

    /**
     * 使用AiLiYun下载
     * @param path 保存路径
     */
    public LibraryManager(String path) {
        URL = "https://maven.aliyun.com/nexus/content/groups/public";
        this.PATH = path;
    }


    public LibraryManager(boolean china,String path) {
        if (china) {
            URL = "https://maven.aliyun.com/nexus/content/groups/public";
        } else {
            URL = "https://repo1.maven.org/maven2";
        }
        this.PATH = FileUtil.getFolder(path).getPath();
    }

    /**
     * 加载并添加到URLClassLoader
     */
    public final void loadToClassLoader() {
        Log.clog(Data.localeUtil.getinput("server.load.jar"));
        load();
        try {
            if (ClassLoader.getSystemClassLoader() instanceof URLClassLoader) {
                Method f = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                f.setAccessible(true);
                load.each(e -> {
                    try {
                        f.invoke(ClassLoader.getSystemClassLoader(), e.toURI().toURL());
                        loadEnd.add(e);
                        Log.info("Load Lib Jar",e.getName());
                    } catch (Exception classLoad) {
                        Log.error("Jar 1.8 Load",classLoad);
                    }
                });
            } else {
                load.each(e -> {
                    try {
                        inst.appendToSystemClassLoaderSearch(new JarFile(e));
                        loadEnd.add(e);
                        Log.info("Load Lib Jar",e.getName());
                    } catch (Exception classLoad) {
                        Log.error("Jar 1.8+ Load",classLoad);
                    }
                });
            }
        } catch (Exception e) {
            Log.fatal("Class Load",e);
            Log.clog(Data.localeUtil.getinput("load.err"));
        }
    }

    private Seq<File> load() {
        dependencies.each(e -> {
            if (e.downFile.exists()) {
                load.add(e.downFile);
            } else {
                if (notIsBlank(e.downUrl)) {
                    if (downUrl(e.downUrl,e.downFile)) {
                        load.add(e.downFile);
                    }
                } else {
                    load.add(e.downFile);
                }
            }
        });
        return load;
    }

    public final void customImportLib(final String name, final String version) {
        String savePath = PATH + "/" +
                name + "-" +
                version + ".jar";
        dependencies.add(new ImportData(null, savePath));
    }

    public final void customImportLib(final String downUrl, final String name, final String version) {
        String url = downUrl + "/" +
                name + "-" +
                version + ".jar";
        String savePath = PATH + "/" +
                name + "-" +
                version + ".jar";
        dependencies.add(new ImportData(url, savePath));
    }

    public final void importLib(final String str, final String name, final String version) {
        final String[] temp = str.split("\\.");
        final StringBuilder url = new StringBuilder(URL);
        url.append("/");
        for (String s : temp) {
            url.append(s).append("/");
        }
        url.append(name).append("/")
            .append(version).append("/")
            .append(name).append("-")
            .append(version).append(".jar");
        String savePath = PATH + "/" +
                name + "-" +
                version + ".jar";
        dependencies.add(new ImportData(url.toString(), savePath));
    }

    public final void removeOldLib() {
        FileUtil fileUtil = new FileUtil(new File(PATH));
        Seq<File> list =  fileUtil.getFileList();
        list.eachBooleanIfs(e -> !loadEnd.contains(e),File::delete);
    }

    private static class ImportData {
        final String downUrl;
        final File downFile;
        public ImportData(String downUrl,String downFile) {
            this.downUrl = downUrl;
            this.downFile = new File(downFile);
        }
    }
}