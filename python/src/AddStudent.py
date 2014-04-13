#! /Library/Frameworks/Python.framework/Versions/2.7/bin/python

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
res=comm_pb2.Response()

 	body.job_op.data.owner_id=owner_id
	body.job_op.data.job_id=job_id
	
def makeHeader(routingId):	
    header.originator = "client"
    #current time in milliseconds
    millis = int(round(time.time() * 1000))
    header.time = millis
    header.routing_id = routingId;
    header.tag = "Add a new student test"
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
	print "success"

def receiveResponse():
	while 1:
		len_buf = readMessage(4)
		msg_len = struct.unpack('>L', len_buf)[0]
		print msg_len
		msg_buf = readMessage(msg_len)
		print msg_buf 
		print('Received')  	
	
def readMessage(n):
	buf = ''
	while n>0 :
		data = s.recv(n)
		if data == '':
			#raise RuntimeError('unexpected connection close')

		buf+=data
		n-=len(data)
		return buf	

def PromptForAddress(person):
  header=makeHeader(comm_pb2.Header.PERSON)
  person.email = raw_input("Enter email address id: ")
  person.password = raw_input("Enter password: ")
  person.fname= raw_input("Enter first name: ")
  person.lname = raw_input("Enter last name: ")
  body.per.email=person.email
  body.per.password=person.password
  body.per.fname=person.fname
  body.per.lname=person.lname

# Main procedure:  Reads the entire address book from a file,
#   adds one person based on user input, then writes it back out to the same
#   file.
if len(sys.argv) != 2:
  print "Usage:", sys.argv[0], "ADDRESS_BOOK_FILE"
  sys.exit(-1)

student = comm_pb2.Student()

# Read the existing address book.
try:
  f = open(sys.argv[1], "rb")
  student.ParseFromString(f.read())
  f.close()
except IOError:
  print sys.argv[1] + ": Could not open file.  Creating a new one."

# Add an address.
PromptForAddress(student.person.add())

# Write the new address book back to disk.
f = open(sys.argv[1], "wb")
f.write(student.SerializeToString())
f.close()
sendToChannel(req)

#receive

receiveResponse()