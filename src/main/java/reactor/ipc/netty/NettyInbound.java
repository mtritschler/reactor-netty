/*
 * Copyright (c) 2011-2016 Pivotal Software Inc, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package reactor.ipc.netty;

import java.net.InetSocketAddress;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import reactor.core.publisher.Flux;
import reactor.ipc.connector.Inbound;

/**
 * An inbound-traffic API delegating to an underlying {@link Channel}
 *
 * @author Stephane Maldini
 * @since 0.6
 */
public interface NettyInbound extends Inbound<ByteBuf> {

	/**
	 * Return a pre-configured attribute stored in every inbound channel
	 * @param key attribute key
	 * @param <T> a channel attribute type
	 * @return a {@link Channel} attribute
	 * @see Channel#attr(AttributeKey)
	 */
	default <T> Attribute<T> attr(AttributeKey<T> key) {
		return context().channel()
		                .attr(key);
	}

	/**
	 * Return a {@link NettyContext} to operate on the underlying
	 * {@link Channel} state.
	 *
	 * @return the {@link NettyContext}
	 */
	NettyContext context();

	/**
	 * Assign a {@link Runnable} to be invoked when reads have become idle for the given
	 * timeout.
	 *
	 * @param idleTimeout the idle timeout
	 * @param onReadIdle the idle timeout handler
	 *
	 * @return {@literal this}
	 */
	default NettyInbound onReadIdle(long idleTimeout, Runnable onReadIdle) {
		context().addHandler(NettyPipeline.OnChannelReadIdle,
				new ReactorNetty.InboundIdleStateHandler(idleTimeout, onReadIdle));
		return this;
	}

	/**
	 * A {@link Flux} extension that allows for extra decoding operators
	 * @return a new {@link ByteBufFlux}
	 */
	@Override
	default ByteBufFlux receive() {
		return ByteBufFlux.fromInbound(receiveObject(),
				context().channel()
				         .alloc());
	}


	/**
	 * a {@literal Object} inbound {@link Flux}
	 *
	 * @return a {@literal Object} inbound {@link Flux}
	 */
	Flux<?> receiveObject();

	/**
	 * Get the address of the remote peer.
	 *
	 * @return the peer's address
	 */
	default InetSocketAddress remoteAddress() {
		return context().address();
	}

}
