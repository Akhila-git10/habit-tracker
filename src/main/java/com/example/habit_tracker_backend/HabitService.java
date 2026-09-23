package com.example.habit_tracker_backend;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class HabitService {

    private HabitRepository habitRepository;

    public HabitService(HabitRepository habitRepository) {
        this.habitRepository = habitRepository;
    }

    public Habit addHabit(Habit habit) {
        return habitRepository.save(habit);
    }

    public List<Habit> getAllHabits() {
        return habitRepository.findAll();
    }

    public Habit findById(int id) {

        return habitRepository.findById(id).orElse(null);
    }

    public Habit updateHabit(int id, Habit updatedHabit) {
        Habit existingHabit = findById(id);
        existingHabit.name = updatedHabit.name;
        existingHabit.description = updatedHabit.description;
        existingHabit.id = id;
        return habitRepository.save(existingHabit);
    }

    public void deleteHabit(int id) {
        habitRepository.deleteById(id);
    }
}
