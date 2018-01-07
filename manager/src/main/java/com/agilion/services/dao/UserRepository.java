package com.agilion.services.dao;

import com.agilion.domain.app.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Do some spring boot magic, yo
 */
public interface UserRepository extends JpaRepository<User, Long>
{
    User findByUsername(String username);
}
