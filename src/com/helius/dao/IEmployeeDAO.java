/**
 * 
 */
package com.helius.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.helius.entities.Employee;
import com.helius.entities.Employee_Assignment_Details;
import com.helius.entities.Employee_Offer_Details;
import com.helius.entities.Help_Videos;





/**
 * @author Tirumala
 * 22-Feb-2018
 */
public interface IEmployeeDAO {
	public Employee get(String employeeid);
	public ResponseEntity<byte[]> getEmployeeFiles(String employee_id,String filetype);	
	public ResponseEntity<byte[]> getOfferFiles(String offerid,String filetype);	
	public Employee getOfferDetails(String offerid);
	public List<Employee_Offer_Details> getOfferDetailsByName(String offerName,String type);
	public void empUpdate(Employee emp, MultipartHttpServletRequest request) throws Throwable;
	public void offUpdate(Employee emp, MultipartHttpServletRequest request) throws Throwable;
	public void sendEmailNotification(String jsonData) throws Throwable;
	ResponseEntity<byte[]> getHelpVideoFIle(int help_videos_id);
	List<Help_Videos> getAllHelpVideos() throws Throwable;
	public void saveOrUpdateHelpVideo(String help, MultipartHttpServletRequest request) throws Throwable;
	public void deleteHelpVideo(String help_videos_id)throws Throwable;
	}
