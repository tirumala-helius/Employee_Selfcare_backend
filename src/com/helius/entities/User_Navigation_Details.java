package com.helius.entities;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
@Entity
public class User_Navigation_Details {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column
	private int user_navigation_details_id;
	@Column
	private String  user_id;
	@Column
	private String  activity;
	@Column
	private String  user_name ;
	@Column
	private Timestamp current_login;
	@Column
	private String work_country;
	@Column
	private Timestamp actual_date_of_joining;
	@Column
	private Timestamp activity_time;
	
	public int getUser_navigation_details_id() {
		return user_navigation_details_id;
	}
	public void setUser_navigation_details_id(int user_navigation_details_id) {
		this.user_navigation_details_id = user_navigation_details_id;
	}
	public String getUser_id() {
		return user_id;
	}
	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}
	public String getActivity() {
		return activity;
	}
	public void setActivity(String activity) {
		this.activity = activity;
	}
	public String getUser_name() {
		return user_name;
	}
	public void setUser_name(String user_name) {
		this.user_name = user_name;
	}
	public Timestamp getCurrent_login() {
		return current_login;
	}
	public void setCurrent_login(Timestamp current_login) {
		this.current_login = current_login;
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
	public Timestamp getActivity_time() {
		return activity_time;
	}
	public void setActivity_time(Timestamp activity_time) {
		this.activity_time = activity_time;
	}
	
	
	
}
