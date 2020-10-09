package com.example.jwtdemo.domain.repositories;

import com.example.jwtdemo.domain.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmail( String email);

    boolean existsByEmail(String email);

}
