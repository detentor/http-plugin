package org.unbiquitous.network.http.connection;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.websocket.Session;

import org.unbiquitous.uos.core.network.connectionManager.ChannelManager;
import org.unbiquitous.uos.core.network.connectionManager.ConnectionManagerListener;
import org.unbiquitous.uos.core.network.exceptions.NetworkException;
import org.unbiquitous.uos.core.network.model.NetworkDevice;
import org.unbiquitous.uos.core.network.model.connection.ClientConnection;

public class WebSocketChannelManager implements ChannelManager {
	private WebSocketDevice device = new WebSocketDevice();
	private ConnectionManagerListener listener;
	
//	private Map<String, WebSocketConnection> connections = new HashMap<>();
	private Map<UUID, WebSocketConnection> connections = new HashMap<>();
	private Map<String, Session> sessions = new HashMap<>();
	private Map<String, String> sessionToUUID = new HashMap<>();
//	private Set<String> busyConnections = new HashSet<>();
	private Set<UUID> busyConnections = new HashSet<>();
	
	public WebSocketChannelManager(ConnectionManagerListener listener) {
		this.listener = listener;
	}
	
	public ClientConnection openPassiveConnection(String networkDeviceName)
			throws NetworkException, IOException {
		throw new RuntimeException("Passive connections are not supported by websockets");
	}

	public NetworkDevice getAvailableNetworkDevice() {
		return device;
	}

	public void tearDown() throws NetworkException, IOException {}
	
	
	public void addConnection(String uuid, String sessionId, Session session){
		sessions.put(uuid, session);
//		WebSocketDevice clientDevice = new WebSocketDevice(uuid);
//		WebSocketConnection conn = new WebSocketConnection(clientDevice, session);
//		connections.put(uuid, conn);
		sessionToUUID.put(sessionId, uuid);
	}
	
	public void notifyListener(WebSocketConnection conn){
		UUID connectionID = null;
		synchronized (busyConnections) {
			if (!busyConnections.contains(conn.connectionId)){
				connectionID = conn.connectionId;
			}else{
				busyConnections.remove(conn.connectionId);
			}
		}
		if (connectionID != null){
			synchronized (connectionID) {
				listener.handleClientConnection(conn);
			}
		}
	}
	
	public boolean knows(String uuid){
//		return connections.containsKey(uuid);
		return sessions.containsKey(uuid);
	}
	
	public WebSocketConnection getConnection(String sessionId, UUID connectionId){
		String uuid = sessionToUUID.get(sessionId);
//		return connections.get(uuid);
		
		WebSocketConnection conn = connections.get(connectionId);
		if (conn == null){
			Session session = sessions.get(uuid);
			WebSocketDevice clientDevice = new WebSocketDevice(uuid);
			conn = new WebSocketConnection(clientDevice, session);
			conn.connectionId = connectionId;
		}
		return conn;
	}
	
	@Override
	public ClientConnection openActiveConnection(String uuid) throws NetworkException, IOException {
//		busyConnections.add(uuid);
//		return connections.get(uuid);
		Session session = sessions.get(uuid);
		WebSocketDevice clientDevice = new WebSocketDevice(uuid);
		WebSocketConnection conn = new WebSocketConnection(clientDevice, session);
		conn.connectionId = UUID.randomUUID();
		connections.put(conn.connectionId, conn);
		busyConnections.add(conn.connectionId);
		return conn;
	}

	
}