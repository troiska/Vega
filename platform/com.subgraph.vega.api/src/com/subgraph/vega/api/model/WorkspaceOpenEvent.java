package com.subgraph.vega.api.model;

import com.subgraph.vega.api.events.IEvent;

public class WorkspaceOpenEvent implements IEvent {
	private final IWorkspace workspace;
	
	public WorkspaceOpenEvent(IWorkspace workspace) {
		this.workspace = workspace;
	}

	public IWorkspace getWorkspace() {
		return workspace;
	}
}
