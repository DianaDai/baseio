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
package com.generallycloud.baseio.component.ssl;

import java.io.IOException;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLEngineResult.Status;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.buffer.EmptyByteBuf;
import com.generallycloud.baseio.common.ReleaseUtil;
import com.generallycloud.baseio.component.SocketChannel;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.protocol.ChannelWriteFuture;
import com.generallycloud.baseio.protocol.ChannelWriteFutureImpl;
import com.generallycloud.baseio.protocol.EmptyReadFuture;

public class SslHandler {

	private ChannelWriteFuture	EMPTY_CWF	= null;

	public SslHandler(SocketChannelContext context) {
		this.EMPTY_CWF = new ChannelWriteFutureImpl(
				EmptyReadFuture.getInstance()
				, EmptyByteBuf.getInstance());
	}

	private ByteBuf allocate(SocketChannel channel, int capacity) {
		return channel.getByteBufAllocator().allocate(capacity);
	}

	public ByteBuf wrap(SocketChannel channel,ByteBuf src) throws IOException {
		
		SSLEngine engine = channel.getSSLEngine();
		
		ByteBuf dst = allocate(channel,engine.getSession().getPacketBufferSize() * 2);

		try {

			for (;;) {

				SSLEngineResult result = engine.wrap(src.nioBuffer(), dst.nioBuffer());

				Status status = result.getStatus();
				
				HandshakeStatus handshakeStatus = result.getHandshakeStatus();

				synchByteBuf(result, src, dst);

				if (status == Status.CLOSED) {
					return gc(channel,dst.flip());
				} else {
					switch (handshakeStatus) {
					case NEED_UNWRAP:
					case NOT_HANDSHAKING:
						return gc(channel,dst.flip());
					case NEED_TASK:
						runDelegatedTasks(engine);
						break;
					case FINISHED:
						channel.finishHandshake(null);
						break;
					default:
						// continue
						break;
					}
				}
			}
		} catch (Throwable e) {

			ReleaseUtil.release(dst);

			if (e instanceof IOException) {

				throw (IOException) e;
			}

			throw new IOException(e);
		}
	}

	//FIXME 部分buf不需要gc
	private ByteBuf gc(SocketChannel channel,ByteBuf buf) throws IOException {

		ByteBuf out = allocate(channel,buf.limit());

		try {

			out.read(buf);

		} catch (Exception e) {

			ReleaseUtil.release(out);

			throw e;
		}

		ReleaseUtil.release(buf);

		return out.flip();
	}

	public ByteBuf unwrap(SocketChannel channel, ByteBuf src) throws IOException {

		ByteBuf dst = allocate(channel,src.capacity() * 2);

		boolean release = true;

		SSLEngine sslEngine = channel.getSSLEngine();

		try {
			for (;;) {

				SSLEngineResult result = sslEngine.unwrap(src.nioBuffer(), dst.nioBuffer());
				
				HandshakeStatus handshakeStatus = result.getHandshakeStatus();

				synchByteBuf(result, src, dst);

				switch (handshakeStatus) {
				case NEED_UNWRAP:
					return null;
				case NEED_WRAP:
					channel.flush(EMPTY_CWF.duplicate());
					return null;
				case NEED_TASK:
					runDelegatedTasks(sslEngine);
					continue;
				case FINISHED:
					channel.finishHandshake(null);
					return null;
				case NOT_HANDSHAKING:
					release = false;
					return dst.flip();
				default:
					throw new IllegalStateException("unknown handshake status: " + handshakeStatus);
				}
			}
		} finally {

			if (release) {
				ReleaseUtil.release(dst);
			}
		}
	}
	
	private void synchByteBuf(SSLEngineResult result,ByteBuf src,ByteBuf dst){
		
		//FIXME 同步。。。。。
		src.reverse();
		dst.reverse();
		
//		int bytesConsumed = result.bytesConsumed();
//		int bytesProduced = result.bytesProduced();
//		
//		if (bytesConsumed > 0) {
//			src.skipBytes(bytesConsumed);
//		}
//
//		if (bytesProduced > 0) {
//			dst.skipBytes(bytesProduced);
//		}
	}

	private void runDelegatedTasks(SSLEngine engine) {
		
		for (;;) {
		
			Runnable task = engine.getDelegatedTask();
			
			if (task == null) {
				break;
			}
			
			task.run();
		}
	}
}