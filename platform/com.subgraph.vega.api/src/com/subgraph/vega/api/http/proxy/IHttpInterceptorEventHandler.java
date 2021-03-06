package com.subgraph.vega.api.http.proxy;

public interface IHttpInterceptorEventHandler {
	/**
	 * Notification that a transaction was queued for processing.
	 *
	 * @param transaction Transaction.
	 * @param idx Array index in queue transaction was added at.
	 */
	public void notifyQueue(IProxyTransaction transaction, int idx);

	/**
	 * Notification that a transaction was removed from the queue.
	 * 
	 * @param idx Array index of transaction that was removed. 
	 */
	public void notifyRemove(int idx);

	/**
	 * Notification that the transaction queue is empty.
	 */
	public void notifyEmpty();
}
