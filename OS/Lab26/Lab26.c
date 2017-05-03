#include <unistd.h>
#include <fcntl.h>
#include <stdlib.h>
#include <stdio.h>
#include <ctype.h>
#include <wait.h>

#define READ_PATH "readstream.out"
#define BUF_SIZE 64

//should include READ_PATH in PATH variable before running

void writeProcess(FILE *writeStream) {
	char toGoBuffer[BUF_SIZE];
	while (NULL != fgets(toGoBuffer,BUF_SIZE,stdin)) {
		if (-1 == write(fileno(writeStream),toGoBuffer,BUF_SIZE)) {
			perror("Could not write to file");
			return;
		}
	}
	return;
}


int main() {
	FILE *writeStream = popen(READ_PATH,"w");
	writeProcess(writeStream);
	pclose(writeStream);
	return 0;
}