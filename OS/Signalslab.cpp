#define _XOPEN_SOURCE

#include <iostream>
#include <unistd.h>
#include <stdlib.h>
#include <getopt.h>
#include <string.h>
#include <ulimit.h>

#include <sys/resource.h>
#include <sys/time.h>

#define OPTIONS "ispucdvV:C:U:"

void print_userIDs() {
	std::cout << "User ID : " << getuid() << std::endl;
	std::cout << "Effective user ID : " << geteuid() << std::endl;
	std::cout << std::endl;
	std::cout << "Group ID : " << getgid() << std::endl;
	std::cout << "Effective group ID : " << getegid() << std::endl;
}

void print_processIDs() {
	std::cout << "Process ID :" << getpid() << std::endl;
	std::cout << "Process group ID : " << getpgid(0) << std::endl;
	std::cout << "Process parent ID : "<< getppid() << std::endl;
}

void makeProcessLeader() {
	if (-1 == setpgid(getpid(),getpid())){
		std::cerr << "Setting new process group ID was not successul" << std::endl;
	}
	else{
		std::cout << "The process " << getpid() << " is leader of the group" << std::endl;
	}
}

void getEnvVar() {
	std::cout << "The list of enviroment variables : " << std::endl;
	while (NULL != *environ){
		std::cout << "	" << *environ << std::endl;
		environ++;
	}
}

void setNewEnvVar() {
	int length = 0;
	std::cout << "Setting new Variable : " << optarg << std::endl;
	if ( putenv(optarg) ) {
		std::cerr << "Could not set EnvValue" << std::endl;
	}
}

void changeCoreDumpSize() {
	struct rlimit rlim;
	rlim.rlim_cur = 0; 
	rlim.rlim_max = atoi(optarg); 
	
	if (-1 != setrlimit(RLIMIT_CORE,&rlim)){
		std::cout << "Core Dump Max Size was changed to " << rlim.rlim_max << std::endl;
	}
	else{
		std::cerr << "Could not change core size" << std::endl;
	}
}
void getCoreDumpSize() {
	struct rlimit rlim;
	if (-1 != getrlimit(RLIMIT_CORE,&rlim)){
		std::cout << "Core Dump SOFT Limit is " << (long long)rlim.rlim_cur << std::endl;
		std::cout << "Core Dump HARD Limit is " << (long long)rlim.rlim_max << std::endl;
	}
	else{
		std::cerr << "Cannot find RLIMIT for Core file" << std::endl;
	}
}

void print_ULimit() {
	struct rlimit rlptr;
	if (-1 != getrlimit(RLIMIT_FSIZE,&rlptr)){
		std::cout << "File Size SOFT limit is " << (long long)rlptr.rlim_cur << std::endl;
		std::cout << "File Size HARD limit is " << (long long)rlptr.rlim_max << std::endl;
	}
	else{
		std::cerr << "Cannot find RLIMIT_FSIZE" << std::endl;
	}
}

void setULimit() {
	char *value1 = NULL;
	char *value2 = NULL;
	int len = 0;
	while ('_' != *optarg){
		len++;
		optarg++;
		if ('\0' == *optarg){
			std::cerr << "Not enough arguments" << std::endl;
			return;
		}
	}
	*optarg = '\0';
	value1 = optarg - len;
	value2 = optarg+1;
	
	std::cout << value1 << ' ' << value2 << std::endl;
	struct rlimit r;
	r.rlim_cur = atoll(value1);
	r.rlim_max = atoll(value2);
	if (-1 != setrlimit(RLIMIT_FSIZE,&r)){
		print_ULimit();
	}
	else{
		std::cerr << std::endl << "The value was not set" << std::endl;
	}

}

int main(int argc, char **argv) { 
	char result = 0;
	while ((result = getopt(argc,argv,OPTIONS)) != -1) {
		switch(result) {
			case 'C':
				changeCoreDumpSize();
				break;

			case 'V':
				setNewEnvVar();
				break;

			case 'i':
				print_userIDs();
				break;

			case 's':
				makeProcessLeader();
				break;

			case 'p':
				print_processIDs();
				break;

			case 'u':
				print_ULimit();
				break;

			case 'U':
				setULimit();
				break;

			case 'c':
				getCoreDumpSize();
				break;

			case 'd':
				std::cout << "Current directory is : " << getenv("PWD") << std::endl;
				break;

			case 'v':
				getEnvVar();
				break;	

			default:
				break;
		}
	}
	return 0;
}