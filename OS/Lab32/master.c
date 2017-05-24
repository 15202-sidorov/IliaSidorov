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

#define CHILD_NUMBER 2


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

int main(int argc, char **argv) {
	signal(SIGINT,quithandler);

	msgqid =  msgget(getuid(),IPC_CREAT | 0666);
	if (-1 == msgqid) {
		perror("Could not create msg queue");
		return 1;
	}

	struct msgbuf message = {0};
	int chars_read = 0;

	int counter = CHILD_NUMBER;
	while (0 != counter) {
		chars_read = msgrcv(msgqid,&message,MESSAGE_SIZE,0,MSG_NOERROR);
		if (END_OF_COMMUTE == message.mtype) {
			counter--;
		}
		else {
			printf("Message recieved from %ld\n",message.mtype);
			if (-1 == write(fileno(stdout),message.mtext,chars_read)){
				perror("Could not write to stdout");
				return 2;
			}
		}
	}
	printf("End of communication, closing queue...\n");
	if (-1 == msgctl(msgqid,IPC_RMID,NULL)) {
		perror("Could not delete queue");
		return 3;
	}

	return 0;
}