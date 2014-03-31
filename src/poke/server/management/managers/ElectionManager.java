/*
 * copyright 2014, gash
 * 
 * Gash licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package poke.server.management.managers;

import io.netty.channel.Channel;

import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import poke.server.management.ManagementQueue;
import eye.Comm.Heartbeat;
import eye.Comm.LeaderElection;
import eye.Comm.Management;
import eye.Comm.LeaderElection.VoteAction;

/**
 * The election manager is used to determine leadership within the network.
 * 
 * @author gash
 * 
 */
public class ElectionManager extends Thread {
	protected static Logger logger = LoggerFactory.getLogger("management");
	protected static AtomicReference<ElectionManager> instance = new AtomicReference<ElectionManager>();

	private String nodeId;
	
	// Shibai
	private String leaderId;
	private boolean ack;
	private boolean myElection;
	private HeartbeatManager heartbeatMgr;

	/** @brief the number of votes this server can cast */
	private int votes = 1;

	public static ElectionManager getInstance(String id, int votes) {
		instance.compareAndSet(null, new ElectionManager(id, votes));
		return instance.get();
	}

	public static ElectionManager getInstance() {
		return instance.get();
	}

	/**
	 * initialize the manager for this server
	 * 
	 * @param nodeId
	 *            The server's (this) ID
	 */
	protected ElectionManager(String nodeId, int votes) {
		this.nodeId = nodeId;

		if (votes >= 0)
			this.votes = votes;

		heartbeatMgr = HeartbeatManager.getInstance();
		broadCastNewElection();
		declareElection();
	}
	
	/*
	 * Broadcast in network that a new election is coming
	 * - Shibai
	 */
	private void broadCastNewElection() {
		myElection = true;
		ack = false;
		leaderId = null;
		for (HeartbeatData hd : heartbeatMgr.outgoingHB.values()) {
			sendRequest(hd, VoteAction.ELECTION,"New election!!");
			// System.out.println("broadcasting: " + hd.getNodeId());
		}
	}
	
	/*
	 * send declaration to all higher ids
	 * - Shibai
	 */
	private void declareElection () {
		for (HeartbeatData hd : heartbeatMgr.outgoingHB.values()) {
			// System.out.println("debug: incoming id: " + hd.getNodeId());

			// System.out.println("debug: my id: " + nodeId);
			
			if (compIds(hd.getNodeId(), nodeId)) {
				sendRequest(hd, VoteAction.NOMINATE,"Nomination!");
			}
		}
	}
	
	/*
	 * send out request
	 * - Shibai
	 */
	private void sendRequest (HeartbeatData hd, eye.Comm.LeaderElection.VoteAction voteAction,String desc) {
		LeaderElection.Builder l = LeaderElection.newBuilder();
		l.setNodeId(nodeId);
		l.setBallotId("1");
		l.setVote(voteAction);
		l.setDesc(desc);

		Management.Builder m = Management.newBuilder();
		m.setElection(l.build());
		
		//Channel channel = null;
		//if (hd.isGood()) {
			//channel = hd.getChannel();

			System.out.println("22222222222222222");
			//ManagementQueue.enqueueResponse(m.build(), channel);
			//hd.channel.writeAndFlush(m.build());
		//}
			
		try {
			hd.channel.writeAndFlush(m.build());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/*
	 * compare node ids
	 * - Shibai
	 */
	private boolean compIds (String id1, String id2) {
		if (Integer.parseInt(id1) > Integer.parseInt(id2)) {
			return true;
		}
		return false;
	}
	
	/**
	 * - Shibai
	 * @param args
	 */
	public void processRequest(LeaderElection req) {
		System.out.println("!!!! get a leaderelection request!!!!!!");
		
		if (req == null)
			return;

		if (req.hasExpires()) {
			long ct = System.currentTimeMillis();
			if (ct > req.getExpires()) {
				// election is over
				return;
			}
		}

		if (req.getVote().getNumber() == VoteAction.ELECTION_VALUE) {
			// an election is declared!
			myElection = false;
			leaderId = null;
			ack = false;
			// stop receiving new jobs
			
		} else if (req.getVote().getNumber() == VoteAction.DECLAREVOID_VALUE) {
			// no one was elected, I am dropping into standby mode`
			// left void
		} else if (req.getVote().getNumber() == VoteAction.DECLAREWINNER_VALUE) {
			// some node declared themself the leader
			// set leader
			leaderId = req.getNodeId();
			myElection = false;
			ack = false;
			System.out.println("ok, new leader is: " + leaderId);
		} else if (req.getVote().getNumber() == VoteAction.ABSTAIN_VALUE) {
			// for some reason, I decline to vote
			// work as ack for now
			ack = true;
			
		} else if (req.getVote().getNumber() == VoteAction.NOMINATE_VALUE) {
			// send back acks if necessary 
			// send out request and set timeout 	
			int comparedToMe = req.getNodeId().compareTo(nodeId);
			if (comparedToMe == -1) {
				// Someone else has a higher priority, forward nomination
				// TODO forward
				// left void. Since we are using bully algorithm, would never receive nominations from higher ids
			} else if (comparedToMe == 1) {
				// I have a higher priority, nominate myself
				// TODO nominate myself
				if (!myElection) {
					myElection = true;
					sendAck(req);
					declareElection();
				}
			}
		} // else if receive acks, set flag to true
	}
	
	
	private void sendAck (LeaderElection req) {
		HeartbeatData hd = heartbeatMgr.incomingHB.get(req.getNodeId());
		sendRequest(hd,VoteAction.NOMINATE,"ACK"); // change nominate to ack
		
	}
	
	
	/*
	 * 
	 * - Shibai
	 * (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run () {
		while (true) {
			// check failures of leader
			while (leaderId != nodeId && leaderId != null) {
				try {
					Thread.sleep(2000);
					HeartbeatData hd = heartbeatMgr.incomingHB.get(leaderId);
					// if failures are detected, start a new election
					if (hd.getFailures() > 2) {
						broadCastNewElection();
						declareElection();
					}
				} catch (Exception e) {
					break;
				}
			}

			// during election, waiting for acks
			int failure = 0;
			while (leaderId == null && !ack) {
				try {
					Thread.sleep(3000);
					failure++;
					if (failure > 3) {
						// Broadcast: I am the new leader!
						leaderId = nodeId;
						for (HeartbeatData hd : heartbeatMgr.incomingHB.values()) {
							sendRequest(hd, VoteAction.DECLAREWINNER,"I am the new leader!!");
						}
						System.out.println(nodeId + ": ok, im the new leader");
						break;
					}
				} catch (Exception e) {
					e.printStackTrace();
					break;
				}
			}
		}
	}
}
