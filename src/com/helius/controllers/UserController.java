package com.helius.controllers;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.helius.entities.Employee;
import com.helius.entities.Employee_Selfcare_Users;
import com.helius.entities.User;
import com.helius.entities.Users;
import com.helius.managers.EmployeeManager;
import com.helius.managers.UserManager;
import com.helius.service.UserService;
import com.helius.service.UserServiceImpl;
import com.helius.utils.Logindetails;
import com.helius.utils.Status;


@RestController
public class UserController {
    @Autowired
    private UserService userService;
    private UserServiceImpl userServiceImp;
    @Autowired
    private UserManager userManager;
    @Autowired
    ApplicationContext context;
    @Autowired
	private EmployeeManager employeemanager;
    @CrossOrigin
    @RequestMapping(value = "/registration", method = RequestMethod.POST)
    public @ResponseBody String registration(@RequestBody Users user,String createLoginFlag) {
      System.out.println("userForm===+model===="+user.getEmployee_id());
      UserManager userManager = (UserManager)context.getBean("userManager");
      createLoginFlag = "N";
		Status status = userManager.createUserLogin(user,createLoginFlag);	
		return "{\"response\":\"" + status.getMessage()+"\"}";
    }
    
	
    /*@CrossOrigin
	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public @ResponseBody String login(@RequestHeader("Authorization") String Authorization) {
		final String authorization = Authorization;
		System.out.println("===authorization==" + authorization);
		// final String authorization = httpRequest.getHeader("Authorization");

		String base64Credentials = authorization.substring("Basic".length()).trim();
		String credentials = new String(Base64.getDecoder().decode(base64Credentials));
		final String[] values = credentials.split(":", 2);
		String	usrId = values[0];
		String password = values[1];
		Status status = userManager.verifyLogin(usrId, password);
		String jsonresponse = "{\"response\":{";
		String result = "";	
		Users usr = userManager.getUsers(usrId);
		String usrnames= null;
		String roleid = null;
		if(usr!=null){
		 roleid = usr.getHelius_role();
		Employee employee = employeemanager.getEmployee(usrId);
		
		if(employee!=null){
		 usrnames = employee.getEmployeePersonalDetails().getEmployee_id();
		}
		}
		if (status.getMessage().equalsIgnoreCase("success")) {
			result = "\"result\":\"success\", \"message\":\"\", \"role\":\"" + roleid +"\",\"username\":\"" + usrnames +"\"";

		} else {
			result = "\"result\":\"failure\",\"message\":\"" + status.getMessage() + "\"";
		}

		return "{\"response\":{" + result + "}}";

	} */
	
    
    
	@CrossOrigin
	@RequestMapping(value = "/activation", method = RequestMethod.POST)
	public @ResponseBody String activation(@RequestHeader("Authorization") String Authorization) {
		final String authorization = Authorization;
		String password = null;
		String username = null;
		// final String authorization = httpRequest.getHeader("Authorization");
	//	if (authorization != null && authorization.startsWith("Basic")) {
			// Authorization: Basic base64credentials
			String base64Credentials = authorization.substring("Basic".length()).trim();
			
			// UserManager userManager =
			// (UserManager)context.getBean("userManager");
			Status status = userManager.activateAccount(base64Credentials);
			//return "{\"response\":\"" + status.getMessage() + "\"}";
		//} else {		
		//return "unable to process activation bad request";
			String jsonresponse = "{\"response\":{";
			String result = "";
			if (status.getMessage().equalsIgnoreCase("success")) {
				result = "\"result\":\"success\", \"message\":\"" + "Account Activated Successfully Please Login\"";
			} else {
				result = "\"result\":\"failure\",\"message\":\"" + status.getMessage() + "\"";
			}
			return "{\"response\":{" + result + "}}";
		
	}

	@CrossOrigin
	@RequestMapping(value = "/activation12", method = RequestMethod.POST)
	public @ResponseBody String activation12(@RequestHeader("Authorization") String Authorization,String forgot ) {
		final String authorization = Authorization;
		String password = null;
		String username = null;
		// final String authorization = httpRequest.getHeader("Authorization");
	//	if (authorization != null && authorization.startsWith("Basic")) {
			// Authorization: Basic base64credentials
			String base64Credentials = authorization.substring("Basic".length()).trim();
			
			// UserManager userManager =
			// (UserManager)context.getBean("userManager");
			Status status = userManager.activateAccount12(base64Credentials,forgot);
			//return "{\"response\":\"" + status.getMessage() + "\"}";
		//} else {		
		//return "unable to process activation bad request";
			String jsonresponse = "{\"response\":{";
			String result = "";
			if (status.getMessage().equalsIgnoreCase("success")) {
				result = "\"result\":\"success\", \"message\":\"" + "Account updated Successfully Please Login\"";
			} else {
				result = "\"result\":\"failure\",\"message\":\"" + status.getMessage() + "\"";
			}
			return "{\"response\":{" + result + "}}";
		
	}
	
	@CrossOrigin
	@RequestMapping(value = "verifyEmailAddress/", method = RequestMethod.GET)
	public @ResponseBody String verifyEmailAdress(@RequestParam String employeeid,String email) {
		Status status = userManager.verifyEmailadress(employeeid,email);
		String jsonresponse = "{\"response\":{";
		String result = "";
		if (status.getMessage().equalsIgnoreCase("success")) {
			result ="\"result\":\"success\", \"message\":\"" + "Password Reset link has been send to your registered Email Address\""; 
		} else {
			result = "\"result\":\"failure\",\"message\":\"" + status.getMessage() + "\"";
		}
		return "{\"response\":{" + result + "}}";
	}
	
	 @CrossOrigin
		@RequestMapping(value = "/forgotPwd", method = RequestMethod.POST)
	public @ResponseBody String forgotPwd(@RequestHeader("Authorization") String Authorization) {
		final String authorization = Authorization;
		System.out.println("===authorization==" + authorization);
		// final String authorization = httpRequest.getHeader("Authorization");

		String base64Credentials = authorization.substring("Basic".length()).trim();
		Status status = userManager.forgotPassword(base64Credentials);
		String jsonresponse = "{\"response\":{";
		String result = "";
		if (status.getMessage().equalsIgnoreCase("success")) {
			result = "\"result\":\"success\", \"message\":\"" + "Password Changed Successfully Please Login\"";

		} else {
			result = "\"result\":\"failure\",\"message\":\"" + status.getMessage() + "\"";
		}

		return "{\"response\":{" + result + "}}";

	}
	
	@CrossOrigin
    @RequestMapping(value = "user/createuser", method = RequestMethod.POST, consumes = { "multipart/form-data" })
	public @ResponseBody String createUser(@RequestParam("user") String userjson) {
		System.out.println("userjson : " + userjson);
		ObjectMapper obm = new ObjectMapper();
		Status status = null;
		Employee_Selfcare_Users user;
		try {
			user = obm.readValue(userjson, Employee_Selfcare_Users.class);
			UserManager userManager = (UserManager) context.getBean("userManager");

			status = userManager.createUser(user);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "{\"response\":\"" + status.getMessage() + "\"}";
	}
	@CrossOrigin
    @RequestMapping(value = "user/updateuser", method = RequestMethod.POST, consumes = { "multipart/form-data" })
	public @ResponseBody String updateUser(@RequestParam("user") String userjson) {
		System.out.println("updateuser userjson : " + userjson);
		ObjectMapper obm = new ObjectMapper();
		Status status = null;
		Employee_Selfcare_Users user;
		try {
			user = obm.readValue(userjson, Employee_Selfcare_Users.class);
			UserManager userManager = (UserManager) context.getBean("userManager");
			status = userManager.updateUser(user);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return "{\"response\":\"" + status.getMessage() + "\"}";
	}  
	
	@CrossOrigin
    @RequestMapping(value = "user/deleteuser", method = RequestMethod.POST, consumes = { "multipart/form-data" })
	public @ResponseBody String deleteUser(@RequestParam("user") String userjson) {
		System.out.println("delete user userjson : " + userjson);
		ObjectMapper obm = new ObjectMapper();
		Status status = null;
		Employee_Selfcare_Users user;
		try {
			user = obm.readValue(userjson, Employee_Selfcare_Users.class);
			UserManager userManager = (UserManager) context.getBean("userManager");

			status = userManager.updateUser(user);
		} catch (IOException e) {
			
			e.printStackTrace();
		}

		return "{\"response\":\"" + status.getMessage() + "\"}";
	}  
	@CrossOrigin
    @RequestMapping(value = "user/getAllUsers", method = RequestMethod.GET, produces = { "multipart/form-data" })
	public @ResponseBody String getAllUsers() {
		UserManager userManager = (UserManager) context.getBean("userManager");
		ObjectMapper obm = new ObjectMapper();
		String response  = null;
		try {
			List<com.helius.utils.User> users = userManager.getAllUsers();
			response = obm.writeValueAsString(users);
		} catch (Throwable e) {
			response =e.getMessage();
		
		}
		return response;
	}

	@CrossOrigin
   	@RequestMapping(value = "/login", method = RequestMethod.POST)
   	public @ResponseBody String login(@RequestHeader("Authorization") String Authorization) {
   		final String authorization = Authorization;
   		System.out.println("===authorization==" + authorization);
   		// final String authorization = httpRequest.getHeader("Authorization");

   		String base64Credentials = authorization.substring("Basic".length()).trim();
   		String credentials = new String(Base64.getDecoder().decode(base64Credentials));
   		final String[] values = credentials.split(":", 2);
   		String	usrId = values[0];
   		String password = values[1];
   		
   		Logindetails logindetails;
   		String response = "";
		try {
			logindetails = userManager.validateUser(usrId, password);
			ObjectMapper obm = new ObjectMapper();
			response =  obm.writeValueAsString(logindetails);
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
   		
		return response;
   	}
	
	@CrossOrigin
    @RequestMapping(value = "user/changepassword", method = RequestMethod.POST, consumes = { "multipart/form-data" })
	public @ResponseBody String changePassword(@RequestParam("user") String userjson) {
		System.out.println("change password userjson : " + userjson);
		ObjectMapper obm = new ObjectMapper();
		Status status = null;
		Employee_Selfcare_Users user;
		try {
			user = obm.readValue(userjson, Employee_Selfcare_Users.class);
			UserManager userManager = (UserManager) context.getBean("userManager");

			status = userManager.updateUser(user);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if(status.isOk()) {
			return "{\"response\":\"" + "change password is successful" + "\"}";
		}
		return "{\"response\":\"" + status.getMessage() + "\"}";
	}  

}
