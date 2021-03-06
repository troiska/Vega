package com.subgraph.vega.impl.scanner.forms;

import com.subgraph.vega.api.scanner.IFormCredential;

public class FormCredential implements IFormCredential {

	private final String username;
	private final String password;

	private String usernameField;
	private String passwordField;
	private String targetName;

	public FormCredential(String username, String password) {
		this.username = username;
		this.password = password;
	}

	@Override
	public void setTargetName(String name) {
		this.targetName = name;
	}

	@Override
	public void setUsernameFieldName(String name) {
		this.usernameField = name;
	}

	@Override
	public void setPasswordFieldName(String name) {
		this.targetName = name;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getTargetName() {
		return targetName;
	}

	@Override
	public String getUsernameFieldName() {
		return usernameField;
	}

	@Override
	public String getPasswordFieldName() {
		return passwordField;
	}
}
