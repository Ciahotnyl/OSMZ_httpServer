package com.example.httpserver2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import android.os.Handler;
import android.util.Log;

public class SocketServer extends Thread {

	ServerSocket serverSocket;
	Handler handler;
	public final int port = 12345;
	boolean bRunning;
	Semaphore sem;
	int permits;

	public SocketServer (Handler h, int permits) {
		handler = h;
		sem = new Semaphore(permits);
		this.permits = permits;
	}

	public void close() {
		try {
			serverSocket.close();
		} catch (IOException e) {
			Log.d("SERVER", "Error, probably interrupted in accept(), see log");
			e.printStackTrace();
		}
		bRunning = false;
	}

	public void run() {
		try {
			Log.d("SERVER", "Creating Socket");
			serverSocket = new ServerSocket(port);
			bRunning = true;

			while (bRunning) {
				Log.d("SERVER", "Socket Waiting for connection");
				Socket s = serverSocket.accept();
				Log.d("SERVER", "Socket Accepted");

				OutputStream o = s.getOutputStream();
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(o));
				BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));

				boolean bpermit = sem.tryAcquire(0, TimeUnit.SECONDS);
				if(bpermit) {
					ClientThread ct = new ClientThread(s, handler, sem);
					ct.start();
				} else{
					Log.d("SRV", "SERVER IS BUSY");

					String s1 = in.readLine();
					out.write("HTTP/1.0 200 OK\n" +
							"Content-Type: text/html\n" +
							"<html>\n " +
							"<body>\n " +
							"\n"+
							"<h1>Server is busy</h1>\n " +
							"</body>\n " +
							"</html>");
					out.flush();

					out.close();
					o.close();
					s.close();
				}
			}
		}
		catch (IOException | InterruptedException e) {
			if (serverSocket != null && serverSocket.isClosed())
				Log.d("SERVER", "Normal exit");
			else {
				Log.d("SERVER", "Error");
				e.printStackTrace();
			}
		}
		finally {
			serverSocket = null;
			bRunning = false;
		}
	}
}