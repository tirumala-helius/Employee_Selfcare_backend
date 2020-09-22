package com.helius.managers;

import java.util.List;

import org.hibernate.Session;
import org.springframework.http.ResponseEntity;

import com.helius.utils.User;
import com.helius.entities.Employee_Selfcare_Users;
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

	/*public Users getUsers(String employeeid) {
		Users usr = null;
		try {
			usr = userService.get(employeeid);
		} catch (Throwable e) {
			return usr;
		}
		return usr;
	}*/

	/*public Status createUserLogin(Users user,String createLoginFlag) {
		try {
			userService.save(user,createLoginFlag);
		} catch (Throwable e) {
			return new Status(false, e.getMessage());
		}
		return new Status(true, "Employee Details saved successfully");
	}*/
	
	/*public Status createUserLogin(Users user,String createLoginFlag,Session session) {
		try {
			userService.save(user,createLoginFlag,session);
		} catch (Throwable e) {
			return new Status(false, e.getMessage());
		}
		return new Status(true, "Employee Details saved successfully");
	}*/

	public Status activateAccount(String base64Credentials,String token) {
		try {
			userService.activateUserAccount(base64Credentials,token);
		} catch (Throwable e) {
			return new Status(false, e.getMessage());
		}
		return new Status(true, "Account Successfully Activated Please Login !");
	}

	/*public Status activateAccount12(String base64Credentials,String forgot) {
		try {
			userService.activateUserAccount12(base64Credentials,forgot);
		} catch (Throwable e) {
			return new Status(false, e.getMessage());
		}
		return new Status(true, "success");
	}*/
	/*public Status verifyLogin(String usrId,String password) {
		String status = "";
		try {
			status = userService.verifyUserLogin( usrId, password);
		} catch (Throwable e) {
			return new Status(false, e.getMessage());
		}
		return new Status(true, status);
	}*/

	public Status verifyEmailadress(String employeeid,String appUrl) throws Throwable {
		String status = "";
		try {
			status = userService.verifyForgotEmailAddress(employeeid,appUrl);
		} catch (Throwable e) {
			throw new Throwable(e.getMessage());
		}
		return new Status(true, status);
	}

	public Status resetPassword(String base64Credentials,String token,String fg) throws Throwable {
		String status = "";
		try {
			status = userService.resetpswd(base64Credentials,token,fg);
		} catch (Throwable e) {
			throw new Throwable(e.getMessage());
		}
		return new Status(true, status);
	}
	
	public ResponseEntity<byte[]> getPayslipFile(String userId,String month) {
		ResponseEntity<byte[]> res = null;
		try {
			 res =	userService.getPayslipFIle(userId,month);
		} catch (Throwable e) {
				return res;
		}
		return res;
	}
	
	public Status createUser(Employee_Selfcare_Users user) throws Throwable {
		try {
			userService.createUser(user);
		} catch (Throwable e) {
			throw new Throwable(e.getMessage());
		}
		return new Status(true, "created user successfully");
	}
	
	public Status updateUser(Employee_Selfcare_Users user) throws Throwable {
		try {
			userService.updateUser(user);
		} catch (Throwable e) {
			throw new Throwable(e.getMessage());
		}
		return new Status(true, "updated user successfully");
	}
	
	/*public List<com.helius.utils.User> getAllUsers() throws Throwable {
		return userService.getAllUsers();
		
		
	}*/

	
	public Logindetails validateUser(String username, String password) throws Throwable {
		return userService.validateUser(username, password);
	}
	/*public com.helius.utils.User getUser(String userid) throws Throwable{
		return userService.getUser(userid);
	}*/
}
