package com.helius.utils;

import java.sql.Timestamp;


public class Employee_Off_In_Lieu_Data {
	
	private int employee_off_in_lieu_id;
	
	private int client_id ;

	private String client_name;

	private String oil_public_holiday;

	private Timestamp oil_date;

	private String oil_day;
	
	private String no_of_days;
	
	private Timestamp oil_Validity_Start_Date;
	
	private Timestamp oil_Validity_End_Date;
	
	private float leavesUsed;
	
	

	public int getEmployee_off_in_lieu_id() {
		return employee_off_in_lieu_id;
	}

	public void setEmployee_off_in_lieu_id(int employee_off_in_lieu_id) {
		this.employee_off_in_lieu_id = employee_off_in_lieu_id;
	}

	public int getClient_id() {
		return client_id;
	}

	public void setClient_id(int client_id) {
		this.client_id = client_id;
	}

	public String getClient_name() {
		return client_name;
	}

	public void setClient_name(String client_name) {
		this.client_name = client_name;
	}

	public String getOil_public_holiday() {
		return oil_public_holiday;
	}

	public void setOil_public_holiday(String oil_public_holiday) {
		this.oil_public_holiday = oil_public_holiday;
	}

	public Timestamp getOil_date() {
		return oil_date;
	}

	public void setOil_date(Timestamp oil_date) {
		this.oil_date = oil_date;
	}

	public String getOil_day() {
		return oil_day;
	}

	public void setOil_day(String oil_day) {
		this.oil_day = oil_day;
	}

	public String getNo_of_days() {
		return no_of_days;
	}

	public void setNo_of_days(String no_of_days) {
		this.no_of_days = no_of_days;
	}

	public Timestamp getOil_Validity_Start_Date() {
		return oil_Validity_Start_Date;
	}

	public void setOil_Validity_Start_Date(Timestamp oil_Validity_Start_Date) {
		this.oil_Validity_Start_Date = oil_Validity_Start_Date;
	}

	public Timestamp getOil_Validity_End_Date() {
		return oil_Validity_End_Date;
	}

	public void setOil_Validity_End_Date(Timestamp oil_Validity_End_Date) {
		this.oil_Validity_End_Date = oil_Validity_End_Date;
	}

	public float getLeavesUsed() {
		return leavesUsed;
	}

	public void setLeavesUsed(float leavesUsed) {
		this.leavesUsed = leavesUsed;
	}

	
	
	

}
