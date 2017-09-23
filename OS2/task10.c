#define _GNU_SOURCE 
#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <unistd.h>


#define LINES_AMOUNT 1000

pthread_mutex_t mutex;
pthread_mutex_t init_mutex;

//should check out so called not deadblocking type of mutexes
//also atributes and so forth about mutexes

//the body of a new routine ( will be passed as a pointer)
void *new_start_routine( void * arg ) {

	pthread_mutex_lock(&init_mutex);

	for (int i = 0; i < LINES_AMOUNT; i++) {

		pthread_mutex_lock(&mutex);
		printf("New routine printf\n");
		fflush(stdout);
		pthread_mutex_unlock(&mutex);

	}

	return 0;
}


int main( int argc, char **argv ) {
	
	pthread_t thread;
	pthread_mutexattr_t mutex_attr;

	if (0 != pthread_mutexattr_init(&mutex_attr)) {
		perror("Could not init mutex attributes\n");
		return 1;
	}
	
	if (0 != pthread_mutexattr_settype(&mutex_attr, PTHREAD_MUTEX_ERRORCHECK)) {
		perror("Could not set mutex attributes type\n");
		return 1;
	}
	
	if (0 != pthread_mutex_init(&mutex,&mutex_attr)) {
		perror("Could not create mutex\n");
		return 1;
	}

	if (0 != pthread_mutex_init(&init_mutex, NULL)) {
		perror("Could not create init mutex\n");
		return 1;
	}

	pthread_mutex_lock(&init_mutex);
	if (0 != pthread_create(&thread, NULL, new_start_routine, NULL)) {
		perror("Could not create routine\n");
		return 1;
	}


	sleep(1);
	pthread_mutex_unlock(&init_mutex);
	for (int i = 0; i < LINES_AMOUNT; i++) {
	
		pthread_mutex_lock(&mutex);
		printf("Parent routine printf\n");
		fflush(stdout);
		pthread_mutex_unlock(&mutex);
		
	}

	if (0 != pthread_join(thread,NULL)) {
		perror("Could not join the routine\n");
		return 1;
	}

	if (0 != pthread_mutexattr_destroy(&mutex_attr)) {
		perror("Could not destroy mutex attributes\n");
		return 1;
	}
	

	return 0;
}