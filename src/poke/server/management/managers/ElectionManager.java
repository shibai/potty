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
	
	//
	private String leaderId;
	private boolean ack;
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
		
		// start a new election
		// send declaration to all higher ids
		heartbeatMgr = HeartbeatManager.getInstance();
		for (HeartbeatData hd : heartbeatMgr.incomingHB.values()) {
			LeaderElection.Builder l = LeaderElection.newBuilder();
			//l.setBallotId();
			l.setNodeId(nodeId);
			l.setBallotId("1");
			l.setVote(VoteAction.ELECTION);
			l.setDesc("New election!");
			
			Management.Builder m = Management.newBuilder();
			m.setElection(l.build());
			
			Channel channel = null;
			if (hd.isGood()) {
				channel = hd.getChannel();
			}
			
			
		}
		
	}

	/**
	 * @param args
	 */
	public void processRequest(LeaderElection req) {
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
			// set leader to null
			
		} else if (req.getVote().getNumber() == VoteAction.DECLAREVOID_VALUE) {
			// no one was elected, I am dropping into standby mode`
			// left void
		} else if (req.getVote().getNumber() == VoteAction.DECLAREWINNER_VALUE) {
			// some node declared themself the leader
			// set leader
		} else if (req.getVote().getNumber() == VoteAction.ABSTAIN_VALUE) {
			// for some reason, I decline to vote
			// left void
		} else if (req.getVote().getNumber() == VoteAction.NOMINATE_VALUE) {
			
			// send back acks if necessary 
			// send out request and set timeout 
			
			
			
			
			int comparedToMe = req.getNodeId().compareTo(nodeId);
			if (comparedToMe == -1) {
				// Someone else has a higher priority, forward nomination
				// TODO forward
			} else if (comparedToMe == 1) {
				// I have a higher priority, nominate myself
				// TODO nominate myself
			}
		} // else if receive acks, set flag to true
	}
	
	@Override
	public void run () {
		while (true) {
			// check failures of leader
			while (leaderId != nodeId && leaderId != null) {
				try {
					Thread.sleep(5000);
					
					// if failures are detected, start a new election

					// } catch (InterruptedException ie) {
					// break;
				} catch (Exception e) {
					break;
				}
			}

			// during election, waiting for acks
			while (leaderId == null && !ack) {
				try {
					// timer's running
					// check for ack flag
				} catch (Exception e) {
					e.printStackTrace();
					break;
				}
			}
		}
	}
}
