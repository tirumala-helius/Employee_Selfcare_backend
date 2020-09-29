/**
 * 
 */
package com.helius.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.helius.entities.Employee;
import com.helius.entities.Employee_Leave_Data;
import com.helius.entities.Employee_Offer_Details;
import com.helius.entities.Employee_Timesheet_Status;
import com.helius.entities.Help_Videos;
import com.helius.managers.EmployeeManager;
import com.helius.service.EmailService;
import com.helius.utils.Status;
import com.helius.utils.Utils;

/**
 * @author Tirumala 23-Feb-2018
 * 
 */
@RestController
public class EmployeeController {

	private org.hibernate.internal.SessionFactoryImpl sessionFactory;

	
	public org.hibernate.internal.SessionFactoryImpl getSessionFactory() {
		return sessionFactory;
	}


	public void setSessionFactory(org.hibernate.internal.SessionFactoryImpl sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	@Autowired
	private EmailService emailService;
	
	private final String fileLocation = "C:" + File.separator + "Users" + File.separator + "HELIUS" + File.separator
			+ "Documents";
	private final String noPhoto = "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";
	@Autowired
	ApplicationContext context;
	@Autowired
	EmployeeManager employeemanager;
	@Autowired
	Status status;
	/**
	 * 
	 */
	public EmployeeController() {
		// TODO Auto-generated constructor stub
	}
	
	@CrossOrigin
	@RequestMapping(value = "employee/", method = RequestMethod.GET,produces = "application/json;charset=utf-8")
	public ResponseEntity<String> getEmployee(@RequestParam String employeeid) {
		// TODO call EmployeeManager to get the employee details for the
		// employee id and create json and send it back
		// EmployeeManager employeemanager = new EmployeeManager();
		boolean result = Utils.authenticateUrl(employeeid);
		if(!result){
			return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		EmployeeManager employeemanager = (EmployeeManager) context.getBean("employeeManager");
		Employee employee = employeemanager.getEmployee(employeeid);
		ObjectMapper om = new ObjectMapper();
		String employeejson = "";
		String employeejson1 = "";
		if (employee == null) {			
		status.setMessage("Unable to process your Request. Please contact HR !");
		return new ResponseEntity<String>(status.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
		}
	//	om.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
		//SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
		//om.setDateFormat(df);
		try {
			employeejson1 = om.writeValueAsString(employee);
			employeejson = employeejson1.replaceAll(":null", ":\"-\"").replaceAll(":\"\"", ":\"-\"");
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		String url = employee.getEmployeePersonalDetails().getPhoto();
		FileInputStream fi = null;
		try {
			if (url != null && !"".equalsIgnoreCase(url)) {
				String photoUrl=Utils.getProperty("fileLocation") + File.separator + "photo" + File.separator +employeeid+"_"+url;
				fi = new FileInputStream(photoUrl);
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		byte[] imgfile1 = null;
		try {
			if (fi != null) {
				imgfile1 = IOUtils.toByteArray(fi);
				fi.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		String base64String = null;
		if (imgfile1 != null) {
			base64String = Base64.encodeBase64String(imgfile1);
		}
		String totaljson = null;
		if (base64String == null) {
			totaljson = "{\"image\": " + "\"" + noPhoto + "\", \"employee\":" + employeejson + "}";
		} else {
			totaljson = "{\"image\": " + "\"" + base64String + "\", \"employee\":" + employeejson + "}";
		}
		return new ResponseEntity<String>(totaljson,HttpStatus.OK);
	}
	
	@CrossOrigin
	@RequestMapping(value = "getEmployeeLeaveData", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<String> getEmployeeLeaveData(@RequestParam String employee_id) {
		ResponseEntity<String> response = null;
		try{
			Employee_Leave_Data employeeLeaveData = employeemanager.getEmployeeLeaveData(employee_id);
		if (employeeLeaveData != null) {
			ObjectMapper om = new ObjectMapper();
			String result = om.writeValueAsString(employeeLeaveData);
			response = new ResponseEntity<String>(result, HttpStatus.OK);
		} else {
			response = new ResponseEntity<String>(HttpStatus.NO_CONTENT);
		}}catch (Exception e) {
			return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Throwable e) {
			return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return response;
	}
	
	@CrossOrigin
	@RequestMapping(value = "getOfferByID", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
	public @ResponseBody String getOfferbyID(@RequestParam String offerid) {
		EmployeeManager employeemanager = (EmployeeManager) context.getBean("employeeManager");
		Employee empoffer = employeemanager.getOfferbyID(offerid);
		ObjectMapper om = new ObjectMapper();
			String empofferjson = "";
			String empofferjson1 = "";
			try {
				empofferjson = om.writeValueAsString(empoffer);
				empofferjson1 = empofferjson.replaceAll(":null", ":\"-\"").replaceAll(":\"\"", ":\"-\"");		
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
			String	totaljson = "{\"employeeOffer\":" + empofferjson1 + "}";
			return totaljson;
	}
	
	@CrossOrigin
	@RequestMapping(value = "getOfferByName", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody String getOfferByName(@RequestParam String offerName,@RequestParam String type) {
		ObjectMapper om = new ObjectMapper();
		String empofferjson = "";
		EmployeeManager employeemanager = (EmployeeManager) context.getBean("employeeManager");
		List<Employee_Offer_Details> empoffer = employeemanager.getOfferbyName(offerName,type);
		if(empoffer.size()==0){
		status.setMessage("Unable to find record with this Name please check the Name.");
		return "{\"response\":\"" + status.getMessage() + "\"}";
		}
			try {
				empofferjson = om.writeValueAsString(empoffer);
			//	empofferjson1 = empofferjson.replaceAll(":null", ":\"-\"").replaceAll(":\"\"", ":\"-\"");		
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
			String	totaljson = "{\"employeeOffer\":" + empofferjson + "}";
			return empofferjson;
	}
		
	@CrossOrigin
	@RequestMapping(value = "empUpdate", method = RequestMethod.POST, consumes = { "multipart/form-data" })
	public String empUpdate(@RequestParam("model") String jsondata, MultipartHttpServletRequest request) {	
		System.out.println("employeejsondata:" + jsondata.toString());
		ObjectMapper om = new ObjectMapper();
		Employee emp = null;
		try {
			String jsondata1 = jsondata.replaceAll(":\"-\"", ":null");
			emp = om.readValue(jsondata1, Employee.class);
		}
		 catch(Exception e) {
			e.printStackTrace();
			status.setMessage("Unable to map data Invalid json ");
			return "{\"response\":\"" + status.getMessage() + "\"}";
		}
		//	EmployeeDAOImpl employeeDAOImpl=(EmployeeDAOImpl)context.getBean("employeeDAO");
		try {
		status =   employeemanager.empUpdate(emp, request);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return "{\"response\":\"" + status.getMessage() + "\"}";
	}
	
	@CrossOrigin
	@RequestMapping(value = "offUpdate", method = RequestMethod.POST, consumes = { "multipart/form-data" })
	public String offUpdate(@RequestParam("model") String jsondata, MultipartHttpServletRequest request) {	
		ObjectMapper om = new ObjectMapper();
		Employee emp = null;
		try {
			String jsondata1 = jsondata.replaceAll(":\"-\"", ":null");
			emp = om.readValue(jsondata1, Employee.class);
		}
		 catch(Exception e) {
			e.printStackTrace();
			status.setMessage("Unable to map data Invalid json ");
			return "{\"response\":\"" + status.getMessage() + "\"}";
			}
		try {
		status =   employeemanager.offUpdate(emp, request);
		} catch (Throwable e1) {
			e1.printStackTrace();
		}
		return "{\"response\":\"" + status.getMessage() + "\"}";
	}
	
	@CrossOrigin
	@RequestMapping(value = "getEmployeeFiles", method = RequestMethod.GET, produces = "multipart/form-data")
	public ResponseEntity<byte[]> getEmployeeFiles(@RequestParam String employee_id,String filetype) {
		EmployeeManager employeemanager = (EmployeeManager) context.getBean("employeeManager");
		ResponseEntity<byte[]> responseEntity = employeemanager.getEmployeeFiles(employee_id,filetype);	
		return responseEntity;
	}
	
	@CrossOrigin
	@RequestMapping(value = "getOfferFiles", method = RequestMethod.GET, produces = "multipart/form-data")
	public ResponseEntity<byte[]> getOfferFiles(@RequestParam String offerid,String filetype) {
		EmployeeManager employeemanager = (EmployeeManager) context.getBean("employeeManager");
		ResponseEntity<byte[]> responseEntity = employeemanager.getOfferFiles(offerid,filetype);
		return responseEntity;	
	}
	
	@CrossOrigin
	@RequestMapping(value = "sendPayslip", method = RequestMethod.GET, produces = "application/json")
	public String sendEmailNotification() throws Throwable {
		int j=0;
		String result = "";
		try {
			HashMap<String, String> nameID = new HashMap<String, String>();
			FileInputStream file = new FileInputStream(new File("C:/Users/HELIUS/Documents/Payslips/payslip.xlsx"));
			Workbook workbook = new XSSFWorkbook(file);
			Sheet sheet = workbook.getSheetAt(0);
			int i = 0;
			String emailId = null;
			String filename = null;
			String[] cc = null;
			String subject = "Payslip";
			String text = "Hi PFA for your Payslip";
			for (Row row : sheet) {
				if (i >= 1) {
					if (row.getCell(0) != null && !row.getCell(0).toString().isEmpty()) {
						filename = row.getCell(0).toString();
						emailId = row.getCell(1).toString();
						nameID.put(emailId, filename);
					}
					}
				i++;
				}
			if (workbook != null) {
				workbook.close();
			}
			List<String> failemail = new ArrayList<String>();
			for (Map.Entry<String, String> entry : nameID.entrySet()) {
		        System.out.println(entry.getKey() + ":" + entry.getValue());
		        List<String> al = new ArrayList<String>();
		        String url =Utils.getProperty("fileLocation") + File.separator +"payslips"+File.separator + entry.getValue()+".pdf";
		        al.add(url);
		        try{
		     //   emailService.sendMessageWithAttachment(entry.getKey(), cc, subject, text, al);
		        j++;
		        }catch(Exception e){
		        	e.printStackTrace();
			        System.out.println("Failed to send Email=="+entry.getKey() + ":" + entry.getValue());
			        failemail.add(entry.getKey());
		        }		       
		    }
			 result = "No : Emails Sussesfilly processed = "+j;
			}catch (Exception e) {
				e.printStackTrace();
			}
		return result;
	}
	
	@CrossOrigin
	@RequestMapping(value = "saveOrUpdateHelpVideo", method = RequestMethod.POST, consumes = { "multipart/form-data" })
	public String saveOrUpdateHelpVideo(@RequestParam("model") String jsondata, MultipartHttpServletRequest request) {	
		status = employeemanager.saveOrUpdateHelpVideo(jsondata, request);
		return "{\"response\":\"" + status.getMessage() + "\"}";
	}
	
	@CrossOrigin
	@RequestMapping(value = "deleteHelpVideo", method = RequestMethod.GET, produces = "application/json")
	public String deleteHelpVideo(@RequestParam String help_videos_id) {	
		status = employeemanager.deleteHelpVideo(help_videos_id);
		return "{\"response\":\"" + status.getMessage() + "\"}";
	}
	
	@CrossOrigin
	@RequestMapping(value = "getHelpFile", method = RequestMethod.GET, produces = "multipart/form-data")
	public ResponseEntity<byte[]> getHelpFile(@RequestParam String help_videos_id) {
		int video_id = Integer.parseInt(help_videos_id);
		ResponseEntity<byte[]> responseEntity = employeemanager.getHelpFile(video_id);
		return responseEntity;
	}
	
	@CrossOrigin
	@RequestMapping(value = "getAllHapHelpVideos", method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<String> getAllHapHelpVideos() {	
		ResponseEntity<String> helpVid = null;
		try{
		List<Help_Videos> help = employeemanager.getAllHelpVideos();
		if (help != null && !help.isEmpty() ) {
			ObjectMapper om = new ObjectMapper();
			String timesheet1 = om.writeValueAsString(help);
			helpVid = new ResponseEntity<String>(timesheet1, HttpStatus.OK);
		} else {
			helpVid = new ResponseEntity<String>(HttpStatus.NO_CONTENT);
		}}catch (Exception e) {
			return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Throwable e) {
			return new ResponseEntity<String>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return helpVid;
	}
	/*public static void main(String[] args) {
		// TODO Auto-generated method stub
		// open/read the application context file
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/applicationContext.xml");
		DataSource datasource = (DataSource) ctx.getBean("dataSource");
		EmployeeController employeeController = new EmployeeController();
		String s = employeeController.getEmployee("123");
		System.out.println(s);
		try {
			// readExcel(0,2,33);
			// readNoClock();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
	}*/
	

}
