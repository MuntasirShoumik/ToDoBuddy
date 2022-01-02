package com.example.todobuddy;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import com.example.todobuddy.adapter.TodoAdapter;
import com.example.todobuddy.database.TodoDatabase;
import com.example.todobuddy.entity.Todos;
import com.example.todobuddy.listeners.TodoListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TodoListener {

    int mYear, mMonth, mDay, mHour, mMinute;
    int selectedYear = -1, selectedMonthOfYear = -1, selectedDayOfMonth = -1;
    int selectedHourOfDay = -1,selectedMinute = -1;
    String selectedColor = "#FFFFFF";
    private RecyclerView todoRecyclerView;
    private List<Todos> todosList;
    private TodoAdapter todoAdapter;
    private Todos clickedTodo = null;
    private int clickedPosition;
    private boolean todoDeleted = false;
    boolean isAlarmSet = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        todoBottomSheet();

        todoRecyclerView = findViewById(R.id.toDo_recyclerView);
        todoRecyclerView.setLayoutManager(
                new GridLayoutManager(this,2)
        );
        todosList = new ArrayList<>();
        todoAdapter = new TodoAdapter(todosList,this::onClickedTodo);
        todoRecyclerView.setAdapter(todoAdapter);
        createNotificationChannel();
        getTodos();

        EditText search = findViewById(R.id.search_editText);

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                todoAdapter.cancelTimer();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(todosList.size() != 0 ){
                    todoAdapter.searchTodos(editable.toString());
                }
            }
        });

    }

    private void createNotificationChannel() {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

            CharSequence name = "alarm";
            String description = "reminder";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel("showAlarm",name,importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onClickedTodo(Todos todos,int position) {
        clickedTodo = todos;
        clickedPosition= position;

        todoBottomSheet();
    }

    private void todoBottomSheet(){

        final LinearLayout addToDoLinearLayout = findViewById(R.id.layout_add_todo);
        final BottomSheetBehavior<LinearLayout> bottomSheetBehavior =BottomSheetBehavior.from(addToDoLinearLayout);


        TextView saveTextButton = addToDoLinearLayout.findViewById(R.id.save_textView);
        TextView deleteTextButton = addToDoLinearLayout.findViewById(R.id.delete_textView);
        EditText title = addToDoLinearLayout.findViewById(R.id.title_input_editText);
        EditText description = addToDoLinearLayout.findViewById(R.id.description_input_editText);
        RadioButton red = addToDoLinearLayout.findViewById(R.id.redRadioButton);
        RadioButton orange = addToDoLinearLayout.findViewById(R.id.orangeRadioButton);
        RadioButton yellow = addToDoLinearLayout.findViewById(R.id.yellowRadioButton);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch alarmSwitch =addToDoLinearLayout.findViewById(R.id.alarm_switch);


        if(clickedTodo != null){
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            title.setText(clickedTodo.getTitle().toString().trim());
            selectedHourOfDay = clickedTodo.getHour(); selectedMinute = clickedTodo.getMinute();
            selectedYear = clickedTodo.getYear(); selectedMonthOfYear = clickedTodo.getMonth(); selectedDayOfMonth = clickedTodo.getDay();
            if(clickedTodo.getDescription()!= null){
                description.setText(clickedTodo.getDescription().toString().trim());
            }
            if(clickedTodo.isAlarmSet()){
                alarmSwitch.setChecked(true);
            }


            deleteTextButton.setVisibility(View.VISIBLE);

            switch (clickedTodo.getPriorityColor()){
                case "#FF0000":
                    red.performClick();
                    break;
                case "#FF521C":
                    orange.performClick();
                    break;
                case "#000000":
                    yellow.performClick();
                    break;
            }
        }

        addToDoLinearLayout.findViewById(R.id.text_what_to_do).setOnClickListener(new View.OnClickListener() {   //on clock listener on what to do text
            @Override
            public void onClick(View v) {
                clickedTodo = null;
                title.setText(null);
                description.setText(null);
                red.setChecked(false);
                orange.setChecked(false);
                yellow.setChecked(false);
                deleteTextButton.setVisibility(View.GONE);
                selectedColor = "#FFFFFF";
                selectedHourOfDay = -1; selectedMinute = -1; selectedYear = -1; selectedMonthOfYear = -1; selectedDayOfMonth = -1;
                alarmSwitch.setChecked(false);
                isAlarmSet = false;


                if(bottomSheetBehavior.getState() ==  bottomSheetBehavior.STATE_COLLAPSED){

                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }else {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });

        addToDoLinearLayout.findViewById(R.id.set_time_imageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Calendar c = Calendar.getInstance();
                mHour = c.get(Calendar.HOUR_OF_DAY);
                mMinute = c.get(Calendar.MINUTE);


                TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                selectedHourOfDay = hourOfDay;
                                selectedMinute = minute;
                            }
                        }, mHour, mMinute, false);
                timePickerDialog.show();
            }
        });


        addToDoLinearLayout.findViewById(R.id.set_date_imageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);


                DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                                selectedYear = year;
                                selectedMonthOfYear = monthOfYear;
                                selectedDayOfMonth = dayOfMonth;
                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();

            }
        });

        alarmSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){

                    isAlarmSet = true;
                }else {


                    isAlarmSet = false;
                }
            }
        });

        red.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedColor = "#FF0000";
            }
        });
        orange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedColor = "#FF9800";
            }
        });
        yellow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedColor = "#FFEB3B";
            }
        });

        saveTextButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (title.getText().toString().trim().isEmpty()){
                    Toast.makeText(MainActivity.this,"Title can't be empty",Toast.LENGTH_LONG).show();
                    return;
                }
                if(selectedHourOfDay == -1){
                    Toast.makeText(MainActivity.this,"You must set a time",Toast.LENGTH_LONG).show();
                    return;
                }
                if(selectedMonthOfYear == -1){
                    Toast.makeText(MainActivity.this,"You must set a date",Toast.LENGTH_LONG).show();
                    return;
                }
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                saveTodo(title,description,selectedHourOfDay,selectedMinute,selectedYear
                        ,selectedMonthOfYear,selectedDayOfMonth,selectedColor,isAlarmSet);

                deleteTextButton.setVisibility(View.GONE);
                title.setText(null);
                description.setText(null);
                red.setChecked(false);
                orange.setChecked(false);
                yellow.setChecked(false);
                selectedColor ="#FFFFFF";
                selectedHourOfDay = -1; selectedMinute = -1; selectedYear = -1;
                selectedMonthOfYear = -1; selectedDayOfMonth = -1;
                alarmSwitch.setChecked(false);
                isAlarmSet = false;

            }
        });
        deleteTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                class DeleteTodo extends AsyncTask<Void,Void,Void>{

                    @Override
                    protected Void doInBackground(Void... voids) {
                        TodoDatabase.getTodoDatabase(getApplicationContext()).todoDau().removeTodo(clickedTodo);
                        todoDeleted = true;
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void unused) {
                        super.onPostExecute(unused);
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        getTodos();
                        deleteTextButton.setVisibility(View.GONE);
                        title.setText(null);
                        description.setText(null);
                        red.setChecked(false);
                        orange.setChecked(false);
                        yellow.setChecked(false);
                        selectedColor ="#FFFFFF";
                        isAlarmSet = false;
                        if(clickedTodo.isAlarmSet())
                        deleteAlarm(clickedTodo);

                    }
                }
                new DeleteTodo().execute();
            }
        });

    }
    void saveTodo(EditText title,EditText description,int selectedHourOfDay,int selectedMinute
            ,int selectedYear,int selectedMonthOfYear,int selectedDayOfMonth,String selectedColor,boolean isAlarmSet){

        final Todos todos = new Todos();
        if (clickedTodo != null){
            todos.setId(clickedTodo.getId());
        }
        todos.setTitle(title.getText().toString().trim());

        todos.setDescription(description.getText().toString().trim());
        todos.setHour(selectedHourOfDay);
        todos.setMinute(selectedMinute);
        todos.setYear(selectedYear);
        todos.setMonth(selectedMonthOfYear);
        todos.setDay(selectedDayOfMonth);

        todos.setPriorityColor(selectedColor);
        todos.setAlarmSet(isAlarmSet);

        class saveTodosTask extends AsyncTask<Void,Void,Void>{
            @Override
            protected Void doInBackground(Void... voids) {
                TodoDatabase.getTodoDatabase(getApplicationContext()).todoDau().addTodo(todos);

                return null;
            }
        }
        new saveTodosTask().execute();

        getTodos();
        addAlarm();
    }

    void getTodos(){

        @SuppressLint("StaticFieldLeak")
        class getTodosTask extends AsyncTask<Void,Void, List<Todos>>{

            @Override
            protected List<Todos> doInBackground(Void... voids) {
                return TodoDatabase.getTodoDatabase(getApplicationContext()).todoDau().getAllTodos();
            }

            @Override
            protected void onPostExecute(List<Todos> todos) {
                super.onPostExecute(todos);
                if(clickedTodo != null){
                    if(todoDeleted){
                        todosList.remove(clickedPosition);
                        todoAdapter.notifyItemRemoved(clickedPosition);
                        clickedTodo = null;
                        todoDeleted = false;
                    }else {
                        todosList.remove(clickedPosition);
                        todosList.add(clickedPosition,todos.get(clickedPosition));
                        todoAdapter.notifyItemChanged(clickedPosition);
                        clickedTodo = null;
                    }

                }else if(todosList.size() == 0){
                    todosList.addAll(todos);
                    todoAdapter.notifyDataSetChanged();
                }
                else {
                    todosList.add(0,todos.get(0));
                    todoAdapter.notifyItemInserted(0);
                }
                todoRecyclerView.smoothScrollToPosition(0);
            }
        }
        new  getTodosTask().execute();
    }

    void addAlarm(){


        if(clickedTodo != null && isAlarmSet == false){

            if(clickedTodo.isAlarmSet()){

                deleteAlarm(clickedTodo);
                Log.d("a_tm", "saveTodo: "+"off  id:"+ clickedTodo.getId());
                return;
            }

        }

        if(clickedTodo != null && isAlarmSet){

            setNew(clickedTodo);
            Log.d("a_tm", "saveTodo: "+"set alarm on modification  id:"+clickedTodo.getId());
            return;
        }if(clickedTodo == null && isAlarmSet){
            class getTodosTask extends AsyncTask<Void,Void, List<Todos>>{

                @Override
                protected List<Todos> doInBackground(Void... voids) {
                    return TodoDatabase.getTodoDatabase(getApplicationContext()).todoDau().getAllTodos();
                }

                @SuppressLint("MissingPermission")
                @Override
                protected void onPostExecute(List<Todos> todos) {
                    super.onPostExecute(todos);

                    setNew(todos.get(0));

                    Log.d("a_tm", "saveTodo: "+"set alarm on new id: "+todos.get(0).getId());
                }
            }
            new  getTodosTask().execute();

        }
    }

    @SuppressLint("MissingPermission")
    void setNew(Todos todos){

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY,todos.getHour());
        calendar.set(Calendar.MINUTE,todos.getMinute());
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.YEAR,todos.getYear());
        calendar.set(Calendar.MONTH,todos.getMonth());
        calendar.set(Calendar.DAY_OF_MONTH,todos.getDay());

        AlarmManager alarmManager;
        PendingIntent pendingIntent;
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(MainActivity.this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this,todos.getId(),intent,0);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        Toast.makeText(MainActivity.this,"Alarm set successfully!",Toast.LENGTH_LONG).show();
    }


    void deleteAlarm(Todos todos){

        AlarmManager alarmManager;
        PendingIntent pendingIntent;
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(MainActivity.this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this,clickedTodo.getId(),intent,0);
        alarmManager.cancel(pendingIntent);
        Toast.makeText(MainActivity.this,"alarm canceled ",Toast.LENGTH_LONG).show();

    }
}