#include <unistd.h>
#include <fcntl.h>
#include <stdlib.h>
#include <stdio.h>
#include <ctype.h>

#define MAX_STRING_SIZE 50

void writeProcess(int writefd) {
	char toGoString[MAX_STRING_SIZE];
	printf("Enter the line you'd like to pass : \n");
	fgets(toGoString,sizeof(toGoString),stdin);
	if (-1 == write(writefd,toGoString,MAX_STRING_SIZE)){
		perror("Could not write to stream");
		return;
	}
	return;
}

void readProcess(int readfd) {
	char recievedString[MAX_STRING_SIZE];
	printf("Another process waits for the string to read\n");
	if (-1 == read(readfd,recievedString,sizeof(recievedString))) {
		perror("Could not read from stream\n");
		return;
	}
	printf("Recieved successfuly\n");
	printf("Upper case : ");
	int i = 0;
	while ((i < MAX_STRING_SIZE) && (recievedString[i] != '\n')) {
		recievedString[i] = toupper(recievedString[i]);
		printf("%c",recievedString[i]);
		i++;
	}
	printf("\n");
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
		writeProcess(fds[1]);
	}
	else {
		readProcess(fds[0]);
	}
	
	close(fds[0]);
	close(fds[1]);
}
