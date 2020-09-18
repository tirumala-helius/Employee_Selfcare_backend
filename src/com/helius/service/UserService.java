package com.helius.service;

import java.util.List;

import org.hibernate.Session;

import com.helius.entities.Employee_Selfcare_Users;
import com.helius.entities.User;
import com.helius.entities.Users;

public interface UserService {
	//public void save(Users user,String createLoginFlag) throws Throwable;

	//public Users get(String employeeid);
	
	public Employee_Selfcare_Users getUser(String userid)throws Throwable;

	public void activateUserAccount(String base64Credentials,String token) throws Throwable;

	//public String verifyUserLogin(String username,String password);

	public String forgotpswd(String base64Credentials,String token) throws Throwable;

	public String verifyForgotEmailAddress(String employeeid) throws Throwable;
	
	//void activateUserAccount12(String base64Credentials, String forgot) throws Throwable;

	//public void save(Users user, String createLoginFlag, Session session) throws Throwable;
	
	public void createUser(Employee_Selfcare_Users user) throws Throwable;
	public String updateUser(Employee_Selfcare_Users user) throws Throwable;
	//public String deleteUser(User user) throws Throwable;
	//public List<com.helius.utils.User> getAllUsers() throws Throwable;

	public com.helius.utils.Logindetails validateUser(String username, String password) throws Throwable;

}