package com.lge.notyet.lib.comm.mqtt;

/**
 * Created by beney.kim on 2016-06-11.
 * This class provide Passive Redundancy for MqttBaseChannel
 */

import java.net.InetAddress;

import com.eclipsesource.json.JsonObject;
import com.lge.notyet.lib.comm.*;
import com.lge.notyet.lib.comm.util.Log;
import org.eclipse.paho.client.mqttv3.*;

public class MqttPassiveRedundancyNetworkConnection extends PassiveRedundancyNetworkConnection {

    private static final int SERVER_REDUNDANCY_PING_ECHO_TIME = 10;

    protected boolean preHandleConnected() {
        mWillSubscribeChannel.listen();
        return super.preHandleConnected();
    }

    public MqttPassiveRedundancyNetworkConnection(String channelName, MqttNetworkConnection networkConnection) {

        super(channelName, networkConnection);
        mWillSubscribeChannel = new WillSubscribeChannel(networkConnection);
    }

    public void connect(InetAddress ipAddress, INetworkCallback networkCb) throws UnsupportedOperationException {

        if (mBaseNetworkConnection != null) {

            MqttNetworkMessage mqttNetworkMessage = getWillMessage();
            mqttNetworkMessage.addMessageType(NetworkMessage.MESSAGE_TYPE_NOTIFICATION);

            MqttConnectOptions mqttOption = new MqttConnectOptions();
            mqttOption.setWill(getSelfConfigurationUri().getLocation() + MqttConstants.WILL_MESSAGE_TOPIC,
                    mqttNetworkMessage.getBytes(),
                    MqttConstants.WILL_MESSAGE_QOS,
                    true);
            mqttOption.setKeepAliveInterval(SERVER_REDUNDANCY_PING_ECHO_TIME);

            mOriginalNetworkCallback = networkCb;
            // Because the constructor accepts MqttNetworkChannel only.
            ((MqttNetworkConnection) mBaseNetworkConnection).connect(ipAddress, mNetworkCallback, mqttOption);
        }
    }

    private final class WillSubscribeChannel extends SubscribeChannel {

        WillSubscribeChannel(INetworkConnection networkConnection) {
            super(networkConnection);
        }

        @Override
        public Uri getChannelDescription() {
            return new MqttUri(getSelfConfigurationUri().getLocation() + MqttConstants.WILL_MESSAGE_TOPIC);
        }

        @Override
        public void onNotify(NetworkChannel networkChannel, Uri uri, NetworkMessage message) {
            Log.logd("WillSubscribeChannel", "onNotified:" + message.getMessage() + " on channel=" + getChannelDescription());
            if (!mIsMaster) doSelfConfiguration();
        }
    }
    private final WillSubscribeChannel mWillSubscribeChannel;

    protected Uri getSelfConfigurationUri() {
        return new MqttUri("/master_slave/" + mChannelName);
    }

    protected NetworkMessage getMasterSolicitationMessage() {

         return MqttNetworkMessage.build(new JsonObject()
                         .add(MASTER_SELF_CONFIGURATION_MESSAGE_ID, mServerId)
                         .add(MASTER_SELF_CONFIGURATION_MESSAGE_TYPE, MASTER_SELF_CONFIGURATION_MESSAGE_TYPE_REQUEST));
    }

    protected NetworkMessage getMasterAdvertisementMessage() {
        return MqttNetworkMessage.build(new JsonObject()
                        .add(MASTER_SELF_CONFIGURATION_MESSAGE_ID, mServerId)
                        .add(MASTER_SELF_CONFIGURATION_MESSAGE_TYPE, MASTER_SELF_CONFIGURATION_MESSAGE_TYPE_RESPONSE));
    }

    private MqttNetworkMessage getWillMessage() {
        return MqttNetworkMessage.build(new JsonObject()
                        .add(MASTER_SELF_CONFIGURATION_MESSAGE_ID, mServerId)
                        .add(MASTER_SELF_CONFIGURATION_MESSAGE_TYPE, MASTER_SELF_CONFIGURATION_MESSAGE_TYPE_WILL));
    }

    protected boolean isLoopbackMessage(NetworkMessage message) {
        JsonObject msg = (JsonObject) message.getMessage();
        return mServerId == msg.get(MASTER_SELF_CONFIGURATION_MESSAGE_ID).asLong();
    }

    protected boolean isSolicitationMessage(NetworkMessage message) {
        JsonObject msg = (JsonObject) message.getMessage();
        return MASTER_SELF_CONFIGURATION_MESSAGE_TYPE_REQUEST.equals(msg.get(MASTER_SELF_CONFIGURATION_MESSAGE_TYPE).asString());
    }
}
