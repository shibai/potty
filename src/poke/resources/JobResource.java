/*
 * copyright 2012, gash
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
package poke.resources;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import persistence.HibernateUtil;
import poke.bean.Course;
import poke.server.resources.Resource;
import poke.server.resources.ResourceUtil;
import eye.Comm.JobDesc;
import eye.Comm.JobDesc.JobCode;
import eye.Comm.JobStatus;
import eye.Comm.NameValueSet;
import eye.Comm.NameValueSet.NodeType;
import eye.Comm.Payload;
import eye.Comm.PokeStatus;
import eye.Comm.Request;

public class JobResource implements Resource {
	protected static Logger logger = LoggerFactory.getLogger("server");
	protected String nameSpace;
	protected String courseName1;

	public JobResource() {

	}

	@Override
	public Request process(Request request) {
		Request.Builder rb = Request.newBuilder();
		rb.setHeader(ResourceUtil.buildHeaderFrom(request.getHeader(),
				PokeStatus.SUCCESS, null));
		logger.info("poke"
				+ request.getBody().getJobOp().getData().getNameSpace());
		nameSpace = request.getBody().getJobOp().getData().getNameSpace();
		courseName1 = request.getBody().getJobOp().getData().getOptions()
				.getValue();
		System.out.println("Abinaya test namespace" + nameSpace);

		if (nameSpace != null && nameSpace.equals("listcourses")) {
			System.out.println("Hibernate");
			Session session = HibernateUtil.getSessionFactory().openSession();
			session.beginTransaction();
			Query query = session.createQuery("from Course");
			List<Course> listCourse = query.list();
			System.out.println("Abinaya Sampath " + listCourse.toString());
			session.getTransaction().commit();
			List<NameValueSet> listNameValueSet = new ArrayList<NameValueSet>();

			for (Course course : listCourse) {
				listNameValueSet.add(buildNameValueSet(course));
			}
			NameValueSet.Builder nameValueSet = NameValueSet.newBuilder();
			nameValueSet.setName("coursename");
			nameValueSet.setNodeType(NodeType.NODE);
			nameValueSet.addAllNode(listNameValueSet);
			nameValueSet.build();

			Payload.Builder payLoad = Payload.newBuilder();
			JobStatus.Builder jobStatus = JobStatus.newBuilder();
			JobDesc.Builder jobDesc = JobDesc.newBuilder();
			jobStatus.setJobId(request.getBody().getJobOp().getData()
					.getJobId());
			jobDesc.setNameSpace("listcourses");
			jobDesc.setOwnerId(request.getBody().getJobOp().getData()
					.getOwnerId());
			jobDesc.setJobId(request.getBody().getJobOp().getData().getJobId());
			jobDesc.setStatus(JobCode.JOBRECEIVED);
			jobDesc.setOptions(nameValueSet);
			jobDesc.build();
			System.out
					.println("Abinaya namevalueset" + nameValueSet.toString());
			jobStatus.setJobState(JobCode.JOBRECEIVED);
			jobStatus.setStatus(PokeStatus.SUCCESS);
			jobStatus.addData(jobDesc);
			payLoad.setJobStatus(jobStatus);
			payLoad.build();
			rb.setBody(payLoad);

		}

		if (nameSpace != null && nameSpace.equals("getdescription")) {
			System.out.println("Abinaya getting desc");
			Session session = HibernateUtil.getSessionFactory().openSession();
			session.beginTransaction();
			Query query = session
					.createQuery("from Course where courseName=:courseName1");
			query.setParameter("courseName1", request.getBody().getJobOp()
					.getData().getOptions().getValue());
			List<Course> listCourse = query.list();
			Course course = (Course) listCourse.get(0);
			String courseDescription = course.getCourseDesc();
			session.getTransaction().commit();
			Payload.Builder payLoad = Payload.newBuilder();
			JobStatus.Builder jobStatus = JobStatus.newBuilder();
			JobDesc.Builder jobDesc = JobDesc.newBuilder();
			NameValueSet.Builder nameValueSet = NameValueSet.newBuilder();
			nameValueSet.setNodeType(NodeType.VALUE);
			nameValueSet.setName("coursedescription");
			nameValueSet.setValue(courseDescription);
			nameValueSet.build();
			jobStatus.setJobId(request.getBody().getJobOp().getData()
					.getJobId());
			jobDesc.setNameSpace("listcourses");
			jobDesc.setOwnerId(request.getBody().getJobOp().getData()
					.getOwnerId());
			jobDesc.setJobId(request.getBody().getJobOp().getData().getJobId());
			jobDesc.setStatus(JobCode.JOBRECEIVED);
			jobDesc.setOptions(nameValueSet);
			jobDesc.build();
			jobStatus.setJobState(JobCode.JOBRECEIVED);
			jobStatus.setStatus(PokeStatus.SUCCESS);
			jobStatus.addData(jobDesc);
			payLoad.setJobStatus(jobStatus);
			payLoad.build();
			rb.setBody(payLoad);
			

		}

		Request reply = rb.build();
		return reply;
	}

	NameValueSet buildNameValueSet(Course course) {
		NameValueSet.Builder nameValueSet = NameValueSet.newBuilder();
		nameValueSet.setName("coursename");
		nameValueSet.setValue(course.getCourseName());
		nameValueSet.setNodeType(NodeType.VALUE);
		return nameValueSet.build();
	}
}
