package com.helius.security;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

import com.helius.entities.*;



public interface UserService {
	
	User findById(long id);
	
	Employee_Selfcare_Users findByName(String name);
	
	void saveUser(Employee_Selfcare_Users user_inmemory);
	
	void updateUser(Employee_Selfcare_Users user_inmemory);
	
	void deleteUserById(long id);

	List<Employee_Selfcare_Users> findAllUsers(); 
	
	void deleteAllUsers();
	
	public boolean isUserExist(Employee_Selfcare_Users user_inmemory);
	public void populateDummyUsers(DataSource datasource);
	

	
	
}
