package org.m.svtpk.utils;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class RunnableCopier implements Runnable {
    private Thread t;
    private final String threadName;

    public RunnableCopier(String name) {
        threadName = name;
        System.out.println("Creating thread " + threadName);
    }

    public byte[] getAndSave(int i, String filename, String suffix, String url, HttpMethod httpMethod, HttpEntity<String> httpEntity) {
        ResponseEntity<byte[]> byteRes;
        List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
        messageConverters.add(new ByteArrayHttpMessageConverter());
        RestTemplate restTemplateByte = new RestTemplate(messageConverters);

        URI u = URI.create(url + suffix
        );
        if (!u.isAbsolute()) return null;
        byteRes = restTemplateByte.exchange(u, httpMethod, httpEntity, byte[].class);
        if (byteRes.getBody() == null) return null;
        try {
            filename = StringHelpers.fileNameFixerUpper(filename.concat(String.valueOf(i)).concat(suffix));
            Settings s = Settings.load();
            String pathString = s.getPath().trim() + "\\" + filename.trim();
            Path p;
            p = Paths.get(pathString);
            Files.write(p, byteRes.getBody());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteRes.getBody();
    }

    public static void test(){
        String[] cmd = {"ping", "google.com"};
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(cmd);
        processBuilder.directory(new File(System.getProperty("user.dir")));
        try{
            Process process = processBuilder.start();
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = br.readLine()) != null){
                System.out.println(line);
            }
            System.out.println("Exited with error code "+process.waitFor());




            ProcessBuilder b2 = new ProcessBuilder("notepad.exe");
            Process p2= b2.start();

            while(p2.isAlive()){
                System.out.println("I AM ALIVE");
                Thread.sleep(500);
            }
            System.out.println("am ded");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void run() {
        System.out.println("Running thread " + threadName);


    }

    public void start() {
        System.out.println("Starting thread " + threadName);
        if (t == null) {
            t = new Thread(this, threadName);
            t.start();
        }
    }
}
