package com.orangelabs.rcs.service.api.client.terms;


/**
 * Terms & conditions API
 */
interface ITermsApi {
	// Accept terms and conditions
	boolean acceptTerms(in String remote, in String id, in String pin);

	// Reject terms and conditions
	boolean rejectTerms(in String remote, in String id, in String pin);
}
