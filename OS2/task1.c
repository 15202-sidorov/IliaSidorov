#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>

#define LINES_AMOUNT 10

int a;

//the body of a new routine ( will be passed as a pointer)
void *new_start_routine( void * arg ) {
	a = 6;
	for (int i = 0; i < LINES_AMOUNT; i++) {
		printf("New routine printf\n");
	}

	printf("%d\n",a);
	return 0;
}

int main( int argc, char **argv ) {
	for (int i = 0; i < LINES_AMOUNT; i++) {
		printf("Parent routine printf\n");
	}
	a = 5;

	//pointer to the identifer of the routine, after successful exit will
	//contains the identifer of the routine
	pthread_t thread;

	if (0 != pthread_create(&thread, NULL, new_start_routine, NULL)) {
		perror("Could not create routine\n");
		return 1;
	}


	if (0 != pthread_join(thread,NULL)) {
		perror("Could not join the routine\n");
		return 1;
	}
	
	printf("After all : %d\n",a);

	return 0;
}