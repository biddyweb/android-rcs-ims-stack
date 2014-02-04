package com.orangelabs.rcs.richcall;

import java.util.Vector;

import android.test.AndroidTestCase;

import com.orangelabs.rcs.core.ims.network.sip.SipUtils;
import com.orangelabs.rcs.core.ims.protocol.rtp.codec.video.h264.H264Config;
import com.orangelabs.rcs.core.ims.protocol.rtp.codec.video.h264.profiles.H264Profile1_2;
import com.orangelabs.rcs.core.ims.protocol.rtp.codec.video.h264.profiles.H264Profile1b;
import com.orangelabs.rcs.core.ims.protocol.sdp.MediaDescription;
import com.orangelabs.rcs.core.ims.protocol.sdp.SdpParser;
import com.orangelabs.rcs.core.ims.service.richcall.video.VideoCodecManager;
import com.orangelabs.rcs.core.ims.service.richcall.video.VideoSdpBuilder;
import com.orangelabs.rcs.provider.settings.RcsSettings;
import com.orangelabs.rcs.service.api.client.media.MediaCodec;
import com.orangelabs.rcs.service.api.client.media.video.VideoCodec;
import com.orangelabs.rcs.utils.logger.Logger;

public class VideoSdpTest extends AndroidTestCase {
    private static int RTP_PORT = 12345;
    private static String videoSdp = 
            "v=tester" + SipUtils.CRLF +
            "m=video 12345 RTP/AVP 99 98 97 96" + SipUtils.CRLF +
            "a=framerate:15" + SipUtils.CRLF +
            "a=rtpmap:99 H264/90000" + SipUtils.CRLF +
            "a=framesize:99 352-288" + SipUtils.CRLF +
            "a=fmtp:99 profile-level-id=42800c;packetization-mode=1" + SipUtils.CRLF +
            "a=rtpmap:98 H264/90000" + SipUtils.CRLF +
            "a=framesize:98 352-288" + SipUtils.CRLF +
            "a=fmtp:98 profile-level-id=42800c;packetization-mode=1" + SipUtils.CRLF +
            "a=rtpmap:97 H264/90000" + SipUtils.CRLF +
            "a=framesize:97 320-240" + SipUtils.CRLF +
            "a=fmtp:97 profile-level-id=42800c;packetization-mode=1" + SipUtils.CRLF +
            "a=rtpmap:96 H264/90000" + SipUtils.CRLF +
            "a=framesize:96 176-144" + SipUtils.CRLF +
            "a=fmtp:96 profile-level-id=42900b;packetization-mode=1" + SipUtils.CRLF;
    private static String videoSdp2 = 
            "v=tester" + SipUtils.CRLF +
            "m=video 12345 RTP/AVP 99 98 97 96" + SipUtils.CRLF +
            "b=AS:128" + SipUtils.CRLF +
            "b=RS:256" + SipUtils.CRLF +
            "b=RR:1024" + SipUtils.CRLF +
            "a=rtpmap:99 H264/90000" + SipUtils.CRLF +
            "a=framesize:99 352-288" + SipUtils.CRLF +
            "a=framerate:99 15" + SipUtils.CRLF +
            "a=fmtp:99 profile-level-id=42800c;packetization-mode=1" + SipUtils.CRLF +
            "a=rtpmap:98 H264/90000" + SipUtils.CRLF +
            "a=framesize:98 352-288" + SipUtils.CRLF +
            "a=framerate:98 12" + SipUtils.CRLF +
            "a=fmtp:98 profile-level-id=42800c;packetization-mode=1" + SipUtils.CRLF +
            "a=rtpmap:97 H264/90000" + SipUtils.CRLF +
            "a=framesize:97 320-240" + SipUtils.CRLF +
            "a=framerate:97 12" + SipUtils.CRLF +
            "a=fmtp:97 profile-level-id=42800c;packetization-mode=1" + SipUtils.CRLF +
            "a=rtpmap:96 H264/90000" + SipUtils.CRLF +
            "a=framesize:96 176-144" + SipUtils.CRLF +
            "a=framerate:96 10" + SipUtils.CRLF +
            "a=fmtp:96 profile-level-id=42900b;packetization-mode=1" + SipUtils.CRLF;
    private MediaCodec[] codecs;

    protected void setUp() throws Exception {
        super.setUp();

        RcsSettings.createInstance(getContext());

        // Create list of codecs
        codecs = new MediaCodec[4];
        int payload_count = 95;
        codecs[3] = new VideoCodec(H264Config.CODEC_NAME,
                ++payload_count,
                H264Config.CLOCK_RATE,
                H264Config.CODEC_PARAM_PROFILEID + "=" + H264Profile1b.BASELINE_PROFILE_ID + ";" + H264Config.CODEC_PARAM_PACKETIZATIONMODE + "=1",
                10,
                96000,
                H264Config.QCIF_WIDTH, 
                H264Config.QCIF_HEIGHT).getMediaCodec();
        codecs[2] = new VideoCodec(H264Config.CODEC_NAME,
                ++payload_count,
                H264Config.CLOCK_RATE,
                H264Config.CODEC_PARAM_PROFILEID + "=" + H264Profile1_2.BASELINE_PROFILE_ID + ";" + H264Config.CODEC_PARAM_PACKETIZATIONMODE + "=1",
                12,
                256,
                H264Config.QVGA_WIDTH, 
                H264Config.QVGA_HEIGHT).getMediaCodec();
        codecs[1] = new VideoCodec(H264Config.CODEC_NAME,
                ++payload_count,
                H264Config.CLOCK_RATE,
                H264Config.CODEC_PARAM_PROFILEID + "=" + H264Profile1_2.BASELINE_PROFILE_ID + ";" + H264Config.CODEC_PARAM_PACKETIZATIONMODE + "=1",
                12,
                256,
                H264Config.CIF_WIDTH, 
                H264Config.CIF_HEIGHT).getMediaCodec();
        codecs[0] = new VideoCodec(H264Config.CODEC_NAME,
                ++payload_count,
                H264Config.CLOCK_RATE,
                H264Config.CODEC_PARAM_PROFILEID + "=" + H264Profile1_2.BASELINE_PROFILE_ID + ";" + H264Config.CODEC_PARAM_PACKETIZATIONMODE + "=1",
                15,
                396,
                H264Config.CIF_WIDTH, 
                H264Config.CIF_HEIGHT).getMediaCodec();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testCreateSdp() {
        // Create SDP
        String createdSdp = "v=tester" + SipUtils.CRLF + 
        		 VideoSdpBuilder.buildSdpOfferWithoutOrientation(codecs, RTP_PORT);
        // TEST SDP
        assertEquals(createdSdp, videoSdp);
    }

    public void testParseSdp() {
        // Parse the remote SDP part
        SdpParser parser = new SdpParser(videoSdp2.getBytes());

        // Test port
        MediaDescription mediaVideo = parser.getMediaDescription("video");
        int port = mediaVideo.port;
        assertEquals(port, RTP_PORT);

        // Test codecs
        Vector<MediaDescription> medias = parser.getMediaDescriptions("video");
        Vector<VideoCodec> proposedCodecs = VideoCodecManager.extractVideoCodecsFromSdp(medias);
        assertEquals(proposedCodecs.size(), codecs.length);
        for (int i = 0 ; i < proposedCodecs.size(); i++) {
            VideoCodec codec = new VideoCodec(codecs[i]);
            assertEquals(proposedCodecs.elementAt(i).getCodecName(), codec.getCodecName());
            assertEquals(proposedCodecs.elementAt(i).getPayload(), codec.getPayload());
            assertEquals(proposedCodecs.elementAt(i).getCodecParams(), codec.getCodecParams());
            assertEquals(proposedCodecs.elementAt(i).getFramerate(), codec.getFramerate());
            assertEquals(proposedCodecs.elementAt(i).getWidth(), codec.getWidth());
            assertEquals(proposedCodecs.elementAt(i).getHeight(), codec.getHeight());
            // Bitrate and order pref not tested because not in SDP
        }
    }
}
