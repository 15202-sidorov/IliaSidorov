#define _GNU_SOURCE
#include <stdio.h>
#include <stdlib.h>
#include <signal.h>
#include <sys/types.h>
#include <sys/ipc.h>
#include <sys/sem.h>
#include <unistd.h>
#include <sys/shm.h>

#define SHM_SIZE 64
#define MESSAGE_COUNT 3

int semid = 0;
int shmid = 0;

void quithandler(int sig) {
	if (-1 == semctl(semid,0,IPC_RMID,0)) {
		perror("Could not delete produce semaphore");
		exit(1);
	}

	if (-1 == shmctl(shmid, IPC_RMID, 0)) {
		perror("Could not delete shared memory");
		exit(1);
	}
	exit(0);
}


int main(int argc, char **argv) {
	signal(SIGINT,quithandler);
	char *message;
	struct sembuf inprocess = {1, -1, 0};
	struct sembuf produced =  {0, 1, 0};

	semid = semget(getuid(), 2, IPC_CREAT | 0666);
	if (-1 == semid) {
		perror("Could not create semaphors");
		return 1;
	}

	shmid = shmget(getuid(),SHM_SIZE, IPC_CREAT | 0666);
	if (-1 == shmid) {
		perror("Could not create shared segment");
		return 2;
	}

	message = (char *)shmat(shmid,0,0);

	for (int i = 0; i < MESSAGE_COUNT; i++) {
		if (-1 == semop(semid,&inprocess,1)) {
			perror("Could not set inprocess semaphore");
			return 3;
		}
		printf("Produsing message\n");
		sprintf(message,"message #%d is produced\n",i);

		if (-1 == semop(semid,&produced,1)) {
			perror("Could not set produced semaphore");
			return 4;
		}
	}

	shmdt(message);
	semctl(semid, 0, IPC_RMID, 0);
	shmctl(shmid, IPC_RMID, 0);

}