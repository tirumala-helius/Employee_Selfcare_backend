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
import com.helius.entities.Employee_Offer_Details;
import com.helius.entities.Employee_Personal_Details;
import com.helius.entities.Employee_Salary_Details;
import com.helius.entities.Employee_Terms_And_Conditions;
import com.helius.entities.Employee_Timesheet_Status;
import com.helius.entities.Employee_Work_Permit_Details;
import com.helius.entities.Help_Videos;
import com.helius.entities.Indian_Employee_Family_Member;
import com.helius.entities.Indian_Employees_Insurance_Details;
import com.helius.entities.Leave_Eligibility_Details;
import com.helius.entities.Leave_Record_Details;
import com.helius.entities.Leave_Usage_Details;
import com.helius.entities.Leaves_Eligibility_defined_By_Client_Policy;
import com.helius.entities.Singapore_Employee_Family_Member;
import com.helius.entities.Singapore_Employee_Insurance_Details;
import com.helius.entities.Sow_Ctc_Breakup;
import com.helius.entities.Sow_Employee_Association;
import com.helius.entities.Timesheet_Email;
import com.helius.entities.Work_Permit_Master;
import com.helius.service.EmailService;
import com.helius.service.UserServiceImpl;
import com.helius.utils.FilecopyStatus;
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
			// Leave Eligibility details
			String leaveEligibility = "select * from  Leave_Eligibility_Details where employee_id = :employee_id AND year = :year";
			List<Leave_Eligibility_Details> eligibilityList = session.createSQLQuery(leaveEligibility)
					.addEntity(Leave_Eligibility_Details.class).setParameter("employee_id", employee_id).setParameter("year",year).list();
			List<Leave_Eligibility_Details> leave_Eligibility_DetailsList = new ArrayList<Leave_Eligibility_Details>();
			if (!eligibilityList.isEmpty()) {
				for (Leave_Eligibility_Details leave_Eligibility : eligibilityList) {
					leave_Eligibility_DetailsList.add(leave_Eligibility);
				}
			}
			if (leave_Eligibility_DetailsList != null && !leave_Eligibility_DetailsList.isEmpty()) {
				employeeLeaveData.setLeavesEligibility(leave_Eligibility_DetailsList);
			}
			
			String leaveUsageDetails = "select * from  Leave_Usage_Details where employee_id = :employee_id AND YEAR(usageMonth)= :year ORDER BY usageMonth DESC";
			java.util.List leaveUsageDetailsList = session.createSQLQuery(leaveUsageDetails)
					.addEntity(Leave_Usage_Details.class).setParameter("employee_id", employee_id).setParameter("year",year).list();
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
			
			String recordQuery = "select * from  Leave_Record_Details where employee_id = :employee_id AND YEAR(leaveMonth) = :year ORDER BY leaveMonth DESC";
			java.util.List recordQueryList = session.createSQLQuery(recordQuery)
					.addEntity(Leave_Record_Details.class).setParameter("employee_id", employee_id).setParameter("year", year).list();
			ArrayList<Leave_Record_Details> leave_Record_DetailsList = new ArrayList<Leave_Record_Details>();
			Leave_Record_Details leaveRecordDetails = null;
			if(leaveUsageDetailsList != null){
				for(Object obj : recordQueryList){
					leaveRecordDetails = (Leave_Record_Details)obj;
					leave_Record_DetailsList.add(leaveRecordDetails);
				}
				if(leave_Record_DetailsList != null && !leave_Record_DetailsList.isEmpty()){
				employeeLeaveData.setLeaveRecordDetails(leave_Record_DetailsList);
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
	
}
