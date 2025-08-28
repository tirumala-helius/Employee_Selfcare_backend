package com.helius.entities;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.Audited;

@Entity
@Table(name="ExceptionalHolidayCalendar")
@Audited
public class ExceptionalHolidayCalendar extends HeliusEntity{

	@Id
	@Column
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int HolidayCalendarId;
	@Column
	private String employee_id;
	@Column
	private String employee_name;
	
	@Column
	private String holiday_calendar_type;

	@Column
	private String work_country;
	
	@Column
	private String created_by;
	
	@Column
	@CreationTimestamp
	private Timestamp created_date;
	
	@Column
	private String last_modified_by;
	
	@Column
	private String client;
	
	
	public String getClient() {
		return client;
	}

	public void setClient(String client) {
		this.client = client;
	}

	public int getHolidayCalendarId() {
		return HolidayCalendarId;
	}

	public void setHolidayCalendarId(int holidayCalendarId) {
		HolidayCalendarId = holidayCalendarId;
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

	public String getHoliday_calendar_type() {
		return holiday_calendar_type;
	}

	public void setHoliday_calendar_type(String holiday_calendar_type) {
		this.holiday_calendar_type = holiday_calendar_type;
	}

	public String getWork_country() {
		return work_country;
	}

	public void setWork_country(String work_country) {
		this.work_country = work_country;
	}

	public String getCreated_by() {
		return created_by;
	}

	public void setCreated_by(String created_by) {
		this.created_by = created_by;
	}

	public Timestamp getCreated_date() {
		return created_date;
	}

	public void setCreated_date(Timestamp created_date) {
		this.created_date = created_date;
	}

	public String getLast_modified_by() {
		return last_modified_by;
	}

	public void setLast_modified_by(String last_modified_by) {
		this.last_modified_by = last_modified_by;
	}

	public Timestamp getLast_modified_date() {
		return last_modified_date;
	}

	public void setLast_modified_date(Timestamp last_modified_date) {
		this.last_modified_date = last_modified_date;
	}

	@Column
	@UpdateTimestamp
	private Timestamp last_modified_date;
	
	
}
