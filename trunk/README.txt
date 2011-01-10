	README v2.1.1, 	10/01/2011

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
