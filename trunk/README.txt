README - RCS Android client
Release 2.2.4



News:
-----

- RCS-e release.
- Capability discovery via SIP OPTIONS: Periodic polling, Update when address book has changed, Integration into native address book.
- Chat service: chat 1-1, Chat group, File transfer, Chat history, Multi-session management, Block/unblock contacts, Is-composing.
- Integration into the native address book thanks to ContactContracts interface.
- Integration of GSMA Test fest results (Helsinki, 01/2011).
- New SIP stack release based on NIST.gov contribution. Now we use the same stack
  as in the Froyo release. In the past we are using only the parser/build of NIST,
  now we use the overall SIP stack.
- TCP support for SIP.
- Anonymous fetch management to discover RCS capabilities of all the contacts in
  the native address book.
- Add new settings which may be changed via an dedicated application (see \provisioning).
- Add a new tool which permits to get & analyze SIP traces more easily via a graphical view.
- Add Service-Route header management.
- Add P-Associated header management.
- Add P-Preferred-URI header management.
- Multi-chat session management.
- Block IM feature for selected contacts
- Support of H.264 codec.
- Support of visio service.
- IMDN management.
- Add several participants to a group chat from a single SIP REFER.
- WAP PUSH notificatione event management.


Bugs fixed:
-----------

See Mantis details at http://container.rd.francetelecom.com
See Opensource issue details at http://code.google.com/p/android-rcs-ims-stack/issues/list

v2.2.4


v2.2.3
- UI enhancements.
- Mantis #0011436	Service is not started when air plane mode is disabled
- Mantis #0010849	Chat not well displayed in event log
- Mantis #0010368	Video preview error on LG Optimus


v2.2.2
- UI enhancements.

v2.2.1
- Mantis #10895	 Bad Service-Route header
- Mantis #10410	 Crash on the "Is composing" feature (with RI)
- Mantis #10580	 RCS service startup failed if GIBA and Airplane mode activated
- Mantis #10634	 SIP provider exception due to stopAddressBookmonitoring error on stack termina
- Mantis #10827	 Capability discovery mechanism creates a new phone number even if it already exists in the address book
- Opensource #3	 Register Authorization qop parameter not compliant with RFC2617
- Opensource #4	 REGISTER challenge: wrong calculation of the response in case of several tokens for qop option
- Opensource #10 Client uses unquoted etags in XCAP operations
- Opensource #11 Client does not handle SUBSCRIBE/202 responses properly
- Opensource #12 X-3GPP-Intended-Identity header is unquoted


v2.2.0
- Mantis #0010428 	Expire value from settings database not taken into account
- Mantis #0010391 	No 200 OK sent on NOTIFY (terminated)
- Mantis #0009961	Exception during a file transfer on HTC Hero
- Mantis #0010388	Initial anonymous fecth procedure not threaded
- Mantis #0010430	First account launch procedure failed if not connected to IMS
- Mantis #0010394	Too many ACK after 200 OK
- Mantis #0009964	Too many ACK retransmission
- Mantis #0010401	Message-ID is different between file transfer chunks

v2.1.5
- Mantis #0010387	Change presence status icon

v2.1.4
- Anonymous fetches for multiple contacts at terminal startup are all done in the same thread (no longer one thread per request)
- Anonymous fetches requests may be disabled by setting the "anonymous refresh timeout" setting to -1
- No more anonymous fetches when coming to the contact View if the profile is shared with this number
- Timestamps for profile invitations were not correctly set


v2.1.3
- Mantis #0010389	When a RCS contact is deleted it is automatically recreated when a NOTIFY is received	
- Mantis #0010393	Automatic linking failing when contact has two numbers	
- Mantis #0010388	Initial anonymous fecth procedure not threaded	
- Mantis #0010389	When a RCS contact is deleted it is automatically recreated when a NOTIFY is received	
- Mantis #0009963	Phone contact number keeps coming back in EAB
- Mantis #0009728	NullPointer exception on terminating video sharing invitation
- Missing "Privacy: id" header on anonymous fetch

v2.1.2
- Defect #188	Bad Is composing timeout
- Defect #177	Menu file transfer from contact card don't load
- Defect #170	Rich call popup no more displayed
- Defect #168	File transfer crash
- Timestamp of <Tuble> elements not taken into account
- SO_TIMEOUT value to small in EDGE
- Crash during file transfer on terminating side


v2.1.1
- Defect #130	File transfer not cancelled on network error
- Defect #139	Must subscribe to his own presence


Known bugs:
-----------

- Mantis #10522	Bad SIP retransmission timer
- Mantis #10627	Exception on 407 response for RCS-e OPTION
- Mantis #10849	RI: Chat not well displayed in event log
- Defect #99	Double call - No Csh menu
- Defect #121	Black screen during call
- Defect #131	No message sent at the stop of the device (same as Defect E2E#108)
- Defect #194	Each time the terminal boot, pending presence sharing invitations are again displayed in notification bar
- RCS phone numbers without country code are not well managed in native address book


Notes:
-------
- This release works only with 2.x Android OS
- Use only RCS phone numbers with country code (see bugs list)


Contact:
--------
OrangeLabs, ASC Devices
jeanmarc.auffret@orange-ftgroup.com
