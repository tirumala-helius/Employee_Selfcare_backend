package com.helius.dao;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.metamodel.source.LocalBindingContext;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.helius.entities.Employee;
import com.helius.entities.EmployeeTicketingSystemBean;
import com.helius.entities.Employee_Offer_Details;
import com.helius.entities.Employee_Ticketing_System;
import com.helius.entities.Employee_Ticketing_System_Ticket_Types;
import com.helius.service.EmailService;
import com.helius.utils.FilecopyStatus;
import com.helius.utils.Utils;

public class TicketSystemDAOImpl implements TicketSystemDAO {
	private org.hibernate.internal.SessionFactoryImpl sessionFactory;

	public org.hibernate.internal.SessionFactoryImpl getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(org.hibernate.internal.SessionFactoryImpl sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Autowired
	private EmailService emailService;
	private List<String> copied_with_success = new ArrayList<String>();
    private static final Logger logger = LogManager.getLogger(EmployeeDAOImpl.class.getName());
	@Override
	public void saveEmpTicket(Employee emp, MultipartHttpServletRequest request) throws Throwable {
		Session session = null;
		Transaction transaction = null;
		Map<String, String> templateFilenames = new HashMap<String, String>();
		Map<String, String> fileFolder = new HashMap<String, String>();
		Object TicketId = null;
		String ticketNumber = "";
		int ticket_id = 0;
		String employee_id = null;

		try {
			session = sessionFactory.openSession();
			transaction = session.beginTransaction();
			String ticketNumberQuery = "SELECT * FROM Employee_Ticketing_System";
			List<Employee_Ticketing_System> numberList = session.createSQLQuery(ticketNumberQuery).list();

			if (emp.getEmployeeTicketingSystem() != null) {
				// TicketId = emp.getEmployeeTicketingSystem().getTicket_Number();

				if (emp.getEmployeeTicketingSystem().getEmployee_id() != null
						&& !emp.getEmployeeTicketingSystem().getEmployee_id().isEmpty()) {
					ticketNumber = generateTicketNumber(numberList.size());
					emp.getEmployeeTicketingSystem().setTicket_number(ticketNumber);
				}

				TicketId = session.save(emp.getEmployeeTicketingSystem());
				ticket_id = (int) TicketId;

				if (emp.getEmployeeTicketingSystem().getTicket_attachment_path() != null
						&& !emp.getEmployeeTicketingSystem().getTicket_attachment_path().isEmpty()) {

					String modifiedFileName = ticketNumber + "_"
							+ emp.getEmployeeTicketingSystem().getTicket_attachment_path();
					templateFilenames.put(emp.getEmployeeTicketingSystem().getTicket_attachment_path(),
							modifiedFileName);
					fileFolder.put(emp.getEmployeeTicketingSystem().getTicket_attachment_path(), "ticketing_system");
				}
				// TicketId = session.save(emp);
			}

			if (TicketId != null) {
				Map<String, MultipartFile> files = null;
				files = request.getFileMap();
				if (files.size() > 0) {
					FilecopyStatus status = Utils.copyFiles(request, templateFilenames, fileFolder);
					copied_with_success = status.getCopied_with_success();
				}
			}
			transaction.commit();

			try {

				String to = "";
				String TicketNumber = ticketNumber;
				String Description = emp.getEmployeeTicketingSystem().getTicket_discription();
				String ticketage = emp.getEmployeeTicketingSystem().getTicket_age();
				String ticket_status = emp.getEmployeeTicketingSystem().getTicket_status();
				String comment = emp.getEmployeeTicketingSystem().getComments();
				String employee_name = emp.getEmployeeTicketingSystem().getEmployee_name();
				String RaisedBy = emp.getEmployeeTicketingSystem().getTicket_raised_by();
				String Tickettype = emp.getEmployeeTicketingSystem().getTicket_type();
				String work_country = emp.getEmployeeTicketingSystem().getWork_country();
				String assign = emp.getEmployeeTicketingSystem().getTicket_assigned_to();
				String query = emp.getEmployeeTicketingSystem().getTicket_query();

				//Employee_Ticketing_System_Ticket_Types ticketType = getTicketType(Tickettype, session);

				Employee_Ticketing_System_Ticket_Types ticketType = getTicketType(query, session);

				
				//change
				String tikcet_query = emp.getEmployeeTicketingSystem().getTicket_query();
				/*Timestamp date = ticketType.getCreate_date();
				SimpleDateFormat sdf = new SimpleDateFormat("dd/mm/yyyy hh:mm:ss");*/
				
				Timestamp date = emp.getEmployeeTicketingSystem().getCreate_date();
				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
				String formattedDate = sdf.format(new Date(date.getTime()));

				// change

				String tno = "";
				if (TicketNumber != null) {
					tno = TicketNumber;
				}

				String ticket_type = "";
				if (Tickettype != null) {
					ticket_type = Tickettype;
				}

				String empname = "";
				if (employee_name != null && !employee_name.isEmpty()) {
					empname = employee_name;
				}

				String descrion = "";
				if (Description != null) {
					descrion = Description;
				}

				String subject = "OPENED - "+Tickettype + " - " + TicketNumber + " raised on " +formattedDate;
				String text = "";

				Employee_Offer_Details emailid = getPersonalEmailID(emp.getEmployeeTicketingSystem().getEmployee_id(),
						session);

				String client = "";
				if (emailid != null) {
					to = emailid.getPersonal_email_id();
					client = emailid.getClient();
					String mno = emailid.getMobile_number();
			
					text = "Hi,"+ "\n\n" + "The ticket details are:" + "\n" 
							+ "1. Ticket Query - "+tikcet_query + "\n" 
							+ "2. Ticket Description - " + descrion + "\n"
							+ "3. Raised by – employee name - " + empname + "\n"
							+ "4. Raised by – employee contact email - " + to + "\n"
							+ "5. Raised by – employee – phone number - "+mno + "\n"
							+ "6. Client - "+client + "\n"
							+ "7. Location -"+work_country+ "\n\n"
							+ "Please log into HAP for more details and uploaded reference documents and update the"+ "\n"
							+ "HAP ticket status once the ticket has been resolved."+"\n\n"
							+ "Regards," + "\n"+ "Helius Operations team";

					emailService.sendEmail(to, null, null, subject, text);
				}
				       
				if (work_country.equalsIgnoreCase("Singapore")) {
					to = "";
					to = ticketType.getSingapore_helius_email_id();
					//String cc = ticketType.getCc_helius_email_id();
				    String[] cc = new String[]{ticketType.getCc_helius_email_id()};
					assign= ticketType.getSingapore_spoc_name();
					text = "Hi," + "\n\n"+"Thank you for raising this ticket which has been forwarded to "+ assign +"\n"
						       + "and he/she will work on resolving your issue. In case they need any additional" +"\n"
							   + "details or clarifications, they will contact you for the same."+"\n\n"
						       + "Once the ticket is resolved the ticket status will be changed in your self-service portal" +"\n"
							   + "and an email will be sent to you immediately." +"\n\n"
						       + "Thank you for your patience."+"\n"
							   + "Regards,"+"\n"
						       + "Helius Operations team";
					
					emailService.sendEmail(to, cc, null, subject, text);

				}
				if (work_country.equalsIgnoreCase("India")) {
					to = "";
					to = ticketType.getIndia_helius_email_id();
					//String cc= ticketType.getCc_helius_email_id();
				    String[] cc = new String[]{ticketType.getCc_helius_email_id()};

					assign= ticketType.getIndia_spoc_name();
					text = "Hi," + "\n\n"+"Thank you for raising this ticket which has been forwarded to "+ assign +"\n"
						       + "and he/she will work on resolving your issue. In case they need any additional" +"\n"
							   + "details or clarifications, they will contact you for the same."+"\n\n"
						       + "Once the ticket is resolved the ticket status will be changed in your self-service portal" +"\n"
							   + "and an email will be sent to you immediately." +"\n\n"
						       + "Thank you for your patience."+"\n"
							   + "Regards,"+"\n"
						       + "Helius Operations team";
					
					emailService.sendEmail(to, cc, null, subject, text);
				}

			} catch (Exception e) {
				employee_id = emp.getEmployeeTicketingSystem().getEmployee_id();
				String subject = "New Ticket Email Trigger Failed";
				// emailService.sendEmailInExaceptionCase(null,employee_id, subject, null);
				e.printStackTrace();
			}

		} catch (Exception e) {
			e.printStackTrace();
			Utils.deleteFiles(copied_with_success);
			throw new Throwable(e.getCause().getMessage());
		} finally {
			if (session != null) {
				session.close();
			}
		}

	}

	// method for Ticketid auto generated
	private String generateTicketNumber(Integer ticketNumber) {
		Employee emp = new Employee();
		// String lastTicketNumber = null;
		/*
		 * String lastTicketNumber =
		 * emp.getEmployeeTicketingSystem().getTicket_number(); Integer nextTicketNumber
		 * = Integer.parseInt(lastTicketNumber); nextTicketNumber = 0;
		 */
		Integer nextTicketNumber = ticketNumber + 1;
		String formattedTicketNumber = String.format("ETS%03d", nextTicketNumber);
		return formattedTicketNumber;
	}

	@Override
	public void updateTicket(Employee emp, MultipartHttpServletRequest request) throws Throwable {
		Session session = null;
		Transaction transaction = null;
		Map<String, String> templateFilenames = new HashMap<String, String>();
		Map<String, String> fileFolder = new HashMap<String, String>();

		String ticketNumber = "";

		try {
			session = sessionFactory.openSession();
			transaction = session.beginTransaction();
			if (emp.getEmployeeTicketingSystem() != null) {
				session.update(emp.getEmployeeTicketingSystem());
				String ticketid = emp.getEmployeeTicketingSystem().getTicket_number();
				if (emp.getEmployeeTicketingSystem().getTicket_attachment_path() != null
						&& !emp.getEmployeeTicketingSystem().getTicket_attachment_path().isEmpty()) {

					String modifiedFileName = ticketid + "_"
							+ emp.getEmployeeTicketingSystem().getTicket_attachment_path();
					templateFilenames.put(emp.getEmployeeTicketingSystem().getTicket_attachment_path(),
							modifiedFileName);
					fileFolder.put(emp.getEmployeeTicketingSystem().getTicket_attachment_path(), "ticketing_system");
				}

				Map<String, MultipartFile> files = null;
				files = request.getFileMap();
				if (files.size() > 0) {
					FilecopyStatus status = Utils.copyFiles(request, templateFilenames, fileFolder);
					copied_with_success = status.getCopied_with_success();
				}

			}
			transaction.commit();
		} catch (Exception e) {
			e.printStackTrace();
			Utils.deleteFiles(copied_with_success);
			throw new Throwable(e.getCause().getMessage());
		} finally {
			if (session != null) {
				session.close();
			}

		}

	}

	@Override
	public String getTicketHistoryByEmpId(String empId) {
		Employee emp = new Employee();
		Session session = null;
		List<EmployeeTicketingSystemBean> empticketlist = null;
		try {
			session = sessionFactory.openSession();
			String empTicket_query = "select * from Employee_Ticketing_System where employee_id = :empId ";
			empticketlist = session.createSQLQuery(empTicket_query)
					.setResultTransformer(Transformers.aliasToBean(EmployeeTicketingSystemBean.class))
					.setParameter("empId", empId).list();

			if (!empticketlist.isEmpty() && empticketlist != null) {
				for (EmployeeTicketingSystemBean empTicket : empticketlist) {
					if (empTicket.getCreate_date() != null) {
						LocalDate createDate = empTicket.getCreate_date().toLocalDateTime().toLocalDate();
						long daysDifference = ChronoUnit.DAYS.between(createDate, LocalDate.now());
						empTicket.setTicket_age(Long.toString(daysDifference));
					}
				}
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (session != null) {
				session.close();
			}
		}
		String empTicketByStatus = null;
		String offerdetailsjson = "";
		String empTicketjson = "";
		if (empticketlist != null) {
			ObjectMapper om = new ObjectMapper();
			try {
				empTicketjson = om.writeValueAsString(empticketlist);
				offerdetailsjson = empTicketjson.replaceAll(":null", ":\"-\"").replaceAll(":\"\"", ":\"-\"");
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
			empTicketByStatus = "{ \"empTicketByStatus\":" + offerdetailsjson + "}";
		} else {
			empTicketByStatus = "No ticket data  Available";
		}
		return empTicketByStatus;
	}

	/*public Employee_Ticketing_System_Ticket_Types getTicketType(String Tickettype, Session session) {
		String ticketTypeQuery = "SELECT * FROM Employee_Ticketing_System_Ticket_Types WHERE ticket_type='" + Tickettype
				+ "'";
		Employee_Ticketing_System_Ticket_Types ticket_Types = null;
		try {
			java.util.List ticketType = session.createSQLQuery(ticketTypeQuery)
					.addEntity(Employee_Ticketing_System_Ticket_Types.class).list();

			if (ticketType != null && !ticketType.isEmpty()) {
				ticket_Types = (Employee_Ticketing_System_Ticket_Types) ticketType.iterator().next();
			}

		} catch (Exception e) {
			throw e;
		}
		return ticket_Types;
	}*/
	public Employee_Ticketing_System_Ticket_Types getTicketType(String query, Session session) {
		String ticketTypeQuery = "SELECT * FROM Employee_Ticketing_System_Ticket_Types WHERE  ticket_query = '" + query + "'";
		Employee_Ticketing_System_Ticket_Types ticket_Types = null;
		try {
			java.util.List ticketType = session.createSQLQuery(ticketTypeQuery)
					.addEntity(Employee_Ticketing_System_Ticket_Types.class).list();

			if (ticketType != null && !ticketType.isEmpty()) {
				ticket_Types = (Employee_Ticketing_System_Ticket_Types) ticketType.iterator().next();
			}

		} catch (Exception e) {
			throw e;
		}
		return ticket_Types;
	}

	public Employee_Offer_Details getPersonalEmailID(String employee_id, Session session) {
		String offerQuery = "SELECT * FROM Employee_Offer_Details WHERE employee_id= :empId";

		Employee_Offer_Details personalEmailId = null;
		try {
			java.util.List offerDetails = session.createSQLQuery(offerQuery).addEntity(Employee_Offer_Details.class)
					.setParameter("empId", employee_id).list();

			if (offerDetails != null && !offerDetails.isEmpty()) {
				personalEmailId = (Employee_Offer_Details) offerDetails.iterator().next();
			}

		} catch (Exception e) {
			throw e;
		}
		return personalEmailId;
	}

	@Override
	public ResponseEntity<byte[]> downloadFile(String ticketid) {
		Session session = null;
		byte[] files = null;
		String check = Utils.awsCheckFlag();
		try {
			session = sessionFactory.openSession();
			LocalDate now = LocalDate.now();
			String url = null;

			String query = "SELECT * FROM Employee_Ticketing_System WHERE ticket_number =:ticketid";
			List<EmployeeTicketingSystemBean> empticketlist = session.createSQLQuery(query)
					.setResultTransformer(Transformers.aliasToBean(EmployeeTicketingSystemBean.class))
					.setParameter("ticketid", ticketid).list();

			if (!empticketlist.isEmpty() && empticketlist != null) {
				for (EmployeeTicketingSystemBean emp : empticketlist) {
					String filePath = emp.getTicket_attachment_path();

					if (filePath != null || !filePath.isEmpty() && "yes".equalsIgnoreCase(check)) {

						url = "ticketing_system" + "/" + ticketid + "_" + filePath;
						try {
							files = Utils.downloadFileByAWSS3Bucket(url);
						} catch (Exception e) {
							return new ResponseEntity<byte[]>(HttpStatus.NOT_FOUND);
						}
					} else {
						return new ResponseEntity<byte[]>(HttpStatus.NO_CONTENT);

					}
				}
			}
		} catch (Throwable e) {
			logger.error("failed to download file - " + ticketid, e.getMessage());
			return new ResponseEntity<byte[]>(HttpStatus.INTERNAL_SERVER_ERROR);
		} finally {
			if (session != null) {
				session.close();
			}
		}
		return new ResponseEntity<byte[]>(files, HttpStatus.OK);
	}

}
