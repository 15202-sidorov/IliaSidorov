#include <stdio.h>
#include <string.h>
#include <sys/msg.h>
#include <sys/ipc.h>
#include <unistd.h>

#define CHILD_NUMBER 4
#define RECIEVER_PATH "/home/ilia/IliaSidorov/OS/Lab31/reciever.out"
#define CHUNCK_SIZE 64
#define END_TYPE 2L
#define COMMON_TYPE 1L
#define FINAL_TYPE 3L

struct msgbuf {
	long mtype;
	char mtext[CHUNCK_SIZE];
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
				perror("Chinld could not be exceuted")
				return 4;
			}
		}
	}

	char text[CHUNCK_SIZE] = {0};
	struct msgbuf message = {0};

	while (1) {
		
		fgets(text,CHUNCK_SIZE,stdin)
		if (text == "EOF") {
			message.mtype = END_TYPE;
		}
		else {
			message.mtype = COMMON_TYPE;
		}

		message.mtext = text;
		
		for (int i = 0; i < CHILD_NUMBER; i++) {
			if (-1 == msgsnd(msgqid,&message,CHUNCK_SIZE,NO_ERROR)) {
				perror("Could not send message");
				return 2;
			}
		}

		if (END_TYPE == message.mtype) {
			int counter = CHILD_NUMBER;
			while (counter) {
				if (-1 == msgrcv(msgqid,&message,CHUNCK_SIZE,-FINAL_TYPE,NO_ERROR)) {
					perror("Parent could not get the final answer");
					return 3;
				}
				else {
					counter--;
				}
			}
			break;
		}

	}







	return 0;
}