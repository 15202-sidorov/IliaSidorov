#include <stdio.h>
#include <string.h>
#include <sys/msg.h>
#include <sys/ipc.h>

#define CHUNCK_SIZE 64

#define END_TYPE 2L
#define COMMON_TYPE 1L
#define FINAL_TYPE 3L
#define WAIT_TIME 3


struct msgbuf {
	long mtype;
	char mtext[CHUNCK_SIZE];
}

int main (int argc, char **argv) {

	int msg_id = msgget(getuid(),0);
	if (-1 == msg_id) {
		perror("Child could not get msg queue");
		return 1;
	}
	
	struct  msgbuf message = {0};
	if (-1 == msgrcv(msg_id,&message,CHUNCK_SIZE,0,NO_ERROR)) {
		perror("Child could not recieve message");
		return 2;
	}
	else {
		if (COMMON_TYPE == message.mtype) {
			printf("Recieved message %s\n",message.mtext);
		}	
		else if (END_TYPE == message.mtype) {
			sleep(WAIT_TIME);
			message.mtext = "";
			message.mtype = FINAL_TYPE;
			if (-1 == msgsnd(msg_id,&message,CHUNCK_SIZE,NO_ERROR)) {
				perror("Could not send final msg");
				return 4;
			}
		}
		else {
			fprintf(stderr, "Unknown message recieved\n");
			return 3;
		}
	}
}