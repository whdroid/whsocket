package com.android.socket.client.impl;

import com.android.socket.client.core.utils.SLog;
import com.android.socket.client.impl.abilities.IConnectionSwitchListener;
import com.android.socket.client.sdk.client.ConnectionInfo;
import com.android.socket.client.sdk.client.WhSocketOptions;
import com.android.socket.client.sdk.client.connection.IConnectionManager;
import com.android.socket.client.common.interfaces.common_interfacies.server.IServerManager;
import com.android.socket.client.common.interfaces.common_interfacies.server.IServerManagerPrivate;
import com.android.socket.client.common.interfaces.utils.SPIUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ManagerHolder {

    private volatile Map<ConnectionInfo, IConnectionManager> mConnectionManagerMap = new HashMap<>();

    private volatile Map<Integer, IServerManagerPrivate> mServerManagerMap = new HashMap<>();

    private static class InstanceHolder {
        private static final ManagerHolder INSTANCE = new ManagerHolder();
    }

    public static ManagerHolder getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private ManagerHolder() {
        mConnectionManagerMap.clear();
    }

    public IServerManager getServer(int localPort) {
        IServerManagerPrivate manager = mServerManagerMap.get(localPort);
        if (manager == null) {
            manager = (IServerManagerPrivate) SPIUtils.load(IServerManager.class);
            if (manager == null) {
                String err = "Socket.Server() load error";
                SLog.e(err);
                throw new IllegalStateException(err);
            } else {
                synchronized (mServerManagerMap) {
                    mServerManagerMap.put(localPort, manager);
                }
                manager.initServerPrivate(localPort);
                return manager;
            }
        }
        return manager;
    }

    public IConnectionManager getConnection(ConnectionInfo info) {
        IConnectionManager manager = mConnectionManagerMap.get(info);
        if (manager == null) {
            return getConnection(info, WhSocketOptions.getDefault());
        } else {
            return getConnection(info, manager.getOption());
        }
    }

    public IConnectionManager getConnection(ConnectionInfo info, WhSocketOptions okOptions) {
        IConnectionManager manager = mConnectionManagerMap.get(info);
        if (manager != null) {
            if (!okOptions.isConnectionHolden()) {
                synchronized (mConnectionManagerMap) {
                    mConnectionManagerMap.remove(info);
                }
                return createNewManagerAndCache(info, okOptions);
            } else {
                manager.option(okOptions);
            }
            return manager;
        } else {
            return createNewManagerAndCache(info, okOptions);
        }
    }

    private IConnectionManager createNewManagerAndCache(ConnectionInfo info, WhSocketOptions okOptions) {
        AbsConnectionManager manager = new ConnectionManagerImpl(info);
        manager.option(okOptions);
        manager.setOnConnectionSwitchListener(new IConnectionSwitchListener() {
            @Override
            public void onSwitchConnectionInfo(IConnectionManager manager, ConnectionInfo oldInfo,
                                               ConnectionInfo newInfo) {
                synchronized (mConnectionManagerMap) {
                    mConnectionManagerMap.remove(oldInfo);
                    mConnectionManagerMap.put(newInfo, manager);
                }
            }
        });
        synchronized (mConnectionManagerMap) {
            mConnectionManagerMap.put(info, manager);
        }
        return manager;
    }

    protected List<IConnectionManager> getList() {
        List<IConnectionManager> list = new ArrayList<>();

        Map<ConnectionInfo, IConnectionManager> map = new HashMap<>(mConnectionManagerMap);
        Iterator<ConnectionInfo> it = map.keySet().iterator();
        while (it.hasNext()) {
            ConnectionInfo info = it.next();
            IConnectionManager manager = map.get(info);
            if (!manager.getOption().isConnectionHolden()) {
                it.remove();
                continue;
            }
            list.add(manager);
        }
        return list;
    }


}
