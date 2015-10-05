package cz.znj.kvr.sw.pof.netty.persistentdatagram;


import cz.znj.kvr.sw.pof.netty.persistentdatagram.persistentdatagram.PersistentDatagramDistributionHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.util.concurrent.Future;


/**
 * Created by rat on 2015-09-20.
 */
public class AsyncDatagramServer
{
	public static void		main(String[] args) throws Exception
	{
		System.exit(new AsyncDatagramServer().run(args));
	}

	public int			run(String[] args)
	{
		return this.process();
	}

	public int			process()
	{
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			Bootstrap udpb = new Bootstrap();
			udpb.group(bossGroup)
				.channel(NioDatagramChannel.class)
				.handler(new PersistentDatagramDistributionHandler(workerGroup) {
					@Override
					public Channel initChildChannel(Channel childChannel) {
						childChannel.pipeline()
							.addLast(new LongBytesCodec())
							.addLast(new IncrementServerHandler())
						;
						return childChannel;
					}
				})
				.option(ChannelOption.SO_REUSEADDR, true);

			// Bind and start to accept incoming connections.
			ChannelFuture udpf = udpb.bind(port).sync();

			new Thread(() -> {
				try {
					Thread.sleep(2000000);
				}
				catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
				udpf.channel().close();
			}).start();

			// Wait until the server socket is closed.
			// In this example, this does not happen, but you can do that to gracefully
			// shut down your server.
			udpf.channel().closeFuture().awaitUninterruptibly();
		}
		catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		finally {
			Future<?> shutdownWorkerFuture = workerGroup.shutdownGracefully();
			Future<?> shutdownBossFuture = bossGroup.shutdownGracefully();
			shutdownWorkerFuture.awaitUninterruptibly();
			shutdownBossFuture.awaitUninterruptibly();
		}
		return 0;
	}

	protected int			port = 4200;
}
