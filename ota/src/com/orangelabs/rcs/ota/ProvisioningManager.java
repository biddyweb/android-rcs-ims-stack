package com.orangelabs.rcs.ota;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import com.orangelabs.rcs.platform.AndroidFactory;
import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.provider.settings.RcsSettingsData;
import com.orangelabs.rcs.service.api.client.ClientApi;
import com.orangelabs.rcs.utils.logger.Logger;

/**
 * Provisioning manager based on the following specifications:<br>
 * - Rich Communication Suite Release 2 Management Objects, Version 2.0, 14 February 2011.<br>
 * - RCS-e Advanced Communications, Services and Client Specification, Version 1.1, April 08, 2011.<br>
 * - 3GPP TS 24.167 V10.2.0 (2011-03).<br>
 * - 3GPP TS 24.229 V8.6.0 (2008-12).
 * 
 * @author jexa7410
 */
public class ProvisioningManager {
	/**
	 * Default configuration server URL
	 */
	public final static String DEFAULT_CONFIG_SERVER = "https://rcse-config.24423.com";
	
    /**
	 * The logger
	 */
    private Logger logger = Logger.getLogger(this.getClass().getName());
		
    /**
     * Constructor
     */
    public ProvisioningManager() {
    }
    
    /**
     * Download config file over HTTP
     * 
     * @param completeUrl URL to be downloaded
     * @return Document
     * @throws IOException
     */
    public byte[] downloadConfigFile(String completeUrl) throws IOException {
		if (logger.isActivated()) {
			logger.debug("Download configuration file " + completeUrl);
		}

		URL url = new URL(completeUrl);
        URLConnection ucon = url.openConnection();
        InputStream is = ucon.getInputStream();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        
        byte[] buffer = new byte[1024];
        int readed = 0;
        while((readed = is.read(buffer)) > 0 ) {
            bos.write(buffer, 0, readed);
        }
        byte[] result = bos.toByteArray();
        bos.close();
        
        return result;
    }    

    /**
     * Check params
     * 
     * @param params Table of parameters
     */
    public void checkParams(Hashtable<String, Parameter> params) {
		if (logger.isActivated()) {
			logger.debug("Check received parameters");
		}

		// Get the config version
		Parameter version = params.get("/VERS/version");
		if (logger.isActivated()) {
			logger.debug("Config version is " + version.getValue());
		}

		// Check the application ID
		Parameter appId = params.get("/APPLICATION/appid");
		if ((appId != null) && (!appId.getValue().equals("RCS-e"))) {
			if (logger.isActivated()) {
				logger.debug("Not a RCS-e document");
			}
			return;
		}

		// Check parameters
		Vector<String> v = new Vector<String>(params.keySet());
	    Collections.sort(v);
	    Iterator<String> it = v.iterator();
	    while(it.hasNext()) {		
    		String key = it.next();
    		Parameter param = params.get(key);
    		String value = param.getValue();
    		if (logger.isActivated()) {
    			logger.debug(param.toString());
    		}
    		
    		// Naming convention:
    		// - all parameter types are in uppercase
    		// - all parameter names are in lowercase
    		// - Each parameter starts with a '/'
    		// - Each parameter list ends with a '/'
    		
    		
    		// ---------------------------------------------------------------------
    		// Type: urn:oma:mo:ext-3gpp-ims:1.0
    		// ---------------------------------------------------------------------
    		if (key.equals("/IMS/name")) {
    			// The Name leaf is a name for the 3GPP_IMS settings.
    			// Not used
    		} else
    		if (key.startsWith("/IMS/conrefs/")) {
    			// List of network access point objects
    			// Not used
    		} else
    		if (key.equals("/IMS/pdp_contextoperpref")) {
    			// The PDP_ContextOperPref leaf indicates an operatorâ€™s preference to have a dedicated PDP context for SIP signalling.
    			// Values: 0, 1
    			// 0 â€“ Indicates that the operator has no preference for a dedicated PDP context for SIP signalling.
    			// 1 â€“ Indicates that the operator has preference for a dedicated PDP context for SIP signalling.
    			// Not used
    		} else
    		if (key.equals("/IMS/p-cscf_address")) {
				// The P-CSCF_Address leaf defines an FQDN or an IPv4 address to an IPv4 P-CSCF.
    			// - Values: 'FQDN', 'IPv4', 'IPv6'.
	    		if (logger.isActivated()) {
	    			logger.debug("Update parameter " + key + ": " + param.getValue());
	    		}
    			RcsSettings.getInstance().writeParameter(RcsSettingsData.USERPROFILE_IMS_PROXY_MOBILE, param.getValue());
    			RcsSettings.getInstance().writeParameter(RcsSettingsData.USERPROFILE_IMS_PROXY_WIFI, param.getValue());
			} else
			if (key.equals("/IMS/timer_t1")) {
				// The SIP timer T1 in milliseconds.
	    		if (logger.isActivated()) {
	    			logger.debug("Update parameter " + key + ": " + param.getValue());
	    		}
    			RcsSettings.getInstance().writeParameter(RcsSettingsData.SIP_TIMER_T1, param.getValue());
			} else
			if (key.equals("/IMS/timer_t2")) {
				// The SIP timer T2 in milliseconds.
	    		if (logger.isActivated()) {
	    			logger.debug("Update parameter " + key + ": " + param.getValue());
	    		}
    			RcsSettings.getInstance().writeParameter(RcsSettingsData.SIP_TIMER_T2, param.getValue());
			} else
			if (key.equals("/IMS/timer_t4")) {
				// The SIP timer T4 in milliseconds.
	    		if (logger.isActivated()) {
	    			logger.debug("Update parameter " + key + ": " + param.getValue());
	    		}
    			RcsSettings.getInstance().writeParameter(RcsSettingsData.SIP_TIMER_T4, param.getValue());
			} else
			if (key.equals("/IMS/private_user_identity")) {
				// The format of the private user identity is defined by 3GPP TS 23.003.
				// Sample: 234150999999999@ims.mnc015.mcc234.3gppnetwork.org
	    		if (logger.isActivated()) {
	    			logger.debug("Update parameter " + key + ": " + param.getValue());
	    		}
    			RcsSettings.getInstance().writeParameter(RcsSettingsData.USERPROFILE_IMS_PRIVATE_ID, param.getValue());
			} else
			if (key.startsWith("/IMS/public_user_identity_list/")) {
				// List of public user identities defined by 3GPP TS 23.003.
				// Sample: sip:234150999999999@ims.mnc015.mcc234.3gppnetwork.org
	    		if (logger.isActivated()) {
	    			logger.debug("Update parameter " + key + ": " + param.getValue());
	    		}
    			RcsSettings.getInstance().writeParameter(RcsSettingsData.USERPROFILE_IMS_USERNAME, param.getValue());
			} else
			if (key.equals("/IMS/home_network_domain_name")) {
				// The home network domain name specified in 3GPP TS 24.229.
				// Sample: sip: 234150999999999@ims.mnc015.mcc234.3gppnetwork.org
	    		if (logger.isActivated()) {
	    			logger.debug("Update parameter " + key + ": " + param.getValue());
	    		}
    			RcsSettings.getInstance().writeParameter(RcsSettingsData.USERPROFILE_IMS_HOME_DOMAIN, param.getValue());
			} else
			if (key.startsWith("/IMS/icsi_list")) {
				// List of IMS communication service identifiers that are supported by a
				// subscriberâ€˜s network for that subscriber.
				// Not used
			} else
			if (key.startsWith("/IMS/lbo_p-cscf_address/address")) {
				// List of P-CSCFs
	    		if (logger.isActivated()) {
	    			logger.debug("Update parameter " + key + ": " + param.getValue());
	    		}
    			RcsSettings.getInstance().writeParameter(RcsSettingsData.USERPROFILE_IMS_PROXY_MOBILE, param.getValue());
    			RcsSettings.getInstance().writeParameter(RcsSettingsData.USERPROFILE_IMS_PROXY_WIFI, param.getValue());
			} else
			if (key.equals("/IMS/resource_allocation_mode")) {
				// indicates whether UE initiates resource allocation for the media controlled
				// by IM CN subsystem for all IMS sessions not covered by any "ICSI Resource Allocation Mode",
				// when both UE and network can initiate resource allocation.
				// Values: 0, 1
				// 0 â€“ Indicates that the UE attempts to initiate resource allocation for the media controlled by IM CN subsystem.
				// 1 â€“ Indicates that the UE does not attempt to initiate resource allocation for the media controlled by IM CN subsystem.
				// Not used
			} else
			if (key.equals("/IMS/voice_domain_preference_e_utran")) {
				// Indicates network operator's preference for selection of the domain to be used for voice
				// communication services by the UE.
				// Values: 1, 2, 3, 4
				// 1 â€“ Indicates that the UE does not attempt to initiate voice sessions over the IM CN Subsystem
				//	   using an E-UTRAN bearer. This value equates to "CS Voice only" as described in 3GPP TS 23.221.
				// 2 â€“ Indicates that the UE preferably attempts to use the CS domain to originate voice sessions.
				//     In addition, a UE, in accordance with TS 24.292, upon receiving a request for a session including
				//     voice, preferably attempts to use the CS domain for the audio media stream. This value equates to
				//     "CS Voice preferred, IMS PS Voice as secondary" as described in 3GPP TS 23.221.
				// 3 â€“ Indicates that the UE preferably attempts to use the IM CN Subsystem using an E-UTRAN bearer to
				//     originate sessions including voice. In addition, a UE, in accordance with TS 24.292, upon receiving
				//     a request for a session including voice, preferably attempts to use an E-UTRAN bearer for the audio
				//	   media stream. This value equates to "IMS PS Voice preferred, CS Voice as secondary" as described in
				//     3GPP TS 23.221.
				// 4 â€“ Indicates that the UE attempts to initiate voice sessions over IM CN Subsystem using an E-UTRAN bearer.
				//     In addition, a UE, upon receiving a request for a session including voice, attempts to use an E-UTRAN
				//     bearer for all the the audio media stream(s). This value equates to "IMS PS Voice only" as described
				//     in 3GPP TS 23.221.
				// Not used
			} else
			if (key.equals("/IMS/sms_over_ip_networks_indication")) {
				// Indicates network operator's preference for selection of the domain to be used for short message service
				// (SMS) originated by the UE.
				// Values: 0, 1
				// 0 â€“ Indicates that the SMS service is not to be invoked over the IP networks.
				// 1 â€“ Indicates that the SMS service is preferred to be invoked over the IP networks.
				// Not used
			} else
			if (key.equals("/IMS/keep_alive_enabled")) {
				// Indicates whether the UE sends keep alives.
				// Values: 0, 1
				// 0 â€“ Indicates that the UE does not send keep alives.
				// 1 â€“ Indicates that the UE is to send keep alives.
	    		if (logger.isActivated()) {
	    			logger.debug("Update parameter " + key + ": " + param.getValue());
	    		}
				if (value.equals("1")) {
	    			RcsSettings.getInstance().writeParameter(RcsSettingsData.SIP_KEEP_ALIVE, RcsSettingsData.TRUE);					
				} else {
	    			RcsSettings.getInstance().writeParameter(RcsSettingsData.SIP_KEEP_ALIVE, RcsSettingsData.FALSE);					
				}
			} else
			if (key.equals("/IMS/voice_domain_preference_utran")) {
				// Indicates network operator's preference for selection of the domain to be used for voice communication
				// services by the UE.
				// Values: 1, 2, 3
				// 1 â€“ Indicates that the UE does not attempt to initiate voice sessions over the IM CN Subsystem using an
				//     UTRAN PS bearer. This value equates to "CS Voice only" as described in 3GPP TS 23.221.
				// 2 â€“ Indicates that the UE preferably attempts to use the CS domain to originate voice sessions. In addition,
				//     a UE, in accordance with 3GPP TS 24.292, upon receiving a request for a session including voice,
				//     preferably attempts to use the CS domain for the audio media stream. This value equates to
				//     "CS Voice preferred, IMS PS Voice as secondary" as described in 3GPP TS 23.221.
				// 3 â€“ Indicates that the UE preferably attempts to use the IM CN Subsystem using an UTRAN PS bearer to originate
				//     sessions including voice. In addition, a UE, in accordance with 3GPP TS 24.292, upon receiving a
				//     request for a session including voice, preferably attempts to use an UTRAN PS bearer for the audio media
				//     stream. This value equates to "IMS PS Voice preferred, CS Voice as secondary" as described in 3GPP TS 23.221.
				// Not used
			} else
			if (key.equals("/IMS/mobility_management_ims_voice_termination")) {
				// Indicates whether the UE mobility management performs additional procedures as specified in 3GPP TS 24.008
				// and 3GPP TS 24.301 to support terminating access domain selection by the network.
				// Values: 0, 1
				// 0 â€“ Mobility Management for IMS Voice Termination disabled.
				// 1 â€“ Mobility Management for IMS Voice Termination enabled.
				// Not used
			} else
			if (key.equals("/IMS/regretrybasetime")) {
				// Represents the value of the base-time parameter in seconds of the algorithm defined in subclause 4.5 of RFC 5626.
				// TODO
			} else				
			if (key.equals("/IMS/regretrymaxtime")) {
				// Represents the value of the max-time parameter in seconds of the algorithm defined in subclause 4.5 of RFC 5626.
				// TODO
			} else				
			if (key.equals("/IMS/phonecontext_list/")) {
				// List of phone-context parameter values for other local numbers, than geo-local or home-local numbers, as defined
				// in subclause 5.1.2A.1.5 of 3GPP TS 24.229.
				// TODO
			} else
    		if (key.equals("/IMS/servcappresentity/voicecall")) {
				// Voice call capability.
    			// Values: 0, 1
    			// 0 - Indicates authorization
    			// 1 - Indicates non authorization
    			// Not used
    		} else
    		if (key.equals("/IMS/servcappresentity/chat")) {
				// Chat capability.
    			// Values: 0, 1
    			// 0 - Indicates authorization
    			// 1 - Indicates non authorization
	    		if (logger.isActivated()) {
	    			logger.debug("Update parameter " + key + ": " + param.getValue());
	    		}
				if (value.equals("0")) {
	    			RcsSettings.getInstance().writeParameter(RcsSettingsData.CAPABILITY_IM_SESSION, RcsSettingsData.TRUE);
				} else {
	    			RcsSettings.getInstance().writeParameter(RcsSettingsData.CAPABILITY_IM_SESSION, RcsSettingsData.FALSE);
				}
    		} else
    		if (key.equals("/IMS/servcappresentity/sendsms")) {
				// Chat capability.
    			// Values: 0, 1
    			// 0 - Indicates authorization
    			// 1 - Indicates non authorization
    			// Not used
    		} else
    		if (key.equals("/IMS/servcappresentity/filetransfer")) {
				// FT capability.
    			// Values: 0, 1
    			// 0 - Indicates authorization
    			// 1 - Indicates non authorization
	    		if (logger.isActivated()) {
	    			logger.debug("Update parameter " + key + ": " + param.getValue());
	    		}
				if (value.equals("0")) {
	    			RcsSettings.getInstance().writeParameter(RcsSettingsData.CAPABILITY_FILE_TRANSFER, RcsSettingsData.TRUE);
				} else {
	    			RcsSettings.getInstance().writeParameter(RcsSettingsData.CAPABILITY_FILE_TRANSFER, RcsSettingsData.FALSE);
				}
    		} else
    		if (key.equals("/IMS/servcappresentity/videoshare")) {
				// Image share capability.
    			// Values: 0, 1
    			// 0 - Indicates authorization
    			// 1 - Indicates non authorization
	    		if (logger.isActivated()) {
	    			logger.debug("Update parameter " + key + ": " + param.getValue());
	    		}
				if (value.equals("0")) {
	    			RcsSettings.getInstance().writeParameter(RcsSettingsData.CAPABILITY_VIDEO_SHARING, RcsSettingsData.TRUE);
				} else {
	    			RcsSettings.getInstance().writeParameter(RcsSettingsData.CAPABILITY_VIDEO_SHARING, RcsSettingsData.FALSE);
				}
    		} else    			
    		if (key.equals("/IMS/servcappresentity/imageshare")) {
				// Image share capability.
    			// Values: 0, 1
    			// 0 - Indicates authorization
    			// 1 - Indicates non authorization
	    		if (logger.isActivated()) {
	    			logger.debug("Update parameter " + key + ": " + param.getValue());
	    		}
				if (value.equals("0")) {
	    			RcsSettings.getInstance().writeParameter(RcsSettingsData.CAPABILITY_IMAGE_SHARING, RcsSettingsData.TRUE);
				} else {
	    			RcsSettings.getInstance().writeParameter(RcsSettingsData.CAPABILITY_IMAGE_SHARING, RcsSettingsData.FALSE);
				}
    		} else    			
				
    		// ---------------------------------------------------------------------
			// Type: urn:gsma:mo:rcs:2.0:IMS-ext
    		// ---------------------------------------------------------------------
			if (key.equals("/APPAUTH/authtype")) {
				// Describe the type of IMS authentication for the user. Values: 'EarlyIMS', 'AKA', 'Digest'.
	    		if (logger.isActivated()) {
	    			logger.debug("Update parameter " + key + ": " + param.getValue());
	    		}
				if (param.getValue().equalsIgnoreCase("EarlyIMS")) {
					RcsSettings.getInstance().writeParameter(RcsSettingsData.IMS_AUTHENT_PROCEDURE_MOBILE, RcsSettingsData.GIBA_AUTHENT);
				} else {
					RcsSettings.getInstance().writeParameter(RcsSettingsData.IMS_AUTHENT_PROCEDURE_MOBILE, RcsSettingsData.DIGEST_AUTHENT);
				}
			} else
			if (key.equals("/APPAUTH/realm")) {
				// IMS mode for authentication is 'Digest', this leaf node exists and contains the realm URL affected to the user. 
	    		if (logger.isActivated()) {
	    			logger.debug("Update parameter " + key + ": " + param.getValue());
	    		}
    			RcsSettings.getInstance().writeParameter(RcsSettingsData.USERPROFILE_IMS_HOME_DOMAIN, param.getValue());
			} else
			if (key.equals("/APPAUTH/username")) {
				// IMS mode for authentication is 'Digest', this leaf node exists and contains the realm
				// User name affected to the user for IMS authorization/registration. 
	    		if (logger.isActivated()) {
	    			logger.debug("Update parameter " + key + ": " + param.getValue());
	    		}
    			RcsSettings.getInstance().writeParameter(RcsSettingsData.USERPROFILE_IMS_USERNAME, param.getValue());
			} else
			if (key.equals("/APPAUTH/userpwd")) {
				// IMS mode for authentication is 'Digest', this leaf node exists and contains the User
				// password affected to the user for IMS authorization/registration.
	    		if (logger.isActivated()) {
	    			logger.debug("Update parameter " + key + ": " + param.getValue());
	    		}
    			RcsSettings.getInstance().writeParameter(RcsSettingsData.USERPROFILE_IMS_PASSWORD, param.getValue());
			} else
    		if (key.equals("/IMS/naturlfmt")) {
    			// Indicates which format (SIP URL or Tel URL) is to be used in case the callee numbering is dialled
    			// in national format.
    			// Values: 0, 1
    			// 0: Tel URL format is to be used (example: 0234578901; phone-context=<home-domain-name>)
    			// 1: SIP URL format is to be used (example: 0234578901@operator.com; user=phone)    			
	    		if (logger.isActivated()) {
	    			logger.debug("Update parameter " + key + ": " + param.getValue());
	    		}
				if (value.equals("0")) {
	    			RcsSettings.getInstance().writeParameter(RcsSettingsData.TEL_URI_FORMAT, RcsSettingsData.TRUE);
				} else {
	    			RcsSettings.getInstance().writeParameter(RcsSettingsData.TEL_URI_FORMAT, RcsSettingsData.FALSE);
				}
    		} else
    		if (key.equals("/IMS/inturlfmt")) {
    			// Indicates which format (SIP URL or Tel URL) is to be used in case the callee numbering is
    			// dialled in international format.
    			// Values: 0, 1
    			// 0: Tel URL format is to be used (example: +1234578901)
    			// 1: SIP URL format is to be used (example: +1234578901@operator.com; user=phone)
	    		if (logger.isActivated()) {
	    			logger.debug("Update parameter " + key + ": " + param.getValue());
	    		}
				if (value.equals("0")) {
	    			RcsSettings.getInstance().writeParameter(RcsSettingsData.TEL_URI_FORMAT, RcsSettingsData.TRUE);
				} else {
	    			RcsSettings.getInstance().writeParameter(RcsSettingsData.TEL_URI_FORMAT, RcsSettingsData.FALSE);
				}
    		} else
    		if (key.equals("/IMS/q-value")) {
    			// Indicates the Q-value to be put in the Contact header of the Register method. This is useful
    			// in case of multi-device for forking algorithm. Values: '0.1', '0.2', '0.3', '0.4', '0.5', '0.6',
    			// '0.7', '0.8', '0.9', '1.0'
				// Not used
    		} else
    		if (key.startsWith("/IMS/secondarydevicepar/")) {
    			// Presence of this interior node indicates that the RCS2 device is a secondary device. This node
    			// is not instantiated in case of primary device.
    			// Not used
    		} else    			
    		if (key.equals("/IMS/maxsizeimageshare")) {
				// Maximum authorized size of the content that can be sent in an Image Share session in bytes.
    			// Value: <content maximum size in bytes> A value equals to 0 means no limitation.
	    		if (logger.isActivated()) {
	    			logger.debug("Update parameter " + key + ": " + param.getValue());
	    		}
    			RcsSettings.getInstance().writeParameter(RcsSettingsData.MAX_IMAGE_SHARE_SIZE, param.getValue());
    		} else    			
    		if (key.equals("/IMS/maxtimevideoshare")) {
				// Maximum authorized duration time for a Video Share session in seconds.
    			// Value: <Timer value in seconds> A value equals to 0 means no limitation.
	    		if (logger.isActivated()) {
	    			logger.debug("Update parameter " + key + ": " + param.getValue());
	    		}
    			RcsSettings.getInstance().writeParameter(RcsSettingsData.MAX_VIDEO_SHARE_DURATION, param.getValue());
    		} else    			
    			
    		// ---------------------------------------------------------------------
        		// Type: urn:gsma:mo:rcs:2.0:Presence-ext
    		// ---------------------------------------------------------------------
    			
    		if (key.equals("/PRESENCE/usepresence")) {
    			// Use presence service.
    			// Values:
    			// - 0, the presence related features are disabled
    			// - 1, the presence related features are enabled
	    		if (logger.isActivated()) {
	    			logger.debug("Update parameter " + key + ": " + param.getValue());
	    		}
				if (value.equals("0")) {
	    			RcsSettings.getInstance().writeParameter(RcsSettingsData.CAPABILITY_SOCIAL_PRESENCE, RcsSettingsData.FALSE);
				} else {
	    			RcsSettings.getInstance().writeParameter(RcsSettingsData.CAPABILITY_SOCIAL_PRESENCE, RcsSettingsData.TRUE);
				}
    		} else	
    		if (key.equals("/PRESENCE/availabilityauth")) {
				// Authorization for the Presence UA to use Availability status	feature.
    			// Values: 0, 1
    			// 0 - Indicates that the use of Availability status is not authorized
    			// 1 - Indicates that the use of Availability status is authorized    			
    			// Not used
    		} else    			
    		if (key.startsWith("/PRESENCE/favlink/")) {
    			// Determines the operator policy for Favorite Link instantiation in the local presence document
    			// of the presentity. Values: 'Auto', 'Man', 'Auto+Man'.
    			// Not used
    		} else
    		if (key.equals("/PRESENCE/iconmaxsize")) {
    			// Represent the maximum authorized size for an icon in bytes.
	    		if (logger.isActivated()) {
	    			logger.debug("Update parameter " + key + ": " + param.getValue());
	    		}
    			RcsSettings.getInstance().writeParameter(RcsSettingsData.MAX_PHOTO_ICON_SIZE, param.getValue());
    		} else
    		if (key.equals("/PRESENCE/notemaxsize")) {
    			// Represent the maximum authorized size for a note in characters
	    		if (logger.isActivated()) {
	    			logger.debug("Update parameter " + key + ": " + param.getValue());
	    		}
    			RcsSettings.getInstance().writeParameter(RcsSettingsData.MAX_FREETXT_LENGTH, param.getValue());
    		} else
    		if (key.equals("/PRESENCE/servcapwatch/fetchaut")) {
    			// Represent operator setting of parameters linked with watcher behaviour of the device
    			// Values: 0, 1
    			// 0- Indicates that this automatic fetch is not authorized
    			// 1- Indicates that this automatic fetch is authorized
    			// TODO
    		} else
    		if (key.equals("/PRESENCE/servcapwatch/contactcappresaut")) {
    			// Indicates if the device is authorized to display to the user the ability of the user
    			// contacts declared in the local address book to share Social Presence Information
    			// Values: 0, 1
    			// 0- Indicates that rendering is not authorized
    			// 1- Indicates that rendering is authorized
    			// TODO
    		} else
    		if (key.equals("/PRESENCE/servcappresentity/watcherfetchaut")) {
    			// Indicates if watchers are authorized to â€œanonymousâ€� fetch service capabilities of the user
    			// Values: 0, 1
    			// 0- Indicates that watchers are authorized to fetch user service capabilities
    			// 1- Indicates that watchers are not authorized to fetch user service capabilities
    			// TODO
    		} else
    		if (key.equals("/PRESENCE/publishtimer")) {
    			// Indicates the timer value for the Presence Publish refreshment in seconds
	    		if (logger.isActivated()) {
	    			logger.debug("Update parameter " + key + ": " + param.getValue());
	    		}
    			RcsSettings.getInstance().writeParameter(RcsSettingsData.PUBLISH_EXPIRE_PERIOD, param.getValue());
    		} else
    			
    		// ---------------------------------------------------------------------
        		// Type: urn:gsma:mo:rcs:2.0:xdm-ext
    		// ---------------------------------------------------------------------

    		if (key.equals("/XDMS/revoketimer")) {
    			// Indicates the duration a contact should remain in the RCS revocation list in seconds
	    		if (logger.isActivated()) {
	    			logger.debug("Update parameter " + key + ": " + param.getValue());
	    		}
    			RcsSettings.getInstance().writeParameter(RcsSettingsData.REVOKE_TIMEOUT, param.getValue());
    		} else
    		if (key.equals("/XDMS/xcaprooturi")) {
    			// XDMS root URI
	    		if (logger.isActivated()) {
	    			logger.debug("Update parameter " + key + ": " + param.getValue());
	    		}
    			RcsSettings.getInstance().writeParameter(RcsSettingsData.USERPROFILE_XDM_SERVER, param.getValue());
    		} else
    		if (key.equals("/XDMS/xcapauthenticationusername")) {
    			// XDMS account login
	    		if (logger.isActivated()) {
	    			logger.debug("Update parameter " + key + ": " + param.getValue());
	    		}
    			RcsSettings.getInstance().writeParameter(RcsSettingsData.USERPROFILE_XDM_LOGIN, param.getValue());
    		} else
    		if (key.equals("/XDMS/xcapauthenticationsecret")) {
    			// XDMS account password
	    		if (logger.isActivated()) {
	    			logger.debug("Update parameter " + key + ": " + param.getValue());
	    		}
    			RcsSettings.getInstance().writeParameter(RcsSettingsData.USERPROFILE_XDM_PASSWORD, param.getValue());
    		} else
    		if (key.equals("/XDMS/xcapauthenticationtype")) {
    			// XDMS authentication type
    			// Not used
    		} else

    		// ---------------------------------------------------------------------
    		// Type: urn:gsma:mo:rcs:2.0:im-ext
    		// ---------------------------------------------------------------------
    			
    		if (key.equals("/IM/chatauth")) {
    			// Represent the authorization for user to use Chat service
    			// Values: 0, 1
    			// 0- Indicates that Chat service is disabled
    			// 1- Indicates that Chat service is enabled
	    		if (logger.isActivated()) {
	    			logger.debug("Update parameter " + key + ": " + param.getValue());
	    		}
				if (value.equals("0")) {
	    			RcsSettings.getInstance().writeParameter(RcsSettingsData.CAPABILITY_IM_SESSION, RcsSettingsData.TRUE);
				} else {
	    			RcsSettings.getInstance().writeParameter(RcsSettingsData.CAPABILITY_IM_SESSION, RcsSettingsData.FALSE);
				}
    		} else
    		if (key.equals("/IM/smsfallbackauth")) {
    			// Represent the authorization for the device to propose automatically a SMS
    			// fallback in case of chat initiation failure.
    			// Values: 0, 1
    			// 0- Indicates authorization is ok
    			// 1- Indicates authorization is non ok
	    		if (logger.isActivated()) {
	    			logger.debug("Update parameter " + key + ": " + param.getValue());
	    		}
				if (value.equals("0")) {
	    			RcsSettings.getInstance().writeParameter(RcsSettingsData.SMS_FALLBACK_SERVICE, RcsSettingsData.TRUE);
				} else {
	    			RcsSettings.getInstance().writeParameter(RcsSettingsData.SMS_FALLBACK_SERVICE, RcsSettingsData.FALSE);
				}
    		} else
    		if (key.equals("/IM/autaccept")) {
    			// Represent the automatic/manual chat session answer mode.
    			// Values: 0, 1
    			// 0- Indicates manual answer mode
    			// 1- Indicates automatic answer mode (default value)
	    		if (logger.isActivated()) {
	    			logger.debug("Update parameter " + key + ": " + param.getValue());
	    		}
				if (value.equals("0")) {
	    			RcsSettings.getInstance().writeParameter(RcsSettingsData.CHAT_INVITATION_AUTO_ACCEPT, RcsSettingsData.FALSE);
				} else {
	    			RcsSettings.getInstance().writeParameter(RcsSettingsData.CHAT_INVITATION_AUTO_ACCEPT, RcsSettingsData.TRUE);
				}
    		} else
    		if (key.equals("/IM/maxsize1to1")) {
    			// Represent the maximum authorized size of a content chat message in a 1 To 1 chat session in bytes
    			RcsSettings.getInstance().writeParameter(RcsSettingsData.MAX_CHAT_MSG_LENGTH, param.getValue());
    		} else
    		if (key.equals("/IM/maxsize1tom")) {
    			// Represent the maximum authorized size of a content chat message in a 1 To M chat session in bytes
    			RcsSettings.getInstance().writeParameter(RcsSettingsData.MAX_CHAT_MSG_LENGTH, param.getValue());
    		} else
    		if (key.equals("/IM/timeridle")) {
    			// Represent the timeout for a chat session in idle mode (when there is no chat user activity) in seconds
	    		if (logger.isActivated()) {
	    			logger.debug("Update parameter " + key + ": " + param.getValue());
	    		}
    			RcsSettings.getInstance().writeParameter(RcsSettingsData.CHAT_IDLE_DURATION, param.getValue());
    		} else
    		if (key.equals("/IM/maxsizefiletr")) {
    			// Represent the maximum authorized size of a file that can be transfers using the RCS File Transfer service
	    		if (logger.isActivated()) {
	    			logger.debug("Update parameter " + key + ": " + param.getValue());
	    		}
    			RcsSettings.getInstance().writeParameter(RcsSettingsData.MAX_FILE_TRANSFER_SIZE, param.getValue());
    		} else
    		
    		if (key.equals("/APN/rcseonlyapn")) {
    			// RCS-e APN
	    		if (logger.isActivated()) {
	    			logger.debug("Update parameter " + key + ": " + param.getValue());
	    		}
    			RcsSettings.getInstance().writeParameter(RcsSettingsData.RCS_APN, param.getValue());
    		} else
    		if (key.equals("/APN/enablercseswitch")) {
    			// Describes whether to show the RCS-e enabled/disabled switch permanently
    			// Values:
    			// 1- The setting is shown permanently
    			// 0- Otherwise it may be only shown during roaming
    			// Not used
    		}
    	}

    	// Restart the service in background
		if (logger.isActivated()) {
			logger.debug("Restart the RCS service in background");
		}
	    Thread t = new Thread() {
	    	public void run() {
	    		ClientApi.stopRcsService(AndroidFactory.getApplicationContext());
	    		ClientApi.startRcsService(AndroidFactory.getApplicationContext());
	    	}
	    };
	    t.start();
    }
}
