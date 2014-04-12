import comm_pb2
import sys
import google
import socket
import struct
import time

HOST= 'localhost'
PORT=5570
req=comm_pb2.Request()
header = req.header
body = req.body
global s
global msg
#global msg_buf
#res=comm_pb2.Response()


def makeHeader(routingId):	
    header.originator = "client"
    #current time in milliseconds
    millis = int(round(time.time() * 1000))
    header.time = millis
    header.routing_id = routingId;
    header.tag = "List Courses"
    return header

# This function fills in a Person message based on user input.
def sendToChannel(req):
	for res in socket.getaddrinfo(HOST, PORT, socket.AF_UNSPEC, socket.SOCK_STREAM):
		af, socktype, proto, canonname, sa = res
		try:
			global s
        	    	s = socket.socket(af, socktype, proto)
		except OSError,msg:
			s = None
        	    	continue
		try:
			s.connect(sa)
		except OSError,msg:
			s.close()
			s = None
			continue
			break
	if s is None:
		print('could not open socket')
		sys.exit(1)
    
	sstr = req.SerializeToString()
	packed_len = struct.pack('>L', len(sstr))
	s.sendall(packed_len + sstr)

def receiveResponse():
	while 1:
		msg=''
		len_buf = readMessage(4)
		msg_len = struct.unpack('>L', len_buf)[0]
		#print msg_len
		msg_buf = readMessage(msg_len)
		#res.ParseFromString(msg_buf)
		#print res.body.job_status.data
#		print res.body.job_op.data.options.value
		#return msg_buf
		print msg_buf
		#print "Received"
		return
	
def readMessage(n):
	buf = ''
	while n>0 :
		data = s.recv(n)
		if data == '':
			#raise RuntimeError('unexpected connection close')
			print "Data not found"
		buf+=data
		n-=len(data)
		return buf	

def addUser():
	print "Add me please"

def listCourses():
	header=makeHeader(comm_pb2.Header.JOBS)
	body.job_op.data.name_space="listcourses"
 	body.job_op.data.owner_id=21
	body.job_op.data.job_id="Abitest"
	body.job_op.data.status=4
	body.job_op.data.options.node_type=2
	body.job_op.action=1
	sendToChannel(req)
	receiveResponse()
	return
	#print "Received"

def getDescription():
	print "Description hurray"
	#job_op.data.options.value=raw_input("Enter the course name:")
	header=makeHeader(comm_pb2.Header.JOBS)
	body.job_op.data.name_space="getdescription"
 	body.job_op.data.owner_id=21
	body.job_op.data.job_id="Abilist"
	body.job_op.data.status=4
	body.job_op.data.options.node_type=2
	body.job_op.data.options.name="coursename"
	body.job_op.data.options.value=raw_input("Enter the course name:")
	body.job_op.action=4
	sendToChannel(req)
	receiveResponse()
	return
	

def listMoreCourses():
	print "listed MORE"

print "Select an option"
print "1. New User??? Sign Up"
print "2. List the Courses"
print "3. Get the description of a selected course"
print "4. Want More Courses!!!Get more Courses"
print "5.Exit"

while True:

    OPTION = int(raw_input("CHOOSE AN OPTION: ")) 

    if OPTION == 1: 
        print "SIGN UP"
        addUser()

    elif OPTION == 2:
        print "THE FOLLOWING COURSES ARE OFFERED"
        listCourses()
        continue

    elif OPTION == 3:
        print "GET THE DESCRIPTION "
        getDescription()

    elif OPTION == 4:
        print "ADDITIONAL COURSES ARE LISTED BELOW"
        print listMoreCourses()
        
    elif OPTION == 5:
    	exit()
    	
        
        
