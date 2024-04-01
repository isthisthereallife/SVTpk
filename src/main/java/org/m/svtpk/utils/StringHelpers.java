package org.m.svtpk.utils;

public class StringHelpers {

    public static String fileNameFixerUpper(String string) {
        return string
                //.replace("å", "a")
                //.replace("Å", "A")
                //.replace("ä", "a")
                //.replace("Ä", "A")
                //.replace("ö", "o")
                //.replace("Ö", "O")
                .replace(" ", ".")
                .replace(":", ".")
                .replace("*", ".")
                .replace("?", ".")
                .replace("\"", ".")
                .replace("<", ".")
                .replace(">", ".")
                .replace("|", ".")
                .replace("/", ".")
                .replace("\\", ".");
    }

    public static String lazyfix(String orig){
        return orig.replaceAll("\"","")
                .replaceAll("\\\\","")
                .replaceAll(":"," ")
                .trim();
    }
}
