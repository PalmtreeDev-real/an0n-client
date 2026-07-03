package dev.anon.client.features.chat;

import com.google.gson.*;
import com.mojang.authlib.exceptions.AuthenticationException;
import dev.anon.client.AnonClient;
import dev.anon.client.events.chat.*;
import dev.anon.client.features.chat.packet.*;
import dev.anon.client.features.chat.packet.ClientPackets.*;
import dev.anon.client.features.chat.packet.ServerPackets.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import net.minecraft.client.Minecraft;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class AxochatClient {
    private static final URI CHAT_URI = URI.create("wss://chat.liquidbounce.net:7886/ws");
    private static final int MAX_FRAME_SIZE = 65536;

    private final Minecraft mc = Minecraft.getInstance();
    private Channel channel;
    private io.netty.channel.EventLoopGroup eventLoopGroup;
    private volatile boolean isConnecting;
    private boolean isLoggedIn;

    private final PacketSerializer serializer = new PacketSerializer();
    private final PacketDeserializer deserializer = new PacketDeserializer();
    private long lastMessageTime;
    private boolean offlineMode;
    private final Gson serializerGson;
    private final Gson deserializerGson;

    public AxochatClient() {
        serializer.registerPacket("RequestMojangInfo", RequestMojangInfoPacket.class);
        serializer.registerPacket("LoginMojang", LoginMojangPacket.class);
        serializer.registerPacket("Message", ServerMessagePacket.class);
        serializer.registerPacket("PrivateMessage", ServerPrivateMessagePacket.class);
        serializer.registerPacket("BanUser", BanUserPacket.class);
        serializer.registerPacket("UnbanUser", UnbanUserPacket.class);
        serializer.registerPacket("RequestJWT", RequestJWTPacket.class);
        serializer.registerPacket("LoginJWT", LoginJWTPacket.class);

        deserializer.registerPacket("MojangInfo", MojangInfoPacket.class);
        deserializer.registerPacket("NewJWT", NewJWTPacket.class);
        deserializer.registerPacket("Message", ClientMessagePacket.class);
        deserializer.registerPacket("PrivateMessage", ClientPrivateMessagePacket.class);
        deserializer.registerPacket("Error", ErrorPacket.class);
        deserializer.registerPacket("Success", SuccessPacket.class);

        serializerGson = new GsonBuilder()
            .registerTypeAdapter(AxochatPacket.C2S.class, serializer)
            .create();

        deserializerGson = new GsonBuilder()
            .registerTypeAdapter(AxochatPacket.S2C.class, deserializer)
            .create();
    }

    public boolean isConnected() {
        return channel != null && channel.isOpen();
    }

    public boolean isConnecting() {
        return isConnecting;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public CompletableFuture<Void> connect() {
        CompletableFuture<Void> future = new CompletableFuture<>();

        if (isConnecting || isConnected()) {
            future.complete(null);
            return future;
        }

        AnonClient.EVENT_BUS.post(new ClientChatStateChange(ClientChatStateChange.State.CONNECTING));
        isConnecting = true;
        isLoggedIn = false;

        boolean ssl = CHAT_URI.getScheme().equalsIgnoreCase("wss");

        try {
            SslContext sslContext = null;
            if (ssl) {
                sslContext = SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .build();
            }

            WebSocketClientHandshaker handshaker = WebSocketClientHandshakerFactory.newHandshaker(
                CHAT_URI, WebSocketVersion.V13, null, true, new DefaultHttpHeaders(), MAX_FRAME_SIZE
            );

            SslContext finalSslContext = sslContext;

            eventLoopGroup = new io.netty.channel.nio.NioEventLoopGroup();
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        if (finalSslContext != null) {
                            p.addLast(finalSslContext.newHandler(ch.alloc()));
                        }
                        p.addLast(new HttpClientCodec());
                        p.addLast(new HttpObjectAggregator(MAX_FRAME_SIZE));
                        p.addLast(new WebSocketHandler(handshaker, future));
                    }
                });

            bootstrap.connect(CHAT_URI.getHost(), CHAT_URI.getPort()).addListener((ChannelFutureListener) cf -> {
                if (cf.isSuccess()) {
                    channel = cf.channel();
                } else {
                    isConnecting = false;
                    AnonClient.EVENT_BUS.post(new ClientChatErrorEvent(
                        cf.cause().getLocalizedMessage() != null ?
                            cf.cause().getLocalizedMessage() :
                            cf.cause().getMessage() != null ?
                                cf.cause().getMessage() :
                                cf.cause().getClass().getName()
                    ));
                    future.completeExceptionally(cf.cause());
                }
            });
        } catch (Exception e) {
            isConnecting = false;
            AnonClient.EVENT_BUS.post(new ClientChatErrorEvent(
                e.getLocalizedMessage() != null ? e.getLocalizedMessage() : e.getMessage()
            ));
            future.completeExceptionally(e);
        }

        return future;
    }

    public void disconnect() {
        if (channel != null) {
            channel.writeAndFlush(new CloseWebSocketFrame(1000, ""))
                .addListener(ChannelFutureListener.CLOSE);
            channel = null;
        }
        if (eventLoopGroup != null) {
            eventLoopGroup.shutdownGracefully();
            eventLoopGroup = null;
        }
        AnonClient.EVENT_BUS.post(new ClientChatStateChange(ClientChatStateChange.State.DISCONNECTED));
        isConnecting = false;
        isLoggedIn = false;
    }

    public void setOfflineMode(boolean offline) {
        this.offlineMode = offline;
    }

    public void requestMojangLogin() {
        sendPacket(new RequestMojangInfoPacket());
    }

    private void loginOffline() {
        AnonClient.EVENT_BUS.post(new ClientChatStateChange(ClientChatStateChange.State.LOGGING_IN));
        sendPacket(new LoginMojangPacket(
            mc.getUser().getName(),
            mc.getUser().getProfileId(),
            true
        ));
    }

    public void sendMessage(String message) {
        long now = System.currentTimeMillis();
        if (now - lastMessageTime < 1500) {
            AnonClient.EVENT_BUS.post(new ClientChatErrorEvent("Please wait before sending another message."));
            return;
        }
        lastMessageTime = now;
        sendPacket(new ServerMessagePacket(message));
    }

    public void sendPrivateMessage(String receiver, String message) {
        sendPacket(new ServerPrivateMessagePacket(receiver, message));
    }

    public void banUser(String target) {
        sendPacket(new BanUserPacket(target));
    }

    public void unbanUser(String target) {
        sendPacket(new UnbanUserPacket(target));
    }

    public void loginViaJwt(String token) {
        AnonClient.EVENT_BUS.post(new ClientChatStateChange(ClientChatStateChange.State.LOGGING_IN));
        sendPacket(new LoginJWTPacket(token, true));
    }

    public void sendRawPacket(AxochatPacket.C2S packet) {
        sendPacket(packet);
    }

    void sendPacket(AxochatPacket.C2S packet) {
        if (channel != null && channel.isOpen()) {
            String json = serializerGson.toJson(packet, AxochatPacket.C2S.class);
            channel.writeAndFlush(new TextWebSocketFrame(json));
        }
    }

    void handlePlainMessage(String message) {
        AnonClient.LOG.info("[AN0N Chat] RAW << {}", message);
        try {
            AxochatPacket.S2C packet = deserializerGson.fromJson(message, AxochatPacket.S2C.class);
            handleFunctionalPacket(packet);
        } catch (Exception e) {
            AnonClient.LOG.error("[AN0N Chat] Deserialization failed for: {}", message, e);
        }
    }

    private void handleFunctionalPacket(AxochatPacket.S2C packet) {
        if (packet == null) {
            AnonClient.LOG.warn("[AN0N Chat] Received null packet");
            return;
        }

        if (packet instanceof MojangInfoPacket mojangInfo) {
            AnonClient.LOG.info("[AN0N Chat] Received MojangInfo, sessionHash='{}'", mojangInfo.getSessionHash());

            if (offlineMode) {
                loginOffline();
                return;
            }

            var services = mc.services();
            var profileId = mc.getUser().getProfileId();
            var accessToken = mc.getUser().getAccessToken();
            var sessionHash = mojangInfo.getSessionHash();

            AnonClient.EVENT_BUS.post(new ClientChatStateChange(ClientChatStateChange.State.LOGGING_IN));

            try {
                services.sessionService().joinServer(profileId, accessToken, sessionHash);
                sendPacket(new LoginMojangPacket(
                    mc.getUser().getName(),
                    mc.getUser().getProfileId(),
                    true
                ));
            } catch (AuthenticationException e) {
                AnonClient.LOG.error("[AN0N Chat] AuthenticationException: {}", e.getMessage());
                AnonClient.EVENT_BUS.post(new ClientChatStateChange(ClientChatStateChange.State.AUTHENTICATION_FAILED));
            } catch (Exception e) {
                AnonClient.LOG.error("[AN0N Chat] joinServer error", e);
                AnonClient.EVENT_BUS.post(new ClientChatErrorEvent(
                    e.getLocalizedMessage() != null ? e.getLocalizedMessage() :
                        e.getMessage() != null ? e.getMessage() :
                            e.getClass().getName()
                ));
            }
            return;
        }

        if (packet instanceof ClientMessagePacket msg) {
            AnonClient.EVENT_BUS.post(new ClientChatMessageEvent(
                msg.getUser(), msg.getContent(),
                ClientChatMessageEvent.ChatGroup.PUBLIC_CHAT
            ));
            return;
        }

        if (packet instanceof ClientPrivateMessagePacket pm) {
            AnonClient.EVENT_BUS.post(new ClientChatMessageEvent(
                pm.getUser(), pm.getContent(),
                ClientChatMessageEvent.ChatGroup.PRIVATE_CHAT
            ));
            return;
        }

        if (packet instanceof ErrorPacket error) {
            AnonClient.EVENT_BUS.post(new ClientChatErrorEvent(translateErrorMessage(error)));
            return;
        }

        if (packet instanceof SuccessPacket success) {
            switch (success.getReason()) {
                case "Login" -> {
                    AnonClient.EVENT_BUS.post(new ClientChatStateChange(ClientChatStateChange.State.LOGGED_IN));
                    isLoggedIn = true;
                }
                case "Ban" -> AnonClient.LOG.info("[AN0N Chat] Successfully banned user!");
                case "Unban" -> AnonClient.LOG.info("[AN0N Chat] Successfully unbanned user!");
            }
            return;
        }

        if (packet instanceof NewJWTPacket jwt) {
            AnonClient.EVENT_BUS.post(new ClientChatJwtTokenEvent(jwt.getToken()));
        }
    }

    private String translateErrorMessage(ErrorPacket packet) {
        return switch (packet.getMessage()) {
            case "NotSupported" -> "This method is not supported!";
            case "LoginFailed" -> "Login Failed!";
            case "NotLoggedIn" -> "You must be logged in to use the chat!";
            case "AlreadyLoggedIn" -> "You are already logged in!";
            case "MojangRequestMissing" -> "Mojang request missing!";
            case "NotPermitted" -> "You are missing the required permissions!";
            case "NotBanned" -> "You are not banned!";
            case "Banned" -> "You are banned!";
            case "RateLimited" -> "You have been rate limited. Please try again later.";
            case "PrivateMessageNotAccepted" -> "Private message not accepted!";
            case "EmptyMessage" -> "You are trying to send an empty message!";
            case "MessageTooLong" -> "Message is too long!";
            case "InvalidCharacter" -> "Message contains a non-ASCII character!";
            case "InvalidId" -> "The given ID is invalid!";
            case "Internal" -> "An internal server error occurred!";
            default -> packet.getMessage();
        };
    }

    private class WebSocketHandler extends SimpleChannelInboundHandler<Object> {
        private final WebSocketClientHandshaker handshaker;
        private final CompletableFuture<Void> connectFuture;
        private ChannelPromise handshakeFuture;

        WebSocketHandler(WebSocketClientHandshaker handshaker, CompletableFuture<Void> connectFuture) {
            this.handshaker = handshaker;
            this.connectFuture = connectFuture;
        }

        @Override
        public void handlerAdded(ChannelHandlerContext ctx) {
            handshakeFuture = ctx.newPromise();
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            handshaker.handshake(ctx.channel());
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            AnonClient.EVENT_BUS.post(new ClientChatStateChange(ClientChatStateChange.State.DISCONNECTED));
            isConnecting = false;
            isLoggedIn = false;
            if (eventLoopGroup != null) {
                eventLoopGroup.shutdownGracefully();
                eventLoopGroup = null;
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            AnonClient.LOG.error("AN0N Chat error", cause);
            AnonClient.EVENT_BUS.post(new ClientChatErrorEvent(
                cause.getLocalizedMessage() != null ? cause.getLocalizedMessage() :
                    cause.getMessage() != null ? cause.getMessage() :
                        cause.getClass().getName()
            ));

            if (!handshakeFuture.isDone()) {
                handshakeFuture.setFailure(cause);
            }
            ctx.close();
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
            Channel ch = ctx.channel();

            if (!handshaker.isHandshakeComplete()) {
                try {
                    handshaker.finishHandshake(ch, (FullHttpResponse) msg);
                    handshakeFuture.setSuccess();
                    isConnecting = false;

                    if (isConnected()) {
                        AnonClient.EVENT_BUS.post(new ClientChatStateChange(ClientChatStateChange.State.CONNECTED));
                    }
                    connectFuture.complete(null);
                } catch (WebSocketHandshakeException e) {
                    handshakeFuture.setFailure(e);
                    connectFuture.completeExceptionally(e);
                }
                return;
            }

            if (msg instanceof TextWebSocketFrame frame) {
                handlePlainMessage(frame.text());
            } else if (msg instanceof CloseWebSocketFrame) {
                ch.close();
            }
        }
    }
}
