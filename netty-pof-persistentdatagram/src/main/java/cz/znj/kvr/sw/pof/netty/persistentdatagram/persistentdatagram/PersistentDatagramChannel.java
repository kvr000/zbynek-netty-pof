package cz.znj.kvr.sw.pof.netty.persistentdatagram.persistentdatagram;

import io.netty.buffer.Unpooled;
import io.netty.channel.AbstractChannel;
import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelMetadata;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoop;
import io.netty.channel.RecvByteBufAllocator;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.ReferenceCountUtil;

import java.net.InetSocketAddress;
import java.net.SocketAddress;


/**
 * Created by rat on 2015-09-20.
 */
public class PersistentDatagramChannel extends AbstractChannel
{
	public PersistentDatagramChannel(Channel parentChannel, InetSocketAddress childPeerAddress)
	{
		super(null);
		this.parentChannel = parentChannel;
		this.childPeerAddress = childPeerAddress;
		unsafe();
	}

	@Override
	protected AbstractUnsafe newUnsafe()
	{
		return null;
	}

	@Override
	public Unsafe			unsafe()
	{
		if (unsafe == null)
			unsafe = new ForwardingUnsafe();
		return unsafe;
	}

	@Override
	protected boolean isCompatible(EventLoop loop)
	{
		return loop instanceof EventLoop;
	}

	@Override
	protected SocketAddress localAddress0()
	{
		return parentChannel.localAddress();
	}

	@Override
	protected SocketAddress remoteAddress0()
	{
		return childPeerAddress;
	}

	@Override
	protected void doBind(SocketAddress localAddress) throws Exception
	{
		// NOOP
	}

	@Override
	protected void doDisconnect() throws Exception
	{
		doClose();
	}

	@Override
	protected void doClose() throws Exception
	{
		closed = true;
	}

	@Override
	protected void doBeginRead() throws Exception
	{
		// NOOP
	}

	@Override
	protected void doWrite(ChannelOutboundBuffer in) throws Exception
	{
		// NOOP
	}

	@Override
	public ChannelConfig config()
	{
		return parentChannel.config();
	}

	@Override
	public boolean isOpen()
	{
		return !closed;
	}

	@Override
	public boolean isActive()
	{
		return !closed;
	}


	private class ForwardingUnsafe extends AbstractUnsafe
	{
		@Override
		public RecvByteBufAllocator.Handle recvBufAllocHandle()
		{
			throw new UnsupportedOperationException("connect");
		}

		@Override
		public SocketAddress localAddress()
		{
			return parentChannel.localAddress();
		}

		@Override
		public SocketAddress remoteAddress()
		{
			return childPeerAddress;
		}

		@Override
		public void bind(SocketAddress localAddress, ChannelPromise promise)
		{
			throw new UnsupportedOperationException("bind");
		}

		@Override
		public void connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
			throw new UnsupportedOperationException("connect");
		}

		@Override
		public void disconnect(ChannelPromise promise)
		{
			close(promise);
		}

		@Override
		public void close(ChannelPromise promise)
		{
			// remove the io.netty.channel from manager
		}

		@Override
		public void closeForcibly()
		{
			close(null);
		}

		@Override
		public void deregister(ChannelPromise promise)
		{
			throw new UnsupportedOperationException("deregister");
		}

		@Override
		public void beginRead()
		{
			//throw new UnsupportedOperationException("beginRead");
		}

		@Override
		public void write(Object msg, ChannelPromise promise)
		{
			parentChannel.pipeline().writeAndFlush(new DatagramPacket(Unpooled.wrappedBuffer((byte[])msg), childPeerAddress));
		}

		@Override
		public void flush()
		{
			parentChannel.pipeline().flush();
		}

		@Override
		public ChannelPromise voidPromise()
		{
			throw new UnsupportedOperationException("voidPromise");
		}

		@Override
		public ChannelOutboundBuffer outboundBuffer()
		{
			throw new UnsupportedOperationException("outboundBuffer");
		}
	}


	@Override
	public ChannelMetadata metadata()
	{
		return parentChannel.metadata();
	}

	protected Channel		parentChannel;

	protected InetSocketAddress	childPeerAddress;

	boolean				closed = false;

	Unsafe				unsafe = null;
}
