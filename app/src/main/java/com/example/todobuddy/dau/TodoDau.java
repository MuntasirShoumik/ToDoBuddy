package com.example.todobuddy.dau;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.todobuddy.entity.Todos;

import java.util.List;

@Dao
public interface TodoDau {

    @Query("SELECT *FROM todos ORDER BY id DESC")
    List<Todos> getAllTodos();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addTodo(Todos todos);

    @Delete
    void removeTodo(Todos todos);

    @Query("SELECT *FROM todos WHERE HourOfDay = :Hour AND Minute = :Min AND DayOfMonth = :day")
    List<Todos> getTitle(int Hour,int Min,int day);


}
