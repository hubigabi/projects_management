package com.example.demo.service;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import com.example.demo.ui.LoggedUI;
import com.google.common.hash.Hashing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.TimeZone;

@Service
public class InitService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    ProjectParticipationRepository projectParticipationRepository;

    @Autowired
    SprintRepository sprintRepository;

    @Autowired
    TaskRepository taskRepository;

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));

        User u1 = new User(0L, "Adam01", Hashing.sha512().hashString("Adam01", StandardCharsets.UTF_8).toString(), "Adam01@gmail.com", "Adam01");
        User u2 = new User(0L, "Marcin01", Hashing.sha512().hashString("Marcin01", StandardCharsets.UTF_8).toString(), "Marcin01@gmail.com", "Marcin01");
        User u3 = new User(0L, "Filip01", Hashing.sha512().hashString("Filip01", StandardCharsets.UTF_8).toString(), "Filip01@gmail.com", "Filip01");
        User u4 = new User(0L, "Adrian01", Hashing.sha512().hashString("Adrian01", StandardCharsets.UTF_8).toString(), "Adrian01@gmail.com", "Adrian01");
        u1 = userRepository.save(u1);
        u2 = userRepository.save(u2);
        u3 = userRepository.save(u3);
        u4 = userRepository.save(u4);

        Project p1 = new Project(0L, "Project 1", "Description for project 1", u1);
        Project p2 = new Project(0L, "Project 2", "Description for project 2", u2);
        p1 = projectRepository.save(p1);
        p2 = projectRepository.save(p2);

        ProjectParticipation projectParticipation1 = new ProjectParticipation(0L, p1, u1);
        ProjectParticipation projectParticipation2 = new ProjectParticipation(0L, p2, u1);
        ProjectParticipation projectParticipation3 = new ProjectParticipation(0L, p1, u2);
        ProjectParticipation projectParticipation4 = new ProjectParticipation(0L, p2, u2);
        ProjectParticipation projectParticipation5 = new ProjectParticipation(0L, p1, u3);
        ProjectParticipation projectParticipation6 = new ProjectParticipation(0L, p2, u4);
        projectParticipationRepository.saveAll(Arrays.asList(projectParticipation1, projectParticipation2,
                projectParticipation3, projectParticipation4, projectParticipation5, projectParticipation6));


        Sprint s1 = sprintRepository.save(new Sprint(0L, LocalDate.now(), LocalDate.now().plusDays(7L), 50, p1));
        Sprint s2 = sprintRepository.save(new Sprint(0L, LocalDate.now().plusDays(10L), LocalDate.now().plusDays(18), 70, p1));
        Sprint s3 = sprintRepository.save(new Sprint(0L, LocalDate.now().plusDays(6L), LocalDate.now().plusDays(12L), 60, p2));
        Sprint s4 = sprintRepository.save(new Sprint(0L, LocalDate.now().plusDays(16), LocalDate.now().plusDays(24), 70, p2));

        taskRepository.save(new Task(0L, "Task 1", "Description for task 1", s1, 3, 15, Progress.IN_PROGRESS, u1));
        taskRepository.save(new Task(0L, "Task 2", "Description for task 2", s1, 5, 30, Progress.DONE, u1));
        taskRepository.save(new Task(0L, "Task 3", "Description for task 3", s1, 4, 15, Progress.BACKLOG, u3));
        taskRepository.save(new Task(0L, "Task 4", "Description for task 4", s2, 5, 30, Progress.TODO, u3));
        taskRepository.save(new Task(0L, "Task 5", "Description for task 5", s2, 2, 25, Progress.DONE, u2));
        taskRepository.save(new Task(0L, "Task 6", "Description for task 6", s2, 4, 15, Progress.QA, u1));
        taskRepository.save(new Task(0L, "Task 1", "Description for task 1", s3, 5, 20, Progress.IN_PROGRESS, u1));
        taskRepository.save(new Task(0L, "Task 2", "Description for task 2", s3, 1, 25, Progress.TODO, u2));
        taskRepository.save(new Task(0L, "Task 3", "Description for task 3", s3, 5, 15, Progress.IN_PROGRESS, u2));
        taskRepository.save(new Task(0L, "Task 4", "Description for task 4", s4, 2, 30, Progress.QA, u1));
        taskRepository.save(new Task(0L, "Task 5", "Description for task 5", s4, 5, 20, Progress.DONE, u4));
        taskRepository.save(new Task(0L, "Task 6", "Description for task 6", s4, 4, 10, Progress.IN_PROGRESS, u4));
    }

}
