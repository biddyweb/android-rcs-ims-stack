README - RCS Android client
Release 2.2.8


News:
-----
- RCS widget to start/stop the service.
- NAT traversal support (RFC6135, draft-ietf-simple-msrp-sessmatch-11).
- GRUU for multidevice support.
- Block FT if contact is blocked.


Bugs fixed:
-----------

See Mantis details at http://container.rd.francetelecom.com
See Opensource issue details at http://code.google.com/p/android-rcs-ims-stack/issues/list


v2.2.8
- Mantis #0013059	Rich call app: Pre-recorded video sharing always get media player error
- Mantis #0012077	Chat app: Chat 1-1 crash application during chat
- Mantis #0012594	Stack: Switch between 2 wi-fi access
- Mantis #0012544	Stack: Chat session no "ack" send by MO side on 200OK received
- Mantis #0013051	Chat app: Latency when chat view is opened
- Mantis #0013053	Chat app: File transfer notification not well formatted
- Mantis #0013054	Chat app: Transfer file dialog not well displayed
- Mantis #0013029	RI: Chat invitation notification is never removed if not clicked
- Mantis #0013033	Chat app: Is composing feature not taken into account when chat view is in background
- Mantis #0013037	Stack: Bad Accept-types in chat session
- Mantis #0012994	Stack: Menu event log does not appear in native contact card
- Mantis #0012995	Stack: Menu event log should not be displayed for normal contacts
- Mantis #0012986	Stack: Bad IMDN SIP MESSAGE content-type
- Mantis #0011249	RI: Display only capable contacts for FT, chat and rich call
- Mantis #0012862	Stack: ANR in rcs settings activity while syncing a lot of contacts (>100)
- Mantis #0012703	EAB app: Exception when adding presence widget
- Mantis #0012252	RI: Problem with video output while video sharing with galaxy Tab
- Mantis #0012797	Stack: Start RCS service failed in IPv6
- Mantis #0011249	RI: Display only capable contacts for FT, chat and rich call
- Mantis #0012639	Stack: Create new contact does not send a SIP OPTIONS on Nexus S
- Mantis #0012638	Stack: Event log: chat entry not well displayed

v2.2.7
- Mantis #0011921	Rich call app: Notification not removed when 3G lost during a rich call
- Mantis #0012186	Chat app: Add participant generates 3 times the same participant instead of one
- Mantis #0012495	Stack: No empty packet sent for file transfer on terminating side
- Mantis #0012604	Stack: RCS icon not grayed in air plane mode
- Mantis #0012600	RI: chat view exception
- Mantis #0012599	Stack: Bad boudary tag in multipart content of the chat INVITE after a 407 response
- Mantis #0012180	Chat app: Exception if capture photo is used to initiate a file transfer
- Mantis #0012559	Stack: Exception when changing a contact's telephone number
- Mantis #0011254	RI: No text entry to edit the first message before to initiate a chat session
- Mantis #0010429	Stack: When a number is present more than once, only the first entry encountered is enriched with a rcs raw contact
- Mantis #0011248	Stack: Request capabilities on a lot of contacts is too long
- Mantis #0012494	Stack: User-agent or Server header shall start with "IM-client" text
- Mantis #0011921	Stack: Notification not removed when 3G lost during a rich call


v2.2.5
- Mantis #0012119	Stack: RTCP transmiter not well stopped at the end of the session
- Mantis #0010627	Stack: Exception on 407 response for RCS-e OPTIONS
- Mantis #0012065	Stack: Force content sharing capabilities reset at the end of the call
- Mantis #0012006	Stack: Set unused MSRP port to 9 as defined in RFC4975 chapitre 5.4
- Mantis #0012068	Chat app: No limit on text length
- Mantis #0011253	RI: chat session list not updated
- Mantis #0012003	RI: Event log exception
- Mantis #0012069	Chat app: Displayed report not displayed in 1-1 chat
- Mantis #0012069	Stack: No BYE sent by the calling party when stopping the chat group session
- Mantis #0011658	Chat app: Add contact button never set to enabled when initiating chat session
- Mantis #0010623	Stack: ContactsManager exception on GalaxyS
- Mantis #0012103	Chat application crash after initiation has failed
- Mantis #0011955	Stack: BYE failed on terminating chat group
- Opensource #17	Stack: Encoding of RCS-e capabilities in Contact Header
- Opensource #18	Stack: Can't connect if no P-Associated-URI header in REGISTER response

v2.2.4
- Mantis #0009919	File transfer failed with file upper than 1Mb
- Mantis #0009642	No "from" tag on REGISTER
- Mantis #0009596	Contact formatting issue on chat initiation
- Mantis #0009567	Activate roaming don't start the RCS service if authorized
- Mantis #0010895	Bad Service-Route header
- Mantis #0010474	Exception on terminating file transfer
- Mantis #0010158	Invitation notification not removed in notification bar
- Mantis #0011252	Display an "in progress" screen during network operations
- Mantis #0011396	Bad expire period on un-register after a 407
- Mantis #0011246	Received file transfer's name truncated
- Mantis #0010580	RCS service startup failed if GIBA and Airplane mode activated
- Mantis #0011249	Display only capable contacts for FT, chat and rich call

v2.2.3
- Mantis #0011436	Service is not started when air plane mode is disabled
- Mantis #0010849	Chat not well displayed in event log
- Mantis #0010368	Video preview error on LG Optimus

v2.2.2
- UI enhancements.

v2.2.1
- Mantis #0010895	Bad Service-Route header
- Mantis #0010410	Crash on the "Is composing" feature (with RI)
- Mantis #0010580	RCS service startup failed if GIBA and Airplane mode activated
- Mantis #0010634	SIP provider exception due to stopAddressBookmonitoring error on stack termina
- Mantis #0010827	Capability discovery mechanism creates a new phone number even if it already exists in the address book
- Opensource #3		Register Authorization qop parameter not compliant with RFC2617
- Opensource #4		REGISTER challenge: wrong calculation of the response in case of several tokens for qop option
- Opensource #10	Client uses unquoted etags in XCAP operations
- Opensource #11	Client does not handle SUBSCRIBE/202 responses properly
- Opensource #12	X-3GPP-Intended-Identity header is unquoted


v2.2.0
- Mantis #0010428	Expire value from settings database not taken into account
- Mantis #0010391	No 200 OK sent on NOTIFY (terminated)
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
- Defect #188 Bad Is composing timeout
- Defect #177 Menu file transfer from contact card don't load
- Defect #170 Rich call popup no more displayed
- Defect #168 File transfer crash
- Timestamp of <Tuble> elements not taken into account
- SO_TIMEOUT value to small in EDGE
- Crash during file transfer on terminating side


v2.1.1
- Defect #130 File transfer not cancelled on network error
- Defect #139 Must subscribe to his own presence


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
- This release works only with 2.x Android OS.
- TLS may be only used from ANdroid 2.3 release.


Contact:
--------
OrangeLabs, ASC Devices
jeanmarc.auffret@orange-ftgroup.com
