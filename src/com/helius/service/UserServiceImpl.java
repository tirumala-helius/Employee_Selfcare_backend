package com.helius.service;


import java.io.File;
import java.io.FileInputStream;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Service;

import com.helius.controllers.UsernavigationBean;
import com.helius.dao.EmployeeDAOImpl;
import com.helius.entities.Employee;
import com.helius.entities.Employee_Beeline_Details;
import com.helius.entities.Employee_Personal_Details;
import com.helius.entities.Employee_Selfcare_Users;
import com.helius.entities.User;
import com.helius.entities.UserNavigationTracker;
import com.helius.entities.User_Navigation_Details;
import com.helius.utils.Logindetails;
import com.helius.utils.Utils;

@Service
public class UserServiceImpl implements com.helius.service.UserService {
	@Autowired
	private EmailService emailService;
	@Autowired
	EmployeeDAOImpl employeeDAO;
	@Autowired
	ApplicationContext context;
	@Autowired 
	private InMemoryUserDetailsManager inMemoryUserDetailsManager;
	
	private org.hibernate.internal.SessionFactoryImpl sessionFactory;
	
	public org.hibernate.internal.SessionFactoryImpl getSessionFactory() {
		return sessionFactory;
	}

	/**
	 * @param sessionFactory
	 *            the sessionFactory to set
	 */
	public void setSessionFactory(org.hibernate.internal.SessionFactoryImpl sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
    private static final Logger logger = LogManager.getLogger(UserServiceImpl.class.getName());

	/** create userlogin id & password **/
	/*@Override
	public void save(Users user, String createLoginFlag) throws Throwable {
		Employee employee = employeemanager.getEmployee(user.getEmployee_id());
		Session session = null;
		Transaction transaction = null;
		session = sessionFactory.openSession();
		transaction = session.beginTransaction();
		try {
			String pwd = user.getEmployee_id() + "Helius@12";
			user.setPassword(pwd);
			user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
			// user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
			String getIDChck = user.getEmployee_id();
			Users empIdChck = get(getIDChck);
			if (empIdChck.getEmployee_id() == null) {
				try {
					session.save(user);
				} catch (Exception e) {
					e.printStackTrace();
					throw new Throwable("Failed to Save UserLogin" + e.getMessage());
				}
			} else {
				throw new Throwable("User already exist");
			}
			String appUrl = "http://localhost:63342/helius/changepassword.html#!/";
			String To = employee.getEmployeeOfferDetails().getPersonal_email_id();
			SimpleMailMessage passwordResetEmail = new SimpleMailMessage();
			passwordResetEmail.setFrom("vinaynaiduer@gmail.com");
			passwordResetEmail.setTo("vinaynaiduer@gmail.com");
			passwordResetEmail.setSubject("Activate your account");
			passwordResetEmail.setText("Dear " + employee.getEmployeePersonalDetails().getEmployee_id() + "," + "\n\n"
					+ "Welcome to Helius Family." + "\n\n"
					+ "To Activate verify your emailaddress, please click below and change your password " + "\n\n"
					+ appUrl + "?token=" + user.getEmployee_id() + "\n\n" + "Regards," + "\n" + "Helius Technologies.");
			if (empIdChck != null && "Y".equals(createLoginFlag)) {
				try {
					emailService.sendEmail(passwordResetEmail);
				} catch (MailException e) {
					e.printStackTrace();
					throw new Throwable("Failed to Send Email to New User");
				}
			}
			transaction.commit();
		} catch (HibernateException e) {
			transaction.rollback();
			e.printStackTrace();
			throw new Throwable("Failed to Save UserLogin");
		} catch (Exception e) {
			transaction.rollback();
			e.printStackTrace();
			throw new Throwable("Failed to Save UserLogin");
		} finally {
			session.close();
		}
	}

	@Override
	public void save(Users user, String createLoginFlag, Session session) throws Throwable {
		Employee employee = employeemanager.getEmployee(user.getEmployee_id());
		// Transaction transaction = null;
		try {
			String pwd = user.getEmployee_id() + "Helius@12";
			user.setPassword(pwd);
			user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt())); // user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
			String getIDChck = user.getEmployee_id();
			Users empIdChck = get(getIDChck);
			if (empIdChck.getEmployee_id() == null) {
				try {
					session.save(user);
				} catch (HibernateException e) {
					// transaction.rollback();
					e.printStackTrace();
					throw new Throwable("Failed to Save UserLogin");
				} catch (Exception e) {
					e.printStackTrace();
					throw new Throwable("Failed to Save UserLogin" + e.getMessage());
				}
			} else {
				throw new Throwable("User already exist");
			}
		} catch (Exception e) {
			// transaction.rollback();
			e.printStackTrace();
			throw new Throwable("Failed to Save UserLogin");
		}
	}
*/
    
    /**
     * This code is used to retrieve the particular Employee_Selfcare_Users details of an employee from database based on userid parameter.
     * */
	@Override
	public Employee_Selfcare_Users getUser(String userid) throws Throwable {
		Employee_Selfcare_Users User = null;
		String status = "";
		Session session = null;
		try {
			session = sessionFactory.openSession();
			String user_query = "select * from Employee_Selfcare_Users u where u.active='Yes' and u.employee_id='"
					+ userid + "' ";
			java.util.List userlist = session.createSQLQuery(user_query).addEntity(Employee_Selfcare_Users.class).list();
			if (userlist != null && !userlist.isEmpty()) {
				User = (Employee_Selfcare_Users) userlist.iterator().next();
			}
		} catch (Exception e) {
			logger.error("failed to get employee detail of empid - "+userid,e);
			throw new Throwable("Unable to fetch User Details !");
		}finally{
			session.close();
		}
		return User;
	}

	/**
	 * forgot password first verify the emailaddress and send email to employee
	 * to set new password
	 **/
	@Override
	public String verifyForgotEmailAddress(String employeeid,String appUrl) throws Throwable{
		String status = "";
		Session session = null;
		Transaction transaction = null;
		String To = "";
		Employee employee = null;
		Employee_Selfcare_Users user = null;
		try {
			session = sessionFactory.openSession();
			transaction = session.beginTransaction();
			employee = employeeDAO.get(employeeid);
			user = getUser(employeeid);
			if (employee != null && user !=null) {
				//String token = UUID.randomUUID().toString();
				if(user.getUser_login_attempts()>0){
			    SecureRandom random = new SecureRandom();
			    Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
				byte[] buffer = new byte[20];
		        random.nextBytes(buffer);
		        String token = encoder.encodeToString(buffer)+Instant.now().toEpochMilli();
				user.setToken(token);
				session.update(user);
				To = employee.getEmployeeOfferDetails().getPersonal_email_id();
			//	String appUrl = "http://localhost:8080/helius/changepassword.html#!/";
				String subject= "Forgot Password";
				String text = "Hello " + employee.getEmployeePersonalDetails().getEmployee_name() + ","
						+ "\n\n" + "Please click below and change your password "
						+ "\n\n" + appUrl + "?token="+employeeid+"-fgtY"+ token+"\n\n" + "With Regards," + "\n"
						+ "Helius Technologies.";
				transaction.commit();
				logger.info("new token for forgot password is updated for user -"+employeeid+"--token--"+token);
				emailService.sendEmail(To, null, null, subject, text);
				logger.info("forgot password link is sent to user successfully "+To);
				status = "Forgot Password link has been send to your registered Email Address";
				}else{
				status = "Your Account is not activated. Please activate your account using activation link shared earlier or Contact - HR ";
				}
			} else {
				throw new Throwable("Please check your User-Id or contact HR !");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("issue in sending forgot password link to user "+employeeid,e);
			throw new Throwable("Please check your User-Id or contact HR !");
		} finally {
			session.close();
		}
		return status;
	}

	/*@Override
	public void activateUserAccount(String base64Credentials,String token) throws Throwable {
		Session session = null;
		Transaction transaction = null;
		String password = null;
		String userid = null;
		try {
			session = sessionFactory.openSession();
			transaction = session.beginTransaction();
			String credentials = new String(Base64.getDecoder().decode(base64Credentials));
			final String[] values = credentials.split(":", 2);
			password = values[1];
			userid = values[0];
		//	String pwd = (BCrypt.hashpw(password, BCrypt.gensalt()));
			Employee_Selfcare_Users user = getUser(userid);
			if (user.getEmployee_id() != null) {
				int User_login_attempts = user.getUser_login_attempts();
				if (User_login_attempts == 0 && token.equals(user.getToken())) {
					user.setUser_login_attempts(User_login_attempts + 1);
					user.setPassword(password);
					session.update(user);
				} else {
					throw new Throwable("Account is already activated please Login");
				}
			} else {
				throw new Throwable("Account not found ");
			}
			transaction.commit();
		} catch (Exception e) {
			e.printStackTrace();
			throw new Throwable("Failed to activate account");
		} finally {
			session.close();
		}
	}*/

	/**
	 * Used to activate account and change password for the first time login and
	 * also to set new password incase of forgot
	 **/
	//Override
	/*public void activateUserAccount12(String base64Credentials, String forgot) throws Throwable {
		Session session = null;
		Transaction transaction = null;
		String password = null;
		String username = null;
		String status = "";
		try {
			session = sessionFactory.openSession();
			transaction = session.beginTransaction();
			String credentials = new String(Base64.getDecoder().decode(base64Credentials));
			final String[] values = credentials.split(":", 2);
			password = values[1];
			username = values[0];
			String pwd = (BCrypt.hashpw(password, BCrypt.gensalt()));
			Employee_Selfcare_Users user = getUser(username);
			if (forgot.equals("N")) {
				if (user.getEmployee_id() != null) {
					int User_login_attempts = user.getUser_login_attempts();
					if (User_login_attempts == 0) {
						user.setUser_login_attempts(User_login_attempts + 1);
						user.setPassword(password);
						session.update(user);
					} else {
						throw new Throwable("Account is already activated please Login");
					}
				} else {
					throw new Throwable("Account not found ");
				}
			} else {
				try {
					user.setPassword(pwd);
					session.update(user);
					status = "success";
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					transaction.rollback();
					throw new Throwable("Unable to change Password Please Contact HR");
				}
			}
			transaction.commit();
		} catch (HibernateException e) {
			transaction.rollback();
			e.printStackTrace();
			throw new Throwable("Failed to activate account");
		} finally {
			session.close();
		}
	}
*/
	
	/**
	 * This code is used to Handles account activation for first-time login and password reset in case of a forgotten password.  
	 * Validates the user token and updates the password if the token matches.  
	 * Ensures that an already activated account cannot reset its password multiple times using the same token.  
	 */

	@Override
	public String resetpswd(String base64Credentials,String token,String fg) throws Throwable{
		Session session = null;
		Transaction transaction = null;
		String password = null;
		String userid = null;
		String status = "";
		try {
			session = sessionFactory.openSession();
			transaction = session.beginTransaction();
			String credentials = new String(Base64.getDecoder().decode(base64Credentials));
			final String[] values = credentials.split(":", 2);
			password = values[1];
			userid = values[0];
			//String pwd = (BCrypt.hashpw(password, BCrypt.gensalt()));
			Employee_Selfcare_Users user = getUser(userid);
			if("Y".equalsIgnoreCase(fg)){
			if(token.equals(user.getToken())){
			user.setPassword(password);
			user.setLast_modified_by(user.getEmployee_id());
			session.update(user);
			updateuser_to_memory(user);
			transaction.commit();
			status = "Password saved succesfully Please Login !";
			}else{
				logger.error("failed to authenticate user email link token is not matching with db token for user -"+userid+ "given email token is "+token+" db token is "+user.getToken());
				throw new Throwable("Failed to change Password Please Contact HR !");
			}
		}
			if("N".equalsIgnoreCase(fg)){				
				int User_login_attempts = user.getUser_login_attempts();
				if(token.equals(user.getToken())){
				if (User_login_attempts == 0) {
					user.setUser_login_attempts(User_login_attempts + 1);
					user.setPassword(password);
					user.setLast_modified_by(user.getEmployee_id());
					session.update(user);
					adduser_to_memory(user);
					transaction.commit();
					status = "Password saved succesfully Please Login !";
				} else {
					status = "Account is already activated please Login";
				}
			}else{
				logger.error("failed to authenticate user email link token is not matching with db token given email token is "+token+"db token is "+user.getToken());
				throw new Throwable("Failed to change Password Please Contact HR !");
			}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("Failed to update password for user - "+userid+" internal error check stacktrace",e);
			throw new Throwable("Failed to change Password Please Contact HR !");
		} finally {
			session.close();
		}
		return status;
	}

	/** employee login **//*
	@Override
	public String verifyUserLogin(String username, String password) {
		String status = "";
		if (username != null && !username.isEmpty()) {
			UserManager userManager = (UserManager) context.getBean("userManager");
			Boolean verifyPwd = true;
			Users user = userManager.getUsers(username);
			if (user != null && user.getUser_login_attempts() > 0) {
				verifyPwd = checkPass(password, user.getPassword());
				if (verifyPwd) {
					status = "success";

				} else {
					status = "Enter valid  Password.!";
				}
			} else {
				status = "Username doesn't Exist or Account is not Activated ";
			}
		}
		return status;
	}

	
	// to compare password with encrypt pswd
	private Boolean checkPass(String plainPassword, String hashedPassword) {
		Boolean status = null;
		if (BCrypt.checkpw(plainPassword, hashedPassword))
			status = true;
		else
			status = false;
		return status;
	}*/

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.helius.service.UserService#createUser(com.helius.entities.User)
	 */
	
	
	/**
	 * This code is used to creates a new user entry in the Employee_Selfcare_Users table.
	 * Generates a unique token for account activation and saves the user in the database.
	 * Sends an account activation email to the user's personal email ID.
	 * If an error occurs, logs the issue and throws an exception.
	 */

	@Override
	public void createUser(Employee_Selfcare_Users user) throws Throwable {
		Session session  = null;
		Employee employee = null;
		try {
			session = sessionFactory.openSession();
			Transaction transaction = session.beginTransaction();
			// user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
			String token = UUID.randomUUID().toString();
			user.setToken(token);
			session.save(user);
			transaction.commit();
			logger.info("-----user loginId created in db successfully");
			adduser_to_memory(user);
			employee = employeeDAO.get(user.getEmployee_id());
			String To = employee.getEmployeeOfferDetails().getPersonal_email_id();
			String appUrl = "http://localhost:8080/helius/changepassword.html#!/";
			String subject= "Account Activation";
			String text = "Hello " + employee.getEmployeePersonalDetails().getEmployee_name() + ","
					+ "\n\n" + "Please click below to activate and create your password "
					+ "\n\n" + appUrl + "?token=" +user.getEmployee_id()+"-fgtN"+ token + "\n\n" + "With Regards," + "\n"
					+ "Helius Technologies.";
			emailService.sendBulkEmail(To, null, null, subject, text);
		} catch (Exception e) {
			// transaction.rollback();
			logger.error("internal error failed to create loginid for user - "+user.getEmployee_id(),e);
			throw new Throwable("Failed to Save User");
		} catch (Throwable e) {
			logger.error("internal error failed to create loginid for user - "+user.getEmployee_id(),e);
			throw new Throwable("Failed to Save User");
		}
		finally{
			session.close();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.helius.service.UserService#updateUser(com.helius.entities.User)
	 */
	
	
	/**
	 * This code is used to update particular user details in Employee_Selfcare_Users entity in database by using user as parameter.
	 * */
	@Override
	public void updateUser(Employee_Selfcare_Users user) throws Throwable {
		Session session = null;
		try {
			session = sessionFactory.openSession();
			Transaction transaction = session.beginTransaction();
			// user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
			session.evict(user);
			session.merge(user);
			transaction.commit();
			//sc.updateuser(user);
			updateuser_to_memory(user);
		} catch (HibernateException e) {
			// transaction.rollback();
			logger.error("internal error failed to update login details in db or else in memory for user - "+user.getEmployee_id(),e);
			throw new Throwable("Failed to update details");
		} catch (Exception e) {
			logger.error("internal error failed to update login details in db or else in memory for user - "+user.getEmployee_id(),e);
			throw new Throwable("Failed to update details");
		}
		finally{
			session.close();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.helius.service.UserService#deleteUser(com.helius.entities.User)
	 */
	/*@Override
	public String deleteUser(User user) throws Throwable {
		// TODO Auto-generated method stub
		return null;
	}*/

	/*public List<com.helius.utils.User> getAllUsers() throws Throwable {
		List<com.helius.utils.User> allusers = new ArrayList<com.helius.utils.User>();
		
		Session session = null;
		try {

			session = sessionFactory.openSession();

			Query query = session.createSQLQuery("select * from user u where u.active='Yes'")
					.addEntity(com.helius.entities.User.class);
			List users = query.list();
			if (!users.isEmpty()) {
				Iterator iter = users.iterator();
				while (iter.hasNext()) {
					com.helius.entities.User user_entity = (com.helius.entities.User) iter.next();
					com.helius.utils.User user_util = new com.helius.utils.User();
					user_util.setActive(user_entity.getActive());
					user_util.setId(user_entity.getId());
					user_util.setUserid(user_entity.getUserid());
					user_util.setPassword(user_entity.getPassword());
					user_util.setUsername(user_entity.getUsername());
					user_util.setEdit(user_entity.getEdit());
					user_util.setView(user_entity.getView());
					String[] rr = user_entity.getRole().split(",");
					List<String> role = new ArrayList<String>();
					for (int i = 0; i < rr.length; i++) {
						role.add(rr[i]);
					}
					user_util.setRole(role);
					List<String> country = new ArrayList<String>();
					String[] countries = user_entity.getCountry().split(",");
					for (int i = 0; i < countries.length; i++) {
						country.add(countries[i]);
					}
					user_util.setCountry(country);

					allusers.add(user_util);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Could not retrive users");
		}
		finally{
			session.close();
		}
		return allusers;
	}*/

	/* (non-Javadoc)
	 * @see com.helius.service.UserService#validateUser()
	 */
	
	/**
	 * This code retrieves employee data from the database based on the provided user ID and password  
	 * from the Employee_Selfcare_Users entity.  
	 * It checks whether the user ID exists in the database.  
	 * If the user ID is found, it validates the credentials; otherwise, it prevents login  
	 * and returns a relevant exception message.  
	 */

	@Override
	public Logindetails validateUser(String userid, String password) throws Throwable {
		Logindetails validauser = new Logindetails();
		if (userid != null && !userid.isEmpty()) {
			Session session = null;
			Transaction transaction = null;
			try {
				String encodedpassword = Base64.getEncoder().encodeToString(password.getBytes());
				session = sessionFactory.openSession();
				transaction = session.beginTransaction();
				String querystr = "select * from Employee_Selfcare_Users u where u.active='Yes' and u.employee_id='"
						+ userid + "'";
				Query query = session.createSQLQuery(querystr).addEntity(com.helius.entities.Employee_Selfcare_Users.class);
				Object userobj = query.uniqueResult();
				if (userobj != null) {
					com.helius.entities.Employee_Selfcare_Users user_entity = (com.helius.entities.Employee_Selfcare_Users) userobj;
					if (user_entity != null && encodedpassword.equals(user_entity.getPassword())) {
					if (user_entity.getUser_login_attempts() > 0) {
					Timestamp lastlogin = user_entity.getUser_last_login();
					Timestamp currentTimestamp = Timestamp.from(Instant.now());
					user_entity.setUser_last_login(currentTimestamp);
					user_entity.setUser_login_attempts(user_entity.getUser_login_attempts() + 1);
					
					//change
					if(user_entity.getVersion_Number()==null){
						String VersionNumber=Version();
						user_entity.setVersion_Number(VersionNumber);
					}
					
					session.update(user_entity);

					transaction.commit();
					validauser.setResult("Login success");
					user_entity.setUser_last_login(lastlogin);
					validauser.setUser(user_entity);
					}else{
						validauser.setResult("Account is not Activated. Please check your emails for activation link or contact HR");
						validauser.setUser(null);
						logger.info(" - Account is not Activated. Please check your emails for activation link or contact HR "+userid);
					}
					}else{
						validauser.setResult("Invalid LoginId & Password");
						validauser.setUser(null);
						logger.info(" passsword does not match failed to login "+userid);
					}
				} else {
					validauser.setResult("User not found in the system");
					validauser.setUser(null);
				}

			} catch (Exception e) {
				logger.error("unable to fetch "+userid+" details looks like internal error or unable to update lastlogin in db",e);
				validauser.setResult("Login failure");
				validauser.setUser(null);
			} finally {
				session.close();
			}
		}
		return validauser;
	}

	//change
	public String Version() {
	    String versionQuery = "SELECT value FROM Dropdown_Picklist_Items WHERE drop_down_type = :dropDownType";
	    String version = null; // Initialize version as null
	    Session session = null;  // Declare session object
	    
	    // Declare session and transaction objects
	    try {
	        session = sessionFactory.openSession();  // Open a new session
	        // Execute the query to fetch version
	        version = (String) session.createSQLQuery(versionQuery)
	                                  .setParameter("dropDownType", "version")
	                                  .uniqueResult(); // Fetching a single value directly
	        
	        // Log the version fetched or a message if not found
	        if (version == null) {
	            logger.warn("No version found for dropDownType 'version'.");
	        } else {
	            logger.info("Fetched Version: " + version);
	        }
	    } catch (Exception e) {
	        // Log the exception in case of any errors
	        logger.error("Error fetching version", e);
	    } finally {
	        if (session != null) {
	            session.close();  // Ensure the session is closed to release resources
	        }
	    }
	    
	    return version; // Return the fetched version value (or null if not found)
	}


	private void adduser_to_memory(Employee_Selfcare_Users user) {
		String decodedpassword = new String(Base64.getDecoder().decode(user.getPassword()));
		String userid = user.getEmployee_id();
		List<GrantedAuthority> role = new ArrayList<GrantedAuthority>();
		/*String[] rr = user.getRole().split(",");
		for (int i = 0; i < rr.length; i++) {
			GrantedAuthority ga = new SimpleGrantedAuthority("ROLE_"+rr[i]);
			role.add(ga);
		}	*/		
		org.springframework.security.core.userdetails.User user_sec =
				new org.springframework.security.core.userdetails.User(user.getEmployee_id(),decodedpassword,true,true,true,true,role);
		inMemoryUserDetailsManager.createUser(user_sec);
		logger.info(" - user created in memory "+user.getEmployee_id());
	}
	private void updateuser_to_memory(Employee_Selfcare_Users user) {
		String decodedpassword = new String(Base64.getDecoder().decode(user.getPassword()));
		String userid = user.getEmployee_id();
		List<GrantedAuthority> role = new ArrayList<GrantedAuthority>();
		/*String[] rr = user.getRole().split(",");
		for (int i = 0; i < rr.length; i++) {
			GrantedAuthority ga = new SimpleGrantedAuthority("ROLE_"+rr[i]);
			role.add(ga);
		}*/			
		org.springframework.security.core.userdetails.User user_sec =
				new org.springframework.security.core.userdetails.User(user.getEmployee_id(),decodedpassword,true,true,true,true,role);
		inMemoryUserDetailsManager.updateUser(user_sec);
		logger.info(" - user updated in memory "+user.getEmployee_id());
	}

	
	/* (non-Javadoc)
	 * @see com.helius.service.UserService#getUser(java.lang.String)
	 */
	/*public com.helius.utils.User getUserold(String userid) throws Exception{
		com.helius.utils.User user = null;
		
		Session session = null;
		try {

			session = sessionFactory.openSession();

			Query query = session.createSQLQuery("select * from user u where u.userid=" + userid + "")
					.addEntity(com.helius.entities.User.class);
			List users = query.list();
			if (!users.isEmpty()) {
				Iterator iter = users.iterator();
				while (iter.hasNext()) {
					com.helius.entities.User user_entity = (com.helius.entities.User) iter.next();
					com.helius.utils.User user_util = new com.helius.utils.User();
					user_util.setActive(user_entity.getActive());
					user_util.setId(user_entity.getId());
					user_util.setUserid(user_entity.getUserid());
					user_util.setPassword(user_entity.getPassword());
					user_util.setUsername(user_entity.getUsername());
					user_util.setEdit(user_entity.getEdit());
					user_util.setView(user_entity.getView());
					String[] rr = user_entity.getRole().split(",");
					List<String> role = new ArrayList<String>();
					for (int i = 0; i < rr.length; i++) {
						role.add(rr[i]);
					}
					user_util.setRole(role);
					List<String> country = new ArrayList<String>();
					String[] countries = user_entity.getCountry().split(",");
					for (int i = 0; i < countries.length; i++) {
						country.add(countries[i]);
					}
					user_util.setCountry(country);

					user =user_util;
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Could not retrive users");
		}
		finally{
			session.close();
		}
		return user;
	}*/
	
	public HashMap<String,String> getOldANDNewEmpIdAssosc() throws Throwable {
		Session session = null;
		HashMap<String,String> map = new HashMap<String,String>();
		try {
			session = sessionFactory.openSession();
			String query = "SELECT employee_id,old_employee_id from Employee_Old_Id_VS_New_Id";
			List<Object[]> EmpAssoc = session.createSQLQuery(query).list();
			for(Object[] obj : EmpAssoc){
					map.put(obj[0].toString(), obj[1].toString());
				}
		} catch (Exception e) {
			e.printStackTrace(); 
			throw new Throwable("Failed to fetch beeline employee Association details " + e.getMessage());
		}finally{
			session.close();
		}
		return map;
	}
	
	
	/**
	 * This method retrieves and returns a payslip file for a given userId and month.
	 * It first checks if the user has an associated employee ID, then queries the database 
	 * to get the path of the payslip file for the specified month.
	 * If the file is found, it is returned as a byte array. 
	 * 
	 * 
	 * If the payslip file is not found or an error occurs, appropriate HTTP status codes are returned.
	 */
	
	@Override
	public ResponseEntity<byte[]> getPayslipFIle(String userId,String date) throws Throwable {
		Session session = null;
		byte[] files = null;
		String check = Utils.awsCheckFlag();
		try{
		session = sessionFactory.openSession();
		HashMap<String,String> assosc = getOldANDNewEmpIdAssosc();
		String employeeId = null;
		if(assosc.containsKey(userId)){
			if(assosc.get(userId) != null && !assosc.get(userId).isEmpty()){
				employeeId = assosc.get(userId);
			}
		}else{
			employeeId = userId;
		}
		SimpleDateFormat sdfMonth = new SimpleDateFormat("yyyy-MM");
		java.util.Date selectedMonth = sdfMonth.parse(date);
		Timestamp playslipMonth = new Timestamp(selectedMonth.getTime());
		String query = "SELECT payslip_path from Employee_Pay_Slips where month = :month AND employee_id = :employee_id";
		List<String> payslipUrl = session.createSQLQuery(query).setParameter("month", playslipMonth).setParameter("employee_id", userId).list();
		if(payslipUrl.size() > 1){
			return new ResponseEntity<byte[]>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		if(payslipUrl.size() == 0){
		return new ResponseEntity<byte[]>(HttpStatus.NO_CONTENT);
		}
		String url = null;
		for(String urls : payslipUrl){
			url = urls;
		}
		if(url == null || url.isEmpty()){
			return new ResponseEntity<byte[]>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		//if(url.contains("/"+employeeId+".")){
		if(url.contains(File.separator+employeeId+".")){
		FileInputStream fi = null;
			File file = new File(url);
			if ("no".equalsIgnoreCase(check)) {
				if (file.exists()) {
					fi = new FileInputStream(url);
					files = IOUtils.toByteArray(fi);
					fi.close();
				} else {
					logger.error("payslip file not found for user - "+userId);
					return new ResponseEntity<byte[]>(HttpStatus.NO_CONTENT);
				}
			}
			if ("yes".equalsIgnoreCase(check)) {
				try {
					files = Utils.downloadFileByAWSS3Bucket(url);
				} catch (Exception e) {
					return new ResponseEntity<byte[]>(HttpStatus.NOT_FOUND);
				}
			}
			
		}else{
			logger.error("unable to download payslip as the filename is not matching with the userId "+userId+" filename is "+url);
			return new ResponseEntity<byte[]>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		} catch (Throwable e) {
			logger.error("internal error failed to dowwnload payslip for user - "+userId,e);
			return new ResponseEntity<byte[]>(HttpStatus.INTERNAL_SERVER_ERROR);
		}finally{
			session.close();
		}
		return 	new ResponseEntity<byte[]>(files, HttpStatus.OK);		
	}
	
	
	/**
	 * This method creates bulk user accounts in the Employee_Selfcare_Users table.
	 * It retrieves a list of active employees working in Singapore from the database.
	 * For each employee, a default password is generated, encoded, and a unique token is assigned.
	 * The new user records are then saved in the database.
	 */

	public void createBulkUserIdService() throws Throwable {
		Session session = null;
		Transaction transaction = null;
		try {
			session = sessionFactory.openSession();
			transaction = session.beginTransaction();
			String query = "SELECT a.employee_id,a.employee_name from Employee_Personal_Details a LEFT JOIN Employee_Work_Permit_Details b ON a.employee_id=b.employee_id where a.employee_status='Active' AND b.work_country = 'Singapore'";
			List<Object[]> EmpQuery = session.createSQLQuery(query).list();
			System.out.println("total emp ==="+EmpQuery.size());
			HashMap<String,String> succesMap = new HashMap<String,String>();
			HashMap<String,String> failMap = new HashMap<String,String>();
			/*Object[] obj5 = new Object[]{"011","Stanley Li Chen"};
			Object[] obj1 = new Object[]{"067","SREENIVASULU  NARRAVULA"};
			Object[] obj2 = new Object[]{"1374","Praveen Kumar Jana"};
			Object[] obj3 = new Object[]{"032","George Pancho"};
			Object[] obj4 = new Object[]{"1181","Cindy Tech"};
			Object[] obj6 = new Object[]{"1531","Zhuo xinni"};
			Object[] obj7 = new Object[]{"10097","Emmanuel Elijacob Kuizon Delfino"};

			ArrayList<Object[]> EmpQuery = new ArrayList<Object[]>();
				EmpQuery.add(obj1);
				EmpQuery.add(obj2);
				EmpQuery.add(obj3);
				EmpQuery.add(obj4);
				EmpQuery.add(obj5);
				EmpQuery.add(obj6);
				EmpQuery.add(obj7);*/
			for(Object[] obj : EmpQuery){
				try{
				String employee_id = obj[0].toString();
				String employee_name = obj[1].toString();
				String password = employee_id+"hap@heliustech";
				String encodedpassword = Base64.getEncoder().encodeToString(password.getBytes());
				SecureRandom random = new SecureRandom();
			    Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
				byte[] buffer = new byte[20];
		        random.nextBytes(buffer);
		        String token = encoder.encodeToString(buffer)+Instant.now().toEpochMilli();
			    Employee_Selfcare_Users user = new Employee_Selfcare_Users();
			    user.setEmployee_id(employee_id);
			    user.setEmployee_name(employee_name);
			    user.setPassword(encodedpassword);
			    user.setToken(token);
			    user.setActive("Yes");
			    user.setUser_login_attempts(0);
			    user.setCreated_by("HAP");
			    session.save(user);			
			    System.out.println("--user created---"+employee_id);
				}catch(Exception e){
					e.printStackTrace();
				    System.out.println("--failed to create user---"+obj[0].toString());
				}
			}
			transaction.commit();			
		} catch (Exception e) {
			e.printStackTrace(); 
			throw new Throwable("Failed to run bulk user creation service " + e.getMessage());
		}finally{
			session.close();
		}
	}
	
	
	/**
	 * This method sends bulk email notifications to active employees in Singapore 
	 * with an activation link to change their password for the Self-Service Portal. 
	 * It queries the database for employee details and sends an email to each employee 
	 * with their unique token to activate their account. 
	 * Successful and failed email notifications are tracked and printed in the logs.
	 */

	public void sendBulkNotifyForUserIdActivationLinkService() throws Throwable {
		Session session = null;
		try {
			session = sessionFactory.openSession();
			String query = "SELECT a.employee_id,a.employee_name,b.personal_email_id,d.token from Employee_Personal_Details a left join Employee_Offer_Details b ON a.employee_id = b.employee_id LEFT JOIN Employee_Work_Permit_Details c ON a.employee_id = c.employee_id LEFT JOIN Employee_Selfcare_Users d ON a.employee_id=d.employee_id where a.employee_status='Active' AND b.work_country = 'Singapore'";
			List<Object[]> EmpQuery = session.createSQLQuery(query).list();
			System.out.println("total list ==="+EmpQuery.size());
			//String appUrl = "https://hap-testing.heliusapp.com/Employee_selfcare/changepwd.html";
			String appUrl = "https://hap.heliusapp.com/Employee_selfcare/changepwd.html";
			int index  = appUrl.lastIndexOf("/");
			String urlindex = appUrl.substring(0, index);
			String loginLink = urlindex+"/login.html";
			HashMap<String,String> successMap = new HashMap<String,String>();
			HashMap<String,String> failMap = new HashMap<String,String>();
			for(Object[] obj : EmpQuery){
				try{
				String employee_id = obj[0].toString();
				String employee_name = obj[1].toString();
				String To = obj[2].toString();
				String token = obj[3].toString();
				String subject= "Account Activation";
				String text = "Dear " + employee_name + ","
						+ "\n\n"+ "Welcome to Helius Self Service Portal!"
						+ "\n\n"+ "We have developed this employee self-service portal to support remote HR needs of our employees who are working from customer locations for extended periods."
						+ "\n\n"+ "As a first step, we are giving your personal details, assignment details, leave balance. Please go through the details and in case you find any information that is not correct,"
						+ "\n"+ "please send us the correction request by clicking the link for sending a request to HR."
						+ "\n\n"+ "The upcoming features are also mentioned in the portal and will be available in future."
						+ "\n\n"+ "Please reach us at hap@helius-tech.com,   for suggestions, comments, improvement, new features."
						+ "\n\n" + "Please click below and change your password "
						+ "\n\n" + appUrl + "?token="+employee_id+"-fgtN"+ token+"\n\n"
						+ "Please click below to login "
						+"\n\n" + loginLink +"\n\n"
						+"With Regards," + "\n" + "HR Team," + "\n" + "Helius Technologies Pte.Ltd";
				if (token != null && !token.isEmpty()) {
				emailService.sendBulkEmail(To, null, null, subject, text);
				successMap.put(employee_id, To);
				}else{
				failMap.put(employee_id, To);
				}
				}catch(Exception e){
					e.printStackTrace();
					System.out.println("======check failednonce====="+obj[0].toString()+"====ashhsha==="+obj[1].toString());
					emailService.sendBulkEmail("hap@helius-tech.com", null, null, "unable to email activation link ", "unable to send activation link to : "+obj[0].toString()+" - "+obj[1].toString());
					failMap.put(obj[0].toString(), obj[1].toString());
				}
			}
			System.out.println("total success is : "+successMap.size());
			ArrayList<String> al1 = new ArrayList<String>();
			for(String key : successMap.keySet()){
				System.out.println("success mails-----"+key+"-----"+successMap.get(key));
			}
			System.out.println("total failure is : "+failMap.size());
			for(String key2 : failMap.keySet()){
				System.out.println("failed mails-----"+key2+"-----"+failMap.get(key2));
			}
		} catch (Exception e) {
			e.printStackTrace(); 
			throw new Throwable("Failed to run bulk userid notification service--" + e.getMessage());
		}finally{
			session.close();
		}
	}

	
	/**
	 * This method deactivates the selfcare accounts of employees who have exited the company the previous day. 
	 * It sends email notifications with the list of successfully deactivated accounts and any failures.
	 * The method is scheduled to run daily at 1:01 AM.
	 */

	@Override
   @Scheduled(cron = "0 1 1 ? * *")
	public void deactivateExitEmpSelfcareAccount() throws Throwable {
		Session session =null;
		Transaction transaction = null;
		String exitEmpQuery="SELECT * FROM Employee_Personal_Details WHERE employee_status ='Exited' AND DATE(relieving_date)=DATE_SUB(CURDATE(), INTERVAL 1 DAY)";
		List<String> processEmpSelfcareUser =null;
		List<String> UnprocessEmpSelfcareUser =null;
		String emailsubject = "Deactivate Employee selfcare User ";
		String emailto = Utils.getProperty("emailUserName");
		try {
			session = sessionFactory.openSession();
			transaction= session.beginTransaction();
			processEmpSelfcareUser =new ArrayList<>();
			UnprocessEmpSelfcareUser =new ArrayList<>();
			List<Employee_Personal_Details> exitDetails = session.createSQLQuery(exitEmpQuery).addEntity(Employee_Personal_Details.class).list();
			 
			for (Employee_Personal_Details employee_Personal_Details : exitDetails) {
				Employee_Selfcare_Users users = getUser(employee_Personal_Details.getEmployee_id());
				try {
					if (users !=null) {
					    users.setActive("No");
						session.update(users);
						updateuser_to_memory(users);
						processEmpSelfcareUser.add(users.getEmployee_id());
					}
					
				} catch (Exception e) {
					e.printStackTrace();
					UnprocessEmpSelfcareUser.add(employee_Personal_Details.getEmployee_id());
				}
				}
			transaction.commit();
			if (processEmpSelfcareUser != null && !processEmpSelfcareUser.isEmpty()) {
				StringBuffer message = new StringBuffer();
				message.append("Hi," + "\n\n" + "Running daily service deactivate exit Employee Selfcare Account"
						+ "\n\n" + "Total number of deactivate selfcare Account : " + processEmpSelfcareUser.size()
						+ "\n\n");
				for (String empidd : processEmpSelfcareUser) {
					message.append(empidd + " ," + "\n");
				}
				message.append("\n\n" + "Thanks," + "\n\n" + "HR Team," + "\n" + "Helius Technologies Pte.Ltd");
				emailService.sendEmail(emailto, null, null, emailsubject, message.toString());
				System.out.println("processExitEmployeeList::" + processEmpSelfcareUser);
			}
			if (UnprocessEmpSelfcareUser != null && !UnprocessEmpSelfcareUser.isEmpty()) {
				StringBuffer message = new StringBuffer();
				message.append("Hi," + "\n\n" + "issue in deactivate exit Employee Selfcare Account"
						+ "\n\n");
				for (String empidd : UnprocessEmpSelfcareUser) {
					message.append(empidd + " ," + "\n");
				}
				message.append("\n\n" + "Thanks," + "\n\n" + "HR Team," + "\n" + "Helius Technologies Pte.Ltd");
				emailService.sendEmail(emailto, null, null, emailsubject, message.toString());
				System.out.println("processExitEmployeeList::" + processEmpSelfcareUser);
			}
			
		} catch (Exception e) {
			StringBuffer message = new StringBuffer();
			message.append("Hi," + "\n\n" + "Issue in deactivate exit employee selfcare Account " + "\n\n");
			message.append("\n\n" + "Thanks," + "\n\n" + "HR Team," + "\n" + "Helius Technologies Pte.Ltd");
			emailService.sendEmail(emailto, null, null, emailsubject, message.toString());
			e.printStackTrace();
			throw new Throwable("Failed to Deactivate Exit Employee Login Account");
		}finally {
	        if (session != null) {
	            session.close();
	        }
	    }
		
	}

	
	/**
	 * This method tracks and saves the navigation details of a user. 
	 * It iterates through the user's tracing details and stores each one in the database.
	 * If an error occurs, an exception is thrown with a relevant message.
	 */

	@Override
	public void trackUserNavigation(UserNavigationTracker user) throws Throwable {
		Session session  = null;
     //UserNavigationTracker navigationTracker=null;
		try {
			session = sessionFactory.openSession();
			Transaction transaction = session.beginTransaction();
		for (User_Navigation_Details userdetails : user.getUserTracingDetails()) {
			session.save(userdetails);
		}
			transaction.commit();
			
		} catch (Exception e) {
			throw new Throwable("Failed to trace user details");
			
		}
		finally {
			if (session != null) {
				session.close();
			}
		}
	}


}
