package com.example.httpserver2;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import android.webkit.MimeTypeMap;
import android.util.Log;

public class SocketServer extends Thread {

	ServerSocket serverSocket;
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

				ClientThread ct = new ClientThread(s);
				HttpServerActivity.threadsView.setText("Funguje to?");
				ct.start();
			}
		}
		catch (IOException e) {
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
	private static void run2(){

	}
}

// TODO 2: Posílat z klientských vláken stavové informace (např URI + objem dat) -> handler.obtainMessage -> HANDLESTATE

// TODO 3: Posílat i manifest