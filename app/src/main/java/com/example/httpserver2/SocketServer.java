package com.example.httpserver2;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Semaphore;

import android.os.Handler;
import android.util.Log;

public class SocketServer extends Thread {

	ServerSocket serverSocket;

	Handler handler;
	Semaphore sem = new Semaphore(2);

	public SocketServer (Handler h) {
		handler = h;

	}

	public final int port = 12345;
	boolean bRunning;

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

				sem.acquire();
				ClientThread ct = new ClientThread(s, handler, sem);
				ct.start();
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

// TODO 3: Posílat i manifest