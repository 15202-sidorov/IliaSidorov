#include <sys/types.h>
#include <fcntl.h>
#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>

#define FILENAME "SomeFile.txt"

int main(int argc, char **argv) {
	int fd = open(FILENAME,O_RDONLY);

	struct flock f = { .l_type = FWRLCK,
			   		   .l_whence = SEEK_SET,
			   		   .l_start = 0,
		   	  		   .l_len = 0,
			   		   .l_pid = 
			 }
	return 0;
}
