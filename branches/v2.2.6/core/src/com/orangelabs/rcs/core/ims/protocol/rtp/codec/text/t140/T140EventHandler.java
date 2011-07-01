/*******************************************************************************
 * Software Name : RCS IMS Stack
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

package com.orangelabs.rcs.core.ims.protocol.rtp.codec.text.t140;

/**
 * A T140EventHandler should be able to receive new T.140 events. <br>
 * <br>
 * If a text window implements this, it will be able to receive T.140 events
 * from the depacketizer. <br>
 *
 * @author Andreas Piirimets, Omnitor AB
 */
public interface T140EventHandler {

    /**
     * Signalls a new event
     *
     * @param event The new T.140 event
     */
    public void newEvent(T140Event event);

}
