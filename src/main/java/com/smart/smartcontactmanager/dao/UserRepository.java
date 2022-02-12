package com.smart.smartcontactmanager.dao;

import com.smart.smartcontactmanager.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
}
