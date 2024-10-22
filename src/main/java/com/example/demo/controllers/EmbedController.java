// ----------------------------------------------------------------------------
// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.
// ----------------------------------------------------------------------------

package com.example.demo.controllers;

import com.example.demo.config.Config;
import com.example.demo.models.EmbedConfig;
import com.example.demo.services.AzureADService;
import com.example.demo.services.PowerBIService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.HttpClientErrorException;

import java.net.MalformedURLException;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Controller
public class EmbedController {

	static final Logger logger = LoggerFactory.getLogger(EmbedController.class);

	/** 
	 * Embedding details controller
	 * @return ResponseEntity<String> body contains the JSON object with embedUrl and embedToken
	 * @throws JsonProcessingException  Json precessing error
	 * @throws JsonMappingException  Json mapping error
	 */
	@GetMapping(path = "/embeddings")
	@ResponseBody
	public ResponseEntity<String> embedInfoController() throws JsonMappingException, JsonProcessingException {

		// Get access token
		String accessToken;
		try {
			accessToken = AzureADService.getAccessToken();
		} catch (ExecutionException | MalformedURLException | RuntimeException ex) {
			// Log error message
			logger.error(ex.getMessage());

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());

		} catch (InterruptedException interruptedEx) {
			// Log error message
			logger.error(interruptedEx.getMessage());
			
			Thread.currentThread().interrupt();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(interruptedEx.getMessage());
		}

		// Get required values for embedding the report
		try {

			// Get report details
			EmbedConfig reportEmbedConfig = PowerBIService.getEmbedConfig(accessToken, Config.workspaceId, Config.reportId);

			// Convert ArrayList of EmbedReport objects to JSON Array
			JSONArray jsonArray = new JSONArray();
			for (int i = 0; i < reportEmbedConfig.embedReports.size(); i++) {
				jsonArray.put(reportEmbedConfig.embedReports.get(i).getJSONObject());
			}

			// Return JSON response in string
			JSONObject responseObj = new JSONObject();
			responseObj.put("embedToken", reportEmbedConfig.embedToken.token);
			responseObj.put("embedReports", jsonArray);
			responseObj.put("tokenExpiry", reportEmbedConfig.embedToken.expiration);

			String response = responseObj.toString();
			return ResponseEntity.ok(response);

		} catch (HttpClientErrorException hcex) {
			// Build the error message
			String errMsg = getString(hcex);

			// Log error message
			logger.error(errMsg);

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errMsg);

		} catch (RuntimeException rex) {
			// Log error message
			logger.error(rex.getMessage());

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(rex.getMessage());
		}
	}

	private static String getString(HttpClientErrorException hcex) {
		StringBuilder errMsgStringBuilder = new StringBuilder("Error: ");
		errMsgStringBuilder.append(hcex.getMessage());

		// Get Request Id
		HttpHeaders header = hcex.getResponseHeaders();
		assert header != null;
		List<String> requestIds = header.get("requestId");
		if (requestIds != null) {
			for (String requestId: requestIds) {
				errMsgStringBuilder.append("\nRequest Id: ");
				errMsgStringBuilder.append(requestId);
			}
		}

		// Error message string to be returned
        return errMsgStringBuilder.toString();
	}
}