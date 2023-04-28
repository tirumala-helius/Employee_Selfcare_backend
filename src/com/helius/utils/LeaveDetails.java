package com.helius.utils;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.NotAudited;

public class LeaveDetails {

	private int leave_record_details_id;

	private String type_of_leave;

	private String ampm;

	private String remarks;

	private Timestamp startdate;

	private Timestamp enddate;

	private float leaves_used;

	private String leaveRecordPath;

	private Timestamp create_date;

	private String created_by;

	private String last_modified_by;

	private Timestamp last_modified_date;

	private String leaveday;

	public String getLeaveRecordPath() {
		return leaveRecordPath;
	}

	public void setLeaveRecordPath(String leaveRecordPath) {
		this.leaveRecordPath = leaveRecordPath;
	}

	public int getLeave_record_details_id() {
		return leave_record_details_id;
	}

	public void setLeave_record_details_id(int leave_record_details_id) {
		this.leave_record_details_id = leave_record_details_id;
	}

	public String getLeaveday() {
		return leaveday;
	}

	public void setLeaveday(String leaveday) {
		this.leaveday = leaveday;
	}

	public String getAmpm() {
		return ampm;
	}

	public void setAmpm(String ampm) {
		this.ampm = ampm;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public String getType_of_leave() {
		return type_of_leave;
	}

	public void setType_of_leave(String type_of_leave) {
		this.type_of_leave = type_of_leave;
	}

	public Timestamp getStartdate() {
		return startdate;
	}

	public void setStartdate(Timestamp startdate) {
		this.startdate = startdate;
	}

	public Timestamp getEnddate() {
		return enddate;
	}

	public void setEnddate(Timestamp enddate) {
		this.enddate = enddate;
	}

	public float getLeaves_used() {
		return leaves_used;
	}

	public void setLeaves_used(float leaves_used) {
		this.leaves_used = leaves_used;
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

}
