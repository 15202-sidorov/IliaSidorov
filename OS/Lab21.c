#include <stdlib.h>
#include <stdio.h>
#include <signal.h>
#include <termios.h>
#include <unistd.h>

#define ASCII_DELETE 127
#define ASCII_BEEP 7
#define ITARATION_TIME 10

int sum = 0;
struct termios term_settings;
struct termios save_settings;

void sig_handle(int sig) {

	if (SIGQUIT == sig) {
		
		printf("Quit caught %d\n",sum);
		tcsetattr(fileno(stdin),TCSANOW,&save_settings);
		exit(0);
	}

	else if (SIGINT == sig) {
		sum++;
		printf("SIGINT is caught\n");
		return;
	}
	return;
}
	
int main(int argc, char **argv) {
	signal(SIGINT,sig_handle);
	signal(SIGQUIT,sig_handle);
	tcgetattr(fileno(stdin),&save_settings);
	term_settings = save_settings;
	term_settings.c_lflag &= ~ECHO;
	term_settings.c_cc[VINTR] = ASCII_DELETE;
	tcsetattr(fileno(stdin),TCSANOW,&term_settings);
	while (1) {
		sleep(ITARATION_TIME);
	}
	return 0;
}