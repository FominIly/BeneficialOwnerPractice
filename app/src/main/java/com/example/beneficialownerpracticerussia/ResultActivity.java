package com.example.beneficialownerpracticerussia;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ResultActivity extends AppCompatActivity {

    //создаем свой адаптер, чтобы заполнить кастомный лист вью
    public class Adapter extends ArrayAdapter<cases> {
        private Context context;
        private List<cases> casesInfo;

        //конструктор
        public Adapter(Context context, int resource, ArrayList<cases> objects) {
            super(context, resource, objects);
            this.context = context;
            this.casesInfo = objects;
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            cases property = casesInfo.get(position);

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.custom_list_view, null); // передаем кастомный лист вью

            //цвет бэкграунда элементов лист вью меняется через один
            LinearLayout info = (LinearLayout)  view.findViewById(R.id.infoSection);
            if(position % 2 != 0){
                info.setBackground((ContextCompat.getDrawable(context, R.drawable.final_result_1)));
            } else {
                info.setBackground((ContextCompat.getDrawable(context, R.drawable.final_result_2)));
            }

            TextView caseName = (TextView) view.findViewById(R.id.caseName);
            TextView caseResult = (TextView) view.findViewById(R.id.result);
            TextView typePayments = (TextView) view.findViewById(R.id.typeOfPayments);
            TextView reference = (TextView) view.findViewById(R.id.reference);

            caseName.setText(property.getCaseName());
            caseResult.setText(property.getCaseResult());

            //меняем цвет в зависимости от результата
            if(property.getCaseResult().equals("проигран")){
                caseResult.setTextColor(Color.parseColor("#cc0000"));
            } else{
                caseResult.setTextColor(Color.parseColor("#009900"));
            }

            typePayments.setText(property.getTypeOfPayment());
            reference.setText(property.getCaseReference());

            return view;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        getSupportActionBar().hide();

        //Передаем в адаптер массив, созданный на главном экране
        ArrayAdapter<cases> adapter = new Adapter(this, 0, MainActivity.infoCases);

        //Наполняем лист вью
        ListView listView = (ListView) findViewById(R.id.resultListView);
        listView.setAdapter(adapter);

    }
}