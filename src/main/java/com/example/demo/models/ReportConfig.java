// ----------------------------------------------------------------------------
// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.
// ----------------------------------------------------------------------------

package com.example.demo.models;

import com.example.demo.controllers.EmbedController;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Properties for embedding the report 
 */
public class ReportConfig {
	static final Logger logger = LoggerFactory.getLogger(EmbedController.class);
	
	public String id = "";

	public String embedUrl = "";
	
	public String name = "";

	public  String reportType = "";

	public Boolean isEffectiveIdentityRolesRequired = false;

	public Boolean isEffectiveIdentityRequired = false;

	public Boolean enableRLS = false;

	public String username;

	public String roles;
	
	public JSONObject getJSONObject() {
		JSONObject jsonObj = new JSONObject();
		try {
			jsonObj.put("reportId", id);
			jsonObj.put("embedUrl", embedUrl);
			jsonObj.put("reportName", name);
			jsonObj.put("reportType", reportType);
		} catch (JSONException e) {
            logger.error("DefaultListItem.toString JSONException: {}", e.getMessage());
		}
		return jsonObj;
	}
}
