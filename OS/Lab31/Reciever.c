#include <stdio.h>
#include <string.h>
#include <sys/msg.h>
#include <sys/ipc.h>

#define MESSAGE_SIZE 64
#define END_TYPE 3L
#define COMMON_RECIEVE 1L
#define FINAL_TYPE 4L


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
	int chars_read = 0;
	while (1) {
		chars_read = msgrcv(msg_id,&message,MESSAGE_SIZE,COMMON_RECIEVE,MSG_NOERROR);
		if (-1 == chars_read) {
			perror("Could not recieve message");
			return 1;
		}
		printf("")
	}
	
}