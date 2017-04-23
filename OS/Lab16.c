#include <stdlib.h>
#include <stdio.h>
#include <signal.h>
#include <termios.h>
#include <unistd.h>

int main(int argc, char **argv) {

	struct termios term_settings;
	struct termios save_settings;
	tcgetattr(fileno(stdin),&save_settings);
	term_settings = save_settings;
	term_settings.c_lflag &= ~(ICANON | ECHO);
	tcsetattr(fileno(stdin),TCSANOW,&term_settings);
	printf("Enter one charachter\n");
	char a = getchar();
	printf("You've entered %c\n",a);

	tcsetattr(fileno(stdin),TCSANOW,&save_settings);
	return 0;
}