package com.subgraph.vega.ui.http.requesteditviewer;

import java.net.URISyntaxException;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.BasicHttpContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.part.ViewPart;

import com.subgraph.vega.api.analysis.IContentAnalyzer;
import com.subgraph.vega.api.analysis.IContentAnalyzerFactory;
import com.subgraph.vega.api.http.requests.IHttpRequestBuilder;
import com.subgraph.vega.api.http.requests.IHttpRequestEngine;
import com.subgraph.vega.api.http.requests.IHttpRequestEngineFactory;
import com.subgraph.vega.api.http.requests.IHttpResponse;
import com.subgraph.vega.api.model.IWorkspace;
import com.subgraph.vega.api.model.requests.IRequestLogRecord;
import com.subgraph.vega.ui.http.Activator;
import com.subgraph.vega.ui.http.builder.HeaderEditor;
import com.subgraph.vega.ui.http.builder.IHttpBuilderPart;
import com.subgraph.vega.ui.http.builder.RequestEditor;
import com.subgraph.vega.ui.httpviewer.HttpMessageViewer;

public class RequestEditView extends ViewPart {
	public final static String VIEW_ID = "com.subgraph.vega.views.requestEdit";
	private IHttpRequestEngine requestEngine;
	private IHttpRequestBuilder requestBuilder;
	private IContentAnalyzer contentAnalyzer;
	private SashForm parentComposite;
	private IHttpBuilderPart requestBuilderPartCurr;
	private RequestEditor requestEditor;
	private HeaderEditor requestHeaderEditor;
	private TabFolder requestTabFolder;
	private TabItem requestTabItem;
	private TabItem requestHeaderTabItem;
	private TabItem requestTabFolderItem;
	private HttpMessageViewer responseViewer;

	public RequestEditView() {
		super();
		if (requestEngine == null) {
			IHttpRequestEngineFactory requestEngineFactory = Activator.getDefault().getHttpRequestEngineFactoryService();
			final HttpClient httpClient = requestEngineFactory.createBasicClient();
			requestEngine = requestEngineFactory.createRequestEngine(httpClient, requestEngineFactory.createConfig());
		}
		requestBuilder = requestEngine.createRequestBuilder();
	}
	
	@Override
	public void createPartControl(Composite parent) {
		parentComposite = new SashForm(parent, SWT.VERTICAL);

		createRequestEditor(parentComposite);
		createResponseViewer(parentComposite);
		
		parentComposite.setWeights(new int[] {50, 50});
		parentComposite.pack();

		final IContentAnalyzerFactory contentAnalyzerFactory = Activator.getDefault().getContentAnalyzerFactoryService();
		final IWorkspace workspace = Activator.getDefault().getModel().getCurrentWorkspace();
		if(workspace != null) {
			contentAnalyzer = contentAnalyzerFactory.createContentAnalyzer(workspace.getScanAlertRepository().getProxyScanInstance());
			contentAnalyzer.setDefaultAddToRequestLog(true);
			contentAnalyzer.setAddLinksToModel(true);
		}
	}

	@Override
	public void setFocus() {
		if (parentComposite != null) {
			parentComposite.setFocus();
		}
	}

	public void setRequest(IRequestLogRecord record) throws URISyntaxException {
		if (record != null) {
			requestBuilder.setFromRequest(record);
		} else {
			requestBuilder.clear();
		}

		if (requestBuilderPartCurr != null) {
			requestBuilderPartCurr.refresh();
		}
	}

	private void displayError(String text) {
		MessageBox messageDialog = new MessageBox(parentComposite.getShell(), SWT.ICON_WARNING | SWT.OK);
		messageDialog.setText("Error");
		if (text == null) {
			text = "Unexpected error occurred";
		}
		messageDialog.setMessage(text);
		messageDialog.open();
	}
	
	private void displayExceptionError(Exception e) {
		if (e.getMessage() != null) {
			displayError(e.getMessage());
		} else {
			displayError(e.getCause().getMessage());
		}
	}

	public void sendRequest() {
		if (requestBuilderPartCurr != null) {
			try {
				requestBuilderPartCurr.processContents();
			} catch (Exception e) {
				displayExceptionError(e);
				return;
			}
		}

		HttpUriRequest uriRequest;
		try {
			uriRequest = requestBuilder.buildRequest();
		} catch (Exception e) {
			displayExceptionError(e);
			return;
		}

		BasicHttpContext ctx = new BasicHttpContext();
		IHttpResponse response;
		try {
			response = requestEngine.sendRequest(uriRequest, ctx);
			responseViewer.displayHttpResponse(response.getRawResponse());
		} catch (Exception e) {
			displayExceptionError(e);
			return;
		}
		if(contentAnalyzer != null) {
			contentAnalyzer.processResponse(response);
		}

		if (requestBuilderPartCurr != null) {
			requestBuilderPartCurr.refresh();
		}
	}

	private Composite createRequestEditor(Composite parent) {
		final Composite rootControl = new Composite(parent, SWT.NONE);
		rootControl.setLayout(new FillLayout());

		requestTabFolder = new TabFolder(rootControl, SWT.TOP);

		requestTabItem = new TabItem(requestTabFolder, SWT.NONE);
		requestTabItem.setText("Request");
		requestEditor = new RequestEditor(requestTabFolder, requestBuilder);
		requestTabItem.setControl(requestEditor);
		requestTabItem.setData(requestEditor);

		requestHeaderTabItem = new TabItem(requestTabFolder, SWT.NONE);
		requestHeaderTabItem.setText("Headers");
		requestHeaderEditor = new HeaderEditor(requestTabFolder, requestBuilder, 0);
		requestHeaderTabItem.setControl(requestHeaderEditor);
		requestHeaderTabItem.setData(requestHeaderEditor);

		requestTabFolder.addSelectionListener(createRequestTabFolderSelectionListener());
		requestTabFolderItem = requestTabFolder.getSelection()[0];
		requestBuilderPartCurr = (IHttpBuilderPart) requestTabFolderItem.getData();
		
		return rootControl;
	}

	private SelectionListener createRequestTabFolderSelectionListener() {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TabItem[] selection = requestTabFolder.getSelection();
				if (selection != null) {
					if (requestBuilderPartCurr != null) {
						try {
							requestBuilderPartCurr.processContents();
						} catch (Exception ex) {
							requestTabFolder.setSelection(requestTabFolderItem);
							displayExceptionError(ex);
							return;
						}
					}

					requestTabFolderItem = selection[0];
					requestBuilderPartCurr = (IHttpBuilderPart) requestTabFolderItem.getData();
					requestBuilderPartCurr.refresh();
				}
			}
		};
	}
	
	private Composite createResponseViewer(Composite parent) {
		final Group rootControl = new Group(parent, SWT.NONE);
		rootControl.setText("Response");
		rootControl.setLayout(new FillLayout());

		responseViewer = new HttpMessageViewer(rootControl);
		responseViewer.setEditable(false);

		return rootControl;
	}

}
