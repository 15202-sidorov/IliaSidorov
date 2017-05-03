#include <stdio.h>
#include <sys/msg.h>
#include <sys/ipc.h>
#include <sys/types.h>
#include <string.h>
#include <unistd.h>
#include <signal.h>

#define MESSAGE_SIZE 64

int msg_id = 0;

struct msgbuf {
	long mtype;
	char mtext[MESSAGE_SIZE];
};

int main(int argc, char **argv) {
	msg_id = msgget(getuid(),0);
	if (-1 == msg_id) {
		perror("Could not access queue");
		return 1;
	}

	struct msgbuf message_to_recieve;
	message_to_recieve.mtype = 1L;
	while (1) {
		if (-1 == msgrcv(msg_id,&message_to_recieve,MESSAGE_SIZE,1L,MSG_NOERROR)) {
			perror("Could not recieve message");
			msgctl(msg_id,IPC_RMID,NULL);
			return 1;
		}
		
		if (message_to_recieve.mtype == 0L) {
			break;
		}

		printf("Message recieved successfuly\n");
		printf("Content : %s",message_to_recieve.mtext);
		printf("Type : %ld\n",message_to_recieve.mtype);
	} 
	

	msgctl(msg_id,IPC_RMID,NULL);
	return 0;
}