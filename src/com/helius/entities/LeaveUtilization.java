package com.helius.entities;

public class LeaveUtilization {
 private String employee_id;
 private int client_id;
 private float carryForward = 0;
 private String leaveType;
 private double entitlement= 0.0;
 private double utilizedLeave = 0.0;
 private double balanceLeave = 0.0;
public String getEmployee_id() {
	return employee_id;
}
public void setEmployee_id(String employee_id) {
	this.employee_id = employee_id;
}
public int getClient_id() {
	return client_id;
}
public void setClient_id(int client_id) {
	this.client_id = client_id;
}
public float getCarryForward() {
	return carryForward;
}
public void setCarryForward(float carryForward) {
	this.carryForward = carryForward;
}
public String getLeaveType() {
	return leaveType;
}
public void setLeaveType(String leaveType) {
	this.leaveType = leaveType;
}
public double getEntitlement() {
	return entitlement;
}
public void setEntitlement(double entitlement) {
	this.entitlement = entitlement;
}
public double getUtilizedLeave() {
	return utilizedLeave;
}
public void setUtilizedLeave(double utilizedLeave) {
	this.utilizedLeave = utilizedLeave;
}
public double getBalanceLeave() {
	return balanceLeave;
}
public void setBalanceLeave(double balanceLeave) {
	this.balanceLeave = balanceLeave;
}


}
