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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Service;

import com.helius.dao.EmployeeDAOImpl;
import com.helius.entities.Employee;
import com.helius.entities.Employee_Beeline_Details;
import com.helius.entities.Employee_Selfcare_Users;
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
	@Override
	public Employee_Selfcare_Users getUser(String userid) throws Throwable {
		System.out.println("\n===emp id" + userid);
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
			e.printStackTrace();
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
			    SecureRandom random = new SecureRandom();
			    Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
				byte[] buffer = new byte[20];
		        random.nextBytes(buffer);
		        String token = encoder.encodeToString(buffer)+Instant.now().toEpochMilli();
				user.setToken(token);
				session.update(user);
				To = employee.getEmployeeOfferDetails().getPersonal_email_id();
				String[] cc = null;
				String getCC = Utils.getHapProperty("heliusHR");
				if (getCC != null && !getCC.isEmpty()) {
					cc = getCC.split(",");
				}
			//	String appUrl = "http://localhost:8080/helius/changepassword.html#!/";
				String subject= "Forgot Password";
				String text = "Hello " + employee.getEmployeePersonalDetails().getEmployee_name() + ","
						+ "\n\n" + "Please click below and change your password "
						+ "\n\n" + appUrl + "?token="+employeeid+"-fgtY"+ token+"\n\n" + "With Regards," + "\n"
						+ "Helius Technologies.";
				transaction.commit();
				emailService.sendEmail(To, cc, null, subject, text);
				status = "Forgot Password link has been send to your registered Email Address";
			} else {
				throw new Throwable("Please check your User-Id or contact HR !");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new Throwable("Please check your User-Id or contact HR !");
		} finally {
			session.close();
		}
		return status;
	}

	@Override
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
	}

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
	@Override
	public String resetpswd(String base64Credentials,String token,String fg) throws Throwable{
		System.out.println("---fg---"+fg+"----token-----"+token);
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
			session.update(user);
			transaction.commit();
			status = "Password saved succesfully Please Login !";
			updateuser_to_memory(user);
			}else{
				throw new Throwable("Failed to change Password Please Contact HR !");
			}
		}
			if("N".equalsIgnoreCase(fg)){				
				int User_login_attempts = user.getUser_login_attempts();
				if(token.equals(user.getToken())){
				if (User_login_attempts == 0) {
					user.setUser_login_attempts(User_login_attempts + 1);
					user.setPassword(password);
					session.update(user);
					transaction.commit();
					status = "Password saved succesfully Please Login !";
					adduser_to_memory(user);
				} else {
					status = "Account is already activated please Login";
				}
			}else{
				throw new Throwable("Failed to change Password Please Contact HR !");
			}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			adduser_to_memory(user);
			employee = employeeDAO.get(user.getEmployee_id());
			String To = employee.getEmployeeOfferDetails().getPersonal_email_id();
			String[] cc = null;
			String getCC = Utils.getHapProperty("heliusHR");
			if (getCC != null && !getCC.isEmpty()) {
				cc = getCC.split(",");
			}
			String appUrl = "http://localhost:8080/helius/changepassword.html#!/";
			String subject= "Account Activation";
			String text = "Hello " + employee.getEmployeePersonalDetails().getEmployee_name() + ","
					+ "\n\n" + "Please click below to activate and create your password "
					+ "\n\n" + appUrl + "?token=" +user.getEmployee_id()+"-fgtN"+ token + "\n\n" + "With Regards," + "\n"
					+ "Helius Technologies.";
			emailService.sendBulkEmail(To, cc, null, subject, text);
		} catch (HibernateException e) {
			// transaction.rollback();
			e.printStackTrace();
			throw new Throwable("Failed to Save User");
		} catch (Exception e) {
			e.printStackTrace();
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
			e.printStackTrace();
			throw new Throwable("Failed to update details");
		} catch (Exception e) {
			e.printStackTrace();
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
	@Override
	public Logindetails validateUser(String userid, String password) throws Throwable {
		Logindetails validauser = new Logindetails();
		String error = "";
		if (userid != null && !userid.isEmpty()) {
			Session session = null;
			Transaction transaction = null;
			try {
				String encodedpassword = Base64.getEncoder().encodeToString(password.getBytes());
				session = sessionFactory.openSession();
				transaction = session.beginTransaction();
				String querystr = "select * from Employee_Selfcare_Users u where u.active='Yes' and u.employee_id='"
						+ userid + "' and" + " u.password='" + encodedpassword + "'";
				Query query = session.createSQLQuery(querystr).addEntity(com.helius.entities.Employee_Selfcare_Users.class);
				Object userobj = query.uniqueResult();
				if (userobj != null) {
					com.helius.entities.Employee_Selfcare_Users user_entity = (com.helius.entities.Employee_Selfcare_Users) userobj;
					if (user_entity != null && user_entity.getUser_login_attempts() > 0) {
				/*	Employee_Selfcare_Users user_util = new Employee_Selfcare_Users();
					user_util.setEmployee_Selfcare_Users_Id(user_entity.getEmployee_Selfcare_Users_Id());
					user_util.setEmployee_id(user_entity.getEmployee_id());
					user_util.setPassword(user_entity.getPassword());
					user_util.setActive(user_entity.getActive());
					user_util.setUser_last_login(user_entity.getUser_last_login());
					user_util.setUser_login_attempts(user_entity.getUser_login_attempts());
					user_util.setEmployee_name(user_entity.getEmployee_name());*/
					validauser.setResult("Login success");
					validauser.setUser(user_entity);
					logger.info("-------info---");
					logger.debug("----debug-----");
					System.out.println("--------sysss");
					Timestamp currentTimestamp = Timestamp.from(Instant.now());
					user_entity.setUser_last_login(currentTimestamp);
					session.update(user_entity);
					transaction.commit();
					}else{
						validauser.setResult("Account is not Activated. Please check your emails for activation link or contact HR");
						validauser.setUser(null);
						logger.error("----error----");
					}
				} else {
					validauser.setResult("User not found in the system");
					validauser.setUser(null);
				}

			} catch (Exception e) {
				e.printStackTrace();
				validauser.setResult("Login failure");
				validauser.setUser(null);

			} finally {
				session.close();
			}
		}
		return validauser;
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
	}
	private void updateuser_to_memory(Employee_Selfcare_Users user) {
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
		inMemoryUserDetailsManager.updateUser(user_sec);
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
	
	@Override
	public ResponseEntity<byte[]> getPayslipFIle(String userId,String date) throws Throwable {
		Session session = null;
		byte[] files = null;
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
		String url = null;
		for(String urls : payslipUrl){
			url = urls;
		}
		if(url.contains(File.separator+employeeId+".")){
		FileInputStream fi = null;
			File file = new File(url);
			if (file.exists()) {
				fi = new FileInputStream(url);
				files = IOUtils.toByteArray(fi);
				fi.close();
			} else {
				return new ResponseEntity<byte[]>(HttpStatus.NOT_FOUND);
			}
		}else{
			return new ResponseEntity<byte[]>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		} catch (Throwable e) {
			e.printStackTrace();
			return new ResponseEntity<byte[]>(HttpStatus.INTERNAL_SERVER_ERROR);
		}finally{
			session.close();
		}
		return 	new ResponseEntity<byte[]>(files, HttpStatus.OK);		
	}
	
	public void createBulkUserIdService() throws Throwable {
		Session session = null;
		Transaction transaction = null;
		try {
			session = sessionFactory.openSession();
			transaction = session.beginTransaction();
			String query = "SELECT a.employee_id,a.employee_name from Employee_Personal_Details a LEFT JOIN Employee_Work_Permit_Details b ON a.employee_id=b.employee_id where a.employee_status='Active' AND b.work_country = 'India'";
			List<Object[]> EmpQuery = session.createSQLQuery(query).list();
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
	
	public void sendBulkNotifyForUserIdActivationLinkService() throws Throwable {
		Session session = null;
		try {
			session = sessionFactory.openSession();
			String query = "SELECT a.employee_id,a.employee_name,b.personal_email_id,d.token from Employee_Personal_Details a left join Employee_Offer_Details b ON a.employee_id = b.employee_id LEFT JOIN Employee_Work_Permit_Details c ON a.employee_id = c.employee_id LEFT JOIN Employee_Selfcare_Users d ON a.employee_id=d.employee_id where a.employee_status='Active' AND b.work_country = 'India'";
			List<Object[]> EmpQuery = session.createSQLQuery(query).list();
			String appUrl = "http://localhost:63342/helius/changepassword.html#!/";
			for(Object[] obj : EmpQuery){
				try{
				String employee_id = obj[0].toString();
				String employee_name = obj[1].toString();
				String To = obj[2].toString();
				String token = obj[3].toString();
				String[] userCC = null;
				String getCC = Utils.getHapProperty("heliusHR");
				if (getCC != null && !getCC.isEmpty()) {
					userCC = getCC.split(",");
				}
				String subject= "Account Activation";
				String text = "Hello " + employee_name + ","
						+ "\n\n" + "Please click below and change your password "
						+ "\n\n" + appUrl + "?token="+employee_id+"-fgtN"+ token+"\n\n" + "With Regards," + "\n"
						+ "Helius Technologies.";
				if (token != null && !token.isEmpty()) {
				emailService.sendBulkEmail(To, userCC, null, subject, text);
				}
			    System.out.println("--email sent to ---"+employee_id);
				}catch(Exception e){
					e.printStackTrace();
				    System.out.println("--failed to send email to user---"+obj[0].toString());
				}
			}
		} catch (Exception e) {
			e.printStackTrace(); 
			throw new Throwable("Failed to run bulk userid notification service--" + e.getMessage());
		}finally{
			session.close();
		}
	}
}
