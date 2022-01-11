package com.example.myclient;


import android.os.SystemClock;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Connection
{
    private  Socket  mSocket = null;
    private  String  mHost   = null;
    private  int     mPort   = 0;

    public static final String LOG_TAG = "myLogConnection";

    public Connection() {}

    public Connection (final String host, final int port)
    {
        this.mHost = host;
        this.mPort = port;
    }

    // Метод открытия сокета
    public void openConnection() throws Exception
    {
        // Если сокет уже открыт, то он закрывается
        closeConnection();
        try {
            // Создание сокета
            mSocket = new Socket(mHost, mPort);
        } catch (IOException e) {
            throw new Exception("Невозможно создать сокет: "
                    + e.getMessage());
        }
    }
    /**
     * Метод закрытия сокета
     */
    public void closeConnection()
    {
        if (mSocket != null && !mSocket.isClosed()) {
            try {
                mSocket.close();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Ошибка при закрытии сокета :"
                        + e.getMessage());
            } finally {
                mSocket = null;
            }
        }
        mSocket = null;
    }
    /**
     * Метод отправки данных
     */
    public void sendData(String data) throws Exception {
        // Проверка открытия сокета
        if (mSocket == null || mSocket.isClosed()) {
            throw new Exception("Ошибка отправки данных. " +
                    "Сокет не создан или закрыт");
        }
        // Отправка данных
        try {
            mSocket.getOutputStream().write(data.getBytes());
            //SystemClock.sleep(1000);
            mSocket.getOutputStream().flush();
        } catch (IOException e) {
            throw new Exception("Ошибка отправки данных : "
                    + e.getMessage());
        }
    }

    public void sendFile(InputStream inputStream, String dirFile, String nameFile) throws Exception{
        // Проверка открытия сокета
        if (mSocket == null || mSocket.isClosed()) {
            throw new Exception("Ошибка отправки данных. " +
                    "Сокет не создан или закрыт");
        }
        // Отправка данных
        byte[] bytearray = new byte[1024];
        try {
            String t;
            t = "File" + " " + dirFile + " " + nameFile;
            mSocket.getOutputStream().write(t.getBytes());
            SystemClock.sleep(500);
            Log.e(LOG_TAG, "!!!!!!!!!!!!!!!!!!!!!!!");
            OutputStream output = mSocket.getOutputStream();
            BufferedInputStream bis = new BufferedInputStream(inputStream);
            int readLength = -1;
            while ((readLength = bis.read(bytearray)) > 0) {
                output.write(bytearray, 0, readLength);
                Log.e(LOG_TAG, "@@@@@@@@@@@@@@@@@@");
            }
            bis.close();
            output.close();
        } catch (IOException e) {
            throw new Exception("Ошибка отправки данных : "
                    + e.getMessage());
        }
    }

    @Override
    protected void finalize() throws Throwable
    {
        super.finalize();
        closeConnection();
    }
}