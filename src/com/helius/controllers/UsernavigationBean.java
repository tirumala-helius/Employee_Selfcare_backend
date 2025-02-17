package com.helius.controllers;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;

import javax.persistence.Column;

public class UsernavigationBean{
	private String  employeeId;
	private String  employeeName;
	private Timestamp last_login;
	private Integer login_count;
    private String work_country;
	private Timestamp actual_date_of_joining;
	private HashMap<String,Integer>  activity;
	
	public String getEmployeeId() {
		return employeeId;
	}
	public void setEmployeeId(String employeeId) {
		this.employeeId = employeeId;
	}
	public String getEmployeeName() {
		return employeeName;
	}
	public void setEmployeeName(String employeeName) {
		this.employeeName = employeeName;
	}
	public Timestamp getLast_login() {
		return last_login;
	}
	public void setLast_login(Timestamp last_login) {
		this.last_login = last_login;
	}
	public Integer getLogin_count() {
		return login_count;
	}
	public void setLogin_count(Integer login_count) {
		this.login_count = login_count;
	}
	public String getWork_country() {
		return work_country;
	}
	public void setWork_country(String work_country) {
		this.work_country = work_country;
	}
	public Timestamp getActual_date_of_joining() {
		return actual_date_of_joining;
	}
	public void setActual_date_of_joining(Timestamp actual_date_of_joining) {
		this.actual_date_of_joining = actual_date_of_joining;
	}
	public HashMap<String, Integer> getActivity() {
		return activity;
	}
	public void setActivity(HashMap<String, Integer> activity) {
		this.activity = activity;
	}
	
	
	}
