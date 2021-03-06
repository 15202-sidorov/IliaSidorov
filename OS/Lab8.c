#include <sys/types.h>
#include <fcntl.h>
#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>

#define FILENAME "SomeFile.txt"

int main(int argc, char **argv) {
	int fd = open(FILENAME,O_WRONLY);

	struct flock f = { 
					   .l_type = F_WRLCK,
			   		   .l_whence = SEEK_SET,
			   		   .l_start = 0,
		   	  		   .l_len = 0,
			   		   .l_pid = getpid()
					 };
	if (-1 == fcntl(fd,F_SETLKW,&f)) {
		perror("Could not lock file");
		return 1;
	}
	system("vim SomeFile.txt");
	close(fd);
	return 0;
}
