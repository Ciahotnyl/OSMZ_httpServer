package com.example.httpserver2;

import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.logging.Handler;

public class ClientThread extends Thread {
    Socket s;

    public ClientThread(Socket s) {
        this.s = s;
    }

    @Override
    public void run(){

        try{
            OutputStream o = s.getOutputStream();
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(o));
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));

            String tmp = in.readLine();
            String[] pole;
            String uri = "/";
            if(tmp != null)
            {
                pole = tmp.split("[\\s+]");
                uri = pole[1];
            }

            String externalStorageDirectoryPath = "/sdcard/Picture";
            String filePath = externalStorageDirectoryPath + uri;

            File f = new File(filePath);
            if(f.exists()){
                if(f.isFile()){
                    //TODO zobrazení souboru
                    if (filePath.endsWith(".png") || filePath.endsWith(".jpg") || filePath.endsWith(".jpeg")) {
                        out.write("HTTP/1.0 200 OK\n" +
                                "Content-Type: " + getFileType(filePath) + "\n"+
                                "Content-Length: " + f.length() + "\n" +
                                "\n");
                        out.flush();

                        FileInputStream fileInputStream = new FileInputStream(f);
                        byte[] fileBytes = new byte[2048];
                        while (fileInputStream.read(fileBytes) != 0) {
                            o.write(fileBytes);
                        }
                        o.flush();
                    } else {
                        BufferedReader reader;
                        reader = new BufferedReader( new FileReader(filePath));
                        String line = reader.readLine();
                        out.write("HTTP/1.0 200 OK\n" +
                                "Content-Type: text/html\n"+
                                "\n");
                        out.flush();
                        while(line != null){
                            out.write(line+"\n");
                            line = reader.readLine();
                        }
                        out.flush();
                        reader.close();
                    }
                }
                else{
                    String path = externalStorageDirectoryPath+uri; // V rootu
                    File directory = new File(path+"/");
                    File[] files = directory.listFiles();

                    String resultList = "HTTP/1.0 200 OK\n" +
                            "Content-Type: text/html\n" +
                            "\n" +
                            "<html>\n" +
                            "<body>\n";

                    resultList += "<ul>\n";
                    for(int i = 0; i < files.length; i++)
                    {
                        resultList += "<li><a href="+("/"+files[i].getName())+">"+files[i].getName()+"</a></li>";
                    }
                    resultList += "</ul>\n";
                    resultList += "</body>\n" +
                            "</html>";
                    out.write(resultList);
                    Log.d("List", resultList);
                }
            }
            else {
                out.write("HTTP/1.0 404 Not Found\n" +
                        "Content-Type: text/html\n" +
                        "\n" +
                        "<html>\n" +
                        "<body>\n" +
                        "<h1>Chyba - TEST OSMZ</h1>\n" +
                        "</body>\n" +
                        "</html>");
            }
            out.flush();
            s.close();

        }
        catch (IOException e) {

            Log.d("SRV", "Error");
            e.printStackTrace();
        }
    }
    private static String getFileType(String path) {
        String type = null;

        if (path.charAt(path.length() - 1) == '/') {
            path = path.substring(0, path.length() - 1);;
        }

        String extension = MimeTypeMap.getFileExtensionFromUrl(path);
        if (extension != null) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            type = mime.getMimeTypeFromExtension(extension);
        }

        return type;
    }
}