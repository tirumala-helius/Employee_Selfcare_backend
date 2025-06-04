package com.helius.dao;

import java.io.IOException;
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.http.HttpHeaders;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.metamodel.source.LocalBindingContext;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.helius.entities.Employee;
import com.helius.entities.EmployeeCommentsList;
import com.helius.entities.EmployeeTicketingFiles;
import com.helius.entities.EmployeeTicketingFilesBean;
import com.helius.entities.EmployeeTicketingFilesCombine;
import com.helius.entities.EmployeeTicketingSystemBean;
import com.helius.entities.EmployeeTicketingSystemCommentsList;
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
    
    
    /**
     * This feature is related to the Employee Ticketing System. In this, we are saving ticket data in the database along with related files.
     * We are storing the files in S3 and saving their paths in the database. Additionally, we are retrieving the required data from the database 
     * for email sending, saving it in JSON format, and sending an email to the employee and the respective ticket owner.
     */

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
		String assign_name = null;
	    String originalFileName = null;
	    FilecopyStatus status = null;

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
				//ticket_id = emp.getEmployeeTicketingSystem().getEmployee_ticketing_system_id(); 
				

			/*	if (emp.getEmployeeTicketingSystem().getTicket_attachment_path() != null
						&& !emp.getEmployeeTicketingSystem().getTicket_attachment_path().isEmpty()) {



				}*/
				// TicketId = session.save(emp);
			}

			/*if (TicketId != null) {
				Map<String, MultipartFile> files = null;
				files = request.getFileMap();
				if (files.size() > 0) {
					FilecopyStatus status = Utils.copyFiles(request, templateFilenames, fileFolder);
					copied_with_success = status.getCopied_with_success();
				}
			}*/
			//change
		      // Handle file upload
			
/*			String country = emp.getEmployeeTicketingSystem().getWork_country();
			String query = emp.getEmployeeTicketingSystem().getTicket_query();
			String raised_by = emp.getEmployeeTicketingSystem().getEmployee_name();
			String Tickettype = emp.getEmployeeTicketingSystem().getTicket_type();*/

			
			//Employee_Ticketing_System_Ticket_Types ticketType = getTicketType(Tickettype,query, session);
			  if (TicketId != null) {
	                Map<String, List<MultipartFile>> filesMap = request.getMultiFileMap();
	                if (filesMap != null && !filesMap.isEmpty()) {
	                    for (Map.Entry<String, List<MultipartFile>> entry : filesMap.entrySet()) {
	                        String fieldName = entry.getKey();  // Field name (for example, "files" or any other key used in the form)
	                        List<MultipartFile> files = entry.getValue();  // List of files associated with this field

	                        for (MultipartFile file : files) {
	                            if (file != null && !file.isEmpty()) {
	                                originalFileName = file.getOriginalFilename();
	                                String modifiedFileName = ticketNumber + "_" + originalFileName;  // Create a modified file name
	                                
	                                // Store the original and modified file names for later use
	                                templateFilenames.put(originalFileName, modifiedFileName);
	                                fileFolder.put(originalFileName, "ticketing_system");
	                                
	                                // Copy the files to the desired location
	                              //  FilecopyStatus status = Utils.copyFiles(request, templateFilenames, fileFolder);
	                        
	                                
	                       
	                        
	                        
	                				if (filesMap.size() > 0) {
	                					 status = Utils.copyFiles(request, templateFilenames, fileFolder);
	                					copied_with_success = status.getCopied_with_success();
	                				}

	                                if (status != null && status.getCopied_with_success() != null && !status.getCopied_with_success().isEmpty()) {
	                                    copied_with_success = status.getCopied_with_success();
	                                    System.out.println("File copied successfully: " + originalFileName);
	                                } else {
	                                    System.err.println("File copy failed for: " + originalFileName);
	                                }

	                                /*EmployeeTicketingFiles empticketfiles = new EmployeeTicketingFiles();
	                                emp.getEmployeeTicketingFiles().setTicket_id(ticketNumber);
	                                //empticketfiles.setTicket_id(ticketNumber);
	                                emp.getEmployeeTicketingFiles().setTicket_path(originalFileName);
	                               // empticketfiles.setTicket_path(originalFileName);
	                                session.save(emp.getEmployeeTicketingFiles());*/
	                                
	                                EmployeeTicketingFiles empticketfiles = new EmployeeTicketingFiles();
	                                //empticketfiles.setTicket_id(ticketNumber);
	                                if(ticketNumber!=null && originalFileName!=null){
	                                empticketfiles.setTicket_number(ticketNumber);
	                                empticketfiles.setTicket_path(modifiedFileName);
	                                empticketfiles.setEmployee_id(emp.getEmployeeTicketingSystem().getEmployee_id());
	                                empticketfiles.setCreated_by(emp.getEmployeeTicketingSystem().getEmployee_name());
	                                empticketfiles.setLast_modified_by(emp.getEmployeeTicketingSystem().getEmployee_name());
	                                }

	                                // Save each new EmployeeTicketingFiles object
	                                //session.save(empticketfiles);
	                                session.saveOrUpdate(empticketfiles);
	                              /*  EmployeeTicketingFiles empticketfiles = new EmployeeTicketingFiles();
	                                if (emp.getEmployeeTicketingFiles() == null) {
	                                    emp.setEmployeeTicketingFiles(new EmployeeTicketingFiles());
	                                }

	                                // Now you can safely set the data
	                                emp.getEmployeeTicketingFiles().setTicket_id(ticketNumber);
	                                emp.getEmployeeTicketingFiles().setTicket_path(originalFileName);
	                                
	                                // Save the EmployeeTicketingFiles object associated with emp
	                                session.save(emp.getEmployeeTicketingFiles());*/
	                        
	                                // Create and save new Employee_Ticketing_System record with file path for each file
	                             //   Employee_Ticketing_System empTicket = new Employee_Ticketing_System();
	                                
	                              /*  empTicket.setTicket_number(ticketNumber);
	                                empTicket.setTicket_attachment_path(modifiedFileName);  // Set the file path for each file
	                                empTicket.setEmployee_id(emp.getEmployeeTicketingSystem().getEmployee_id());
	                                empTicket.setEmployee_name(emp.getEmployeeTicketingSystem().getEmployee_name());
	                                empTicket.setTicket_query(emp.getEmployeeTicketingSystem().getTicket_query());
	                                empTicket.setTicket_type(emp.getEmployeeTicketingSystem().getTicket_type());
	                                empTicket.setTicket_discription(emp.getEmployeeTicketingSystem().getTicket_discription());
	                                empTicket.setTicket_age(emp.getEmployeeTicketingSystem().getTicket_age());
	                                empTicket.setTicket_status(emp.getEmployeeTicketingSystem().getTicket_status());
	                                empTicket.setComments(emp.getEmployeeTicketingSystem().getComments());
	                                empTicket.setWork_country(emp.getEmployeeTicketingSystem().getWork_country());
	                                empTicket.setCreate_date(new Timestamp(System.currentTimeMillis()));*/
	                                // Additional fields you may want to set, such as raised_by, assigned_to, etc.
	                                     
	                        
	                    			/*if(country.equalsIgnoreCase("india")){
	                    				assign_name= ticketType.getIndia_spoc_name();
	                    				emp.getEmployeeTicketingSystem().setTicket_assigned_to(assign_name);
	                    				empTicket.setTicket_assigned_to(assign_name);
	                    			}if(country.equalsIgnoreCase("Singapore")){
	                    				assign_name = ticketType.getSingapore_spoc_name();
	                    				emp.getEmployeeTicketingSystem().setTicket_assigned_to(assign_name);
	                    				empTicket.setTicket_assigned_to(assign_name);
	                    			}
	                    			emp.getEmployeeTicketingSystem().setTicket_raised_by(raised_by);*/

	                                // Save the new ticket record with the file path
	                    			//TicketId = session.save(empTicket);
	                              
	                            }
	                        }
	                    }
	                }
	            }
			/*if (TicketId != null) {
			    // Retrieve the files map from the request
			    Map<String, List<MultipartFile>> filesMap = request.getMultiFileMap();
			    
			    
			    if (filesMap != null && !filesMap.isEmpty()) {
			        // Iterate over the list of files
			        for (Map.Entry<String, List<MultipartFile>> entry : filesMap.entrySet()) {
			            String fieldName = entry.getKey();  // Field name (for example, "files" or any other key used in the form)
			            List<MultipartFile> files = entry.getValue();  // List of files associated with this field name
			            
			            // Iterate over the files for this field
			            for (MultipartFile file : files) {
			                if (file != null && !file.isEmpty()) {
			                    originalFileName = file.getOriginalFilename();
			                    String modifiedFileName = ticketNumber + "_" + originalFileName;  // Create a modified file name
			                    
			                    // Store the original and modified file names for later use
			                    templateFilenames.put(originalFileName, modifiedFileName);
			                    fileFolder.put(originalFileName, "ticketing_system");
			                    
			                    // Copy the files to the desired location
			                    FilecopyStatus status = Utils.copyFiles(request, templateFilenames, fileFolder);
			                    
			                    if (status != null && status.getCopied_with_success() != null && !status.getCopied_with_success().isEmpty()) {
			                        // File copy was successful
			                        System.out.println("File copied successfully: " + originalFileName);
			                    } else {
			                        // Handle failure case
			                        System.err.println("File copy failed for: " + originalFileName);
			                    }
			                }
			            }
			        }
			    }
			}*/
			//emp.getEmployeeTicketingSystem().setTicket_attachment_path(originalFileName);

			String country = emp.getEmployeeTicketingSystem().getWork_country();
			String query = emp.getEmployeeTicketingSystem().getTicket_query();
			String raised_by = emp.getEmployeeTicketingSystem().getEmployee_name();
			String Tickettype = emp.getEmployeeTicketingSystem().getTicket_type();

			
			Employee_Ticketing_System_Ticket_Types ticketType = getTicketType(Tickettype,query, session);

			if(country.equalsIgnoreCase("india")){
				assign_name= ticketType.getIndia_spoc_name();
				emp.getEmployeeTicketingSystem().setTicket_assigned_to(assign_name);
				
			}if(country.equalsIgnoreCase("Singapore")){
				assign_name = ticketType.getSingapore_spoc_name();
				emp.getEmployeeTicketingSystem().setTicket_assigned_to(assign_name);
			}
			emp.getEmployeeTicketingSystem().setTicket_raised_by(raised_by);
		    //session.save(emp.getEmployeeTicketingSystem());
	      //  session.saveOrUpdate(emp.getEmployeeTicketingSystem()); // Ensure the entity is saved or updated

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
				String work_country = emp.getEmployeeTicketingSystem().getWork_country();
				String assign = emp.getEmployeeTicketingSystem().getTicket_assigned_to();

	
				String tikcet_query = emp.getEmployeeTicketingSystem().getTicket_query();
				
				Timestamp date = emp.getEmployeeTicketingSystem().getCreate_date();
				Timestamp time = new Timestamp(System.currentTimeMillis());
				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
				String formattedDate = sdf.format(new Date(time.getTime()));

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
										
				  if (work_country.equalsIgnoreCase("india")){
						
					assign= ticketType.getIndia_spoc_name();
				/*	text = "Hi," + "\n\n"+"Thank you for raising this ticket which has been forwarded to "+ assign +"\n"
						       + "and he/she will work on resolving your issue. In case they need any additional" +"\n"
							   + "details or clarifications, they will contact you for the same."+"\n\n"
						       + "Once the ticket is resolved the ticket status will be changed in your self-service portal" +"\n"
							   + "and an email will be sent to you immediately." +"\n\n"
						       + "Thank you for your patience."+"\n"
							   + "Regards,"+"\n"
						       + "Helius Operations team";*/
					
					//change
					text = "Hi," + "\n\n"+"Thank you for raising this ticket which has been forwarded to "+ assign +" and he/she will work on resolving your issue." + "\n\n"
					             + "Our usual response time is one business day. If we require additional time to consult with other relevant parties, we will update you." + "\n\n"
							     + "To view the status of your request in the self-service portal, please go to 'Employee Ticketing System' and select 'View Ticket History'." + "\n\n"
					             + "Once the ticket is resolved, the ticket status will be changed in your self-service portal, and an email will be sent to you immediately." + "\n\n"
							     + "Regards," + "\n" 
					             + "Helius Operations team";
					

					emailService.sendEmail(to, null, null, subject, text);
				}
					if (work_country.equalsIgnoreCase("singapore")){
						
						assign= ticketType.getSingapore_spoc_name();
						/*text = "Hi," + "\n\n"+"Thank you for raising this ticket which has been forwarded to "+ assign +"\n"
							       + "and he/she will work on resolving your issue. In case they need any additional" +"\n"
								   + "details or clarifications, they will contact you for the same."+"\n\n"
							       + "Once the ticket is resolved the ticket status will be changed in your self-service portal" +"\n"
								   + "and an email will be sent to you immediately." +"\n\n"
							       + "Thank you for your patience."+"\n"
								   + "Regards,"+"\n"
							       + "Helius Operations team";*/
						
						text = "Hi," + "\n\n"+"Thank you for raising this ticket which has been forwarded to "+ assign +" and he/she will work on resolving your issue." + "\n\n"
					             + "Our usual response time is one business day. If we require additional time to consult with other relevant parties, we will update you." + "\n\n"
							     + "To view the status of your request in the self-service portal, please go to 'Employee Ticketing System' and select 'View Ticket History'." + "\n\n"
					             + "Once the ticket is resolved, the ticket status will be changed in your self-service portal, and an email will be sent to you immediately." + "\n\n"
							     + "Regards," + "\n" 
					             + "Helius Operations team";

						emailService.sendEmail(to, null, null, subject, text);
					}
				}
				       
				if (work_country.equalsIgnoreCase("Singapore")) {
					to = "";
					to = ticketType.getSingapore_helius_email_id();
					String mno = emailid.getMobile_number();
				   // String[] cc = new String[]{ticketType.getCc_helius_email_id()};	
				   //assign= ticketType.getSingapore_spoc_name();
					
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
				if (work_country.equalsIgnoreCase("India")) {
					to = "";
					to = ticketType.getIndia_helius_email_id();
					//String cc= ticketType.getCc_helius_email_id();
				    String[] cc = new String[]{ticketType.getCc_helius_email_id()};

					String mno = emailid.getMobile_number();
					//assign= ticketType.getIndia_spoc_name();
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
	/**
	 * This code is used to generate a unique ticket number for the Employee
	 * Ticketing System. It ensures that each ticket created for an employee has
	 * a distinct identifier that can be used for tracking, managing, and
	 * referencing the ticket throughout its lifecycle. *
	 */
	private String generateTicketNumber(Integer ticketNumber) {
		Employee emp = new Employee();
		
		Integer nextTicketNumber = ticketNumber + 1;
		String formattedTicketNumber = String.format("ETS%03d", nextTicketNumber);
		return formattedTicketNumber;
	}

	/*@Override
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
*/
	/*@Override
	public void updateTicket(EmployeeTicketingFilesCombine emp, MultipartHttpServletRequest request) throws Throwable {
	    Session session = null;
	    Transaction transaction = null;
	    Map<String, String> templateFilenames = new HashMap<>();
	    Map<String, String> fileFolder = new HashMap<>();
	    String ticketNumber = "";
	    int ticket_id = 0;
	    String employee_id = null;
	    String assign_name = null;
	    String originalFileName = null;
	    FilecopyStatus status = null;
	    List<EmployeeTicketingFiles> fileDTOs = new ArrayList<>();

	    try {
	        session = sessionFactory.openSession();
	        transaction = session.beginTransaction();

	        if (emp != null) {
	        	 session.merge(emp);
	        }

	        ticketNumber = emp.getTicket_number();
	        if (ticketNumber != null && !ticketNumber.isEmpty()) {
	            Map<String, List<MultipartFile>> filesMap = request.getMultiFileMap();
	            if (filesMap != null && !filesMap.isEmpty()) {
	                for (Map.Entry<String, List<MultipartFile>> entry : filesMap.entrySet()) {
	                    String fieldName = entry.getKey();  // Field name (for example, "files" or any other key used in the form)
	                    List<MultipartFile> files = entry.getValue();  // List of files associated with this field

	                    for (MultipartFile file : files) {
	                        if (file != null && !file.isEmpty()) {
	                            originalFileName = file.getOriginalFilename();
	                            String modifiedFileName = ticketNumber + "_" + originalFileName;  // Create a modified file name

	                            templateFilenames.put(originalFileName, modifiedFileName);
	                            fileFolder.put(originalFileName, "ticketing_system");

	                            if (filesMap.size() > 0) {
	                                status = Utils.copyFiles(request, templateFilenames, fileFolder);
	                                List<String> copied_with_success = status.getCopied_with_success();

	                                if (status != null && copied_with_success != null && !copied_with_success.isEmpty()) {
	                                    System.out.println("File copied successfully: " + originalFileName);
	                                } else {
	                                    System.err.println("File copy failed for: " + originalFileName);
	                                }

	                                // Create the EmployeeTicketingFileDTO and add it to the list
	                                EmployeeTicketingFiles fileDTO = new EmployeeTicketingFiles();
	                                fileDTO.setTicket_number(ticketNumber);
	                                fileDTO.setTicket_path(modifiedFileName);
	                                fileDTO.setEmployee_id(emp.getEmployee_id());
	                                //fileDTO.setCreated_date(System.currentTimeMillis());
	                                //fileDTO.setCreated_by("-");  // Assuming you get this from context or use a placeholder
	                               // fileDTO.setLast_modified_by("-");
	                                //fileDTO.setLast_modified_date(System.currentTimeMillis());
	                                //fileDTO.setFile_id(0);  // Assign a valid ID based on your logic (e.g., auto-generated ID)

	                                fileDTOs.add(fileDTO);

	                                EmployeeTicketingFiles empticketfiles = new EmployeeTicketingFiles();
	                                empticketfiles.setTicket_number(ticketNumber);
	                                empticketfiles.setTicket_path(modifiedFileName);
	                                empticketfiles.setEmployee_id(emp.getEmployee_id());
	                                session.saveOrUpdate(empticketfiles);
	                            }
	                        }
	                    }
	                }
	            }
	        }

	        // Now, create the EmployeeTicketingSystemDTO and set values
	        EmployeeTicketingFilesCombine ticketingSystemDTO = new EmployeeTicketingFilesCombine();
	        ticketingSystemDTO.setEmployee_id(emp.getEmployee_id());
	        ticketingSystemDTO.setEmployee_ticketing_system_id(emp.getEmployee_ticketing_system_id());
	        ticketingSystemDTO.setTicket_query(emp.getTicket_query());
	        ticketingSystemDTO.setEmployee_name(emp.getEmployee_name());
	        ticketingSystemDTO.setTicket_raised_by(emp.getTicket_raised_by());
	        ticketingSystemDTO.setTicket_age(emp.getTicket_age());
	        ticketingSystemDTO.setTicket_closure_date(emp.getTicket_closure_date());
	        ticketingSystemDTO.setLast_modified_date(emp.getLast_modified_date());
	        ticketingSystemDTO.setLast_modified_by(emp.getLast_modified_by());
	        ticketingSystemDTO.setCreate_date(emp.getCreate_date());
	        ticketingSystemDTO.setTicket_number(emp.getTicket_number());
	        ticketingSystemDTO.setTicket_type(emp.getTicket_type());
	        ticketingSystemDTO.setTicket_status(emp.getTicket_status());
	        ticketingSystemDTO.setTicket_assigned_to(emp.getTicket_assigned_to());
	        ticketingSystemDTO.setTicket_discription(emp.getTicket_discription());
	        ticketingSystemDTO.setComments(emp.getComments());
	        ticketingSystemDTO.setCreated_by(emp.getCreated_by());
	        ticketingSystemDTO.setTicket_attachment_path(emp.getTicket_attachment_path());
	        ticketingSystemDTO.setWork_country(emp.getWork_country());
	        ticketingSystemDTO.setEmployeeTicketingFiles(fileDTOs);  // Set the list of files

	        // Return or send the DTO as response
	        // Assuming you are sending this as JSON response
	        // You can use a framework like Jackson to serialize it to JSON if needed

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
	}*/
	
	
	/**
	 * In this, we are updating ticket data in the database along with related files.
	 * If the old ticket owner is not equal to the new ticket owner, we send an email to the new ticket owner.
	 * Additionally, we are updating the data in the database and saving the ticket data in the respective JSON format.
	 */

	@Override
	public void updateTicket(EmployeeTicketingFilesCombine emp, MultipartHttpServletRequest request, String userName) throws Throwable {
	    Session session = null;
	    Transaction transaction = null;
	    Map<String, String> templateFilenames = new HashMap<>();
	    Map<String, String> fileFolder = new HashMap<>();
	    String ticketNumber = emp.getEmployee_Ticketing_System().getTicket_number();
	    List<EmployeeTicketingFiles> fileDTOs = new ArrayList<>();
	    List<String> copied_with_success = new ArrayList<>();

	    try {
	        session = sessionFactory.openSession();
	        transaction = session.beginTransaction();

	        if (emp.getEmployee_Ticketing_System() != null) {
	        	session.evict(emp.getEmployee_Ticketing_System());
	            session.merge(emp.getEmployee_Ticketing_System());  // Merge emp object into the session
	        }
	     /*   if (emp.getEmployeeTicketingFiles() != null) {
	        	
	        	List<EmployeeTicketingFiles> ticketingFiles = emp.getEmployeeTicketingFiles();
	        	
	        	for (EmployeeTicketingFiles ticket: ticketingFiles){
	        		 String modifiedFileName =  ticket.getTicket_path();
	        		
	        		  EmployeeTicketingFiles existingFile = emp.getEmployeeTicketingFiles().stream()
                              .filter(f -> f.getTicket_path().equals(modifiedFileName))
                              .findFirst()
                              .orElse(null);

                          EmployeeTicketingFiles fileDTO = new EmployeeTicketingFiles();
                          if (existingFile != null) {
                              // Update the existing file record
                              fileDTO.setFile_id(existingFile.getFile_id());
                              fileDTO.setTicket_number(ticketNumber);
                              fileDTO.setTicket_path(modifiedFileName);
                              fileDTO.setEmployee_id(emp.getEmployee_Ticketing_System().getEmployee_id());
                              fileDTO.setLast_modified_by(emp.getEmployee_Ticketing_System().getEmployee_name());
                             // session.update(fileDTO);
                              session.saveOrUpdate(fileDTO);
                          }
	        		
	        		session.evict(ticket);
	        		session.merge(ticket);
	        	}
	        }*/
	        if(emp.getEmployeeTicketingFiles()!=null && !emp.getEmployeeTicketingFiles().isEmpty()){
	            List<EmployeeTicketingFiles> ticketingFiles = emp.getEmployeeTicketingFiles();
		        	
		        	for (EmployeeTicketingFiles ticket1: ticketingFiles){
		        		 String modifiedFileName =  ticket1.getTicket_path();
		        		
		        		  EmployeeTicketingFiles existingFile = emp.getEmployeeTicketingFiles().stream()
	                              .filter(f -> f.getTicket_path().equals(modifiedFileName))
	                              .findFirst()
	                              .orElse(null);

	                          EmployeeTicketingFiles fileDTO = new EmployeeTicketingFiles();
	                          if (existingFile != null) {
	                              // Update the existing file record
	                              fileDTO.setFile_id(existingFile.getFile_id());
	                              fileDTO.setTicket_number(existingFile.getTicket_number());
	                              fileDTO.setTicket_path(existingFile.getTicket_path());
	                              fileDTO.setEmployee_id(existingFile.getEmployee_id());
	                             // fileDTO.setLast_modified_by(emp.getEmployee_Ticketing_System().getEmployee_name());
	                              fileDTO.setLast_modified_by(existingFile.getLast_modified_by());
	                              
	                              fileDTO.setCreated_date(existingFile.getCreated_date());
	                              fileDTO.setCreated_by(existingFile.getCreated_by());
	                             // session.update(fileDTO);
	                              session.saveOrUpdate(fileDTO);
	                              session.evict(fileDTO);
	                            /*  session.evict(fileDTO);
	                              session.merge(fileDTO);*/
	                          }
		        		
					//session.evict(emp.getEmployeeTicketingFiles());
					//session.merge(emp.getEmployeeTicketingFiles());
				}
				}

	        if (ticketNumber != null && !ticketNumber.isEmpty()) {
	            Map<String, List<MultipartFile>> filesMap = request.getMultiFileMap();
	            if (filesMap != null && !filesMap.isEmpty()) {
	                for (Map.Entry<String, List<MultipartFile>> entry : filesMap.entrySet()) {
	                    List<MultipartFile> files = entry.getValue();

	                    for (MultipartFile file : files) {
	                        if (file != null && !file.isEmpty()) {
	                            String originalFileName = file.getOriginalFilename();
	                            String modifiedFileName = ticketNumber + "_" + originalFileName;

	                            // Mapping filenames and folder for file storage
	                            templateFilenames.put(originalFileName, modifiedFileName);
	                            fileFolder.put(originalFileName, "ticketing_system");

	                            // Copy the files
	                            FilecopyStatus status = Utils.copyFiles(request, templateFilenames, fileFolder);
	                            copied_with_success = status.getCopied_with_success();

	                            if (status != null && copied_with_success != null && !copied_with_success.isEmpty()) {
	                                System.out.println("File copied successfully: " + originalFileName);

	                                // Check if the file already exists based on ticket path
	                               /* EmployeeTicketingFiles existingFile = emp.getEmployeeTicketingFiles().stream()
	                                    .filter(f -> f.getTicket_path().equals(modifiedFileName))
	                                    .findFirst()
	                                    .orElse(null);*/
	                                EmployeeTicketingFiles existingFile = null;

	                                for (EmployeeTicketingFiles file1 : emp.getEmployeeTicketingFiles()) {
	                                    if (file1.getTicket_path().equals(modifiedFileName)) {
	                                        existingFile = file1;
	                                        break; // Exit the loop once the file is found
	                                    }
	                                }


	                                EmployeeTicketingFiles fileDTO = new EmployeeTicketingFiles();
	                              /* if (existingFile != null) {
	                                    // Update the existing file record
	                                    fileDTO.setFile_id(existingFile.getFile_id());
	                                    fileDTO.setTicket_number(ticketNumber);
	                                    fileDTO.setTicket_path(modifiedFileName);
	                                    fileDTO.setEmployee_id(emp.getEmployee_Ticketing_System().getEmployee_id());
	                                    session.update(fileDTO);
	                                }*/ //else {
	                                    // Create a new file record
	                                    if(existingFile==null){
	                                    fileDTO.setTicket_number(ticketNumber);
	                                    fileDTO.setTicket_path(modifiedFileName);
	                                    fileDTO.setEmployee_id(emp.getEmployee_Ticketing_System().getEmployee_id());
	                                    //fileDTO.setCreated_by(emp.getEmployee_Ticketing_System().getEmployee_name());
	                                    fileDTO.setCreated_by(userName);
	                                    fileDTO.setLast_modified_by(userName);
	                                    session.save(fileDTO);
	                               }

	                                // Save or update the fileDTO in the session
	                              //  session.saveOrUpdate(fileDTO);
	                                fileDTOs.add(fileDTO);
	                            } else {
	                                System.err.println("File copy failed for: " + originalFileName);
	                            }
	                        }
	                    }
	                }
	            }
	        }

	        // Convert EmployeeTicketingFiles to EmployeeTicketingFilesBean if necessary
	        List<EmployeeTicketingFilesBean> fileDTOBeans = fileDTOs.stream()
	            .map(file -> {
	                EmployeeTicketingFilesBean bean = new EmployeeTicketingFilesBean();
	                // Set properties from fileDTO to fileDTOBean (assuming matching fields)
	                bean.setTicket_number(file.getTicket_number());
	                bean.setTicket_path(file.getTicket_path());
	                bean.setEmployee_id(file.getEmployee_id());
	                return bean;
	            })
	            .collect(Collectors.toList());

	        // Set the fileDTOBeans into emp (DTO object)
	       // emp.setEmployeeTicketingFiles(fileDTOBeans);

	        // Commit the transaction
	        transaction.commit();
	    } catch (Exception e) {
	        e.printStackTrace();
	        Utils.deleteFiles(copied_with_success);
	        if (transaction != null) transaction.rollback();
	        throw new Throwable("Error updating ticket: " + e.getMessage(), e);
	    } finally {
	        if (session != null) {
	            session.close();
	        }
	    }
	}


	/*@Override
	public void updateTicket(Employee_Ticketing_System emp, MultipartHttpServletRequest request) throws Throwable {
		Session session = null;
		Transaction transaction = null;
		Map<String, String> templateFilenames = new HashMap<String, String>();
		Map<String, String> fileFolder = new HashMap<String, String>();
		String ticketNumber = "";
		int ticket_id = 0;
		String employee_id = null;
		String assign_name = null;
	    String originalFileName = null;
	    FilecopyStatus status = null;

		// String ticketNumber = "";

		try {
			session = sessionFactory.openSession();
			transaction = session.beginTransaction();

			if (emp.getEmployeeTicketingSystem() != null) {
				session.evict(emp.getEmployeeTicketingSystem());
				session.merge(emp.getEmployeeTicketingSystem());
			}
			if (emp!= null) {
				session.evict(emp);
				session.merge(emp);
			}
			 ticketNumber = emp.getTicket_number();
			if(emp.getTicket_number()!=null && !emp.getTicket_number().isEmpty()){
                Map<String, List<MultipartFile>> filesMap = request.getMultiFileMap();
                if (filesMap != null && !filesMap.isEmpty()) {
                    for (Map.Entry<String, List<MultipartFile>> entry : filesMap.entrySet()) {
                        String fieldName = entry.getKey();  // Field name (for example, "files" or any other key used in the form)
                        List<MultipartFile> files = entry.getValue();  // List of files associated with this field

                        for (MultipartFile file : files) {
                            if (file != null && !file.isEmpty()) {
                                originalFileName = file.getOriginalFilename();
                                String modifiedFileName = ticketNumber + "_" + originalFileName;  // Create a modified file name
                                
                                // Store the original and modified file names for later use
                                templateFilenames.put(originalFileName, modifiedFileName);
                                fileFolder.put(originalFileName, "ticketing_system");
                           
                        
                				if (filesMap.size() > 0) {
                					 status = Utils.copyFiles(request, templateFilenames, fileFolder);
                					copied_with_success = status.getCopied_with_success();
                				}

                                if (status != null && status.getCopied_with_success() != null && !status.getCopied_with_success().isEmpty()) {
                                    copied_with_success = status.getCopied_with_success();
                                    System.out.println("File copied successfully: " + originalFileName);
                                } else {
                                    System.err.println("File copy failed for: " + originalFileName);
                                }

                            
                                
                                EmployeeTicketingFiles empticketfiles = new EmployeeTicketingFiles();
                                //empticketfiles.setTicket_id(ticketNumber);
                                if(ticketNumber!=null && originalFileName!=null){
                                empticketfiles.setTicket_number(ticketNumber);
                                empticketfiles.setTicket_path(modifiedFileName);
                                empticketfiles.setEmployee_id(emp.getEmployee_id());
                                }
                                session.saveOrUpdate(empticketfiles);


                            }
                        }
                    }
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

	}*/

	/*@Override
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
					String ticket_id = empTicket.getTicket_number();
					int employee_ticketing_system_id = empTicket.getEmployee_ticketing_system_id();
					String ticket_type = empTicket.getTicket_type(); 
					String country = empTicket.getWork_country();
					String assign = empTicket.getTicket_assigned_to();
					String query = "SELECT * FROM Employee_Ticketing_System_Ticket_Types WHERE ticket_type ='" + ticket_type + "'";
					List<Employee_Ticketing_System_Ticket_Types> ltd = session.createSQLQuery(query).addEntity(Employee_Ticketing_System_Ticket_Types.class).list();
					String india_spoc = ltd.get(0).getIndia_spoc_name();
					String singapore_spoc = ltd.get(0).getSingapore_spoc_name();
					
					if("India".equalsIgnoreCase(country)){
						empTicket.setTicket_assigned_to(india_spoc);
					}
					else if("Singapore".equalsIgnoreCase(country)){
						empTicket.setTicket_assigned_to(singapore_spoc);
					}
				
					String qu= "SELECT * FROM Employee_Ticketing_System WHERE ticket_number ='" + ticket_id + "'";
					List<Employee_Ticketing_System>td = session.createSQLQuery(qu).addEntity(Employee_Ticketing_System.class).list();
					String new_spoc = td.get(0).getTicket_assigned_to();
					if(new_spoc!=null){
						empTicket.setTicket_assigned_to(new_spoc);
					}

					//change
				//	String Quer = "select * from EmployeeTicketingFiles where ticket_number = '"+ ticket_id + "' AND employee_id = '" + empId + "'" ;
					String Quer = "SELECT * FROM EmployeeTicketingFiles WHERE ticket_number = '" + ticket_id + 
				              "' AND employee_id = '" + empId + "'";
				              "' AND employee_ticketing_system_id = " + employee_ticketing_system_id;
					List<EmployeeTicketingFiles> ticketlist = session.createSQLQuery(Quer).addEntity(EmployeeTicketingFiles.class).list();
					if(ticketlist!=null && !ticketlist.isEmpty()){
						empTicket.setEmployeeTicketingFiles(ticketlist);
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
	}*/
	
	
	/**
	 * In this, we are retrieving all ticket data and the respective ticket files of a particular employee 
	 * based on employee_id and provide it to the frontend in the respective JSON format.
	 */

	@Override
	public String getTicketHistoryByEmpId(String empId) {
	    Employee emp = new Employee();
	    Session session = null;
	    List<EmployeeTicketingSystemBean> empticketlist = null;
	    try {
	        session = sessionFactory.openSession();
	        String empTicket_query = "SELECT * FROM Employee_Ticketing_System WHERE employee_id = :empId ";
	        empticketlist = session.createSQLQuery(empTicket_query)
	                .setResultTransformer(Transformers.aliasToBean(EmployeeTicketingSystemBean.class))
	                .setParameter("empId", empId)
	                .list();

	        if (!empticketlist.isEmpty() && empticketlist != null) {
	            for (EmployeeTicketingSystemBean empTicket : empticketlist) {
	                // Assign SPOC based on country
	                String ticket_id = empTicket.getTicket_number();
	                String country = empTicket.getWork_country();
	                String ticket_type = empTicket.getTicket_type();

	                String query = "SELECT * FROM Employee_Ticketing_System_Ticket_Types WHERE ticket_type ='" + ticket_type + "'";
	                List<Employee_Ticketing_System_Ticket_Types> ltd = session.createSQLQuery(query).addEntity(Employee_Ticketing_System_Ticket_Types.class).list();
	                String india_spoc = ltd.get(0).getIndia_spoc_name();
	                String singapore_spoc = ltd.get(0).getSingapore_spoc_name();

	                if ("India".equalsIgnoreCase(country)) {
	                    empTicket.setTicket_assigned_to(india_spoc);
	                } else if ("Singapore".equalsIgnoreCase(country)) {
	                    empTicket.setTicket_assigned_to(singapore_spoc);
	                }

	                // Fetch ticket assigned to based on ticket_number
	                String qu = "SELECT * FROM Employee_Ticketing_System WHERE ticket_number ='" + ticket_id + "'";
	                List<Employee_Ticketing_System> td = session.createSQLQuery(qu).addEntity(Employee_Ticketing_System.class).list();
	                String new_spoc = td.get(0).getTicket_assigned_to();
	                if (new_spoc != null) {
	                    empTicket.setTicket_assigned_to(new_spoc);
	                }

	                // Fetch ticket files for the current ticket
	                String fileQuery = "SELECT * FROM EmployeeTicketingFiles WHERE ticket_number = '" + ticket_id + "' AND employee_id = '" + empId + "'";
	                List<EmployeeTicketingFiles> ticketFilesList = session.createSQLQuery(fileQuery).addEntity(EmployeeTicketingFiles.class).list();
	                if (ticketFilesList != null && !ticketFilesList.isEmpty()) {
	                    empTicket.setEmployeeTicketingFiles(ticketFilesList);  // Setting files for the current ticket
	                }
	            }
	        }
	    } catch (Exception e) {
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
	            offerdetailsjson = empTicketjson.replaceAll(":null", ":\"-\"").replaceAll(":\"\"", ":\"-\"");  // Replacing null/empty values with "-"
	        } catch (JsonProcessingException e) {
	            e.printStackTrace();
	        }
	        empTicketByStatus = "{ \"empTicketByStatus\":" + offerdetailsjson + "}";
	    } else {
	        empTicketByStatus = "No ticket data Available";
	    }
	    return empTicketByStatus;
	}

	/*@Override
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
	}*/


	public Employee_Ticketing_System_Ticket_Types getTicketType(String Tickettype , String query, Session session) {
		String ticketTypeQuery = "SELECT * FROM Employee_Ticketing_System_Ticket_Types WHERE ticket_query = '" + query + "' AND ticket_type = '" + Tickettype + "'";

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

	/*@Override
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
	}*/
/*	@Override
	public ResponseEntity<byte[]> downloadFile(String ticketid) {
	    Session session = null;
	    byte[] files = null;
	    String check = Utils.awsCheckFlag();
	    String fileName = null;
	    HttpHeaders headers = new HttpHeaders();

	    try {
	        session = sessionFactory.openSession();
	        String url = null;

	        // Query to fetch the ticketing files
	        String query = "SELECT * FROM EmployeeTicketingFiles WHERE ticket_number =:ticketid";
	        List<EmployeeTicketingFiles> empticketlist = session.createSQLQuery(query)
	                .addEntity(EmployeeTicketingFiles.class)
	                .setParameter("ticketid", ticketid)
	                .list();

	        if (!empticketlist.isEmpty()) {
	            for (EmployeeTicketingFiles emp : empticketlist) {
	                String filePath = emp.getTicket_path();
	                // Create a unique file name for the download
	                fileName = emp.getTicket_number() + "_" + emp.getEmployee_id() + "_" + emp.getCreated_date();

	                headers.add("Employee-Id", emp.getEmployee_id());
	                headers.add("Create-Date", String.valueOf(emp.getCreated_date()));

	                if (filePath != null && !filePath.isEmpty() && "yes".equalsIgnoreCase(check)) {
	                    url = "ticketing_system" + "/" + filePath;
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

	        // Set Content-Disposition manually for file download
	        headers.set("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
	        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

	    } catch (Throwable e) {
	        logger.error("failed to download file - " + ticketid, e.getMessage());
	        return new ResponseEntity<byte[]>(HttpStatus.INTERNAL_SERVER_ERROR);
	    } finally {
	        if (session != null) {
	            session.close();
	        }
	    }

	    return new ResponseEntity<>(files, headers, HttpStatus.OK);
	}*/
	/*@Override
	public ResponseEntity<byte[]> downloadFile(String ticketid) {
	    Session session = null;
	    byte[] zipBytes = null;
	    String check = Utils.awsCheckFlag();
	    String fileName = null;
	    HttpHeaders headers = new HttpHeaders();
	    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
	    ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);

	    try {
	        session = sessionFactory.openSession();
	        String url = null;

	        // Query to fetch the ticketing files
	        String query = "SELECT * FROM EmployeeTicketingFiles WHERE ticket_number =:ticketid";
	        List<EmployeeTicketingFiles> empticketlist = session.createSQLQuery(query)
	                .addEntity(EmployeeTicketingFiles.class)
	                .setParameter("ticketid", ticketid)
	                .list();

	        if (!empticketlist.isEmpty()) {
	            for (EmployeeTicketingFiles emp : empticketlist) {
	                String filePath = emp.getTicket_path();
	               // fileName = emp.getTicket_number() + "_" + emp.getEmployee_id() + "_" + emp.getCreated_date();
	                fileName = emp.getEmployee_id() + "_" + emp.getCreated_date() + "_" + emp.getTicket_path();

	                headers.add("Employee-Id", emp.getEmployee_id());
	                headers.add("Create-Date", String.valueOf(emp.getCreated_date()));

	                if (filePath != null && !filePath.isEmpty() && "yes".equalsIgnoreCase(check)) {
	                    url = "ticketing_system" + "/" + filePath;
	                    try {
	                        byte[] fileData = Utils.downloadFileByAWSS3Bucket(url);
	                        
	                        // Add file to the zip output stream
	                        ZipEntry zipEntry = new ZipEntry(fileName);
	                        zipOutputStream.putNextEntry(zipEntry);
	                        zipOutputStream.write(fileData);
	                        zipOutputStream.closeEntry();
	                    } catch (Exception e) {
	                        return new ResponseEntity<byte[]>(HttpStatus.NOT_FOUND);
	                    }
	                } else {
	                    return new ResponseEntity<byte[]>(HttpStatus.NO_CONTENT);
	                }
	            }
	        }

	        // Finish writing the zip file
	        zipOutputStream.finish();

	        // Set Content-Disposition manually for the zip download
	        headers.set("Content-Disposition", "attachment; filename=\"" + ticketid + "_files.zip\"");
	        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

	        // Set the bytes of the zip file
	        zipBytes = byteArrayOutputStream.toByteArray();

	    } catch (Throwable e) {
	        logger.error("Failed to download files for ticket - " + ticketid, e.getMessage());
	        return new ResponseEntity<byte[]>(HttpStatus.INTERNAL_SERVER_ERROR);
	    } finally {
	        if (session != null) {
	            session.close();
	        }
	        try {
	            zipOutputStream.close();
	        } catch (IOException e) {
	            logger.error("Error closing zip stream", e);
	        }
	    }

	    return new ResponseEntity<>(zipBytes, headers, HttpStatus.OK);
	}*/
	
	
	/**
	 * This functionality is used to download ticket-related file based on the ticket ID. 
	 * The file is retrieved from S3 as byte content and returned to the frontend in the same format.
	 */
	
	@Override
	public ResponseEntity<byte[]> downloadFile(String ticketid) {
		Session session = null;
		byte[] files = null;
		String check = Utils.awsCheckFlag();
		try {
			session = sessionFactory.openSession();
			LocalDate now = LocalDate.now();
			String url = null;

			String query = "SELECT * FROM EmployeeTicketingFiles WHERE ticket_path =:ticketid  ";
			List<EmployeeTicketingFiles> empticketlist = session.createSQLQuery(query).addEntity(EmployeeTicketingFiles.class)
					.setParameter("ticketid", ticketid).list();

			if (!empticketlist.isEmpty() && empticketlist != null) {
				for (EmployeeTicketingFiles emp : empticketlist) {
					String filePath = emp.getTicket_path();

					if (filePath != null || !filePath.isEmpty() && "yes".equalsIgnoreCase(check)) {

						url = "ticketing_system" + "/" + filePath;
					//	ticketing_system/ETS230_code Review Form for CTCBreakupDetails123.xlsx
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
	//secondne 
	/*@Override
	public ResponseEntity<byte[]> downloadFile(String ticketid) {
	    Session session = null;
	    byte[] zipBytes = null;
	    String check = Utils.awsCheckFlag();
	    String fileName = null;
	    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
	    ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);

	    try {
	        session = sessionFactory.openSession();
	        String url = null;

	        // Query to fetch the ticketing files
	        String query = "SELECT * FROM EmployeeTicketingFiles WHERE ticket_number =:ticketid";
	        List<EmployeeTicketingFiles> empticketlist = session.createSQLQuery(query)
	                .addEntity(EmployeeTicketingFiles.class)
	                .setParameter("ticketid", ticketid)
	                .list();

	        if (!empticketlist.isEmpty()) {
	            for (EmployeeTicketingFiles emp : empticketlist) {
	                String filePath = emp.getTicket_path();
	                fileName = emp.getEmployee_id() + "_" + emp.getCreated_date() + "_" + emp.getTicket_path();

	                if (filePath != null && !filePath.isEmpty() && "yes".equalsIgnoreCase(check)) {
	                    url = "ticketing_system" + "/" + filePath;
	                    try {
	                        byte[] fileData = Utils.downloadFileByAWSS3Bucket(url);
	                        
	                        if (fileData == null || fileData.length == 0) {
	                            logger.error("No file data for URL: " + url);
	                            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	                        }

	                        // Add file to zip
	                        ZipEntry zipEntry = new ZipEntry(fileName);
	                        zipOutputStream.putNextEntry(zipEntry);
	                        zipOutputStream.write(fileData);
	                        zipOutputStream.closeEntry();
	                    } catch (Exception e) {
	                        logger.error("Error downloading or adding file: " + filePath, e);
	                        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	                    }
	                } else {
	                    logger.warn("File path is empty or not found for ticket: " + ticketid);
	                    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	                }
	            }
	        }

	        // Finish writing the zip file
	        zipOutputStream.finish();

	        // Get the bytes of the zip file
	        zipBytes = byteArrayOutputStream.toByteArray();

	    } catch (Throwable e) {
	        logger.error("Failed to download files for ticket - " + ticketid, e.getMessage());
	        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	    } finally {
	        if (session != null) {
	            session.close();
	        }
	        try {
	            zipOutputStream.close();
	        } catch (IOException e) {
	            logger.error("Error closing zip stream", e);
	        }
	    }

	    // Return the zip file as response without any HttpHeaders
	    return new ResponseEntity<>(zipBytes, HttpStatus.OK);
	}*/


//original file
	
	
	/*@Override
	public ResponseEntity<byte[]> downloadFile(String ticketid) {
		Session session = null;
		byte[] files = null;
		String check = Utils.awsCheckFlag();
		try {
			session = sessionFactory.openSession();
			LocalDate now = LocalDate.now();
			String url = null;

			String query = "SELECT * FROM EmployeeTicketingFiles WHERE ticket_number =:ticketid";
			List<EmployeeTicketingFiles> empticketlist = session.createSQLQuery(query).addEntity(EmployeeTicketingFiles.class)
					.setParameter("ticketid", ticketid).list();

			if (!empticketlist.isEmpty() && empticketlist != null) {
				for (EmployeeTicketingFiles emp : empticketlist) {
					String filePath = emp.getTicket_path();

					if (filePath != null || !filePath.isEmpty() && "yes".equalsIgnoreCase(check)) {

						url = "ticketing_system" + "/" + filePath;
					//	ticketing_system/ETS230_code Review Form for CTCBreakupDetails123.xlsx
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
*/

	/**
	 * Retrieves the comment history of an employee from the Employee_Ticketing_System_AUD entity.
	 * The method uses the provided empID and ticketNumber parameters to filter and fetch the relevant comment history.
	 * The results are returned in a structured JSON format, which includes the list of comments related to the specified employee and ticket.
	 */
	
	@Override
	public EmployeeCommentsList getEmployeeCommentsList(String empID, String ticketNumber) {
		Session session = null;

		EmployeeCommentsList commentsList = new EmployeeCommentsList();

		List<EmployeeTicketingSystemCommentsList> personalcomments = null;
		List<EmployeeTicketingSystemCommentsList> MainTablecomments = null;

		try {
			session = sessionFactory.openSession();
			/*
			 * String query =
			 * "SELECT a.comments,a.last_modified_by,a.last_modified_date FROM `Employee_Ticketing_System_AUD` a WHERE a.employee_id ='"
			 * + empID + "' \r\n" +
			 * " AND a.comments_MOD ='1' ORDER BY REV DESC";
			 */
			String query = "SELECT a.comments, a.last_modified_by, a.last_modified_date "
					+ "FROM Employee_Ticketing_System_AUD a " + "WHERE a.employee_id = '" + empID + "' "
					+ "AND a.ticket_number = '" + ticketNumber + "' " + "AND a.comments_MOD = '1' "
					+ "ORDER BY a.REV DESC";

			personalcomments = session.createSQLQuery(query)
					.setResultTransformer(Transformers.aliasToBean(EmployeeTicketingSystemCommentsList.class)).list();

			String filequery = "SELECT * FROM EmployeeTicketingFiles WHERE ticket_number = '" + ticketNumber
					+ "' AND employee_id = '" + empID + "'";
			List<EmployeeTicketingFiles> list = session.createSQLQuery(filequery)
					.addEntity(EmployeeTicketingFiles.class).list();
			if (list != null && !list.isEmpty()) {
				commentsList.setEmployeeTicketingFiles(list);
			}
			//change
			if(personalcomments.size()==0){
				String MainComments = "SELECT  a.comments, a.last_modified_by, a.last_modified_date FROM Employee_Ticketing_System a WHERE a.employee_id = '" + empID + "' AND a.ticket_number = '" + ticketNumber + "'";
				MainTablecomments =  session.createSQLQuery(MainComments)
						.setResultTransformer(Transformers.aliasToBean(EmployeeTicketingSystemCommentsList.class)).list();
			}

			if (personalcomments != null && !personalcomments.isEmpty()) {
				commentsList.setEmployeeTicketingSystemComments(personalcomments);
			}
			else {
				commentsList.setEmployeeTicketingSystemComments(MainTablecomments);
			}

			//commentsList.setEmployeeTicketingSystemComments(personalcomments);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			session.close();
		}
		return commentsList;
	}

	
	/**
	 * This code retrieves all ticket filenames for a specific ticket ID from the `EmployeeTicketingFiles` Entity.
	 * It fetches the ticket path, created date, and employee name, formats them, and returns the results in a structured JSON format.
	 */
	@Override
	public String getallticketfilenames(String ticketId) {
	    Session session = null;
	    List<String> resultList = new ArrayList<>();
	    try {
	        session = sessionFactory.openSession();
	        String query = "SELECT ticket_path, created_date, employee_id FROM EmployeeTicketingFiles WHERE ticket_number = :ticketId";
	        List<Object[]> result = session.createSQLQuery(query)
	                .setParameter("ticketId", ticketId)
	                .list();

	        // Iterate through the query result and format the output
	        for (Object[] obj : result) {
	            String ticketPath = (String) obj[0];
	            Timestamp createdDate = (Timestamp) obj[1];
	            String employeeName = (String) obj[2];

	            // Format the date to yyyy-MM-dd
	            String formattedDate = new SimpleDateFormat("yyyy-MM-dd").format(createdDate);

	            // Remove the prefix before the first underscore, if it exists
	            if (ticketPath != null && ticketPath.contains("_")) {
	                ticketPath = ticketPath.substring(ticketPath.indexOf("_") + 1);
	            }

	            // Format the output as required
	            String formattedOutput = "<" + employeeName + ">" + "<" + formattedDate + ">" + "<" + ticketPath + ">";
	            resultList.add(formattedOutput);
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        if (session != null) {
	            session.close();
	        }
	    }

	    // Use Jackson ObjectMapper to convert the list to a JSON string
	    try {
	        ObjectMapper objectMapper = new ObjectMapper();
	        return objectMapper.writeValueAsString(resultList);  // Convert list to JSON
	    } catch (Exception e) {
	        e.printStackTrace();
	        return "Error converting list to JSON";  // Handle errors in conversion
	    }
	}


	/*@Override
	public ResponseEntity<byte[]> downloadFile(String ticketid) {
	    byte[] files = null;
	    String check = Utils.awsCheckFlag();
	    String folder = "ticketing_system"; // Folder where files are stored
	    String fileName = "code Review Form for CTCBreakupDetails123.xlsx"; // Hardcoded original file name
	    String fullPath = folder + "/" + ticketid + "_" + fileName; // Full path e.g., "ticketing_system/ETS202_ABC.xl"

	    try {
	        // Check if AWS download flag is enabled
	        if ("yes".equalsIgnoreCase(check)) {
	            try {
	                // Attempt to download the file from AWS S3 bucket
	                files = Utils.downloadFileByAWSS3Bucket(fullPath);
	                if (files != null) {
	                    // Successfully downloaded
	                    return new ResponseEntity<>(files, HttpStatus.OK);
	                }
	            } catch (Exception e) {
	                // Log and return NOT_FOUND if the file could not be downloaded
	                logger.error("File not found in AWS S3 bucket: " + fullPath, e);
	                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	            }
	        } else {
	            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	        }
	    } catch (Throwable e) {
	        logger.error("Failed to download file - " + ticketid, e);
	        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	    
	    // Default response if the file was not found or could not be downloaded
	    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}*/

}
