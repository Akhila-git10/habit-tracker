package com.example.habit_tracker_backend;

public class Habit {

    public int id;
    public String name;
    public String description;
    public boolean completedToday;

    Habit() {
}

    Habit(int id , String name , String description){
        this.id = id;
        this.name = name ;
        this.description = description;
        this.completedToday = false;
    }

    void toggleComplete(){
        this.completedToday = !this.completedToday;
    }

    void updateHabit(String name , String description){
        this.name = name;
        this.description = description;
    }

}