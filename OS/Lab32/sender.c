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


#define MESSAGE_SIZE 64
#define END_OF_COMMUTE 7L

struct msgbuf {
	long int mtype;
	char mtext[MESSAGE_SIZE];
};


int main(int argc, char **argv) {
	printf("I'm ON it\n");
	long COMMON_SEND = (long)(*argv[1] - '0');
	int msgqid = msgget(getuid(),0);
	if (-1 == msgqid) {
		perror("Child could not get msg queue");
		return 1;
	}

	struct  msgbuf message = {0};
	int readval = 0;
	while (1) {
		readval = read(fileno(stdin),message.mtext,MESSAGE_SIZE);
		if (-1 == readval) {
			perror("Could not read from stdin");
			return 2;
		}
		
		if (0 == readval) {
			message.mtext[0] = EOF;
			message.mtype = END_OF_COMMUTE;
			if (-1 == msgsnd(msgqid,&message,1,MSG_NOERROR)) {
				perror("Could not send end message to process");
				return 3;
			}
		}
		
		message.mtype = COMMON_SEND;
		if (-1 == msgsnd(msgqid,&message,readval,MSG_NOERROR)) {
			perror("Could not send common message");
			return 4;
		}
	}

	return 0;
}