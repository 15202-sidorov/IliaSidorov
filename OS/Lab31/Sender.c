#include <unistd.h>
#include <fcntl.h>
#include <stdlib.h>
#include <stdio.h>
#include <ctype.h>
#include <sys/msg.h>
#include <sys/ipc.h>
#include <sys/types.h>
#include <string.h>
#include <getopt.h>
#include <unistd.h>
#include <signal.h>

#define CHILD_NUMBER 2
#define MESSAGE_SIZE 64
#define COMMON_SEND 1L

#define RECIEVER_END_OF_COMMUTE 7L

struct msgbuf {
	long int mtype;
	char mtext[MESSAGE_SIZE];
};

int msgqid = 0;

void quithandler(int sig) {
	if (-1 == msgctl(msgqid,IPC_RMID,NULL)) {
		perror("Could not delete queue");
		exit(1);
	}
	exit(0);
}

int main (int argc, char **argv) {
	//all pids of recievers
	signal(SIGINT,quithandler);
	msgqid =  msgget(getuid(),IPC_CREAT | 0666);
	if (-1 == msgqid) {
		perror("Could not create msg queue");
		return 1;
	}

	struct msgbuf message = {0};
	message.mtype = COMMON_SEND;
	int readval = 0;
	while (1) {
		readval = read(fileno(stdin),message.mtext,MESSAGE_SIZE);
		if (-1 == readval) {
			perror("Sender could not read from stdin");
			if (-1 == msgctl(msgqid,IPC_RMID,NULL)) {
				perror("Could not delete queue");
			}
			return 1;
		}

		if (0 == readval) {
			message.mtext[0] = EOF;
			for (int i = 0; i < CHILD_NUMBER; i++) {
				message.mtype = COMMON_SEND + i;
				if (-1 == msgsnd(msgqid,&message,MESSAGE_SIZE,MSG_NOERROR)) {
					perror("Could not send end message to process");
					return 1;
				}
			}

			//waiting for all childs to send back end of communication 
			for (int i = 0; i < CHILD_NUMBER; i++) {
				if (-1 == msgrcv(msgqid,&message,MESSAGE_SIZE,RECIEVER_END_OF_COMMUTE,MSG_NOERROR)) {
					perror("Could not recieve final message");
					return 2;
				}
			}
			
			printf("Connection closed, removing queue\n");
			if (-1 == msgctl(msgqid,IPC_RMID,NULL)) {
				perror("Could not delete queue");
				return 3;
			}
			return 0;
		}
		else {
			//sending common message
			for (int i = 0; i < CHILD_NUMBER; i++) {
				message.mtype = COMMON_SEND + i;
				if (-1 == msgsnd(msgqid,&message,readval,MSG_NOERROR)) {
					perror("Could not send common message");
					return 5;
				}
			}
		}

	}

	return 0;
}