/*******************************************************************************
 * Software Name : RCS IMS Stack
 * Version : 2.0
 * 
 * Copyright © 2010 France Telecom S.A.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.orangelabs.rcs.provider.settings;

import android.net.Uri;

/**
 * RCS settings data constants
 * 
 * @author jexa7410
 */
public class RcsSettingsData {
	/**
	 * Database URI
	 */
	static final Uri CONTENT_URI = Uri.parse("content://com.orangelabs.rcs.settings/settings"); 
	
	/**
	 * Column name
	 */
	static final String KEY_ID = "_id";
	
	/**
	 * Column name
	 */
	static final String KEY_KEY = "key";
	
	/**
	 * Column name
	 */
	static final String KEY_VALUE = "value";
	
	/**
	 * Column name
	 */
	static final String KEY_READ_ONLY = "readonly";
	
	/**
	 * Column name
	 */
	static final String KEY_REBOOT = "reboot";

	// ---------------------------------------------------------------------------
	// Parameters which can be modified by the end user. These parameters may be
	// displayed at UI level (e.g. settings application).
	// ---------------------------------------------------------------------------
	
	/**
	 * Service activation parameter which indicates if the RCS service may be started or not 
	 */
	static final String SERVICE_ACTIVATED = "ServiceActivated";
	
	/**
	 * Roaming authorization parameter which indicates if the RCS service may be used or not in roaming 
	 */
	static final String ROAMING_AUTHORIZED = "RoamingAuthorized";
	
	/**
	 * Ringtone which is played when a social presence sharing invitation is received 
	 */
	static final String PRESENCE_INVITATION_RINGTONE = "PresenceInvitationRingtone";

	/**
	 * Vibrate or not when a social presence sharing invitation is received 
	 */
	static final String PRESENCE_INVITATION_VIBRATE = "PresenceInvitationVibrate";

	/**
	 * Ringtone which is played when a content sharing invitation is received 
	 */
	static final String CSH_INVITATION_RINGTONE = "CShInvitationRingtone";

	/**
	 * Vibrate or not when a content sharing invitation is received 
	 */
	static final String CSH_INVITATION_VIBRATE = "CShInvitationVibrate";

	/**
	 * Make a beep or not when content sharing is available during a call 
	 */
	static final String CSH_AVAILABLE_BEEP = "CShAvailableBeep";

	/**
	 * Video format for video share 
	 */
	static final String CSH_VIDEO_FORMAT = "CShVideoFormat";
	
	/**
	 * Video size for video share 
	 */
	static final String CSH_VIDEO_SIZE = "CShVideoSize";
	
	/**
	 * Ringtone which is played when a file transfer invitation is received 
	 */
	static final String FILETRANSFER_INVITATION_RINGTONE = "FileTransferInvitationRingtone";

	/**
	 * Vibrate or not when a file transfer invitation is received 
	 */
	static final String FILETRANSFER_INVITATION_VIBRATE = "FileTransferInvitationVibrate";
	
	/**
	 * Ringtone which is played when a chat invitation is received 
	 */
	static final String CHAT_INVITATION_RINGTONE = "ChatInvitationRingtone";

	/**
	 * Vibrate or not when a chat invitation is received 
	 */
	static final String CHAT_INVITATION_VIBRATE = "ChatInvitationVibrate";

	/**
	 * Auto-accept mode for chat invitation 
	 */
	static final String CHAT_INVITATION_AUTO_ACCEPT = "ChatInvitationAutoAccept";
	
	/**
	 * Predefined freetext 
	 */
	static final String FREETEXT1 = "Freetext1";
	
	/**
	 * Predefined freetext 
	 */
	static final String FREETEXT2 = "Freetext2";
	
	/**
	 * Predefined freetext 
	 */
	static final String FREETEXT3 = "Freetext3";

	/**
	 * Predefined freetext 
	 */
	static final String FREETEXT4 = "Freetext4";
	
	// ---------------------------------------------------------------------------
	// Parameters which CAN'T be modified by the end user. These parameters are
	// mainly used by UI.
	// ---------------------------------------------------------------------------

	/**
	 * Max number of RCS contacts or enriched contacts
	 */
	static final String MAX_RCS_CONTACTS = "MaxRcsContacts";
	
	/**
	 * Max photo-icon size
	 */
	static final String MAX_PHOTO_ICON_SIZE = "MaxPhotoIconSize";

	/**
	 * Max length of the freetext
	 */
	static final String MAX_FREETXT_LENGTH = "MaxFreetextLength";

	/**
	 * Max number of participants in a group chat
	 */
	static final String MAX_CHAT_PARTICIPANTS = "MaxChatParticipants";
	
	/**
	 * Max length of a chat message
	 */
	static final String MAX_CHAT_MSG_LENGTH = "MaxChatMessageLength";

	/**
	 * Idle duration of a chat session
	 */
	static final String CHAT_IDLE_DURATION = "ChatIdleDuration";

	/**
	 * Max size of a file transfer
	 */
	static final String MAX_FILE_TRANSFER_SIZE = "MaxFileTransferSize";

	/**
	 * Max size of an image share
	 */
	static final String MAX_IMAGE_SHARE_SIZE = "MaxImageShareSize";

	/**
	 * Max duration of a video share
	 */
	static final String MAX_VIDEO_SHARE_DURATION = "MaxVideoShareDuration";

	/**
	 * Max number of simultaneous chat sessions
	 */
	static final String MAX_CHAT_SESSIONS = "MaxChatSessions";

	/**
	 * Max number of simultaneous file transfer sessions
	 */
	static final String MAX_FILE_TRANSFER_SESSIONS = "MaxFileTransferSessions";

	/**
	 * Activate or not anonymous fetch service
	 */
	static final String ANONYMOUS_FETCH_SERVICE = "AnonymousFetchService";

	/**
	 * Activate or not chat service
	 */
	static final String CHAT_SERVICE = "ChatService";

	/**
	 * Activate or not SMS fallback service
	 */
	static final String SMS_FALLBACK_SERVICE = "SmsFallbackService";
	
	// ---------------------------------------------------------------------------
	// Parameters of the end user profile. In case of manual provisionning these
	// parameters may be displayed at UI level (e.g. settings application).
	// ---------------------------------------------------------------------------

	/**
	 * IMS username or username part of the IMPU (for HTTP Digest only)
	 */
	static final String USERPROFILE_IMS_USERNAME = "ImsUsername";

	/**
	 * IMS display name 
	 */
	static final String USERPROFILE_IMS_DISPLAY_NAME = "ImsDisplayName";

	/**
	 * IMS private URI or IMPI (for HTTP Digest only) 
	 */
	static final String USERPROFILE_IMS_PRIVATE_ID = "ImsPrivateId";
	
	/**
	 * IMS password (for HTTP Digest only) 
	 */
	static final String USERPROFILE_IMS_PASSWORD = "ImsPassword";
	
	/**
	 * IMS home domain (for HTTP Digest only)
	 */
	static final String USERPROFILE_IMS_HOME_DOMAIN = "ImsHomeDomain";
	
	/**
	 * P-CSCF or outbound proxy address & port
	 */
	static final String USERPROFILE_IMS_PROXY = "ImsOutboundProxyAddr";
	
	/**
	 * XDM server address & port
	 */
	static final String USERPROFILE_XDM_SERVER = "XdmServerAddr";

	/**
	 * XDM server login (for HTTP Digest only)
	 */
	static final String USERPROFILE_XDM_LOGIN= "XdmServerLogin";

	/**
	 * XDM server password (for HTTP Digest only)
	 */
	static final String USERPROFILE_XDM_PASSWORD = "XdmServerPassword";

	/**
	 * IM conference URI for group chat session
	 */
	static final String USERPROFILE_IM_CONF_URI = "ImConferenceUri";

	// ---------------------------------------------------------------------------
	// Parameters which CAN'T be modified by the end user. These parameters are
	// used by the stack only.
	// ---------------------------------------------------------------------------
	
	/**
	 * Polling period used before each IMS connection attempt
	 */
	static final String IMS_CONNECTION_POLLING_PERIOD = "ImsConnectionPollingPeriod";

	/**
	 * Polling period used before each IMS service check (e.g. test subscription state for presence service)
	 */
	static final String IMS_SERVICE_POLLING_PERIOD = "ImsServicePollingPeriod";
	
	/**
	 * Default SIP port 
	 */
	static final String SIP_DEFAULT_PORT = "SipListeningPort";
	
	/**
	 * Default SIP protocol 
	 */
	static final String SIP_DEFAULT_PROTOCOL = "SipDefaultProtocol";

	/**
	 * SIP transaction timeout used to wait a SIP response
	 */
	static final String SIP_TRANSACTION_TIMEOUT = "SipTransactionTimeout";
	
	/**
	 * Default TCP port for MSRP session 
	 */
	static final String MSRP_DEFAULT_PORT = "DefaultMsrpPort";
	
	/**
	 * Default UDP port for RTP session 
	 */
	static final String RTP_DEFAULT_PORT = "DefaultRtpPort";

	/**
	 * MSRP transaction timeout used to wait MSRP response
	 */
	static final String MSRP_TRANSACTION_TIMEOUT = "MsrpTransactionTimeout";	
	
	/**
	 * Registration expire period 
	 */
	static final String REGISTER_EXPIRE_PERIOD = "RegisterExpirePeriod";
	
	/**
	 * Publish expire period
	 */
	static final String PUBLISH_EXPIRE_PERIOD = "PublishExpirePeriod";
	
	/**
	 * Anonymous refresh timeout used to decide when to refresh the internal cache
	 */
	static final String ANONYMOUS_FETCH_REFRESH_TIMEOUT = "AnonymousFetchRefrehTimeout";
	
	/**
	 * Revoke timeout  
	 */
	static final String REVOKE_TIMEOUT = "RevokeTimeout";
	
	/**
	 * IMS authentication procedure (GIBA or HTTP Digest)
	 */
	static final String IMS_AUTHENT_MODE = "ImsAuhtenticationProcedure";
	
	/**
	 * Activate or not Tel-URI format
	 */
	static final String TEL_URI_FORMAT = "TelUriFormat";
	
	/**
	 * Ringing session period. At the end of the period the session is cancelled  
	 */
	static final String RINGING_SESSION_PERIOD = "RingingPeriod";
	
	/**
	 * Subscribe expiration timeout
	 */
	static final String SUBSCRIBE_EXPIRE_PERIOD = "SubscribeExpirePeriod";
	
	/**
	 * "Is-composing" timeout for chat service
	 */
	static final String IS_COMPOSING_TIMEOUT = "IsComposingTimeout";
	
	/**
	 * SIP session refresh expire period
	 */
	static final String SESSION_REFRESH_EXPIRE_PERIOD = "SessionRefreshExpirePeriod";
	
	
	/**
	 * Activate or not rich call mode. If richcall mode is activated, a content sharing
	 * may be initiated only during a call.
	 */
	static final String RICHCALL_MODE = "Richcall";

	/**
	 * Activate or not permanent state mode
	 */
	static final String PERMANENT_STATE_MODE = "PermanentState";

	/**
	 * Activate or not the logger
	 */
	static final String TRACE_ACTIVATION = "TraceActivation";

	/**
	 * Logger trace level
	 */
	static final String TRACE_LEVEL = "TraceLevel";

	/**
	 * Activate or not the SIP trace
	 */
	static final String SIP_TRACE_ACTIVATION = "SipTraceActivation";

	/**
	 * Activate or not the media trace
	 */
	static final String MEDIA_TRACE_ACTIVATION = "MediaTraceActivation";
}
