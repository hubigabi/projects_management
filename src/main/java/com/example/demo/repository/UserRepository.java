package com.example.demo.repository;

import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByLogin(String login);
    List<User> findAll();

    @Query(value = "SELECT login FROM user", nativeQuery = true)
    List<Object> findAllLogins();

    @Query(value = "SELECT email FROM user", nativeQuery = true)
    List<Object> findAllEmails();

    @Query(value = "SELECT name FROM user", nativeQuery = true)
    List<Object> findAllNames();
}
