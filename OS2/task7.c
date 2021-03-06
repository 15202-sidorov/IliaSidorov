#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>

long THREAD_NUMBER = 0;

void *count_sum( void * arg ) {
	double *result = (double *)calloc(1,sizeof(double));
	int thread_id = *(int *)arg;
	long start = ITARATIONS_NUMBER * thread_id;
	long end = start + ITARATIONS_PER_THREAD;
	for (long i = start; i < end; i++) {
		result[0] += 1.0/(i*4.0 + 1.0);
		result[0] -= 1.0/(i*4.0 + 3.0);
	}

	pthread_exit(result);
}


int main( int argc, char **argv ) {
	double pi = 0;
	THREAD_NUMBER = atoi(argv[1]);
	pthread_t thread[THREAD_NUMBER];
	int thread_ids[THREAD_NUMBER];
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