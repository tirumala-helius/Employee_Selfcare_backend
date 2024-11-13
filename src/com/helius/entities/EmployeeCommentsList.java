package com.helius.entities;

import java.util.List;

public class EmployeeCommentsList {

	private List<EmployeeTicketingSystemCommentsList> employeeTicketingSystemComments;
	private List<EmployeeTicketingFiles> employeeTicketingFiles;
	

	public List<EmployeeTicketingFiles> getEmployeeTicketingFiles() {
		return employeeTicketingFiles;
	}

	public void setEmployeeTicketingFiles(List<EmployeeTicketingFiles> employeeTicketingFiles) {
		this.employeeTicketingFiles = employeeTicketingFiles;
	}

	public List<EmployeeTicketingSystemCommentsList> getEmployeeTicketingSystemComments() {
		return employeeTicketingSystemComments;
	}

	public void setEmployeeTicketingSystemComments(
			List<EmployeeTicketingSystemCommentsList> employeeTicketingSystemComments) {
		this.employeeTicketingSystemComments = employeeTicketingSystemComments;
	}

	
}
