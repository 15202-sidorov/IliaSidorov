#include <stdio.h>
#include <sys/msg.h>
#include <sys/ipc.h>
#include <sys/types.h>
#include <string.h>
#include <getopt.h>
#include <unistd.h>

#define MESSAGE_SIZE 64

struct msgbuf {
	long mtype;
	char mtext[MESSAGE_SIZE];
};

int main(int argc, char **argv) {
	int msg_id = msgget(getuid(),IPC_CREAT | 0666);
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

		if (message_to_send.mtext == "exit") {

		}
		printf("Message is added to the queue successfuly\n");
	}

	return 0;
}