#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>
#include <fcntl.h>
#include <signal.h>

#define FileName "vimfileanalize.c"
#define MAXSTRING 200
#define TIME_OUT 5

int fd = 0;

typedef 
	struct Note {
		off_t location; //offset of the string in file
		int length; //length of the given string
	} Note;

void initNote(Note *new ,int location, int length) {
	new->location = location;
	new->length = length;
	return;
}

void createTable(Note *allNotes) {
	char inputChar = 0;
	int countLength = 0;
	int currentOffset = 0;
	int countOffset = 0;
	int line = 1;
	while (0 != read(fd,&inputChar,sizeof(char))) {
		countLength++;
		if ('\n' == inputChar) {
			initNote(allNotes + line,currentOffset,countLength);
			line++;
			countLength = 0;
			currentOffset = countOffset + 1;
		}
		countOffset++;
	}
}

void showString(int line, const Note *table) {
	if ((line >= MAXSTRING) || (line < 0)) {
		perror("Entered wrong line\n");
		return;
	}

	if (-1 == lseek(fd,table[line].location,0)) {
		perror("Could not move the offset\n");
		return;
	}

	char *resultString = (char *)malloc(table[line].length + 1);
	if (-1 == read(fd,resultString,table[line].length)) {
		perror("Could not read string\n");
		return;
	}

	resultString[table[line].length] = '\0';
	printf("%s",resultString);
	free(resultString);
}

void showEverything(int sig) {
	char outputChar = 0;
	if (-1 == lseek(fd,0,0)) {
		perror("Could not move to the start of the file\n");
		return;
	}
	while (0 != read(fd,&outputChar,sizeof(char))) {
		printf("%c",outputChar);
	}
	kill(getpid(),SIGALRM);
	return;
}

int main(int argc, char **argv) {
	fd = open(FileName,O_RDONLY);
	if (-1 == fd) {
		return 1;
	}
	Note table[MAXSTRING] = {0};
	
	createTable(table);

	int inputLine = 1;
	//signal(signum,disposition)
	if (signal(SIGALRM,showEverything) == SIG_ERR) {
		perror("Could not set the signal\n");
		return 1;
	}
	while (inputLine != 0) {
		alarm(TIME_OUT);
		scanf("%d",&inputLine);
		alarm(0);
		showString(inputLine,table);
	}

	close(fd);
	return 0;
}
