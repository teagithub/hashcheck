package com.abc.tool;

import java.io.*;
import java.security.DigestInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.security.MessageDigest;

/**
 * @Author: mxc
 * @Date: Created in 2019/7/4 15:43
 * @Description:
 */
public class hashchek {
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    private static final String GENERATE = "generate";
    private static final String CHECK = "check";

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    private static void compute(String fileDir, String algorithm, StringBuffer strbuf) {
        List<File> fileList = new ArrayList<>();
        File file = new File(fileDir);
        File[] files;
        if (file.isFile()) {
            files = new File[1];
            files[0] = file;
        } else {
            files = file.listFiles();// 获取目录下的所有文件或文件夹
        }
        if (files == null) {// 如果目录为空，直接退出
            return;
        }
        // 遍历，目录下的所有文件
        for (File f : files) {
            if (f.isFile()) {
                fileList.add(f);
            } else if (f.isDirectory()) {
                compute(f.getAbsolutePath(), algorithm, strbuf);
            }
        }
        for (File f1 : fileList) {
            int bufferSize = 256 * 1024;
            FileInputStream fileInputStream = null;
            DigestInputStream digestInputStream = null;
            try {
                MessageDigest md5 = MessageDigest.getInstance(algorithm);
                fileInputStream = new FileInputStream(f1.getAbsoluteFile());
                digestInputStream = new DigestInputStream(fileInputStream, md5);
                byte[] buffer = new byte[bufferSize];
                while (digestInputStream.read(buffer) > 0) ;
                // 获取最终的MessageDigest
                md5 = digestInputStream.getMessageDigest();
                // 拿到结果，也是字节数组，包含16个元素
                byte[] resultByteArray = md5.digest();
                strbuf.append(bytesToHex(resultByteArray) + " ");
                strbuf.append(f1.getAbsolutePath() + "" + System.getProperty("line.separator"));
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }


    }

    private static void check(StringBuffer strbuf, File hashcodefile) {
        List<String> list = new ArrayList<>();
        try {
            FileReader fr = new FileReader(hashcodefile);
            BufferedReader bf = new BufferedReader(fr);
            String str;
            String[] strlines = strbuf.toString().split(System.getProperty("line.separator"));
            HashMap<String, String> strlinesmap = new HashMap<>();
            for (int i = 0; i < strlines.length; i++) {
                String[] params = strlines[i].split(" ");
                strlinesmap.put(params[1], params[0]);
            }
            int i = 0;
            int failed = 0;
            while ((str = bf.readLine()) != null) {
                String[] params = str.split(" ");
                if (strlinesmap.containsKey(params[1]) && strlinesmap.get(params[1]).equals(params[0])) {
                    System.out.println(params[1] + " OK");
                } else {
                    System.out.println(params[1] + " Failed");
                    failed++;
                    list.add(params[1]);
                }
                i++;
            }
            System.out.println("Total " + i + " files, " + failed + " files failed.");
            if (failed > 0) {
                System.out.println("Failed files are: ");
            }
            for (String f : list) {
                System.out.println(f);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        StringBuffer strbuf = new StringBuffer();
        if (args.length < 2) {
            System.out.println("Usage: java -jar hashchek.jar generate <directory path> <algorithm, e.g. MD5(default)> //生成hash校验文件 \n" +
                    "Usage: java -jar hashchek.jar check <directory path> <algorithm, e.g. MD5(default)> //校验hash码");
            return;
        }
        HashSet<String> operaSet = new HashSet<>();
        operaSet.add(GENERATE);
        operaSet.add(CHECK);
        if (args.length >= 2 && (!operaSet.contains(args[0]))) {
            System.out.println("Usage: java -jar hashchek.jar generate <directory path> <algorithm, e.g. MD5(default)> //生成hash校验文件 \n" +
                    "Usage: java -jar hashchek.jar check <directory path> <algorithm, e.g. MD5(default)> //校验hash码");
            return;
        }
        String operation = args[0];
        String path = args[1];
        File file = new File(path);
        if (!file.exists()) {
            System.out.println("Path cannot be found!");
            return;
        }
        String algorithm = "MD5";
        HashSet<String> algSet = new HashSet<>();
        algSet.add("MD5");
        algSet.add("SHA-1");
        algSet.add("SHA-256");
        if (args.length > 2) {
            algorithm = args[2];
            if (!algSet.contains(algorithm)) {
                System.out.println("Algorithm " + algorithm + " not support!");
                return;
            }
        }
        if (operation.equals(GENERATE)) {
            compute(path, algorithm, strbuf);
            //System.out.print(strbuf);
            File hashcode = new File((file.getParent() == null ? "" : file.getParent()) + file.getName() + "_hashchek.txt");
            try {
                FileWriter fw = new FileWriter(hashcode);
                fw.write(strbuf.toString());
                fw.flush();
                fw.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }
        if (operation.equals(CHECK)) {
            File hashcode = new File((file.getParent() == null ? "" : file.getParent()) + file.getName() + "_hashchek.txt");
            if (!hashcode.exists()) {
                System.out.println("hashchek.txt not found!");
            }
            compute(path, algorithm, strbuf);
            check(strbuf, hashcode);
        }
    }

}
