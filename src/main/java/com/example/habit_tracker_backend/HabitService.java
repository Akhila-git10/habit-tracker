package com.example.habit_tracker_backend;

import java.util.ArrayList;
import java.util.List;

public class HabitService {
    
    private List<Habit> habits = new ArrayList<>();

    public void addHabit(Habit habit){
        habits.add(habit);
    }

    public List<Habit> getAllHabits() {
        return habits;
    }

    public Habit findById(int id){

        for(Habit habit :  habits){
            if(habit.id == id){
                return habit;
            }
        }
        return  null;
    }
}
