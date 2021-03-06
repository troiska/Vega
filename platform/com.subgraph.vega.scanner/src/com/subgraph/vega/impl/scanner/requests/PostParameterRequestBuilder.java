package com.subgraph.vega.impl.scanner.requests;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;

import com.subgraph.vega.api.model.web.IWebPath;

public class PostParameterRequestBuilder extends AbstractParameterRequestBuilder {

	public PostParameterRequestBuilder(IWebPath path, List<NameValuePair> parameters, int index) {
		super(path, parameters, index);
	}

	@Override
	public HttpUriRequest createBasicRequest() {
		return createPostRequest(parameters);
	}

	private HttpUriRequest createPostRequest(List<NameValuePair> parameters) {
		final URI u = createUri(getBasePath());
		final HttpPost req = new HttpPost(u);
		req.setEntity(createParameterEntity(parameters));
		return req;
	}

	@Override
	public HttpUriRequest createAlteredRequest(String value, boolean append) {
		return createPostRequest(getAlteredParameters(value, append, false));
	}

	@Override
	public HttpUriRequest createAlteredParameterNameRequest(String name) {
		return createPostRequest(getAlteredParameters(name, false, true));
	}
	
	private HttpEntity createParameterEntity(List<NameValuePair> parameters) {
		try {
			return new UrlEncodedFormEntity(parameters, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Failed to encode form parameters.", e);
		}
	}
	
	@Override
	public String toString() {
		return "POST "+ getBasePath() + " ("+ printParameters() + ") (idx = "+ parameterFuzzIndex + ")";
	}
	
	private String printParameters() {
		final StringBuilder sb = new StringBuilder();
		boolean first = true;
		for(NameValuePair p: parameters) {
			if(first)
				first = false;
			else
				sb.append(", ");
			sb.append(p.getName());
			if(p.getValue() != null) {
				sb.append("=");
				sb.append(p.getValue());
			}
		}
		return sb.toString();
	}
}
