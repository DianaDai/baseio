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

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

import com.generallycloud.baseio.protocol.ChannelFuture;
import com.generallycloud.baseio.protocol.Future;

/**
 * @author wangkai
 *
 */
public interface SocketSessionManager {

    void broadcast(Future future) throws IOException;

    void broadcast(Future future, Collection<SocketSession> sessions) throws IOException;

    void broadcastChannelFuture(ChannelFuture future);

    void broadcastChannelFuture(ChannelFuture future, Collection<SocketSession> sessions);

    Map<Integer, SocketSession> getManagedSessions();

    int getManagedSessionSize();

    SocketSession getSession(int sessionId);

    void putSession(SocketSession session) throws RejectedExecutionException;

    void removeSession(SocketSession session);

    void sessionIdle(long currentTime);

    void stop();

}
