package com.helius.utils;

import java.sql.Timestamp;

public class Holiday_Master {
	
	private Integer holiday_master_id;
	private String client_id;
	private String holiday_name;
	private Timestamp holiday_date;
	private String holiday_country;
	private String holiday_day;
	private Timestamp last_modified_date;
	private String last_modified_by;
	private Timestamp create_date;
	private String created_by;
	
	
	
	public Integer getHoliday_master_id() {
		return holiday_master_id;
	}
	public void setHoliday_master_id(Integer holiday_master_id) {
		this.holiday_master_id = holiday_master_id;
	}
	public String getClient_id() {
		return client_id;
	}
	public void setClient_id(String client_id) {
		this.client_id = client_id;
	}
	public String getHoliday_name() {
		return holiday_name;
	}
	public void setHoliday_name(String holiday_name) {
		this.holiday_name = holiday_name;
	}
	public Timestamp getHoliday_date() {
		return holiday_date;
	}
	public void setHoliday_date(Timestamp holiday_date) {
		this.holiday_date = holiday_date;
	}
	public String getHoliday_country() {
		return holiday_country;
	}
	public void setHoliday_country(String holiday_country) {
		this.holiday_country = holiday_country;
	}
	public String getHoliday_day() {
		return holiday_day;
	}
	public void setHoliday_day(String holiday_day) {
		this.holiday_day = holiday_day;
	}
	public Timestamp getLast_modified_date() {
		return last_modified_date;
	}
	public void setLast_modified_date(Timestamp last_modified_date) {
		this.last_modified_date = last_modified_date;
	}
	public String getLast_modified_by() {
		return last_modified_by;
	}
	public void setLast_modified_by(String last_modified_by) {
		this.last_modified_by = last_modified_by;
	}
	public Timestamp getCreate_date() {
		return create_date;
	}
	public void setCreate_date(Timestamp create_date) {
		this.create_date = create_date;
	}
	public String getCreated_by() {
		return created_by;
	}
	public void setCreated_by(String created_by) {
		this.created_by = created_by;
	}

}
