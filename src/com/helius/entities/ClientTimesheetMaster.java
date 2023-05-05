package com.helius.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "Client_Timesheet_Master")
public class ClientTimesheetMaster {

	@Id
	@Column(name = "client_timesheet_master_id")
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int clientTimesheetMasterId;
	@Column(name = "client_id")
	private int clientId;
	@Column(name = "client_name")
	private String clientName;
	@Column(name = "work_country")
	private String workCountry;
	@Column(name = "leave_allowed_in_first_3_months")
	private String leaveAllowedInFirst3Months;
	@Column(name = "medical_certificate_required_for_1_day_ml")
	private String medicalCertificateRequiredFor1DayML;
	@Column(name = "working_on_weekend_allowed")
	private String workingOnWeekendAllowed;
	@Column(name = "working_on_public_holiday_allowed")
	private String workingOnPublicHolidayAllowed;
	@Column(name = "shifts_allowed")
	private String shiftsAllowed;
	@Column(name = "upload_timesheet_template")
	private String uploadTimesheetTemplateType;
	@Column(name = "upload_timesheet_template_path")
	private String uploadTimesheetTemplatePath;

	public int getClientTimesheetMasterId() {
		return clientTimesheetMasterId;
	}

	public void setClientTimesheetMasterId(int clientTimesheetMasterId) {
		this.clientTimesheetMasterId = clientTimesheetMasterId;
	}

	public int getClientId() {
		return clientId;
	}

	public void setClientId(int clientId) {
		this.clientId = clientId;
	}

	public String getWorkCountry() {
		return workCountry;
	}

	public void setWorkCountry(String workCountry) {
		this.workCountry = workCountry;
	}

	public String getLeaveAllowedInFirst3Months() {
		return leaveAllowedInFirst3Months;
	}

	public void setLeaveAllowedInFirst3Months(String leaveAllowedInFirst3Months) {
		this.leaveAllowedInFirst3Months = leaveAllowedInFirst3Months;
	}

	public String getMedicalCertificateRequiredFor1DayML() {
		return medicalCertificateRequiredFor1DayML;
	}

	public void setMedicalCertificateRequiredFor1DayML(String medicalCertificateRequiredFor1DayML) {
		this.medicalCertificateRequiredFor1DayML = medicalCertificateRequiredFor1DayML;
	}

	public String getWorkingOnWeekendAllowed() {
		return workingOnWeekendAllowed;
	}

	public void setWorkingOnWeekendAllowed(String workingOnWeekendAllowed) {
		this.workingOnWeekendAllowed = workingOnWeekendAllowed;
	}

	public String getWorkingOnPublicHolidayAllowed() {
		return workingOnPublicHolidayAllowed;
	}

	public void setWorkingOnPublicHolidayAllowed(String workingOnPublicHolidayAllowed) {
		this.workingOnPublicHolidayAllowed = workingOnPublicHolidayAllowed;
	}

	public String getShiftsAllowed() {
		return shiftsAllowed;
	}

	public void setShiftsAllowed(String shiftsAllowed) {
		this.shiftsAllowed = shiftsAllowed;
	}

	public String getUploadTimesheetTemplateType() {
		return uploadTimesheetTemplateType;
	}

	public void setUploadTimesheetTemplateType(String uploadTimesheetTemplateType) {
		this.uploadTimesheetTemplateType = uploadTimesheetTemplateType;
	}

	public String getUploadTimesheetTemplatePath() {
		return uploadTimesheetTemplatePath;
	}

	public void setUploadTimesheetTemplatePath(String uploadTimesheetTemplatePath) {
		this.uploadTimesheetTemplatePath = uploadTimesheetTemplatePath;
	}

	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

}
