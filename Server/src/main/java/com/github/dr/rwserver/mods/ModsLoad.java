/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.mods;

import com.github.dr.rwserver.Main;
import com.github.dr.rwserver.struct.ObjectMap;
import com.github.dr.rwserver.struct.OrderedMap;
import com.github.dr.rwserver.struct.Seq;
import com.github.dr.rwserver.util.IsUtil;
import com.github.dr.rwserver.util.alone.annotations.DidNotFinish;
import com.github.dr.rwserver.util.alone.annotations.NeedHelp;
import com.github.dr.rwserver.util.file.FileUtil;
import com.github.dr.rwserver.util.io.IoReadConversion;
import com.github.dr.rwserver.util.log.Log;
import com.github.dr.rwserver.util.zip.zip.ZipDecoder;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.dr.rwserver.mods.ModsLoadUtil.checkCharAt;
import static com.github.dr.rwserver.mods.ModsLoadUtil.checkForInclusion;

/**
 * Mods加载
 * @author Dr
 */
@NeedHelp
@DidNotFinish
@Deprecated(forRemoval = false)
public class ModsLoad {
    public ModsLoad(FileUtil fileUtil) throws Exception {
        Seq<String> list = FileUtil.readFileListString(Objects.requireNonNull(Main.class.getResourceAsStream("/unitData-114")));
        Seq<String> lll = new Seq<>();
        list.each(e -> {
            lll.add(e.split("%#%")[1]);
        });
        final ModsData modsData = new ModsData(new ZipDecoder(fileUtil.getFile()).getZipNameInputStream("common.ini"));
        final ObjectMap<String,Integer> objectMap = new ObjectMap<>();
        OrderedMap<String, byte[]> orderedMap = new ZipDecoder(fileUtil.getFile()).getSpecifiedSuffixInThePackageAllFileName("ini");
        orderedMap.entries().forEachRemaining((k) -> {
            ModsData s = null;
            try {
                s = new ModsData(k.value);

                if (Boolean.getBoolean(s.modFileData.get("core").get("dont_load"))) {
                    return;
                }

                s.a(s,s,modsData,orderedMap);

                s.addModsConfig(modsData);

                s.loadCopyFromSection();

                s.aa();

                //Log.clog(k.key);
               //Log.clog(String.valueOf(s.getMd5()));
                if (s.getName() != null) {
                    //objectMap.put(s.getName(),s.getMd5());
                    if (!lll.contains(String.valueOf(s.getMd5()))) {
                        Log.clog(k.key + "    " + s.getMd5());

                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        /*
        try {
            DataOutputStream stream = Data.utilData.stream;
            stream.writeInt(1);
            stream.writeInt(objectMap.size);
            String[] unitdata;
            objectMap.each((k,v) -> {
                try {
                stream.writeUTF(k);
                stream.writeInt(v);
                stream.writeBoolean(true);
                stream.writeBoolean(false);
                stream.writeLong(0);
                stream.writeLong(0);
                } catch (Exception e) {
                    Log.error(e);
                }
            });

        } catch (Exception e) {
            Log.error(e);
        } */
    }

    private static class ModsData {
        /** 提取在[ ] 中间 */
        private static final Pattern g = Pattern.compile("\\s*\\[([^]]*)]\\s*");
        /** 分割 = */
        private static final Pattern h = Pattern.compile("\\s*([^=:]*)[=:](.*)");
        private static final Pattern a = Pattern.compile("\\$\\{([^}]*)}");
        private static final Pattern b = Pattern.compile("[A-Za-z_][A-Za-z_.0-9]*");

        private final LinkedHashMap<String,LinkedHashMap<String,String>> modFileData = new LinkedHashMap<>();

        private LinkedHashMap<String,String> global;
        private LinkedHashMap<String,String> define;

        private ModsData(InputStream inputStream) throws IOException {
            readModFile(IoReadConversion.streamBufferRead(inputStream));
        }

        private ModsData(byte[] bytes) throws IOException {
            readModFile(new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes))));
        }

        private int getMd5() {
            try {
                MessageDigest messageDigest = MessageDigest.getInstance("MD5");
                for (String str : this.modFileData.keySet()) {
                    LinkedHashMap<String, String> map = this.modFileData.get(str);
                    for (String str1 : map.keySet()) {
                        String str2 = str + ":" + str1 + ":" + map.get(str1);
                        messageDigest.update(str2.getBytes(StandardCharsets.UTF_8));
                    }
                }
                return new BigInteger(1, messageDigest.digest()).intValue();
            } catch (NoSuchAlgorithmException noSuchAlgorithmException) {
                throw new RuntimeException(noSuchAlgorithmException);
            }
        }

        private void readModFile(BufferedReader paramBufferedReader) throws IOException {
            try {
                String str2 = null;
                String lineString;
                while ((lineString = paramBufferedReader.readLine()) != null) {
                    if (lineString.startsWith("\"")) {
                        lineString = lineString.substring(1);
                    }
                    if (lineString.trim().startsWith("#") || lineString.trim().length() == 0) {
                        continue;
                    }
                    if (lineString.contains("[")) {
                        Matcher matcher1 = g.matcher(lineString);
                        if (matcher1.matches()) {
                            str2 = matcher1.group(1).trim();
                            continue;
                        }
                    }
                    if (str2 != null && str2.startsWith("comment_")) {
                        continue;
                    }
                    Matcher matcher = h.matcher(lineString);
                    if (matcher.matches()) {
                        if (str2 == null) {
                            continue;
                        }
                        String str3 = matcher.group(1).trim();
                        String str4 = matcher.group(2).trim();
                        LinkedHashMap<String, String> linkedHashMap = this.modFileData.computeIfAbsent(str2, k -> new LinkedHashMap<>());
                        linkedHashMap.put(str3, str4);
                    }
                }
            } finally {
                paramBufferedReader.close();
            }
        }

        private String getName() {
            return this.modFileData.get("core").get("name");
        }

        private void a(ModsData modsData1,ModsData modsData2,ModsData modsData,OrderedMap<String, byte[]> orderedMap) {
            String str = modsData2.modFileData.get("core").get("copyFrom");
            ModsData a = null;
            if (str != null) {
                String[] arrayOfString = str.split(",");
                Collections.reverse(Arrays.asList((Object[])arrayOfString));
                for (String str1 : arrayOfString) {
                    str1 = str1.trim();
                    if (!IsUtil.isBlank(str1)) {
                        if (str1.contains("..")) {
                            continue;
                        }
                        if (str1.startsWith("ROOT:")) {
                        //do not understand
                        } else {
                            try {
                                a = new ModsData(orderedMap.get(str));
                                modsData1.addModsConfig(a);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    a(modsData1,a,modsData,orderedMap);
                }
            } else {
                //modsData1.addModsConfig(modsData);
            }

        }

        private Seq<String> getCustomNameList(String equal) {
            final Seq<String> result = new Seq<>();
            for (String str : this.modFileData.keySet()) {
                LinkedHashMap<String,String> map = this.modFileData.get(str);
                if (map.get(equal) != null) {
                    result.add(str);
                }
            }
            return result;
        }

        private Seq<String> getTheCustomNameListInTheName(String name,String equal) {
            final Seq<String> result = new Seq<>();
            for (String str : this.modFileData.get(name).keySet()) {
                if (str.startsWith(equal)) {
                    if ("IGNORE".equals(this.modFileData.get(name).get(str))) {
                        continue;
                    }
                    result.add(str);
                }
            }
            return result;
        }

        private Seq<String> getTheKeyOfTheValueInTheLinkedList(final String value) {
            final Seq<String> result = new Seq<>();
            this.modFileData.forEach((k,v) -> {
                v.forEach((k1,v1) -> {
                    if (k1.startsWith(value)) {
                        if ("IGNORE".equals(v1)) {
                            return;
                        }
                        result.add(k);
                    }
                });
            });
            return result;
        }


        private void loadCopyFromSection() {
            getCustomNameList("@copyFromSection").each(e -> {
                loadCopyFromSection(this,e,e);
            });
        }

        public void loadCopyFromSection(ModsData paramab, String paramString1, String paramString2) {
            String sectionValue = this.modFileData.get(paramString2).get("@copyFromSection");
            if (IsUtil.isBlank(sectionValue)) {
                return;
            }
            String[] sectionValueArray = sectionValue.split(",");
            Collections.reverse(Arrays.asList(sectionValueArray));
            for (String str1 : sectionValueArray) {
                str1 = str1.trim();
                if (!"".equals(str1)) {
                    Seq<String> m1 = paramab.getTheCustomNameListInTheName(str1, "");
                    if (m1.size() == 0) {
                        throw new RuntimeException("[" + paramString2 + "]@copyFromSection: Could not find keys in target section: " + str1);
                    }
                    for (String str2 : m1) {
                        String str3 = paramab.getMaptoMapVaule(str1, str2);
                        if (str3 != null) {
                            paramab.writeToLinkedList(paramString1, str2, str3);
                        }
                    }
                    loadCopyFromSection(paramab, paramString1, str1);
                }
            }
        }

        public LinkedHashMap<String,String> getGlobalData() {
            final LinkedHashMap<String,String> result = new LinkedHashMap<>();
            getTheKeyOfTheValueInTheLinkedList("@global ").each(e -> {
                getTheCustomNameListInTheName(e,"@global ").each(e1 -> {
                    String str2 = e1.substring("@global ".length()).trim();
                    String str3 = this.modFileData.get(e).get(e1);
                    result.put(str2, str3);
                });
            });
            return result;
        }

        public LinkedHashMap<String,String> getDefineData() {
            final LinkedHashMap<String,String> result = new LinkedHashMap<>();
            for (String str : this.modFileData.keySet()) {
                if (str == null || str.startsWith("comment_") || str.startsWith("template_")) {
                    continue;
                }
                for (String str1 : getTheCustomNameListInTheName(str, "@define ")) {
                    String str2 = str1.substring("@define ".length()).trim();
                    String str3 = this.modFileData.get(str).get(str1);
                    result.put(str2, str3);
                }
            }
            return result;
        }

        private void aa() {
            this.global = getGlobalData();
            //this.define = getDefineData();
            final LinkedHashMap<String,String> result = new LinkedHashMap<>();
            for (String str : this.modFileData.keySet()) {
                if (str == null || str.startsWith("comment_") || str.startsWith("template_")) {
                    continue;
                }

                if (this.define != null) this.define.clear();
                this.define = new LinkedHashMap<>();
                for (String str1 : getTheCustomNameListInTheName(str, "@define ")) {
                    String str2 = str1.substring("@define ".length()).trim();
                    String str3 = this.modFileData.get(str).get(str1);
                    //Log.clog(str2+"   "+str3);
                    this.define.put(str2, str3);
                }

                LinkedHashMap<String,String> map =this.modFileData.get(str);
                for (String str1 : map.keySet()) {
                    String str2 = (String)map.get(str1);
                    if (str2 == null || !str2.contains("${")) {
                        continue;
                    }
                    StringBuffer stringBuffer = new StringBuffer();
                    Matcher matcher = a.matcher(str2);
                    while (matcher.find()) {
                        String str4;
                        String str3 = matcher.group(1);
                        str4 = a(str, str3);
                        matcher.appendReplacement(stringBuffer, str4);
                    }
                    matcher.appendTail(stringBuffer);
                    str2 = stringBuffer.toString();
                    writeToLinkedList(str, str1, str2);
                    //Log.clog(str2);
                }
            }
        }

        public String a(String paramString1, String paramString2) {
            paramString2 = paramString2.trim();
            boolean bool = checkForInclusion(paramString2);
            byte b1 = 0;
            StringBuffer stringBuffer = new StringBuffer();
            Matcher matcher = b.matcher(paramString2);
            while (matcher.find()) {
                String str1 = matcher.group(0);
                if (checkCharAt(str1)) {
                    continue;
                }
                if ("int".equals(str1) || "cos".equals(str1) || "sin".equals(str1) || "sqrt".equals(str1)) {
                    continue;
                }
                String str2 = b(paramString1, str1);
                if (bool) {
                    if (!checkCharAt(str2)) {
                        throw new RuntimeException("Cannot do maths on '" + str2 + "' from " + str1 + " (not a number)");
                    }
                }
                matcher.appendReplacement(stringBuffer, str2);
            }
            matcher.appendTail(stringBuffer);
            paramString2 = stringBuffer.toString();
            if (bool) {
                paramString2 = ModsLoadUtil.b(new b$1(paramString2).b());
            }
            return paramString2;
        }


        public String b(String paramString1, String paramString2) {
            if (paramString2.contains(".")) {
                String[] arrayOfString = ModsLoadUtil.b(paramString2, '.');
                if (arrayOfString.length != 2) {
                    throw new RuntimeException("Unexpected key format: " + paramString2);
                }
                String str1 = arrayOfString[0];
                String str2 = arrayOfString[1];
                if ("section".equals(str1)) {
                    str1 = paramString1;
                }
                String str3 = this.modFileData.get(str1).get(str2);
                if (str3 == null) {
                    throw new RuntimeException("Could not find: [" + str1 + "]" + str2 + " in this conf file");
                }
                if (str3.contains("${")) {
                    throw new RuntimeException("Reference [" + str1 + "]" + str2 + " is dynamic, chaining is not yet supported");
                }
                return str3;
            }
            String str = this.define.get(paramString2);
            if (str != null) {
                return str;
            }
            str = this.global.get(paramString2);
            if (str != null) {
                return str;
            }
            throw new RuntimeException("Could not find variable with name: " + paramString2);
            //return "";
        }

        public String getMaptoMapVaule(String paramString1, String paramString2) {
            LinkedHashMap<String,String> map = this.modFileData.get(paramString1);
            if (map == null) {
                return null;
            }
            return map.get(paramString2);
        }

        public void writeToLinkedList(String paramString1, String paramString2, String paramString3) {
            LinkedHashMap<String, String> linkedHashMap = this.modFileData.computeIfAbsent(paramString1, k -> new LinkedHashMap<>());
            linkedHashMap.putIfAbsent(paramString2, paramString3);
        }

        private void addModsConfig(ModsData modsData) {
            modsData.modFileData.forEach((k,v) -> {
                v.forEach((k1,v1) -> {
                    if ("@copyFrom_skipThisSection".equals(k1)) {
                        if (Boolean.getBoolean(v1)) {
                            return;
                        }
                    }
                    if (this.modFileData.get(k) == null) {
                        this.modFileData.computeIfAbsent(k, keyCache -> new LinkedHashMap<>()).put(k1, v1);
                    } else {
                        if (this.modFileData.get(k).get(k1) == null) {
                            this.modFileData.get(k).put(k1,v1);
                        }
                    }

                });
            });
        }
    }
}