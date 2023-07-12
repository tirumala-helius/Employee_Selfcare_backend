package com.helius.entities;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name ="Timesheet_Automation_Status")
public class Timesheet_Automation_Status {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int timesheet_automation_status_id;
	
	@Column
	private String employee_id;
	@Column
	private String  employee_name;
	@Column
	private String timesheet_email;
	@Column
	private Timestamp timesheet_month;
	@Column
	private String timesheet_upload_path;
	@Column
	private Timestamp submited_date;
	@Column
	private String client_id;
	@Column
	private String client_name;
	
	
	public int getTimesheet_automation_status_id() {
		return timesheet_automation_status_id;
	}
	public void setTimesheet_automation_status_id(int timesheet_automation_status_id) {
		this.timesheet_automation_status_id = timesheet_automation_status_id;
	}
	public String getEmployee_id() {
		return employee_id;
	}
	public void setEmployee_id(String employee_id) {
		this.employee_id = employee_id;
	}
	public String getEmployee_name() {
		return employee_name;
	}
	public void setEmployee_name(String employee_name) {
		this.employee_name = employee_name;
	}
	public String getTimesheet_email() {
		return timesheet_email;
	}
	public void setTimesheet_email(String timesheet_email) {
		this.timesheet_email = timesheet_email;
	}
	public Timestamp getTimesheet_month() {
		return timesheet_month;
	}
	public void setTimesheet_month(Timestamp timesheet_month) {
		this.timesheet_month = timesheet_month;
	}
	public String getTimesheet_upload_path() {
		return timesheet_upload_path;
	}
	public void setTimesheet_upload_path(String timesheet_upload_path) {
		this.timesheet_upload_path = timesheet_upload_path;
	}
	public Timestamp getSubmited_date() {
		return submited_date;
	}
	public void setSubmited_date(Timestamp submited_date) {
		this.submited_date = submited_date;
	}
	public String getClient_id() {
		return client_id;
	}
	public void setClient_id(String client_id) {
		this.client_id = client_id;
	}
	public String getClient_name() {
		return client_name;
	}
	public void setClient_name(String client_name) {
		this.client_name = client_name;
	}
	
	

}
