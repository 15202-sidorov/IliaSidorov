#include <sys/types.h>
#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>

#include <wait.h>

#define PROGRAM_NAME "cat"
#define BIN_PATH "/bin/cat"
#define FILE_NAME "Lab9.c"
#define CHILD_PROCESS_ID 0

//pipes 
//filedescriptor

void Child_process_prototype() {
	if (-1 == execl(BIN_PATH,PROGRAM_NAME,FILE_NAME,NULL)) {
		perror("Could not execute the file\n");
		return;
	}
	return;
}

void Parent_process_prototype() {
	printf("Parent text\n");
	return;
}

int main(int argc, char **argv) {
	pid_t process_id = fork();

	if( -1 == process_id ) {
		perror("Could not create process\n");
		return EXIT_FAILURE;
	}

	//if child process, then process_id is set to 0
	if (CHILD_PROCESS_ID == process_id) {
		Child_process_prototype();
		//wait() waits for termination of the child process
		return EXIT_SUCCESS;
	}
	
	wait(NULL);
	Parent_process_prototype();

	return 0;
}