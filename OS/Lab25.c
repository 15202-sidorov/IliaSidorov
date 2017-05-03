#include <unistd.h>
#include <fcntl.h>
#include <stdlib.h>
#include <stdio.h>
#include <ctype.h>
#include <string.h>

#define BUF_SIZE 64

void writeProcess(int writefd) {
	char toGoBuffer[BUF_SIZE];
	int returnVal = 1;
	while (NULL != fgets(toGoBuffer,BUF_SIZE,stdin)) {
		returnVal = write(writefd,toGoBuffer,BUF_SIZE);
		if (-1 == returnVal) {
			perror("Could not write to stream");
			return;
		}	
	}
	return;
}

void readProcess(int readfd) {
	char recievedBuffer[BUF_SIZE];
	int returnVal = 1;
	while (1) {
		returnVal = read(readfd,recievedBuffer,BUF_SIZE);
		if (-1 == returnVal) {
			perror("Could not read from stream\n");
			return;
		}
		if (0 == returnVal) {
			return;
		}
		
		for (int i = 0; i < strlen(recievedBuffer); i++) {
			printf("%c",toupper(recievedBuffer[i]));
		}
	}
	
	return;
}


int main() {

	int fds[] = {0, 0};
	//after that file descriptors 
	//are pointing to the write and read ends of the pipe
	//after fork all file descriptors are inherited
	if (-1 == pipe(fds)){
		perror("Could not create pipe");
		return 1;
	}
	
	//create two processes
	pid_t child_pid = fork();
	if (-1 == child_pid) {
		perror("Could not create new process");
		return 2;
	}

	if (0 == child_pid) {
		close(fds[0]);
		writeProcess(fds[1]);
		close(fds[1]);
	}
	else {
		close(fds[1]);
		readProcess(fds[0]);
		close(fds[0]);
	}

	return 0;
}