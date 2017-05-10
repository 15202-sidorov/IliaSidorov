#include <stdio.h>
#include <string.h>
#include <sys/msg.h>
#include <sys/ipc.h>
#include <unistd.h>

#define CHILD_NUMBER 4
#define RECIEVER_PATH "/home/ilia/IliaSidorov/OS/Lab31/reciever.out"
#define MESSAGE_SIZE 64
#define END_TYPE 3L
#define COMMON_SEND 1L
#define FINAL_TYPE 4L

struct msgbuf {
	long mtype;
	char mtext[MESSAGE_SIZE];
}

int main (int argc, char **argv) {
	//all pids of recievers

	int msgqid =  msgget(getuid(),IPC_CREAT | 0666);
	if (-1 == msgqid) {
		perror("Could not create msg queue");
		return 1;
	}

	pid_t child_pids[CHILD_NUMBER] = {0};
	pid_t child_reciever_pid = 0;
	for (int i = 0 ; i < CHILD_NUMBER; i++){
		child_reciever_pid = fork();
		child_pids[i] = child_reciever_pid;
		if (0 == child_reciever_pid) {
			//running reciever
			if (-1 == execv(RECIEVER_PATH,argv)) {
				perror("Child cannot be exceuted")
				return 4;
			}
		}
	}

	char text[MESSAGE_SIZE] = {0};
	struct msgbuf message = {0};

	while (1) {
		
		fgets(text,MESSAGE_SIZE,stdin)
		if (*text == EOF) {
			message.mtext = "";
			message.mtype = END_TYPE;
			for (int i = 0; i < CHILD_NUMBER; i++) {
				if (-1 == msgsnd(msgqid,&message,MESSAGE_SIZE,MSG_NOERROR)) {
					perror("Could not send end message to process");
					return 1;
				}
			}

			for (int i = 0; i < CHILD_NUMBER; i++) {
				if (-1 == msgrcv(msgqid,&message,MESSAGE_SIZE,FINAL_TYPE,MSG_NOERROR)) {
					perror("Could not recieve final message");
					return 2;
				}
			}
			
			printf("Connection closed, removing queue\n")
			if (-1 == msgctl(msgqid,IPC_RMID,NULL)) {
				perror("Could not delete queue");
				return 3;
			}
			return 0;
		}
		else {
			message.mtype = COMMON_SEND;
			for (int i = 0; i < CHILD_NUMBER; i++) {
				if (-1 == msgsnd(msgqid,&message,MESSAGE_SIZE,MSG_NOERROR)) {
					perror("Could not send common message");
					return 5;
				}
			}
		}

	}







	return 0;
}