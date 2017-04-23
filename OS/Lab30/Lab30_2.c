#include <stdio.h>
#include <sys/msg.h>
#include <sys/ipc.h>
#include <sys/types.h>
#include <string.h>
#include <unistd.h>

#define MESSAGE_SIZE 64

struct msgbuf {
	long mtype;
	char mtext[MESSAGE_SIZE];
};

int main(int argc, char **argv) {
	int msg_id = msgget(getuid(),0);
	if (-1 == msg_id) {
		perror("Could not access queue");
		return 1;
	}

	struct msgbuf message_to_recieve;
	if (-1 == msgrcv(msg_id,&message_to_recieve,MESSAGE_SIZE,1L,IPC_NOWAIT)) {
		perror("Could not recieve message");
		return 1;
	}

	printf("Message recieved successfuly\n");
	printf("%s",message_to_recieve.mtext);
	printf("%ld\n",message_to_recieve.mtype);

	msgctl(msg_id,IPC_RMID,NULL);
}