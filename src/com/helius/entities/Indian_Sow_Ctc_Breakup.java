package com.helius.entities;

import java.math.BigDecimal;
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
@Audited
@Table(name = "Indian_Sow_Ctc_Breakup")
public class Indian_Sow_Ctc_Breakup extends HeliusEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)

	@Column
	private int Indian_sow_ctc_breakup_id;

	@Column
	private String employee_id;

	@Column
	private int offer_id;

	@Column
	private BigDecimal amount;

	@Column
	private String frequency;

	@Column
	private String sow_field;

	@Column
	private String currency;

	@Column
	private String status;

	@Column
	private String last_modified_by;

	@Column
	@UpdateTimestamp
	private Timestamp last_modified_date;

	@Column
	private String created_by;

	@Column
	private String comments;

	@Column
	@CreationTimestamp
	private Timestamp create_date;

	public int getIndian_sow_ctc_breakup_id() {
		return Indian_sow_ctc_breakup_id;
	}

	public void setIndian_sow_ctc_breakup_id(int indian_sow_ctc_breakup_id) {
		Indian_sow_ctc_breakup_id = indian_sow_ctc_breakup_id;
	}

	public String getEmployee_id() {
		return employee_id;
	}

	public void setEmployee_id(String employee_id) {
		this.employee_id = employee_id;
	}

	public int getOffer_id() {
		return offer_id;
	}

	public void setOffer_id(int offer_id) {
		this.offer_id = offer_id;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public String getFrequency() {
		return frequency;
	}

	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}

	public String getSow_field() {
		return sow_field;
	}

	public void setSow_field(String sow_field) {
		this.sow_field = sow_field;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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

	public String getCreated_by() {
		return created_by;
	}

	public void setCreated_by(String created_by) {
		this.created_by = created_by;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public Timestamp getCreate_date() {
		return create_date;
	}

	public void setCreate_date(Timestamp create_date) {
		this.create_date = create_date;
	}

}
