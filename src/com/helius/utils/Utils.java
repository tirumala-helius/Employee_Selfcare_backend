package com.helius.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.helius.entities.Employee;
import com.helius.entities.Workpermit_Worklocation;
import com.helius.entities.workpermit;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

public class Utils {
	public static Properties instance = null;
	public static Properties happrop = null;
    public static Properties awsprop = null;
    private static String bucketName;
	public static S3Client s3Client ;
	
	Utils(S3Client s3Client) {
		this.s3Client = s3Client;
	}
	static{
		String path = System.getProperty("jboss.server.home.dir")+"/conf/helius_hcm_ssp.properties";
		//String path1 = "C:"+File.separator+"Users"+File.separator+"HELIUS"+File.separator+"git"+File.separator+"Helius-HCM-Server"+File.separator+"WebContent"+File.separator+"WEB-INF"+File.separator+"helius_hcm.properties";
		//String path =  System.getProperty("helius_hcm.properties");
		InputStream inStream;
		try {
			inStream = new FileInputStream(path);
			instance = new Properties();
			instance.load(inStream);
			bucketName = instance.getProperty("aws.bucketname");
			 String check =	instance.getProperty("hcm_testing");
	        	if("yes".equalsIgnoreCase(check)){
	                System.out.println("====check===="+check);
	        		String fileLoc = instance.getProperty("fileLocation")+File.separator+"hapTesting";
	        		instance.setProperty("fileLocation", fileLoc);
	        	}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		/*
		 * InputStream is = null; try { happrop = new Properties(); is =
		 * Utils.class.getResourceAsStream("/com/helius/utils/hap.properties");
		 * happrop.load(is); String check = happrop.getProperty("hcm_testing");
		 * if("yes".equalsIgnoreCase(check)){ System.out.println("====check===="+check);
		 * String fileLoc =
		 * instance.getProperty("fileLocation")+File.separator+"hapTesting";
		 * instance.setProperty("fileLocation", fileLoc); } } catch
		 * (FileNotFoundException e) { e.printStackTrace(); } catch (IOException e) {
		 * e.printStackTrace(); }
		 * 
		 * InputStream aws = null; try { awsprop = new Properties(); aws =
		 * Utils.class.getResourceAsStream("/com/helius/utils/awsconfig.properties");
		 * awsprop.load(aws); bucketName = awsprop.getProperty("aws.bucketname"); }
		 * catch (FileNotFoundException e) { e.printStackTrace(); } catch (IOException
		 * e) { e.printStackTrace(); }
		 */
	}
	
	/*
	 * public Utils(){
	 * 
	 * }
	 */
	
	public static String getProperty(String key) {
		return instance.getProperty(key);
	}
	
	public static String getHapProperty(String key) {
		return instance.getProperty(key);
	}
    
	public static String getAwsprop(String key) {
		return instance.getProperty(key);
	}
	public static Set<Entry<Object,Object>> getHAPEntrySet() {
		return instance.entrySet();
	}
	private  org.hibernate.internal.SessionFactoryImpl sessionFactory;

	/**
	 * @return the sessionFactory
	 */
	public org.hibernate.internal.SessionFactoryImpl getSessionFactory() {
		return sessionFactory;
	}

	/**
	 * @param sessionFactory the sessionFactory to set
	 */
	public void setSessionFactory(org.hibernate.internal.SessionFactoryImpl sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	public final String imagedir = "C:" + File.separator + "Users" + File.separator + "HELIUS" + File.separator
			+ "Documents" + File.separator + "employee_photo";


	/*public final static String fileLocation = "C:" + File.separator + "Users" + File.separator + "HELIUS" + File.separator
			+ "Documents";
	public static final String fileLocation = File.separator + "home" + File.separator + "ec2-user" + File.separator + "documents";
	*/
	public final static String fileLocation = "C:" + File.separator + "Users" + File.separator + "HELIUS" + File.separator
			+ "Documents";
    private static final Logger logger = LogManager.getLogger(Utils.class.getName());

	public static boolean authenticateUrl(String userId) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null && auth.getName() != null) {
			if (userId.equalsIgnoreCase(auth.getName())) {
				return true;
			} else {
				logger.warn("Failed to autenticate url as userid in url does not match with logged in user " + auth.getName()+ " userid passed in url is " + userId);
				return false;
			}
		} else {
			logger.error("Failed to authenticate url as unable to get loggedin user details from security context UserId passed in url is "+userId);
			return false;
		}
	}

	public static String jsonPicklist(Set<String> data) {
		StringBuffer Sb = new StringBuffer();
		List<String> sortedList = new ArrayList(data);
		Collections.sort(sortedList,String.CASE_INSENSITIVE_ORDER);
		int num = 0;
		for (String s : sortedList) {
			if (num < sortedList.size() - 1) {
				Sb.append("\"" + s + "\",");
			} else {
				Sb.append("\"" + s + "\"");
			}
			num++;
		}
		String jsonResult = "[" + Sb.toString() + "" + "]";
		return jsonResult;
	}
	
	public static String jsonPicklistClientGroups44(List<Object[]> ClientGrouplists) {
		// TODO Auto-generated method stub	
		List<String> sub_groups1 = new ArrayList<String>();
		HashMap<String, HashMap<String,List<String>>> client_groups_subgroups = new HashMap<String, HashMap<String,List<String>>>();
		HashMap<String, List<String>> group_subgroups = new HashMap<String, List<String>>();
		String grp=null;
		String cli=null;
		String subgrp=null;
		for (Object[] row : ClientGrouplists) {
			 grp = row[1].toString();
			 cli = row[0].toString();
			 subgrp = row[2].toString();
	/*		if(!(sub_groups1.contains(subgrp))){
		sub_groups1.add(subgrp);
			}
if(!(group_subgroups.containsValue(subgrp))){
		group_subgroups.put(grp, sub_groups1);
		System.out.println("=============group_subgroupssas====="+group_subgroups);

}*/
			 if(group_subgroups.containsKey(grp)){
				    // if the key has already been used,
				    // we'll just grab the array list and add the value to it
				 sub_groups1 =  group_subgroups.get(grp);
				 sub_groups1.add(subgrp);
				} else {
				    // if the key hasn't been used yet,
				    // we'll create a new ArrayList<String> object, add the value
				    // and put it in the array list with the new key
					sub_groups1 = new ArrayList<String>();
					sub_groups1.add(subgrp);
				    group_subgroups.put(grp, sub_groups1);
					System.out.println("======group_subgroups2323====="+group_subgroups);

				}
			//	client_groups_subgroups.put(cli,group_subgroups);

	/*	List<String> sub_groups2 = new ArrayList<String>();
		sub_groups2.add("RIB");
		sub_groups2.add("Support");
		group_subgroups.put("Digital", sub_groups2);*/
		
		//if(!(client_groups_subgroups.containsValue(group_subgroups.containsKey(grp)))){
			 
		 if(client_groups_subgroups.containsKey(cli)){
			    // if the key has already been used,
			    // we'll just grab the array list and add the value to it
			 group_subgroups =  client_groups_subgroups.get(cli);
			 group_subgroups.put(grp, sub_groups1);
			} else {
			    // if the key hasn't been used yet,
			    // we'll create a new ArrayList<String> object, add the value
			    // and put it in the array list with the new key
				group_subgroups = new HashMap<String, List<String>>();

				group_subgroups.put(grp, sub_groups1);
				client_groups_subgroups.put(cli,group_subgroups);

				System.out.println("======client_groups_subgroups23789====="+client_groups_subgroups);

			}
		//}
		}
	/*	String clientmasterdetails=null;
		for (Object cligrplst : ClientGrouplists) {
			clientmasterdetails = (String) cligrplst;
			System.out.println("\n\n===clientmasterdetailsxxxxx===" + clientmasterdetails);
			sub_groups1.add(clientmasterdetails);
		}
		*/
		
	
		
		ObjectMapper objmapper = new ObjectMapper();
		String mapToJson=null;
		try {
			 mapToJson = objmapper.writeValueAsString(client_groups_subgroups);
			System.out.println(mapToJson);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mapToJson;
	}
	
	
	public static String jsonPicklistClientGroups(List<Object[]> ClientGrouplists) {
		// TODO Auto-generated method stub
		List<String> sub_groups1 = new ArrayList<String>();
		HashMap<String, HashMap<String, List<String>>> client_groups_subgroups = new HashMap<String, HashMap<String, List<String>>>();
		HashMap<String, List<String>> group_subgroups = new HashMap<String, List<String>>();
		String grp = null;
		String cli = null;
		String subgrp = null;
		for (Object[] row : ClientGrouplists) {
			grp = row[1].toString();
			cli = row[0].toString();
			subgrp = row[2].toString();
			if (group_subgroups.containsKey(grp)) {
				sub_groups1 = group_subgroups.get(grp);
				sub_groups1.add(subgrp);
			} else {
				sub_groups1 = new ArrayList<String>();
				sub_groups1.add(subgrp);
				group_subgroups.put(grp, sub_groups1);
			}
			HashMap<String, List<String>> group_subgroupses;
			if (client_groups_subgroups.containsKey(cli)) {
				group_subgroupses = client_groups_subgroups.get(cli);
				group_subgroupses.put(grp, sub_groups1);
			} else {
				group_subgroupses = new HashMap<String, List<String>>();
				group_subgroupses.put(grp, sub_groups1);
				client_groups_subgroups.put(cli, group_subgroupses);
			}
		}
		ObjectMapper objmapper = new ObjectMapper();
		String mapToJson = null;
		try {
			mapToJson = objmapper.writeValueAsString(client_groups_subgroups);
			System.out.println(mapToJson);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mapToJson;
	}
	
	/** to check whether given string is date timestamp format **/
	public static boolean isValidDate(String inDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
       dateFormat.setLenient(false);
        try {
            dateFormat.parse(inDate.trim());
        } catch (ParseException pe) {
            return false;
        }
        return true;
    }
	
	public static String jsonPicklistClientMembers(List<Object[]> ClientMember) {
		
		// TODO Auto-generated method stub	
		List<String> members = new ArrayList<String>();
		HashMap<String, List<String>> client_members = new HashMap<String, List<String>>();
		for (Object[] row : ClientMember) {
			String member = row[1].toString();
			String cli = row[0].toString();
			
			if (client_members.containsKey(cli)) {
				members = client_members.get(cli);
				members.add(member);
			} else {
				members = new ArrayList<String>();
				
				members.add(member);
				client_members.put(cli, members);
				System.out.println("======clientmemberssdsja23=====" + client_members);
			}	
		}
		ObjectMapper objmapper = new ObjectMapper();
		String mapToJson=null;
		try {
			 mapToJson = objmapper.writeValueAsString(client_members);
			System.out.println(mapToJson);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mapToJson;
	}
		public static String jsonPicklist1(List<Object[]> rows,int index) {
			StringBuffer Sb = new StringBuffer();
			Set<String> data= new HashSet<String>();
			for (Object[] row : rows) {
				if (row[index] != null) {
					String rowList = row[index].toString().trim();
					if (rowList.length() > 0 ) {
						data.add(rowList);
					}
				}
			}
			List<String> sortedList = new ArrayList(data);
			Collections.sort(sortedList,String.CASE_INSENSITIVE_ORDER);
			int num = 0;
			for (String s : sortedList) {
				if (num < sortedList.size() - 1) {
					Sb.append("\"" + s + "\",");
				} else {
					Sb.append("\"" + s + "\"");
				}
				num++;
			}
			String jsonResult = "[" + Sb.toString() + "" + "]";
			return jsonResult;
		}
	
	public static String jsonWorkPermitPicklist(List<Object[]> work_Permit_Mstr) {
		StringBuffer Sb = new StringBuffer();
		HashMap<String, Workpermit_Worklocation> workpermit_nationality = new HashMap<String, Workpermit_Worklocation>();
		/*
		  for (Object[] row : work_Permit_Mstr) {
		   String nationality = row[1].toString(); if(workPermitMaster.containsKey(nationality)) {
		  List values = (List)workPermitMaster.get(nationality); String wrkLoc  = row[2].toString(); if (!(values.contains(wrkLoc))) {
		  values.add(row[2].toString()); values.add("{"+row[6].toString()+"}");
		  }
		   values.add(row[4].toString()); values.add(row[5].toString()); }
		  else
		   {
		    List values = new ArrayList(); values.add(row[2].toString());
		  values.add(row[4].toString()); values.add(row[5].toString());
		  values.add("{"+row[6].toString()+"}");
		  workPermitMaster.put(nationality, values); } }
		 */
		try{
			String nationality=null;
		String worklocation = null;
		String WPname = null;
		String[] dspFields=null;
		workpermit wp=null;
		for (Object[] row : work_Permit_Mstr) {
			 nationality = row[1].toString(); 
			 	if(row[2]!=null){
					 worklocation = row[2].toString();
			 	}else{				 		
			 		worklocation=null;
			 	}			
			String key = nationality + "," + worklocation;		
			if(row[4] != null && row[5] != null) {
				String displayFields = row[4].toString();
				 dspFields = displayFields.split(",");
				 wp = new workpermit(row[5].toString(), dspFields);

			}else{
				
				wp = null;
			}
			//workpermit wp = new workpermit(row[5].toString(), dspFields);
			String im = (row[6].toString());
			Workpermit_Worklocation ww = workpermit_nationality.get(key);
			if (ww == null) {
				ww = new Workpermit_Worklocation(nationality, worklocation);
			}
			String[] identications = im.split(",");
			for (String id : identications) {
				ww.addid(id);
			}
			ww.addworkpermit(wp);
			workpermit_nationality.put(key, ww);
		}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ObjectMapper om = new ObjectMapper();
		// om.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
		StringBuffer json = new StringBuffer();
		try {
			int i = 0;
			for (Entry<String, Workpermit_Worklocation> entry : workpermit_nationality.entrySet()) {
				json.append(om.writeValueAsString(entry.getValue()));
				// json.append(",");
				if (i < (workpermit_nationality.size() - 1)) {
					json.append(",");
					i++;
				}
			}
			System.out.println("json" + json);
			// employeejson = employeejson1.replaceAll("null",
			// "\"-\"").replaceAll("\"\"", "\"-\"");
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String jsonResult = "[" + json.toString() + "" + "]";
		return jsonResult;
	}	

	public String mapUiLabel(Employee employee) {
		Map<String, Object> paramMap = new HashMap<>();
		ObjectMapper om = new ObjectMapper();
		String jsonResponse = null;
		try {
			if (employee.getEmployeePersonalDetails().getEmployee_id() != null) {
				paramMap.put("Employee ID", employee.getEmployeePersonalDetails().getEmployee_id());
			}
			jsonResponse = om.writeValueAsString(paramMap);
		} catch (JsonProcessingException e) {

		}
		return jsonResponse;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	/**
	 * This method is used to get picklist_name and helius_email_id from pickllistNameAndEmployeeNameAssoc and returns the response in a structured format. 
	 * */
	public static HashMap<String,String> getEmailIdFromPickllistNameAndEmployeeNameAssoc(String[] picklist_name, Session session)
			throws Exception {
		List<Object[]> emailids = null;
		String Query = "SELECT picklist_name,helius_email_id from pickllistNameAndEmployeeNameAssoc where picklist_name IN (:picklist_name) ";
		emailids = session.createSQLQuery(Query).setParameterList("picklist_name", picklist_name).list();
		HashMap<String, String> map = new HashMap<String, String>();
		for (Object[] obj : emailids) {
			String email = "";
			if (obj[1] != null) {
				email = obj[1].toString();
			}
			map.put(obj[0].toString(), email);
		}
		return map;
	}
	
	
	/**
	 * This method retrieves a list of helius_email_id values from the `pickllistNameAndEmployeeNameAssoc` table based on the provided picklist_name.
	 * @throws Exception If there are any issues during the query execution or data retrieval.
	 */
	public static List getEmailIdFromPickllistNameAndEmployeeNameAssoc(String picklist_name,Session session) throws Exception {
		String Query = "SELECT helius_email_id from pickllistNameAndEmployeeNameAssoc where picklist_name = :picklist_name ";
		java.util.List emailids = session.createSQLQuery(Query).setParameter("picklist_name", picklist_name).list();
		return emailids;
		}
	
	
	/**
	 * This method handles the copying of files either to a local directory or to an AWS S3 bucket.
	 * 
	 * It performs the following tasks:
	 * 
	 * 1. It checks whether files should be copied to a local folder or uploaded to AWS S3 based on the `awsCheckFlag`.
	 * 2. If copying locally:
	 *    - It creates the target directory if it doesn't exist.
	 *    - It saves the file with the modified filename in the specified folder.
	 * 3. If uploading to AWS S3:
	 *    - It uploads the file to the S3 bucket using the `saveAwsS3bucket` method.
	 * 4. If any file copy operation fails, it reverts the changes by deleting any successfully copied files and throws an error.
	 * 5. It returns a status object that contains whether the operation was successful and a list of successfully copied files.
	 * 
	 * @param request The request containing the files to be copied.
	 * @param modifiedFilenames A map containing the modified names for the files.
	 * @param filefolder The folder path where the files will be saved.
	 * @return A status object containing the result of the file copy operation and a list of copied files.
	 * @throws Exception If any errors occur during the file copy process.
	 */

	
	public static FilecopyStatus copyFiles(MultipartHttpServletRequest request, Map<String,String> modifiedFilenames, String filefolder) throws Exception {
		String check = Utils.awsCheckFlag();
		List<String> copied_with_success = new ArrayList<String>();
		FilecopyStatus success = new FilecopyStatus();
		success.setOk(true);
		String clientfilelocation = Utils.getProperty("fileLocation") + File.separator + filefolder;
		if ("no".equalsIgnoreCase(check)) {
			
			File fileDir = new File(clientfilelocation);
			if (!fileDir.exists()) {
				boolean iscreated = fileDir.mkdirs();
				if (!iscreated) {
					throw new Exception("Failed to copy files Directory not available");
				}
			}
		}
		
		Iterator<String> fileNames = request.getFileNames();
		//String filename = "";
		while(fileNames.hasNext()) {		
			String filename = fileNames.next();
			MultipartFile file = request.getFile(filename);
			//filename = file.getOriginalFilename();
			String modifiedfilename = modifiedFilenames.get(filename);
			// filename = id + "_" + file.getOriginalFilename();
			String fileUrl = clientfilelocation;
			// fileUrl = fileUrl.replaceAll("\\\\", "\\\\\\\\");
			
			if ("no".equalsIgnoreCase(check)) {
				try {
					file.transferTo(new File(new File(fileUrl), modifiedfilename));
					copied_with_success.add(fileUrl+File.separator + modifiedfilename);
				} catch (IllegalStateException | IOException e) {
					// TODO Auto-generated catch block
					//success = false;
					success.setOk(false);
					deleteFiles(copied_with_success);
					throw new Exception("Failed to save the files")	;					
				}
			}
			//upload file aws s3-bucket
			if ("yes".equalsIgnoreCase(check)) {
				String sucessMsg = "";
				try {
					String folder_path = filefolder + "/";
					sucessMsg = saveAwsS3bucket(file, modifiedfilename, folder_path);
					copied_with_success.add(sucessMsg);

				} catch (Exception e) {
					e.printStackTrace();
					success.setOk(false);
					deleteFiles(copied_with_success);
					throw new Exception(e.getMessage());
				}
			}
			
		}
		success.setCopied_with_success(copied_with_success);
		return success;
	}
	
	/**
	 * This method handles file copying from a request to either a local folder or an AWS S3 bucket.
	 * 
	 * It performs the following tasks:
	 * 
	 * 1. Iterates through all files in the request.
	 * 2. For each file:
	 *    - If AWS S3 is not used (`awsCheck` is "no"), it saves the file to a local directory.
	 *    - If AWS S3 is used (`awsCheck` is "yes"), it saves the file to an S3 bucket.
	 * 3. It creates the necessary directories if they don't exist.
	 * 4. If any file copy operation fails, it deletes the successfully copied files and throws an error.
	 * 5. It returns a status object indicating whether the file copying was successful, along with the list of files that were successfully copied.
	 * 
	 * @param request The request containing the files to be copied.
	 * @param modifiedFilenames A map containing modified names for the files.
	 * @param filefolder A map containing the folder paths where the files should be saved.
	 * @return A status object containing the result and list of successfully copied files.
	 * @throws Exception If any errors occur during the file copy process.
	 */

	
	public static FilecopyStatus copyFiles(MultipartHttpServletRequest request, Map<String,String> modifiedFilenames, Map<String,String> filefolder) throws Exception {
		String awsCheck = Utils.awsCheckFlag();
		List<String> copied_with_success = new ArrayList<String>();
		FilecopyStatus success = new FilecopyStatus();
		success.setOk(true);
		String clientfilelocation = Utils.getProperty("fileLocation");
		Iterator<String> fileNames = request.getFileNames();
		//String filename = "";
		while(fileNames.hasNext()) {
			String filename = fileNames.next();
			MultipartFile file = request.getFile(filename);
			//filename = file.getOriginalFilename();
			String modifiedfilename = modifiedFilenames.get(filename);
			// filename = id + "_" + file.getOriginalFilename();
			String folder = filefolder.get(filename);
			String fileUrl = clientfilelocation + File.separator + folder;
			
			if("no".equalsIgnoreCase(awsCheck)){
		    File fileDir = new File(fileUrl);
			if (!fileDir.exists()) {
				boolean iscreated = fileDir.mkdirs();
				if (!iscreated) {
					throw new Exception("Failed to copy files Directory not available");
				}
			}
			try {
				file.transferTo(new File(new File(fileUrl), modifiedfilename));
				copied_with_success.add(fileUrl+File.separator + modifiedfilename);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				//success = false;
				success.setOk(false);
				deleteFiles(copied_with_success);
				throw new Exception("Failed to save the files")	;					
			}
			}
			//save files in s3 bucket
			if ("yes".equalsIgnoreCase(awsCheck)) {
				String sucessMsg="";
				try {
					String folder_path=folder+"/";
					sucessMsg=saveAwsS3bucket(file,modifiedfilename,folder_path);
					copied_with_success.add(sucessMsg);

				} catch (Exception e) {
					e.printStackTrace();
					success.setOk(false);
					deleteFiles(copied_with_success);
					throw new Exception(e.getMessage())	;
				}	
				
			}
		}
		success.setCopied_with_success(copied_with_success);
		return success;
	}
	
	
	
	/**
	 * This method copies files from an HTTP request to a specified folder on the server.
	 * It does the following:
	 * 
	 * 1. Reads the file names from the request and gets the new file names and folder paths.
	 * 2. Checks if the target folder exists. If not, it creates the folder.
	 * 3. Saves each file with the new name in the correct folder.
	 * 4. If any file fails to save, it stops the process, deletes the files that were saved, and throws an error.
	 * 
	 * The method returns a status indicating whether the files were successfully copied and lists the successfully copied files.
	 * 
	 * @param request The request containing the files to be copied.
	 * @param modifiedFilenames The new names for the files.
	 * @param filefolder The folders where the files will be saved.
	 * @return A status object with the result and list of successfully copied files.
	 * @throws Exception If something goes wrong during the file copying process.
	 */

public static FilecopyStatus copySowFiles(MultipartHttpServletRequest request, Map<String,String> modifiedFilenames, Map<String,String> filefolder) throws Exception {		
		List<String> copied_with_success = new ArrayList<String>();
		FilecopyStatus success = new FilecopyStatus();
		success.setOk(true);
		String clientfilelocation = Utils.getProperty("fileLocation");
		Iterator<String> fileNames = request.getFileNames();
		//String filename = "";
		while(fileNames.hasNext()) {
			String filename1 = fileNames.next();
			MultipartFile file = request.getFile(filename1);
			String filename = new String(filename1.getBytes("ISO8859_1"), "UTF8");
			//filename = file.getOriginalFilename();
			String modifiedfilename = modifiedFilenames.get(filename);
			// filename = id + "_" + file.getOriginalFilename();
			String folder = filefolder.get(filename);
			String fileUrl = clientfilelocation + File.separator + folder;
			File fileDir = new File(fileUrl);
			if (!fileDir.exists()) {
				boolean iscreated = fileDir.mkdirs();
				if (!iscreated) {
					throw new Exception("Failed to copy files Directory not available");
				}
			}
			try {
				file.transferTo(new File(new File(fileUrl), modifiedfilename));
				copied_with_success.add(fileUrl+File.separator + modifiedfilename);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				//success = false;
				success.setOk(false);
				deleteFiles(copied_with_success);
				throw new Exception("Failed to save the files")	;					
			}
		}
		success.setCopied_with_success(copied_with_success);
		return success;
	}
	
	public static void deleteFiles(List<String> copied_with_success) {
		
		for(String filename : copied_with_success) {
			File file = new File(filename);
			file.delete();
		}
	}
	
	
	
	/**
	 * This method handles downloading a file based on the given URL. It first checks if the AWS service is being used (based on a flag).
	 * - If AWS is not used, it tries to retrieve the file from the local file system using the given URL (as a relative file path).
	 * - If AWS is used, it attempts to download the file from an AWS S3 bucket.
	 * 
	 * If the file is found, the method reads the file's contents into a byte array and returns it with an HTTP status of OK (200).
	 * If the file is not found, it returns a 404 Not Found response.
	 * If any unexpected errors occur, it returns a 500 Internal Server Error response.
	 * 
	 * @param url The path or URL of the file to be downloaded (either from the local file system or S3).
	 * @return A ResponseEntity containing the file as a byte array and the corresponding HTTP status code.
	 */

	public static ResponseEntity<byte[]> downloadFileByUrl(String url) {
		String awsCheck = awsCheckFlag();
		byte[] files = null;
		FileInputStream fi = null;
		try {
			
			if ("no".equalsIgnoreCase(awsCheck)) {
				String fileUrl = Utils.getProperty("fileLocation")+ File.separator + url;
				File file = new File(fileUrl);
				if (file.exists()) {
					fi = new FileInputStream(fileUrl);
					files = IOUtils.toByteArray(fi);
					fi.close();
				} else {
					return new ResponseEntity<byte[]>(HttpStatus.NOT_FOUND);
				}
			}
			if ("yes".equalsIgnoreCase(awsCheck)) {
				try {
					if (url!=null &&!url.isEmpty()) {
						files = downloadFileByAWSS3Bucket(url);
					}else {
						return new ResponseEntity<byte[]>(HttpStatus.NOT_FOUND);
					}
				} catch (Exception e) {
					e.printStackTrace();
					return new ResponseEntity<byte[]>(HttpStatus.NOT_FOUND);
				}
			}
			
		} catch (Throwable e) {
			e.printStackTrace();
			return new ResponseEntity<byte[]>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		ResponseEntity<byte[]> responseEntity = new ResponseEntity<byte[]>(files, HttpStatus.OK);
		return responseEntity;
	}
	
	/**
	 * This method downloads a file from an AWS S3 bucket based on the provided file path. 
	 * It first checks if the file exists in the S3 bucket by verifying the file path. If the file is found, 
	 * the method retrieves the file as bytes and returns them. If the file is not found, 
	 * a custom exception with a "File Not Found" message is thrown.
	 * 
	 * @param filePath The path of the file in the S3 bucket that needs to be downloaded.
	 * @return A byte array representing the file content if the file exists.
	 * @throws Exception If the file is not found or an error occurs during the process.
	 */

	public static byte[] downloadFileByAWSS3Bucket(String filePath) throws Exception {
		byte[] bytes = null;
		String fileUrl = removeExistingPath(filePath);
	try {
			boolean fileExist = doesKeyExist(bucketName, fileUrl, s3Client);
			if (fileExist == true) {
				 software.amazon.awssdk.core.ResponseBytes<GetObjectResponse> s3Object = s3Client.getObject(
		                    GetObjectRequest.builder().bucket(bucketName).key(fileUrl).build(),
		                    ResponseTransformer.toBytes());
				 bytes = s3Object.asByteArray();

			} else {
				throw new Exception("File Not Found");
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return bytes;
	}
	
	//creating aws s3client Object
	/*
	 * public static S3Client s3client() { return S3Client.builder().build();
	 * 
	 * }
	 */
	
	public static boolean doesKeyExist(String bucket, String key, S3Client s3Client) {
		ListObjectsV2Response listObjects = s3Client
				.listObjectsV2(ListObjectsV2Request.builder().bucket(bucket).prefix(key).build());
		return !listObjects.contents().isEmpty();
	}
	
	
	/**
	 * This method uploads a PDF file (in byte format) to an AWS S3 bucket. 
	 * It takes the byte array of the PDF and the desired file path in the S3 bucket as input. 
	 * The method creates a request to upload the file to the specified location in the bucket using the AWS SDK.
	 * After the file is uploaded, a response is returned and logged.
	 * 
	 * @param pdfBytes The byte array of the PDF file to upload.
	 * @param out The path where the file will be saved in the S3 bucket.
	 */

	public static void convertXlsxAndPdfFileToByte(byte[] pdfBytes, String out) {
		
		PutObjectResponse response = s3Client.putObject(PutObjectRequest.builder().bucket(bucketName).key(out).build(),
				RequestBody.fromBytes(pdfBytes));
		System.out.println(response);
		/*
		 * InputStream inp =new ByteArrayInputStream(pdfBytes);
		 * 
		 * PutObjectRequest putObjectRequest =
		 * PutObjectRequest.builder().bucket(bucketName).key(out)
		 * .contentType("application/pdf").build(); s3Client.putObject(putObjectRequest,
		 * RequestBody.fromInputStream(inp, pdfBytes.length));
		 */
		 
	}
	/**
	 * This method is used to check the file is exist or not in AWS S3 bucket based on provided invoicepdfpathfolder.
	 * */
	public static Boolean checkFileExist(String invoicepdfpathfolder) {
		boolean fileExist = doesKeyExist(bucketName, invoicepdfpathfolder, s3Client);
		return fileExist;
	}

	public static String removeExistingPath(String filePath) {
		String url = "";
		String[] p = filePath.split("/", 5);
		String newpath = "";
		if (p.length >= 5) {

			newpath = p[0] + "/" + p[1] + "/" + p[2] + "/" + p[3] + "/";

			if (newpath.equalsIgnoreCase("/opt/wildfly-10.1.0.Final/haptesting/")) {
				url = filePath.replace(newpath, "");
			} else {
				// condn for /opt/wildfly-10.1.0.Final/ if it is match replace it ""
				newpath = p[0] + "/" + p[1] + "/" + p[2] + "/";
				if (newpath.equalsIgnoreCase("/opt/wildfly-10.1.0.Final/")) {
					url = filePath.replace(newpath, "");
				} else {
					url = filePath;
				}

			}
		} else {
			url = filePath;
		}
		return url;
	}

	public static String awsCheckFlag() {
		String awsCheck = "";
		 awsCheck = Utils.getHapProperty("aws_s3flag");
		//awsCheck = Utils.getHapProperty("hcm_testing");
		return awsCheck;

	}
	
	
	/**
	 * This method deletes all files and subdirectories inside a given directory.
	 * 
	 * It goes through each file and subdirectory in the directory and deletes them. If a subdirectory is found, 
	 * it calls the method again to delete the files inside it before deleting the subdirectory itself.
	 * 
	 * @param dirPath The directory whose files and subdirectories will be deleted.
	 */

	 public static void deleteFiles(File dirPath) {
	      File filesList[] = dirPath.listFiles();
	      for(File file : filesList) {
	         if(file.isFile()) {
	            file.delete();
	         } else {
	            deleteFiles(file);
	         }
	      }
	 }
	 
	 
	 /**
	  * This method is responsible for saving a file to an AWS S3 bucket. It accepts a file, a modified filename, and a folder name as parameters, then uploads the file to the specified S3 bucket under the given folder path. 
	  * If an exception occurs during the file conversion or upload process, it catches the exception and throws an appropriate error message. 
	  * The method first converts the MultipartFile to a File object, then constructs the S3 file path using the provided folder and modified filename. 
	  * A PutObjectRequest is created and executed to upload the file to the S3 bucket. 
	  * After the file is uploaded, the method checks whether the file was successfully saved by verifying the file's existence in the S3 bucket using the `doesKeyExist` function. 
	  * If the file is successfully saved, a success message containing the file path in the S3 bucket is returned. 
	  * If the file upload fails or any other error occurs, an exception is thrown with a corresponding error message.
	  * 
	  * @param file The file to be uploaded to the S3 bucket.
	  * @param modifiedfilename The modified name of the file to be saved.
	  * @param folder The folder path in the S3 bucket where the file will be stored.
	  * @return A string containing the S3 file path if the upload is successful.
	  * @throws Exception If any error occurs during the file conversion, upload, or verification process.
	  */

	 public static String saveAwsS3bucket(MultipartFile file, String modifiedfilename, String folder)throws Exception {
			boolean flag=false;
			String sucessMsg="";
			try {

				File file1 = convertMultipartToFile(file);
			
				String filepath=folder + modifiedfilename;
		        
				PutObjectRequest request = PutObjectRequest.builder()
		                            .bucket(bucketName).key(filepath).build();
		         
				s3Client.putObject(request, RequestBody.fromFile(file1));  
				     flag = doesKeyExist(bucketName, filepath,s3Client);
				
			
			if (flag == true) {
				sucessMsg=bucketName+"/"+filepath;
			}
			else {
				throw new Exception("Failed to save File");
			}
				

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new Exception(e.getMessage());
			}
			return sucessMsg;	
			}
	 
	 private static File convertMultipartToFile(MultipartFile file){
			
	     File converFile = new File(file.getOriginalFilename());
	     FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(converFile);
			fos.write(file.getBytes());
			fos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 	return converFile;
		}
}
