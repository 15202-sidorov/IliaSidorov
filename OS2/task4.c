#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>

#include <unistd.h>
#include <math.h>
#include <error.h>

void user_function() {
	return;
}

void *start_routine( void * attr ) {
	user_function();
	return 0;
}

int main( int argc, char **argv ) {
	pthread_t thread;
	if (0 != pthread_create(&thread, NULL, start_routine, NULL)) {
		perror("Could not create new thread\n");
		return 1;
	}
	
	sleep(1);
	int err = 0;
	if (0 != (err = pthread_cancel(thread))) {
		perror("Could not cancel thread\n");
		printf("%d\n", ESRCH );
		return 1;
	}



	return 0;
}