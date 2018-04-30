/*
 * Copyright 2015-2017 GenerallyCloud.com
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
 */
package com.generallycloud.baseio.component;

import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;

public class SocketSessionIEListenerWrapper implements SocketSessionIdleEventListener {

    private SocketSessionIEListenerWrapper next;

    private SocketSessionIdleEventListener        value;

    public SocketSessionIEListenerWrapper(SocketSessionIdleEventListener value) {
        this.value = value;
        this.logger = LoggerFactory.getLogger(value.getClass());
    }

    public SocketSessionIEListenerWrapper getNext() {
        return next;
    }

    public void setNext(SocketSessionIEListenerWrapper next) {
        this.next = next;
    }

    private Logger logger = null;

    @Override
    public void sessionIdled(SocketSession session, long lastIdleTime, long currentTime) {

        try {
            value.sessionIdled(session, lastIdleTime, currentTime);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        SocketSessionIEListenerWrapper listener = getNext();

        if (listener == null) {
            return;
        }

        listener.sessionIdled(session, lastIdleTime, currentTime);
    }

}
