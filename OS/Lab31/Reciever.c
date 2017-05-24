
#include <stdio.h>
#include <stdlib.h>
#include <sys/msg.h>
#include <sys/ipc.h>
#include <sys/types.h>
#include <string.h>
#include <getopt.h>
#include <unistd.h>
#include <signal.h>

#define MESSAGE_SIZE 64

#define RECIEVER_END_OF_COMMUTE 7L

struct msgbuf {
	long int mtype;
	char mtext[MESSAGE_SIZE];
};

int main (int argc, char **argv) {
	printf("I'm ON it\n");
	long COMMON_SEND = (long)(*argv[1] - '0');
	printf("hey there\n");
	int msg_id = msgget(getuid(),0);
	if (-1 == msg_id) {
		perror("Child could not get msg queue");
		return 1;
	}
	
	struct  msgbuf message = {0};
	int chars_read = 0;
	while (1) {
		chars_read = msgrcv(msg_id,&message,MESSAGE_SIZE,COMMON_SEND,MSG_NOERROR);
		if (-1 == chars_read) {
			perror("Could not recieve message");
			return 1;
		}
		
		if (EOF == message.mtext[0]) {
			printf("End of communication is caught\n");
			message.mtype = RECIEVER_END_OF_COMMUTE;
			if (-1 == msgsnd(msg_id,&message,MESSAGE_SIZE,MSG_NOERROR)) {
				perror("Could not send final message");
				return 2;
			}
			return 0;
		}
		else {
			if (-1 == write(fileno(stdout),message.mtext,chars_read)){
				perror("Could not write to stdout");
				return 3;
			}
		}
	}
	return 0;
	
}