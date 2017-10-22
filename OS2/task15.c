#define _GNU_SOURCE 
#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <unistd.h>
#include <semaphore.h>
#include <sys/mman.h>
#include <sys/stat.h>
#include <fcntl.h>

#define LINES_AMOUNT 10

//-lrt!!

///Именнованные семафоры!!!!

sem_t *shared_mem;

void printPARENT() {
	for (int i = 0; i < LINES_AMOUNT; i++) {
		sem_wait(shared_mem + 1);
		printf("PARENT\n");
		sem_post(shared_mem);
	}

	return;
}

void printCHILD() {
	for (int i = 0; i < LINES_AMOUNT; i++) {
		sem_wait(shared_mem);
		printf("CHILD\n");
		sem_post(shared_mem + 1);
	}

	return;
}

int main( int argc, char **argv ) {
	int fd = shm_open("/mysemaphore", O_CREAT | O_RDWR, 0777);
	if (-1 == fd) {
		perror("Could not open shared memory object\n");
		return 1;
	}

	shared_mem = (sem_t *)mmap(NULL, 2 * sizeof(sem_t), PROT_NONE, MAP_SHARED, fd, 0); 
	if (MAP_FAILED == shared_mem) {
		perror("Could not map shared memory\n");
		return 1;
	}

	close(fd);

	//initilizing PROCESS SHARED semaphores
	if (0 != sem_init(shared_mem, 1, 0)) {
		perror("Could not initialize semaphore\n");
		return 1;
	}
	printf("Inited\n");
	if (0 != sem_init(shared_mem + 1, 1, 1)) {
		perror("Could not initialize semaphore\n");
		return 1;
	}

	printf("Inited\n");

	pid_t child_pid = fork();

	if (-1 == child_pid) {
		perror("Could not fork the process\n");
		return 1;
	}

	if (0 == child_pid) {
		printPARENT();
		if (0 != sem_destroy(shared_mem)) {
			perror("Could not destroy semaphore\n");
			return 1;
		}

		if (0 != sem_destroy(shared_mem + 1)) {
			perror("Could not destroy semaphore\n");
			return 1;
		}

		if (0 != munmap(shared_mem, 2 * sizeof(sem_t))) {
			perror("Could not unmap semaphore's shared memory\n");
			return 1;
		}
	
	}
	else {
		printCHILD();
	}

	return 0;
}