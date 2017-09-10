#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>

void *start_routine(void * attr) {
	printf("Hello , I'm new thread\n");
	return 0;
}


int main( int argc, char ** argv) {
	pthread_t thread = 0;
	printf("Parent thread returning ... \n");
	if (0 != pthread_create(&thread, NULL, start_routine, NULL)) {
		perror("Could not create thread\n");
		return 1;
	}

	return 0;
}