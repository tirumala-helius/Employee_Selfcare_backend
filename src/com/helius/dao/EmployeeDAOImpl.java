/**
 * 
 */
package com.helius.dao;
import static java.time.temporal.TemporalAdjusters.lastDayOfYear;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.mail.MessagingException;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.formula.functions.Now;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.transform.Transformers;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.helius.entities.Client_Leave_Policy;
import com.helius.entities.Contact_Address_Details;
import com.helius.entities.DeleteIndianEmployeeFamilyMember;
import com.helius.entities.DeleteSingaporeEmployeeFamilyMember;
import com.helius.entities.EmailScreen;
import com.helius.entities.Emergency_Contact_Details;
import com.helius.entities.Employee;
import com.helius.entities.Employee_Appraisal_Details;
import com.helius.entities.Employee_Assignment_Details;
import com.helius.entities.Employee_Bank_Details;
import com.helius.entities.Employee_Identification_Details;
import com.helius.entities.Employee_Leave_Data;
import com.helius.entities.Employee_Leaves_Eligibility;
import com.helius.entities.Employee_Off_In_Lieu;
import com.helius.entities.Employee_Offer_Details;
import com.helius.entities.Employee_Personal_Details;
import com.helius.entities.Employee_Salary_Details;
import com.helius.entities.Employee_Terms_And_Conditions;
import com.helius.entities.Employee_Ticketing_System_Ticket_Types;
import com.helius.entities.Employee_Timesheet_Status;
import com.helius.entities.Employee_Work_Permit_Details;
import com.helius.entities.Help_Videos;
import com.helius.entities.Indian_Employee_Family_Member;
import com.helius.entities.Indian_Employees_Insurance_Details;
import com.helius.entities.LeaveUtilization;
import com.helius.entities.Leave_Eligibility_Details;
import com.helius.entities.Leave_Record_Details;
import com.helius.entities.Leave_Usage_Details;
import com.helius.entities.Leaves_Eligibility_defined_By_Client_Policy;
import com.helius.entities.Singapore_Employee_Family_Member;
import com.helius.entities.Singapore_Employee_Insurance_Details;
import com.helius.entities.Sow_Ctc_Breakup;
import com.helius.entities.Sow_Details;
import com.helius.entities.Sow_Employee_Association;
import com.helius.entities.Timesheet_Email;
import com.helius.entities.Work_Permit_Master;
import com.helius.service.EmailService;
import com.helius.service.UserServiceImpl;
import com.helius.utils.Employee_Off_In_Lieu_Data;
import com.helius.utils.FilecopyStatus;
import com.helius.utils.Holiday_Master;
import com.helius.utils.TimesheetAutomationHolidays;
import com.helius.utils.Utils;
/**
 * @author Tirumala 22-Feb-2018
 */
public class EmployeeDAOImpl implements IEmployeeDAO {

	private final String hql1 = "delete from Employee_Personal_Details where employee_id = :employee_id";
	private final String hql2 = "delete from Employee_Salary_Details where employee_id = :employee_id";
	private final String hql3 = "delete from Employee_Assignments where employee_id = :employee_id";
	private final String hql4 = "delete from Employee_Professional_Details where employee_id = :employee_id";
	private final String hql5 = "delete from Employee_Insurance_Details where employee_id = :employee_id";

	/**
	 * @return the sessionFactory
	 */
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

	/**
	 * 
	 */
	public EmployeeDAOImpl() {
		// TODO Auto-generated constructor stub
	}
	@Autowired
	ApplicationContext context;
	@Autowired
	private EmailService emailService;
	private List<String> copied_with_success = new ArrayList<String>();
    private static final Logger logger = LogManager.getLogger(EmployeeDAOImpl.class.getName());
    
	/** returns employee details **/
	@SuppressWarnings("unchecked")
	@Override
	public Employee get(String employeeid) {
		Employee emp = new Employee();
		Integer client_id = 0;
		Session session = null;
		try {
			session = sessionFactory.openSession();
			// Employee personal Details
			Employee_Personal_Details employee_Personal_Details = (Employee_Personal_Details) session
					.get(Employee_Personal_Details.class, employeeid);
			if (employee_Personal_Details == null) {
				logger.error("employee id not found in database for employee "+employeeid);
				return null;
			} else {
				emp.setEmployeePersonalDetails(employee_Personal_Details);
			}
			// Salary details
			String employee_salary_query = "select salary.* from Employee_Salary_Details salary where employee_id = :employee_id ";
			java.util.List salaryList = session.createSQLQuery(employee_salary_query)
					.addEntity(Employee_Salary_Details.class).setParameter("employee_id", employeeid).list();
			Employee_Salary_Details employee_Salary_Details = null;
			if (!salaryList.isEmpty()) {
				employee_Salary_Details = (Employee_Salary_Details) salaryList.iterator().next();
			}
			if (employee_Salary_Details != null) {
				emp.setEmployeeSalaryDetails(employee_Salary_Details);
			}

			// Assignment Details
			
			String employee_assignment_query = "select assignments.* from Employee_Assignment_Details assignments where employee_id = :employee_id ";
			java.util.List assignmentList = session.createSQLQuery(employee_assignment_query)
					.addEntity(Employee_Assignment_Details.class).setParameter("employee_id", employeeid).list();
			Employee_Assignment_Details employee_Assigmnent_Details = null;
			if (!assignmentList.isEmpty()) {
				employee_Assigmnent_Details = (Employee_Assignment_Details) assignmentList.iterator().next();
				List<Sow_Details> sow_details_list =null;
				String sowquery = "select * from Sow_Employee_Association a, Sow_Details b where a.employee_id=:employee_id"
						+ " and a.sow_details_id=b.sow_details_id and a.status='active' and  b.sow_status='active' ";
				sow_details_list = session.createSQLQuery(sowquery).addEntity(Sow_Details.class)
						.setParameter("employee_id", employeeid).list();
				if(sow_details_list != null && !sow_details_list.isEmpty()){
					
					Sow_Details sowdetails =  sow_details_list.iterator().next();
					employee_Assigmnent_Details.setSow_start_date(sowdetails.getSowStartDate());
					employee_Assigmnent_Details.setSow_expiry_date(sowdetails.getSowExpiryDate());
				}
			}
			
			if (employee_Assigmnent_Details != null) {
				emp.setEmployeeAssignmentDetails(employee_Assigmnent_Details);
			}

			// Employee bank details
			String bankdetails_query = "select bankdetails.* from Employee_Bank_Details bankdetails where employee_id = :employee_id ";
			java.util.List bankList = session.createSQLQuery(bankdetails_query).addEntity(Employee_Bank_Details.class)
					.setParameter("employee_id", employeeid).list();
			Employee_Bank_Details employee_Bank_Details = null;
			if (!bankList.isEmpty()) {
				employee_Bank_Details = (Employee_Bank_Details) bankList.iterator().next();
			}
			if (employee_Bank_Details != null) {
				emp.setEmployeeBankDetails(employee_Bank_Details);
			}

			// Employee appraisal details
			  String appraisal_query = "select appraisal.* from Employee_Appraisal_Details appraisal where employee_id = :employee_id "; 
			  java.util.List appraisalList = session.createSQLQuery(appraisal_query).addEntity(Employee_Appraisal_Details.class).setParameter("employee_id", employeeid).list();
			  Employee_Appraisal_Details employee_Appraisal_Details = null; 
			  if(!appraisalList.isEmpty()) {
			  employee_Appraisal_Details = (Employee_Appraisal_Details) appraisalList.iterator().next();
			  }
			  if(employee_Appraisal_Details!= null) {
			  emp.setEmployeeAppraisalDetails(employee_Appraisal_Details); 
			  }
			  
			  String sowbreakup_query = "select breakup.* from Sow_Ctc_Breakup breakup where employee_id = :employee_id AND status = :status"; 
			  java.util.List breakupList = session.createSQLQuery(sowbreakup_query).addEntity(Sow_Ctc_Breakup.class).setParameter("employee_id", employeeid).setParameter("status", "active").list();
			  Sow_Ctc_Breakup sowCtcBreakup = null; 
				List<Sow_Ctc_Breakup> sow_Ctc_BreakupList = new ArrayList<Sow_Ctc_Breakup>();
			/*	Collections.sort(breakupList,new Comparator<Sow_Ctc_Breakup>() {
					@Override
					public int compare(Sow_Ctc_Breakup o1, Sow_Ctc_Breakup o2) {
						// TODO Auto-generated method stub
						//return o1.getGroupId().compareTo(o2.getGroupId());  
						return Integer.compare(o1.getGroupId(),o2.getGroupId());  
					}
				}); */
			  if(!breakupList.isEmpty()) {
				  for (Object sowbreakup : breakupList) {
					  sowCtcBreakup = (Sow_Ctc_Breakup) sowbreakup;
					  sow_Ctc_BreakupList.add(sowCtcBreakup);
					}			  }
			  if(sow_Ctc_BreakupList!= null) {
			  emp.setSowCtcBreakup(sow_Ctc_BreakupList); 
			  }
			 
			  
			  String sowbreakuphis_query = "select breakup.* from Sow_Ctc_Breakup breakup where employee_id = :employee_id AND status = :status"; 
			  java.util.List breakuphisList = session.createSQLQuery(sowbreakuphis_query).addEntity(Sow_Ctc_Breakup.class).setParameter("employee_id", employeeid).setParameter("status", "inactive").list();
			  Sow_Ctc_Breakup sowCtcBreakupHis = null; 
			//	List<Object> sow_Ctc_BreakupHisList = new ArrayList<Object>();
				HashMap<String, List<Sow_Ctc_Breakup>> map = new HashMap<String, List<Sow_Ctc_Breakup>>();
				List<Sow_Ctc_Breakup> grp = new ArrayList<Sow_Ctc_Breakup>();
			  if(!breakuphisList.isEmpty()) {
				  for (Object sowbreakuphislst : breakuphisList) {
					  sowCtcBreakupHis = (Sow_Ctc_Breakup) sowbreakuphislst;
					  String key = Integer.toString(sowCtcBreakupHis.getGroupId());
					if(map.containsKey(key)){
						 grp =  map.get(key);
						 grp.add(sowCtcBreakupHis);
					}else{
						 grp = new ArrayList<Sow_Ctc_Breakup>();
						 grp.add(sowCtcBreakupHis);
						 String key1 = Integer.toString(sowCtcBreakupHis.getGroupId());
						 map.put(key1,grp);
					}
					}			  }
			  if(!map.isEmpty()) {
			  emp.setSowCtcBreakupHistory(map.values()); 
			  }
			 
			  String sowEmpAssoc_query = "select empAssoc.* from Sow_Employee_Association empAssoc where employee_id = :employee_id"; 
			  java.util.List empAssocList = session.createSQLQuery(sowEmpAssoc_query).addEntity(Sow_Employee_Association.class).setParameter("employee_id", employeeid).list();
			  Sow_Employee_Association sowAssoc = null; 
				List<Sow_Employee_Association> sow_EmpAssoscList = new ArrayList<Sow_Employee_Association>();
			  if(!empAssocList.isEmpty()) {
				  for (Object empassoc : empAssocList) {
					  sowAssoc = (Sow_Employee_Association) empassoc;
					  sow_EmpAssoscList.add(sowAssoc);
					}			  }
			  if(sow_EmpAssoscList!= null) {
			  emp.setSowEmployeeAssoc(sow_EmpAssoscList); 
			  }	  
			// Employee leave eligibility
			LocalDate date = LocalDate.now();
			int year = date.getYear();
			String leaveeligibility_query = "select leaveeligible.* from Leave_Eligibility_Details leaveeligible where employee_id = :employee_id AND year = :year";
			java.util.List leaveeligibilityList = session.createSQLQuery(leaveeligibility_query)
					.addEntity(Leave_Eligibility_Details.class).setParameter("employee_id", employeeid).setParameter("year",year).list();
			Leave_Eligibility_Details leave_Eligibility_Details = null;
			List<Leave_Eligibility_Details> leave_Eligibility_DetailsList = new ArrayList<Leave_Eligibility_Details>();
			if (!leaveeligibilityList.isEmpty()) {
				for (Object leave_Eligibility : leaveeligibilityList) {
					leave_Eligibility_Details = (Leave_Eligibility_Details) leave_Eligibility;
					client_id =  leave_Eligibility_Details.getClient_id();
					leave_Eligibility_DetailsList.add(leave_Eligibility_Details);
				}
			}
			if (leave_Eligibility_DetailsList != null) {
				emp.setLeavesEligibility(leave_Eligibility_DetailsList);
			}
			// Employee offer details
			String employee_offer_query = "select offer.* from Employee_Offer_Details offer where employee_id = :employee_id ";
			java.util.List offerList = session.createSQLQuery(employee_offer_query)
					.addEntity(Employee_Offer_Details.class).setParameter("employee_id", employeeid).list();
			Employee_Offer_Details employee_offer_Details = null;
			if (!offerList.isEmpty()) {
				employee_offer_Details = (Employee_Offer_Details) offerList.iterator().next();
			}
			if (employee_offer_Details != null) {
				emp.setEmployeeOfferDetails(employee_offer_Details);
			}
			// Employee Terms and Conditions
			String employee_TandC_query = "select TandC.* from Employee_Terms_And_Conditions TandC where employee_id = :employee_id ";
			java.util.List TandCList = session.createSQLQuery(employee_TandC_query)
					.addEntity(Employee_Terms_And_Conditions.class).setParameter("employee_id", employeeid).list();
			Employee_Terms_And_Conditions Employee_Terms_And_Conditions = null;
			if (!TandCList.isEmpty()) {
				Employee_Terms_And_Conditions = (Employee_Terms_And_Conditions) TandCList.iterator().next();
			}
			if (Employee_Terms_And_Conditions != null) {
				emp.setEmployeeTermsAndConditions(Employee_Terms_And_Conditions);
			}

			// Employee Singapore insurance details
			if (emp.getEmployeeOfferDetails() != null) {
				if (emp.getEmployeeOfferDetails().getWork_country().equals("Singapore")) {
					String employee_SG_insurance_query = "select insurance.* from Singapore_Employee_Insurance_Details insurance where employee_id = :employee_id ";
					java.util.List sgInsuranceList = session.createSQLQuery(employee_SG_insurance_query)
							.addEntity(Singapore_Employee_Insurance_Details.class)
							.setParameter("employee_id", employeeid).list();
					Singapore_Employee_Insurance_Details Singapore_Insurance_Details = null;
					if (!sgInsuranceList.isEmpty()) {
						Singapore_Insurance_Details = (Singapore_Employee_Insurance_Details) sgInsuranceList.iterator()
								.next();
					}
					if (Singapore_Insurance_Details != null) {
						String sg_members_query = "select SGMembers.* from Singapore_Employee_Family_Member SGMembers where employee_id = :employee_id ";
						java.util.List memberlist = session.createSQLQuery(sg_members_query)
								.addEntity(Singapore_Employee_Family_Member.class)
								.setParameter("employee_id", employeeid).list();
						Singapore_Employee_Family_Member singaporemember = null;
						List<Singapore_Employee_Family_Member> singaporememberList = new ArrayList<Singapore_Employee_Family_Member>();
						for (Object singaporemembers : memberlist) {
							singaporemember = (Singapore_Employee_Family_Member) singaporemembers;
							singaporememberList.add(singaporemember);
						}
						Singapore_Insurance_Details.setSingaporeEmployeeFamilyMember(singaporememberList);
						emp.setSingaporeEmployeeInsuranceDetails(Singapore_Insurance_Details);
					}
				}
			}

			// Employee india insurance details
			if (emp.getEmployeeOfferDetails() != null) {
				if (emp.getEmployeeOfferDetails().getWork_country().equals("India"))
					;
				{
					String employee_IN_insurance_query = "select insurance.* from Indian_Employees_Insurance_Details insurance where employee_id = :employee_id ";
					java.util.List inInsuranceList = session.createSQLQuery(employee_IN_insurance_query)
							.addEntity(Indian_Employees_Insurance_Details.class).setParameter("employee_id", employeeid)
							.list();
					Indian_Employees_Insurance_Details indian_Insurance_Details = null;
					if (!inInsuranceList.isEmpty()) {
						indian_Insurance_Details = (Indian_Employees_Insurance_Details) inInsuranceList.iterator()
								.next();
					}

					if (indian_Insurance_Details != null) {
						String in_members_query = "select inMembers.* from Indian_Employee_Family_Member inMembers where employee_id = :employee_id ";
						java.util.List memberlist = session.createSQLQuery(in_members_query)
								.addEntity(Indian_Employee_Family_Member.class).setParameter("employee_id", employeeid)
								.list();
						Indian_Employee_Family_Member indianmember = null;
						List<Indian_Employee_Family_Member> indianmemberList = new ArrayList<Indian_Employee_Family_Member>();
						for (Object indianmembers : memberlist) {
							indianmember = (Indian_Employee_Family_Member) indianmembers;
							indianmemberList.add(indianmember);
						}
						indian_Insurance_Details.setIndianEmployeeFamilyMember(indianmemberList);
						emp.setIndianEmployeesInsuranceDetails(indian_Insurance_Details);
					}
				}
			}

			// Employee identification details
			String employee_identification_details_query = "select identificationdetails.* from Employee_Identification_Details identificationdetails where employee_id = :employee_id ";
			java.util.List identificationDetails_List = session.createSQLQuery(employee_identification_details_query)
					.addEntity(Employee_Identification_Details.class).setParameter("employee_id", employeeid).list();
			Employee_Identification_Details employee_Identification_Details = null;
			List<Employee_Identification_Details> employee_Identification_DetailsList = new ArrayList<Employee_Identification_Details>();
			for (Object employee_Identification : identificationDetails_List) {
				employee_Identification_Details = (Employee_Identification_Details) employee_Identification;
				employee_Identification_DetailsList.add(employee_Identification_Details);
			}
			emp.setEmployeeIdentificationDetails(employee_Identification_DetailsList);

			// Employee Contact address details
			String employee_contact_addressdetails_query = "select contactaddressdetails.* from Contact_Address_Details contactaddressdetails where employee_id = :employee_id ";
			java.util.List contactaddress_List = session.createSQLQuery(employee_contact_addressdetails_query)
					.addEntity(Contact_Address_Details.class).setParameter("employee_id", employeeid).list();
			Contact_Address_Details employee_Contact_Address_Details = null;
			List<Contact_Address_Details> employee_Contact_Address_DetailsList = new ArrayList<Contact_Address_Details>();
			if (!contactaddress_List.isEmpty()) {
				for (Object employee_ContactAddress : contactaddress_List) {
					employee_Contact_Address_Details = (Contact_Address_Details) employee_ContactAddress;
					employee_Contact_Address_DetailsList.add(employee_Contact_Address_Details);
				}
			}
			if (employee_Contact_Address_DetailsList != null) {
				emp.setEmployeeContactAddressDetails(employee_Contact_Address_DetailsList);
			}

			// Emergency Details
			String employee_emergency_query = "select emergency.* from Emergency_Contact_Details emergency where employee_id = :employee_id ";
			java.util.List emergencyList = session.createSQLQuery(employee_emergency_query)
					.addEntity(Emergency_Contact_Details.class).setParameter("employee_id", employeeid).list();
			Emergency_Contact_Details employee_Emergency_Details = null;
			if (!emergencyList.isEmpty()) {
				employee_Emergency_Details = (Emergency_Contact_Details) emergencyList.iterator().next();
			}
			if (employee_Emergency_Details != null) {
				emp.setEmergencyContactDetails(employee_Emergency_Details);
			}
			
			// Employee work permit details
			String employee_work_permit_details_query = "select workpermitdetails.* from Employee_Work_Permit_Details workpermitdetails where employee_id = :employee_id ";
			java.util.List workpermitDetails_List = session.createSQLQuery(employee_work_permit_details_query)
					.addEntity(Employee_Work_Permit_Details.class).setParameter("employee_id", employeeid).list();
			Employee_Work_Permit_Details employee_Work_Permit_Details = null;
			if (!workpermitDetails_List.isEmpty()) {
				employee_Work_Permit_Details = (Employee_Work_Permit_Details) workpermitDetails_List.iterator().next();
			}
			if (employee_Work_Permit_Details != null) {
				String wpNum = "";
				String workpermit = employee_Work_Permit_Details.getWork_permit_number();
				if (workpermit != null && !workpermit.isEmpty() && workpermit.contains(",")) {
					if (!",".equalsIgnoreCase(workpermit)) {
						String[] regx = workpermit.split(",");
						if (!"".equalsIgnoreCase(regx[0]) && !"-".equalsIgnoreCase(regx[0])
								&& !"undefined".equalsIgnoreCase(regx[0])) {
							wpNum = regx[0].replaceAll("\\w(?=\\w{4})", "*");
						}
						if (regx.length == 2) { // to handle arrayoutofbound
												// exception
							if (!"".equalsIgnoreCase(regx[1]) && !"-".equalsIgnoreCase(regx[1])
									&& !"undefined".equalsIgnoreCase(regx[1])) {
								String wpassNum = regx[1].replaceAll("\\w(?=\\w{4})", "*");
								wpNum = wpNum + "," + wpassNum;
							}
						}
					}
				} else {
					if (workpermit != null && !workpermit.isEmpty() && !"-".equalsIgnoreCase(workpermit)
							&& !"undefined".equalsIgnoreCase(workpermit)) {
						wpNum = workpermit.replaceAll("\\w(?=\\w{4})", "*");
					}
				}
				employee_Work_Permit_Details.setWork_permit_number(wpNum);
				String passportNum = "";
				String passport = employee_Work_Permit_Details.getPassport_number();
				if(passport != null && !passport.isEmpty()){
					passportNum = passport.replaceAll("\\w(?=\\w{4})", "*");
				}
				employee_Work_Permit_Details.setPassport_number(passportNum);
				emp.setEmployeeWorkPermitDetails(employee_Work_Permit_Details);
			}
			
			/*// get ticketingSystem types
			String ticketTypeQuery = "SELECT * FROM Employee_Ticketing_System_Ticket_Types";
			List<Employee_Ticketing_System_Ticket_Types> empTicketTypeList = new ArrayList<>();
			List<Employee_Ticketing_System_Ticket_Types> ticketTypeList = session.createSQLQuery(ticketTypeQuery)
					.addEntity(Employee_Ticketing_System_Ticket_Types.class).list();
			if (!ticketTypeList.isEmpty()) {
				ticketTypeList.stream().forEach(ticket -> {
					empTicketTypeList.add(ticket);
				});
				emp.setEmployeeTicketTypes(empTicketTypeList);
			}*/
			String ticketTypeQuery = "SELECT * FROM Employee_Ticketing_System_Ticket_Types";
			List<Employee_Ticketing_System_Ticket_Types> empTicketTypeList = new ArrayList<>();
			List<Employee_Ticketing_System_Ticket_Types> ticketTypeList = session.createSQLQuery(ticketTypeQuery)
			        .addEntity(Employee_Ticketing_System_Ticket_Types.class).list();
			System.out.println(ticketTypeList);
			if (!ticketTypeList.isEmpty()) {
			    for (Employee_Ticketing_System_Ticket_Types ticket : ticketTypeList) {
			        empTicketTypeList.add(ticket);
			    }
			    emp.setEmployeeTicketTypes(empTicketTypeList);
			}

			
//Employe_Tickt_type json

	        // Define your SQL query
	        String ticketTypeQuery1 = "SELECT ticket_type, ticket_query FROM Employee_Ticketing_System_Ticket_Types";
	        List<Object[]> ticketTypeList1 = session.createSQLQuery(ticketTypeQuery1).list();

	        // Create a map to store ticket_type as keys and lists of ticket_query as values
	        Map<String, List<String>> ticketTypeMap = new HashMap<>();

	        // Configure Jackson ObjectMapper
	        ObjectMapper objectMapper = new ObjectMapper();

	        // Iterate through the query result and populate the map
	        for (Object[] row : ticketTypeList1) {
	            String ticketType = (String) row[0];
	            String ticketQuery = (String) row[1];

	            // If the ticket type is already in the map, add the ticket query to the existing list
	            if (ticketTypeMap.containsKey(ticketType)) {
	                ticketTypeMap.get(ticketType).add(ticketQuery);
	            } else {
	                // If the ticket type is not in the map, create a new list and add the ticket query
	                List<String> ticketQueries = new ArrayList<>();
	                ticketQueries.add(ticketQuery);
	                ticketTypeMap.put(ticketType, ticketQueries);
	            }
	        }
	       
	        // Convert the map to JSON using Jackson
	        String json = null;
	        try {
	            json = objectMapper.writeValueAsString(ticketTypeMap);
	            System.out.println(json);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }//emp.setEmployeeTicketTypes(ticketTypeMapList);

	        emp.setTicketTypeMapList(ticketTypeMap);
	        
			// To get TimeSheet Automation Status based on client

			String statusQuery = "select status.* from Client_Timesheet_Automation_Status status where client_id =:client_id ";
			List<com.helius.utils.Client_Timesheet_Automation_Status> status = session.createSQLQuery(statusQuery)
					.setResultTransformer(Transformers.aliasToBean(com.helius.utils.Client_Timesheet_Automation_Status.class))
					.setParameter("client_id", client_id).list();

			if (!status.isEmpty()) {
				emp.setClient_timesheet_Automation_Status(status.get(0));
			}

			// get client holiday list 
						String holidaysQuery = "SELECT * FROM `Holiday_Master` WHERE client_id =:client_id";
						List<Holiday_Master> holidayMasters = new ArrayList<>();
						List<Holiday_Master> holidayList = session.createSQLQuery(holidaysQuery)
								.setResultTransformer(Transformers.aliasToBean(Holiday_Master.class)).setParameter("client_id", client_id).list();
						if (!holidayList.isEmpty()) {
							holidayList.stream().forEach(holidays -> {
								holidayMasters.add(holidays);
							});
							emp.setClientHolidays(holidayMasters);
						}
						else {
							if (emp.getEmployeeOfferDetails().getWork_country().equals("Singapore")) {
								client_id = 225;
								String singaporeholidaysQuery = "SELECT * FROM `Holiday_Master` WHERE client_id =:client_id";
								List<Holiday_Master> singaporeholidayMasters = new ArrayList<>();
								List<Holiday_Master> singaporeholidayList = session.createSQLQuery(singaporeholidaysQuery)
										.setResultTransformer(Transformers.aliasToBean(Holiday_Master.class)).setParameter("client_id", client_id).list();
								if (!singaporeholidayList.isEmpty()) {
									singaporeholidayList.stream().forEach(holidays -> {
										singaporeholidayMasters.add(holidays);
									});
									emp.setClientHolidays(singaporeholidayMasters);
								}
							}else if (emp.getEmployeeOfferDetails().getWork_country().equals("India")) {
								client_id = 226;
								String inidaholidaysQuery = "SELECT * FROM `Holiday_Master` WHERE client_id =:client_id";
								List<Holiday_Master> indiaholidayMasters = new ArrayList<>();
								List<Holiday_Master> indiaholidayList = session.createSQLQuery(inidaholidaysQuery)
										.setResultTransformer(Transformers.aliasToBean(Holiday_Master.class)).setParameter("client_id", client_id).list();
								if (!indiaholidayList.isEmpty()) {
									indiaholidayList.stream().forEach(holidays -> {
										indiaholidayMasters.add(holidays);
									});
									emp.setClientHolidays(indiaholidayMasters);
								}
							}
						}
			
			
		} catch (Exception e) {
			logger.error("issue in fetcing employee details for employee "+employeeid +" find stacktrace",e);
		}finally{
			session.close();
		}
		return emp;
	}

	public String WorkLocationPicklist() {
		Session session = null;
		String workPermitJson = null;
		try {
			session = sessionFactory.openSession();
			SQLQuery query = session.createSQLQuery(
					"SELECT Country_Master.country_code, Country_Master.nationality,Work_Permit_Master.work_location, Work_Permit_Master.work_permit_master_id,Work_Permit_Master.display_fields,Work_Permit_Master.work_permit_name,Country_Master.identification_ids FROM Country_Master LEFT JOIN Work_Permit_Master ON Country_Master.country_code = Work_Permit_Master.country_code ORDER BY Country_Master.country_code");
			String work_Permit_Master_query = "SELECT Country_Master.country_code, Country_Master.nationality,Work_Permit_Master.work_location, Work_Permit_Master.work_permit_master_id,Work_Permit_Master.display_fields,Work_Permit_Master.work_permit_name,Country_Master.identification_ids FROM Country_Master LEFT JOIN Work_Permit_Master ON Country_Master.country_code = Work_Permit_Master.country_code ORDER BY Country_Master.country_code";
			java.util.List work_Permit_MasterList = session.createSQLQuery(work_Permit_Master_query)
					.addEntity(Work_Permit_Master.class).list();
			Work_Permit_Master work_Permit_Master = null;
			List<Object[]> work_Permit_Mstr = query.list();
			workPermitJson = Utils.jsonWorkPermitPicklist(work_Permit_Mstr);
			session.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "{\"workPermitJson\" : " + workPermitJson + "}";
	}

	
	/** Returns offer details using offerid **/
	public Employee getOfferDetails(String offerid) {
		Employee emp = new Employee();
		Session session = null;
		try {
			session = sessionFactory.openSession();
			String offer_query = "select * from Employee_Offer_Details where offer_id = :offer_id ";
			java.util.List offerlist = session.createSQLQuery(offer_query).addEntity(Employee_Offer_Details.class)
					.setParameter("offer_id", offerid).list();
			Employee_Offer_Details check_Employee_OfferDetails = null;
			if (!offerlist.isEmpty()) {
				check_Employee_OfferDetails = (Employee_Offer_Details) offerlist.iterator().next();
			}
			if (check_Employee_OfferDetails != null) {
				emp.setEmployeeOfferDetails(check_Employee_OfferDetails);
			}
			String employee_salary_query = "select salary.* from Employee_Salary_Details salary where offer_id = :offer_id ";
			java.util.List salaryList = session.createSQLQuery(employee_salary_query)
					.addEntity(Employee_Salary_Details.class).setParameter("offer_id", offerid).list();
			Employee_Salary_Details employee_Salary_Details = null;
			if (!salaryList.isEmpty()) {
				employee_Salary_Details = (Employee_Salary_Details) salaryList.iterator().next();
			}
			if (employee_Salary_Details != null) {
				emp.setEmployeeSalaryDetails(employee_Salary_Details);
			}
			
			String sowbreakup_query = "select breakup.* from Sow_Ctc_Breakup breakup where offer_id = :offer_id AND group_id =:group_id"; 
			  java.util.List breakupList = session.createSQLQuery(sowbreakup_query).addEntity(Sow_Ctc_Breakup.class).setParameter("offer_id", offerid).setParameter("group_id", 1).list();
			  Sow_Ctc_Breakup sowCtcBreakup = null; 
				List<Sow_Ctc_Breakup> sow_Ctc_BreakupList = new ArrayList<Sow_Ctc_Breakup>();
			  if(!breakupList.isEmpty()) {
				  for (Object sowbreakup : breakupList) {
					  sowCtcBreakup = (Sow_Ctc_Breakup) sowbreakup;
					  sow_Ctc_BreakupList.add(sowCtcBreakup);
					}			  }
			  if(sow_Ctc_BreakupList!= null) {
			  emp.setSowCtcBreakup(sow_Ctc_BreakupList); 
			  }
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			session.close();
		}
		return emp;
	}
	
	/** Return offer details by name **/
	@Override
	public List<Employee_Offer_Details> getOfferDetailsByName(String offerName,String type) {
		Session session = null;
		int offerid = 0;
		List<Employee_Offer_Details> employee_Offer_Details = new ArrayList<>();
		java.util.List offerlist = null;
		try {
			session = sessionFactory.openSession();
			if(type.equalsIgnoreCase("NAME")){
			String offer_query = "select * from Employee_Offer_Details where employee_name LIKE :offerName";
			 offerlist = session.createSQLQuery(offer_query).addEntity(Employee_Offer_Details.class)
					.setParameter("offerName", "%"+ offerName + "%").list();
			}else{
				String offer_query = "select * from Employee_Offer_Details where offer_id = :offer_id";
				 offerlist = session.createSQLQuery(offer_query).addEntity(Employee_Offer_Details.class)
						.setParameter("offer_id", offerName).list();
			}
			Employee_Offer_Details check_Employee_OfferDetails = null;
			for (Object Employee_Offer_Detail : offerlist) {
				check_Employee_OfferDetails = (Employee_Offer_Details) Employee_Offer_Detail;
				employee_Offer_Details.add(check_Employee_OfferDetails);
			}
			session.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return employee_Offer_Details;
	}
	@Override
	public Employee_Leave_Data getEmployeeLeaveData(String employee_id) throws Throwable {
		Employee_Leave_Data employeeLeaveData = new Employee_Leave_Data();
		Session session = null;
		LocalDate date = LocalDate.now();
		int year = date.getYear();
		try {
			session = sessionFactory.openSession();	
			//String emp_work_country_query = "select a.work_country from Employee_Work_Permit_Details a where a.employee_id=" + employee_id;
			String emp_work_country_query = "select a.work_country from Employee_Work_Permit_Details a where a.employee_id='"+employee_id+"'" ;
			System.out.println("emp_work_country_query :"+emp_work_country_query);
			List workcountry_list = session.createQuery(emp_work_country_query).list();
			String workcountry = (String) workcountry_list.get(0);
			String leaveEligibility = "";
			List<Leave_Eligibility_Details> eligibilityList =null;
			List<Leave_Eligibility_Details> leave_Eligibility_DetailsList = null;
			int client_id = 0;
			// Leave Eligibility details
			if ("Singapore".equalsIgnoreCase(workcountry)) {
				leave_Eligibility_DetailsList = new ArrayList<Leave_Eligibility_Details>();
				int currentYear = LocalDate.now().getYear();
				int previousYear = LocalDate.now().getYear() - 1 ;
				
				leaveEligibility = "SELECT a.* FROM Leave_Eligibility_Details a, Employee_Assignment_Details b, client_details c  WHERE a.employee_id=:employee_id "
						+ "AND a.employee_id=b.employee_id AND a.client_id=c.client_id AND   b.client=c.client_name and a.year In('"+currentYear+"','"+previousYear+"')";
				System.out.println("leaveEligibility:"+leaveEligibility);
				eligibilityList = session.createSQLQuery(leaveEligibility)
						.addEntity(Leave_Eligibility_Details.class).setParameter("employee_id", employee_id).list();
				
				Map<String,Leave_Eligibility_Details> led_leavetype = new HashMap<String,Leave_Eligibility_Details>();
				if (!eligibilityList.isEmpty()) {
					for (Leave_Eligibility_Details leave_Eligibility : eligibilityList) {
						if(led_leavetype.containsKey(leave_Eligibility.getType_of_leave())) {
							Leave_Eligibility_Details temp1 = leave_Eligibility; 
							Leave_Eligibility_Details temp2 = led_leavetype.get(leave_Eligibility.getType_of_leave());
							if(temp1.getLeave_eligibility_details_id() > temp2.getLeave_eligibility_details_id() ){
								led_leavetype.put(temp1.getType_of_leave(), temp1);
								
							}
						} else {
							led_leavetype.put(leave_Eligibility.getType_of_leave(), leave_Eligibility);
						}
						}
					}
				
				for(Iterator<Map.Entry<String, Leave_Eligibility_Details>> it = led_leavetype.entrySet().iterator(); it.hasNext(); ) {
				    Map.Entry<String, Leave_Eligibility_Details> entry = it.next();
					  /*  if(entry.getKey().equalsIgnoreCase("CF Leave") && entry.getValue().getYear() == LocalDate.now().getYear() - 1) {
					    	 it.remove();
					    }*/
				    	Leave_Eligibility_Details le = entry.getValue();
				    	client_id = le.getClient_id();
				    	
				    }
					
					Iterator<Leave_Eligibility_Details>  iter = led_leavetype.values().iterator();
					Leave_Eligibility_Details lv =null;
					while(iter.hasNext()){
						
						leave_Eligibility_DetailsList.add(iter.next());
			      }
					
			}
			else{
			 leaveEligibility = "select * from  Leave_Eligibility_Details where employee_id = :employee_id AND year = :year";
			 eligibilityList = session.createSQLQuery(leaveEligibility)
					.addEntity(Leave_Eligibility_Details.class).setParameter("employee_id", employee_id).setParameter("year",year).list();
			 leave_Eligibility_DetailsList = new ArrayList<Leave_Eligibility_Details>();
			 
				if (!eligibilityList.isEmpty()) {
					for (Leave_Eligibility_Details leave_Eligibility : eligibilityList) {
						leave_Eligibility_DetailsList.add(leave_Eligibility);
					}
				}
			}
			
			if (leave_Eligibility_DetailsList != null && !leave_Eligibility_DetailsList.isEmpty()) {
				employeeLeaveData.setLeavesEligibility(leave_Eligibility_DetailsList);
			}
			//Leave_Usage_details
			String leaveUsageDetails = "";
			java.util.List leaveUsageDetailsList = null;
			if ("Singapore".equalsIgnoreCase(workcountry)) {
				leaveUsageDetails = "SELECT a.* FROM Leave_Usage_Details a, Employee_Assignment_Details b, client_details c  WHERE a.employee_id=:employee_id "
						+ " AND YEAR(usageMonth)= :year AND a.employee_id=b.employee_id AND a.client_id=c.client_id  ORDER BY usageMonth DESC";
				System.out.println("leaveUsageDetails::"+leaveUsageDetails);
				leaveUsageDetailsList = session.createSQLQuery(leaveUsageDetails)
						      .addEntity(Leave_Usage_Details.class).setParameter("employee_id", employee_id).setParameter("year",year).list();
			}
			else{
			leaveUsageDetails = "select * from  Leave_Usage_Details where employee_id = :employee_id AND YEAR(usageMonth)= :year ORDER BY usageMonth DESC";
			 leaveUsageDetailsList = session.createSQLQuery(leaveUsageDetails)
					.addEntity(Leave_Usage_Details.class).setParameter("employee_id", employee_id).setParameter("year",year).list();
			}
			 ArrayList<Leave_Usage_Details> leave_Usage_DetailsList = new ArrayList<Leave_Usage_Details>();
			Leave_Usage_Details leave_Usage_Details = null;
			if(leaveUsageDetailsList != null){
				for(Object obj : leaveUsageDetailsList){
				leave_Usage_Details = (Leave_Usage_Details)obj;
				leave_Usage_DetailsList.add(leave_Usage_Details);
				}
				if(leave_Usage_DetailsList != null && !leave_Usage_DetailsList.isEmpty()){
				employeeLeaveData.setLeaveUsageDetails(leave_Usage_DetailsList);
				}
			}
			//Leave_Record_Details
			String recordQuery ="";
			List<Leave_Record_Details> recordQueryList =null;
			/*List<Sow_Details> sow_details =null;
			String sowquery = "select * from Sow_Employee_Association a, Sow_Details b where a.employee_id=:employee_id"
					+ " and a.sow_details_id=b.sow_details_id and a.status='active' and  b.sow_status='active' ";
			sow_details = session.createSQLQuery(sowquery).addEntity(Sow_Details.class)
					.setParameter("employee_id", employee_id).list();*/
			if ("Singapore".equalsIgnoreCase(workcountry)) {
				recordQueryList = new ArrayList<>();
				
				/*//if (client_id == 227) {
				if(sow_details != null && !sow_details.isEmpty()) {
					recordQuery = "SELECT DISTINCT a.* FROM Leave_Record_Details a, Employee_Assignment_Details b, client_details c,Sow_Employee_Association d,Sow_Details e"
							+ " WHERE a.employee_id = :employee_id  AND a.employee_id=b.employee_id AND a.client_id=c.client_id "
							+ "AND b.client=c.client_name AND a.employee_id = d.employee_id AND d.sow_details_id=e.sow_details_id AND e.sow_status ='Active' AND a.leaveMonth"
							+ " BETWEEN DATE_SUB(e.sow_start_date, INTERVAL 1 MONTH) AND  DATE_SUB(e.sow_expiry_date, INTERVAL -1 MONTH) ORDER BY leaveMonth DESC";
					System.out.println("recordQuery::" + recordQuery);
					recordQueryList = session.createSQLQuery(recordQuery).addEntity(Leave_Record_Details.class)
							.setParameter("employee_id", employee_id).list();
				} else {

					recordQuery = "SELECT a.* FROM Leave_Record_Details a, Employee_Assignment_Details b, client_details c WHERE a.employee_id= :employee_id AND "
							+ " a.employee_id=b.employee_id AND a.client_id=c.client_id AND b.client=c.client_name AND a.leaveMonth "
							+ "BETWEEN DATE_SUB(b.sow_start_date, INTERVAL 1 MONTH) AND DATE_SUB(b.sow_expiry_date, INTERVAL -1 MONTH) ORDER BY leaveMonth DESC";
					System.out.println("recordQuery::" + recordQuery);
					recordQueryList = session.createSQLQuery(recordQuery).addEntity(Leave_Record_Details.class)
							.setParameter("employee_id", employee_id).list();

				}*/
				
				    recordQuery = "SELECT DISTINCT a.* FROM Leave_Record_Details a, Employee_Assignment_Details b, client_details c,Employee_Terms_And_Conditions d "
						+ "WHERE a.employee_id=b.employee_id AND a.employee_id = d.employee_id AND a.client_id=c.client_id AND b.client=c.client_name AND a.leaveMonth "
						+ "AND CAST(a.startdate AS DATE)>= CAST(d.contract_startdate AS DATE) AND CAST(a.enddate AS DATE) <= CAST(d.contract_enddate AS DATE) AND "
						+ "a.employee_id= :employee_id ORDER BY leaveMonth DESC";
				    System.out.println("recordQuery::"+recordQuery);
				    
				    recordQueryList = session.createSQLQuery(recordQuery).addEntity(Leave_Record_Details.class)
							.setParameter("employee_id", employee_id).list();
				    

			} else {
				recordQuery = "select * from  Leave_Record_Details where employee_id = :employee_id AND YEAR(leaveMonth) = :year ORDER BY leaveMonth DESC";
				recordQueryList = session.createSQLQuery(recordQuery).addEntity(Leave_Record_Details.class)
						.setParameter("employee_id", employee_id).setParameter("year", year).list();
			}
			
			ArrayList<Leave_Record_Details> leave_Record_DetailsList = new ArrayList<Leave_Record_Details>();
			//Leave_Record_Details leaveRecordDetails = null;
			if(recordQueryList != null){
				for(Leave_Record_Details lvRecordDetails : recordQueryList){
					if ("Singapore".equalsIgnoreCase(workcountry)) {
						if (lvRecordDetails.getType_of_leave().equalsIgnoreCase("Lieu")) {
							lvRecordDetails.setType_of_leave("Off In Lieu");
						}
						if (lvRecordDetails.getType_of_leave().equalsIgnoreCase("Sick Leave")) {
							lvRecordDetails.setType_of_leave("Medical Leave");
						}
					}
					
					leave_Record_DetailsList.add(lvRecordDetails);
				}
				if(leave_Record_DetailsList != null && !leave_Record_DetailsList.isEmpty()){
				employeeLeaveData.setLeaveRecordDetails(leave_Record_DetailsList);
				}
			}
			
			/* Summary of leaves entitlement and utilized */
			List<LeaveUtilization> LeaveUtilizationList = null;
			
			if ("Singapore".equalsIgnoreCase(workcountry)) {
				LeaveUtilization utilization = null;
				String leaveUtilizedQuery = "";
				java.util.List leaveUtilizedList = null;
				//if (client_id == 227) {
				/*if(sow_details != null && !sow_details.isEmpty()) {
					leaveUtilizedQuery = "SELECT DISTINCT c.employee_id, SUM(a.leaves_used) AS utilizedLeave,a.type_of_leave AS leaveType, a.client_id FROM Leave_Record_Details a, Employee_Work_Permit_Details b,"
							+ " Sow_Employee_Association c, Sow_Details d WHERE a.employee_id=b.employee_id AND b.work_country='Singapore' AND a.employee_id=c.employee_id AND c.sow_details_id=d.sow_details_id AND c.status='active'"
							+ " AND d.sow_status='active' AND CAST(a.startdate AS DATE)>= CAST(d.sow_start_date AS DATE) AND CAST(a.enddate AS DATE) <= CAST(d.sow_expiry_date AS DATE) AND"
							+ " a.employee_id='" + employee_id + "' GROUP BY a.type_of_leave";
					System.out.println("leaveUtilizedQuery :"+leaveUtilizedQuery);
					leaveUtilizedList = session.createSQLQuery(leaveUtilizedQuery)
							.setResultTransformer(Transformers.aliasToBean(LeaveUtilization.class)).list();

				} else {
					leaveUtilizedQuery = "SELECT DISTINCT c.employee_id,SUM(a.leaves_used) AS utilizedLeave,a.type_of_leave AS leaveType, a.client_id FROM Leave_Record_Details a, Employee_Work_Permit_Details b,"
							+ " Employee_Assignment_Details c WHERE a.employee_id=b.employee_id AND b.work_country='Singapore' AND a.employee_id=c.employee_id AND"
							+ " CAST(a.startdate AS DATE)>= CAST(c.sow_start_date AS DATE) AND CAST(a.enddate AS DATE) <= CAST(c.sow_expiry_date AS DATE) AND"
							+ " a.employee_id= " + employee_id + " GROUP BY a.type_of_leave";
					System.out.println("leaveUtilizedQuery :"+leaveUtilizedQuery);
					leaveUtilizedList = session.createSQLQuery(leaveUtilizedQuery)
							.setResultTransformer(Transformers.aliasToBean(LeaveUtilization.class)).list();

				}*/
				leaveUtilizedQuery ="SELECT DISTINCT c.employee_id, SUM(a.leaves_used) AS utilizedLeave,a.type_of_leave AS leaveType, a.client_id FROM "
						+ "Leave_Record_Details a, Employee_Work_Permit_Details b,Employee_Terms_And_Conditions c,Employee_Assignment_Details d,client_details e"
						+ " WHERE a.employee_id=b.employee_id AND a.employee_id=c.employee_id AND a.employee_id = d.employee_id AND d.client =e.client_name AND a.client_id=e.client_id AND "
						+ "b.work_country='Singapore' AND CAST(a.startdate AS DATE)>= CAST(c.contract_startdate AS DATE) AND CAST(a.enddate AS DATE) <= CAST(c.contract_enddate AS DATE) "
						+ "AND d.client !='Helius' AND a.employee_id ='" + employee_id + "' GROUP BY a.type_of_leave";
				System.out.println("leaveUtilizedQuery::"+leaveUtilizedQuery);
				leaveUtilizedList = session.createSQLQuery(leaveUtilizedQuery)
						.setResultTransformer(Transformers.aliasToBean(LeaveUtilization.class)).list();
				
				 List<LeaveUtilization> summaryOfLeaveUtilization = getConvertLieuTypeInOffInLieu(leaveUtilizedList);
				
				Map<String, Leave_Eligibility_Details> eligibilityMap = new HashMap<>();
				if (leave_Eligibility_DetailsList != null && !leave_Eligibility_DetailsList.isEmpty()) {
					for (Leave_Eligibility_Details list : leave_Eligibility_DetailsList) {
						if (!eligibilityMap.containsKey(list.getType_of_leave())) {
							eligibilityMap.put(list.getType_of_leave(), list);
						}
				}
				}
				
				LeaveUtilizationList = new ArrayList<LeaveUtilization>();
				float cfLeave = 0;
				if (summaryOfLeaveUtilization != null && !summaryOfLeaveUtilization.isEmpty()) {
					utilization = new LeaveUtilization();
				//	LeaveUtilizationList = new ArrayList<LeaveUtilization>();

					for (Object obj : summaryOfLeaveUtilization) {
						LeaveUtilization leaveUtilization = new LeaveUtilization();
						utilization = (LeaveUtilization) obj;
						leaveUtilization.setEmployee_id(utilization.getEmployee_id());
						leaveUtilization.setClient_id(utilization.getClient_id());
						leaveUtilization.setUtilizedLeave(utilization.getUtilizedLeave());
						float eligible = 0;
						if (eligibilityMap.containsKey(utilization.getLeaveType())) {

							for (Leave_Eligibility_Details details : eligibilityMap.values()) {
								if (details.getType_of_leave().equalsIgnoreCase("CF Leave")) {
									cfLeave = details.getNumber_of_days();
								}
								if (details.getType_of_leave().equals(utilization.getLeaveType())) {

									eligible = details.getNumber_of_days();
									leaveUtilization.setLeaveType(utilization.getLeaveType());
									leaveUtilization.setEntitlement(eligible);
									if (utilization.getLeaveType().equalsIgnoreCase("Annual Leave")) {
										leaveUtilization.setCarryForward(cfLeave);
										float annualAndCFLeave = eligible + cfLeave;
										leaveUtilization.setBalanceLeave(annualAndCFLeave - utilization.getUtilizedLeave());
									} else {
										leaveUtilization.setBalanceLeave(eligible - utilization.getUtilizedLeave());
										
										if(utilization.getLeaveType().equalsIgnoreCase("Off In Lieu")){
											leaveUtilization.setBalanceLeave(eligible);
										}
									}
								}
								
							}

						} else {
							leaveUtilization.setLeaveType(utilization.getLeaveType());
							leaveUtilization.setEntitlement(eligible);
							leaveUtilization.setUtilizedLeave(utilization.getUtilizedLeave());
							leaveUtilization.setBalanceLeave(eligible - utilization.getUtilizedLeave());
						}
						if (utilization.getLeaveType().equalsIgnoreCase("Annual Leave")
								|| utilization.getLeaveType().equalsIgnoreCase("Sick Leave")
								/*|| utilization.getLeaveType().equalsIgnoreCase("Childcare Leave")*/
								/*	|| utilization.getLeaveType().equalsIgnoreCase("Off In Lieu")*/ ) {

							LeaveUtilizationList.add(leaveUtilization);
							
						}
						
					}
					
				}
				Map<String, LeaveUtilization> utilizationMap= new HashMap<>();
				//LeaveUtilization leave = new LeaveUtilization();
				if(LeaveUtilizationList !=null && !LeaveUtilizationList.isEmpty()){
				for (Object object : LeaveUtilizationList) {
					LeaveUtilization leave = new LeaveUtilization();
					 leave =(LeaveUtilization) object;
					 if (!utilizationMap.containsKey(leave.getLeaveType())) {
						 utilizationMap.put(leave.getLeaveType(), leave);
					}
				}
				}
				if (eligibilityMap!=null && !eligibilityMap.isEmpty()) {
					float utilizedLeave =0;
					LeaveUtilization eligibilityUtlizedLeave =null;
					List<LeaveUtilization> utilizationList  = new ArrayList<>();
					//LeaveUtilizationList =new ArrayList<>();
					for (Leave_Eligibility_Details details : eligibilityMap.values()) {
						eligibilityUtlizedLeave =new LeaveUtilization();
						float eligible =0;
						if (details.getType_of_leave().equalsIgnoreCase("CF Leave")) {
							 cfLeave = details.getNumber_of_days(); 
						 }
							if (!utilizationMap.containsKey(details.getType_of_leave())) {
								if (!details.getType_of_leave().equalsIgnoreCase("CF Leave")) {
									//eligibilityUtlizedLeave =new LeaveUtilization();
									eligible =details.getNumber_of_days();
								
									eligibilityUtlizedLeave.setEmployee_id(details.getEmployee_id());
									eligibilityUtlizedLeave.setClient_id(details.getClient_id());
								 if (details.getType_of_leave().equalsIgnoreCase("Annual Leave")) {
									 eligibilityUtlizedLeave.setCarryForward(cfLeave);
										float annualAndCFLeave = eligible + cfLeave;
										eligibilityUtlizedLeave.setBalanceLeave(annualAndCFLeave - utilizedLeave);

								}
								 else{
									 eligibilityUtlizedLeave.setBalanceLeave(eligible - utilizedLeave); 
								 }
								 eligibilityUtlizedLeave.setLeaveType(details.getType_of_leave());
								 eligibilityUtlizedLeave.setEntitlement(details.getNumber_of_days());
								
								if (details.getType_of_leave().equalsIgnoreCase("Annual Leave")
										||details.getType_of_leave().equalsIgnoreCase("Sick Leave")) {
							//	||details.getType_of_leave().equalsIgnoreCase("Off In Lieu")) {
									utilizationList.add(eligibilityUtlizedLeave);
								}
								
							}
						}
					
					}
					//utilizationList.stream().forEach(System.out::println);
					LeaveUtilizationList.addAll(utilizationList);
					
					
				}
				
			}
			List<LeaveUtilization> finalLeaveUtilizationList =null;
			finalLeaveUtilizationList =new ArrayList<>();
			 if(LeaveUtilizationList != null && !LeaveUtilizationList.isEmpty()){
				 for (Object object : LeaveUtilizationList) {
						LeaveUtilization leaveutil =new LeaveUtilization();
						leaveutil=(LeaveUtilization) object;
						if(leaveutil.getLeaveType().equalsIgnoreCase("Sick Leave")){
							leaveutil.setLeaveType("Medical Leave");
						}
						finalLeaveUtilizationList.add(leaveutil);
						}
				 //finalLeaveUtilizationList.stream().forEach(System.out::println);
				 if(finalLeaveUtilizationList !=null && !finalLeaveUtilizationList.isEmpty()){
					 employeeLeaveData.setLeaveUtilizations(finalLeaveUtilizationList);
				 }
					
				} 
           if("Singapore".equalsIgnoreCase(workcountry)) {
				 
				 Timestamp  conStartDate= null;
					List<Employee_Off_In_Lieu_Data> employee_Off_In_Lieu_Datas = new ArrayList<>();
					List<Employee_Off_In_Lieu>  Off_In_Lieus = new ArrayList<>();
					Timestamp sowEndDate = null;
					Timestamp sowStartDate = null;
					
			
					String contractdateQuery = "SELECT a.employee_id, b.client_start_date, a.actual_date_of_joining,b.sow_expiry_date,b.sow_start_date FROM Employee_Personal_Details a \r\n"
							+ "    LEFT JOIN Employee_Assignment_Details b\r\n"
							+ "    ON a.employee_id = b.employee_id  WHERE a.employee_id ='"+employee_id+"'";
					List<Object[]> contractdateList =session.createSQLQuery(contractdateQuery).list();
					if (contractdateList != null) {
						for (Object[] obj : contractdateList) {
							if(obj[1] !=null && obj[2]!= null){
								conStartDate = (Timestamp) obj[1];
							}else {
								conStartDate = (Timestamp) obj[2];
							}
							if(obj[3] !=null) {
								sowEndDate = (Timestamp) obj[3];
							}
							if(obj[4] !=null) {
								sowStartDate = (Timestamp) obj[4];
							}
						}
					}
				
					Integer no_of_oil_days = 0;
					String validity = null;
					String QueryClientdetails ="SELECT * FROM `client_details` WHERE client_id =:client_id";
					List<com.helius.entities.ClientDetail> clientDetailsList =  session.createSQLQuery(QueryClientdetails).addEntity(com.helius.entities.ClientDetail.class)
							.setParameter("client_id", client_id).list();
					if(!clientDetailsList.isEmpty() && clientDetailsList.get(0).getOil_validity()!= null){
						 validity = clientDetailsList.get(0).getOil_validity();
						if(!validity.equalsIgnoreCase("sowEndDate")) {
							no_of_oil_days = Integer.parseInt(validity);
						}	
					}
					if( validity !=null  && validity.equalsIgnoreCase("sowEndDate")) {
						conStartDate =sowStartDate;
					}
					Date date2 = new Date();
					Timestamp currenttimestamp = new Timestamp(date2.getTime());
					Timestamp oildate = new Timestamp(date2.getTime());
					String typeofleave= "Off In Lieu";
					float totalSumofLeaves =0;
					
				
					
					if(conStartDate!= null) {
						if(validity !=null  && validity.equalsIgnoreCase("sowEndDate")) {
							String query_OIL ="SELECT * FROM `Employee_Off_In_Lieu` WHERE oil_date >=:sowStartDate  AND oil_date<=:sowEndDate";
							 Off_In_Lieus =  session.createSQLQuery(query_OIL).addEntity(Employee_Off_In_Lieu.class).setParameter( "sowStartDate",sowStartDate)
										.setParameter( "sowEndDate",sowEndDate).list();
							
						}else {
							String query_OIL ="SELECT * FROM `Employee_Off_In_Lieu` WHERE oil_date >=:conStartDate AND YEAR(oil_date) =:currenttimestamp";
							  Off_In_Lieus =  session.createSQLQuery(query_OIL).addEntity(Employee_Off_In_Lieu.class).setParameter( "conStartDate",conStartDate)
									.setParameter( "currenttimestamp",currenttimestamp).list();
						}
						if(!Off_In_Lieus.isEmpty()) {
							oildate = Off_In_Lieus.get(0).getOil_date();
						}
						
						
						
						String leaveRecordQuery = "SELECT a.employee_id,a.type_of_leave, SUM(a.leaves_used) AS total  FROM Leave_Record_Details a \r\n"
								+ "  WHERE a.leaveMonth >= '"+conStartDate+"'  AND YEAR(a.leaveMonth) >= '"+oildate+"' AND  a.leaveMonth <= '"+currenttimestamp+"' AND (a.type_of_leave ='Off In Lieu' OR a.type_of_leave = 'Off-In-Lieu') "
										+ "AND a.employee_id= '"+employee_id+"' AND a.client_id='"+client_id+"'";
						List<Object[]> recordList =session.createSQLQuery(leaveRecordQuery).list();
						if (recordList != null) {
							for (Object[] obj : recordList) {
								if(obj[0] !=null && obj[1] !=null
										&& obj[2]!= null){
									totalSumofLeaves = Float.parseFloat(obj[2].toString());
								}
								
							}
						}
						
						
						
						if(!Off_In_Lieus.isEmpty()) {
							//float totalleaves = totalSumofLeaves;
							Employee_Off_In_Lieu_Data lieu = null;
							for( Employee_Off_In_Lieu OIL_db : Off_In_Lieus) {
								lieu= new Employee_Off_In_Lieu_Data();
								lieu.setEmployee_off_in_lieu_id(OIL_db.getEmployee_off_in_lieu_id());
								lieu.setClient_id(OIL_db.getClient_id());
								lieu.setOil_public_holiday(OIL_db.getOil_public_holiday());
								lieu.setClient_name(OIL_db.getClient_name());
								lieu.setNo_of_days(OIL_db.getNo_of_days());
								lieu.setOil_date(OIL_db.getOil_date());
								lieu.setOil_day(OIL_db.getOil_day());
								 
							    Timestamp holidayDate =OIL_db.getOil_date();
							 
					            Calendar calendar = Calendar.getInstance();
					            calendar.setTime(holidayDate);
					            calendar.add(Calendar.DAY_OF_MONTH, 1);
					            Timestamp validityStartDate = new Timestamp(calendar.getTime().getTime());

					            Timestamp validityEndDate = null;
					            if(no_of_oil_days !=0) {
					            	calendar.setTime(holidayDate);
						            calendar.add(Calendar.DAY_OF_MONTH, no_of_oil_days);
						            validityEndDate = new Timestamp(calendar.getTime().getTime());
					            }else if(validity !=null && validity.equalsIgnoreCase("sowEndDate")) {
					            	validityEndDate = sowEndDate;
					            }
					            if(totalSumofLeaves   >= 0.5 ) {
					            	if(totalSumofLeaves >=1) {
					            		lieu.setLeavesUsed(1);
					            		totalSumofLeaves =	totalSumofLeaves -1;
					            	}else {
					            		lieu.setLeavesUsed(totalSumofLeaves);
					            		totalSumofLeaves = (float) (totalSumofLeaves -0.5);
					            	}
					            	
					            }
					            

					            lieu.setOil_Validity_Start_Date(validityStartDate);
					            lieu.setOil_Validity_End_Date(validityEndDate);
					            
					            employee_Off_In_Lieu_Datas.add(lieu);
								
							}
						}
						if (employee_Off_In_Lieu_Datas != null && !employee_Off_In_Lieu_Datas.isEmpty()) {
							employeeLeaveData.setEmployee_Off_In_Lieu_Datas(employee_Off_In_Lieu_Datas);
						}
					}
					
				}
			 
			 
			 
			 
			
		}catch(Exception e){
			e.printStackTrace();
			logger.error("failed to fetch leave details for employee "+employee_id,e);
			throw new Throwable("Failed to fetch Employee Leave Details");
		}finally{
			session.close();
		}
		return employeeLeaveData;		
	}
	

	@Override
	public void offUpdate(Employee employee, MultipartHttpServletRequest request) throws Throwable {
		Session session = null;
		Transaction transaction = null;
		int offerId = 0;
		Map<String, String> templateFilenames = new HashMap<String, String>();
		Map<String, String> fileFolder = new HashMap<String, String>();
		try {
			session = sessionFactory.openSession();
			transaction = session.beginTransaction();
			if (employee.getEmployeeOfferDetails() != null) {
				offerId = employee.getEmployeeOfferDetails().getOffer_id();	
				if(employee.getEmployeeOfferDetails().getName_in_chinese_characters()!=null ){
					String convertedValue = new String(employee.getEmployeeOfferDetails().getName_in_chinese_characters().getBytes("ISO8859_1"), "UTF8");	
					employee.getEmployeeOfferDetails().setName_in_chinese_characters(convertedValue);	
				}
			//	session.evict(employee.getEmployeeOfferDetails());
				session.update(employee.getEmployeeOfferDetails());
				if(employee.getEmployeeOfferDetails().getOffer_path()!=null && !employee.getEmployeeOfferDetails().getOffer_path().isEmpty()){
					String modifiedFileName =  offerId+"_"+employee.getEmployeeOfferDetails().getOffer_path();
				    templateFilenames.put(employee.getEmployeeOfferDetails().getOffer_path(), modifiedFileName);
				    fileFolder.put(employee.getEmployeeOfferDetails().getOffer_path(), "offer");
				}
				if(employee.getEmployeeOfferDetails().getJob_desc_path()!=null && !employee.getEmployeeOfferDetails().getJob_desc_path().isEmpty()){
					String modifiedFileName =  offerId+"_"+employee.getEmployeeOfferDetails().getJob_desc_path();
				    templateFilenames.put(employee.getEmployeeOfferDetails().getJob_desc_path(), modifiedFileName);
				    fileFolder.put(employee.getEmployeeOfferDetails().getJob_desc_path(), "jobdescription");
				}
				if(employee.getEmployeeOfferDetails().getEp_path()!=null && !employee.getEmployeeOfferDetails().getEp_path().isEmpty()){
					String modifiedFileName =  offerId+"_"+employee.getEmployeeOfferDetails().getEp_path();
				    templateFilenames.put(employee.getEmployeeOfferDetails().getEp_path(), modifiedFileName);
				    fileFolder.put(employee.getEmployeeOfferDetails().getEp_path(), "ep");
				}
				if(employee.getEmployeeOfferDetails().getBgv_report_path()!=null && !employee.getEmployeeOfferDetails().getBgv_report_path().isEmpty()){
					String modifiedFileName =  offerId+"_"+employee.getEmployeeOfferDetails().getBgv_report_path();
				    templateFilenames.put(employee.getEmployeeOfferDetails().getBgv_report_path(), modifiedFileName);
				    fileFolder.put(employee.getEmployeeOfferDetails().getBgv_report_path(), "bgv");
				}
				}
			if (employee.getEmployeeSalaryDetails()!= null) {
				employee.getEmployeeSalaryDetails().setOffer_id(offerId);
				session.evict(employee.getEmployeeSalaryDetails());
				session.merge(employee.getEmployeeSalaryDetails());
				}
			if (employee.getSowCtcBreakup() != null) {
				Iterator<Sow_Ctc_Breakup> itr = employee.getSowCtcBreakup().iterator();
				while (itr.hasNext()) {
					Sow_Ctc_Breakup sowCtcBreakup = itr.next();
					sowCtcBreakup.setOfferId(offerId);
					sowCtcBreakup.setGroupId(1);
					sowCtcBreakup.setStatus("active");
					session.evict(sowCtcBreakup);
					session.merge(sowCtcBreakup);
				}
			}
			if(employee.getDeletedsowCtcBreakup()!=null){
				Iterator<Sow_Ctc_Breakup> itr = employee.getDeletedsowCtcBreakup().iterator();
				while (itr.hasNext()) {
					Sow_Ctc_Breakup sow_Ctc_Breakup = itr.next();
					session.delete(sow_Ctc_Breakup);
				}
			}
			Map<String, MultipartFile> files = null;
			files = request.getFileMap();
			if (files.size() > 0) {			
				FilecopyStatus status = Utils.copyFiles(request, templateFilenames, fileFolder);
			copied_with_success = status.getCopied_with_success();
			}
			transaction.commit();
		} catch (HibernateException e) {
			e.printStackTrace();
			Utils.deleteFiles(copied_with_success);
			throw new Throwable(e.getCause().getMessage());
		
		} catch (Exception e) {
			e.printStackTrace();
			Utils.deleteFiles(copied_with_success);
			throw new Throwable(e.getMessage());
		} finally {
			session.close();
		}
	}
	
	@Override
	public ResponseEntity<byte[]> getEmployeeFiles(String employee_id, String filetype) {		
		Employee employee = get(employee_id);
		String url = null;
		if (filetype.equalsIgnoreCase("photo")) {
			url=employee.getEmployeePersonalDetails().getPhoto();
			if (url != null && !"".equalsIgnoreCase(url)) {
			url = Utils.getProperty("fileLocation") + File.separator + filetype + File.separator + employee_id+"_"+url;
		}
		}
		if (filetype.equalsIgnoreCase("cancelledcheque")) {
			url=employee.getEmployeeBankDetails().getCancelled_cheque_path();
			if (url != null && !"".equalsIgnoreCase(url)) {
			url = Utils.getProperty("fileLocation") + File.separator + filetype + File.separator + employee_id+"_"+url;
		}
		}
		byte[] files = null;
		FileInputStream fi = null;
		try {
			fi = new FileInputStream(url);
			files = IOUtils.toByteArray(fi);
			fi.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		}
		ResponseEntity<byte[]> responseEntity = new ResponseEntity<byte[]>(files, HttpStatus.OK);
		return responseEntity;
	}

	@Override
	public ResponseEntity<byte[]> getOfferFiles(String offerid, String filetype) {
		Employee employee = getOfferDetails(offerid);
		String url = null;
		if (filetype.equalsIgnoreCase("offer")) {
			url = employee.getEmployeeOfferDetails().getOffer_path();
			if (url != null && !"".equalsIgnoreCase(url)) {
				url = Utils.getProperty("fileLocation") + File.separator + filetype + File.separator + offerid+"_"+url;
			}
		}
		if (filetype.equalsIgnoreCase("jobdescription")) {
			url = employee.getEmployeeOfferDetails().getJob_desc_path();
			if (url != null && !"".equalsIgnoreCase(url)) {
				url = Utils.getProperty("fileLocation") + File.separator + filetype + File.separator + offerid+"_"+url;
			}
		}
		if (filetype.equalsIgnoreCase("ep")) {
			url = employee.getEmployeeOfferDetails().getEp_path();
			if (url != null && !"".equalsIgnoreCase(url)) {
				url = Utils.getProperty("fileLocation") + File.separator + filetype + File.separator + offerid+"_"+url;
			}
		}
		if (filetype.equalsIgnoreCase("bgv")) {
			url = employee.getEmployeeOfferDetails().getBgv_report_path();
			if (url != null && !"".equalsIgnoreCase(url)) {
				url = Utils.getProperty("fileLocation") + File.separator + filetype + File.separator + offerid+"_"+url;
			}
		}
		byte[] files = null;
		FileInputStream fi = null;
		try {
			fi = new FileInputStream(url);
			files = IOUtils.toByteArray(fi);
			fi.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		}
		ResponseEntity<byte[]> responseEntity = new ResponseEntity<byte[]>(files, HttpStatus.OK);
		return responseEntity;
	}

	@Override
	public void empUpdate(Employee employee, MultipartHttpServletRequest request) throws Throwable {
	String	offerId = null;
		Session session = null;
		Transaction transaction = null;
		session = sessionFactory.openSession();
		transaction = session.beginTransaction();
		Map<String, String> templateFilenames = new HashMap<String, String>();
		Map<String, String> fileFolder = new HashMap<String, String>();
		long millis = System.currentTimeMillis();
		java.sql.Date date = new java.sql.Date(millis);
		Timestamp date1 = new Timestamp(System.currentTimeMillis());
		Object employeeid = null;
		String employee_id = null;
		try {		
			if(employee.getEmployeePersonalDetails()!=null){
			Employee_Personal_Details employee_personal_details = employee.getEmployeePersonalDetails();
			employee_id = employee.getEmployeePersonalDetails().getEmployee_id();
			if(employee_personal_details.getName_in_chinese_characters()!=null ){
				String convertedValue = new String(employee_personal_details.getName_in_chinese_characters().getBytes("ISO8859_1"), "UTF8");	
				employee_personal_details.setName_in_chinese_characters(convertedValue);	
			}
			session.evict(employee_personal_details);
			session.merge(employee_personal_details);
			if(employee.getEmployeePersonalDetails().getPhoto()!=null && !employee.getEmployeePersonalDetails().getPhoto().isEmpty()){
				String modifiedFileName =  employee_id+"_"+employee_personal_details.getPhoto();
			    templateFilenames.put(employee.getEmployeePersonalDetails().getPhoto(), modifiedFileName);
			    fileFolder.put(employee.getEmployeePersonalDetails().getPhoto(), "photo");
			}
			}
			if (employee.getEmployeeAssignmentDetails() != null) {
				employee.getEmployeeAssignmentDetails().setEmployee_id(employee_id);
				session.evict(employee.getEmployeeAssignmentDetails());
				session.merge(employee.getEmployeeAssignmentDetails());	
				}
			if (employee.getIndianEmployeesInsuranceDetails() != null) {
				employee.getIndianEmployeesInsuranceDetails().setEmployee_id(employee_id);
				session.evict(employee.getIndianEmployeesInsuranceDetails());
				session.merge(employee.getIndianEmployeesInsuranceDetails());
				Indian_Employees_Insurance_Details indianInsurance = employee.getIndianEmployeesInsuranceDetails();
				List<Indian_Employee_Family_Member> members = indianInsurance.getIndianEmployeeFamilyMember();
				if (members != null) {
					Iterator<Indian_Employee_Family_Member> itr = indianInsurance.getIndianEmployeeFamilyMember()
							.iterator();
					while (itr.hasNext()) {
						Indian_Employee_Family_Member indianFamilyMembers = itr.next();
						indianFamilyMembers.setEmployee_id(employee_id);
						session.evict(indianFamilyMembers);
						session.merge(indianFamilyMembers);
					}
				}
			}
			if (employee.getSingaporeEmployeeInsuranceDetails() != null) {
				employee.getSingaporeEmployeeInsuranceDetails().setEmployee_id(employee_id);
				session.evict(employee.getSingaporeEmployeeInsuranceDetails());
				session.merge(employee.getSingaporeEmployeeInsuranceDetails());
				Singapore_Employee_Insurance_Details singaporeInsurance = employee
						.getSingaporeEmployeeInsuranceDetails();
				List<Singapore_Employee_Family_Member> members = singaporeInsurance.getSingaporeEmployeeFamilyMember();
				if (members != null) {
					Iterator<Singapore_Employee_Family_Member> itr = singaporeInsurance
							.getSingaporeEmployeeFamilyMember().iterator();
					while (itr.hasNext()) {
						Singapore_Employee_Family_Member singaporeFamilyMembers = itr.next();
						singaporeFamilyMembers.setEmployee_id(employee_id);
						session.evict(singaporeFamilyMembers);
						session.merge(singaporeFamilyMembers);
					}
				}
			}
			if (employee.getEmployeeTermsAndConditions() != null) {
				employee.getEmployeeTermsAndConditions().setEmployee_id(employee_id);
				session.evict(employee.getEmployeeTermsAndConditions());
				session.merge(employee.getEmployeeTermsAndConditions());
				}
			if (employee.getEmployeeBankDetails() != null) {
				employee.getEmployeeBankDetails().setEmployee_id(employee_id);
				session.evict(employee.getEmployeeBankDetails());
				session.merge(employee.getEmployeeBankDetails());
				if(employee.getEmployeeBankDetails().getCancelled_cheque_path()!=null && !employee.getEmployeeBankDetails().getCancelled_cheque_path().isEmpty() ){
					String modifiedFileName =  employee_id+"_"+employee.getEmployeeBankDetails().getCancelled_cheque_path();
				    templateFilenames.put(employee.getEmployeeBankDetails().getCancelled_cheque_path(), modifiedFileName);
				    fileFolder.put(employee.getEmployeeBankDetails().getCancelled_cheque_path(), "cancelledcheque");
				}
				}
			/*if (employee.getLeavesEligibility() != null && !employee.getLeavesEligibility().isEmpty()) {
				Iterator<Leave_Eligibility_Details> itr = employee.getLeavesEligibility().iterator();
				//Employee_Leave_Data leavedata = leaveserv.getEmployeeLeaveData(employee_id);
				while (itr.hasNext()) {
					Leave_Eligibility_Details leaveEligibility = itr.next();
					leaveEligibility.setEmployee_id(employee_id);
					session.evict(leaveEligibility);
					session.merge(leaveEligibility);		
					}
				String leaveUsageDetails = "select * from  Leave_Usage_Details where employee_id = :employee_id";
				java.util.List leaveUsageDetailsList = session.createSQLQuery(leaveUsageDetails)
						.addEntity(Leave_Usage_Details.class).setParameter("employee_id", employee_id).list();			
				if(leaveUsageDetailsList == null || leaveUsageDetailsList.isEmpty()) {
				Timestamp adoj = employee.getEmployeePersonalDetails().getActual_date_of_joining();
				List<Leave_Usage_Details> accruedLeaveusage = leaveserv.newEmployeeLeaveUsage(adoj,employee.getEmployeeWorkPermitDetails().getWork_country(),employee.getLeavesEligibility());
				for(Leave_Usage_Details lvusage : accruedLeaveusage){
					lvusage.setEmployee_id(employee_id);
					session.save(lvusage);
				}
				}
			}*/
			  if (employee.getEmployeeAppraisalDetails() != null) {
			  employee.getEmployeeAppraisalDetails().setEmployee_id(employee_id ); 
			 /* Timestamp t1 = employee.getEmployeePersonalDetails().getActual_date_of_joining(); 
					  Calendar cal = Calendar.getInstance();
					  cal.setTime(t1);
					  cal.add(Calendar.MONTH, 10);
					  t1.setTime(cal.getTime().getTime()); */
			  session.evict(employee.getEmployeeAppraisalDetails());
				session.merge(employee.getEmployeeAppraisalDetails());
				}
			 
			if (employee.getEmployeeContactAddressDetails() != null) {
				Iterator<Contact_Address_Details> itr = employee.getEmployeeContactAddressDetails().iterator();
				while (itr.hasNext()) {
					Contact_Address_Details employeeContactAddress = itr.next();
					employeeContactAddress.setEmployee_id(employee_id);
					session.evict(employeeContactAddress);
					session.merge(employeeContactAddress);
				}
			}
			if (employee.getEmergencyContactDetails() != null) {
				employee.getEmergencyContactDetails().setEmployee_id(employee_id);
				session.evict(employee.getEmergencyContactDetails());
				session.merge(employee.getEmergencyContactDetails());
				}
			if (employee.getEmployeeIdentificationDetails() != null) {
				Iterator<Employee_Identification_Details> itr = employee.getEmployeeIdentificationDetails().iterator();
				while (itr.hasNext()) {
					Employee_Identification_Details employeeIdentificationDetails = itr.next();
					employeeIdentificationDetails.setEmployee_id(employee_id);
					session.evict(employeeIdentificationDetails);
					session.merge(employeeIdentificationDetails);
					}
			}
			if(employee.getDeleteSingaporeEmployeeFamilyMember()!=null){
					Iterator<DeleteSingaporeEmployeeFamilyMember> itr = employee.getDeleteSingaporeEmployeeFamilyMember().iterator();
					Singapore_Employee_Family_Member singmember = new Singapore_Employee_Family_Member();
					while (itr.hasNext()) {
						DeleteSingaporeEmployeeFamilyMember deleteSingaporeEmployeeFamilyMember = itr.next();
						int memberid = deleteSingaporeEmployeeFamilyMember.getSingapore_employee_family_member_id();
						singmember.setSingapore_employee_family_member_id(memberid);
						session.delete(singmember);
					}
					
				}
			if(employee.getDeleteIndianEmployeeFamilyMember()!=null){
				Iterator<DeleteIndianEmployeeFamilyMember> itr = employee.getDeleteIndianEmployeeFamilyMember().iterator();
				Indian_Employee_Family_Member indmember = new Indian_Employee_Family_Member();
				while (itr.hasNext()) {
					DeleteIndianEmployeeFamilyMember deleteIndianEmployeeFamilyMember = itr.next();
					int memberid = deleteIndianEmployeeFamilyMember.getIndian_employee_family_member_id();
					indmember.setIndian_employee_family_member_id(memberid);
					session.delete(indmember);
				}
				
			}
			if (employee.getEmployeeWorkPermitDetails() != null) {
				employee.getEmployeeWorkPermitDetails().setEmployee_id(employee_id);
				session.evict(employee.getEmployeeWorkPermitDetails());
				session.merge(employee.getEmployeeWorkPermitDetails());
				}
			if (employee.getSowEmployeeAssoc() != null) {
				Iterator<Sow_Employee_Association> itr = employee.getSowEmployeeAssoc().iterator();
				while (itr.hasNext()) {
					Sow_Employee_Association sowEmpAssoc = itr.next();
					sowEmpAssoc.setEmployeeId(employee_id);
					session.evict(sowEmpAssoc);
					session.merge(sowEmpAssoc);
				}
			}
			if (employee.getSowCtcBreakup() != null) {
				Iterator<Sow_Ctc_Breakup> itr = employee.getSowCtcBreakup().iterator();
				int count=0;
				int groupId = 0;
				int renewGroupId = 0;
				while (itr.hasNext()) {
					Sow_Ctc_Breakup sowCtcBreakup = itr.next();
					if(groupId == 0){
					if(sowCtcBreakup.getGroupId() != 0){
					groupId = sowCtcBreakup.getGroupId();
					}else{
						for(Sow_Ctc_Breakup brkp : employee.getSowCtcBreakup()){
							if(brkp.getGroupId() != 0){
								groupId = brkp.getGroupId();
								break;
							}
						}
					}
					}
					if(count==0){
					if("renewal".equalsIgnoreCase(sowCtcBreakup.getStatus())){
						Employee emp = get(employee_id);
						if (emp.getSowCtcBreakup() != null) {
							Iterator<Sow_Ctc_Breakup> itr1 = emp.getSowCtcBreakup().iterator();
							while (itr1.hasNext()) {
								Sow_Ctc_Breakup sowCtcBreakupPrev = itr1.next();
								sowCtcBreakupPrev.setStatus("inactive");
								session.update(sowCtcBreakupPrev);
								renewGroupId = sowCtcBreakupPrev.getGroupId();
							}
						}	
						employee.setDeletedsowCtcBreakup(null);
					}
					count++;
					}
					if(renewGroupId==0){
					sowCtcBreakup.setGroupId(groupId);
					sowCtcBreakup.setStatus("active");
					sowCtcBreakup.setEmployeeId(employee_id);
				//	sowCtcBreakup.setOfferId(employee.getEmployeeOfferDetails().getOffer_id());
					session.evict(sowCtcBreakup);
					session.merge(sowCtcBreakup);
					}else{
					int	renGroupId = renewGroupId;
						sowCtcBreakup.setSowCtcBreakupId(0);
						sowCtcBreakup.setGroupId(renGroupId+1);
						sowCtcBreakup.setStatus("active");
						sowCtcBreakup.setEmployeeId(employee_id);
						sowCtcBreakup.setOfferId(0);
						session.save(sowCtcBreakup);
					}
				}
			}
			if(employee.getDeletedsowCtcBreakup()!=null ){
				Iterator<Sow_Ctc_Breakup> itr = employee.getDeletedsowCtcBreakup().iterator();
				while (itr.hasNext()) {
					Sow_Ctc_Breakup sow_Ctc_Breakup = itr.next();
					session.delete(sow_Ctc_Breakup);
				}
			}
			Map<String, MultipartFile> files = null;
			files = request.getFileMap();
			if (files.size() > 0) {			
				FilecopyStatus status = Utils.copyFiles(request, templateFilenames, fileFolder);
			copied_with_success = status.getCopied_with_success();
			}
			transaction.commit();
			try{
				if(employee.getEmployeeExitCheck().isEmployeeexit_changed() || employee.getEmployeeExitCheck().isEmployeerelievingdate_changed()){
				SimpleMailMessage Email = new SimpleMailMessage();
				String[] cc = null;				
				/*if("Singapore".equalsIgnoreCase(employee.getEmployeeWorkPermitDetails().getWork_country())){
					String picklist_name = employee.getEmployeeAssignmentDetails().getAccount_manager();
					String Query = "SELECT helius_email_id from pickllistNameAndEmployeeNameAssoc where picklist_name = :picklist_name ";
					java.util.List emailids = session.createSQLQuery(Query).setParameter("picklist_name", picklist_name).list();
					String toSingapore = "";
					for (Object emailid : emailids) {
						toSingapore = emailid.toString();
					}
					Email.setTo(toSingapore);
					String getCC = Utils.getHapProperty("notifyExitDetailsForSingaporeEmployee-CC");
					if (getCC != null && !getCC.isEmpty()) {
						cc = getCC.split(",");
					}
				}*/
				String recruiterName = employee.getEmployeeOfferDetails().getHelius_recruiter();
				String accountManagerName = employee.getEmployeeAssignmentDetails().getAccount_manager();
				String client = employee.getEmployeeAssignmentDetails().getClient();
				String payroll = employee.getEmployeeSalaryDetails().getPayroll_entity();
				String[] picklistNames = {recruiterName,accountManagerName};				
				if("Singapore".equalsIgnoreCase(employee.getEmployeeWorkPermitDetails().getWork_country())){
					HashMap<String,String> emailids = Utils.getEmailIdFromPickllistNameAndEmployeeNameAssoc(picklistNames, session);
					Email.setTo(Utils.getHapProperty("notifyExitDetailsForSingaporeEmployee-TO"));
					String getCC = Utils.getHapProperty("notifyExitDetailsForSingaporeEmployee-CC");				
						getCC = getCC+","+emailids.get(recruiterName)+","+emailids.get(accountManagerName);
						if("DBS".equalsIgnoreCase(client) && "Helius".equalsIgnoreCase(payroll)){
							getCC = getCC + ","+Utils.getHapProperty("notifyExitDetailsForSingaporeCC-DBS-Helius");	
						}
						if("DBS".equalsIgnoreCase(client) && !"Helius".equalsIgnoreCase(payroll)){
							getCC = getCC + ","+Utils.getHapProperty("notifyExitDetailsForSingaporeCC-DBS-nonHelius");	
						}
						if(!"DBS".equalsIgnoreCase(client)){
							getCC = getCC + ","+Utils.getHapProperty("notifyExitDetailsForSingaporeCC-nonDBS");	
						}
					if (getCC != null && !getCC.isEmpty()) {	
						cc = getCC.split(",");
					}
				}
				if("India".equalsIgnoreCase(employee.getEmployeeWorkPermitDetails().getWork_country())){
					Email.setTo(Utils.getHapProperty("notifyExitDetailsForIndiaEmployee-TO"));
					String getCC = Utils.getHapProperty("notifyExitDetailsForIndiaEmployee-CC");
					if (getCC != null && !getCC.isEmpty()) {
						cc = getCC.split(",");
					}
				}				
				if("Thailand".equalsIgnoreCase(employee.getEmployeeWorkPermitDetails().getWork_country())){
					Email.setTo(Utils.getHapProperty("notifyExitDetailsForThailandEmployee-TO"));
					String getCC = Utils.getHapProperty("notifyExitDetailsForThailandEmployee-CC");
					if (getCC != null && !getCC.isEmpty()) {
						cc = getCC.split(",");
					}
				}
				Email.setCc(cc);
				Email.setSubject("Employee exit details updated for "+employee.getEmployeePersonalDetails().getEmployee_name());
				StringBuffer message = new StringBuffer();
				message.append("Hi, "+ "\n\n");
				if(employee.getEmployeeExitCheck().isEmployeeexit_changed()){
					message.append("Employee Status is updated for "+employee.getEmployeePersonalDetails().getEmployee_name()+" TO "+employee.getEmployeePersonalDetails().getEmployee_status()+ "\n\n" );
				}
				if(employee.getEmployeeExitCheck().isEmployeerelievingdate_changed()){
					message.append("Employee Exit Date is updated for "+employee.getEmployeePersonalDetails().getEmployee_name()+" TO "+employee.getEmployeePersonalDetails().getRelieving_date()+ "\n\n" );
				}
				message.append("Thanks," + "\n\n" + "HR Team," + "\n" + "Helius Technologies Pte.Ltd");
				
				Email.setText(message.toString());
			//	emailService.sendEmail(Email);
				}
				}catch (Exception e) {
					e.printStackTrace();
				}
		} catch (HibernateException e) {
			e.printStackTrace();
			Utils.deleteFiles(copied_with_success);
			throw new Throwable("Failed to update the Employee Details" + e.getCause().getMessage());
		}
		 catch (Exception e) {
				e.printStackTrace();
				Utils.deleteFiles(copied_with_success);
				throw new Throwable("Failed to update Employee Details" + e.getMessage());
			}
			finally {
				session.close();
			}
		}
	
	
	/*@Override
	public void sendEmailNotification(String jsonData) throws Throwable {
		ObjectMapper obm = new ObjectMapper();
		try {
		EmailScreen emailJson = obm.readValue(jsonData, EmailScreen.class);
		String st = emailJson.getCc();
		String[] cc = null;
		if (st != null && !st.isEmpty()) {
			cc = st.split(";");
		}
		String to = emailJson.getTo();
		String subject = emailJson.getSubject();
		String text = emailJson.getText();
		List<String> urlList = new ArrayList<String>();
		emailService.sendMessageWithAttachment(to, cc, subject, text, urlList);		
		}  catch (MessagingException e) {
			e.printStackTrace();
			throw new Throwable("Unable to Send Email notification " + e.getMessage());
		}catch (Exception e) {
			e.printStackTrace();
			throw new Throwable("Unable to Send Email notification " + e.getMessage());
		}
	}*/
	
	@Override
	public void sendEmail(String jsonData, MultipartHttpServletRequest request) throws Throwable {
		try {
			Map<String, List<MultipartFile>> files = request.getMultiFileMap();
			List<File> al = new ArrayList<File>();
			if (files.size() > 0) {
				for (String key : files.keySet()) {
					List<MultipartFile> ff = files.get(key);
					for (MultipartFile multifile : ff) {
						File convFile = new File(multifile.getOriginalFilename());
						FileOutputStream fos = new FileOutputStream(convFile);
						fos.write(multifile.getBytes());
						al.add(convFile);
						fos.close();
					}
				}
			}
			ObjectMapper obm = new ObjectMapper();
			EmailScreen emailJson = obm.readValue(jsonData, EmailScreen.class);
			String st = emailJson.getCc();
			String[] cc = null;
			if (st != null && !st.isEmpty()) {
				cc = st.split(";");
			}
			String bccs = emailJson.getBcc();
			String[] bcc = null;
			if (bccs != null && !bccs.isEmpty()) {
				bcc = st.split(";");
			}
			String to = emailJson.getTo();
			String subject = emailJson.getSubject();
			String text = emailJson.getText();
			emailService.sendEmailWithAttachment(to, cc,bcc, subject, text, al);
		} catch (Exception e) {
			logger.error("Failed to send email to user "+jsonData,e);
			throw new Throwable(e.getMessage());
		}
	}
	
	@Override
	public void saveOrUpdateHelpVideo(String helpdata, MultipartHttpServletRequest request) throws Throwable {
		Session session = null;
		Transaction transaction = null;
		Map<String, String> templateFilenames = new HashMap<String, String>();
		ObjectMapper om = new ObjectMapper();
		try {
			session = sessionFactory.openSession();
			transaction = session.beginTransaction();
			Help_Videos	help = om.readValue(helpdata, Help_Videos.class);
			if (help.getHelp_videos_id()==0) {
				session.save(help);
			}else{
				session.evict(help);
				session.merge(help);
			}
				Map<String, MultipartFile> files = null;
				files = request.getFileMap();
				if (files.size() > 0) {		
				templateFilenames.put(help.getVideos_url(), help.getVideos_url());
				FilecopyStatus status = Utils.copyFiles(request, templateFilenames, "hapHelpVideo");
				copied_with_success = status.getCopied_with_success();
				}
			transaction.commit();
		} catch (HibernateException e) {
			e.printStackTrace();
			Utils.deleteFiles(copied_with_success);
			throw new Throwable(e.getCause().getMessage());	
		} catch (Exception e) {
			e.printStackTrace();
			Utils.deleteFiles(copied_with_success);
			throw new Throwable(e.getMessage());
		} finally {
			session.close();
		}
	}
	
	public void deleteHelpVideo(String help_videos_id)throws Throwable{
		Session session = null;
		Transaction transaction = null;
		List<Help_Videos> help_Videos = null;
		try {
		session = sessionFactory.openSession();
		transaction = session.beginTransaction();
		help_Videos = getAllHelpVideos();
		Help_Videos videoObj = null;
		for(Help_Videos help : help_Videos){
			int videos_id = Integer.parseInt(help_videos_id);
			if(videos_id == help.getHelp_videos_id()){
				videoObj = help;
				 break;
			}
		}
		String url = null;
			if (videoObj.getVideos_url() != null && !"".equalsIgnoreCase(videoObj.getVideos_url())) {
				url = Utils.getProperty("fileLocation") + File.separator + "hapHelpVideo" + File.separator + videoObj.getVideos_url();
			}
			if(videoObj != null){
			session.delete(videoObj);
			transaction.commit();
			}
			File file = new File(url);
			if(file.exists()){
			file.delete();
			}
	} catch (HibernateException e) {
		e.printStackTrace();
		Utils.deleteFiles(copied_with_success);
		throw new Throwable(e.getCause().getMessage());	
	} finally {
		session.close();
	}
	}

	
	@Override
	public List<Help_Videos> getAllHelpVideos() throws Throwable {
		Session session = null;
		List<Help_Videos> help_Videos = new ArrayList<Help_Videos>();
		try {
			session = sessionFactory.openSession();
			String query = "select * from Help_Videos";
			help_Videos = session.createSQLQuery(query).addEntity(Help_Videos.class).list();
		} catch (Exception e) {
			e.printStackTrace();
			throw new Throwable("Failed to fetch help videos");
		} finally {
			session.close();
		}
		return help_Videos;
	}
	
	@Override
	public ResponseEntity<byte[]> getHelpVideoFIle(int help_videos_id) {
		List<Help_Videos> help_Videos = null;
		try {
			help_Videos = getAllHelpVideos();
		} catch (Throwable e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String videoURL = null;
		for(Help_Videos help : help_Videos){
			if(help_videos_id == help.getHelp_videos_id()){
				 videoURL = help.getVideos_url();
				 break;
			}
		}
		String url = null;
			if (videoURL != null && !"".equalsIgnoreCase(videoURL)) {
				url = Utils.getProperty("fileLocation") + File.separator + "hapHelpVideo" + File.separator + videoURL;
			}
		byte[] files = null;
		FileInputStream fi = null;
		try {
			fi = new FileInputStream(url);
			files = IOUtils.toByteArray(fi);
			fi.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		}
		ResponseEntity<byte[]> responseEntity = new ResponseEntity<byte[]>(files, HttpStatus.OK);
		return responseEntity;
	}
	
	/* public List audit(){
		Session session = null;
		String className ="Employee_Personal_Details";
    Class classTemp = null;
	List empchecklist = new ArrayList();

	List ecklist=null;
		try {
		 session = sessionFactory.openSession();
		AuditReader reader = AuditReaderFactory.get(session);
		AuditQuery query = reader.createQuery()
				  .forRevisionsOfEntity(Employee_Personal_Details.class, true, true);
		 ecklist =  query.getResultList();
				}catch (Exception e) {
			e.printStackTrace();		
			}finally {
				session.close();
			}	
		return ecklist;
	}*/
	/*public BaseEntity getPreviousVersion(BaseEntity entity, int current_rev) {

	    AuditReader reader = AuditReaderFactory.get(entityManager);

	    Number prior_revision = (Number) reader.createQuery()
	            .forRevisionsOfEntity(entity.getClass(), false, true)
	            .addProjection(AuditEntity.revisionNumber().max())
	            .add(AuditEntity.id().eq(entity.getId()))
	            .add(AuditEntity.revisionNumber().lt(current_rev))
	            .getSingleResult();

	    if (prior_revision != null)
	        return reader.find(entity.getClass(), entity.getId(), prior_revision);
	    else
	        return null;
	}
	*/
	
	private List<LeaveUtilization> getConvertLieuTypeInOffInLieu(List leaveUtilizedList) {
		double lieuTypeLeave = 0;
		Map<String, LeaveUtilization> lieumap = null;
		Map<String, LeaveUtilization> OffInLieuMap = null;
		List<LeaveUtilization> lieuList = null;
		logger.info("entering getConvertLieuTypeInOffInLieu at " + System.currentTimeMillis());
		//System.out.println("entering getConvertLieuTypeInOffInLieu at " + System.currentTimeMillis()) ;
		try {
			lieumap = new HashMap<>();
			lieuList = new ArrayList<>();
			OffInLieuMap = new HashMap<>();
			if (leaveUtilizedList != null && !leaveUtilizedList.isEmpty()) {
				for (Object object : leaveUtilizedList) {
					LeaveUtilization leaveUtilization = new LeaveUtilization();
					leaveUtilization = (LeaveUtilization) object;
					if (leaveUtilization.getLeaveType().equalsIgnoreCase("Lieu")) {
						lieumap.put(leaveUtilization.getLeaveType(), leaveUtilization);
						
					}
					if (leaveUtilization.getLeaveType().equalsIgnoreCase("Off In Lieu")) {
						OffInLieuMap.put(leaveUtilization.getLeaveType(), leaveUtilization);
					}
				}
				for (Object object : leaveUtilizedList) {
					LeaveUtilization leaveUtilization = new LeaveUtilization();
					leaveUtilization = (LeaveUtilization) object;
					if (leaveUtilization.getLeaveType().equalsIgnoreCase("Off In Lieu")) {
						double offinlieuTypeLeave = leaveUtilization.getUtilizedLeave();
						if (lieumap != null && !lieumap.isEmpty()) {
							for (LeaveUtilization leave : lieumap.values()) {
								lieuTypeLeave = leave.getUtilizedLeave();
							}
							leaveUtilization.setUtilizedLeave(offinlieuTypeLeave + lieuTypeLeave);
						}
					} else {
						if (leaveUtilization.getLeaveType().equalsIgnoreCase("Lieu")) {
							
							if (OffInLieuMap == null || OffInLieuMap.isEmpty()) {
								leaveUtilization.setLeaveType("Off In Lieu");
							}
						}
					}
					if (!leaveUtilization.getLeaveType().equalsIgnoreCase("Lieu")) {
						lieuList.add(leaveUtilization);
					}
				}
				//lieuList.stream().forEach(System.out::println);
			}
			logger.info("leaving getConvertLieuTypeInOffInLieu at " + System.currentTimeMillis());
			//System.out.println("leaving getConvertLieuTypeInOffInLieu at " + System.currentTimeMillis()) ;

		} catch (Exception e) {
			throw e;
		}
		return lieuList;
	}

	@Override
	public ResponseEntity<byte[]> getDownloadForm16(String empId, String filePath) throws Throwable {
		Session session = null;
		byte[] files = null;
		String check = Utils.awsCheckFlag();
		try {
			session = sessionFactory.openSession();
			LocalDate now = LocalDate.now();
			String url = null;

				if (filePath == null || !filePath.isEmpty() && "yes".equalsIgnoreCase(check)) {
					url = "form16" + "/" + "form16_" + now.getYear() + "/" + empId  + "_"
							+ filePath;
					try {
						files = Utils.downloadFileByAWSS3Bucket(url);
					} catch (Exception e) {
						return new ResponseEntity<byte[]>(HttpStatus.NOT_FOUND);
					}
				}else {
					return new ResponseEntity<byte[]>(HttpStatus.NO_CONTENT);

				}

		} catch (Throwable e) {
			logger.error("failed to download file  - " + empId, e.getMessage());
			return new ResponseEntity<byte[]>(HttpStatus.INTERNAL_SERVER_ERROR);
		} finally {
			session.close();
		}
		return new ResponseEntity<byte[]>(files, HttpStatus.OK);
	}
}
