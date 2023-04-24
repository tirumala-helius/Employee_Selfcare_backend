package com.helius.utils;

import java.sql.Timestamp;

public class TimesheetAutomationHolidays {

	private String client_id;
	private String holiday_name;
	private Timestamp holiday_date;

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

}
