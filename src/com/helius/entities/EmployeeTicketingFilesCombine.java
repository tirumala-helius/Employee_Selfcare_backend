package com.helius.entities;

import java.sql.Timestamp;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EmployeeTicketingFilesCombine {
	

	private Employee_Ticketing_System employee_Ticketing_System;
	private List<EmployeeTicketingFiles> employeeTicketingFiles;
	
	
	public Employee_Ticketing_System getEmployee_Ticketing_System() {
		return employee_Ticketing_System;
	}
	public void setEmployee_Ticketing_System(Employee_Ticketing_System employee_Ticketing_System) {
		this.employee_Ticketing_System = employee_Ticketing_System;
	}
	public List<EmployeeTicketingFiles> getEmployeeTicketingFiles() {
		return employeeTicketingFiles;
	}
	public void setEmployeeTicketingFiles(List<EmployeeTicketingFiles> employeeTicketingFiles) {
		this.employeeTicketingFiles = employeeTicketingFiles;
	}
	
	
	

	

}
