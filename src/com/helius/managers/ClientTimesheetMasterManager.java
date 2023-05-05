package com.helius.managers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.helius.dao.IClientTimesheetMasterDAO;
import com.helius.entities.ClientTimesheetMaster;
import com.helius.entities.TimesheetMaster;
import com.helius.utils.Status;

public class ClientTimesheetMasterManager {
	private IClientTimesheetMasterDAO clientTimesheetMasterDAO = null;

	public IClientTimesheetMasterDAO getClientTimesheetMasterDAO() {
		return clientTimesheetMasterDAO;
	}

	public void setClientTimesheetMasterDAO(IClientTimesheetMasterDAO clientTimesheetMasterDAO) {
		this.clientTimesheetMasterDAO = clientTimesheetMasterDAO;
	}

	public List<ClientTimesheetMaster> getAllChecklist() throws Throwable {

		List<ClientTimesheetMaster> clientTimesheetMasters = null;

		try {
			clientTimesheetMasters = clientTimesheetMasterDAO.getAllTimeshhetmaster();

		} catch (Exception e) {
			// TODO: handle exception
			throw new Throwable("Failed to fetch New Joinee Employee Details");
		}
		return clientTimesheetMasters;

	}

}
