package com.lge.notyet.lib.comm;

import java.util.ArrayList;

abstract public class ServerChannelRegistry extends ServerChannel {

    protected ServerChannelRegistry(INetworkConnection networkConnection) {
        super(networkConnection);
    }

    private final ArrayList<IOnRequest> mIOnRequestList = new ArrayList <>();

    public ServerChannelRegistry addObserver(IOnRequest observer) {
        synchronized (ServerChannelRegistry.class) {
            mIOnRequestList.add(observer);
        }
        return this;
    }

    public ServerChannelRegistry removeObserver(IOnRequest observer) {
        synchronized (ServerChannelRegistry.class) {
            mIOnRequestList.remove(observer);
        }
        return this;
    }

    @Override
    public final void onRequest(NetworkChannel networkChannel, Uri uri, NetworkMessage message) {
        synchronized (ServerChannelRegistry.class) {
            for (IOnRequest observer : mIOnRequestList) {
                try {
                    observer.onRequest(networkChannel, uri, message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
