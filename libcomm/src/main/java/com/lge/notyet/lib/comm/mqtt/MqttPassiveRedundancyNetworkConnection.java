package com.lge.notyet.lib.comm.mqtt;

/**
 * Created by beney.kim on 2016-06-11.
 * This class provide Passive Redundancy with MqttConnection
 */

import java.net.InetAddress;

import com.eclipsesource.json.JsonObject;
import com.lge.notyet.lib.comm.INetworkCallback;
import com.lge.notyet.lib.comm.NetworkMessage;
import com.lge.notyet.lib.comm.PassiveRedundancyNetworkConnection;
import com.lge.notyet.lib.comm.Uri;
import org.eclipse.paho.client.mqttv3.*;

public class MqttPassiveRedundancyNetworkConnection extends PassiveRedundancyNetworkConnection {

    private static final int WILL_MESSAGE_MQTT_QOS = 2;

    public MqttPassiveRedundancyNetworkConnection(String connectionName, MqttNetworkConnection networkConnection) {

        super(connectionName, networkConnection);
    }

    public void connect(InetAddress ipAddress, INetworkCallback networkCb) throws UnsupportedOperationException {

        if (mBaseNetworkConnection != null) {

            MqttNetworkMessage mqttNetworkMessage = MqttNetworkMessage.build(NetworkMessage.MESSAGE_TYPE_NOTIFICATION,
                    new JsonObject()
                            .add(MASTER_SELF_CONFIGURATION_MESSAGE_ID, mServerId)
                            .add(MASTER_SELF_CONFIGURATION_MESSAGE_TYPE, MASTER_SELF_CONFIGURATION_MESSAGE_TYPE_WILL));
            mqttNetworkMessage.addMessageType(NetworkMessage.MESSAGE_TYPE_NOTIFICATION);

            MqttConnectOptions mqttOption = new MqttConnectOptions();
            mqttOption.setWill(mConnectionUri.getPath() + MqttNetworkMessage.WILL_TOPIC,
                    mqttNetworkMessage.getBytes(),
                    WILL_MESSAGE_MQTT_QOS,
                    true);

            mOriginalNetworkCallback = networkCb;
            // Because the constructor accepts MqttNetworkConnection only.
            ((MqttNetworkConnection) mBaseNetworkConnection).connect(ipAddress, mNetworkCallback, mqttOption);
        }
    }

    public void subscribeSelfConfigurationChannel(Uri channelUri) {
        subscribe(new MqttUri(mConnectionUri.getPath() + "/#"));
    }

    public void unsubscribeSelfConfigurationChannel(Uri channelUri) {
        unsubscribe(new MqttUri(mConnectionUri.getPath() + "/#"));
    }

    protected boolean preHandleMessage(Uri uri, NetworkMessage msg) {

        boolean ret = false;

        if (uri != null && uri.getPath().equals(mConnectionUri.getPath() + MqttNetworkMessage.WILL_TOPIC)) {
            if (mIsMaster == false) doSelfConfiguration();
            ret = true;
        }

        return ret || super.preHandleMessage(uri, msg);
    }
}
