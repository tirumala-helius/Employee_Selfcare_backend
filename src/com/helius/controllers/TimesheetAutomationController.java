package com.helius.controllers;

import java.io.IOException;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.helius.entities.ClientTimesheetMaster;
import com.helius.managers.ClientTimesheetMasterManager;
import com.helius.utils.Status;

@RestController
public class TimesheetAutomationController {

	private org.hibernate.internal.SessionFactoryImpl sessionFactory;

	public org.hibernate.internal.SessionFactoryImpl getSessionFactory() {
		return sessionFactory;
	}

	@Autowired
	ApplicationContext context;
	@Autowired
	Status status;

	/*
	 * @Autowired TimesheetMasterManager timesheetMasterManager;
	 */

	@CrossOrigin
	@RequestMapping(value = "getAllTimesheetAutomation", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody String getAllChecklist() {
		ClientTimesheetMasterManager checklistmanager = (ClientTimesheetMasterManager) context
				.getBean("timesheetMasterManager");
		ObjectMapper obm = new ObjectMapper();
		String response = null;
		try {
			List<ClientTimesheetMaster> allChecklist = checklistmanager.getAllChecklist();
			response = obm.writeValueAsString(allChecklist);
		} catch (Exception e) {
			response = "Could Not Retrieve Timesheet Data " + "\n" + e.getMessage();
			return response;
		} catch (Throwable e) {
			response = "Could Not Retrieve Timesheet Data " + "\n" + e.getMessage();
			return response;
		}
		return response;
	}

}
