package com.example.habit_tracker_backend;

import java.util.List;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;

@RestController
public class HabitController {

    private HabitService habitService;

    public HabitController(HabitService habitService) {
        this.habitService = habitService;
    }

    @GetMapping("/habits")
    public List<Habit> getAllHabits() {
        return habitService.getAllHabits();
    }

    @GetMapping("/habits/{id}")
    public Habit getHabitById(@PathVariable int id) {
        return habitService.findById(id);
    }

    @PostMapping("/habits")
    public Habit addHabit(@RequestBody Habit habit) {
        habitService.addHabit(habit);
        return habit;
    }

    @PutMapping("/habits/{id}")
    public Habit updateHabit(@PathVariable int id, @RequestBody Habit habit) {
        return habitService.updateHabit(id, habit);
    }

    @DeleteMapping("/habits/{id}")
    public void deleteHabit(@PathVariable int id, @RequestBody Habit habit) {
        habitService.deleteHabit(id);
    }

}
