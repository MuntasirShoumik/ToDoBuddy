package com.example.todobuddy.listeners;

import com.example.todobuddy.entity.Todos;

public interface TodoListener {

    void onClickedTodo(Todos todos,int position);
}
