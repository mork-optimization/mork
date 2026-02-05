#include <stdio.h>
#include <stdlib.h>
#include <string.h>

static int 
parseLine(char* line)
{
    int i = strlen(line);
    while (*line < '0' || *line > '9') line++;
    line[i-3] = '\0';
    i = atoi(line);
    return i;
}

/* Value is in KB. */
static int
getValue(const char * type)
{ 
    FILE* file = fopen("/proc/self/status", "r");
    int result = -1;
    char line[256];
    
    while (fgets(line, 255, file) != NULL){
        if (strncmp(line, type, strlen(type)) == 0) {
            result = parseLine(line);
            break;
        }
    }
    fclose(file);
    return result;
}

int
getVmSize(void)
{
    return getValue ("VmSize:");
}

int
getVmRSS(void)
{
    return getValue ("VmRSS:");
}

