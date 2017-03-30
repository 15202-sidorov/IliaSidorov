#include <stdlib.h>
#include <stdio.h>
#include <wait.h>
#include <unistd.h>
#include <sys/types.h>
#include <signal.h>

//should add printEvn to the PATH variable

#define CHILD_PROCESS_ID 0

int Child_process(char **argv) {
	//call the programm with arguments
	if (-1 == execvp(argv[1],argv + 1)) {
		perror("Could not execute execvp");
		return 1;
	}
	return 0;
}

int main(int argc, char **argv) {
	if (argc < 2) {
		fprintf(stderr,"Not enough arguments\n");
		return 1;
	}
	pid_t process_id = fork();

	if (-1 == process_id) {
		perror("Could not create a child process\n");
		return EXIT_FAILURE;
	}

	if (CHILD_PROCESS_ID == process_id) {
		//signal(SIGALRM,SIG_DFL);
		//kill(getpid(),SIGALRM);
		return Child_process(argv);
	}

	int status = 0;
	wait(&status);
	if (WIFEXITED(status)) {
		printf("Child process exited with : %d\n",WEXITSTATUS(status));
	}
	else if (WIFSIGNALED(status)) {
		printf("Child process terminated by signal: %d\n",WTERMSIG(status));
	}
	
	//three variant 0,1, killed + signal, and which signal

	return EXIT_SUCCESS;
}