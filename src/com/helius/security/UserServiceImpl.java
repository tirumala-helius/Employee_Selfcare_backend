package com.helius.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

import javax.sql.DataSource;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;

import com.helius.entities.Employee_Selfcare_Users;
import com.helius.entities.User;


public class UserServiceImpl implements UserService, ApplicationListener {
	

	

	private static final AtomicLong counter = new AtomicLong();
	
	private static List<Employee_Selfcare_Users> user_inmemories = new ArrayList<Employee_Selfcare_Users>();
	private static boolean usersLoaded = false;
	
	private SessionFactory sessionFactory = null;
	
	/*static{
		user_inmemories= populateDummyUsers();
		System.out.println("loaded dummy Users");
	
	}*/
	public UserServiceImpl() {
		
	}
	public UserServiceImpl(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
		//SessionFactory sessionFactory = localSessionFactoryBean.getObject();
		Session session = sessionFactory.openSession();
		SQLQuery query = session.createSQLQuery("select * from Employee_Selfcare_Users u")
				.addEntity(Employee_Selfcare_Users.class);
		List<Employee_Selfcare_Users> list = query.list();
		for (Employee_Selfcare_Users user : list) {
			user_inmemories.add(user);
			System.out.println("loading DB users");
		}
		usersLoaded = true;
		session.close();
	}
	
	public List<Employee_Selfcare_Users> findAllUsers() {
		return user_inmemories;
	}
	
	public Employee_Selfcare_Users findById(int id) {
		for(Employee_Selfcare_Users user_inmemory : user_inmemories){
			if(user_inmemory.getEmployee_Selfcare_Users_Id() == id){
				return user_inmemory;
			}
		}
		return null;
	}
	
	public Employee_Selfcare_Users findByName(String name) {
		for(Employee_Selfcare_Users user_inmemory : user_inmemories){
			if(user_inmemory.getEmployee_id().equalsIgnoreCase(name)){
				return user_inmemory;
			}
		}
		return null;
	}
	
	public void saveUser(Employee_Selfcare_Users user_inmemory) {
		//user_inmemory.setUserid(userid);(counter.incrementAndGet());
		user_inmemories.add(user_inmemory);
	}

	public void updateUser(Employee_Selfcare_Users user_inmemory) {
		int index = user_inmemories.indexOf(user_inmemory);
		user_inmemories.set(index, user_inmemory);
	}

	public void deleteUserById(int id) {
		
		for (Iterator<Employee_Selfcare_Users> iterator = user_inmemories.iterator(); iterator.hasNext(); ) {
			Employee_Selfcare_Users user_inmemory = iterator.next();
		    if (user_inmemory.getEmployee_Selfcare_Users_Id() == id) {
		        iterator.remove();
		    }
		}
	}

	public boolean isUserExist(Employee_Selfcare_Users user_inmemory) {
		return findByName(user_inmemory.getEmployee_id())!=null;
	}
	
	public void deleteAllUsers(){
		user_inmemories.clear();
	}

	
	public  void populateDummyUsers(DataSource datasource){
		
		/*user_inmemories.add(new User(counter.incrementAndGet(),"Sam",30, 70000));
		user_inmemories.add(new User(counter.incrementAndGet(),"Tom",40, 50000));
		user_inmemories.add(new User(counter.incrementAndGet(),"Jerome",45, 30000));
		user_inmemories.add(new User(counter.incrementAndGet(),"Silvia",50, 40000));*/
		if (!usersLoaded) {
			//List<User> user_inmemories = new ArrayList<User>();
			  
			LocalSessionFactoryBean localSessionFactoryBean = new LocalSessionFactoryBean();
			  localSessionFactoryBean.setDataSource(datasource);
			  Properties prop = new Properties();
			  prop.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
			  prop.put("hibernate.show_sql", false);			  
			  localSessionFactoryBean.setHibernateProperties(prop);	  
			  localSessionFactoryBean.setPackagesToScan("com.helius.entities");
			  localSessionFactoryBean.setAnnotatedPackages("com.helius.entities");
			  try {
				localSessionFactoryBean.afterPropertiesSet();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			SessionFactory sessionFactory = localSessionFactoryBean.getObject();
			Session session = sessionFactory.openSession();
			SQLQuery query = session.createSQLQuery("select * from Employee_Selfcare_Users u").addEntity(Employee_Selfcare_Users.class);
			List<Employee_Selfcare_Users> list = query.list();

			for (Employee_Selfcare_Users user : list) {
				user_inmemories.add(user);
				System.out.println("loading DB users");
			}
			usersLoaded = true;
			session.close();
		}
		
	}

	/* (non-Javadoc)
	 * @see com.websystique.springmvc.service.UserService#populateDummyUsers()
	 */

	
	
	
	public static void populateDBUsers() {
	/*	LocalSessionFactoryBean localSessionFactoryBean = null;
		if (event.getSource() instanceof XmlWebApplicationContext) {

			ApplicationContext applicationContext = (XmlWebApplicationContext) event.getSource();
			
			try {
				localSessionFactoryBean = (LocalSessionFactoryBean) applicationContext
						.getBean("hibernate4AnnotatedSessionFactory");
			} catch (BeansException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
		
		SessionFactory sessionFactory = localSessionFactoryBean.getObject();
		Session session = sessionFactory.openSession();
		SQLQuery query = session.createSQLQuery("select * from user u")
				.addEntity(com.websystique.springmvc.model.User.class);
		List<User> list = query.list();

		for (User user : list) {
			user_inmemories.add(user);
			System.out.println("loading DB users");
		}
		usersLoaded = true;
		session.close();
		}*/
	}

	/* (non-Javadoc)
	 * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
	 */
	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		System.out.println("event recieved");
	
	}

	/* (non-Javadoc)
	 * @see com.websystique.springmvc.service.UserService#findById(long)
	 */
	@Override
	public User findById(long id) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.websystique.springmvc.service.UserService#deleteUserById(long)
	 */
	@Override
	public void deleteUserById(long id) {
		// TODO Auto-generated method stub
		
	}

	
	
}
