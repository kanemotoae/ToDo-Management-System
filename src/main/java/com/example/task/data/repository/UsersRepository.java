package com.example.task.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.task.data.entity.Users;

public interface UsersRepository extends JpaRepository<Users, String> {

}