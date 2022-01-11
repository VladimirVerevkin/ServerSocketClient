package com.example.myclient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class Setings extends AppCompatActivity {

    final String LOG_SE = "myLogsSetings";

    private Button btnSaveSetServer;
    private EditText etIpServer;
    private EditText etPortServer;

    private Button btnAddButton;
    private Button btnSaveButton;
    private Button btnCloseSettings;

    DBHelper dbHelper;
    SQLiteDatabase database;

    LinearLayout llDBTable;

    //Создаем список вьюх которые будут создаваться
    private List<View> allCustomView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setings);

        btnSaveSetServer  = (Button)findViewById(R.id.btnSaveSetServer);
        etIpServer = (EditText)findViewById(R.id.etIpServer);
        etPortServer = (EditText)findViewById(R.id.etPortServer);

        btnAddButton = (Button)findViewById(R.id.btnAddButton);
        btnSaveButton = (Button)findViewById(R.id.btnSaveButton);
        btnCloseSettings = (Button)findViewById(R.id.btnCloseSettings);

        loadSetServer();    //загрузка настроек для подключения к серверу

        //слушатель кнопки Сохранить настройки подключения к серверу в память
        btnSaveSetServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSaveSetServerClick();
            }
        });
        //слушатель кнопки Добавить кнопку
        btnAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddButton();
              }
        });
         //слушатель кнопки Сохранить - сохранение в БД текущего ListView
        btnSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSaveButton();
            }
        });

        btnSaveButton.setClickable(false); //отключить кнопку

        //слушатель кнопки Закрыть - выход без сохраниея изменений
        btnCloseSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCloseSettings();
            }
        });

        //находим наш linear в который будут помещенны CustomView в activity_settings.xml
        //final LinearLayout linear = (LinearLayout) findViewById(R.id.linear);
        llDBTable = (LinearLayout) findViewById(R.id.llDBTable);
        //инициализировали наш массив с CustomView
        allCustomView = new ArrayList<View>();

        // создаем объект для создания и управления версиями БД
        dbHelper = new DBHelper(this);  //экземпляр базы данных
        //database = dbHelper.getWritableDatabase(); // для теста - чтение запись данных
        database = dbHelper.getReadableDatabase();  //объект класса SQLiteDatabase
        // получить все записи из таблицы TABLE_PATH
        Cursor cursor = database.query(DBHelper.TABLE_PATH, null,null, null, null, null, null);
        if(cursor.moveToFirst()){
            int idIndex = cursor.getColumnIndex(DBHelper.KEY_ID);
            int nameIndex = cursor.getColumnIndex(DBHelper.KEY_NAME_BUTTON);
            int pathIndex = cursor.getColumnIndex(DBHelper.KEY_PATH);
            int posIndex = cursor.getColumnIndex(DBHelper.KEY_POSITION);
            do {
                Log.d(LOG_SE, "ID = " + cursor.getInt(idIndex)+
                                        ", name = " + cursor.getString(nameIndex)+
                                        ", путь = " + cursor.getString(pathIndex)+
                                        ", позиция = " + cursor.getString(posIndex));
                //Создание LinearLayout
                //берем наш кастомный лейаут находим через него все наши кнопки и едит тексты, задаем нужные данные
                final View view = getLayoutInflater().inflate(R.layout.custom_view_layout, null);
                Button deleteField = (Button) view.findViewById(R.id.btnDelButton);
                deleteField.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            //получаем родительский view и удаляем его
                            ((LinearLayout) view.getParent()).removeView(view);
                            //удаляем эту же запись из массива что бы не оставалось мертвых записей
                            allCustomView.remove(view);
                            btnSaveButton.setClickable(true); //Включаем кнопку
                        } catch(IndexOutOfBoundsException ex) {
                            ex.printStackTrace();
                        }
                    }
                });
                EditText etSetNameButton = (EditText) view.findViewById(R.id.etSetNameButton);
                etSetNameButton.setText(cursor.getString(nameIndex));
                EditText etSetPathFolder = (EditText) view.findViewById(R.id.etSetPathFolder);
                etSetPathFolder.setText(cursor.getString(pathIndex));

                //добавляем все что создаем в массив
                allCustomView.add(view);
                //добавляем елементы в linearlayou llBDTable
                llDBTable.addView(view);

               } while (cursor.moveToNext());
        } else Log.d(LOG_SE, "Таблица пустая");
        cursor.close();



    }
    /**********************************************************
    ** Метод сохранение IP адреса и номера порта сервера в память
    ***********************************************************/
    private void onSaveSetServerClick(){
        SharedPreferences sPref = getSharedPreferences(MainActivity.FILE_SOCKET_SET,MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(MainActivity.IP_SERVER, etIpServer.getText().toString());
        ed.putString(MainActivity.PORT_SERVER, etPortServer.getText().toString());
        ed.commit();
        Toast.makeText(this, "ip&port saved", Toast.LENGTH_SHORT).show();
    }
    /**********************************************************
     ** Метод чтения IP адреса и номера порта сервера из памяти
     ***********************************************************/
    private void loadSetServer() {
        SharedPreferences sPref = getSharedPreferences(MainActivity.FILE_SOCKET_SET, MODE_PRIVATE);
        String ip = sPref.getString(MainActivity.IP_SERVER, "");
        etIpServer.setText(ip);
        String port = sPref.getString(MainActivity.PORT_SERVER, "");
        etPortServer.setText(port);
        Toast.makeText(this, ip + ":" + port, Toast.LENGTH_SHORT).show();
    }
    /**********************************************************
     ** Метод добавления кнопки с параметрами по умолчанию
     ***********************************************************/
    private void onAddButton(){
        //берем наш кастомный лейаут находим через него все наши кнопки и едит тексты, задаем нужные данные
        final View view = getLayoutInflater().inflate(R.layout.custom_view_layout, null);
        Button deleteField = (Button) view.findViewById(R.id.btnDelButton);
        //Слушатель кнопки - удалить View
        deleteField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    //получаем родительский view и удаляем его
                    ((LinearLayout) view.getParent()).removeView(view);
                    //удаляем эту же запись из массива что бы не оставалось мертвых записей
                    allCustomView.remove(view);
                } catch(IndexOutOfBoundsException ex) {
                    ex.printStackTrace();
                }
            }
        });
        EditText etSetNameButton = (EditText) view.findViewById(R.id.etSetNameButton);
        etSetNameButton.setText("Имя");
        EditText etSetPathFolder = (EditText) view.findViewById(R.id.etSetPathFolder);
        etSetPathFolder.setText("D:/папка/");

        //добавляем все что создаем в массив
        allCustomView.add(view);
        //добавляем елементы в linearlayou llBDTable
        llDBTable.addView(view);

        btnSaveButton.setClickable(true); //включить кнопку
    }
    /**********************************************************
     ** Метод Сохранить изменения в списке кнопок
     ***********************************************************/
    private void onSaveButton(){

        //преобразуем наш ArrayList в просто String Array
        String [] itemsName = new String[allCustomView.size()];
        String [] itemsPath = new String[allCustomView.size()];
        String [] itemsPosition = new String[allCustomView.size()];

        //запускаем чтение всех элементов этого списка и запись в массивы
        for(int i=0; i < allCustomView.size(); i++) {
            itemsName[i] = ((EditText) allCustomView.get(i).findViewById(R.id.etSetNameButton)).getText().toString();
            itemsPath[i] = ((EditText) allCustomView.get(i).findViewById(R.id.etSetPathFolder)).getText().toString();
            itemsPosition[i] = Integer.toString(i);
        }
        //удалить записи в таблице БД
        database.delete(DBHelper.TABLE_PATH, null, null);
        dbHelper.close();

        // создаем объект для создания и управления версиями БД
        dbHelper = new DBHelper(this);  //экземпляр базы данных
        //database = dbHelper.getWritableDatabase(); // для теста - чтение запись данных
        database = dbHelper.getWritableDatabase();  //объект класса SQLiteDatabase
        //внести в БД данные
        for(int i=0; i < itemsPosition.length; i++) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DBHelper.KEY_NAME_BUTTON, itemsName[i]);
            contentValues.put(DBHelper.KEY_PATH, itemsPath[i]);
            contentValues.put(DBHelper.KEY_POSITION, itemsPosition[i]);
            database.insert(DBHelper.TABLE_PATH, null, contentValues);
            Log.d(LOG_SE, "првыиамирумиа БД");
        }
        btnSaveButton.setClickable(false); //отключить кнопку

     }
    /*********** Закрыть Activity без сохранения *********/
    private void   onCloseSettings(){
        //закрыть Ativity
        dbHelper.close();
        super.finish();
    }

}