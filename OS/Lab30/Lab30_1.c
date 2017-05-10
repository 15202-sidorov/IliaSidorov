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

struct msgbuf {
	long mtype;
	char mtext[MESSAGE_SIZE];
};

int msg_id = 0;

void quithandler(int sig) {
	if (-1 == msgctl(msg_id,IPC_RMID,NULL)) {
		perror("Could not delete queue");
		exit(1);
	}
	exit(0);
}

int main(int argc, char **argv) {
	signal(SIGINT,quithandler);
	msg_id = msgget(getuid(),IPC_CREAT | 0666);
	if (-1 == msg_id) {
		perror("Could not create message queue");
		return 1;
	}

	struct msgbuf message_to_send = { 1L, "" };
	while (1) {
		fgets(message_to_send.mtext,MESSAGE_SIZE,stdin);
		if (-1 == msgsnd(msg_id,&message_to_send,MESSAGE_SIZE,MSG_NOERROR)) {
			perror("Could not send message");
			return 1;
		}

	}

	return 0;
}