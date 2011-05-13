	README v2.2.0, 	14/02/2011

Release note v2.2.0 :
---------------------

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


Release note v2.1.1 :
---------------------

- Non regression tests.


Release note v2.1.0:
--------------------

- Replace "API demo application" by "RI application". The RI application is a reference implementation which
 offers a basic UI to RCS features (see testcases defined by GSMA). The RI application is used during
 GSMA IOT event (i.e. test fest).

- Use Android library concept instead of rcs_core_api.jar to share API interface between applications.

- Move XML settings to settings content provider in order to prepare OMA DM integration.

- GIBA (eraly-IMS) authentication.

- Remove hyper-availability (see last GSMA RCS 2.0 Change Request).

- Anonymous fetch management by using a cache.

- SO_TIMEOUT management for MSRP connection.

- Send an empty MSRP chunk after chat session initialization.

- Content sharing history.

- Chat and file transfer history.

- Event log which agregates all communications (calls, content sharing, chat and file transfer).

- Roaming settings management.

- New RCS settings: max chat sessions, max content sharing session, .etc.


Defects:
--------

- Correct issues 2, 3 and 4.
