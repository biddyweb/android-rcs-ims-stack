/*******************************************************************************
 * Software Name : RCS IMS Stack
 *
 * Copyright (C) 2010 France Telecom S.A.
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

package com.orangelabs.rcs.provisioning;

import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.provider.settings.RcsSettingsData;
import com.orangelabs.rcs.utils.logger.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Provisioning parser
 *
 * @author jexa7410
 */
public class ProvisioningParser {
    /**
     * Provisioning info
     */
    private ProvisioningInfo mProvisioningInfo = new ProvisioningInfo();

    /**
     * Content
     */
    private String content;

    /**
     * The logger
     */
    private Logger logger = Logger.getLogger(this.getClass().getName());

    /**
     * Constructor
     *
     * @param content Content
     */
    public ProvisioningParser(String content) {
        this.content = content;
    }

    /**
     * Returns provisioning info
     *
     * @return Provisioning info
     */
    public ProvisioningInfo getProvisioningInfo() {
        return mProvisioningInfo;
    }

    /**
     * Parse the provisioning document
     *
     * @return Boolean result
     */
    public boolean parse() {
        try {
            if (logger.isActivated()) {
                logger.debug("Start the parsing of content");
            }
            ByteArrayInputStream mInputStream = new ByteArrayInputStream(content.getBytes());
            DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dbuilder = dfactory.newDocumentBuilder();
            Document doc = dbuilder.parse(mInputStream);
            mInputStream.close();
            mInputStream = null;
            if (doc == null) {
                if (logger.isActivated()) {
                    logger.debug("The document is null");
                }
                return false;
            }

            Node rootnode = doc.getDocumentElement();
            Node childnode = rootnode.getFirstChild();
            if (childnode == null) {
                if (logger.isActivated()) {
                    logger.debug("The first chid node is null");
                }
                return false;
            }

            do {
                if (childnode.getNodeName().equals("characteristic")) {
                    if (childnode.getAttributes().getLength() > 0) {
                        Node typenode = childnode.getAttributes().getNamedItem("type");
                        if (typenode != null) {
                            if (logger.isActivated()) {
                                logger.debug("Node " + childnode.getNodeName() + " with type "
                                        + typenode.getNodeValue());
                            }
                            if (typenode.getNodeValue().equals("VERS")) {
                                parseVersion(childnode);
                            } else if (typenode.getNodeValue().equals("MSG")) {
                                parseMSG(childnode);
                            } else if (typenode.getNodeValue().equals("APPLICATION")) {
                                parseApplication(childnode);
                            } else if (typenode.getNodeValue().equals("IMS")) {
                                parseIMS(childnode);
                            } else if (typenode.getNodeValue().equals("PRESENCE")) {
                                parsePresence(childnode);
                            } else if (typenode.getNodeValue().equals("XDMS")) {
                                parseXDMS(childnode);
                            } else if (typenode.getNodeValue().equals("IM")) {
                                parseIM(childnode);
                            } else if (typenode.getNodeValue().equals("CAPDISCOVERY")) {
                                parseCAPDiscovery(childnode);
                            } else if (typenode.getNodeValue().equals("APN")) {
                                parseAPN(childnode);
                            } else if (typenode.getNodeValue().equals("OTHER")) {
                                parseOther(childnode);
                            }
                        }
                    }
                }
            } while ((childnode = childnode.getNextSibling()) != null);
            return true;
        } catch (Exception e) {
            if (logger.isActivated()) {
                logger.error("Can't parse content", e);
            }
            return false;
        }
    }

    /**
     * Parse the provisioning version
     *
     * @param node the version node
     */
    private void parseVersion(Node node) {
        String versionvalue = null;
        String validityvalue = null;
        if (node == null) {
            return;
        }
        Node versionchild = node.getFirstChild();

        if (versionchild != null) {
            do {
                if (versionvalue == null) {
                    if ((versionvalue = getValueByParmName("version", versionchild)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> Version = " + versionvalue);
                        }
                        mProvisioningInfo.versionvalue = versionvalue;
                        continue;
                    }
                }
                if (validityvalue == null) {
                    if ((validityvalue = getValueByParmName("validity", versionchild)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> Validity = " + validityvalue);
                        }
                        mProvisioningInfo.validityvalue = Long.parseLong(validityvalue);
                        continue;
                    }
                }
            } while ((versionchild = versionchild.getNextSibling()) != null);
        }
    }

    /**
     * Parse the message
     *
     * @param node the message node
     */
    private void parseMSG(Node node) {
        String titlevalue = null;
        String messagevalue = null;
        String acceptbtnvalue = null;
        String rejectbtnvalue = null;
        if (node == null) {
            return;
        }
        Node childnode = node.getFirstChild();

        if (childnode != null) {
            do {
                if (titlevalue == null) {
                    if ((titlevalue = getValueByParmName("title", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> Title = " + titlevalue);
                        }
                        mProvisioningInfo.titlevalue = titlevalue;
                        continue;
                    }
                }
                if (messagevalue == null) {
                    if ((messagevalue = getValueByParmName("message", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> Message = " + messagevalue);
                        }
                        mProvisioningInfo.messagevalue = messagevalue;
                        continue;
                    }
                }
                if (acceptbtnvalue == null) {
                    if ((acceptbtnvalue = getValueByParmName("Accept_btn", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> Accept_btn = " + acceptbtnvalue);
                        }
                        mProvisioningInfo.acceptbtnvalue = acceptbtnvalue;
                        continue;
                    }
                }
                if (rejectbtnvalue == null) {
                    if ((rejectbtnvalue = getValueByParmName("Reject_btn", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> Reject_btn = " + rejectbtnvalue);
                        }
                        mProvisioningInfo.rejectbtnvalue = rejectbtnvalue;
                        continue;
                    }
                }
            } while ((childnode = childnode.getNextSibling()) != null);
        }
    }

    /**
     * Parse the application node
     *
     * @param node application node
     */
    private void parseApplication(Node node) {
        String appidvalue = null;
        String namevalue = null;
        String apprefvalue = null;
        if (node == null) {
            return;
        }
        Node childnode = node.getFirstChild();

        if (childnode != null) {
            do {
                if (appidvalue == null) {
                    if ((appidvalue = getValueByParmName("AppID", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> AppID = " + appidvalue);
                        }
                        continue;
                    }
                }
                if (namevalue == null) {
                    if ((namevalue = getValueByParmName("Name", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> Name = " + namevalue);
                        }
                        continue;
                    }
                }
                if (apprefvalue == null) {
                    if ((apprefvalue = getValueByParmName("AppRef", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> AppRef = " + apprefvalue);
                        }
                        continue;
                    }
                }
            } while ((childnode = childnode.getNextSibling()) != null);
        }

        if (apprefvalue != null && apprefvalue.equals("IMS-Settings")) {
            parseIMS(node);
        }

        if (apprefvalue != null && apprefvalue.equals("RCSe-Settings")) {
            parseRCSe(node);
        }
    }

    /**
     * Parse presence favorite link
     *
     * @param node FAVLINK node
     */
    private void parseFAVLINK(Node node) {
        if (node == null) {
            return;
        }
        // !!! Not used !!!
        // Determines the operator policy for Favorite Link instantiation in the
        // local presence document of the presentity. Values: 'Auto', 'Man',
        // 'Auto+Man'.
    }

    /**
     * Parse presence SERVCAPWATCH
     *
     * @param node SERVCAPWATCH node
     */
    private void parseSERVCAPWATCH(Node node) {
        String FetchAuthvalue = null;
        String ContactCapPresAutvalue = null;
        if (node == null) {
            return;
        }
        Node childnode = node.getFirstChild();

        if (childnode != null) {
            do {
                if (FetchAuthvalue == null) {
                    if ((FetchAuthvalue = getValueByParmName("FetchAuth", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> FetchAuth = " + FetchAuthvalue);
                        }
                        // Represent operator setting of parameters linked with
                        // watcher behaviour of the device
                        // Values: 0, 1
                        // 0- Indicates that this automatic fetch is not
                        // authorized
                        // 1- Indicates that this automatic fetch is authorized
                        // TODO
                        continue;
                    }
                }

                if (ContactCapPresAutvalue == null) {
                    if ((ContactCapPresAutvalue = getValueByParmName("ContactCapPresAut", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> ContactCapPresAut = " + ContactCapPresAutvalue);
                        }
                        // Indicates if the device is authorized to display to
                        // the user the ability of the user
                        // contacts declared in the local address book to share
                        // Social Presence Information
                        // Values: 0, 1
                        // 0- Indicates that rendering is not authorized
                        // 1- Indicates that rendering is authorized
                        // TODO
                        continue;
                    }
                }
            } while ((childnode = childnode.getNextSibling()) != null);
        }
    }

    /**
     * Parse presence ServCapPresentity
     *
     * @param node ServCapPresentity node
     */
    private void parseServCapPresentity(Node node) {
        String WATCHERFETCHAUTHvalue = null;
        if (node == null) {
            return;
        }
        Node childnode = node.getFirstChild();

        if (childnode != null) {
            do {
                if (WATCHERFETCHAUTHvalue == null) {
                    if ((WATCHERFETCHAUTHvalue = getValueByParmName("WATCHERFETCHAUTH", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> WATCHERFETCHAUTH = " + WATCHERFETCHAUTHvalue);
                        }
                        // Indicates if watchers are authorized to
                        // â€œanonymousâ€� fetch service capabilities
                        // of the user
                        // Values: 0, 1
                        // 0- Indicates that watchers are authorized to fetch
                        // user service capabilities
                        // 1- Indicates that watchers are not authorized to
                        // fetch user service capabilities
                        // TODO
                        continue;
                    }
                }
            } while ((childnode = childnode.getNextSibling()) != null);
        }
    }

    /**
     * Parse presence
     *
     * @param node
     */
    private void parsePresence(Node node) {
        String usePresencevalue = null;
        String presencePrflvalue = null;
        String AvailabilityAuthvalue = null;
        String IconMaxSizevalue = null;
        String NoteMaxSizevalue = null;
        String PublishTimervalue = null;
        String clientobjdatalimitvalue = null;
        String contentserverurivalue = null;
        String sourcethrottlepublishvalue = null;
        String maxnumberofsubscriptionsinpresencelistvalue = null;
        String serviceuritemplatevalue = null;
        Node typenode = null;
        if (node == null) {
            return;
        }
        Node childnode = node.getFirstChild();

        if (childnode != null) {
            do {
                if (childnode.getNodeName().equals("characteristic")) {
                    if (childnode.getAttributes().getLength() > 0) {
                        typenode = childnode.getAttributes().getNamedItem("type");
                        if (typenode != null) {
                            if (logger.isActivated()) {
                                logger.debug("Node " + childnode.getNodeName() + " with type "
                                        + typenode.getNodeValue());
                            }
                            if (typenode.getNodeValue().equals("FAVLINK")) {
                                parseFAVLINK(childnode);
                            } else if (typenode.getNodeValue().equals("SERVCAPWATCH")) {
                                parseSERVCAPWATCH(childnode);
                            } else if (typenode.getNodeValue().equals("ServCapPresentity")) {
                                parseServCapPresentity(childnode);
                            }
                        }
                    }
                }

                if (usePresencevalue == null) {
                    if ((usePresencevalue = getValueByParmName("usePresence", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> usePresence = " + usePresencevalue);
                        }
                        if (usePresencevalue.equals("0")) {
                            RcsSettings.getInstance().writeParameter(
                                    RcsSettingsData.CAPABILITY_SOCIAL_PRESENCE,
                                    RcsSettingsData.FALSE);
                        } else {
                            RcsSettings.getInstance().writeParameter(
                                    RcsSettingsData.CAPABILITY_SOCIAL_PRESENCE,
                                    RcsSettingsData.TRUE);
                        }
                        continue;
                    }
                }

                if (presencePrflvalue == null) {
                    if ((presencePrflvalue = getValueByParmName("presencePrfl", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> presencePrfl = " + presencePrflvalue);
                        }
                        continue;
                    }
                }

                if (AvailabilityAuthvalue == null) {
                    if ((AvailabilityAuthvalue = getValueByParmName("AvailabilityAuth", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> AvailabilityAuth = " + AvailabilityAuthvalue);
                        }
                        // !!! Not used !!!
                        // Authorization for the Presence UA to use Availability
                        // status feature.
                        // Values: 0, 1
                        // 0 indicates that use of Availability status is not authorized
                        // 1 indicates that use of Availability status is authorized
                        continue;
                    }
                }

                if (IconMaxSizevalue == null) {
                    if ((IconMaxSizevalue = getValueByParmName("IconMaxSize", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> IconMaxSize = " + IconMaxSizevalue);
                        }
                        RcsSettings.getInstance().writeParameter(
                                RcsSettingsData.MAX_PHOTO_ICON_SIZE, IconMaxSizevalue);
                        continue;
                    }
                }

                if (NoteMaxSizevalue == null) {
                    if ((NoteMaxSizevalue = getValueByParmName("NoteMaxSize", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> NoteMaxSize = " + NoteMaxSizevalue);
                        }
                        RcsSettings.getInstance().writeParameter(
                                RcsSettingsData.MAX_FREETXT_LENGTH, NoteMaxSizevalue);
                        continue;
                    }
                }

                if (PublishTimervalue == null) {
                    if ((PublishTimervalue = getValueByParmName("PublishTimer", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> PublishTimer = " + PublishTimervalue);
                        }
                        RcsSettings.getInstance().writeParameter(
                                RcsSettingsData.PUBLISH_EXPIRE_PERIOD, PublishTimervalue);
                        continue;
                    }
                }

                if (clientobjdatalimitvalue == null) {
                    if ((clientobjdatalimitvalue = getValueByParmName("client-obj-datalimit",
                            childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> client-obj-datalimit = " + clientobjdatalimitvalue);
                        }
                        // TODO Add in settings or NOT USED ?
                        continue;
                    }
                }

                if (contentserverurivalue == null) {
                    if ((contentserverurivalue = getValueByParmName("content-serveruri", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> content-serveruri = " + contentserverurivalue);
                        }
                        // TODO Add in settings or NOT USED ?
                        continue;
                    }
                }

                if (sourcethrottlepublishvalue == null) {
                    if ((sourcethrottlepublishvalue = getValueByParmName("source-throttlepublish",
                            childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> source-throttlepublish = " + sourcethrottlepublishvalue);
                        }
                        // TODO Add in settings or NOT USED ?
                        continue;
                    }
                }

                if (maxnumberofsubscriptionsinpresencelistvalue == null) {
                    if ((maxnumberofsubscriptionsinpresencelistvalue = getValueByParmName(
                            "max-number-ofsubscriptions-inpresence-list", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> max-number-ofsubscriptions-inpresence-list = "
                                    + maxnumberofsubscriptionsinpresencelistvalue);
                        }
                        // TODO Add in settings or NOT USED ?
                        continue;
                    }
                }

                if (serviceuritemplatevalue == null) {
                    if ((serviceuritemplatevalue = getValueByParmName("service-uritemplate",
                            childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> service-uritemplate = " + serviceuritemplatevalue);
                        }
                        // TODO Add in settings or NOT USED ?
                        continue;
                    }
                }
            } while ((childnode = childnode.getNextSibling()) != null);
        }
    }

    /**
     * Parse presence XDMS
     *
     * @param node XDMS node
     */
    private void parseXDMS(Node node) {
        String RevokeTimervalue = null;
        String XCAPRootURIvalue = null;
        String XCAPAuthenticationUserNamevalue = null;
        String XCAPAuthenticationSecretvalue = null;
        String XCAPAuthenticationTypevalue = null;
        if (node == null) {
            return;
        }
        Node childnode = node.getFirstChild();

        if (childnode != null) {
            do {
                if (RevokeTimervalue == null) {
                    if ((RevokeTimervalue = getValueByParmName("RevokeTimer", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> RevokeTimer = " + RevokeTimervalue);
                        }
                        RcsSettings.getInstance().writeParameter(RcsSettingsData.REVOKE_TIMEOUT,
                                RevokeTimervalue);
                        continue;
                    }
                }

                if (XCAPRootURIvalue == null) {
                    if ((XCAPRootURIvalue = getValueByParmName("XCAPRootURI", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> XCAPRootURI = " + "(---hiden---)"); // XCAPRootURIvalue);
                        }
                        RcsSettings.getInstance().writeParameter(
                                RcsSettingsData.USERPROFILE_XDM_SERVER, XCAPRootURIvalue);
                        continue;
                    }
                }

                if (XCAPAuthenticationUserNamevalue == null) {
                    if ((XCAPAuthenticationUserNamevalue = getValueByParmName(
                            "XCAPAuthenticationUserName", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> XCAPAuthenticationUserName = " + "(---hiden---)"); // XCAPAuthenticationUserNamevalue);
                        }
                        RcsSettings.getInstance().writeParameter(
                                RcsSettingsData.USERPROFILE_XDM_LOGIN,
                                XCAPAuthenticationUserNamevalue);
                        continue;
                    }
                }

                if (XCAPAuthenticationSecretvalue == null) {
                    if ((XCAPAuthenticationSecretvalue = getValueByParmName(
                            "XCAPAuthenticationSecret", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> XCAPAuthenticationSecret = " + "(---hiden---)");
                            // + XCAPAuthenticationSecretvalue);
                        }
                        RcsSettings.getInstance().writeParameter(
                                RcsSettingsData.USERPROFILE_XDM_PASSWORD,
                                XCAPAuthenticationSecretvalue);
                        continue;
                    }
                }

                if (XCAPAuthenticationTypevalue == null) {
                    if ((XCAPAuthenticationTypevalue = getValueByParmName("XCAPAuthenticationType",
                            childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> XCAPAuthenticationType = " + "(---hiden---)");
                            // + XCAPAuthenticationTypevalue);
                        }
                        // !!! Not used !!!
                        // XDMS authentication type
                        continue;
                    }
                }
            } while ((childnode = childnode.getNextSibling()) != null);
        }
    }

    /**
     * Parse IM
     *
     * @param node IM node
     */
    private void parseIM(Node node) {
        String imCapAlwaysONvalue = null;
        String imWarnSFvalue = null;
        String imSessionStartvalue = null;
        String ftWarnSizevalue = null;
        String ChatAuthvalue = null;
        String SmsFallBackAuthvalue = null;
        String AutAcceptvalue = null;
        String MaxSize1to1value = null;
        String MaxSize1toMvalue = null;
        String TimerIdlevalue = null;
        String MaxSizeFileTrvalue = null;
        String pressrvcapvalue = null;
        String deferredmsgfuncurivalue = null;
        String maxadhocgroupsizevalue = null;
        String conffctyurivalue = null;
        String exploderurivalue = null;
        if (node == null) {
            return;
        }
        Node childnode = node.getFirstChild();

        if (childnode != null) {
            do {
                if (imCapAlwaysONvalue == null) {
                    if ((imCapAlwaysONvalue = getValueByParmName("imCapAlwaysON", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> imCapAlwaysON = " + imCapAlwaysONvalue);
                        }
                        RcsSettings.getInstance().writeParameter(
                                RcsSettingsData.IM_CAPABILITY_ALWAYS_ON, imCapAlwaysONvalue);
                        continue;
                    }
                }

                if (imWarnSFvalue == null) {
                    if ((imWarnSFvalue = getValueByParmName("imWarnSF", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> imWarnSF = " + imWarnSFvalue);
                        }
                        RcsSettings.getInstance().writeParameter(RcsSettingsData.WARN_SF_SERVICE,
                                imWarnSFvalue);
                        continue;
                    }
                }

                if (imSessionStartvalue == null) {
                    if ((imSessionStartvalue = getValueByParmName("imSessionStart", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> imSessionStart = " + imSessionStartvalue);
                        }
                        RcsSettings.getInstance().writeParameter(RcsSettingsData.IM_SESSION_START,
                                imSessionStartvalue);
                        continue;
                    }
                }

                if (ftWarnSizevalue == null) {
                    if ((ftWarnSizevalue = getValueByParmName("ftWarnSize", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> ftWarnSize = " + ftWarnSizevalue);
                        }
                        RcsSettings.getInstance().writeParameter(
                                RcsSettingsData.WARN_FILE_TRANSFER_SIZE, ftWarnSizevalue);
                        continue;
                    }
                }

                if (ChatAuthvalue == null) {
                    if ((ChatAuthvalue = getValueByParmName("ChatAuth", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> ChatAuth = " + ChatAuthvalue);
                        }
                        if (ChatAuthvalue.equals("0")) {
                            RcsSettings.getInstance().writeParameter(
                                    RcsSettingsData.CAPABILITY_IM_SESSION, RcsSettingsData.TRUE);
                        } else {
                            RcsSettings.getInstance().writeParameter(
                                    RcsSettingsData.CAPABILITY_IM_SESSION, RcsSettingsData.FALSE);
                        }
                        continue;
                    }
                }

                if (SmsFallBackAuthvalue == null) {
                    if ((SmsFallBackAuthvalue = getValueByParmName("SmsFallBackAuth", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> SmsFallBackAuth = " + SmsFallBackAuthvalue);
                        }
                        if (SmsFallBackAuthvalue.equals("0")) {
                            RcsSettings.getInstance().writeParameter(
                                    RcsSettingsData.SMS_FALLBACK_SERVICE, RcsSettingsData.TRUE);
                        } else {
                            RcsSettings.getInstance().writeParameter(
                                    RcsSettingsData.SMS_FALLBACK_SERVICE, RcsSettingsData.FALSE);
                        }
                        continue;
                    }
                }

                if (AutAcceptvalue == null) {
                    if ((AutAcceptvalue = getValueByParmName("AutAccept", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> AutAccept = " + AutAcceptvalue);
                        }
                        if (AutAcceptvalue.equals("0")) {
                            RcsSettings.getInstance().writeParameter(
                                    RcsSettingsData.CHAT_INVITATION_AUTO_ACCEPT,
                                    RcsSettingsData.FALSE);
                        } else {
                            RcsSettings.getInstance().writeParameter(
                                    RcsSettingsData.CHAT_INVITATION_AUTO_ACCEPT,
                                    RcsSettingsData.TRUE);
                        }
                        continue;
                    }
                }

                if (MaxSize1to1value == null) {
                    if ((MaxSize1to1value = getValueByParmName("MaxSize1to1", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> MaxSize1to1 = " + MaxSize1to1value);
                        }
                        RcsSettings.getInstance().writeParameter(
                                RcsSettingsData.MAX_CHAT_MSG_LENGTH, MaxSize1to1value);
                        continue;
                    }
                }

                if (MaxSize1toMvalue == null) {
                    if ((MaxSize1toMvalue = getValueByParmName("MaxSize1toM", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> MaxSize1toM = " + MaxSize1toMvalue);
                        }
                        RcsSettings.getInstance().writeParameter(
                                RcsSettingsData.MAX_CHAT_MSG_LENGTH, MaxSize1to1value);
                        continue;
                    }
                }

                if (TimerIdlevalue == null) {
                    if ((TimerIdlevalue = getValueByParmName("TimerIdle", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> TimerIdle = " + TimerIdlevalue);
                        }
                        RcsSettings.getInstance().writeParameter(
                                RcsSettingsData.CHAT_IDLE_DURATION, TimerIdlevalue);
                        continue;
                    }
                }

                if (MaxSizeFileTrvalue == null) {
                    if ((MaxSizeFileTrvalue = getValueByParmName("MaxSizeFileTr", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> MaxSizeFileTr = " + MaxSizeFileTrvalue);
                        }
                        RcsSettings.getInstance().writeParameter(
                                RcsSettingsData.MAX_FILE_TRANSFER_SIZE, MaxSizeFileTrvalue);
                        continue;
                    }
                }

                if (pressrvcapvalue == null) {
                    if ((pressrvcapvalue = getValueByParmName("pres-srv-cap", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> pres-srv-cap = " + pressrvcapvalue);
                        }
                        // TODO Add in settings or NOT USED ?
                        continue;
                    }
                }

                if (deferredmsgfuncurivalue == null) {
                    if ((deferredmsgfuncurivalue = getValueByParmName("deferred-msg-func-uri",
                            childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> deferred-msg-func-uri = " + "(---hiden---)");
                            // + deferredmsgfuncurivalue);
                        }
                        // TODO Add in settings or NOT USED ?
                        continue;
                    }
                }

                if (maxadhocgroupsizevalue == null) {
                    if ((maxadhocgroupsizevalue = getValueByParmName("max_adhoc_group_size",
                            childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> max_adhoc_group_size = " + maxadhocgroupsizevalue);
                        }
                        // TODO Add in settings or NOT USED ?
                        continue;
                    }
                }

                if (conffctyurivalue == null) {
                    if ((conffctyurivalue = getValueByParmName("conf-fcty-uri", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> conf-fcty-uri = " + "(---hiden---)"); 
                            // + conffctyurivalue);
                        }
                        // TODO Add in settings or NOT USED ?
                        continue;
                    }
                }

                if (exploderurivalue == null) {
                    if ((exploderurivalue = getValueByParmName("exploder-uri", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> exploder-uri = " + exploderurivalue);
                        }
                        // TODO Add in settings or NOT USED ?
                        continue;
                    }
                }
            } while ((childnode = childnode.getNextSibling()) != null);
        }
    }

    /**
     * Parse CAP discovery
     * @param node
     */
    private void parseCAPDiscovery(Node node) {
        String pollingPeriodvalue = null;
        String capInfoExpiryvalue = null;
        String presenceDiscvalue = null;
        if (node == null) {
            return;
        }
        Node childnode = node.getFirstChild();

        if (childnode != null) {
            do {
                if (pollingPeriodvalue == null) {
                    if ((pollingPeriodvalue = getValueByParmName("pollingPeriod", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> pollingPeriod = " + pollingPeriodvalue);
                        }
                        // TODO Add in settings or NOT USED ?
                        continue;
                    }
                }

                if (capInfoExpiryvalue == null) {
                    if ((capInfoExpiryvalue = getValueByParmName("capInfoExpiry", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> capInfoExpiry = " + capInfoExpiryvalue);
                        }
                        // TODO Add in settings or NOT USED ?
                        continue;
                    }
                }

                if (presenceDiscvalue == null) {
                    if ((presenceDiscvalue = getValueByParmName("presenceDisc", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> presenceDisc = " + presenceDiscvalue);
                        }
                        // TODO Add in settings or NOT USED ?
                        continue;
                    }
                }
            } while ((childnode = childnode.getNextSibling()) != null);
        }
    }

    /**
     * Parse APN node
     *
     * @param node APN node
     */
    private void parseAPN(Node node) {
        String rcseOnlyAPNvalue = null;
        String enableRcseSwitchvalue = null;
        if (node == null) {
            return;
        }
        Node childnode = node.getFirstChild();

        if (childnode != null) {
            do {
                if (rcseOnlyAPNvalue == null) {
                    if ((rcseOnlyAPNvalue = getValueByParmName("rcseOnlyAPN", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> rcseOnlyAPN = " + "(---hiden---)");
                            // + rcseOnlyAPNvalue);
                        }
                        RcsSettings.getInstance().writeParameter(RcsSettingsData.RCS_APN,
                                rcseOnlyAPNvalue);
                        continue;
                    }
                }

                if (enableRcseSwitchvalue == null) {
                    if ((enableRcseSwitchvalue = getValueByParmName("enableRcseSwitch", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> enableRcseSwitch = " + enableRcseSwitchvalue);
                        }
                        // !!! Not used !!!
                        // Describes whether to show the RCS-e enabled/disabled
                        // switch permanently
                        // Values:
                        // 1- The setting is shown permanently
                        // 0- Otherwise it may be only shown during roaming
                        continue;
                    }
                }
            } while ((childnode = childnode.getNextSibling()) != null);
        }
    }

    /**
     * Parse transportProto node
     *
     * @param node transportProto node
     */
    private void parsetransportProto(Node node) {
        String psSignallingvalue = null;
        String psMediavalue = null;
        String psRTMediavalue = null;
        String wifiSignallingvalue = null;
        String wifiMediavalue = null;
        String wifiRTMediavalue = null;
        if (node == null) {
            return;
        }
        Node childnode = node.getFirstChild();

        if (childnode != null) {
            do {
                if (psSignallingvalue == null) {
                    if ((psSignallingvalue = getValueByParmName("psSignalling", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> psSignalling = " + psSignallingvalue);
                        }
                        // TODO Add in settings or NOT USED ?
                        continue;
                    }
                }

                if (psMediavalue == null) {
                    if ((psMediavalue = getValueByParmName("psMedia", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> psMedia = " + psMediavalue);
                        }
                        // TODO Add in settings or NOT USED ?
                        continue;
                    }
                }

                if (psRTMediavalue == null) {
                    if ((psRTMediavalue = getValueByParmName("psRTMedia", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> psRTMedia = " + psRTMediavalue);
                        }
                        // TODO Add in settings or NOT USED ?
                        continue;
                    }
                }

                if (wifiSignallingvalue == null) {
                    if ((wifiSignallingvalue = getValueByParmName("wifiSignalling", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> wifiSignalling = " + wifiSignallingvalue);
                        }
                        // TODO Add in settings or NOT USED ?
                        continue;
                    }
                }

                if (wifiMediavalue == null) {
                    if ((wifiMediavalue = getValueByParmName("wifiMedia", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> wifiMedia = " + wifiMediavalue);
                        }
                        // TODO Add in settings or NOT USED ?
                        continue;
                    }
                }

                if (wifiRTMediavalue == null) {
                    if ((wifiRTMediavalue = getValueByParmName("wifiRTMedia", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> wifiRTMedia = " + wifiRTMediavalue);
                        }
                        // TODO Add in settings or NOT USED ?
                        continue;
                    }
                }
            } while ((childnode = childnode.getNextSibling()) != null);
        }
    }

    /**
     * Parse Other node
     *
     * @param node OTHER node
     */
    private void parseOther(Node node) {
        String endUserConfReqIdvalue = null;
        String deviceIDvalue = null;
        String WarnSizeImageSharevalue = null;
        Node typenode = null;
        if (node == null) {
            return;
        }
        Node childnode = node.getFirstChild();

        if (childnode != null) {
            do {
                if (childnode.getNodeName().equals("characteristic")) {
                    if (childnode.getAttributes().getLength() > 0) {
                        typenode = childnode.getAttributes().getNamedItem("type");
                        if (typenode != null) {
                            if (logger.isActivated()) {
                                logger.debug("Node " + childnode.getNodeName() + " with type "
                                        + typenode.getNodeValue());
                            }
                            if (typenode.getNodeValue().equals("transportProto")) {
                                parsetransportProto(childnode);
                            }
                        }
                    }
                }

                if (endUserConfReqIdvalue == null) {
                    if ((endUserConfReqIdvalue = getValueByParmName("endUserConfReqId", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> endUserConfReqId = " + endUserConfReqIdvalue);
                        }
                        // TODO Add in settings or NOT USED ?
                        continue;
                    }
                }

                if (deviceIDvalue == null) {
                    if ((deviceIDvalue = getValueByParmName("deviceID", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> deviceID = " + deviceIDvalue);
                        }
                        // TODO Add in settings or NOT USED ?
                        continue;
                    }
                }

                if (WarnSizeImageSharevalue == null) {
                    if ((WarnSizeImageSharevalue = getValueByParmName("WarnSizeImageShare",
                            childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> WarnSizeImageShare = " + WarnSizeImageSharevalue);
                        }
                        // TODO Add in settings or NOT USED ?
                        continue;
                    }
                }
            } while ((childnode = childnode.getNextSibling()) != null);
        }
    }

    /**
     * Parse ConRefs node
     *
     * @param node ConRefs node
     */
    private void parseConRefs(Node node) {
        String conrefsvalue = null;
        if (node == null) {
            return;
        }
        Node childnode = node.getFirstChild();

        if (childnode != null) {
            do {
                if (conrefsvalue == null) {
                    if ((conrefsvalue = getValueByParmName("ConRef", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> ConRef = " + conrefsvalue);
                        }
                        // !!! Not used !!!
                        // List of network access point objects
                        continue;
                    }
                }
            } while ((childnode = childnode.getNextSibling()) != null);
        }
    }

    /**
     * Parse public user identity node
     *
     * @param node public user identity node
     */
    private void parsePublicUserIdentity(Node node) {
        String publicuseridentityvalue = null;
        if (node == null) {
            return;
        }
        Node childnode = node.getFirstChild();

        if (childnode != null) {
            do {
                if (publicuseridentityvalue == null) {
                    if ((publicuseridentityvalue = getValueByParmName("Public_User_Identity",
                            childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> Public_User_Identity = " + "(---hiden---)");
                            // + publicuseridentityvalue);
                        }
                        RcsSettings.getInstance().writeParameter(
                                RcsSettingsData.USERPROFILE_IMS_USERNAME, publicuseridentityvalue);
                        continue;
                    }
                }
            } while ((childnode = childnode.getNextSibling()) != null);
        }
    }

    /**
     * Parse the SecondaryDevicePar node
     *
     * @param node
     */
    private void parseSecondaryDevicePar(Node node) {
        String VoiceCallvalue = null;
        String Chatvalue = null;
        String SendSmsvalue = null;
        String FileTranfervalue = null;
        String VideoSharevalue = null;
        String ImageSharevalue = null;
        if (node == null) {
            return;
        }
        Node childnode = node.getFirstChild();

        if (childnode != null) {
            do {
                if (VoiceCallvalue == null) {
                    if ((VoiceCallvalue = getValueByParmName("VoiceCall", childnode)) != null) {
                        logger.debug("=> VoiceCall = " + VoiceCallvalue);
                        // !!! Not used !!!
                        // Voice call capability.
                        // Values: 0, 1
                        // 0 indicates authorization
                        // 1 indicates non authorization
                        continue;
                    }
                }

                if (Chatvalue == null) {
                    if ((Chatvalue = getValueByParmName("Chat", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> Chat = " + Chatvalue);
                        }
                        if (Chatvalue.equals("0")) {
                            RcsSettings.getInstance().writeParameter(
                                    RcsSettingsData.CAPABILITY_IM_SESSION, RcsSettingsData.TRUE);
                        } else {
                            RcsSettings.getInstance().writeParameter(
                                    RcsSettingsData.CAPABILITY_IM_SESSION, RcsSettingsData.FALSE);
                        }
                        continue;
                    }
                }

                if (SendSmsvalue == null) {
                    if ((SendSmsvalue = getValueByParmName("SendSms", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> SendSms = " + SendSmsvalue);
                        }
                        // !!! Not used !!!
                        // Chat capability.
                        // Values: 0, 1
                        // 0 indicates authorization
                        // 1 indicates non authorization
                        continue;
                    }
                }

                if (FileTranfervalue == null) {
                    if ((FileTranfervalue = getValueByParmName("FileTranfer", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> FileTranfer = " + FileTranfervalue);
                        }
                        if (FileTranfervalue.equals("0")) {
                            RcsSettings.getInstance().writeParameter(
                                    RcsSettingsData.CAPABILITY_FILE_TRANSFER, RcsSettingsData.TRUE);
                        } else {
                            RcsSettings.getInstance()
                                    .writeParameter(RcsSettingsData.CAPABILITY_FILE_TRANSFER,
                                            RcsSettingsData.FALSE);
                        }
                        continue;
                    }
                }

                if (VideoSharevalue == null) {
                    if ((VideoSharevalue = getValueByParmName("VideoShare", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> VideoShare = " + VideoSharevalue);
                        }
                        if (VideoSharevalue.equals("0")) {
                            RcsSettings.getInstance().writeParameter(
                                    RcsSettingsData.CAPABILITY_VIDEO_SHARING, RcsSettingsData.TRUE);
                        } else {
                            RcsSettings.getInstance()
                                    .writeParameter(RcsSettingsData.CAPABILITY_VIDEO_SHARING,
                                            RcsSettingsData.FALSE);
                        }
                        continue;
                    }
                }

                if (ImageSharevalue == null) {
                    if ((ImageSharevalue = getValueByParmName("ImageShare", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> ImageShare = " + ImageSharevalue);
                        }
                        if (ImageSharevalue.equals("0")) {
                            RcsSettings.getInstance().writeParameter(
                                    RcsSettingsData.CAPABILITY_IMAGE_SHARING, RcsSettingsData.TRUE);
                        } else {
                            RcsSettings.getInstance()
                                    .writeParameter(RcsSettingsData.CAPABILITY_IMAGE_SHARING,
                                            RcsSettingsData.FALSE);
                        }
                        continue;
                    }
                }
            } while ((childnode = childnode.getNextSibling()) != null);
        }
    }

    /**
     * Parse Ext node
     *
     * @param node Ext node
     */
    private void parseExt(Node node) {
        String NatUrlFmtvalue = null;
        String IntUrlFmtvalue = null;
        String QValuevalue = null;
        String MaxSizeImageSharevalue = null;
        String MaxTimeVideoSharevalue = null;
        Node typenode = null;
        if (node == null) {
            return;
        }
        Node childnode = node.getFirstChild();

        if (childnode != null) {
            do {
                if (childnode.getNodeName().equals("characteristic")) {
                    if (childnode.getAttributes().getLength() > 0) {
                        typenode = childnode.getAttributes().getNamedItem("type");
                        if (typenode != null) {
                            if (logger.isActivated()) {
                                logger.debug("Node " + childnode.getNodeName() + " with type "
                                        + typenode.getNodeValue());
                            }
                            if (typenode.getNodeValue().equals("SecondaryDevicePar")) {
                                parseSecondaryDevicePar(childnode);
                            }
                        }
                    }
                }

                if (NatUrlFmtvalue == null) {
                    if ((NatUrlFmtvalue = getValueByParmName("NatUrlFmt", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> NatUrlFmt = " + NatUrlFmtvalue);
                        }
                        if (NatUrlFmtvalue.equals("0")) {
                            RcsSettings.getInstance().writeParameter(
                                    RcsSettingsData.TEL_URI_FORMAT, RcsSettingsData.TRUE);
                        } else {
                            RcsSettings.getInstance().writeParameter(
                                    RcsSettingsData.TEL_URI_FORMAT, RcsSettingsData.FALSE);
                        }
                        continue;
                    }
                }

                if (IntUrlFmtvalue == null) {
                    if ((IntUrlFmtvalue = getValueByParmName("IntUrlFmt", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> IntUrlFmt = " + IntUrlFmtvalue);
                        }
                        if (IntUrlFmtvalue.equals("0")) {
                            RcsSettings.getInstance().writeParameter(
                                    RcsSettingsData.TEL_URI_FORMAT, RcsSettingsData.TRUE);
                        } else {
                            RcsSettings.getInstance().writeParameter(
                                    RcsSettingsData.TEL_URI_FORMAT, RcsSettingsData.FALSE);
                        }
                        continue;
                    }
                }

                if (QValuevalue == null) {
                    if ((QValuevalue = getValueByParmName("Q-Value", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> Q-Value = " + QValuevalue);
                        }
                        // !!! Not used !!!
                        // Indicates the Q-value to be put in the Contact header
                        // of the Register method. This is useful
                        // in case of multi-device for forking algorithm.
                        // Values: '0.1', '0.2', '0.3', '0.4', '0.5', '0.6',
                        // '0.7', '0.8', '0.9', '1.0'
                        continue;
                    }
                }

                if (MaxSizeImageSharevalue == null) {
                    if ((MaxSizeImageSharevalue = getValueByParmName("MaxSizeImageShare", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> MaxSizeImageShare = " + MaxSizeImageSharevalue);
                        }
                        RcsSettings.getInstance().writeParameter(
                                RcsSettingsData.MAX_IMAGE_SHARE_SIZE, MaxSizeImageSharevalue);
                        continue;
                    }
                }

                if (MaxTimeVideoSharevalue == null) {
                    if ((MaxTimeVideoSharevalue = getValueByParmName("MaxTimeVideoShare", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> MaxTimeVideoShare = " + MaxTimeVideoSharevalue);
                        }
                        RcsSettings.getInstance().writeParameter(
                                RcsSettingsData.MAX_VIDEO_SHARE_DURATION, MaxTimeVideoSharevalue);
                        continue;
                    }
                }
            } while ((childnode = childnode.getNextSibling()) != null);
        }
    }

    /**
     * Parse ICSIList node
     *
     * @param node ICSIList node
     */
    private void parseICSIList(Node node) {
        String ICSIvalue = null;
        String ICSIResourceAllocationModevalue = null;
        if (node == null) {
            return;
        }
        Node childnode = node.getFirstChild();

        if (childnode != null) {
            do {
                if (ICSIvalue == null) {
                    if ((ICSIvalue = getValueByParmName("ICSI", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> ICSI = " + ICSIvalue);
                        }
                        // !!! Not used !!!
                        // List of IMS communication service identifiers that
                        // are supported by a subscriber network for that
                        // subscriber.
                        continue;
                    }
                }

                if (ICSIResourceAllocationModevalue == null) {
                    if ((ICSIResourceAllocationModevalue = getValueByParmName(
                            "ICSI_Resource_Allocation_Mode", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> ICSI_Resource_Allocation_Mode = "
                                    + ICSIResourceAllocationModevalue);
                        }
                        // TODO Add in settings or NOT USED ?
                        continue;
                    }
                }
            } while ((childnode = childnode.getNextSibling()) != null);
        }
    }

    /**
     * Parse PCSCF address node
     *
     * @param node PCSCF node
     */
    private void parseLBOPCSCFAddress(Node node) {
        String addressvalue = null;
        String addressTypevalue = null;
        if (node == null) {
            return;
        }
        Node childnode = node.getFirstChild();

        if (childnode != null) {
            do {
                if (addressvalue == null) {
                    if ((addressvalue = getValueByParmName("Address", childnode)) != null) {
                        String[] address = addressvalue.split(":");
                        if (logger.isActivated()) {
                            logger.debug("=> Address = " + "(---hiden---)"); // address[0]);
                        }
                        RcsSettings.getInstance().writeParameter(
                                RcsSettingsData.USERPROFILE_IMS_PROXY_ADDR_MOBILE, address[0]);
                        RcsSettings.getInstance().writeParameter(
                                RcsSettingsData.USERPROFILE_IMS_PROXY_ADDR_WIFI, address[0]);
                        if (address.length > 1) {
                            if (logger.isActivated()) {
                                logger.debug("=> Address port = " + "(---hiden---)"); // address[1]);
                            }
                            RcsSettings.getInstance().writeParameter(
                                    RcsSettingsData.USERPROFILE_IMS_PROXY_PORT_MOBILE, address[1]);
                            RcsSettings.getInstance().writeParameter(
                                    RcsSettingsData.USERPROFILE_IMS_PROXY_PORT_WIFI, address[1]);
                        }
                        continue;
                    }
                }

                if (addressTypevalue == null) {
                    if ((addressTypevalue = getValueByParmName("=> AddressType", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("AddressType = " + addressTypevalue);
                        }
                        // TODO Add in settings or NOT USED ?
                        continue;
                    }
                }
            } while ((childnode = childnode.getNextSibling()) != null);
        }
    }

    /**
     * Parse Phone Context node
     *
     * @param node Phone context node
     */
    private void parsePhoneContextList(Node node) {
        String PhoneContextvalue = null;
        String Publicuseridentityvalue = null;
        if (node == null) {
            return;
        }
        Node childnode = node.getFirstChild();

        if (childnode != null) {
            do {
                if (PhoneContextvalue == null) {
                    if ((PhoneContextvalue = getValueByParmName("PhoneContext", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> PhoneContext = " + "(---hiden---)");
                            // + PhoneContextvalue);
                        }
                        // TODO Add in settings or NOT USED ?
                        continue;
                    }
                }

                if (Publicuseridentityvalue == null) {
                    if ((Publicuseridentityvalue = getValueByParmName("Public_user_identity",
                            childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> Public_user_identity = " + "(---hiden---)");
                            // + Publicuseridentityvalue);
                        }
                        RcsSettings.getInstance().writeParameter(
                                RcsSettingsData.USERPROFILE_IMS_USERNAME, Publicuseridentityvalue);
                        continue;
                    }
                }
            } while ((childnode = childnode.getNextSibling()) != null);
        }
    }

    /**
     * Parse APPAUTH node
     *
     * @param node APPAUTH node
     */
    private void parseAPPAUTH(Node node) {
        String AuthTypevalue = null;
        String Realmvalue = null;
        String UserNamevalue = null;
        String UserPwdvalue = null;
        if (node == null) {
            return;
        }
        Node childnode = node.getFirstChild();

        if (childnode != null) {
            do {
                if (AuthTypevalue == null) {
                    if ((AuthTypevalue = getValueByParmName("AuthType", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> AuthType = " + "(---hiden---)");
                            // + AuthTypevalue);
                        }
                        if (AuthTypevalue.equalsIgnoreCase("EarlyIMS")) {
                            RcsSettings.getInstance().writeParameter(
                                    RcsSettingsData.IMS_AUTHENT_PROCEDURE_MOBILE,
                                    RcsSettingsData.GIBA_AUTHENT);
                        } else {
                            RcsSettings.getInstance().writeParameter(
                                    RcsSettingsData.IMS_AUTHENT_PROCEDURE_MOBILE,
                                    RcsSettingsData.DIGEST_AUTHENT);
                        }
                        continue;
                    }
                }

                if (Realmvalue == null) {
                    if ((Realmvalue = getValueByParmName("Realm", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> Realm = " + "(---hiden---)");
                            // + Realmvalue);
                        }
                        RcsSettings.getInstance().writeParameter(
                                RcsSettingsData.USERPROFILE_IMS_HOME_DOMAIN, Realmvalue);
                        continue;
                    }
                }

                if (UserNamevalue == null) {
                    if ((UserNamevalue = getValueByParmName("UserName", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> UserName = " + "(---hiden---)");
                            // + UserNamevalue);
                        }
                        RcsSettings.getInstance().writeParameter(
                                RcsSettingsData.USERPROFILE_IMS_USERNAME, UserNamevalue);
                        continue;
                    }
                }

                if (UserPwdvalue == null) {
                    if ((UserPwdvalue = getValueByParmName("UserPwd", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> UserPwd = " + "(---hiden---)");
                            // + UserPwdvalue);
                        }
                        RcsSettings.getInstance().writeParameter(
                                RcsSettingsData.USERPROFILE_IMS_PASSWORD, UserPwdvalue);
                        continue;
                    }
                }
            } while ((childnode = childnode.getNextSibling()) != null);
        }
    }

    /**
     * Parse RCSe settings node
     *
     * @param node RCSe settings node
     */
    private void parseRCSe(Node node) {
        Node typenode = null;
        if (node == null) {
            return;
        }
        Node childnode = node.getFirstChild();

        if (childnode != null) {
            do {
                if (childnode.getNodeName().equals("characteristic")) {
                    if (childnode.getAttributes().getLength() > 0) {
                        typenode = childnode.getAttributes().getNamedItem("type");
                        if (typenode != null) {
                            if (logger.isActivated()) {
                                logger.debug("Node " + childnode.getNodeName() + " with type "
                                        + typenode.getNodeValue());
                            }
                            if (typenode.getNodeValue().equals("IMS")) {
                                parseIMS(childnode);
                            } else if (typenode.getNodeValue().equals("PRESENCE")) {
                                parsePresence(childnode);
                            } else if (typenode.getNodeValue().equals("XDMS")) {
                                parseXDMS(childnode);
                            } else if (typenode.getNodeValue().equals("IM")) {
                                parseIM(childnode);
                            } else if (typenode.getNodeValue().equals("CAPDISCOVERY")) {
                                parseCAPDiscovery(childnode);
                            } else if (typenode.getNodeValue().equals("APN")) {
                                parseAPN(childnode);
                            } else if (typenode.getNodeValue().equals("OTHER")) {
                                parseOther(childnode);
                            }
                        }
                    }
                }
            } while ((childnode = childnode.getNextSibling()) != null);
        }
    }

    /**
     * Parse IMS settings
     *
     * @param node IMS settings node
     */
    private void parseIMS(Node node) {
        String pdpcontextoperprefvalue = null;
        String timert1value = null;
        String timert2value = null;
        String timert4value = null;
        String privateuseridentityvalue = null;
        String homenetworkdomainnamevalue = null;
        String voicedomainpreferenceeutranvalue = null;
        String smsoveripnetworksindicationvalue = null;
        String keepaliveenabledvalue = null;
        String voicedomainpreferenceutranvalue = null;
        String mobilitymanagementimsvoiceterminationvalue = null;
        String regretrybasetimevalue = null;
        String regretrymaxtimevalue = null;
        Node typenode = null;
        if (node == null) {
            return;
        }
        Node childnode = node.getFirstChild();

        if (childnode != null) {
            do {
                if (childnode.getNodeName().equals("characteristic")) {
                    if (childnode.getAttributes().getLength() > 0) {
                        typenode = childnode.getAttributes().getNamedItem("type");
                        if (typenode != null) {
                            if (logger.isActivated()) {
                                logger.debug("Node " + childnode.getNodeName() + " with type "
                                        + typenode.getNodeValue());
                            }
                            if (typenode.getNodeValue().equals("ConRefs")) {
                                parseConRefs(childnode);
                            } else if (typenode.getNodeValue().equals("Public_User_Identity")) {
                                parsePublicUserIdentity(childnode);
                            } else if (typenode.getNodeValue().equals("Ext")) {
                                parseExt(childnode);
                            } else if (typenode.getNodeValue().equals("ICSI_List")) {
                                parseICSIList(childnode);
                            } else if (typenode.getNodeValue().equals("LBO_P-CSCF_Address")) {
                                parseLBOPCSCFAddress(childnode);
                            } else if (typenode.getNodeValue().equals("PhoneContext_List")) {
                                parsePhoneContextList(childnode);
                            } else if (typenode.getNodeValue().equals("APPAUTH")) {
                                parseAPPAUTH(childnode);
                            }
                        }
                    }
                }

                if (pdpcontextoperprefvalue == null) {
                    if ((pdpcontextoperprefvalue = getValueByParmName("PDP_ContextOperPref",
                            childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> PDP_ContextOperPref = " + pdpcontextoperprefvalue);
                        }
                        // !!! Not used !!!
                        // The PDP_ContextOperPref leaf indicates an operator
                        // preference to have a dedicated PDP context for SIP
                        // signalling.
                        // Values: 0, 1
                        // 0 indicates that the operator has no preference for a
                        // dedicated PDP context for SIP signalling.
                        // 1 indicates that the operator has preference for a
                        // dedicated PDP context for SIP signalling.
                        continue;
                    }
                }

                if (timert1value == null) {
                    if ((timert1value = getValueByParmName("Timer_T1", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> Timer_T1 = " + timert1value);
                        }
                        RcsSettings.getInstance().writeParameter(RcsSettingsData.SIP_TIMER_T1,
                                timert1value);
                        continue;
                    }
                }

                if (timert2value == null) {
                    if ((timert2value = getValueByParmName("Timer_T2", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> Timer_T2 = " + timert2value);
                        }
                        RcsSettings.getInstance().writeParameter(RcsSettingsData.SIP_TIMER_T2,
                                timert2value);
                        continue;
                    }
                }

                if (timert4value == null) {
                    if ((timert4value = getValueByParmName("Timer_T4", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> Timer_T4 = " + timert4value);
                        }
                        RcsSettings.getInstance().writeParameter(RcsSettingsData.SIP_TIMER_T4,
                                timert4value);
                        continue;
                    }
                }

                if (privateuseridentityvalue == null) {
                    if ((privateuseridentityvalue = getValueByParmName("Private_User_Identity",
                            childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> Private_User_Identity = " + privateuseridentityvalue);
                        }
                        RcsSettings.getInstance().writeParameter(
                                RcsSettingsData.USERPROFILE_IMS_PRIVATE_ID,
                                privateuseridentityvalue);
                        continue;
                    }
                }

                if (homenetworkdomainnamevalue == null) {
                    if ((homenetworkdomainnamevalue = getValueByParmName(
                            "Home_network_domain_name", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> Home_network_domain_name = "
                                    + homenetworkdomainnamevalue);
                        }
                        RcsSettings.getInstance().writeParameter(
                                RcsSettingsData.USERPROFILE_IMS_HOME_DOMAIN,
                                homenetworkdomainnamevalue);
                        continue;
                    }
                }

                if (voicedomainpreferenceeutranvalue == null) {
                    if ((voicedomainpreferenceeutranvalue = getValueByParmName(
                            "Voice_Domain_Preference_E_UTRAN", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> Voice_Domain_Preference_E_UTRAN = "
                                    + voicedomainpreferenceeutranvalue);
                        }
                        // !!! Not used !!!
                        // Indicates network operator's preference for selection
                        // of the domain to be used for voice
                        // communication services by the UE.
                        // Values: 1, 2, 3, 4
                        // 1 indicates that the UE does not attempt to
                        // initiate voice sessions over the IM CN Subsystem
                        // using an E-UTRAN bearer. This value equates to
                        // "CS Voice only" as described in 3GPP TS 23.221.
                        // 2 indicates that the UE preferably attempts
                        // to use the CS domain to originate voice sessions.
                        // In addition, a UE, in accordance with TS 24.292, upon
                        // receiving a request for a session including
                        // voice, preferably attempts to use the CS domain for
                        // the audio media stream. This value equates to
                        // "CS Voice preferred, IMS PS Voice as secondary" as
                        // described in 3GPP TS 23.221.
                        // 3 indicates that the UE preferably attempts
                        // to use the IM CN Subsystem using an E-UTRAN bearer to
                        // originate sessions including voice. In addition, a
                        // UE, in accordance with TS 24.292, upon receiving
                        // a request for a session including voice, preferably
                        // attempts to use an E-UTRAN bearer for the audio
                        // media stream. This value equates to
                        // "IMS PS Voice preferred, CS Voice as secondary" as
                        // described in
                        // 3GPP TS 23.221.
                        // 4 indicates that the UE attempts to initiate
                        // voice sessions over IM CN Subsystem using an E-UTRAN
                        // bearer.
                        // In addition, a UE, upon receiving a request for a
                        // session including voice, attempts to use an E-UTRAN
                        // bearer for all the the audio media stream(s). This
                        // value equates to "IMS PS Voice only" as described
                        // in 3GPP TS 23.221.
                        continue;
                    }
                }

                if (smsoveripnetworksindicationvalue == null) {
                    if ((smsoveripnetworksindicationvalue = getValueByParmName(
                            "SMS_Over_IP_Networks_Indication", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> SMS_Over_IP_Networks_Indication = "
                                    + smsoveripnetworksindicationvalue);
                        }
                        // !!! Not used !!!
                        // Indicates network operator's preference for selection
                        // of the domain to be used for short message service
                        // (SMS) originated by the UE.
                        // Values: 0, 1
                        // 0 indicates that the SMS service is not to
                        // be invoked over the IP networks.
                        // 1 indicates that the SMS service is
                        // preferred to be invoked over the IP networks.
                        continue;
                    }
                }

                if (keepaliveenabledvalue == null) {
                    if ((keepaliveenabledvalue = getValueByParmName("Keep_Alive_Enabled", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> Keep_Alive_Enabled = " + keepaliveenabledvalue);
                        }
                        if (keepaliveenabledvalue.equals("1")) {
                            RcsSettings.getInstance().writeParameter(
                                    RcsSettingsData.SIP_KEEP_ALIVE, RcsSettingsData.TRUE);
                        } else {
                            RcsSettings.getInstance().writeParameter(
                                    RcsSettingsData.SIP_KEEP_ALIVE, RcsSettingsData.FALSE);
                        }
                        continue;
                    }
                }

                if (voicedomainpreferenceutranvalue == null) {
                    if ((voicedomainpreferenceutranvalue = getValueByParmName(
                            "Voice_Domain_Preference_UTRAN", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> Voice_Domain_Preference_UTRAN = "
                                    + voicedomainpreferenceutranvalue);
                        }
                        // !!! Not used !!!
                        // Indicates network operator's preference for selection
                        // of the domain to be used for voice communication
                        // services by the UE.
                        // Values: 1, 2, 3
                        // 1 indicates that the UE does not attempt to
                        // initiate voice sessions over the IM CN Subsystem
                        // using an
                        // UTRAN PS bearer. This value equates to
                        // "CS Voice only" as described in 3GPP TS 23.221.
                        // 2 indicates that the UE preferably attempts
                        // to use the CS domain to originate voice sessions. In
                        // addition,
                        // a UE, in accordance with 3GPP TS 24.292, upon
                        // receiving a request for a session including voice,
                        // preferably attempts to use the CS domain for the
                        // audio media stream. This value equates to
                        // "CS Voice preferred, IMS PS Voice as secondary" as
                        // described in 3GPP TS 23.221.
                        // 3 indicates that the UE preferably attempts
                        // to use the IM CN Subsystem using an UTRAN PS bearer
                        // to originate
                        // sessions including voice. In addition, a UE, in
                        // accordance with 3GPP TS 24.292, upon receiving a
                        // request for a session including voice, preferably
                        // attempts to use an UTRAN PS bearer for the audio
                        // media
                        // stream. This value equates to
                        // "IMS PS Voice preferred, CS Voice as secondary" as
                        // described in 3GPP TS 23.221.
                        continue;
                    }
                }

                if (mobilitymanagementimsvoiceterminationvalue == null) {
                    if ((mobilitymanagementimsvoiceterminationvalue = getValueByParmName(
                            "Mobility_Management_IMS_Voice_Termination", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("Mobility_Management_IMS_Voice_Termination = "
                                    + mobilitymanagementimsvoiceterminationvalue);
                        }
                        // !!! Not used !!!
                        // Indicates whether the UE mobility management performs
                        // additional procedures as specified in 3GPP TS 24.008
                        // and 3GPP TS 24.301 to support terminating access
                        // domain selection by the network.
                        // Values: 0, 1
                        // 0 Mobility Management for IMS Voice
                        // Termination disabled.
                        // 1 Mobility Management for IMS Voice
                        // Termination enabled.
                        continue;
                    }
                }

                if (regretrybasetimevalue == null) {
                    if ((regretrybasetimevalue = getValueByParmName("RegRetryBaseTime", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("=> RegRetryBaseTime = " + regretrybasetimevalue);
                        }
                        RcsSettings.getInstance().writeParameter(
                                RcsSettingsData.REGISTER_RETRY_BASE_TIME, regretrybasetimevalue);
                        continue;
                    }
                }

                if (regretrymaxtimevalue == null) {
                    if ((regretrymaxtimevalue = getValueByParmName("RegRetryMaxTime", childnode)) != null) {
                        if (logger.isActivated()) {
                            logger.debug("RegRetryMaxTime = " + regretrymaxtimevalue);
                        }
                        RcsSettings.getInstance().writeParameter(
                                RcsSettingsData.REGISTER_RETRY_MAX_TIME, regretrymaxtimevalue);
                        continue;
                    }
                }
            } while ((childnode = childnode.getNextSibling()) != null);
        }
    }

    /**
     * Get value of a parameter
     *
     * @param ParmName the parameter name
     * @param node the node
     * @return value or null
     */
    private String getValueByParmName(String ParmName, Node node) {
        Node namenode = null;
        Node valuenode = null;
        if (node == null
                || !(node.getNodeName().equals("parm") || node.getNodeName().equals("param"))) {
            return null;
        }

        if (node != null && node.getAttributes().getLength() > 0) {
            namenode = node.getAttributes().getNamedItem("name");
            if (namenode == null) {
                return null;
            }
            valuenode = node.getAttributes().getNamedItem("value");
            if (valuenode == null) {
                return null;
            }
            if (namenode.getNodeValue().equals(ParmName)) {
                return valuenode.getNodeValue();
            } else {
                return null;
            }
        }
        return null;
    }
}
