package com.helius.utils;

import java.sql.Timestamp;
import java.util.List;

import com.helius.entities.Leave_Record_Details;

public class TimesheetAutomation {

	private String empId;
	private String clientId;
	private String employeeName;
	private String client;
	private Timestamp leaveMonth;
	private Timestamp startdate;
	private Timestamp enddate;
	private String reportingManagerName;
	private String reportingManagermailID;
	private String empmailid;
	private List<LeaveDetails> leaveDetails;
	private List<OverTime> overTimes;
	private List<WorkingOnPublicHolidays> workingOnPH;
	private List<WorkedOnShifts> onShifts;

	public String getEmpId() {
		return empId;
	}

	public void setEmpId(String empId) {
		this.empId = empId;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getEmployeeName() {
		return employeeName;
	}

	public void setEmployeeName(String employeeName) {
		this.employeeName = employeeName;
	}

	public String getClient() {
		return client;
	}

	public void setClient(String client) {
		this.client = client;
	}

	public Timestamp getLeaveMonth() {
		return leaveMonth;
	}

	public void setLeaveMonth(Timestamp leaveMonth) {
		this.leaveMonth = leaveMonth;
	}

	public Timestamp getStartdate() {
		return startdate;
	}

	public void setStartdate(Timestamp startdate) {
		this.startdate = startdate;
	}

	public Timestamp getEnddate() {
		return enddate;
	}

	public void setEnddate(Timestamp enddate) {
		this.enddate = enddate;
	}

	public String getReportingManagerName() {
		return reportingManagerName;
	}

	public void setReportingManagerName(String reportingManagerName) {
		this.reportingManagerName = reportingManagerName;
	}

	public String getReportingManagermailID() {
		return reportingManagermailID;
	}

	public void setReportingManagermailID(String reportingManagermailID) {
		this.reportingManagermailID = reportingManagermailID;
	}

	public List<OverTime> getOverTimes() {
		return overTimes;
	}

	public void setOverTimes(List<OverTime> overTimes) {
		this.overTimes = overTimes;
	}

	public List<WorkingOnPublicHolidays> getWorkingOnPH() {
		return workingOnPH;
	}

	public void setWorkingOnPH(List<WorkingOnPublicHolidays> workingOnPH) {
		this.workingOnPH = workingOnPH;
	}

	public List<WorkedOnShifts> getOnShifts() {
		return onShifts;
	}

	public void setOnShifts(List<WorkedOnShifts> onShifts) {
		this.onShifts = onShifts;
	}

	public String getEmpmailid() {
		return empmailid;
	}

	public void setEmpmailid(String empmailid) {
		this.empmailid = empmailid;
	}

	public List<LeaveDetails> getLeaveDetails() {
		return leaveDetails;
	}

	public void setLeaveDetails(List<LeaveDetails> leaveDetails) {
		this.leaveDetails = leaveDetails;
	}

}
