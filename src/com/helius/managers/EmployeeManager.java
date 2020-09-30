/**
 * 
 */
package com.helius.managers;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import com.helius.dao.IEmployeeDAO;
import com.helius.entities.Employee;
import com.helius.entities.Employee_Leave_Data;
import com.helius.entities.Employee_Offer_Details;
import com.helius.entities.Employee_Timesheet_Status;
import com.helius.entities.Help_Videos;
import com.helius.utils.Status;

/**
 * @author Tirumala
 * 23-Feb-2018
 */
public class EmployeeManager {

	IEmployeeDAO employeeDAO = null;
	
	/**
	 * @return the employeeDAO
	 */
	public IEmployeeDAO getEmployeeDAO() {
		return employeeDAO;
	}
	/**
	 * @param employeeDAO the employeeDAO to set
	 */
	public void setEmployeeDAO(IEmployeeDAO employeeDAO) {
		this.employeeDAO = employeeDAO;
	}
	/**
	 * 
	 */
	public EmployeeManager() {
		// TODO Auto-generated constructor stub
	}
	public Employee getEmployee(String employeeid) {
		Employee emp = null;
		try {
			emp = employeeDAO.get(employeeid);
		} catch (Throwable e) {
			return emp;
		}
		return emp;
	}
	
	public Employee_Leave_Data getEmployeeLeaveData(String employee_id) throws Throwable {
		Employee_Leave_Data employeeLeaveData = null;
		try {
			employeeLeaveData = employeeDAO.getEmployeeLeaveData(employee_id);
		} catch (Throwable e) {
			throw new Throwable("Failed to fetch Employee Leave Details");
		}
		return employeeLeaveData;
	}
	
	public Status sendEmailService(String jsondata,MultipartHttpServletRequest request)
			throws Throwable {
		try {
			employeeDAO.sendEmail(jsondata,request);
		} catch (Throwable e) {
			throw new Throwable(" Failed to send Email ");
		}
		return new Status(true, "Email Sent Successfully.!");
	}
	
	public ResponseEntity<byte[]> getEmployeeFiles(String employee_Id, String filetype) {
		ResponseEntity<byte[]> res = null;
		try {
			 res =	employeeDAO.getEmployeeFiles(employee_Id,filetype);
		} catch (Throwable e) {
				return res;
		}
		return res;
	}
	
	public ResponseEntity<byte[]> getOfferFiles(String offerid, String filetype) {
		ResponseEntity<byte[]> response = null;
		try {
			response =	employeeDAO.getOfferFiles(offerid,filetype);
		} catch (Throwable e) {
				return response;
		}
		return response;
	}
	
	public Status empUpdate(Employee emp, MultipartHttpServletRequest request) {
		try {
			employeeDAO.empUpdate(emp, request);
		} catch (Throwable e) {
			return new Status(false,"Employee Details Not Updated " + e.getMessage());
		}
		return new Status(true, "Employee Details Updated Successfully.!");
	}
	
	public Status offUpdate(Employee emp, MultipartHttpServletRequest request) {
		try {
			employeeDAO.offUpdate(emp, request);
		} catch (Throwable e) {
			return new Status(false,"Offer Details Not Updated ");
		}
		return new Status(true, "Offer Details Updated Successfully.!");
	}
	
	public Employee getOfferbyID(String offerid) {
		Employee offerdetailsById = null;
		try {
			offerdetailsById = employeeDAO.getOfferDetails(offerid);
		} catch (Throwable e) {
			return offerdetailsById;
		}
		return offerdetailsById;
	}
	
	
	public List<Help_Videos> getAllHelpVideos() throws Throwable {
		List<Help_Videos> help_Videos = null;
		try {
			help_Videos = employeeDAO.getAllHelpVideos();
		} catch (Throwable e) {
			throw new Throwable("Failed to fetch  Help video details");
		}
		return help_Videos;
	}

	public List<Employee_Offer_Details> getOfferbyName(String offerName,String type) {
		List<Employee_Offer_Details> offerdetailsByName = null;
		try {
			offerdetailsByName = employeeDAO.getOfferDetailsByName(offerName,type);
		} catch (Throwable e) {
			return offerdetailsByName;
		}
		return offerdetailsByName;
	}
	public ResponseEntity<byte[]> getHelpFile(int help_videos_id) {
		ResponseEntity<byte[]> res = null;
		try {
			 res =	employeeDAO.getHelpVideoFIle(help_videos_id);
		} catch (Throwable e) {
				return res;
		}
		return res;
	}
	
	public Status saveOrUpdateHelpVideo(String help, MultipartHttpServletRequest request) {
			try {
				employeeDAO.saveOrUpdateHelpVideo(help, request);
			} catch (Throwable e) {
				return new Status(false,"Failed to save video file");
			}
			return new Status(true, "video saved successfully");
		}
	public Status deleteHelpVideo(String help_videos_id) {
		try {
			employeeDAO.deleteHelpVideo(help_videos_id);
		} catch (Throwable e) {
			return new Status(false,"Failed to delete video file");
		}
		return new Status(true, "video deleted successfully");	
	}
}
