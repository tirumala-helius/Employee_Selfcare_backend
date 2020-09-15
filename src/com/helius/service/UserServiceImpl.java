package com.helius.service;


import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Service;

import com.helius.entities.Employee;
import com.helius.entities.User;
import com.helius.entities.Users;
import com.helius.managers.EmployeeManager;
import com.helius.managers.UserManager;
import com.helius.utils.Logindetails;

@Service
public class UserServiceImpl implements com.helius.service.UserService {
	@Autowired
	private EmailService emailService;
	@Autowired
	private EmployeeManager employeemanager;
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

	/** create userlogin id & password **/
	@Override
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

	/** fetch userlogin role loginattempts details **/
	@Override
	public Users get(String employeeid) {
		System.out.println("\n===emp id" + employeeid);
		Users Users = new Users();
		String status = "";

		Session session = null;
		try {
			session = sessionFactory.openSession();
			String user_query = "select * from Users where employee_id = :employee_id ";
			java.util.List userlist = session.createSQLQuery(user_query).addEntity(Users.class)
					.setParameter("employee_id", employeeid).list();
			if (!userlist.isEmpty()) {
				Users = (Users) userlist.iterator().next();
			}
			session.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Users;
	}

	/**
	 * forgot password first verify the emailaddress and send email to employee
	 * to set new password
	 **/
	@Override
	public String verifyForgotEmailAddress(String employeeid, String email) {
		String status = "";
		Session session = null;
		String To = "";
		try {
			session = sessionFactory.openSession();
			Employee employee = employeemanager.getEmployee(employeeid);
			if (employee != null && email.equals(employee.getEmployeeOfferDetails().getPersonal_email_id())) {
				To = employee.getEmployeeOfferDetails().getPersonal_email_id();
				String appUrl = "http://localhost:63342/helius/changepassword.html#!/";
				SimpleMailMessage passwordResetEmail = new SimpleMailMessage();
				passwordResetEmail.setFrom("vinay.er@helius-tech.com");
				passwordResetEmail.setTo("vinay.er@helius-tech.com");
				passwordResetEmail.setSubject("Forgot Password");
				passwordResetEmail.setText("Hello " + employee.getEmployeePersonalDetails().getEmployee_id() + ","
						+ "\n\n" + "Welcome to Helius Family." + "\n\n" + "Please click below and change your password "
						+ "\n\n" + appUrl + "?token=" + employeeid + "\n\n" + "With Regards," + "\n"
						+ "Helius Technologies.");
				emailService.sendEmail(passwordResetEmail);
				status = "success";
			} else {
				status = "Please check Employee ID and Email ID does not match!";
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			status = e.getMessage();
		} finally {
			session.close();
		}
		return status;
	}

	@Override
	public void activateUserAccount(String base64Credentials) throws Throwable {
		Session session = null;
		Transaction transaction = null;
		String password = null;
		String username = null;
		try {
			session = sessionFactory.openSession();
			transaction = session.beginTransaction();
			String credentials = new String(Base64.getDecoder().decode(base64Credentials));
			final String[] values = credentials.split(":", 2);
			password = values[1];
			username = values[0];
			String pwd = (BCrypt.hashpw(password, BCrypt.gensalt()));
			Users user = get(username);
			if (user.getEmployee_id() != null) {
				int User_login_attempts = user.getUser_login_attempts();
				if (User_login_attempts == 0) {
					user.setUser_login_attempts(User_login_attempts + 1);
					user.setPassword(pwd);
					session.update(user);
				} else {
					throw new Throwable("Account is already activated please Login");
				}
			} else {
				throw new Throwable("Account not found ");
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

	/**
	 * Used to activate account and change password for the first time login and
	 * also to set new password incase of forgot
	 **/
	@Override
	public void activateUserAccount12(String base64Credentials, String forgot) throws Throwable {
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
			Users user = get(username);
			if (forgot.equals("N")) {
				if (user.getEmployee_id() != null) {
					int User_login_attempts = user.getUser_login_attempts();
					if (User_login_attempts == 0) {
						user.setUser_login_attempts(User_login_attempts + 1);
						user.setPassword(pwd);
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

	@Override
	public String forgotpswd(String base64Credentials) {
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
			Users user = get(username);
			user.setPassword(pwd);
			session.update(user);
			status = "success";
			transaction.commit();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			transaction.rollback();
			status = "Unable to change Password Please Contact HR";
		} finally {
			session.close();
		}
		return status;
	}

	/** employee login **/
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
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.helius.service.UserService#createUser(com.helius.entities.User)
	 */
	@Override
	public String createUser(User user) throws Throwable {
		Session session  = null;
		try {
			session = sessionFactory.openSession();
			Transaction transaction = session.beginTransaction();
			// user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
			session.save(user);
			transaction.commit();
			adduser_to_memory(user);
			
		} catch (HibernateException e) {
			// transaction.rollback();
			e.printStackTrace();
			throw new Throwable("Failed to Save User");
		} catch (Exception e) {
			e.printStackTrace();
			throw new Throwable("Failed to Save User" + e.getMessage());
		}
		
		finally{
			session.close();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.helius.service.UserService#updateUser(com.helius.entities.User)
	 */
	@Override
	public String updateUser(User user) throws Throwable {
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
			throw new Throwable("Failed to update User");
		} catch (Exception e) {
			e.printStackTrace();
			throw new Throwable("Failed to Save User" + e.getMessage());
		}
		finally{
			session.close();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.helius.service.UserService#deleteUser(com.helius.entities.User)
	 */
	@Override
	public String deleteUser(User user) throws Throwable {
		// TODO Auto-generated method stub
		return null;
	}

	public List<com.helius.utils.User> getAllUsers() throws Throwable {
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
	}

	/* (non-Javadoc)
	 * @see com.helius.service.UserService#validateUser()
	 */
	@Override
	public Logindetails validateUser(String userid, String password) throws Throwable {
		Logindetails validauser = new Logindetails();
		String error = "";
		if (userid != null && !userid.isEmpty()) {
			Session session = null;
			try {
				String encodedpassword = Base64.getEncoder().encodeToString(password.getBytes());
				session = sessionFactory.openSession();
				String querystr = "select * from user u where u.active='Yes' and u.userid='"
						+ userid + "' and" + " u.password='" + encodedpassword + "'";
				Query query = session.createSQLQuery(querystr).addEntity(com.helius.entities.User.class);
				Object userobj = query.uniqueResult();
				if (userobj != null) {
					com.helius.entities.User user_entity = (com.helius.entities.User) userobj;

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
					validauser.setResult("Login success");
					validauser.setUser(user_util);

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

	private void adduser_to_memory(User user) {
		String decodedpassword = new String(Base64.getDecoder().decode(user.getPassword()));
		String userid = user.getUserid();
		String[] rr = user.getRole().split(",");
		List<GrantedAuthority> role = new ArrayList<GrantedAuthority>();
		for (int i = 0; i < rr.length; i++) {
			GrantedAuthority ga = new SimpleGrantedAuthority("ROLE_"+rr[i]);
			role.add(ga);
		}			
		org.springframework.security.core.userdetails.User user_sec =
				new org.springframework.security.core.userdetails.User(user.getUserid(),decodedpassword,true,true,true,true,role);
		inMemoryUserDetailsManager.createUser(user_sec);
	}
	private void updateuser_to_memory(User user) {
		String decodedpassword = new String(Base64.getDecoder().decode(user.getPassword()));
		String userid = user.getUserid();
		String[] rr = user.getRole().split(",");
		List<GrantedAuthority> role = new ArrayList<GrantedAuthority>();
		for (int i = 0; i < rr.length; i++) {
			GrantedAuthority ga = new SimpleGrantedAuthority("ROLE_"+rr[i]);
			role.add(ga);
		}			
		org.springframework.security.core.userdetails.User user_sec =
				new org.springframework.security.core.userdetails.User(user.getUserid(),decodedpassword,true,true,true,true,role);
		inMemoryUserDetailsManager.updateUser(user_sec);
	}

	/* (non-Javadoc)
	 * @see com.helius.service.UserService#getUser(java.lang.String)
	 */
	@Override
	public com.helius.utils.User getUser(String userid) throws Exception{
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
	}
}
