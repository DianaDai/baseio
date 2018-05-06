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
package com.generallycloud.test.io.load.http11;

import java.util.concurrent.atomic.AtomicInteger;

import com.generallycloud.baseio.acceptor.SocketChannelAcceptor;
import com.generallycloud.baseio.codec.http11.ServerHttpCodec;
import com.generallycloud.baseio.component.IoEventHandleAdaptor;
import com.generallycloud.baseio.component.LoggerSocketSEListener;
import com.generallycloud.baseio.component.NioSocketChannelContext;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.configuration.Configuration;
import com.generallycloud.baseio.protocol.Future;

public class TestHttpLoadServer {

    public static void main(String[] args) throws Exception {

        final AtomicInteger res = new AtomicInteger();
        final AtomicInteger req = new AtomicInteger();

        IoEventHandleAdaptor eventHandleAdaptor = new IoEventHandleAdaptor() {

            @Override
            public void accept(SocketSession session, Future future) throws Exception {
                future.write("hello world!8080",session.getContext());
                session.flush(future);
                //				System.out.println("req======================"+req.getAndIncrement());
            }

        };

        Configuration c = new Configuration(8080);

        //		c.setSERVER_MEMORY_POOL_CAPACITY(2560000);
        c.setSERVER_MEMORY_POOL_UNIT(256);
        c.setSERVER_ENABLE_MEMORY_POOL_DIRECT(true);
        		c.setSERVER_CORE_SIZE(1);
        c.setSERVER_ENABLE_MEMORY_POOL(true);
        c.setSERVER_MEMORY_POOL_CAPACITY_RATE(1);

        SocketChannelContext context = new NioSocketChannelContext(c);

        SocketChannelAcceptor acceptor = new SocketChannelAcceptor(context);

        context.setProtocolCodec(new ServerHttpCodec());
        context.setIoEventHandleAdaptor(eventHandleAdaptor);
        context.addSessionEventListener(new LoggerSocketSEListener());

        acceptor.bind();
    }
}
