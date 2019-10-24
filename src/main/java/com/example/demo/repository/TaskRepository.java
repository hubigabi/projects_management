package com.example.demo.repository;

import com.example.demo.model.Progress;
import com.example.demo.model.Sprint;
import com.example.demo.model.Task;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findAllBySprint(Sprint sprint);
    List<Task> findAllBySprintAndAndProgress(Sprint sprint, Progress progress);
    List<Task> findAllByUser(User user);
    List<Task> findAllByProgress(Progress progress);
    List<Task> findAllByNameStartingWithIgnoreCase(String name);

    @Transactional
    void deleteAllBySprint(Sprint sprint);
}
