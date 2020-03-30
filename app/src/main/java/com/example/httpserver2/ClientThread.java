package com.example.httpserver2;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.webkit.MimeTypeMap;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

import java.util.Arrays;
import java.util.concurrent.Semaphore;

import static com.example.httpserver2.HttpServerActivity.obrazek;


public class ClientThread extends Thread {
    Socket s;
    String msg;
    Handler handler;
    Semaphore sem;
    String snapshot = "/camera/snapshot";
    String process = "/cgi-bin/";
    String stream = "/camera/stream";
    byte[] showPicture;


    public ClientThread(Socket s, Handler h, Semaphore sem) {
        this.s = s;
        this.handler = h;
        this.sem = sem;
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

            if(uri.contains("?rand")){
                Log.d("SRV", "URI BEFORE: -" + uri + "-");
                StringBuilder temp = new StringBuilder();
                for (char c : uri.toCharArray()) {
                    if(c == '?'){
                        break;
                    }
                    temp.append(c);
                }
                uri = temp.toString();
            }

            String externalStorageDirectoryPath = "/sdcard/Picture";
            String filePath = externalStorageDirectoryPath + uri;

            File f = new File(filePath);

            if(uri.startsWith(process)){                //CGI-BIN
                String command = uri.substring(process.length());
                String[] splitted = command.split("%");
                String arg = "";

                if(splitted.length > 1){
                    String[] parameters = Arrays.copyOfRange(splitted, 1, splitted.length);

                    for(int i = 0; i < parameters.length; i++){
                        arg += parameters[i] + ",";
                    }
                    arg = arg.substring(0, arg.length() - 1 );
                    Log.d("arg", arg);
                }

                try {
                    ProcessBuilder pb;
                    if(splitted.length > 1)
                    {
                        pb = new ProcessBuilder(splitted[0], arg);
                    } else{
                        pb = new ProcessBuilder(command);
                    }

                    final Process p=pb.start();
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(
                                    p.getInputStream()));
                    String line;
                    String text = "HTTP/1.0 200 OK\n" +
                            "Content-Type: text/html\n" +
                            "\n" +
                            "<html>\n" +
                            "<body>\n" +
                            "<h1>Spousteni procesu</h1>\n";
                    while((line=br.readLine()) != null) {
                        text += "<p>" + line + "</p>\n";
                    }
                    text += "</body>\n" +
                            "</html>";
                    out.write(text);

                } catch (Exception ex) {
                    System.out.println(ex);
                }
            }
            else if(uri.equals(snapshot) && obrazek != null){   //SNAPSHOT
                out.write("HTTP/1.0 200 OK\n" +
                        "Content-Type: image/jpeg\n" +
                        "Content-Length: " + obrazek.length + "\n" +
                        "\n");
                out.flush();

                msg = "URI : " + uri + "\n Content type: image/jpeg \n Size: " + obrazek.length;
                sendMsg(msg);

                ByteArrayInputStream bai = new ByteArrayInputStream(obrazek);

                int ch;
                while((ch = bai.read()) != -1)
                {
                    o.write(obrazek);
                }


                o.flush();
            }
            else if(uri.equals(stream) && obrazek != null){ //STREAM

                /*
                    Tady se zdárně přepisuje obrázek ve streamu, jak je možné vidět v Logu ("obr" a "obr2"),
                    ale stránka se nerefreshuje automaticky. Při manuální refreshi vše zdárně funguje.
                 */
                out.write("HTTP/1.0 200 OK\n" +
                        "Content-Type: multipart/x-mixed-replace;boundary=ciahotnyl\n" +
                        "Content-Length: " + obrazek.length + "\n" +
                        "\n");


                msg = "URI : " + uri + "\n Content type: image/jpeg \n Size: " + obrazek.length;
                sendMsg(msg);

                while(true){
                        out.flush();
                        showPicture = obrazek;
                        Log.d("obr", ""+obrazek);
                        Log.d("obr2", ""+showPicture);

                        out.write("--ciahotnyl\n" +
                                "Content-Type: image/jpeg\n" +
                                "Content-Length: " + showPicture.length + "\n\n");

                        out.flush();

                        ByteArrayInputStream bai = new ByteArrayInputStream(showPicture);

                        while((bai.read()) != -1)
                        {
                            o.write(showPicture);
                        }
                        o.flush();
                }

            }
            else {
                if (f.exists()) {
                    if (f.isFile()) {
                        if (filePath.endsWith(".png") || filePath.endsWith(".jpg") || filePath.endsWith(".jpeg")) {     //Obrázky
                            out.write("HTTP/1.0 200 OK\n" +
                                    "Content-Type: " + getFileType(filePath) + "\n" +
                                    "Content-Length: " + f.length() + "\n" +
                                    "\n");
                            out.flush();

                            msg = "URI : " + uri + "\n Content type: " + getFileType(filePath) + "\n Size: " + f.length();
                            sendMsg(msg);


                            FileInputStream fileInputStream = new FileInputStream(f);
                            byte[] fileBytes = new byte[2048];
                            while (fileInputStream.read(fileBytes) != 0) {
                                o.write(fileBytes);
                            }
                            o.flush();
                        } else {    //Textové soubory
                            BufferedReader reader;
                            reader = new BufferedReader(new FileReader(filePath));
                            String line = reader.readLine();
                            out.write("HTTP/1.0 200 OK\n" +
                                    "Content-Type: text/html\n" +
                                    "\n");
                            out.flush();
                            msg = "URI : " + uri + "\n Content type: " + getFileType(filePath) + "\n Size: " + f.length();
                            sendMsg(msg);
                            while (line != null) {
                                out.write(line + "\n");
                                line = reader.readLine();
                            }
                            out.flush();
                            reader.close();
                        }
                    } else {    //Výpis struktury
                        String path = externalStorageDirectoryPath + uri; // V rootu
                        File directory = new File(path + "/");
                        File[] files = directory.listFiles();

                        String resultList = "HTTP/1.0 200 OK\n" +
                                "Content-Type: text/html\n" +
                                "\n" +
                                "<html>\n" +
                                "<body>\n";

                        resultList += "<ul>\n";
                        for (int i = 0; i < files.length; i++) {
                            resultList += "<li><a href=" + ("/" + files[i].getName()) + ">" + files[i].getName() + "</a></li>";
                        }
                        resultList += "</ul>\n";
                        resultList += "</body>\n" +
                                "</html>";
                        out.write(resultList);
                        out.flush();
                        msg = "Directory";
                        sendMsg(msg);
                    }
                } else {
                    out.write("HTTP/1.0 404 Not Found\n" +
                            "Content-Type: text/html\n" +
                            "\n" +
                            "<html>\n" +
                            "<body>\n" +
                            "<h1>Chyba - OSMZ</h1>\n" +
                            "</body>\n" +
                            "</html>");
                }
            }
            out.flush();
            s.close();

        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            sem.release();
        }
    }
    private static String getFileType(String path) {
        String type = null;

        if (path.charAt(path.length() - 1) == '/') {
            path = path.substring(0, path.length() - 1);
        }

        String extension = MimeTypeMap.getFileExtensionFromUrl(path);
        if (extension != null) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            type = mime.getMimeTypeFromExtension(extension);
        }

        return type;
    }
    private synchronized void sendMsg(String m){
        Bundle bundle = new Bundle();
        bundle.putString("list", m);
        Message message = Message.obtain();
        message.setData(bundle);
        Log.d("send", ""+message);
        handler.sendMessage(message);
    }

}