package com.helius.controllers;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.helius.entities.Employee;
import com.helius.entities.Employee_Selfcare_Users;
import com.helius.entities.User;
import com.helius.entities.UserNavigationTracker;
import com.helius.entities.User_Navigation_Details;
import com.helius.entities.Users;
import com.helius.managers.EmployeeManager;
import com.helius.managers.UserManager;
import com.helius.service.UserService;
import com.helius.service.UserServiceImpl;
import com.helius.utils.Logindetails;
import com.helius.utils.Status;
import com.helius.utils.Utils;


@RestController
public class UserController {
    private static final int List = 0;
	@Autowired
    private UserService userService;
    private UserServiceImpl userServiceImp;
    @Autowired
    private UserManager userManager;
    @Autowired
    ApplicationContext context;

   /* @CrossOrigin
    @RequestMapping(value = "/registration", method = RequestMethod.POST)
    public @ResponseBody String registration(@RequestBody Users user,String createLoginFlag) {
      System.out.println("userForm===+model===="+user.getEmployee_id());
      UserManager userManager = (UserManager)context.getBean("userManager");
      createLoginFlag = "N";
		Status status = userManager.createUserLogin(user,createLoginFlag);	
		return "{\"response\":\"" + status.getMessage()+"\"}";
    }*/
    
	
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
	
    
    
	/*@CrossOrigin
	@RequestMapping(value = "/activation", method = RequestMethod.POST)
	public @ResponseBody String activation(@RequestHeader("Authorization") String Authorization,String token) {
		final String authorization = Authorization;
			String base64Credentials = authorization.substring("Basic".length()).trim();		
			Status status = userManager.activateAccount(base64Credentials,token);
			String result = "";
			if (status.getMessage().equalsIgnoreCase("success")) {
				result = "\"result\":\"success\", \"message\":\"" + "Account Activated Successfully Please Login\"";
			} else {
				result = "\"result\":\"failure\",\"message\":\"" + status.getMessage() + "\"";
			}
			return "{\"response\":{" + result + "}}";
		
	}*/

/*	@CrossOrigin
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
		
	}*/
	
	@CrossOrigin
	@RequestMapping(value = "/verifyEmailAddress", method = RequestMethod.GET)
	public ResponseEntity<String> verifyEmailAdress(@RequestParam String employeeid,String appUrl) {
		Status status = null;
		try{
		status = userManager.verifyEmailadress(employeeid,appUrl);
	} catch (Exception e) {
		return new ResponseEntity<String>("{\"response\":\"" + e.getMessage() + "\"}",HttpStatus.INTERNAL_SERVER_ERROR);
	} catch (Throwable e) {
		return new ResponseEntity<String>("{\"response\":\"" + e.getMessage() + "\"}",HttpStatus.INTERNAL_SERVER_ERROR);
	}
	String res ="{\"response\":\"" + status.getMessage() + "\"}";
	return new ResponseEntity<String>(res,HttpStatus.OK);
	}
	
	@CrossOrigin
	@RequestMapping(value = "/resetPwd", method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<String> resetPwd(@RequestHeader("Authorization") String Authorization, String token,String fgt) {
		Status status = null;
		try {
			final String authorization = Authorization;
			String base64Credentials = authorization.substring("Basic".length()).trim();
			status = userManager.resetPassword(base64Credentials, token,fgt);
		} catch (Exception e) {
			return new ResponseEntity<String>("{\"response\":\"" + e.getMessage() + "\"}",HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Throwable e) {
			return new ResponseEntity<String>("{\"response\":\"" + e.getMessage() + "\"}",HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<String>("{\"response\":\"" + status.getMessage() + "\"}",HttpStatus.OK);
	}
	
	/*@CrossOrigin
    @RequestMapping(value = "user/createuser", method = RequestMethod.POST, consumes = { "multipart/form-data" })
	public ResponseEntity<String> createUser(@RequestParam("user") String userjson) {
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
		if(status.isOk()){
			return new ResponseEntity<String>(status.getMessage(), HttpStatus.OK);
		}else{
			return new ResponseEntity<String>(status.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}*/
	@CrossOrigin
    @RequestMapping(value = "user/createuser", method = RequestMethod.POST, consumes = { "multipart/form-data" })
	public ResponseEntity<String> createUser(@RequestParam("user") String userjson) {
		ObjectMapper obm = new ObjectMapper();
		Status status = null;
		Employee_Selfcare_Users user;
		try {
			user = obm.readValue(userjson,Employee_Selfcare_Users.class);
			status = userManager.createUser(user);
		} catch (Exception e) {
			return new ResponseEntity<String>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Throwable e) {
			return new ResponseEntity<String>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<String>(status.getMessage(),HttpStatus.OK);
	}
	@CrossOrigin
    @RequestMapping(value = "user/updateuser", method = RequestMethod.POST, consumes = { "multipart/form-data" })
	public ResponseEntity<String> updateUser(@RequestParam("user") String userjson) {
		System.out.println("updateuser userjson : " + userjson);
		ObjectMapper obm = new ObjectMapper();
		Status status = null;
		Employee_Selfcare_Users user;
		try {
			user = obm.readValue(userjson, Employee_Selfcare_Users.class);
			boolean auth = Utils.authenticateUrl(user.getEmployee_id());
			if(!auth){
				return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
			status = userManager.updateUser(user);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Throwable e) {
			return new ResponseEntity<String>("{\"response\":\"" + e.getMessage() + "\"}",HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<String>("{\"response\":\"" + status.getMessage() + "\"}",HttpStatus.OK);
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
			//status = userManager.updateUser(user);
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		return "{\"response\":\"" + status.getMessage() + "\"}";
	}  
	/*@CrossOrigin
    @RequestMapping(value = "user/getAllUsers", method = RequestMethod.GET, produces = { "multipart/form-data" })
	public @ResponseBody String getAllUsers() {
		UserManager userManager = (UserManager) context.getBean("userManager");
		ObjectMapper obm = new ObjectMapper();
		String response  = null;
		try {
			List<Employee_Selfcare_Users> users = userManager.getAllUsers();
			response = obm.writeValueAsString(users);
		} catch (Throwable e) {
			response =e.getMessage();
		
		}
		return response;
	}*/

	@CrossOrigin
   	@RequestMapping(value = "/login", method = RequestMethod.POST)
   	public @ResponseBody String login(@RequestHeader("Authorization") String Authorization) throws Exception {
   		Logindetails logindetails = null;
   		String response = "";
		ObjectMapper obm = new ObjectMapper();
		try {
			final String authorization = Authorization;
	   		String base64Credentials = authorization.substring("Basic".length()).trim();
	   		String credentials = new String(Base64.getDecoder().decode(base64Credentials));
	   		final String[] values = credentials.split(":", 2);
	   		String	usrId = values[0];
	   		String password = values[1];
			logindetails = userManager.validateUser(usrId, password);
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logindetails.setResult("Login failure");
			logindetails.setUser(null);
		}
		response =  obm.writeValueAsString(logindetails);
		return response;
   	}
	
	@CrossOrigin
    @RequestMapping(value = "user/changepassword", method = RequestMethod.POST, consumes = { "multipart/form-data" })
	public ResponseEntity<String> changepassword(@RequestParam("user") String userjson) {
		ObjectMapper obm = new ObjectMapper();
		Status status = null;
		Employee_Selfcare_Users user;
		try {
			user = obm.readValue(userjson, Employee_Selfcare_Users.class);
			boolean result = Utils.authenticateUrl(user.getEmployee_id());
			if(!result){
				return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
			}	
			status = userManager.updateUser(user);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Throwable e) {
			return new ResponseEntity<String>("{\"response\":\"" + e.getMessage() + "\"}",HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<String>("{\"response\":\"" + status.getMessage() + "\"}",HttpStatus.OK);
	}  
	
	@CrossOrigin
	@RequestMapping(value = "getPayslipFile", method = RequestMethod.GET, produces = "multipart/form-data")
	public ResponseEntity<byte[]> getPayslipFile(@RequestParam String userId,String month) {
		boolean result = Utils.authenticateUrl(userId);
		if(!result){
			return new ResponseEntity<byte[]>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		ResponseEntity<byte[]> responseEntity = userManager.getPayslipFile(userId,month);
		return responseEntity;
	}
	
	@CrossOrigin
    @RequestMapping(value = "sendBulkNotifyForUserIdActivationLinkService", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<String> sendBulkNotifyForUserIdActivationLinkService() {
		ObjectMapper obm = new ObjectMapper();
		Status status = null;
		Employee_Selfcare_Users user;
		try {
			status = userManager.sendBulkNotifyForUserIdActivationLinkService();
		} catch (Exception e) {
			return new ResponseEntity<String>("{\"response\":\"" + e.getMessage() + "\"}",HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Throwable e) {
			return new ResponseEntity<String>("{\"response\":\"" + e.getMessage() + "\"}",HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<String>("{\"response\":\"" + status.getMessage() + "\"}",HttpStatus.OK);
	}  
	
	@CrossOrigin
    @RequestMapping(value = "createBulkUserIdService", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<String> createBulkUserIdService() {
		ObjectMapper obm = new ObjectMapper();
		Status status = null;
		Employee_Selfcare_Users user;
		try {
			status = userManager.createBulkUserIdService();
		} catch (Exception e) {
			return new ResponseEntity<String>("{\"response\":\"" + e.getMessage() + "\"}",HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Throwable e) {
			return new ResponseEntity<String>("{\"response\":\"" + e.getMessage() + "\"}",HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<String>("{\"response\":\"" + status.getMessage() + "\"}",HttpStatus.OK);
	}  
	
	@CrossOrigin
    @RequestMapping(value = "dactivateSelcareUser", method = RequestMethod.GET)
	public ResponseEntity<String> deactivateExitEmpSelfcareAccount() {
		Status status = null;
		try {
			status = userManager.deactivateExitEmpSelfcareAccount();
		} catch (Exception e) {
			return new ResponseEntity<String>("{\"response\":\"" + e.getMessage() + "\"}",HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Throwable e) {
			return new ResponseEntity<String>("{\"response\":\"" + e.getMessage() + "\"}",HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<String>("{\"response\":\"" + status.getMessage() + "\"}",HttpStatus.OK);
	}  

	@CrossOrigin
    @RequestMapping(value = "saveNavigation", method = RequestMethod.POST,consumes ={ "multipart/form-data"})
	public ResponseEntity<String> trackUserNavigation(@RequestParam("model") String userjson) {
		ObjectMapper obm = new ObjectMapper();
		Status status = null;
		UserNavigationTracker userTracker=null;
		
		try {
			userTracker = obm.readValue(userjson,UserNavigationTracker.class);
			status = userManager.trackUserNavigation(userTracker);
		} catch (Exception e) {
			return new ResponseEntity<String>("{\"response\":\"" + e.getMessage() + "\"}",HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Throwable e) {
			return new ResponseEntity<String>("{\"response\":\"" + e.getMessage() + "\"}",HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<String>("{\"response\":\"" + status.getMessage() + "\"}",HttpStatus.OK);
	}
	
	
	
	
	
	
}
