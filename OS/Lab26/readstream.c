#include <unistd.h>
#include <fcntl.h>
#include <stdlib.h>
#include <stdio.h>
#include <ctype.h>
#include <string.h>

#define BUF_SIZE 64

int main() {
	char recieveBuffer[BUF_SIZE];
	int fd = fileno(stdin);
	int returnVal = 0;
	while (1) {
		returnVal = read(fd,recieveBuffer,BUF_SIZE);
		if (-1 == returnVal) {
			perror("Could not read buffer");
			return 1;
		}
		if (0 == returnVal) {
			break;
		}

		for (int i = 0; i < strlen(recieveBuffer); i++) {
			printf("%c",toupper(recieveBuffer[i]));
		}
	}
	return 0;
}