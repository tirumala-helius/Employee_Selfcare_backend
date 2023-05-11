package com.helius.utils;

import java.sql.Time;
import java.sql.Timestamp;

public class WorkedOnShifts {

	private Timestamp startDate;
	
	private Timestamp endDate;

	private String shiftName;


	public Timestamp getStartDate() {
		return startDate;
	}

	public void setStartDate(Timestamp startDate) {
		this.startDate = startDate;
	}

	public Timestamp getEndDate() {
		return endDate;
	}

	public void setEndDate(Timestamp endDate) {
		this.endDate = endDate;
	}

	public String getShiftName() {
		return shiftName;
	}

	public void setShiftName(String shiftName) {
		this.shiftName = shiftName;
	}

}
