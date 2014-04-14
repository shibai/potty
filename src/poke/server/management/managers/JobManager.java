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

import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eye.Comm.JobBid;
import eye.Comm.JobProposal;
import eye.Comm.LeaderElection.VoteAction;

/**
 * The job manager class is used by the system to assess and vote on a job. This
 * is used to ensure leveling of the servers take into account the diversity of
 * the network.
 * 
 * @author gash
 * 
 */
public class JobManager {
	protected static Logger logger = LoggerFactory.getLogger("management");
	protected static AtomicReference<JobManager> instance = new AtomicReference<JobManager>();

	private String nodeId;

	public static JobManager getInstance(String id) {
		instance.compareAndSet(null, new JobManager(id));
		return instance.get();
	}

	public static JobManager getInstance() {
		return instance.get();
	}

	public JobManager(String nodeId) {
		this.nodeId = nodeId;
	}

	/**
	 * a new job proposal has been sent out that I need to evaluate if I can run
	 * it
	 * 
	 * @param req
	 *            The proposal
	 */
	public void processRequest(JobProposal req) {
		// if leader receives this, it forwards it to other nodes
		// otherwise it generates a random msg 
		// if leader is not present, then block
		if (ElectionManager.getInstance().leaderDown()) {
			// block
		}
		if (ElectionManager.getInstance().isLeader()) {
			for (HeartbeatData hd :  HeartbeatManager.getInstance().incomingHB.values()) {
				// forward req 
				
				
			}
			
			
		}else {
			// this receiving node should evaluate job and the load balance of itself, then make a decision
			// at the time being, we do nothing but just rely yes or no
			Random random = new Random(); 
			int bid = random.nextInt(2); 
			JobBid.Builder jbid = JobBid.newBuilder();
			jbid.setJobId(req.getJobId());
			jbid.setOwnerId(req.getOwnerId()); //This is the Cluster ID
			jbid.setNameSpace(req.getNameSpace());
			jbid.setBid(bid);
			
			
		}
		
		
	}

	/**
	 * a job bid for my job
	 * 
	 * @param req
	 *            The bid
	 */
	public void processRequest(JobBid req) {
		
	}
}
