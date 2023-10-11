package com.helius.entities;

import java.util.List;

import com.helius.utils.Employee_Off_In_Lieu_Data;

public class Employee_Leave_Data {

	private List<Leave_Eligibility_Details> leavesEligibility;
	
	private List<Leave_Usage_Details> leaveUsageDetails;
	
	private List<Leave_Record_Details> leaveRecordDetails;

	private Leave_Record_Details deleteLeaveRecord;
	
	private Leave_Eligibility_Details deleteLeaveEligibility;
	
    private List<LeaveUtilization>  leaveUtilizations;
    
    private List<Employee_Off_In_Lieu_Data> employee_Off_In_Lieu_Datas;

	public Leave_Eligibility_Details getDeleteLeaveEligibility() {
		return deleteLeaveEligibility;
	}

	public void setDeleteLeaveEligibility(Leave_Eligibility_Details deleteLeaveEligibility) {
		this.deleteLeaveEligibility = deleteLeaveEligibility;
	}

	public Leave_Record_Details getDeleteLeaveRecord() {
		return deleteLeaveRecord;
	}

	public void setDeleteLeaveRecord(Leave_Record_Details deleteLeaveRecord) {
		this.deleteLeaveRecord = deleteLeaveRecord;
	}

	public List<Leave_Record_Details> getLeaveRecordDetails() {
		return leaveRecordDetails;
	}

	public void setLeaveRecordDetails(List<Leave_Record_Details> leaveRecordDetails) {
		this.leaveRecordDetails = leaveRecordDetails;
	}

	public List<Leave_Eligibility_Details> getLeavesEligibility() {
		return leavesEligibility;
	}

	public void setLeavesEligibility(List<Leave_Eligibility_Details> leavesEligibility) {
		this.leavesEligibility = leavesEligibility;
	}

	public List<Leave_Usage_Details> getLeaveUsageDetails() {
		return leaveUsageDetails;
	}

	public void setLeaveUsageDetails(List<Leave_Usage_Details> leaveUsageDetails) {
		this.leaveUsageDetails = leaveUsageDetails;
	}

	public List<LeaveUtilization> getLeaveUtilizations() {
		return leaveUtilizations;
	}

	public void setLeaveUtilizations(List<LeaveUtilization> leaveUtilizations) {
		this.leaveUtilizations = leaveUtilizations;
	}

	public List<Employee_Off_In_Lieu_Data> getEmployee_Off_In_Lieu_Datas() {
		return employee_Off_In_Lieu_Datas;
	}

	public void setEmployee_Off_In_Lieu_Datas(List<Employee_Off_In_Lieu_Data> employee_Off_In_Lieu_Datas) {
		this.employee_Off_In_Lieu_Datas = employee_Off_In_Lieu_Datas;
	}
	

}
 