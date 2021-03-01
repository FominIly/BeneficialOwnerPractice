package com.example.beneficialownerpracticerussia;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

//Создаем класс для работы с базой данный SQL
class DatabaseHelper extends SQLiteOpenHelper {
    private static String DB_PATH; // полный путь к базе данных
    private static String DB_NAME = "BO.db";
    private static final int SCHEMA = 1; // версия базы данных
    static final String TABLE = "bocases"; // название таблицы в бд
    // названия столбцов
    static final String COLUMN_ID = "field1";
    static final String COLUMN_NAME = "field2";
    static final String COLUMN_YEAR = "field3";
    static final String COLUMN_payments = "field4";
    static final String COLUMN_url = "field5";

    private Context myContext;

    DatabaseHelper(Context context) {
        super(context, DB_NAME, null, SCHEMA);
        this.myContext=context;
        DB_PATH =context.getFilesDir().getPath() + DB_NAME;
    }
    @Override
    public void onCreate(SQLiteDatabase db) { }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion,  int newVersion) { }

    void create_db(){
        InputStream myInput = null;
        OutputStream myOutput = null;
        try {
            File file = new File(DB_PATH);
            if (!file.exists()) {
                //получаем локальную бд как поток
                myInput = myContext.getAssets().open(DB_NAME);
                // Путь к новой бд
                String outFileName = DB_PATH;
                // Открываем пустую бд
                myOutput = new FileOutputStream(outFileName);
                // побайтово копируем данные
                byte[] buffer = new byte[1024];
                int length;
                while ((length = myInput.read(buffer)) > 0) {
                    myOutput.write(buffer, 0, length);
                }
                myOutput.flush();
            }
        }
        catch(IOException ex){
            Log.d("DatabaseHelper", ex.getMessage());
        }
        finally {
            try{
                if(myOutput!=null) myOutput.close();
                if(myInput!=null) myInput.close();
            }
            catch(IOException ex){
                Log.d("DatabaseHelper", ex.getMessage());
            }
        }
    }
    public SQLiteDatabase open()throws SQLException {
        return SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
    }
}

public class MainActivity extends AppCompatActivity {
    //переменные
    //общее количесвто дел
    int numberOfCases = 0;
    //количество побед
    int numberOfWins = 0;
    //количество поражений
    int numberOfLoses = 0;
    //возожные результаты
    int possibleResults;
    //логический контроль состояний поиска по другим критериям
    int otherCriteria = 0;

    //массив для номеров дел и состояния соответсвия
    String[][] massiveCases = new String[45] [2];
    //массив для контроля состояний чекбоксов по критериям
    String[][] massiveTypePayments = new String[4][2];
    String[][] massiveResults =  new String[2][2];

    //объявление визуальныъ элементов
    TextView allCases;
    TextView winCases;
    TextView losesCases;
    TextView posssibleResults;

    // объявление переменных для работы с внутренней базой данных
    DatabaseHelper databaseHelper;
    SQLiteDatabase db;
    Cursor userCursor;

    //финальный массив кейсов, который в итоге идет в лист вью
    public static ArrayList<cases> infoCases = new ArrayList<>();

    //Функция, которая обновляет (проставляет истина, если соответсвуети, и ложь, если нет) массив номеров дел по типам выплат
    public void paymentsTypeCriteria(View view ){
        int caseCounter = 0;
        otherCriteria = 0;
        possibleResults = 0;
        CheckBox checkBox = (CheckBox)  view;
        databaseHelper = new DatabaseHelper(getApplicationContext());
        // создаем базу данных
        databaseHelper.create_db();
        // открываем подключение
        db = databaseHelper.open();
        //получаем данные из бд в виде курсора
        userCursor = db.rawQuery("select * from " + DatabaseHelper.TABLE, null);
        // определяем, какие столбцы из курсора будут выводиться в ListView
        userCursor.moveToFirst();

        // Обновляем состояние кнопок по типу выплат
        for(int x = 0; x<=3; x++){
            if(checkBox.getTag().equals(massiveTypePayments[x][0]) && checkBox.isChecked()){
                massiveTypePayments[x][1] = "true";
            } else if (checkBox.getTag().equals(massiveTypePayments[x][0]) && !checkBox.isChecked()){
                massiveTypePayments[x][1] = "false";
            }
        }

        //Проверяем состояние кнопок по результату спора
        for(int x = 0; x<=1; x++){
            if(massiveResults[x][1].equals("true"))
            {
                otherCriteria = 1;
                break;
            }
        }

        while(!userCursor.isAfterLast()) {
            String name = userCursor.getString(0);
            //СЛУЧАЙ КОГДА ЕСТЬ ИНЫЕ АКТВИНЫЕ КРИТЕРИИ
            if(otherCriteria == 1)
            {
                for (int x = 0; x <= 1; x++) {
                    if (massiveResults [x][1].equals("true")) {
                        //ПРОСТАВЛЕНИЕ ОТМЕТКИ ПО КРИТЕРИЮ
                        if (checkBox.isChecked()
                                && name != null
                                && name.equals(massiveCases[caseCounter][0])
                                && checkBox.getTag().equals(userCursor.getString(3))
                                && massiveResults[x][0].equals(userCursor.getString(2)))
                        {
                            massiveCases[caseCounter][1] = "true";
                        } else if(name != null
                                && (!massiveResults[x][0].equals(userCursor.getString(2)) || !checkBox.getTag().equals(userCursor.getString(3))))
                        {
                            massiveCases[caseCounter][1] = "false";
                        }
                        // СНЯТИЕ ОТМЕТКИ С КРИТЕРИЯ
                        if (!checkBox.isChecked()
                                && name != null
                                && name.equals(massiveCases[caseCounter][0])
                                && massiveResults[x][0].equals(userCursor.getString(2)))
                        {
                            massiveCases[caseCounter][1] = "true";
                        } else if(!checkBox.isChecked()
                                && name != null
                                && name.equals(massiveCases[caseCounter][0])
                                && !massiveResults[x][0].equals(userCursor.getString(2)))
                        {
                            massiveCases[caseCounter][1] = "false";
                        }
                    }
                }
            }
            //СЛУЧАЙ КОГДА НЕТ ИНЫХ АКТИВНЫХ КРИТЕРИЕВ
            else if(otherCriteria == 0)
            {
                //ПРОСТАВЛЕНИЕ ОТМЕТКИ
                if (checkBox.isChecked() && name != null && name.equals(massiveCases[caseCounter][0]) && checkBox.getTag().equals(userCursor.getString(3))) {
                    if (!massiveCases[caseCounter][1].equals("true")){
                        massiveCases[caseCounter][1] = "true";
                    }
                }
                // СНЯТИЕ ОТМЕТКИ
                else if (!checkBox.isChecked() && name != null && name.equals(massiveCases[caseCounter][0]) && checkBox.getTag().equals(userCursor.getString(3)))
                {
                    massiveCases[caseCounter][1] = "false";
                }
            }
            userCursor.moveToNext();
            caseCounter++;
        }

        // Считаем сколько релевантных дел в массиве
        for(int x = 0; x<=44; x++){
            if(massiveCases[x][1] != null && massiveCases[x][1].equals("true")){
                possibleResults++;
            }
        }

        //оторбражаем количество релевантных дел на визульаный элемент
        posssibleResults.setText("Количество дел соответствующих запросу: " + String.valueOf(possibleResults));
        userCursor.close();
        db.close();
    }

    //Функция, которая обновляет (проставляет истина, если соответсвуети, и ложь, если нет) массив номеров дел по критерию проиграно/выиграно
    public void winLoseCriteria(View view){
        int caseCounter = 0;
        otherCriteria = 0;
        possibleResults = 0;
        CheckBox checkBox = (CheckBox)  view;
        databaseHelper = new DatabaseHelper(getApplicationContext());
        // создаем базу данных
        databaseHelper.create_db();
        // открываем подключение
        db = databaseHelper.open();
        //получаем данные из бд в виде курсора
        userCursor = db.rawQuery("select * from " + DatabaseHelper.TABLE, null);
        // определяем, какие столбцы из курсора будут выводиться в ListView
        userCursor.moveToFirst();

        // Обновляем состояние кнопок по результату
        for(int x = 0; x<=1; x++){
            if(checkBox.getTag().equals(massiveResults[x][0]) && checkBox.isChecked()){
                massiveResults[x][1] = "true";
            } else if (checkBox.getTag().equals(massiveResults[x][0]) && !checkBox.isChecked()){
                massiveResults[x][1] = "false";
            }
        }

        // Проверяем состояние кнопок по типу выплат
        for(int x = 0; x<=3; x++){
            if(massiveTypePayments[x][1].equals("true")){
                otherCriteria = 1;
                break;
            }
        }

        while(!userCursor.isAfterLast()) {
            String name = userCursor.getString(0);
            // ДРУГИЕ КРИТЕРИИ ПРОСТАВЛЕНЫ
            if(otherCriteria == 1) {
                for (int x = 0; x <= 3; x++) {
                    if (massiveTypePayments[x][1].equals("true")) {

                        //ПРОСТАВЛЕНИЕ ОТМЕТКИ ПО КРИТЕРИЮ
                        if (checkBox.isChecked()
                                && name != null
                                && name.equals(massiveCases[caseCounter][0])
                                && checkBox.getTag().equals(userCursor.getString(2))
                                && massiveTypePayments[x][0].equals(userCursor.getString(3)))
                        {
                            massiveCases[caseCounter][1] = "true";

                        } else if(checkBox.isChecked()
                                && name != null
                                && (!checkBox.getTag().equals(userCursor.getString(2))
                                || !massiveTypePayments[x][0].equals(userCursor.getString(3))))
                        {
                            massiveCases[caseCounter][1] = "false";
                        }

                        // СНЯТИЕ ОТМЕТКИ С КРИТЕРИЯ
                        if (!checkBox.isChecked()
                                && name != null
                                && name.equals(massiveCases[caseCounter][0])
                                && massiveTypePayments[x][0].equals(userCursor.getString(3)))
                        {
                            massiveCases[caseCounter][1] = "true";
                        } else if(!checkBox.isChecked()
                                && name != null
                                && name.equals(massiveCases[caseCounter][0])
                                && !massiveTypePayments[x][0].equals(userCursor.getString(3)))
                        {
                            massiveCases[caseCounter][1] = "false";
                        }
                    }
                }
            }
            // ДРУГИЕ КРИТЕРИИ НЕ ПРОСТАВЛЕНЫ
            else if (otherCriteria == 0)
            {
                //ПРОСТАВЛЕНИЕ ОТМЕТКИ
                if (checkBox.isChecked() && name != null && name.equals(massiveCases[caseCounter][0]) && checkBox.getTag().equals(userCursor.getString(2))) {
                    if (!massiveCases[caseCounter][1].equals("true")){
                        massiveCases[caseCounter][1] = "true";
                    }
                }

                // СНЯТИЕ ОТМЕТКИ
                else if (!checkBox.isChecked() && name != null && name.equals(massiveCases[caseCounter][0]) && checkBox.getTag().equals(userCursor.getString(2)))
                {
                        massiveCases[caseCounter][1] = "false";
                }
            }

            userCursor.moveToNext();
            caseCounter++;
        }
        // Считаем сколько релевантных дел в массиве
        for(int x = 0; x<=44; x++){
            if(massiveCases[x][1] != null && massiveCases[x][1].equals("true")){
                possibleResults++;
            }
        }
        //оторбражаем количество релевантных дел на визульаный элемент
        posssibleResults.setText("Количество дел соответствующих запросу: " + String.valueOf(possibleResults));
        userCursor.close();
        db.close();

    }

    // Функция, которая первоначально наполняет массив и дает первую статистику по делам имеющимся в базе
    public void dataCheck (){
        databaseHelper = new DatabaseHelper(getApplicationContext());
        // создаем базу данных
        databaseHelper.create_db();
        // открываем подключение
        db = databaseHelper.open();
        //получаем данные из бд в виде курсора
        userCursor = db.rawQuery("select * from " + DatabaseHelper.TABLE, null);
        // определяем, какие столбцы из курсора будут выводиться в ListView
        userCursor.moveToFirst();

        while(!userCursor.isAfterLast()) {
            String name = userCursor.getString(1);
            if (name != null && !name.equals("company")){
                massiveCases[numberOfCases][0] = userCursor.getString(0);
                massiveCases[numberOfCases][1] = "false";
                //считаем общее количество дел
                numberOfCases++;

                //считаем количество побед
                if(userCursor.getString(2).equals("выигран")){
                    numberOfWins++;

                    // считаем количество поражений
                } else if (userCursor.getString(2).equals("проигран")){
                    numberOfLoses++;
                }
            }
            userCursor.moveToNext();
        }
        userCursor.close();
        db.close();
    }

    //функция, которая открывает новое активити, где отображаются результаты поиска
    public void showResults(View view){
        //чистим массив, который отправляется в финальный лист вью
        infoCases.clear();
        int caseCounter = 0;
        Button button = (Button) view;
        databaseHelper = new DatabaseHelper(getApplicationContext());
        // создаем базу данных
        databaseHelper.create_db();
        // открываем подключение
        db = databaseHelper.open();
        //получаем данные из бд в виде курсора
        userCursor = db.rawQuery("select * from " + DatabaseHelper.TABLE, null);
        // определяем, какие столбцы из курсора будут выводиться в ListView
        userCursor.moveToFirst();
        //бежим курсором по базе и собираем данные в массив данные по тем делам, которые соответствовали выбранным критериям
        while(!userCursor.isAfterLast()){
            String name = userCursor.getString(0);
            if (name != null && name.equals(massiveCases[caseCounter][0]) && massiveCases[caseCounter][1].equals("true")){
                infoCases.add(new cases(userCursor.getString(1), userCursor.getString(2), userCursor.getString(3), userCursor.getString(4)));
            }
            caseCounter++;
            userCursor.moveToNext();
        }
        //открываем активити
        Intent intent = new Intent(getApplicationContext(), ResultActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //скрываем заголовок приложения
        getSupportActionBar().hide();

        //заполняем массивы для контроля состояния кнопок
        massiveTypePayments[0][0] = "роялти";
        massiveTypePayments[0][1] = "false";
        massiveTypePayments[1][0] = "проценты";
        massiveTypePayments[1][1] = "false";
        massiveTypePayments[2][0] = "дивиденды";
        massiveTypePayments[2][1] = "false";
        massiveTypePayments[3][0] = "доход от продажи акций";
        massiveTypePayments[3][1] = "false";

        massiveResults[0][0] = "выигран";
        massiveResults[0][1] = "false";
        massiveResults[1][0] = "проигран";
        massiveResults[1][1] = "false";

        // инициализируем визуальные элементы
        allCases = (TextView) findViewById(R.id.allCases);
        winCases = (TextView) findViewById(R.id.winCases);
        losesCases = (TextView) findViewById(R.id.lostCases);
        posssibleResults = (TextView)  findViewById(R.id.possibleResults);

        // пробегаемся по базе, чтобы собрать первоначальную статистику по делам
        dataCheck();

        //отправляем первоначальные данные по базе в визаульные элементы
        allCases.setText(String.valueOf(numberOfCases));
        winCases.setText(String.valueOf(numberOfWins));
        losesCases.setText(String.valueOf(numberOfLoses));
        posssibleResults.setText("Количесвто дел соответствующих запросу: " + String.valueOf(possibleResults));
    }

}