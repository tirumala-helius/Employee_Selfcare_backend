package com.helius.managers;

import java.util.List;

import org.hibernate.Session;

import com.helius.utils.User;
import com.helius.entities.Users;
import com.helius.service.UserService;

import com.helius.utils.Logindetails;

import com.helius.utils.Status;


public class UserManager {

	UserService userService;
	Users users;

	public UserService getUserService() {
		return userService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public Users getUsers() {
		return users;
	}

	public void setUsers(Users users) {
		this.users = users;
	}

	public Users getUsers(String employeeid) {
		Users usr = null;
		try {
			usr = userService.get(employeeid);
		} catch (Throwable e) {
			return usr;
		}
		return usr;
	}

	public Status createUserLogin(Users user,String createLoginFlag) {
		try {
			userService.save(user,createLoginFlag);
		} catch (Throwable e) {
			return new Status(false, e.getMessage());
		}
		return new Status(true, "Employee Details saved successfully");
	}
	
	public Status createUserLogin(Users user,String createLoginFlag,Session session) {
		try {
			userService.save(user,createLoginFlag,session);
		} catch (Throwable e) {
			return new Status(false, e.getMessage());
		}
		return new Status(true, "Employee Details saved successfully");
	}

	public Status activateAccount(String base64Credentials) {
		try {
			userService.activateUserAccount(base64Credentials);
		} catch (Throwable e) {
			return new Status(false, e.getMessage());
		}
		return new Status(true, "success");
	}

	public Status activateAccount12(String base64Credentials,String forgot) {
		try {
			userService.activateUserAccount12(base64Credentials,forgot);
		} catch (Throwable e) {
			return new Status(false, e.getMessage());
		}
		return new Status(true, "success");
	}
	public Status verifyLogin(String usrId,String password) {
		String status = "";
		try {
			status = userService.verifyUserLogin( usrId, password);
		} catch (Throwable e) {
			return new Status(false, e.getMessage());
		}
		return new Status(true, status);
	}

	public Status verifyEmailadress(String employeeid,String email) {
		String status = "";
		try {
			status = userService.verifyForgotEmailAddress(employeeid,email);
		} catch (Throwable e) {
			return new Status(false, e.getMessage());
		}
		return new Status(true, status);
	}

	public Status forgotPassword(String base64Credentials) {
		String status = "";
		try {
			status = userService.forgotpswd(base64Credentials);
		} catch (Throwable e) {
			return new Status(false, e.getMessage());
		}
		return new Status(true, status);
	}
	
	public Status createUser(User user) {
		com.helius.entities.User user_entity = new com.helius.entities.User();
		
		user_entity.setActive("Yes");
		user_entity.setId(user.getId());
		user_entity.setUserid(user.getUserid()); 
		user_entity.setPassword(user.getPassword());
		user_entity.setUsername(user.getUsername());
		user_entity.setEdit(user.getEdit());
		user_entity.setView(user.getView());
		String roles = "";
		for(String role : user.getRole()){
			roles = role + "," + roles;
		}
		roles = roles.substring(0, roles.lastIndexOf(","));
		user_entity.setRole(roles);
		
		String countries = "";
		for(String country : user.getCountry()){
			countries = country + "," + countries;
		}
		countries = countries.substring(0, countries.lastIndexOf(","));
		user_entity.setCountry(countries);
		try {
			userService.createUser(user_entity);
		} catch (Throwable e) {
			
			return new Status(false, e.getMessage());
		}
		return new Status(true, "created user successfully");
	}
	
	public Status updateUser(User user) {
		com.helius.entities.User user_entity = new com.helius.entities.User();
		
		user_entity.setId(user.getId());
		user_entity.setUserid(user.getUserid()); 
		user_entity.setPassword(user.getPassword());
		user_entity.setUsername(user.getUsername());
		user_entity.setEdit(user.getEdit());
		user_entity.setView(user.getView());
		user_entity.setActive(user.getActive());
		String roles = "";
		for(String role : user.getRole()){
			roles = role + "," + roles;
		}
		roles = roles.substring(0, roles.lastIndexOf(","));
		user_entity.setRole(roles);
		
		String countries = "";
		for(String country : user.getCountry()){
			countries = country + "," + countries;
		}
		countries = countries.substring(0, countries.lastIndexOf(","));
		user_entity.setCountry(countries);
		try {
			userService.updateUser(user_entity);
		} catch (Throwable e) {
			
			return new Status(false, e.getMessage());
		}
		return new Status(true, "updated user successfully");
	}
	
	public List<com.helius.utils.User> getAllUsers() throws Throwable {
		return userService.getAllUsers();
		
		
	}

	
	public Logindetails validateUser(String username, String password) throws Throwable {
		return userService.validateUser(username, password);
	}
	public com.helius.utils.User getUser(String userid) throws Throwable{
		return userService.getUser(userid);
	}
}
