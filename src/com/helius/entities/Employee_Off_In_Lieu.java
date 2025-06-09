package com.helius.entities;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="Employee_Off_In_Lieu")
public class Employee_Off_In_Lieu {
	
	@Id
	@Column
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int employee_off_in_lieu_id;
	@Column
	private int client_id ;
	@Column
	private String client_name;
	@Column
	private String oil_public_holiday;
	@Column
	private Timestamp oil_date;
	@Column
	private String oil_day;
	@Column
	private String no_of_days;
	@Column
	private String validitytype;
	/**
	 * @return the validitytype
	 */
	public String getValiditytype() {
		return validitytype;
	}
	/**
	 * @param validitytype the validitytype to set
	 */
	public void setValiditytype(String validitytype) {
		this.validitytype = validitytype;
	}
	/**
	 * @return the validitydate
	 */
	public Timestamp getValiditydate() {
		return validitydate;
	}
	/**
	 * @param validitydate the validitydate to set
	 */
	public void setValiditydate(Timestamp validitydate) {
		this.validitydate = validitydate;
	}
	@Column
	private Timestamp validitydate;
	
	
	
	
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
	
	
	
	
	
	
	

}
