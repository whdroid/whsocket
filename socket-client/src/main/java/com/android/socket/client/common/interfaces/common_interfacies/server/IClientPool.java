package com.android.socket.client.common.interfaces.common_interfacies.server;


import com.android.socket.client.core.iocore.interfaces.ISendable;

public interface IClientPool<T, K> {

    void cache(T t);

    T findByUniqueTag(K key);

    int size();

    void sendToAll(ISendable sendable);
}
