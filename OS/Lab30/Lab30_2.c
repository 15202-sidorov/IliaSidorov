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
	int chars_read = 0;
	while (1) {
		//if OL , then first message is read
		//if greater then 0, then first message with that type is recieved
		chars_read = msgrcv(msg_id,&message_to_recieve,MESSAGE_SIZE,1L,MSG_NOERROR);
		if (-1 == chars_read) {
			perror("Could not recieve message");
			return 1;
		}
	
		printf("Message : %sType: %ld\nChar Count: %d\n",
			message_to_recieve.mtext,
			message_to_recieve.mtype,
			chars_read);
		
	} 
	
	return 0;
}