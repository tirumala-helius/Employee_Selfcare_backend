package com.helius.utils;

import java.sql.Time;
import java.sql.Timestamp;

public class WorkedOnShifts {

	private Timestamp date;

	private String shiftName;

	public Timestamp getDate() {
		return date;
	}

	public void setDate(Timestamp date) {
		this.date = date;
	}

	public String getShiftName() {
		return shiftName;
	}

	public void setShiftName(String shiftName) {
		this.shiftName = shiftName;
	}

}
