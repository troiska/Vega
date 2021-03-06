package com.subgraph.vega.internal.model.conditions;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;

import com.db4o.query.Query;
import com.subgraph.vega.api.model.conditions.IHttpCondition;
import com.subgraph.vega.api.model.conditions.IHttpConditionType;
import com.subgraph.vega.api.model.conditions.match.IHttpConditionMatchAction;
import com.subgraph.vega.internal.model.conditions.match.StringMatchActionSet;

public class ConditionHostname extends AbstractCondition {
	static private transient IHttpConditionType conditionType;
	
	static IHttpConditionType getConditionType() {
		synchronized(ConditionHostname.class) {
			if(conditionType == null)
				conditionType = createType();
			return conditionType;
		}
	}

	private static IHttpConditionType createType() {
		return new ConditionType("hostname",  new StringMatchActionSet()) {
			@Override
			public IHttpCondition createConditionInstance(IHttpConditionMatchAction matchAction) {
				return new ConditionHostname(matchAction);
			}
		};
	}

	private ConditionHostname(IHttpConditionMatchAction matchAction) {
		super(matchAction);
	}
	
	@Override
	public boolean matches(HttpRequest request) {
		
		final URI uri = getRequestUri(request);
		if(uri == null)
			return false;
		return matchesString(uri.getHost());
	}
	
	private URI getRequestUri(HttpRequest request) {
		if(request == null)
			return null;
		try {
			return new URI(request.getRequestLine().getUri());
		} catch (URISyntaxException e) {
			return null;
		}		
	}

	@Override
	public boolean matches(HttpResponse response) {
		return false;
	}

	@Override
	public boolean matches(HttpRequest request, HttpResponse response) {
		return matches(request);
	}

	@Override
	public IHttpConditionType getType() {
		return getConditionType();
	}

	@Override
	public void filterRequestLogQuery(Query query) {
		constrainQuery(query.descend("hostname"));		
	}
}
