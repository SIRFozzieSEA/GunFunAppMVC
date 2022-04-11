package com.codef.gunfunmvc.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.codef.gunfunmvc.models.entities.User;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {

	User findByEmail(String email);
	User findByUserName(String userName);

}

