package com.example.demo.repository;

import com.example.demo.model.Project;
import com.example.demo.model.ProjectParticipation;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectParticipationRepository extends JpaRepository<ProjectParticipation, Long> {
    List<ProjectParticipation> findAllByUserAndProject(User user, Project project);
    List<ProjectParticipation> findAllByUser(User user);
    List<ProjectParticipation> findAllByProject(Project project);
}
