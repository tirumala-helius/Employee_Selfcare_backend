package com.helius.dao;

import java.util.List;


import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.helius.entities.ClientTimesheetMaster;

public interface IClientTimesheetMasterDAO {

	public List<ClientTimesheetMaster> getAllTimeshhetmaster() throws Throwable;

}
