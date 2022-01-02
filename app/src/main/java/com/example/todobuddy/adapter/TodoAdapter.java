package com.example.todobuddy.adapter;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todobuddy.R;
import com.example.todobuddy.entity.Todos;
import com.example.todobuddy.listeners.TodoListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoViewHolder> {

    private List<Todos> todos;
    private TodoListener todoListener;
    private Timer timer;
    private List<Todos> todosSource;

    public TodoAdapter(List<Todos> todos,TodoListener todoListener) {

        this.todoListener = todoListener;
        this.todos = todos;
        todosSource = todos;
    }

    @NonNull
    @Override
    public TodoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TodoViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.container_layout,
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull TodoAdapter.TodoViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.setTodo(todos.get(position));
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                todoListener.onClickedTodo(todos.get(position),position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return todos.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    static class TodoViewHolder extends RecyclerView.ViewHolder{
        TextView title,dateTime,description;
        LinearLayout layout;

        public TodoViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.container_title_textView);
            description = itemView.findViewById(R.id.container_description_textView);
            dateTime = itemView.findViewById(R.id.container_date_time_textView);
            layout = itemView.findViewById(R.id.layout_container);

        }

        void setTodo(Todos todo){

            title.setText(todo.getTitle());


            description.setText(todo.getDescription());
            Log.d("description", "adapter des: "+todo.getPriorityColor());

            String time = todo.getHour()+" : "+todo.getMinute();
            String date = todo.getYear()+" / "+todo.getMonth()+" / "+todo.getDay();
            dateTime.setText("Date & Time:  "+date+"  at  "+time);
            title.setTextColor(Color.parseColor(todo.getPriorityColor()));



        }

    }

    public void searchTodos(final String searchKeyWord){
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(searchKeyWord.trim().isEmpty()){
                    todos = todosSource;
                }else {

                    ArrayList<Todos> temp = new ArrayList<>();
                    for(Todos todos: todosSource){
                        if(todos.getTitle().toLowerCase().contains(searchKeyWord.toLowerCase())
                                || todos.getDescription().toLowerCase().contains(searchKeyWord.toLowerCase())){
                            temp.add(todos);
                        }
                    }
                    todos = temp;
                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
            }
        },500);
    }

    public void cancelTimer(){
        if(timer !=null){
            timer.cancel();
        }
    }

}
