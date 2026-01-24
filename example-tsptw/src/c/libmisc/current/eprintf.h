#ifndef _EPRINTF_H_
#define _EPRINTF_H_

#include "common.h"
__BEGIN_DECLS
extern char *program_invocation_short_name;

void eprintf(const char * str,...)
             __attribute__ ((format(printf, 1, 2))) __noreturn;
__END_DECLS
#endif
