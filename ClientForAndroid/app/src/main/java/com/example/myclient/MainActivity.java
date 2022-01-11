package com.example.myclient;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.SystemClock;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    final String LOG_MA = "myLogsMainActivity";

    static final String IP_SERVER = "ip_server";
    static final String PORT_SERVER = "port_server";
    static final String FILE_SOCKET_SET = "file_socket_set";

    DBHelper dbHelper;
    SQLiteDatabase database;

    LinearLayout llButton;

    private Intent intentIn;
    private String ipServer;
    private int portServer;

    private Connection  mConnect  = null;
    private int statusLoader; //переменная для хранения числа передоваемых файлв: 1 - один, >1 несколько файлов

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        llButton = (LinearLayout) findViewById(R.id.llButton);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //очистка LinearLayout llButton
        llButton.removeAllViews();

        // создаем объект для создания и управления версиями БД
        dbHelper = new DBHelper(this);  //экземпляр базы данных
        //database = dbHelper.getWritableDatabase(); // для теста - чтение запись данных
        database = dbHelper.getReadableDatabase();  //объект класса SQLiteDatabase

        // получить записи из столбца KEY_NAME_BUTTON таблицы TABLE_PATH = число кнопок
        Cursor cursor = database.query(DBHelper.TABLE_PATH, new String[] {DBHelper.KEY_ID,DBHelper.KEY_NAME_BUTTON},null, null, null, null, null);
        if(cursor.moveToFirst()){
            int idIndex = cursor.getColumnIndex(DBHelper.KEY_ID);
            int nameIndex = cursor.getColumnIndex(DBHelper.KEY_NAME_BUTTON);
            do {
                // создаем Button, пишем текст и добавляем в LinearLayout
                Button btnNew = new Button(this);
                btnNew.setId(cursor.getInt(idIndex));
                btnNew.setText(cursor.getString(nameIndex));
                btnNew.setTextSize(32);
                btnNew.setOnClickListener(this);
                llButton.addView(btnNew);
                Log.d(LOG_MA, ", name = " + cursor.getString(nameIndex));
            } while (cursor.moveToNext());
        } else Log.d(LOG_MA, "Таблица пустая");
        cursor.close();

        // для хранения данных: IP и номера порта
        SharedPreferences sPref = getSharedPreferences(FILE_SOCKET_SET, MODE_PRIVATE);
        ipServer = sPref.getString(IP_SERVER, "");
        portServer = Integer.parseInt(sPref.getString(PORT_SERVER, "0"));
        Log.d(LOG_MA, "ipServer = " + ipServer);
        Log.d(LOG_MA, "portServer = " + portServer);

        //получение данных для приложения
        intentIn = new Intent();
        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        intentIn = intent;

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleSendText(intent); // Handle text being sent
            } else if (type.startsWith("image/")) {
                //handleSendImage(intent); // Handle single image being sent
                statusLoader = 1; // будет отправлен один файл на сервер
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                //handleSendMultipleImages(intent); // Handle multiple images being sent
                statusLoader = 3; // будет отправено несколько файлов на сервер
            }
        } else {
            // Handle other intents, such as being started from the home screen
        }
        //Toast.makeText(getApplicationContext(), "onStart()", Toast.LENGTH_SHORT).show();
        Log.d(LOG_MA, "onStart()");
    }

    /********* Метод генерации имени файла для передачи на сервер ***************/
    public String getNameFromDateTime() {
        // Текущее время
        Date currentDate = new Date();
        // Форматирование времени как "день.месяц.год"
        DateFormat dateFormat = new SimpleDateFormat("ddMMyy", Locale.getDefault());
        String dateText = dateFormat.format(currentDate);
        // Форматирование времени как "часы:минуты:секунды"
        DateFormat timeFormat = new SimpleDateFormat("HHmmss", Locale.getDefault());
        String timeText = timeFormat.format(currentDate);
        return ("IMG_" + dateText + "_" + timeText + ".jpg");
    }

    /********************************************************************
     *
     *********************************************************************/
    void handleSendText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            // Update UI to reflect text being shared
            Log.d(LOG_MA, "sharedText != null");
        }
    }

    /*************************************************************************
     *   Метод создания потока для отправки файла на сервер
     *************************************************************************/
    private void transferFileIMG(Uri imageUri, String folderPath){
        onOpenClick(ipServer, portServer);  //создать подключение сокет
        SystemClock.sleep(1000);
        String fileName = getNameFromDateTime();
        Log.d(LOG_MA, "threadSendFile " + folderPath + " " + fileName);
        if (mConnect == null) {
        Log.d(LOG_MA, "Соединение не установлено");
        }
        else {
            Log.d(LOG_MA, "Отправка");
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(imageUri);
                        mConnect.sendFile(inputStream, folderPath, fileName);
                    }
                    catch (Exception e) {
                        Log.e(LOG_MA, e.getMessage());
                    }
                }
            };
            Thread thread = new Thread(runnable);
            thread.start();
            try {
                thread.join();  //ожидание завершения работы потока(для запуска следующего)
            }
            catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    }
    /***************************************************************************
     *      Метод создание потока для передачи текста
     ***************************************************************************/
    private void transferText(String data) {
        onOpenClick(ipServer, portServer);  //создать подключение сокет
        SystemClock.sleep(200);
        Log.d(LOG_MA, "Отправка сообщения");
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    // отправляем сообщение
                    mConnect.sendData(data);
                } catch (Exception e) {
                    Log.e(LOG_MA, e.getMessage());
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
        try {
            thread.join();  //ожидание завершения работы потока(для запуска следующего)
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    /***************************************************************************
    *      Метод создание потока для передачи файла
    ***************************************************************************/
    private void threadSendFile(String folderPath) {
        Log.d(LOG_MA, "threadSendFile");
        Uri imageUri = (Uri) intentIn.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            transferFileIMG(imageUri, folderPath);
        }
     }
    /***************************************************************************
    *      Метод создание потока для передачи файлов
    ***************************************************************************/
    private void threadSendFiles(String folderPath) {
        Log.d(LOG_MA, "threadSendFileS");
        ArrayList<Uri> imageUris = intentIn.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (imageUris != null) {
            for(int i = 0; i < imageUris.size(); i++){
                transferFileIMG(imageUris.get(i), folderPath);
            }
        }
    }
    /***************************************************************
     *   Метод  подключение к серверу в отдельном потоке
     ***************************************************************/
    private void onOpenClick(final String HOST, final int PORT)
    {
        Log.d(LOG_MA, "onOpenClick " + HOST + ":" + PORT);
        //HOST = "192.168.1.116";
       //PORT = 9090;
        // Создание подключения
        mConnect = new Connection(HOST, PORT);
        // Открытие сокета в отдельном потоке
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mConnect.openConnection();
                    // Разблокирование кнопок в UI потоке
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(LOG_MA, "run run");
                        }
                    });
                    Log.d(LOG_MA, "Соединение установлено");
                } catch (Exception e) {
                    Log.e(LOG_MA, e.getMessage());
                    mConnect = null;
                }
            }
        }).start();
    }

    /***********************************************
     *  Обработчик кнопок
     ***********************************************/
    @Override
    public void onClick(View view)
    {
        view.getId();
        Button button = (Button)findViewById(view.getId());
        //Получение текста нажатой кнопки
        String buttonText = button.getText().toString();
        Log.d(LOG_MA, "Текст нажатой кнопки - " + buttonText);
        // получить записи из столбца KEY_PATH нажатой кнопки таблицы TABLE_PATH
        Cursor cursor = database.query(DBHelper.TABLE_PATH, new String[] {DBHelper.KEY_NAME_BUTTON, DBHelper.KEY_PATH}, DBHelper.KEY_NAME_BUTTON + " = ?", new String[] {buttonText},null, null, null);
        if(cursor.moveToFirst()){
            int key_path = cursor.getColumnIndex(DBHelper.KEY_PATH);
            String fpFile = cursor.getString(key_path);
            buttonText = fpFile;
            Log.d(LOG_MA, "путь = " + fpFile);
         } else Log.d(LOG_MA, "Путь отсутствует");
        cursor.close();

        //отправить файл\файлы на сервер
        if(statusLoader == 1){
            Log.d(LOG_MA, ", путь = " + buttonText);
            threadSendFile(buttonText);
            Toast.makeText(this,  "Файл отправлен", Toast.LENGTH_SHORT).show();
        }
        else {
            threadSendFiles(buttonText);
            Toast.makeText(this,  "Файлы отправлены", Toast.LENGTH_SHORT).show();
        }
        Toast.makeText(this,  "ПЕРЕДАЧА ДАННЫХ ЗАВЕРШЕНА", Toast.LENGTH_SHORT).show();
        transferText("endDataTransfer"); //отправка сообщения - передача завершина
        //закрыть приложение
        super.finish();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, Setings.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStop() {
        super.onStop();
        //hideProgress(); //остановить диалог
        dbHelper.close();
        // Закрытие соединения
        if(mConnect != null)
            mConnect.closeConnection();
    }

}