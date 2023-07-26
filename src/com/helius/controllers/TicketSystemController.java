package com.helius.controllers;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.helius.entities.Employee;
import com.helius.managers.EmployeeManager;
import com.helius.managers.TicketManager;
import com.helius.utils.Status;

@RestController
public class TicketSystemController {
	@Autowired
	Status status;

	@Autowired
	ApplicationContext context;
	@Autowired
	private TicketManager ticketManager;

	@CrossOrigin
	@RequestMapping(value = "saveEmpTicket", method = RequestMethod.POST, consumes = { "multipart/form-data" })
	public String saveTicket(@RequestParam("model") String jsondata, MultipartHttpServletRequest request) {
		ObjectMapper om = new ObjectMapper();
		System.out.println("saveticketjson======" + jsondata);
		Employee emp = null;
		try {
			emp = om.readValue(jsondata, Employee.class);
		} catch (IOException e) {
			e.printStackTrace();
			status.setMessage("Unable to save Ticket details invalid json");
			return "{\"response\":\"" + status.getMessage() + "\"}";
		}
		status = ticketManager.saveEmpTicket(emp, request);
		return "{\"response\":\"" + status.getMessage() + "\"}";
	}

	@CrossOrigin
	@RequestMapping(value = "updateEmpTicket", method = RequestMethod.POST, consumes = { "multipart/form-data" })
	public String updatePermanentOffer(@RequestParam("model") String jsondata, MultipartHttpServletRequest request) {
		ObjectMapper om = new ObjectMapper();
		System.out.println("updatepermofferjson======" + jsondata);
		Employee emp = null;
		try {
			emp = om.readValue(jsondata, Employee.class);
		} catch (IOException e) {
			e.printStackTrace();
			status.setMessage("Unable to save Ticket details invalid json");
			return "{\"response\":\"" + status.getMessage() + "\"}";
		}
		status = ticketManager.updateTicket(emp, request);
		return "{\"response\":\"" + status.getMessage() + "\"}";
	}

	@CrossOrigin
	@RequestMapping(value = "getTicketDataByEmpId", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
	public @ResponseBody String getTicketHistoryByEmpId(@RequestParam String empId) {
		String empTicketingData = ticketManager.getTicketHistoryByEmpId(empId);
		return empTicketingData;

	}

}
