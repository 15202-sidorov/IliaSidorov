#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>

#define THREAD_NUMBER 4
#define ITARATIONS_NUMBER 1000
#define ITARATIONS_PER_THREAD ITARATIONS_NUMBER / THREAD_NUMBER

void *count_sum( void * arg ) {
	printf("New thread is started\n");
	double *result = (double *)calloc(1,sizeof(double));
	int thread_id = *(int *)arg;
	int start = ITARATIONS_PER_THREAD * thread_id;
	int end = start + ITARATIONS_NUMBER;
	for (int i = start; i < end; i++) {
		
		result[0] += 1.0/(i*4.0 + 1.0);
		result[0] -= 1.0/(i*4.0 + 3.0);
	}

	pthread_exit(result);
}


int main( int argc, char **argv ) {
	double pi = 0;
	pthread_t thread[THREAD_NUMBER];
	int thread_ids[THREAD_NUMBER] = {0};
	for (int i = 0; i < THREAD_NUMBER; i++) {
		thread_ids[i] = i;
		if (0 != pthread_create(thread + i , NULL, count_sum, &thread_ids[i])) {
			perror("Could not create thread\n");
			return 1;
		}
	}

	printf("All threads are created\n");

	int isRight = 1;
	double *result;

	//getting sums from all the threads
	for (int i = 0; i < THREAD_NUMBER; i++) {
		if (0 != pthread_join(*(thread + i), (void *)(&result))) { // *retval should be double *
			perror("Could not join thread\n");
			return 1;
		}

		printf("One thread joined\n");

		if (NULL == result) {
			fprintf(stderr, "Thread returned nullptr\n");
			isRight = 0;
		}
		else {
			pi += *result;
			free(result);
			printf("pi is added\n");
		}
	}

	if ( isRight )  {
		printf("Pi is : %f\n",pi * 4.0);
		return 0;
	}
	else {
		printf("Could not count pi value\n");
		return 1;
	}

	return 0;
}