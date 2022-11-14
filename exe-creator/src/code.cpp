#include <tchar.h>
#include <windows.h>
#include <string>
#include <stdio.h>

std::string GetExeFileName() {
	char buffer[MAX_PATH];
	GetModuleFileName( NULL, buffer, MAX_PATH );
	return std::string(buffer);
}

std::string GetExePath() {
	std::string f = GetExeFileName();
	return f.substr(0, f.find_last_of("\\/"));
}

int APIENTRY _tWinMain(HINSTANCE hInstance, HINSTANCE hPrevInstance,LPTSTR lpCmdLine, int nCmdShow) {   
    STARTUPINFO si;
    PROCESS_INFORMATION pi;

    ZeroMemory(&si, sizeof(si));
    si.cb = sizeof(si);
    ZeroMemory(&pi, sizeof(pi));
	std::string command = "";
	std::string javaLocation = "javaw.exe";
	FILE *file;
	if (file = fopen("SnipSniper\\jdk\\bin\\javaw.exe", "r")) {
		javaLocation = GetExePath() + "\\SnipSniper\\jdk\\bin\\javaw.exe -Dhttps.protocols=TLSv1.2";
		fclose(file);
	}
	command += javaLocation;

#ifdef WIN
    command += " -Dplatform=win";
#endif
#ifdef WIN_INSTALLED
    command += " -Dplatform=win_installed";
#endif

#ifdef NORMAL
	command += " -jar SnipSniper.jar";
#endif
#ifdef EDITOR
	command += " -jar SnipSniper.jar -editor";
#endif

#ifdef VIEWER
	command += " -jar SnipSniper.jar -viewer";
#endif

	for(int i = 1; i < __argc; i++) {
		command += " ";
		command += __argv[i];
	}

    char cmdline[command.length() + 1];
	strcpy(cmdline, command.c_str());
	
	std::string pathString = GetExePath();
	char path[pathString.length() + 1];
	strcpy(path, pathString.c_str());
	
	HANDLE ghJob = CreateJobObject(NULL, NULL);
	if (ghJob != NULL) {
		JOBOBJECT_EXTENDED_LIMIT_INFORMATION jeli = {0};
		//Child processes should be closed with parent process
		jeli.BasicLimitInformation.LimitFlags = JOB_OBJECT_LIMIT_KILL_ON_JOB_CLOSE;
		SetInformationJobObject(ghJob, JobObjectExtendedLimitInformation, &jeli, sizeof(jeli));
	}
	
    if(!CreateProcess(NULL,  	// No module name (use command line)
        cmdline,              	// Command line
        NULL,             		// Process handle not inheritable
        NULL,             		// Thread handle not inheritable
        FALSE,            		// Set handle inheritance to FALSE
        CREATE_NO_WINDOW, 		// No creation flags
        NULL,           		// Use parent's environment block
        path,           		// Use parent's starting directory 
        &si,         			// Pointer to STARTUPINFO structure
        &pi)            		// Pointer to PROCESS_INFORMATION structure
    ) {
        printf( "CreateProcess failed (%d).\n", GetLastError() );
        return 1;
    }
	AssignProcessToJobObject(ghJob, pi.hProcess);
	ShowWindow(GetConsoleWindow(), SW_HIDE);

    // Wait until child process exits.
    WaitForSingleObject(pi.hProcess, INFINITE);

    // Close process and thread handles. 
    CloseHandle(pi.hProcess);
    CloseHandle(pi.hThread);
    return 0;
}